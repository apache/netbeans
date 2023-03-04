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

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.netbeans.modules.form.codestructure.*;

/**
 * Interface providing necessary context information for LayoutSupportDelegate
 * implementation. Its purpose is to "connect" the layout delegate with the
 * rest of the layout support infrastructure (which implements this interface).
 * LayoutSupportDelegate receives an instance of LayoutSupportContext as
 * a parameter of initialize method.
 * Besides providing information, this interface also contains two methods
 * which should be called from the layout delegate to notify the infrastructure
 * about changes: containerLayoutChanged and componentLayoutChanged.
 * Note: these calls need not be handled explicitly if the layout support
 * implementation uses FormProperty subclass for properties (instead of
 * Node.Property only).
 *
 * @author Tomas Pavek
 */

public interface LayoutSupportContext {

    /** Gets the CodeStructure object to be used for reading/creating code of
     * the container layout configuration.
     * @return main CodeStructure object holding code data
     */
    public CodeStructure getCodeStructure();

    /** Gets the code expression of the primary container (reference container
     * instance in form metadata structures).
     * @return CodeExpression of the primary container
     */
    public CodeExpression getContainerCodeExpression();

    /** Gets the code expression of the primary container delegate.
     * #return CodeEpression of primary container delegate.
     */
    public CodeExpression getContainerDelegateCodeExpression();

    /** Gets the primary container. This is the reference instance used in form
     * metadata structures.
     * @return instance of the primary container
     */
    public Container getPrimaryContainer();

    /** Gets the container delegate of the primary container.
     * @return instance of the primary container delegate
     */
    public Container getPrimaryContainerDelegate();

    /** Provides the instance of LayoutManager as it was initially set in the
     * primary container delegate. Makes only sense for general (non-dedicated
     * containers. Should not be changed.
     * @return default (initial) LayoutManager instance
     */
    public LayoutManager getDefaultLayoutInstance();

    /** Gets the primary component (reference instance) on given index in
     * the primary container.
     * @return component on given index in primary container.
     */
    public Component getPrimaryComponent(int index);

    /** This method should be called by the layout delegate if some change
     * requires to update the layout in the primary container completely
     * (remove components, set new layout, add components again). To be used
     * probably only in case the supported layout manager is not a bean
     * (e.g. BoxLayout).
     */
    public void updatePrimaryContainer();

    /** This method should be called by the layout delegate to notify about
     * changing a property of container layout. The infrastructure then calls
     * back the delegate's acceptContainerLayoutChange method which may
     * throw PropertyVetoException to revert the property change.
     */
    public void containerLayoutChanged(PropertyChangeEvent evt)
        throws PropertyVetoException;

    /** This method should be called by the layout delegate to notify about
     * changing a property of component layout constraint. The infrastructure
     * then  calls back the delegate's acceptComponentLayoutChange method which
     * may throw PropertyVetoException to revert the property change.
     */
    public void componentLayoutChanged(int index, PropertyChangeEvent evt)
        throws PropertyVetoException;
}
