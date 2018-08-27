<?php
trait FirstTrait {
    public $publicFirstField = 10;
    protected $protectedFirstField = 10;
    private $privateFirstField = 10;

    public function publicFirstMethod() {}
    protected function protecedFirstMethod() {}
    private function privateFirstMethod() {}
}

trait SecondTrait {
    public $publicSecondField = 10;
    protected $protectedSecondField = 10;
    private $privateSecondField = 10;

    public function publicSecondMethod() {}
    protected function protecedSecondMethod() {}
    private function privateSecondMethod() {}
}

class WithMultiUses {
    use FirstTrait, SecondTrait;

    function functionName() {
        $this->publicFirstField;
    }
}

$wmu = new WithMultiUses();
$wmu->publicFirstField;

?>