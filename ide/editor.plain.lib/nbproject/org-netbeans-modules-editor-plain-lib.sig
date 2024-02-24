#Signature file v4.1
#Version 1.56

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public org.netbeans.editor.Syntax
cons public init()
fld protected boolean lastBuffer
fld protected char[] buffer
fld protected int offset
fld protected int state
fld protected int stopOffset
fld protected int stopPosition
fld protected int tokenLength
fld protected int tokenOffset
fld protected org.netbeans.editor.TokenContextPath tokenContextPath
fld protected org.netbeans.editor.TokenID supposedTokenID
fld public final static int DIFFERENT_STATE = 1
fld public final static int EQUAL_STATE = 0
fld public final static int INIT = -1
innr public abstract interface static StateInfo
innr public static BaseStateInfo
meth protected org.netbeans.editor.TokenID parseToken()
meth public char[] getBuffer()
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public int getOffset()
meth public int getPreScan()
meth public int getTokenLength()
meth public int getTokenOffset()
meth public java.lang.String getStateName(int)
meth public java.lang.String toString()
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenID getSupposedTokenID()
meth public org.netbeans.editor.TokenID nextToken()
meth public void load(org.netbeans.editor.Syntax$StateInfo,char[],int,int,boolean,int)
meth public void loadInitState()
meth public void loadState(org.netbeans.editor.Syntax$StateInfo)
meth public void relocate(char[],int,int,boolean,int)
meth public void reset()
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr java.lang.Object

CLSS public org.netbeans.editor.TokenContext
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.editor.TokenContext[])
meth protected void addDeclaredTokenIDs() throws java.lang.IllegalAccessException
meth protected void addTokenID(org.netbeans.editor.TokenID)
meth public java.lang.String getNamePrefix()
meth public org.netbeans.editor.TokenCategory[] getTokenCategories()
meth public org.netbeans.editor.TokenContextPath getContextPath()
meth public org.netbeans.editor.TokenContextPath getContextPath(org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenContextPath[] getAllContextPaths()
meth public org.netbeans.editor.TokenContext[] getChildren()
meth public org.netbeans.editor.TokenID[] getTokenIDs()
supr java.lang.Object
hfds EMPTY_CHILDREN,allContextPaths,children,contextPath,lastContextPathPair,namePrefix,pathCache,tokenCategories,tokenCategoryList,tokenIDList,tokenIDs

CLSS public org.netbeans.editor.ext.plain.PlainSyntax
 anno 0 java.lang.Deprecated()
cons public init()
meth protected org.netbeans.editor.TokenID parseToken()
supr org.netbeans.editor.Syntax
hfds ISI_TEXT

CLSS public org.netbeans.editor.ext.plain.PlainTokenContext
 anno 0 java.lang.Deprecated()
fld public final static int EOL_ID = 2
fld public final static int TEXT_ID = 1
fld public final static org.netbeans.editor.BaseImageTokenID EOL
fld public final static org.netbeans.editor.BaseTokenID TEXT
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.editor.ext.plain.PlainTokenContext context
supr org.netbeans.editor.TokenContext

