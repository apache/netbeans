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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.nodes.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Main class of general layout support infrastructure. Connects form editor
 * metadata with specialized LayoutSupportDelegate implementations (layout
 * specific functionality is delegated to the right LayoutSupportDelegate).
 *
 * @author Tomas Pavek
 */

public final class LayoutSupportManager implements LayoutSupportContext {

    // possible component resizing directions (bit flag constants)
    public static final int RESIZE_UP = 1;
    public static final int RESIZE_DOWN = 2;
    public static final int RESIZE_LEFT = 4;
    public static final int RESIZE_RIGHT = 8;

    private LayoutSupportDelegate layoutDelegate;
    private boolean needInit;
    private boolean initializeFromInstance;
    private boolean initializeFromCode;

    private Node.PropertySet[] propertySets;

    private LayoutListener layoutListener;

    private RADVisualContainer metaContainer;

    private Container primaryContainer; // bean instance from metaContainer
    private Container primaryContainerDelegate; // container delegate for it

    private CodeStructure codeStructure;

    private CodeExpression containerCodeExpression;
    private CodeExpression containerDelegateCodeExpression;

    // ----------
    // initialization

    // initialization for a new container, layout delegate is set to null
    public LayoutSupportManager(RADVisualContainer container,
                                CodeStructure codeStructure)
    {
        this.metaContainer = container;
        this.codeStructure = codeStructure;
    }

    /**
     * Creation and initialization of a layout delegate for a new container.
     * @return false if suitable layout delegate is not found
     * @throws IllegalArgumentException if the container instance is not empty
     */
    public boolean prepareLayoutDelegate(boolean fromCode, boolean initialize)
        throws Exception
    {
        LayoutSupportDelegate delegate = null;
        LayoutManager lmInstance = null;

        FormModel formModel = metaContainer.getFormModel();
        LayoutSupportRegistry layoutRegistry =
            LayoutSupportRegistry.getRegistry(formModel);

        // first try to find a dedicated layout delegate (for the container)
        delegate = layoutRegistry.createSupportForContainer(metaContainer.getBeanClass());
        if (delegate != null) {
            if (!fromCode && !delegate.checkEmptyContainer(getPrimaryContainer())) {
                RuntimeException ex = new IllegalArgumentException(
                        AbstractLayoutSupport.getBundle().getString("MSG_ERR_NonEmptyContainer")); // NOI18N
                throw ex;
            }
        } else {
            // find a general layout delegate (for LayoutManager of the container)
            if (fromCode) { // initialization from code
                Iterator it = CodeStructure.getDefinedStatementsIterator(
                                      getContainerDelegateCodeExpression());
                CodeStatement[] statements =
                    CodeStructure.filterStatements(
                        it, AbstractLayoutSupport.getSetLayoutMethod());

                if (statements.length > 0) { // setLayout method found
                    CodeExpressionOrigin layoutOrigin =
                        statements[0].getStatementParameters()[0].getOrigin();
                    Class layoutType = layoutOrigin.getType();
                    delegate = layoutRegistry.createSupportForLayout(layoutType);
                    if (delegate == null) {
                        if (layoutOrigin.getType() == LayoutManager.class
                                && layoutOrigin.getCreationParameters().length == 0
                                && layoutOrigin.getParentExpression() == null
                                && "null".equals(layoutOrigin.getJavaCodeString(null, null))) { // NOI18N
                            // special case of null layout
                            delegate = new NullLayoutSupport();
                        } else if (layoutOrigin.getMetaObject() instanceof java.lang.reflect.Constructor
                                   && layoutOrigin.getCreationParameters().length == 0) {
                            // likely custom layout originally used as a bean because in palette,
                            // now not in palette anymore but let's do like if it still was
                            System.err.println("[WARNING] No support for " + layoutType.getName() + // NOI18N
                                    " was found, trying to use default support like if the layout was in palette as a bean."); // NOI18N
                            LayoutSupportRegistry.registerSupportForLayout(layoutType.getName(), LayoutSupportRegistry.DEFAULT_SUPPORT);
                            delegate = new DefaultLayoutSupport(layoutOrigin.getType());
                        } else {
                            return false;
                        }
                    }
                    lmInstance = getPrimaryContainerDelegate().getLayout();
                }
            }

            if (delegate == null) { // initialization from LayoutManager instance
                Container contDel = getPrimaryContainerDelegate();
                if (!(contDel instanceof InvalidComponent)) {
                    if (contDel.getComponentCount() == 0) {
                        // we can still handle only empty containers ...
                        lmInstance = contDel.getLayout();
                        delegate = lmInstance != null ?
                            layoutRegistry.createSupportForLayout(lmInstance.getClass()) :
                            new NullLayoutSupport();
                    } else {
                        RuntimeException ex = new IllegalArgumentException(
                                AbstractLayoutSupport.getBundle().getString("MSG_ERR_NonEmptyContainer")); // NOI18N
                        throw ex;
                    }
                }
            }
        }

        if (delegate == null)
            return false;

        if (initialize) {
            setLayoutDelegate(delegate, lmInstance, fromCode);
        } else {
            layoutDelegate = delegate;
            needInit = true;
            initializeFromInstance = lmInstance != null;
            initializeFromCode = fromCode;
        }

        return true;
    }

