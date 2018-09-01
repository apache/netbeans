<?php
interface MyIface {
    const FOO = 12;
    public static function functionName();
    public function doSomething();
}

interface MySecondIface extends MyIface {
    const BAR = 25;
    public static function anotherStatic();
    public function doAnything();
}
?>