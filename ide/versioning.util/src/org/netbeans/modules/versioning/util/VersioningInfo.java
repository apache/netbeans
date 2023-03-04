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

package org.netbeans.modules.versioning.util;

import java.awt.Dialog;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Shows a dialog listing all given versioning info properties
 * @author ondra
 */
public class VersioningInfo {

    /**
     * Shows a dialog listing all given versioning info properties.
     * @param properties
     */
    public static void show (HashMap<File, Map<String, String>> properties) {
        PropertySheet ps = new PropertySheet();
        ps.setNodes(new VersioningInfoNode[] {new VersioningInfoNode(properties)});
        DialogDescriptor dd = new DialogDescriptor(ps, NbBundle.getMessage(VersioningInfo.class, "MSG_VersioningInfo_title"), //NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.addWindowListener(new DialogBoundsPreserver(NbPreferences.forModule(VersioningInfo.class), "versioning.util.versioningInfo")); //NOI18N
        dialog.setVisible(true);
    }

    private static class VersioningInfoNode extends AbstractNode {
        private final HashMap<File, Map<String, String>> properties;
        private final String name;
        private final String desc;

        public VersioningInfoNode (HashMap<File, Map<String, String>> properties) {
            super(Children.LEAF);
            this.properties = properties;
            this.name = NbBundle.getMessage(VersioningInfo.class, "MSG_VersioningInfoNode_name"); //NOI18N
            this.desc = NbBundle.getMessage(VersioningInfo.class, "MSG_VersioningInfoNode_desc"); //NOI18N
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = new Sheet();

            for (Map.Entry<File, Map<String, String>> e : properties.entrySet()) {
                Map<String, String> fileProps = e.getValue();
                Sheet.Set ps = new Sheet.Set();
                ps.setName(e.getKey().getAbsolutePath());
                ps.setDisplayName(e.getKey().getName());
                ps.setShortDescription(e.getKey().getAbsolutePath());
                Property[] props = new Property[fileProps.size()];
                int i = 0;
                for (Map.Entry<String, String> prop : fileProps.entrySet()) {
                    String value = prop.getValue();
                    if (value == null) {
                        Logger.getLogger(VersioningInfo.class.getName()).log(Level.INFO, "null value for property {0}", prop.getKey()); //NOI18N
                        value = NbBundle.getMessage(VersioningInfo.class, "MSG_VersioningInfoNode_unknownvalue"); //NOI18N
                    }
                    props[i++] = new VersioningInfoProperty(prop.getKey(), value);
                }
                ps.put(props);
                sheet.put(ps);
            }
            return sheet;
        }

        @Override
        public String getName () {
            return name;
        }

        @Override
        public String getShortDescription() {
            return desc;
        }
    }

    private static class VersioningInfoProperty extends PropertySupport.ReadOnly<String> {
        private final String value;

        public VersioningInfoProperty (String name, String value) {
            super(name, String.class, name, value);
            this.value = value;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }
    }

}
