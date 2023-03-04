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

package org.netbeans.test.editor.popup;

import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.DocumentUtilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;

/**
 * Test behavior of main menus - Edit, View
 * @author Martin Roskanin
 */
public class MainMenuTest extends MenuTestCase {
    
    String xmlFile =  "testMainMenu.xml";
    
    /** Creates a new instance of Main */
    public MainMenuTest(String testMethodName) {
        super(testMethodName);
    }
    
    
    public void testMainMenu(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            
            EditorOperator editor = getDefaultSampleEditorOperator();
            JTextComponentOperator text = new JTextComponentOperator(editor);
            final JTextComponent target = (JTextComponent)text.getSource();
            final Preferences prefs = MimeLookup.getLookup("text/xml").lookup(Preferences.class);
            
            boolean lineNumberVisibleSetting = prefs.getBoolean(
                    SimpleValueNames.LINE_NUMBER_VISIBLE,
                    EditorPreferencesDefaults.defaultLineNumberVisible);
            
            //enable line number
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_S);
            
            ValueResolver resolver = new ValueResolver(){
                public Object getValue(){
                    return prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, Boolean.FALSE);
                }
            };
            
            waitMaxMilisForValue(2000, resolver, Boolean.TRUE);
            
            lineNumberVisibleSetting = prefs.getBoolean(
                    SimpleValueNames.LINE_NUMBER_VISIBLE,
                    EditorPreferencesDefaults.defaultLineNumberVisible);
            
            if (lineNumberVisibleSetting == false){
                log("Java editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(target));
            }
            
            //assert lineNumberVisibleSetting == true;
            assertTrue("Java editor - line numbers not visible", lineNumberVisibleSetting);
            
            openSourceFile(getDefaultSamplePackage(), xmlFile);
            
            EditorOperator editorXML = new EditorOperator(xmlFile);
            JTextComponentOperator textXML = new JTextComponentOperator(editorXML);
            final JTextComponent targetXML = (JTextComponent)textXML.getSource();
            
            //enable line number
            JEditorPaneOperator txtOperXML = editorXML.txtEditorPane();
            txtOperXML.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOperXML.pushKey(KeyEvent.VK_S);
            
            ValueResolver resolverXML = new ValueResolver(){
                public Object getValue(){
                    return prefs.getBoolean(
                            SimpleValueNames.LINE_NUMBER_VISIBLE,
                            Boolean.FALSE);
                }
            };
            
            
            waitMaxMilisForValue(2000, resolverXML, Boolean.TRUE);
            
            lineNumberVisibleSetting = prefs.getBoolean(
                    SimpleValueNames.LINE_NUMBER_VISIBLE,
                    EditorPreferencesDefaults.defaultLineNumberVisible);
            
            if (lineNumberVisibleSetting == false){
                log("XML editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(targetXML));
            }
            
            // assert lineNumberVisibleSetting == true;
            assertTrue("XML editor - line numbers not visible", lineNumberVisibleSetting);
        } finally {
            // now close XML file
            try {
                //find editor
                EditorOperator editor = new EditorOperator(xmlFile);
                editor.closeDiscard();
            } catch ( TimeoutExpiredException ex) {
                log(ex.getMessage());
                log("Can't close the file:"+xmlFile);
            }
            
            //and java file
            closeFileWithDiscard();
            
            
        }
    }

    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(MainMenuTest.class).enableModules(".*").clusters(".*"));
   }
    
}
