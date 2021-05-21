<?php
//START

$globVar = 5;

class ClassName {

    function functionName($param) {
        $assignment = "foo";

        try {

        } catch (Exception $ex) {
            echo $ex->getTraceAsString();
        }

        do {
            $doCond = false;
        } while ($doCond);

        foreach ($array as $key => $value) {

        }

        global $globVar;

        $this->foo();

        $GLOBALS["a"];
        $_SERVER["a"];
        $_GET["a"];
        $_POST["a"];
        $_FILES["a"];
        $_COOKIE["a"];
        $_SESSION["a"];
        $_REQUEST["a"];
        $_ENV["a"];
    }

    function foo(&$referenceParam = null) {
        $this->foo($uninit);

        if ($uninitIf) {

        }

        do {

        } while ($uninitDo);

        while ($uninitWhile) {

        }

        for ($index = 0; $index < $uninitFor; $index++) {

        }

        if (true) {

        } elseif ($uninitElseif) {

        }
    }

}

$a = $b = 5;
foobar($b);

function functionNameArrayAccess() {
    $subnodes[] = 1;
    return $subnodes;
}

function foo() {
  $myArray = array();

  foreach ($myArray as $value) {
     echo $value;
     bar($myVar);
  }
}

echo $argc;
echo $argv;
echo $php_errormsg;
echo $HTTP_RAW_POST_DATA;
echo $http_response_header;

$refArr = array();
foreach ($refArr as &$refVar) {

}
unset($refVar);

function fnc239089($types, &$var1, &$problem = null) {}

function functionName239089() {
    fnc239089("ok", $shouldBeOkInRefs);
}

//END
?>