<?php
namespace Run;

use A\ {
    ClsA,
    B\ClsAB,
    B\C\ClsABC
};

$a03 = new ClsA();
$a03->test();
$ab03 = new ClsAB();
$ab03->test();
$abc03 = new ClsABC();
$abc03->test();
