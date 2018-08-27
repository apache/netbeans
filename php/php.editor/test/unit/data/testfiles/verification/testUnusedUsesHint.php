<?php
//START
use My\Space\Whatever;
use Your\Space\Something,
        My\Space\Something as S2;
use Unused\Simple\Statement;

class Foo1 {

    function functionName() {
        Whatever::bar();
        S2::blah();
    }

}

use What\MyClass;
use Faces\IFace;

class Foo2 extends MyClass implements IFace {}


// it IS used - in PHPDoc
use Foo\Bar\Baz\Def;

class ClassName {

    /**
     * @return Def
     */
    function functionName() {
    }

}

// it IS NOT used
use Foo\Bar\Baz\Dex;

class ClassName1 {

    /**
     * @return Baz\Dex
     */
    function functionName() {
    }

}


use MyNamespace\MyClass1;
use MyNamespace\MyClass2;
use MyNamespace\MyClass3;

trait MyTrait {
    public function method(MyClass1 $o1) {
        $o2 = new MyClass2;
        $o3 = new MyClass3;
    }
}

//END
?>