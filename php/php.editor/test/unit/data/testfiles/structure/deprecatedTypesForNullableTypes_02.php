<?php
/**
 * @method ?\DeprecatedForNullableTypes2 testMethod2(?DeprecatedForNullableTypes2 $tags) Description
 * @property ?DeprecatedForNullableTypes2 $test Description
 * @deprecated
 */
class DeprecatedForNullableTypes2 {

    /**
     * @param ?DeprecatedForNullableTypes2 $tags
     * @return ?DeprecatedForNullableTypes2
     */
    public function testMethod(?DeprecatedForNullableTypes2 $tags, bool $isNull): ?DeprecatedForNullableTypes2 {
        if ($isNull) {
            return null;
        }
        return new DeprecatedForNullableTypes2();
    }

}

