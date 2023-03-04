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
package org.netbeans.modules.web.el.hints;

import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResourceBundleKeysTest extends HintTestBase {

    public ResourceBundleKeysTest(String testName) {
        super(testName);
    }

    @Override
    protected Rule createRule() {
        return new ResourceBundleKeys();
    }

    public void testUnknownResourceBundleKeyInBrackets() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/resourceBundle/resourceBundle01.xhtml", null);
    }

    public void testKnownResourceBundleKeyInBrackets() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/resourceBundle/resourceBundle02.xhtml", null);
    }

    public void testUnknownResourceBundleKeyAsProperty() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/resourceBundle/resourceBundle03.xhtml", null);
    }

    public void testKnownResourceBundleKeyAsProperty() throws Exception {
        checkHints(this, createRule(), "projects/testWebProject/web/hints/resourceBundle/resourceBundle04.xhtml", null);
    }

}
