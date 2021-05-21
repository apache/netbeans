<?php
//START

// OK
$foo->bar(htmlspecialchars($_REQUEST));

// HINT
$foo->bar($_REQUEST);

// HINT
echo $_REQUEST["foo"];

// OK
is_numeric($_REQUEST["foo"]);

// OK
if (is_numeric($_REQUEST["foo"])) {
    // OK
    return $_REQUEST["foo"];
}

do {
    // HINT
    echo $_REQUEST["foo"];
// OK
} while (is_numeric($_REQUEST["foo"]));

// OK
while (is_numeric($_REQUEST["foo"])) {
    // OK
    return $_REQUEST["foo"];
}

// OK
echo is_numeric($_REQUEST["foo"]) ? $_REQUEST[""] : $_REQUEST[""];

echo is_numeric($foo)
    // HINT
    ? $_REQUEST[""]
    // HINT
    : $_REQUEST[""];

// OK
echo is_null($_REQUEST['foo']);

// OK
echo is_nan($_REQUEST['foo']);

// OK
echo is_real($_REQUEST['foo']);

// OK
echo is_scalar($_REQUEST['foo']);

//END
?>