
function Mammal() {
    this.numberOfLegs = 2;
    this.info = function () {
        return {a1: 1, a2: 2};
    };
}

function Person() {
    this.firstName = "";
    this.dob = new Date();
    this.origin = new Mammal();
    this.props = {
        a: 2, a2 :1,
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

Person.prototype.lastName = "";
Person.prototype.getName = function () {
    return this.firstName + " " + this.lastName;
};

Person.prototype.age = function () {
    return new Date();
};

Person.prototype.fakeOrigin = new Mammal();
Person.prototype.config = {
    c1: 1, c2: 2
};
module.exports = new Person();