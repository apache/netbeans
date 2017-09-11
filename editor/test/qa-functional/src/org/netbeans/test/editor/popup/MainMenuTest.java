/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
