<?php

trait X {

    /**
     * @return self
     */
    function example() {
        return $this;
    }

    /**
     * @return X
     */
    function more_example() {
        return $this;
    }

}

class Test {

    use X;

    public function abc() {
        $this->more_example()->
    }

}

?>