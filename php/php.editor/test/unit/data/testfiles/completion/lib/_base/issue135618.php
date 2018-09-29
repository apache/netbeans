<?php
class A135618 {
    const AAA = "aaa";
}
class B135618 extends A135618{
    const BBB = "bbb";
    function testMe() {
        self::BBB;
    }
}
A135618::AAA;
B135618::AAA;
?>