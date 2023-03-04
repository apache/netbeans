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

package org.netbeans.performance.mobility.window;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MIDletEditorOperator extends TopComponentOperator {

    public MIDletEditorOperator(String midletName) {
        super(midletName);
    }
    /**
     * Find midlet operator located certain top component
     * @param midletName name of the top component
     * @return MIDletEditorOperator
     */
    public static MIDletEditorOperator findMIDletEditorOperator(String midletName) {
        StringComparator oldOperator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new DefaultStringComparator(false, true));
        MIDletEditorOperator midletOperator =  new MIDletEditorOperator(midletName);
        Operator.setDefaultStringComparator(oldOperator);
        return midletOperator;        
    }
    
    public void switchToSource() {
         switchToViewByName("Source");        
    }
    
    public void switchToScreen() {
         switchToViewByName("Screen");          
    }
    
    public void switchToFlow() {
         switchToViewByName("Flow");        
    }
    
    public void switchToAnalyze() {
         switchToViewByName("Analyze");
    }
    
    public void switchToViewByName(String viewName) {
        JToggleButtonOperator viewButton = new JToggleButtonOperator(this,viewName); // NOI18N
        
        if(!viewButton.isSelected())
            viewButton.pushNoBlock();            
    }
}
