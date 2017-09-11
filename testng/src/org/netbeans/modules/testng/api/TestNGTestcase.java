/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2009-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author answer
 */
//suite/test/class/test-method
public final class TestNGTestcase extends Testcase {

    private FileObject classFO = null;
    private boolean confMethod = false;
    private String parameters;
    private List<String> values = new ArrayList<String>();
    private final String testName;
    private String description;

    //TODO: there should be subnode for each value instead
    public TestNGTestcase(String name, String params, String values, TestSession session) {
        super(values != null ? name + "(" + values+ ")" : name, "TestNG Test", session);
        setClassName(name.substring(0, name.lastIndexOf('.')));
//        parameters = params;
        parameters = values;
        this.values.add(values);
        testName = name.substring(name.lastIndexOf(".") + 1);
    }

    public String getParameters() {
        return parameters;
    }

    public void addValues(String values) {
        this.values.add(values);
    }

    public int getInvocationCount() {
        return values.size();
    }

    public FileObject getClassFileObject() {
        FileLocator fileLocator = getSession().getFileLocator();
        if ((classFO == null) && (fileLocator != null) && (getClassName() != null)) {
            classFO = fileLocator.find(getClassName().replace('.', '/') + ".java"); //NOI18N
        }
        return classFO;
    }

    public boolean isConfigMethod() {
        return confMethod;
    }

    public void setConfigMethod(boolean isConfigMethod) {
        confMethod = isConfigMethod;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTestName() {
        return testName;
    }
}
