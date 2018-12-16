<?php
//START

// OK
$foo->bar(htmlspecialchars($_POST));

// HINT
$foo->bar($_POST);

// HINT
echo $_POST["foo"];

// OK
is_numeric($_POST["foo"]);

// OK
if (is_numeric($_POST["foo"])) {
    // OK
    return $_POST["foo"];
}

do {
    // HINT
    echo $_POST["foo"];
// OK
} while (is_numeric($_POST["foo"]));

// OK
while (is_numeric($_POST["foo"])) {
    // OK
    return $_POST["foo"];
}

// OK
echo is_numeric($_POST["foo"]) ? $_POST[""] : $_POST[""];

echo is_numeric($foo)
    // HINT
    ? $_POST[""]
    // HINT
    : $_POST[""];

// OK
echo is_null($_POST['foo']);

// OK
echo is_nan($_POST['foo']);

// OK
echo is_real($_POST['foo']);

// OK
echo is_scalar($_POST['foo']);

//END
?>