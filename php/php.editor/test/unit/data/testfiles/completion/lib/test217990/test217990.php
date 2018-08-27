<?php

namespace foo\lib\url;
class Url {
    static function simpleUrl() {}
    static function simpleUrl2() {}
}

class FooUrl {
    static function fooUrl() {}
    static function fooUrl2() {}
}

namespace God;

use foo\lib\url\Url;
use foo\lib\url\FooUrl;

class UrlClient {

    public function doSomething() {
        Url::
    }

}
?>