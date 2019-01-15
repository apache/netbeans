<?php
interface Logger {
    public function log(string $msg);
}

$anonCls = new class implements Logger {
    public function log(string $msg1) {
        echo $msg1;
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
    public function log(string $msg2) {
        echo $msg2;
    }
});

(new class implements Logger {
    public function log(string $msg3) {
        echo $msg3 . PHP_EOL;
    }
})->log('hello world');
