<?php
//START

class MethodRedeclarationClassName {

    function functionNameSame($param) {

    }

    function functionNameSame($param) {

    }

    function functionName($param) {

    }

}

interface MethodRedeclarationInterfaceName {
    function ifaceFunctionNameSame($param);

    function ifaceFunctionNameSame($param);

    function ifaceFunctionName($param);
}

trait MethodRedeclarationTraitName {

    function traitFunctionNameSame($param) {

    }

    function traitFunctionNameSame($param) {

    }

    function traitFunctionName($param) {

    }
}

function globalFunctionNameSame($param) {
}

function globalFunctionNameSame($param) {
}

//END
?>