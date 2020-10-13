<?php
declare(strict_types=1);

namespace ExampleNamespace;

use Some\Classes\{ClassA, ClassB, ClassC as C};

class Example implements Iface1, Iface2, Iface3 {

private ClassA|ClassB|null $unionType;

    public function ifExample ($a, $b) {
        if (convert($a) > $b) {
            echo "a is bigger than b";
        } elseif ($a == $b) {
            echo $a." is equal to ".$b[0];
        } else {
            $result = getText($this->property1, $this->property2) ;
        }
	$result = $a < $b ? $a : $b;
    }

public function forExample() {
    for ($i = 1; $i <= 10; $i++) {echo 'Item: '; echo $i;}
}

public function foreachEample() {
$arr = array(1, 2, 3, 4, "b"=>5, "a"=>6);
foreach ($arr as &$value) {
    $value = (int)$value * 2;
}
}

public function whileExample() {
$i = 1;
        while ($i <= 10) {
            echo $i++;
        }
}

public function doWhileExample($i) {
do {
    echo $i--;
} while ($i > 0);
}

public function switchExample() {
switch ($i) {
    case 0:
        echo "i equals 0";
        break;
    case 1:
        echo "i equals 1";
        break;
}
}

public function matchExample() {
$result = match ($i) {
    1,2,3 => "1, 2, or 3",
    4,5, => "4 or 5",
    default => $this->getDefaultValue(),
};
}

public function getDefaultValue(): int {
    return 10;
}

public function tryExample() {
    try {
    echo inverse(5) . "\n";
} catch (Exception $e) {
    echo 'Caught exception: '.  $e->getMessage(). "\n";
} finally {
    echo "Finally block";
}

}

public function anonymousClassExample($arg) {
    $instance = new class ($arg) extends Anonymous {
        public function __construct($arg) {
        }
        public function anon() {
            echo "anonymous";
        }
    };
    return $instance;
}

// Wrapping: Method Parameters must be set
public function alignParamsExample($arg1,
        $arg2, $arg3,
        $arg4, $arg5) {
}

public function unionTypesExample(
        Class1|Class2|null $object
        ): int|float|null {
    return null;
}

public function nullsafeOperatorExample(object $object){
    $object?->nullsafe();
}
}

// Wrapping: Method Call Arguments must be set
(new Example())->alignParamsExample('one',
        'two', 'three',
        'four', 'five');

$shortName=10;
$veryLooongName=20;
$data=[
'short_key'=>10,
'very_looong_key'=>100,
];

?>
