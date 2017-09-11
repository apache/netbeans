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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.modules.settings.convertors;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public class FooSetting {
    private final static String PROP_PROPERTY1 = "property1";
    private final static String PROP_NAME = "name";

    /** Holds value of property property1. */
    private String property1;

    /** Holds value of property name. */
    private String name = "defaultName";

    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);

    private int listenerCount = 0;
    
    /** Creates a new instance of FooSetting */
    public FooSetting() {
    }
    public FooSetting(String txt) {
        this.property1 = txt;
    }
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
        listenerCount++;
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
        listenerCount--;
    }
    
    int getListenerCount() {
        return listenerCount;
    }

    /** Getter for property property1.
     * @return Value of property property1.
     */
    public String getProperty1() {
        return this.property1;
    }
    
    /** Setter for property property1.
     * @param property1 New value of property property1.
     */
    public void setProperty1(String property1) {
        String oldProperty1 = this.property1;
        this.property1 = property1;
        propertyChangeSupport.firePropertyChange(PROP_PROPERTY1, oldProperty1, property1);
    }
    
    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, name);
    }
    
    private void readProperties(Properties p) {
        property1 = p.getProperty(PROP_PROPERTY1);
        String _name = p.getProperty(PROP_NAME);
        if (_name != null) name = _name;
    }
    
    private void writeProperties(Properties p) {
        if (property1 != null) {
            p.setProperty(PROP_PROPERTY1, property1);
        }
        if (name != null) {
            p.setProperty(PROP_NAME, name);
        }
    }
    
    public String toString() {
        return this.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(this)) +
            '[' + property1 + ", " + name + ']';
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FooSetting)) return false;
        FooSetting foo = (FooSetting) obj;
        if (property1 == null || foo.property1 == null) {
            return property1 == foo.property1;
        }
        return property1.equals(foo.property1);
    }
    
}
