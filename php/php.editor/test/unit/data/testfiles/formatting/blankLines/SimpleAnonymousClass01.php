<?php
var_dump(new class {
// property declaration
public $var = 'a default value';
// method declaration
public function displayVar() {
echo $this->var;
}
});
