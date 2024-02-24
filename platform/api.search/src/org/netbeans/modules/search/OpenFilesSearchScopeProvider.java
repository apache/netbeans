/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                new ArrayList<>(1);
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
            return SearchInfoUtils.createSearchInfoForRoots(files.toArray(new FileObject[0]));
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
            LinkedHashSet<FileObject> result = new LinkedHashSet<>();
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
                return Mutex.EVENT.readAccess((Action<Boolean>) () ->
                        (tc instanceof CloneableTopComponent) // #246597
                        || NbDocument.findRecentEditorPane(editor) != null);
            }
            return false;
        }
    }
}
