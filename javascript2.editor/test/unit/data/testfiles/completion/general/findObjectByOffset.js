/**
 * 
 * @param {string} name
 * @returns {TestFindObject}
 */
var TestFindObject = function (name) {
    this._name = name;
    this._name.small();
    this._age = 1;
    this._age.toFixed();
};

TestFindObject.prototype.getName = function () {
    return this._name.small();
};

TestFindObject.prototype.getAge = function () {
    return this._age.toFixed(1);
};
