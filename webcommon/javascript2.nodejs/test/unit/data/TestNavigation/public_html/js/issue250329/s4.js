




var ut = require('./utl');
var ut2 = require('./utl2');
exports.registerRoute = function (app) {

    app.get("/api/string/:text", function (req, res) {
        ut.errorRespond();
        ut2.endsWith2();
    });
};