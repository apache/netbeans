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

package org.netbeans.modules.versionvault.ui.status;

import java.io.IOException;
import org.netbeans.modules.versionvault.client.status.FileEntry;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.ui.diff.DiffAction;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * The node that is rendered in the SyncTable view. It gets values to display from the
 * CvsFileNode which serves as the 'data' node for this 'visual' node.
 * 
 * @author Maros Sandor
 */
public class SyncFileNode extends AbstractNode {
    
    private FileNode node;

    static final String COLUMN_NAME_NAME        = "name";   // NOI18N
    static final String COLUMN_NAME_PATH        = "path";   // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    static final String COLUMN_NAME_RULE        = "rule";   // NOI18N
    static final String COLUMN_NAME_BRANCH      = "branch"; // NOI18N
    
    private String htmlDisplayName;

    private RequestProcessor.Task nodeload;        
    
    private final VersioningPanel panel;

    SyncFileNode(FileNode node, VersioningPanel _panel) {
        this(Children.LEAF, node, _panel);        
    }

    private SyncFileNode(Children children, FileNode node, VersioningPanel _panel) {
        super(children, Lookups.fixed(node.getLookupObjects()));
        this.node = node;
        this.panel = _panel;
        initProperties();
        refreshHtmlDisplayName();
    }
    
    File getFile() {
        return node.getFile();
    }
    
    FileInformation getInfo() {
        return node.getInfo(); 
    }
    
    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public Action getPreferredAction() {        
        return new DiffAction(NbBundle.getMessage(SyncFileNode.class, "DiffAction_Name"), VCSContext.forNodes(new Node[] {this})); // NOI18N
    }

    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @Override
    public Cookie getCookie(Class klass) {
        FileObject fo = FileUtil.toFileObject(getFile());
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
        if (node.getFile().isDirectory()) setIconBaseWithExtension("org/openide/loaders/defaultFolder.gif"); // NOI18N

        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new PathProperty());
        ps.put(new StatusProperty());
        ps.put(new RuleProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = node.getInfo(); 
        htmlDisplayName = Clearcase.getInstance().getAnnotator().annotateNameHtml(node.getFile().getName(), info, null);
        fireDisplayNameChange(node.getName(), node.getName());
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    void refresh() {
        refreshHtmlDisplayName();
    }

    private abstract class SyncFileProperty extends org.openide.nodes.PropertySupport.ReadOnly {

        protected SyncFileProperty(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                Clearcase.LOG.log(Level.INFO, null,e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class PathProperty extends SyncFileProperty {

        private String shortPath;

        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2003"), NbBundle.getMessage(SyncFileNode.class, "BK2004")); // NOI18N
            setValue("sortkey", "\u65000\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            if (shortPath == null) {                
                Runnable run = new Runnable() {
                    public void run() {
                        File file = node.getFile();
                        shortPath = ClearcaseUtils.getLocation(file);
                        if (shortPath == null) {
                            shortPath = org.openide.util.NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
                        }
                        setValue("sortkey", shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
                        // Table sorter is not thread safe, use this as workaround
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                firePropertyChange(COLUMN_NAME_PATH, null, null);
                            }
                        });
                    }
                };          
                // XXX don't use the Clearcase.getInstance().getRequestProcessor() - it might delay FS events processings
                nodeload = Clearcase.getInstance().getRequestProcessor().post(run);
                return org.openide.util.NbBundle.getMessage(SyncFileNode.class, "LBL_RepositoryPath_LoadingProgress"); // NOI18N
            }
            return shortPath;
        }
    }
    
    @Override
    public void destroy() throws IOException {
        super.destroy();
        if (nodeload != null) {
            nodeload.cancel();
        }
    }
    
    private class NameProperty extends SyncFileProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2005"), NbBundle.getMessage(SyncFileNode.class, "BK2006")); // NOI18N
            setValue("sortkey", SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return SyncFileNode.this.getDisplayName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class StatusProperty extends SyncFileProperty {
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2007"), NbBundle.getMessage(SyncFileNode.class, "BK2008")); // NOI18N
            String shortPath = "path"; // NOI18N
            String sortable = Integer.toString(ClearcaseUtils.getComparableStatus(node.getInfo().getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            FileInformation finfo =  node.getInfo();
            finfo.getFileEntry(Clearcase.getInstance().getClient(), node.getFile());  
            int mask = panel.getDisplayStatuses();
            return finfo.getStatusText(mask);
        }
    }
    
    private class RuleProperty extends SyncFileProperty {
        
        public RuleProperty() {
            super(COLUMN_NAME_RULE, String.class, NbBundle.getMessage(SyncFileNode.class, "BK2009"), NbBundle.getMessage(SyncFileNode.class, "BK2009")); // NOI18N
            setValue("sortkey", SyncFileNode.this.getName()); // NOI18N
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            FileInformation finfo =  node.getInfo();
            FileEntry entry = finfo.getCachedEntry();
            if(entry != null) {
                return entry.getRule();
            } else {
                return "";
            }
            
        }
    }    
}
