<?php
class A {
    public function one() {
        return  'one';
    }

    public function two() {
        return 'two';
    }
}

class B {
    public static function getClass($name) {
        return new $name;
    }
}

class C {
    /**
     * @var A
     */
    public $a;

    public function  __construct() {
        $this->a = B::getClass('A');
    }

    public function go_to_declaration_bug() {
        /*
         * Go to Declaration doesn't work here but list of methods is correct.
         * Why so?
         */
         $this->a->one();

    }

    public function go_to_declaration_ok() {
        // Go to Declaration works fine
        $a = new A();
        $a->one();
    }

}
?>
