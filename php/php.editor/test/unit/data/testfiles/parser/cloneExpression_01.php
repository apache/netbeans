<?php
class CloneExpression
{
    public function create() {
        $test = (clone $this->test1())->test2();
        return (clone $this)->test1();
    }

    public function test1() {
    }

    public function test2() {
    }

}

(clone (new CloneExpression()))->create();
