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

import static java.util.logging.Level.FINER;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.RegexpUtil;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchPattern.MatchType;
import org.netbeans.api.search.SearchScopeOptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Class encapsulating basic search criteria.
 *
 * @author Marian Petras
 */
public final class BasicSearchCriteria {

    private static int instanceCounter;
    private final int instanceId = instanceCounter++;
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchCriteria");         //NOI18N
    private SearchPattern searchPattern = SearchPattern.create(null, false,
            false, false);
    private SearchScopeOptions searcherOptions = SearchScopeOptions.create();
    private String replaceExpr;
    private String replaceString;
    private boolean preserveCase;
    private boolean textPatternSpecified = false;
    private boolean fileNamePatternSpecified = false;
    private boolean textPatternValid = false;
    private boolean replacePatternValid = false;
    private boolean fileNamePatternValid = false;
    private Pattern textPattern;
    private Pattern fileNamePattern;
    private boolean useIgnoreList = false;
    private boolean criteriaUsable = false;
    private ChangeListener usabilityChangeListener;
    /**
     * Holds a {@code DataObject} that will be used to create a {@code TextDetail}.
     * It should be set to {@code null} immediately after all {@code TextDetail}s
     * are created for given {@code DataObject}.
     *
     * @see #findDataObject(org.openide.filesystems.FileObject)
     * @see #freeDataObject()
     */
    private DataObject dataObject;

