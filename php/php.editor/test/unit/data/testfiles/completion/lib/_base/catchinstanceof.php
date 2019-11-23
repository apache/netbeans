<?php
class MyTextException {
    public function getMessage() {}
}
try {
    if ($vInstanceof  instanceof MyTextException) {
        $vInstanceof->getMessage();
    }
} catch (MyTextException $vCatch ) {
    $vCatch->getMessage();
}
?>
