<?php

class One
{
    private function getTwo()
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