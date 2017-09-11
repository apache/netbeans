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

package org.netbeans.core.windows;

import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * Test component with persistence type PERSISTENCE_ALWAYS and it is singleton.
 * 
 * @author  Marek Slama
 *
 */
public class Component00 extends TopComponent {

    static final long serialVersionUID = 6021472310161712674L;

    private static Component00 component = null;
    
    private static final String TC_ID = "component00";
    
    /** 
     * Used to detect if TC instance was created either using deserialization
     * or by getDefault.
     */
    private static boolean deserialized = false;
    
    private Component00 () {
    }

    protected String preferredID () {
        return TC_ID;
    }
    
    /* Singleton accessor. As Component00 is persistent singleton this
     * accessor makes sure that Component00 is deserialized by window system.
     * Uses known unique TopComponent ID "component00" to get Component00 instance
     * from window system. "component00" is name of settings file defined in module layer.
     */
    public static synchronized Component00 findDefault() {
        if (component == null) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            TopComponent tc = WindowManager.getDefault().findTopComponent(TC_ID);
            if (tc != null) {
                if (!(tc instanceof Component00)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + Component00.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    Component00.getDefault();
                }
            } else {
                //This should not happen when settings file is correctly defined in module layer.
                //TestComponent00 cannot be deserialized
                //Fallback to accessor reserved for window system.
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "Cannot deserialize TopComponent for tcID:'" + TC_ID + "'"); // NOI18N
                Component00.getDefault();
            }
        }
        return component;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * TestComponent00 instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of TestComponent00. */
    public static synchronized Component00 getDefault() {
        if (component == null) {
            component = new Component00();
        }
        deserialized = true;
        return component;
    }
    
    /** Overriden to explicitely set persistence type of TestComponent00
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /** Resolve to singleton instance */
    public Object readResolve() throws java.io.ObjectStreamException {
        return Component00.getDefault();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        deserialized = true;
    }

    static void clearRef () {
        component = null;
    }
    
    public static boolean wasDeserialized () {
        return deserialized;
    }
    
}
