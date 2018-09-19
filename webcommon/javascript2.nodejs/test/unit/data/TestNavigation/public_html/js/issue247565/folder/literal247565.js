function Runner() {
    this.nick = "";
    this.dob = new Date();
    this.hello = function () {
    };
    this.conf = {
        a: 1, b: 2
    };
}

module.exports = {
    pokus: new Date(),
    obj: new Runner()
};