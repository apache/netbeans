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

package org.netbeans.modules.junit.api;

import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author answer
 */
public class JUnitTestcase extends Testcase{
    private FileObject classFO = null;

    /**
     * @param name The method name of this test method
     * @param type The type of test case
     * @param session  The session where this test is executed
     */
    public JUnitTestcase(String name, String type, TestSession session) {
        this(name, name, type, session);
    }
    
    /**
     * @param name The method name of this test method
     * @param displayName The display name of this test method
     * @param type The type of test case
     * @param session the session where this test case is executed.
     */
    public JUnitTestcase(String name, String displayName, String type, TestSession session) {
        super(name, displayName, type, session);
    }

    @Override
    public String getName() {
        TestSuite currentSuite = getSession().getCurrentSuite();
        String className = getClassName();
        if (className == null || currentSuite == null) {
            return super.getName();
        }
        String suiteName = currentSuite.getName();
        // if the running suite is actually a test file return just the method name
        if(suiteName == null || suiteName.equals(className)) {
            return super.getName();
        }
        // the running suite is actually a suite, so return method's full path
        return className + "." + super.getName();
    }
    
    public FileObject getClassFileObject(){
        return getClassFileObject(false);
    }
    
    public FileObject getClassFileObject(boolean searchForInnerClass){
        FileLocator fileLocator = getSession().getFileLocator();
        if ((classFO == null) && (fileLocator != null) && (getClassName() != null)){
            String className = getClassName();
            classFO = fileLocator.find(className.replace('.', '/') + ".java"); //NOI18N
            if (classFO == null && searchForInnerClass) {
                int indexOf = className.indexOf('$');
                if (indexOf != -1) { // innerclass
                    className = className.substring(0, indexOf);
                    return fileLocator.find(className.replace('.', '/') + ".java"); //NOI18N
                }
            }
        }
        return classFO;
    }
}
