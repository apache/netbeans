<?php
class TestIssue145206 {

    /**
     * @return TestIssue145206
     */
    static function  createStatic() {}

    /**
     * @return TestIssue145206
     */
     function  create() {}
}

 echo TestIssue145206 :: createStatic()->create() ->create() -> create();

?>