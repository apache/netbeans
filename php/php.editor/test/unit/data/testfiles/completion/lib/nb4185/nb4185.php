<?php

class A {

    /**
     * returnStatic method.
     *
     * @return static
     */
    public function returnStatic(): self {
        return new static;
    }

    /**
     * staticReturnStatic method.
     *
     * @return static
     */
    public static function staticReturnStatic(): self {
        return new static;
    }

    /**
     * returnSelf method.
     */
    public function returnSelf(): self {
        return new self;
    }

    /**
     * staticReturnSelf method.
     */
    public static function staticReturnSelf(): self {
        return new self;
    }

    /**
     * returnThis method.
     *
     * @return $this
     */
    public function returnThis(): self {
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

    public function testB() {
        return "testB";
    }

    public static function staticTestB() {
        return "staticTestB";
    }

}

// return static
echo get_class((new B)->returnStatic()) . PHP_EOL; // B
echo get_class(B::staticReturnStatic()) . PHP_EOL; // B

echo (new B)->returnStatic()->testB() . PHP_EOL; // testB
echo B::staticReturnStatic()->staticTestB() . PHP_EOL; // staticTestB
echo B::staticReturnStatic()::staticTestB() . PHP_EOL; // staticTestB (PHP7)

$b = new B();
echo $b::staticReturnStatic()->staticTestB() . PHP_EOL; // staticTestB
echo $b::staticReturnStatic()::staticTestB() . PHP_EOL; // staticTestB (PHP7)
echo $b->returnStatic()::staticTestB() . PHP_EOL; // staticTestB (PHP7)

// return self
echo get_class((new B)->returnSelf()) . PHP_EOL; // A
echo get_class(B::staticReturnSelf()) . PHP_EOL; // A

echo (new B)->returnSelf()->testA() . PHP_EOL; // testA
echo B::staticReturnSelf()->staticTestA() . PHP_EOL; // staticTestA
echo B::staticReturnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)

echo $b::staticReturnSelf()->staticTestA() . PHP_EOL; // staticTestA
echo $b::staticReturnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)
echo $b->returnSelf()::staticTestA() . PHP_EOL; // staticTestA (PHP7)

// return this
echo get_class((new B)->returnThis()) . PHP_EOL; // B

echo (new B)->returnThis()->testB() . PHP_EOL; // testB

echo $b->returnThis()::staticTestB() . PHP_EOL; // staticTestB (PHP7)
