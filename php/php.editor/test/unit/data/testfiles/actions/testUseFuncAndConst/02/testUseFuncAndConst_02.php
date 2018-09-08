<?php

use const Name\Space\FOO;
use const Name\Space\FOO2;
use function Name\Space\fnc;
use function Name\Space\fnc2;
use Name\Space\Bar;
use Name\Space\Bar2;

class ClassName {

    function __construct() {
        echo Name\Space\FOO;
        echo Name\Space\FOO2;
        Name\Space\fnc();
        Name\Space\fnc2();
        new Name\Space\Bar();
        new Name\Space\Bar2();
    }

}
?>