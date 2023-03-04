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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import org.openide.nodes.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Main interface for working with various layouts of visual containers
 * in Form Editor. An implementations of this interface should hold some
 * metadata representing the layout, be able to set up live components and
 * containers (according to the metadata), handle code generation and
 * persistence, provide drag&drop and resizing support, etc.
 * This interface is very general, it is not recommended to implement it
 * directly. There is a default implementation - AbstractLayoutSupport - which
 * provides most of the necessary functionality, assuming that the supported
 * layout works with certain patterns and rules.
 *
 * @see LayoutConstraints
 * @see LayoutSupportContext
 *
 * @author Tomas Pavek
 */

public interface LayoutSupportDelegate {

    /** Bit flag indicating possible component resizing in upper direction. */
    final int RESIZE_UP = 1;
    /** Bit flag indicating possible component resizing in down direction. */
    final int RESIZE_DOWN = 2;
    /** Bit flag indicating possible component resizing in left direction. */
    final int RESIZE_LEFT = 4;
    /** Bit flag indicating possible component resizing in right direction. */
    final int RESIZE_RIGHT = 8;

    /** Initialization of the layout delegate before the first use.
     * There are three types of initialization which must be supported:
     * (1) default initialization for an empty (newly created) layout
     *    (lmInstance == null, fromCode == false),
     * (2) initialization from an already existing instance of LayoutManager
     *    (lmInstance != null, fromCode == false),
     * (3) initialization from persistent code structure,
     *    (lmInstance == null, fromCode == true).
     * @param layoutContext provides a necessary context information for the
     *                      layout delegate
     * @param lmInstance LayoutManager instance for initialization (may be null)
     * @param fromCode indicates whether to initialize from code structure
     * @exception any Exception occurred during initialization
     */
    void initialize(LayoutSupportContext layoutContext,
                    LayoutManager lmInstance,
                    boolean fromCode)
        throws Exception;

    /** Gets the supported layout manager or container class. Container class
     * is returned if the delegate is "dedicated" to some special container
     * rather than to a layout manager used generally for any container.
     * @return the class supported by this delegate
     * @see isDedicated method
     */
    Class getSupportedClass();

    /** States whether this delegate class is dedicted to some special
     * container layout.
     * @return true if the delegates supports just certain container only,
     *         false if the delegates supports a layout manager for use in
     *               any container
     * @see getSupportedClass method
     */
    boolean isDedicated();

    /** For dedicated supports: check whether given default container instance
     * is empty.
     * @param cont default instance of Container
     * @return true if the container can be used as default (empty) instance
     *         with this layout support
     */
    boolean checkEmptyContainer(Container cont);

    /** Indicates whether the layout should be presented as a node in Component
     * Inspector (for setting properties). The node is provided for layout
     * managers typically (except null layou), and not for dedicated containers
     * support.
     * @return whether a node should be created for the layout
     */
    boolean shouldHaveNode();

    /** Provides a localized display name for the layout node (to be used in
     * Component Inspector and Palette).
     * @return display name of supported layout
     */
    String getDisplayName();

    /** Provides an icon to be used for the layout node in Component
     * Inspector. Only 16x16 color icon is required.
     * @param type is one of BeanInfo constants: ICON_COLOR_16x16,
     *        ICON_COLOR_32x32, ICON_MONO_16x16, ICON_MONO_32x32
     * @return icon to be displayed for node in Component Inspector,
     *         null if no icon is provided
     */
    Image getIcon(int type);

    /** This method returns properties of the supported layout (so of some
     * LayoutManager implementation class typically). These properties are
     * editable by the user in Component Inspector when layout node is
     * selected. These are not properties of individual component constraints
     * (see LayoutConstraints.getProperties() for that).
     * @return properties of supported layout
     */
    Node.PropertySet[] getPropertySets();

    /** Returns a class of a customizer for the layout manager being used as
     * a JavaBean. The class should be a java.awt.Component and
     * java.beans.Customizer. Such a customizer is usually provided with the
     * layout bean itself, specified in BeanInfo class. When the customizer is
     * to be used, it is instantiated and given the reference layout manager
     * instance (using Customizer.setObject(...) method).
     * Note: If the layout delegate provides special customizer from
     * getSupportCustomizer() method, it should still return its class here so
     * it is apparent that there is some customizer provided.
     * @return layout customizer class, null if no customizer is provided
     */
    Class getCustomizerClass();

    /** Returns an instance of a special customizer provided by the layout
     * delegate. This customizer need not implement java.beans.Customizer,
     * because its creation is under full control of the layout delegate - and
     * vice versa, the customizer can have full control over the layout
     * delegate (unlike the bean customizer which operates only with layout
     * manager bean instance).
     * Note: If the layout delegate provides the customizer here, the class of
     * the customizer should be also returned from getCustomizerClass() method.
     * @return instance of layout support customizer
     */
    Component getSupportCustomizer();

