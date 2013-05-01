<%@ page import="com.rally.integration.teamcity.RallySettingsController" %>
<%@include file="/include.jsp"%>

<!--<jsp:useBean id="settingsBean" scope="request" type="com.rally.integration.teamcity.SettingsBean"/>-->
<h2>Rally Integration Settings</h2>
<p>
  Server URL: <strong><c:out value="${settingsBean.url}"/></strong>
</p>
<p>
  User name: <strong><c:out value="${settingsBean.userName}"/></strong>
</p>
<p>
  Use proxy: <strong><c:out value="${settingsBean.proxyUsed}"/></strong>
</p>
<p>
  Proxy Uri: <strong><c:out value="${settingsBean.proxyUri}"/></strong>
</p>
<p>
  Proxy Username: <strong><c:out value="${settingsBean.proxyUsername}"/></strong>
</p>
<p>
  Test only: <strong><c:out value="${settingsBean.testOnly}"/></strong>
</p>
<p>
  Create BuildDef: <strong><c:out value="${settingsBean.createNotExist}"/></strong>
</p>
<% String editUrl = RallySettingsController.EDIT_SETTINGS_URL + "?init=1"; %>
<p>
  <a href="<c:url value='<%=editUrl%>'/>">Edit settings &raquo;</a>
</p>
<br clear="all"/>
