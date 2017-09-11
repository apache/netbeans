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

package org.netbeans.modules.extbrowser;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;

public class SimpleExtBrowserBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (SimpleExtBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "CTL_SimpleExtBrowser"));
        descr.setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_SimpleExtBrowser"));
        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.SimpleExtBrowser");  // NOI18N
        return descr;
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        try {
            properties = new PropertyDescriptor [] {
                new PropertyDescriptor (SimpleExtBrowser.PROP_BROWSER_EXECUTABLE, SimpleExtBrowser.class)    // NOI18N
            };
            properties[0].setDisplayName (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SimpleExtBrowserBeanInfo.class, "HINT_browserExecutable"));
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
        return properties;
    }

    /**
    * Returns the icon. 
    */
    @Override
    public Image getIcon (int type) {
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.gif"); // NOI18N
    }
}

