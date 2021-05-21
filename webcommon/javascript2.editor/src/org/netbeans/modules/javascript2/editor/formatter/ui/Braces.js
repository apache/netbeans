var colors = [0, 1, 2];
function computeColor(limit) {
    try {
        color = 0;
        for (var a = -1; a < limit; a++) {
            color += Math.round(Math.random() * 2);
        }
    } catch (error) {
        println(error);
    } finally {
        log();
    }
    while (color > 100) {
        do {
            color = (color / 2) + 1;
        } while (isOk());
    }
    color = color < 1 ? 1 : color;
}

var color = computeColor();
switch (color) {
    case 0:
    case 1:
        code = 'low';
        break;
    case 2:
        code = 'high';
        break;
    default:
        code = undefined;
}

class Colored {
    constructor(color) {
        this.color = color;
    }
}

function CustomColor(red, green, blue) {
    this.red = red;
    this.green = green;
    this.blue = blue;
}

var redColor = new CustomColor(255, 0, 0);
with (redColor) {
    if (red > green) {
        console.log("red");
    } else if (green > blue) {
        console.log("green");
    } else {
        console.log("unknown");
    }
}

Object.defineProperty(this, "length",
        {
            enumerable: true,
            get: function () {
                return length;
            }
        });