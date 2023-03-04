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
