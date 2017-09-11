/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
