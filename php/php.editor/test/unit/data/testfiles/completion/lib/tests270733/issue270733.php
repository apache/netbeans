<?php
class Exception {
}

class SomeException extends \Exception
{
    public static function create($message) {
        return new SomeException($message);
    }
}

try {
    if ($error === 1) {
        throw new SomeException($message);
    } else if ($error === 2) {
        throw SomeException::create($message);
    }
} catch (\Exception $exc) {
    echo $exc->getTraceAsString();
}
