<?php

namespace Foo;
use Bar\MyClass;

class TypedPropertiesClass {

    public MyClass $myClass;
    public ?MyClass $myClass2;
    public \Bar\MyClass $myClass3;
    public ?\Bar\MyClass $myClass4;

    public function test(): void {
        $this->myClass->publicTestMethod();
        $this->myClass2->publicTestMethod();
        $this->myClass3->publicTestMethod();
        $this->myClass4->publicTestMethod();
        $this->myClass::publicStaticTestMethod();
        $this->myClass2::publicStaticTestMethod();
        $this->myClass3::publicStaticTestMethod();
        $this->myClass4::publicStaticTestMethod();
    }

}

namespace Bar;
class MyClass {

    public function publicTestMethod(): void {
    }

    public static function publicStaticTestMethod(): void {
    }
}
