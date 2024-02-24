#Signature file v4.1
#Version 1.132

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public final org.netbeans.modules.bugtracking.api.Issue
fld public final static java.lang.String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed"
fld public final static java.lang.String EVENT_STATUS_CHANGED = "issue.status_changed"
innr public final static !enum Status
meth public boolean canAttachFiles()
meth public boolean isFinished()
meth public boolean refresh()
meth public java.lang.String getDisplayName()
meth public java.lang.String getID()
meth public java.lang.String getShortenedDisplayName()
meth public java.lang.String getSummary()
meth public java.lang.String getTooltip()
meth public org.netbeans.modules.bugtracking.api.Issue$Status getStatus()
meth public org.netbeans.modules.bugtracking.api.Repository getRepository()
meth public void addComment(java.lang.String,boolean)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void attachFile(java.io.File,java.lang.String,boolean)
meth public void open()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds impl

CLSS public final static !enum org.netbeans.modules.bugtracking.api.Issue$Status
 outer org.netbeans.modules.bugtracking.api.Issue
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status CONFLICT
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status INCOMING_MODIFIED
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status INCOMING_NEW
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status OUTGOING_MODIFIED
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status OUTGOING_NEW
fld public final static org.netbeans.modules.bugtracking.api.Issue$Status SEEN
meth public static org.netbeans.modules.bugtracking.api.Issue$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.bugtracking.api.Issue$Status[] values()
supr java.lang.Enum<org.netbeans.modules.bugtracking.api.Issue$Status>

