<?php

namespace Full\Name\Space {

    class FirstParent {

        public function getSomething() {
            return "baf\n";
        }

    }

}

namespace Test2 {

    use Full\Name\Space\FirstParent as SecondParent;

    class Yours extends SecondParent {

    }

}

namespace Test2 {

    use Full\Name\Space\FirstParent;

    class Yours1 extends FirstParent {

    }

}

?>