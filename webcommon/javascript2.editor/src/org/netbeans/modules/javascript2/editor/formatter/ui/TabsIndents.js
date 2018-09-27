var engine = {
    cylinders : 8,
    power: "22k",
    getDescription: function () {
        if (enabled) {
            println ('Cylinders: '
                + this.cylinders + ' with power: ' + this.power);
        }
    }
}

var color = computeColor();
switch (color) {
    case 'white':
    case 'black':
        code = 0;
        break;
    case 'red':
        code = 1;
        break;
    default:
        code = undefined
}