CLSS public final org.netbeans.modules.bugtracking.api.IssueQuickSearch
innr public final static !enum RepositoryFilter
meth public javax.swing.JComponent getComponent()
meth public org.netbeans.modules.bugtracking.api.Issue getIssue()
meth public org.netbeans.modules.bugtracking.api.Repository getSelectedRepository()
meth public static org.netbeans.modules.bugtracking.api.Issue selectIssue(java.lang.String,org.netbeans.modules.bugtracking.api.Repository,javax.swing.JPanel,org.openide.util.HelpCtx)
meth public static org.netbeans.modules.bugtracking.api.IssueQuickSearch create()
meth public static org.netbeans.modules.bugtracking.api.IssueQuickSearch create(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.bugtracking.api.IssueQuickSearch create(org.openide.filesystems.FileObject,org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter)
meth public void setChangeListener(javax.swing.event.ChangeListener)
meth public void setEnabled(boolean)
meth public void setIssue(org.netbeans.modules.bugtracking.api.Issue)
meth public void setRepository(org.netbeans.modules.bugtracking.api.Repository)
supr java.lang.Object
hfds panel

CLSS public final static !enum org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter
 outer org.netbeans.modules.bugtracking.api.IssueQuickSearch
fld public final static org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter ALL
fld public final static org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter ATTACH_FILE
meth public static org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter valueOf(java.lang.String)
meth public static org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter[] values()
supr java.lang.Enum<org.netbeans.modules.bugtracking.api.IssueQuickSearch$RepositoryFilter>

CLSS public final org.netbeans.modules.bugtracking.api.Query
fld public final static java.lang.String EVENT_QUERY_REFRESHED = "bugtracking.query.finished"
meth public java.lang.String getDisplayName()
meth public java.lang.String getTooltip()
meth public java.util.Collection<org.netbeans.modules.bugtracking.api.Issue> getIssues()
meth public org.netbeans.modules.bugtracking.api.Repository getRepository()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void refresh()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.bugtracking.api.Repository
fld public final static java.lang.String EVENT_ATTRIBUTES_CHANGED = "bugtracking.repository.attributes.changed"
fld public final static java.lang.String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed"
meth public !varargs org.netbeans.modules.bugtracking.api.Issue[] getIssues(java.lang.String[])
meth public boolean canAttachFiles()
meth public boolean isMutable()
meth public java.awt.Image getIcon()
meth public java.lang.String getDisplayName()
meth public java.lang.String getId()
meth public java.lang.String getTooltip()
meth public java.lang.String getUrl()
meth public java.util.Collection<org.netbeans.modules.bugtracking.api.Query> getQueries()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void remove()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,impl

CLSS public final org.netbeans.modules.bugtracking.api.RepositoryManager
fld public final static java.lang.String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"
meth public java.util.Collection<org.netbeans.modules.bugtracking.api.Repository> getRepositories()
meth public java.util.Collection<org.netbeans.modules.bugtracking.api.Repository> getRepositories(java.lang.String)
meth public org.netbeans.modules.bugtracking.api.Repository getRepository(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.bugtracking.api.RepositoryManager getInstance()
meth public void addPropertChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds changeSupport,instance,registry
hcls RepositoryListener

CLSS public final org.netbeans.modules.bugtracking.api.RepositoryQuery
meth public static org.netbeans.modules.bugtracking.api.Repository getRepository(org.openide.filesystems.FileObject,boolean)
supr java.lang.Object
hfds instance

CLSS public final org.netbeans.modules.bugtracking.api.Util
meth public static boolean edit(org.netbeans.modules.bugtracking.api.Repository)
meth public static int[] getIssueSpans(java.lang.String)
meth public static java.lang.String getIssueId(java.lang.String)
meth public static java.util.List<org.netbeans.modules.bugtracking.api.Issue> getRecentIssues()
meth public static org.netbeans.modules.bugtracking.api.Repository createRepository()
meth public static org.netbeans.modules.bugtracking.api.Repository getTeamRepository(java.lang.String,java.lang.String)
meth public static void closeQuery(org.netbeans.modules.bugtracking.api.Query)
meth public static void createIssue(org.netbeans.modules.bugtracking.api.Repository,java.lang.String,java.lang.String)
meth public static void createNewIssue(org.netbeans.modules.bugtracking.api.Repository)
meth public static void createNewQuery(org.netbeans.modules.bugtracking.api.Repository)
meth public static void createNewQuery(org.netbeans.modules.bugtracking.api.Repository,boolean)
meth public static void openIssue(org.netbeans.modules.bugtracking.api.Repository,java.lang.String)
meth public static void openIssue(org.openide.filesystems.FileObject,java.lang.String)
meth public static void selectQuery(org.netbeans.modules.bugtracking.api.Query)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.BugtrackingConnector
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.modules.bugtracking.api.Repository createRepository()
meth public abstract org.netbeans.modules.bugtracking.api.Repository createRepository(org.netbeans.modules.bugtracking.spi.RepositoryInfo)

CLSS public abstract interface static !annotation org.netbeans.modules.bugtracking.spi.BugtrackingConnector$Registration
 outer org.netbeans.modules.bugtracking.spi.BugtrackingConnector
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean providesRepositoryManagement()
meth public abstract !hasdefault java.lang.String iconPath()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String id()
meth public abstract java.lang.String tooltip()

CLSS public final org.netbeans.modules.bugtracking.spi.BugtrackingSupport<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init(org.netbeans.modules.bugtracking.spi.RepositoryProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%1},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>,org.netbeans.modules.bugtracking.spi.QueryProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%1},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>,org.netbeans.modules.bugtracking.spi.IssueProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>)
meth public boolean editRepository({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},java.lang.String)
meth public java.awt.Image[] getPriorityIcons()
meth public org.netbeans.modules.bugtracking.api.Repository createRepository({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},org.netbeans.modules.bugtracking.spi.IssueStatusProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>,org.netbeans.modules.bugtracking.spi.IssueScheduleProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>,org.netbeans.modules.bugtracking.spi.IssuePriorityProvider<{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2}>,org.netbeans.modules.bugtracking.spi.IssueFinder)
meth public void addToCategory({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2})
meth public void editQuery({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%1})
meth public void openIssue({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%2})
meth public void setQueryAutoRefresh({org.netbeans.modules.bugtracking.spi.BugtrackingSupport%0},{org.netbeans.modules.bugtracking.spi.BugtrackingSupport%1},boolean)
supr java.lang.Object
hfds issueProvider,queryProvider,repositoryProvider

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssueController
fld public final static java.lang.String PROP_CHANGED = "bugtracking.changed"
meth public abstract boolean discardUnsavedChanges()
meth public abstract boolean isChanged()
meth public abstract boolean saveChanges()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void closed()
meth public abstract void opened()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssueFinder
meth public abstract int[] getIssueSpans(java.lang.CharSequence)
meth public abstract java.lang.String getIssueId(java.lang.String)

