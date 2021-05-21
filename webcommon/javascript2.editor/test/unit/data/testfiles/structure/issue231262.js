goog.provide('pp.m.Building');

goog.require('goog.string');
goog.require('xx.m.Feature');



/**
 * Building.
 * @constructor
 * @extends {xx.m.Feature}
 * @implements {xx.m.HasTitle}
 * @implements {pp.m.HasLocationCode}
 */
pp.m.Building = function() {
  xx.m.Feature.call(this);

  /**
   * Building ID.
   * @type {number}
   */
  this.buildingId;

  /**
   * Five-letter location code.
   * @type {string}
   */
  this.locationCode;

  /**
   * Title including organization unit and address.
   * @type {string}
   */
  this.title;

  /**
   * Array of floors ordered from lowest to highest.
   * @type {Array.<xx.m.Floor>}
   */
  this.floors;

};
goog.inherits(pp.m.Building, xx.m.Feature);


/**
 * @return {string} organization unit title.
 */
pp.m.Building.prototype.getOrganizationUnitTitle = function() {
  return this.title.split(',', 1)[0];
};


/**
 * @return {string} title without organization unit.
 */
pp.m.Building.prototype.getTitleWithoutOrganizationUnit = function() {
  return goog.string.trim(
      this.title.substr(this.getOrganizationUnitTitle().length + 1));
};