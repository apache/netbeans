"use strict";

var TripModel = require("./model");
var Promise = require("promise");
var dbProvider = require("./dao");
var TPError = require("../model/promiseError");
var Mediator = require("../core/mediator");
var tripDayCtrl = require("../tripday/controller");


function TripCtrl() {

}

TripCtrl.prototype.getDEditorsId = function (id) {
    return dbProvider.getEditorsId(id);
};
/**
 * Returns resolved of rejected promise if user can edit given trip
 * @param {String} tripId
 * @param {String} userId
 * @returns {Promise}
 */
TripCtrl.prototype.getDUserEdit = function (tripId, userId) {
    return new Promise(function (resolve, reject) {
        dbProvider.getEditorsId(tripId).then(function (ids) {
            ids.indexOf(userId) > -1 ? resolve(true) : reject(false);
        });
    });
};

module.exports = new TripCtrl();
  