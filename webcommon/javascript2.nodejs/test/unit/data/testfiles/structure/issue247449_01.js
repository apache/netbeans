"use strict";
var express = require("express");
var fs = require("fs");
var path = require("path");
var utils = require("./misc/util");
var db = require("./core/db");
var bodyParser = require("body-parser");
var app = express();
var server = null;
var applicationCore = {};
var tripPlanner = require("./core/tripPlanner");


function loadConfig(filename) {
    try {
        return JSON.parse(fs.readFileSync(filename).toString());
    } catch (err) {
        /*console.log("There has been an error parsing your JSON Configuration (server-config.json)");
         console.log(err);*/
        throw "There has been an error parsing your JSON Configuration (server-config.json)\n" + err;
    }
}

(function start() {
    
    console.log("loading configuration...");
    var config = loadConfig("./config.json") || {};
    
    console.log("loading sources...");
    applicationCore.tripPlanner = tripPlanner;
    // connect to DB & setup models
    db.init(config.database.host, config.database.port, config.database.databaseName);

    // CORS
    app.all("*", function (req, res, next) {
        res.header("Access-Control-Allow-Origin", "*");
        res.header("Access-Control-Allow-Headers", "X-Requested-With, X-TripPlanner-SessionId, X-TripPlanner-Created, X-TripPlanner-UserId, Content-Type");
        next();
    });

    app.use(bodyParser.json());       // to support JSON-encoded bodies
    app.use(bodyParser.urlencoded({
        extended: true
    }));

    console.log("loading routing handlers...");
    var routes = utils.listFiles((path.join(path.dirname(__filename), config.server.paths.api)).toString());
    // load all routers
    routes.forEach(function (route) {
        require(route).registerRoute(app);
    });


    applicationCore.ext = {
        trip: {},
        tripDay: {}
    };

    console.log("loading trip extensions...");
    var tripExts = utils.listFoldersAndNames((path.join(path.dirname(__filename), config.server.paths.tripExtensions)).toString());
    var _e;
    for (var i in tripExts) {
        if (tripExts.hasOwnProperty(i)) {
            _e = require(tripExts[i] + "/src.js");
            if (applicationCore.tripPlanner.isExtensionValid(_e)) {
                applicationCore.ext.trip[i] = _e;
            }
        }
    }

    console.log("loading trip day extensions...");
    var tripDayExts = utils.listFoldersAndNames((path.join(path.dirname(__filename), config.server.paths.tripDayExtensions)).toString());

    for (var i in tripDayExts) {
        if (tripDayExts.hasOwnProperty(i)) {
            _e = require(tripDayExts[i] + "/src.js");
            if (applicationCore.tripPlanner.isExtensionValid(_e)) {
                applicationCore.ext.tripDay[i] = _e;
            }
        }
    }

    server = app.listen(config.server.port, function () {
        console.log("Listening on port %d", server.address().port);
    });
})();

module.exports = applicationCore;