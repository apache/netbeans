<?php
namespace Run;

use \A\{
    ClsA,
    B\ClsAB,
    B\C\ClsABC
};

$a02 = new ClsA();
$a02->test();
$ab02 = new ClsAB();
$ab02->test();
$abc02 = new ClsABC();
$abc02->test();
