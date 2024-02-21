#Signature file v4.1
#Version 1.43

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.modules.docker.api.ActionChunkedResult
innr public static Chunk
intf java.io.Closeable
meth public org.netbeans.modules.docker.api.ActionChunkedResult$Chunk fetchChunk()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds charset,fetcher,s

CLSS public static org.netbeans.modules.docker.api.ActionChunkedResult$Chunk
 outer org.netbeans.modules.docker.api.ActionChunkedResult
meth public boolean isError()
meth public java.lang.String getData()
supr java.lang.Object
hfds data,error

CLSS public final org.netbeans.modules.docker.api.ActionStreamResult
intf java.io.Closeable
meth public boolean hasTty()
meth public java.io.InputStream getStdErr()
meth public java.io.InputStream getStdOut()
meth public java.io.OutputStream getStdIn()
meth public java.nio.charset.Charset getCharset()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds result

CLSS public final org.netbeans.modules.docker.api.BuildEvent
innr public abstract interface static Listener
innr public static Error
meth public boolean isError()
meth public boolean isUpload()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public org.netbeans.modules.docker.api.BuildEvent$Error getDetail()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.docker.api.DockerInstance getSource()
supr java.util.EventObject
hfds detail,error,instance,message,upload

CLSS public static org.netbeans.modules.docker.api.BuildEvent$Error
 outer org.netbeans.modules.docker.api.BuildEvent
cons public init(long,java.lang.String)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public long getCode()
supr java.lang.Object
hfds code,message

CLSS public abstract interface static org.netbeans.modules.docker.api.BuildEvent$Listener
 outer org.netbeans.modules.docker.api.BuildEvent
intf java.util.EventListener
meth public abstract void onEvent(org.netbeans.modules.docker.api.BuildEvent)

CLSS public final org.netbeans.modules.docker.api.Credentials
cons public init(java.lang.String,java.lang.String,char[],java.lang.String)
meth public char[] getPassword()
meth public java.lang.String getEmail()
meth public java.lang.String getRegistry()
meth public java.lang.String getUsername()
supr java.lang.Object
hfds email,password,registry,username

CLSS public final org.netbeans.modules.docker.api.CredentialsManager
meth public java.util.List<org.netbeans.modules.docker.api.Credentials> getAllCredentials() throws java.io.IOException
meth public org.netbeans.modules.docker.api.Credentials getCredentials(java.lang.String) throws java.io.IOException
meth public static org.netbeans.modules.docker.api.CredentialsManager getDefault()
meth public void removeCredentials(org.netbeans.modules.docker.api.Credentials) throws java.io.IOException
meth public void setCredentials(org.netbeans.modules.docker.api.Credentials) throws java.io.IOException
supr java.lang.Object
hfds INSTANCE

