"use strict";
var Promise = require("promise");
var extensions = {};

/**
 * Resolves all extensions for given day
 * @param {TripDay} day
 * @param {String} action
 * @returns {Promise}
 */
function resolveDay(day, index, days, action) {
    var p = Promise.resolve();
    for (var i = 0, max = day.data.length; i < max; i++) {
        (function (index) {
            p = p.then(resolveExtension.bind(null, day, day.data[index].name, index, action));
        }(i));
    }

    return p;
}

module.exports = new TripDayExtCtrl();