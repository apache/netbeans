<?php

class Bar { public function __invoke() {echo "class __invoke" . PHP_EOL;} }
(new Bar())();
(new Bar)();
