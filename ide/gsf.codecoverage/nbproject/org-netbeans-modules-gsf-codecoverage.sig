#Signature file v4.1
#Version 1.55

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

CLSS public org.netbeans.modules.gsf.codecoverage.api.CoverageActionFactory
cons public init()
meth public static javax.swing.Action createCollectorAction(javax.swing.Action,javax.swing.Action[])
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.gsf.codecoverage.api.CoverageManager
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageManager INSTANCE
meth public abstract boolean isAggregating(org.netbeans.api.project.Project)
meth public abstract boolean isEnabled(org.netbeans.api.project.Project)
meth public abstract void resultsUpdated(org.netbeans.api.project.Project,org.netbeans.modules.gsf.codecoverage.api.CoverageProvider)
meth public abstract void setEnabled(org.netbeans.api.project.Project,boolean)

CLSS public abstract interface org.netbeans.modules.gsf.codecoverage.api.CoverageProvider
meth public abstract boolean isAggregating()
meth public abstract boolean isEnabled()
meth public abstract boolean supportsAggregation()
meth public abstract boolean supportsHitCounts()
meth public abstract java.lang.String getTestAllAction()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.List<org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary> getResults()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails getDetails(org.openide.filesystems.FileObject,javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void clear()
meth public abstract void setAggregating(boolean)
meth public abstract void setEnabled(boolean)

CLSS public org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper
meth public static boolean isAggregating(org.netbeans.api.project.Project)
meth public static boolean isEnabled(org.netbeans.api.project.Project)
meth public static void setAggregating(org.netbeans.api.project.Project,boolean)
meth public static void setEnabled(org.netbeans.api.project.Project,boolean)
supr java.lang.Object
hfds COVERAGE_NAMESPACE_URI

CLSS public final !enum org.netbeans.modules.gsf.codecoverage.api.CoverageType
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageType COVERED
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageType INFERRED
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageType NOT_COVERED
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageType PARTIAL
fld public final static org.netbeans.modules.gsf.codecoverage.api.CoverageType UNKNOWN
meth public java.lang.String getDescription()
meth public static org.netbeans.modules.gsf.codecoverage.api.CoverageType valueOf(java.lang.String)
meth public static org.netbeans.modules.gsf.codecoverage.api.CoverageType[] values()
supr java.lang.Enum<org.netbeans.modules.gsf.codecoverage.api.CoverageType>
hfds desc

CLSS public abstract interface org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails
meth public abstract boolean hasHitCounts()
meth public abstract int getHitCount(int)
meth public abstract int getLineCount()
meth public abstract long lastUpdated()
meth public abstract org.netbeans.modules.gsf.codecoverage.api.CoverageType getType(int)
meth public abstract org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary getSummary()
meth public abstract org.openide.filesystems.FileObject getFile()

CLSS public org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary
cons public init(org.openide.filesystems.FileObject,java.lang.String,int,int,int,int)
intf java.lang.Comparable<org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary>
meth public float getCoveragePercentage()
meth public int compareTo(org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary)
meth public int getExecutedLineCount()
meth public int getInferredCount()
meth public int getLineCount()
meth public int getPartialCount()
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds displayName,executedLineCount,file,inferredCount,lineCount,partialCount

