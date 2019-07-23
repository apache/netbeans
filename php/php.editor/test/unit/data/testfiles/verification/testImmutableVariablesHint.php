<?php
//START

Cls::method('x', function () {
    $component = "";
    return $component;
});
Cls::method('y', function () {
    $component = "";
    return $component;
});

try {
    $foo = "";
} catch (Exception $e) {
    $foo = false;
}

$test = "ok";
$test = "2 - should fail";

class y {
    private static function x() {
        $h = '';
        self::$h = 1;
    }
}

for ($issue210698=0; $issue210698 <=20; $issue210698 = $issue210698 +4) {
    print $issue210698.' ';
}

if (true)
    $a = 0;
else
    $a = 1;

function b($bb = true) {
    $bb = false;
}

function c($cc) {
    $cc = false;
}
//END
?>