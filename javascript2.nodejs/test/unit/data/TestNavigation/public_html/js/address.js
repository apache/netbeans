var Address = function (street, town) {
    this.street = street;
    this.town = town;
}

Address.prototype.print = function () {
    console.log(this.street + " : " + this.town); 
}

module.exports = new Address("Naskove", "Prague");