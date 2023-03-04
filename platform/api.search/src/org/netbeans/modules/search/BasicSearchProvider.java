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

import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.modules.search.IgnoreListPanel.IgnoreListManager;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.matcher.AbstractMatcher;
import org.netbeans.modules.search.matcher.DefaultMatcher;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchFilterDefinition.FolderResult;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/** Basic Search provider
 *
 * @author jhavlin
 */
@ServiceProviders(value = {
    @ServiceProvider(service = SearchProvider.class, position = 0),
    @ServiceProvider(service = BasicSearchProvider.class, position = 0)})
public class BasicSearchProvider extends SearchProvider {

    /**
     * Presenter is {@link BasicSearchPresenter}.
     */
    @Override
    public Presenter createPresenter(boolean replaceMode) {
        return new BasicSearchPresenter(replaceMode, null, null, this);
    }

    /**
     * Replacing is supported.
     */
    @Override
    public boolean isReplaceSupported() {
        return true;
    }

    /**
     * This search provider is always enabled.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static Presenter createBasicPresenter(boolean replacing) {
        return new BasicSearchPresenter(replacing, null, null);
    }

    public static Presenter createBasicPresenter(
            boolean replacing,
            @NullAllowed SearchPattern searchPattern,
            @NullAllowed String replaceString,
            @NullAllowed Boolean preserveCase,
            @NullAllowed SearchScopeOptions searchScopeOptions,
            @NullAllowed Boolean useIgnoreList,
            @NullAllowed String scopeId,
            @NonNull SearchScopeDefinition... extraSearchScopes
            ) {
        BasicSearchCriteria bsc = createCriteria(searchScopeOptions,
                useIgnoreList, searchPattern, preserveCase, replacing,
                replaceString);
        return new BasicSearchPresenter(replacing, scopeId, bsc,
                extraSearchScopes);
    }

    @Override
    public String getTitle() {
        return UiUtils.getText("BasicSearchForm.tabText");              //NOI18N
    }

    /**
     *
     */
    private static class BasicSearchPresenter
            extends BasicSearchProvider.Presenter {

        BasicSearchForm form = null;
        private String scopeId;
        private BasicSearchCriteria explicitCriteria;
        private SearchScopeDefinition[] extraSearchScopes;
        private boolean wasUsableAlready = false;

        public BasicSearchPresenter(boolean replacing, String scopeId,
                BasicSearchCriteria explicitCriteria,
                SearchScopeDefinition... extraSearchScopes) {
            this(replacing, scopeId, explicitCriteria,
                    Lookup.getDefault().lookup(BasicSearchProvider.class),
                    extraSearchScopes);
        }

        public BasicSearchPresenter(boolean replacing, String scopeId,
                BasicSearchCriteria explicitCriteria,
                BasicSearchProvider provider,
                SearchScopeDefinition... extraSearchScopes) {
            super(provider, replacing);
            this.scopeId = scopeId;
            this.explicitCriteria = explicitCriteria;
            this.extraSearchScopes = extraSearchScopes;
        }

        @Override
        public JComponent getForm() {
            if (form == null) {
                String scopeToUse = chooseSearchScope(scopeId);
                form = new BasicSearchForm(scopeToUse, isReplacing(),
                        explicitCriteria, extraSearchScopes);
                form.setUsabilityChangeListener((ChangeEvent e) -> fireChange());
            }
            return form;
        }

        private String chooseSearchScope(String preferredscopeId) {
            return preferredscopeId == null
                    ? FindDialogMemory.getDefault().getScopeTypeId()
                    : preferredscopeId;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(SearchPanel.class.getCanonicalName()
                    + "." + isReplacing());                             //NOI18N
        }

        @Override
        public SearchComposition<Def> composeSearch() {

            String msg = Manager.getInstance().mayStartSearching();
            if (msg != null) {
                /*
                 * Search cannot be started, for example because the replace
                 * operation has not finished yet.
                 */
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                        msg,
                        NotifyDescriptor.INFORMATION_MESSAGE));
                return null;
            }

            form.onOk();
            BasicSearchCriteria basicSearchCriteria =
                    form.getBasicSearchCriteria();

