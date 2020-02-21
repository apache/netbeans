/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 *
 */
public class CsmHyperlinkTest extends CndBaseTestSuite {
    
    public CsmHyperlinkTest() {
        super("C/C++ Hyperlink part 1");
        
        this.addTestSuite(NamespacesHyperlinkTestCase.class);
        this.addTestSuite(BasicHyperlinkTestCase.class);
        this.addTestSuite(UnnamedEnumTestCase.class);
        this.addTestSuite(LibrariesContentHyperlinkTestCase.class);
    }

    public static Test suite() {
        TestSuite suite = new CsmHyperlinkTest();
        return suite;
    }
}
