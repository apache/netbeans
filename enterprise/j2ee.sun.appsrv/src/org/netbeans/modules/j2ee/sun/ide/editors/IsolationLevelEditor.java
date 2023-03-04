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
package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.ResourceBundle;

public class IsolationLevelEditor extends ChoiceEditor {
    String defaultChoice = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default");     //NOI18N
    private boolean isRuntime = false;
 
    public String[] choices = {
            "read-uncommitted",  //NOI18N
            "read-committed",    //NOI18N
            "repeatable-read",   //NOI18N
            "serializable",       //NOI18N
            defaultChoice,
    }; 
    
    public String[] choicesRuntime = { 
            "read-uncommitted",  //NOI18N
            "read-committed",    //NOI18N
            "repeatable-read",   //NOI18N
            "serializable",       //NOI18N
    };
    
    public IsolationLevelEditor() {
	curr_Sel = null;
    }
     
    public IsolationLevelEditor(boolean isRT) {
	curr_Sel = null;
        this.isRuntime = isRT;
    }
    
    public String[] getTags () {
        if(this.isRuntime)
            return choicesRuntime;
        else
            return choices;
    }
    
    public boolean supportsEditingTaggedValues () {
        return false;
    }    
}


