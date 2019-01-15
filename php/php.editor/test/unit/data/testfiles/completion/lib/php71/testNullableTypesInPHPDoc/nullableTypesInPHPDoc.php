<?php
/**
 * @method PHPDocTags|?
 * @method ?\
 * @property ?PHP
 */
class PHPDocTags {

    /**
     * @param ?PHPDo
     * @return ?
     */
    public function testMethod(?PHPDocTags $tags, bool $isNull) {
        if ($isNull) {
            return null;
        }
        return new PHPDocTags();
    }

}
