#Signature file v4.1
#Version 7.0.6.0

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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.IllegalStateException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract interface java.nio.channels.Channel
intf java.io.Closeable
meth public abstract boolean isOpen()
meth public abstract void close() throws java.io.IOException

CLSS public java.util.concurrent.CancellationException
cons public init()
cons public init(java.lang.String)
supr java.lang.IllegalStateException

CLSS public abstract interface java.util.function.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Object> java.util.function.Function<{%%0},{java.util.function.Function%1}> compose(java.util.function.Function<? super {%%0},? extends {java.util.function.Function%0}>)
meth public <%0 extends java.lang.Object> java.util.function.Function<{java.util.function.Function%0},{%%0}> andThen(java.util.function.Function<? super {java.util.function.Function%1},? extends {%%0}>)
meth public abstract {java.util.function.Function%1} apply({java.util.function.Function%0})
meth public static <%0 extends java.lang.Object> java.util.function.Function<{%%0},{%%0}> identity()

CLSS public abstract interface javax.security.auth.callback.CallbackHandler
meth public abstract void handle(javax.security.auth.callback.Callback[]) throws java.io.IOException,javax.security.auth.callback.UnsupportedCallbackException

CLSS public abstract interface org.apache.sshd.agent.SshAgent
fld public final static java.lang.String SSH_AUTHSOCKET_ENV_NAME = "SSH_AUTH_SOCK"
intf java.nio.channels.Channel
meth public abstract !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public abstract java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public abstract java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public abstract void removeAllIdentities() throws java.io.IOException
meth public abstract void removeIdentity(java.security.PublicKey) throws java.io.IOException
meth public java.security.KeyPair resolveLocalIdentity(java.security.PublicKey)

CLSS public abstract interface org.apache.sshd.agent.SshAgentFactory
meth public abstract java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public abstract org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public abstract org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.common.auth.UserAuthFactoriesManager<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth,org.apache.sshd.client.auth.UserAuthFactory>
intf org.apache.sshd.common.keyprovider.KeyIdentityProviderHolder
meth public abstract java.lang.String removePasswordIdentity(java.lang.String)
meth public abstract java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public abstract org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public abstract org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public abstract org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public abstract org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public abstract org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public abstract org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public abstract org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public abstract void addPasswordIdentity(java.lang.String)
meth public abstract void addPublicKeyIdentity(java.security.KeyPair)
meth public abstract void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public abstract void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public abstract void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public abstract void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public abstract void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public abstract void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void setUserAuthFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public abstract interface org.apache.sshd.client.ClientFactoryManager
intf org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderManager
intf org.apache.sshd.client.session.ClientProxyConnectorHolder
intf org.apache.sshd.client.session.ClientSessionCreator
intf org.apache.sshd.common.FactoryManager
intf org.apache.sshd.common.config.keys.FilePasswordProviderManager
meth public abstract org.apache.sshd.client.config.hosts.HostConfigEntryResolver getHostConfigEntryResolver()
meth public abstract void setHostConfigEntryResolver(org.apache.sshd.client.config.hosts.HostConfigEntryResolver)

