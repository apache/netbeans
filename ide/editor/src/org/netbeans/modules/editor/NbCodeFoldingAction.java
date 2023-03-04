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

package org.netbeans.modules.editor;

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *  Code Folding action displayed under Menu/View/
 *
 *  @author  Martin Roskanin
 */
public class NbCodeFoldingAction implements Presenter.Menu {

    
    /** Creates a new instance of NbCodeFoldingAction */
    public NbCodeFoldingAction() {
    }
    
    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
            "Menu/View/CodeFolds"); //NOI18N
    }        

    public boolean isEnabled() {
        return false;
    }

    /** Get a menu item that can present this action in a {@link javax.swing.JMenu}.
    * @return the representation for this action
    */
    public JMenuItem getMenuPresenter(){
        return new CodeFoldsMenu(getName());
    }
    
    private static JTextComponent getComponent(){
        return Utilities.getFocusedComponent();
    }
    
    public void actionPerformed (java.awt.event.ActionEvent ev){
    }
    
    private BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? BaseKit.getKit(NbEditorKit.class) : Utilities.getKit(component);
    }
    
    public class CodeFoldsMenu extends JMenu implements DynamicMenuContent {
        public CodeFoldsMenu(){
            super();
        }
        
        public CodeFoldsMenu(String s){
            super(s);
            //#40585 fix start - setting the empty, transparent icon for the menu item to align it correctly with other items
            //setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/empty.gif"))); //NOI18N
            //#40585 fix end
            org.openide.awt.Mnemonics.setLocalizedText(this, s);
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[] { this };
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            getPopupMenu();
            return items;
        }
        
        public @Override JPopupMenu getPopupMenu(){
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            boolean enable = false;
            BaseKit bKit = getKit();
            if (bKit==null) bKit = BaseKit.getKit(NbEditorKit.class);
            if (bKit!=null){
                Action action = bKit.getActionByName(NbEditorKit.generateFoldPopupAction);
                if (action instanceof BaseAction) {
                    JTextComponent component = NbCodeFoldingAction.getComponent();
                    MimePath mimePath = component == null ? MimePath.EMPTY : MimePath.parse(DocumentUtilities.getMimeType(component));
                    Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                    boolean foldingAvailable = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
                    
                    if (foldingAvailable){
                        ActionMap contextActionmap = org.openide.util.Utilities.actionsGlobalContext().lookup(ActionMap.class);
                        if (contextActionmap!=null){
                            foldingAvailable = contextActionmap.get(BaseKit.collapseFoldAction) != null &&
                                component != null;

                            if (!foldingAvailable){
                                bKit = BaseKit.getKit(NbEditorKit.class);
                                if (bKit!=null){
                                    Action defaultAction = bKit.getActionByName(NbEditorKit.generateFoldPopupAction);
                                    if (defaultAction instanceof BaseAction) action = defaultAction;
                                }
                            }
                        }
                    }

                    JMenu menu = (JMenu)((BaseAction)action).getPopupMenuItem(foldingAvailable ? component : null);
                    if (menu!=null){
                        Component comps[] = menu.getMenuComponents();
                        for (int i=0; i<comps.length; i++){
                            pm.add(comps[i]);
                            if (comps[i].isEnabled() && !(comps[i] instanceof JSeparator)) {
                                enable = true;
                            }
                        }
                    }
                }
            }
            setEnabled(enable);
            pm.pack();
            return pm;
        }
    }
    
}
