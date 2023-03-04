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

package org.netbeans.modules.java.testrunner.ui.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.openide.nodes.Node;

/**
 *
 * @author Theofanis Oikonomou
 */
public abstract class NodeOpener {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {

        /**
         * @return Project type of the CoreManager, e.g. {@link org.netbeans.modules.gsf.testrunner.api.Utils#ANT_PROJECT_TYPE} or
         * {@link org.netbeans.modules.gsf.testrunner.api.Utils#MAVEN_PROJECT_TYPE}
         */
        String projectType();

        /**
         *
         * @return Testing framework of the CoreManager, e.g. {@link org.netbeans.modules.gsf.testrunner.api.Utils#JUNIT_TF} or
         * {@link org.netbeans.modules.gsf.testrunner.api.Utils#TESTNG_TF}
         */
        String testingFramework();        
    }
    
    public abstract void openTestsuite(TestsuiteNode node);
    public abstract void openTestMethod(TestMethodNode node);
    public abstract void openCallstackFrame(Node node, @NonNull String frameInfo);
    
}
