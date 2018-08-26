<?php

class One
{
    private function getTwo() //One
    {
        return new Two();
    }

    public function doSomething()
    {
        $two = $this->getTwo();
        return $two->getTwo();
    }
}
(new Two)->getTwo();
?>