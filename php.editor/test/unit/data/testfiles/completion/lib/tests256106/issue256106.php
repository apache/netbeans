<?php

namespace Issue256106;

trait BaseTrait {

    public $publicTraitField;
    private $privateTraitField;
    protected $protectedTraitField;
    public static $publicStaticTraitField;
    private static $privateStaticTraitField;
    protected static $protectedStaticTraitField;

    public static function publicStaticTraitMethod() {
    }

    private static function privateStaticTraitMethod() {
    }

    protected static function protectedStaticTraitMethod() {
    }

    /**
     * @return $this
     */
    public function publicTraitMethod() {
        return $this;
    }

    /**
     * @return $this
     */
    private function privateTraitMethod() {
        return $this;
    }

    /**
     * @return $this
     */
    protected function protectedTraitMethod() {
        return $this;
    }

    /**
     * @return self
     */
    public function publicTraitOnlyReturnSelfMethod() {
    }

    /**
     * @return self
     */
    protected function protectedTraitOnlyReturnSelfMethod() {
    }

    /**
     * @return static
     */
    public function publicTraitOnlyReturnStaticMethod() {
    }

    /**
     * @return static
     */
    private function privateTraitOnlyReturnStaticMethod() {
    }

}

class ParentClass {

    public $publicParentField;
    private $privateParentField;
    protected $protectedParentField;

    public function publicParentMethod() {
        return $this;
    }

    private function privateParentMethod() {
        return $this;
    }

    protected function protectedParentMethod() {
        return $this;
    }

}

class ChildClass extends ParentClass {

    use BaseTrait;

    public $publicChildField;
    private $privateChildField;
    protected $protectedChildField;

    /**
     * @return $this
     */
    public function publicChildMethod() {
        return $this;
    }

    /**
     * @return $this
     */
    private function privateChildMethod() {
        return $this;
    }

    /**
     * @return $this
     */
    protected function protectedChildMethod() {
        return $this;
    }

    public function enclosingTest() {
        $this->protectedParentMethod()->privateTraitMethod()->privateChildMethod(); // test
        $this->protectedParentMethod()->privateTraitOnlyReturnStaticMethod()->privateTraitMethod(); // test static
        $this->protectedParentMethod()->protectedTraitOnlyReturnSelfMethod()->privateTraitMethod(); // test self
    }

}

// not enclosing
$childClass = new ChildClass();
$childClass->publicChildMethod()->publicTraitMethod()->publicChildMethod(); // test
$childClass->publicTraitOnlyReturnStaticMethod()->publicTraitMethod(); // test static
$childClass->publicTraitOnlyReturnSelfMethod()->publicTraitMethod(); // test self

// enclosing
$childClass->enclosingTest();
