<?php

namespace {
    class Exception {}
}

namespace Nette\Application {

    class Exception {}

    class BadRequestException extends \Exception {}

    class ForbiddenRequestException extends BadRequestException {}

}

?>