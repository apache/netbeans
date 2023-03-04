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

package org.netbeans.modules.git;

import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ondra
 */
public abstract class GitStatusNode<T extends GitFileNode> extends VCSStatusNode<T> {

    public GitStatusNode (T node) {
        this(node, Lookups.fixed(node.getLookupObjects()));
    }

    public GitStatusNode (T node, Lookup lkp) {
        super(node, lkp);
        initProperties();
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

        ps.put(nameProperty);
        ps.put(pathProperty);
        ps.put(new GitStatusProperty(this));

        sheet.put(ps);
        setSheet(sheet);
    }

    @Override
    public void refresh() {
        // do something when needed
    }

    public abstract String getStatusText ();

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    public static class GitStatusProperty extends NodeProperty<String> {
        public static final String NAME = "gitstatus"; //NOI18N
        @NbBundle.Messages("LBL_Status.DisplayName=Status")
        public static final String DISPLAY_NAME = Bundle.LBL_Status_DisplayName();
        @NbBundle.Messages("LBL_Status.Description=Last known Git status")
        public static final String DESCRIPTION = Bundle.LBL_Status_Description();
        private final GitStatusNode node;

        public GitStatusProperty (GitStatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION);
            String sortable = Integer.toString(statusNode.getFileNode().getInformation().getComparableStatus());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + statusNode.getFileNode().getName()); // NOI18N
            this.node = statusNode;
        }

        @Override
        public String getValue () {
            return node.getStatusText();
        }
    }
}
