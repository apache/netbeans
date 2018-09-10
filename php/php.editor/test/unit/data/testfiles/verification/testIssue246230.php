<?php

function foo() {
try {
    throw new Exception('Something');
} catch ( Exception $ex ) {
    // ignore hint
}

try {
    throw new Exception('Something');
} catch ( Exception $ex ) {
    echo ""; // do not ignore...
}
}


?>