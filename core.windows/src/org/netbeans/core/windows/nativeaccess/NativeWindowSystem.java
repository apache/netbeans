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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.windows.nativeaccess;

import java.awt.Frame;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.Icon;
import org.openide.util.Lookup;

/**
 * Support for access to native, window-related routines in operating system.
 * 
 * @author S. Aubrecht
 */
public abstract class NativeWindowSystem {
    
    /** Instance of dummy window manager. */
    private static NativeWindowSystem dummyInstance;
    
    /**
     * Singleton.
     * 
     * @return Instance of NativeWindowSystem implementation installed in the system.
     */
    public static final NativeWindowSystem getDefault() {
        NativeWindowSystem wmInstance = Lookup.getDefault().lookup(NativeWindowSystem.class);

        return (wmInstance != null) ? wmInstance : getDummyInstance();
    }

    private static synchronized NativeWindowSystem getDummyInstance() {
        if (dummyInstance == null) {
            dummyInstance = new NoNativeAccessWindowSystem();
        }

        return dummyInstance;
    }
    
    /**
     * Check whether it is possible to make top-level windows transparent.
     * 
     * @return True if top-level windows (Frame, Window, Dialog) can be transparent.
     */
    public abstract boolean isWindowAlphaSupported();
    
    /**
     * Check whether it is possible to make top-level UNDECORATED windows transparent.
     * @return  True if top-level undecorated windows (Frame, Window, Dialog) can be transparent.
     * @see Frame#isUndecorated() 
     * @since 2.69
     */
    public abstract boolean isUndecoratedWindowAlphaSupported();
    
    /**
     * Adjust transparency of the given window.
     * 
     * @param w Window, cannot be null.
     * @param alpha Desired transparency, 1.0f - window is fully opaque, 
     * 0.0f - window is fully transparent.
     */
    public abstract void setWindowAlpha( Window w, float alpha );
    
    /**
     *  Adjust the shape of the given window.
     * @param w Window, cannot be null.
     * @param mask New window shape.
     */
    public abstract void setWindowMask( Window w, Shape mask );
    
    /**
     *  Adjust the shape of the given window.
     * @param w Window, cannot be null.
     * @param mask New window shape.
     */
    public abstract void setWindowMask( Window w, Icon mask );
}
