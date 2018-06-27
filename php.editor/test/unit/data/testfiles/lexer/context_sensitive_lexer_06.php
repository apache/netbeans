<?php
namespace A;

const CONSTANTA = "CONSTANTA";
function testA() {
    echo "testA" . PHP_EOL;
}

class MyA {}
class MyAA {}

namespace B;

use A\{
    MyA,
    const CONSTANTA,
    function testA,
    function  testA AS mytestA,
    MyAA
};
