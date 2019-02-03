
function Bigfish() {
    this.numberOfLegs = 2;
    this.info = function () {
        return {a1: 1, a2: 2};
    };
}

function Witch() {
    this.firstName = "";
    this.dob = new Date();
    this.origin = new Bigfish();
    this.props = {
        a: 2,
        b: {
            b1: 1, b2: 3
        }
    };

    this.walk = function () {
    };

    this.identify = function () {
        return this.firstName;
    };

    this.dateOfBirth = function () {
        return this.dob;
    };

    this.today = function () {
        return new Date();
    };

}

Witch.prototype.lastName = "";
Witch.prototype.getName = function () {
    return this.firstName + " " + this.lastName;
};

Witch.prototype.age = function () {
    return new Date();
};

Witch.prototype.fakeOrigin = new Bigfish();
Witch.prototype.config = {
    c1: 1, c2: 2
};
exports.spell2 = function () {
};
exports.mana2 = 1;
var w = new Witch();
exports.witch = w;