    BasicSearchCriteria() {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>()", instanceId);               //NOI18N
        }
    }

    /**
     * Copy-constructor.
     *
     * @param template template to create a copy from
     */
    BasicSearchCriteria(BasicSearchCriteria template) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "#{0}: <init>(template)", instanceId);       //NOI18N
        }

        /*
         * check-boxes:
         */
        setCaseSensitive(template.searchPattern.isMatchCase());
        setWholeWords(template.searchPattern.isWholeWords());
        setMatchType(template.searchPattern.getMatchType());
        setPreserveCase(template.preserveCase);
        setSearchInArchives(template.searcherOptions.isSearchInArchives());
        setSearchInGenerated(template.searcherOptions.isSearchInGenerated());
        setFileNameRegexp(template.searcherOptions.isRegexp());
        setUseIgnoreList(template.useIgnoreList);

        /*
         * combo-boxes:
         */
        setTextPattern(template.searchPattern.getSearchExpression());
        setFileNamePattern(template.searcherOptions.getPattern());
        setReplaceExpr(template.replaceExpr);
    }

    /**
     * Returns a {@link Pattern} object corresponding to the substring pattern
     * specified in the criteria.
     *
     * @return {@code Pattern} object, or {@code null} if no pattern has been
     * specified
     */
    Pattern getTextPattern() {

        if (!textPatternValid || !textPatternSpecified) {
            return null;
        }
        if (textPattern != null) {
            return textPattern;
        }

        try {
            return TextRegexpUtil.makeTextPattern(searchPattern);
        } catch (PatternSyntaxException e) {
            textPatternValid = false;
            return null;
        }
    }

    public String getTextPatternExpr() {
        return searchPattern.getSearchExpression() != null
                ? searchPattern.getSearchExpression()
                : "";                                                   //NOI18N
    }

    /**
     * Sets a text pattern. Whether it is considered a simple pattern or a
     * regexp pattern, is determined by the current <em>regexp</em> setting (see {@link #setRegexp(boolean)}).
     *
     * @param pattern pattern to be set
     */
    void setTextPattern(String pattern) {

        searchPattern = searchPattern.changeSearchExpression(pattern);
        boolean wasValid = textPatternValid;

        if (pattern == null || pattern.equals("")) {
            textPattern = null;
            textPatternSpecified = false;
            textPatternValid = false;
        } else {
            textPatternSpecified = true;
            updateTextPattern();
        }

        replacePatternValid = validateReplacePattern();
        updateUsability(textPatternValid != wasValid);
    }

    private void updateFileNamePattern() {
        try {
            if (fileNamePatternSpecified) {
                fileNamePattern = RegexpUtil.makeFileNamePattern(
                        searcherOptions);
                fileNamePatternValid = true;
            }
        } catch (PatternSyntaxException e) {
            fileNamePattern = null;
            fileNamePatternValid = false;
        }
    }

    private void updateTextPattern() throws NullPointerException {
        try {
            if (textPatternSpecified) {
                textPattern = TextRegexpUtil.makeTextPattern(searchPattern);
                textPatternValid = true;
            }
        } catch (PatternSyntaxException e) {
            textPatternValid = false;
        }
    }

    /**
     * Tries to compile the regular expression pattern, thus checking its
     * validity. In case of success, the compiled pattern is stored to {@link #textPattern},
     * otherwise the field is set to {@code null}.
     *
     * <p>Actually, this method defines a pattern used in searching, i.e. it
     * defines behaviour of the searching. It should be the same as behavior of
     * the Find action (Ctrl+F) in the Editor to avoid any confusions (see Bug
     * #175101). Hence, this implementation should specify default flags in the
     * call of the method {@link Pattern#compile(java.lang.String, int)
     * java.util.regex.Pattern.compile(String regex, int flags)} that are the
     * same as in the implementation of the Find action (i.e in the method {@code getFinder}
     * of the class {@code org.netbeans.modules.editor.lib2.search.DocumentFinder}).
     * </p>
     *
     * @return {@code true} if the regexp pattern expression was valid; {@code false}
     * otherwise
     */
    private boolean validateReplacePattern() {
        if (searchPattern.isRegExp() && textPatternValid
                && textPatternSpecified && replaceExpr != null
                && !replaceExpr.isEmpty()) {
            int groups = getTextPattern().matcher("").groupCount();
            String tmpSearch = "";
            for (int i = 1; i <= groups; i++) {
                tmpSearch += "(" + i + ")";
            }
            try {
                Pattern.compile(tmpSearch).matcher("123456789").
                        replaceFirst(replaceExpr);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get text pattern match type.
     */
    MatchType getMatchType() {
        return searchPattern.getMatchType();
    }

    boolean isPreserveCase() {
        return preserveCase;
    }

    void setPreserveCase(boolean preserveCase) {
        if (LOG.isLoggable(FINER)) {
            LOG.log(FINER, "setPreservecase({0}{1}",
                    new Object[]{preserveCase, ')'});                   //NOI18N
        }
        if (preserveCase == this.preserveCase) {
            LOG.finest(" - no change");                                 //NOI18N
            return;
        }

        this.preserveCase = preserveCase;

        if (!searchPattern.isRegExp()) {
            textPattern = null;
        }
    }

    public boolean isFileNameRegexp() {
        return searcherOptions.isRegexp();
    }

    public void setFileNameRegexp(boolean fileNameRegexp) {

        if (this.searcherOptions.isRegexp() != fileNameRegexp) {
            searcherOptions.setRegexp(fileNameRegexp);
            updateFileNamePattern();
            updateUsability(true);
        }
    }

    public boolean isSearchInArchives() {
        return searcherOptions.isSearchInArchives();
    }

    public void setSearchInArchives(boolean searchInArchives) {
        this.searcherOptions.setSearchInArchives(searchInArchives);
    }

    public boolean isSearchInGenerated() {
        return searcherOptions.isSearchInGenerated();
    }

    public void setSearchInGenerated(boolean searchInGenerated) {
        this.searcherOptions.setSearchInGenerated(searchInGenerated);
    }

    public boolean isUseIgnoreList() {
        return useIgnoreList;
    }

    public void setUseIgnoreList(boolean useIgnoreList) {
        this.useIgnoreList = useIgnoreList;
    }

    void setMatchType(MatchType matchType) {

        searchPattern = searchPattern.changeMatchType(matchType);
        updateTextPattern();
        replacePatternValid = validateReplacePattern();
        updateUsability(true);
    }

    boolean isWholeWords() {
        return searchPattern.isWholeWords();
    }

    void setWholeWords(boolean wholeWords) {

        searchPattern = searchPattern.changeWholeWords(wholeWords);
        updateTextPattern();
    }

    boolean isCaseSensitive() {
        return searchPattern.isMatchCase();
    }

    void setCaseSensitive(boolean caseSensitive) {

        searchPattern = searchPattern.changeMatchCase(caseSensitive);
        updateTextPattern();
    }

    boolean isFullText() {
        return textPatternValid;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns a {@link Pattern} object corresponding to the file name pattern
     * or set of patterns specified.
     *
     * @return {@code Pattern} object, or {@code null} if no pattern has been
     * specified
     */
    Pattern getFileNamePattern() {
        if (!fileNamePatternValid || !fileNamePatternSpecified) {
            return null;
        }

        if (fileNamePattern == null) {
            updateFileNamePattern();
            return fileNamePattern;
        } else {
            return fileNamePattern;
        }
    }

    String getFileNamePatternExpr() {
        return searcherOptions.getPattern();
    }

    void setFileNamePattern(String pattern) {
        searcherOptions.setPattern(pattern);
        if (searcherOptions.getPattern().isEmpty()) {
            fileNamePatternSpecified = false;
        } else {
            fileNamePatternSpecified = true;
            updateFileNamePattern();
        }
        // Force updating of usability if standard pattern is used:
        //  Info message about missing wildcards may need updating.
        boolean force = !isFileNameRegexp();
        updateUsability(force);
    }

    //--------------------------------------------------------------------------
    boolean isSearchAndReplace() {
        return replaceExpr != null;
    }

    /**
     * Returns the replacement expression.
     *
     * @return replace expression, or {@code null} if no replace expression has
     * been specified
     */
    public String getReplaceExpr() {
        return replaceExpr;
    }

    /**
     * Returns the replacement string.
     *
     * @return replace string, or {@code null} if no replace string has been
     * specified
     */
    String getReplaceString() {
        if ((replaceString == null) && (replaceExpr != null)) {
            String[] sGroups =
                    replaceExpr.split("\\\\\\\\", replaceExpr.length());//NOI18N
            String res = "";                         //NOI18N
            for (int i = 0; i < sGroups.length; i++) {
                String tmp = sGroups[i];
                tmp = tmp.replace("\\" + "r", "\r"); //NOI18N
                tmp = tmp.replace("\\" + "n", "\n"); //NOI18N
                tmp = tmp.replace("\\" + "t", "\t"); //NOI18N
                res += tmp;
                if (i != sGroups.length - 1) {
                    res += "\\\\";                   //NOI18N
                }
            }
            this.replaceString = res;
        }
        return replaceString;
    }

    /**
     * Sets a replacement string/expression.
     *
     * @param replaceExpr string to replace matches with, or {@code null} if no
     * replacing should be performed
     */
    void setReplaceExpr(String replaceExpr) {
        this.replaceExpr = replaceExpr;
        this.replaceString = null;
        this.replacePatternValid = validateReplacePattern();
        updateUsability(false);
    }

    //--------------------------------------------------------------------------
    private void updateUsability(boolean force) {
        boolean wasUsable = criteriaUsable;
        criteriaUsable = isUsable();
        if (criteriaUsable != wasUsable || force) {
            fireUsabilityChanged();
        }
    }

    boolean isUsable() {
        return (textPatternSpecified
                || (!isSearchAndReplace() && fileNamePatternSpecified))
                && !isInvalid();
    }

    private boolean isInvalid() {
        return isTextPatternInvalid() || isFileNamePatternInvalid()
                || isReplacePatternInvalid();
    }

    void setUsabilityChangeListener(ChangeListener l) {
        this.usabilityChangeListener = l;
    }

    private void fireUsabilityChanged() {
        if (usabilityChangeListener != null) {
            usabilityChangeListener.stateChanged(new ChangeEvent(this));
        }
    }

    boolean isTextPatternUsable() {
        return textPatternSpecified && textPatternValid;
    }

    boolean isTextPatternInvalid() {
        return textPatternSpecified && !textPatternValid;
    }

    boolean isReplacePatternInvalid() {
        return !replacePatternValid;
    }

    boolean isFileNamePatternUsable() {
        return fileNamePatternSpecified && fileNamePatternValid;
    }

    boolean isFileNamePatternInvalid() {
        return fileNamePatternSpecified && !fileNamePatternValid;
    }

    //--------------------------------------------------------------------------
    /**
     * Called when the criteria in the Find dialog are confirmed by the user and
     * the search is about to be started. Makes sure everything is ready for
     * searching, e.g. regexp patterns are compiled.
     */
    void onOk() {
        LOG.finer("onOk()");                                            //NOI18N
        if (textPatternValid && (textPattern == null)) {
            textPattern = TextRegexpUtil.makeTextPattern(searchPattern);
        }
        if (fileNamePatternValid && (fileNamePattern == null)) {
            fileNamePattern = RegexpUtil.makeFileNamePattern(searcherOptions);
        }

        assert !textPatternValid || (textPattern != null);
        assert !fileNamePatternValid || (fileNamePattern != null);
    }

    boolean isTextPatternValidAndSpecified() {
        return textPatternValid && textPatternSpecified;
    }

    /**
     * Get underlying search pattern.
     *
     * @return Current search pattern, never null.
     */
    SearchPattern getSearchPattern() {
        return this.searchPattern;
    }

    /**
     * Get underlying searcher options.
     *
     * @return Current searcher options, with no custom filters specififed.
     * Never returns null.
     */
    SearchScopeOptions getSearcherOptions() {
        return this.searcherOptions;
    }
}
