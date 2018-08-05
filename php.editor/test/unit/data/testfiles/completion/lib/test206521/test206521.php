<?php

namespace MC;

class MockClass
{
    public static $a = 1;
}

namespace TC;
class TestCase
{
    public static $b = 1;
    public function test()
    {
        TestCase::$b;
        \MC\MockClass::$a;
    }
}
?>