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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.ui.elements.ElementNode.Description;
import org.netbeans.modules.refactoring.java.ui.elements.SortActionSupport.SortByNameAction;
import org.netbeans.modules.refactoring.java.ui.elements.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.refactoring.java.ui.elements.*;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.*;

/**
 *
 * @author Ralph Ruijs
 */
@Messages({"#filters",
    "LBL_ShowNonPublic=Show Non-Public Members",
    "LBL_ShowStatic=Show Static Members",
    "LBL_ShowFields=Show Fields",
    "LBL_ShowInherited=Show Inherited Members",
    "LBL_ShowNonPublicTip=Show non-public members",
    "LBL_ShowStaticTip=Show static members",
    "LBL_ShowFieldsTip=Show fields",
    "LBL_ShowInheritedTip=Show inherited members"})
public class MoveMembersPanel extends javax.swing.JPanel implements CustomRefactoringPanel, ExplorerManager.Provider, DescriptionFilter, FiltersManager.FilterChangeListener {

    private static final String JAVADOC = "updateJavadoc.moveMembers"; // NOI18N
    private static final String DELEGATE = "delegate.moveMembers"; // NOI18N
    private static final String DEPRECATE = "deprecate.moveMembers"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(MoveMembersPanel.class.getName(), 1);
    private final ListCellRenderer GROUP_CELL_RENDERER = new MoveClassPanel.GroupCellRenderer();
    private final ListCellRenderer PROJECT_CELL_RENDERER = new MoveClassPanel.ProjectCellRenderer();
    private final ListCellRenderer CLASS_CELL_RENDERER = new MoveClassPanel.ClassListCellRenderer();
    private ChangeListener parent;
    private FiltersManager filtersManager;
    private final ExplorerManager manager;
    private final TreePathHandle[] selectedElements;
    private final FileObject fileObject;
    private TapPanel filtersPanel;
    /**
     * constants for defined filters
     */
    private static final String SHOW_NON_PUBLIC = "show_non_public"; //NOI18N
    private static final String SHOW_STATIC = "show_static"; //NOI18N
    private static final String SHOW_FIELDS = "show_fields"; //NOI18N
    private static final String SHOW_INHERITED = "show_inherited"; //NOI18N
    private JToggleButton sortByNameButton;
    private JToggleButton sortByPositionButton;
    private boolean naturalSort;
    private final Action[] actions;
    private Project project;
    private SourceGroup[] groups;
    private JLabel label;
    private ComponentListener componenListener;

    /**
     * Creates new form MoveMembersPanel
     */
    public MoveMembersPanel(TreePathHandle[] selectedElements, final ChangeListener parent) {
        manager = new ExplorerManager();
        this.parent = parent;
        this.naturalSort = NbPreferences.forModule(MoveMembersPanel.class).getBoolean("naturalSort", false); //NOI18N
        this.selectedElements = selectedElements;
        this.fileObject = selectedElements[0].getFileObject();
        initComponents();

        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer(PROJECT_CELL_RENDERER);
        classComboBox.setRenderer(CLASS_CELL_RENDERER);
        Project fileOwner = this.fileObject != null ? FileOwnerQuery.getOwner(this.fileObject) : null;
        project = fileOwner != null ? fileOwner : OpenProjects.getDefault().getOpenProjects()[0];

        manager.setRootContext(ElementNode.getWaitNode());
        outlineView1.getOutline().setRootVisible(true);
        outlineView1.getOutline().setTableHeader(null);
        initFiltersPanel();
        actions = new Action[]{
            new SortByNameAction(this),
            new SortBySourceAction(this)
        };
        ItemListener parentListener = new ItemListener() {
                                        @Override
                                        public void itemStateChanged(ItemEvent e) {
                                            parent.stateChanged(null);
                                        }
                                    };
        btnAsIs.addItemListener(parentListener);
        btnDefault.addItemListener(parentListener);
        btnEscalate.addItemListener(parentListener);
        btnJavadocAsIs.addItemListener(parentListener);
        btnJavadocUpdate.addItemListener(parentListener);
        btnPrivate.addItemListener(parentListener);
        btnProtected.addItemListener(parentListener);
        btnPublic.addItemListener(parentListener);
    }

