<?php

use function Name\Space\fnc;
use function Name\Space\fnc2;
use Name\Space\Bar;
use Name\Space\Bar2;

class ClassName {

    function __construct() {
        Name\Space\fnc();
        Name\Space\fnc2();
        new Name\Space\Bar();
        new Name\Space\Bar2();
    }

}
?>