    public void initializeLayoutDelegate() throws Exception {
        if (layoutDelegate != null && needInit) {
            LayoutManager lmInstance = initializeFromInstance ?
                    getPrimaryContainerDelegate().getLayout() : null;
            layoutDelegate.initialize(this, lmInstance, initializeFromCode);
            fillLayout(null);
            getPropertySets(); // force properties and listeners creation
            needInit = false;
        }
    }

    public void setLayoutDelegate(LayoutSupportDelegate newDelegate,
                                  LayoutManager lmInstance,
                                  boolean fromCode)
        throws Exception
    {
        LayoutConstraints[] oldConstraints;
        LayoutSupportDelegate oldDelegate = layoutDelegate;

        if (layoutDelegate != null
                && (layoutDelegate != newDelegate || !fromCode))
            oldConstraints = removeLayoutDelegate(true);
        else
            oldConstraints = null;

        layoutDelegate = newDelegate;
        propertySets = null;
        needInit = false;

        if (layoutDelegate != null) {
            try {
                layoutDelegate.initialize(this, lmInstance, fromCode);
                if (!fromCode)
                    fillLayout(oldConstraints);
                getPropertySets(); // force properties and listeners creation
            }
            catch (Exception ex) {
                removeLayoutDelegate(false);
                layoutDelegate = oldDelegate;
                if (layoutDelegate != null)
                    fillLayout(null);
                throw ex;
            }
        }
    }

    public LayoutSupportDelegate getLayoutDelegate() {
        return layoutDelegate;
    }

    public static LayoutSupportDelegate getLayoutDelegateForDefaultLayout(
                      FormModel formModel, LayoutManager layout) throws Exception {
        LayoutSupportDelegate defaultLayoutDelegate;
        if (layout == null) {
            defaultLayoutDelegate = new NullLayoutSupport();
        } else {
            LayoutSupportRegistry layoutRegistry = LayoutSupportRegistry.getRegistry(formModel);
            defaultLayoutDelegate = layoutRegistry.createSupportForLayout(layout.getClass());
            if (defaultLayoutDelegate == null) {
                defaultLayoutDelegate = new UnknownLayoutSupport();
            }
        }
        return defaultLayoutDelegate;
    }

    public void setUnknownLayoutDelegate(boolean fromCode) {
        try {
            setLayoutDelegate(new UnknownLayoutSupport(), null, fromCode);
        }
        catch (Exception ex) { // nothing should happen, ignore
            ex.printStackTrace();
        }
    }

    public boolean isUnknownLayout() {
        return layoutDelegate == null
               || layoutDelegate instanceof UnknownLayoutSupport;
    }

    public boolean isSpecialLayout() {
        // Every standard layout manager has its own layout delegate.
        // Hence, the DefaultLayoutSupport is used by special layout managers only.
        return layoutDelegate instanceof DefaultLayoutSupport;
    }

