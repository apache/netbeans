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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.subversion.ui.diff;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
public class DiffNode extends AbstractNode {
    
    static final String COLUMN_NAME_NAME = "name";
    static final String COLUMN_NAME_PROPERTY = "property";
    static final String COLUMN_NAME_STATUS = "status";
    static final String COLUMN_NAME_LOCATION = "location";
        
    private final Setup     setup;
    private String          htmlDisplayName;
    private int displayStatuses;
    
    DiffNode(Setup setup, SvnFileNode node, int displayStatuses) {
        super(Children.LEAF, getLookupFor(setup, node.getLookupObjects()));
        this.setup = setup;
        this.displayStatuses = displayStatuses;
        setName(setup.getBaseFile().getName());
        initProperties();
        refreshHtmlDisplayName();
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = setup.getInfo(); 
        int status = info.getStatus();
        // Special treatment: Mergeable status should be annotated as Conflict in Versioning view according to UI spec
        if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            status = FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT;
        }
        String oldHtmlDisplayName = htmlDisplayName;
        htmlDisplayName = Subversion.getInstance().getAnnotator().annotateNameHtml(setup.getBaseFile().getName(), info, null);
        fireDisplayNameChange(oldHtmlDisplayName, htmlDisplayName);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }
    
    Setup getSetup() {
        return setup;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) return null;
        return new Action [0];
    }
    
    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @SuppressWarnings("unchecked") // Adding getCookie(Class<Cookie> klass) results in name clash
    @Override
    public Cookie getCookie(Class klass) {
        FileObject fo = getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                if (fo.equals(dobj.getPrimaryFile())) {
                    return dobj.getCookie(klass);
                }
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
        return super.getCookie(klass);
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new LocationProperty());
        ps.put(new StatusProperty());
        if (setup.getPropertyName() != null) {
            ps.put(new PropertyNameProperty());
        }
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private static org.openide.util.Lookup getLookupFor (Setup setup, Object[] lookupObjects) {
        EditorCookie eCookie = DiffUtils.getEditorCookie(setup);
        Object[] allLookupObjects;
        if (eCookie == null) {
            allLookupObjects = new Object[lookupObjects.length + 1];
        } else {
            allLookupObjects = new Object[lookupObjects.length + 2];
            allLookupObjects[allLookupObjects.length - 1] = eCookie;
        }
        allLookupObjects[0] = setup;
        System.arraycopy(lookupObjects, 0, allLookupObjects, 1, lookupObjects.length);
        DiffLookup lkp = new DiffLookup();
        lkp.setData(allLookupObjects);
        return lkp;
    }

    private abstract class DiffNodeProperty extends PropertySupport.ReadOnly<String> {

        protected DiffNodeProperty(String name, String displayName, String shortDescription) {
            super(name, String.class, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
    }

    private class NameProperty extends DiffNodeProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, COLUMN_NAME_NAME, COLUMN_NAME_NAME);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return DiffNode.this.getName();
        }
    }

    private class PropertyNameProperty extends DiffNodeProperty {

        public PropertyNameProperty() {
            super(COLUMN_NAME_PROPERTY, COLUMN_NAME_PROPERTY, COLUMN_NAME_PROPERTY);
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return setup.getPropertyName();
        }
    }
    
    private class LocationProperty extends DiffNodeProperty {
        
        private String location;

        public LocationProperty() {
            super(COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION);
            try {
                location = SvnModuleConfig.getDefault().isRepositoryPathPrefixed()
                        ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(setup.getBaseFile())) : SvnUtils.getRelativePath(setup.getBaseFile());
            } catch (SVNClientException e) {
                location = "";
            }
            setValue("sortkey", location + "\t" + DiffNode.this.getName()); // NOI18N
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return location;
        }
    }
    
    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N

    private class StatusProperty extends DiffNodeProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, COLUMN_NAME_STATUS, COLUMN_NAME_STATUS);
            String shortPath = null;
            try {
                shortPath = SvnUtils.getRelativePath(setup.getBaseFile());
            } catch (SVNClientException e) {
                shortPath = "";
            }
            String sortable = Integer.toString(SvnUtils.getComparableStatus(setup.getInfo().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + DiffNode.this.getName().toUpperCase()); // NOI18N
        }

        public String getValue() throws IllegalAccessException, InvocationTargetException {
            FileInformation finfo =  setup.getInfo();
            finfo.getEntry(setup.getBaseFile());  // XXX not interested in return value, side effect loads ISVNStatus structure
            return finfo.getStatusText(displayStatuses);            
        }
    }
}
