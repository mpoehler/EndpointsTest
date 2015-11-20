<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <title>Endpoint-Test</title>
  </head>
  <body>
      User (from UserServiceFactory): <%=UserServiceFactory.getUserService().getCurrentUser()%><br/>
      User (from request.getUserPrincipal): <%=request.getUserPrincipal()%>
      <ul>
          <li><a href="/login">Login</a></li>
          <li><a href="/logout">Logout</a></li>
          <li><a href="https://endpoints-test-970.appspot.com/_ah/api/explorer">API-Explorer</a></li>
          <li><a href="#" onclick="auth();">Login OAuth</a></li>
      </ul>
      <div id="log"></div>
      <script type="text/javascript">

          function init() {
              console.log("function init called.");
              // Loads the OAuth and helloworld APIs asynchronously, and triggers login
              // when they have completed.
              var apisToLoad;
              var callback = function () {
                  if (--apisToLoad == 0) {
                      console.log("all apis loaded");
                      signin(true, userAuthed);
                  }
              }

              apisToLoad = 2; // must match number of calls to gapi.client.load()
              gapi.client.load('endpointstest', 'v1', callback, '//' + window.location.host + '/_ah/api');
              gapi.client.load('oauth2', 'v2', callback);
          }
          function signin(mode, authorizeCallback) {
              console.log("function signin called");
              gapi.auth.authorize({
                  client_id: '894185615170-8f7h45jj25e310smph5do6lglnbtnggm.apps.googleusercontent.com',
                  scope: ['https://www.googleapis.com/auth/userinfo.email'],
                  immediate: mode
              }, authorizeCallback);
          }

          function userAuthed() {
              console.log("function userAuthed called");
              var request =
                      gapi.client.oauth2.userinfo.get().execute(function(resp) {
                          if (!resp.code) {
                              console.log("User is signed in, call my Endpoint");
                              gapi.client.endpointstest.oauth.user().execute(function(resp) {
                                  console.log("result from function: " + resp);
                              });

                              console.log("now call the account list");
                              gapi.client.endpointstest.adwords.listaccounts().execute(function(resp) {
                                  console.log("result from adwordslist: " + resp);
                              });

                          }
                          console.log(resp);
                      });
          }

          function auth() {
              signin(false, userAuthed);
          };
      </script>
      <script src="https://apis.google.com/js/client.js?onload=init">
      </script>
  </body>
</html>
