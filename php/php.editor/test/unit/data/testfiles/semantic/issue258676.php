<?php
class Example
{
    public function doSomething()
    {
        throw new \Exception($this->getMessage());
    }

    private function getMessage()
    {
        return 'Hello World!';
    }
}
