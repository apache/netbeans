<?php

function voidReturnType(): void { // func
}

function invalidInParameter(void $void): void { // func
}

class TestClass {
    public function returnType(): void { // class
    }
    public function invalidInParameter(void $void): void { // class
    }
}

interface TestInterface {
    public function returnType(): void; // interface
    public function invalidInParameter(void $void): void; // interface
}
