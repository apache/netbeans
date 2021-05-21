<?php
abstract class SimpleObject {
    public function methodOne() {
    }

    public function methodTwo() {
    }
}

class ComplexObject extends SimpleObject {
    public function methodThree() {
    }
}

class Foo {

/**
 * @var SimpleObject
 */
    private $_object;

    public function __construct(SimpleObject $example) {
        $this->_object = $example;
    }

    /**
     * Example method returns something like SimpleObject
     *
     * @return SimpleObject
     */
    public function getOject() {
        return $this->_object;
    }
}

$foo = new Foo();
$fooObject = $foo->getOject();
/*test 1*/$fooObject->methodOne();
/* @var $fooObject ComplexObject */
/*test 2*/$fooObject->methodOne();
?>
