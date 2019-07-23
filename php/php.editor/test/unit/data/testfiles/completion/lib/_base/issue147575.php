<?php
class cls147575 {
    /**
     * @return cls147575
     */
    function factory() { return new cls147575;}
    function dieme() { die (1); }

    public function dieanyway() {
        if (1) $this->factory()->dieanyway();
        else $this->factory()->dieanyway();
        for ($i = 0 ; $i < 10 ; $i++) $this->factory()->dieanyway();
        while (1) $this->factory()->dieanyway();

        if(1) $this->factory()->dieanyway();
        for($i = 0 ; $i < 10 ; $i++) $this->factory()->dieanyway();
        while(1) $this->factory()->dieanyway();

        if(1)$this->factory()->dieanyway();
        for($i = 0 ; $i < 10 ; $i++)$this->factory()->dieanyway();
        while(1)$this->factory()->dieanyway();

    }
}
$v147575 = new cls147575;
$v147575->dieanyway();
?>
