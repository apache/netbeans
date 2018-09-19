/**
 * This is function test1.
 * @param a First parameter
 * @param b Second parameter
 */
function test1 (a, b)
{
   test2 ();
}

/**
 * This is function test2.
 * @param a First parameter
 */
function test2 (a) {
   test1 (0, 1);
}