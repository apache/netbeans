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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;
import javax.swing.undo.UndoableEdit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import org.netbeans.modules.form.actions.TestAction;
import org.netbeans.modules.form.menu.MenuEditLayer;
import org.netbeans.modules.form.palette.PaletteItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerUtils;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;

import org.netbeans.modules.form.assistant.*;
import org.netbeans.modules.form.wizard.ConnectionWizard;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.layoutdesign.LayoutConstants.PaddingType;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutBuilder;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;


/**
 * This is a TopComponent subclass holding the form designer. It consist of two
 * layers - HandleLayer (responsible for interaction with user) and
 * ComponentLayer (presenting the components, not accessible to the user).
 *
 * FormDesigner
 *  +- AssistantView
 *  +- JScrollPane
 *      +- JLayeredPane
 *          +- HandleLayer
 *          +- ComponentLayer
 *
 * @author Tran Duc Trung, Tomas Pavek, Josef Kozak
 */

public class FormDesigner {
    static final String PROP_DESIGNER_SIZE = "designerSize"; // NOI18N
    public static final String PROP_TOP_DESIGN_COMPONENT = "topDesignComponent"; // NOI18N

    // UI components composition
    private JComponent canvasRoot;
    private JLayeredPane layeredPane;
    private ComponentLayer componentLayer;
    private HandleLayer handleLayer;
    private NonVisualTray nonVisualTray;
    private FormToolBar formToolBar;
    
    // in-place editing
    private InPlaceEditLayer textEditLayer;
    private FormProperty editedProperty;
    private InPlaceEditLayer.FinishListener finnishListener;
    
    private MenuEditLayer menuEditLayer;
            
    // metadata
    private FormModel formModel;
    private FormModelListener formModelListener;
    private RADVisualComponent topDesignComponent;
    private boolean designerSizeExplictlySet;

    private static FormDesigner selectedDesigner;
    private FormEditor formEditor;

    // selection
    private List<RADComponent> selectedComponents = new ArrayList<RADComponent>();
    private List<RADVisualComponent> selectedLayoutComponents = new ArrayList<RADVisualComponent>();
    private ExplorerManager explorerManager;
    private boolean synchronizingSelection;

    // layout visualization and interaction
    private VisualReplicator replicator;
    private LayoutDesigner layoutDesigner;
    private List<Action> designerActions;
    private Action[] resizabilityActions;
    
    private int designerMode;
    public static final int MODE_SELECT = 0;
    public static final int MODE_CONNECT = 1;
    public static final int MODE_ADD = 2;
    
    private boolean initialized = false;
    private boolean active;

    private RADComponent connectionSource;
    private RADComponent connectionTarget;

    private InstanceContent lookupContent;
    private FormProxyLookup lookup;
    private boolean settingLookup;
    private UndoRedo.Provider undoRedoProvider;

    private PropertyChangeSupport propertyChangeSupport;

//    private AssistantView assistantView;
//    private PreferenceChangeListener settingsListener;

    // ----------
    // constructors and setup

    public FormDesigner(FormEditor formEditor) {
        FormLoaderSettings settings = FormLoaderSettings.getInstance();
        Color backgroundColor = settings.getFormDesignerBackgroundColor();
        Color borderColor = settings.getFormDesignerBorderColor();

        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        loadingPanel.setBackground(backgroundColor);
        JLabel loadingLbl = new JLabel(FormUtils.getBundleString("LBL_FormLoading")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setBorder(new LineBorder(borderColor, 4));
        loadingLbl.setPreferredSize(new Dimension(408, 308));
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingPanel.add(loadingLbl);
        canvasRoot = loadingPanel;

        this.formEditor = formEditor;

        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(new NodeSelectionListener());
            // Note: ComponentInspector does some updates on nodes selection as well.

        initLookup();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        assert formEditor.isFormLoaded();
        initialized = true;

        formModel = formEditor.getFormModel();

        componentLayer = new ComponentLayer(formModel);
        handleLayer = new HandleLayer(this);
        nonVisualTray = FormEditor.isNonVisualTrayEnabled() ?
                new NonVisualTray(explorerManager, formEditor.getOthersContainerNode()) : null;

        JPanel designPanel = new JPanel(new BorderLayout());
        designPanel.add(componentLayer, BorderLayout.CENTER);
        if (nonVisualTray != null) {
            designPanel.add(nonVisualTray, BorderLayout.SOUTH);
        }
        
        layeredPane = new JLayeredPane() {
            // hack: before each paint make sure the dragged components have
            // bounds set out of visible area (as they physically stay in their
            // container and the layout manager may lay them back if some
            // validation occurs)
            @Override
            protected void paintChildren(Graphics g) {
                handleLayer.maskDraggingComponents();
                super.paintChildren(g);
            }
        };
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.add(designPanel, Integer.valueOf(1000));
        layeredPane.add(handleLayer, Integer.valueOf(1001));
        canvasRoot = layeredPane;

//        updateAssistant();
//        settingsListener = new PreferenceChangeListener() {
//            @Override
//            public void preferenceChange(PreferenceChangeEvent evt) {
//                if (FormLoaderSettings.PROP_ASSISTANT_SHOWN.equals(evt.getKey())) {
//                    updateAssistant();
//                }
//            }
//
//        };
//        FormLoaderSettings.getPreferences().addPreferenceChangeListener(settingsListener);

        explorerManager.setRootContext(formEditor.getFormRootNode());

        undoRedoProvider = new UndoRedo.Provider() {
            @Override
            public UndoRedo getUndoRedo() {
                UndoRedo ur = formModel != null ? formModel.getUndoRedoManager() : null;
                return ur != null ? ur : UndoRedo.NONE;
            }
        };
        lookupContent.add(undoRedoProvider);

        initLookup();

        if (formModelListener == null)
            formModelListener = new FormListener();
        formModel.addFormModelListener(formModelListener);

        replicator = new VisualReplicator(true, FormUtils.getViewConverters(), 
            FormEditor.getBindingSupport(formModel));

        resetTopDesignComponent(false);
        handleLayer.setViewOnly(formModel.isReadOnly());

        // Beans without layout model don't need layout designer
        if (formModel.getLayoutModel() != null) {
            layoutDesigner = new LayoutDesigner(formModel.getLayoutModel(), new LayoutMapper());
            int paintLayout = FormLoaderSettings.getInstance().getPaintAdvancedLayoutInfo();
            layoutDesigner.setPaintAlignment((paintLayout&1) != 0);
            layoutDesigner.setPaintGaps((paintLayout&2) != 0);
        }

        updateWholeDesigner();

        // not very nice hack - it's better FormEditorSupport has its
        // listener registered after FormDesigner
        formEditor.reinstallListener();

        if (formEditor.getFormDesigner() == null) {
            // 70940: Make sure some form designer is registered
            formEditor.setFormDesigner(this);
        }
        
        //force the menu edit layer to be created
        getMenuEditLayer();

        // vlv: print
        designPanel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
    }

    public void close() {
        if (menuEditLayer != null) {
            menuEditLayer.hideMenuLayer();
            menuEditLayer = null;
        }
                
        setSelectedDesigner(this, false);
        if (initialized) {
            clearSelectionImpl();
            explorerManager.setRootContext(Node.EMPTY);
        }
        initialized = false;

        canvasRoot = null;
        componentLayer = null;
        handleLayer = null;
        if (nonVisualTray != null) {
            nonVisualTray.close();
            nonVisualTray = null;
        }
        layeredPane = null;        
        if (textEditLayer!=null) {            
            if (textEditLayer.isVisible()){
                textEditLayer.finishEditing(false);                
            }
            textEditLayer.removeFinishListener(getFinnishListener());
            textEditLayer=null;               
        }

        if (undoRedoProvider != null) {
            lookupContent.remove(undoRedoProvider);
            undoRedoProvider = null;
        }

        if (formModel != null) {
            if (formModelListener != null) {
                formModel.removeFormModelListener(formModelListener);                
            }                
//            if (settingsListener != null) {
//                FormLoaderSettings.getPreferences().removePreferenceChangeListener(settingsListener);
//            }
            topDesignComponent = null;
            designerSizeExplictlySet = false;
            formModel = null;
        }
        
        replicator = null;
        layoutDesigner = null;

        designerMode = MODE_SELECT;
        connectionSource = null;
        connectionTarget = null;        
        formEditor = null;
    }

    private void initLookup() {
        Lookup explorerLookup; // lookup for EpxlorerManager
        Lookup plainContentLookup; // lookup with various fixed instances
        Lookup paletteLookup; // lookup for palette
        Lookup saveCookieLookup; // to make sure Save action is enabled
        Lookup dataObjectLookup; // to make sure DO is in lookup even if no node selected

        if (lookup == null) {
            lookup = new FormProxyLookup();

            explorerLookup = null;

            lookupContent = new InstanceContent();
            lookupContent.add(new NavigatorLookupPanelsPolicy() {
                @Override
                public int getPanelsPolicy() {
                    return NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY;
                }
            });
            lookupContent.add(new NavigatorLookupHint() {
                @Override
                public String getContentType() {
                    return "text/x-form"; // NOI18N
                }
            });
            plainContentLookup = new AbstractLookup(lookupContent);

            paletteLookup = PaletteUtils.getPaletteLookup(formEditor.getFormDataObject().getPrimaryFile());
            
            saveCookieLookup = new Lookup() {
                @Override
                public <T> T lookup(final Class<T> clazz) {
                    if (clazz.isAssignableFrom(SaveCookie.class) && formEditor != null) {
                        return formEditor.getFormDataObject().getLookup().lookup(clazz);
                    } else {
                        return null;
                    }
                }
                @Override
                public <T> Result<T> lookup(Template<T> template) {
                    if (template.getType().isAssignableFrom(SaveCookie.class) && formEditor != null) {
                        return formEditor.getFormDataObject().getLookup().lookup(template);
                    } else {
                        return Lookup.EMPTY.lookup(template);
                    }
                }
            };

            dataObjectLookup = null;
        } else {
            Lookup[] lookups = lookup.getSubLookups();
            explorerLookup = lookups[0];
            plainContentLookup = lookups[1];
            paletteLookup = lookups[2];
            saveCookieLookup = lookups[3];
            dataObjectLookup = lookups[4];
        }

        if (!initialized) {
            explorerLookup = Lookup.EMPTY;
        } else if (explorerLookup == Lookup.EMPTY) {
            // TODO ActionMap from TC? There was some close action in it. But works even without that...
            ActionMap map = ComponentInspector.getInstance().setupActionMap(canvasRoot.getActionMap());
            explorerLookup = ExplorerUtils.createLookup(explorerManager, map);
        }

        if (dataObjectLookup == null || (dataObjectLookup == Lookup.EMPTY && !initialized)) {
            FormDataObject formDataObject = formEditor.getFormDataObject();
            dataObjectLookup = formDataObject.getNodeDelegate().getLookup();
        }

        lookup.setSubLookups(new Lookup[] {
            explorerLookup, plainContentLookup, paletteLookup, saveCookieLookup, dataObjectLookup
        });
    }

    public Lookup getLookup() {
        return lookup;
    }

    private void switchNodeInLookup(boolean includeDataNodeLookup) {
        if (settingLookup) {
            return;
        }
        Lookup[] lookups = lookup.getSubLookups();
        int index = lookups.length - 1;
        boolean dataNodeLookup = (lookups[index] != Lookup.EMPTY);
        if (includeDataNodeLookup != dataNodeLookup) {
            lookups[index] = includeDataNodeLookup
                    ? formEditor.getFormDataObject().getNodeDelegate().getLookup()
                    : Lookup.EMPTY;
            try {
                settingLookup = true; // avoid re-entrant call
                lookup.setSubLookups(lookups);
            } finally {
                settingLookup = false;
            }
        }
    }

//    private void updateAssistant() {
//        if (FormLoaderSettings.getInstance().getAssistantShown()) {
//            AssistantModel assistant = FormEditor.getAssistantModel(formModel);
//            assistantView = new AssistantView(assistant);
//            assistant.setContext("select"); // NOI18N
//            add(assistantView, BorderLayout.NORTH);
//        } else {
//            if (assistantView != null) {
//                remove(assistantView);
//                assistantView = null;
//            }
//        }
//        revalidate();
//    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
    }

    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
        }
    }

    // ------
    // important getters

    public FormModel getFormModel() {
        return formModel;
    }

    public HandleLayer getHandleLayer() {
        return handleLayer;
    }

    ComponentLayer getComponentLayer() {
        return componentLayer;
    }
    
    NonVisualTray getNonVisualTray() {
        return nonVisualTray;
    }

    public void setToolBar(JToolBar toolbar) {
        formToolBar = new FormToolBar(this, toolbar);
    }

    public JToolBar getToolBar() {
        return getFormToolBar().getToolBar();
    }

    FormToolBar getFormToolBar() {
        if (formToolBar == null) {
            formToolBar = new FormToolBar(this, null);
        }
        return formToolBar;
    }

    public LayoutDesigner getLayoutDesigner() {
        return layoutDesigner;
    }
    
    public FormEditor getFormEditor() {
        return formEditor;
    }
    
    // ------------
    // designer content

    public Object getComponent(RADComponent metacomp) {
        return replicator != null ? replicator.getClonedComponent(metacomp.getId()) : null;
    }

    public Object getComponent(String componentId) {
        return replicator != null ? replicator.getClonedComponent(componentId) : null;
    }

    public RADComponent getMetaComponent(Object comp) {
        String id = replicator.getClonedComponentId(comp);
        return id != null ? formModel.getMetaComponent(id) : null;
    }
    
