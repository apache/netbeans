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
package org.netbeans.modules.junit.ui.actions;

import javax.swing.text.Document;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodDebuggerProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=TestMethodDebuggerProvider.class, position=10)
public class JUnitMethodDebuggerProvider extends TestMethodDebuggerProvider {

    @Override
    public boolean isTestClass(Node activatedNode) {
        return TestSingleMethodSupport.isTestClass(activatedNode);
    }
    
    @Override
    public SingleMethod getTestMethod(Document doc, int cursor){
        return TestSingleMethodSupport.getTestMethod(doc, cursor);
    }

    @Override
    public boolean canHandle(Node activatedNode) {
        return TestSingleMethodSupport.canHandle(activatedNode);
    }    
}
