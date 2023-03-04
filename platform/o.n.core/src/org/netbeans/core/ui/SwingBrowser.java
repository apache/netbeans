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

package org.netbeans.core.ui;

import java.beans.*;
import java.lang.reflect.Constructor;

import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/** Factory and descriptions for default Swing based browser
 */

public class SwingBrowser implements HtmlBrowser.Factory, java.io.Serializable {

    /** Property name */
    public static final String PROP_DESCRIPTION = "description"; // NOI18N

    protected transient PropertyChangeSupport pcs;

    private static final long serialVersionUID = -3735603646171376891L;
    
    /** Creates new Browser */
    public SwingBrowser () {
        init ();
    }

    /** initialize object */
    private void init () {
        pcs = new PropertyChangeSupport (this);
    }

    /** Getter for browser name
     *  @return browserName name of browser
     */
    public String getDescription () {
        return NbBundle.getMessage (SwingBrowser.class, "LBL_SwingBrowserDescription");
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        try {
            Class<?> clz = Class.forName ("org.openide.awt.SwingBrowserImpl"); // NOI18N
            Constructor con = clz.getDeclaredConstructor (new Class [] {});
            con.setAccessible (true);
            return (HtmlBrowser.Impl)con.newInstance (new Object [] {});
        }
        catch (Exception ex) {
            org.openide.DialogDisplayer.getDefault ().notify (
                new NotifyDescriptor.Message (NbBundle.getMessage (SwingBrowser.class, "MSG_cannot_create_browser"))
            );
            return null;
        }
    }
    
    /**
     * @param l new PropertyChangeListener */    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * @param l PropertyChangeListener to be removed */    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    private void readObject (java.io.ObjectInputStream ois) 
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        init ();
    }
}