    public boolean hasComponentConstraints() {
        if (layoutDelegate != null) {
            for (int i=0, n=getComponentCount(); i < n; i++) {
                if (layoutDelegate.getConstraints(i) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    // copy layout delegate from another container
    public void copyLayoutDelegateFrom(
                    LayoutSupportManager sourceLayoutSupport,
                    RADVisualComponent[] newMetaComps)
    {
        LayoutSupportDelegate sourceDelegate =
            sourceLayoutSupport.getLayoutDelegate();

        int componentCount = sourceDelegate.getComponentCount();

        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        if (layoutDelegate != null)
            removeLayoutDelegate(false);

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] primaryComps = new Component[componentCount];

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = newMetaComps[i];
            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = (Component) metacomp.getBeanInstance();
            ensureFakePeerAttached(primaryComps[i]);
        }

        LayoutSupportDelegate newDelegate =
            sourceDelegate.cloneLayoutSupport(this, compExps);

        newDelegate.setLayoutToContainer(cont, contDel);
        newDelegate.addComponentsToContainer(cont, contDel, primaryComps, 0);

        layoutDelegate = newDelegate;

        // Ensure correct propagation of copied properties (issue 50011, 72351)
        try {
            layoutDelegate.acceptContainerLayoutChange(null);
        } catch (PropertyVetoException pvex) {
            // should not happen
        }
    }

    public void clearPrimaryContainer() {
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
    }

    public RADVisualContainer getMetaContainer() {
        return metaContainer;
    }

//    public boolean supportsArranging() {
//        return layoutDelegate instanceof LayoutSupportArranging;
//    }

    private LayoutConstraints[] removeLayoutDelegate(
                                    boolean extractConstraints)
    {
        CodeGroup code = layoutDelegate.getLayoutCode();
        if (code != null)
            CodeStructure.removeStatements(code.getStatementsIterator());

        int componentCount = layoutDelegate.getComponentCount();
        LayoutConstraints[] constraints = null;

        if (componentCount > 0) {
            RADVisualComponent[] metacomps = metaContainer.getSubComponents();
            if (metacomps.length == componentCount) { // robustness: might be called after failed layout initialization
                if (extractConstraints)
                    constraints = new LayoutConstraints[componentCount];

                for (int i=0; i < componentCount; i++) {
                    LayoutConstraints constr = layoutDelegate.getConstraints(i);
                    if (extractConstraints)
                        constraints[i] = constr;
                    if (constr != null)
                        metacomps[i].setLayoutConstraints(layoutDelegate.getClass(),
                                                          constr);
                    code = layoutDelegate.getComponentCode(i);
                    if (code != null)
                        CodeStructure.removeStatements(code.getStatementsIterator());
                }
            }
        }

        layoutDelegate.removeAll();
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
        layoutDelegate = null;

        return constraints;
    }

    private void fillLayout(LayoutConstraints[] oldConstraints) {
        RADVisualComponent[] metacomps = metaContainer.getSubComponents();
        int componentCount = metacomps.length;

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] designComps = new Component[componentCount];
        Component[] primaryComps = new Component[componentCount];
        LayoutConstraints[] newConstraints = new LayoutConstraints[componentCount];

        FormDesigner designer = FormEditor.getFormDesigner(metaContainer.getFormModel());

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = metacomps[i];

            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = (Component) metacomp.getBeanInstance();
            ensureFakePeerAttached(primaryComps[i]);
            newConstraints[i] = metacomp.getLayoutConstraints(
                                             layoutDelegate.getClass());

            Component comp = designer != null ?
                            (Component) designer.getComponent(metacomp) : null;
            designComps[i] = comp != null ?
                             comp : (Component) metacomp.getBeanInstance();
        }

        if (metaContainer.getFormModel().isUndoRedoRecording()) {
            layoutDelegate.convertConstraints(oldConstraints,
                                              newConstraints,
                                              designComps);
        } // otherwise in undo/redo - don't try to convert constraints

        if (componentCount > 0) {
            layoutDelegate.acceptNewComponents(compExps, newConstraints, 0);
            layoutDelegate.addComponents(compExps, newConstraints, 0);

            for (int i=0; i < componentCount; i++)
                metacomps[i].resetConstraintsProperties();
        }

        // setup primary container
        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();
//        layoutDelegate.clearContainer(cont, contDel);
        if (metaContainer.isDefaultLayoutDelegate(layoutDelegate) && layoutDelegate.getSupportedClass() == null) {
            // e.g. UnknownLayoutSupport can't set the layout
            contDel.setLayout(metaContainer.getDefaultLayout());
        }
        layoutDelegate.setLayoutToContainer(cont, contDel);
        if (componentCount > 0) {
            layoutDelegate.addComponentsToContainer(cont, contDel, primaryComps, 0);
        }
    }

