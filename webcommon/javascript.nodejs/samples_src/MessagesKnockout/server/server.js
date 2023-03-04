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

"use strict";
var express = require("express");
var fs = require("fs");
var path = require("path");
var bodyParser = require("body-parser");
var app = express();
var server = null;
var utils = require("./misc/utils");

require("./misc/shims");

function loadConfig(filename) {
    try {
        return JSON.parse(fs.readFileSync(filename).toString());
    } catch (err) {
        throw new Error("There has been an error parsing your JSON Configuration (" + filename + ") - " + err.toString());
    }
}

(function start() {

    console.log("loading configuration...");
    var config = loadConfig(path.join(__dirname, "config.json")) || {};

    console.log("setting up CORS...");

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


    app.use(express.static(path.join(__dirname, config.server.paths.static)));


    server = app.listen(config.server.port, function () {
        console.log("Listening on port %d", server.address().port);
    });
})();
