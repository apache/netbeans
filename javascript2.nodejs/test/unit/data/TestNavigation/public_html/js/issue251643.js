"use strict";

var sanitizer = require("sanitizer");
var path = require('path');


String.prototype.sa = function(){
    
    sa;// cc here
    path.;
};

/**
 * Removes unsafe tags and attributes from html
 * @param {String} inputData
 * @returns {String} stripped String
 */
exports.sanitize = function(inputData){
  return sanitizer.sanitize(inputData);
};