    // ---------
    // public API delegated to LayoutSupportDelegate

    public boolean isDedicated() {
        return layoutDelegate.isDedicated();
    }

    public Class getSupportedClass() {
        return layoutDelegate.getSupportedClass();
    }

    // node presentation
    public boolean shouldHaveNode() {
        return layoutDelegate.shouldHaveNode();
    }

    public String getDisplayName() {
        return layoutDelegate.getDisplayName();
    }

    public Image getIcon(int type) {
        return layoutDelegate.getIcon(type);
    }

    // properties and customizer
    public Node.PropertySet[] getPropertySets() {
        if (propertySets == null) {
            if (layoutDelegate == null) return new Node.PropertySet[0]; // Issue 63916
            propertySets = layoutDelegate.getPropertySets();

            for (int i=0; i < propertySets.length; i++) {
                Node.Property[] props = propertySets[i].getProperties();
                for (int j=0; j < props.length; j++)
                    if (props[j] instanceof FormProperty) {
                        FormProperty prop = (FormProperty) props[j];
                        prop.addVetoableChangeListener(getLayoutListener());
                        prop.addPropertyChangeListener(getLayoutListener());
                    }
            }
        }
        return propertySets;
    }

    public Node.Property[] getAllProperties() {
        if (layoutDelegate instanceof AbstractLayoutSupport)
            return ((AbstractLayoutSupport)layoutDelegate).getAllProperties();

        java.util.List<Node.Property> allPropsList = new ArrayList<Node.Property>();
        for (int i=0; i < propertySets.length; i++) {
            Node.Property[] props = propertySets[i].getProperties();
            for (int j=0; j < props.length; j++)
                allPropsList.add(props[j]);
        }

        Node.Property[] allProperties = new Node.Property[allPropsList.size()];
        allPropsList.toArray(allProperties);
        return allProperties;
    }

    public Node.Property getLayoutProperty(String name) {
        if (layoutDelegate instanceof AbstractLayoutSupport)
            return ((AbstractLayoutSupport)layoutDelegate).getProperty(name);

        Node.Property[] properties = getAllProperties();
        for (int i=0; i < properties.length; i++)
            if (name.equals(properties[i].getName()))
                return properties[i];

        return null;
    }

    public boolean isLayoutPropertyChangedFromInitial(FormProperty prop) {
        if (layoutDelegate instanceof AbstractLayoutSupport) {
            return ((AbstractLayoutSupport)layoutDelegate).isPropertyChangedFromInitial(prop);
        }
        return prop.isChanged();
    }

    public Class getCustomizerClass() {
        return layoutDelegate.getCustomizerClass();
    }

    public Component getSupportCustomizer() {
        return layoutDelegate.getSupportCustomizer();
    }

    // code meta data
    public CodeGroup getLayoutCode() {
        return layoutDelegate.getLayoutCode();
    }

    public CodeGroup getComponentCode(int index) {
        return layoutDelegate.getComponentCode(index);
    }

    public CodeGroup getComponentCode(RADVisualComponent metacomp) {
        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getComponentCode(index) : null;
    }

    public int getComponentCount() {
        return layoutDelegate.getComponentCount();
    }

    // data validation
    public void acceptNewComponents(RADVisualComponent[] components,
                                    LayoutConstraints[] constraints,
                                    int index)
    {
        CodeExpression[] compExps = new CodeExpression[components.length];
        for (int i=0; i < components.length; i++)
            compExps[i] = components[i].getCodeExpression();

        layoutDelegate.acceptNewComponents(compExps, constraints, index);
    }

    // components adding/removing
    public void addComponents(RADVisualComponent[] components,
                              LayoutConstraints[] constraints,
                              int index)
    {
        CodeExpression[] compExps = new CodeExpression[components.length];
        Component[] comps = new Component[components.length];

        for (int i=0; i < components.length; i++) {
            compExps[i] = components[i].getCodeExpression();
            comps[i] = (Component) components[i].getBeanInstance();
            ensureFakePeerAttached(comps[i]);
        }

        if (index < 0)
            index = layoutDelegate.getComponentCount();

        layoutDelegate.addComponents(compExps, constraints, index);

        for (int i=0; i < components.length; i++)
            components[i].resetConstraintsProperties();

        layoutDelegate.addComponentsToContainer(getPrimaryContainer(),
                                                getPrimaryContainerDelegate(),
                                                comps, index);
    }

