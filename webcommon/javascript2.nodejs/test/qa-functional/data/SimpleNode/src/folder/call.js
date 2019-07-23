
function SampleFuncCall() {
    this.callale = 1;
    this.callale2 = {
        f: 2,
        f1: 3
    };
    this.callbar = function () {
        return new Date();
    };
}

function InnerClCall() {
    this.callver = 1;
    this.callda = new Date();
    this.calllog = {
        messages: [], msg: "",
        owner: "Smith"
    };

    this.callinit = function () {
    };
}


SampleFuncCall.prototype.callattempt = 1;
SampleFuncCall.prototype.callgetAttempt = function(){
    return {aa : 1, ab :2};
};

function init(){
    return new SampleFuncCall();
}

module.exports = init();