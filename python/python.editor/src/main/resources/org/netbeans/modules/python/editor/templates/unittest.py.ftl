<#-- This is a FreeMarker template -->
<#if encoding??>
# -*- coding: utf-8 -*-

</#if>
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

import unittest

<#assign clsname = name?cap_first?replace('TestCase$', '', 'ri')?replace('Test$', '', 'ri')>

class  ${clsname}TestCase(unittest.TestCase):
    #def setUp(self):
    #    self.foo = ${clsname?cap_first}()
    #

    #def tearDown(self):
    #    self.foo.dispose()
    #    self.foo = None

    def test_${clsname?uncap_first}(self):
        #assert x != y;
        #self.assertEqual(x, y, "Msg");
        self.fail("TODO: Write test")

if __name__ == '__main__':
    unittest.main()

