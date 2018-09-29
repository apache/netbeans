<?php
//START

if (true) {
    return;
    echo ""; //HINT
} elseif (true) {
    return;
    echo ""; //HINT
} else {
    return;
    echo ""; //HINT
}
return;
echo "foo"; //HINT

function functionName() {
    if (true)
        return; //OK
    else
        echo ""; //OK
    return; //OK
}

function fnc() {
    switch ($foo) {
        case 1:
        case 2:
            return;
            echo ""; //HINT
            break;
    }

    do
        return; //OK
    while (true);

    do {
        return;
        echo ""; //HINT
    } while (true);

    while (true)
        return; //OK

    while (true) {
        return;
        echo ""; //HINT
    }

    foreach ($array as $key)
        return; //OK

    foreach ($array as $key) {
        return;
        echo ""; //HINT
    }

    for ($i = 0; $i < 10; $i++)
        return; //OK

    for ($i = 0; $i < 10; $i++) {
        return;
        echo ""; //HINT
        echo ""; //OK
    }
}

//END
?>