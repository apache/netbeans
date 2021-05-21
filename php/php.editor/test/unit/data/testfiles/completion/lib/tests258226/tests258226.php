<?php

class A {
    public function myA() {
    }
}

namespace TEST;
class testClass{
	public function testMethod():\A{
	}
	public static function testStaticMethod():\A{
	}
}
$testInstance = new testClass();
$testInstance->testMethod()->myA();
testClass::testStaticMethod()->myA();
