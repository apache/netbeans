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
/**
 * JComboBox with auto completion feature.
 *
 * @author marekfukala
 */
package org.netbeans.modules.css.visual;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;

public class AutocompleteJComboBox extends JComboBox {
    
    private static Comparator<String> PROPERTY_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            //sort the vendor spec. props below the common ones
            boolean s1vendor = Properties.isVendorSpecificPropertyName(s1);
            boolean s2vendor = Properties.isVendorSpecificPropertyName(s2);

            if (s1vendor && !s2vendor) {
                return +1;
            } else if (!s1vendor && s2vendor) {
                return -1;
            }
            //delegate to string compare
            return s1.compareTo(s2);
        }
    };

    public AutocompleteJComboBox(FileObject file) {
        super(new DefaultComboBoxModel(getProperties(file)));
    }

    private static String[] getProperties(FileObject file) {
        Collection<String> properties = new TreeSet<String>(PROPERTY_COMPARATOR);
        for (PropertyDefinition pdef : Properties.getPropertyDefinitions(file, true)) {
            properties.add(pdef.getName());
        }
        return properties.toArray(new String[0]);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //make sure the combo list is opened when editing starts
        setPopupVisible( true );
    }
    
}
