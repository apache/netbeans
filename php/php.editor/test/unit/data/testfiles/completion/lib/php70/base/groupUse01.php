<?php
namespace Run;

use A\{
    ClsA,
    B\ClsAB,
    B\C\ClsABC
};

$a01 = new ClsA();
$a01->test();
$ab01 = new ClsAB();
$ab01->test();
$abc01 = new ClsABC();
$abc01->test();
