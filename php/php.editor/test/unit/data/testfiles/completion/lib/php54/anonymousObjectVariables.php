<?php
class AnonymousObject {
    public $bar;
    function baz() {}
}

(new AnonymousObject)->bar;

(new AnonymousObject(new Bar($baz, $bat)))->baz();
?>