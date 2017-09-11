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
    public String getDescritpion () {
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
