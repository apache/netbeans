<?php
class Foo
{
    use A, B, C {
        C::bar insteadof A, B;
    }

}
?>