    public void removeComponent(RADVisualComponent metacomp, int index) {
        // first store constraints in the meta component
        LayoutConstraints constr = layoutDelegate.getConstraints(index);
        if (constr != null)
            metacomp.setLayoutConstraints(layoutDelegate.getClass(), constr);

        // remove code
        CodeStructure.removeStatements(
            layoutDelegate.getComponentCode(index).getStatementsIterator());

        // remove the component from layout
        layoutDelegate.removeComponent(index);

        // remove the component instance from the primary container instance
        if (!layoutDelegate.removeComponentFromContainer(
                                getPrimaryContainer(),
                                getPrimaryContainerDelegate(),
                                (Component)metacomp.getBeanInstance()))
        {   // layout delegate does not support removing individual components,
            // so we clear the container and add the remaining components again
            layoutDelegate.clearContainer(getPrimaryContainer(),
                                          getPrimaryContainerDelegate());

            RADVisualComponent[] metacomps = metaContainer.getSubComponents();
            if (metacomps.length > 1) {
                // we rely on that metacomp was not removed from the model yet
                Component[] comps = new Component[metacomps.length-1];
                for (int i=0; i < metacomps.length; i++) {
                    if (i != index) {
                        Component comp = (Component) metacomps[i].getBeanInstance();
                        ensureFakePeerAttached(comp);
                        comps[i < index ? i : i-1] = comp;
                    }
                }
                layoutDelegate.addComponentsToContainer(
                                   getPrimaryContainer(),
                                   getPrimaryContainerDelegate(),
                                   comps,
                                   0);
            }
        }
    }

    public void removeAll() {
        // first store constraints in meta components
        RADVisualComponent[] components = metaContainer.getSubComponents();
        for (int i=0; i < components.length; i++) {
            LayoutConstraints constr =
                layoutDelegate.getConstraints(i);
            if (constr != null)
                components[i].setLayoutConstraints(layoutDelegate.getClass(),
                                                   constr);
        }

        // remove code of all components
        for (int i=0, n=layoutDelegate.getComponentCount(); i < n; i++)
            CodeStructure.removeStatements(
                layoutDelegate.getComponentCode(i).getStatementsIterator());

        // remove components from layout
        layoutDelegate.removeAll();

        // clear the primary container instance
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
    }

    public boolean isLayoutChanged() {
        Container defaultContainer = (Container)
                BeanSupport.getDefaultInstance(metaContainer.getBeanClass());
        Container defaultContDelegate =
                metaContainer.getContainerDelegate(defaultContainer);

        return layoutDelegate.isLayoutChanged(defaultContainer,
                                              defaultContDelegate);
    }

    // managing constraints
    public LayoutConstraints getConstraints(int index) {
        return layoutDelegate.getConstraints(index);
    }

    public LayoutConstraints getConstraints(RADVisualComponent metacomp) {
        if (layoutDelegate == null)
            return null;

        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getConstraints(index) : null;
    }

    public static LayoutConstraints storeConstraints(
                                        RADVisualComponent metacomp)
    {
        RADVisualContainer parent = metacomp.getParentContainer();
        if (parent == null)
            return null;

        LayoutSupportManager layoutSupport = parent.getLayoutSupport();
        if (layoutSupport == null)
            return null;
        LayoutConstraints constr = layoutSupport.getConstraints(metacomp);
        if (constr != null)
            metacomp.setLayoutConstraints(
                         layoutSupport.getLayoutDelegate().getClass(),
                         constr);
        return constr;
    }

    public LayoutConstraints getStoredConstraints(RADVisualComponent metacomp) {
        return metacomp.getLayoutConstraints(layoutDelegate.getClass());
    }

