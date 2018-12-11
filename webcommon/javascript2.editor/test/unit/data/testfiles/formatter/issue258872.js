"use strict";

const net = require("./net");

class Parser {
    parse(catalogUrl) {
        var catalog = net.get(catalogUrl, JSON.parse);
        console.log(catalog.elements)
    }
}


module.exports = Parser;