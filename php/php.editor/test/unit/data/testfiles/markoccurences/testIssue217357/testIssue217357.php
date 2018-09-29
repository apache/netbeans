<?php
namespace Abc;
class String {

    function functionName($param) {

    }

}

namespace Ooo;
use Abc\String;
$s = new String();
$s->functionName($param);
?>