/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var directions = {};
var contactsWithAddress = [];
var contactAddresses = [];
var compassWatchId = -1;
var locationWatchId = -1;
var map_with_pos = {};
var previous_pos_marker = {};

$(document).ready(function() {
    document.addEventListener("deviceready", onDeviceReady, false);
    //for testing in Chrome browser uncomment
//    onDeviceReady();
});

function onDeviceReady() {

    console.log("Ready");
    $(window).bind('pageshow resize orientationchange', function(e) { // resize page if needed
        maxHeight();
    });
    $('#toggleswitch').change(function() { // toggle switch for FROM location
        var v = $(this).val();
        if (v === "on") {
            $("#fromfield").css("display", "none");
        } else {
            $("#fromfield").css("display", "block");
        }
    });

    $("#refreshMyLocationBt").click(function(e) { // refresh my location button
        e.stopImmediatePropagation();
        e.preventDefault();
        var app = new MyApplication();
        app.mylocation();
    });

    $("#cancelMyLocationBt").click(function(e) { // stop/resume watching my location button
        e.stopImmediatePropagation();
        e.preventDefault();
        var $icon = $($(this).find(".ui-icon")[0]);
        var oldClass = $icon.attr("class");
        if ($(this).text() === "Stop") {
            $($(this).find(".ui-btn-text")[0]).text("Resume");
            $icon.attr("class", oldClass.replace("delete", "star"));
            window.navigator.geolocation.clearWatch(locationWatchId);
        } else {
            $($(this).find(".ui-btn-text")[0]).text("Stop");
            $icon.attr("class", oldClass.replace("star", "delete"));
            var map = new MapCtrl();
            map.syncPositionWithMap();
        }
    });

    $("#refreshPhotoBt").click(function(e) { // refresh my photo button
        e.stopImmediatePropagation();
        e.preventDefault();
        var app = new MyApplication();
        app.photos();
    });
    $("#showPhoneStatus").click(function(e) { // show phone status information
        e.stopImmediatePropagation();
        e.preventDefault();
        var app = new MyApplication();
        app.showPhoneStatus();
    });

    maxHeight();
    var app = new MyApplication();
}

function maxHeight() {

    var w = $(window).height();
    var cs = $('div[data-role="content"]');
    for (var i = 0, max = cs.length; i < max; i++) {
        var c = $(cs[i]);
        var h = $($('div[data-role="header"]')[i]).outerHeight(true);
        var f = $($('div[data-role="footer"]')[i]).outerHeight(true);
        var c_h = c.height();
        var c_oh = c.outerHeight(true);
        var c_new = w - h - f - c_oh + c_h;
        var total = h + f + c_oh;
        if (c_h < c.get(0).scrollHeight) {
            c.height(c.get(0).scrollHeight);
        } else {
            c.height(c_new);
        }
    }

}

function showAlert(message, title) {
    if (window.navigator.notification) {
        window.navigator.notification.alert(message, null, title, 'OK');
    } else {
        alert(title ? (title + ": " + message) : message);
    }
}

