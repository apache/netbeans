--TEST--
Heredocs can be used as static scalars.
--FILE--
<?php

require_once 'nowdoc.inc';

class e {

    const E = <<<THISMUSTNOTERROR
If you see this, everything is ok.
THISMUSTNOTERROR;

};

print e::E . "\n";

?>
--EXPECT--
If you see this, everything is ok.
