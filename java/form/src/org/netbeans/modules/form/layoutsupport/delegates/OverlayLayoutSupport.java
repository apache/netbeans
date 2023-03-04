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

import java.awt.Container;
import java.awt.LayoutManager;
import java.lang.reflect.Constructor;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.openide.util.Exceptions;

/**
 * Support class for OverlayLayout. OverlayLayout is very simple, has no
 * properties, components have no constraints. The only specialty is that it
 * is not a JavaBean - because of Container parameter in constructor (similar to
 * BoxLayout). This whole class practically only takes care of that.
 *
 * @author Tomas Pavek
 */
public class OverlayLayoutSupport extends AbstractLayoutSupport {
    
    /** Gets the supported layout manager class - OverlayLayout.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return OverlayLayout.class;
    }

    /** Sets up the layout (without adding components) on a real container,
     * according to the internal metadata representation. This method must
     * override AbstractLayoutSupport because a new OverlayLayout instance must
     * be created for each container.
     * @param container instance of a real container to be set
     * @param containerDelegate effective container delegate of the container;
     *        for layout managers we always use container delegate instead of
     *        the container
     */
    @Override
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate) {
        containerDelegate.setLayout(cloneLayoutInstance(container,
                                                        containerDelegate));
    }

    /** Creates a default instance of LayoutManager (for internal use).
     * This method must override AbstractLayoutSupport because OverlayLayout
     * is not a bean (so it cannot be created automatically).
     * @return new instance of OverlayLayout
     */
    @Override
    protected LayoutManager createDefaultLayoutInstance() {
        return new OverlayLayout(new JPanel());
    }

    /** Cloning method - creates a clone of the reference LayoutManager
     * instance (for external use). This method must override
     * AbstractLayoutSupport because OverlayLayout is not a bean (so it cannot
     * be cloned automatically).
     * @param container instance of a real container in whose container
     *        delegate the layout manager will be probably used
     * @param containerDelegate effective container delegate of the container
     * @return cloned instance of OverlayLayout
     */
    @Override
    protected LayoutManager cloneLayoutInstance(Container container,
                                                Container containerDelegate) {
        return new OverlayLayout(containerDelegate);
    }

    /** Creates code structures for a new layout manager (opposite to
     * readInitLayoutCode). As OverlayLayout is not a bean, this method must
     * override from AbstractLayoutSupport.
     * @param layoutCode CodeGroup to be filled with relevant
     *        initialization code; not needed here because OverlaLayout is
     *        represented only by a single constructor code expression and
     *        no statements
     * @return new CodeExpression representing the OverlayLayout
     */
    @Override
    protected CodeExpression createInitLayoutCode(CodeGroup layoutCode) {
        CodeStructure codeStructure = getCodeStructure();

        CodeExpression[] params = new CodeExpression[] {
                getLayoutContext().getContainerDelegateCodeExpression()
        };

        return codeStructure.createExpression(getOverlayLayoutConstructor(),
                                              params);
    }

    private static Constructor getOverlayLayoutConstructor() {
        if (overlayLayoutConstructor == null) {
            try {
                overlayLayoutConstructor = OverlayLayout.class.getConstructor(
                                           new Class[] { Container.class });
            } catch (NoSuchMethodException ex) { // should not happen
                Exceptions.printStackTrace(ex);
            }
        }
        return overlayLayoutConstructor;
    }

    private static Constructor overlayLayoutConstructor;
}
