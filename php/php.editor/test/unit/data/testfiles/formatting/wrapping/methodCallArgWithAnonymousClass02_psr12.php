<?php
test(new class {
public function foo() {
}
});

test(
new class {
public function foo() {
}
});

test($arg1,
new class {
public function foo() {
}
});

test($arg1,new class {
public function foo() {
}
});

test($arg1,new class {
public function foo() {
}
},$arg2,$arg3);

test(
$arg1,
new class {
public function foo() {
}
});

// method
$test->test(new class {
public function foo() {
}
});

$test->test(
new class {
public function foo() {
}
});

$test->test($arg1,
new class {
public function foo() {
}
});

$test->test($arg1,new class {
public function foo() {
}
});

$test->test($arg1,new class {
public function foo() {
}
},$arg2,$arg3);

$test->test(
$arg1,
new class {
public function foo() {
}
});
