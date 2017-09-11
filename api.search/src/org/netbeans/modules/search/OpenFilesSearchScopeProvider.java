/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SearchScopeDefinitionProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * SearchScopeProvider for all the open documents.
 *
 * @author markiewb
 */
@ServiceProvider(service = SearchScopeDefinitionProvider.class)
public class OpenFilesSearchScopeProvider extends SearchScopeDefinitionProvider {

    @Override
    public List<SearchScopeDefinition> createSearchScopeDefinitions() {
        List<SearchScopeDefinition> list =
                new ArrayList<SearchScopeDefinition>(1);
        list.add(new OpenFilesScope());
        return list;
    }

    @NbBundle.Messages({
        "# {0} - number of files. Please do not translate \"choice\", \"#\", \"number\" and \"integer\".",
        "LBL_OpenFileScope=Open {0,choice,0#Documents|1#Document|1<Documents} ({0,choice,0#0 files|1#1 file|1<{0,number,integer} files})"
    })
    private static class OpenFilesScope extends SearchScopeDefinition {

        //icon taken from http://hg.netbeans.org/main-golden/rev/edbd736e674e
        @StaticResource
        private static final String ICON_PATH =
                "org/netbeans/modules/search/res/multi_selection.png"; //NOI18N
        private static final Icon ICON;

        static {
            ICON = ImageUtilities.loadImageIcon(ICON_PATH, false);
        }

        @Override
        public String getTypeId() {
            return "openFiles";//NOI18N
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_OpenFileScope(getCurrentlyOpenedFiles().size());
        }

        @Override
        public boolean isApplicable() {
            Collection<FileObject> files = getCurrentlyOpenedFiles();
            return files != null && !files.isEmpty();
        }

        @Override
        public SearchInfo getSearchInfo() {
            Collection<FileObject> files = getCurrentlyOpenedFiles();
            //use all current open files
            return SearchInfoUtils.createSearchInfoForRoots(
                    files.toArray(new FileObject[files.size()]));
        }

        @Override
        public int getPriority() {
            return 450;
        }

        @Override
        public void clean() {
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         *
         * @return all the currenlty opened FileObjects
         */
        private Collection<FileObject> getCurrentlyOpenedFiles() {
            LinkedHashSet<FileObject> result = new LinkedHashSet<FileObject>();
            for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
                DataObject dob = tc.getLookup().lookup(DataObject.class);
                if (tc.isOpened() && dob != null && isFromEditorWindow(dob, tc)) {
                    FileObject primaryFile = dob.getPrimaryFile();
                    if (primaryFile != null) {
                        result.add(primaryFile);
                    }
                }
            }
            return result;
        }

        protected boolean isFromEditorWindow(DataObject dobj,
                final TopComponent tc) {
            final EditorCookie editor = dobj.getLookup().lookup(
                    EditorCookie.class);
            if (editor != null) {
                return Mutex.EVENT.readAccess(new Action<Boolean>() {
                    @Override
                    public Boolean run() {
                        return (tc instanceof CloneableTopComponent) // #246597
                                || NbDocument.findRecentEditorPane(editor) != null;
                    }
                });
            }
            return false;
        }
    }
}
