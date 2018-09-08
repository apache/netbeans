<?php
//START

// OK
$foo->bar(htmlspecialchars($_GET));

// HINT
$foo->bar($_GET);

// HINT
echo $_GET["foo"];

// OK
is_numeric($_GET["foo"]);

// OK
if (is_numeric($_GET["foo"])) {
    // OK
    return $_GET["foo"];
}

do {
    // HINT
    echo $_GET["foo"];
// OK
} while (is_numeric($_GET["foo"]));

// OK
while (is_numeric($_GET["foo"])) {
    // OK
    return $_GET["foo"];
}

// OK
echo is_numeric($_GET["foo"]) ? $_GET[""] : $_GET[""];

echo is_numeric($foo)
    // HINT
    ? $_GET[""]
    // HINT
    : $_GET[""];

// OK
echo is_null($_GET['foo']);

// OK
echo is_nan($_GET['foo']);

// OK
echo is_real($_GET['foo']);

// OK
echo is_scalar($_GET['foo']);

//END
?>