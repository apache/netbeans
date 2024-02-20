#Signature file v4.1
#Version 1.44

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.api.search.RegexpUtil
meth public static java.util.regex.Pattern makeFileNamePattern(org.netbeans.api.search.SearchScopeOptions)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.api.search.ReplacePattern
meth public boolean equals(java.lang.Object)
meth public boolean isPreserveCase()
meth public int hashCode()
meth public java.lang.String getReplaceExpression()
meth public org.netbeans.api.search.ReplacePattern changePreserveCase(boolean)
meth public org.netbeans.api.search.ReplacePattern changeReplaceExpression(java.lang.String)
meth public static org.netbeans.api.search.ReplacePattern create(java.lang.String,boolean)
supr java.lang.Object
hfds preserveCase,replaceExpression

CLSS public final org.netbeans.api.search.SearchControl
meth public static void openFindDialog(org.netbeans.api.search.SearchPattern,org.netbeans.api.search.SearchScopeOptions,java.lang.Boolean,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static void openFindDialog(org.netbeans.spi.search.provider.SearchProvider$Presenter)
meth public static void openReplaceDialog(org.netbeans.api.search.SearchPattern,java.lang.String,java.lang.Boolean,org.netbeans.api.search.SearchScopeOptions,java.lang.Boolean,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public static void openReplaceDialog(org.netbeans.spi.search.provider.SearchProvider$Presenter)
meth public static void startBasicSearch(org.netbeans.api.search.SearchPattern,org.netbeans.api.search.SearchScopeOptions,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public final org.netbeans.api.search.SearchHistory
fld public final static java.lang.String ADD_TO_HISTORY = "add-to-history"
fld public final static java.lang.String ADD_TO_REPLACE = "add-to-replace"
fld public final static java.lang.String LAST_SELECTED = "last-selected"
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.netbeans.api.search.ReplacePattern> getReplacePatterns()
meth public java.util.List<org.netbeans.api.search.SearchPattern> getSearchPatterns()
meth public org.netbeans.api.search.SearchPattern getLastSelected()
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.search.SearchHistory getDefault()
meth public void add(org.netbeans.api.search.SearchPattern)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addReplace(org.netbeans.api.search.ReplacePattern)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setLastSelected(org.netbeans.api.search.SearchPattern)
 anno 0 java.lang.Deprecated()
meth public void storeFileNamePattern(java.lang.String)
supr java.lang.Object
hfds INSTANCE,MAX_PATTERN_LENGTH,MAX_SEARCH_PATTERNS_ITEMS,PREFS_NODE,PROP_REPLACE_PATTERN_PREFIX,PROP_SEARCH_PATTERN_PREFIX,pcs,prefs,replacePatternsList,searchPatternsList

CLSS public final org.netbeans.api.search.SearchPattern
innr public final static !enum MatchType
meth public boolean equals(java.lang.Object)
meth public boolean isMatchCase()
meth public boolean isRegExp()
meth public boolean isWholeWords()
meth public int hashCode()
meth public java.lang.String getSearchExpression()
meth public org.netbeans.api.search.SearchPattern changeMatchCase(boolean)
meth public org.netbeans.api.search.SearchPattern changeMatchType(org.netbeans.api.search.SearchPattern$MatchType)
meth public org.netbeans.api.search.SearchPattern changeRegExp(boolean)
meth public org.netbeans.api.search.SearchPattern changeSearchExpression(java.lang.String)
meth public org.netbeans.api.search.SearchPattern changeWholeWords(boolean)
meth public org.netbeans.api.search.SearchPattern$MatchType getMatchType()
meth public static org.netbeans.api.search.SearchPattern create(java.lang.String,boolean,boolean,boolean)
meth public static org.netbeans.api.search.SearchPattern create(java.lang.String,boolean,boolean,org.netbeans.api.search.SearchPattern$MatchType)
supr java.lang.Object
hfds matchCase,matchType,searchExpression,wholeWords

CLSS public final static !enum org.netbeans.api.search.SearchPattern$MatchType
 outer org.netbeans.api.search.SearchPattern
fld public final static org.netbeans.api.search.SearchPattern$MatchType BASIC
fld public final static org.netbeans.api.search.SearchPattern$MatchType LITERAL
fld public final static org.netbeans.api.search.SearchPattern$MatchType REGEXP
meth public java.lang.String toString()
meth public static org.netbeans.api.search.SearchPattern$MatchType valueOf(java.lang.String)
meth public static org.netbeans.api.search.SearchPattern$MatchType[] values()
supr java.lang.Enum<org.netbeans.api.search.SearchPattern$MatchType>
hfds canonicalPatternFlag,displayName

CLSS public final org.netbeans.api.search.SearchRoot
cons public init(java.net.URI,java.util.List<org.netbeans.api.search.provider.SearchFilter>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(org.openide.filesystems.FileObject,java.util.List<org.netbeans.api.search.provider.SearchFilter>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.net.URI getUri()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<org.netbeans.api.search.provider.SearchFilter> getFilters()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EMPTY_FILTER_LIST,LOG,filters,rootFile,rootUri

CLSS public org.netbeans.api.search.SearchScopeOptions
meth public boolean isRegexp()
meth public boolean isSearchInArchives()
meth public boolean isSearchInGenerated()
meth public java.lang.String getPattern()
meth public java.util.List<org.netbeans.spi.search.SearchFilterDefinition> getFilters()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.SearchScopeOptions create()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.SearchScopeOptions create(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.search.SearchScopeOptions create(java.lang.String,boolean,boolean,boolean,java.util.List<org.netbeans.spi.search.SearchFilterDefinition>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public void addFilter(org.netbeans.spi.search.SearchFilterDefinition)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setPattern(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setRegexp(boolean)
meth public void setSearchInArchives(boolean)
meth public void setSearchInGenerated(boolean)
supr java.lang.Object
hfds DEFAULT,filters,pattern,regexp,searchInArchives,searchInGenerated
hcls DefaultSearchOptions

CLSS public abstract org.netbeans.api.search.provider.FileNameMatcher
meth public abstract boolean pathMatches(java.io.File)
meth public abstract boolean pathMatches(java.net.URI)
meth public abstract boolean pathMatches(org.openide.filesystems.FileObject)
meth public static org.netbeans.api.search.provider.FileNameMatcher create(org.netbeans.api.search.SearchScopeOptions)
supr java.lang.Object
hfds TAKE_ALL_INSTANCE
hcls ExtensionMatcher,RegexpPatternMatcher,SimplePatternMatcher,TakeAllMatcher

CLSS public abstract org.netbeans.api.search.provider.SearchFilter
cons public init()
innr public final static !enum FolderResult
meth public abstract boolean searchFile(java.net.URI)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean searchFile(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.search.provider.SearchFilter$FolderResult traverseFolder(java.net.URI)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.search.provider.SearchFilter$FolderResult traverseFolder(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static !enum org.netbeans.api.search.provider.SearchFilter$FolderResult
 outer org.netbeans.api.search.provider.SearchFilter
fld public final static org.netbeans.api.search.provider.SearchFilter$FolderResult DO_NOT_TRAVERSE
fld public final static org.netbeans.api.search.provider.SearchFilter$FolderResult TRAVERSE
fld public final static org.netbeans.api.search.provider.SearchFilter$FolderResult TRAVERSE_ALL_SUBFOLDERS
meth public static org.netbeans.api.search.provider.SearchFilter$FolderResult valueOf(java.lang.String)
meth public static org.netbeans.api.search.provider.SearchFilter$FolderResult[] values()
supr java.lang.Enum<org.netbeans.api.search.provider.SearchFilter$FolderResult>

CLSS public abstract org.netbeans.api.search.provider.SearchInfo
cons public init()
meth protected abstract java.util.Iterator<java.net.URI> createUrisToSearchIterator(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth protected abstract java.util.Iterator<org.openide.filesystems.FileObject> createFilesToSearchIterator(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean canSearch()
meth public abstract java.util.List<org.netbeans.api.search.SearchRoot> getSearchRoots()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final java.lang.Iterable<java.net.URI> getUrisToSearch(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public final java.lang.Iterable<org.openide.filesystems.FileObject> getFilesToSearch(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.api.search.provider.SearchInfoUtils
cons public init()
fld public final static java.util.List<org.netbeans.api.search.provider.SearchFilter> DEFAULT_FILTERS
fld public final static org.netbeans.api.search.provider.SearchFilter SHARABILITY_FILTER
fld public final static org.netbeans.api.search.provider.SearchFilter VISIBILITY_FILTER
meth public !varargs static org.netbeans.api.search.provider.SearchInfo createCompoundSearchInfo(org.netbeans.api.search.provider.SearchInfo[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.api.search.provider.SearchInfo createSearchInfoForRoots(org.openide.filesystems.FileObject[],boolean,org.netbeans.spi.search.SearchFilterDefinition[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo createEmptySearchInfo()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo createForDefinition(org.netbeans.spi.search.SearchInfoDefinition)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo createSearchInfoForRoot(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo createSearchInfoForRoots(org.openide.filesystems.FileObject[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo findDefinedSearchInfo(org.openide.nodes.Node)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.provider.SearchInfo getSearchInfoForNode(org.openide.nodes.Node)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract org.netbeans.api.search.provider.SearchListener
cons protected init()
meth public void directoryEntered(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void fileContentMatchingError(java.lang.String,java.lang.Throwable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void fileContentMatchingProgress(java.lang.String,long)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void fileContentMatchingStarted(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void fileSkipped(java.net.URI,org.netbeans.spi.search.SearchFilterDefinition,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public void fileSkipped(org.openide.filesystems.FileObject,org.netbeans.spi.search.SearchFilterDefinition,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public void generalError(java.lang.Throwable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.api.search.ui.ComponentUtils
meth public !varargs static org.netbeans.api.search.ui.ScopeController adjustComboForScope(javax.swing.JComboBox,java.lang.String,org.netbeans.spi.search.SearchScopeDefinition[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.ui.FileNameController adjustComboForFileName(javax.swing.JComboBox)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.ui.ScopeOptionsController adjustPanelForOptions(javax.swing.JPanel,boolean,org.netbeans.api.search.ui.FileNameController)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.ui.ScopeOptionsController adjustPanelsForOptions(javax.swing.JPanel,javax.swing.JPanel,boolean,org.netbeans.api.search.ui.FileNameController)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.search.ui.SearchPatternController adjustComboForSearchPattern(javax.swing.JComboBox)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.api.search.ui.FileNameController
fld protected javax.swing.JComboBox component
meth protected final void fireChange()
meth public boolean isAllFilesInfoDisplayed()
meth public boolean isRegularExpression()
meth public final boolean hasListeners()
meth public final javax.swing.JComboBox getComponent()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getFileNamePattern()
meth public void displayAllFilesInfo()
meth public void hideAllFilesInfo()
meth public void setFileNamePattern(java.lang.String)
meth public void setRegularExpression(boolean)
supr java.lang.Object<javax.swing.JComboBox>
hfds defaultColor,fileNamePatternEditor,fileNamePatternWatcher,ignoreFileNamePatternChanges,patternValid,regexp
hcls FileNameChangeListener,FileNamePatternWatcher

CLSS public final org.netbeans.api.search.ui.ScopeController
fld protected javax.swing.JComboBox component
meth protected final void fireChange()
meth public final boolean hasListeners()
meth public final javax.swing.JComboBox getComponent()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getSelectedScopeId()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSelectedScopeTitle()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.search.provider.SearchInfo getSearchInfo()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object<javax.swing.JComboBox>
hfds active,extraSearchScopes,manualSelectionListener,manuallySelectedId,preferredId,scopeList,searchScopeChangeListener,searchScopeChangeListenerWeak,selectedSearchScope
hcls ManualSelectionListener,ScopeCellRenderer,ScopeComboBoxHierarchyListener,ScopeItem,SearchScopeChangeListener

CLSS public final org.netbeans.api.search.ui.ScopeOptionsController
fld protected javax.swing.JCheckBox chkUseIgnoreList
fld protected javax.swing.JPanel component
fld protected javax.swing.JPanel ignoreListOptionPanel
meth protected final void fireChange()
meth public boolean isFileNameRegExp()
meth public boolean isSearchInArchives()
meth public boolean isSearchInGenerated()
meth public boolean isUseIgnoreList()
meth public final boolean hasListeners()
meth public final javax.swing.JPanel getComponent()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public javax.swing.JPanel getFileNameComponent()
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public org.netbeans.api.search.SearchScopeOptions getSearchScopeOptions()
meth public void setFileNameRegexp(boolean)
meth public void setSearchInArchives(boolean)
meth public void setSearchInGenerated(boolean)
meth public void setUseIgnoreList(boolean)
supr java.lang.Object<javax.swing.JPanel>
hfds btnEditIgnoreList,btnTestFileNamePattern,checkBoxListener,chkArchives,chkFileNameRegex,chkGenerated,fileNameComboBox,fileNameComponent,replacing
hcls CheckBoxListener

CLSS public final org.netbeans.api.search.ui.SearchPatternController
fld protected javax.swing.JComboBox component
innr public final static !enum Option
meth protected final void fireChange()
meth public final boolean hasListeners()
meth public final javax.swing.JComboBox getComponent()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.search.SearchPattern getSearchPattern()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void bind(org.netbeans.api.search.ui.SearchPatternController$Option,javax.swing.AbstractButton)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void bindMatchTypeComboBox(javax.swing.JComboBox)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setSearchPattern(org.netbeans.api.search.SearchPattern)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void unbind(org.netbeans.api.search.ui.SearchPatternController$Option,javax.swing.AbstractButton)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object<javax.swing.JComboBox>
hfds bindings,defaultTextColor,listener,matchType,matchTypeComboBox,options,textToFindEditor,valid
hcls ModelItem,TextToFindChangeListener

CLSS public final static !enum org.netbeans.api.search.ui.SearchPatternController$Option
 outer org.netbeans.api.search.ui.SearchPatternController
fld public final static org.netbeans.api.search.ui.SearchPatternController$Option MATCH_CASE
fld public final static org.netbeans.api.search.ui.SearchPatternController$Option REGULAR_EXPRESSION
fld public final static org.netbeans.api.search.ui.SearchPatternController$Option WHOLE_WORDS
meth public static org.netbeans.api.search.ui.SearchPatternController$Option valueOf(java.lang.String)
meth public static org.netbeans.api.search.ui.SearchPatternController$Option[] values()
supr java.lang.Enum<org.netbeans.api.search.ui.SearchPatternController$Option>

CLSS public abstract org.netbeans.spi.search.SearchFilterDefinition
cons public init()
innr public final static !enum FolderResult
meth public abstract boolean searchFile(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.spi.search.SearchFilterDefinition$FolderResult traverseFolder(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean searchFile(java.net.URI)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.search.SearchFilterDefinition$FolderResult traverseFolder(java.net.URI)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG

CLSS public final static !enum org.netbeans.spi.search.SearchFilterDefinition$FolderResult
 outer org.netbeans.spi.search.SearchFilterDefinition
fld public final static org.netbeans.spi.search.SearchFilterDefinition$FolderResult DO_NOT_TRAVERSE
fld public final static org.netbeans.spi.search.SearchFilterDefinition$FolderResult TRAVERSE
fld public final static org.netbeans.spi.search.SearchFilterDefinition$FolderResult TRAVERSE_ALL_SUBFOLDERS
meth public static org.netbeans.spi.search.SearchFilterDefinition$FolderResult valueOf(java.lang.String)
meth public static org.netbeans.spi.search.SearchFilterDefinition$FolderResult[] values()
supr java.lang.Enum<org.netbeans.spi.search.SearchFilterDefinition$FolderResult>

CLSS public abstract org.netbeans.spi.search.SearchInfoDefinition
cons public init()
meth public abstract boolean canSearch()
meth public abstract java.util.Iterator<org.openide.filesystems.FileObject> filesToSearch(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<org.netbeans.api.search.SearchRoot> getSearchRoots()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Iterator<java.net.URI> urisToSearch(org.netbeans.api.search.SearchScopeOptions,org.netbeans.api.search.provider.SearchListener,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.spi.search.SearchInfoDefinitionFactory
fld public final static java.util.List<org.netbeans.spi.search.SearchFilterDefinition> DEFAULT_FILTER_DEFS
fld public final static org.netbeans.spi.search.SearchFilterDefinition SHARABILITY_FILTER
fld public final static org.netbeans.spi.search.SearchFilterDefinition VISIBILITY_FILTER
meth public static org.netbeans.spi.search.SearchInfoDefinition createFlatSearchInfo(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createFlatSearchInfo(org.openide.filesystems.FileObject,org.netbeans.spi.search.SearchFilterDefinition[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createSearchInfo(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createSearchInfo(org.openide.filesystems.FileObject,org.netbeans.spi.search.SearchFilterDefinition[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createSearchInfo(org.openide.filesystems.FileObject[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createSearchInfo(org.openide.filesystems.FileObject[],org.netbeans.spi.search.SearchFilterDefinition[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.search.SearchInfoDefinition createSearchInfoBySubnodes(org.openide.nodes.Children)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT_FILTERS

CLSS public abstract org.netbeans.spi.search.SearchScopeDefinition
cons public init()
meth protected final void notifyListeners()
meth public abstract boolean isApplicable()
meth public abstract int getPriority()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getTypeId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.search.provider.SearchInfo getSearchInfo()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void clean()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getAdditionalInfo()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public javax.swing.Icon getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void selected()
supr java.lang.Object
hfds changeListeners

CLSS public abstract org.netbeans.spi.search.SearchScopeDefinitionProvider
cons public init()
meth public abstract java.util.List<org.netbeans.spi.search.SearchScopeDefinition> createSearchScopeDefinitions()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract org.netbeans.spi.search.SubTreeSearchOptions
cons public init()
meth public abstract java.util.List<org.netbeans.spi.search.SearchFilterDefinition> getFilters()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer<%0 extends java.lang.Object>
innr public abstract static ResultNodeShiftSupport
meth public java.lang.String getTitle()
meth public javax.swing.JComponent getVisualComponent()
meth public org.openide.explorer.view.OutlineView getOutlineView()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addButton(javax.swing.AbstractButton)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addMatchingObject({org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer%0})
meth public void searchFinished()
meth public void searchStarted()
meth public void setInfoNode(org.openide.nodes.Node)
meth public void setResultNodeShiftSupport(org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer$ResultNodeShiftSupport)
supr org.netbeans.spi.search.provider.SearchResultsDisplayer<{org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer%0}>
hfds DEFAULT_NODE_SHIFT_SUPPORT,helper,panel,presenter,searchComposition,shiftSupport,title
hcls TrivialResultNodeShiftSupport

CLSS public abstract static org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer$ResultNodeShiftSupport
 outer org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer
cons public init()
meth public abstract boolean isRelevantNode(org.openide.nodes.Node)
meth public abstract void relevantNodeSelected(org.openide.nodes.Node)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.search.provider.SearchComposition<%0 extends java.lang.Object>
cons protected init()
meth public abstract boolean isTerminated()
meth public abstract org.netbeans.spi.search.provider.SearchResultsDisplayer<{org.netbeans.spi.search.provider.SearchComposition%0}> getSearchResultsDisplayer()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void start(org.netbeans.api.search.provider.SearchListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void terminate()
supr java.lang.Object

CLSS public abstract org.netbeans.spi.search.provider.SearchProvider
cons protected init()
innr public abstract static Presenter
meth public abstract boolean isEnabled()
meth public abstract boolean isReplaceSupported()
meth public abstract java.lang.String getTitle()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.spi.search.provider.SearchProvider$Presenter createPresenter(boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract static org.netbeans.spi.search.provider.SearchProvider$Presenter
 outer org.netbeans.spi.search.provider.SearchProvider
cons protected init(org.netbeans.spi.search.provider.SearchProvider,boolean)
meth protected final void fireChange()
meth public abstract boolean isUsable(org.openide.NotificationLineSupport)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.JComponent getForm()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.spi.search.provider.SearchComposition<?> composeSearch()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final boolean hasListeners()
meth public final boolean isReplacing()
meth public final org.netbeans.spi.search.provider.SearchProvider getSearchProvider()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.openide.util.HelpCtx getHelpCtx()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void clean()
supr java.lang.Object
hfds changeSupport,replacing,searchProvider

CLSS public abstract org.netbeans.spi.search.provider.SearchResultsDisplayer<%0 extends java.lang.Object>
cons protected init()
innr public abstract static NodeDisplayer
meth public abstract java.lang.String getTitle()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.JComponent getVisualComponent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addMatchingObject({org.netbeans.spi.search.provider.SearchResultsDisplayer%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends java.lang.Object> org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer<{%%0}> createDefault(org.netbeans.spi.search.provider.SearchResultsDisplayer$NodeDisplayer<{%%0}>,org.netbeans.spi.search.provider.SearchComposition<{%%0}>,org.netbeans.spi.search.provider.SearchProvider$Presenter,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public void closed()
meth public void searchFinished()
meth public void searchStarted()
meth public void setInfoNode(org.openide.nodes.Node)
supr java.lang.Object

CLSS public abstract static org.netbeans.spi.search.provider.SearchResultsDisplayer$NodeDisplayer<%0 extends java.lang.Object>
 outer org.netbeans.spi.search.provider.SearchResultsDisplayer
cons protected init()
meth public abstract org.openide.nodes.Node matchToNode({org.netbeans.spi.search.provider.SearchResultsDisplayer$NodeDisplayer%0})
supr java.lang.Object

