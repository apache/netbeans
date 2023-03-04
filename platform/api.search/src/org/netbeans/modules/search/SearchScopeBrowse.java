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
package org.netbeans.modules.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Search scope that shows a file chooser to select directories that should be
 * searched.
 *
 * TODO: Show several last selected directories as additional search scopes.
 *
 * @author jhavlin
 */
public class SearchScopeBrowse {

    private static final String ICON_KEY_UIMANAGER_NB =
            "Nb.Explorer.Folder.openedIcon";                            //NOI18N
    private static final Icon ICON;

    private static FileObject[] roots = null;
    private SearchScopeDefinition browseScope = new BrowseScope();
    private SearchScopeDefinition getLastScope = new GetLastScope();

    static {
        Icon icon = UIManager.getIcon(ICON_KEY_UIMANAGER_NB);
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon(
                    "org/openide/loaders/defaultFolder.gif", false);    //NOI18N
        }
        ICON = icon;
    }

    /**
     * Search Scope with title "Browse..." that can open file chooser to get
     * search roots.
     */
    private class BrowseScope extends SearchScopeDefinition {

        private SearchInfo searchInfo = SearchInfoUtils.createForDefinition(
                new BrowseSearchInfo());

        @Override
        public String getTypeId() {
            return "browse";                                            //NOI18N
        }

        @Override
        public String getDisplayName() {
            return UiUtils.getText("LBL_ScopeBrowseName");              //NOI18N
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return searchInfo;
        }

        @Override
        public int getPriority() {
            return 501;
        }

        @Override
        public void clean() {
            // nothing to do
        }

        /**
         * Browse... scope changes to previously selected scope and previously
         * selected scope changes to Browse...
         */
        @Override
        public void selected() {
            chooseRoots();
            notifyListeners();
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }
    }

    /**
     * Search scope representing previously selected search roots.
     */
    private class GetLastScope extends SearchScopeDefinition {

        @Override
        public String getTypeId() {
            return "browse";                                            //NOI18N
        }

        @Override
        public String getDisplayName() {
            if (roots != null && roots.length > 0) {
                return UiUtils.getText("LBL_ScopeBrowseName") //NOI18N
                        + " [" + roots[0].getName() //NOI18N
                        + (roots.length > 1 ? "..." : "") + "]";        //NOI18N
            } else {
                return "no files selected";                             //NOI18N
            }
        }

        @Override
        public boolean isApplicable() {
            return roots != null && roots.length > 0;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return SearchInfoUtils.createSearchInfoForRoots(roots);
        }

        @Override
        public int getPriority() {
            return 500;
        }

        @Override
        public void clean() {
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }
    }

    /**
     * Search info definition that opens file chooser when search roots or file
     * iterator is requested for the first time.
     *
     * This is used if the Browse... scope was not clicked in the UI but was
     * displayed as the default search scope.
     */
    private class BrowseSearchInfo extends SearchInfoDefinition {

        private SearchInfo delegate;

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
                SearchListener listener, AtomicBoolean terminated) {

            return getDelegate().getFilesToSearch(options, listener,
                    terminated).iterator();
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            return getDelegate().getSearchRoots();
        }

        private synchronized SearchInfo getDelegate() {
            if (delegate == null) {
                delegate = createDelegate();
            }
            return delegate;
        }

        private SearchInfo createDelegate() {
            FileObject[] fileObjects = chooseRoots();
            return SearchInfoUtils.createSearchInfoForRoots(fileObjects);
        }
    }

    /**
     * Open file chooser to choose search roots.
     */
    private FileObject[] chooseRoots() {
        FileChooserBuilder chooserBuilder =
                new FileChooserBuilder(SearchScopeBrowse.class);
        chooserBuilder.setTitle(UiUtils.getText(
                "LBL_ScopeBrowseFileChooserTitle"));                    //NOI18N
        chooserBuilder.setApproveText(UiUtils.getText(
                "LBL_ScopeBrowseFileChooserApprove"));                  //NOI18N
        File[] files = chooserBuilder.showMultiOpenDialog();
        if (files == null) {
            files = new File[0];
        }
        List<File> existingFiles = selectExistingFiles(files);
        FileObject[] fileObjects = new FileObject[existingFiles.size()];
        for (int i = 0; i < existingFiles.size(); i++) {
            fileObjects[i] = FileUtil.toFileObject(existingFiles.get(i));
        }
        if (fileObjects.length > 0) {
            roots = fileObjects;
        }
        return fileObjects;
    }

    /**
     * Take an array of files and return a list of existing files from that
     * array. If some of the files does not exist, show an error message, but
     * only for the first detected non-existing file.
     */
    @NbBundle.Messages({
        "# {0} - file path",
        "MSG_FileDoesNotExist=File {0} does not exist.",
        "TTL_FileDoesNotExist=File Error"
    })
    private List<File> selectExistingFiles(File[] files) {
        List<File> existingFiles = new ArrayList<>(files.length);
        boolean errorShown = false;
        for (File file : files) {
            if (file.exists()) {
                existingFiles.add(file);
            } else if (!errorShown) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(
                        Bundle.MSG_FileDoesNotExist(file),
                        Bundle.TTL_FileDoesNotExist(),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE, null,
                        NotifyDescriptor.OK_OPTION));
                errorShown = true;
            }
        }
        return existingFiles;
    }

    /**
     * Get instance for choosing search roots, by selecting the item in combo
     * box or by starting the search.
     */
    public SearchScopeDefinition getBrowseScope() {
        return browseScope;
    }

    /**
     * Get instance representing previously chosen search roots.
     */
    public SearchScopeDefinition getGetLastScope() {
        return getLastScope;
    }
}
