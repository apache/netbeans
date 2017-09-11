/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
