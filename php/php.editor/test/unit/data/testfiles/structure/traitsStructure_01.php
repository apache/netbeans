<?php

trait MyTrait {
    public $traitField = 10;
    public function traitFoo() {}
}

class TraitedClass {
    use MyTrait;
}

?>