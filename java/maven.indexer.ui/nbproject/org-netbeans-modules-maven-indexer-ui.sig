#Signature file v4.1
#Version 2.57

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer
fld public final static java.lang.String HINT_ARTIFACT = "art"
fld public final static java.lang.String HINT_DEPENDENCIES = "dep"
fld public final static java.lang.String HINT_GRAPH = "grf"
fld public final static java.lang.String HINT_PROJECT = "prj"
meth public static void showArtifactViewer(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.lang.String)
meth public static void showArtifactViewer(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ui.ArtifactNodeSelector
meth public abstract void select(org.netbeans.modules.maven.indexer.api.NBVersionInfo)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory
meth public abstract org.openide.util.Lookup createLookup(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract org.openide.util.Lookup createLookup(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.util.Lookup createLookup(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.windows.TopComponent createTopComponent(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider
meth public abstract org.netbeans.core.spi.multiview.MultiViewDescription createPanel(org.openide.util.Lookup)

