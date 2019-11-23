<?php
namespace Foo\Bar\Baz;

function out($methodName) {
    echo "    " . $methodName . "()" . PHP_EOL;
}

class Delegate
{

    /**
     * @var BaseClass
     */
    private $baseClass;

    public function __construct(BaseClass $baseClass) {
        $this->baseClass = $baseClass;
    }

    public function publicDelegateMethod() {
        out("publicDelegateMethod");
        return $this;
    }

    private function privateDelegateMethod() {
        out("privateDelegateMethod");
        return $this;
    }

    protected function protectedDelegateMethod() {
        out("protectedDelegateMethod");
        return $this;
    }
}

class BaseClass
{
    private $delegate = false;

    public function setDelegate($delegate) {
        out("setDelegate");
        $this->delegate = $delegate;
    }

    /**
     * Multiple return types.
     */
    public function multipleReturnStatements() {
        out("multipleReturnStatements");
        if ($this->delegate) {
            return new Delegate($this);
        }
        return $this;
    }

    /**
     * Multiple return types (only return tag).
     *
     * @return $this|Delegate
     */
    public function multipleReturnTags() {
    }

    /**
     * Multiple return types (both return tag and statement).
     *
     * @return $this|Delegate
     */
    public function multipleReturnTagsAndStatements() {
        out("multipleReturnTagsAndStatements");
        if ($this->delegate) {
            return new Delegate($this);
        }
        return $this;
    }

    /**
     * @return \this
     */
    public function returnTagWithOldThis() {
        out("returnTagWithOldThis");
    }

    /**
     * @return $this
     */
    public function publicBaseMethod() {
        out("publicBaseMethod");
        return $this;
    }

    private function privateBaseMethod() {
        out("privateBaseMethod");
        return $this;
    }

    protected function protectedBaseMethod() {
        out("protectedBaseMethod");
        return $this;
    }

    public function publicBaseVoidMethod() {
        out("publicBaseVoidMethod");
    }
}

class ExClass extends BaseClass
{
    public function test() {
        $this->setDelegate(false);
        $this->multipleReturnStatements()->privateExMethod()->multipleReturnStatements(); // test
        $this->setDelegate(true);
        $this->multipleReturnTagsAndStatements()->publicDelegateMethod()->publicDelegateMethod(); // test
        out("test");
    }

    public function publicExMethod() {
        out("publicExMethod");
        return $this;
    }

    private function privateExMethod() {
        out("privateExMethod");
        return $this->protectedBaseMethod();
    }

    protected function protectedExMethod() {
        out("protectedExMethod");
        return $this;
    }

    public function publicExVoidMethod() {
        out("publicExVoidMethod");
    }

}

$base = new BaseClass();
echo PHP_EOL . "[\$base->publicBaseMethod()->multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod()]" . PHP_EOL;
$base->setDelegate(true);
$base->publicBaseMethod()->multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod(); // test

echo PHP_EOL . "[\$base->multipleReturnTagsAndStatements()->publicBaseVoidMethod()]" . PHP_EOL;
$base->setDelegate(false);
$base->multipleReturnTagsAndStatements()->publicBaseVoidMethod(); // test

$exClass = new ExClass();
echo PHP_EOL . "[\$exClass->test()]" . PHP_EOL;
$exClass->test();

echo PHP_EOL . "[\$exClass->publicBaseMethod()->publicExClassMethod()->multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod()]" . PHP_EOL;
$exClass->setDelegate(true);
$exClass->publicBaseMethod()->publicExMethod()->multipleReturnStatements()->publicDelegateMethod()->publicDelegateMethod(); // test

echo PHP_EOL . "[\$exClass->publicExMethod()->publicBaseMethod()->multipleReturnStatements()->publicExMethod()->publicBaseMethod()]" . PHP_EOL;
$exClass->setDelegate(false);
$exClass->publicExMethod()->publicBaseMethod()->multipleReturnStatements()->publicExMethod()->publicBaseMethod();

// cannot run actually because it is not returned anything (only @return tag)
echo PHP_EOL . "[\$base->multipleReturnTags()->multipleReturnStatements()]" . PHP_EOL;
$base->multipleReturnTags()->multipleReturnStatements(); // test
$base->returnTagWithOldThis()->multipleReturnStatements(); // test
$exClass->multipleReturnTags()->multipleReturnStatements(); //test
