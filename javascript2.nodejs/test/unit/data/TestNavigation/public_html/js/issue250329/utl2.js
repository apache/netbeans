

"use strict";

var fs = require("fs");


module.exports = {
    endsWith2: function (pattern, suffix) {
        return pattern.indexOf(suffix, pattern.length - suffix.length) !== -1;
    },
    listFiles2: function (path) {
        var arr = [];
        var files = fs.readdirSync(path);
        files.forEach(function (file) {
            if (exports.endsWith(file, "js")) {
                arr.push(path + file);
            }
        });
        return arr;
    }
};
