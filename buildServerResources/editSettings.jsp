
<%@ include file="/include.jsp" %>

<jsp:useBean id="settingsBean" scope="request" class="com.rally.integration.teamcity.SettingsBean"/>

  <style type="text/css">
    @import "<c:url value='/plugins/${settingsBean.PLUGIN_NAME}/css/RallySettings.css'/>";
  </style>
  <bs:linkScript>
    /js/crypt/rsa.js
    /js/crypt/jsbn.js
    /js/crypt/prng4.js
    /js/crypt/rng.js
    /js/bs/forms.js
    /js/bs/modalDialog.js
    /js/bs/testConnection.js
    /js/bs/encrypt.js
    /plugins/${settingsBean.PLUGIN_NAME}/js/editSettings.js
  </bs:linkScript>
  <script type="text/javascript">
    Behaviour.addLoadEvent(function() {
      Rally.SettingsForm.setupEventHandlers();
      $('url').focus();
      Rally.SettingsForm.changeStatusProxy();
    });
  </script>

  <div id="container">
    <form action="<c:url value='${settingsBean.PAGE_URL}?edit=1'/>" method="post" onsubmit="return Rally.SettingsForm.submitSettings()" autocomplete="off">
    <div class="editIntegratorSettingsPage">

      <bs:messages key="settingsSaved"/>
    <table class="runnerFormTable">
      <tr>
      <th><label for="url">Server URL (https): <l:star/></label></th>
      <td><forms:textField name="url" value="${settingsBean.url}"/>
        <span class="error" id="errorUrl"></span></td>
      </tr>

      <tr>
      <th><label for="userName">Server user: <l:star/></label></th>
      <td><forms:textField name="userName" value="${settingsBean.userName}"/>
        <span class="error" id="errorUserName"></span></td>
      </tr>

      <tr>
      <th><label for="password">Server user password: <l:star/></label></th>
      <td><forms:passwordField name="password" encryptedPassword="${settingsBean.encryptedPassword}"/>
        <span class="error" id="errorPassword"></span></td>
      </tr>

      <tr>
      <th><label disabled="true" for="proxyUsed">Use proxy: </label></th>
      <td><forms:checkbox  name="proxyUsed" disabled="true" value="true" checked="${settingsBean.proxyUsed}" onclick="Rally.SettingsForm.changeStatusProxy()"/></td>
      </tr>

      <tr>
      <th><label disabled="true" for="proxyUri">Proxy URI: <l:star/></label></th>
      <td><forms:textField name="proxyUri" value="${settingsBean.proxyUri}" disabled="true"/>
        <span class="error" id="errorProxyUri"></span></td>
      </tr>

      <tr>
      <th><label disabled="true" for="proxyUsername">Proxy user: </label> </th>
      <td><forms:textField name="proxyUsername" value="${settingsBean.proxyUsername}" disabled="true"/></td>
      </tr>

      <tr>
      <th><label disabled="true" for="proxyPassword">Proxy password: </label></th>
      <td><forms:passwordField name="proxyPassword" encryptedPassword="${settingsBean.encryptedProxyPassword}" disabled="true"/></td>
      </tr>

      <tr>
      <th><label for="testOnly">Test only: </label></th>
      <td><forms:checkbox  name="testOnly" disabled="false" value="true" checked="${settingsBean.testOnly}"/></td>
      </tr>

      <tr>
      <th><label for="createNotExist">Create BuildDef : </label></th>
      <td><forms:checkbox  name="createNotExist" disabled="false" value="false" checked="${settingsBean.createNotExist}"/></td>
      </tr>

      <tr>
      <th>Note:</th>
      <td>Ensure that you define RallyWorkspace,RallyProject,RallyBuildDef and RallySCM properties for each project. These properties are case sensitive.</p>
       <b>RallyWorkspace</b> defines which workspace to find the buildDef. </p>
       <b>RallyProject</b> defines te project for the build def. If omitted, the first match will be used.</p>
       <b>RallyBuildDef</b> is the build def to assign the build to. If only RallyBuildDef is defined the first found match by name will be used.</p>
       <b>RallySCM</b> is the key for the SCMRepository for change sets.</p>
       If <b>Create BuildDef</b> = <i>true</i> and you provide a Workspace, Project and Build name that does not exist, a build definition will be created.</td>
      </tr>
    </table>

      <div class="saveButtonsBlock">
        <span class="error" id="errorInvalidCredentials"></span>
        <input class="btn btn_primary submitButton" class="submitButton" type="submit" value="Save">
        <input class="btn btn_primary submitButton" class="submitButton" id="testConnection" type="button" value="Test connection"/>
        <input type="hidden" id="submitSettings" name="submitSettings" value="store"/>
        <input type="hidden" id="testAddress" name="testAddress" value=""/>
        <input type="hidden" id="publicKey" name="publicKey" value="<c:out value='${settingsBean.hexEncodedPublicKey}'/>"/>
        <forms:saving/>
      </div>

    </div>
    </form>
  </div>

  <bs:dialog dialogId="testConnectionDialog" title="Test Connection" closeCommand="BS.TestConnectionDialog.close();" closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails"></div>
  </bs:dialog>

  <forms:modified/>




