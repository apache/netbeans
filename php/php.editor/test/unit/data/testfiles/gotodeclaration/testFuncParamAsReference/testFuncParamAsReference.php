<?php
function funcWithRefParam(&$param) {
    $param++;
}
$var = 1;
echo "Before value: {$var}\n";
funcWithRefParam($var);
echo "After value: {$var}\n";
?>
