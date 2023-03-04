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
