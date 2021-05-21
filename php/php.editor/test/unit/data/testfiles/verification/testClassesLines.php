<?php
//START

namespace TooManyLines;

interface InterfaceName {
    public function functionName1($param);
    public function functionName2($param);
    public function functionName3($param);
    public function functionName4($param);
    public function functionName5($param);
    public function functionName6($param);
    public function functionName7($param);
}

class ClassName {

    function __construct() {
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
    }

}

trait TraitName {

    function functionName() {
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
        echo "line";
    }

}

$fnc = function() {
    echo "line";
    echo "line";
    echo "line";
    echo "line";
    echo "line";
    echo "line";
    echo "line";
};

//END
?>