<?php
namespace A;

const CONSTANTA = "CONSTANTA";
const CONSTANTB = "CONSTANTB";
function testA() {
    echo "testA" . PHP_EOL;
}

class MyA {}
class MyAA {}

namespace B;

use A\{
    MyA,
    const CONSTANTA,
    const CONSTANTB,
    function testA,
    function  testA AS mytestA,
    MyAA
};
