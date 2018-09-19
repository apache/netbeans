
function MyFnc() {
    this.ale = 1;
    this.ale2 = {
        f: 2,
        f1: 3
    };
    this.bar = function () {
        return new Date();
    };
}

function SomeFnc() {
    this.ver = 1;
    this.da = new Date();
    this.log = {
        messages: [],
        owner: "Smith"
    };

    this.init = function () {
    };
}

MyFnc.inn = new SomeFnc();
MyFnc.stNumber = 1;
MyFnc.stDate = new Date();
MyFnc.stObj = {
    foo: 2,
    test: {
        w: 1,
        ww: 2
    },
    hi: function () {
    }
};
MyFnc.prototype.attempt = 1;
MyFnc.prototype.getAttempt = function () {
    return {aa: 1, ab: 2};
};
exports.foobar = MyFnc;
exports.innerFnc = SomeFnc;