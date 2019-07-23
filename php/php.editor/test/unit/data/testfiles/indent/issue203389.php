<?php

if ($foo == 0) {
    $query = "
        set DATESTYLE to 'Postgres,European';
        insert into mytable (
            foofield1, ^foofield2, foofield3, foofield4
        )
        select
            $foo1, $foo2, $foo3, $foo4
    ";
}

?>