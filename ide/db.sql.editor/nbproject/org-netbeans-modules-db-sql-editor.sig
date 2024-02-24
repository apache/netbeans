#Signature file v4.1
#Version 1.60.0

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

CLSS public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion
fld public final static java.lang.String UNKNOWN_TAG = "__UNKNOWN__"
meth public static boolean canComplete(org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext)
meth public static org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion create(org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext)
meth public void query(org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet,org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler)
supr java.lang.Object
hfds delegate,initContext

CLSS public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext
meth public int getOffset()
meth public java.lang.CharSequence getStatement()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext setDatabaseConnection(org.netbeans.api.db.explorer.DatabaseConnection)
meth public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext setOffset(int)
meth public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext setStatement(java.lang.CharSequence)
meth public static org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext empty()
supr java.lang.Object
hfds dbconn,offset,statement

CLSS public org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet
meth public int getAnchorOffset()
meth public java.util.List<org.netbeans.spi.editor.completion.CompletionItem> getItems()
meth public static org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet create()
meth public void addAllItems(java.util.Collection<? extends org.netbeans.spi.editor.completion.CompletionItem>)
meth public void addItem(org.netbeans.spi.editor.completion.CompletionItem)
meth public void setAnchorOffset(int)
supr java.lang.Object
hfds anchorOffset,items

CLSS public abstract interface org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler
meth public abstract void substituteText(javax.swing.text.JTextComponent,int,java.lang.String)

