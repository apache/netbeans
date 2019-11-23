<?php
class Monitor {

    public function __construct() {
        echo "Monitor constructor\n";
    }
    private function __get($name) {
        echo("getter ".$name."\n");
    }

    private function __set($name, $value) {
        echo("setter ".$name." value: ".$value."\n");
    }

    private function test() {

    }
}
?>