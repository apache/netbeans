<?php
//START

function functionName() {
    try {
        throw new Exception();
        echo ""; //HINT
    } catch (Exception $ex) {
        echo ""; //OK
    }
    echo ""; //OK
    throw new Exception();
    echo ""; //HINT
}

foreach ($array as $value) {
    if (true) {
        break;
        echo ""; //HINT
    } else {
        continue;
        echo ""; //HINT
    }
    echo ""; //OK
}

for ($i = 0; $i < 10; $i++) {
    if (true) {
        break;
        echo ""; //HINT
    } else {
        continue;
        echo ""; //HINT
    }
    echo ""; //OK
}

do {
    if (true) {
        break;
        echo ""; //HINT
    } else {
        continue;
        echo ""; //HINT
    }
    echo ""; //OK
} while (true);

while (true) {
    if (true) {
        break;
        echo ""; //HINT
    } else {
        continue;
        echo ""; //HINT
    }
    echo ""; //OK
}
function fnc() {
    switch ($i) {
        case 1:
            echo ""; //OK
            break;
            echo ""; //HINT
        default:
            echo ""; //OK
            break;
            echo ""; //HINT
    }
}

switch ($i) {
    case 1:
        echo ""; //OK
        break;
        echo ""; //HINT
    default:
        echo ""; //OK
        break;
        echo ""; //HINT
}

switch ($i) {
    case $value:
        break;
    default:
        break;
}

switch ($i) {
    case $value:
        break;
        echo ""; //HINT
    default:
        break;
}

//END
?>