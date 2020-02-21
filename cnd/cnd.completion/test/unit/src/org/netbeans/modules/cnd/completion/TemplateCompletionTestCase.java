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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 */
public class TemplateCompletionTestCase extends CompletionBaseTestCase  {

    /**
     * Creates a new instance of TemplateCompletionTestCase
     */
    public TemplateCompletionTestCase(String testName) {
        super(testName, true);
    }
    
    public void testTemplates1() throws Exception {
        super.performTest("template.cc", 37, 5);
    }
    
    public void testTemplates2() throws Exception {
        super.performTest("template.cc", 37, 5, "t1.");
    }

    public void testTemplates3() throws Exception {
        super.performTest("template.cc", 37, 5, "T1<1>::");
    }

    public void testTemplates4() throws Exception {
        super.performTest("template.cc", 37, 5, "T2<T1<1>>::");
    }

    public void testTemplates5() throws Exception {
        super.performTest("template.cc", 37, 5, "T2< T1<1> >::");
    }

    public void testTemplates6() throws Exception {
        super.performTest("template.cc", 37, 5, "T2<T1<1>::> t2;",-5);
    }

    public void testTemplates7() throws Exception {
        super.performTest("template.cc", 37, 5, "T3<1, T1<1>>::");
    }

    public void testTemplates8() throws Exception {
        super.performTest("template.cc", 37, 5, "T3<1, int>::");
    }

    // IZ 147507 : Code completion issue with templated temporary objects
    public void testTemplates9() throws Exception {
        super.performTest("template.cc", 37, 5, "T4<int>().");
    }

    // IZ 147507 : Code completion issue with templated temporary objects
    public void testTemplates10() throws Exception {
        super.performTest("template.cc", 37, 5, "((T4<int>) 0).");
    }

    public void testIZ150843() throws Exception {
        super.performTest("template.cc", 37, 5, "select<Person>().");
    }

    public void testTemplateFunInstDeref() throws Exception {
        super.performTest("template.cc", 37, 5, "select<Person>().one().");
    }
}

