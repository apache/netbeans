#Signature file v4.1
#Version 1.66

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

CLSS public final org.netbeans.modules.editor.indent.api.Indent
meth public int indentNewLine(int) throws javax.swing.text.BadLocationException
meth public static org.netbeans.modules.editor.indent.api.Indent get(javax.swing.text.Document)
meth public void lock()
meth public void reindent(int) throws javax.swing.text.BadLocationException
meth public void reindent(int,int) throws javax.swing.text.BadLocationException
meth public void unlock()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.editor.indent.api.IndentUtils
meth public static boolean isExpandTabs(javax.swing.text.Document)
meth public static int indentLevelSize(javax.swing.text.Document)
meth public static int lineIndent(javax.swing.text.Document,int) throws javax.swing.text.BadLocationException
meth public static int lineStartOffset(javax.swing.text.Document,int) throws javax.swing.text.BadLocationException
meth public static int tabSize(javax.swing.text.Document)
meth public static java.lang.String createIndentString(int,boolean,int)
meth public static java.lang.String createIndentString(javax.swing.text.Document,int)
supr java.lang.Object
hfds LOG,MAX_CACHED_INDENT,MAX_CACHED_TAB_SIZE,cachedSpacesStrings,cachedTabIndents

CLSS public final org.netbeans.modules.editor.indent.api.Reformat
meth public static org.netbeans.modules.editor.indent.api.Reformat get(javax.swing.text.Document)
meth public void lock()
meth public void reformat(int,int) throws javax.swing.text.BadLocationException
meth public void unlock()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.editor.indent.spi.CodeStylePreferences
innr public abstract interface static Provider
meth public java.util.prefs.Preferences getPreferences()
meth public static org.netbeans.modules.editor.indent.spi.CodeStylePreferences get(javax.swing.text.Document)
meth public static org.netbeans.modules.editor.indent.spi.CodeStylePreferences get(javax.swing.text.Document,java.lang.String)
meth public static org.netbeans.modules.editor.indent.spi.CodeStylePreferences get(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.editor.indent.spi.CodeStylePreferences get(org.openide.filesystems.FileObject,java.lang.String)
supr java.lang.Object
hfds LOG,defaultProvider,docOrFile,mimeType
hcls CachingPreferences

CLSS public abstract interface static org.netbeans.modules.editor.indent.spi.CodeStylePreferences$Provider
 outer org.netbeans.modules.editor.indent.spi.CodeStylePreferences
meth public abstract java.util.prefs.Preferences forDocument(javax.swing.text.Document,java.lang.String)
meth public abstract java.util.prefs.Preferences forFile(org.openide.filesystems.FileObject,java.lang.String)

CLSS public final org.netbeans.modules.editor.indent.spi.Context
innr public final static Region
meth public boolean isIndent()
meth public int caretOffset()
meth public int endOffset()
meth public int lineIndent(int) throws javax.swing.text.BadLocationException
meth public int lineStartOffset(int) throws javax.swing.text.BadLocationException
meth public int startOffset()
meth public java.lang.String mimePath()
meth public java.util.List<org.netbeans.modules.editor.indent.spi.Context$Region> indentRegions()
meth public javax.swing.text.Document document()
meth public org.openide.util.Lookup getLookup()
meth public void modifyIndent(int,int) throws javax.swing.text.BadLocationException
meth public void modifyIndent(int,int,java.lang.String) throws javax.swing.text.BadLocationException
meth public void setCaretOffset(int) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds mimeItem
hcls PackageAccessor

CLSS public final static org.netbeans.modules.editor.indent.spi.Context$Region
 outer org.netbeans.modules.editor.indent.spi.Context
meth public int getEndOffset()
meth public int getStartOffset()
supr java.lang.Object
hfds region

CLSS public abstract interface org.netbeans.modules.editor.indent.spi.ExtraLock
meth public abstract void lock()
meth public abstract void unlock()

CLSS public abstract interface org.netbeans.modules.editor.indent.spi.IndentTask
innr public abstract interface static Factory
meth public abstract org.netbeans.modules.editor.indent.spi.ExtraLock indentLock()
meth public abstract void reindent() throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.modules.editor.indent.spi.IndentTask$Factory
 outer org.netbeans.modules.editor.indent.spi.IndentTask
meth public abstract org.netbeans.modules.editor.indent.spi.IndentTask createTask(org.netbeans.modules.editor.indent.spi.Context)

CLSS public abstract interface org.netbeans.modules.editor.indent.spi.ReformatTask
innr public abstract interface static Factory
meth public abstract org.netbeans.modules.editor.indent.spi.ExtraLock reformatLock()
meth public abstract void reformat() throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.modules.editor.indent.spi.ReformatTask$Factory
 outer org.netbeans.modules.editor.indent.spi.ReformatTask
meth public abstract org.netbeans.modules.editor.indent.spi.ReformatTask createTask(org.netbeans.modules.editor.indent.spi.Context)

