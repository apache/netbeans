<?php
trait MyTrait_05 {
    use MyTrait, Mytrait_02 {
        MyTrait::foo as bar;
        MyTrait::foo insteadof MyTrait_02;
    }
}
?>