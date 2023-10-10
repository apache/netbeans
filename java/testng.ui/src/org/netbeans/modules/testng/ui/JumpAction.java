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

package org.netbeans.modules.testng.ui;

import org.netbeans.modules.testng.api.TestNGTestSuite;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class JumpAction extends org.netbeans.modules.java.testrunner.ui.api.JumpAction {

    public JumpAction(Node node, String callstackFrameInfo) {
        super(node, callstackFrameInfo, "", CommonUtils.TESTNG_TF);
    }

    @Override
    public boolean isEnabled() {
        Node node = getNode();
        if (node instanceof TestNGSuiteNode) {
            TestSuite suite = ((TestNGSuiteNode) node).getSuite();
            if (suite instanceof TestNGTestSuite) {
                return ((TestNGTestSuite) suite).getSuiteFO() != null;
            }
        }
        return super.isEnabled();
    }

}
