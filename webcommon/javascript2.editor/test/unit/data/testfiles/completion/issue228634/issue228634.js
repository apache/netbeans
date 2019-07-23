con.x = function(a) {
    var t = a;
    function ret() {
        ret.testik = 7;
        return this || t;
    }
    return ret;
};

con.y = function(a) {
    var ret2 = con.x();
    return ret2;
};

con.y("a").te
