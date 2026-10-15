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
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryCustomizer;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
    
    private static final String SEPARATOR = "PersistenceProviderComboboxHelper.SEPARATOR";
    private static final String EMPTY = "PersistenceProviderComboboxHelper.EMPTY";
    private static final Provider preferredProvider = ProviderUtil.ECLIPSELINK_PROVIDER3_1;

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
            aProviderSupplier = new DefaultPersistenceProviderSupplier(project);
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
            
            private Object currentItem = providerCombo.getSelectedItem();
            private int currentIndex = providerCombo.getSelectedIndex();
            
            @Override
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
            boolean specialCase = (Util.isJPAVersionSupported(project, Persistence.VERSION_2_0)
                    || Util.isJPAVersionSupported(project, Persistence.VERSION_2_1)
                    || Util.isJPAVersionSupported(project, Persistence.VERSION_2_2)
                    || Util.isJPAVersionSupported(project, Persistence.VERSION_3_0)
                    || Util.isJPAVersionSupported(project, Persistence.VERSION_3_1)
                    || Util.isJPAVersionSupported(project, Persistence.VERSION_3_2))
                    && (defProviderVersion == null || defProviderVersion.equals(Persistence.VERSION_1_0));//jpa 3.1 is supported by default (or first) is jpa1.0 or udefined version provider
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
        @Override
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_NewPersistenceLibrary");
        }
        @Override
        public void performAction() {
            PersistenceLibraryCustomizer.showCustomizer();
        }
    }
    
    private static class ManageLibrariesItem implements LibraryItem {
        @Override
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_ManageLibraries");
        }
        @Override
        public void performAction() {
            LibrariesCustomizer.showCustomizer(null);
        }
    }

    private static class PersistenceProviderCellRenderer extends DefaultListCellRenderer {
        
        Provider defaultProvider;
        
        PersistenceProviderCellRenderer(Provider defaultProvider) {
            this.defaultProvider = defaultProvider;
        }
        
        @Override
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

        private final Project project;

        public DefaultPersistenceProviderSupplier(Project project) {
            this.project = project;
        }

        @Override
        public List<Provider> getSupportedProviders() {
            ArrayList<Provider> providers = new ArrayList<>();

            SourceGroup[] sourceGroups = ProjectUtils
                    .getSources(project)
                    .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            List<ClassPath> classPaths = new ArrayList<>();
            if (sourceGroups != null) {
                for (SourceGroup sourceGroup : sourceGroups) {
                    ClassPath cp = ClassPath.getClassPath(sourceGroup.getRootFolder(), ClassPath.COMPILE);
                    classPaths.add(cp);
                }
            }
            ClassPath cp = ClassPathSupport.createProxyClassPath(classPaths.toArray(ClassPath[]::new));

            for(Provider p: ProviderUtil.getAllProviders()) {
                if (p.isOnClassPath(cp) && !providers.contains(p)) {
                    providers.add(p);
                }
            }

            for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()){
                if (! providers.contains(each)){
                   providers.add(each);
                }
            }
            return providers;
        }

        @Override
        public boolean supportsDefaultProvider() {
            return false;
        }
    }
}
