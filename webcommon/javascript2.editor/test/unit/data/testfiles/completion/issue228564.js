x = function(a) {
    var t = a;
    function ret() {
        ret.testik = 7;
        return this || t;
    }
    return ret;
}

y = function(a) {
    var ret2 = x();
    return ret2;
}
y("d").