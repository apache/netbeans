<?php

namespace {

    const TEST1 = [0, 1];

}

namespace Issue268712_A {

    const TEST2 = [[0, 1], 1];
    const TEST3 = \TEST1[0];

}

namespace Issue268712_B\Sub {
    const SUB = ["sub" => "sub", "array" => [0, 1]];
}

namespace Issue268712_B {

    const TEST4 = \Issue268712_A\TEST3;
    const TEST5 = \Issue268712_A\TEST2[0][1];
    const TEST6 = ["test" => "test", "array" => [0, 1]];

    $test = 0;
    $const1 = \TEST1[$test];
    $const2 = \Issue268712_A\TEST2[1];

    echo $const1 . PHP_EOL;
    echo $const2 . PHP_EOL;

    echo TEST4 . PHP_EOL;
    echo TEST5 . PHP_EOL;
    echo \Issue268712_B\TEST6["test"] . PHP_EOL;
    echo \Issue268712_B\TEST6["array"][0] . PHP_EOL;
    echo Sub\SUB["sub"] . PHP_EOL;
    echo Sub\SUB["array"][0] . PHP_EOL;
    echo namespace\TEST6["test"] . PHP_EOL;
    echo namespace\TEST6["array"][0] . PHP_EOL;

}
