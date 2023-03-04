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

package org.netbeans.core.execution.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import org.openide.execution.NbClassPath;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/** A property editor for NbClassPath.
* @author  Jaroslav Tulach
*/
public class NbClassPathEditor extends Object implements ExPropertyEditor {
    private NbClassPath pd;
    private PropertyChangeSupport support;
    private boolean editable = true;

    public NbClassPathEditor () {
        support = new PropertyChangeSupport (this);
    }

    public Object getValue () {
        return pd;
    }

    public void setValue (Object value) {
        Object old = pd;
        pd = (NbClassPath) value;
        support.firePropertyChange ("value", old, pd); // NOI18N
    }

    public String getAsText () {
        if ( pd != null )
            return pd.getClassPath ();
        else
            return "null"; // NOI18N
    }

    public void setAsText (String string) {
        if ( ! "null".equals( string ) )
            setValue (new NbClassPath (string));
    }

    public String getJavaInitializationString () {
        return "new NbClassPath (" + getAsText () + ")"; // NOI18N
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new NbClassPathCustomEditor (this);
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    /** gets information if the text in editor should be editable or not */
    public boolean isEditable(){
        return editable;
    }
    
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
        }
    }
}
