
<?php
$func1 = function(){echo "anonymous function1" . PHP_EOL;};

$func2 = function() {
    return function() {echo "anonymous function2" . PHP_EOL;};
};

function test() {
}

test();
