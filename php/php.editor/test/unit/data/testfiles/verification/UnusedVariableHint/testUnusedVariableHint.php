<?php
//START
function createsLocalVarScope() {
class UnusedVariableClassName {
    public static $staticField;
    public function __construct() {}
    function functionName() {$this->functionName();}
    function formalParams($first, $second) {}
}
$GLOBALS["a"];
$_SERVER["a"];
$_GET["a"];
$_POST["a"];
$_FILES["a"];
$_COOKIE["a"];
$_SESSION["a"];
$_REQUEST["a"];
$_ENV["a"];
echo $echoUsed;

$simpleUnused;

include  $incUsed . '/foo.php';

$funcName = "myFunc";
$funcName();

$c = new UnusedVariableClassName();
$methodName = "functionName";
$c->$methodName();

if ($ifUsed) {}

$result = ($instanceUsed instanceof Foo);

$postfixUsed++;
++$prefixUsed;

$cloned = clone $c;

$casted = (int) $flt;

$assign = $rightUsed;

$condRes = $cond ? $true : $false;

function functionName() {
    return $retUsed;
}

switch ($swiUsed) {
    default:
        break;
}

throw $ex;

$cls = new $clsName($prm);

do {
} while ($doUsed);

foreach ($arrayUsed as $key => $value) {
}

for ($indexUsed = 0; $indexUsed < 5; $indexUsed++) {
}

$staticClassUsed::method();

while ($whileUsed) {
}

$fnc = function($formUsed) use($lexUsed) {};

$staticAnotherClass::$staticField;

abstract class AbstractFoo
{
    abstract public function notHandled(array $array);
}

function FilterByNameStart($field)
{
    return function($param) use ($field) {return $field == $param;};
}

function($param1) use ($field1) {return $field1;};

function($param11) use ($field11) {return $param11;};

$instanceOf = "\Foo";
if ($ins instanceof $instanceOf) {

}

$omg = 60;
$gom = 60;
switch ($omg) {
    case $gom: break;
}

$variableCompact = 'test';
compact('variableCompact');

function test() {
    $index = 1;
    $used = 1;

    function ($index) use ($used) {};
}

function test2() {
    function ($index) { };
    function ($index) { };
}

function test3() {
    function ($index) { };
    function () { $index = 5; };
}
}
?>

<?= $usedShortEcho; ?>

<?php
//END
?>