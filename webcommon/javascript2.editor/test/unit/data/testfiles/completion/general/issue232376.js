function OceanAnimal() {
    this.wswim = function(n) {};
}

function LandAnimal() {
    this.walk = function(n) {};
}

/**
 * @extends LandAnimal
 * @extends OceanAnimal
 * @returns {Turtle}
 */
function Turtle() {
    LandAnimal.call(this);
    OceanAnimal.call(this);
}
var a232376 = new Turtle();
a232376.w
        