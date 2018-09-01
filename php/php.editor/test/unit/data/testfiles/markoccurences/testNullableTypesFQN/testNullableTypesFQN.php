<?php
namespace Test\Sub;
class TestClass {}

interface TestInterface {}

namespace Test\Sub2;

function testReturnType(): ?\Test\Sub\TestClass {
}

function testParameterType(?\Test\Sub\TestClass $testClass, ?\Test\Sub\TestInterface $testInterface) {
}

class NullableTypesClass {

    public function testClassReturnType(): ?\Test\Sub\TestClass {
    }

    public function testClassParameterType(?\Test\Sub\TestClass $testClass, ?\Test\Sub\TestInterface $testInterface) {
    }

    public static function testStaticClassReturnType(): ?\Test\Sub\TestInterface {
    }

    public static function testStaticClassParameterType(?\Test\Sub\TestClass $testClass, ?\Test\Sub\TestInterface $testInterface) {
    }

}

trait NullableTypesTrait {

    public function testTraitReturnType(): ?\Test\Sub\TestClass {
    }

    public function testTraitParameterType(?\Test\Sub\TestClass $testClass, ?\Test\Sub\TestInterface $testInterface) {
    }

}

interface NullableTypesInterface {

    public function testInterfaceReturnType(): ?\Test\Sub\TestClass;

    public function testInterfaceParameterType(?\Test\Sub\TestClass $testClass, ?\Test\Sub\TestInterface $testInterface);

}


