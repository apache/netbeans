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

package org.netbeans.modules.j2ee.persistence.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryCustomizer;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * A helper class for populating combo box with persistence providers.
 * Providers may be provided (no pun intended) by server in a container
 * managed environment or they might come from libraries.
 *
 * @author Libor Kotouc
 * @author Erno Mononen
 */
public final class PersistenceProviderComboboxHelper {
    
    private final static String SEPARATOR = "PersistenceProviderComboboxHelper.SEPARATOR";
    private final static String EMPTY = "PersistenceProviderComboboxHelper.EMPTY";
    private final static Provider preferredProvider = ProviderUtil.ECLIPSELINK_PROVIDER;

    private final PersistenceProviderSupplier providerSupplier;
    private final Project project;

    /**
     * Creates a new PersistenceProviderComboboxHelper. 
     * @param project the current project. Must have an implementation of 
     * the PersistenceProviderSupplier in its lookup.
     * @throws IllegalArgumentException if the project did not have an implementation of 
     * the PersistenceProviderSupplier in its lookup.
     */ 
    public PersistenceProviderComboboxHelper(Project project) {
        Parameters.notNull("project", project);
        
        PersistenceProviderSupplier aProviderSupplier =project.getLookup().lookup(PersistenceProviderSupplier.class); 
        
        if (aProviderSupplier == null){
            // a java se project
            aProviderSupplier = new DefaultPersistenceProviderSupplier();
        }
        this.project = project;
        this.providerSupplier = aProviderSupplier;
    }
    
