#Signature file v4.1
#Version 1.58

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public final org.netbeans.libs.git.GitBlameResult
meth public int getLineCount()
meth public java.io.File getBlamedFile()
meth public org.netbeans.libs.git.GitLineDetails getLineDetails(int)
supr java.lang.Object
hfds blamedFile,lineCount,lineDetails

CLSS public final org.netbeans.libs.git.GitBranch
fld public final static java.lang.String NO_BRANCH = "(no branch)"
fld public final static org.netbeans.libs.git.GitBranch NO_BRANCH_INSTANCE
meth public boolean isActive()
meth public boolean isRemote()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public org.netbeans.libs.git.GitBranch getTrackedBranch()
supr java.lang.Object
hfds active,id,name,remote,trackedBranch

CLSS public final org.netbeans.libs.git.GitCherryPickResult
innr public final static !enum CherryPickStatus
meth public java.util.Collection<java.io.File> getConflicts()
meth public java.util.Collection<java.io.File> getFailures()
meth public org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus getCherryPickStatus()
meth public org.netbeans.libs.git.GitRevisionInfo getCurrentHead()
meth public org.netbeans.libs.git.GitRevisionInfo[] getCherryPickedCommits()
supr java.lang.Object
hfds cherryPickedCommits,conflicts,currentHead,failures,status

CLSS public final static !enum org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus
 outer org.netbeans.libs.git.GitCherryPickResult
fld public final static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus ABORTED
fld public final static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus CONFLICTING
fld public final static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus FAILED
fld public final static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus OK
fld public final static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus UNCOMMITTED
meth public static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitCherryPickResult$CherryPickStatus>

