<?php
interface A {
    static function make(): A;
}
class B implements A {
    static function make(): A {
        return new B();
    }
}
trait C {
    static function make(): C;
}