    public Action[] getActions() {
        return actions;
    }

    private void initFiltersPanel() throws MissingResourceException {
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        FiltersDescription desc = new FiltersDescription();

//        desc.addFilter(SHOW_INHERITED,
//                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowInherited"), //NOI18N
//                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowInheritedTip"), //NOI18N
//                false, ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/filterHideInherited.png", false), //NOI18N
//                null);
        desc.addFilter(SHOW_FIELDS,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowFields"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowFieldsTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/filterHideFields.png", false), //NOI18N
                null);
        desc.addFilter(SHOW_STATIC,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowStatic"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowStaticTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/filterHideStatic.png", false), //NOI18N
                null);
        desc.addFilter(SHOW_NON_PUBLIC,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowNonPublic"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowNonPublicTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/filterHideNonPublic.png", false), //NOI18N
                null);
        AbstractButton[] res = new AbstractButton[4];
        sortByNameButton = new JToggleButton(new SortActionSupport.SortByNameAction(this));
        sortByNameButton.setToolTipText(sortByNameButton.getText());
        sortByNameButton.setText(null);
        sortByNameButton.setSelected(!isNaturalSort());
        res[0] = sortByNameButton;

        sortByPositionButton = new JToggleButton(new SortActionSupport.SortBySourceAction(this));
        sortByPositionButton.setToolTipText(sortByPositionButton.getText());
        sortByPositionButton.setText(null);
        sortByPositionButton.setSelected(isNaturalSort());
        res[1] = sortByPositionButton;

        res[2] = new JButton(null, new JCheckBoxIcon(true, new Dimension(16, 16)));
        res[2].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(true);
            }
        });
        res[2].setToolTipText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "TIP_SelectAll"));
        
        res[3] = new JButton(null, new JCheckBoxIcon(false, new Dimension(16, 16)));
        res[3].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(false);
            }
        });
        res[3].setToolTipText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "TIP_DeselectAll"));
        
        filtersManager = FiltersDescription.createManager(desc);
        filtersManager.hookChangeListener(this);

        JComponent buttons = filtersManager.getComponent(res);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) //NOI18N
        {
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        membersListPanel.add(filtersPanel, BorderLayout.SOUTH);
    }

    private void initValues() {

        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        Arrays.sort(openProjects, new MoveClassPanel.ProjectByDisplayNameComparator());
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel(openProjects);
        projectsComboBox.setModel(projectsModel);
        projectsComboBox.setSelectedItem(project);

        updateRoots();
        updatePackages();
        updateClasses();
    }

    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        int preselectedItem = 0;
        for (int i = 0; i < groups.length; i++) {
            if (this.fileObject != null) {
                try {
                    if (groups[i].contains(this.fileObject)) {
                        preselectedItem = i;
                    }
                } catch (IllegalArgumentException e) {
                    // XXX this is a poor abuse of exception handling
                }
            }
        }

        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        if (groups.length > 0) {
            rootComboBox.setSelectedIndex(preselectedItem);
        }
    }

    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        packageComboBox.setModel(g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel());
    }

    private void updateClasses() {
        classComboBox.setModel(new DefaultComboBoxModel(new Object[]{ElementNode.getWaitNode()}));
        RP.post(new Runnable() {

            @Override
            public void run() {
                final ComboBoxModel model;
                SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
                String packageName = packageComboBox.getSelectedItem().toString();
                if (g != null && packageName != null) {
                    String pathname = packageName.replace(".", "/"); // NOI18N
                    FileObject fo = g.getRootFolder().getFileObject(pathname);
                    ClassPath bootCp = ClassPath.getClassPath(fo, ClassPath.BOOT);
                    ClassPath compileCp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                    ClassPath sourcePath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                    final ClasspathInfo info = ClasspathInfo.create(bootCp, compileCp, sourcePath);
                    Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<ClassIndex.SearchScopeType>(1);
                    final Set<String> packageSet = Collections.singleton(packageName);
                    searchScopeType.add(new ClassIndex.SearchScopeType() {

                        @Override
                        public Set<? extends String> getPackages() {
                            return packageSet;
                        }

                        @Override
                        public boolean isSources() {
                            return true;
                        }

                        @Override
                        public boolean isDependencies() {
                            return false;
                        }
                    });
                    final Set<ElementHandle<TypeElement>> result = info.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, searchScopeType);
                    if (result != null && !result.isEmpty()) {
                        JavaSource javaSource = JavaSource.create(info);
                        final ArrayList<ClassItem> items = new ArrayList<ClassItem>(result.size());
                        try {
                            javaSource.runUserActionTask(new CancellableTask<CompilationController>() {

                                private AtomicBoolean cancel = new AtomicBoolean();

                                @Override
                                public void cancel() {
                                    this.cancel.set(true);
                                }

                                @Override
                                public void run(CompilationController parameter) throws Exception {
                                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                                    for (ElementHandle<TypeElement> elementHandle : result) {
                                        TypeElement element = elementHandle.resolve(parameter);
                                        if (element != null) {
                                            String fqn = element.getQualifiedName().toString();
                                            if (!fqn.isEmpty()) {
                                                Icon icon = ElementIcons.getElementIcon(element.getKind(), element.getModifiers());
                                                int packageNameLength = packageSet.iterator().next().length();
                                                String className = packageNameLength > 0 && packageNameLength < fqn.length() ? fqn.substring(packageNameLength + 1) : fqn;
                                                ClassItem classItem = new ClassItem(className, icon, TreePathHandle.create(element, parameter));
                                                items.add(classItem);
                                            }
                                        }
                                    }
                                }
                            }, true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        items.sort(new Comparator() {
                            private Comparator COLLATOR = Collator.getInstance();

                            @Override
                            public int compare(Object o1, Object o2) {

                                if ( !( o1 instanceof ClassItem ) ) {
                                    return 1;
                                }
                                if ( !( o2 instanceof ClassItem ) ) {
                                    return -1;
                                }

                                ClassItem p1 = (ClassItem)o1;
                                ClassItem p2 = (ClassItem)o2;

                                return COLLATOR.compare(p1.getDisplayName(), p2.getDisplayName());
                            }
                        });
                        model = new DefaultComboBoxModel(items.toArray(new ClassItem[0]));
                    } else {
                        model = new DefaultComboBoxModel();
                    }
                } else {
                    model = new DefaultComboBoxModel();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        classComboBox.setModel(model);
                        parent.stateChanged(null);
                    }
                });
            }
        });
    }

    @Override
    public Collection<Description> filter(Collection<Description> original) {

        boolean non_public = filtersManager.isSelected(SHOW_NON_PUBLIC);
        boolean statik = filtersManager.isSelected(SHOW_STATIC);
        boolean fields = filtersManager.isSelected(SHOW_FIELDS);
        boolean inherited = /* filtersManager.isSelected(SHOW_INHERITED) */ false;

        boolean warn = false;
        ArrayList<Description> result = new ArrayList<Description>(original.size());
        for (Description description : original) {

            if (description.isConstructor()) {
                if(description.getSelected() == Boolean.TRUE) {
                    warn |= true;
                }
                continue;
            }
            if (!inherited && description.isInherited()) {
                if(description.getSelected() == Boolean.TRUE) {
                    warn |= true;
                }
                continue;
            }
            if (!non_public
                    && !description.getModifiers().contains(Modifier.PUBLIC)) {
                if(description.getSelected() == Boolean.TRUE) {
                    warn |= true;
                }
                continue;
            }

            if (!statik && description.getModifiers().contains(Modifier.STATIC)) {
                if(description.getSelected() == Boolean.TRUE) {
                    warn |= true;
                }
                continue;
            }

            if (!fields && description.getKind() == ElementKind.FIELD) {
                if(description.getSelected() == Boolean.TRUE) {
                    warn |= true;
                }
                continue;
            }
            result.add(description);
        }
        result.sort(isNaturalSort() ? Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR);
        if(warn) {
            if(this.label == null && outlineView1.isValid()) {
                final JLayeredPane layeredPaneAbove = JLayeredPane.getLayeredPaneAbove(outlineView1);
                ImageIcon imageIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/warning_16.png", false); //NOI18N
                this.label = new JLabel("Some selected members are not visible", imageIcon, SwingConstants.LEFT);//NOI18N
                this.label.setBackground(outlineView1.getBackground());
                this.label.setOpaque(true);

                Rectangle ownerCompBounds = SwingUtilities.convertRectangle(outlineView1.getParent(), outlineView1.getBounds(), layeredPaneAbove);

                final Dimension labelSize = label.getPreferredSize();
                final Insets insets = outlineView1.getInsets();
                int x = ownerCompBounds.x + ownerCompBounds.width - labelSize.width - insets.right;
                int y = ownerCompBounds.y + ownerCompBounds.height - labelSize.height - insets.bottom;
                label.setBounds(x, y, labelSize.width, labelSize.height);
                outlineView1.addComponentListener(this.componenListener = new ComponentListener() {

                    @Override
                    public void componentResized(ComponentEvent e) {
                        Rectangle ownerCompBounds = SwingUtilities.convertRectangle(outlineView1.getParent(), outlineView1.getBounds(), layeredPaneAbove);
                        int x = ownerCompBounds.x + ownerCompBounds.width - labelSize.width - insets.right;
                        int y = ownerCompBounds.y + ownerCompBounds.height - labelSize.height - insets.bottom;
                        label.setBounds(x, y, labelSize.width, labelSize.height);
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                        Rectangle ownerCompBounds = SwingUtilities.convertRectangle(outlineView1.getParent(), outlineView1.getBounds(), layeredPaneAbove);
                        int x = ownerCompBounds.x + ownerCompBounds.width - labelSize.width - insets.right;
                        int y = ownerCompBounds.y + ownerCompBounds.height - labelSize.height - insets.bottom;
                        label.setBounds(x, y, labelSize.width, labelSize.height);
                    }

                    @Override
                    public void componentShown(ComponentEvent e) {}

                    @Override
                    public void componentHidden(ComponentEvent e) {}
                });
                layeredPaneAbove.add(label, (JLayeredPane.POPUP_LAYER - 1));
            }
        } else {
            JLayeredPane layeredPaneAbove = JLayeredPane.getLayeredPaneAbove(outlineView1);
            if(this.label != null) {
                outlineView1.removeComponentListener(componenListener);
                componenListener = null;
                layeredPaneAbove.remove(label);
                label = null;
                layeredPaneAbove.repaint();
            }
        }
        return result;
    }

    public void setNaturalSort(boolean naturalSort) {
        this.naturalSort = naturalSort;
        NbPreferences.forModule(MoveMembersPanel.class).putBoolean("naturalSort", naturalSort); //NOI18N
        if (null != sortByNameButton) {
            sortByNameButton.setSelected(!naturalSort);
        }
        if (null != sortByPositionButton) {
            sortByPositionButton.setSelected(naturalSort);
        }
        sort();
    }

    public void sort() {
        ElementNode root = getRootNode();
        if (null != root) {
            root.refreshRecursively();
        }
    }

    private ElementNode getRootNode() {
        Node n = manager.getRootContext();
        if (n instanceof ElementNode) {
            return (ElementNode) n;
        } else {
            return null;
        }
    }

    private void selectAll(boolean select) {
        for (Node node : manager.getRootContext().getChildren().getNodes()) {
            if (node instanceof ElementNode) {
                ElementNode elementNode = (ElementNode) node;
                CheckableNode check = elementNode.getLookup().lookup(CheckableNode.class);
                if (check != null) {
                    check.setSelected(select);
                    elementNode.selectionChanged();
                }
            }
        }
    }
    private boolean initialized = false;

    @Override
    public void initialize() {
        if (!initialized) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    JavaSource javaSource = JavaSource.forFileObject(fileObject);
                    if (javaSource != null) {
                        try {
                            javaSource.runUserActionTask(new ElementScanningTask(), true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    initValues();
                }
            });
            initialized = true;
            parent.stateChanged(null);
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        visibilityButtonGroup = new javax.swing.ButtonGroup();
        javadocButtonGroup = new javax.swing.ButtonGroup();
        membersListPanel = new javax.swing.JPanel();
        outlineView1 = new org.openide.explorer.view.OutlineView();
        visibilityPanel = new javax.swing.JPanel();
        btnEscalate = new javax.swing.JRadioButton();
        btnAsIs = new javax.swing.JRadioButton();
        btnPrivate = new javax.swing.JRadioButton();
        btnDefault = new javax.swing.JRadioButton();
        btnProtected = new javax.swing.JRadioButton();
        btnPublic = new javax.swing.JRadioButton();
        lblMoveMembersFrom = new javax.swing.JLabel();
        lblSource = new javax.swing.JLabel();
        chkDelegate = new javax.swing.JCheckBox();
        chkDeprecate = new javax.swing.JCheckBox();
        targetPanel = new javax.swing.JPanel();
        rootComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        labelProject = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        classComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelClass = new javax.swing.JLabel();
        javadocPanel = new javax.swing.JPanel();
        btnJavadocUpdate = new javax.swing.JRadioButton();
        btnJavadocAsIs = new javax.swing.JRadioButton();

        membersListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.membersListPanel.border.title"))); // NOI18N
        membersListPanel.setLayout(new java.awt.BorderLayout());

        outlineView1.setDoubleBuffered(true);
        outlineView1.setDragSource(false);
        outlineView1.setDropTarget(false);
        outlineView1.setTreeSortable(true);
        membersListPanel.add(outlineView1, java.awt.BorderLayout.CENTER);

        visibilityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.visibilityPanel.border.title"))); // NOI18N

        visibilityButtonGroup.add(btnEscalate);
        btnEscalate.setSelected(true);
        btnEscalate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnEscalate.text")); // NOI18N
        btnEscalate.setActionCommand(Visibility.ESCALATE.name());

        visibilityButtonGroup.add(btnAsIs);
        btnAsIs.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnAsIs.text")); // NOI18N
        btnAsIs.setActionCommand(Visibility.ASIS.name());

        visibilityButtonGroup.add(btnPrivate);
        btnPrivate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnPrivate.text")); // NOI18N
        btnPrivate.setActionCommand(Visibility.PRIVATE.name());

        visibilityButtonGroup.add(btnDefault);
        btnDefault.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnDefault.text")); // NOI18N
        btnDefault.setActionCommand(Visibility.DEFAULT.name());

        visibilityButtonGroup.add(btnProtected);
        btnProtected.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnProtected.text")); // NOI18N
        btnProtected.setActionCommand(Visibility.PROTECTED.name());

        visibilityButtonGroup.add(btnPublic);
        btnPublic.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnPublic.text")); // NOI18N
        btnPublic.setActionCommand(Visibility.PUBLIC.name());

        javax.swing.GroupLayout visibilityPanelLayout = new javax.swing.GroupLayout(visibilityPanel);
        visibilityPanel.setLayout(visibilityPanelLayout);
        visibilityPanelLayout.setHorizontalGroup(
            visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visibilityPanelLayout.createSequentialGroup()
                .addGroup(visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEscalate)
                    .addComponent(btnAsIs)
                    .addComponent(btnPrivate)
                    .addComponent(btnDefault)
                    .addComponent(btnProtected)
                    .addComponent(btnPublic))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        visibilityPanelLayout.setVerticalGroup(
            visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visibilityPanelLayout.createSequentialGroup()
                .addComponent(btnEscalate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAsIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrivate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDefault)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProtected)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPublic))
        );

        lblMoveMembersFrom.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.lblMoveMembersFrom.text")); // NOI18N

        lblSource.setText("<ClassName>"); // NOI18N

        chkDelegate.setSelected(((Boolean) RefactoringModule.getOption(DELEGATE, Boolean.FALSE)).booleanValue());
        chkDelegate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.chkDelegate.text")); // NOI18N
        chkDelegate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkDelegateItemStateChanged(evt);
            }
        });

        chkDeprecate.setSelected(((Boolean) RefactoringModule.getOption(DEPRECATE, Boolean.TRUE)).booleanValue());
        chkDeprecate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.chkDeprecate.text")); // NOI18N
        chkDeprecate.setEnabled(((Boolean) RefactoringModule.getOption(DELEGATE, Boolean.FALSE)).booleanValue());
        chkDeprecate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkDeprecateItemStateChanged(evt);
            }
        });

        targetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.targetPanel.border.title"))); // NOI18N

        rootComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rootComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.labelLocation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.labelProject.text")); // NOI18N

        packageComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                packageComboBoxItemStateChanged(evt);
            }
        });

        classComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                classComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.labelPackage.text")); // NOI18N

        projectsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectsComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelClass, org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.labelClass.text")); // NOI18N

        javax.swing.GroupLayout targetPanelLayout = new javax.swing.GroupLayout(targetPanel);
        targetPanel.setLayout(targetPanelLayout);
        targetPanelLayout.setHorizontalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addComponent(labelClass, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(classComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addComponent(labelPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(packageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addComponent(labelProject, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(projectsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addComponent(labelLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(rootComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        targetPanelLayout.setVerticalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(labelProject))
                    .addComponent(projectsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(labelLocation))
                    .addComponent(rootComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(labelPackage))
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(labelClass))
                    .addComponent(classComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        javadocPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.javadocPanel.border.title"))); // NOI18N

        javadocButtonGroup.add(btnJavadocUpdate);
        btnJavadocUpdate.setSelected(((Boolean) RefactoringModule.getOption(JAVADOC, Boolean.FALSE)).booleanValue());
        btnJavadocUpdate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnJavadocUpdate.text")); // NOI18N

        javadocButtonGroup.add(btnJavadocAsIs);
        btnJavadocAsIs.setSelected(!((Boolean) RefactoringModule.getOption(JAVADOC, Boolean.FALSE)).booleanValue());
        btnJavadocAsIs.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnJavadocAsIs.text")); // NOI18N

        javax.swing.GroupLayout javadocPanelLayout = new javax.swing.GroupLayout(javadocPanel);
        javadocPanel.setLayout(javadocPanelLayout);
        javadocPanelLayout.setHorizontalGroup(
            javadocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javadocPanelLayout.createSequentialGroup()
                .addGroup(javadocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnJavadocAsIs)
                    .addComponent(btnJavadocUpdate))
                .addGap(75, 75, 75))
        );
        javadocPanelLayout.setVerticalGroup(
            javadocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javadocPanelLayout.createSequentialGroup()
                .addComponent(btnJavadocAsIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnJavadocUpdate)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(chkDeprecate))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblMoveMembersFrom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSource))
                            .addComponent(chkDelegate))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(targetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(membersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(visibilityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(javadocPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMoveMembersFrom)
                    .addComponent(lblSource))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(visibilityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(javadocPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(membersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkDelegate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkDeprecate)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkDeprecateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkDeprecateItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(DEPRECATE, b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkDeprecateItemStateChanged

    private void chkDelegateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkDelegateItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(DELEGATE, b);
        chkDeprecate.setEnabled(b);
        parent.stateChanged(null);
    }//GEN-LAST:event_chkDelegateItemStateChanged

    private void projectsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_projectsComboBoxItemStateChanged
        project = (Project) projectsComboBox.getSelectedItem();
        updateRoots();
        updatePackages();
        updateClasses();
    }//GEN-LAST:event_projectsComboBoxItemStateChanged

    private void rootComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rootComboBoxItemStateChanged
        updatePackages();
        updateClasses();
    }//GEN-LAST:event_rootComboBoxItemStateChanged

    private void packageComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_packageComboBoxItemStateChanged
        updateClasses();
    }//GEN-LAST:event_packageComboBoxItemStateChanged

    private void classComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_classComboBoxItemStateChanged
        parent.stateChanged(null);
    }//GEN-LAST:event_classComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnAsIs;
    private javax.swing.JRadioButton btnDefault;
    private javax.swing.JRadioButton btnEscalate;
    private javax.swing.JRadioButton btnJavadocAsIs;
    private javax.swing.JRadioButton btnJavadocUpdate;
    private javax.swing.JRadioButton btnPrivate;
    private javax.swing.JRadioButton btnProtected;
    private javax.swing.JRadioButton btnPublic;
    private javax.swing.JCheckBox chkDelegate;
    private javax.swing.JCheckBox chkDeprecate;
    private javax.swing.JComboBox classComboBox;
    private javax.swing.ButtonGroup javadocButtonGroup;
    private javax.swing.JPanel javadocPanel;
    private javax.swing.JLabel labelClass;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JLabel lblMoveMembersFrom;
    private javax.swing.JLabel lblSource;
    private javax.swing.JPanel membersListPanel;
    private org.openide.explorer.view.OutlineView outlineView1;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JPanel targetPanel;
    private javax.swing.ButtonGroup visibilityButtonGroup;
    private javax.swing.JPanel visibilityPanel;
    // End of variables declaration//GEN-END:variables

    public List<? extends TreePathHandle> getHandles() {
        List<TreePathHandle> result = new LinkedList<TreePathHandle>();
        ElementNode rootNode = getRootNode();
        if(rootNode != null && rootNode.getDescription() != null) {
            for (Description description : rootNode.getDescription().getSubs()) {
                if (description.getSelected() == Boolean.TRUE) {
                    result.add(TreePathHandle.from(description.getElementHandle(), description.getCpInfo()));
                }
            }
        }
        return result;
    }

    public boolean getDeprecated() {
        return chkDeprecate.isSelected();
    }

    public boolean getUpdateJavaDoc() {
        return btnJavadocUpdate.isSelected();
    }

    public boolean getDelegate() {
        return chkDelegate.isSelected();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public TreePathHandle getTarget() {
        Object selectedItem = classComboBox.getSelectedItem();
        if (selectedItem instanceof ClassItem) {
            ClassItem classItem = (ClassItem) selectedItem;
            return classItem.getHandle();
        } else {
            return null;
        }
    }

    private void refresh(final Description description) {
        final DescriptionFilter descriptionFilter = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                manager.setRootContext(new ElementNode(description, descriptionFilter, parent));
                outlineView1.getOutline().setRootVisible(false);
                lblSource.setText("<html>" + description.getHtmlHeader()); //NOI18N
                lblSource.setIcon(ElementIcons.getElementIcon(description.getKind(), description.getModifiers()));
            }
        });
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }

    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        if (root != null) {
            root.refreshRecursively();
        }
    }

    public Visibility getVisibility() {
        return Visibility.valueOf(visibilityButtonGroup.getSelection().getActionCommand());
    }

    class ElementScanningTask implements CancellableTask<CompilationController> {

        private final AtomicBoolean canceled = new AtomicBoolean();

        public ElementScanningTask() {
        }

        public void cancel() {
            //System.out.println("Element task canceled");
            canceled.set(true);
        }

        public void run(CompilationController info) throws Exception {
            canceled.set(false); // Task shared for one file needs reset first
            info.toPhase(JavaSource.Phase.RESOLVED);

            Description rootDescription = null;

            final Map<Element, Long> pos = new HashMap<Element, Long>();
            TreePath typeElementPath = JavaRefactoringUtils.findEnclosingClass(info, selectedElements[0].resolve(info), true, true, false, false, false);

            if (!canceled.get()) {
                Trees trees = info.getTrees();
                PositionVisitor posVis = new PositionVisitor(trees, canceled);
                posVis.scan(info.getCompilationUnit(), pos);
            }

            if (!canceled.get() && typeElementPath != null) {
                TypeElement topLevelElement = (TypeElement) info.getTrees().getElement(typeElementPath);
                rootDescription = element2description(topLevelElement, null, false, info, pos);
                if (null != rootDescription) {
                    addMembers(topLevelElement, rootDescription, info, pos);
                }
            }

            if (!canceled.get()) {
                refresh(null != rootDescription ? rootDescription : new Description());
            }
        }

        private void addMembers(final TypeElement e, final Description parentDescription, final CompilationInfo info, final Map<Element, Long> pos) {
            List<? extends Element> members = e.getEnclosedElements();
            for (Element m : members) {
                if (canceled.get()) {
                    return;
                }

                Description d = element2description(m, e, parentDescription.isInherited(), info, pos);
                if (null != d) {
                    parentDescription.getSubs().add(d);
                    if (m instanceof TypeElement && !d.isInherited()) {
                        addMembers((TypeElement) m, d, info, pos);
                    }
                }
            }
        }

        private Description element2description(final Element e, final Element parent,
                final boolean isParentInherited, final CompilationInfo info,
                final Map<Element, Long> pos) {
            if (info.getElementUtilities().isSynthetic(e)) {
                return null;
            }

            boolean inherited = isParentInherited || (null != parent && !parent.equals(e.getEnclosingElement()));
            Description d = new Description(e.getSimpleName().toString(), ElementHandle.create(e), e.getKind(), inherited);

            switch (e.getKind()) {
                case CLASS:
                case INTERFACE:
                case RECORD:
                case ENUM:
                case ANNOTATION_TYPE:
                    if(parent == null) {
                        d.setSubs(new HashSet<Description>());
                        d.setHtmlHeader(UIUtilities.createHeader((TypeElement) e, info.getElements().isDeprecated(e), d.isInherited(), true, false));
                    } else {
                        return null;
                    }
                    break;

                case ENUM_CONSTANT:
                case FIELD:
                    d.setHtmlHeader(UIUtilities.createHeader((VariableElement) e, info.getElements().isDeprecated(e), d.isInherited(), true, false));
                    break;
                  
                case METHOD:
                    d.setHtmlHeader(UIUtilities.createHeader((ExecutableElement) e, info.getElements().isDeprecated(e), d.isInherited(), true, false));
                    break;
                case CONSTRUCTOR:
                    return null;
                    
                default:
                    return null;
                    
            }
            d.setModifiers(e.getModifiers());
            d.setPos(getPosition(e, info, pos));
            d.setCpInfo(info.getClasspathInfo());
            d.setSelected(isSelected(e, info));
            return d;
        }

        private long getPosition(final Element e, final CompilationInfo info, final Map<Element, Long> pos) {
            Long res = pos.get(e);
            if (res == null) {
                return -1;
            }
            return res.longValue();
        }

        private Boolean isSelected(Element e, CompilationInfo info) {
            Boolean result = Boolean.FALSE;
            for (TreePathHandle tph : selectedElements) {
                if (e.equals(tph.resolveElement(info))) {
                    result = Boolean.TRUE;
                    break;
                }
            }
            return result;
        }
    }

    private static class PositionVisitor extends ErrorAwareTreePathScanner<Void, Map<Element, Long>> {

        private final Trees trees;
        private final SourcePositions sourcePositions;
        private final AtomicBoolean canceled;
        private CompilationUnitTree cu;

        public PositionVisitor(final Trees trees, final AtomicBoolean canceled) {
            assert trees != null;
            assert canceled != null;
            this.trees = trees;
            this.sourcePositions = trees.getSourcePositions();
            this.canceled = canceled;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Map<Element, Long> p) {
            this.cu = node;
            return super.visitCompilationUnit(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void scan(Tree tree, Map<Element, Long> p) {
            if (!canceled.get()) {
                return super.scan(tree, p);
            } else {
                return null;
            }
        }
    }
}
