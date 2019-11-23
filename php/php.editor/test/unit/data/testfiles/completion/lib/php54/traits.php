<?php
trait BaseTrait {
    public $baseTraitField = 10;
    protected $protectedBaseField = 10;
    private $privateBaseField = 10;

    public function baseTraitMethod() {}
    protected function baseProtected() {}
    private function basePrivate() {}
}

class BaseClass {
    use BaseTrait;
    
    function functionName() {
        $this->baseTraitField;
    }
}

trait TraitedTrait {
    use BaseTrait;
    public $traitedTraitField = 20;
    protected $protectedTraitedField = 20;
    private $privateTraitedField = 20;

    public function traitedTraitMethod() {}
    protected function traitedProtected() {}
    private function traitedPrivate() {}
}

class TraitedClass {
    use TraitedTrait;

    function functionName() {
        $this->traitedTraitField;
    }
}

$bc = new BaseClass();
$bc->baseTraitField;

$tc = new TraitedClass();
$tc->traitedTraitField;

?>