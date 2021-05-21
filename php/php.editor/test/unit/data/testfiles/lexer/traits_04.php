<?php
trait MyTrait_04 {
    use MyTrait, Mytrait_02 {
        MyTrait::foo insteadof MyTrait_02;
    }
}
?>