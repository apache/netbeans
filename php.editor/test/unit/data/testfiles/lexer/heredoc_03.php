<?php

echo <<<HERE
select * from $foo
HERE
 . " where foo = bar";
?>