function Car() {
    this.manufacturer = "test";
    this.drive = function() {
    };
}

/**
 * @extends Car
 * @returns {undefined}
 */
function SuperCar() {
    this.topSpeed = 220;
    this.drift = function() {
    };
}

var a = new SuperCar();
//cc;19;a.;drive,drift,manufacturer,topSpeed;0


var obj = {
    "hello": 1,
    "isTest": true
};
/**
 * @extends  obj
 */
function Item() {
    this.greetings = function(){};
}

var b = new Item();
//cc;34;b.;isTest,hello,greetings;0
