
function SampleFunc() {
    this.ale = 1;
    this.ale2 = {
        f: 2,
        f1: 3
    };
    this.bar = function () {
        return new Date();
    };
}

function InnerCl() {
    this.ver = 1;
    this.da = new Date();
    this.log = {
        messages: [], msg: "",
        owner: "Smith"
    };

    this.init = function () {
    };
}

SampleFunc.inn = new InnerCl();
SampleFunc.stNumber = 1;
SampleFunc.stDate = new Date();
SampleFunc.stObj = {
    foo: 2,
    test: {
        w: 1,
        ww: 2
    },
    hi: function () {
    }
};
SampleFunc.prototype.attempt = 1;
SampleFunc.prototype.getAttempt = function(){
    return {aa : 1, ab :2};
};
module.exports = SampleFunc;