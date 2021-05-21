
function Mammal() {
    this.bnumberOfLegs = 2;
    this.binfo = function () {
        return {a1: 1, a2: 2};
    };
}

function Person() {
    this.bfirstName = "";
    this.bdob = new Date();
    this.borigin = new Mammal();
    this.bprops = {
        a: 2,
        b: {
            b1: 1, b2: 3
        }
    };

    this.bwalk = function () {
    };

    this.bidentify = function () {
        return this.bfirstName;
    };

    this.bdateOfBirth = function () {
        return this.bdob;
    };

    this.btoday = function () {
        return new Date();
    };

}

Person.prototype.lastName = "";
Person.prototype.getName = function () {
    return this.bfirstName + " " + this.blastName;
};

Person.prototype.age = function () {
    return new Date();
};

Person.prototype.fakeOrigin = new Mammal();
Person.prototype.config = {
    c1: 1, c2: 2
};
module.exports = new Pe