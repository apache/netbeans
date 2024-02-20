#Signature file v4.1
#Version 1.56

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

CLSS public org.netbeans.modules.db.dataview.api.DataView
meth public boolean hasExceptions()
meth public boolean hasResultSet()
meth public boolean hasWarnings()
meth public int getErrorPosition()
meth public int getUpdateCount()
meth public java.util.Collection<java.lang.Throwable> getExceptions()
meth public java.util.Collection<java.sql.SQLWarning> getWarnings()
meth public java.util.List<java.awt.Component> createComponents()
meth public java.util.List<java.lang.Integer> getUpdateCounts()
meth public java.util.List<java.lang.Long> getFetchTimes()
meth public javax.swing.JButton[] getEditButtons()
meth public long getExecutionTime()
meth public static org.netbeans.modules.db.dataview.api.DataView create(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String,int)
meth public static org.netbeans.modules.db.dataview.api.DataView create(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String,int,boolean)
meth public void setEditable(boolean)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.db.dataview.api.DataViewPageContext
fld public final static int DEFAULT_PAGE_SIZE = 100
meth public static int getPageSize(org.netbeans.modules.db.dataview.api.DataView)
meth public static int getStoredPageSize()
meth public static void setStoredPageSize(int)
supr java.lang.Object
hfds PROP_STORED_PAGE_SIZE,defaultPageSize

CLSS public abstract interface org.netbeans.modules.db.dataview.spi.DBConnectionProvider
meth public abstract java.sql.Connection getConnection(org.netbeans.api.db.explorer.DatabaseConnection)
meth public abstract void closeConnection(java.sql.Connection)

