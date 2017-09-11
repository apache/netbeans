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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.form.codestructure.CodeGroup;
import org.netbeans.modules.form.layoutsupport.AbstractLayoutSupport;

/**
 * Fake layout support for Swing menus allowing to treat menu components as
 * visual components/containers and generate correct code. Does not support dragging.
 */
public class MenuFakeSupport extends AbstractLayoutSupport {

    @Override
    public Class getSupportedClass() {
        return null;
    }

    @Override
    public boolean isDedicated() {
        return true;
    }

    @Override
    public boolean shouldHaveNode() {
        return false;
    }

    @Override
    protected void readLayoutCode(CodeGroup layoutCode) {
    }

    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        for (int i=0; i < components.length; i++) {
            containerDelegate.add(components[i], i + index);
            // Issue 110587 - the default background of menu depends (on some l&f)
            // on the location of the menu (if it is top-level menu or not).
            // The background is changed when "ancestor" property change event
            // is fired. This usually happens when addNotify() is called.
            // Unfortunately, addNotify() is not called if the parent doesn't
            // have peer - this happens for model instances. The following
            // code fires the property change event explicitly to force
            // update of the background.
            PropertyChangeEvent event = new PropertyChangeEvent(components[i], "ancestor", null, container); // NOI18N
            for (PropertyChangeListener listener : components[i].getPropertyChangeListeners()) {
                listener.propertyChange(event);
            }
        }
    }
}
