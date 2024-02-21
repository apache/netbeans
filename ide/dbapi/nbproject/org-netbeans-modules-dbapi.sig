#Signature file v4.1
#Version 1.57.0

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

CLSS public abstract interface org.netbeans.modules.db.api.explorer.ActionProvider
meth public abstract java.util.List<javax.swing.Action> getActions()

CLSS public abstract interface org.netbeans.modules.db.api.explorer.MetaDataListener
meth public abstract void tableChanged(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String)
meth public abstract void tablesChanged(org.netbeans.api.db.explorer.DatabaseConnection)

CLSS public abstract interface org.netbeans.modules.db.api.explorer.NodeProvider
meth public abstract java.util.List<org.openide.nodes.Node> getNodes()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.modules.db.api.metadata.DBConnMetadataModelManager
meth public static org.netbeans.modules.db.metadata.model.api.MetadataModel get(org.netbeans.api.db.explorer.DatabaseConnection)
supr java.lang.Object
hfds LOGGER,mgr

CLSS public final org.netbeans.modules.db.api.sql.SQLKeywords
meth public static boolean isSQL99Keyword(java.lang.String)
meth public static boolean isSQL99Keyword(java.lang.String,boolean)
meth public static boolean isSQL99NonReservedKeyword(java.lang.String)
meth public static boolean isSQL99ReservedKeyword(java.lang.String)
supr java.lang.Object
hfds SQL99_NON_RESERVED,SQL99_RESERVED

