<?php

/**
 * @param type $okname
 * @param type $wrongName
 */
function functionName0($okname, $param) {
}

/**
 * @param type $wrongName
 * @param type $okname
 */
function functionName1($param, $okname) {
}

/**
 * @param type $okname1
 * @param type $okname2
 */
function functionName2($okname1, $okname2) {
}

function functionName3($okname, $okname2) {
}

/**
 * @param type
 * @param type $okname2
 */
function functionName4($okname, $okname2) {
}

class ClassName {

    /**
     * @param type $okname
     * @param type $wrongName
     */
    function functionName0($okname, $param) {
    }

    /**
     * @param type $wrongName
     * @param type $okname
     */
    function functionName1($param, $okname) {
    }

    /**
     * @param type $okname1
     * @param type $okname2
     */
    function functionName2($okname1, $okname2) {
    }

    function functionName3($okname, $okname2) {
    }

    /**
     * @param type
     * @param type $okname2
     */
    function functionName4($okname, $okname2) {
    }

}

// variadic PHP 5.6
function f(&$req, $opt = null, ...$params) {
    // $params is an array containing the remaining arguments.
} 

?>