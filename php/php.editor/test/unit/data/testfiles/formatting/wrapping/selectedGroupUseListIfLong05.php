<?php

namespace GroupUse1;

function test1() {
}
function test2() {
}
function test3() {
}
function test4() {
}

namespace GroupUse2;

use function GroupUse1\{
    test1,    test2, // comment
    /*FORMAT_START*/test3,    test4/*FORMAT_END*/
};
