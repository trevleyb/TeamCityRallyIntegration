package com.rally.integration.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.entities.RallyRepository;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.*;
import com.rallydev.rest.response.*;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import jetbrains.buildServer.issueTracker.errors.ConnectionException;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RallyConnector {

    private static final Logger LOG = Logger.getInstance(RallyConnector.class.getName());
    private static RallyRestApi rallyInstance = null;
    private static RallySubscription rallySubscription = null;
    protected com.rally.integration.rally.RallyConfig config;

    /**
     * Checks to see if there is a valid connection to Rally.
     * Can't just connect, must issue a query to validate the connection.
     *
     * @return true if there is a valid connection to Rally, otherwise false.
     */
    public boolean isConnectionValid() {
        try {
            return getRallyInstance() != null;
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
    }

    /**
     * Provides access to an instance of a connection to Rally.
     *
     * @return RallyRestAPI instance and will create that instance if it does not exist.
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public RallyRestApi getRallyInstance() throws AuthenticationException, URISyntaxException {
        if (rallyInstance == null) rallyInstance = connect();
        return rallyInstance;
    }

    public RallySubscription getSubscription() throws Exception {
        if (rallySubscription == null) rallySubscription = buildSubscriptionInfo();
        return rallySubscription;
    }

    /**
     * Connects to rally, sets the instance variable to point to the new instance and returns
     * a instance of the connection object for further calls to Rally.
     *
     * @return RallyRestAPI instance and will create that instance if it does not exist.
     * @throws AuthenticationException
     * @throws URISyntaxException
     * @throws ConnectionException
     */
    protected RallyRestApi connect() throws AuthenticationException, URISyntaxException, ConnectionException {

        LOG.info("Connecting to Rally through '" + config.getUrl() + '\'');
        if (config.getUserName() == null || config.getPassword() == null) {
            LOG.error("No username or password was provided. Cannot connect.");
            throw new AuthenticationException("No username or password provided. Proxy not currently implemented.");
        }

        try {
            LOG.info("Connecting as: " + config.getUserName());
            RallyRestApi instance = new RallyRestApi(new URI(config.getUrl()), config.getUserName(), config.getPassword());
            instance.setWsapiVersion(new String("1.42"));
            instance.setApplicationName("TeamCity to Rally Integrator");
            instance.setApplicationVendor("OpenSource");
            LOG.info("Connected: " + instance.getWsapiVersion());
            return instance;
        } catch (Exception e) {
            LOG.error("Failed to connect: " + e.getMessage());
            throw new ConnectionException("Could not connect to Rally. ", e);
        }
    }

    protected RallySubscription buildSubscriptionInfo() throws Exception {

        LOG.info("Building subscription and SCM repository data.");
        try {
            return new RallySubscription(getRallySubscription(), getSCMRepositories());
        } catch (Exception e) {
            LOG.error("Could not build Workspace and Projects list from Subscription data.");
            LOG.error(e);
            throw e;
        }
    }

    private JsonObject getRallySubscription() throws Exception {
        try {
            LOG.info("Getting Subscription data from Rally.");
            QueryRequest request = new QueryRequest("Subscription");
            request.setFetch(new Fetch("Workspaces,Name,Projects,BuildDefinitions"));
            QueryResponse response = getRallyInstance().query(request);
            if (response.wasSuccessful()) {
                LOG.info("Obtained Subscription Data: Response Count=" + response.getTotalResultCount());
                return response.getResults().get(0).getAsJsonObject();
            } else {
                LOG.error("Could not obtain subscription information from Rally.");
                LOG.error(response.getErrors().toString());
                throw new ConnectionException("Could not connect to Rally and get Subscription data.");
            }
        } catch (Exception e) {
            LOG.error("Could not obtain subscription information from Rally.");
            LOG.error(e);
            throw new ConnectionException("Could not connect to Rally and get Subscription data.");
        }
    }

    private JsonArray getSCMRepositories() throws Exception {
        LOG.info("Getting SCM Repository Data from Rally.");
        QueryRequest request = new QueryRequest("SCMRepository");
        request.setFetch(new Fetch("Name,Description,SCMType,Projects"));
        QueryResponse response = getRallyInstance().query(request);
        if (response.wasSuccessful()) {
            LOG.info("Obtained Repository Data: Response Count=" + response.getTotalResultCount());
            return response.getResults();
        } else {
            LOG.error("Could not obtain SCMRepository information from Rally.");
            LOG.error(response.getErrors().toString());
            throw new ConnectionException("Could not connect to Rally and get SCMRepository data.");
        }
    }

    public String FindChangeSet(String scmName, String revision) {
        LOG.info("Looking for change set: " + scmName + ":" + revision);
        try {
            RallyRepository repository = getSubscription().FindRepository(scmName);
            if (repository != null) {
                QueryRequest request = new QueryRequest("ChangeSet");
                QueryFilter filter = new QueryFilter("SCMRepository", "=", repository.getRef()).and(new QueryFilter("Revision", "=", revision));
                request.setFetch(new Fetch("Name,Revision"));
                request.setQueryFilter(filter);
                QueryResponse response = getRallyInstance().query(request);
                if (response.wasSuccessful() && response.getTotalResultCount() > 0) {
                    LOG.info("Found change set. Response Count=" + response.getTotalResultCount());
                    return response.getResults().get(0).getAsJsonObject().get("_ref").getAsString();
                } else {
                    LOG.warn("Could not obtain ChangeSet data for: " + scmName + " : " + revision);
                    LOG.warn(response.getErrors().toString());
                }
            }
        } catch (Exception e) {
            LOG.error("Error finding change set. ");
            LOG.error(e);
        }
        return null;
    }

    public String Create(String type, JsonObject obj) {
        LOG.info("Creating: " + type);
        try {
            CreateRequest request = new CreateRequest(type, obj);
            CreateResponse response = getRallyInstance().create(request);
            if (response.wasSuccessful()) return response.getObject().get("_ref").getAsString();

            LOG.error("Could not create object of type: " + type);
            LOG.info(request.getBody());
            logErrors(response.getErrors());
            logWarnings(response.getWarnings());
        } catch (Exception e) {
            LOG.error("Could not create object of type: " + type, e);
        }
        return null;
    }

    public void Update(String ref, JsonObject obj) {
        LOG.info("Updating: " + ref);
        try {
            UpdateRequest request = new UpdateRequest(ref, obj);
            UpdateResponse response = getRallyInstance().update(request);
            if (!response.wasSuccessful()) {
                LOG.error("Could not update object: " + ref);
                LOG.info(request.getBody());
                logErrors(response.getErrors());
                logWarnings(response.getWarnings());
            }
        } catch (Exception e) {
            LOG.error("Could not update object: " + ref, e);
        }
    }

    public JsonObject Get(String ref) {
        return Get(ref, null);
    }

    public JsonObject Get(String ref, String[] fields) {
        LOG.info("Getting: " + ref);
        try {
            GetRequest request = new GetRequest(ref);
            if (fields != null && fields.length > 0) request.setFetch(new Fetch(fields));
            GetResponse response = getRallyInstance().get(request);
            if (!response.wasSuccessful()) {
                LOG.error("Could not get object: " + ref);
                logErrors(response.getErrors());
                logWarnings(response.getWarnings());
            }
            return response.getObject();
        } catch (Exception e) {
            LOG.error("Could not get object: " + ref, e);
        }
        return null;
    }

    public void Delete(String ref, JsonObject obj) {
        LOG.info("Deleting: " + ref);
        try {
            DeleteRequest request = new DeleteRequest(ref);
            DeleteResponse response = getRallyInstance().delete(request);
            if (!response.wasSuccessful()) {
                LOG.error("Could not delete object: " + ref);
                logErrors(response.getErrors());
                logWarnings(response.getWarnings());
            }
        } catch (Exception e) {
            LOG.error("Could not delete object: " + ref, e);
        }
    }

    public void logErrors(String[] messages) {
        if (messages != null && messages.length > 0) {
            for (int i = 0; i < messages.length; i++) LOG.error(messages[i]);
        }
    }

    public void logWarnings(String[] messages) {
        if (messages != null && messages.length > 0) {
            for (int i = 0; i < messages.length; i++) LOG.warn(messages[i]);
        }
    }

    public void disconnect() {
        LOG.info("Disconnecting from Rally.");
        try {
            if (rallyInstance != null) rallyInstance.close();
        } catch (Exception e) { /* ignore if there is an error. */ }
        rallyInstance = null;
        rallySubscription = null;
    }

    public void setConnectionSettings(com.rally.integration.rally.RallyConfig config) throws IOException {
        disconnect();
        this.config = config;
    }
}