CLSS public final org.netbeans.modules.bugtracking.spi.IssuePriorityInfo
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.awt.Image)
meth public java.awt.Image getIcon()
meth public java.lang.String getDisplayName()
meth public java.lang.String getID()
supr java.lang.Object
hfds displayName,icon,id

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssuePriorityProvider<%0 extends java.lang.Object>
meth public abstract java.lang.String getPriorityID({org.netbeans.modules.bugtracking.spi.IssuePriorityProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.IssuePriorityInfo[] getPriorityInfos()

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssueProvider<%0 extends java.lang.Object>
fld public final static java.lang.String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed"
fld public final static java.lang.String EVENT_ISSUE_DELETED = "issue.deleted"
meth public abstract boolean isFinished({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract boolean isNew({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract boolean refresh({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract java.lang.String getDisplayName({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract java.lang.String getID({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract java.lang.String getSummary({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract java.lang.String getTooltip({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract java.util.Collection<java.lang.String> getSubtasks({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.IssueController getController({org.netbeans.modules.bugtracking.spi.IssueProvider%0})
meth public abstract void addComment({org.netbeans.modules.bugtracking.spi.IssueProvider%0},java.lang.String,boolean)
meth public abstract void addPropertyChangeListener({org.netbeans.modules.bugtracking.spi.IssueProvider%0},java.beans.PropertyChangeListener)
meth public abstract void attachFile({org.netbeans.modules.bugtracking.spi.IssueProvider%0},java.io.File,java.lang.String,boolean)
meth public abstract void removePropertyChangeListener({org.netbeans.modules.bugtracking.spi.IssueProvider%0},java.beans.PropertyChangeListener)

CLSS public final org.netbeans.modules.bugtracking.spi.IssueScheduleInfo
cons public init(java.util.Date)
cons public init(java.util.Date,int)
meth public boolean equals(java.lang.Object)
meth public int getInterval()
meth public int hashCode()
meth public java.util.Date getDate()
supr java.lang.Object
hfds date,interval

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssueScheduleProvider<%0 extends java.lang.Object>
fld public final static java.lang.String EVENT_ISSUE_SCHEDULE_CHANGED = "issue.schedule_changed"
meth public abstract java.util.Date getDueDate({org.netbeans.modules.bugtracking.spi.IssueScheduleProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.IssueScheduleInfo getSchedule({org.netbeans.modules.bugtracking.spi.IssueScheduleProvider%0})
meth public abstract void setSchedule({org.netbeans.modules.bugtracking.spi.IssueScheduleProvider%0},org.netbeans.modules.bugtracking.spi.IssueScheduleInfo)

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.IssueStatusProvider<%0 extends java.lang.Object, %1 extends java.lang.Object>
fld public final static java.lang.String EVENT_STATUS_CHANGED = "issue.status_changed"
innr public final static !enum Status
meth public abstract boolean submit({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1})
meth public abstract java.util.Collection<{org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1}> getUnsubmittedIssues({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status getStatus({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1})
meth public abstract void addPropertyChangeListener({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1},java.beans.PropertyChangeListener)
meth public abstract void discardOutgoing({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1})
meth public abstract void removePropertyChangeListener({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1},java.beans.PropertyChangeListener)
meth public abstract void setSeenIncoming({org.netbeans.modules.bugtracking.spi.IssueStatusProvider%1},boolean)

CLSS public final static !enum org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status
 outer org.netbeans.modules.bugtracking.spi.IssueStatusProvider
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status CONFLICT
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status INCOMING_MODIFIED
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status INCOMING_NEW
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status OUTGOING_MODIFIED
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status OUTGOING_NEW
fld public final static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status SEEN
meth public static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status[] values()
supr java.lang.Enum<org.netbeans.modules.bugtracking.spi.IssueStatusProvider$Status>

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.QueryController
fld public final static java.lang.String PROP_CHANGED = "bugtracking.query.changed"
innr public final static !enum QueryMode
meth public abstract boolean discardUnsavedChanges()
meth public abstract boolean isChanged()
meth public abstract boolean providesMode(org.netbeans.modules.bugtracking.spi.QueryController$QueryMode)
meth public abstract boolean saveChanges(java.lang.String)
meth public abstract javax.swing.JComponent getComponent(org.netbeans.modules.bugtracking.spi.QueryController$QueryMode)
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void closed()
meth public abstract void opened()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final static !enum org.netbeans.modules.bugtracking.spi.QueryController$QueryMode
 outer org.netbeans.modules.bugtracking.spi.QueryController
fld public final static org.netbeans.modules.bugtracking.spi.QueryController$QueryMode EDIT
fld public final static org.netbeans.modules.bugtracking.spi.QueryController$QueryMode VIEW
meth public static org.netbeans.modules.bugtracking.spi.QueryController$QueryMode valueOf(java.lang.String)
meth public static org.netbeans.modules.bugtracking.spi.QueryController$QueryMode[] values()
supr java.lang.Enum<org.netbeans.modules.bugtracking.spi.QueryController$QueryMode>

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.QueryProvider<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public final static IssueContainer
meth public abstract boolean canRemove({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract boolean canRename({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract java.lang.String getDisplayName({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract java.lang.String getTooltip({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.QueryController getController({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract void refresh({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract void remove({org.netbeans.modules.bugtracking.spi.QueryProvider%0})
meth public abstract void rename({org.netbeans.modules.bugtracking.spi.QueryProvider%0},java.lang.String)
meth public abstract void setIssueContainer({org.netbeans.modules.bugtracking.spi.QueryProvider%0},org.netbeans.modules.bugtracking.spi.QueryProvider$IssueContainer<{org.netbeans.modules.bugtracking.spi.QueryProvider%1}>)

CLSS public final static org.netbeans.modules.bugtracking.spi.QueryProvider$IssueContainer<%0 extends java.lang.Object>
 outer org.netbeans.modules.bugtracking.spi.QueryProvider
meth public !varargs void add({org.netbeans.modules.bugtracking.spi.QueryProvider$IssueContainer%0}[])
meth public !varargs void remove({org.netbeans.modules.bugtracking.spi.QueryProvider$IssueContainer%0}[])
meth public void clear()
meth public void refreshingFinished()
meth public void refreshingStarted()
meth public void restoreFinished()
meth public void restoreStarted()
supr java.lang.Object
hfds delegate

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.RepositoryController
meth public abstract boolean isValid()
meth public abstract java.lang.String getErrorMessage()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void applyChanges()
meth public abstract void cancelChanges()
meth public abstract void populate()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final org.netbeans.modules.bugtracking.spi.RepositoryInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,char[],char[])
meth public char[] getHttpPassword()
meth public char[] getPassword()
meth public java.lang.String getConnectorId()
meth public java.lang.String getDisplayName()
meth public java.lang.String getHttpUsername()
meth public java.lang.String getID()
meth public java.lang.String getTooltip()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public java.lang.String getValue(java.lang.String)
meth public void putValue(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DELIMITER,LOG,PROPERTY_CONNECTOR_ID,PROPERTY_DISPLAY_NAME,PROPERTY_HTTP_USERNAME,PROPERTY_ID,PROPERTY_TOOLTIP,PROPERTY_URL,PROPERTY_USERNAME,map

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.RepositoryProvider<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
fld public final static java.lang.String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed"
fld public final static java.lang.String EVENT_UNSUBMITTED_ISSUES_CHANGED = "bugtracking.repository.unsubmittedIssues.changed"
meth public abstract !varargs java.util.Collection<{org.netbeans.modules.bugtracking.spi.RepositoryProvider%2}> getIssues({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0},java.lang.String[])
meth public abstract boolean canAttachFiles({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract java.awt.Image getIcon({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract java.util.Collection<{org.netbeans.modules.bugtracking.spi.RepositoryProvider%1}> getQueries({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract java.util.Collection<{org.netbeans.modules.bugtracking.spi.RepositoryProvider%2}> simpleSearch({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0},java.lang.String)
meth public abstract org.netbeans.modules.bugtracking.spi.RepositoryController getController({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract org.netbeans.modules.bugtracking.spi.RepositoryInfo getInfo({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract void addPropertyChangeListener({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0},java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0},java.beans.PropertyChangeListener)
meth public abstract void removed({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract {org.netbeans.modules.bugtracking.spi.RepositoryProvider%1} createQuery({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract {org.netbeans.modules.bugtracking.spi.RepositoryProvider%2} createIssue({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0})
meth public abstract {org.netbeans.modules.bugtracking.spi.RepositoryProvider%2} createIssue({org.netbeans.modules.bugtracking.spi.RepositoryProvider%0},java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.bugtracking.spi.RepositoryQueryImplementation
meth public abstract java.lang.String getRepositoryUrl(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.bugtracking.spi.SchedulePicker
cons public init()
meth public javax.swing.JComponent getComponent()
meth public org.netbeans.modules.bugtracking.spi.IssueScheduleInfo getScheduleDate()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setScheduleDate(org.netbeans.modules.bugtracking.spi.IssueScheduleInfo)
supr java.lang.Object
hfds impl

