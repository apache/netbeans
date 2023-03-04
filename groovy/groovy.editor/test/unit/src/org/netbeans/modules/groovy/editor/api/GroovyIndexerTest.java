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

package org.netbeans.modules.groovy.editor.api;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 *
 * @author Martin Adamek
 */
public class GroovyIndexerTest extends GroovyTestBase {

    
    public GroovyIndexerTest(String testName) {
        super(testName);
    }

    @Override
    public String prettyPrintValue(String key, String value) {
        if (GroovyIndexer.CLASS_OFFSET.equals(key) && value.length() > 0 && value.charAt(0) == '[') {
            value = value.replace(',', ':');
        }
        return value;
    }

    public void testIsIndexable1() throws Exception {
        checkIsIndexable("testfiles/BookmarkController.groovy", true);
    }
    
    public void testIndex1() throws Exception {
        checkIndexer("testfiles/BookmarkController.groovy");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/Hello.groovy");
    }
}
