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
package org.netbeans.modules.gsf.testrunner.api;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class CommonUtils {

    private static Reference<CommonUtils> instanceRef;
    public static final String JUNIT_TF = "junit"; // NOI18N
    public static final String TESTNG_TF = "testng"; // NOI18N
    public static final String ANT_PROJECT_TYPE = "ant"; // NOI18N
    public static final String MAVEN_PROJECT_TYPE = "maven"; // NOI18N
    private String testingFramework = ""; // NOI18N

    /**
     * Returns a singleton instance of this class. If no instance exists at the
     * moment, a new instance is created.
     *
     * @return singleton of this class
     */
    public static CommonUtils getInstance() {
        if (instanceRef != null) {
            CommonUtils inst = instanceRef.get();
            if (inst != null) {
                return inst;
            }
        }
        final CommonUtils instance = new CommonUtils();
        instanceRef = new WeakReference<CommonUtils>(instance);
        return instance;
    }

    /**
     *
     * @return the testing framework, e.g. {@link #JUNIT_TF} or {@link #TESTNG_TF}
     */
    public String getTestingFramework() {
        return testingFramework;
    }

    /**
     *
     * @param testingFramework the selected testing framework, e.g. {@link #JUNIT_TF} or {@link #TESTNG_TF}
     */
    public void setTestingFramework(String testingFramework) {
        this.testingFramework = testingFramework;
    }

    /**Get the ActionProvider associated with the project, if any, which "owns" the given file.
     *
     * @param fileObject the selected file
     * @return the ActionProvider associated with the given file, or {@code null}
     */
    public ActionProvider getActionProvider(FileObject fileObject) {
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) { // #183586
            return null;
        }
        return owner.getLookup().lookup(ActionProvider.class);
    }

}
