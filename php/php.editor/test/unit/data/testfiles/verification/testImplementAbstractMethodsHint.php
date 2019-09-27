<?php
//START

abstract class AbstractClass {
    abstract public function abstractFoo();
}

class ExtendingClass extends AbstractClass {

}

interface InterfaceName {
    public function abstractBar();
}

class ImplementingClass implements InterfaceName {

}

class ImplementA {
    function foo() {}
}
interface ImplementB {
    function foo();
}
class ImplementC extends ImplementA implements ImplementB {

}

interface B {
    function example();
}

trait X {
    function example() { }
}

class A implements B {
    use X;
}
////////////////////////////
interface I1 {
    public function m();
}

trait T1 {
    public $foo;
    public function m() {}
}

abstract class A1 implements I1 {
    use T1;
}

class C1 extends A1 {}
////////////////////////////

interface MyFace {
    public function toImplement();
}

class MyParent implements MyFace {
    public function toImplement() {
    }
}

abstract class AbstractSuper extends MyParent {
}

class Datagrid extends AbstractSuper implements MyFace {
}

class MyCls extends Datagrid {
}

//END
?>