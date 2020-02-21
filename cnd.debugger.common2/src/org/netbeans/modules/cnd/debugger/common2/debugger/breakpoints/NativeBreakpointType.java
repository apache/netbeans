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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference; 

import javax.swing.JComponent;

import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;

abstract public class NativeBreakpointType extends BreakpointType {

    private final static String CATEGORY_NAME = "native"; // NOI18N

    /**
     * Factory method.
     */

    public abstract NativeBreakpoint newInstance(int flags);

    private Reference<BreakpointPanel> customizerRef = 
	new WeakReference<BreakpointPanel>(null);

    // interface BreakpointType
    @Override
    public JComponent getCustomizer() {
	BreakpointPanel customizer = getCustomizer(null);
	customizerRef = new WeakReference<BreakpointPanel>(customizer);
	return customizer;
    }
    
    // interface BreakpointType
    @Override
    public Controller getController() {
	// OLD BreakpointPanel customizer = getCustomizer(null);
	BreakpointPanel customizer = customizerRef.get();
	if (customizer != null)
	    return customizer.getController();
	else
	    return null;
    }

    /**
     * The following abstract getCustomizer() is not the same as 
     * BreakpointType.getCustomizer()!
     *
     * If 'bpt' is non-null the returned component is used for editing (i.e.
     * CustomizeBreakpoint actions)
     *
     * If 'bpt' is null the returned component is used for creation of
     * new breakpoints.
     */

    abstract public BreakpointPanel getCustomizer(NativeBreakpoint editableBreakpoint);

    // interface BreakpointType
    @Override
    public final String getCategoryDisplayName() {
	    return CATEGORY_NAME;
    } 

    static boolean isOurs(String category) {
	return CATEGORY_NAME.equals(category);
    }

    /**
     * Only one subclass of ours, FunctionBreakpointType, overrides this
     * to return true;.
     */

    // interface BreakpointType
    @Override
    public boolean isDefault() {
	return false;
    } 
    
    // used in xml
    public abstract String id();
}
