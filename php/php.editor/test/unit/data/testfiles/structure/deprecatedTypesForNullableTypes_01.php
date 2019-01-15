<?php
/**
 * @method ?\DeprecatedForNullableTypes1 testMethod2(?DeprecatedForNullableTypes1 $tags) Description
 * @property ?DeprecatedForNullableTypes1 $test Description
 * @deprecated
 */
class DeprecatedForNullableTypes1 {

    /**
     * @param ?DeprecatedForNullableTypes1 $tags
     * @return ?DeprecatedForNullableTypes1
     */
    public function testMethod(?DeprecatedForNullableTypes1 $tags, bool $isNull) {
        if ($isNull) {
            return null;
        }
        return new DeprecatedForNullableTypes1();
    }

}

