/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.twig.editor.parsing;

import org.netbeans.modules.php.twig.editor.TwigTestBase;

public class TwigParserErrorTest extends TwigTestBase {

    public TwigParserErrorTest(String testName) {
        super(testName);
    }

    // Unopend
    // #271040
    public void testUnopendSetError_01() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_error_01.twig");
    }

    public void testUnopendSetError_02() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_error_02.twig");
    }

    public void testUnopendSetError_03() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_error_03.twig");
    }

    public void testUnopendSetError_04() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_error_04.twig");
    }

    public void testUnopendSetError_05() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_error_05.twig");
    }

    public void testUnopendSetNoError_01() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_noerror_01.twig");
    }

    public void testUnopendSetNoError_02() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_noerror_02.twig");
    }

    public void testUnopendSetNoError_03() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_noerror_03.twig");
    }

    public void testUnopendSetNoError_04() throws Exception {
        checkErrors("testfiles/parsing/unopend/unopend_set_noerror_04.twig");
    }

    // Unclosed
    public void testUnclosedSetError_01() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_error_01.twig");
    }

    public void testUnclosedSetError_02() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_error_02.twig");
    }

    public void testUnclosedSetError_03() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_error_03.twig");
    }

    public void testUnclosedSetError_04() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_error_04.twig");
    }

    public void testUnclosedSetNoError_01() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_noerror_01.twig");
    }

    public void testUnclosedSetNoError_02() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_noerror_02.twig");
    }

    public void testUnclosedSetNoError_03() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_noerror_03.twig");
    }

    public void testUnclosedSetNoError_04() throws Exception {
        checkErrors("testfiles/parsing/unclosed/unclosed_set_noerror_04.twig");
    }

}
