
var Context = {};

Context.id = 22;
Context.user = "tomas";

/**
 * @param {Color} color car color
 * @param {String} maker car maker
 * @type Car
 */
function Car (color, maker) {
    this.color = color;
    this.maker = maker;

    this.getColor = function () {
        return this.color;
    }
    this.getMaker = function () {
        return this.maker;
    }
}

/**
 * @param {String} street
 * @param {String} town
 * @param {String} country
 * @return {Address} address
 */
function Address (street, town, country) {
    this.street = street;
    this.town = town;
    this.country = country;
}

var mujString = new String("mujString");

var object = new Car("red", "Skoda");
formatter.println("Car:");
formatter.addIndent(4);
formatter.println("color: " + object.getColor());
formatter.println("maker: " + object.maker);
formatter.removeIndent(4);

object = new Address("V Parku", "Prague", "Czech Republic");
formatter.println("Address:");
formatter.addIndent(4);
formatter.println("street: " + object.street);
formatter.println("town: " + object.town);
formatter.removeIndent(4);
