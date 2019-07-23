/**
 * @param {string=} somebody - Somebody's name.
 */
function sayHello6(somebody) {
    if (!somebody) {
        somebody = 'John Doe';
    }
    alert('Hello ' + somebody);
}

sayHello6()