function MyApplication() {
    var self = this;
    var connectionLess = ["undefinedAction", "about", "compass", "contacts", "addresses"];
    var forceConnectionCheck = ["search", "directions", "showAddress"];
    var states = {};
    states[Connection.UNKNOWN] = 'Unknown';
    states[Connection.ETHERNET] = 'Ethernet';
    states[Connection.WIFI] = 'WiFi';
    states[Connection.CELL_2G] = 'Mobile';
    states[Connection.CELL_3G] = 'Mobile';
    states[Connection.CELL_4G] = 'Mobile';
    states[Connection.NONE] = 'No network';

    function hasConnection() {
        if (window.navigator.connection.type === Connection.NONE) {
            return false;
        }
        return true;
    }

    this.showPhoneStatus = function() {
        showAlert(window.device.model + "(" + window.device.platform + " " + window.device.version + ")\nConnection: " + states[window.navigator.connection.type], "About");
    };

    /**
     * Calls method of MyApplication based on value of hash parameter
     * @returns {undefined}
     */
    this.route = function() {
        window.navigator.compass.clearWatch(compassWatchId);
        window.navigator.geolocation.clearWatch(locationWatchId);
        var _h = window.location.hash || "#undefinedAction";
        var stop = _h.length;
        if (_h.indexOf("?") > 0) {
            stop = _h.indexOf("?") - 1;
        }
        _h = _h.substr(1, stop);
        $("#map").html("");
        $("#addressMap").html("");

        if (!checkOK(_h)) {
            showAlert("Internet connection is required", "No internet connection");
            return;
        }

        if (typeof this[_h] === "function") {
            this[_h]();
        } else {
            window.console.log("action function not found: " + _h);
        }
    };

    function checkOK(page) {
        if (hasConnection()) {
            return true;
        }

        if (forceConnectionCheck.indexOf(page) > 0 && !hasConnection()) {
            return false;
        }
        if (!hasConnection() && (connectionLess.indexOf(page) < 0)) {
            return false;
        }
        return true;
    }

    this.photos = function() {
        $("#photosContent").html("");
        var mapHandler = new MapCtrl();
        mapHandler.loadPhoto(printPhoto);
    };

    function printPhoto(photo, name) {
        $("#photosContent").html("");
        if (typeof name !== "undefined") {
            $("#photosContent").append("<h4>" + name + "</h4>");
        }
        if (typeof photo !== "undefined") {
            $("#photosContent").append("<img style=\"width: 100%; \" src=\"" + photo + "\"/>");
        } else {
            showAlert("Photo not found", "Error");
        }
    }

    function getContactName(contact) {
        if (!contact.displayName) {
            var contact_name = "";
            if (!contact.name) {
                return (contact.nickname || "unknown");
            } else {
                var contact_name = (contact.name.givenName + " " + contact.name.familyName);
                if (contact_name.length < 2) {
                    contact_name = contact.name.formatted || "unknown";
                }
            }
            return contact_name;
        } else {
            return contact.displayName;
        }
    }

    this.about = function() {
        $("#aboutContent").html("<h1>Sample Map App</h1><div>Sample map application built with Cordova in NetBeans IDE. Visit <a href='#' id='externalLink' target='_blank' rel='https://www.netbeans.org'>netbeans.org</a> for more information</div><br/><div><b>Device: </b>" + window.device.model + "(" + window.device.platform + ": " + window.device.version + ")</div><div><b>Connection: </b>" + states[navigator.connection.type] + "</div>");
        $("#externalLink").live('tap', function() {
            window.navigator.app.loadUrl($(this).attr("rel"), {openExternal: true});
            return false;
        });
    };

    this.results = function() {
        var address = $('#addressfield').val();
        if (!address) {
            return;
        }
        var mapHandler = new MapCtrl();
        mapHandler.address = address;
        mapHandler.mapContainter = "addressMap";
        mapHandler.findMatchingResults(function(results) {
            printGeocoderResults(results, "#results", "#resultsList");
        });
    };

    this.showAddress = function() {
        var index = getParmFromHash(window.location.href, "i") || -1;
        if (contactAddresses === null || typeof contactAddresses[index] === "undefined") {
            $("#matchingAddresses").html("Address not found");
            return;
        }
        var mapHandler = new MapCtrl();
        mapHandler.headerID = "#addressHeader";
        mapHandler.address = contactAddresses[index];
        mapHandler.findMatchingResults(function(results) {
            $("#addressHeader").html("Matching places");
            printGeocoderResults(results, "#showAddress", "#matchingAddresses");
        });
    };

    this.compass = function() {
        compassWatchId = window.navigator.compass.watchHeading(function(heading) {
            var h = 360 - Math.round(heading.magneticHeading);
            $("#compassInfo").html("Heading: " + Math.round(heading.magneticHeading) + "&deg;");
            $("#compassImg").css("-webkit-transform", "rotate(" + h + "deg)");
            $("#compassImg").css("transform", "rotate(" + h + "deg)");
        }, function(error) {
            showAlert(error.code, "Error");
        }, {frequency: 100});
    };

    this.contacts = function() {
        contactsWithAddress = [];
        $("#contactsResultsList").html("");
        $.mobile.showPageLoadingMsg();
        var ch = new ContactsCtrl();
        ch.listContactsWithAddress(printContactsResult, function(error) {
            $('h1[id="contactsHeader"]').html("Contacts error");
            $.mobile.hidePageLoadingMsg();
            try {
                showAlert(error.message);
            } catch (e) {
                window.console.error(e);
            }
        });
    };

    this.addresses = function() {
        contactAddresses = [];
        $("#addressesResultsList").html("");
        var index = getParmFromHash(window.location.href, "i") || -1;
        if (index > -1 && typeof contactsWithAddress[index] !== "undefined") {
            var html = "<h4>" + getContactName(contactsWithAddress[index]) + "</h4><ul id=\"addrResults\" data-role=\"listview\" data-divider-theme=\"b\" data-inset=\"true\">";
            for (var j = 0, max = contactsWithAddress[index].addresses.length; j < max; j++) {
                var _t = (contactsWithAddress[index].addresses[j].streetAddress || '') + " " + (contactsWithAddress[index].addresses[j].locality || '') + " " + (contactsWithAddress[index].addresses[j].postalCode || '') + " " + (contactsWithAddress[index].addresses[j].country || '');
                contactAddresses.push(_t);
                html += " <li data-role=\"list-divider\" role=\"heading\">" + (contactsWithAddress[index].addresses[j].type || 'other') + "</li>" +
                        "<li data-theme=\"c\"><a href=\"#showAddress?i=" + j + "\" rel=\"external\" data-transition=\"slide\">" + (_t + "") + "</a></li>";
            }
            html += "</ul>";
            $("#addressesResultsList").append(html);
            if ($('#addresses').hasClass('ui-listview')) {
                $('#addresses').listview('refresh');
            } else {
                $('#addresses').trigger('create');
            }
        } else {
            $("#addressesResultsList").html("No address found");
        }
    };

    function printContactsResult(results) {
        contactsWithAddress = results;

        $("#contactsResultsList").html("");
        if (results.length === 0 || typeof results === "undefined") {
            $("#contactsResultsList").html("No directions found");
            return;
        }
        var html = " <ul id=\"contactsResults\" data-role=\"listview\" data-divider-theme=\"b\" data-inset=\"true\">";
        for (var i = 0, max = results.length; i < max; i++) {
            if (results[i].addresses !== null && typeof results[i].addresses !== "undefined" && results[i].addresses.length > 0) {

                var _t = (results[i].addresses[0].streetAddress || '') + (results[i].addresses[0].locality || '') + (results[i].addresses[0].postalCode || '') + (results[i].addresses[0].country || '') + "";
                if (_t.length > 0) {
                    html += "<li data-theme=\"c\"><a href=\"#addresses?i=" + i + "\" rel=\"external\" data-transition=\"slide\">" + getContactName(results[i]) + "</a></li>";
                }
            }
        }
        $.mobile.hidePageLoadingMsg();
        html += "</ul>";
        $("#contactsResultsList").append(html);
        if ($('#contacts').hasClass('ui-listview')) {
            $('#contacts').listview('refresh');
        } else {
            $('#contacts').trigger('create');
        }
    }

    this.findDirections = function() {
        $("#directionsResultsList").html("");
        var useMyLocation = $("#toggleswitch").val() === "off" ? false : true;
        var to = $("#tofield").val();
        var from = $("#fromfield").val();
        var mode = "DRIVING";
        $("input[name*=radio-choice-]:checked").each(function() {
            mode = $(this).val();
        });
        var mapHandler = new MapCtrl(function(error) {
            $('h1[id="dirlistHeader"]').html("GPS error");
            try {
                showAlert(error.message);
            } catch (e) {
            }
        });
        mapHandler.mapContainter = "directionMap";
        mapHandler.headerID = "#dirlistHeader";
        mapHandler.findDirections(from, to, useMyLocation, mode, printDirectionResults);
    };

    this.showDirection = function() {
        var m = new MapCtrl();
        var index = getParmFromHash(window.location.href, "i") || -1;
        m.mapContainter = "directionMap";
        m.headerID = "#directionHeader";
        m.printDirection(index);
    };

    function printDirectionResults(results) {
        $("#directionsResultsList").html("");
        if (results.length === 0 || typeof results === "undefined") {
            $("#directionsResultsList").html("No directions found");
            return;
        }
        var html = " <ul id=\"dirResults\" data-role=\"listview\" data-divider-theme=\"b\" data-inset=\"true\">";
        for (var i = 0, max = results.length; i < max; i++) {
            html += "<li data-theme=\"c\"><a href=\"#showDirection?i=" + i + "\" rel=\"external\" data-transition=\"slide\">" + results[i].summary + " (" + results[i].legs[0].distance.text + " @ " + results[i].legs[0].duration.text + ")</a></li>";
        }
        html += "</ul>";
        $("#directionsResultsList").append(html);
        if ($('#findDirections').hasClass('ui-listview')) {
            $('#findDirections').listview('refresh');
        } else {
            $('#findDirections').trigger('create');
        }
    }

    this.address = function() {
        var mapHandler = new MapCtrl(function(error){
            window.console.error(error.message);
        });
        var address = getParmFromHash(window.location.href, "ad");
        mapHandler.address = decodeURIComponent(address);
        mapHandler.mapContainter = "addressMap";
        mapHandler.headerID = "#addressHeader";
        mapHandler.locateAddress();
    };

    function printGeocoderResults(results, pageId, contentId) {
        $(contentId).html("");
        var html = " <ul data-role=\"listview\" data-divider-theme=\"b\" data-inset=\"true\">";
        for (var i = 0, max = results.length; i < max; i++) {
            html += "<li data-theme=\"c\"><a href=\"#address?ad='" + encodeURIComponent(results[i].formatted_address) + "'\" rel=\"external\" data-transition=\"slide\">" + results[i].formatted_address + "</a></li>";
        }
        html += "</ul>";
        $(contentId).append(html);
        if ($(pageId).hasClass('ui-listview')) {
            $(pageId).listview('refresh');
        } else {
            $(pageId).trigger('create');
        }
    }

    this.undefinedAction = function() {
        window.console.log("Action not defined");
    };

    this.mylocation = function() {
        resetStopButton();
        var mapHandler = new MapCtrl(function(error) {
            window.console.error(error.message);
        });
        mapHandler.headerID = "#header";
        mapHandler.locateMe();
    };

    function resetStopButton() {
        var $stopButton = $($("#cancelMyLocationBt")[0]);
        var $icon = $($stopButton.find(".ui-icon")[0]);
        var oldClass = $icon.attr("class");
        $($stopButton.find(".ui-btn-text")[0]).text("Stop");
        $icon.attr("class", oldClass.replace("star", "delete"));
    }

    function init() {
        self.route();
    }

    $(window).on('hashchange', function() {
        self.route();
    });

    init();
}

