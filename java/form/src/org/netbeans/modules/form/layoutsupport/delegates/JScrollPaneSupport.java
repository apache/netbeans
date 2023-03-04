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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import javax.swing.*;
import java.lang.reflect.Method;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Dedicated layout support class for JScrollPane.
 *
 * @author Tomas Pavek
 */

public class JScrollPaneSupport extends AbstractLayoutSupport {

    private static Method setViewportViewMethod;

    /** Gets the supported layout manager class - JScrollPane.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return JScrollPane.class;
    }

    /** For dedicated supports: check whether given default container instance
     * is empty. In case of JScrollPane it means to check for empty viewport.
     * @param cont default instance of Container
     * @return true if the container can be used as default (empty) instance
     *         with this layout support
     */
    @Override
    public boolean checkEmptyContainer(Container cont) {
        boolean empty = false;
        if (cont instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)cont;
            JViewport viewport = scrollPane.getViewport();
            empty = (viewport == null) || (viewport.getView() == null);
        }
        return empty;
    }

    /** This method should calculate position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container; not needed
     * @param posInComp position of mouse in the dragged component; not needed
     * @return index corresponding to the position of the component in the
     *         container; we just return 0 here - as the drag&drop does not
     *         have much sense in JScrollPane
     */
    @Override
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        assistantParams = checkEmptyContainer(container);
        return assistantParams ? 0 : -1;
    }

    private boolean assistantParams;
    @Override
    public String getAssistantContext() {
        return assistantParams ? "jscrollPaneLayout" : null; // NOI18N
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for JScrollPane
     * @param newIndex component's index position to be presented; not needed
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    @Override
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (checkEmptyContainer(container))
        {   // empty JScrollPane - it makes sense to add something to it
            Dimension sz = container.getSize();
            Insets insets = container.getInsets();
            sz.width -= insets.left + insets.right;
            sz.height -= insets.top + insets.bottom;

            g.drawRect(0, 0, sz.width, sz.height);
            return true;
        }
        return false;
    }

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (components.length == 0)
            return;

        if (container instanceof JScrollPane)
            ((JScrollPane)container).setViewportView(components[0]);
    }

    /** Removes a real component from a real container.
     * @param container instance of a real container
     * @param containerDelegate effective container delegate of the container
     * @param component component to be removed
     * @return whether it was possible to remove the component (some containers
     *         may not support removing individual components reasonably)
     */
    @Override
    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component)
    {
        return false; // cannot remove component from JScrollPane
    }

    /** Removes all components from given real container.
     * @param container instance of a real container to be cleared
     * @param containerDelegate effective container delegate of the container
     * @return whether it was possible to clear the container (some containers
     *         may not support this)
     */
    @Override
    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {
        if (container instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) container;
            if (scrollPane.getViewport() != null) {
                Component comp = scrollPane.getViewport().getView();
                if (comp != null) {
                    comp.removeNotify();
                    comp.setBounds(0, 0, 0, 0);
                }
            }
            scrollPane.setViewportView(null);
            return true;
        }
        else return super.clearContainer(container, containerDelegate);
    }

    // ------------

    /** This methods returns the code expression to be used for container on
     * which the layout is set and to which components are added. This can be
     * either container, or container delegate expression. In fact, it is
     * container delegate in most cases, but not in case of JScrollPane which
     * has its viewport as the container delegate, but we work with the
     * JScrollPane (whole container).
     * @return code expression representing the effective container
     */
    @Override
    protected CodeExpression getActiveContainerCodeExpression() {
        return getLayoutContext().getContainerCodeExpression();
    }

    /** This method is used for scanning code structures and recognizing
     * components added to containers and their constraints. It's called from
     * initialize method. When a relevant code statement is found, then the
     * CodeExpression of component is get and added to component, and also the
     * layout constraints information is read (using separate
     * readConstraintsCode method).
     * @param statement CodeStatement to be tested if it contains relevant code
     * @param componentCode CodeGroup to be filled with all component code
     * @return CodeExpression representing found component; null if the
     *         statement is not relevant
     */
    @Override
    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        if (getSetViewportViewMethod().equals(statement.getMetaObject())
            || getSimpleAddMethod().equals(statement.getMetaObject()))
        {
            componentCode.addStatement(statement);
            getConstraintsList().add(null); // no constraints
            return statement.getStatementParameters()[0];
        }

        return null;
    }

    /** Creates code for a component added to the layout (opposite to
     * readComponentCode method).
     * @param componentCode CodeGroup to be filled with complete component code
     *        (code for initializing the layout constraints and adding the
     *        component to the layout)
     * @param componentExpression CodeExpression object representing component
     * @param index position of the component in the layout
     */
    @Override
    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression componentExpression,
                                       int index)
    {
        CodeStatement addStatement = CodeStructure.createStatement(
                         getLayoutContext().getContainerCodeExpression(),
                         getSetViewportViewMethod(),
                         new CodeExpression[] { componentExpression });
        componentCode.addStatement(addStatement);
    }

    private static Method getSetViewportViewMethod() {
        if (setViewportViewMethod == null) {
            try {
                setViewportViewMethod = JScrollPane.class.getMethod(
                                            "setViewportView", // NOI18N
                                            new Class[] { Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setViewportViewMethod;
    }
}
