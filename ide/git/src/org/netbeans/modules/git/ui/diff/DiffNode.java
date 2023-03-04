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

package org.netbeans.modules.git.ui.diff;

import org.netbeans.modules.git.GitStatusNode;
import java.io.File;
import javax.swing.Action;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.GitFileNode;
import org.netbeans.modules.git.GitFileNode.GitHistoryFileNode;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.openide.cookies.EditorCookie;

/**
 *
 * @author ondra
 */
public abstract class DiffNode<T extends GitFileNode> extends GitStatusNode<T> implements Cloneable {

    private final Setup setup;
    
    protected DiffNode (T node, Setup setup, EditorCookie eCookie) {
        super(node, getLookupFor(eCookie, node.getLookupObjects()));
        this.setup = setup;
    }
    
    public Setup getSetup() {
        return setup;
    }
    
    @Override
    public abstract DiffNode clone ();

    public boolean isExcluded () {
        return GitModuleConfig.getDefault().isExcludedFromCommit(getFile().getAbsolutePath());
    }
    
    public static class DiffLocalNode extends DiffNode<GitLocalFileNode> {
    
        private final Mode mode;

        DiffLocalNode (GitLocalFileNode node, Setup setup, EditorCookie eCookie, Mode mode) {
            super(node, setup, eCookie);
            this.mode = mode;
        }

        @Override
        public Action getPreferredAction () {
            return getNodeAction();
        }

        @Override
        public Action getNodeAction () {
            return new OpenInEditorAction(new File[] { getFile() });
        }

        @Override
        public String getStatusText () {
            return node.getInformation().getStatusText(mode);
        }

        @Override
        public DiffNode clone () {
            return new DiffLocalNode(getFileNode(), getSetup(), getLookup().lookup(EditorCookie.class), mode);
        }
    }
    
    public static class DiffHistoryNode extends DiffNode<GitHistoryFileNode> {
    
        DiffHistoryNode (GitHistoryFileNode node, Setup setup) {
            super(node, setup, null);
        }

        @Override
        public Action getPreferredAction () {
            return null;
        }

        @Override
        public String getStatusText () {
            return node.getInformation().getStatusText();
        }

        @Override
        public DiffNode clone () {
            return new DiffHistoryNode(getFileNode(), getSetup());
        }

        @Override
        public boolean isExcluded () {
            return false;
        }
        
    }
    
    public static class DiffImmutableNode extends DiffNode<GitFileNode> {
    
        DiffImmutableNode (GitFileNode node, Setup setup, EditorCookie eCookie) {
            super(node, setup, eCookie);
        }

        @Override
        public Action getPreferredAction () {
            return null;
        }

        @Override
        public String getStatusText () {
            return node.getStatusText();
        }

        @Override
        public DiffNode clone () {
            return new DiffImmutableNode(getFileNode(), getSetup(), getLookup().lookup(EditorCookie.class));
        }

        @Override
        public boolean isExcluded () {
            return node.getCommitOptions() == VCSCommitOptions.EXCLUDE;
        }
        
    }

    private static org.openide.util.Lookup getLookupFor (EditorCookie eCookie, Object[] lookupObjects) {
        Object[] allLookupObjects;
        if (eCookie == null) {
            allLookupObjects = new Object[lookupObjects.length];
        } else {
            allLookupObjects = new Object[lookupObjects.length + 1];
            allLookupObjects[allLookupObjects.length - 1] = eCookie;
        }
        System.arraycopy(lookupObjects, 0, allLookupObjects, 0, lookupObjects.length);
        DiffLookup lkp = new DiffLookup();
        lkp.setData(allLookupObjects);
        return lkp;
    }
}
