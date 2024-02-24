#Signature file v4.1
#Version 1.74.0

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

CLSS public abstract org.netbeans.api.diff.Diff
cons public init()
meth public abstract java.awt.Component createDiff(java.lang.String,java.lang.String,java.io.Reader,java.lang.String,java.lang.String,java.io.Reader,java.lang.String) throws java.io.IOException
meth public org.netbeans.api.diff.DiffView createDiff(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
meth public static java.util.Collection<? extends org.netbeans.api.diff.Diff> getAll()
meth public static org.netbeans.api.diff.Diff getDefault()
supr java.lang.Object

CLSS public final org.netbeans.api.diff.DiffController
fld public final static java.lang.String PROP_DIFFERENCES = "(void) differencesChanged"
innr public final static !enum DiffPane
innr public final static !enum LocationType
meth public int getDifferenceCount()
meth public int getDifferenceIndex()
meth public javax.swing.JComponent getJComponent()
meth public static org.netbeans.api.diff.DiffController create(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
meth public static org.netbeans.api.diff.DiffController createEnhanced(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setLocation(org.netbeans.api.diff.DiffController$DiffPane,org.netbeans.api.diff.DiffController$LocationType,int)
supr java.lang.Object
hfds impl
hcls DiffControllerViewBridge

CLSS public final static !enum org.netbeans.api.diff.DiffController$DiffPane
 outer org.netbeans.api.diff.DiffController
fld public final static org.netbeans.api.diff.DiffController$DiffPane Base
fld public final static org.netbeans.api.diff.DiffController$DiffPane Modified
meth public static org.netbeans.api.diff.DiffController$DiffPane valueOf(java.lang.String)
meth public static org.netbeans.api.diff.DiffController$DiffPane[] values()
supr java.lang.Enum<org.netbeans.api.diff.DiffController$DiffPane>

CLSS public final static !enum org.netbeans.api.diff.DiffController$LocationType
 outer org.netbeans.api.diff.DiffController
fld public final static org.netbeans.api.diff.DiffController$LocationType DifferenceIndex
fld public final static org.netbeans.api.diff.DiffController$LocationType LineNumber
meth public static org.netbeans.api.diff.DiffController$LocationType valueOf(java.lang.String)
meth public static org.netbeans.api.diff.DiffController$LocationType[] values()
supr java.lang.Enum<org.netbeans.api.diff.DiffController$LocationType>

CLSS public abstract interface org.netbeans.api.diff.DiffView
fld public final static java.lang.String PROP_DIFF_COUNT = "diffCount"
meth public abstract boolean canSetCurrentDifference()
meth public abstract int getCurrentDifference()
meth public abstract int getDifferenceCount()
meth public abstract java.awt.Component getComponent()
meth public abstract javax.swing.JToolBar getToolBar()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setCurrentDifference(int)

CLSS public org.netbeans.api.diff.Difference
cons public init(int,int,int,int,int)
cons public init(int,int,int,int,int,java.lang.String,java.lang.String)
cons public init(int,int,int,int,int,java.lang.String,java.lang.String,org.netbeans.api.diff.Difference$Part[],org.netbeans.api.diff.Difference$Part[])
fld public final static int ADD = 1
fld public final static int CHANGE = 2
fld public final static int DELETE = 0
innr public final static Part
intf java.io.Serializable
meth public int getFirstEnd()
meth public int getFirstStart()
meth public int getSecondEnd()
meth public int getSecondStart()
meth public int getType()
meth public java.lang.String getFirstText()
meth public java.lang.String getSecondText()
meth public java.lang.String toString()
meth public org.netbeans.api.diff.Difference$Part[] getFirstLineDiffs()
meth public org.netbeans.api.diff.Difference$Part[] getSecondLineDiffs()
supr java.lang.Object
hfds firstEnd,firstLineDiffs,firstStart,firstText,secondEnd,secondLineDiffs,secondStart,secondText,serialVersionUID,type

CLSS public final static org.netbeans.api.diff.Difference$Part
 outer org.netbeans.api.diff.Difference
cons public init(int,int,int,int)
intf java.io.Serializable
meth public int getEndPosition()
meth public int getLine()
meth public int getStartPosition()
meth public int getType()
supr java.lang.Object
hfds line,pos1,pos2,serialVersionUID,text,type

CLSS public org.netbeans.api.diff.PatchUtils
meth public static boolean isPatch(java.io.File) throws java.io.IOException
meth public static void applyPatch(java.io.File,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.api.diff.StreamSource
cons public init()
meth public abstract java.io.Reader createReader() throws java.io.IOException
meth public abstract java.io.Writer createWriter(org.netbeans.api.diff.Difference[]) throws java.io.IOException
meth public abstract java.lang.String getMIMEType()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTitle()
meth public boolean isEditable()
meth public org.openide.util.Lookup getLookup()
meth public static org.netbeans.api.diff.StreamSource createSource(java.lang.String,java.lang.String,java.lang.String,java.io.File)
meth public static org.netbeans.api.diff.StreamSource createSource(java.lang.String,java.lang.String,java.lang.String,java.io.Reader)
meth public void close()
supr java.lang.Object
hcls Impl

CLSS public abstract org.netbeans.spi.diff.DiffControllerImpl
cons protected init()
fld protected final java.beans.PropertyChangeSupport support
meth protected final void setDifferenceIndex(int)
meth public abstract int getDifferenceCount()
meth public abstract javax.swing.JComponent getJComponent()
meth public final int getDifferenceIndex()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setLocation(org.netbeans.api.diff.DiffController$DiffPane,org.netbeans.api.diff.DiffController$LocationType,int)
supr java.lang.Object
hfds differenceIndex

CLSS public abstract org.netbeans.spi.diff.DiffControllerProvider
cons public init()
meth public abstract org.netbeans.spi.diff.DiffControllerImpl createDiffController(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
meth public org.netbeans.spi.diff.DiffControllerImpl createEnhancedDiffController(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.diff.DiffProvider
cons public init()
meth public abstract org.netbeans.api.diff.Difference[] computeDiff(java.io.Reader,java.io.Reader) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.diff.DiffVisualizer
cons public init()
meth public abstract java.awt.Component createView(org.netbeans.api.diff.Difference[],java.lang.String,java.lang.String,java.io.Reader,java.lang.String,java.lang.String,java.io.Reader,java.lang.String) throws java.io.IOException
meth public org.netbeans.api.diff.DiffView createDiff(org.netbeans.api.diff.Difference[],org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.diff.MergeVisualizer
cons public init()
meth public abstract java.awt.Component createView(org.netbeans.api.diff.Difference[],org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource) throws java.io.IOException
supr java.lang.Object

