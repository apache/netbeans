<?php

class Test {
    public static $CHECK = "check";
}
echo $test->{Test::$CHECK};
echo Test::$CHECK;

?>