CLSS public final org.netbeans.libs.git.GitClient
fld public final static java.lang.String INDEX = "INDEX"
fld public final static java.lang.String WORKING_TREE = "WORKING_TREE"
innr public final static !enum DiffMode
innr public static !enum CherryPickOperation
innr public static !enum RebaseOperationType
innr public static !enum ResetType
meth public boolean catFile(java.io.File,java.lang.String,java.io.OutputStream,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public boolean catIndexEntry(java.io.File,int,java.io.OutputStream,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.io.File[] ignore(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.io.File[] listModifiedIndexEntries(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.io.File[] unignore(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitRevisionInfo$GitFileInfo> getStatus(java.io.File[],java.lang.String,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitStatus> getConflicts(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitStatus> getStatus(java.io.File[],java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitStatus> getStatus(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitSubmoduleStatus> getSubmoduleStatus(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitSubmoduleStatus> initializeSubmodules(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitSubmoduleStatus> updateSubmodules(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,java.lang.String> listRemoteTags(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitBranch> getBranches(boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitBranch> listRemoteBranches(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitRemoteConfig> getRemotes(org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTag> getTags(org.netbeans.libs.git.progress.ProgressMonitor,boolean) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTransportUpdate> fetch(java.lang.String,java.util.List<java.lang.String>,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTransportUpdate> fetch(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitBlameResult blame(java.io.File,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitBranch createBranch(java.lang.String,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitBranch setUpstreamBranch(java.lang.String,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitCherryPickResult cherryPick(org.netbeans.libs.git.GitClient$CherryPickOperation,java.lang.String[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitMergeResult merge(java.lang.String,org.netbeans.libs.git.GitRepository$FastForwardOption,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitMergeResult merge(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitPullResult pull(java.lang.String,java.util.List<java.lang.String>,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitPushResult push(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRebaseResult rebase(org.netbeans.libs.git.GitClient$RebaseOperationType,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRefUpdateResult updateReference(java.lang.String,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRemoteConfig getRemote(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRepositoryState getRepositoryState(org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevertResult revert(java.lang.String,java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo commit(java.io.File[],java.lang.String,org.netbeans.libs.git.GitUser,org.netbeans.libs.git.GitUser,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo commit(java.io.File[],java.lang.String,org.netbeans.libs.git.GitUser,org.netbeans.libs.git.GitUser,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo getCommonAncestor(java.lang.String[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo getPreviousRevision(java.io.File,java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo log(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo stashSave(java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo[] log(org.netbeans.libs.git.SearchCriteria,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo[] log(org.netbeans.libs.git.SearchCriteria,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRevisionInfo[] stashList(org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitTag createTag(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitUser getUser() throws org.netbeans.libs.git.GitException
meth public void add(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void addNotificationListener(org.netbeans.libs.git.progress.NotificationListener)
meth public void checkout(java.io.File[],java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void checkoutRevision(java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void clean(java.io.File[],org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void copyAfter(java.io.File,java.io.File,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void deleteBranch(java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void deleteTag(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void exportCommit(java.lang.String,java.io.OutputStream,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void exportDiff(java.io.File[],java.lang.String,java.lang.String,java.io.OutputStream,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void exportDiff(java.io.File[],org.netbeans.libs.git.GitClient$DiffMode,java.io.OutputStream,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void init(org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void release()
meth public void remove(java.io.File[],boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void removeNotificationListener(org.netbeans.libs.git.progress.NotificationListener)
meth public void removeRemote(java.lang.String,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void rename(java.io.File,java.io.File,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void reset(java.io.File[],java.lang.String,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void reset(java.lang.String,org.netbeans.libs.git.GitClient$ResetType,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void setCallback(org.netbeans.libs.git.GitClientCallback)
meth public void setRemote(org.netbeans.libs.git.GitRemoteConfig,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void stashApply(int,boolean,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void stashDrop(int,org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
meth public void stashDropAll(org.netbeans.libs.git.progress.ProgressMonitor) throws org.netbeans.libs.git.GitException
supr java.lang.Object
hfds credentialsProvider,delegateListener,gitFactory,gitRepository,listeners
hcls DelegateListener

CLSS public static !enum org.netbeans.libs.git.GitClient$CherryPickOperation
 outer org.netbeans.libs.git.GitClient
fld public final static org.netbeans.libs.git.GitClient$CherryPickOperation ABORT
fld public final static org.netbeans.libs.git.GitClient$CherryPickOperation BEGIN
fld public final static org.netbeans.libs.git.GitClient$CherryPickOperation CONTINUE
fld public final static org.netbeans.libs.git.GitClient$CherryPickOperation QUIT
meth public static org.netbeans.libs.git.GitClient$CherryPickOperation valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitClient$CherryPickOperation[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitClient$CherryPickOperation>

CLSS public final static !enum org.netbeans.libs.git.GitClient$DiffMode
 outer org.netbeans.libs.git.GitClient
fld public final static org.netbeans.libs.git.GitClient$DiffMode HEAD_VS_INDEX
fld public final static org.netbeans.libs.git.GitClient$DiffMode HEAD_VS_WORKINGTREE
fld public final static org.netbeans.libs.git.GitClient$DiffMode INDEX_VS_WORKINGTREE
meth public static org.netbeans.libs.git.GitClient$DiffMode valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitClient$DiffMode[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitClient$DiffMode>

CLSS public static !enum org.netbeans.libs.git.GitClient$RebaseOperationType
 outer org.netbeans.libs.git.GitClient
fld public final static org.netbeans.libs.git.GitClient$RebaseOperationType ABORT
fld public final static org.netbeans.libs.git.GitClient$RebaseOperationType BEGIN
fld public final static org.netbeans.libs.git.GitClient$RebaseOperationType CONTINUE
fld public final static org.netbeans.libs.git.GitClient$RebaseOperationType SKIP
meth public static org.netbeans.libs.git.GitClient$RebaseOperationType valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitClient$RebaseOperationType[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitClient$RebaseOperationType>

CLSS public static !enum org.netbeans.libs.git.GitClient$ResetType
 outer org.netbeans.libs.git.GitClient
fld public final static org.netbeans.libs.git.GitClient$ResetType HARD
fld public final static org.netbeans.libs.git.GitClient$ResetType MIXED
fld public final static org.netbeans.libs.git.GitClient$ResetType SOFT
meth public static org.netbeans.libs.git.GitClient$ResetType valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitClient$ResetType[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitClient$ResetType>

CLSS public abstract org.netbeans.libs.git.GitClientCallback
cons public init()
meth public abstract char[] getPassphrase(java.lang.String,java.lang.String)
meth public abstract char[] getPassword(java.lang.String,java.lang.String)
meth public abstract java.lang.Boolean askYesNoQuestion(java.lang.String,java.lang.String)
meth public abstract java.lang.String askQuestion(java.lang.String,java.lang.String)
meth public abstract java.lang.String getIdentityFile(java.lang.String,java.lang.String)
meth public abstract java.lang.String getUsername(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.libs.git.GitConflictDescriptor
innr public abstract static !enum Type
meth public org.netbeans.libs.git.GitConflictDescriptor$Type getType()
supr java.lang.Object
hfds type

CLSS public abstract static !enum org.netbeans.libs.git.GitConflictDescriptor$Type
 outer org.netbeans.libs.git.GitConflictDescriptor
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type ADDED_BY_THEM
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type ADDED_BY_US
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type BOTH_ADDED
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type BOTH_DELETED
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type BOTH_MODIFIED
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type DELETED_BY_THEM
fld public final static org.netbeans.libs.git.GitConflictDescriptor$Type DELETED_BY_US
meth public abstract java.lang.String getDescription()
meth public static org.netbeans.libs.git.GitConflictDescriptor$Type valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitConflictDescriptor$Type[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitConflictDescriptor$Type>

CLSS public org.netbeans.libs.git.GitException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
innr public static AuthorizationException
innr public static CheckoutConflictException
innr public static MissingObjectException
innr public static NotMergedException
innr public static RefUpdateException
supr java.lang.Exception

CLSS public static org.netbeans.libs.git.GitException$AuthorizationException
 outer org.netbeans.libs.git.GitException
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
meth public java.lang.String getRepositoryUrl()
supr org.netbeans.libs.git.GitException
hfds repositoryUrl

CLSS public static org.netbeans.libs.git.GitException$CheckoutConflictException
 outer org.netbeans.libs.git.GitException
cons public init(java.lang.String[])
cons public init(java.lang.String[],java.lang.Throwable)
meth public java.lang.String[] getConflicts()
supr org.netbeans.libs.git.GitException
hfds conflicts

CLSS public static org.netbeans.libs.git.GitException$MissingObjectException
 outer org.netbeans.libs.git.GitException
cons public init(java.lang.String,org.netbeans.libs.git.GitObjectType)
cons public init(java.lang.String,org.netbeans.libs.git.GitObjectType,java.lang.Throwable)
meth public java.lang.String getObjectName()
meth public org.netbeans.libs.git.GitObjectType getObjectType()
supr org.netbeans.libs.git.GitException
hfds objectName,objectType

CLSS public static org.netbeans.libs.git.GitException$NotMergedException
 outer org.netbeans.libs.git.GitException
cons public init(java.lang.String)
meth public java.lang.String getUnmergedRevision()
supr org.netbeans.libs.git.GitException
hfds unmergedRevision

CLSS public static org.netbeans.libs.git.GitException$RefUpdateException
 outer org.netbeans.libs.git.GitException
cons public init(java.lang.String,org.netbeans.libs.git.GitRefUpdateResult)
meth public org.netbeans.libs.git.GitRefUpdateResult getResult()
supr org.netbeans.libs.git.GitException
hfds result

CLSS public final org.netbeans.libs.git.GitLineDetails
meth public int getSourceLine()
meth public java.io.File getSourceFile()
meth public java.lang.String getContent()
meth public org.netbeans.libs.git.GitRevisionInfo getRevisionInfo()
meth public org.netbeans.libs.git.GitUser getAuthor()
meth public org.netbeans.libs.git.GitUser getCommitter()
supr java.lang.Object
hfds author,committer,content,revision,sourceFile,sourceLine

CLSS public final org.netbeans.libs.git.GitMergeResult
innr public static !enum MergeStatus
meth public java.lang.String getBase()
meth public java.lang.String getNewHead()
meth public java.lang.String[] getMergedCommits()
meth public java.util.Collection<java.io.File> getConflicts()
meth public java.util.Collection<java.io.File> getFailures()
meth public org.netbeans.libs.git.GitMergeResult$MergeStatus getMergeStatus()
supr java.lang.Object
hfds base,conflicts,failures,mergeStatus,mergedCommits,newHead,workDir

CLSS public static !enum org.netbeans.libs.git.GitMergeResult$MergeStatus
 outer org.netbeans.libs.git.GitMergeResult
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus ABORTED
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus ALREADY_UP_TO_DATE
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus CONFLICTING
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus FAILED
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus FAST_FORWARD
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus MERGED
fld public final static org.netbeans.libs.git.GitMergeResult$MergeStatus NOT_SUPPORTED
meth public static org.netbeans.libs.git.GitMergeResult$MergeStatus valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitMergeResult$MergeStatus[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitMergeResult$MergeStatus>

CLSS public !enum org.netbeans.libs.git.GitObjectType
fld public final static org.netbeans.libs.git.GitObjectType BLOB
fld public final static org.netbeans.libs.git.GitObjectType COMMIT
fld public final static org.netbeans.libs.git.GitObjectType HEAD
fld public final static org.netbeans.libs.git.GitObjectType TAG
fld public final static org.netbeans.libs.git.GitObjectType TREE
fld public final static org.netbeans.libs.git.GitObjectType UNKNOWN
meth public static org.netbeans.libs.git.GitObjectType valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitObjectType[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitObjectType>

CLSS public final org.netbeans.libs.git.GitPullResult
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTransportUpdate> getFetchResult()
meth public org.netbeans.libs.git.GitMergeResult getMergeResult()
supr java.lang.Object
hfds fetchResult,mergeResult

CLSS public final org.netbeans.libs.git.GitPushResult
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTransportUpdate> getLocalRepositoryUpdates()
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitTransportUpdate> getRemoteRepositoryUpdates()
supr java.lang.Object
hfds localRepositoryUpdates,remoteRepositoryUpdates

CLSS public final org.netbeans.libs.git.GitRebaseResult
innr public abstract static !enum RebaseStatus
meth public java.lang.String getCurrentCommit()
meth public java.lang.String getCurrentHead()
meth public java.util.Collection<java.io.File> getConflicts()
meth public java.util.Collection<java.io.File> getFailures()
meth public org.netbeans.libs.git.GitRebaseResult$RebaseStatus getRebaseStatus()
supr java.lang.Object
hfds conflicts,currentCommit,currentHead,failures,rebaseStatus

CLSS public abstract static !enum org.netbeans.libs.git.GitRebaseResult$RebaseStatus
 outer org.netbeans.libs.git.GitRebaseResult
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus ABORTED
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus CONFLICTS
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus FAILED
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus FAST_FORWARD
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus NOTHING_TO_COMMIT
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus OK
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus STOPPED
fld public final static org.netbeans.libs.git.GitRebaseResult$RebaseStatus UP_TO_DATE
meth public abstract boolean isSuccessful()
meth public static org.netbeans.libs.git.GitRebaseResult$RebaseStatus valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRebaseResult$RebaseStatus[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRebaseResult$RebaseStatus>

CLSS public final !enum org.netbeans.libs.git.GitRefUpdateResult
fld public final static org.netbeans.libs.git.GitRefUpdateResult AWAITING_REPORT
fld public final static org.netbeans.libs.git.GitRefUpdateResult FAST_FORWARD
fld public final static org.netbeans.libs.git.GitRefUpdateResult FORCED
fld public final static org.netbeans.libs.git.GitRefUpdateResult IO_FAILURE
fld public final static org.netbeans.libs.git.GitRefUpdateResult LOCK_FAILURE
fld public final static org.netbeans.libs.git.GitRefUpdateResult NEW
fld public final static org.netbeans.libs.git.GitRefUpdateResult NON_EXISTING
fld public final static org.netbeans.libs.git.GitRefUpdateResult NOT_ATTEMPTED
fld public final static org.netbeans.libs.git.GitRefUpdateResult NO_CHANGE
fld public final static org.netbeans.libs.git.GitRefUpdateResult OK
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_CURRENT_BRANCH
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_MISSING_OBJECT
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_NODELETE
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_NONFASTFORWARD
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_OTHER_REASON
fld public final static org.netbeans.libs.git.GitRefUpdateResult REJECTED_REMOTE_CHANGED
fld public final static org.netbeans.libs.git.GitRefUpdateResult RENAMED
fld public final static org.netbeans.libs.git.GitRefUpdateResult UP_TO_DATE
meth public static org.netbeans.libs.git.GitRefUpdateResult valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRefUpdateResult[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRefUpdateResult>

CLSS public final org.netbeans.libs.git.GitRemoteConfig
cons public init(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public java.lang.String getRemoteName()
meth public java.util.List<java.lang.String> getFetchRefSpecs()
meth public java.util.List<java.lang.String> getPushRefSpecs()
meth public java.util.List<java.lang.String> getPushUris()
meth public java.util.List<java.lang.String> getUris()
supr java.lang.Object
hfds fetchSpecs,pushSpecs,pushUris,remoteName,uris

CLSS public final org.netbeans.libs.git.GitRepository
innr public static !enum FastForwardOption
meth public org.netbeans.libs.git.GitClient createClient() throws org.netbeans.libs.git.GitException
meth public org.netbeans.libs.git.GitRepository$FastForwardOption getDefaultFastForwardOption() throws org.netbeans.libs.git.GitException
meth public static org.netbeans.libs.git.GitRepository getInstance(java.io.File)
supr java.lang.Object
hfds gitRepository,repositoryLocation,repositoryPool

CLSS public static !enum org.netbeans.libs.git.GitRepository$FastForwardOption
 outer org.netbeans.libs.git.GitRepository
fld public final static org.netbeans.libs.git.GitRepository$FastForwardOption FAST_FORWARD
fld public final static org.netbeans.libs.git.GitRepository$FastForwardOption FAST_FORWARD_ONLY
fld public final static org.netbeans.libs.git.GitRepository$FastForwardOption NO_FAST_FORWARD
meth public static org.netbeans.libs.git.GitRepository$FastForwardOption valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRepository$FastForwardOption[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRepository$FastForwardOption>

CLSS public abstract !enum org.netbeans.libs.git.GitRepositoryState
fld public final static org.netbeans.libs.git.GitRepositoryState APPLY
fld public final static org.netbeans.libs.git.GitRepositoryState BARE
fld public final static org.netbeans.libs.git.GitRepositoryState BISECTING
fld public final static org.netbeans.libs.git.GitRepositoryState CHERRY_PICKING
fld public final static org.netbeans.libs.git.GitRepositoryState CHERRY_PICKING_RESOLVED
fld public final static org.netbeans.libs.git.GitRepositoryState MERGING
fld public final static org.netbeans.libs.git.GitRepositoryState MERGING_RESOLVED
fld public final static org.netbeans.libs.git.GitRepositoryState REBASING
fld public final static org.netbeans.libs.git.GitRepositoryState SAFE
meth public abstract boolean canCheckout()
meth public abstract boolean canCommit()
meth public abstract boolean canResetHead()
meth public static org.netbeans.libs.git.GitRepositoryState valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRepositoryState[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRepositoryState>

CLSS public final org.netbeans.libs.git.GitRevertResult
innr public static !enum Status
meth public java.util.Collection<java.io.File> getConflicts()
meth public java.util.Collection<java.io.File> getFailures()
meth public org.netbeans.libs.git.GitRevertResult$Status getStatus()
meth public org.netbeans.libs.git.GitRevisionInfo getNewHead()
supr java.lang.Object
hfds conflicts,failures,revertCommit,status

CLSS public static !enum org.netbeans.libs.git.GitRevertResult$Status
 outer org.netbeans.libs.git.GitRevertResult
fld public final static org.netbeans.libs.git.GitRevertResult$Status CONFLICTING
fld public final static org.netbeans.libs.git.GitRevertResult$Status FAILED
fld public final static org.netbeans.libs.git.GitRevertResult$Status NO_CHANGE
fld public final static org.netbeans.libs.git.GitRevertResult$Status REVERTED
fld public final static org.netbeans.libs.git.GitRevertResult$Status REVERTED_IN_INDEX
meth public static org.netbeans.libs.git.GitRevertResult$Status valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRevertResult$Status[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRevertResult$Status>

CLSS public final org.netbeans.libs.git.GitRevisionInfo
innr public final static GitFileInfo
meth public java.lang.String getFullMessage()
meth public java.lang.String getRevision()
meth public java.lang.String getShortMessage()
meth public java.lang.String[] getParents()
meth public java.util.Map<java.io.File,org.netbeans.libs.git.GitRevisionInfo$GitFileInfo> getModifiedFiles() throws org.netbeans.libs.git.GitException
meth public java.util.Map<java.lang.String,org.netbeans.libs.git.GitBranch> getBranches()
meth public long getCommitTime()
meth public org.netbeans.libs.git.GitUser getAuthor()
meth public org.netbeans.libs.git.GitUser getCommitter()
supr java.lang.Object
hfds LOG,branches,modifiedFiles,repository,revCommit,shortMessage

CLSS public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo
 outer org.netbeans.libs.git.GitRevisionInfo
innr public final static !enum Status
meth public java.io.File getFile()
meth public java.io.File getOriginalFile()
meth public java.lang.String getOriginalPath()
meth public java.lang.String getRelativePath()
meth public org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status getStatus()
supr java.lang.Object
hfds file,originalFile,originalPath,relativePath,status

CLSS public final static !enum org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status
 outer org.netbeans.libs.git.GitRevisionInfo$GitFileInfo
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status ADDED
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status COPIED
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status MODIFIED
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status REMOVED
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status RENAMED
fld public final static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status UNKNOWN
meth public static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitRevisionInfo$GitFileInfo$Status>

CLSS public final org.netbeans.libs.git.GitStatus
innr public final static !enum Status
meth public boolean isConflict()
meth public boolean isCopied()
meth public boolean isFolder()
meth public boolean isRenamed()
meth public boolean isTracked()
meth public java.io.File getFile()
meth public java.io.File getOldPath()
meth public java.lang.String getRelativePath()
meth public long getIndexEntryModificationDate()
meth public org.netbeans.libs.git.GitConflictDescriptor getConflictDescriptor()
meth public org.netbeans.libs.git.GitStatus$Status getStatusHeadIndex()
meth public org.netbeans.libs.git.GitStatus$Status getStatusHeadWC()
meth public org.netbeans.libs.git.GitStatus$Status getStatusIndexWC()
supr java.lang.Object
hfds conflictDescriptor,diffEntry,file,indexEntryModificationDate,isFolder,relativePath,statusHeadIndex,statusHeadWC,statusIndexWC,tracked,workTreePath

CLSS public final static !enum org.netbeans.libs.git.GitStatus$Status
 outer org.netbeans.libs.git.GitStatus
fld public final static org.netbeans.libs.git.GitStatus$Status STATUS_ADDED
fld public final static org.netbeans.libs.git.GitStatus$Status STATUS_IGNORED
fld public final static org.netbeans.libs.git.GitStatus$Status STATUS_MODIFIED
fld public final static org.netbeans.libs.git.GitStatus$Status STATUS_NORMAL
fld public final static org.netbeans.libs.git.GitStatus$Status STATUS_REMOVED
meth public static org.netbeans.libs.git.GitStatus$Status valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitStatus$Status[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitStatus$Status>

CLSS public final org.netbeans.libs.git.GitSubmoduleStatus
innr public final static !enum StatusType
meth public java.io.File getSubmoduleFolder()
meth public java.lang.String getHeadId()
meth public java.lang.String getReferencedCommitId()
meth public org.netbeans.libs.git.GitSubmoduleStatus$StatusType getStatus()
supr java.lang.Object
hfds delegate,folder,statusType

CLSS public final static !enum org.netbeans.libs.git.GitSubmoduleStatus$StatusType
 outer org.netbeans.libs.git.GitSubmoduleStatus
fld public final static org.netbeans.libs.git.GitSubmoduleStatus$StatusType INITIALIZED
fld public final static org.netbeans.libs.git.GitSubmoduleStatus$StatusType MISSING
fld public final static org.netbeans.libs.git.GitSubmoduleStatus$StatusType REV_CHECKED_OUT
fld public final static org.netbeans.libs.git.GitSubmoduleStatus$StatusType UNINITIALIZED
meth public static org.netbeans.libs.git.GitSubmoduleStatus$StatusType valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitSubmoduleStatus$StatusType[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitSubmoduleStatus$StatusType>

CLSS public final org.netbeans.libs.git.GitTag
meth public boolean isLightWeight()
meth public java.lang.String getMessage()
meth public java.lang.String getTagId()
meth public java.lang.String getTagName()
meth public java.lang.String getTaggedObjectId()
meth public org.netbeans.libs.git.GitObjectType getTaggedObjectType()
meth public org.netbeans.libs.git.GitUser getTagger()
supr java.lang.Object
hfds id,lightWeight,message,name,taggedObject,tagger,type

CLSS public final org.netbeans.libs.git.GitTransportUpdate
innr public final static !enum Type
meth public java.lang.String getLocalName()
meth public java.lang.String getNewObjectId()
meth public java.lang.String getOldObjectId()
meth public java.lang.String getRemoteName()
meth public java.lang.String getRemoteUri()
meth public org.netbeans.libs.git.GitRefUpdateResult getResult()
meth public org.netbeans.libs.git.GitTransportUpdate$Type getType()
supr java.lang.Object
hfds localName,newObjectId,oldObjectId,remoteName,result,type,uri

CLSS public final static !enum org.netbeans.libs.git.GitTransportUpdate$Type
 outer org.netbeans.libs.git.GitTransportUpdate
fld public final static org.netbeans.libs.git.GitTransportUpdate$Type BRANCH
fld public final static org.netbeans.libs.git.GitTransportUpdate$Type REFERENCE
fld public final static org.netbeans.libs.git.GitTransportUpdate$Type TAG
meth public static org.netbeans.libs.git.GitTransportUpdate$Type valueOf(java.lang.String)
meth public static org.netbeans.libs.git.GitTransportUpdate$Type[] values()
supr java.lang.Enum<org.netbeans.libs.git.GitTransportUpdate$Type>

CLSS public final org.netbeans.libs.git.GitURI
cons public init(java.lang.String) throws java.net.URISyntaxException
meth public boolean equals(java.lang.Object)
meth public boolean isRemote()
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getHost()
meth public java.lang.String getPass()
meth public java.lang.String getPath()
meth public java.lang.String getScheme()
meth public java.lang.String getUser()
meth public java.lang.String toPrivateString()
meth public java.lang.String toString()
meth public org.netbeans.libs.git.GitURI setHost(java.lang.String)
meth public org.netbeans.libs.git.GitURI setPass(java.lang.String)
meth public org.netbeans.libs.git.GitURI setPath(java.lang.String)
meth public org.netbeans.libs.git.GitURI setPort(int)
meth public org.netbeans.libs.git.GitURI setScheme(java.lang.String)
meth public org.netbeans.libs.git.GitURI setUser(java.lang.String)
supr java.lang.Object
hfds uri

CLSS public final org.netbeans.libs.git.GitUser
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getEmailAddress()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds email,name

CLSS public final org.netbeans.libs.git.SearchCriteria
cons public init()
meth public boolean isFollow()
meth public boolean isIncludeMerges()
meth public int getLimit()
meth public java.io.File[] getFiles()
meth public java.lang.String getMessage()
meth public java.lang.String getRevisionFrom()
meth public java.lang.String getRevisionTo()
meth public java.lang.String getUsername()
meth public java.util.Date getFrom()
meth public java.util.Date getTo()
meth public void setFiles(java.io.File[])
meth public void setFollowRenames(boolean)
meth public void setFrom(java.util.Date)
meth public void setIncludeMerges(boolean)
meth public void setLimit(int)
meth public void setMessage(java.lang.String)
meth public void setRevisionFrom(java.lang.String)
meth public void setRevisionTo(java.lang.String)
meth public void setTo(java.util.Date)
meth public void setUsername(java.lang.String)
supr java.lang.Object
hfds files,follow,from,includeMerges,limit,message,revisionFrom,revisionTo,to,username

CLSS public abstract interface org.netbeans.libs.git.progress.FileListener
intf org.netbeans.libs.git.progress.NotificationListener
meth public abstract void notifyFile(java.io.File,java.lang.String)

CLSS public abstract interface org.netbeans.libs.git.progress.NotificationListener
intf java.util.EventListener

CLSS public abstract org.netbeans.libs.git.progress.ProgressMonitor
cons public init()
fld public final static int UNKNOWN_WORK_UNITS = 0
innr public static DefaultProgressMonitor
meth public abstract boolean isCanceled()
meth public abstract void finished()
meth public abstract void notifyError(java.lang.String)
meth public abstract void notifyWarning(java.lang.String)
meth public abstract void preparationsFailed(java.lang.String)
meth public abstract void started(java.lang.String)
meth public void beginTask(java.lang.String,int)
meth public void endTask()
meth public void notifyMessage(java.lang.String)
meth public void updateTaskState(int)
supr java.lang.Object

CLSS public static org.netbeans.libs.git.progress.ProgressMonitor$DefaultProgressMonitor
 outer org.netbeans.libs.git.progress.ProgressMonitor
cons public init()
meth public final boolean cancel()
meth public final boolean isCanceled()
meth public void finished()
meth public void notifyError(java.lang.String)
meth public void notifyWarning(java.lang.String)
meth public void preparationsFailed(java.lang.String)
meth public void started(java.lang.String)
supr org.netbeans.libs.git.progress.ProgressMonitor
hfds canceled

CLSS public abstract interface org.netbeans.libs.git.progress.RevisionInfoListener
intf org.netbeans.libs.git.progress.NotificationListener
meth public abstract void notifyRevisionInfo(org.netbeans.libs.git.GitRevisionInfo)

CLSS public abstract interface org.netbeans.libs.git.progress.StatusListener
intf org.netbeans.libs.git.progress.NotificationListener
meth public abstract void notifyStatus(org.netbeans.libs.git.GitStatus)

