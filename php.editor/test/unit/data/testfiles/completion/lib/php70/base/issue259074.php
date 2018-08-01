<?php
namespace GroupUse1;
const CONST_1 = "CONST_1";
const CONST_2 = "CONST_2";

class C1 {
}

class C2 {
}

function test1() {
}

function test2() {
}

namespace GroupUse2;

use GroupUse1\{
    C1, /* comment */
    C2
};
use function GroupUse1\{
    test1, // comment
    test2
};
use const GroupUse1\{
    CONST_1, // comment
    CONST_2
};
