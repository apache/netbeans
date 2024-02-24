#Signature file v4.1
#Version 1.29

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

CLSS public final org.netbeans.api.maven.MavenActions
cons public init()
supr java.lang.Object

CLSS public final org.netbeans.api.maven.archetype.Archetype
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getArtifactId()
meth public java.lang.String getDescription()
meth public java.lang.String getGroupId()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRepository()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setArtifactId(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setRepository(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds delegate

CLSS public org.netbeans.api.maven.archetype.ArchetypeWizards
meth public static java.util.Set<org.openide.filesystems.FileObject> openProjects(java.io.File,java.io.File) throws java.io.IOException
meth public static org.openide.WizardDescriptor$InstantiatingIterator<?> definedArchetype(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static void createFromArchetype(java.io.File,org.netbeans.api.maven.archetype.ProjectInfo,org.netbeans.api.maven.archetype.Archetype,java.util.Map<java.lang.String,java.lang.String>,boolean) throws java.io.IOException
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public final org.netbeans.api.maven.archetype.ProjectInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getPackageName()
meth public java.lang.String getVersion()
supr java.lang.Object
hfds delegate

