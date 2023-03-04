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

package org.netbeans.modules.editor;

import java.beans.*;
import java.awt.Image;
import java.lang.reflect.Method;
import org.netbeans.editor.LocaleSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/** BeanInfo for the FormatterIndentEngine class
*
* @author Miloslav Metelka
*/
public abstract class FormatterIndentEngineBeanInfo extends SimpleBeanInfo {

    /** Prefix of the icon location. */
    private String iconPrefix;

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    private PropertyDescriptor[] propertyDescriptors;

    private String[] propertyNames;

    public FormatterIndentEngineBeanInfo() {
        this(null);
    }

    public FormatterIndentEngineBeanInfo(String iconPrefix) {
        this.iconPrefix = iconPrefix;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            String[] propNames = getPropertyNames();
            PropertyDescriptor[] pds = new PropertyDescriptor[propNames.length];

            for (int i = 0; i < propNames.length; i++) {
                pds[i] = createPropertyDescriptor(propNames[i]);
                // Set display-name and short-description
                pds[i].setDisplayName(getString("PROP_indentEngine_" + propNames[i])); // NOI18N
                pds[i].setShortDescription(getString("HINT_indentEngine_" + propNames[i])); // NOI18N

            }

            propertyDescriptors = pds; // now the array are inited

            // Now various properties of the descriptors can be updated
            updatePropertyDescriptors();
        }

        return propertyDescriptors;
    }

    /** Create property descriptor for a particular property-name. */
    protected PropertyDescriptor createPropertyDescriptor(String propName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(propName, getBeanClass());

        } catch (IntrospectionException e) {
            try {
                // Create property without read/write methods
                pd = new PropertyDescriptor(propName, null, null);
            } catch (IntrospectionException e2) {
                throw new IllegalStateException("Invalid property name=" + propName); // NOI18N
            }

            // Try a simple search for get/set methods - just by name
            // Successor can customize it if necessary
            String cap = capitalize(propName);
            Method m = findMethod("get" + cap); // NOI18N
            if (m != null) {
                try {
                    pd.setReadMethod(m);
                } catch (IntrospectionException e2) {
                }
            }
            m = findMethod("set" + cap); // NOI18N
            if (m != null) {
                try {
                    pd.setWriteMethod(m);
                } catch (IntrospectionException e2) {
                }
            }
        }

        return pd;
    }

    protected void updatePropertyDescriptors() {
    }

    private Method findMethod(String name) {
        try {
            Method[] ma = getBeanClass().getDeclaredMethods();
            for (int i = 0; i < ma.length; i++) {
                if (name.equals(ma[i].getName())) {
                    return ma[i];
                }
            }
        } catch (SecurityException e) {
        }
        return null;
    }

    private static String capitalize(String s) {
	if (s.length() == 0) {
 	    return s;
	}
	char chars[] = s.toCharArray();
	chars[0] = Character.toUpperCase(chars[0]);
	return new String(chars);
    }

    protected abstract Class getBeanClass();

    protected String[] getPropertyNames() {
        if (propertyNames == null) {
            propertyNames = createPropertyNames();
        }
        return propertyNames;
    }

    protected String[] createPropertyNames() {
        return new String[] {
            FormatterIndentEngine.EXPAND_TABS_PROP,
            FormatterIndentEngine.SPACES_PER_TAB_PROP
        };
    }

    protected PropertyDescriptor getPropertyDescriptor(String propertyName) {
        String[] propNames = getPropertyNames();
        for (int i = 0; i < propNames.length; i++) {
            if (propertyName.equals(propNames[i])) {
                return getPropertyDescriptors()[i];
            }
        }
        return null;
    }

    protected void setPropertyEditor(String propertyName, Class propertyEditor) {
        PropertyDescriptor pd = getPropertyDescriptor(propertyName);
        if (pd != null) {
            pd.setPropertyEditorClass(propertyEditor);
        }
    }

    protected void setExpert(String[] expertPropertyNames) {
        for (int i = 0; i < expertPropertyNames.length; i++) {
            PropertyDescriptor pd = getPropertyDescriptor(expertPropertyNames[i]);
            if (pd != null) {
                pd.setExpert(true);
            }
        }
    }

    protected void setHidden(String[] hiddenPropertyNames) {
        for (int i = 0; i < hiddenPropertyNames.length; i++) {
            PropertyDescriptor pd = getPropertyDescriptor(hiddenPropertyNames[i]);
            if (pd != null) {
                pd.setHidden(true);
            }
        }
    }
    
    private String getValidIconPrefix() {
        return (iconPrefix != null) ? iconPrefix
            : "org/netbeans/modules/editor/resources/indentEngine"; // NOI18N
    }
    
    private Image getDefaultIcon(String iconResource){
        return ImageUtilities.loadImage(iconResource);
    }

    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = loadImage(getValidIconPrefix() + ".gif"); // NOI18N
            }
            return (icon != null) ? icon : getDefaultIcon(getValidIconPrefix() + ".gif"); // NOI18N

        } else {
            if (icon32 == null) {
                icon32 = loadImage(getValidIconPrefix() + "32.gif"); // NOI18N
            }
            return (icon32 != null) ? icon32 : getDefaultIcon(getValidIconPrefix() + "32.gif"); // NOI18N
        }
    }

    /** 
     * Get the localized string. This method must be overriden
     * in children if they add new properties or other stuff
     * that needs to be localized.
     * @param key key to find in a bundle
     * @return localized string
     */
    protected String getString(String key) {
        return LocaleSupport.getString( key );
    }

}

