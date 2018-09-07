<?php
$globVar = "";
function() use($globVar) {
    echo $globVar;
};

class TestMe {
    public function test() {
        $var = "";
        function() use($var) {
            echo $var;
        };
    }
}

?>