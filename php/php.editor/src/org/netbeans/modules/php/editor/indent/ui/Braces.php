<?php
namespace ExampleNamespace;

use Some\Classes\{ClassA, ClassB, ClassC as C};

class ClassExample extends AbstractClass
{
    public function printOut() {
        print $this->getValue() . "\n";
    }

    public function printValue($a) {
	if ($a) {
	    for ($i = 1; $i <= 10; $i++) {
    echo $i;
}
	}
else {
echo "NetBeans";
}
    }
}

$anonymous = new class extends AnonymousExample {
    public function main() {
        return "anonymous";
    }
};

function getFruit() {
    return "Apple";
}

?>
