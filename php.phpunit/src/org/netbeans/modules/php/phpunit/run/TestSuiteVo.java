/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class TestSuiteVo {

    private final List<TestCaseVo> testCases = new ArrayList<>();
    private final String name;
    private final String file;
    private final long time;


    @NbBundle.Messages("TestSuiteVo.name.empty=&lt;no suite name>")
    public TestSuiteVo(String name, String file, long time) {
        assert name != null;
        assert file != null;
        this.name = StringUtils.hasText(name) ? name : Bundle.TestSuiteVo_name_empty();
        this.file = file;
        this.time = time;
    }

    void addTestCase(TestCaseVo testCase) {
        testCases.add(testCase);
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public FileObject getLocation() {
        if (file == null) {
            return null;
        }
        File f = new File(file);
        if (!f.isFile()) {
            return null;
        }
        return FileUtil.toFileObject(f);
    }

    public List<TestCaseVo> getPureTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public List<TestCaseVo> getTestCases() {
        return sanitizedTestCases();
    }

    public long getTime() {
        return time;
    }

    private List<TestCaseVo> sanitizedTestCases() {
        if (!testCases.isEmpty()) {
            return Collections.unmodifiableList(testCases);
        }
        return Collections.singletonList(TestCaseVo.skippedTestCase());
    }

    @Override
    public String toString() {
        return String.format("TestSuiteVo{name: %s, file: %s, time: %d, cases: %d}", name, file, time, testCases.size()); // NOI18N
    }

}
