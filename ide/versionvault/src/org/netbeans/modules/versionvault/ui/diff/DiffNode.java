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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.ui.diff;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class DiffNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME = "name";
    static final String COLUMN_NAME_STATUS = "status";
    static final String COLUMN_NAME_LOCATION = "location";
        
    private final Setup     setup;
    private String          htmlDisplayName;
    private int             displayStatuses;
    
    public DiffNode(Setup setup, int displayStatuses) {
        super(Children.LEAF, Lookups.singleton(setup));
        this.setup = setup;
        this.displayStatuses = displayStatuses;
        setName(setup.getBaseFile().getName());
        initProperties();
        refreshHtmlDisplayName();
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = setup.getInfo(); 
        htmlDisplayName = Clearcase.getInstance().getAnnotator().annotateNameHtml(setup.getBaseFile().getName(), info, null);
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    public Setup getSetup() {
        return setup;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) return null;
        return new Action [0];
    }
    
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new LocationProperty());
        ps.put(new StatusProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private abstract class DiffNodeProperty extends PropertySupport.ReadOnly<String> {

        protected DiffNodeProperty(String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue();
            } catch (Exception e) {
                Clearcase.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
    }

    private class NameProperty extends DiffNodeProperty {

        public NameProperty() {
            super(DiffNode.COLUMN_NAME_NAME, String.class, DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_NAME);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return DiffNode.this.getName();
        }
    }

    private class LocationProperty extends DiffNodeProperty {
        
        private String location;

        public LocationProperty() {
            super(DiffNode.COLUMN_NAME_LOCATION, String.class, DiffNode.COLUMN_NAME_LOCATION, DiffNode.COLUMN_NAME_LOCATION);
            location = ClearcaseUtils.getLocation(setup.getBaseFile());
            setValue("sortkey", location + "\t" + DiffNode.this.getName()); // NOI18N
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return location;
        }
    }
    
    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N

    private class StatusProperty extends DiffNodeProperty {
        
        public StatusProperty() {
            super(DiffNode.COLUMN_NAME_STATUS, String.class, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_STATUS);
            String shortPath = null;
            shortPath = ClearcaseUtils.getLocation(setup.getBaseFile());
            String sortable = "0";
            setValue("sortkey", DiffNode.zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + DiffNode.this.getName().toUpperCase()); // NOI18N
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            FileInformation finfo =  setup.getInfo();
            return finfo.getStatusText(displayStatuses);            
        }
    }
}
