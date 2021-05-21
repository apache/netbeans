aaa(function() {
    function MapProvider() {
        this.directionsService = new google.maps.DirectionsService();
        this.mapAlreadyRendered = false;
        this.directionsDisplay = new google.maps.DirectionsRenderer();
    }

    /**
     * 
     * @param {Array} routes array of objects returned by getSimpleDirections()
     * @param {String} elementId ID of element where map should be placed
     * @param {Number} index index of route from routes parameter to be displayed (starts with 0)
     * @param {String} panelElementId element ID of element where route descriptions should be placed
     * @returns {undefined}
     */
    MapProvider.prototype.displayRoute = function(routes, elementId, index, panelElementId) {

    };
});

neco(function() {
    /**
     * 
     * @type MapProvider
     */
    var p = semTam[0];
    p.d
});