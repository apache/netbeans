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
