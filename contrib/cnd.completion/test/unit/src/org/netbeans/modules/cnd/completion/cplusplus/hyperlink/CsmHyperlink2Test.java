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
public class CsmHyperlink2Test extends CndBaseTestSuite {
    
    public CsmHyperlink2Test() {
        super("C/C++ Hyperlink part 2");
        
        this.addTestSuite(ClassMembersHyperlinkTestCase.class);
        this.addTestSuite(TemplateSpecializationsTestCase.class);
        this.addTestSuite(InstantiationHyperlinkTestCase.class);
        this.addTestSuite(Cpp11TestCase.class);
        this.addTestSuite(Cpp11TemplatesTestCase.class);
        this.addTestSuite(Cpp11TooltipsTestCase.class);
        this.addTestSuite(Cpp14TestCase.class);
    }

    public static Test suite() {
        TestSuite suite = new CsmHyperlink2Test();
        return suite;
    }
}
