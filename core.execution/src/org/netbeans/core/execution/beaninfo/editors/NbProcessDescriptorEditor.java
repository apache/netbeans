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


package org.netbeans.core.execution.beaninfo.editors;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import org.openide.execution.NbProcessDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/**
 * A property editor for <code>NbProcessDescriptor</code>.
 *
 * @author  Ian Formanek
 */
public class NbProcessDescriptorEditor extends Object implements ExPropertyEditor {
    private PropertyEnv env;

    /** <code>NbProcessDescriptor</code> to custmize. */
    NbProcessDescriptor pd;
    /** Property support, helper instance. */
    private PropertyChangeSupport support;

    
    /** Creates property editor. */
    public NbProcessDescriptorEditor() {
        support = new PropertyChangeSupport (this);
    }

    
    /** Gets value. Implements <code>PropertyEditor</code> interface. */
    public Object getValue () {
        return pd;
    }

    /** Sets value. Implements <code>PropertyEditor</code> interface. */
    public void setValue (Object value) {
        pd = (NbProcessDescriptor) value;
        support.firePropertyChange("", null, null); // NOI18N
    }

    /** Gets value as text. Implements <code>PropertyEditor</code> interface. */
    public String getAsText () {
        if ( pd == null )
            return "null";        // NOI18N
        return pd.getProcessName () + " " + pd.getArguments (); // NOI18N
    }

    /** Sets value as text. Implemetns <code>ProepertyEditor</code> interface. */
    public void setAsText(String string) {
        string = string.trim ();

        int indx = string.indexOf(' ');
        
        String prg;
        String args;
        
        // Fix #13186. If the string represents path
        // with directories containing white spaces don't separate them to args.
        if(indx == -1 || new File(string).exists()) {
            prg = string;
            args = ""; // NOI18N
        } else {
            prg = string.substring(0, indx);
            args = string.substring(indx + 1);
        }

        NbProcessDescriptor newPD = null;
        if ( pd == null )
            newPD = new NbProcessDescriptor (
                        prg,
                        args
                    );
        else
            newPD = new NbProcessDescriptor (
                        prg,
                        args,
                        pd.getInfo()
                    );
        
        setValue(newPD);
    }

    /** Gets java initialization string. Implements <code>PropertyEditor</code>
     * interface.
     * @return <code>null</code> */
    public String getJavaInitializationString () {
        return null; // no code generation
    }

    /** Gets tags. Implements <code>PropertyEditor</code> interface. 
     * @return <code>null</code> */
    public String[] getTags () {
        return null;
    }

    /** Indicates wheter this editor paints itself the value. Implements
     * <code>PropertyEditor</code> interface. 
     * @return <code>null</code> */
    public boolean isPaintable () {
        return false;
    }

    /** Dummy implementation of <code>PropertyEditor</code> interface method.
     * @see #isPaintable */
    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    /** Inidicates whether this editor supports custom editing. Implements 
     * <code>PropertyEdtitor</code> interface. 
     * @return <code>true</code> */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** Gets custom editor. Implements <code>PropertyEditor</code> interface.
     * @return <code>NbProcessDescriptorCustomEditor</code> 
     * @see NbProcessDescriptorCustomEditor */
    public Component getCustomEditor () {
        return new NbProcessDescriptorCustomEditor (this, env);
    }

    /** Adds <code>PropertyChangeListener</code>. Implements 
     * <code>PropertyEditor</code> interface. */
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    /** Removes <code>PropertyChangeListner</code>. Implements 
     * <code>PropertyEditor</code> interface. */
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
}
