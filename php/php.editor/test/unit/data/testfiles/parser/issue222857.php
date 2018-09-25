<?php

$staticLambda = static function() {
    print 'abc';
};

call_user_func(static function() {
    print 'abc';
});

?>