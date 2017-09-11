/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        List<File> existingFiles = new ArrayList<File>(files.length);
        boolean errorShown = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists()) {
                existingFiles.add(files[i]);
            } else if (!errorShown) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(
                        Bundle.MSG_FileDoesNotExist(files[i]),
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
