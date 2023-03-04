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

/*
 * KeyBindings.java
 *
 * Created on 10/17/02 12:54 PM
 */
package org.netbeans.jellytools.modules.editor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.operators.*;
import java.util.*;

import javax.swing.ListModel;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;

/** Class implementing all necessary methods for handling "Key Bindings" NbDialog.
 *
 * @author eh103527
 * @version 1.0
 */
public class KeyBindings extends JDialogOperator {
    
    /** Creates new KeyBindings that can handle it.
     */
    public KeyBindings() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("PROP_KeyBindings"));
    }
    
    private JRadioButtonOperator _rbSortByName;
    private JRadioButtonOperator _rbSortByAction;
    private JListOperator _lstActions;
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JLabelOperator _lblKeybindings;
    private JListOperator _lstKeybindings;
    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find " Sort by Name" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSortByName() {
        if (_rbSortByName==null) {
            _rbSortByName = new JRadioButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_name_sort_button"));
        }
        return _rbSortByName;
    }
    
    /** Tries to find " Sort by Action" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSortByAction() {
        if (_rbSortByAction==null) {
            _rbSortByAction = new JRadioButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_action_sort_button"));
        }
        return _rbSortByAction;
    }
    
    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstActions() {
        if (_lstActions==null) {
            _lstActions = new JListOperator(this);
        }
        return _lstActions;
    }
    
    /** Tries to find "" MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton() {
        if (_btMetalScrollButton==null) {
            _btMetalScrollButton = new JButtonOperator(this, "");
        }
        return _btMetalScrollButton;
    }
    
    /** Tries to find "" MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton2() {
        if (_btMetalScrollButton2==null) {
            _btMetalScrollButton2 = new JButtonOperator(this, "", 1);
        }
        return _btMetalScrollButton2;
    }
    
    /** Tries to find "Keybindings:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblKeybindings() {
        if (_lblKeybindings==null) {
            _lblKeybindings = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_Sequences"));
        }
        return _lblKeybindings;
    }
    
    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstKeybindings() {
        if (_lstKeybindings==null) {
            _lstKeybindings = new JListOperator(this, 1);
        }
        return _lstKeybindings;
    }
    
    /** Tries to find "Add..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_Add"));
        }
        return _btAdd;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_Remove"));
        }
        return _btRemove;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_OK_LABEL"));
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.core.awt.Bundle").getString("CancelButton"));
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on " Sort by Name" JRadioButton
     */
    public void sortByName() {
        rbSortByName().push();
    }
    
    /** clicks on " Sort by Action" JRadioButton
     */
    public void sortByAction() {
        rbSortByAction().push();
    }
    
    /** clicks on "" MetalScrollButton
     */
    public void metalScrollButton() {
        btMetalScrollButton().push();
    }
    
    /** clicks on "" MetalScrollButton
     */
    public void metalScrollButton2() {
        btMetalScrollButton2().push();
    }
    
    /** clicks on "Add..." JButton
     */
    public void add() {
        btAdd().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }
    
    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
    }
    
    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of KeyBindings by accessing all its components.
     */
    public void verify() {
        rbSortByName();
        rbSortByAction();
        lstActions();
        btMetalScrollButton();
        btMetalScrollButton2();
        lblKeybindings();
        lstKeybindings();
        btAdd();
        btRemove();
        btOK();
        btCancel();
        btHelp();
    }
    
    public List listActions() {
        ListModel model = lstActions().getModel();
        List ret=new Vector();
        for (int i=0;i < model.getSize();i++) {
            ret.add(model.getElementAt(i));
        }
        return ret;
    }
    
    public List listKeyBindings(String actionName) {
        JListOperator jlist=lstActions();
        jlist.selectItem(actionName);
        ListModel model = lstKeybindings().getModel();
        List ret=new Vector();
        for (int i=0;i < model.getSize();i++) {
            ret.add(model.getElementAt(i));
        }
        return ret;
    }
    
    /**
     * @param editorName
     * @return
     */
    public static KeyBindings invoke(String editorName) {
        OptionsOperator options = OptionsOperator.invoke();

        //No classic view anymore
        //options.switchToClassicView();

        options.selectOption(ResourceBundle.getBundle("org/netbeans/core/Bundle").getString("UI/Services/Editing")+"|"+ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_all")+"|" + editorName);
        //new EventTool().waitNoEvent(500);
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        PropertySheetOperator pso = new PropertySheetOperator(options);
        Property p=new Property(pso, ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_KeyBindings"));
        p.openEditor();
        KeyBindings ret=new KeyBindings();
        options.close();
        return ret;
    }
    
    public static List listActions(String editorName) {
        KeyBindings instance = invoke(editorName);
        List          result = instance.listActions();
        instance.oK();
        return result;
    }
    
    public static Hashtable listAllKeyBindings(String editorName) {
        KeyBindings instance = invoke(editorName);
        List          result = instance.listActions();
        Hashtable ret=new Hashtable();
        for (int i=0;i < result.size();i++) {
            ret.put(result.get(i).toString(),instance.listKeyBindings(result.get(i).toString()));
        }
        instance.oK();
        return ret;
    }
    
    /** Performs simple test of KeyBindings
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //repeating test
        String[] names=new String[] {"Plain Editor","HTML Editor","Java Editor"};
        List list;
        for (int j=0;j < 20;j++) {
            
            for (int i=0;i < names.length;i++) {
                list = KeyBindings.listActions(names[i]);
                System.out.println("step "+j+" output size="+list.size());
            }
        }
        /*
        String name="Java Editor";
        Hashtable table;
        table = KeyBindings.listAllKeyBindings(name);
        Object[] keys=table.keySet().toArray();
        Arrays.sort(keys);
        List list;
        try {
            File out=new File("/tmp/"+name+" actions.lst");
            PrintWriter pw=new PrintWriter(new FileWriter(out));
            for (int i=0;i < keys.length;i++) {
                pw.print(keys[i]+": ");
                list=(List)table.get(keys[i]);
                for (int j=0;j < list.size();j++) {
                    pw.print(list.get(j)+" ");
                }
                pw.println();
            }
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }
}

