<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

namespace tests\unit;

use atoum;

/**
 * Test class for ${name}.
 *
 * @author ${user}
 */
class ${name} extends atoum {

    // put your code here

    public function testSkipped() {
        $this->skip('This test was skipped');
    }

}
