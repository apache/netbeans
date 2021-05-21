<?php

interface Logger {
    public function log(string $msg);
}

$anonCls = new class implements Logger {
    public function log(string $msg) { // 1
        echo $msg; // 1
    }
};
var_dump($anonCls);

class Application {
    private $logger;

    public function getLogger(): Logger {
         return $this->logger;
    }

    public function setLogger(Logger $logger) {
         $this->logger = $logger;
    }
}

$app = new Application();
$app->setLogger(new class implements Logger {
    public function log(string $msg) { // 2
        echo $msg; // 2
    }
});

(new class implements Logger {
    public function log(string $msg) { // 3
        echo $msg; // 3
    }
})->log('hello world');