            SearchScopeOptions so = basicSearchCriteria.getSearcherOptions();
            if (basicSearchCriteria.isUseIgnoreList()) {
                so.addFilter(new IgnoreListFilter());
            }
            SearchInfo ssi = form.getSearchInfo();
            AbstractMatcher am = new DefaultMatcher(
                    basicSearchCriteria.getSearchPattern());
            am.setStrict(isReplacing());
            return new BasicComposition(
                    ssi, am, basicSearchCriteria, form.getSelectedScopeName());
        }

        @Override
        public boolean isUsable(NotificationLineSupport notifySupport) {
            boolean usable = form.isUsable();
            BasicSearchCriteria bsc = form.getBasicSearchCriteria();
            if (!usable) {
                String msg;
                if (bsc.isTextPatternInvalid()) {
                    msg = "BasicSearchForm.txtErrorTextPattern";        //NOI18N
                } else if (bsc.isSearchAndReplace()
                        && bsc.isReplacePatternInvalid()) {
                    msg = "BasicSearchForm.txtErrorReplacePattern";     //NOI18N
                } else if (bsc.isFileNamePatternInvalid()) {
                    msg = "BasicSearchForm.txtErrorFileName";           //NOI18N
                } else {
                    msg = "BasicSearchForm.txtErrorMissingCriteria";    //NOI18N
                    if (!wasUsableAlready) { // #212614
                        notifySupport.setInformationMessage(
                                UiUtils.getText(msg));
                        return false;
                    }
                }
                notifySupport.setErrorMessage(UiUtils.getText(msg));
            } else {
                wasUsableAlready = true;
                if (!bsc.isFileNameRegexp()
                        && !bsc.getFileNamePatternExpr().isEmpty()
                        && bsc.getFileNamePatternExpr().matches(
                        "^[\\w-]*$")) { //NOI18N
                    notifySupport.setInformationMessage(UiUtils.getText(
                            "BasicSearchForm.txtInfoNoWildcards"));     //NOI18N
                } else {
                    notifySupport.setInformationMessage(UiUtils.getText(
                            "BasicSearchForm.txtInfoMultiline"));     //NOI18N
                }
            }
            return usable;
        }

        @Override
        public void clean() {
            super.clean();
        }
    }

    private static class IgnoreListFilter extends SearchFilterDefinition {

        private IgnoreListManager ignoreListManager;

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {
            if (file.isFolder()) {
                throw new IllegalArgumentException(file
                        + " is folder, but should be regular file."); //NOI18N
            }
            return !isIgnored(file);
        }

        @Override
        public FolderResult traverseFolder(FileObject folder)
                throws IllegalArgumentException {
            if (!folder.isFolder()) {
                throw new IllegalArgumentException(folder
                        + " is file, but should be folder."); //NOI18N
            }
            if (isIgnored(folder)) {
                return FolderResult.DO_NOT_TRAVERSE;
            } else {
                return FolderResult.TRAVERSE;
            }
        }

        /**
         * Check whether the passed file object should be ignored. Use global
         * ignore list.
         *
         * @return true if the file object is ignored, false otherwise.
         */
        private boolean isIgnored(FileObject fileObj) {
            return getIgnoreListManager().isIgnored(fileObj);
        }

        /**
         * Get ignore list manager. If not yet initialized, initialize it.
         */
        IgnoreListPanel.IgnoreListManager getIgnoreListManager() {
            if (ignoreListManager == null) {
                List<String> il = FindDialogMemory.getDefault().getIgnoreList();
                ignoreListManager = new IgnoreListPanel.IgnoreListManager(il);
            }
            return ignoreListManager;
        }
    }

    /**
     * Start a search task with specified parameters.
     */
    public static void startSearch(
            @NonNull SearchPattern searchPattern,
            @NonNull SearchScopeOptions searchScopeOptions,
            @NullAllowed String scopeId) throws IllegalArgumentException {

        BasicSearchCriteria criteria = createCriteria(searchScopeOptions,
                Boolean.FALSE, searchPattern, null, false, null);
        if (!criteria.isUsable()) {
            throw new IllegalArgumentException(
                    "Search cannot be started - No restrictions set."); //NOI18N
        }
        SearchScopeDefinition bestScope = findBestSearchScope(scopeId);
        BasicComposition composition = new BasicComposition(
                bestScope.getSearchInfo(), new DefaultMatcher(searchPattern),
                criteria, null);
        Manager.getInstance().scheduleSearchTask(
                new SearchTask(composition, false));
    }

    /**
     * Create basic search criteria instance for passed arguments.
     */
    private static BasicSearchCriteria createCriteria(
            SearchScopeOptions searchScopeOptions, Boolean useIgnoreList,
            SearchPattern searchPattern, Boolean preserveCase,
            boolean replacing, String replaceString) {
        BasicSearchCriteria bsc = new BasicSearchCriteria();
        bsc.setFileNamePattern(searchScopeOptions.getPattern());
        bsc.setFileNameRegexp(searchScopeOptions.isRegexp());
        bsc.setTextPattern(searchPattern.getSearchExpression());
        bsc.setCaseSensitive(searchPattern.isMatchCase());
        bsc.setWholeWords(searchPattern.isWholeWords());
        bsc.setMatchType(searchPattern.getMatchType());
        if (preserveCase != null) {
            bsc.setPreserveCase(preserveCase);
        }
        if (useIgnoreList != null) {
            bsc.setUseIgnoreList(useIgnoreList);
        }
        if (replacing) {
            bsc.setReplaceExpr(replaceString);
        } else {
            bsc.setSearchInArchives(searchScopeOptions.isSearchInArchives());
            bsc.setSearchInGenerated(searchScopeOptions.isSearchInGenerated());
        }
        return bsc;
    }

    /**
     * Find best available search scope.
     */
    private static SearchScopeDefinition findBestSearchScope(
            String preferredscopeId) throws IllegalStateException {
        SearchScopeList ssl = new SearchScopeList();
        SearchScopeDefinition bestScope = null;
        for (SearchScopeDefinition ssd : ssl.getSeachScopeDefinitions()) {
            if (ssd.isApplicable()) {
                if (preferredscopeId != null && ssd.getTypeId().equals(preferredscopeId)) {
                    bestScope = ssd;
                    break;
                } else if (bestScope == null) {
                    bestScope = ssd;
                }
            }
        }
        if (bestScope == null) {
            throw new IllegalStateException("No default search scope"); //NOI18N
        }
        return bestScope;
    }

    /**
     * Create filter for actual ignore list.
     */
    public static SearchFilterDefinition getIgnoreListFilter() {
        return new IgnoreListFilter();
    }
}
