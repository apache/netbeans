google.load('maps', '2.x');
var gmap = {
    map: false,
    markers: [],
    bounds: false,
    addMarker: function(m) {
        if (!gmap.bounds) gmap.bounds = new
                    google.maps.LatLngBounds();
        m.lat = m.lat * 57.2957795;
        m.lng = m.lng * 57.2957795;
        var coords = new google.maps.LatLng(m.lat, m.lng);
        var marker = new google.maps.Marker(coords);
        marker.bindInfoWindowHtml(m.label);
        gmap.markers.push(marker);
        gmap.bounds.extend(coords);
    },
    renderMap: function(map_id) {
        gmap.map = new
                google.maps.Map2(document.getElementById(map_id));
        var zoom = gmap.map.getBoundsZoomLevel(gmap.bounds);
        if (zoom > 15) zoom = 15;
        gmap.map.setCenter(gmap.bounds.getCenter(), zoom);
        var i = 0;
        for (i = 0; i < gmap.markers.length; i++) {
            gmap.map.addOverlay(gmap.markers[i]);
        }
        gmap.map.setUIToDefault();
    }
}

function initialize() {
    if (GBrowserIsCompatible()) {
        gmap.renderMap('map');
    }
}

$(document).ready(function() {
    google.setOnLoadCallback(initialize);
});