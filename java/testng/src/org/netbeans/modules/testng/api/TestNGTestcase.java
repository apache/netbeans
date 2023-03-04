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
