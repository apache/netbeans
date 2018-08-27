<?php
class BlaBla {
    private $count = array();//see this line
    function count() {
        self::$count = 10;
        self::$count[0] = 10;
    }
}
?>