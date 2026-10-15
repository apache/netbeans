<?php
namespace ExampleNamespace;

use Some\Classes\{ClassA, ClassB, ClassC as C};

class Example extends Foo implements Bar, Baz {

    public function ifExample ($a, $b) {
        if (convert($a) > $b)
            echo "a is bigger than b";
        elseif ($a == $b)
            echo $a." is equal to ".$b[0];
        else
            $result = getText($this->property1, $this->property2) ;
	$result = $a < $b ? $a : $b;
    }

    public function coalescingOperatorExample(?string $a): string {
        return $a ?? 'default value';
    }

public function forExample() {
    for ($i = 1; $i <= 10; $i++) echo 'Item: ';
}

public function foreachEample() {
$arr = array(1, 2, 3, 4, "b"=>5, "a"=>6);
foreach ($arr as &$value)
    $value = (int)$value * 2;
}

public function whileExample() {
$i = 1;
        while ($i <= 10)
            echo $i++;
}

public function doWhileExample($i) {
do
    echo $i--;
while ($i > 0);
}

public function rest() {
    $this->first()->second()->third();
    $this->foo($bar, $baz, $bat);
}

}

// if there is no newline within "()", don't wrap
function example(string $pram1,int $param2): string {
    echo "Example";
}

function example2(
string $pram1,
int $param2): string {
    echo "Example";
}

// if there is no new line within "()", don't wrap
foreach ($array as $key => $value) {
    echo $key . "=>" . $value . PHP_EOL;
}

foreach (
        $array as $key => $value) {
    echo $key . "=>" . $value . PHP_EOL;
}
?>
