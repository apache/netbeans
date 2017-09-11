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

package org.netbeans.modules.form.beaninfo.swing;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * A class that provides some support for simplier BeanInfos in this package.
 *
 * @author petr.nejedly@sun.com
 */
abstract class BISupport extends SimpleBeanInfo {

    private PropertyDescriptor[] pds;
    private String icon;
    private Class beanClass;

    protected BISupport(String iconBaseName, Class beanClass) {
        icon = iconBaseName;
        this.beanClass = beanClass;
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(beanClass);
    }

    @Override
    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        if (pds == null) {
            try {
                pds = createPropertyDescriptors();
            } catch (IntrospectionException e) {
                pds = super.getPropertyDescriptors();
            }
        }
        return pds;
    }

    
    @Override
    public Image getIcon(int type) {
        if (type == ICON_COLOR_32x32 || type == ICON_MONO_32x32)
            return ImageUtilities.loadImage("org/netbeans/modules/form/beaninfo/swing/" + icon + "32.gif"); // NOI18N
        else
            return ImageUtilities.loadImage("org/netbeans/modules/form/beaninfo/swing/" + icon + ".gif"); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        return new PropertyDescriptor[0];
    }
    
    protected String getString(String key) {
        return NbBundle.getMessage(BISupport.class, key);
    }
    
    protected PropertyDescriptor createRW( Class beanClass, String name)
                                            throws IntrospectionException {
        String title = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        PropertyDescriptor pd = new PropertyDescriptor(name, beanClass,
                "get" + title, "set" + title ); // NOI18N
        
        setProps(pd, beanClass, title);
        return pd;
    }

    protected PropertyDescriptor createRO( Class beanClass, String name)
                                            throws IntrospectionException {
        String title = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        PropertyDescriptor pd = new PropertyDescriptor(name, beanClass,
                "get" + title, null); // NOI18N
        
        setProps(pd, beanClass, title);
        return pd;
    }

    private void setProps( PropertyDescriptor pd, Class beanClass, String title ) {
        String className = beanClass.getName();
        int dotIdx = className.lastIndexOf('.');
        className = dotIdx > 0 ? className.substring(dotIdx+1) : className;
        pd.setDisplayName(getString("PROP_" + className + '.' + title)); // NOI18N
        pd.setShortDescription(getString("HINT_" + className + '.' + title)); // NOI18N
    }



    static class TaggedPropertyEditor extends PropertyEditorSupport {
        private String[] tags;
        private int[] values;
        private String[] javaInitStrings;
        private String[] tagKeys;

        protected TaggedPropertyEditor( int[] values, String[] javaInitStrings, String[] tagKeys ) {
            this.values = values;
            this.javaInitStrings = javaInitStrings;
            this.tagKeys = tagKeys;
        }
        
        @Override
        public String[] getTags() {
            if (tags == null) createTags();
            return tags;
        }

        @Override
        public String getAsText() {
            Object valObj = getValue();
            if (valObj instanceof Integer) {
                if (tags == null) createTags();

                int value = ((Integer)valObj).intValue();
                for (int i = 0; i < values.length; i++)
                    if (value == values[i])
                        return tags[i];
            }
            return null;
        }

        @Override
        public void setAsText(String str) {
            if (tags == null) createTags();

            int value = -1;

            for (int i = 0; i < tags.length; i++) {
                if (str.equals(tags[i])) {
                    value = values[i];
                    break;
                }
            }

            if (value != -1)
                setValue(new Integer(value));
        }

        @Override
        public String getJavaInitializationString() {
            Object valObj = getValue();
            if (valObj instanceof Integer) {
                int value = ((Integer)valObj).intValue();
                for (int i = 0; i < values.length; i++)
                    if (value == values[i])
                        return javaInitStrings[i];
            }
            return "???"; // NOI18N
        }

        private void createTags() {
            tags = new String[tagKeys.length];
            ResourceBundle bundle = NbBundle.getBundle(BISupport.class);
            for (int i=0; i<tagKeys.length; i++) {
                tags[i] = bundle.getString(tagKeys[i]);
            }
        }
    }
}
