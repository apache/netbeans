var engine = {

    cylinders : 8,

    power: "22k",


    getDescription: function () {
        with (this) {
            if (!disabled) {
                println('Cylinders: '
                    + cylinders + ' with power: ' + power);
            } else {
                log();
            }
        }
    },


    get parameter () {
        return power;
    }
}

class Car {

    constructor(engine) {
        this.engine = engine;
    }


    start() {
        this.engine.start(throttle => throttle + max);
    }
}

var colors = [0,

    1,


    2]
