<?php
//START

if ($ok === 1 || $ok == 2) {
    $a === "";
    $b == "";
}

while ($ok === 3 || $ok == 4) {
    $c === "";
    $d == "";
}

echo $ok == 5 || $ok === 6 ? true : false;

$e === "";
$f == "";

$ok = $another === true;

/**
 * Test argument against 10.
 *
 * @param int $a
 * @return bool
 */
function is10($a)
{
    return $a == 10;
}

////////////////////////////////////////////////////////////////////////////////

info($error === null);

class Issue223549 {

    function info($param) {
        $this->info($error === null);
    }

}

//END
?>