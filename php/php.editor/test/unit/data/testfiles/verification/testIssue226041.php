<?php
//START
$outsideScope = 'out';

custom_callable_function('test', function() {
    echo $insideScope;
    echo $outsideScope;
});
//END
?>