function MapCtrl(onFail) {
    var map;
    var marker;
    var infoWindow;

    var self = this;
    self.mapPrinted = false;
    this.mapContainter = "map";
    this.headerID = "#noheader";
    /**
     * Address to show on map
     * @type String
     */
    this.address = "Prague";

    /**
     * Loads new map
     * @param {Function} callback function to be called when map is loaded
     * @returns {undefined}
     */
    function loadMap(mapContainer, callback, waitForPostion) {
        var latlng = new google.maps.LatLng(55.17, 23.76);
        var myOptions = {
            zoom: 6,
            center: latlng,
            streetViewControl: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            zoomControl: true
        };

        map = new google.maps.Map(document.getElementById(mapContainer), myOptions);
        self.map = map;
        google.maps.event.trigger(map, 'resize');

        google.maps.event.addListener(map, 'tilesloaded', function() {
            if (!self.mapPrinted) {
                self.mapPrinted = true;
                if (waitForPostion) {
                    window.navigator.geolocation.getCurrentPosition(callback, onFail, {maximumAge: 10000, timeout: 300000, enableHighAccuracy: true});
                } else {
                    callback();
                }
            }
        });
    }

    this.syncPositionWithMap = function() {
        locationWatchId = window.navigator.geolocation.watchPosition(function(position) {
            showOnMap(position, self.headerID);
        }, onFail, {maximumAge: 10000, timeout: 10000, enableHighAccuracy: true});
    };

    /**
     * Loads new map
     * @param {Function} callback function to be called when map is loaded
     * @returns {undefined}
     */
    function loadMapWatchLocation(mapContainer, callback) {
        var latlng = new google.maps.LatLng(55.17, 23.76);
        var myOptions = {
            zoom: 6,
            center: latlng,
            streetViewControl: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            zoomControl: true
        };

        map = new google.maps.Map(document.getElementById(mapContainer), myOptions);
        self.map = map;
        map_with_pos = map;
        google.maps.event.trigger(map, 'resize');

        google.maps.event.addListener(map, 'tilesloaded', function() {
            if (!self.mapPrinted) {
                self.mapPrinted = true;
                locationWatchId = window.navigator.geolocation.watchPosition(callback, onFail, {maximumAge: 10000, timeout: 10000, enableHighAccuracy: true});
            }
        });
    }

    this.loadPhoto = function(callback) {
        $.mobile.showPageLoadingMsg();
        google.load("maps", "3.8", {"callback": function() {
                navigator.geolocation.getCurrentPosition(function(position) {
                    requestPhotosFromMaps(position, callback);
                }, onFail, {maximumAge: 10000, timeout: 300000, enableHighAccuracy: true});
            }, other_params: "sensor=true&language=en&libraries=places"});
    };

    function requestPhotosFromMaps(position, callback) {
        var request = {
            location: new google.maps.LatLng(position.coords.latitude, position.coords.longitude),
            radius: 3000
        };

        var service = new google.maps.places.PlacesService(document.getElementById("photosContent"));
        service.nearbySearch(request, function(results, status) {
            pickRandomPhoto(results, status, callback);
        });

    }

    function pickRandomPhoto(results, status, callback) {
        if (results.length === 0 || status != google.maps.places.PlacesServiceStatus.OK) {
            callback();
        }
        var random = parseInt(1000 * Math.random()) % results.length;
        var notFound = true;
        var iteration = 0;
        while (notFound && iteration < results.length) {
            if (typeof results[random].photos !== "undefined") {
                notFound = false;
                var url = (!results[random].photos[0].raw_reference) ? results[random].photos[0].getUrl({maxHeight: 1200}) : results[random].photos[0].raw_reference.fife_url;
                callback(url, results[random].name);
            } else {
                random = parseInt(1000 * Math.random()) % results.length;
                iteration += 1;
            }
        }
        $.mobile.hidePageLoadingMsg();
        if (notFound) {
            callback();
        }
    }

    function geo_error(error) {
        if (typeof error === "function") {
            error.callback();
        } else {
            showAlert("Problem with retrieving location ", "Error");
        }
    }


    /**
     * Uses Geocoder to translate string address to map position and places marker on found position
     */
    function showAddressOnMap(address, mapContainter, headerID) {
        if (address.indexOf("'") === 0) {
            address = address.substring(1, address.length);
        }
        if (address.charAt(address.length - 1) === '\'') {
            address = address.substring(0, address.length - 1);
        }

        var geocoder = new google.maps.Geocoder();
        geocoder.geocode({'address': address}, function(results, status) {
            if (status === google.maps.GeocoderStatus.OK) {
                var myOptions = {
                    zoom: 6,
                    center: new google.maps.LatLng(55.17, 23.76),
                    streetViewControl: true,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    zoomControl: true
                };
                map = new google.maps.Map(document.getElementById(mapContainter), myOptions);
                self.map = map;
                map.setCenter(results[0].geometry.location);
                map.setZoom(13);

                if (!marker) {
                    marker = new google.maps.Marker({
                        position: results[0].geometry.location,
                        map: map
                    });
                } else {
                    marker.setPosition(results[0].geometry.location);
                }

                if (!infoWindow) {
                    infoWindow = new google.maps.InfoWindow({
                        content: address
                    });
                } else {
                    infoWindow.setContent(address);
                }
                $(headerID).text("Location found");
                google.maps.event.addListener(marker, 'click', function() {
                    infoWindow.open(map, marker);
                });
            } else {
                geo_error(onFail);
            }
        });
    }

    function reuseOldMap() {
        try {
            if (!self.map && map_with_pos && previous_pos_marker) {
                self.map = map_with_pos; // use previous map (instead of loading a new one)
                previous_pos_marker.setMap(null); // remove previous marker
            }
        } catch (e) {
            window.console.log("nothing to reuse");
        }
    }

    /**
     * Shows reader's position on map
     */
    function showOnMap(position, headerID) {
        $(headerID).html("You Are Here");
        $.mobile.hidePageLoadingMsg();
        reuseOldMap();
        self.map.setCenter(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
        self.map.setZoom(15);

        var info =
                ('Latitude: ' + position.coords.latitude + '<br>' +
                        'Longitude: ' + position.coords.longitude + '<br>' +
                        'Altitude: ' + position.coords.altitude + '<br>' +
                        'Accuracy: ' + position.coords.accuracy + '<br>' +
                        'Altitude Accuracy: ' + position.coords.altitudeAccuracy + '<br>' +
                        'Heading: ' + position.coords.heading + '<br>' +
                        'Speed: ' + position.coords.speed + '<br>' +
                        'Timestamp: ' + new Date(position.timestamp));

        var point = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
        if (!marker) {
            marker = new google.maps.Marker({
                position: point,
                map: self.map
            });
        } else {
            marker.setPosition(point);
        }
        previous_pos_marker = marker;
        if (!infoWindow) {
            infoWindow = new google.maps.InfoWindow({
                content: info
            });
        } else {
            infoWindow.setContent(info);
        }
        google.maps.event.addListener(marker, 'click', function() {
            infoWindow.open(self.map, marker);
        });
    }



    this.locateMe = function() {
        $.mobile.showPageLoadingMsg();
        google.load("maps", "3.8", {"callback": function() {
                loadMapWatchLocation(self.mapContainter, function(position) {
                    showOnMap(position, self.headerID);
                });
            }, other_params: "sensor=true&language=en&libraries=places"});
    };

    this.locateAddress = function() {
        $(this.headerID).text("Loading...");
        $.mobile.showPageLoadingMsg();
        google.load("maps", "3.8", {"callback": function() {
                $.mobile.hidePageLoadingMsg();
                showAddressOnMap(self.address, self.mapContainter, self.headerID);
            }, other_params: "sensor=true&language=en&libraries=places"});
    };

    this.findMatchingResults = function(callback) {
        $.mobile.showPageLoadingMsg();
        google.load("maps", "3.8", {"callback": function() {
                var geocoder = new google.maps.Geocoder();
                geocoder.geocode({'address': self.address}, function(results, status) {
                    $.mobile.hidePageLoadingMsg();
                    if (status === google.maps.GeocoderStatus.OK) {
                        callback(results);
                    }
                });
            }, other_params: "sensor=true&language=en&libraries=places"});
    };

    this.findDirections = function(from, to, useMyLocation, mode, callback) {
        $(self.headerID).html("Loading...");
        $.mobile.showPageLoadingMsg();
        if (useMyLocation) {
            window.navigator.geolocation.getCurrentPosition(function(position) {
                google.load("maps", "3.8", {"callback": function() {
                        loadMap(self.mapContainter, function() {
                            getDirections(new google.maps.LatLng(position.coords.latitude, position.coords.longitude), to, mode, callback);
                        }, false);
                    }, other_params: "sensor=true&language=en"});
            }, onFail, {maximumAge: 10000, timeout: 10000, enableHighAccuracy: true});
        } else {
            google.load("maps", "3.8", {"callback": function() {
                    loadMap(self.mapContainter, function() {
                        getDirections(from, to, mode, callback);
                    }, false);
                }, other_params: "sensor=true&language=en&libraries=places"});
        }
    };

    function getDirections(from, to, mode, callback) {
        var directionsDisplay = new google.maps.DirectionsRenderer();
        var directionsService = new google.maps.DirectionsService();
        directionsDisplay.setMap(self.map);
        var request = {
            origin: from,
            destination: to,
            provideRouteAlternatives: true,
            travelMode: google.maps.DirectionsTravelMode[mode]
        };
        directionsService.route(request, function(response, status) {
            $(self.headerID).html("Directions");
            $.mobile.hidePageLoadingMsg();
            directions = response;
            if (status == google.maps.DirectionsStatus.OK) {
                callback(response.routes);
            } else {
                callback([]);
            }
        });
    }

    this.printDirection = function(index) {
        $.mobile.showPageLoadingMsg();
        if (parseInt(index) < 0) {
            showAlert("Direction not found", "Problem");
            return;
        }
        var _r = directions.routes[index];
        directions.routes = [_r];
        $(self.headerID).html("Loading...");
        google.load("maps", "3.8", {"callback": function() {
                loadMap(self.mapContainter, function() {
                    var directionsDisplay = new google.maps.DirectionsRenderer();
                    directionsDisplay.setMap(self.map);
                    $(self.headerID).html(directions.routes[0].summary);
                    directionsDisplay.setDirections(directions);
                    $.mobile.hidePageLoadingMsg();
                }, false);
            }, other_params: "sensor=true&language=en&libraries=places"});
    };
}

function ContactsCtrl() {

    this.headerID = "#noheader";

    this.listContactsWithAddress = function(onSuccess, onError) {
        $(this.headerID).html("Loading...");
        var options = new ContactFindOptions();
        options.filter = "";
        options.multiple = true;
        var filter = ["name", "addresses"];
        window.navigator.contacts.find(filter, onSuccess, onError, options);
    };
}


function validateSearchField(elementID, newhash) {
    if ($("#" + elementID).val().length > 0) {
        window.location.hash = newhash;
    } else {
        showAlert("You need to specify address", "Empty value");
    }
}
function validateDirectionFields() {
    var useMyLocation = $("#toggleswitch").val() === "off" ? false : true;
    if ($("#tofield").val().length > 0 && ($("#fromfield").val().length > 0 || useMyLocation)) {
        window.location.hash = "findDirections";
    } else {
        showAlert("You need to specify address", "Empty value");
    }
}

function getParmFromHash(url, parm) {
    var re = new RegExp("#.*[?&]" + parm + "=([^&]+)(&|$)");
    var match = url.match(re);
    return(match ? match[1] : "");
}
