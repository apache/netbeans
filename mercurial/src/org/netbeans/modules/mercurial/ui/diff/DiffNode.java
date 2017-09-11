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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.ErrorManager;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.ui.status.OpenInEditorAction;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Visible in the Search History Diff view.
 * 
 * @author Maros Sandor
 */
class DiffNode extends VCSStatusNode<HgFileNode> {
    
    static final String COLUMN_NAME_NAME = "name";
    static final String COLUMN_NAME_STATUS = "status";
    static final String COLUMN_NAME_LOCATION = "location";
        
    private final Setup     setup;
    private String          htmlDisplayName;
    private String location;
    private DataObject dobj;

    public DiffNode(Setup setup, HgFileNode node) {
        super(node, getLookupFor(setup, node.getLookupObjects()));
        this.setup = setup;
        setName(setup.getBaseFile().getName());
        initProperties();
        refreshHtmlDisplayName();
        FileObject fo = getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
    }

    @Override
    public void refresh () {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    String getLocation () {
        if (location == null) {
            location = HgUtils.getRelativePath(setup.getBaseFile());
        }
        return location;
    }

    @Override
    public File getFile () {
        return setup.getBaseFile();
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = setup.getInfo(); 
        int status = info.getStatus();
        // Special treatment: Mergeable status should be annotated as Conflict in Versioning view according to UI spec
        if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            status = FileInformation.STATUS_VERSIONED_CONFLICT;
        }
        htmlDisplayName = Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(setup.getBaseFile().getName(), info, null);
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

    @Override
    public Action getNodeAction () {
        return new OpenInEditorAction();
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
        if (fo != null && dobj != null && fo.equals(dobj.getPrimaryFile())) {
            return dobj.getCookie(klass);
        }
        return super.getCookie(klass);
    }

    @Override
    public Image getIcon (int type) {
        if (dobj != null && dobj.isValid()) {
            return dobj.getNodeDelegate().getIcon(type);
        } else {
            return super.getIcon(type);
        }
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

    private abstract class DiffNodeProperty extends PropertySupport.ReadOnly {

        @SuppressWarnings("unchecked")
        protected DiffNodeProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return e.getLocalizedMessage();
            }
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            try {
                return new DiffNode.DiffPropertyEditor(getValue());
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }
    }

    private class NameProperty extends DiffNodeProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, COLUMN_NAME_NAME, COLUMN_NAME_NAME);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return DiffNode.this.getName();
        }
    }
    
    private class LocationProperty extends DiffNodeProperty {
        
        private String location;

        public LocationProperty() {
            super(COLUMN_NAME_LOCATION, String.class, COLUMN_NAME_LOCATION, COLUMN_NAME_LOCATION);
            setValue("sortkey", getLocation() + "\t" + DiffNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return getLocation();
        }
    }
    
    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N

    private class StatusProperty extends DiffNodeProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, COLUMN_NAME_STATUS, COLUMN_NAME_STATUS);
            String shortPath = HgUtils.getRelativePath(setup.getBaseFile());
            String sortable = Integer.toString(HgUtils.getComparableStatus(setup.getInfo().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + DiffNode.this.getName().toUpperCase()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return setup.getInfo().getStatusText();
        }
    }
    
    private static class DiffPropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public DiffPropertyEditor (Object value) {
            setValue(value);
        }

        @Override
        public void paintValue (Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            Object val = getValue();
            if (val instanceof Date) {
                val = DateFormat.getDateTimeInstance().format((Date) val);
            }
            renderer.setText(val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable() {
            return true;
        }
    }
}
