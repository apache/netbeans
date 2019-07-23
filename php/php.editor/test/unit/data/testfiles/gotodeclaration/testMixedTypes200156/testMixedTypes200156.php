<?php

/**
 * @property Foo|Bar $property
 */
class Foo {

    /**
     * @var Foo|Bar
     */
    public $field;
    
    function fooMethod() {
        
    }

}

/**
 * @method Foo|Bar m1() m1(Foo|Bar $param) a magic method declaration 
 */
class Bar {

    function barMethod() {
        
    }

}


$foo = new Foo();
$foo->field->barMethod();
$foo->property->barMethod();


?>