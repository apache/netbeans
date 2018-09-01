<?php

function testException($param, $mExpectedException = null) {
    try {
        //some code
    } catch (Exception $e) {
        
        $bEmptyOrBadExceptedException
                = empty($mExpectedException)
                || !($e instanceof $mExpectedException)
        || !($e instanceof Exception);
    }
}

?>