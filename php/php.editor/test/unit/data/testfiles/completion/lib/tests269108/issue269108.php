<?php

class A {

    /**
     * returnStatic method.
     *
     * @return static
     */
    public function returnStatic() {
        return new static;
    }

    /**
     * staticReturnStatic method.
     *
     * @return static
     */
    public static function staticReturnStatic() {
        return new static;
    }

    /**
     * returnSelf method.
     *
     * @return self
     */
    public function returnSelf() {
        return new self;
    }

    /**
     * staticReturnSelf method.
     *
     * @return self
     */
    public static function staticReturnSelf() {
        return new self;
    }

    /**
     * returnThis method.
     *
     * @return $this
     */
    public function returnThis() {
        return $this;
    }


    public function testA() {
        return "testA";
    }

    public static function staticTestA() {
        return "staticTestA";
    }

}

class B extends A {

    /**
     * {@inheritdoc}
     */
    public function returnStatic() {
        return parent::returnStatic();
    }

    /**
     * {@inheritdoc}
     */
    public static function staticReturnStatic() {
        return parent::staticReturnStatic();
    }

    /**
     * {@inheritdoc}
     */
    public function returnSelf() {
        return parent::returnSelf();
    }

    /**
     * {@inheritdoc}
     */
    public static function staticReturnSelf() {
        return parent::staticReturnSelf();
    }

    /**
     * {@inheritdoc}
     */
    public function returnThis() {
        return parent::returnThis();
    }

    public function testB() {
        return "testB";
    }

    public static function staticTestB() {
        return "staticTestB";
    }

}

class C extends B {

    public function testC() {
        return "testC";
    }

    public static function staticTestC() {
        return "staticTestC";
    }

}

// return static
echo get_class((new C)->returnStatic()) . PHP_EOL; // C
echo get_class(C::staticReturnStatic()) . PHP_EOL; // C

echo (new C)->returnStatic()->testC() . PHP_EOL; // testC
echo C::staticReturnStatic()->staticTestC() . PHP_EOL; // staticTestC
echo C::staticReturnStatic()::staticTestC() . PHP_EOL; // staticTestC (PHP7)

$c = new C();
echo $c::staticReturnStatic()->staticTestC() . PHP_EOL; // staticTestC
echo $c::staticReturnStatic()::staticTestC() . PHP_EOL; // staticTestC (PHP7)
echo $c->returnStatic()::staticTestC() . PHP_EOL; // staticTestC (PHP7)

// return self
echo get_class((new C)->returnSelf()) . PHP_EOL; // A
echo get_class(C::staticReturnSelf()) . PHP_EOL; // A

echo (new C)->returnSelf()->testA() . PHP_EOL; // testA
echo C::staticReturnSelf()->staticTestA() . PHP_EOL; // staticTestA
echo C::staticReturnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)

echo $c::staticReturnSelf()->staticTestA() . PHP_EOL; // staticTestA
echo $c::staticReturnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)
echo $c->returnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)

// return this
echo get_class((new C)->returnThis()) . PHP_EOL; // C

echo (new C)->returnThis()->testC() . PHP_EOL; // testC

echo $c->returnThis()::staticTestC() . PHP_EOL; // staticTestC (PHP7)
