function Alien() {
    this.nick = "";
    this.dob = new Date();
    this.hello = function () {
    };
    this.conf = {
        a: 1, b: 2
    };
}

exports.naive = function () {
};

exports.myobj = {
    jejda: {
        ale: 1,
        ale2: 1
    },
    pokus: new Date(),
    obj: new Alien()
};