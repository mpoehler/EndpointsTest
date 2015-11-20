
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
function init(apiRoot) {
    // Loads the OAuth and helloworld APIs asynchronously, and triggers login
    // when they have completed.
    var apisToLoad;
    var callback = function() {
        if (--apisToLoad == 0) {
            console.log();
        }
    }

    apisToLoad = 1; // must match number of calls to gapi.client.load()
    gapi.client.load('helloworld', 'v1', callback, apiRoot);
};