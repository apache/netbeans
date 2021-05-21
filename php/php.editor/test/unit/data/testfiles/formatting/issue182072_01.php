<?php

class G_Check {

function test($param) {
$this->setValidators(array(
"name" => new sfValidatorString(array('required' => true), array(
"required" => "Tato položka je povinná",
"invalid" => "Tato položka byla špatně vyplněna")),
"text" => new sfValidatorString(array('required' => true), array(
"required" => "Tato položka je povinná",
"invalid" => "Tato položka byla špatně vyplněna")),
));
}

}
?>
