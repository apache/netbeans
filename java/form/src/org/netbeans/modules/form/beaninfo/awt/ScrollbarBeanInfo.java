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

package org.netbeans.modules.form.beaninfo.awt;

import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import java.awt.Scrollbar;

/** A BeanInfo for java.awt.ScrollBar.
*
* @author Ales Novak
*/
public class ScrollbarBeanInfo extends ComponentBeanInfo.Support {

    public ScrollbarBeanInfo() {
        super("scrollbar", java.awt.Scrollbar.class); // NOI18N
    }

    /** @return Propertydescriptors */
    @Override
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            new PropertyDescriptor("unitIncrement", Scrollbar.class), // NOI18N
            new PropertyDescriptor("minimum", Scrollbar.class), // NOI18N
            new PropertyDescriptor("maximum", Scrollbar.class), // NOI18N
            new PropertyDescriptor("value", Scrollbar.class), // NOI18N
            new PropertyDescriptor("blockIncrement", Scrollbar.class), // NOI18N
            new PropertyDescriptor("orientation", Scrollbar.class), // NOI18N
            new PropertyDescriptor("visibleAmount", Scrollbar.class), // NOI18N
        };
        pds[5].setPropertyEditorClass(ScrollbarBeanInfo.OrientationPropertyEditor.class);
        return pds;
    }

    /** orientation PropertyEditor */
    public static class OrientationPropertyEditor extends PropertyEditorSupport {
        String[] tags;
        
        /** @return tags */
        @Override
        public synchronized String[] getTags() {
            if (tags == null) {
                ResourceBundle rb = NbBundle.getBundle(ScrollbarBeanInfo.class);
                tags = new String[] {
                    rb.getString("HORIZONTAL"),
                    rb.getString("VERTICAL"),
                };
            }
            return tags;
        }

        @Override
        public void setAsText(String s) {
            Integer i;
            getTags();
            if (s.equals(tags[0])) i = new Integer(Scrollbar.HORIZONTAL);
            else i = new Integer(Scrollbar.VERTICAL);
            setValue(i);
        }

        @Override
        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            getTags();
            return i == Scrollbar.VERTICAL ? tags[1] : tags[0];
        }

        @Override
        public String getJavaInitializationString() {
            int i = ((Integer) getValue()).intValue();
            return i == Scrollbar.VERTICAL ? "java.awt.Scrollbar.VERTICAL" : "java.awt.Scrollbar.HORIZONTAL"; // NOI18N
        }
    }
}
