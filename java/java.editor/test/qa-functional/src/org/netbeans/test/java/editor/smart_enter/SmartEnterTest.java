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

package org.netbeans.test.java.editor.smart_enter;
/*
 * Main.java
 *
 * Created on 23. srpen 2004, 17:25
 */

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.NbModuleSuite;


/**
 *
 * @author  Petr Felenda
 */
public class SmartEnterTest extends JavaEditorTestCase {

    private final int keyCode = KeyEvent.VK_ENTER;

    /** Creates a new instance of Main */
    public SmartEnterTest(String testMethodName) {
        super(testMethodName);
    }
    

    public void testSmartEnter(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // 1. move to adequate place 
            editor.setCaretPosition(5, 28);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SmartEnterTest.class).enableModules(".*").clusters(".*"));
    }
    
}
