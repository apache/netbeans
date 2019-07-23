 function RefMammal() {
    this.rnumberOfLegs = 2;
    this.rinfo = function () {
        return {a1: 1, a2: 2};
    };
}
   
function RefPerson() {
    this.rfirstName = "";
    this.rdob = new Date();
    this.rorigin = new RefMammal();
    this.rprops = {
        a: 2,a3:21,
        b: {
            b1: 1, b2: 3
        } 
    };

    this.rwalk = function () {
    };

    this.ridentify = function () {
        return this.rfirstName;
    };

    this.rdateOfBirth = function () {
        return this.rdob;
    };

    this.rtoday = function () {
        return new Date();
    };

}

RefPerson.prototype.rlastName = "";
RefPerson.prototype.rgetName = function () {
    return this.rfirstName + " " + this.rlastName;
};

RefPerson.prototype.rage = function () {
    return new Date();
};

RefPerson.prototype.rfakeOrigin = new RefMammal();
RefPerson.prototype.rconfig = {
    c1: 1, c2: 2
};

var p = new RefPerson();
     
module.exports = p;