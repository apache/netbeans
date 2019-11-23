<?php
/**
 * @return DateTime
 */
function(): Iterator {
}

/**
 * @return DateTime
 */
$a = function() {
};

$b = function(): int use($a) {
};