    /**
     * Populates the given <code>providerCombo</code> with persistence providers. Supported 
     * providers from the project's server (if it had one) are also added. If the project
     * doesn't have a server, only providers from libraries
     * are added. Items for adding and managing libraries are always included.
     * @param providerCombo the combo box to be populated.
     */
    public void connect(final JComboBox providerCombo) {
        providerCombo.setEditable(false);
        initCombo(providerCombo);
        // handling of <ENTER> key event
        providerCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (KeyEvent.VK_ENTER == keyCode) {
                    Object selectedItem = providerCombo.getSelectedItem();
                    if (selectedItem instanceof LibraryItem) {
                        providerCombo.hidePopup();
                        ((LibraryItem) selectedItem).performAction();
                        e.consume();
                        initCombo(providerCombo);
                    }
                }
            }
        });
        
        providerCombo.addActionListener(new ActionListener() {
            
            Object currentItem = providerCombo.getSelectedItem();
            int currentIndex = providerCombo.getSelectedIndex();
            
            public void actionPerformed(ActionEvent e) {
                Object selectedItem = providerCombo.getSelectedItem();
                // skipping of separator
                if (SEPARATOR.equals(selectedItem)) {
                    int selectedIndex = providerCombo.getSelectedIndex();
                    if (selectedIndex > currentIndex) {
                        currentIndex = selectedIndex + 1;
                        currentItem = providerCombo.getItemAt(currentIndex);
                    } else {
                        currentIndex = selectedIndex - 1;
                        currentItem = providerCombo.getItemAt(currentIndex);
                    }
                    providerCombo.setSelectedItem(currentItem);
                    // handling mouse click, see KeyEvent.getKeyModifiersText(e.getModifiers())
                } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    if (selectedItem instanceof LibraryItem) {
                        providerCombo.setPopupVisible(false);
                        ((LibraryItem) selectedItem).performAction();
                        initCombo(providerCombo);
                    }
                } else {
                    currentItem = selectedItem;
                    currentIndex = providerCombo.getSelectedIndex();
                }
            }
        });
    }
    
    private void initCombo(JComboBox providerCombo) {
        
        DefaultComboBoxModel providers = new DefaultComboBoxModel();
        
        for(Provider each : providerSupplier.getSupportedProviders()){
           providers.addElement(each);
        }

        if (providers.getSize() == 0 && providerSupplier.supportsDefaultProvider()){
            providers.addElement(ProviderUtil.DEFAULT_PROVIDER);
        } 

        if (providers.getSize() == 0){
            providers.addElement(EMPTY);
        }
        
        providerCombo.setModel(providers);
        providerCombo.addItem(SEPARATOR);
        providerCombo.addItem(new NewPersistenceLibraryItem());
        providerCombo.addItem(new ManageLibrariesItem());
        providerCombo.setRenderer(new PersistenceProviderCellRenderer(getDefaultProvider(providers)));
        //select either default or first or preferred provider depending on project details
        int selectIndex = 0;
        if(providers.getSize()>1 && providers.getElementAt(0) instanceof Provider){
            String defProviderVersion = ProviderUtil.getVersion((Provider) providers.getElementAt(0));
            boolean specialCase = (Util.isJPAVersionSupported(project, Persistence.VERSION_2_0) || Util.isJPAVersionSupported(project, Persistence.VERSION_2_1)) && (defProviderVersion == null || defProviderVersion.equals(Persistence.VERSION_1_0));//jpa 2.0 is supported by default (or first) is jpa1.0 or udefined version provider
            if(specialCase){
                for (int i = 1; i<providers.getSize() ; i++){
                    if(preferredProvider.equals(providers.getElementAt(i))){
                        selectIndex = i;
                        break;
                    }
                }
            }
        }
        providerCombo.setSelectedIndex(selectIndex);
    }
    
    
    
    /**
     * Gets the provider representing the default provider from
     * the given <code>providers</code>.
     * @param providers the providers. if the default provider is supported,
     * it has to be the first element in the model.
     * @return the default provider or null if is not supported.
     */ 
    private Provider getDefaultProvider(ComboBoxModel providers){
        if (!providerSupplier.supportsDefaultProvider()){
            return null;
        }
        if (providers.getElementAt(0) instanceof Provider){
            return (Provider) providers.getElementAt(0);
        }
        return null;
    }
    
    public static interface LibraryItem {
        String getText();
        void performAction();
    }
    
    private static class NewPersistenceLibraryItem implements LibraryItem {
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_NewPersistenceLibrary");
        }
        public void performAction() {
            PersistenceLibraryCustomizer.showCustomizer();
        }
    }
    
    private static class ManageLibrariesItem implements LibraryItem {
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_ManageLibraries");
        }
        public void performAction() {
            LibrariesCustomizer.showCustomizer(null);
        }
    }

    private static class PersistenceProviderCellRenderer extends DefaultListCellRenderer {
        
        Provider defaultProvider;
        
        PersistenceProviderCellRenderer(Provider defaultProvider) {
            this.defaultProvider = defaultProvider;
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            if (value instanceof Provider) {
                Provider provider = (Provider)value;
                String text = provider.getDisplayName();
                if (value.equals(defaultProvider) && (!(value instanceof DefaultProvider))) {
                    text += NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_DEFAULT_PROVIDER");
                }
                setText(text);
                
            } else if (SEPARATOR.equals(value)) {
                JSeparator s = new JSeparator();
                s.setPreferredSize(new Dimension(s.getWidth(), 1));
                s.setForeground(Color.BLACK);
                return s;
                
            } else if (EMPTY.equals(value)) {
                setText(" ");
                
            } else if (value instanceof LibraryItem) {
                setText(((LibraryItem) value).getText());
                
            } else {
                setText(value != null ?  value.toString() : ""); // NOI18N
            }
            
            return this;
        }
        
    }
    
    /**
     * An implementation of the PersistenceProviderSupplier that returns an empty list for supported
     * providers and doesn't support a default provider. Used when an implementation of 
     * the PersistenceProviderSupplier can't be found in the project lookup (as is the case
     * for instance for Java SE projects).
     */ 
    private static class DefaultPersistenceProviderSupplier implements PersistenceProviderSupplier{
        
        @Override
        public List<Provider> getSupportedProviders() {
            ArrayList<Provider> providers = new ArrayList<Provider>();
            for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()){
                boolean found = false;
                for (int i = 0; i < providers.size(); i++) {
                    Object elem = providers.get(i);
                    if (elem instanceof Provider && each.equals(elem)){
                        found = true;
                        break;
                    }
                }
                if (!found){
                   providers.add(each);
                }
            }
            return providers;
        }

        public boolean supportsDefaultProvider() {
            return false;
        }
}
}
