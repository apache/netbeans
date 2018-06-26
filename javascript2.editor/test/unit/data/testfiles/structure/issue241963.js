/**
 * Expected: Boolean
 * Actual: Number.x
 * @param {string} x
 */
function a(x) {
        return !!x;
}

/**
 * Expected: Number
 * Actual: String
 * @param {string} y
 */
function b(y) {
        return +y;
}

/**
 * Expected: Number
 * Actual: Number|String
 * @param {string} z
 */
function c(z) {
        return z|0;
}

/**
 * Expected: String
 * Actual: String
 * @param {number} n
 */
function ok(n) {
        return ''+n;
}