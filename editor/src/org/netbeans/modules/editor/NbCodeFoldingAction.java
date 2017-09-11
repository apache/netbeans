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
