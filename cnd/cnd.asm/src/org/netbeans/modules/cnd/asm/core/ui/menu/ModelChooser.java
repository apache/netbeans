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
