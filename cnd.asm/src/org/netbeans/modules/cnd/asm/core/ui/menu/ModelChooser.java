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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.asm.core.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import java.util.Collection;

import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

import org.netbeans.modules.cnd.asm.model.AsmModelProvider;
import org.netbeans.modules.cnd.asm.model.AsmSyntaxProvider;
import org.netbeans.modules.cnd.asm.model.AsmTypesProvider;
import org.netbeans.modules.cnd.asm.model.AsmTypesProvider.AsmTypesEntry;


public class ModelChooser extends AbstractAction
                                implements Presenter.Menu {

    private static AsmModelProvider curModel;
    private static AsmSyntaxProvider curSyntax;
    
    private static final JMenu menu;
    
    static {   
         menu = new UpdatingMenu();
    }        
    
    public static AsmModelProvider getModelProvider() {
        return curModel;
    }
    
    public static AsmSyntaxProvider getSyntaxProvider() {
        return curSyntax;
    }
    
    public ModelChooser() {
        super(NbBundle.getMessage(ModelChooser.class, "CTL_ModelChooser")); // NOI18N
    }

    public void actionPerformed(ActionEvent ev) {
        assert false;// no operation
    }

    public JMenuItem getMenuPresenter() {        
        String label = NbBundle.getMessage(ModelChooser.class, "CTL_ModelChooser"); // NOI18N
        Mnemonics.setLocalizedText(menu, label);
        return menu;
    }

    private static final class UpdatingMenu extends JMenu implements DynamicMenuContent {

        private final JComponent[] models;
        
        public UpdatingMenu() {
            models = calcPresenters();
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return models;
        }
        
        public JComponent[] getMenuPresenters() {  
            return models;
        }

        public JComponent[] calcPresenters() {                                                 
            removeAll();                            
            ButtonGroup group = new ButtonGroup();   
            
            boolean isFirst = true;
            
            Collection<? extends AsmTypesProvider> mods = 
                 Lookup.getDefault().lookup(new Lookup.Template<AsmTypesProvider>(AsmTypesProvider.class)).allInstances();
                            
            /*
            Map<AsmModelProvider, List<AsmSyntaxProvider>> mapa = 
                    new HashMap<AsmModelProvider, List<AsmSyntaxProvider>>(); */
            
            for (AsmTypesProvider p : mods ) {
                 for (AsmTypesEntry type : p.getAsmTypes()) {
                     for (AsmSyntaxProvider synt : type.getSyntaxProviders()) {
                         final AsmSyntaxProvider closureSyntax = synt;
                         final AsmModelProvider closureModel = type.getModelProvider();

                         String modelName = closureModel.toString();
                         String syntName = closureSyntax.toString();
                         JRadioButtonMenuItem item = new JRadioButtonMenuItem(modelName + " - " + syntName); // NOI18N
                             item.addActionListener(new ActionListener() {
                                 public void actionPerformed(ActionEvent event) {
                                     curModel = closureModel;
                                     curSyntax = closureSyntax;
                                 }
                             });     

                           group.add(item);
                           add(item);     
                           if (isFirst) {
                               group.setSelected(item.getModel(), true);
                               curModel = closureModel;
                               curSyntax = closureSyntax;
                               isFirst = false;
                           }
                  }
               }                         
            }            
            setEnabled(true);
            
            return new JComponent[] {this};
        }              
    }   
}
