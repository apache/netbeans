<?php

class Base{
function baseRet1(){}
}

class Deriv extends Base{
//var $x;
}

interface Interf{
function interfRet1();
}

interface Inter extends Interf {

}

class Index extends Deriv implements Inter{

function indexRet1(){
  $this->indexRet1();
}
}

?>