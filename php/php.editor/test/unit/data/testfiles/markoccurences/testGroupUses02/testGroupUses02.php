<?php
namespace A;
const CONSTANT = "CONSTANT";
function test() {
    echo "test" . PHP_EOL;
}
class MyA {}

namespace B;

use A\{
    MyA,
    const CONSTANT,
    function test,
    function test AS mytest
};

new MyA();
echo CONSTANT; // CONSTANT
test(); // test
mytest(); // test
