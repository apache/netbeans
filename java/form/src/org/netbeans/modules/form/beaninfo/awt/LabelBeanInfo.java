/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.beaninfo.awt;

import java.awt.Label;
import java.beans.*;

import org.openide.util.NbBundle;

/** A BeanInfo for java.awt.Label.
*
* @author Ales Novak

*/
public class LabelBeanInfo extends ComponentBeanInfo.Support {

    public LabelBeanInfo() {
        super("label", java.awt.Label.class); // NOI18N
    }

    /** @return Propertydescriptors */
    @Override
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            new PropertyDescriptor("alignment", Label.class), // NOI18N
            new PropertyDescriptor("text", Label.class), // NOI18N
        };
        pds[0].setPropertyEditorClass(LabelBeanInfo.AlignmentPropertyEditor.class);
        return pds;
    }

    public static class AlignmentPropertyEditor extends PropertyEditorSupport {
        String[] tags;

        /** @return tags */
        @Override
        public synchronized String[] getTags() {
            if (tags == null) {
                tags = new String[] {
                    getString("LEFT"),
                    getString("CENTER"),
                    getString("RIGHT")
                };
            }
            return tags;
        }

        @Override
        public void setAsText(String s) {
            Integer i;
            getTags();
            if (s.equals(tags[0])) i = java.awt.Label.LEFT;
            else if (s.equals(tags[1])) i = java.awt.Label.CENTER;
            else i = java.awt.Label.RIGHT;
            setValue(i);
        }

        @Override
        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            getTags();
            return tags[i == java.awt.Label.CENTER ? 1 : (i == java.awt.Label.LEFT ? 0 : 2)];
        }

        @Override
        public String getJavaInitializationString () {
            int i = ((Integer) getValue()).intValue();
            switch (i) {
            case java.awt.Label.RIGHT :  return "java.awt.Label.RIGHT"; // NOI18N
            case java.awt.Label.LEFT :   return "java.awt.Label.LEFT"; // NOI18N
            default:
            case java.awt.Label.CENTER : return "java.awt.Label.CENTER"; // NOI18N
            }
        }
    }

    /** i18n */
    static String getString(String x) {
        return NbBundle.getMessage(LabelBeanInfo.class, x);
    }
}
