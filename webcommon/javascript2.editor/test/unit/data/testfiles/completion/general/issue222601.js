function f222650_1() {
    var a = 1;
    return a;
}

function f222650_2() {
    var a = new Date();
    return a;
}

/**
 * @type String
 */
function f222650_3() {
    var a = "new Date()";
    var b = a + "test";
    return b + "test";
}

var b222601 = f222650_2();
b222601.getDate();


