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
/*
 * ExtWebBrowserBeanInfoTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import org.netbeans.junit.*;
import java.beans.*;

/**
 *
 * @author rk109395
 */
public class ExtWebBrowserBeanInfoTest extends NbTestCase {

    public ExtWebBrowserBeanInfoTest (java.lang.String testName) {
        super(testName);
    }

    /** Test of getBeanDescriptor method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetBeanDescriptor () {
        if (testObject.getBeanDescriptor () == null)
            fail ("ExtWebBrowserBeanInfo.getBeanDescriptor () returned <null>.");
    }
    
    /** Test of getPropertyDescriptors method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetPropertyDescriptors () {
        if (testObject.getPropertyDescriptors () == null)
            fail ("ExtWebBrowserBeanInfo.getPropertyDescriptors () returned <null>.");
    }
    
    /** Test of getIcon method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetIcon () {
        if (testObject.getIcon (BeanInfo.ICON_COLOR_32x32) == null)
            fail ("ExtWebBrowserBeanInfo.getIcon (BeanInfo.ICON_COLOR_32x32) returned <null>.");
    }
    
    protected BeanInfo testObject;
    
    protected void setUp () {
        testObject = new ExtWebBrowserBeanInfo ();
    }

}
