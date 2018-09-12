!function(test) {
    console.info("vstupni param: " + test);

    var bar = "bar promenna";

    var Alert = function () {

    };

    Alert.prototype.getInfo = function (param) {
        return "Info from Alert " + param + " and " + bar;
    };

    var alert = new Alert();
    console.info(alert.getInfo(22));
}("sranda");  