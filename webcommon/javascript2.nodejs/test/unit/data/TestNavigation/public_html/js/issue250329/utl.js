
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