CLSS public org.apache.sshd.client.SshClient
cons public init()
fld protected java.util.List<org.apache.sshd.client.auth.UserAuthFactory> userAuthFactories
fld protected org.apache.sshd.client.session.SessionFactory sessionFactory
fld protected org.apache.sshd.common.io.IoConnector connector
fld public final static java.util.List<org.apache.sshd.client.auth.UserAuthFactory> DEFAULT_USER_AUTH_FACTORIES
fld public final static java.util.List<org.apache.sshd.common.ServiceFactory> DEFAULT_SERVICE_FACTORIES
fld public final static org.apache.sshd.common.Factory<org.apache.sshd.client.SshClient> DEFAULT_SSH_CLIENT_FACTORY
intf org.apache.sshd.client.ClientFactoryManager
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> parseProxyJumps(java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> parseProxyJumps(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth protected org.apache.sshd.client.config.hosts.HostConfigEntry resolveHost(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth protected org.apache.sshd.client.future.ConnectFuture doConnect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException
meth protected org.apache.sshd.client.future.ConnectFuture doConnect(org.apache.sshd.client.config.hosts.HostConfigEntry,java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry>,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth protected org.apache.sshd.client.session.SessionFactory createSessionFactory()
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.io.IoConnectFuture> createConnectCompletionListener(org.apache.sshd.client.future.ConnectFuture,java.lang.String,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry)
meth protected org.apache.sshd.common.io.IoConnector createConnector()
meth protected org.apache.sshd.common.keyprovider.KeyIdentityProvider ensureFilePasswordProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth protected org.apache.sshd.common.keyprovider.KeyIdentityProvider preloadClientIdentities(java.util.Collection<? extends org.apache.sshd.common.NamedResource>) throws java.io.IOException
meth protected void checkConfig()
meth protected void onConnectOperationComplete(org.apache.sshd.common.io.IoSession,org.apache.sshd.client.future.ConnectFuture,java.lang.String,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException,java.security.GeneralSecurityException
meth protected void setupDefaultSessionIdentities(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.keyprovider.KeyIdentityProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static <%0 extends org.apache.sshd.client.SshClient> {%%0} setKeyPairProvider({%%0},boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static <%0 extends org.apache.sshd.client.SshClient> {%%0} setKeyPairProvider({%%0},java.nio.file.Path,boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean isStarted()
meth public java.lang.String removePasswordIdentity(java.lang.String)
meth public java.lang.String toString()
meth public java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public java.util.List<org.apache.sshd.client.auth.UserAuthFactory> getUserAuthFactories()
meth public org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public org.apache.sshd.client.config.hosts.HostConfigEntryResolver getHostConfigEntryResolver()
meth public org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public org.apache.sshd.client.session.SessionFactory getSessionFactory()
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
meth public org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public static org.apache.sshd.client.SshClient setUpDefaultClient()
meth public static org.apache.sshd.client.simple.SimpleClient setUpDefaultSimpleClient()
meth public static org.apache.sshd.client.simple.SimpleClient wrapAsSimpleClient(org.apache.sshd.client.SshClient)
meth public void addPasswordIdentity(java.lang.String)
meth public void addPublicKeyIdentity(java.security.KeyPair)
meth public void open() throws java.io.IOException
meth public void setClientIdentityLoader(org.apache.sshd.client.config.keys.ClientIdentityLoader)
meth public void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)
meth public void setFilePasswordProvider(org.apache.sshd.common.config.keys.FilePasswordProvider)
meth public void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public void setHostConfigEntryResolver(org.apache.sshd.client.config.hosts.HostConfigEntryResolver)
meth public void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public void setSessionFactory(org.apache.sshd.client.session.SessionFactory)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.client.auth.UserAuthFactory>)
meth public void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void start()
meth public void stop()
supr org.apache.sshd.common.helpers.AbstractFactoryManager
hfds clientIdentityLoader,filePasswordProvider,hostBasedAuthenticationReporter,hostConfigEntryResolver,identities,identitiesProvider,keyIdentityProvider,passwordAuthenticationReporter,passwordIdentityProvider,proxyConnector,publicKeyAuthenticationReporter,serverKeyVerifier,started,userInteraction

CLSS public abstract org.apache.sshd.client.auth.AbstractUserAuth
cons protected init(java.lang.String)
intf org.apache.sshd.client.auth.UserAuth
meth protected abstract boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected abstract boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected void setCancellable(boolean)
meth public boolean isCancellable()
meth public boolean process(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public java.lang.String getService()
meth public java.lang.String toString()
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public void destroy()
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds cancellable,clientSession,name,service

CLSS public abstract org.apache.sshd.client.auth.AbstractUserAuthFactory
cons protected init(java.lang.String)
intf org.apache.sshd.client.auth.UserAuthFactory
supr org.apache.sshd.common.auth.AbstractUserAuthMethodFactory<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth>

CLSS public abstract interface org.apache.sshd.client.auth.UserAuth
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.auth.UserAuthInstance<org.apache.sshd.client.session.ClientSession>
meth public abstract boolean process(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract void destroy()
meth public abstract void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public boolean isCancellable()
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.auth.UserAuthFactory
intf org.apache.sshd.common.auth.UserAuthMethodFactory<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth>

CLSS public abstract interface org.apache.sshd.client.auth.keyboard.UserInteraction
fld public final static boolean DEFAULT_AUTO_DETECT_PASSWORD_PROMPT = true
fld public final static java.lang.String AUTO_DETECT_PASSWORD_PROMPT = "user-interaction-auto-detect-password-prompt"
fld public final static java.lang.String CHECK_INTERACTIVE_PASSWORD_DELIM = "user-interaction-check-password-delimiter"
fld public final static java.lang.String DEFAULT_CHECK_INTERACTIVE_PASSWORD_DELIM = ":"
fld public final static java.lang.String DEFAULT_INTERACTIVE_PASSWORD_PROMPT = "password"
fld public final static java.lang.String INTERACTIVE_PASSWORD_PROMPT = "user-interaction-password-prompt"
fld public final static org.apache.sshd.client.auth.keyboard.UserInteraction NONE
meth public abstract java.lang.String getUpdatedPassword(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)
meth public abstract java.lang.String[] interactive(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])
meth public boolean isInteractionAllowed(org.apache.sshd.client.session.ClientSession)
meth public java.lang.String resolveAuthPasswordAttempt(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
meth public java.security.KeyPair resolveAuthPublicKeyIdentityAttempt(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
meth public static int findPromptComponentLastPosition(java.lang.String,java.lang.String)
meth public void serverVersionInfo(org.apache.sshd.client.session.ClientSession,java.util.List<java.lang.String>)
meth public void welcome(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)

CLSS public org.apache.sshd.client.auth.pubkey.UserAuthPublicKey
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld protected final java.util.Deque<java.lang.String> currentAlgorithms
fld protected java.lang.String chosenAlgorithm
fld protected java.util.Iterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity> keys
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> factories
fld protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity current
fld public final static java.lang.String NAME = "publickey"
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Boolean> USE_DEFAULT_IDENTITIES
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.String> IDENTITY_AGENT
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected byte[] appendSignature(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.security.PublicKey,java.security.PublicKey,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected java.lang.String getDefaultSignatureAlgorithm(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.client.auth.pubkey.PublicKeyIdentity,java.security.KeyPair,java.lang.String) throws java.lang.Exception
meth protected java.util.Iterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity> createPublicKeyIterator(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager) throws java.lang.Exception
meth protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity resolveAttemptedPublicKeyIdentity(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity resolveAttemptedPublicKeyIdentity(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter) throws java.lang.Exception
meth protected void releaseKeys() throws java.io.IOException
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void destroy()
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth

CLSS public org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "publickey"
fld public final static org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public org.apache.sshd.client.auth.pubkey.UserAuthPublicKey createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.client.auth.AbstractUserAuthFactory
hfds factories

CLSS public org.apache.sshd.client.config.hosts.HostConfigEntry
cons public init()
cons public init(java.lang.String,java.lang.String,int,java.lang.String)
cons public init(java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)
fld protected final java.util.Collection<java.lang.String> identities
fld protected final java.util.Map<java.lang.String,java.lang.String> properties
fld protected int port
fld protected java.lang.Boolean exclusiveIdentites
fld protected java.lang.String host
fld protected java.lang.String hostName
fld protected java.lang.String proxyJump
fld protected java.lang.String username
fld public final static boolean DEFAULT_EXCLUSIVE_IDENTITIES = false
fld public final static char LOCAL_HOME_MACRO = 'd'
fld public final static char LOCAL_HOST_MACRO = 'l'
fld public final static char LOCAL_USER_MACRO = 'u'
fld public final static char PATH_MACRO_CHAR = '%'
fld public final static char REMOTE_HOST_MACRO = 'h'
fld public final static char REMOTE_PORT_MACRO = 'p'
fld public final static char REMOTE_USER_MACRO = 'r'
fld public final static java.lang.String CERTIFICATE_FILE_CONFIG_PROP = "CertificateFile"
fld public final static java.lang.String EXCLUSIVE_IDENTITIES_CONFIG_PROP = "IdentitiesOnly"
fld public final static java.lang.String HOST_CONFIG_PROP = "Host"
fld public final static java.lang.String HOST_NAME_CONFIG_PROP = "HostName"
fld public final static java.lang.String IDENTITY_AGENT = "IdentityAgent"
fld public final static java.lang.String IDENTITY_FILE_CONFIG_PROP = "IdentityFile"
fld public final static java.lang.String MATCH_CONFIG_PROP = "Match"
fld public final static java.lang.String MULTI_VALUE_SEPARATORS = " ,"
fld public final static java.lang.String PORT_CONFIG_PROP = "Port"
fld public final static java.lang.String PROXY_JUMP_CONFIG_PROP = "ProxyJump"
fld public final static java.lang.String STD_CONFIG_FILENAME = "config"
fld public final static java.lang.String USER_CONFIG_PROP = "User"
fld public final static java.util.NavigableSet<java.lang.String> EXPLICIT_PROPERTIES
intf org.apache.sshd.common.auth.MutableUserHolder
meth public !varargs static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyValues({%%0},java.lang.String,java.lang.Object[]) throws java.io.IOException
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> findMatchingEntries(java.lang.String,org.apache.sshd.client.config.hosts.HostConfigEntry[])
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs static void writeHostConfigEntries(java.nio.file.Path,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>,java.nio.file.OpenOption[]) throws java.io.IOException
meth public <%0 extends java.lang.Appendable> {%%0} append({%%0}) throws java.io.IOException
meth public boolean isIdentitiesOnly()
meth public int getPort()
meth public java.lang.String appendPropertyValue(java.lang.String,java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getHostName()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getProperty(java.lang.String,java.lang.String)
meth public java.lang.String getProxyJump()
meth public java.lang.String getUsername()
meth public java.lang.String removeProperty(java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> getIdentities()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public static <%0 extends java.lang.Appendable> {%%0} appendHostConfigEntries({%%0},java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyPort({%%0},java.lang.String,int) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyProperties({%%0},java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyProperty({%%0},java.lang.String,java.lang.Object) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyValues({%%0},java.lang.String,java.util.Collection<?>) throws java.io.IOException
meth public static java.lang.String resolveIdentityFilePath(java.lang.String,java.lang.String,int,java.lang.String) throws java.io.IOException
meth public static java.nio.file.Path getDefaultHostConfigFile()
meth public static java.util.List<java.lang.String> parseConfigValue(java.lang.String)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> findMatchingEntries(java.lang.String,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.InputStream,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.Reader,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.net.URL) throws java.io.IOException
meth public static org.apache.sshd.client.config.hosts.HostConfigEntryResolver toHostConfigEntryResolver(java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>)
meth public static void writeHostConfigEntries(java.io.OutputStream,boolean,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>) throws java.io.IOException
meth public void addIdentity(java.lang.String)
meth public void addIdentity(java.nio.file.Path)
meth public void collate(org.apache.sshd.client.config.hosts.HostConfigEntry)
meth public void processProperty(java.lang.String,java.util.Collection<java.lang.String>)
meth public void setHost(java.lang.String)
meth public void setHost(java.util.Collection<java.lang.String>)
meth public void setHostName(java.lang.String)
meth public void setIdentities(java.util.Collection<java.lang.String>)
meth public void setIdentitiesOnly(boolean)
meth public void setPort(int)
meth public void setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public void setProxyJump(java.lang.String)
meth public void setUsername(java.lang.String)
supr org.apache.sshd.client.config.hosts.HostPatternsHolder
hcls LazyDefaultConfigFileHolder

CLSS public abstract interface org.apache.sshd.client.config.hosts.HostConfigEntryResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.client.config.hosts.HostConfigEntryResolver EMPTY
meth public abstract org.apache.sshd.client.config.hosts.HostConfigEntry resolveEffectiveHost(java.lang.String,int,java.net.SocketAddress,java.lang.String,java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException

CLSS public abstract org.apache.sshd.client.config.hosts.HostPatternsHolder
cons protected init()
fld public final static char NEGATION_CHAR_PATTERN = '!'
fld public final static char NON_STANDARD_PORT_PATTERN_ENCLOSURE_END_DELIM = ']'
fld public final static char NON_STANDARD_PORT_PATTERN_ENCLOSURE_START_DELIM = '['
fld public final static char PORT_VALUE_DELIMITER = ':'
fld public final static char SINGLE_CHAR_PATTERN = '?'
fld public final static char WILDCARD_PATTERN = '*'
fld public final static java.lang.String ALL_HOSTS_PATTERN
fld public final static java.lang.String PATTERN_CHARS
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostPatternValue> parsePatterns(java.lang.CharSequence[])
meth public boolean isHostMatch(java.lang.String,int)
meth public java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue> getPatterns()
meth public static boolean isHostMatch(java.lang.String,int,java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue>)
meth public static boolean isHostMatch(java.lang.String,java.util.regex.Pattern)
meth public static boolean isPortMatch(int,int)
meth public static boolean isSpecificHostPattern(java.lang.String)
meth public static boolean isValidPatternChar(char)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostPatternValue> parsePatterns(java.util.Collection<? extends java.lang.CharSequence>)
meth public static org.apache.sshd.client.config.hosts.HostPatternValue toPattern(java.lang.CharSequence)
meth public void setPatterns(java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue>)
supr java.lang.Object
hfds patterns

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public static org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder loaderHolderOf(org.apache.sshd.client.config.keys.ClientIdentityLoader)

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityLoaderManager
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
meth public abstract void setClientIdentityLoader(org.apache.sshd.client.config.keys.ClientIdentityLoader)

CLSS public abstract interface org.apache.sshd.client.keyverifier.ServerKeyVerifier
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)

CLSS public abstract org.apache.sshd.client.session.AbstractClientSession
cons protected init(org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.common.io.IoSession)
fld protected final boolean sendImmediateClientIdentification
fld protected final boolean sendImmediateKexInit
intf org.apache.sshd.client.session.ClientSession
meth protected !varargs void setKexSeed(byte[])
meth protected boolean readIdentification(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] sendKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager)
meth protected org.apache.sshd.client.session.ClientUserAuthService getUserAuthService()
meth protected org.apache.sshd.common.forward.Forwarder getForwarder()
meth protected org.apache.sshd.common.io.IoWriteFuture sendClientIdentification() throws java.lang.Exception
meth protected org.apache.sshd.common.session.ConnectionService getConnectionService()
meth protected void checkKeys() throws java.io.IOException
meth protected void initializeKeyExchangePhase() throws java.lang.Exception
meth protected void initializeProxyConnector() throws java.lang.Exception
meth protected void receiveKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,byte[]) throws java.io.IOException
meth protected void signalExtraServerVersionInfo(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public java.lang.String removePasswordIdentity(java.lang.String)
meth public java.net.SocketAddress getConnectAddress()
meth public java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public java.security.PublicKey getServerKey()
meth public java.util.List<org.apache.sshd.client.auth.UserAuthFactory> getUserAuthFactories()
meth public org.apache.sshd.client.ClientFactoryManager getFactoryManager()
meth public org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public org.apache.sshd.client.channel.ChannelDirectTcpip createDirectTcpipChannel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(byte[],org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,java.nio.charset.Charset,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelShell createShellChannel(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelSubsystem createSubsystemChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public org.apache.sshd.common.AttributeRepository getConnectionContext()
meth public org.apache.sshd.common.future.KeyExchangeFuture switchToNoneCipher() throws java.io.IOException
meth public org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public org.apache.sshd.common.util.net.SshdSocketAddress startDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void addPasswordIdentity(java.lang.String)
meth public void addPublicKeyIdentity(java.security.KeyPair)
meth public void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)
meth public void setConnectAddress(java.net.SocketAddress)
meth public void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public void setServerKey(java.security.PublicKey)
meth public void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.client.auth.UserAuthFactory>)
meth public void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void startService(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void stopDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
supr org.apache.sshd.common.session.helpers.AbstractSession
hfds connectAddress,connectionContext,hostBasedAuthenticationReporter,identities,identitiesProvider,keyIdentityProvider,passwordAuthenticationReporter,passwordIdentityProvider,proxyConnector,publicKeyAuthenticationReporter,serverKey,serverKeyVerifier,userAuthFactories,userInteraction

CLSS public abstract interface org.apache.sshd.client.session.ClientProxyConnector
 anno 0 java.lang.FunctionalInterface()
meth public abstract void sendClientProxyMetadata(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.session.ClientProxyConnectorHolder
meth public abstract org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public abstract void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)

CLSS public abstract interface org.apache.sshd.client.session.ClientSession
fld public final static java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> REMOTE_COMMAND_WAIT_EVENTS
innr public final static !enum ClientSessionEvent
intf org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.client.session.ClientProxyConnectorHolder
intf org.apache.sshd.common.forward.PortForwardingManager
intf org.apache.sshd.common.session.Session
meth public abstract java.net.SocketAddress getConnectAddress()
meth public abstract java.security.PublicKey getServerKey()
meth public abstract java.util.Map<java.lang.Object,java.lang.Object> getMetadataMap()
meth public abstract java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> getSessionState()
meth public abstract java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,long)
meth public abstract org.apache.sshd.client.ClientFactoryManager getFactoryManager()
meth public abstract org.apache.sshd.client.channel.ChannelDirectTcpip createDirectTcpipChannel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelExec createExecChannel(byte[],org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,java.nio.charset.Charset,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelShell createShellChannel(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelSubsystem createSubsystemChannel(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.AuthFuture auth() throws java.io.IOException
meth public abstract org.apache.sshd.common.AttributeRepository getConnectionContext()
meth public abstract org.apache.sshd.common.future.KeyExchangeFuture switchToNoneCipher() throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.io.OutputStream,java.nio.charset.Charset) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.io.OutputStream,java.nio.charset.Charset,java.time.Duration) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.time.Duration) throws java.io.IOException
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,java.time.Duration)
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelShell createShellChannel() throws java.io.IOException
meth public org.apache.sshd.client.session.forward.DynamicPortForwardingTracker createDynamicPortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createLocalPortForwardingTracker(int,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createLocalPortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createRemotePortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public static java.util.Iterator<java.lang.String> passwordIteratorOf(org.apache.sshd.client.session.ClientSession) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider providerOf(org.apache.sshd.client.session.ClientSession)
meth public void executeRemoteCommand(java.lang.String,java.io.OutputStream,java.io.OutputStream,java.nio.charset.Charset) throws java.io.IOException
meth public void executeRemoteCommand(java.lang.String,java.io.OutputStream,java.io.OutputStream,java.nio.charset.Charset,java.time.Duration) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.client.session.ClientSessionCreator
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<org.apache.sshd.common.util.net.SshdSocketAddress> TARGET_SERVER
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.client.session.ClientSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.client.session.ClientSession getClientSession()

CLSS public org.apache.sshd.client.session.ClientSessionImpl
cons public init(org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected <%0 extends java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>> {%%0} updateCurrentSessionState({%%0})
meth protected java.lang.String nextServiceName()
meth protected java.util.List<org.apache.sshd.common.Service> getServices()
meth protected org.apache.sshd.common.session.helpers.CurrentService initializeCurrentService()
meth protected void handleDisconnect(int,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void preClose()
meth protected void sendInitialServiceRequest() throws java.io.IOException
meth protected void signalAuthFailure(java.lang.Throwable)
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener$Event) throws java.lang.Exception
meth public java.util.Map<java.lang.Object,java.lang.Object> getMetadataMap()
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> getSessionState()
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,long)
meth public org.apache.sshd.client.future.AuthFuture auth() throws java.io.IOException
meth public void exceptionCaught(java.lang.Throwable)
meth public void start() throws java.lang.Exception
meth public void switchToNextService() throws java.io.IOException
supr org.apache.sshd.client.session.AbstractClientSession
hfds authErrorHolder,authFuture,beforeAuthErrorHolder,initialServiceRequestSent,metadataMap
hcls Services

CLSS public abstract interface org.apache.sshd.common.AttributeRepository
innr public static AttributeKey
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract int getAttributesCount()
meth public abstract java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.AttributeRepository ofKeyValuePair(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public static org.apache.sshd.common.AttributeRepository ofAttributesMap(java.util.Map<org.apache.sshd.common.AttributeRepository$AttributeKey<?>,?>)

CLSS public abstract interface org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.AttributeRepository
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public abstract void clearAttributes()

CLSS public abstract interface org.apache.sshd.common.Closeable
intf java.nio.channels.Channel
meth public abstract boolean isClosed()
meth public abstract boolean isClosing()
meth public abstract org.apache.sshd.common.future.CloseFuture close(boolean)
meth public abstract void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public abstract void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public boolean isOpen()
meth public static java.time.Duration getMaxCloseWaitTime(org.apache.sshd.common.PropertyResolver)
meth public static void close(org.apache.sshd.common.Closeable) throws java.io.IOException
meth public void close() throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.FactoryManager
fld public final static java.lang.String DEFAULT_VERSION = "SSHD-UNKNOWN"
intf org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.channel.ChannelListenerManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.io.IoServiceEventListenerManager
intf org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.session.ReservedSessionMessagesManager
intf org.apache.sshd.common.session.SessionDisconnectHandlerManager
intf org.apache.sshd.common.session.SessionHeartbeatController
intf org.apache.sshd.common.session.SessionListenerManager
intf org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract java.lang.String getVersion()
meth public abstract java.util.List<? extends org.apache.sshd.common.ServiceFactory> getServiceFactories()
meth public abstract java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> getChannelFactories()
meth public abstract java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> getGlobalRequestHandlers()
meth public abstract java.util.concurrent.ScheduledExecutorService getScheduledExecutorService()
meth public abstract org.apache.sshd.agent.SshAgentFactory getAgentFactory()
meth public abstract org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> getRandomFactory()
meth public abstract org.apache.sshd.common.file.FileSystemFactory getFileSystemFactory()
meth public abstract org.apache.sshd.common.forward.ForwarderFactory getForwarderFactory()
meth public abstract org.apache.sshd.common.io.IoServiceFactory getIoServiceFactory()
meth public abstract org.apache.sshd.server.forward.ForwardingFilter getForwardingFilter()
meth public org.apache.sshd.server.forward.AgentForwardingFilter getAgentForwardingFilter()
meth public org.apache.sshd.server.forward.TcpForwardingFilter getTcpForwardingFilter()
meth public org.apache.sshd.server.forward.X11ForwardingFilter getX11ForwardingFilter()
meth public static <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.FactoryManager,org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)

CLSS public abstract interface org.apache.sshd.common.FactoryManagerHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.FactoryManager getFactoryManager()

CLSS public abstract interface org.apache.sshd.common.NamedResource
 anno 0 java.lang.FunctionalInterface()
fld public final static java.util.Comparator<org.apache.sshd.common.NamedResource> BY_NAME_COMPARATOR
fld public final static java.util.function.Function<org.apache.sshd.common.NamedResource,java.lang.String> NAME_EXTRACTOR
meth public abstract java.lang.String getName()
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} findByName(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} findFirstMatchByName(java.util.Collection<java.lang.String>,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} removeByName(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static int safeCompareByName(org.apache.sshd.common.NamedResource,org.apache.sshd.common.NamedResource,boolean)
meth public static java.lang.String getNames(java.util.Collection<? extends org.apache.sshd.common.NamedResource>)
meth public static java.util.List<java.lang.String> getNameList(java.util.Collection<? extends org.apache.sshd.common.NamedResource>)
meth public static org.apache.sshd.common.NamedResource ofName(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.PropertyResolver
fld public final static org.apache.sshd.common.PropertyResolver EMPTY
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public boolean getBooleanProperty(java.lang.String,boolean)
meth public boolean isEmpty()
meth public int getIntProperty(java.lang.String,int)
meth public java.lang.Boolean getBoolean(java.lang.String)
meth public java.lang.Integer getInteger(java.lang.String)
meth public java.lang.Long getLong(java.lang.String)
meth public java.lang.Object getObject(java.lang.String)
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String getStringProperty(java.lang.String,java.lang.String)
meth public java.nio.charset.Charset getCharset(java.lang.String,java.nio.charset.Charset)
meth public long getLongProperty(java.lang.String,long)
meth public static boolean isEmpty(org.apache.sshd.common.PropertyResolver)

CLSS public abstract org.apache.sshd.common.auth.AbstractUserAuthMethodFactory<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%0}>>
cons protected init(java.lang.String)
intf org.apache.sshd.common.auth.UserAuthMethodFactory<{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%0},{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%1}>
meth public final java.lang.String getName()
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds name

CLSS public abstract interface org.apache.sshd.common.auth.MutableUserHolder
intf org.apache.sshd.common.auth.UsernameHolder
meth public abstract void setUsername(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthFactoriesManager<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.UserAuthFactoriesManager%0}>, %2 extends org.apache.sshd.common.auth.UserAuthMethodFactory<{org.apache.sshd.common.auth.UserAuthFactoriesManager%0},{org.apache.sshd.common.auth.UserAuthFactoriesManager%1}>>
meth public !varargs void setUserAuthFactoriesNames(java.lang.String[])
meth public abstract java.util.List<{org.apache.sshd.common.auth.UserAuthFactoriesManager%2}> getUserAuthFactories()
meth public abstract void setUserAuthFactories(java.util.List<{org.apache.sshd.common.auth.UserAuthFactoriesManager%2}>)
meth public abstract void setUserAuthFactoriesNames(java.util.Collection<java.lang.String>)
meth public java.lang.String getUserAuthFactoriesNameList()
meth public java.util.List<java.lang.String> getUserAuthFactoriesNames()
meth public void setUserAuthFactoriesNameList(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthInstance<%0 extends org.apache.sshd.common.session.SessionContext>
intf org.apache.sshd.common.NamedResource
meth public abstract {org.apache.sshd.common.auth.UserAuthInstance%0} getSession()

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthMethodFactory<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.UserAuthMethodFactory%0}>>
fld public final static java.lang.String HOST_BASED = "hostbased"
fld public final static java.lang.String KB_INTERACTIVE = "keyboard-interactive"
fld public final static java.lang.String PASSWORD = "password"
fld public final static java.lang.String PUBLIC_KEY = "publickey"
intf org.apache.sshd.common.NamedResource
meth public abstract {org.apache.sshd.common.auth.UserAuthMethodFactory%1} createUserAuth({org.apache.sshd.common.auth.UserAuthMethodFactory%0}) throws java.io.IOException
meth public static <%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{%%0}>> {%%1} createUserAuth({%%0},java.util.Collection<? extends org.apache.sshd.common.auth.UserAuthMethodFactory<{%%0},{%%1}>>,java.lang.String) throws java.io.IOException
meth public static boolean isDataIntegrityAuthenticationTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isSecureAuthenticationTransport(org.apache.sshd.common.session.SessionContext)

CLSS public abstract interface org.apache.sshd.common.auth.UsernameHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.String getUsername()

CLSS public abstract interface org.apache.sshd.common.channel.ChannelListenerManager
meth public abstract org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public abstract void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public abstract void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)

CLSS public abstract interface org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver NONE
meth public abstract org.apache.sshd.common.channel.throttle.ChannelStreamWriter resolveChannelStreamWriter(org.apache.sshd.common.channel.Channel,byte)

CLSS public abstract interface org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver
meth public abstract org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public abstract void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriter resolveChannelStreamWriter(org.apache.sshd.common.channel.Channel,byte)
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver resolveChannelStreamWriterResolver()

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProvider
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider EMPTY
innr public abstract interface static Decoder
innr public final static !enum ResourceDecodeResult
meth public <%0 extends java.lang.Object> {%%0} decode(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider$Decoder<? extends {%%0}>) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract java.lang.String getPassword(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int) throws java.io.IOException
meth public org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult handleDecodeAttemptResult(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int,java.lang.String,java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.config.keys.FilePasswordProvider of(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProviderHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
meth public static org.apache.sshd.common.config.keys.FilePasswordProviderHolder providerHolderOf(org.apache.sshd.common.config.keys.FilePasswordProvider)

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProviderManager
intf org.apache.sshd.common.config.keys.FilePasswordProviderHolder
meth public abstract void setFilePasswordProvider(org.apache.sshd.common.config.keys.FilePasswordProvider)

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingEventListenerManager
meth public abstract org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public abstract void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public abstract void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingInformationProvider
meth public abstract java.util.List<java.util.Map$Entry<java.lang.Integer,org.apache.sshd.common.util.net.SshdSocketAddress>> getRemoteForwardsBindings()
meth public abstract java.util.List<java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress>> getLocalForwardsBindings()
meth public abstract java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getBoundLocalPortForwards(int)
meth public abstract java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getStartedLocalPortForwards()
meth public abstract java.util.NavigableSet<java.lang.Integer> getStartedRemotePortForwards()
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress getBoundRemotePortForward(int)
meth public boolean isLocalPortForwardingStartedForPort(int)
meth public boolean isRemotePortForwardingStartedForPort(int)

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingManager
intf org.apache.sshd.common.forward.PortForwardingInformationProvider
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(int,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException

CLSS public abstract org.apache.sshd.common.helpers.AbstractFactoryManager
cons protected init()
fld protected boolean shutdownExecutor
fld protected final java.util.Collection<org.apache.sshd.common.channel.ChannelListener> channelListeners
fld protected final java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListener> tunnelListeners
fld protected final java.util.Collection<org.apache.sshd.common.session.SessionListener> sessionListeners
fld protected final java.util.concurrent.atomic.AtomicReference<java.util.concurrent.ScheduledFuture<?>> timeoutListenerFuture
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.session.helpers.SessionTimeoutListener> sessionTimeoutListener
fld protected final org.apache.sshd.common.channel.ChannelListener channelListenerProxy
fld protected final org.apache.sshd.common.forward.PortForwardingEventListener tunnelListenerProxy
fld protected final org.apache.sshd.common.session.SessionListener sessionListenerProxy
fld protected java.util.List<? extends org.apache.sshd.common.ServiceFactory> serviceFactories
fld protected java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> channelFactories
fld protected java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> globalRequestHandlers
fld protected java.util.concurrent.ScheduledExecutorService executor
fld protected org.apache.sshd.agent.SshAgentFactory agentFactory
fld protected org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> randomFactory
fld protected org.apache.sshd.common.file.FileSystemFactory fileSystemFactory
fld protected org.apache.sshd.common.forward.ForwarderFactory forwarderFactory
fld protected org.apache.sshd.common.io.IoServiceFactory ioServiceFactory
fld protected org.apache.sshd.common.io.IoServiceFactoryFactory ioServiceFactoryFactory
fld protected org.apache.sshd.server.forward.ForwardingFilter forwardingFilter
intf org.apache.sshd.common.FactoryManager
meth protected org.apache.sshd.common.session.helpers.SessionTimeoutListener createSessionTimeoutListener()
meth protected void checkConfig()
meth protected void removeSessionTimeout(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth protected void setupSessionTimeout(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth protected void stopSessionTimeoutListener(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public int getAttributesCount()
meth public int getNioWorkers()
meth public java.lang.String getVersion()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public java.util.List<? extends org.apache.sshd.common.ServiceFactory> getServiceFactories()
meth public java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> getChannelFactories()
meth public java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> getGlobalRequestHandlers()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.concurrent.ScheduledExecutorService getScheduledExecutorService()
meth public org.apache.sshd.agent.SshAgentFactory getAgentFactory()
meth public org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> getRandomFactory()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public org.apache.sshd.common.file.FileSystemFactory getFileSystemFactory()
meth public org.apache.sshd.common.forward.ForwarderFactory getForwarderFactory()
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public org.apache.sshd.common.io.IoServiceFactory getIoServiceFactory()
meth public org.apache.sshd.common.io.IoServiceFactoryFactory getIoServiceFactoryFactory()
meth public org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public org.apache.sshd.server.forward.ForwardingFilter getForwardingFilter()
meth public void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void clearAttributes()
meth public void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removeSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void setAgentFactory(org.apache.sshd.agent.SshAgentFactory)
meth public void setChannelFactories(java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory>)
meth public void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public void setFileSystemFactory(org.apache.sshd.common.file.FileSystemFactory)
meth public void setForwarderFactory(org.apache.sshd.common.forward.ForwarderFactory)
meth public void setForwardingFilter(org.apache.sshd.server.forward.ForwardingFilter)
meth public void setGlobalRequestHandlers(java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>>)
meth public void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)
meth public void setIoServiceFactoryFactory(org.apache.sshd.common.io.IoServiceFactoryFactory)
meth public void setNioWorkers(int)
meth public void setParentPropertyResolver(org.apache.sshd.common.PropertyResolver)
meth public void setRandomFactory(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random>)
meth public void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)
meth public void setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
meth public void setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService,boolean)
meth public void setServiceFactories(java.util.List<? extends org.apache.sshd.common.ServiceFactory>)
meth public void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)
meth public void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
supr org.apache.sshd.common.kex.AbstractKexFactoryManager
hfds attributes,channelStreamWriterResolver,eventListener,parentResolver,properties,reservedSessionMessagesHandler,sessionDisconnectHandler,unknownChannelReferenceHandler

CLSS public abstract interface org.apache.sshd.common.io.IoServiceEventListenerManager
meth public abstract org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public abstract void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)

CLSS public abstract org.apache.sshd.common.kex.AbstractKexFactoryManager
cons protected init()
cons protected init(org.apache.sshd.common.kex.KexFactoryManager)
intf org.apache.sshd.common.kex.KexFactoryManager
meth protected <%0 extends java.lang.Object, %1 extends java.util.Collection<{%%0}>> {%%1} resolveEffectiveFactories({%%1},{%%1})
meth protected <%0 extends java.lang.Object> {%%0} resolveEffectiveProvider(java.lang.Class<{%%0}>,{%%0},{%%0})
meth protected org.apache.sshd.common.kex.KexFactoryManager getDelegate()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> getCipherFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> getCompressionFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> getMacFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> getKeyExchangeFactories()
meth public org.apache.sshd.common.kex.extension.KexExtensionHandler getKexExtensionHandler()
meth public void setCipherFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>>)
meth public void setCompressionFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>>)
meth public void setKexExtensionHandler(org.apache.sshd.common.kex.extension.KexExtensionHandler)
meth public void setKeyExchangeFactories(java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory>)
meth public void setMacFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>>)
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds cipherFactories,compressionFactories,delegate,kexExtensionHandler,keyExchangeFactories,macFactories,signatureFactories

CLSS public abstract interface org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.kex.extension.KexExtensionHandlerManager
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public !varargs void setCipherFactoriesNames(java.lang.String[])
meth public !varargs void setCompressionFactoriesNames(java.lang.String[])
meth public !varargs void setMacFactoriesNames(java.lang.String[])
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> getCipherFactories()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> getCompressionFactories()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> getMacFactories()
meth public abstract java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> getKeyExchangeFactories()
meth public abstract void setCipherFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>>)
meth public abstract void setCompressionFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>>)
meth public abstract void setKeyExchangeFactories(java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory>)
meth public abstract void setMacFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>>)
meth public java.lang.String getCipherFactoriesNameList()
meth public java.lang.String getCompressionFactoriesNameList()
meth public java.lang.String getMacFactoriesNameList()
meth public java.util.List<java.lang.String> getCipherFactoriesNames()
meth public java.util.List<java.lang.String> getCompressionFactoriesNames()
meth public java.util.List<java.lang.String> getMacFactoriesNames()
meth public void setCipherFactoriesNameList(java.lang.String)
meth public void setCipherFactoriesNames(java.util.Collection<java.lang.String>)
meth public void setCompressionFactoriesNameList(java.lang.String)
meth public void setCompressionFactoriesNames(java.util.Collection<java.lang.String>)
meth public void setMacFactoriesNameList(java.lang.String)
meth public void setMacFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public abstract interface org.apache.sshd.common.kex.extension.KexExtensionHandlerManager
meth public abstract org.apache.sshd.common.kex.extension.KexExtensionHandler getKexExtensionHandler()
meth public abstract void setKexExtensionHandler(org.apache.sshd.common.kex.extension.KexExtensionHandler)

CLSS public abstract org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
cons protected init()
intf org.apache.sshd.common.keyprovider.KeyPairProvider
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider<%0 extends java.lang.Object>
cons protected init()
innr protected KeyPairIterator
meth protected java.io.InputStream openKeyPairResource(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}) throws java.io.IOException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0},org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends {org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}>)
meth protected org.apache.sshd.common.util.io.resource.IoResource<?> getIoResource(org.apache.sshd.common.session.SessionContext,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0})
meth protected void resetCacheMap(java.util.Collection<?>)
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getPasswordFinder()
meth public void setPasswordFinder(org.apache.sshd.common.config.keys.FilePasswordProvider)
supr org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
hfds cacheMap,passwordFinder

CLSS public org.apache.sshd.common.keyprovider.FileKeyPairProvider
cons public !varargs init(java.nio.file.Path[])
cons public init()
cons public init(java.nio.file.Path)
cons public init(java.util.Collection<? extends java.nio.file.Path>)
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth protected org.apache.sshd.common.util.io.resource.IoResource<java.nio.file.Path> getIoResource(org.apache.sshd.common.session.SessionContext,java.nio.file.Path)
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public java.util.Collection<? extends java.nio.file.Path> getPaths()
meth public void setPaths(java.util.Collection<? extends java.nio.file.Path>)
supr org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider<java.nio.file.Path>
hfds files

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyIdentityProvider
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.keyprovider.KeyIdentityProvider EMPTY_KEYS_PROVIDER
meth public !varargs static org.apache.sshd.common.keyprovider.KeyIdentityProvider multiProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider[])
meth public !varargs static org.apache.sshd.common.keyprovider.KeyIdentityProvider wrapKeyPairs(java.security.KeyPair[])
meth public abstract java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static boolean isEmpty(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public static java.lang.Iterable<java.security.KeyPair> iterableOf(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
meth public static java.security.KeyPair exhaustCurrentIdentities(java.util.Iterator<java.security.KeyPair>)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider multiProvider(java.util.Collection<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider resolveKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider wrapKeyPairs(java.lang.Iterable<java.security.KeyPair>)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyIdentityProviderHolder
meth public abstract org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public abstract void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyPairProvider
fld public final static java.lang.String ECDSA_SHA2_NISTP256
fld public final static java.lang.String ECDSA_SHA2_NISTP384
fld public final static java.lang.String ECDSA_SHA2_NISTP521
fld public final static java.lang.String SSH_DSS = "ssh-dss"
fld public final static java.lang.String SSH_DSS_CERT = "ssh-dss-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP256_CERT = "ecdsa-sha2-nistp256-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP384_CERT = "ecdsa-sha2-nistp384-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP521_CERT = "ecdsa-sha2-nistp521-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ED25519 = "ssh-ed25519"
fld public final static java.lang.String SSH_ED25519_CERT = "ssh-ed25519-cert-v01@openssh.com"
fld public final static java.lang.String SSH_RSA = "ssh-rsa"
fld public final static java.lang.String SSH_RSA_CERT = "ssh-rsa-cert-v01@openssh.com"
fld public final static org.apache.sshd.common.keyprovider.KeyPairProvider EMPTY_KEYPAIR_PROVIDER
intf org.apache.sshd.common.keyprovider.KeyIdentityProvider
meth public !varargs static org.apache.sshd.common.keyprovider.KeyPairProvider wrap(java.security.KeyPair[])
meth public java.lang.Iterable<java.lang.String> getKeyTypes(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.KeyPair loadKey(org.apache.sshd.common.session.SessionContext,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.keyprovider.KeyPairProvider wrap(java.lang.Iterable<java.security.KeyPair>)

CLSS public abstract interface org.apache.sshd.common.session.ReservedSessionMessagesManager
meth public abstract org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public abstract void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)

CLSS public abstract interface org.apache.sshd.common.session.Session
intf org.apache.sshd.common.FactoryManagerHolder
intf org.apache.sshd.common.auth.MutableUserHolder
intf org.apache.sshd.common.channel.ChannelListenerManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.forward.PortForwardingInformationProvider
intf org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.session.ReservedSessionMessagesManager
intf org.apache.sshd.common.session.SessionContext
intf org.apache.sshd.common.session.SessionDisconnectHandlerManager
intf org.apache.sshd.common.session.SessionListenerManager
intf org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract !varargs org.apache.sshd.common.io.IoWriteFuture sendIgnoreMessage(byte[]) throws java.io.IOException
meth public abstract <%0 extends org.apache.sshd.common.Service> {%%0} getService(java.lang.Class<{%%0}>)
meth public abstract java.time.Duration getAuthTimeout()
meth public abstract java.time.Duration getIdleTimeout()
meth public abstract java.time.Instant getAuthTimeoutStart()
meth public abstract java.time.Instant getIdleTimeoutStart()
meth public abstract java.time.Instant resetAuthTimeout()
meth public abstract java.time.Instant resetIdleTimeout()
meth public abstract org.apache.sshd.common.future.GlobalRequestFuture request(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler) throws java.io.IOException
meth public abstract org.apache.sshd.common.future.KeyExchangeFuture reExchangeKeys() throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoSession getIoSession()
meth public abstract org.apache.sshd.common.io.IoWriteFuture sendDebugMessage(boolean,java.lang.Object,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public abstract org.apache.sshd.common.kex.KeyExchange getKex()
meth public abstract org.apache.sshd.common.session.helpers.TimeoutIndicator getTimeoutStatus()
meth public abstract org.apache.sshd.common.util.buffer.Buffer createBuffer(byte,int)
meth public abstract org.apache.sshd.common.util.buffer.Buffer prepareBuffer(byte,org.apache.sshd.common.util.buffer.Buffer)
meth public abstract org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public abstract void disconnect(int,java.lang.String) throws java.io.IOException
meth public abstract void exceptionCaught(java.lang.Throwable)
meth public abstract void setAuthenticated() throws java.io.IOException
meth public abstract void startService(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.net.SocketAddress getLocalAddress()
meth public java.net.SocketAddress getRemoteAddress()
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer createBuffer(byte)
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.session.Session,org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)

CLSS public abstract interface org.apache.sshd.common.session.SessionContext
fld public final static int MAX_VERSION_LINE_LENGTH = 256
fld public final static java.lang.String DEFAULT_SSH_VERSION_PREFIX = "SSH-2.0-"
fld public final static java.lang.String FALLBACK_SSH_VERSION_PREFIX = "SSH-1.99-"
intf org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.auth.UsernameHolder
intf org.apache.sshd.common.session.SessionHeartbeatController
intf org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public abstract boolean isAuthenticated()
meth public abstract boolean isServerSession()
meth public abstract byte[] getSessionId()
meth public abstract java.lang.String getClientVersion()
meth public abstract java.lang.String getNegotiatedKexParameter(org.apache.sshd.common.kex.KexProposalOption)
meth public abstract java.lang.String getServerVersion()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getClientKexProposals()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexNegotiationResult()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getServerKexProposals()
meth public abstract org.apache.sshd.common.cipher.CipherInformation getCipherInformation(boolean)
meth public abstract org.apache.sshd.common.compression.CompressionInformation getCompressionInformation(boolean)
meth public abstract org.apache.sshd.common.kex.KexState getKexState()
meth public abstract org.apache.sshd.common.mac.MacInformation getMacInformation(boolean)
meth public static boolean isDataIntegrityTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isSecureSessionTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isValidSessionPayloadSize(long)
meth public static boolean isValidVersionPrefix(java.lang.String)
meth public static long validateSessionPayloadSize(long,java.lang.String)

CLSS public abstract interface org.apache.sshd.common.session.SessionDisconnectHandlerManager
meth public abstract org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public abstract void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)

CLSS public abstract interface org.apache.sshd.common.session.SessionHeartbeatController
innr public final static !enum HeartbeatType
intf org.apache.sshd.common.PropertyResolver
meth public java.time.Duration getSessionHeartbeatInterval()
meth public org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType getSessionHeartbeatType()
meth public void disableSessionHeartbeat()
meth public void setSessionHeartbeat(org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType,java.time.Duration)
meth public void setSessionHeartbeat(org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType,java.util.concurrent.TimeUnit,long)

CLSS public abstract interface org.apache.sshd.common.session.SessionListenerManager
meth public abstract org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public abstract void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public abstract void removeSessionListener(org.apache.sshd.common.session.SessionListener)

CLSS public abstract interface org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public abstract org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public abstract org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public abstract void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractSession
cons protected init(boolean,org.apache.sshd.common.FactoryManager,org.apache.sshd.common.io.IoSession)
fld protected boolean initialKexDone
fld protected boolean strictKex
fld protected byte[] inMacResult
fld protected byte[] sessionId
fld protected final java.lang.Object requestLock
fld protected final java.util.Collection<org.apache.sshd.common.channel.ChannelListener> channelListeners
fld protected final java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListener> tunnelListeners
fld protected final java.util.Collection<org.apache.sshd.common.session.SessionListener> sessionListeners
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> clientProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> negotiationResult
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> serverProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodClientProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodNegotiationResult
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodServerProposal
fld protected final java.util.concurrent.atomic.AtomicLong ignorePacketsCount
fld protected final java.util.concurrent.atomic.AtomicLong inBlocksCount
fld protected final java.util.concurrent.atomic.AtomicLong inBytesCount
fld protected final java.util.concurrent.atomic.AtomicLong inPacketsCount
fld protected final java.util.concurrent.atomic.AtomicLong maxRekeyBlocks
fld protected final java.util.concurrent.atomic.AtomicLong outBlocksCount
fld protected final java.util.concurrent.atomic.AtomicLong outBytesCount
fld protected final java.util.concurrent.atomic.AtomicLong outPacketsCount
fld protected final java.util.concurrent.atomic.AtomicReference<java.time.Instant> lastKeyTimeValue
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.future.DefaultKeyExchangeFuture> kexFutureHolder
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.kex.KexState> kexState
fld protected final java.util.concurrent.locks.ReentrantLock decodeLock
fld protected final java.util.concurrent.locks.ReentrantLock encodeLock
fld protected final org.apache.sshd.common.channel.ChannelListener channelListenerProxy
fld protected final org.apache.sshd.common.forward.PortForwardingEventListener tunnelListenerProxy
fld protected final org.apache.sshd.common.random.Random random
fld protected final org.apache.sshd.common.session.SessionListener sessionListenerProxy
fld protected final org.apache.sshd.common.session.SessionWorkBuffer decoderBuffer
fld protected final org.apache.sshd.common.session.helpers.CurrentService currentService
fld protected final org.apache.sshd.common.session.helpers.KeyExchangeMessageHandler kexHandler
fld protected int decoderLength
fld protected int decoderState
fld protected int ignorePacketDataLength
fld protected int ignorePacketsVariance
fld protected int inCipherSize
fld protected int inMacSize
fld protected int outCipherSize
fld protected int outMacSize
fld protected java.lang.Boolean firstKexPacketFollows
fld protected java.lang.String clientVersion
fld protected java.lang.String serverVersion
fld protected java.time.Duration maxRekeyInterval
fld protected long ignorePacketsFrequency
fld protected long initialKexInitSequenceNumber
fld protected long maxRekeyBytes
fld protected long maxRekyPackets
fld protected long seqi
fld protected long seqo
fld protected org.apache.sshd.common.SshException discarding
fld protected org.apache.sshd.common.cipher.Cipher inCipher
fld protected org.apache.sshd.common.cipher.Cipher outCipher
fld protected org.apache.sshd.common.compression.Compression inCompression
fld protected org.apache.sshd.common.compression.Compression outCompression
fld protected org.apache.sshd.common.future.DefaultKeyExchangeFuture kexInitializedFuture
fld protected org.apache.sshd.common.kex.KeyExchange kex
fld protected org.apache.sshd.common.mac.Mac inMac
fld protected org.apache.sshd.common.mac.Mac outMac
fld protected org.apache.sshd.common.session.SessionWorkBuffer uncompressBuffer
fld protected org.apache.sshd.common.session.helpers.AbstractSession$MessageCodingSettings inSettings
fld protected org.apache.sshd.common.session.helpers.AbstractSession$MessageCodingSettings outSettings
fld public final static java.lang.String SESSION = "org.apache.sshd.session"
innr protected static MessageCodingSettings
meth protected <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} validateTargetBuffer(int,{%%0})
meth protected abstract !varargs void setKexSeed(byte[])
meth protected abstract boolean readIdentification(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected abstract void checkKeys() throws java.io.IOException
meth protected abstract void receiveKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,byte[]) throws java.io.IOException
meth protected abstract void start() throws java.lang.Exception
meth protected boolean doInvokeUnimplementedMessageHandler(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean handleFirstKexPacketFollows(int,org.apache.sshd.common.util.buffer.Buffer,boolean)
meth protected boolean handleServiceRequest(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean isRekeyBlocksCountExceeded()
meth protected boolean isRekeyDataSizeExceeded()
meth protected boolean isRekeyPacketCountsExceeded()
meth protected boolean isRekeyRequired()
meth protected boolean isRekeyTimeIntervalExceeded()
meth protected boolean removeValue(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,org.apache.sshd.common.kex.KexProposalOption,java.lang.String)
meth protected boolean validateServiceKexState(org.apache.sshd.common.kex.KexState)
meth protected byte[] getClientKexData()
meth protected byte[] getServerKexData()
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected byte[] sendKexInit() throws java.lang.Exception
meth protected byte[] sendKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected int resolveIgnoreBufferDataLength()
meth protected java.lang.String resolveSessionKexProposal(java.lang.String) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.common.Service> getServices()
meth protected java.util.Map$Entry<java.lang.String,java.lang.String> comparePreferredKexProposalOption(org.apache.sshd.common.kex.KexProposalOption)
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> doStrictKexProposal(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> negotiate() throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> setNegotiationResult(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected long determineRekeyBlockLimit(int,int)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.future.KeyExchangeFuture checkRekey() throws java.lang.Exception
meth protected org.apache.sshd.common.future.KeyExchangeFuture requestNewKeysExchange() throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture doWritePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture notImplemented(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture sendNewKeys() throws java.lang.Exception
meth protected org.apache.sshd.common.session.helpers.CurrentService initializeCurrentService()
meth protected org.apache.sshd.common.session.helpers.KeyExchangeMessageHandler initializeKeyExchangeMessageHandler()
meth protected org.apache.sshd.common.util.buffer.Buffer encode(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer preProcessEncodeBuffer(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer resolveOutputPacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void aeadOutgoingBuffer(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void appendOutgoingMac(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void decode() throws java.lang.Exception
meth protected void doHandleMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doKexNegotiation() throws java.lang.Exception
meth protected void encryptOutgoingBuffer(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void failStrictKex(int) throws org.apache.sshd.common.SshException
meth protected void handleKexExtension(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleKexMessage(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleNewCompression(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleNewKeys(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceAccept(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceAccept(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void performKexNegotiation() throws java.lang.Exception
meth protected void preClose()
meth protected void prepareNewKeys() throws java.lang.Exception
meth protected void refreshConfiguration()
meth protected void requestFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void requestSuccess(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void setClientKexData(byte[])
meth protected void setInputEncoding() throws java.lang.Exception
meth protected void setOutputEncoding() throws java.lang.Exception
meth protected void setServerKexData(byte[])
meth protected void validateIncomingMac(byte[],int,int) throws java.lang.Exception
meth protected void validateKexState(int,org.apache.sshd.common.kex.KexState)
meth public <%0 extends org.apache.sshd.common.Service> {%%0} getService(java.lang.Class<{%%0}>)
meth public byte[] getSessionId()
meth public java.lang.String getClientVersion()
meth public java.lang.String getNegotiatedKexParameter(org.apache.sshd.common.kex.KexProposalOption)
meth public java.lang.String getServerVersion()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getClientKexProposals()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexNegotiationResult()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getServerKexProposals()
meth public org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public org.apache.sshd.common.cipher.CipherInformation getCipherInformation(boolean)
meth public org.apache.sshd.common.compression.CompressionInformation getCompressionInformation(boolean)
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.future.GlobalRequestFuture request(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler) throws java.io.IOException
meth public org.apache.sshd.common.future.KeyExchangeFuture reExchangeKeys() throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public org.apache.sshd.common.kex.KexState getKexState()
meth public org.apache.sshd.common.kex.KeyExchange getKex()
meth public org.apache.sshd.common.mac.MacInformation getMacInformation(boolean)
meth public org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public org.apache.sshd.common.util.buffer.Buffer createBuffer(byte,int)
meth public org.apache.sshd.common.util.buffer.Buffer prepareBuffer(byte,org.apache.sshd.common.util.buffer.Buffer)
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public static int calculatePadLength(int,int,boolean)
meth public static org.apache.sshd.common.session.helpers.AbstractSession getSession(org.apache.sshd.common.io.IoSession)
meth public static org.apache.sshd.common.session.helpers.AbstractSession getSession(org.apache.sshd.common.io.IoSession,boolean)
meth public static void attachSession(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.session.helpers.AbstractSession)
meth public void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void messageReceived(org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removeSessionListener(org.apache.sshd.common.session.SessionListener)
supr org.apache.sshd.common.session.helpers.SessionHelper
hfds clientKexData,globalSequenceNumbers,pendingGlobalRequests,serverKexData
hcls KexStart

CLSS public abstract org.apache.sshd.common.session.helpers.SessionHelper
cons protected init(boolean,org.apache.sshd.common.FactoryManager,org.apache.sshd.common.io.IoSession)
fld protected java.time.Instant authStart
fld protected java.time.Instant idleStart
fld protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> initialKexProposal
intf org.apache.sshd.common.session.Session
meth protected abstract java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager) throws java.io.IOException,java.security.GeneralSecurityException
meth protected abstract org.apache.sshd.common.session.ConnectionService getConnectionService()
meth protected boolean doInvokeUnimplementedMessageHandler(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] resizeKey(byte[],int,org.apache.sshd.common.digest.Digest,byte[],byte[]) throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal() throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.String resolveIdentificationString(java.lang.String)
meth protected java.lang.String resolveSessionKexProposal(java.lang.String) throws java.io.IOException
meth protected java.net.SocketAddress resolvePeerAddress(java.net.SocketAddress)
meth protected java.util.List<java.lang.String> doReadIdentification(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> createProposal(java.lang.String) throws java.io.IOException
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexProposal() throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> mergeProposals(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected long calculateNextIgnorePacketCount(org.apache.sshd.common.random.Random,long,int)
meth protected org.apache.sshd.common.forward.Forwarder getForwarder()
meth protected org.apache.sshd.common.io.IoWriteFuture sendIdentification(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture sendNotImplemented(long) throws java.io.IOException
meth protected org.apache.sshd.common.session.ReservedSessionMessagesHandler resolveReservedSessionMessagesHandler()
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkAuthenticationTimeout(java.time.Instant,java.time.Duration)
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkForTimeouts() throws java.io.IOException
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkIdleTimeout(java.time.Instant,java.time.Duration)
meth protected org.apache.sshd.common.util.buffer.Buffer preProcessEncodeBuffer(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void doInvokeDebugMessageHandler(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doInvokeIgnoreMessageHandler(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDebug(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDisconnect(int,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDisconnect(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleIgnore(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleUnimplemented(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void invokeSessionSignaller(org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.session.SessionListener,java.lang.Void>) throws java.lang.Throwable
meth protected void signalDisconnect(int,java.lang.String,java.lang.String,boolean)
meth protected void signalDisconnect(org.apache.sshd.common.session.SessionListener,int,java.lang.String,java.lang.String,boolean)
meth protected void signalExceptionCaught(java.lang.Throwable)
meth protected void signalExceptionCaught(org.apache.sshd.common.session.SessionListener,java.lang.Throwable)
meth protected void signalNegotiationEnd(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.Throwable)
meth protected void signalNegotiationEnd(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.Throwable)
meth protected void signalNegotiationOptionsCreated(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationOptionsCreated(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationStart(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationStart(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalPeerIdentificationReceived(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalPeerIdentificationReceived(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalReadPeerIdentificationLine(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalReadPeerIdentificationLine(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalSendIdentification(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalSendIdentification(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalSessionClosed()
meth protected void signalSessionClosed(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected void signalSessionCreated(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionEstablished(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected void signalSessionEstablished(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener$Event) throws java.lang.Exception
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener,org.apache.sshd.common.session.SessionListener$Event) throws java.io.IOException
meth public !varargs org.apache.sshd.common.io.IoWriteFuture sendIgnoreMessage(byte[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public boolean isAuthenticated()
meth public boolean isLocalPortForwardingStartedForPort(int)
meth public boolean isRemotePortForwardingStartedForPort(int)
meth public boolean isServerSession()
meth public int getAttributesCount()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public java.time.Duration getAuthTimeout()
meth public java.time.Duration getIdleTimeout()
meth public java.time.Instant getAuthTimeoutStart()
meth public java.time.Instant getIdleTimeoutStart()
meth public java.time.Instant resetAuthTimeout()
meth public java.time.Instant resetIdleTimeout()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public java.util.List<java.util.Map$Entry<java.lang.Integer,org.apache.sshd.common.util.net.SshdSocketAddress>> getRemoteForwardsBindings()
meth public java.util.List<java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress>> getLocalForwardsBindings()
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getBoundLocalPortForwards(int)
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getStartedLocalPortForwards()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.NavigableSet<java.lang.Integer> getStartedRemotePortForwards()
meth public org.apache.sshd.common.FactoryManager getFactoryManager()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver resolveChannelStreamWriterResolver()
meth public org.apache.sshd.common.io.IoSession getIoSession()
meth public org.apache.sshd.common.io.IoWriteFuture sendDebugMessage(boolean,java.lang.Object,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.helpers.TimeoutIndicator getTimeoutStatus()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getBoundRemotePortForward(int)
meth public void clearAttributes()
meth public void disconnect(int,java.lang.String) throws java.io.IOException
meth public void exceptionCaught(java.lang.Throwable)
meth public void setAuthenticated() throws java.io.IOException
meth public void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)
meth public void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)
meth public void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
meth public void setUsername(java.lang.String)
supr org.apache.sshd.common.kex.AbstractKexFactoryManager
hfds attributes,authed,channelStreamPacketWriterResolver,ioSession,properties,reservedSessionMessagesHandler,serverSession,sessionDisconnectHandler,timeoutStatus,unknownChannelReferenceHandler,username

CLSS public abstract interface org.apache.sshd.common.signature.SignatureFactoriesHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public java.lang.String getSignatureFactoriesNameList()
meth public java.util.List<java.lang.String> getSignatureFactoriesNames()

CLSS public abstract interface org.apache.sshd.common.signature.SignatureFactoriesManager
intf org.apache.sshd.common.signature.SignatureFactoriesHolder
meth public !varargs void setSignatureFactoriesNames(java.lang.String[])
meth public abstract void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories(org.apache.sshd.common.signature.SignatureFactoriesManager)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> resolveSignatureFactories(org.apache.sshd.common.signature.SignatureFactoriesManager,org.apache.sshd.common.signature.SignatureFactoriesManager)
meth public void setSignatureFactoriesNameList(java.lang.String)
meth public void setSignatureFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public abstract org.apache.sshd.common.util.closeable.AbstractCloseable
cons protected init()
cons protected init(java.lang.String)
fld protected final java.lang.Object futureLock
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.util.closeable.AbstractCloseable$State> state
fld protected final org.apache.sshd.common.future.CloseFuture closeFuture
innr public final static !enum State
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected org.apache.sshd.common.util.closeable.Builder builder()
meth protected void doCloseImmediately()
meth protected void preClose()
meth public final boolean isClosed()
meth public final boolean isClosing()
meth public final org.apache.sshd.common.future.CloseFuture close(boolean)
meth public java.lang.Object getFutureLock()
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
supr org.apache.sshd.common.util.closeable.IoBaseCloseable

CLSS public abstract org.apache.sshd.common.util.closeable.AbstractInnerCloseable
cons protected init()
cons protected init(java.lang.String)
meth protected abstract org.apache.sshd.common.Closeable getInnerCloseable()
meth protected final org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected final void doCloseImmediately()
supr org.apache.sshd.common.util.closeable.AbstractCloseable

CLSS public abstract org.apache.sshd.common.util.closeable.IoBaseCloseable
cons protected init()
cons protected init(java.lang.String)
intf org.apache.sshd.common.Closeable
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.common.util.logging.AbstractLoggingBean
cons protected init()
cons protected init(java.lang.String)
cons protected init(org.slf4j.Logger)
fld protected final org.slf4j.Logger log
meth protected org.apache.sshd.common.util.logging.SimplifiedLog getSimplifiedLogger()
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void info(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void info(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
supr java.lang.Object
hfds simplifiedLog

CLSS public abstract interface org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public abstract java.net.SocketAddress getLocalAddress()
meth public abstract java.net.SocketAddress getRemoteAddress()

CLSS public org.eclipse.jgit.internal.signing.ssh.OpenSshSigningKeyDatabase
cons public init()
intf org.eclipse.jgit.signing.ssh.CachingSigningKeyDatabase
meth public boolean isRevoked(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,java.security.PublicKey) throws java.io.IOException
meth public int getCacheSize()
meth public java.lang.String isAllowed(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,java.security.PublicKey,java.lang.String,org.eclipse.jgit.lib.PersonIdent) throws java.io.IOException,org.eclipse.jgit.signing.ssh.VerificationException
meth public void clearCache()
meth public void setCacheSize(int)
supr java.lang.Object
hfds DEFAULT_CACHE_SIZE,allowedSigners,cacheSize,revocations
hcls LRU

CLSS public final org.eclipse.jgit.internal.signing.ssh.SigningDatabase
meth public static org.eclipse.jgit.signing.ssh.SigningKeyDatabase getInstance()
meth public static org.eclipse.jgit.signing.ssh.SigningKeyDatabase setInstance(org.eclipse.jgit.signing.ssh.SigningKeyDatabase)
supr java.lang.Object
hfds INSTANCE

CLSS public org.eclipse.jgit.internal.signing.ssh.SshSignatureVerifier
cons public init()
intf org.eclipse.jgit.lib.SignatureVerifier
meth public java.lang.String getName()
meth public org.eclipse.jgit.lib.SignatureVerifier$SignatureVerification verify(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,byte[],byte[]) throws java.io.IOException
meth public void clear()
supr java.lang.Object
hfds LOG,OBJECT,TREE,TYPE

CLSS public org.eclipse.jgit.internal.signing.ssh.SshSigner
cons public init()
intf org.eclipse.jgit.lib.Signer
meth public boolean canLocateSigningKey(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,org.eclipse.jgit.lib.PersonIdent,java.lang.String,org.eclipse.jgit.transport.CredentialsProvider) throws org.eclipse.jgit.api.errors.CanceledException
meth public org.eclipse.jgit.lib.GpgSignature sign(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,byte[],org.eclipse.jgit.lib.PersonIdent,java.lang.String,org.eclipse.jgit.transport.CredentialsProvider) throws java.io.IOException,org.eclipse.jgit.api.errors.CanceledException,org.eclipse.jgit.api.errors.UnsupportedSigningFormatException
supr java.lang.Object
hfds GIT_KEY_PREFIX,LINE_LENGTH,LOG
hcls AgentIdentity,KeyPairIdentity

CLSS public org.eclipse.jgit.internal.transport.sshd.AuthenticationCanceledException
cons public init()
supr java.util.concurrent.CancellationException
hfds serialVersionUID

CLSS public org.eclipse.jgit.internal.transport.sshd.AuthenticationLogger
cons public init(org.apache.sshd.client.session.ClientSession)
meth public java.util.List<java.lang.String> getLog()
meth public void clear()
supr java.lang.Object
hfds gssLogger,messages,passwordLogger,pubkeyLogger

CLSS public org.eclipse.jgit.internal.transport.sshd.CachingKeyPairProvider
cons public init(java.util.List<java.nio.file.Path>,org.eclipse.jgit.transport.sshd.KeyCache)
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.util.Map<java.lang.String,java.nio.file.Path>> KEY_PATHS_BY_FINGERPRINT
intf java.lang.Iterable<java.security.KeyPair>
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public java.util.Iterator<java.security.KeyPair> iterator()
supr org.apache.sshd.common.keyprovider.FileKeyPairProvider
hfds cache
hcls CancellingKeyPairIterator

CLSS public org.eclipse.jgit.internal.transport.sshd.GssApiMechanisms
fld public final static java.lang.String GSSAPI_HOST_PREFIX = "host@"
fld public final static org.ietf.jgss.Oid KERBEROS_5
fld public final static org.ietf.jgss.Oid SPNEGO
meth public static java.lang.String getCanonicalName(java.net.InetSocketAddress)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public static java.net.InetAddress resolve(java.net.InetSocketAddress)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public static java.util.Collection<org.ietf.jgss.Oid> getSupportedMechanisms()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public static org.ietf.jgss.GSSContext createContext(org.ietf.jgss.Oid,java.lang.String)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth public static void closeContextSilently(org.ietf.jgss.GSSContext)
meth public static void failed(org.ietf.jgss.Oid)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public static void worked(org.ietf.jgss.Oid)
 anno 1 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds LOCK,supportedMechanisms

CLSS public org.eclipse.jgit.internal.transport.sshd.GssApiWithMicAuthFactory
fld public final static java.lang.String NAME = "gssapi-with-mic"
fld public final static org.eclipse.jgit.internal.transport.sshd.GssApiWithMicAuthFactory INSTANCE
meth public org.apache.sshd.client.auth.UserAuth createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
supr org.apache.sshd.client.auth.AbstractUserAuthFactory

CLSS public org.eclipse.jgit.internal.transport.sshd.GssApiWithMicAuthentication
cons public init()
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void destroy()
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth
hfds SSH_MSG_USERAUTH_GSSAPI_RESPONSE,SSH_MSG_USERAUTH_GSSAPI_TOKEN,context,currentMechanism,mechanisms,nextMechanism,state
hcls ProtocolState

CLSS public abstract interface org.eclipse.jgit.internal.transport.sshd.GssApiWithMicAuthenticationReporter
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<org.eclipse.jgit.internal.transport.sshd.GssApiWithMicAuthenticationReporter> GSS_AUTHENTICATION_REPORTER
meth public void signalAuthenticationAttempt(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)
meth public void signalAuthenticationExhausted(org.apache.sshd.client.session.ClientSession,java.lang.String)
meth public void signalAuthenticationFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,boolean,java.util.List<java.lang.String>)
meth public void signalAuthenticationSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitClientSession
cons public init(org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.common.io.IoSession) throws java.lang.Exception
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.util.function.Supplier<org.eclipse.jgit.transport.sshd.KeyPasswordProvider>> KEY_PASSWORD_PROVIDER_FACTORY
innr public static ChainingAttributes
innr public static SessionAttributes
meth protected byte[] sendKexInit() throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager)
meth protected java.lang.String resolveSessionKexProposal(java.lang.String) throws java.io.IOException
meth protected java.util.List<java.lang.String> doReadIdentification(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.StreamCorruptedException
meth protected org.apache.sshd.common.io.IoWriteFuture sendIdentification(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public java.util.List<java.lang.String> modifyAlgorithmList(java.util.List<java.lang.String>,java.util.Set<java.lang.String>,java.lang.String,java.lang.String)
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> getCipherFactories()
meth public org.apache.sshd.client.config.hosts.HostConfigEntry getHostConfigEntry()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.eclipse.jgit.transport.CredentialsProvider getCredentialsProvider()
meth public void messageReceived(org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void setCredentialsProvider(org.eclipse.jgit.transport.CredentialsProvider)
meth public void setHostConfigEntry(org.apache.sshd.client.config.hosts.HostConfigEntry)
meth public void setProxyHandler(org.eclipse.jgit.internal.transport.sshd.proxy.StatefulProxyConnector)
supr org.apache.sshd.client.session.ClientSessionImpl
hfds DEFAULT_MAX_IDENTIFICATION_SIZE,FORBIDDEN_CIPHERS,ciphers,credentialsProvider,hostConfig,isInitialKex,proxyHandler

CLSS public static org.eclipse.jgit.internal.transport.sshd.JGitClientSession$ChainingAttributes
 outer org.eclipse.jgit.internal.transport.sshd.JGitClientSession
cons public init(org.apache.sshd.common.AttributeRepository,org.apache.sshd.common.AttributeRepository)
intf org.apache.sshd.common.AttributeRepository
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public int getAttributesCount()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
supr java.lang.Object
hfds delegate,parent

CLSS public static org.eclipse.jgit.internal.transport.sshd.JGitClientSession$SessionAttributes
 outer org.eclipse.jgit.internal.transport.sshd.JGitClientSession
cons public init(org.apache.sshd.common.AttributeRepository,org.apache.sshd.common.AttributeRepository,org.apache.sshd.common.PropertyResolver)
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.util.Map<java.lang.String,java.lang.Object>> PROPERTIES
intf org.apache.sshd.common.PropertyResolver
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
supr org.eclipse.jgit.internal.transport.sshd.JGitClientSession$ChainingAttributes
hfds parentProperties

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitHostConfigEntry
cons public init()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getMultiValuedOptions()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public void setMultiValuedOptions(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
supr org.apache.sshd.client.config.hosts.HostConfigEntry
hfds multiValuedOptions

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitPublicKeyAuthFactory
fld public final static org.eclipse.jgit.internal.transport.sshd.JGitPublicKeyAuthFactory FACTORY
meth public org.apache.sshd.client.auth.pubkey.UserAuthPublicKey createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
supr org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitPublicKeyAuthentication
meth protected java.util.Iterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity> createPublicKeyIterator(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager) throws java.lang.Exception
meth protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity resolveAttemptedPublicKeyIdentity(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected void releaseKeys() throws java.io.IOException
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
supr org.apache.sshd.client.auth.pubkey.UserAuthPublicKey
hfds LOG_FORMAT,addKeysToAgent,agent,askBeforeAdding,constraints,hostConfig,skProvider
hcls KeyIterator

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitServerKeyVerifier
cons public init(org.eclipse.jgit.transport.sshd.ServerKeyDatabase)
 anno 1 org.eclipse.jgit.annotations.NonNull()
intf org.apache.sshd.client.keyverifier.ServerKeyVerifier
intf org.eclipse.jgit.internal.transport.sshd.ServerKeyLookup
meth public boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth public java.util.List<java.security.PublicKey> lookup(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress)
supr java.lang.Object
hfds LOG,database
hcls SessionConfig

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitSshClient
cons public init()
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.String> PREFERRED_AUTHENTICATIONS
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.nio.file.Path> HOME_DIRECTORY
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<org.apache.sshd.common.util.net.SshdSocketAddress> LOCAL_FORWARD_ADDRESS
meth protected org.apache.sshd.client.session.SessionFactory createSessionFactory()
meth protected org.eclipse.jgit.transport.sshd.ProxyDataFactory getProxyDatabase()
meth public java.util.function.Supplier<org.eclipse.jgit.transport.sshd.KeyPasswordProvider> getKeyPasswordProviderFactory()
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.eclipse.jgit.transport.CredentialsProvider getCredentialsProvider()
meth public void setCredentialsProvider(org.eclipse.jgit.transport.CredentialsProvider)
meth public void setKeyCache(org.eclipse.jgit.transport.sshd.KeyCache)
meth public void setKeyPasswordProviderFactory(java.util.function.Supplier<org.eclipse.jgit.transport.sshd.KeyPasswordProvider>)
meth public void setProxyDatabase(org.eclipse.jgit.transport.sshd.ProxyDataFactory)
supr org.apache.sshd.client.SshClient
hfds HOST_CONFIG_ENTRY,ORIGINAL_REMOTE_ADDRESS,credentialsProvider,keyCache,keyPasswordProviderFactory,proxyDatabase
hcls CombinedKeyIdentityProvider,JGitSessionFactory

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitSshConfig
cons public init(org.eclipse.jgit.transport.SshConfigStore)
intf org.apache.sshd.client.config.hosts.HostConfigEntryResolver
meth public org.apache.sshd.client.config.hosts.HostConfigEntry resolveEffectiveHost(java.lang.String,int,java.net.SocketAddress,java.lang.String,java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
supr java.lang.Object
hfds configFile

CLSS public org.eclipse.jgit.internal.transport.sshd.JGitUserInteraction
cons public init(org.eclipse.jgit.transport.CredentialsProvider)
intf org.apache.sshd.client.auth.keyboard.UserInteraction
meth public boolean isInteractionAllowed(org.apache.sshd.client.session.ClientSession)
meth public java.lang.String getUpdatedPassword(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)
meth public java.lang.String resolveAuthPasswordAttempt(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
meth public java.lang.String[] interactive(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])
meth public static org.eclipse.jgit.transport.URIish toURI(java.lang.String,java.net.InetSocketAddress)
supr java.lang.Object
hfds ongoing,provider
hcls SessionAuthMarker

CLSS public org.eclipse.jgit.internal.transport.sshd.KnownHostEntryReader
meth public static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readFromFile(java.nio.file.Path) throws java.io.IOException
supr java.lang.Object
hfds LOG

CLSS public org.eclipse.jgit.internal.transport.sshd.OpenSshServerKeyDatabase
cons public init(boolean,java.util.List<java.nio.file.Path>)
intf org.eclipse.jgit.transport.sshd.ServerKeyDatabase
meth public boolean accept(java.lang.String,java.net.InetSocketAddress,java.security.PublicKey,org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration,org.eclipse.jgit.transport.CredentialsProvider)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
 anno 4 org.eclipse.jgit.annotations.NonNull()
meth public java.util.List<java.security.PublicKey> lookup(java.lang.String,java.net.InetSocketAddress,org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds LOG,MARKER_CA,MARKER_REVOKED,askAboutNewFile,defaultFiles,knownHostsFiles,prng
hcls AskUser,HostKeyFile,RevokedKeyException

CLSS public org.eclipse.jgit.internal.transport.sshd.PasswordProviderWrapper
cons public init(java.util.function.Supplier<org.eclipse.jgit.transport.sshd.KeyPasswordProvider>)
 anno 1 org.eclipse.jgit.annotations.NonNull()
intf org.apache.sshd.common.config.keys.FilePasswordProvider
meth public java.lang.String getPassword(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int) throws java.io.IOException
meth public org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult handleDecodeAttemptResult(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int,java.lang.String,java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
supr java.lang.Object
hfds STATE,factory,noSessionState
hcls PerSessionState

CLSS public abstract interface org.eclipse.jgit.internal.transport.sshd.ServerKeyLookup
meth public abstract java.util.List<java.security.PublicKey> lookup(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress)
 anno 0 org.eclipse.jgit.annotations.NonNull()

CLSS public final org.eclipse.jgit.internal.transport.sshd.SshdText
cons public init()
fld public java.lang.String authGssApiAttempt
fld public java.lang.String authGssApiExhausted
fld public java.lang.String authGssApiFailure
fld public java.lang.String authGssApiNotTried
fld public java.lang.String authGssApiPartialSuccess
fld public java.lang.String authPasswordAttempt
fld public java.lang.String authPasswordChangeAttempt
fld public java.lang.String authPasswordExhausted
fld public java.lang.String authPasswordFailure
fld public java.lang.String authPasswordNotTried
fld public java.lang.String authPasswordPartialSuccess
fld public java.lang.String authPubkeyAttempt
fld public java.lang.String authPubkeyAttemptAgent
fld public java.lang.String authPubkeyExhausted
fld public java.lang.String authPubkeyFailure
fld public java.lang.String authPubkeyNoKeys
fld public java.lang.String authPubkeyPartialSuccess
fld public java.lang.String authenticationCanceled
fld public java.lang.String authenticationOnClosedSession
fld public java.lang.String cannotReadPublicKey
fld public java.lang.String closeListenerFailed
fld public java.lang.String configInvalidPath
fld public java.lang.String configInvalidPattern
fld public java.lang.String configInvalidPositive
fld public java.lang.String configInvalidProxyJump
fld public java.lang.String configNoKnownAlgorithms
fld public java.lang.String configProxyJumpNotSsh
fld public java.lang.String configProxyJumpWithPath
fld public java.lang.String configUnknownAlgorithm
fld public java.lang.String ftpCloseFailed
fld public java.lang.String gssapiFailure
fld public java.lang.String gssapiInitFailure
fld public java.lang.String gssapiUnexpectedMechanism
fld public java.lang.String gssapiUnexpectedMessage
fld public java.lang.String identityFileCannotDecrypt
fld public java.lang.String identityFileMultipleKeys
fld public java.lang.String identityFileNoKey
fld public java.lang.String identityFileNotFound
fld public java.lang.String identityFileUnsupportedFormat
fld public java.lang.String invalidSignatureAlgorithm
fld public java.lang.String kexServerKeyInvalid
fld public java.lang.String keyEncryptedMsg
fld public java.lang.String keyEncryptedPrompt
fld public java.lang.String keyEncryptedRetry
fld public java.lang.String keyLoadFailed
fld public java.lang.String knownHostsCouldNotUpdate
fld public java.lang.String knownHostsFileLockedUpdate
fld public java.lang.String knownHostsFileReadFailed
fld public java.lang.String knownHostsInvalidLine
fld public java.lang.String knownHostsInvalidPath
fld public java.lang.String knownHostsKeyFingerprints
fld public java.lang.String knownHostsModifiedKeyAcceptPrompt
fld public java.lang.String knownHostsModifiedKeyDenyMsg
fld public java.lang.String knownHostsModifiedKeyStorePrompt
fld public java.lang.String knownHostsModifiedKeyWarning
fld public java.lang.String knownHostsRevokedCertificateMsg
fld public java.lang.String knownHostsRevokedKeyMsg
fld public java.lang.String knownHostsUnknownKeyMsg
fld public java.lang.String knownHostsUnknownKeyPrompt
fld public java.lang.String knownHostsUnknownKeyType
fld public java.lang.String knownHostsUserAskCreationMsg
fld public java.lang.String knownHostsUserAskCreationPrompt
fld public java.lang.String loginDenied
fld public java.lang.String noExplicitKeys
fld public java.lang.String passwordPrompt
fld public java.lang.String pkcs11Error
fld public java.lang.String pkcs11FailedInstantiation
fld public java.lang.String pkcs11GeneralMessage
fld public java.lang.String pkcs11NoKeys
fld public java.lang.String pkcs11NonExisting
fld public java.lang.String pkcs11NotAbsolute
fld public java.lang.String pkcs11Unsupported
fld public java.lang.String pkcs11Warning
fld public java.lang.String proxyCannotAuthenticate
fld public java.lang.String proxyHttpFailure
fld public java.lang.String proxyHttpInvalidUserName
fld public java.lang.String proxyHttpUnexpectedReply
fld public java.lang.String proxyHttpUnspecifiedFailureReason
fld public java.lang.String proxyJumpAbort
fld public java.lang.String proxyPasswordPrompt
fld public java.lang.String proxySocksAuthenticationFailed
fld public java.lang.String proxySocksFailureForbidden
fld public java.lang.String proxySocksFailureGeneral
fld public java.lang.String proxySocksFailureHostUnreachable
fld public java.lang.String proxySocksFailureNetworkUnreachable
fld public java.lang.String proxySocksFailureRefused
fld public java.lang.String proxySocksFailureTTL
fld public java.lang.String proxySocksFailureUnspecified
fld public java.lang.String proxySocksFailureUnsupportedAddress
fld public java.lang.String proxySocksFailureUnsupportedCommand
fld public java.lang.String proxySocksGssApiFailure
fld public java.lang.String proxySocksGssApiMessageTooShort
fld public java.lang.String proxySocksGssApiUnknownMessage
fld public java.lang.String proxySocksGssApiVersionMismatch
fld public java.lang.String proxySocksNoRemoteHostName
fld public java.lang.String proxySocksPasswordTooLong
fld public java.lang.String proxySocksUnexpectedMessage
fld public java.lang.String proxySocksUnexpectedVersion
fld public java.lang.String proxySocksUsernameTooLong
fld public java.lang.String pubkeyAuthAddKeyToAgentError
fld public java.lang.String pubkeyAuthAddKeyToAgentQuestion
fld public java.lang.String pubkeyAuthWrongCommand
fld public java.lang.String pubkeyAuthWrongKey
fld public java.lang.String pubkeyAuthWrongSignatureAlgorithm
fld public java.lang.String serverIdNotReceived
fld public java.lang.String serverIdTooLong
fld public java.lang.String serverIdWithNul
fld public java.lang.String sessionCloseFailed
fld public java.lang.String sessionWithoutUsername
fld public java.lang.String signAllowedSignersCertAuthorityError
fld public java.lang.String signAllowedSignersEmptyIdentity
fld public java.lang.String signAllowedSignersEmptyNamespaces
fld public java.lang.String signAllowedSignersFormatError
fld public java.lang.String signAllowedSignersInvalidDate
fld public java.lang.String signAllowedSignersLineFormat
fld public java.lang.String signAllowedSignersMultiple
fld public java.lang.String signAllowedSignersNoIdentities
fld public java.lang.String signAllowedSignersPublicKeyParsing
fld public java.lang.String signAllowedSignersUnterminatedQuote
fld public java.lang.String signCertAlgorithmMismatch
fld public java.lang.String signCertAlgorithmUnknown
fld public java.lang.String signCertificateExpired
fld public java.lang.String signCertificateInvalid
fld public java.lang.String signCertificateNotForName
fld public java.lang.String signCertificateRevoked
fld public java.lang.String signCertificateTooEarly
fld public java.lang.String signCertificateWithoutPrincipals
fld public java.lang.String signDefaultKeyEmpty
fld public java.lang.String signDefaultKeyFailed
fld public java.lang.String signDefaultKeyInterrupted
fld public java.lang.String signGarbageAtEnd
fld public java.lang.String signInvalidAlgorithm
fld public java.lang.String signInvalidKeyDSA
fld public java.lang.String signInvalidMagic
fld public java.lang.String signInvalidNamespace
fld public java.lang.String signInvalidSignature
fld public java.lang.String signInvalidVersion
fld public java.lang.String signKeyExpired
fld public java.lang.String signKeyRevoked
fld public java.lang.String signKeyTooEarly
fld public java.lang.String signKrlBlobLeftover
fld public java.lang.String signKrlBlobLengthInvalid
fld public java.lang.String signKrlBlobLengthInvalidExpected
fld public java.lang.String signKrlCaKeyLengthInvalid
fld public java.lang.String signKrlCertificateLeftover
fld public java.lang.String signKrlCertificateSubsectionLeftover
fld public java.lang.String signKrlCertificateSubsectionLength
fld public java.lang.String signKrlEmptyRange
fld public java.lang.String signKrlInvalidBitSetLength
fld public java.lang.String signKrlInvalidKeyIdLength
fld public java.lang.String signKrlInvalidMagic
fld public java.lang.String signKrlInvalidReservedLength
fld public java.lang.String signKrlInvalidVersion
fld public java.lang.String signKrlNoCertificateSubsection
fld public java.lang.String signKrlSerialZero
fld public java.lang.String signKrlShortRange
fld public java.lang.String signKrlUnknownSection
fld public java.lang.String signKrlUnknownSubsection
fld public java.lang.String signLogFailure
fld public java.lang.String signMismatchedSignatureAlgorithm
fld public java.lang.String signNoAgent
fld public java.lang.String signNoPrincipalMatched
fld public java.lang.String signNoPublicKey
fld public java.lang.String signNoSigningKey
fld public java.lang.String signNotUserCertificate
fld public java.lang.String signPublicKeyError
fld public java.lang.String signSeeLog
fld public java.lang.String signSignatureError
fld public java.lang.String signStderr
fld public java.lang.String signTooManyPrivateKeys
fld public java.lang.String signUnknownHashAlgorithm
fld public java.lang.String signUnknownSignatureAlgorithm
fld public java.lang.String signWrongNamespace
fld public java.lang.String sshAgentEdDSAFormatError
fld public java.lang.String sshAgentPayloadLengthError
fld public java.lang.String sshAgentReplyLengthError
fld public java.lang.String sshAgentReplyUnexpected
fld public java.lang.String sshAgentShortReadBuffer
fld public java.lang.String sshAgentUnknownKey
fld public java.lang.String sshAgentWrongKeyLength
fld public java.lang.String sshAgentWrongNumberOfKeys
fld public java.lang.String sshClosingDown
fld public java.lang.String sshCommandTimeout
fld public java.lang.String sshProcessStillRunning
fld public java.lang.String sshProxySessionCloseFailed
fld public java.lang.String unknownProxyProtocol
meth public static org.eclipse.jgit.internal.transport.sshd.SshdText get()
supr org.eclipse.jgit.nls.TranslationBundle

CLSS public final org.eclipse.jgit.internal.transport.sshd.agent.ConnectorFactoryProvider
meth public static org.eclipse.jgit.transport.sshd.agent.ConnectorFactory getDefaultFactory()
meth public static void setDefaultFactory(org.eclipse.jgit.transport.sshd.agent.ConnectorFactory)
supr java.lang.Object
hfds INSTANCE

CLSS public org.eclipse.jgit.internal.transport.sshd.agent.JGitSshAgentFactory
cons public init(org.eclipse.jgit.transport.sshd.agent.ConnectorFactory,java.io.File)
 anno 1 org.eclipse.jgit.annotations.NonNull()
intf org.apache.sshd.agent.SshAgentFactory
meth public java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
supr java.lang.Object
hfds factory,homeDir

CLSS public org.eclipse.jgit.internal.transport.sshd.agent.SshAgentClient
cons public init(org.eclipse.jgit.transport.sshd.agent.Connector)
intf org.apache.sshd.agent.SshAgent
meth public !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public boolean isOpen()
meth public java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void removeAllIdentities() throws java.io.IOException
meth public void removeIdentity(java.security.PublicKey) throws java.io.IOException
supr java.lang.Object
hfds LOG,MAX_NUMBER_OF_KEYS,closed,connector

CLSS public abstract org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.net.InetSocketAddress)
fld protected boolean done
fld protected java.net.InetSocketAddress proxy
fld protected {org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler%0} params
intf org.eclipse.jgit.internal.transport.sshd.auth.AuthenticationHandler<{org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler%0},{org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler%1}>
meth public final boolean isDone()
meth public final void setParams({org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler%0})
supr java.lang.Object

CLSS public abstract interface org.eclipse.jgit.internal.transport.sshd.auth.AuthenticationHandler<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.io.Closeable
meth public abstract boolean isDone()
meth public abstract void close()
meth public abstract void process() throws java.lang.Exception
meth public abstract void setParams({org.eclipse.jgit.internal.transport.sshd.auth.AuthenticationHandler%0})
meth public abstract void start() throws java.lang.Exception
meth public abstract {org.eclipse.jgit.internal.transport.sshd.auth.AuthenticationHandler%1} getToken() throws java.lang.Exception

CLSS public abstract org.eclipse.jgit.internal.transport.sshd.auth.BasicAuthentication<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.net.InetSocketAddress,java.lang.String,char[])
fld protected byte[] password
fld protected java.lang.String user
meth protected void askCredentials()
meth protected void clearPassword()
meth public final void close()
meth public final void start() throws java.lang.Exception
meth public void process() throws java.lang.Exception
supr org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler<{org.eclipse.jgit.internal.transport.sshd.auth.BasicAuthentication%0},{org.eclipse.jgit.internal.transport.sshd.auth.BasicAuthentication%1}>

CLSS public abstract org.eclipse.jgit.internal.transport.sshd.auth.GssApiAuthentication<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.net.InetSocketAddress)
fld protected byte[] token
meth protected abstract byte[] extractToken({org.eclipse.jgit.internal.transport.sshd.auth.GssApiAuthentication%0}) throws java.lang.Exception
meth protected abstract org.ietf.jgss.GSSContext createContext() throws java.lang.Exception
meth public final void process() throws java.lang.Exception
meth public final void start() throws java.lang.Exception
meth public void close()
supr org.eclipse.jgit.internal.transport.sshd.auth.AbstractAuthenticationHandler<{org.eclipse.jgit.internal.transport.sshd.auth.GssApiAuthentication%0},{org.eclipse.jgit.internal.transport.sshd.auth.GssApiAuthentication%1}>
hfds context

CLSS public org.eclipse.jgit.internal.transport.sshd.pkcs11.Pkcs11Provider
meth public java.lang.Iterable<org.apache.sshd.client.auth.pubkey.KeyAgentIdentity> getKeys(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.lang.String getName()
meth public static org.eclipse.jgit.internal.transport.sshd.pkcs11.Pkcs11Provider getProvider(java.nio.file.Path,int) throws java.io.IOException
 anno 1 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds COUNT,LOG,NULL_AGENT,PROVIDERS,builder,keys,prompter,provider
hcls Pkcs11Identity

CLSS public org.eclipse.jgit.internal.transport.sshd.pkcs11.SecurityCallback
cons public init(org.eclipse.jgit.transport.URIish)
intf javax.security.auth.callback.CallbackHandler
meth public boolean passwordTried(java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth public int init(org.apache.sshd.common.session.SessionContext)
meth public void handle(javax.security.auth.callback.Callback[]) throws java.io.IOException,javax.security.auth.callback.UnsupportedCallbackException
supr java.lang.Object
hfds LOG,attempts,credentialsProvider,passwordProvider,uri

CLSS public abstract org.eclipse.jgit.internal.transport.sshd.proxy.AbstractClientProxyConnector
cons public init(java.net.InetSocketAddress,java.net.InetSocketAddress,java.lang.String,char[])
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
fld protected char[] proxyPassword
fld protected final java.net.InetSocketAddress proxyAddress
fld protected final java.net.InetSocketAddress remoteAddress
fld protected java.lang.String proxyUser
intf org.eclipse.jgit.internal.transport.sshd.proxy.StatefulProxyConnector
meth protected long getTimeout()
meth protected void adjustTimeout()
meth protected void clearPassword()
meth protected void init(org.apache.sshd.client.session.ClientSession)
meth protected void setDone(boolean) throws java.lang.Exception
meth public void runWhenDone(java.util.concurrent.Callable<java.lang.Void>) throws java.lang.Exception
supr java.lang.Object
hfds DEFAULT_PROXY_TIMEOUT_NANOS,bufferedCommands,done,lastProxyOperationTime,lock,remainingProxyProtocolTime,unregister

CLSS public org.eclipse.jgit.internal.transport.sshd.proxy.AuthenticationChallenge
cons public init(java.lang.String)
meth public java.lang.String getMechanism()
meth public java.lang.String getToken()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getArguments()
 anno 0 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds arguments,mechanism,token

CLSS public org.eclipse.jgit.internal.transport.sshd.proxy.HttpClientConnector
cons public init(java.net.InetSocketAddress,java.net.InetSocketAddress)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
cons public init(java.net.InetSocketAddress,java.net.InetSocketAddress,java.lang.String,char[])
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth public void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void sendClientProxyMetadata(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
supr org.eclipse.jgit.internal.transport.sshd.proxy.AbstractClientProxyConnector
hfds HTTP_HEADER_PROXY_AUTHENTICATION,HTTP_HEADER_PROXY_AUTHORIZATION,authenticator,availableAuthentications,basic,clientAuthentications,negotiate,ongoing
hcls HttpAuthenticationHandler,HttpBasicAuthentication,NegotiateAuthentication

CLSS public final org.eclipse.jgit.internal.transport.sshd.proxy.HttpParser
innr public static ParseException
meth public static java.util.List<org.eclipse.jgit.internal.transport.sshd.proxy.AuthenticationChallenge> getAuthenticationHeaders(java.util.List<java.lang.String>,java.lang.String)
meth public static org.eclipse.jgit.internal.transport.sshd.proxy.StatusLine parseStatusLine(java.lang.String) throws org.eclipse.jgit.internal.transport.sshd.proxy.HttpParser$ParseException
supr java.lang.Object

CLSS public static org.eclipse.jgit.internal.transport.sshd.proxy.HttpParser$ParseException
 outer org.eclipse.jgit.internal.transport.sshd.proxy.HttpParser
cons public init()
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public org.eclipse.jgit.internal.transport.sshd.proxy.Socks5ClientConnector
cons public init(java.net.InetSocketAddress,java.net.InetSocketAddress)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
cons public init(java.net.InetSocketAddress,java.net.InetSocketAddress,java.lang.String,char[])
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth public void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void sendClientProxyMetadata(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
supr org.eclipse.jgit.internal.transport.sshd.proxy.AbstractClientProxyConnector
hfds SOCKS_ADDRESS_FQDN,SOCKS_ADDRESS_IPv4,SOCKS_ADDRESS_IPv6,SOCKS_CMD_CONNECT,SOCKS_REPLY_ADDRESS_UNSUPPORTED,SOCKS_REPLY_COMMAND_UNSUPPORTED,SOCKS_REPLY_CONNECTION_REFUSED,SOCKS_REPLY_FAILURE,SOCKS_REPLY_FORBIDDEN,SOCKS_REPLY_HOST_UNREACHABLE,SOCKS_REPLY_NETWORK_UNREACHABLE,SOCKS_REPLY_SUCCESS,SOCKS_REPLY_TTL_EXPIRED,SOCKS_VERSION_5,authenticationProposals,authenticator,context,state
hcls ProtocolState,SocksAuthenticationMethod,SocksBasicAuthentication,SocksGssApiAuthentication

CLSS public abstract interface org.eclipse.jgit.internal.transport.sshd.proxy.StatefulProxyConnector
fld public final static java.lang.String TIMEOUT_PROPERTY
intf org.apache.sshd.client.session.ClientProxyConnector
meth public abstract void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public abstract void runWhenDone(java.util.concurrent.Callable<java.lang.Void>) throws java.lang.Exception

CLSS public org.eclipse.jgit.internal.transport.sshd.proxy.StatusLine
cons public init(java.lang.String,int,java.lang.String)
meth public int getResultCode()
meth public java.lang.String getReason()
meth public java.lang.String getVersion()
supr java.lang.Object
hfds reason,resultCode,version

CLSS public abstract interface org.eclipse.jgit.lib.SignatureVerifier
innr public final static !enum TrustLevel
innr public final static SignatureVerification
meth public abstract java.lang.String getName()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.lib.SignatureVerifier$SignatureVerification verify(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,byte[],byte[]) throws java.io.IOException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth public abstract void clear()

CLSS public abstract interface org.eclipse.jgit.lib.SignatureVerifierFactory
meth public abstract org.eclipse.jgit.lib.GpgConfig$GpgFormat getType()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.lib.SignatureVerifier create()
 anno 0 org.eclipse.jgit.annotations.NonNull()

CLSS public abstract interface org.eclipse.jgit.lib.Signer
meth public abstract boolean canLocateSigningKey(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,org.eclipse.jgit.lib.PersonIdent,java.lang.String,org.eclipse.jgit.transport.CredentialsProvider) throws org.eclipse.jgit.api.errors.CanceledException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.lib.GpgSignature sign(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,byte[],org.eclipse.jgit.lib.PersonIdent,java.lang.String,org.eclipse.jgit.transport.CredentialsProvider) throws java.io.IOException,org.eclipse.jgit.api.errors.CanceledException,org.eclipse.jgit.api.errors.UnsupportedSigningFormatException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 4 org.eclipse.jgit.annotations.NonNull()
meth public void signObject(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,org.eclipse.jgit.lib.ObjectBuilder,org.eclipse.jgit.lib.PersonIdent,java.lang.String,org.eclipse.jgit.transport.CredentialsProvider) throws java.io.IOException,org.eclipse.jgit.api.errors.CanceledException,org.eclipse.jgit.api.errors.UnsupportedSigningFormatException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
 anno 4 org.eclipse.jgit.annotations.NonNull()

CLSS public abstract interface org.eclipse.jgit.lib.SignerFactory
meth public abstract org.eclipse.jgit.lib.GpgConfig$GpgFormat getType()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.lib.Signer create()
 anno 0 org.eclipse.jgit.annotations.NonNull()

CLSS public abstract org.eclipse.jgit.nls.TranslationBundle
cons public init()
meth public java.util.Locale effectiveLocale()
meth public java.util.ResourceBundle resourceBundle()
supr java.lang.Object
hfds effectiveLocale,resourceBundle

CLSS public abstract interface org.eclipse.jgit.signing.ssh.CachingSigningKeyDatabase
intf org.eclipse.jgit.signing.ssh.SigningKeyDatabase
meth public abstract int getCacheSize()
meth public abstract void clearCache()
meth public abstract void setCacheSize(int)

CLSS public abstract interface org.eclipse.jgit.signing.ssh.SigningKeyDatabase
meth public abstract boolean isRevoked(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,java.security.PublicKey) throws java.io.IOException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
meth public abstract java.lang.String isAllowed(org.eclipse.jgit.lib.Repository,org.eclipse.jgit.lib.GpgConfig,java.security.PublicKey,java.lang.String,org.eclipse.jgit.lib.PersonIdent) throws java.io.IOException,org.eclipse.jgit.signing.ssh.VerificationException
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
 anno 4 org.eclipse.jgit.annotations.NonNull()
meth public static org.eclipse.jgit.signing.ssh.SigningKeyDatabase getInstance()
meth public static org.eclipse.jgit.signing.ssh.SigningKeyDatabase setInstance(org.eclipse.jgit.signing.ssh.SigningKeyDatabase)

CLSS public final org.eclipse.jgit.signing.ssh.SshSignatureVerifierFactory
cons public init()
intf org.eclipse.jgit.lib.SignatureVerifierFactory
meth public org.eclipse.jgit.lib.GpgConfig$GpgFormat getType()
meth public org.eclipse.jgit.lib.SignatureVerifier create()
supr java.lang.Object

CLSS public final org.eclipse.jgit.signing.ssh.SshSignerFactory
cons public init()
intf org.eclipse.jgit.lib.SignerFactory
meth public org.eclipse.jgit.lib.GpgConfig$GpgFormat getType()
meth public org.eclipse.jgit.lib.Signer create()
supr java.lang.Object

CLSS public org.eclipse.jgit.signing.ssh.VerificationException
cons public init(boolean,java.lang.String)
meth public boolean isExpired()
meth public java.lang.String getMessage()
meth public java.lang.String getReason()
supr java.lang.Exception
hfds expired,reason,serialVersionUID

CLSS public abstract interface org.eclipse.jgit.transport.RemoteSession
meth public abstract java.lang.Process exec(java.lang.String,int) throws java.io.IOException
meth public abstract void disconnect()
meth public org.eclipse.jgit.transport.FtpChannel getFtpChannel()

CLSS public abstract interface org.eclipse.jgit.transport.RemoteSession2
intf org.eclipse.jgit.transport.RemoteSession
meth public abstract java.lang.Process exec(java.lang.String,java.util.Map<java.lang.String,java.lang.String>,int) throws java.io.IOException

CLSS public abstract org.eclipse.jgit.transport.SshSessionFactory
cons public init()
meth public abstract java.lang.String getType()
meth public abstract org.eclipse.jgit.transport.RemoteSession getSession(org.eclipse.jgit.transport.URIish,org.eclipse.jgit.transport.CredentialsProvider,org.eclipse.jgit.util.FS,int) throws org.eclipse.jgit.errors.TransportException
meth public static java.lang.String getLocalUserName()
meth public static org.eclipse.jgit.transport.SshSessionFactory getInstance()
meth public static void setInstance(org.eclipse.jgit.transport.SshSessionFactory)
meth public void releaseSession(org.eclipse.jgit.transport.RemoteSession)
supr java.lang.Object
hcls DefaultFactory

CLSS public org.eclipse.jgit.transport.sshd.DefaultProxyDataFactory
cons public init()
intf org.eclipse.jgit.transport.sshd.ProxyDataFactory
meth public org.eclipse.jgit.transport.sshd.ProxyData get(java.net.InetSocketAddress)
supr java.lang.Object

CLSS public org.eclipse.jgit.transport.sshd.IdentityPasswordProvider
cons public init(org.eclipse.jgit.transport.CredentialsProvider)
innr protected static State
intf org.eclipse.jgit.transport.sshd.KeyPasswordProvider
meth protected boolean keyLoaded(org.eclipse.jgit.transport.URIish,org.eclipse.jgit.transport.sshd.IdentityPasswordProvider$State,char[],java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth protected char[] getPassword(org.eclipse.jgit.transport.URIish,int,org.eclipse.jgit.transport.sshd.IdentityPasswordProvider$State) throws java.io.IOException
 anno 3 org.eclipse.jgit.annotations.NonNull()
meth protected char[] getPassword(org.eclipse.jgit.transport.URIish,java.lang.String)
meth protected org.eclipse.jgit.transport.CredentialsProvider getCredentialsProvider()
meth protected void cancelAuthentication()
meth public boolean keyLoaded(org.eclipse.jgit.transport.URIish,int,java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth public char[] getPassphrase(org.eclipse.jgit.transport.URIish,int) throws java.io.IOException
meth public int getAttempts()
meth public void setAttempts(int)
supr java.lang.Object
hfds attempts,current,provider

CLSS protected static org.eclipse.jgit.transport.sshd.IdentityPasswordProvider$State
 outer org.eclipse.jgit.transport.sshd.IdentityPasswordProvider
cons protected init()
meth public char[] getPassword()
meth public int getCount()
meth public int incCount()
meth public void setPassword(char[])
supr java.lang.Object
hfds count,password

CLSS public org.eclipse.jgit.transport.sshd.JGitKeyCache
cons public init()
intf org.eclipse.jgit.transport.sshd.KeyCache
meth public java.security.KeyPair get(java.nio.file.Path,java.util.function.Function<? super java.nio.file.Path,? extends java.security.KeyPair>)
meth public void close()
supr java.lang.Object
hfds cache

CLSS public abstract interface org.eclipse.jgit.transport.sshd.KeyCache
meth public abstract java.security.KeyPair get(java.nio.file.Path,java.util.function.Function<? super java.nio.file.Path,? extends java.security.KeyPair>)
meth public abstract void close()

CLSS public abstract interface org.eclipse.jgit.transport.sshd.KeyPasswordProvider
meth public abstract boolean keyLoaded(org.eclipse.jgit.transport.URIish,int,java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract char[] getPassphrase(org.eclipse.jgit.transport.URIish,int) throws java.io.IOException
meth public abstract void setAttempts(int)
meth public int getAttempts()

CLSS public final org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory
innr public abstract interface static KeyPasswordProviderCreator
meth public static org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory$KeyPasswordProviderCreator getInstance()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public static org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory$KeyPasswordProviderCreator setInstance(org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory$KeyPasswordProviderCreator)
 anno 0 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds DEFAULT,INSTANCE

CLSS public abstract interface static org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory$KeyPasswordProviderCreator
 outer org.eclipse.jgit.transport.sshd.KeyPasswordProviderFactory
 anno 0 java.lang.FunctionalInterface()
intf java.util.function.Function<org.eclipse.jgit.transport.CredentialsProvider,org.eclipse.jgit.transport.sshd.KeyPasswordProvider>

CLSS public org.eclipse.jgit.transport.sshd.ProxyData
cons public init(java.net.Proxy)
 anno 1 org.eclipse.jgit.annotations.NonNull()
cons public init(java.net.Proxy,java.lang.String,char[])
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public char[] getPassword()
meth public java.lang.String getUser()
meth public java.net.Proxy getProxy()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public void clearPassword()
supr java.lang.Object
hfds proxy,proxyPassword,proxyUser

CLSS public abstract interface org.eclipse.jgit.transport.sshd.ProxyDataFactory
meth public abstract org.eclipse.jgit.transport.sshd.ProxyData get(java.net.InetSocketAddress)

CLSS public abstract interface org.eclipse.jgit.transport.sshd.ServerKeyDatabase
innr public abstract interface static Configuration
meth public abstract boolean accept(java.lang.String,java.net.InetSocketAddress,java.security.PublicKey,org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration,org.eclipse.jgit.transport.CredentialsProvider)
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()
 anno 4 org.eclipse.jgit.annotations.NonNull()
meth public abstract java.util.List<java.security.PublicKey> lookup(java.lang.String,java.net.InetSocketAddress,org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
 anno 3 org.eclipse.jgit.annotations.NonNull()

CLSS public abstract interface static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration
 outer org.eclipse.jgit.transport.sshd.ServerKeyDatabase
innr public final static !enum StrictHostKeyChecking
meth public abstract boolean getHashKnownHosts()
meth public abstract java.lang.String getUsername()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract java.util.List<java.lang.String> getGlobalKnownHostsFiles()
meth public abstract java.util.List<java.lang.String> getUserKnownHostsFiles()
meth public abstract org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking getStrictHostKeyChecking()
 anno 0 org.eclipse.jgit.annotations.NonNull()

CLSS public final static !enum org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking
 outer org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration
fld public final static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking ACCEPT_ANY
fld public final static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking ACCEPT_NEW
fld public final static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking ASK
fld public final static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking REQUIRE_MATCH
meth public static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking valueOf(java.lang.String)
meth public static org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking[] values()
supr java.lang.Enum<org.eclipse.jgit.transport.sshd.ServerKeyDatabase$Configuration$StrictHostKeyChecking>

CLSS public abstract interface org.eclipse.jgit.transport.sshd.SessionCloseListener
 anno 0 java.lang.FunctionalInterface()
meth public abstract void sessionClosed(org.eclipse.jgit.transport.sshd.SshdSession)

CLSS public org.eclipse.jgit.transport.sshd.SshdSession
intf org.eclipse.jgit.transport.RemoteSession2
meth public java.lang.Process exec(java.lang.String,int) throws java.io.IOException
meth public java.lang.Process exec(java.lang.String,java.util.Map<java.lang.String,java.lang.String>,int) throws java.io.IOException
meth public org.eclipse.jgit.transport.FtpChannel getFtpChannel()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public void addCloseListener(org.eclipse.jgit.transport.sshd.SessionCloseListener)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public void disconnect()
meth public void removeCloseListener(org.eclipse.jgit.transport.sshd.SessionCloseListener)
 anno 1 org.eclipse.jgit.annotations.NonNull()
supr java.lang.Object
hfds LOG,MAX_DEPTH,SHORT_SSH_FORMAT,client,listeners,session,uri
hcls SshdExecProcess,SshdFtpChannel

CLSS public org.eclipse.jgit.transport.sshd.SshdSessionFactory
cons public init()
cons public init(org.eclipse.jgit.transport.sshd.KeyCache,org.eclipse.jgit.transport.sshd.ProxyDataFactory)
intf java.io.Closeable
meth protected final org.eclipse.jgit.transport.sshd.KeyCache getKeyCache()
meth protected java.io.File getSshConfig(java.io.File)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth protected java.lang.Iterable<java.security.KeyPair> getDefaultKeys(java.io.File)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth protected java.lang.String getDefaultPreferredAuthentications()
meth protected java.util.List<java.nio.file.Path> getDefaultIdentities(java.io.File)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth protected java.util.List<java.nio.file.Path> getDefaultKnownHostsFiles(java.io.File)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth protected org.eclipse.jgit.transport.SshConfigStore createSshConfigStore(java.io.File,java.io.File,java.lang.String)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth protected org.eclipse.jgit.transport.sshd.KeyPasswordProvider createKeyPasswordProvider(org.eclipse.jgit.transport.CredentialsProvider)
meth protected org.eclipse.jgit.transport.sshd.ServerKeyDatabase createServerKeyDatabase(java.io.File,java.io.File)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth protected org.eclipse.jgit.transport.sshd.ServerKeyDatabase getServerKeyDatabase(java.io.File,java.io.File)
 anno 0 org.eclipse.jgit.annotations.NonNull()
 anno 1 org.eclipse.jgit.annotations.NonNull()
 anno 2 org.eclipse.jgit.annotations.NonNull()
meth protected org.eclipse.jgit.transport.sshd.agent.ConnectorFactory getConnectorFactory()
meth public java.io.File getHomeDirectory()
meth public java.io.File getSshDirectory()
meth public java.lang.String getType()
meth public org.eclipse.jgit.transport.sshd.SshdSession getSession(org.eclipse.jgit.transport.URIish,org.eclipse.jgit.transport.CredentialsProvider,org.eclipse.jgit.util.FS,int) throws org.eclipse.jgit.errors.TransportException
meth public void close()
meth public void setHomeDirectory(java.io.File)
 anno 1 org.eclipse.jgit.annotations.NonNull()
meth public void setSshDirectory(java.io.File)
 anno 1 org.eclipse.jgit.annotations.NonNull()
supr org.eclipse.jgit.transport.SshSessionFactory
hfds MINA_SSHD,closing,defaultHostConfigEntryResolver,defaultKeys,defaultServerKeyDatabase,homeDirectory,keyCache,proxies,sessions,sshDirectory
hcls Tuple

CLSS public final org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
cons public init()
innr public abstract interface static ConfigStoreFactory
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactory build(org.eclipse.jgit.transport.sshd.KeyCache)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setConfigFile(java.util.function.Function<java.io.File,java.io.File>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setConfigStoreFactory(org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder$ConfigStoreFactory)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setConnectorFactory(org.eclipse.jgit.transport.sshd.agent.ConnectorFactory)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setDefaultIdentities(java.util.function.Function<java.io.File,java.util.List<java.nio.file.Path>>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setDefaultKeysProvider(java.util.function.Function<java.io.File,java.lang.Iterable<java.security.KeyPair>>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setDefaultKnownHostsFiles(java.util.function.Function<java.io.File,java.util.List<java.nio.file.Path>>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setHomeDirectory(java.io.File)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setKeyPasswordProvider(java.util.function.Function<org.eclipse.jgit.transport.CredentialsProvider,org.eclipse.jgit.transport.sshd.KeyPasswordProvider>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setPreferredAuthentications(java.lang.String)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setProxyDataFactory(org.eclipse.jgit.transport.sshd.ProxyDataFactory)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setServerKeyDatabase(java.util.function.BiFunction<java.io.File,java.io.File,org.eclipse.jgit.transport.sshd.ServerKeyDatabase>)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder setSshDirectory(java.io.File)
meth public org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder withDefaultConnectorFactory()
supr java.lang.Object
hfds state
hcls State

CLSS public abstract interface static org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder$ConfigStoreFactory
 outer org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.eclipse.jgit.transport.SshConfigStore create(java.io.File,java.io.File,java.lang.String)
 anno 1 org.eclipse.jgit.annotations.NonNull()

CLSS public abstract org.eclipse.jgit.transport.sshd.agent.AbstractConnector
cons protected init()
cons protected init(int)
fld protected final static int DEFAULT_MAX_REPLY_LENGTH = 262144
intf org.eclipse.jgit.transport.sshd.agent.Connector
meth protected int getMaximumMessageLength()
meth protected int toLength(byte,byte[]) throws java.io.IOException
meth protected void prepareMessage(byte,byte[])
supr java.lang.Object
hfds MIN_REPLY_LENGTH,maxReplyLength

CLSS public abstract interface org.eclipse.jgit.transport.sshd.agent.Connector
intf java.io.Closeable
meth public abstract boolean connect() throws java.io.IOException
meth public abstract byte[] rpc(byte,byte[]) throws java.io.IOException
meth public byte[] rpc(byte) throws java.io.IOException

CLSS public abstract interface org.eclipse.jgit.transport.sshd.agent.ConnectorFactory
innr public abstract interface static ConnectorDescriptor
meth public abstract boolean isSupported()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.eclipse.jgit.transport.sshd.agent.ConnectorFactory$ConnectorDescriptor> getSupportedConnectors()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.transport.sshd.agent.Connector create(java.lang.String,java.io.File) throws java.io.IOException
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract org.eclipse.jgit.transport.sshd.agent.ConnectorFactory$ConnectorDescriptor getDefaultConnector()
meth public static org.eclipse.jgit.transport.sshd.agent.ConnectorFactory getDefault()
meth public static void setDefault(org.eclipse.jgit.transport.sshd.agent.ConnectorFactory)

CLSS public abstract interface static org.eclipse.jgit.transport.sshd.agent.ConnectorFactory$ConnectorDescriptor
 outer org.eclipse.jgit.transport.sshd.agent.ConnectorFactory
meth public abstract java.lang.String getDisplayName()
 anno 0 org.eclipse.jgit.annotations.NonNull()
meth public abstract java.lang.String getIdentityAgent()
 anno 0 org.eclipse.jgit.annotations.NonNull()

CLSS abstract interface org.eclipse.jgit.transport.sshd.agent.package-info

CLSS abstract interface org.eclipse.jgit.transport.sshd.package-info

