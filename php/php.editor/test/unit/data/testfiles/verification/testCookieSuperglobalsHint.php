<?php
//START

// OK
$foo->bar(htmlspecialchars($_COOKIE));

// HINT
$foo->bar($_COOKIE);

// HINT
echo $_COOKIE["foo"];

// OK
is_numeric($_COOKIE["foo"]);

// OK
if (is_numeric($_COOKIE["foo"])) {
    // OK
    return $_COOKIE["foo"];
}

do {
    // HINT
    echo $_COOKIE["foo"];
// OK
} while (is_numeric($_COOKIE["foo"]));

// OK
while (is_numeric($_COOKIE["foo"])) {
    // OK
    return $_COOKIE["foo"];
}

// OK
echo is_numeric($_COOKIE["foo"]) ? $_COOKIE[""] : $_COOKIE[""];

echo is_numeric($foo)
    // HINT
    ? $_COOKIE[""]
    // HINT
    : $_COOKIE[""];

// OK
echo is_null($_COOKIE['foo']);

// OK
echo is_nan($_COOKIE['foo']);

// OK
echo is_real($_COOKIE['foo']);

// OK
echo is_scalar($_COOKIE['foo']);

//END
?>