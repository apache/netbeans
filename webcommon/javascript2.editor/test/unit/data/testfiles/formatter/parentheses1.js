var engine = {
    cylinders : 8,
    power: "22k",
    getDescription: function () {
        with(this) {
            if(!disabled) {
                println ('Cylinders: '
                    + cylinders + ' with power: ' + (power - dec));
            }   else {
                log();
            }
        }
    }
}

function computeColor() {
    try {
        color = 0;
        for   (var a = (-1); a < test() + 5; a++) {
            color += Math.round(Math.random() * 2);
        }
    }catch (error) {
        println (error);
    }  finally {
        log();
    }
    while((color > 100)) {
        do {
            color = color / 2;
        }   while (isOk());
    }
    color = (color < 1  ) ? 1 : color
}

var color = computeColor((a + b) * 2);
switch   (color - (x+1)) {
    case 0:
    case 1:
        code = 'low';
        break;
    case 2:
        code = 'high';
        break;
    default:
        code = undefined
}