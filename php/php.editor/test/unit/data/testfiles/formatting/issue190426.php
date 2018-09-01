<?php

// CASE 1 no comment on end of second line, empty line following is OK
if ($a)
    $b = 0;

if ($a)
    $b = 1;


// CASE 2 Comment after second statement -and- empty line following
if ($a)
    $b = 0; // comment

    if ($a) // <<<=== This line indented when it shouldn't be
    $b = 1;


// CASE 3 not having an empty line is OK
if ($a)
    $b = 0; // comment
//

if ($a)
    $b = 1;


// CASE 4 using curly braces all is OK
if ($a) {
    $b = 0;
}

if ($a) {
    $b = 1;
}
?>