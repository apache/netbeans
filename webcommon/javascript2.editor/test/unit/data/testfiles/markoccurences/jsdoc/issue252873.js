/**
 * Checks whether tha passed value is defined and not null.
 *
 * @param {*} variable
 * @returns {Boolean}
 */
Breeze.hasValue = function (variable) {
    return !Breeze.isUndefined(variable) && variable !== null;
}; 