//    public RADComponent getMetaComponent(String componentId) {
//        return formModel.getMetaComponent(componentId);
//    }

    public RADVisualComponent getTopDesignComponent() {
        return topDesignComponent;
    }

    public boolean isTopRADComponent() {
        RADComponent topMetaComp = formModel.getTopRADComponent();
        return topMetaComp != null && topMetaComp == topDesignComponent;
    }
    
    public void setTopDesignComponent(RADVisualComponent component,
                                      boolean update) {
        highlightTopDesignComponentName(false);
        // TODO need to remove bindings of the current cloned view (or clone bound components as well)
        Object old = topDesignComponent;
        topDesignComponent = component;
        designerSizeExplictlySet = false;
        highlightTopDesignComponentName(!isTopRADComponent());        
        if (update) {
            selectedLayoutComponents.clear(); // so e.g. JScrollPane does not keep selected while its contained JPanel became root
            setSelectedComponent(topDesignComponent);
            updateWholeDesigner();
        }
        firePropertyChange(PROP_TOP_DESIGN_COMPONENT, old, component);
        updateTestAction();
        // topDesignComponent could have been out of the design view so far,
        // so not selected as a layout component
        if (!selectedLayoutComponents.contains(topDesignComponent)) {
            selectedLayoutComponents.add(topDesignComponent);
            if (layoutDesigner != null && topDesignComponent instanceof RADVisualContainer
                    && ((RADVisualContainer)topDesignComponent).getLayoutSupport() == null) {
                layoutDesigner.setSelectedComponents(new String[] { topDesignComponent.getId() });
            }
        }
    }

    // Issue 200631. It would be much better if TestAction was observing
    // FormDesigner, but there is no way (currently) to observe selectedDesigner
    // and I not sure it is worth introducing just for this use case.
    private static void updateTestAction() {
        TestAction testAction = SystemAction.get(TestAction.class);
        testAction.updateEnabled();
    }

    private void highlightTopDesignComponentName(boolean bl) {
        if(topDesignComponent!=null) {
            RADComponentNode node = topDesignComponent.getNodeReference();
            if(node!=null) {
                node.highlightDisplayName(bl);
            }
        }        
    }
    
    public void resetTopDesignComponent(boolean update) {
        RADComponent top = formModel.getTopRADComponent();
        setTopDesignComponent(top instanceof RADVisualComponent ? (RADVisualComponent)top : null,
                              update);
    }

    /** Tests whether top designed container is some parent of given component
     * (whether the component is in the tree under top designed container).
     * 
     * @param metacomp component.
     * @return <code>true</code> if the component is in designer,
     * returns <code>false</code> otherwise.
     */
    public boolean isInDesigner(RADVisualComponent metacomp) {
        if (replicator != null) { // not on un-initialized designer
            Object comp = replicator.getClonedComponent(metacomp);
            if  (comp instanceof Component) {
                return componentLayer.isAncestorOf((Component)comp);
            }
        }
        return false;
    }

    void updateWholeDesigner() {
        if (formModelListener != null)
            formModelListener.formChanged(null);
    }

    private void updateComponentLayer(final boolean fireChange) {
        if (formModel == null) { // the form can be closed just after opened, before this gets called (#70439)
            return;
        }
        if (getLayoutDesigner() == null) {
            return;
        }

        // Ensure that the components are laid out
        componentLayer.revalidate(); // Add componentLayer among components to validate
        RepaintManager.currentManager(componentLayer).validateInvalidComponents();

        LayoutModel layoutModel = formModel.getLayoutModel();
        // If after a change (FormModel has a compound edit started, i.e. it's not
        // just after form loaded) that was not primarily in layout, start undo
        // edit in LayoutModel as well: some changes can be done when updating to
        // actual visual state that was affected by changes elsewhere.
        UndoableEdit layoutUndoEdit = formModel.isCompoundEditInProgress()
                                      && !layoutModel.isUndoableEditInProgress()
                ? layoutModel.getUndoableEdit() : null;

        if (getLayoutDesigner().updateCurrentState() && fireChange) {
            formModel.fireFormChanged(true); // hack: to regenerate code once again
        }

        if (layoutModel.endUndoableEdit() && layoutUndoEdit != null) {
            formModel.addUndoableEdit(layoutUndoEdit);
        }

        updateResizabilityActions();
        componentLayer.repaint();
    }

    // updates layout of a container in designer to match current model - used
    // by HandleLayer when canceling component dragging
    void updateContainerLayout(RADVisualContainer metacont) {
        replicator.updateContainerLayout(metacont);
        componentLayer.revalidate();
        componentLayer.repaint();
    }

    public static Container createFormView(final RADComponent metacomp, final FormLAF.PreviewInfo previewInfo)
        throws Exception
    {
        Container result = null;
        FormModel formModel = metacomp.getFormModel();
        FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
        final ClassLoader classLoader = ClassPathUtils.getProjectClassLoader(formFile);
        Locale defaultLocale = switchToDesignLocale(formModel);
        try {
            FormLAF.setUsePreviewDefaults(classLoader, previewInfo);
            result = (Container) FormLAF.executeWithLookAndFeel(formModel,
            new Mutex.ExceptionAction () {
                @Override
                public Object run() throws Exception {
                    FormModel formModel = metacomp.getFormModel();
                    VisualReplicator r = new VisualReplicator(false, FormUtils.getViewConverters(), FormEditor.getBindingSupport(formModel));
                    r.setTopMetaComponent(metacomp);
                    Object container = r.createClone();
                    if (container instanceof RootPaneContainer) {
                        JRootPane rootPane = ((RootPaneContainer)container).getRootPane();
                        JLayeredPane newPane = new JLayeredPane() {
                            @Override
                            public void paint(Graphics g) {
                                try {
                                    FormLAF.setUsePreviewDefaults(classLoader, previewInfo);
                                    super.paint(g);
                                } finally {
                                    FormLAF.setUsePreviewDefaults(null, null);
                                }
                            }
                        };
                        // Copy components from the original layered pane into our one
                        JLayeredPane oldPane = rootPane.getLayeredPane();
                        Component[] comps = oldPane.getComponents();
                        for (int i=0; i<comps.length; i++) {
                            newPane.add(comps[i], Integer.valueOf(oldPane.getLayer(comps[i])));
                        }
                        // Use our layered pane that knows about LAF switching
                        rootPane.setLayeredPane(newPane);
                        // Make the glass pane visible to force repaint of the whole layered pane
                        rootPane.getGlassPane().setVisible(true);
                        // Mark it as design preview
                        rootPane.putClientProperty("designPreview", Boolean.TRUE); // NOI18N
                    } // else AWT Frame - we don't care that the L&F of the Swing
                    // components may not look good - it is a strange use case
                    return container;
                }
            }
        
        );
        } finally {
            FormLAF.setUsePreviewDefaults(null, null);
            if (defaultLocale != null)
                Locale.setDefault(defaultLocale);
        }
        return result;
    }

    private static Locale switchToDesignLocale(FormModel formModel) {
        Locale defaultLocale = null;
        String locale = FormEditor.getResourceSupport(formModel).getDesignLocale();
        if (locale != null && !locale.equals("")) { // NOI18N
            defaultLocale = Locale.getDefault();

            String[] parts = locale.split("_"); // NOI18N
            int i = 0;
            if ("".equals(parts[i])) // NOI18N
                i++;
            String language = i < parts.length ? parts[i++] : null;
            String country = i < parts.length ? parts[i++] : ""; // NOI18N
            String variant = i < parts.length ? parts[i] : ""; // NOI18N
            if (language != null)
                Locale.setDefault(new Locale(language, country, variant));
        }
        return defaultLocale;
    }

    Component getTopDesignComponentView() {
        return (Component) replicator.getClonedComponent(topDesignComponent);
    }

    // NOTE: does not create a new Point instance
    Point pointFromComponentToHandleLayer(Point p, Component sourceComp) {
        Component commonParent = layeredPane;
        Component comp = sourceComp;
        while (comp != commonParent) {
            p.x += comp.getX();
            p.y += comp.getY();
            comp = comp.getParent();
        }
        comp = handleLayer;
        while (comp != commonParent) {
            p.x -= comp.getX();
            p.y -= comp.getY();
            comp = comp.getParent();
        }
        return p;
    }

    // NOTE: does not create a new Point instance
    Point pointFromHandleToComponentLayer(Point p, Component targetComp) {
        Component commonParent = layeredPane;
        Component comp = handleLayer;
        while (comp != commonParent) {
            p.x += comp.getX();
            p.y += comp.getY();
            comp = comp.getParent();
        }
        comp = targetComp;
        while (comp != commonParent) {
            p.x -= comp.getX();
            p.y -= comp.getY();
            comp = comp.getParent();
        }
        return p;
    }
    
    boolean isCoordinatesRoot(Component comp) {
        return (layeredPane == comp);
    }

    private Rectangle componentBoundsToTop(Component component) {
        if (component == null)
            return null;

        Component top = getTopDesignComponentView();

        int dx = 0;
        int dy = 0;

        if (component != top) {
            Component comp = component.getParent();
            while (comp != top) {
                if (comp == null) {
                    break;//return null;
                }
                dx += comp.getX();
                dy += comp.getY();
                comp = comp.getParent();
            }
        }
        else {
            dx = -top.getX();
            dy = -top.getY();
        }

        Rectangle bounds = component.getBounds();
        bounds.x += dx;
        bounds.y += dy;

        return bounds;
    }

    // -------
    // designer mode

    void setDesignerMode(int mode) {
        getFormToolBar().updateDesignerMode(mode);

        if (mode == designerMode || !initialized) {
            return;
        }

        if (mode == MODE_ADD) {
            PaletteItem pitem = PaletteUtils.getSelectedItem();
            if (pitem != null && getSelectedDesigner() == this) {
                boolean prepared = pitem.prepareComponentInitializer(
                                     formEditor.getFormDataObject().getPrimaryFile());
                if (!prepared) {
                    toggleSelectionMode();
                    return;
                }
            }
        }

        designerMode = mode;

        resetConnection();
        if (mode == MODE_CONNECT)
            clearSelection();

        handleLayer.endDragging(null);
        AssistantModel aModel = FormEditor.getAssistantModel(formModel);
        switch (mode) {
            case MODE_CONNECT: aModel.setContext("connectSource"); break; // NOI18N
            case MODE_SELECT: aModel.setContext("select"); break; // NOI18N
        }
    }

    public int getDesignerMode() {
        return designerMode;
    }

    public void toggleSelectionMode() {
        setDesignerMode(MODE_SELECT);
        PaletteUtils.clearPaletteSelection();
    }

    void toggleConnectionMode() {
        setDesignerMode(MODE_CONNECT);
        PaletteUtils.clearPaletteSelection();
    }

    void toggleAddMode() {
        setDesignerMode(MODE_ADD);
        PaletteUtils.clearPaletteSelection();
    }

    // -------
    // designer size

    Dimension getDesignerSize() {
        return componentLayer.getDesignerSize();
    }

    void setDesignerSize(Dimension size, Dimension oldSize) {
        if (topDesignComponent instanceof RADVisualFormContainer) {
            ((RADVisualFormContainer)topDesignComponent).setDesignerSize(size);
        }
        else if (topDesignComponent != null) {
            if (topDesignComponent == formModel.getTopRADComponent()) {
                oldSize = (Dimension) topDesignComponent.getAuxValue(PROP_DESIGNER_SIZE);
                topDesignComponent.setAuxValue(PROP_DESIGNER_SIZE, size);
            }
            if (oldSize == null)
                oldSize = getDesignerSize();

            getFormModel().fireSyntheticPropertyChanged(topDesignComponent,
                    FormDesigner.PROP_DESIGNER_SIZE, oldSize, size);
        }
    }

    void storeDesignerSize(Dimension size) { // without firing model change
        if (topDesignComponent instanceof RADVisualFormContainer)
            ((RADVisualFormContainer)topDesignComponent).setDesignerSizeImpl(size);
        else if (topDesignComponent == formModel.getTopRADComponent()) // root not a visual container
            topDesignComponent.setAuxValue(PROP_DESIGNER_SIZE, size);
    }

    private void setupDesignerSize() {
        if (formModel == null) { // the form can be closed just after opened, before this gets called (#70439, #240027)
            return;
        }
        Dimension size = null;
        RADVisualFormContainer formCont = topDesignComponent instanceof RADVisualFormContainer ?
                                          (RADVisualFormContainer) topDesignComponent : null;
        if (formCont == null
            || formCont.hasExplicitSize()
            || !RADVisualContainer.isFreeDesignContainer(topDesignComponent))
        {   // try to obtain stored designer size
            if (formCont != null) {
                size = formCont.getDesignerSize();
            }
            if (size == null) {
                size = (Dimension) topDesignComponent.getAuxValue(PROP_DESIGNER_SIZE);
            }
            if (size != null) {
                designerSizeExplictlySet = true;
            }
            if (size == null
                && (!formModel.isFreeDesignDefaultLayout()
                     || topDesignComponent == formModel.getTopRADComponent()))
            {   // use default size if no stored size is available and
                // old layout form or top design comp is root in the form (but not a container)
                size = new Dimension(400, 300);
            }
        }

        Dimension setSize = componentLayer.setDesignerSize(size); // null computes preferred size
        storeDesignerSize(setSize);
    }

    private void checkDesignerSize() {
        if ((formModel.isFreeDesignDefaultLayout()
                || RADVisualContainer.isFreeDesignContainer(topDesignComponent))
            && topDesignComponent instanceof RADVisualComponent
            && (!(topDesignComponent instanceof RADVisualFormContainer)
                || !((RADVisualFormContainer)topDesignComponent).hasExplicitSize()))
        {   // new layout container defining designer size
            // designer size not defined explicitly - check minimum size
            Component topComp = getTopDesignComponentView();
            Component topCont = null;
            if (topDesignComponent instanceof RADVisualContainer) {
                topCont = ((RADVisualContainer)topDesignComponent).getContainerDelegate(topComp);
            }
            if (topCont == null) {
                topCont = topComp;
            }
            if (shouldAdjustDesignerSize(topCont)) {
                // can't rely on minimum size of the container wrap - e.g. menu bar
                // returns wrong min height
                int wDiff = topComp.getWidth() - topCont.getWidth();
                int hDiff = topComp.getHeight() - topCont.getHeight();

                Dimension designerSize = new Dimension(getDesignerSize());
                designerSize.width -= wDiff;
                designerSize.height -= hDiff;
                boolean corrected = false;
                if (layoutDesigner != null && layoutDesigner.isPreferredSizeChanged()
                        && shouldHonorDesignerPrefSize(topCont)) {
                    Dimension prefSize = topCont.getPreferredSize();
                    if (designerSize.width != prefSize.width) {
                        designerSize.width = prefSize.width;
                        corrected = true;
                    }
                    if (designerSize.height != prefSize.height) {
                        designerSize.height = prefSize.height;
                        corrected = true;
                    }
                } else {
                    Dimension minSize = topCont.getMinimumSize();
                    if (designerSize.width < minSize.width) {
                        designerSize.width = minSize.width;
                        corrected = true;
                    }
                    if (designerSize.height < minSize.height) {
                        designerSize.height = minSize.height;
                        corrected = true;
                    }
                }

                if (corrected) {
                    if (shouldHonorDesignerMinSize(topCont, designerSizeExplictlySet)) {
                        designerSize.width += wDiff;
                        designerSize.height += hDiff;

                        // hack: we need the size correction in the undo/redo
                        if (formModel.isCompoundEditInProgress()) {
                            FormModelEvent ev = new FormModelEvent(formModel, FormModelEvent.SYNTHETIC_PROPERTY_CHANGED);
                            ev.setComponentAndContainer(topDesignComponent, null);
                            ev.setProperty(PROP_DESIGNER_SIZE, getDesignerSize(), designerSize);
                            formModel.addUndoableEdit(ev.getUndoableEdit());
                        }

                        componentLayer.setDesignerSize(designerSize);
                        storeDesignerSize(designerSize);
                    }
                } else {
                    designerSizeExplictlySet = false;
                }
            }
        }
    }

    private static boolean shouldAdjustDesignerSize(Component topComp) {
        // Null and AbsolutLayout can't provide a reasonable preferred or
        // minimum size, don't try to adjust the designer size according to them.
        // (E.g. when reacting to a change in a subpanel with Free Design which
        // is included in a top container with null layout.)
        if (topComp instanceof Container) {
            LayoutManager lm = ((Container)topComp).getLayout();
            if (lm == null || lm instanceof AbsoluteLayout) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldHonorDesignerPrefSize(Component topComp) {
        // Hack for FlowLayout - don't let the designer follow the preferred size
        // according to the layout. The designer has often different actual size
        // and a change in a subpanel with Free Design should not cause the whole
        // designer to resize.
        return !(topComp instanceof Container)
               || !(((Container)topComp).getLayout() instanceof FlowLayout);
    }

    private static boolean shouldHonorDesignerMinSize(Component topComp, boolean sizeSetExplicitly) {
        // Hack for FlowLayout - it provides minimum size for one row of
        // components. But we should allow to manually shrink the designer below
        // that size, making the components wrap on more lines.
        return !sizeSetExplicitly
               || !(topComp instanceof Container)
               || !(((Container)topComp).getLayout() instanceof FlowLayout);
        // We only care about the top component. For subcomponents it's
        // difficult to determine which one would go below min size to check if
        // it has FlowLayout.
    }

    // ---------
    // components selection

    public java.util.List<RADComponent> getSelectedComponents() {
        return selectedComponents;
    }

    java.util.List<RADVisualComponent> getSelectedLayoutComponents() {
        return selectedLayoutComponents;
    }

    boolean isComponentSelected(RADComponent metacomp) {
        return selectedComponents.contains(metacomp);
    }

    public void setSelectedComponent(RADComponent metacomp) {
        if (selectedComponents.size() == 1 && selectedComponents.contains(metacomp)) {
            return;
        }
        clearSelectionImpl();
        addComponentToSelectionImpl(metacomp);
        repaintSelection();
        syncNodesFromComponents();
    }

    public void setSelectedComponents(RADComponent[] metacomps) {
        clearSelectionImpl();

        for (int i=0; i < metacomps.length; i++) {
            addComponentToSelectionImpl(metacomps[i]);
        }

        repaintSelection();
        syncNodesFromComponents();
    }

    public void addComponentToSelection(RADComponent metacomp) {
        addComponentToSelectionImpl(metacomp);
        repaintSelection();
        syncNodesFromComponents();
    }

    void addComponentsToSelection(RADComponent[] metacomps) {
        for (int i=0; i < metacomps.length; i++)
            addComponentToSelectionImpl(metacomps[i]);

        repaintSelection();
        syncNodesFromComponents();
    }

    public void removeComponentFromSelection(RADComponent metacomp) {
        removeComponentFromSelectionImpl(metacomp);
        repaintSelection();
        syncNodesFromComponents();
    }

    public void clearSelection() {
        clearSelectionImpl();
        repaintSelection();
        syncNodesFromComponents();
    }

    private void syncNodesFromComponents() {
        if (synchronizingSelection) {
            return;
        }
        List<Node> nodes = new ArrayList<Node>(selectedComponents.size());
        for (RADComponent c : selectedComponents) {
            if (c.getNodeReference() != null) { // issue 126192 workaround
                nodes.add(c.getNodeReference());
            }
        }
        try {
            synchronizingSelection = true;
            setSelectedNodes(nodes.toArray(new Node[0]));
        } finally {
            synchronizingSelection = false;
        }
    }

    void addComponentToSelectionImpl(RADComponent metacomp) {
        if (metacomp != null) {
            selectedComponents.add(metacomp);
            RADVisualComponent layoutComponent = componentToLayoutComponent(metacomp);
            if (layoutComponent != null) {
                selectedLayoutComponents.add(layoutComponent);
                ensureComponentIsShown((RADVisualComponent)metacomp);
                selectionChanged();
            }
        }
    }

    void removeComponentFromSelectionImpl(RADComponent metacomp) {
        selectedComponents.remove(metacomp);
        RADVisualComponent layoutComponent = componentToLayoutComponent(metacomp);
        if (layoutComponent != null) {
            selectedLayoutComponents.remove(layoutComponent);
        }
        selectionChanged();
    }

    void clearSelectionImpl() {
        selectedComponents.clear();
        selectedLayoutComponents.clear();
        selectionChanged();
    }

    ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public Node[] getSelectedNodes() {
        return explorerManager.getSelectedNodes();
    }

    void setSelectedNodes(Node... nodes) {
        try {
//            if (formEditor == null) {
//                // Lazy synchronization of already closed form - issue 129877
//                return;
//            }
//            FormDataObject fdo = formEditor.getFormDataObject();
//            if (!fdo.isValid()) {
//                return; // Issue 130637
//            }
            explorerManager.setSelectedNodes(nodes);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    private class NodeSelectionListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                syncComponentsFromNodes();
                Node[] selectedNodes = getSelectedNodes();
                // if no form node, select data node (of FormDataObject) in lookup
                switchNodeInLookup(selectedNodes.length == 0 && formEditor.getFormDataObject().isValid());
                firePropertyChange(ExplorerManager.PROP_SELECTED_NODES, evt.getOldValue(), evt.getNewValue());
                // specially handle node selection in connection mode
                if (getDesignerMode() == MODE_CONNECT && selectedNodes.length > 0) {
                    RADComponentCookie cookie = selectedNodes[0].getCookie(RADComponentCookie.class);
                    if (cookie != null
                            && cookie.getRADComponent() == getConnectionSource()
                            && selectedNodes.length > 1) {
                        cookie = selectedNodes[selectedNodes.length-1].getCookie(RADComponentCookie.class);
                    }
                    if (cookie != null) {
                        connectBean(cookie.getRADComponent(), true);
                    }
                }
            }
        }
    }

    private void syncComponentsFromNodes() {
        if (synchronizingSelection) {
            return;
        }
        Node[] nodes = getSelectedNodes();
        List<RADComponent> components = new ArrayList<>(nodes.length);
        for (Node n : nodes) {
            FormCookie formCookie = n.getCookie(FormCookie.class);
            if (formCookie != null) {
                Node node = formCookie.getOriginalNode();
                if (node instanceof RADComponentNode) {
                    components.add(((RADComponentNode)node).getRADComponent());
                }
            }
        }
        try {
            synchronizingSelection = true;
            setSelectedComponents(components.toArray(new RADComponent[0]));
        } finally {
            synchronizingSelection = false;
        }
    }

    private void selectionChanged() {
        if (formModel == null) {
            // Some (redundant) postponed update => ignore
            // See, for example, issue 153953 - the formDesigner is reset
            // during refactoring. The selection is cleared, but the corresponding
            // event arrives after formModel is cleared and before it is
            // initialized again
            return;
        }
        updateLayoutDesigner();
        updateResizabilityActions();
        updateAssistantContext();
    }

    private void repaintSelection() {
        if (handleLayer != null) { // Issue 174373
            handleLayer.repaint();
        }
    }

    private void updateLayoutDesigner() {
        boolean enabled;
        if (layoutDesigner != null) {
            Collection<String> selectedIds = selectedLayoutComponentIds();
            enabled = layoutDesigner.canAlign(selectedIds);
            selectedIds = getSelectedComponentsForLayoutDesigner(selectedIds);
            layoutDesigner.setSelectedComponents(selectedIds.toArray(new String[0]));
        } else {
            enabled = false;
        }
        for (Action action : getDesignerActions(true)) {
            action.setEnabled(enabled);
        }
    }

    private Collection<String> getSelectedComponentsForLayoutDesigner(Collection<String> selectedIds) {
        for (RADComponent metacomp : getSelectedLayoutComponents()) {
            RADVisualComponent subcomp = substituteWithSubComponent((RADVisualComponent)metacomp);
            if (subcomp != metacomp && subcomp instanceof RADVisualContainer
                    && ((RADVisualContainer)subcomp).getLayoutSupport() == null) {
                // Reversed logic as in componentToLayoutComponent - while the scrollpane
                // should be the selected component which is manipulated in outer layout,
                // the contained component should also be selected as a container.
                selectedIds.add(subcomp.getId());
            }
        }
        return selectedIds;
    }

    public void updateResizabilityActions() {
        Collection componentIds = componentIds();
        Action[] actions = getResizabilityActions();

        RADComponent top = getTopDesignComponent();
        if (top == null || componentIds.contains(top.getId())) {
            for (Action a : actions) {
                a.setEnabled(false);
            }
            return;
        }

        LayoutModel layoutModel = getFormModel().getLayoutModel();
        LayoutDesigner layoutDesigner = getLayoutDesigner();
        Iterator iter = componentIds.iterator();
        boolean resizable[] = new boolean[2];
        boolean nonResizable[] = new boolean[2];
        while (iter.hasNext()) {
            String id = (String)iter.next();
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            for (int i=0; i<2; i++) {
                if (layoutDesigner.isComponentResizing(comp,
                        (i == 0) ? LayoutConstants.HORIZONTAL : LayoutConstants.VERTICAL)) {
                    resizable[i] = true;
                } else {
                    nonResizable[i] = true;
                }
            }
        }
        for (int i=0; i<2; i++) {
            boolean match;
            boolean miss;
            match = resizable[i];
            miss = nonResizable[i];
            actions[i].putValue(Action.SELECTED_KEY, !miss && match);
            actions[i].setEnabled(match || miss);
        }
    }

    private void updateAssistantContext() {
        String context = null;
        String additionalCtx = null;
        List<RADComponent> selComps = getSelectedComponents();
        int selCount = selComps.size();
        if (selCount > 0) {
            RADComponent metacomp = selComps.get(0);
            if (layoutDesigner != null && layoutDesigner.isUnplacedComponent(metacomp.getId())) {
                if (selCount > 1) {
                    List<String> ids = new ArrayList<String>(selCount);
                    for (RADComponent c : selComps) {
                        ids.add(c.getId());
                    }
                    if (layoutDesigner.getDraggableComponents(ids).size() == selCount) {
                        // all selected components are "unplaced" in the same container
                        context = "unplacedComponents1"; // NOI18N
                        additionalCtx = "unplacedComponents2"; // NOI18N
                    }
                } else {
                    context = "unplacedComponent1"; // NOI18N
                    additionalCtx = "unplacedComponent2"; // NOI18N
                }
            }
            if (selCount == 1 && context == null) {
                Object bean = metacomp.getBeanInstance();
                if (bean instanceof JTabbedPane) {
                    JTabbedPane pane = (JTabbedPane)bean;
                    int count = pane.getTabCount();
                    switch (count) {
                        case 0: context = "tabbedPaneEmpty"; break; // NOI18N
                        case 1: context = "tabbedPaneOne"; break; // NOI18N
                        default: context = "tabbedPane"; break; // NOI18N
                    }
                } else if (bean instanceof JRadioButton) {
                    Node.Property property = metacomp.getPropertyByName("buttonGroup"); // NOI18N
                    try {
                        if ((property != null) && (property.getValue() == null)) {
                            context = "buttonGroup"; // NOI18N
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }        
                } else if ((bean instanceof JPanel) && (getTopDesignComponent() != metacomp) && (Math.random() < 0.2)) {
                    context = "designThisContainer"; // NOI18N
                } else if ((bean instanceof JComboBox) && (Math.random() < 0.4)) {
                    context = "comboBoxModel"; // NOI18N
                } else if ((bean instanceof JList) && (Math.random() < 0.4)) {
                    context = "listModel"; // NOI18N
                } else if ((bean instanceof JTable) && (Math.random() < 0.4)) {
                    context = "tableModel"; // NOI18N
                } else if (bean instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)bean;
                    if ((scrollPane.getViewport() != null)
                            && (scrollPane.getViewport().getView() == null)) {
                        context = "scrollPaneEmpty"; // NOI18N
                    } else if (Math.random() < 0.5) {
                        context = "scrollPane"; // NOI18N
                    }
                }
            }
        }
        if (context == null) {
            context = "select"; // NOI18N
        }
        FormEditor.getAssistantModel(formModel).setContext(context, additionalCtx);
    }

    RADVisualComponent componentToLayoutComponent(RADComponent metacomp) {
        if (metacomp instanceof RADVisualComponent) {
            RADVisualComponent visualComp = (RADVisualComponent) metacomp;
            if (!visualComp.isMenuComponent()) {
                RADVisualComponent subst = substituteWithContainer(visualComp);
                if (subst != null) {
                    return subst;
                }
                // otherwise just check if it is visible in the designer
                return isInDesigner(visualComp) ? visualComp : null;
            }
        }
        return null;
    }

    /**
     * For some outwards-related operations the selected components may need to
     * be substituted with the enclosing container, typically JScrollPane. This
     * method returns the parent container for given component if such substition
     * is possible, or just the component itself if not.
     */
    RADVisualComponent substituteWithContainer(RADVisualComponent metacomp) {
        if (metacomp != null) {
            RADVisualContainer metacont = metacomp.getParentContainer();
            if (isTransparentContainer(metacont)
                    && metacont.getParentContainer() != null
                    && isInDesigner(metacont)) {
                return metacont;
            }
        }
        return metacomp;
    }

    /**
     * For some inwards-related operations the selected parent container of certain
     * type (like JScrollPane) may need to be substituted with the enclosed sub
     * component. This method returns the subcomponent for given component if such
     * substition is possible, or just the component itself if not.
     */
    static RADVisualComponent substituteWithSubComponent(RADVisualComponent metacont) {
        if (isTransparentContainer(metacont)) {
            return ((RADVisualContainer)metacont).getSubComponents()[0];
        }
        return metacont;
    }

    static boolean isTransparentContainer(RADVisualComponent metacont) {
        return metacont instanceof RADVisualContainer
                && metacont.getBeanClass().isAssignableFrom(JScrollPane.class)
                && ((RADVisualContainer)metacont).hasVisualSubComponents();
    }

    /** Finds out what component follows after currently selected component
     * when TAB (forward true) or Shift+TAB (forward false) is pressed. 
     * @return the next or previous component for selection
     */
    RADComponent getNextVisualComponent(boolean forward) {
        RADComponent currentComp = null;
        int n = selectedComponents.size();
        if (n > 0) {
            if (n > 1)
                return null;
            RADComponent sel = selectedComponents.get(0);
            if (sel instanceof RADVisualComponent) {
                currentComp = sel;
            } else {
                return null;
            }
        }

        return getNextVisualComponent(currentComp, forward);
    }

    /** @return the next or prevoius component to component comp
     */
    RADComponent getNextVisualComponent(RADComponent comp, boolean forward) {
        if (comp == null)
            return topDesignComponent;
        if (getComponent(comp) == null)
            return null;

        RADVisualContainer cont;
        RADComponent[] subComps;

        if (forward) {
            // try the first sub-component
            subComps = getVisualSubComponents(comp);
            if (subComps.length > 0) {
                return subComps[0];
            }

            // try the next component (or the next of the parent then)
            if (comp == topDesignComponent)
                return topDesignComponent;
            cont = (RADVisualContainer)comp.getParentComponent();
            if (cont == null) {
                return null;
            }
            int i = cont.getIndexOf(comp);
            while (i >= 0) {
                subComps = cont.getSubComponents();
                if (i+1 < subComps.length)
                    return subComps[i+1];

                if (cont == topDesignComponent)
                    break;
                comp = cont; // one level up
                cont = (RADVisualContainer)comp.getParentComponent();
                if (cont == null)
                    return null; // should not happen
                i = cont.getIndexOf(comp);
            }

            return topDesignComponent;
        }
        else { // backward
            // take the previuos component
            if (comp != topDesignComponent) {
                cont = (RADVisualContainer)comp.getParentComponent();
                if (cont == null) {
                    return null;
                }
                int i = cont.getIndexOf(comp);
                if (i >= 0) { // should be always true
                    if (i == 0) return cont; // the opposite to the 1st forward step

                    subComps = cont.getSubComponents();
                    comp = subComps[i-1];
                }
                else comp = topDesignComponent;
            }

            // find the last subcomponent of it
            do {
                subComps = getVisualSubComponents(comp);
                if (subComps.length > 0) {
                    comp = subComps[subComps.length-1];
                    continue;
                } else {
                    break;
                }
            }
            while (true);
            return comp;
        }
    }

    private RADComponent[] getVisualSubComponents(RADComponent metacomp) {
        return metacomp instanceof RADVisualContainer ?
            ((RADVisualContainer)metacomp).getSubComponents() : new RADComponent[0];
        // TBD components set as properties
    }

    /**
     * Aligns selected components in the specified direction.
     *
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    void align(boolean closed, int dimension, int alignment) {
        // Check that the action is enabled
        Action action = null;
        Iterator iter = getDesignerActions(true).iterator();
        while (iter.hasNext()) {
            Action candidate = (Action)iter.next();
            if (candidate instanceof AlignAction) {
                AlignAction alignCandidate = (AlignAction)candidate;
                if ((alignCandidate.getAlignment() == alignment) && (alignCandidate.getDimension() == dimension)) {
                    action = alignCandidate;
                    break;
                }
            }
        }
        if ((action == null) || (!action.isEnabled())) {
            return;
        }
        Collection selectedIds = selectedLayoutComponentIds();
        RADComponent parent = commonParent(selectedIds);
        LayoutModel layoutModel = formModel.getLayoutModel();
        Object layoutUndoMark = layoutModel.getChangeMark();
        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
        boolean autoUndo = true;
        try {
            getLayoutDesigner().align(selectedIds, closed, dimension, alignment);
            autoUndo = false;
        } finally {
            formModel.fireContainerLayoutChanged((RADVisualContainer)parent, null, null, null);
            if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(ue);
            }
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            }
        }
    }
    
    /**
     * Returns designer actions (they will be displayed in toolbar).
     *
     * @param forToolbar determines whether the method should return
     * all designer actions or just the subset for the form toolbar.
     * @return <code>Collection</code> of <code>Action</code> objects.
     */
    public Collection<Action> getDesignerActions(boolean forToolbar) {
        if (designerActions == null) {
            designerActions = new LinkedList<Action>();
            // Grouping actions
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, true));
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, true));
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.CENTER, true));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.LEADING, true));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.TRAILING, true));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.CENTER, true));
            // Align actions
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, false));
            designerActions.add(new AlignAction(LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, false));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.LEADING, false));
            designerActions.add(new AlignAction(LayoutConstants.VERTICAL, LayoutConstants.TRAILING, false));
        }
        return forToolbar ? designerActions.subList(0, 6) : designerActions;
    }

    public Action[] getResizabilityActions() {
        if (resizabilityActions == null) {
            resizabilityActions = new Action[] { new ResizabilityAction(LayoutConstants.HORIZONTAL),
                                                 new ResizabilityAction(LayoutConstants.VERTICAL) };
        }
        return resizabilityActions;
    }
    
    /**
     * Returns collection of ids of the selected layout components.
     *
     * @return <code>Collection</code> of <code>String</code> objects.
     */
    Collection<String> selectedLayoutComponentIds() {
        Iterator metacomps = getSelectedLayoutComponents().iterator();
        Collection<String> selectedIds = new LinkedList<String>();
        while (metacomps.hasNext()) {
            RADComponent metacomp = (RADComponent)metacomps.next();
            selectedIds.add(metacomp.getId());
        }
        return selectedIds;
    }
    
    /**
     * Checks whether the given components are in the same containter.
     *
     * @param compIds <code>Collection</code> of component IDs.
     * @return common container parent or <code>null</code>
     * if the components are not from the same container.
     */
    private RADComponent commonParent(Collection compIds) {
        RADComponent parent = null;
        Iterator iter = compIds.iterator();
        FormModel formModel = getFormModel();
        while (iter.hasNext()) {
            String compId = (String)iter.next();
            RADComponent metacomp = formModel.getMetaComponent(compId);
            RADComponent metacont = metacomp.getParentComponent();
            if (parent == null) {
                parent = metacont;
            }
            if ((metacont == null) || (parent != metacont)) {
                return null;
            }
        }
        return parent;
    }

    // ---------
    // visibility update

    void updateVisualSettings() {
        componentLayer.updateVisualSettings();
        if (nonVisualTray != null) {
            nonVisualTray.updateVisualSettings();
        }
        layeredPane.revalidate();
        layeredPane.repaint(); // repaints both HanleLayer and ComponentLayer
    }

    private void ensureComponentIsShown(RADVisualComponent metacomp) {
        Component comp = (Component) getComponent(metacomp);
        if (comp == null)
            return; // component is not in the visualized tree

//        if (comp == null) { // visual component doesn't exist yet
//            if (metacont != null)
//                metacont.getLayoutSupport().selectComponent(
//                               metacont.getIndexOf(metacomp));
//            return;
//        }

        if (comp.isShowing() || !isInDesigner(metacomp) || metacomp == topDesignComponent) {
            return;
        }

        Component topComp = getTopDesignComponentView();
        if (topComp == null || !topComp.isShowing())
            return; // designer is not showing

        RADVisualContainer metacont = metacomp.getParentContainer();
        RADVisualComponent child = metacomp;

        while (metacont != null) {
            Container cont = (Container) getComponent(metacont);

            LayoutSupportManager laysup = metacont.getLayoutSupport();
            if (laysup != null) {
                Container contDelegate = metacont.getContainerDelegate(cont);
                laysup.selectComponent(child.getComponentIndex());
                laysup.arrangeContainer(cont, contDelegate);
            }

            if (metacont == topDesignComponent || cont.isShowing())
                break;

            child = metacont;
            metacont = metacont.getParentContainer();
        }
    }

    // --------------
    // bean connection

    private void connectBean(RADComponent metacomp, boolean showDialog) {
        if (connectionSource == null) {
            connectionSource = metacomp;
            FormEditor.getAssistantModel(formModel).setContext("connectTarget"); // NOI18N
            handleLayer.repaint();
        }
        else {
            if (metacomp == connectionSource) {
                if (connectionTarget != null) {
                    resetConnection();
                    toggleSelectionMode();
                }
                return;
            }
            connectionTarget = metacomp;
            handleLayer.repaint();
            if (showDialog) {
                if (connectionTarget != null)  {
                    FormEditor.getAssistantModel(formModel).setContext("connectWizard"); // NOI18N
                    createConnection(connectionSource, connectionTarget);
                }
//                resetConnection();
                toggleSelectionMode();
            }
        }
    }

    public RADComponent getConnectionSource() {
        return connectionSource;
    }

    public RADComponent getConnectionTarget() {
        return connectionTarget;
    }

    public void resetConnection() {
        if (connectionSource != null || connectionTarget != null) {
            connectionSource = null;
            connectionTarget = null;
            handleLayer.repaint();
        }
    }

    private void createConnection(RADComponent source, RADComponent target) {
        ConnectionWizard cw = new ConnectionWizard(formModel, source,target);

        if (cw.show()) {
            final Event event = cw.getSelectedEvent();
            final String eventName = cw.getEventName();
            String bodyText = cw.getGeneratedCode();

            formModel.getFormEvents().attachEvent(event, eventName, bodyText);

            // hack: after all updates, switch to editor
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    formModel.getFormEvents().attachEvent(event, eventName, null);
                }
            });
        }
    }

    // -----------------
    // in-place editing

    public void startInPlaceEditing(RADComponent metacomp) {
        
        if (formModel.isReadOnly())
            return;
        if (textEditLayer != null && textEditLayer.isVisible())
            return;
        if (!isEditableInPlace(metacomp)) // check for sure
            return;

        Component comp = (Component) getComponent(metacomp);
        if (comp == null) { // component is not visible
            notifyCannotEditInPlace();
            return;
        }

        FormProperty property = null;
        if (JTabbedPane.class.isAssignableFrom(metacomp.getBeanClass())) {
            JTabbedPane tabbedPane = (JTabbedPane)comp;
            int index = tabbedPane.getSelectedIndex();
            RADVisualContainer metacont = (RADVisualContainer)metacomp;
            RADVisualComponent tabComp = metacont.getSubComponent(index);
            Node.Property[] props = tabComp.getConstraintsProperties();
            for (int i=0; i<props.length; i++) {
                if (props[i].getName().equals("TabConstraints.tabTitle")) { // NOI18N
                    if (props[i] instanceof FormProperty) {
                        property = (FormProperty)props[i];
                    } else {
                        return;
                    }
                }
            }
            if (property == null) return;
        } else {
            property = metacomp.getBeanProperty("text"); // NOI18N
            if (property == null)
                return; // should not happen
        }

        String editText = null;
        try {
            Object text = property.getRealValue();
            if (!(text instanceof String)) text = ""; // or return?
            editText = (String) text;
        }
        catch (Exception ex) { // should not happen
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            return;
        }

        editedProperty = property;

        getInPlaceEditLayer();
        try {
            textEditLayer.setEditedComponent(comp, editText);
        }
        catch (IllegalArgumentException ex) {
            notifyCannotEditInPlace();
            return;
        }

        textEditLayer.setVisible(true);
        handleLayer.setVisible(false);
        textEditLayer.requestFocus();
    }

    private InPlaceEditLayer.FinishListener getFinnishListener() {
        if(finnishListener==null) {
           finnishListener =  new InPlaceEditLayer.FinishListener() {
                @Override
                public void editingFinished(boolean textChanged) {
                    finishInPlaceEditing(textEditLayer.isTextChanged());
                }
            };
        }
        return finnishListener;
    }
        
    
        
    private void finishInPlaceEditing(boolean applyChanges) {
        if (applyChanges) {
            try {       
        Object value = editedProperty.getValue();
        if(value instanceof String) {
            editedProperty.setValue(textEditLayer.getEditedText());         
        } else {    
            PropertyEditor prEd = editedProperty.findDefaultEditor();
            editedProperty.setValue(new FormProperty.ValueWithEditor(textEditLayer.getEditedText(), prEd));             
        }                        
        } catch (Exception ex) { // should not happen
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
    if (handleLayer != null) {
            textEditLayer.setVisible(false);
            handleLayer.setVisible(true);
            handleLayer.requestFocus();
        }
        editedProperty = null;
    }

    public boolean isEditableInPlace(RADComponent metacomp) {
        if (metacomp == null) {
            return false;
        }
        Object comp = getComponent(metacomp);
        if (!(comp instanceof Component)) {
            return false;
        }

        // don't allow in-place editing if there's some AWT parent (it may
        // cause problems with fake peers on some platforms)
        RADComponent parent = metacomp.getParentComponent();
        while (parent != null) {
            if (!JComponent.class.isAssignableFrom(parent.getBeanClass())
                && !RootPaneContainer.class.isAssignableFrom(
                                        parent.getBeanClass()))
                return false;
            parent = parent.getParentComponent();
        }

        Class beanClass = metacomp.getBeanClass();
        if (!InPlaceEditLayer.supportsEditingFor(beanClass, false)) {
            return false;
        }
        if (JTabbedPane.class.isAssignableFrom(beanClass)) {
            if (metacomp instanceof RADVisualContainer) {
                RADVisualContainer metacont = (RADVisualContainer)metacomp;
                if (metacont.getLayoutSupport() != null && metacont.getLayoutSupport().isDedicated()) {
                    // so hopefully really a JTabbedPane container with tabs as subcomponents (bug 231236)
                    int tabCount = ((JTabbedPane)comp).getTabCount();
                    return tabCount > 0 && tabCount == metacont.getSubComponents().length;
                }
            }
            return false;
        }
        return true;
    }

    private void notifyCannotEditInPlace() {
        DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(
                FormUtils.getBundleString("MSG_ComponentNotShown"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE));
    }

    
    // -----------------
    // menu editing
    
    public void openMenu(RADComponent metacomp) {
        MenuEditLayer menuEditLayer = getMenuEditLayer();
        Component comp = (Component) getComponent(metacomp);
        menuEditLayer.setVisible(true);
        menuEditLayer.openAndShowMenu(metacomp,comp);
    }
    
    // --------

    public static void setSelectedDesigner(FormDesigner designer, boolean select) {
        if (select) {
            selectedDesigner = designer;
            FormEditor formEditor = designer.getFormEditor();
            formEditor.setFormDesigner(designer);
            ComponentInspector.getInstance().setFormDesigner(designer);
            PaletteUtils.setContext(formEditor.getFormDataObject().getPrimaryFile());
            if (designer.layoutDesigner != null) {
                designer.layoutDesigner.setActive(true);
            }
        } else if (selectedDesigner == designer) {
            selectedDesigner = null;
            ComponentInspector.getInstance().setFormDesigner(null);
            PaletteUtils.setContext(null);
        }
        updateTestAction();
    }

    public static FormDesigner getSelectedDesigner() {
        return selectedDesigner;
    }

    public JComponent getDesignCanvas() {
        return canvasRoot;
    }

    public void componentActivated() {
        if (!active) {
            active = true;
            setSelectedDesigner(this, true);
            ComponentInspector.getInstance().attachActions();
            getToolBar().putClientProperty("isActive", Boolean.TRUE); // for JDev // NOI18N
        }
        if (formModel != null && (textEditLayer == null || !textEditLayer.isVisible())) {
            handleLayer.requestFocus();
        }
    }

    public void componentDeactivated() {
        active = false;
        if (textEditLayer != null && textEditLayer.isVisible()) {
            textEditLayer.finishEditing(false);
        }
        resetConnection();
        getToolBar().putClientProperty("isActive", Boolean.FALSE); // for JDev // NOI18N
    }

    public void requestActive() {
        // provisional hack
        // TODO temporary, delegate activation to somewhere...
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, canvasRoot);
        if (tc != null) {
            tc.requestActive();
        }
    }

    public void componentShowing() {
        if (formEditor.isFormLoaded()) {
            initialize();
        }
    }

    public void componentHidden() {
        active = false;
        ComponentInspector.getInstance().detachActions();
        if (getDesignerMode() == MODE_CONNECT && formModel != null) {
            clearSelection();
        }
    }

    public void loadingComplete() {
        initialize();
        RADComponent topMetacomp = formModel.getTopRADComponent();
        if (topMetacomp != null) {
            setSelectedComponent(topMetacomp);
        } else {
            setSelectedNodes(formEditor.getFormRootNode());
        }
        if (active) { // focus HandleLayer, need to invoke later - not in hierarchy yet
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    if (active && handleLayer != null) {
                        handleLayer.requestFocus();
                    }
                }
            });
        }
    }

    public InPlaceEditLayer getInPlaceEditLayer() {
        if (textEditLayer == null) {
            textEditLayer = new InPlaceEditLayer();
            textEditLayer.setVisible(false);
            textEditLayer.addFinishListener(getFinnishListener());
            layeredPane.add(textEditLayer, Integer.valueOf(2001));
        }
        return textEditLayer;
    }
    
    MenuEditLayer getMenuEditLayer() {
        if(menuEditLayer == null) {
            menuEditLayer = new MenuEditLayer(this);
            menuEditLayer.setVisible(false);
            layeredPane.add(menuEditLayer, Integer.valueOf(2000));
        }
        return menuEditLayer;
    }
    // -----------
    // innerclasses

    private class LayoutMapper implements VisualMapper {

        // -------

//        public String getTopComponentId() {
//            return getTopDesignComponent().getId();
//        }

        /**
         * @return actual bounds of given component, null if the component is not
         *         currently visualized in the design area
         */
        @Override
        public Rectangle getComponentBounds(String componentId) {
            Component visual = getVisualComponent(componentId, true, false);
            if (visual == null) {
                return null;
            }
            Rectangle rect = componentBoundsToTop(visual);
            
            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  compBounds.put(\"" + componentId + "\", new Rectangle(" +  //NOI18N
                                                            rect.x + ", " + rect.y + ", " + rect.width + ", " + rect.height + "));"); //NOI18N
            }
            
            return rect;
        }

        @Override
        public Rectangle getContainerInterior(String componentId) {
            Component visual = getVisualComponent(componentId, true, false);
            if (visual == null)
                return null;

            RADVisualContainer metacont = (RADVisualContainer)
                                          getMetaComponent(componentId);
            Container cont = metacont.getContainerDelegate(visual);

            Rectangle rect = componentBoundsToTop(cont);
            Dimension dim = cont.getMinimumSize();
            if (dim.width > rect.width) {
                rect.width = dim.width;
            }
            if (dim.height > rect.height) {
                rect.height = dim.height;
            }
            Insets insets = cont.getInsets();
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= insets.left + insets.right;
            rect.height -= insets.top + insets.bottom;

            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  contInterior.put(\"" + componentId + "\", new Rectangle(" +  //NOI18N
                                                        rect.x + ", " + rect.y + ", " + rect.width + ", " + rect.height + "));"); //NOI18N
        }
            
            return rect;
        }

        @Override
        public Dimension getComponentMinimumSize(String componentId) {
            Component visual = getVisualComponent(componentId, false, false);
            Dimension dim = null;
            if (visual != null) {
                dim = visual.getMinimumSize();
            }
            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  compMinSize.put(\"" + componentId + "\", new Dimension(" +  //NOI18N
                    dim.width + ", " + dim.height + "));"); //NOI18N
            }            
            return dim;
        }

        @Override
        public Dimension getComponentPreferredSize(String componentId) {
            Component visual = getVisualComponent(componentId, false, false);
            Dimension dim = null;
            if (visual != null) {
                dim = visual.getPreferredSize();
            }
            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  compPrefSize.put(\"" + componentId + "\", new Dimension(" +  //NOI18N
                    dim.width + ", " + dim.height + "));"); //NOI18N
            }
            return dim;
        }

        @Override
        public boolean hasExplicitPreferredSize(String componentId) {
            JComponent visual = (JComponent) getVisualComponent(componentId, false, true);
            boolean hasExplPrefSize = false;
            if (visual != null) {
                hasExplPrefSize = visual.isPreferredSizeSet();
            }
            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  hasExplicitPrefSize.put(\"" + componentId + "\", new Boolean(" + hasExplPrefSize + "));"); //NOI18N
            }
            return hasExplPrefSize;
        }

        @Override
        public int getBaselinePosition(String componentId, int width, int height) {
            int baseLinePos = -1;            
            JComponent comp = (JComponent) getVisualComponent(componentId, true, true);
            // [hack - vertically resizable components cannot be baseline aligned]
            // [this should be either solved or filtered in LayoutDragger according to vertical resizability of the component]
            if (comp != null && (comp instanceof JScrollPane
                                 || comp.getClass().equals(JPanel.class)
                                 || comp instanceof JTabbedPane
                                 || (comp instanceof AbstractButton
                                     && ((AbstractButton)comp).getVerticalTextPosition() != SwingConstants.CENTER)
                                 || (comp instanceof JLabel
                                     && ((JLabel)comp).getVerticalTextPosition() != SwingConstants.CENTER))) {
//                    || comp instanceof JTextArea
//                    || comp instanceof JTree || comp instanceof JTable || comp instanceof JList
                baseLinePos = 0;
            }

            if (baseLinePos == -1 && comp != null && height >= 0) {
                Insets insets = comp.getInsets();
                if (insets == null || height - insets.top - insets.bottom >= 0) {
                    if (width < 0) {
                        width = 0;
                    }
                    try {
                        baseLinePos = comp.getBaseline(width, height);
                    } catch (Exception ex) {
                        // Sometimes getting baseline may fail even if we do nothing wrong,
                        // e.g. when a JLabel with html text and icon is set smaller than default (bug 229412).
                        Logger.getLogger(FormDesigner.class.getName()).log(Level.INFO, null, ex);
                    }
                }
            }
            if (baseLinePos == -1) {
                baseLinePos = 0;
            }

            if (getLayoutDesigner().logTestCode()) {
                String id = componentId + "-" + width + "-" + height; //NOI18N
                getLayoutDesigner().testCode.add("  baselinePosition.put(\"" + id + "\", new Integer(" + baseLinePos + "));"); //NOI18N
            }

            return baseLinePos;
        }

        @Override
        public int getPreferredPadding(String comp1Id,
                                       String comp2Id,
                                       int dimension,
                                       int comp2Alignment,
                                       PaddingType paddingType)
        {
            String id = null;
        if (getLayoutDesigner().logTestCode()) {
        id = comp1Id + "-" + comp2Id + "-" + dimension + "-" + comp2Alignment + "-" // NOI18N
                     + (paddingType != null ? paddingType.ordinal() : 0);
        }
            
            JComponent comp1 = (JComponent) getVisualComponent(comp1Id, false, true);
            JComponent comp2 = (JComponent) getVisualComponent(comp2Id, false, true);
            if (comp1 == null || comp2 == null) { // not JComponents...
                if (getLayoutDesigner().logTestCode()) {
                    getLayoutDesigner().testCode.add("  prefPadding.put(\"" + id +                  //NOI18N
                "\", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType");       //NOI18N
                }
                return 10; // default distance between components (for non-JComponents)
            }

            assert dimension == HORIZONTAL || dimension == VERTICAL;
            assert comp2Alignment == LEADING || comp2Alignment == TRAILING;

            LayoutStyle.ComponentPlacement type = paddingType == PaddingType.INDENT ? LayoutStyle.ComponentPlacement.INDENT :
                (paddingType == PaddingType.RELATED ? LayoutStyle.ComponentPlacement.RELATED : LayoutStyle.ComponentPlacement.UNRELATED);
            int position = 0;
            if (dimension == HORIZONTAL) {
                if (paddingType == PaddingType.INDENT) {
                    position = comp2Alignment == LEADING ?
                               SwingConstants.WEST : SwingConstants.EAST;
                } else {
                    position = comp2Alignment == LEADING ?
                               SwingConstants.EAST : SwingConstants.WEST;
                }
            }
            else {
                position = comp2Alignment == LEADING ?
                           SwingConstants.SOUTH : SwingConstants.NORTH;
            }

            int prefPadding = paddingType != PaddingType.SEPARATE ?
                FormLAF.getDesignerLayoutStyle().getPreferredGap(comp1, comp2, type, position, null)
                : SwingLayoutBuilder.PADDING_SEPARATE_VALUE; // not in LayoutStyle

            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  prefPadding.put(\"" + id + "\", new Integer(" + prefPadding +   //NOI18N
                ")); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType");             //NOI18N
            }
            
            return prefPadding;
        }

        @Override
        public int getPreferredPaddingInParent(String parentId,
                                               String compId,
                                               int dimension,
                                               int compAlignment)
        {
            String id = null;
        if (getLayoutDesigner().logTestCode()) {
        id = parentId + "-" + compId + "-" + dimension + "-" + compAlignment; //NOI18N
        }
            
            JComponent comp = null;
            Container parent = (Container)getVisualComponent(parentId, true, false);
            if (parent != null) {
                RADVisualContainer metacont = (RADVisualContainer)
                                              getMetaComponent(parentId);
                parent = metacont.getContainerDelegate(parent);
                comp = (JComponent) getVisualComponent(compId, false, true);
            }
            if (comp == null) {
                if (getLayoutDesigner().logTestCode()) {
                    getLayoutDesigner().testCode.add("  prefPaddingInParent.put(\"" + id +      //NOI18N
                "\", new Integer(10)); // parentId-compId-dimension-compAlignment");    //NOI18N
                }
                return 10; // default distance from parent border (for non-JComponents)
            }
            
            assert dimension == HORIZONTAL || dimension == VERTICAL;
            assert compAlignment == LEADING || compAlignment == TRAILING;

            int alignment;

            if (dimension == HORIZONTAL) {
                if (compAlignment == LEADING) {
                    alignment = SwingConstants.WEST;
                }
                else {
                    alignment = SwingConstants.EAST;
                }
            }
            else {
// See issue 182286 for the reason why the following code is commented out
//                if (compAlignment == LEADING) {
//                    alignment = SwingConstants.NORTH;
//                }
//                else {
                    alignment = SwingConstants.SOUTH;
//                }
            }
            int prefPadding = FormLAF.getDesignerLayoutStyle().getContainerGap(comp, alignment, parent);

            if (getLayoutDesigner().logTestCode()) {
                getLayoutDesigner().testCode.add("  prefPaddingInParent.put(\"" + id + "\", new Integer(" +  //NOI18N
            prefPadding + ")); // parentId-compId-dimension-compAlignment");             //NOI18N
            }

            return prefPadding;
        }

        @Override
        public boolean[] getComponentResizability(String compId, boolean[] resizability) {
            resizability[0] = resizability[1] = true;
            // [real resizability spec TBD]
            return resizability;
        }

        @Override
        public void rebuildLayout(String contId) {
            RADVisualContainer metacont = (RADVisualContainer)getMetaComponent(contId);
            replicator.updateContainerLayout(metacont);
            replicator.getLayoutBuilder(contId).doLayout();

            // The layout is rebuilt due to additional changes made when updating
            // LayoutDesigner to actual visual appearance. But the primary change
            // that caused this could happen in a different container. We need to
            // ensure we also have this rebuild done again after undo/redo, so
            // recording a change for that.
            if (formModel.isCompoundEditInProgress()) {
                FormModelEvent ev = new FormModelEvent(formModel, FormModelEvent.CONTAINER_LAYOUT_CHANGED);
                ev.setComponentAndContainer(metacont, metacont);
                formModel.addUndoableEdit(ev.getUndoableEdit());
            } // Note: if FormModel had no compound edit started, then this would be a
              // first change (correction) after the form is loaded. Not to be undone.
        }

        @Override
        public void setComponentVisibility(String componentId, boolean visible) {
            Object comp = getComponent(componentId);
            if (comp instanceof Component) {
                Component component = ((Component)comp);
                Rectangle bounds = null;
                Rectangle visibleBounds = null;
                if (!visible) {
                    bounds = component.getBounds();
                    visibleBounds = FormUtils.getVisibleRect(component);
                }
                component.setVisible(visible);
                RADComponent metacomp = getMetaComponent(componentId);
                handleLayer.updateHiddentComponent(metacomp, bounds, visibleBounds);
            }
        }

        @Override
        public void repaintDesigner(String forComponentId) {
            RADComponent metacomp = formModel != null ? formModel.getMetaComponent(forComponentId) : null;
            if (metacomp instanceof RADVisualComponent
                    && isInDesigner((RADVisualComponent)metacomp)) {
                getHandleLayer().repaint();
            }
        }

        @Override
        public Shape getComponentVisibilityClip(String componentId) {
            Component component = getVisualComponent(componentId, true, false);
            if (component == null) {
                return null;
            }
            
            int x1 = 0;
            int x2 = component.getWidth();
            int y1 = 0;
            int y2 = component.getHeight();
            int cutX1 = 0; // biggest cut of x1 position (negative)
            int cutX2 = 0; // biggest cut of x2 position (positive)
            int cutY1 = 0; // biggest cut of y1 position (negative)
            int cutY2 = 0; // biggest cut of y2 position (positive)

            Component top = getTopDesignComponentView();
            if (component != top) {
                Component comp = component;
                Component parent = comp.getParent();
                while (comp != top) {
                    if (parent == null) {
                        return null; // not under top design component, something wrong
                    }
                    x1 += comp.getX();
                    if (x1 < cutX1) {
                        cutX1 = x1;
                    }
                    x2 += comp.getX();
                    int outX2 = x2 - parent.getWidth();
                    if (outX2 > cutX2) {
                        cutX2 = outX2;
                    }
                    y1 += comp.getY();
                    if (y1 < cutY1) {
                        cutY1 = y1;
                    }
                    y2 += comp.getY();
                    int outY2 = y2 - parent.getHeight();
                    if (outY2 > cutY2) {
                        cutY2 = outY2;
                    }
                    comp = parent;
                    parent = comp.getParent();
                }
            }

            Rectangle bounds = new Rectangle(x1-cutX1, y1-cutY1,
                    x2-cutX2-x1+cutX1, y2-cutY2-y1+cutY1);
            if (bounds.width < 0) {
                bounds.width = 0;
            }
            if (bounds.height < 0) {
                bounds.height = 0;
            }
            return bounds;
        }

        @Override
        public String[] getIndirectSubComponents(String compId) {
            RADComponent metacomp = formModel.getMetaComponent(compId);
            if (metacomp instanceof RADVisualContainer) {
                List<String> l = collectRootLayoutSubComponents((RADVisualContainer)metacomp, null);
                if (l != null) {
                    return l.toArray(new String[0]);
                }
            }
            return null;
        }

        // -------

        private RADComponent getMetaComponent(String compId) {
            RADComponent metacomp = formModel.getMetaComponent(compId);
            if (metacomp == null) {
                RADComponent precreated =
                    formModel.getComponentCreator().getPrecreatedMetaComponent();
                if (precreated != null && precreated.getId().equals(compId)) {
                    metacomp = precreated;
                }
            }
            return metacomp;
        }

        private Component getVisualComponent(String compId, boolean needVisible, boolean needJComponent) {
            Object comp = getComponent(compId);
            if (comp == null) {
                RADVisualComponent precreated =
                    formModel.getComponentCreator().getPrecreatedMetaComponent();
                if (precreated != null && precreated.getId().equals(compId)) {
                    comp = precreated.getBeanInstance();
                }
                if (comp == null && !needVisible) {
                    RADComponent metacomp = getMetaComponent(compId);
                    if (metacomp != null) {
                        comp = metacomp.getBeanInstance();
                    }
                }
            }
            Class<?> type = needJComponent ? JComponent.class : Component.class;
            return comp != null && type.isAssignableFrom(comp.getClass()) ?
                   (Component) comp : null;
        }

    }

    private static List<String> collectRootLayoutSubComponents(RADVisualContainer metacont, List<String> list) {
        for (RADVisualComponent sub : metacont.getSubComponents()) {
            if (sub instanceof RADVisualContainer) {
                RADVisualContainer subcont = (RADVisualContainer) sub;
                if (subcont.getLayoutSupport() == null) {
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(subcont.getId());
                } else {
                    list = collectRootLayoutSubComponents(subcont, list);
                }
            }
        }
        return list;
    }

    // --------

    private Collection<String> componentIds() {
        List<String> componentIds = new LinkedList<String>();
        List selectedComps = getSelectedLayoutComponents();
        LayoutModel layoutModel = getFormModel().getLayoutModel();
        Iterator iter = selectedComps.iterator();
        while (iter.hasNext()) {
            RADVisualComponent visualComp = (RADVisualComponent)iter.next();
            if ((visualComp.getParentContainer() != null)
                && (visualComp.getParentLayoutSupport() == null)
                && layoutModel.getLayoutComponent(visualComp.getId()) != null)
                componentIds.add(visualComp.getId());
        }
        return componentIds;
    }


    // Listener on FormModel - ensures updating of designer view.
    private class FormListener implements FormModelListener, Runnable {

        private FormModelEvent[] events;

        @Override
        public void formChanged(final FormModelEvent[] events) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        processEvents(events);
                    }
                });
            } else {
                processEvents(events);
            }
        }

        private void processEvents(FormModelEvent[] events) {
            boolean lafBlock;
            if (events == null) {
                lafBlock = true;
            }
            else {
                lafBlock = false;
                boolean modifying = false;
                for (int i=0; i < events.length; i++) {
                    FormModelEvent ev = events[i];
                    if (ev.isModifying())
                        modifying = true;
                    if ((ev.getChangeType() == FormModelEvent.COMPONENT_ADDED)
                            || (ev.getChangeType() == FormModelEvent.COMPONENT_PROPERTY_CHANGED)
                            || (ev.getChangeType() == FormModelEvent.BINDING_PROPERTY_CHANGED)) {
                        lafBlock = true;
                        break;
                    }
                }
                if (!modifying)
                    return;

                assert EventQueue.isDispatchThread();
            }

            this.events = events;

            if (lafBlock) { // Look&Feel UI defaults remapping needed
                Locale defaultLocale = switchToDesignLocale(getFormModel());
                try {
                    FormLAF.executeWithLookAndFeel(formModel, this);
                }
                finally {
                    if (defaultLocale != null)
                        Locale.setDefault(defaultLocale);
                }
            }
            else run();
        }

        @Override
        public void run() {
            if (events == null) {
                Object originalVisualComp = getTopDesignComponentView();
                final Dimension originalSize =  originalVisualComp instanceof Component ?
                    ((Component)originalVisualComp).getSize() : null;

                replicator.setTopMetaComponent(topDesignComponent);
                Component formClone = (Component) replicator.createClone();
                if (formClone != null) {
                    formClone.setVisible(true);
                    componentLayer.setTopDesignComponent(formClone);
                    if (originalSize != null) {
                        componentLayer.setDesignerSize(originalSize);
                        checkDesignerSize();
                    } else {
                        setupDesignerSize();
                    }
                    if (getLayoutDesigner() != null) {
                        getLayoutDesigner().externalSizeChangeHappened();
                    }
                    // Must be invoked later. ComponentLayer doesn't have a peer (yet)
                    // when the form is opened and validate does nothing on components
                    // without peer.
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (originalSize == null) {
                                setupDesignerSize(); // once again to workaround some quirks in first layout (e.g. scrollpane with table)
                            }
                            updateComponentLayer(false);
                        }
                    });
                }
                return;
            }

            FormModelEvent[] events = sortEvents(this.events);
            this.events = null;

            int prevType = 0;
            ComponentContainer prevContainer = null;
            boolean updateDone = false;
            boolean updateLayoutDesignerSelection = false;
            boolean structureChanged = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                int type = ev.getChangeType();
                ComponentContainer metacont = ev.getContainer();

                if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                    || type == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                    || type == FormModelEvent.COMPONENT_LAYOUT_CHANGED)
                {
                    RADVisualContainer visualMetaCont = (RADVisualContainer) metacont;
                    if ((prevType != FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                         && prevType != FormModelEvent.CONTAINER_LAYOUT_CHANGED
                         && prevType != FormModelEvent.COMPONENT_LAYOUT_CHANGED)
                        || prevContainer != metacont)
                    {
                        replicator.updateContainerLayout(visualMetaCont);
                        updateDone = true;
                    }
                    if (type == FormModelEvent.CONTAINER_LAYOUT_EXCHANGED
                            && visualMetaCont.getLayoutSupport() == null
                            && getSelectedComponents().contains(visualMetaCont)) {
                        // switched to free design, update selection in LayoutDesigner
                        updateLayoutDesignerSelection = true;
                    }
                    structureChanged = true;
                }
                else if (type == FormModelEvent.COMPONENT_ADDED) {
                    if ((metacont instanceof RADVisualContainer
                            || metacont instanceof RADMenuComponent)
                        && (prevType != FormModelEvent.COMPONENT_ADDED
                            || prevContainer != metacont))
                    {
                        replicator.updateAddedComponents(metacont);
                        // Note: replicator calls BindingDesignSupport to establish
                        // bindings for the the cloned instance (e.g. in remove undo)
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_REMOVED) {
                    RADComponent removed = ev.getComponent();

                    // if the top designed component (or some of its parents)
                    // was removed then whole designer view must be recreated
                    if (removed instanceof RADVisualComponent
                        && (removed == topDesignComponent
                            || removed.isParentComponent(topDesignComponent)))
                    {
                        resetTopDesignComponent(false);
                        updateWholeDesigner();
                        return;
                    }
                    else {
                        replicator.removeComponent(ev.getComponent(), ev.getContainer());
                        updateDone = true;
                    }
                    // Note: BindingDesignSupport takes care of removing bindings
                    structureChanged = true;
                }
                else if (type == FormModelEvent.COMPONENTS_REORDERED) {
                    if (prevType != FormModelEvent.COMPONENTS_REORDERED
                        || prevContainer != metacont)
                    {
                        replicator.reorderComponents(metacont);
                        updateDone = true;
                    }
                }
                else if (type == FormModelEvent.COMPONENT_PROPERTY_CHANGED) {
                    RADProperty eventProperty = ev.getComponentProperty();
                    RADComponent eventComponent = ev.getComponent();
                    if (eventProperty != null) { // bug #220513, don't update anything e.g. for a11y properties
                        replicator.updateComponentProperty(eventProperty);
                        updateConnectedProperties(eventProperty, eventComponent);
                        if (layoutDesigner != null && formModel.isCompoundEditInProgress()) {
                            layoutDesigner.componentDefaultSizeChanged(eventComponent.getId());
                        }
                    }
                    
                    updateDone = true;
                }
                else if (type == FormModelEvent.BINDING_PROPERTY_CHANGED) {
                    if (ev.getSubPropertyName() == null) {
                        replicator.updateBinding(ev.getNewBinding());
                    }
                    // Note: BindingDesignSupport takes care of removing the old binding
                    updateDone = true;
                }
                else if (type == FormModelEvent.SYNTHETIC_PROPERTY_CHANGED
                         && PROP_DESIGNER_SIZE.equals(ev.getPropertyName())) {
                    Dimension size = (Dimension) ev.getNewPropertyValue();
                    if (size == null) {
                        size = (Dimension) topDesignComponent.getAuxValue(PROP_DESIGNER_SIZE);
                    }
                    componentLayer.setDesignerSize(size);
                    designerSizeExplictlySet = true;
                    updateDone = true;
                }

                prevType = type;
                prevContainer = metacont;
            }

            if (updateDone) {
                checkDesignerSize();
                LayoutDesigner layoutDesigner = getLayoutDesigner();
                if ((layoutDesigner != null) && formModel.isCompoundEditInProgress()) {
                    getLayoutDesigner().externalSizeChangeHappened();
                }
                updateComponentLayer(true);
                if (updateLayoutDesignerSelection) {
                    updateLayoutDesigner();
                }
                // If some change happened while adding, moving or resizing, cancel that operation
                // (e.g. in case of deleting a component when just adding a new component next to it).
                if (structureChanged) {
                    if (getDesignerMode() != MODE_SELECT) {
                        toggleSelectionMode();
                    } else {
                        handleLayer.endDragging(null);
                    }
                }
            }
        }
        
        private FormModelEvent[] sortEvents(FormModelEvent[] events) {
            LinkedList<FormModelEvent> l = new LinkedList<>();
            for (FormModelEvent event : events) {
                l.add(event);
                if (event.getContainer() instanceof RADVisualContainer) {
                    int i = 0;
                    int n = l.size() - 1;
                    while (n > 0) {
                        FormModelEvent e = l.get(i);
                        if (e.getContainer() instanceof RADVisualContainer
                                && eventsOrder(e, event) == 0) {
                            // we want subcontainers updated before parent's
                            // layout is rebuilt, and CONTAINER_LAYOUT_CHANGED
                            // processed at the end (after add/remove changes)
                            l.remove(e);
                            l.add(e);
                        } else {
                            i++;
                        }
                        n--;
                    }
                }
            }
            return l.toArray(new FormModelEvent[0]);
        }

        /**
         * @return 1: order is e1 then e2,
         *         0: order is e2 then e1,
         *        -1: order not determined
         */
        private int eventsOrder(FormModelEvent e1, FormModelEvent e2) {
            RADVisualContainer cont1 = (RADVisualContainer) e1.getContainer();
            RADVisualContainer cont2 = (RADVisualContainer) e2.getContainer();
            if (e2.getChangeType() == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                    && (cont2 == cont1 || cont2.isParentComponent(cont1))) {
                return 1;
            }
            if (e1.getChangeType() == FormModelEvent.CONTAINER_LAYOUT_CHANGED
                    && (cont1 == cont2 || cont1.isParentComponent(cont2))) {
                return 0;
            }
            return -1;
        }

        private void updateConnectedProperties(RADProperty eventProperty, RADComponent eventComponent){
            for (RADComponent component : formModel.getAllComponents()){
                RADProperty[] properties = component.getKnownBeanProperties();
                for(int i = 0; i < properties.length; i++){
                    try{
                        if (properties[i].isChanged()) {
                            Object value = properties[i].getValue();
                            if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                                RADConnectionPropertyEditor.RADConnectionDesignValue propertyValue = 
                                    (RADConnectionPropertyEditor.RADConnectionDesignValue)value;

                                if (propertyValue.getRADComponent() != null
                                   && propertyValue.getProperty() != null
                                   && eventComponent.getName().equals(propertyValue.getRADComponent().getName())
                                   && eventProperty.getName().equals(propertyValue.getProperty().getName())) {

                                    replicator.updateComponentProperty(properties[i]);
                                }
                            }
                        }
                    } catch(Exception e){
                        ErrorManager.getDefault().notify(e);
                    }                                                        
                }
            }
                                
        }
    }

    /**
     * Action that aligns selected components in the specified direction.
     */
    private class AlignAction extends AbstractAction {
        // PENDING change to icons provided by Dusan
        private static final String ICON_BASE = "org/netbeans/modules/form/resources/align_"; // NOI18N
        /** Dimension to align in. */
        private int dimension;
        /** Requested alignment. */
        private int alignment;
        /** Group/Align action. */
        private boolean closed;
        
        /**
         * Creates action that aligns selected components in the specified direction.
         *
         * @param dimension dimension to align in.
         * @param alignment requested alignment.
         */
        AlignAction(int dimension, int alignment, boolean closed) {
            this.dimension = dimension;
            this.alignment = alignment;
            this.closed = closed;
            boolean horizontal = (dimension == LayoutConstants.HORIZONTAL);
            boolean leading = (alignment == LayoutConstants.LEADING);
            String code;
            if (alignment == LayoutConstants.CENTER) {
                code = (horizontal ? "ch" : "cv"); // NOI18N
            } else {
                code = (horizontal ? (leading ? "l" : "r") : (leading ? "u" : "d")); // NOI18N
            }
            String iconResource = ICON_BASE + code + ".png"; // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(iconResource, true));
            putValue(Action.SHORT_DESCRIPTION, FormUtils.getBundleString("CTL_AlignAction_" + code)); // NOI18N
            setEnabled(false);
        }
        
        /**
         * Performs the alignment of selected components.
         *
         * @param e event that invoked the action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            align(closed, dimension, alignment);
        }
        
        public int getDimension() {
            return dimension;
        }
        
        public int getAlignment() {
            return alignment;
        }
        
    }    
    /**
     * Action that aligns selected components in the specified direction.
     */
    private class ResizabilityAction extends AbstractAction {
        // PENDING change to icons provided by Dusan
        private static final String ICON_BASE = "org/netbeans/modules/form/resources/resize_"; // NOI18N
        /** Dimension of resizability. */
        private int dimension;
        
        /**
         * Creates action that changes the resizability of the component.
         *
         * @param dimension dimension of the resizability
         */
        ResizabilityAction(int dimension) {
            this.dimension = dimension;
            String code = (dimension == LayoutConstants.HORIZONTAL) ? "h" : "v"; // NOI18N
            String iconResource = ICON_BASE + code + ".png"; // NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(iconResource, false));
            putValue(Action.SHORT_DESCRIPTION, FormUtils.getBundleString("CTL_ResizeButton_" + code)); // NOI18N
            setEnabled(false);
        }
        
        /**
         * Performs the resizability change of selected components.
         *
         * @param e event that invoked the action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            FormModel formModel = getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            LayoutDesigner layoutDesigner = getLayoutDesigner();
            Collection componentIds = componentIds();
            Set<RADVisualContainer> containers = new HashSet<RADVisualContainer>();
            try {
                Iterator iter = componentIds.iterator();
                while (iter.hasNext()) {
                    String compId = (String)iter.next();
                    LayoutComponent layoutComp = layoutModel.getLayoutComponent(compId);
                    boolean resizing = Boolean.TRUE.equals(getValue(Action.SELECTED_KEY));
                    if (layoutDesigner.isComponentResizing(layoutComp, dimension) != resizing) {
                        layoutDesigner.setComponentResizing(layoutComp, dimension, resizing);
                        RADVisualComponent comp = (RADVisualComponent)formModel.getMetaComponent(compId);
                        containers.add(comp.getParentContainer());
                    }
                }
                autoUndo = false;
            } finally {
                Iterator<RADVisualContainer> iter = containers.iterator();
                while (iter.hasNext()) {
                    formModel.fireContainerLayoutChanged(iter.next(), null, null, null);
                }
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
                }
                if (autoUndo) {
                    formModel.forceUndoOfCompoundEdit();
                }
            }
        }
    }
    
    private static class FormProxyLookup extends ProxyLookup {

        FormProxyLookup() {
            super();
        }
        
        Lookup[] getSubLookups() {
            return getLookups();
        }
        
        void setSubLookups(Lookup[] lookups) {
            setLookups(lookups);
        }
        
    }
    
}
