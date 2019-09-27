<?php

function from() {
    yield "from";
}

function gen($key, $value) {
    $i = 0;
    yield;
    (yield);
    yield yield;
    (yield yield);
    yield yield 2;
    (yield yield 2);
    yield $i;
    (yield $i);
    yield $key => $value;
    (yield $key => $value);

    $a = yield;
    $a = (yield);
    $a = yield yield;
    $a = (yield yield);
    $a = yield yield 2;
    $a = (yield yield 2);
    $a = yield ++$i;
    $a = (yield ++$i);
    $a = yield $key => $value;
    $a = (yield $key => $value);
    $a = $b = yield $key => $value;
    $a = $b = (yield $key => $value);
    $a = ($b = (yield $key => $value));
    ($a = yield);

    yield yield from from();

    $from = from();
    yield from $from;
    $from = from();
    (yield from $from);
    yield from from();
    (yield from from());
    yield from new ArrayIterator([2, 3, 4]);
    (yield from new ArrayIterator([2, 3, 4]));

    $from = from();
    $c = yield from $from;
    $from = from();
    $c = (yield from $from);
    $c = yield from from();
    $c = (yield from from());
    $c = yield from new ArrayIterator([2, 3, 4]);
    $c = (yield from new ArrayIterator([2, 3, 4]));
    $c = $d = yield from from();
    $c = $d = (yield from from());
    $c = ($d = (yield from from()));
    ($c = ($d = (yield from from())));
    return yield from from();
    return (yield from from());
}

foreach (gen("key", "value") as $value) {
    var_dump($value);
}
