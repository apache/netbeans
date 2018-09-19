function* gen01(start) {
    yield "A";
    yield "C";
    yield "1";
    yield "Tomas";
}

var g = gen01();
var d = g.next().v