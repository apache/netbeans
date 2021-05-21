<?php

trait MyTrait {
    public $traitField = 10;
    public function traitFoo() {}
}

trait TraitedTrait {
    use MyTrait;
    public $secondFiled = 20;
    public function secondFoo() {}
}

?>