<?php
/**
 * @method ?\PHPDocTags testMethod2(?PHPDocTags $tags) Description
 * @property ?PHPDocTags $test Description
 */
class PHPDocTags {

    /**
     * @param ?PHPDocTags $tags
     * @return ?PHPDocTags
     */
    public function testMethod(?PHPDocTags $tags, bool $isNull): ?PHPDocTags {
        if ($isNull) {
            return null;
        }
        return new PHPDocTags();
    }

}
