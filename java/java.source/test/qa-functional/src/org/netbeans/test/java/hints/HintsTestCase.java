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
package org.netbeans.test.java.hints;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.swing.ListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.test.java.JavaTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Prox
 */
public class HintsTestCase extends JavaTestCase {
    
    private static final int MAX_CYCLES = 20;

    public HintsTestCase(String name) {
        super(name);
    }

    private void compareArrays(String[] ethalon, String[] current) {
        for (int i = 0; i < current.length; i++) {
            String curItem = current[i].toUpperCase();
            int j = 0;
            for (j = 0; j < ethalon.length; j++) {
                String ethalItem = ethalon[j].toUpperCase();
                if (curItem.startsWith(ethalItem)) {
                    break;
                }
            }
            assertFalse("Item " + curItem + " is missing in ethalon list", j == ethalon.length);
        }

        for (int i = 0; i < ethalon.length; i++) {
            String ethalItem = ethalon[i].toUpperCase();
            int j = 0;
            for (j = 0; j < current.length; j++) {
                String curItem = current[j].toUpperCase();
                if (curItem.startsWith(ethalItem)) {
                    break;
                }
            }
            assertFalse("Item " + ethalItem + " is missing in current list", j == current.length);
        }
    }
    
    protected EditorOperator editor;
    protected EditorOperator target;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openDefaultProject();
        target = null;
    }

    @Override
    protected void tearDown() throws Exception {
        if (editor != null) {
            editor.close(false);
        }
        super.tearDown();
    }

    public void useHint(String hint, String[] hints, String pattern) {
        new EventTool().waitNoEvent(750);        
        JListOperator jlo = null;
        int cycles = 0;
        while (cycles < MAX_CYCLES) {
            cycles++;
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",500);
            editor.pushKey(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK);            
            try {
                jlo = new JListOperator(MainWindowOperator.getDefault());
                break;
            } catch (TimeoutExpiredException tee) {
                log("Try "+cycles);
                editor.requestFocus();                
                if(cycles==MAX_CYCLES) throw tee;
            }
        }      
        ListModel model = jlo.getModel();
        int index = -1;
        String[] list = new String[model.getSize()];
        for (int i = 0; i < model.getSize(); i++) {
            String desc = ((Fix) model.getElementAt(i)).getText().toUpperCase();
            list[i] = desc;
            if (desc.startsWith(hint.toUpperCase())) {
                index = i;
            }
        }
        if (hints != null) {
            System.out.println("Found: "+Arrays.toString(list));
            compareArrays(hints, list);
        }
        assertFalse("Required hint " + hint + " not found", index == -1);
        jlo.setSelectedIndex(index);
        jlo.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(750);
        String result;
        if (target == null) {
            result = editor.getText();
        } else {
            target.setVisible(true);
            target.save();
            result = target.getText();
        }
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        if (!p.matcher(result).matches()) {
            log("Pattern: " + pattern);
            System.out.println(pattern);
            log("-------------------");
            log(result);
            System.out.println(result);
            fail("Expected pattern not found");
        }
    }

    protected void selectHintNode(JTreeOperator jto, String category, String hintName) {
        int i = 0;
        for (i = 0; i < jto.getRowCount(); i++) {
            jto.selectRow(i);
            jto.collapseRow(i);            
            Object lastSelectedPathComponent = jto.getLastSelectedPathComponent();
            Object userObject = ((DefaultMutableTreeNode) lastSelectedPathComponent).getUserObject();
            String fileName = ((FileObject) userObject).getName();            
            if (fileName.equals(category)) {
                break;
            }
        }
        assertTrue("Category "+category+" not found", i < jto.getRowCount());
        jto.expandRow(i);
        Object root = jto.getLastSelectedPathComponent();
        for(int j = 0;j<jto.getChildCount(root);j++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)jto.getChild(root, j);
            String displayName = ((AbstractHint) child.getUserObject()).getDisplayName();
            if(displayName.equals(hintName)) {
                jto.selectRow(i + j + 1);
                return;
            }
        }
        assertTrue("Hint "+hintName+" not found", false);     
    }

    protected void chBoxSetSelected(final JCheckBoxOperator chbox1, boolean state) {
        if (chbox1.isSelected() != state) {
            chbox1.doClick();
            new EventTool().waitNoEvent(500);
        }
    }

    public void setInPlaceCreation(boolean inPlace) {
        OptionsOperator oo = OptionsOperator.invoke();
        oo.selectEditor();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(oo);
        jtpo.selectPage("Hints");
        JSplitPaneOperator jspo = new JSplitPaneOperator(oo);
        JTreeOperator jto = new JTreeOperator(jtpo);
        selectHintNode(jto, "errors", "Create Local Variable");
        JCheckBoxOperator chbox1 = new JCheckBoxOperator(new ContainerOperator((Container) jspo.getRightComponent()), "Create Local Variable In Place");
        chBoxSetSelected(chbox1, inPlace);
        oo.ok();
    }
}