CLSS public org.netbeans.modules.docker.api.DockerAction
cons public init(org.netbeans.modules.docker.api.DockerInstance)
fld public final static java.lang.String DOCKER_FILE = "Dockerfile"
meth public boolean ping()
meth public boolean pingWithExceptions() throws java.lang.Exception
meth public java.util.List<org.netbeans.modules.docker.api.DockerContainer> getContainers()
meth public java.util.List<org.netbeans.modules.docker.api.DockerImage> getImages()
meth public java.util.List<org.netbeans.modules.docker.api.DockerRegistryImage> search(java.lang.String)
meth public java.util.concurrent.FutureTask<org.netbeans.modules.docker.api.DockerImage> createBuildTask(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.lang.String,boolean,boolean,org.netbeans.modules.docker.api.BuildEvent$Listener,org.netbeans.modules.docker.api.StatusEvent$Listener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public org.json.simple.JSONObject getRawDetails(org.netbeans.modules.docker.api.DockerEntityType,java.lang.String) throws org.netbeans.modules.docker.api.DockerException
meth public org.json.simple.JSONObject getRunningProcessesList(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.ActionChunkedResult logs(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.ActionStreamResult attach(org.netbeans.modules.docker.api.DockerContainer,boolean,boolean) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.DockerContainerDetail getDetail(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.DockerImage commit(org.netbeans.modules.docker.api.DockerContainer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.DockerImageDetail getDetail(org.netbeans.modules.docker.api.DockerImage) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.DockerTag tag(org.netbeans.modules.docker.api.DockerTag,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.docker.api.DockerException
meth public org.netbeans.modules.docker.api.DockerfileDetail getDetail(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.openide.util.Pair<org.netbeans.modules.docker.api.DockerContainer,org.netbeans.modules.docker.api.ActionStreamResult> run(java.lang.String,org.json.simple.JSONObject) throws org.netbeans.modules.docker.api.DockerException
meth public void pause(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public void pull(java.lang.String,org.netbeans.modules.docker.api.StatusEvent$Listener) throws org.netbeans.modules.docker.api.DockerException
meth public void push(org.netbeans.modules.docker.api.DockerTag,org.netbeans.modules.docker.api.StatusEvent$Listener) throws org.netbeans.modules.docker.api.DockerException
meth public void remove(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public void remove(org.netbeans.modules.docker.api.DockerTag) throws org.netbeans.modules.docker.api.DockerException
meth public void rename(org.netbeans.modules.docker.api.DockerContainer,java.lang.String) throws org.netbeans.modules.docker.api.DockerException
meth public void resizeTerminal(org.netbeans.modules.docker.api.DockerContainer,int,int) throws org.netbeans.modules.docker.api.DockerException
meth public void start(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public void stop(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
meth public void unpause(org.netbeans.modules.docker.api.DockerContainer) throws org.netbeans.modules.docker.api.DockerException
supr java.lang.Object
hfds ACCEPT_JSON_HEADER,ID_PATTERN,LOGGER,PORT_PATTERN,REMOVE_CONTAINER_CODES,REMOVE_IMAGE_CODES,START_STOP_CONTAINER_CODES,emitEvents,instance
hcls CancelHandler,DirectFetcher,EmptyStreamResult

CLSS public org.netbeans.modules.docker.api.DockerAuthenticationException
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr org.netbeans.modules.docker.api.DockerException

CLSS public org.netbeans.modules.docker.api.DockerConflictException
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr org.netbeans.modules.docker.api.DockerException

CLSS public final org.netbeans.modules.docker.api.DockerContainer
innr public final static !enum Status
intf org.netbeans.modules.docker.api.DockerInstanceEntity
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String getImage()
meth public java.lang.String getName()
meth public java.lang.String getShortId()
meth public java.lang.String toString()
meth public org.netbeans.modules.docker.api.DockerContainer$Status getStatus()
meth public org.netbeans.modules.docker.api.DockerEntityType getType()
meth public org.netbeans.modules.docker.api.DockerInstance getInstance()
supr java.lang.Object
hfds id,image,instance,name,status

CLSS public final static !enum org.netbeans.modules.docker.api.DockerContainer$Status
 outer org.netbeans.modules.docker.api.DockerContainer
fld public final static org.netbeans.modules.docker.api.DockerContainer$Status PAUSED
fld public final static org.netbeans.modules.docker.api.DockerContainer$Status RUNNING
fld public final static org.netbeans.modules.docker.api.DockerContainer$Status STOPPED
meth public static org.netbeans.modules.docker.api.DockerContainer$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.docker.api.DockerContainer$Status[] values()
supr java.lang.Enum<org.netbeans.modules.docker.api.DockerContainer$Status>

CLSS public org.netbeans.modules.docker.api.DockerContainerDetail
cons public init(java.lang.String,org.netbeans.modules.docker.api.DockerContainer$Status,boolean,boolean)
cons public init(java.lang.String,org.netbeans.modules.docker.api.DockerContainer$Status,boolean,boolean,java.util.List<org.netbeans.modules.docker.api.PortMapping>)
meth public boolean arePortExposed()
meth public boolean isStdin()
meth public boolean isTty()
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.docker.api.PortMapping> portMappings()
meth public org.netbeans.modules.docker.api.DockerContainer$Status getStatus()
supr java.lang.Object
hfds name,portMappings,status,stdin,tty

CLSS public abstract interface org.netbeans.modules.docker.api.DockerEntity
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getShortId()

CLSS public final !enum org.netbeans.modules.docker.api.DockerEntityType
fld public final static org.netbeans.modules.docker.api.DockerEntityType Container
fld public final static org.netbeans.modules.docker.api.DockerEntityType Image
meth public java.lang.String getUrlPath()
meth public static org.netbeans.modules.docker.api.DockerEntityType valueOf(java.lang.String)
meth public static org.netbeans.modules.docker.api.DockerEntityType[] values()
supr java.lang.Enum<org.netbeans.modules.docker.api.DockerEntityType>
hfds urlPath

CLSS public final org.netbeans.modules.docker.api.DockerEvent
innr public abstract interface static Listener
innr public final static !enum Status
meth public boolean equals(java.lang.Object)
meth public boolean equalsIgnoringTime(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getFrom()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public long getTime()
meth public org.netbeans.modules.docker.api.DockerEvent$Status getStatus()
meth public org.netbeans.modules.docker.api.DockerInstance getSource()
supr java.util.EventObject
hfds from,id,instance,status,time

CLSS public abstract interface static org.netbeans.modules.docker.api.DockerEvent$Listener
 outer org.netbeans.modules.docker.api.DockerEvent
intf java.util.EventListener
meth public abstract void onEvent(org.netbeans.modules.docker.api.DockerEvent)

CLSS public final static !enum org.netbeans.modules.docker.api.DockerEvent$Status
 outer org.netbeans.modules.docker.api.DockerEvent
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status ATTACH
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status COMMIT
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status COPY
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status CREATE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status DELETE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status DESTROY
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status DIE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status EXEC_CREATE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status EXEC_START
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status EXPORT
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status IMPORT
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status KILL
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status OOM
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status PAUSE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status PULL
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status PUSH
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status RENAME
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status RESIZE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status RESTART
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status START
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status STOP
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status TAG
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status TOP
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status UNPAUSE
fld public final static org.netbeans.modules.docker.api.DockerEvent$Status UNTAG
meth public boolean isContainer()
meth public java.lang.String getText()
meth public static org.netbeans.modules.docker.api.DockerEvent$Status parse(java.lang.String)
meth public static org.netbeans.modules.docker.api.DockerEvent$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.docker.api.DockerEvent$Status[] values()
supr java.lang.Enum<org.netbeans.modules.docker.api.DockerEvent$Status>
hfds VALUES,container,text

CLSS public org.netbeans.modules.docker.api.DockerException
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public final org.netbeans.modules.docker.api.DockerImage
intf org.netbeans.modules.docker.api.DockerInstanceEntity
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String getShortId()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.docker.api.DockerTag> getTags()
meth public long getCreated()
meth public long getSize()
meth public long getVirtualSize()
meth public org.netbeans.modules.docker.api.DockerEntityType getType()
meth public org.netbeans.modules.docker.api.DockerInstance getInstance()
supr java.lang.Object
hfds created,id,instance,size,tags,virtualSize

CLSS public org.netbeans.modules.docker.api.DockerImageDetail
cons public init(java.util.List<org.netbeans.modules.docker.api.ExposedPort>)
meth public java.util.List<org.netbeans.modules.docker.api.ExposedPort> getExposedPorts()
supr java.lang.Object
hfds exposedPorts

CLSS public org.netbeans.modules.docker.api.DockerInstance
innr public abstract interface static ConnectionListener
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getCaCertificateFile()
meth public java.io.File getCertificateFile()
meth public java.io.File getKeyFile()
meth public java.lang.String getDisplayName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public static org.netbeans.modules.docker.api.DockerInstance getInstance(java.lang.String,java.lang.String,java.io.File,java.io.File,java.io.File)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addConnectionListener(org.netbeans.modules.docker.api.DockerInstance$ConnectionListener)
meth public void addContainerListener(org.netbeans.modules.docker.api.DockerEvent$Listener)
meth public void addImageListener(org.netbeans.modules.docker.api.DockerEvent$Listener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeConnectionListener(org.netbeans.modules.docker.api.DockerInstance$ConnectionListener)
meth public void removeContainerListener(org.netbeans.modules.docker.api.DockerEvent$Listener)
meth public void removeImageListener(org.netbeans.modules.docker.api.DockerEvent$Listener)
supr java.lang.Object
hfds CA_CERTIFICATE_PATH_KEY,CERTIFICATE_PATH_KEY,DISPLAY_NAME_KEY,INSTANCES_KEY,KEY_PATH_KEY,LOGGER,URL_KEY,caCertificate,certificate,changeSupport,displayName,eventBus,key,listener,prefs,url
hcls InstanceListener

CLSS public abstract interface static org.netbeans.modules.docker.api.DockerInstance$ConnectionListener
 outer org.netbeans.modules.docker.api.DockerInstance
intf java.util.EventListener
meth public abstract void onConnect()
meth public abstract void onDisconnect()

CLSS public abstract interface org.netbeans.modules.docker.api.DockerInstanceEntity
intf org.netbeans.modules.docker.api.DockerEntity
meth public abstract org.netbeans.modules.docker.api.DockerEntityType getType()
meth public abstract org.netbeans.modules.docker.api.DockerInstance getInstance()

CLSS public org.netbeans.modules.docker.api.DockerName
meth public java.lang.String getNamespace()
meth public java.lang.String getRegistry()
meth public java.lang.String getRepository()
meth public java.lang.String getTag()
meth public static org.netbeans.modules.docker.api.DockerName parse(java.lang.String)
supr java.lang.Object
hfds namespace,registry,repository,tag

CLSS public org.netbeans.modules.docker.api.DockerRegistryImage
cons public init(java.lang.String,java.lang.String,long,boolean,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isAutomated()
meth public boolean isOfficial()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public long getStars()
supr java.lang.Object
hfds automated,description,name,official,stars

CLSS public final org.netbeans.modules.docker.api.DockerSupport
meth public boolean isSocketSupported()
meth public java.util.Collection<? extends org.netbeans.modules.docker.api.DockerInstance> getInstances()
meth public org.netbeans.modules.docker.api.DockerInstance addInstance(org.netbeans.modules.docker.api.DockerInstance)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.docker.api.DockerSupport getDefault()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeInstance(org.netbeans.modules.docker.api.DockerInstance)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER,changeSupport,initialized,instances,support

CLSS public final org.netbeans.modules.docker.api.DockerTag
intf org.netbeans.modules.docker.api.DockerInstanceEntity
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String getShortId()
meth public java.lang.String getTag()
meth public java.lang.String toString()
meth public org.netbeans.modules.docker.api.DockerEntityType getType()
meth public org.netbeans.modules.docker.api.DockerImage getImage()
meth public org.netbeans.modules.docker.api.DockerInstance getInstance()
supr java.lang.Object
hfds image,tag

CLSS public org.netbeans.modules.docker.api.DockerfileDetail
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public java.util.Map<java.lang.String,java.lang.String> getBuildArgs()
supr java.lang.Object
hfds buildArgs

CLSS public org.netbeans.modules.docker.api.ExposedPort
cons public init(int,org.netbeans.modules.docker.api.ExposedPort$Type)
innr public final static !enum Type
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public org.netbeans.modules.docker.api.ExposedPort$Type getType()
supr java.lang.Object
hfds port,type

CLSS public final static !enum org.netbeans.modules.docker.api.ExposedPort$Type
 outer org.netbeans.modules.docker.api.ExposedPort
fld public final static org.netbeans.modules.docker.api.ExposedPort$Type TCP
fld public final static org.netbeans.modules.docker.api.ExposedPort$Type UDP
meth public static org.netbeans.modules.docker.api.ExposedPort$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.docker.api.ExposedPort$Type[] values()
supr java.lang.Enum<org.netbeans.modules.docker.api.ExposedPort$Type>

CLSS public org.netbeans.modules.docker.api.PortMapping
cons public init(org.netbeans.modules.docker.api.ExposedPort$Type,java.lang.Integer,java.lang.Integer,java.lang.String)
meth public java.lang.Integer getHostPort()
meth public java.lang.Integer getPort()
meth public java.lang.String getHostAddress()
meth public org.netbeans.modules.docker.api.ExposedPort$Type getType()
supr java.lang.Object
hfds hostAddress,hostPort,port,type

CLSS public final org.netbeans.modules.docker.api.StatusEvent
innr public abstract interface static Listener
innr public static Progress
meth public boolean isError()
meth public java.lang.String getId()
meth public java.lang.String getMessage()
meth public java.lang.String getProgress()
meth public java.lang.String toString()
meth public org.netbeans.modules.docker.api.DockerInstance getSource()
meth public org.netbeans.modules.docker.api.StatusEvent$Progress getDetail()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.util.EventObject
hfds detail,error,id,instance,message,progress

CLSS public abstract interface static org.netbeans.modules.docker.api.StatusEvent$Listener
 outer org.netbeans.modules.docker.api.StatusEvent
intf java.util.EventListener
meth public abstract void onEvent(org.netbeans.modules.docker.api.StatusEvent)

CLSS public static org.netbeans.modules.docker.api.StatusEvent$Progress
 outer org.netbeans.modules.docker.api.StatusEvent
cons public init(long,long)
meth public java.lang.String toString()
meth public long getCurrent()
meth public long getTotal()
supr java.lang.Object
hfds current,total

