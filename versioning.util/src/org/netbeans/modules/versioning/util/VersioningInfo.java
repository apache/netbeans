/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
