<?php

class TestClass {}

interface TestInterface {}

function testReturnType(): ?TestClass {
}

function testParameterType(?TestClass $testClass, ?TestInterface $testInterface) {
}

class NullableTypesClass {

    public function testClassReturnType(): ?TestClass {
    }

    public function testClassParameterType(?TestClass $testClass, ?TestInterface $testInterface) {
    }

    public static function testStaticClassReturnType(): ?TestInterface {
    }

    public static function testStaticClassParameterType(?TestClass $testClass, ?TestInterface $testInterface) {
    }

}

trait NullableTypesTrait {

    public function testTraitReturnType(): ?TestClass {
    }

    public function testTraitParameterType(?TestClass $testClass, ?TestInterface $testInterface) {
    }

}

interface NullableTypesInterface {

    public function testInterfaceReturnType(): ?TestClass;

    public function testInterfaceParameterType(?TestClass $testClass, ?TestInterface $testInterface);

}


