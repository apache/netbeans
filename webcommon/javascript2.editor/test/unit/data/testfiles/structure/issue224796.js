(function(window) {
    "use strict";
    var xWipe = window.xWipe;
    if (!xWipe) {
        xWipe = {
            types : {}
        };
        window.xWipe = xWipe;
    }

    /**
     * @class
     * @param {type} appDiv
     * @returns {App.app}
     */
    function App(appDiv) {
        var app = new xWipe.Pagelet(appDiv);

        app.resize = function() {
            // put method body here
        };

        return app;
    }
}(window));