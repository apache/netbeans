/**
 * @param {string} [somebody=John  Doe] - Somebody's name.
 */
function sayHello7(somebody) {
    if (!somebody) {
        somebody = 'John Doe';
    }
    alert('Hello ' + somebody);
}