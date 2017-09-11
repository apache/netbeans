/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
