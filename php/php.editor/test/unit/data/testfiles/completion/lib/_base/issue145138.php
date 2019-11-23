<?php
function test145138_outer($param1) {
    function test145138_inner($param2) {
        echo $param2;
    }

    print $param1;
}
?>