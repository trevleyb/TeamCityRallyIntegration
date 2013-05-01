package com.rally.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.RallyConnector;
import jetbrains.buildServer.controllers.*;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RallySettingsController extends BaseFormXmlController implements CustomTab {

    private static final Logger LOG = Logger.getInstance(RallySettingsController.class.getName());

    public static final String PAGE_URL = "/plugins/RallyTeamCityIntegrator/editSettings.html";
    private static final String SETTINGS_BEAN_KEY = "settingsBean";
    private static final String FILE_NAME = "editSettings.jsp";
    private static final String TAB_TITLE = "Rally Integrator";
    private static final String TAB_ID = "RallyTeamCityIntegrator";

    private PluginDescriptor descriptor;
    private FileConfig myRallyIntegratorConfig;
    private RallyConnector connector;
    private WebControllerManager webControllerManager;
    protected final PagePlaces myPagePlaces;
    private PlaceId myPlaceId;

    public RallySettingsController(RallyConnector connector, PagePlaces places, WebControllerManager webControllerManager,
                                   PluginDescriptor descriptor, ServerPaths serverPaths) {

        myRallyIntegratorConfig = new FileConfig(serverPaths.getConfigDir());
        this.descriptor = descriptor;
        this.connector = connector;
        this.webControllerManager = webControllerManager;
        this.myPagePlaces = places;
        this.myPlaceId = PlaceId.ADMIN_SERVER_CONFIGURATION_TAB;

        register();
    }

    protected void register() {
        myPagePlaces.getPlaceById(myPlaceId).addExtension(this);
        webControllerManager.registerController(PAGE_URL, this);
    }

    protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        RememberState bean = createSettingsBean();
        ModelAndView view = new ModelAndView(descriptor.getPluginResourcesPath() + FILE_NAME);
        view.getModel().put(SETTINGS_BEAN_KEY, bean);
        return view;
    }

    private SettingsBean createSettingsBean() {
        return new SettingsBean(myRallyIntegratorConfig);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {
        if (PublicKeyUtil.isPublicKeyExpired(request)) {
            PublicKeyUtil.writePublicKeyExpiredError(xmlResponse);
            return;
        }
        SettingsBean bean = getSettingsBean(request);
        FormUtil.bindFromRequest(request, bean);
        if (isStoreInSessionRequest(request)) {
            XmlResponseUtil.writeFormModifiedIfNeeded(xmlResponse, bean);
            return;
        }
        ActionErrors errors = validate(bean);
        if (!errors.hasNoErrors()) {
            writeErrors(xmlResponse, errors);
            return;
        }

        String testConnectionResult = testSettings(bean);
        if (isTestConnectionRequest(request)) {
            LOG.info("RallySettingsController.TestConnectionRequest");
            XmlResponseUtil.writeTestResult(xmlResponse, testConnectionResult);
        } else {
            LOG.info("RallySettingsController.SaveRequest");
            if (testConnectionResult == null) {
                saveSettings(bean);
                FormUtil.removeFromSession(request.getSession(), bean.getClass());
                writeRedirect(xmlResponse, (request.getContextPath() + "admin.html?item=" + getTabId()));
            } else {
                errors.addError("invalidConnection", testConnectionResult);
                writeErrors(xmlResponse, errors);
            }
        }
    }

    protected final boolean isStoreInSessionRequest(HttpServletRequest request) {
        return "storeInSession".equals(request.getParameter("submitSettings"));
    }

    protected final boolean isTestConnectionRequest(HttpServletRequest request) {
        return "testConnection".equals(request.getParameter("submitSettings"));
    }

    protected SettingsBean getSettingsBean(HttpServletRequest request) {
        final SettingsBean bean = createSettingsBean();
        return FormUtil.getOrCreateForm(request, (Class<SettingsBean>) bean.getClass(),
                new FormUtil.FormCreator<SettingsBean>() {
                    public SettingsBean createForm(HttpServletRequest request) {
                        return bean;
                    }
                });
    }

    protected void saveSettings(SettingsBean bean) {
        LOG.info("Saving Rally Settings.");
        copySettings(bean, myRallyIntegratorConfig);
        myRallyIntegratorConfig.save();
        try {
            connector.disconnect();
        } catch (Exception e) {
            LOG.error("Failed to save Rally settings." + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void copySettings(SettingsBean bean, com.rally.integration.rally.RallyConfig target) {
        target.setUrl(bean.getUrl());
        target.setUserName(bean.getUserName());
        target.setPassword(bean.getPassword());
        target.setProxyUsed(getBooleanByString(bean.getProxyUsed().toString()));
        target.setProxyUri(bean.getProxyUri());
        target.setProxyUsername(bean.getProxyUsername());
        target.setProxyPassword(bean.getProxyPassword());
        target.setTestOnly(bean.getTestOnly());
        target.setCreateNotExist(bean.getCreateNotExist());
    }

    private static Boolean getBooleanByString(String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ex) {
            return false;
        }
    }

    public ActionErrors validate(SettingsBean bean) {
        ActionErrors errors = new ActionErrors();
        if (StringUtil.isEmptyOrSpaces(bean.getUrl())) {
            errors.addError("emptyUrl", "Rally Server URL is required.");
        } else try {
            new URL(bean.getUrl());
        } catch (MalformedURLException e) {
            errors.addError("invalidUrl", "Invalid server URL format.");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getUserName())) {
            errors.addError("emptyUserName", "User name is required.");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getPassword())) {
            errors.addError("emptyPassword", "Password is required.");
        }
        if (bean.getProxyUsed() && StringUtil.isEmptyOrSpaces(bean.getProxyUri())) {
            errors.addError("onEmptyProxyUriError", "Proxy URI is required.");
        } else if (bean.getProxyUsed()) {
            try {
                new URL(bean.getProxyUri());
            } catch (MalformedURLException e) {
                errors.addError("onInvalidProxyUriError", "Invalid proxy URI format.");
            }
        }
        return errors;
    }

    public String testSettings(SettingsBean bean)  {
        RallyConnector testConnector = createConnectorToRally(bean);
        testConnector.disconnect();
        if (!testConnector.isConnectionValid()) {
            return "Connection not valid.";
        }
        return null;
    }

    protected RallyConnector createConnectorToRally(SettingsBean bean) {
        final FileConfig testConfig = new FileConfig(bean);
        RallyConnector testConnector = new RallyConnector();
        try {
            testConnector.setConnectionSettings(testConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testConnector;
    }

    @NotNull
    public String getTabId() { return TAB_ID; }

    @NotNull
    public String getTabTitle() { return TAB_TITLE; }

    @NotNull
    public String getIncludeUrl() { return PAGE_URL; }

    @NotNull
    public String getPluginName() { return RallyServerListener.PLUGIN_NAME; }

    @NotNull
    public List<String> getCssPaths() { return new ArrayList<String>(); }

    @NotNull
    public List<String> getJsPaths() { return new ArrayList<String>(); }

    public boolean isAvailable(@NotNull final HttpServletRequest request) { return true; }

    public boolean isVisible() { return true;  }

    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) { }
}