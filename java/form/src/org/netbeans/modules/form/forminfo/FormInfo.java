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

package org.netbeans.modules.form.forminfo;

import java.awt.Container;
import org.openide.nodes.Node;

/** FormInfo is a class which provides information specific to certain form
 * type.  E.g. for a JFrame form, the top-level bean is the JFrame itself, the
 * top-level container is its getContentPane(), and the top-level container
 * generation string is "getContentPane().".
 *
 * @author Ian Formanek
 *
 * @deprecated
 */
@Deprecated
public abstract class FormInfo
{
    /** Constant for empty list of properties */
    public static final Node.Property[] NO_PROPERTIES = new Node.Property[0];

    /** Used to create the design-time instance of the form object, which is used
     * only for displaing properties and events of the form.  I.e. it is not
     * displayed visually, instead the FormTopComponent is used with the
     * container provided from <code>getTopContainer()</code> method.
     * @return the instance of the form
     * @see #getTopContainer
     */
    public abstract Object getFormInstance();

    /** Used to provide the container which is used during design-time as the
     * top-level container.  The container provided by this class should not be a
     * Window, as it is added as a component to the FormTopComponent, rather a
     * JPanel, Panel or JDesktopPane should be used according to the form type.
     * By returning a <code>null</code> value, the form info declares that it
     * does not represent a "visual" form and the visual editing should not be
     * used with it.
     * @return the top level container which will be used during design-time or
     * null if the form is not visual
     */
    public abstract Container getTopContainer();

    /** Used to provide the container which is used during design-time as the
     * top-level container for adding components.  The container provided by this
     * class should not be a Window, as it is added as a component to the
     * FormTopComponent, rather a JPanel, Panel or JDesktopPane should be used
     * according to the form type.  By returning a <code>null</code> value, the
     * form info declares that it does not represent a "visual" form and the
     * visual editing should not be used with it.  The default implementation
     * returns the same value as getTopContainer() method .
     * @return the top level container which will be used during design-time or
     * null if the form is not visual
     */
    public Container getTopAddContainer() {
        return getTopContainer();
    }

    /** By overriding this method, the form info can specify a string which is
     * used to add top-level components - i.e. for java.awt.Frame, the default
     *(empty string) implementation is used, while for javax.swing.JFrame a
     * <code>"getContentPane()."</code> will be returned.
     * @return the String to be used for adding to the top-level container
     * @see #getTopContainer
     */
    public String getContainerGenName() {
        return ""; // NOI18N
    }
}
