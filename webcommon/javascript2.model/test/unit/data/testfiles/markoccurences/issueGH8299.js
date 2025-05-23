'use strict';

function testX(a = true) {
    const x = 1; // caret on x here does not highlight usage of x in next line
    x; // caret on x here highlights both x
    function () {
    }
}
