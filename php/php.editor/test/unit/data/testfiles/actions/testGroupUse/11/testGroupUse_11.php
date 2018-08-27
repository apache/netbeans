<?php

namespace A;

function fa() {
    echo __FUNCTION__ . PHP_EOL;
}
function faa() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace B;

function fb() {
    echo __FUNCTION__ . PHP_EOL;
}
function fbb() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace C;

function fc() {
    echo __FUNCTION__ . PHP_EOL;
}
function fcc() {
    echo __FUNCTION__ . PHP_EOL;
}

namespace Run;

use \A\fa;
use \A\faa;
use \B\fb;
use \C\fc;
use \C\fcc;

fa();
fb();
fc();
fcc();
