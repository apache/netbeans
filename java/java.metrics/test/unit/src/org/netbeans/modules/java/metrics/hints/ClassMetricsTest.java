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
package org.netbeans.modules.java.metrics.hints;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class ClassMetricsTest extends NbTestCase {

    public ClassMetricsTest(String name) {
        super(name);
    }
    
    private String code(String fileName) throws IOException {
        File f = getDataDir();
        FileObject dd = FileUtil.toFileObject(f);
        
        FileObject file = dd.getFileObject("hints/metrics/" + fileName);
        return file.asText();
    }
    
    public void testMethodCount() throws Exception {
        HintTest.create().input("test/MethodCount.java", code("MethodCount.java")).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings("6:13-6:24:verifier:Class MethodCount has too many methods: 21");
    }
    
    public void testMethodCountLess() throws Exception {
        HintTest.create().input("test/MethodCountLess.java", code("MethodCountLess.java")).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings();
    }
    
    public void testMethodCountLessCustom() throws Exception {
        HintTest.create().input("test/MethodCountLess.java", code("MethodCountLess.java")).
        preference(ClassMetrics.OPTION_CLASS_METHODS_LIMIT, 10).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings("6:13-6:28:verifier:Class MethodCountLess has too many methods: 20");
    }

    public void testMethodCounWithAccessors() throws Exception {
        HintTest.create().input("test/MethodCountLess.java", code("MethodCountLess.java")).
        preference(ClassMetrics.OPTION_CLASS_METHODS_IGNORE_ACCESSORS, false).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings("6:13-6:28:verifier:Class MethodCountLess has too many methods: 24");
    }
    
    public void testMethodCountWithOverrides() throws Exception {
        HintTest.create().input("test/MethodCountAbstract.java", code("MethodCountAbstract.java")).
        preference(ClassMetrics.OPTION_CLASS_METHODS_IGNORE_ABSTRACT, false).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings("12:6-12:25:verifier:Class MethodCountAbstract has too many methods: 21");
    }

    public void testMethodCountWithOverrides2() throws Exception {
        HintTest.create().input("test/MethodCountAbstract.java", code("MethodCountAbstract.java")).
        preference(ClassMetrics.OPTION_CLASS_METHODS_IGNORE_ABSTRACT, true).
        run(ClassMetrics.class, "org.netbeans.modules.java.metrics.hints.ClassMetrics.tooManyMethods").
        assertWarnings();
    }
}
