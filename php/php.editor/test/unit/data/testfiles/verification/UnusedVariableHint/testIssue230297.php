<?php
function fnc() {
    $foo = "";
    echo <<<HER
    a${foo}b
HER;
}

?> 