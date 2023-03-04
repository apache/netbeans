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
package org.netbeans.modules.php.editor.elements;

import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.model.impl.ModelTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpElementImplTest extends ModelTestBase {

    public PhpElementImplTest(String testName) {
        super(testName);
    }

    public void testIssue223449_01() throws Exception {
        PhpElementStub elementWithNullFileUrl = new PhpElementStub("testElement", "", null, 0, null);
        assertNull(elementWithNullFileUrl.getFileObject());
    }

    public void testIssue223449_02() throws Exception {
        PhpElementStub elementWithEmptyFileUrl = new PhpElementStub("testElement", "", "", 0, null);
        assertNull(elementWithEmptyFileUrl.getFileObject());
    }

    private static final class PhpElementStub extends PhpElementImpl {

        public PhpElementStub(String name, String in, String fileUrl, int offset, ElementQuery elementQuery) {
            super(name, in, fileUrl, offset, elementQuery, false);
        }

        @Override
        public String getSignature() {
            return null;
        }

        @Override
        public PhpElementKind getPhpElementKind() {
            return null;
        }
    }

}
