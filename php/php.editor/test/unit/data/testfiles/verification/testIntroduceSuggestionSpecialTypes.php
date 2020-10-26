<?php

class ParentClass {}
class SpecialTypes extends ParentClass {
    public function testStatic(): static {
        return new static;
    }
    public function testSelf(): self {
        return new self;
    }
    public function testSelf(): parent {
        return new parent;
    }
}
