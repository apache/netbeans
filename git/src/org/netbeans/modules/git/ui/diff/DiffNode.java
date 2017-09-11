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
