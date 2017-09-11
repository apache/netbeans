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

public class SystemDefaultBrowserBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor (SystemDefaultBrowser.class);
        descr.setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "CTL_SystemDefaultBrowserName"));
        descr.setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_SystemDefaultBrowserName"));

        descr.setValue ("helpID", "org.netbeans.modules.extbrowser.ExtWebBrowser");  // NOI18N //TODO
	return descr;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        
        try {
            properties = new PropertyDescriptor [] {
                                new PropertyDescriptor(ExtWebBrowser.PROP_BROWSER_EXECUTABLE, SystemDefaultBrowser.class, "getBrowserExecutable", null), // NOI18N
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_ACTIVATE_TIMEOUT, SystemDefaultBrowser.class),
                                new PropertyDescriptor(ExtWebBrowser.PROP_DDE_OPENURL_TIMEOUT, SystemDefaultBrowser.class)
                             };

            properties[0].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_browserExecutable"));
            properties[0].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_browserExecutable"));
            properties[0].setPreferred(true);

            properties[1].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_ACTIVATE_TIMEOUT"));
            properties[1].setExpert(true);
            properties[1].setHidden(true);

            properties[2].setDisplayName (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "PROP_DDE_OPENURL_TIMEOUT"));
            properties[2].setShortDescription (NbBundle.getMessage (SystemDefaultBrowserBeanInfo.class, "HINT_DDE_OPENURL_TIMEOUT"));
            properties[2].setExpert(true);
            properties[2].setHidden(true);

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
        return loadImage("/org/netbeans/modules/extbrowser/resources/extbrowser.png"); // NOI18N
    }
    
}
