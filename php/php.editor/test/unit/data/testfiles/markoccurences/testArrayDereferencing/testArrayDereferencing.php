<?php

class MyClass {
    /**
     * @var MyClass[]
     */
    public $field;
    /**
     * @return MyClass[]
     */
    public function getArray() {
        return array(new MyClass());
    }

    public function foo() {}
}

$myClass = new MyClass();

$myClass->field[0]->getArray()[][]->foo();

$myClass->getArray()[0][]->foo();

/**
 * @return MyClass[]
 */
function functionName() {
    
}


functionName()[0]->foo();

?>