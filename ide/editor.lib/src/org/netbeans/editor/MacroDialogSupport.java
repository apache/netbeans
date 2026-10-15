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

package org.netbeans.editor;

import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.*;
import java.awt.event.*;
import java.text.MessageFormat;
import org.openide.util.NbBundle;


/** The support for creating macros.
 *
 * @author  Petr Nejedly
 * @version 1.0
 * @deprecated Without any replacement. This class is no longer functional.
 */
@Deprecated
public class MacroDialogSupport implements ActionListener {

    JButton okButton;
    JButton cancelButton;

    MacroSavePanel panel;
    Dialog macroDialog;
    Class kitClass;
    
    /** Creates new MacroDialogSupport */
    public MacroDialogSupport( Class kitClass ) {
        this.kitClass = kitClass;
        panel = new MacroSavePanel(kitClass);
        ResourceBundle bundle = NbBundle.getBundle(MacroDialogSupport.class);
        okButton = new JButton(bundle.getString("MDS_ok")); // NOI18N
        cancelButton = new JButton(bundle.getString("MDS_cancel")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MDS_ok")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MDS_cancel")); // NOI18N
    }

    public void setBody( String body ) {
        panel.setMacroBody( body );
    }
    
    public void showMacroDialog() {
        macroDialog = DialogSupport.createDialog(
                NbBundle.getBundle(MacroDialogSupport.class).getString("MDS_title"), // NOI18N
                panel, true, new JButton[] { okButton, cancelButton }, false, 0, 1, this );

        macroDialog.pack();
        panel.popupNotify();
        macroDialog.requestFocus();
        macroDialog.show();
    }
    
//    private List getKBList(){
//        Settings.KitAndValue[] kav = Settings.getValueHierarchy(kitClass, SettingsNames.KEY_BINDING_LIST);
//        List kbList = null;
//        for (int i = 0; i < kav.length; i++) {
//            if (kav[i].kitClass == kitClass) {
//                kbList = (List)kav[i].value;
//            }
//        }
//        if (kbList == null) {
//            kbList = new ArrayList();
//        }
//        
//        // must convert all members to serializable MultiKeyBinding
//        int cnt = kbList.size();
//        for (int i = 0; i < cnt; i++) {
//            Object o = kbList.get(i);
//            if (!(o instanceof MultiKeyBinding) && o != null) {
//                JTextComponent.KeyBinding b = (JTextComponent.KeyBinding)o;
//                kbList.set(i, new MultiKeyBinding(b.key, b.actionName));
//            }
//        }
//        return new ArrayList( kbList );
//    }
//
//    private void saveMacro(boolean overwriting){
//        Map macroMap = (Map)Settings.getValue( kitClass, SettingsNames.MACRO_MAP);        
//        Map newMap = new HashMap( macroMap );
//        newMap.put( panel.getMacroName(), panel.getMacroBody() );                
//        Settings.setValue( kitClass, SettingsNames.MACRO_MAP, newMap );
//        List listBindings = panel.getKeySequences();
//
//          // insert listBindings into keybindings
//        List keybindings = getKBList();
//        
//        if (overwriting) {
//            // overwriting existing macro. Removing all previously attached keybindings.
//            List removed = new ArrayList();
//            String macroName = BaseKit.macroActionPrefix+panel.getMacroName();
//            for (int i=0; i<keybindings.size(); i++){
//                MultiKeyBinding multiKey = (MultiKeyBinding)keybindings.get(i);
//                if (multiKey.actionName!=null && multiKey.actionName.equals(macroName)){
//                    removed.add(multiKey);
//                }
//            }
//            for (int i=0; i<removed.size(); i++){
//                keybindings.remove(removed.get(i));
//            }
//        }
//        
//        if (listBindings.size() > 0)
//        {
//            String actionName = new String(BaseKit.macroActionPrefix + panel.getMacroName());
//            for (int i = 0; i < listBindings.size(); i++)
//            {
//                KeyStroke[] keyStrokes = (KeyStroke[])listBindings.get(i);
//                MultiKeyBinding multiKey = new MultiKeyBinding(keyStrokes, actionName);
//                keybindings.add(multiKey);
//            }
//        }
//        // set new KEY_BINDING_LIST
//        Settings.setValue( kitClass, SettingsNames.KEY_BINDING_LIST, keybindings);
//    }
    
    protected int showConfirmDialog(String macroName){
        return JOptionPane.showConfirmDialog(panel,                     
                        MessageFormat.format(NbBundle.getBundle(MacroDialogSupport.class).getString("MDS_Overwrite"), //NOI18N
                            new Object[] {panel.getMacroName()}), 
                        NbBundle.getBundle(MacroDialogSupport.class).getString("MDS_Warning"), // NOI18N
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent evt ) {
//        Object source = evt.getSource();
//        if( source == okButton ) {
//            if (panel.getMacroName() == null || panel.getMacroName().length() == 0 || 
//                panel.getMacroName().trim().length() == 0
//            ) {
//                DialogDisplayer.getDefault ().notify (
//                    new NotifyDescriptor.Message (
//                        NbBundle.getBundle(MacroDialogSupport.class).getString("MDS_Empty_Name"), //NOI18N
//                        NotifyDescriptor.ERROR_MESSAGE
//                    )
//                );
//                
//                panel.nameField.requestFocusInWindow();
//                return;
//            }
//            Map macroMap = (Map)Settings.getValue( kitClass, SettingsNames.MACRO_MAP);
//            
//            if (!macroMap.containsKey(panel.getMacroName())){
//                saveMacro(false);
//            }else{
//                int retVal = showConfirmDialog(panel.getMacroName());
//                if (retVal == JOptionPane.CANCEL_OPTION || retVal == JOptionPane.CLOSED_OPTION) return;
//                if (retVal == JOptionPane.OK_OPTION) saveMacro(true);
//            }
//        }
        macroDialog.setVisible( false );
        macroDialog.dispose();        
    }
    
}
