
var b = {
    x: "",
    y: 1,
    z: {e:""}
}
with (b) {
    with (z) {
         // test
    }
}

with (b) {
    try {

    } catch (ex) {
        with (z) {
            // test catch
        }
    }
}

with (b) {
    function test() {
        // in function
    }
}