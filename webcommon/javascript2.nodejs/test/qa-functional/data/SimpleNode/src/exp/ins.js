
function Fish() {
    this.numberOfLegs = 2;
    this.info = function () {
        return {a1: 1, a2: 2};
    };
}

function Wizard() {
    this.firstName = "";
    this.dob = new Date();
    this.origin = new Fish();
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

Wizard.prototype.lastName = "";
Wizard.prototype.getName = function () {
    return this.firstName + " " + this.lastName;
};

Wizard.prototype.age = function () {
    return new Date();
};

Wizard.prototype.fakeOrigin = new Fish();
Wizard.prototype.config = {
    c1: 1, c2: 2
};
exports.spell = function(){};
exports.mana = 1;
exports.mage = new Wizard();