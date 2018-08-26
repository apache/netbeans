<?php

if (true) {
echo "true";
}
// If done with the 'record' parsing, dump it. -> manually added
elseif (false) {
echo "false";
}

do {
echo "bla";
} // a comment
while (true);

try {
bla();
}
// a comment 1
// a comment 2
catch (Exception $e) {
    return false;
}
?>