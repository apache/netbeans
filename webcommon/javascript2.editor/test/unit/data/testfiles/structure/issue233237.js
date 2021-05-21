/**
 * 
 * @param {String} sName
 * @constructor
 * @returns {Man}
 */
function Man (fName, sName) {
    var firstName = fName;
    var secondName = sName;

    this.getFirstName = function () {
        return firstName;
    };

    this.getSecondName = function () {
        return secondName;
    };


}