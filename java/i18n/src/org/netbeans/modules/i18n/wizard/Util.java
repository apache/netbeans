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

package org.netbeans.modules.i18n.wizard;

import javax.swing.LayoutStyle;
import javax.swing.GroupLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.awt.Mnemonics;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
final class Util extends org.netbeans.modules.i18n.Util {
    public static final String HELP_ID_ADDITIONAL = "internation.Additional"; // NOI18N
    public static final String HELP_ID_SELECTSOURCES = "internation.SelectSources"; // NOI18N
    public static final String HELP_ID_FOUNDSTRINGS = "internation.FoundStrings"; // NOI18N
    public static final String HELP_ID_FOUNDMISSINGRESOURCES = "internation.FoundMissingResources"; // NOI18N
    public static final String HELP_ID_SELECTRESOURCE = "internation.SelectResource"; // NOI18N
    public static final String HELP_ID_SELECTTESTSOURCES = "internation.SelectTestSources"; // NOI18N
    public static final String HELP_ID_SELECTTESTRESOURCE = "internation.SelectTestResource"; // NOI18N
    
    public static String getString(String key) {
        return NbBundle.getMessage(org.netbeans.modules.i18n.wizard.Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }

    // Settings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Create empty settings used in i18n wizards. 
     */
    public static Map<DataObject,SourceData> createWizardSourceMap() {
        return new TreeMap<DataObject,SourceData>(new DataObjectComparator());
    }
    
    /** 
     * Create settings based on selected nodes. Finds all accepted data objects. 
     * Used by actions to populate wizard.
     * @param activatedNodes selected nodes 
     * @return map with accepted data objects as keys or empty map if no such
     * data object were found.
     */
    public static Map<DataObject,SourceData> createWizardSourceMap(Node[] activatedNodes) {
        Map<DataObject,SourceData> sourceMap = createWizardSourceMap();
        
        if (activatedNodes != null && activatedNodes.length > 0) {
            final VisibilityQuery visQuery = VisibilityQuery.getDefault();
            for (Node node : activatedNodes) {
                DataObject dobj = node.getCookie(DataObject.class);
                if (dobj != null && !visQuery.isVisible(dobj.getPrimaryFile())) {
                    continue;
                }

                DataObject.Container container = node.getCookie(DataObject.Container.class);
                if (container != null) {
                    for (DataObject dataObj : I18nUtil.getAcceptedDataObjects(container)) {
                        addSource(sourceMap, dataObj);
                    }
                }

                if (dobj == null) {
                    continue;
                }

                if (FactoryRegistry.hasFactory(dobj.getClass())) {
                    addSource(sourceMap, dobj);
                }
            }
        }
        
        return sourceMap;
    }
    
    /** Adds source to source map (I18N wizard settings). If there is already no change is done.
     * If it's added anew then it is tried to find correspondin reousrce, i.e.
     * first resource from the same folder.
     * @param sourceMap settings where to add teh sources
     * @param source source to add */
    public static void addSource(Map<DataObject,SourceData> sourceMap,
                                 DataObject source) {
        if (sourceMap.containsKey(source)) {
            return;
        }
        
        DataFolder folder = source.getFolder();
        
        if (folder == null) {
            sourceMap.put(source, null);
            return;
        }

        // try to associate Bundle file

        for (DataObject child : folder.getChildren()) {
            if (child instanceof PropertiesDataObject) { // PENDING 
                sourceMap.put(source, new SourceData(child));
                return;
            }
        }
        
        // No resource found in the same folder.
        sourceMap.put(source, null);
    }

    /** Shared enableness logic. Either DataObject.Container or EditorCookie must be present on all nodes.*/
    static boolean wizardEnabled(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }

        for (Node node : activatedNodes) {
            
            /*
             * This block of code fixes IssueZilla bug #63461:
             *
             *     Apisupport modules visualizes the contents of layer.xml
             *     in project view. The popup for folders in the layer.xml
             *     contains Tools->Internationalize->* actions.
             *
             *     Generally should hide on nonlocal files, I suppose.
             *
             * Local files are recognized by protocol of the corresponding URL -
             * local files are those that have protocol "file".
             */
            DataObject dobj = node.getCookie(DataObject.class);
            if (dobj != null) {
                FileObject primaryFile = dobj.getPrimaryFile();
                
                boolean isLocal;
                isLocal = !primaryFile.isVirtual()
                              && primaryFile.isValid()
                              && primaryFile.toURL().getProtocol().equals("file");  //NOI18N
                if (isLocal == false) {
                    return false;
                }
            }
            
            Object container = node.getCookie(DataObject.Container.class);
            if (container != null) {
                continue;
            }
//            if (node.getCookie(EditorCookie.class) == null) {
//                return false;
//            }

	    if (dobj == null) {
                return false;
            }
	    
            Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();
            if(!openProjects.isDone()) {
                return false;
            }
            
	    // check that the node has project
	    if (FileOwnerQuery.getOwner(dobj.getPrimaryFile()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare data objects according their package and name. 
     */
    private static class DataObjectComparator implements Comparator<DataObject> {

        /** Implements <code>Comparator</code> interface. */
        public int compare(DataObject d1, DataObject d2) {
            if(d1 == d2)
                return 0;
            
            if(d1 == null)
                return -1;
            
            if(d2 == null)
                return 1;

            //return d1.getPrimaryFile().getPackageName('.').compareTo(d2.getPrimaryFile().getPackageName('.'));
            return d1.getPrimaryFile().getPath().compareTo( d2.getPrimaryFile().getPath() );
        }
        
        /** Implements <code>Comparator</code> interface method. */
        @Override
        public boolean equals(Object obj) {
            return (this == obj);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    /**
     * @author  Marian Petras
     */
    static void layoutSelectResourcePanel(final Container thePanel,
                                          final String instructionsText,
                                          final String selectionLabelText,
                                          final Component selectionComp,
                                          final JButton button1,
                                          final JButton button2) {
        JTextArea instructions = new JTextArea();
        JLabel lblSelection = new JLabel();

        instructions.setColumns(20);
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setText(instructionsText);
        instructions.setWrapStyleWord(true);
        instructions.setDisabledTextColor(new JLabel().getForeground());
        instructions.setEnabled(false);
        instructions.setOpaque(false);

        lblSelection.setLabelFor(selectionComp);
        Mnemonics.setLocalizedText(lblSelection, selectionLabelText);

        JScrollPane scrollPane = new JScrollPane(selectionComp);

        Container filesSelection = new JPanel();
        GroupLayout layout = new GroupLayout(filesSelection);
        filesSelection.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(LEADING)
            .addComponent(lblSelection)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrollPane, 0, DEFAULT_SIZE, Integer.MAX_VALUE)
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(button1)
                    .addComponent(button2)))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, button1, button2);

        layout.setVerticalGroup(
            layout.createParallelGroup(LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblSelection)
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(scrollPane, 0, DEFAULT_SIZE, Integer.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(button1)
                        .addPreferredGap(RELATED)
                        .addComponent(button2))))
        );

        LayoutStyle layoutStyle = layout.getLayoutStyle();
        if (layoutStyle == null) {
            layoutStyle = LayoutStyle.getInstance();
        }

        BorderLayout mainLayout = new BorderLayout();
        thePanel.setLayout(mainLayout);
        thePanel.add(instructions, BorderLayout.PAGE_START);
        thePanel.add(filesSelection, BorderLayout.CENTER);
        mainLayout.setVgap(layoutStyle.getPreferredGap(instructions,
                                                       lblSelection,
                                                       UNRELATED,
                                                       SwingConstants.NORTH,
                                                       thePanel));
    }

}