    /** Gets the complete code for setting up the layout (including adding
     * components).
     * @return whole container's layout code
     */
    CodeGroup getLayoutCode();

    /** Gets code for setting up one component's constraints and adding the
     * component to the layout (container).
     * @return one component's layout code
     */
    CodeGroup getComponentCode(int index);

    /** Gets CodeExpression object representing one component.
     * @param index index of the component in the layout
     * @return CodeExpression for a component
     */
    CodeExpression getComponentCodeExpression(int index);

    /** Gets number of components in the layout.
     * @return number of components in the layout
     */
    int getComponentCount();

    /** This method is called to accept new components before they are added
     * to the layout (by calling addComponents method). It may adjust the
     * constraints, or refuse the components by throwing a RuntimeException
     * (e.g. IllegalArgumentException). It's up to the delagate to display an
     * error or warning message, the exception is not reported outside.
     * To accept any components simply do nothing here.
     * @param compExpressions array of CodeExpression objects representing the
     *        components to be accepted
     * @param constraints array of layout constraints of the components, may
     *        contain nulls
     * @param index position at which the components are to be added (inserted);
     *        -1 means that the components will be added at the end
     * @exception RunTimeException to refuse components
     */
    void acceptNewComponents(CodeExpression[] compExpressions,
                             LayoutConstraints[] constraints,
                             int index);

    /** This method is called after a property of the layout is changed by
     * the user. The delagate implementation may check whether the layout is
     * valid after the change and throw PropertyVetoException if the change
     * should be reverted. It's up to the delagate to display an error or
     * warning message, the exception is not reported outside. To accept any
     * changes simply do nothing here.
     * @param ev PropertyChangeEvent object describing the change
     */
    void acceptContainerLayoutChange(PropertyChangeEvent ev)
        throws PropertyVetoException;

    /** This method is called after a constraint property of some component
     * is changed by the user. The delegate implementation may check if the
     * layout is valid after the change and throw PropertyVetoException if the
     * change should be reverted. It's up to the delagate to display an error
     * or warning message, the exception is not reported outside. To accept
     * any changes simply do nothing here.
     * @param index index of the component in the layout
     * @param ev PropertyChangeEvent object describing the change
     */
    void acceptComponentLayoutChange(int index, PropertyChangeEvent ev)
        throws PropertyVetoException;

    /** Adds new components to the layout. (This is intended just at the
     * metadata level, no real components are added in fact.)
     * @param compExpressions array of CodeExpression objects representing the
     *        components to be added
     * @param constraints array of layout constraints of the components, may
     *        contain nulls
     * @param index position at which the components should be added (inserted);
     *        if -1, the components should be added at the end
     */
    void addComponents(CodeExpression[] compExpressions,
                       LayoutConstraints[] constraints,
                       int index);

    /** Removes one component from the layout (at metadata level).
     * @param index index of the component in the layout
     */
    void removeComponent(int index);

    /** Removes all components from the layout (at metadata level).
     */
    void removeAll();

    /** Indicates whether there's some change in the layout in comparison
     * with the default layout of given container. If there's no change, no
     * code needs to be delegate (e.g. default FlowLayout in JPanel).
     * Note this is related to the container layout only, not to components.
     * @param defaultContainer instance of the default container to compare with
     * @param defaultContainerDelegate effective container delegate of the
     *        default container (e.g. like content pane of JFrame)
     * @return whether the current layout is different from the default one
     */
    boolean isLayoutChanged(Container defaultContainer,
                            Container defaultContainerDelegate);