    // managing live components
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        layoutDelegate.setLayoutToContainer(container, containerDelegate);
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        layoutDelegate.addComponentsToContainer(container, containerDelegate,
                                                components, index);
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component)
    {
        return layoutDelegate.removeComponentFromContainer(
                            container, containerDelegate, component);
    }

    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {
        return layoutDelegate.clearContainer(container, containerDelegate);
    }

    // drag and drop support
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        
        LayoutConstraints constraints =  layoutDelegate.getNewConstraints(container, containerDelegate,
                                                component, index,
                                                posInCont, posInComp);
        String context = null;
        Object[] params = null;
        if (layoutDelegate instanceof AbstractLayoutSupport) {
            AbstractLayoutSupport support = (AbstractLayoutSupport)layoutDelegate;
            context = support.getAssistantContext();
            params = support.getAssistantParams();
        }
        context = (context == null) ? "generalPosition" : context; // NOI18N
        FormEditor.getAssistantModel(metaContainer.getFormModel()).setContext(context, params);
        return constraints;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        return layoutDelegate.getNewIndex(container, containerDelegate,
                                          component, index,
                                          posInCont, posInComp);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        return layoutDelegate.paintDragFeedback(container, containerDelegate,
                                                component,
                                                newConstraints, newIndex,
                                                g);
    }

    // resizing support
    public int getResizableDirections(Container container,
                                      Container containerDelegate,
                                      Component component,
                                      int index)
    {
        return layoutDelegate.getResizableDirections(container,
                                                     containerDelegate,
                                                     component, index);
    }

    public LayoutConstraints getResizedConstraints(Container container,
                                                   Container containerDelegate,
                                                   Component component,
                                                   int index,
                                                   Rectangle originalBounds,
                                                   Insets sizeChanges,
                                                   Point posInCont)
    {
        return layoutDelegate.getResizedConstraints(container,
                                                    containerDelegate,
                                                    component, index,
                                                    originalBounds,
                                                    sizeChanges,
                                                    posInCont);
    }

    // arranging support
    public void processMouseClick(Point p,
                                  Container cont,
                                  Container contDelegate)
    {
        layoutDelegate.processMouseClick(p, cont, contDelegate);
    }

    // arranging support
    public void selectComponent(int index) {
        layoutDelegate.selectComponent(index);
    }

    // arranging support
    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        layoutDelegate.arrangeContainer(container, containerDelegate);
    }

    // -----------
    // API for layout delegates (LayoutSupportContext implementation)

    @Override
    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    @Override
    public CodeExpression getContainerCodeExpression() {
        if (containerCodeExpression == null) {
            containerCodeExpression = metaContainer.getCodeExpression();
            containerDelegateCodeExpression = null;
        }
        return containerCodeExpression;
    }

    @Override
    public CodeExpression getContainerDelegateCodeExpression() {
        if (containerDelegateCodeExpression == null) {
            containerDelegateCodeExpression =
                    containerDelegateCodeExpression(metaContainer, codeStructure);
        }

        return containerDelegateCodeExpression;
    }
    
    public static CodeExpression containerDelegateCodeExpression(
                                     RADVisualContainer metaContainer,
                                     CodeStructure codeStructure)
    {
        CodeExpression containerCodeExpression = metaContainer.getCodeExpression();
        CodeExpression containerDelegateCodeExpression;
        java.lang.reflect.Method delegateGetter =
            metaContainer.getContainerDelegateMethod();

        if (delegateGetter != null) { // there should be a container delegate
            Iterator it = CodeStructure.getDefinedExpressionsIterator(
                                              containerCodeExpression);
            CodeExpression[] expressions = CodeStructure.filterExpressions(
                                                        it, delegateGetter);
            if (expressions.length > 0) {
                // the expresion for the container delegate already exists
                containerDelegateCodeExpression = expressions[0];
            }
            else { // create a new expresion for the container delegate
                CodeExpressionOrigin origin = CodeStructure.createOrigin(
                                                containerCodeExpression,
                                                delegateGetter,
                                                null);
                containerDelegateCodeExpression =
                    codeStructure.createExpression(origin);
            }
        }
        else // no special container delegate
            containerDelegateCodeExpression = containerCodeExpression;
        return containerDelegateCodeExpression;
    }

    // return container instance of meta container
    @Override
    public Container getPrimaryContainer() {
        return (Container) metaContainer.getBeanInstance();
    }

    // return container delegate of container instance of meta container
    @Override
    public Container getPrimaryContainerDelegate() {
        Container defCont = (Container) metaContainer.getBeanInstance();
        if (primaryContainerDelegate == null || primaryContainer != defCont) {
            primaryContainer = defCont;
            primaryContainerDelegate =
                metaContainer.getContainerDelegate(defCont);
        }
        return primaryContainerDelegate;
    }

    // return initial layout of the primary container delegate
    @Override
    public LayoutManager getDefaultLayoutInstance() {
        return metaContainer.getDefaultLayout();
    }

    // return component instance of meta component
    @Override
    public Component getPrimaryComponent(int index) {
        return (Component) metaContainer.getSubComponent(index).getBeanInstance();
    }

    @Override
    public void updatePrimaryContainer() {
        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        layoutDelegate.clearContainer(cont, contDel);
        layoutDelegate.setLayoutToContainer(cont, contDel);

        RADVisualComponent[] components = metaContainer.getSubComponents();
        if (components.length > 0) {
            Component[] comps = new Component[components.length];
            for (int i=0; i < components.length; i++) {
                comps[i] = (Component) components[i].getBeanInstance();
                ensureFakePeerAttached(comps[i]);
            }

            layoutDelegate.addComponentsToContainer(cont, contDel, comps, 0);
        }
    }

    @Override
    public void containerLayoutChanged(PropertyChangeEvent ev)
        throws PropertyVetoException
    {
        if (ev != null && ev.getPropertyName() != null) {
            layoutDelegate.acceptContainerLayoutChange(getEventWithValues(ev));

            FormModel formModel = metaContainer.getFormModel();
            formModel.fireContainerLayoutChanged(metaContainer,
                                                 ev.getPropertyName(),
                                                 ev.getOldValue(),
                                                 ev.getNewValue());
        }
        else propertySets = null;

        LayoutNode node = metaContainer.getLayoutNodeReference();
        if (node != null) {
            // propagate the change to node
            if (ev != null && ev.getPropertyName() != null)
                node.fireLayoutPropertiesChange();
            else
                node.fireLayoutPropertySetsChange();
        }
    }

    @Override
    public void componentLayoutChanged(int index, PropertyChangeEvent ev)
        throws PropertyVetoException
    {
        RADVisualComponent metacomp = metaContainer.getSubComponent(index);

        if (ev != null && ev.getPropertyName() != null) {
            layoutDelegate.acceptComponentLayoutChange(index,
                                                       getEventWithValues(ev));

            FormModel formModel = metaContainer.getFormModel();
            formModel.fireComponentLayoutChanged(metacomp,
                                                 ev.getPropertyName(),
                                                 ev.getOldValue(),
                                                 ev.getNewValue());

            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().firePropertyChangeHelper(
//                                                     null, null, null);
                                              ev.getPropertyName(),
                                              ev.getOldValue(),
                                              ev.getNewValue());
        }
        else {
            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().fireComponentPropertySetsChange();
            metacomp.resetConstraintsProperties();
        }
    }

    private static PropertyChangeEvent getEventWithValues(PropertyChangeEvent ev) {
        Object oldVal = ev.getOldValue();
        Object newVal = ev.getNewValue();
        if (oldVal instanceof FormProperty.ValueWithEditor)
            ev = new PropertyChangeEvent(
                         ev.getSource(),
                         ev.getPropertyName(),
                         ((FormProperty.ValueWithEditor)oldVal).getValue(),
                         ((FormProperty.ValueWithEditor)newVal).getValue());
        return ev;
    }

    // ---------

    private LayoutListener getLayoutListener() {
        if (layoutListener == null)
            layoutListener = new LayoutListener();
        return layoutListener;
    }

    private class LayoutListener implements VetoableChangeListener,
                                            PropertyChangeListener
    {
        @Override
        public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException
        {
            Object source = ev.getSource();
            String eventName = ev.getPropertyName();
            if (source instanceof FormProperty
                && (FormProperty.PROP_VALUE.equals(eventName)
                    || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)))
            {
                ev = new PropertyChangeEvent(layoutDelegate,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                containerLayoutChanged(ev);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                ev = new PropertyChangeEvent(layoutDelegate,
                                             null, null, null);
                try {
                    containerLayoutChanged(ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }
    }

    private static void ensureFakePeerAttached(Component comp) {
        // This method is called for components to be added to a container.
        // It might happen that the component is still in another container
        // (by error) and then when removed from this container before adding
        // to the new one, the peer would be null-ed. Trying to prevent this by
        // removing the component before attaching the fake peer. (For bug 115431.)
        if (comp != null && comp.getParent() != null) {
            comp.getParent().remove(comp);
        }
        FakePeerSupport.attachFakePeer(comp);
        if (comp instanceof Container)
            FakePeerSupport.attachFakePeerRecursively((Container)comp);
    }
}
