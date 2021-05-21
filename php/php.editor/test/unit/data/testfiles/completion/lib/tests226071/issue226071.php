<?php

class A
{
    public $a = 1;
    public $b = 1;
}

class B
{
    public static $a;
    public $b;

    public function __construct()
    {
        $this->b = new A();
        self::$a = new A();
    }

    public function test()
    {
        $this->b->;
        self::$a->;
        static::$a->;
        B::$a->;
    }
}
?>