    /** Gets layout constraints for a component at the given index.
     * @param index index of the component in the layout
     * @return layout constraints of given component
     */
    LayoutConstraints getConstraints(int index);

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). This method needs to do nothing if there's no
     * reasonable conversion possible (addComponents method receives null
     * constraints then).
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    void convertConstraints(LayoutConstraints[] previousConstraints,
                            LayoutConstraints[] currentConstraints,
                            Component[] components);

    /** Sets up the layout (without adding components) on a real container,
     * according to the internal metadata representation.
     * @param container instance of a real container to be set
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     */
    void setLayoutToContainer(Container container,
                              Container containerDelegate);

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    void addComponentsToContainer(Container container,
                                  Container containerDelegate,
                                  Component[] components,
                                  int index);

    /** Removes a real component from a real container.
     * @param container instance of a real container
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component component to be removed
     * @return whether it was possible to remove the component (some containers
     *         may not support removing individual components reasonably)
     */
    boolean removeComponentFromContainer(Container container,
                                         Container containerDelegate,
                                         Component component);

    /** Removes all components from given real container.
     * @param container instance of a real container to be cleared
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @return whether it was possible to clear the container (some containers
     *         may not support this)
     */
    boolean clearContainer(Container container, Container containerDelegate);

    /** This method is called when user clicks on the container in form
     * designer. The layout delegate may do something with the container,
     * e.g. for JTabbedPane it might switch the selected TAB.
     * @param p Point of click in the container
     * @param real instance of the container when the click occurred
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     */
    void processMouseClick(Point p,
                           Container container,
                           Container containerDelegate);

    /** This method is called when a component is selected in Component
     * Inspector. If the layout delegate is interested in such information,
     * it should store it and use it e.g. in arrangeContainer method.
     * @param index position (index) of the selected component in container
     */
    void selectComponent(int index);

    /** In this method, the layout delegate has a chance to "arrange" real
     * container instance additionally - some other way that cannot be
     * done through layout properties and added components. For example, the
     * selected component index can be applied here (see delegates for
     * CardLayout and JTabbedPane).
     * @param container instance of a real container to be arranged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     */
    void arrangeContainer(Container container, Container containerDelegate);

    /** This method should calculate layout constraints for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component). This method is useful for "constraints oriented"
     * layout managers (like e.g. BorderLayout or GridBagLayout).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component the real component being dragged, can be null
     * @param index position (index) of the component in its container;
     *        -1 if there's no dragged component
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component; null if
     *        there's no dragged component
     * @return new LayoutConstraints object corresponding to the position of
     *         the component in the container; may return null if the layout
     *         does not use component constraints, or if default constraints
     *         should be used
     */
    LayoutConstraints getNewConstraints(Container container,
                                        Container containerDelegate,
                                        Component component,
                                        int index,
                                        Point posInCont,
                                        Point posInComp);

    /** This method should calculate position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component). This method is useful for layout managers that
     * don't use component constraints (like e.g. FlowLayout or GridLayout)
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component the real component being dragged, can be null
     * @param index position (index) of the component in its container;
     *        -1 if there's no dragged component
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component; null if
     *        there's no dragged component
     * @return index corresponding to the position of the component in the
     *         container; may return -1 if the layout rather uses component
     *         constraints, or if a default index should be used
     */
    int getNewIndex(Container container,
                    Container containerDelegate,
                    Component component,
                    int index,
                    Point posInCont,
                    Point posInComp);

    /** This method should paint a feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component). In principle, it should present given component
     * layout constraints or index graphically.
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame) - here the feedback is painted
     * @param component the real component being dragged, can be null
     * @param newConstraints component layout constraints to be presented
     * @param newIndex component's index position to be presented
     *        (if newConstraints == null)
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (may return false if the
     *         constraints or index are invalid, or if the painting is not
     *         implemented)
     */
    boolean paintDragFeedback(Container container, 
                              Container containerDelegate,
                              Component component,
                              LayoutConstraints newConstraints,
                              int newIndex,
                              Graphics g);

    /** Provides resizing options for given component. It can combine the
     * bit-flag constants RESIZE_UP, RESIZE_DOWN, RESIZE_LEFT, RESIZE_RIGHT.
     * @param container instance of a real container in which the
     *        component is to be resized
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component real component to be resized
     * @param index position of the component in its container
     * @return resizing options for the component; 0 if no resizing is possible
     */
    int getResizableDirections(Container container,
                               Container containerDelegate,
                               Component component,
                               int index);

    /** This method should calculate layout constraints for a component being
     * resized.
     * @param container instance of a real container in which the
     *        component is resized
     * @param containerDelegate effective container delegate of the container
     *        (e.g. like content pane of JFrame)
     * @param component real component being resized
     * @param index position of the component in its container
     * @param originalBounds original bounds of the resized component.
     * @param sizeChanges Insets object with size differences
     * @param posInCont position of mouse in the container delegate
     * @return component layout constraints for resized component; null if
     *         resizing is not possible or not implemented
     */
    LayoutConstraints getResizedConstraints(Container container,
                                            Container containerDelegate,
                                            Component component,
                                            int index,
                                            Rectangle originalBounds,
                                            Insets sizeChanges,
                                            Point posInCont);

    /** Cloning method - creates a copy of the layout delegate.
     * @param targetContext LayoutSupportContext for the new layout delegate
     * @param compExpressions array of CodeExpression objects representing the
     *        components for the new layout delegate (corresponding to the
     *        current ones)
     * @return cloned layout delegate instance
     */
    LayoutSupportDelegate cloneLayoutSupport(LayoutSupportContext targetContext,
                                             CodeExpression[] targetComponents);
}
