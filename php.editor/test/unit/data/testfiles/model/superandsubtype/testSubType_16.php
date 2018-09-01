<?php

namespace TestNameSpace;

trait Super {}

trait Mid {
    use Super;
}

class Sub {
    use Mid;
}


?>