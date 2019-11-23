<?PHP

interface MyTestInterface {
    public function requide_method();
}

trait MyTestTrait {
    public function requide_method() {}
}

trait MyTestTrait2 {
    public function requide_method2() {}
}

class MyTestClass2 implements MyTestInterface {
    use MyTestTrait;
    use MyTestTrait2;
}

$x = new MyTestClass2();
$x->requide_method();

class MyTestClass1 implements MyTestInterface {
    use MyTestTrait2;
    use MyTestTrait;
}

$y = new MyTestClass1();
$y->requide_method();

?>