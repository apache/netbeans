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

import java.awt.*;
import java.beans.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * Meta component representing a LayoutManager instance as a JavaBean.
 *
 * @author Tomas Pavek
 */

class MetaLayout extends RADComponent {

    private AbstractLayoutSupport abstLayoutDelegate;

    public MetaLayout(AbstractLayoutSupport layoutDelegate,
                      LayoutManager lmInstance)
    {
        super();

        abstLayoutDelegate = layoutDelegate;

        initialize(((LayoutSupportManager)abstLayoutDelegate.getLayoutContext())
                         .getMetaContainer().getFormModel());

        setBeanInstance(lmInstance);
    }

    @Override
    protected void createCodeExpression() {
        // code expression is handled by the layout support class
    }

    @Override
    protected void createPropertySets(java.util.List<Node.PropertySet> propSets) {
        super.createPropertySets(propSets);

        // RADComponent provides also Code Generation properties for which
        // we have no use here (yet) - so we remove them now
        for (int i=0, n=propSets.size(); i < n; i++) {
            Node.PropertySet propSet = propSets.get(i);
            if (!"properties".equals(propSet.getName()) // NOI18N
                    && !"properties2".equals(propSet.getName())) { // NOI18N
                propSets.remove(i);
                i--;  n--;
            }
        }
    }

    @Override
    public BindingProperty[][] getBindingProperties() {
        // don't even try to find binding properties for a layout manager,
        // would not be used anyway
        return new BindingProperty[][] { new BindingProperty[] {},
                          new BindingProperty[] {}, new BindingProperty[] {} };
    }

    @Override
    protected PropertyChangeListener createPropertyListener() {
        // cannot reuse RADComponent.PropertyListener, because this is not
        // a regular RADComponent (properties have a special meaning)
        return null;
    }

    @Override
    protected BeanInfo createBeanInfo(Class cls) throws IntrospectionException {
        return super.createBeanInfo(abstLayoutDelegate.getSupportedClass());
    }
}
