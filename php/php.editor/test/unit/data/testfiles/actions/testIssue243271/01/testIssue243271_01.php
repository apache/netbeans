<?php
namespace foo\bar;

use baz\SomeClass as SomeClassAlias;

class Test {
    /**
     * 
     * @param baz\SomeClass $someClass
     */
    public function getSomething(SomeClassAlias $someClass) {}
}