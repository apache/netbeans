function Runner1() {
    this.lnick = "";
    this.dobl = new Date();
    this.lhello = function () {
    };
    this.lconf = {
        la: 1, lb: 2, laa: 2
    };
}

exports.literal = {
    prop1: {
        iprop: 1,
        iprop2: 1
    },
    den: new Date(),
    ob: new Runner1(),
    foo2: {
        fprop: 1,
        fprop2: 1
    }
};

var lRef = {
    propX: {
        iprop: 1,
        iprop2: 1
    },
    denX: new Date(),
    obX: new Runner1(),
    fooX: {
        fprop: 1,
        fprop2: 1
    }
};
   

exports.literalRef = lRef;

function FishInst() {
    this.numberOfHeads = 2;
    this.fishInfo = function () {
        return {a1: 1, a2: 2};
    };
}

function WizardInst() {
    this.firstName = "";
    this.dob = new Date();
    this.ancestor = new FishInst();
    this.instProps = {
        a: 2,
        aaab: {
            b1: 1, b2: 3
        }
    };

    this.dateOfSpell = function () {
        return this.dob;
    };

    this.tomorrow = function () {
        return new Date();
    };

}

WizardInst.prototype.nickname = "";
WizardInst.prototype.getNickName = function () {
    return this.firstName + " " + this.nickname;
};

WizardInst.prototype.getSomeAge = function () {
    return new Date();
};

WizardInst.prototype.fakeAncestor = new FishInst();
WizardInst.prototype.configInst = {
    c1: 1, c2: 2
};

exports.inst = new WizardInst();

var instReference = new WizardInst();

exports.instRef = instReference;





function SampleClass() {
    this.ale = 1;
    this.ale2 = {
        f: 2,
        f1: 3
    };
    this.bar = function () {
        return new Date();
    };
}

function InnerClass() {
    this.ver = 1;
    this.da = new Date();
    this.log = {
        messages: [], msg: "",
        owner: "Smith"
    };

    this.init = function () {
    };
}

SampleClass.nest = new InnerClass();
SampleClass.num  = 1;
SampleClass.today = new Date();
SampleClass.something = {
    foo: 2,
    testSample: {
        w: 1,
        ww: 2
    },
    hi: function () {
    }
};
SampleClass.prototype.attempt = 1;
SampleClass.prototype.getSomething = function () {
    return {aa: 1, ab: 2};
};
exports.constr = SampleClass;

exports.justCall = function(){
    return new Date();
};

exports.justCallObj = function(){
    return new SampleClass();
};

exports.justCallLit = function(){
    return {oo : 1, oo1: 2};
};


var events = require('events');

function Car(){
    events.EventEmitter.call(this);
    this.speed = 0;
}

Car.prototype = Object.create(events.EventEmitter.prototype);

Car.prototype.ride = function(){
    this.emit("riding");
};

Car.prototype.stop = function(){
    this.emit("stopping");
};

exports.Car = Car;