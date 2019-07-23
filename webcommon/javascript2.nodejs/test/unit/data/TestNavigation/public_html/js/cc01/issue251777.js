"use strict";

var istPromise = require("promise");
var istTPError = require("../../../model/promiseError");
var isttripDayCtrl = require("../../../tripday/controller");
var istpes = 1;
function NoteDao() {}


NoteDao.prototype.create = function (istnote) {
    ist;// cc here 01
    return new Promise(function (istresolve, istreject) {
        ist;// cc here 02
        tripDayCtrl
                .get(note.tripDayId)
                .then(function (istday) {
                    ist;// cc here 03
                    day.data.push(note);
                    day.save(function (isterr) {
                        ist;// cc here 04
                        if (isterr) {
                            reject(new TPError(TPError.DatabaseError, "Unable to save data to db"));
                        } else {
                            resolve();
                        }
                    });
                }, reject);
    });
};

module.exports = new NoteDao();