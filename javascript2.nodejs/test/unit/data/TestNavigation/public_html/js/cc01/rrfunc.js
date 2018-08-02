
function RSampleClass() {
    this.rale = 1;
    this.rale2 = {
        f: 2,
        f1: 3
    };
    this.rbar = function () {
        return new Date();
    };
}
 
function RInnerClass() {
    this.rver = 1;
    this.rda = new Date();
    this.rlog = {
        messages: [], msg: "",
        owner: "Smith"
    };

    this.rinit = function () {
    };
}

RSampleClass.rinn = new RInnerClass();
RSampleClass.rstNumber = 1;
RSampleClass.rstDate = new Date();
RSampleClass.rstObj = {
    foo: 2,
    test: {
        w: 1,
        ww: 2
    },
    hi: function () {
    }
};
RSampleClass.prototype.rattempt = 1;
RSampleClass.prototype.rgetAttempt = function(){
    return {aa : 1, ab :2};
};
module.exports = RSampleClass;
