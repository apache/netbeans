<?php

class Foo {

    const bar = "";
    public static $bar;
    public $foobar;

    public function test() {
        $this->foobar[Foo::bar];
        $this->foobar[Foo::$bar];
    }

}
?>