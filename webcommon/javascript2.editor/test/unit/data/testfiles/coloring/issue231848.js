function MyApplication() {
    var self = this;
    var connectionLess = ["undefinedAction", "about", "compass", "contacts", "addresses"];
    var forceConnectionCheck = ["search", "directions", "showAddress"];

    function checkOK(page) {
        if (forceConnectionCheck.indexOf(page) > 0 ) {
            return false;
        }
        if ( (connectionLess.indexOf(page) < 0)) {
            return false;
        }
        return true;
    }
    console.log(checkOK("search"));

}
MyApplication();