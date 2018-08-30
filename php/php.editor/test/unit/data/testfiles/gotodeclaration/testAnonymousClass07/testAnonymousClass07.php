<?php

namespace api;

interface Logger {
    function log($msg);
}

namespace impl;

use api\Logger as MyLogger;

$x = new class implements MyLogger {
    public function log($msg) {
    }
};
$x->log('');
