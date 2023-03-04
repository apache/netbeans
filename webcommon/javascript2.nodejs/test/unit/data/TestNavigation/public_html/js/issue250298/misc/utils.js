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

var fs = require("fs");


exports.endsWith = function (pattern, suffix) {
    return pattern.indexOf(suffix, pattern.length - suffix.length) !== -1;
};
/**
 * Returns all files in given directory
 * @param {type} path
 * @returns {Array} array of file names in given path
 */
exports.listFiles = function (path) {
    var arr = [];
    var files = fs.readdirSync(path);
    files.forEach(function (file) {
        if (exports.endsWith(file, "js")) {
            arr.push(path + file);
        }
    });
    return arr;
};

exports.listFoldersAndNames = function (path) {
    var arr = {};
    var files = fs.readdirSync(path);
    files.forEach(function (file) {
        arr[file] = path + file; // TODO problem with same filename in different folder
    });
    return arr;
};



exports.listFilesAndNames = function (path) {

    var arr = {};
    var files = fs.readdirSync(path);
    var _s;
    files.forEach(function (file) {
        if (exports.endsWith(file, "js")) {
            _s = file.substring(0, file.indexOf(".js"));
            arr[_s] = path + file; // TODO problem with same filename in different folder
        }
    });
    return arr;
};

exports.errorRespond = function (response, errCode, errMsg) {
    response.writeHead(errCode, {
        "Content-Type": "text/plain"
    });
    response.write(errMsg);
    response.end();
};

exports.definedNotNull = function (obj) {
    return (typeof obj !== "undefined" && obj !== null);
};
