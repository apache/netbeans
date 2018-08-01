/**
 * @constructor
 * @returns {Man}
 */
function Man (fName, sName) {
    var firstName = fName;
    var secondName = sName;

    this.getFirstName = function () {
        return firstName;
    }

    this.getSecondName = function () {
        return secondName;
    }

    this.address = {
        street: "unknown",
        city: "unknown",
        zip: "15000"
    }

}

var petr = new Man("Petr", "Pavel");
var add = petr.address;
add.zip = "1234";


