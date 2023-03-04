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

package org.netbeans.performance.enterprise;

import junit.framework.Test;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class XMLSchemaComponentOperator extends TopComponentOperator {
    
    /** Creates a new instance of XMLSchemaComponentOperator */
    public XMLSchemaComponentOperator(String topComponentName) {
        this(topComponentName,0);
    }
    
    /** Creates a new instance of XMLSchemaComponentOperator */
    public XMLSchemaComponentOperator(String topComponentName, int Index) {
        super(topComponentName,Index);
    }

    public static XMLSchemaComponentOperator findXMLSchemaComponentOperator(String topComponentName) {
        XMLSchemaComponentOperator schema = null;
        
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",120000);        
            schema = new XMLSchemaComponentOperator(topComponentName);
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",oldTimeout);        
        return schema;
        
    }
    private JToggleButtonOperator getViewButton(String viewName) {
        return new JToggleButtonOperator(this,viewName);
    }
    
    public JToggleButtonOperator getSourceButton() {
        return getViewButton("Source"); // NOI18N
    }
    
    public JToggleButtonOperator getSchemaButton() {
        return getViewButton("Schema"); // NOI18N
    }
    
    public JToggleButtonOperator getDesignButton() {
        return getViewButton("Design"); // NOI18N
    }
    
}
