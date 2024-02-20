#Signature file v4.1
#Version 1.33

CLSS public com.amazonaws.AbortedException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public boolean isRetryable()
supr com.amazonaws.SdkClientException
hfds serialVersionUID

CLSS public com.amazonaws.AmazonClientException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public boolean isRetryable()
supr com.amazonaws.SdkBaseException
hfds serialVersionUID

CLSS public com.amazonaws.AmazonServiceException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
innr public final static !enum ErrorType
meth public byte[] getRawResponse()
meth public com.amazonaws.AmazonServiceException$ErrorType getErrorType()
meth public int getStatusCode()
meth public java.lang.String getErrorCode()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getMessage()
meth public java.lang.String getRawResponseContent()
meth public java.lang.String getRequestId()
meth public java.lang.String getServiceName()
meth public java.util.Map<java.lang.String,java.lang.String> getHttpHeaders()
meth public void setErrorCode(java.lang.String)
meth public void setErrorMessage(java.lang.String)
meth public void setErrorType(com.amazonaws.AmazonServiceException$ErrorType)
meth public void setHttpHeaders(java.util.Map<java.lang.String,java.lang.String>)
meth public void setRawResponse(byte[])
meth public void setRawResponseContent(java.lang.String)
meth public void setRequestId(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void setStatusCode(int)
supr com.amazonaws.SdkClientException
hfds errorCode,errorMessage,errorType,httpHeaders,rawResponse,requestId,serialVersionUID,serviceName,statusCode

CLSS public final static !enum com.amazonaws.AmazonServiceException$ErrorType
 outer com.amazonaws.AmazonServiceException
fld public final static com.amazonaws.AmazonServiceException$ErrorType Client
fld public final static com.amazonaws.AmazonServiceException$ErrorType Service
fld public final static com.amazonaws.AmazonServiceException$ErrorType Unknown
meth public static com.amazonaws.AmazonServiceException$ErrorType valueOf(java.lang.String)
meth public static com.amazonaws.AmazonServiceException$ErrorType[] values()
supr java.lang.Enum<com.amazonaws.AmazonServiceException$ErrorType>

CLSS public abstract com.amazonaws.AmazonWebServiceClient
cons protected init(com.amazonaws.ClientConfiguration,com.amazonaws.metrics.RequestMetricCollector,boolean)
cons protected init(com.amazonaws.client.AwsSyncClientParams)
cons public init(com.amazonaws.ClientConfiguration)
cons public init(com.amazonaws.ClientConfiguration,com.amazonaws.metrics.RequestMetricCollector)
fld protected com.amazonaws.ClientConfiguration clientConfiguration
fld protected com.amazonaws.http.AmazonHttpClient client
fld protected final java.util.List<com.amazonaws.handlers.RequestHandler2> requestHandler2s
fld protected int timeOffset
fld protected volatile java.net.URI endpoint
fld public final static boolean LOGGING_AWS_REQUEST_METRIC = true
 anno 0 java.lang.Deprecated()
meth protected boolean calculateCRC32FromCompressedData()
meth protected boolean shouldGenerateClientSideMonitoringEvents()
meth protected boolean useStrictHostNameVerification()
meth protected com.amazonaws.auth.Signer getSigner()
 anno 0 java.lang.Deprecated()
meth protected com.amazonaws.http.ExecutionContext createExecutionContext(com.amazonaws.AmazonWebServiceRequest)
meth protected com.amazonaws.http.ExecutionContext createExecutionContext(com.amazonaws.AmazonWebServiceRequest,com.amazonaws.internal.auth.SignerProvider)
meth protected com.amazonaws.internal.auth.SignerProvider createSignerProvider(com.amazonaws.auth.Signer)
meth protected com.amazonaws.internal.auth.SignerProvider getSignerProvider()
meth protected com.amazonaws.metrics.RequestMetricCollector requestMetricCollector()
meth protected final <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} beforeClientExecution({%%0})
meth protected final <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} beforeMarshalling({%%0})
meth protected final boolean isCsmEnabled()
meth protected final boolean isRequestMetricsEnabled(com.amazonaws.AmazonWebServiceRequest)
meth protected final com.amazonaws.http.ExecutionContext createExecutionContext(com.amazonaws.Request<?>)
meth protected final void checkMutability()
meth protected final void endClientExecution(com.amazonaws.util.AWSRequestMetrics,com.amazonaws.Request<?>,com.amazonaws.Response<?>)
meth protected final void endClientExecution(com.amazonaws.util.AWSRequestMetrics,com.amazonaws.Request<?>,com.amazonaws.Response<?>,boolean)
 anno 4 java.lang.Deprecated()
meth protected java.lang.String getClientId()
meth protected java.lang.String getServiceAbbreviation()
 anno 0 java.lang.Deprecated()
meth protected java.lang.String getServiceNameIntern()
meth protected java.lang.String getSigningRegion()
meth protected static boolean isProfilingEnabled()
meth protected void setEndpointPrefix(java.lang.String)
meth public <%0 extends com.amazonaws.AmazonWebServiceClient> {%%0} withEndpoint(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public <%0 extends com.amazonaws.AmazonWebServiceClient> {%%0} withRegion(com.amazonaws.regions.Region)
 anno 0 java.lang.Deprecated()
meth public <%0 extends com.amazonaws.AmazonWebServiceClient> {%%0} withRegion(com.amazonaws.regions.Regions)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.AmazonWebServiceClient withTimeOffset(int)
meth public com.amazonaws.ClientConfiguration getClientConfiguration()
meth public com.amazonaws.auth.Signer getSignerByURI(java.net.URI)
meth public com.amazonaws.metrics.RequestMetricCollector getRequestMetricsCollector()
meth public final java.lang.String getSignerRegionOverride()
meth public final void configureRegion(com.amazonaws.regions.Regions)
 anno 0 java.lang.Deprecated()
meth public final void makeImmutable()
 anno 0 java.lang.Deprecated()
meth public final void setServiceNameIntern(java.lang.String)
meth public final void setSignerRegionOverride(java.lang.String)
meth public int getTimeOffset()
meth public java.lang.String getEndpointPrefix()
meth public java.lang.String getServiceName()
meth public java.lang.String getSignerOverride()
meth public java.util.Collection<com.amazonaws.monitoring.MonitoringListener> getMonitoringListeners()
meth public void addRequestHandler(com.amazonaws.handlers.RequestHandler)
 anno 0 java.lang.Deprecated()
meth public void addRequestHandler(com.amazonaws.handlers.RequestHandler2)
 anno 0 java.lang.Deprecated()
meth public void removeRequestHandler(com.amazonaws.handlers.RequestHandler)
 anno 0 java.lang.Deprecated()
meth public void removeRequestHandler(com.amazonaws.handlers.RequestHandler2)
 anno 0 java.lang.Deprecated()
meth public void setEndpoint(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setEndpoint(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setRegion(com.amazonaws.regions.Region)
 anno 0 java.lang.Deprecated()
meth public void setTimeOffset(int)
meth public void shutdown()
supr java.lang.Object
hfds AMAZON,AWS,DEFAULT_CLIENT_ID,agentMonitoringListener,csmConfiguration,endpointPrefix,isImmutable,log,monitoringListeners,serviceName,signerProvider,signerRegionOverride,signingRegion

CLSS public abstract com.amazonaws.AmazonWebServiceRequest
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init()
fld public final static com.amazonaws.AmazonWebServiceRequest NOOP
intf com.amazonaws.HandlerContextAware
intf com.amazonaws.ReadLimitInfo
intf java.lang.Cloneable
meth protected final <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} copyBaseTo({%%0})
meth public <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} withGeneralProgressListener(com.amazonaws.event.ProgressListener)
meth public <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} withRequestCredentialsProvider(com.amazonaws.auth.AWSCredentialsProvider)
meth public <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} withRequestMetricCollector(com.amazonaws.metrics.RequestMetricCollector)
meth public <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} withSdkClientExecutionTimeout(int)
meth public <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} withSdkRequestTimeout(int)
meth public <%0 extends java.lang.Object> void addHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>,{%%0})
meth public <%0 extends java.lang.Object> {%%0} getHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>)
meth public com.amazonaws.AmazonWebServiceRequest clone()
meth public com.amazonaws.AmazonWebServiceRequest getCloneRoot()
meth public com.amazonaws.AmazonWebServiceRequest getCloneSource()
meth public com.amazonaws.RequestClientOptions getRequestClientOptions()
meth public com.amazonaws.auth.AWSCredentials getRequestCredentials()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.auth.AWSCredentialsProvider getRequestCredentialsProvider()
meth public com.amazonaws.event.ProgressListener getGeneralProgressListener()
meth public com.amazonaws.metrics.RequestMetricCollector getRequestMetricCollector()
meth public final int getReadLimit()
meth public java.lang.Integer getSdkClientExecutionTimeout()
meth public java.lang.Integer getSdkRequestTimeout()
meth public java.lang.String putCustomRequestHeader(java.lang.String,java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getCustomRequestHeaders()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getCustomQueryParameters()
meth public void putCustomQueryParameter(java.lang.String,java.lang.String)
meth public void setGeneralProgressListener(com.amazonaws.event.ProgressListener)
meth public void setRequestCredentials(com.amazonaws.auth.AWSCredentials)
 anno 0 java.lang.Deprecated()
meth public void setRequestCredentialsProvider(com.amazonaws.auth.AWSCredentialsProvider)
meth public void setRequestMetricCollector(com.amazonaws.metrics.RequestMetricCollector)
meth public void setSdkClientExecutionTimeout(int)
meth public void setSdkRequestTimeout(int)
supr java.lang.Object
hfds cloneSource,credentialsProvider,customQueryParameters,customRequestHeaders,handlerContext,progressListener,requestClientOptions,requestMetricCollector,sdkClientExecutionTimeout,sdkRequestTimeout

CLSS public com.amazonaws.AmazonWebServiceResponse<%0 extends java.lang.Object>
cons public init()
meth public com.amazonaws.ResponseMetadata getResponseMetadata()
meth public java.lang.String getRequestId()
meth public void setResponseMetadata(com.amazonaws.ResponseMetadata)
meth public void setResult({com.amazonaws.AmazonWebServiceResponse%0})
meth public {com.amazonaws.AmazonWebServiceResponse%0} getResult()
supr java.lang.Object
hfds responseMetadata,result

CLSS public com.amazonaws.AmazonWebServiceResult<%0 extends com.amazonaws.ResponseMetadata>
cons public init()
meth public com.amazonaws.AmazonWebServiceResult<{com.amazonaws.AmazonWebServiceResult%0}> setSdkHttpMetadata(com.amazonaws.http.SdkHttpMetadata)
meth public com.amazonaws.AmazonWebServiceResult<{com.amazonaws.AmazonWebServiceResult%0}> setSdkResponseMetadata({com.amazonaws.AmazonWebServiceResult%0})
meth public com.amazonaws.http.SdkHttpMetadata getSdkHttpMetadata()
meth public {com.amazonaws.AmazonWebServiceResult%0} getSdkResponseMetadata()
supr java.lang.Object
hfds sdkHttpMetadata,sdkResponseMetadata

CLSS public final com.amazonaws.ApacheHttpClientConfig
 anno 0 com.amazonaws.annotation.NotThreadSafe()
meth public com.amazonaws.ApacheHttpClientConfig withSslSocketFactory(org.apache.http.conn.socket.ConnectionSocketFactory)
meth public org.apache.http.conn.socket.ConnectionSocketFactory getSslSocketFactory()
meth public void setSslSocketFactory(org.apache.http.conn.socket.ConnectionSocketFactory)
supr java.lang.Object
hfds sslSocketFactory

CLSS public com.amazonaws.ClientConfiguration
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init()
cons public init(com.amazonaws.ClientConfiguration)
fld public final static boolean DEFAULT_CACHE_RESPONSE_METADATA = true
fld public final static boolean DEFAULT_DISABLE_SOCKET_PROXY = false
fld public final static boolean DEFAULT_TCP_KEEP_ALIVE = false
fld public final static boolean DEFAULT_THROTTLE_RETRIES = true
fld public final static boolean DEFAULT_USE_EXPECT_CONTINUE = true
fld public final static boolean DEFAULT_USE_GZIP = false
fld public final static boolean DEFAULT_USE_REAPER = true
fld public final static com.amazonaws.retry.RetryPolicy DEFAULT_RETRY_POLICY
fld public final static int DEFAULT_CLIENT_EXECUTION_TIMEOUT = 0
fld public final static int DEFAULT_CONNECTION_TIMEOUT = 10000
fld public final static int DEFAULT_MAX_CONNECTIONS = 50
fld public final static int DEFAULT_MAX_CONSECUTIVE_RETRIES_BEFORE_THROTTLING = 100
fld public final static int DEFAULT_REQUEST_TIMEOUT = 0
fld public final static int DEFAULT_RESPONSE_METADATA_CACHE_SIZE = 50
fld public final static int DEFAULT_SOCKET_TIMEOUT = 50000
fld public final static int DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS = 5000
fld public final static java.lang.String DEFAULT_USER_AGENT
fld public final static long DEFAULT_CONNECTION_MAX_IDLE_MILLIS = 60000
fld public final static long DEFAULT_CONNECTION_TTL = -1
meth public boolean disableSocketProxy()
meth public boolean getCacheResponseMetadata()
meth public boolean isDisableHostPrefixInjection()
meth public boolean isPreemptiveBasicProxyAuth()
meth public boolean isUseExpectContinue()
meth public boolean useGzip()
meth public boolean useReaper()
meth public boolean useTcpKeepAlive()
meth public boolean useThrottledRetries()
meth public com.amazonaws.ApacheHttpClientConfig getApacheHttpClientConfig()
meth public com.amazonaws.ClientConfiguration withCacheResponseMetadata(boolean)
meth public com.amazonaws.ClientConfiguration withClientExecutionTimeout(int)
meth public com.amazonaws.ClientConfiguration withConnectionMaxIdleMillis(long)
meth public com.amazonaws.ClientConfiguration withConnectionTTL(long)
meth public com.amazonaws.ClientConfiguration withConnectionTimeout(int)
meth public com.amazonaws.ClientConfiguration withDisableHostPrefixInjection(boolean)
meth public com.amazonaws.ClientConfiguration withDisableSocketProxy(boolean)
meth public com.amazonaws.ClientConfiguration withDnsResolver(com.amazonaws.DnsResolver)
meth public com.amazonaws.ClientConfiguration withGzip(boolean)
meth public com.amazonaws.ClientConfiguration withHeader(java.lang.String,java.lang.String)
meth public com.amazonaws.ClientConfiguration withLocalAddress(java.net.InetAddress)
meth public com.amazonaws.ClientConfiguration withMaxConnections(int)
meth public com.amazonaws.ClientConfiguration withMaxConsecutiveRetriesBeforeThrottling(int)
meth public com.amazonaws.ClientConfiguration withMaxErrorRetry(int)
meth public com.amazonaws.ClientConfiguration withNonProxyHosts(java.lang.String)
meth public com.amazonaws.ClientConfiguration withPreemptiveBasicProxyAuth(boolean)
meth public com.amazonaws.ClientConfiguration withProtocol(com.amazonaws.Protocol)
meth public com.amazonaws.ClientConfiguration withProxyAuthenticationMethods(java.util.List<com.amazonaws.ProxyAuthenticationMethod>)
meth public com.amazonaws.ClientConfiguration withProxyDomain(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyHost(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyPassword(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyPort(int)
meth public com.amazonaws.ClientConfiguration withProxyProtocol(com.amazonaws.Protocol)
meth public com.amazonaws.ClientConfiguration withProxyUsername(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyWorkstation(java.lang.String)
meth public com.amazonaws.ClientConfiguration withReaper(boolean)
meth public com.amazonaws.ClientConfiguration withRequestTimeout(int)
meth public com.amazonaws.ClientConfiguration withResponseMetadataCacheSize(int)
meth public com.amazonaws.ClientConfiguration withRetryPolicy(com.amazonaws.retry.RetryPolicy)
meth public com.amazonaws.ClientConfiguration withSecureRandom(java.security.SecureRandom)
meth public com.amazonaws.ClientConfiguration withSignerOverride(java.lang.String)
meth public com.amazonaws.ClientConfiguration withSocketBufferSizeHints(int,int)
meth public com.amazonaws.ClientConfiguration withSocketTimeout(int)
meth public com.amazonaws.ClientConfiguration withTcpKeepAlive(boolean)
meth public com.amazonaws.ClientConfiguration withThrottledRetries(boolean)
meth public com.amazonaws.ClientConfiguration withUseExpectContinue(boolean)
meth public com.amazonaws.ClientConfiguration withUserAgent(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.ClientConfiguration withUserAgentPrefix(java.lang.String)
meth public com.amazonaws.ClientConfiguration withUserAgentSuffix(java.lang.String)
meth public com.amazonaws.ClientConfiguration withValidateAfterInactivityMillis(int)
meth public com.amazonaws.DnsResolver getDnsResolver()
meth public com.amazonaws.Protocol getProtocol()
meth public com.amazonaws.Protocol getProxyProtocol()
meth public com.amazonaws.retry.RetryPolicy getRetryPolicy()
meth public int getClientExecutionTimeout()
meth public int getConnectionTimeout()
meth public int getMaxConnections()
meth public int getMaxConsecutiveRetriesBeforeThrottling()
meth public int getMaxErrorRetry()
meth public int getProxyPort()
meth public int getRequestTimeout()
meth public int getResponseMetadataCacheSize()
meth public int getSocketTimeout()
meth public int getValidateAfterInactivityMillis()
meth public int[] getSocketBufferSizeHints()
meth public java.lang.String getNonProxyHosts()
meth public java.lang.String getProxyDomain()
meth public java.lang.String getProxyHost()
meth public java.lang.String getProxyPassword()
meth public java.lang.String getProxyUsername()
meth public java.lang.String getProxyWorkstation()
meth public java.lang.String getSignerOverride()
meth public java.lang.String getUserAgent()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getUserAgentPrefix()
meth public java.lang.String getUserAgentSuffix()
meth public java.net.InetAddress getLocalAddress()
meth public java.security.SecureRandom getSecureRandom()
meth public java.util.List<com.amazonaws.ProxyAuthenticationMethod> getProxyAuthenticationMethods()
meth public java.util.Map<java.lang.String,java.lang.String> getHeaders()
meth public long getConnectionMaxIdleMillis()
meth public long getConnectionTTL()
meth public void addHeader(java.lang.String,java.lang.String)
meth public void setCacheResponseMetadata(boolean)
meth public void setClientExecutionTimeout(int)
meth public void setConnectionMaxIdleMillis(long)
meth public void setConnectionTTL(long)
meth public void setConnectionTimeout(int)
meth public void setDisableHostPrefixInjection(boolean)
meth public void setDisableSocketProxy(boolean)
meth public void setDnsResolver(com.amazonaws.DnsResolver)
meth public void setLocalAddress(java.net.InetAddress)
meth public void setMaxConnections(int)
meth public void setMaxConsecutiveRetriesBeforeThrottling(int)
meth public void setMaxErrorRetry(int)
meth public void setNonProxyHosts(java.lang.String)
meth public void setPreemptiveBasicProxyAuth(java.lang.Boolean)
meth public void setProtocol(com.amazonaws.Protocol)
meth public void setProxyAuthenticationMethods(java.util.List<com.amazonaws.ProxyAuthenticationMethod>)
meth public void setProxyDomain(java.lang.String)
meth public void setProxyHost(java.lang.String)
meth public void setProxyPassword(java.lang.String)
meth public void setProxyPort(int)
meth public void setProxyProtocol(com.amazonaws.Protocol)
meth public void setProxyUsername(java.lang.String)
meth public void setProxyWorkstation(java.lang.String)
meth public void setRequestTimeout(int)
meth public void setResponseMetadataCacheSize(int)
meth public void setRetryPolicy(com.amazonaws.retry.RetryPolicy)
meth public void setSecureRandom(java.security.SecureRandom)
meth public void setSignerOverride(java.lang.String)
meth public void setSocketBufferSizeHints(int,int)
meth public void setSocketTimeout(int)
meth public void setUseExpectContinue(boolean)
meth public void setUseGzip(boolean)
meth public void setUseReaper(boolean)
meth public void setUseTcpKeepAlive(boolean)
meth public void setUseThrottleRetries(boolean)
meth public void setUserAgent(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setUserAgentPrefix(java.lang.String)
meth public void setUserAgentSuffix(java.lang.String)
meth public void setValidateAfterInactivityMillis(int)
supr java.lang.Object
hfds apacheHttpClientConfig,cacheResponseMetadata,clientExecutionTimeout,connectionMaxIdleMillis,connectionTTL,connectionTimeout,disableHostPrefixInjection,disableSocketProxy,dnsResolver,headers,localAddress,maxConnections,maxConsecutiveRetriesBeforeThrottling,maxErrorRetry,nonProxyHosts,preemptiveBasicProxyAuth,protocol,proxyAuthenticationMethods,proxyDomain,proxyHost,proxyPassword,proxyPort,proxyProtocol,proxyUsername,proxyWorkstation,requestTimeout,responseMetadataCacheSize,retryPolicy,secureRandom,signerOverride,socketReceiveBufferSizeHint,socketSendBufferSizeHint,socketTimeout,tcpKeepAlive,throttleRetries,useExpectContinue,useGzip,useReaper,userAgentPrefix,userAgentSuffix,validateAfterInactivityMillis

CLSS public com.amazonaws.ClientConfigurationFactory
cons public init()
meth protected com.amazonaws.ClientConfiguration getDefaultConfig()
meth protected com.amazonaws.ClientConfiguration getInRegionOptimizedConfig()
meth public final com.amazonaws.ClientConfiguration getConfig()
supr java.lang.Object

CLSS public com.amazonaws.DefaultRequest<%0 extends java.lang.Object>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init(com.amazonaws.AmazonWebServiceRequest,java.lang.String)
cons public init(java.lang.String)
intf com.amazonaws.Request<{com.amazonaws.DefaultRequest%0}>
meth public <%0 extends java.lang.Object> void addHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>,{%%0})
meth public <%0 extends java.lang.Object> {%%0} getHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>)
meth public com.amazonaws.AmazonWebServiceRequest getOriginalRequest()
meth public com.amazonaws.ReadLimitInfo getReadLimitInfo()
meth public com.amazonaws.Request<{com.amazonaws.DefaultRequest%0}> withParameter(java.lang.String,java.lang.String)
meth public com.amazonaws.Request<{com.amazonaws.DefaultRequest%0}> withTimeOffset(int)
meth public com.amazonaws.http.HttpMethodName getHttpMethod()
meth public com.amazonaws.util.AWSRequestMetrics getAWSRequestMetrics()
meth public int getTimeOffset()
meth public java.io.InputStream getContent()
meth public java.io.InputStream getContentUnwrapped()
meth public java.lang.Object getOriginalRequestObject()
meth public java.lang.String getResourcePath()
meth public java.lang.String getServiceName()
meth public java.lang.String toString()
meth public java.net.URI getEndpoint()
meth public java.util.Map<java.lang.String,java.lang.String> getHeaders()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getParameters()
meth public void addHeader(java.lang.String,java.lang.String)
meth public void addParameter(java.lang.String,java.lang.String)
meth public void addParameters(java.lang.String,java.util.List<java.lang.String>)
meth public void setAWSRequestMetrics(com.amazonaws.util.AWSRequestMetrics)
meth public void setContent(java.io.InputStream)
meth public void setEndpoint(java.net.URI)
meth public void setHeaders(java.util.Map<java.lang.String,java.lang.String>)
meth public void setHttpMethod(com.amazonaws.http.HttpMethodName)
meth public void setParameters(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth public void setResourcePath(java.lang.String)
meth public void setTimeOffset(int)
supr java.lang.Object
hfds content,endpoint,handlerContext,headers,httpMethod,metrics,originalRequest,parameters,resourcePath,serviceName,timeOffset

CLSS public abstract interface com.amazonaws.DnsResolver
meth public abstract java.net.InetAddress[] resolve(java.lang.String) throws java.net.UnknownHostException

CLSS public abstract interface com.amazonaws.HandlerContextAware
meth public abstract <%0 extends java.lang.Object> void addHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>,{%%0})
meth public abstract <%0 extends java.lang.Object> {%%0} getHandlerContext(com.amazonaws.handlers.HandlerContextKey<{%%0}>)

CLSS public final !enum com.amazonaws.HttpMethod
fld public final static com.amazonaws.HttpMethod DELETE
fld public final static com.amazonaws.HttpMethod GET
fld public final static com.amazonaws.HttpMethod HEAD
fld public final static com.amazonaws.HttpMethod PATCH
fld public final static com.amazonaws.HttpMethod POST
fld public final static com.amazonaws.HttpMethod PUT
meth public static com.amazonaws.HttpMethod valueOf(java.lang.String)
meth public static com.amazonaws.HttpMethod[] values()
supr java.lang.Enum<com.amazonaws.HttpMethod>

CLSS public abstract interface com.amazonaws.ImmutableRequest<%0 extends java.lang.Object>
meth public abstract com.amazonaws.ReadLimitInfo getReadLimitInfo()
meth public abstract com.amazonaws.http.HttpMethodName getHttpMethod()
meth public abstract int getTimeOffset()
meth public abstract java.io.InputStream getContent()
meth public abstract java.io.InputStream getContentUnwrapped()
meth public abstract java.lang.Object getOriginalRequestObject()
meth public abstract java.lang.String getResourcePath()
meth public abstract java.net.URI getEndpoint()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getHeaders()
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> getParameters()

CLSS public com.amazonaws.PredefinedClientConfigurations
cons public init()
meth public static com.amazonaws.ClientConfiguration defaultConfig()
meth public static com.amazonaws.ClientConfiguration dynamoDefault()
meth public static com.amazonaws.ClientConfiguration swfDefault()
supr java.lang.Object

CLSS public final !enum com.amazonaws.Protocol
fld public final static com.amazonaws.Protocol HTTP
fld public final static com.amazonaws.Protocol HTTPS
meth public java.lang.String toString()
meth public static com.amazonaws.Protocol valueOf(java.lang.String)
meth public static com.amazonaws.Protocol[] values()
supr java.lang.Enum<com.amazonaws.Protocol>
hfds protocol

CLSS public final !enum com.amazonaws.ProxyAuthenticationMethod
fld public final static com.amazonaws.ProxyAuthenticationMethod BASIC
fld public final static com.amazonaws.ProxyAuthenticationMethod DIGEST
fld public final static com.amazonaws.ProxyAuthenticationMethod KERBEROS
fld public final static com.amazonaws.ProxyAuthenticationMethod NTLM
fld public final static com.amazonaws.ProxyAuthenticationMethod SPNEGO
meth public static com.amazonaws.ProxyAuthenticationMethod valueOf(java.lang.String)
meth public static com.amazonaws.ProxyAuthenticationMethod[] values()
supr java.lang.Enum<com.amazonaws.ProxyAuthenticationMethod>

CLSS public abstract interface com.amazonaws.ReadLimitInfo
meth public abstract int getReadLimit()

CLSS public abstract interface com.amazonaws.Request<%0 extends java.lang.Object>
intf com.amazonaws.HandlerContextAware
intf com.amazonaws.SignableRequest<{com.amazonaws.Request%0}>
meth public abstract com.amazonaws.AmazonWebServiceRequest getOriginalRequest()
meth public abstract com.amazonaws.Request<{com.amazonaws.Request%0}> withParameter(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.Request<{com.amazonaws.Request%0}> withTimeOffset(int)
meth public abstract com.amazonaws.util.AWSRequestMetrics getAWSRequestMetrics()
meth public abstract java.lang.String getServiceName()
meth public abstract void addParameters(java.lang.String,java.util.List<java.lang.String>)
meth public abstract void setAWSRequestMetrics(com.amazonaws.util.AWSRequestMetrics)
meth public abstract void setEndpoint(java.net.URI)
meth public abstract void setHeaders(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract void setHttpMethod(com.amazonaws.http.HttpMethodName)
meth public abstract void setParameters(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth public abstract void setResourcePath(java.lang.String)
meth public abstract void setTimeOffset(int)

CLSS public final com.amazonaws.RequestClientOptions
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init()
fld public final static int DEFAULT_STREAM_BUFFER_SIZE = 131073
innr public final static !enum Marker
meth public boolean isSkipAppendUriPath()
meth public final int getReadLimit()
meth public final void setReadLimit(int)
meth public java.lang.String getClientMarker(com.amazonaws.RequestClientOptions$Marker)
meth public void appendUserAgent(java.lang.String)
meth public void putClientMarker(com.amazonaws.RequestClientOptions$Marker,java.lang.String)
meth public void setSkipAppendUriPath(boolean)
supr java.lang.Object
hfds markers,readLimit,skipAppendUriPath

CLSS public final static !enum com.amazonaws.RequestClientOptions$Marker
 outer com.amazonaws.RequestClientOptions
fld public final static com.amazonaws.RequestClientOptions$Marker USER_AGENT
meth public static com.amazonaws.RequestClientOptions$Marker valueOf(java.lang.String)
meth public static com.amazonaws.RequestClientOptions$Marker[] values()
supr java.lang.Enum<com.amazonaws.RequestClientOptions$Marker>

CLSS public abstract com.amazonaws.RequestConfig
cons public init()
fld public final static com.amazonaws.RequestConfig NO_OP
meth public abstract com.amazonaws.RequestClientOptions getRequestClientOptions()
meth public abstract com.amazonaws.auth.AWSCredentialsProvider getCredentialsProvider()
meth public abstract com.amazonaws.event.ProgressListener getProgressListener()
meth public abstract com.amazonaws.metrics.RequestMetricCollector getRequestMetricsCollector()
meth public abstract java.lang.Integer getClientExecutionTimeout()
meth public abstract java.lang.Integer getRequestTimeout()
meth public abstract java.lang.Object getOriginalRequest()
meth public abstract java.lang.String getRequestType()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getCustomRequestHeaders()
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> getCustomQueryParameters()
supr java.lang.Object

CLSS public com.amazonaws.ResetException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public boolean isRetryable()
meth public java.lang.String getExtraInfo()
meth public java.lang.String getMessage()
meth public void setExtraInfo(java.lang.String)
supr com.amazonaws.SdkClientException
hfds extraInfo,serialVersionUID

CLSS public final com.amazonaws.Response<%0 extends java.lang.Object>
cons public init({com.amazonaws.Response%0},com.amazonaws.http.HttpResponse)
meth public com.amazonaws.http.HttpResponse getHttpResponse()
meth public {com.amazonaws.Response%0} getAwsResponse()
supr java.lang.Object
hfds httpResponse,response

CLSS public com.amazonaws.ResponseMetadata
cons public init(com.amazonaws.ResponseMetadata)
cons public init(java.util.Map<java.lang.String,java.lang.String>)
fld protected final java.util.Map<java.lang.String,java.lang.String> metadata
fld public final static java.lang.String AWS_EXTENDED_REQUEST_ID = "AWS_EXTENDED_REQUEST_ID"
fld public final static java.lang.String AWS_REQUEST_ID = "AWS_REQUEST_ID"
meth public java.lang.String getRequestId()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.SDKGlobalConfiguration
cons public init()
fld public final static int DEFAULT_AWS_CSM_PORT = 31000
fld public final static java.lang.String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY_ID"
fld public final static java.lang.String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId"
fld public final static java.lang.String ALTERNATE_ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY"
fld public final static java.lang.String ALTERNATE_SECRET_KEY_ENV_VAR = "AWS_SECRET_ACCESS_KEY"
fld public final static java.lang.String AWS_CBOR_DISABLE_ENV_VAR = "AWS_CBOR_DISABLE"
fld public final static java.lang.String AWS_CBOR_DISABLE_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableCbor"
fld public final static java.lang.String AWS_CONFIG_FILE_ENV_VAR = "AWS_CONFIG_FILE"
fld public final static java.lang.String AWS_CSM_CLIENT_ID_ENV_VAR = "AWS_CSM_CLIENT_ID"
fld public final static java.lang.String AWS_CSM_CLIENT_ID_SYSTEM_PROPERTY = "com.amazonaws.sdk.csm.clientId"
fld public final static java.lang.String AWS_CSM_ENABLED_ENV_VAR = "AWS_CSM_ENABLED"
fld public final static java.lang.String AWS_CSM_ENABLED_SYSTEM_PROPERTY = "com.amazonaws.sdk.csm.enabled"
fld public final static java.lang.String AWS_CSM_PORT_ENV_VAR = "AWS_CSM_PORT"
fld public final static java.lang.String AWS_CSM_PORT_SYSTEM_PROPERTY = "com.amazonaws.sdk.csm.port"
fld public final static java.lang.String AWS_EC2_METADATA_DISABLED_ENV_VAR = "AWS_EC2_METADATA_DISABLED"
fld public final static java.lang.String AWS_EC2_METADATA_DISABLED_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableEc2Metadata"
fld public final static java.lang.String AWS_ION_BINARY_DISABLE_ENV_VAR = "AWS_ION_BINARY_DISABLE"
fld public final static java.lang.String AWS_ION_BINARY_DISABLE_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableIonBinary"
fld public final static java.lang.String AWS_REGION_ENV_VAR = "AWS_REGION"
fld public final static java.lang.String AWS_REGION_SYSTEM_PROPERTY = "aws.region"
fld public final static java.lang.String AWS_SESSION_TOKEN_ENV_VAR = "AWS_SESSION_TOKEN"
fld public final static java.lang.String DEFAULT_AWS_CSM_CLIENT_ID = ""
fld public final static java.lang.String DEFAULT_METRICS_SYSTEM_PROPERTY = "com.amazonaws.sdk.enableDefaultMetrics"
fld public final static java.lang.String DEFAULT_S3_STREAM_BUFFER_SIZE = "com.amazonaws.sdk.s3.defaultStreamBufferSize"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String DISABLE_CERT_CHECKING_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableCertChecking"
fld public final static java.lang.String DISABLE_REMOTE_REGIONS_FILE_SYSTEM_PROPERTY = "com.amazonaws.regions.RegionUtils.disableRemote"
fld public final static java.lang.String DISABLE_S3_IMPLICIT_GLOBAL_CLIENTS_SYSTEM_PROPERTY = "com.amazonaws.services.s3.disableImplicitGlobalClients"
fld public final static java.lang.String EC2_METADATA_SERVICE_OVERRIDE_SYSTEM_PROPERTY = "com.amazonaws.sdk.ec2MetadataServiceEndpointOverride"
fld public final static java.lang.String ENABLE_IN_REGION_OPTIMIZED_MODE = "com.amazonaws.sdk.enableInRegionOptimizedMode"
fld public final static java.lang.String ENABLE_S3_SIGV4_SYSTEM_PROPERTY = "com.amazonaws.services.s3.enableV4"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ENFORCE_S3_SIGV4_SYSTEM_PROPERTY = "com.amazonaws.services.s3.enforceV4"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROFILING_SYSTEM_PROPERTY = "com.amazonaws.sdk.enableRuntimeProfiling"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String REGIONS_FILE_OVERRIDE_SYSTEM_PROPERTY = "com.amazonaws.regions.RegionUtils.fileOverride"
fld public final static java.lang.String RETRY_THROTTLING_SYSTEM_PROPERTY = "com.amazonaws.sdk.enableThrottledRetry"
fld public final static java.lang.String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY"
fld public final static java.lang.String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey"
fld public final static java.lang.String SESSION_TOKEN_SYSTEM_PROPERTY = "aws.sessionToken"
meth public static boolean isCborDisabled()
meth public static boolean isCertCheckingDisabled()
meth public static boolean isEc2MetadataDisabled()
meth public static boolean isInRegionOptimizedModeEnabled()
meth public static boolean isIonBinaryDisabled()
meth public static int getGlobalTimeOffset()
 anno 0 java.lang.Deprecated()
meth public static void setGlobalTimeOffset(int)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public com.amazonaws.SDKGlobalTime
cons public init()
meth public static int getGlobalTimeOffset()
meth public static void setGlobalTimeOffset(int)
supr java.lang.Object
hfds globalTimeOffset

CLSS public com.amazonaws.SdkBaseException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public com.amazonaws.SdkClientException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr com.amazonaws.AmazonClientException

CLSS public final com.amazonaws.SdkThreadLocals
meth public static void remove()
supr java.lang.Object

CLSS public abstract interface com.amazonaws.SignableRequest<%0 extends java.lang.Object>
intf com.amazonaws.ImmutableRequest<{com.amazonaws.SignableRequest%0}>
meth public abstract void addHeader(java.lang.String,java.lang.String)
meth public abstract void addParameter(java.lang.String,java.lang.String)
meth public abstract void setContent(java.io.InputStream)

CLSS public com.amazonaws.SystemDefaultDnsResolver
cons public init()
intf com.amazonaws.DnsResolver
meth public java.net.InetAddress[] resolve(java.lang.String) throws java.net.UnknownHostException
supr java.lang.Object

CLSS public abstract interface !annotation com.amazonaws.annotation.Immutable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.amazonaws.annotation.NotThreadSafe
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.amazonaws.annotation.SdkInternalApi
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE, FIELD, CONSTRUCTOR, METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.amazonaws.annotation.SdkProtectedApi
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE, FIELD, CONSTRUCTOR, METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.amazonaws.annotation.ThreadSafe
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public com.amazonaws.auth.AWS3Signer
cons public init()
fld protected final static com.amazonaws.util.DateUtils dateUtils
 anno 0 java.lang.Deprecated()
meth protected boolean shouldUseHttpsScheme(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String getCanonicalizedHeadersForStringToSign(com.amazonaws.SignableRequest<?>)
meth protected java.util.List<java.lang.String> getHeadersForStringToSign(com.amazonaws.SignableRequest<?>)
meth protected void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds AUTHORIZATION_HEADER,HTTPS_SCHEME,HTTP_SCHEME,NONCE_HEADER,log,overriddenDate

CLSS public com.amazonaws.auth.AWS4Signer
cons public init()
cons public init(boolean)
cons public init(com.amazonaws.auth.SdkClock)
fld protected boolean doubleUrlEncode
fld protected final static com.amazonaws.log.InternalLogApi log
fld protected java.lang.String regionName
fld protected java.lang.String serviceName
fld protected java.util.Date overriddenDate
intf com.amazonaws.auth.EndpointPrefixAwareSigner
intf com.amazonaws.auth.Presigner
intf com.amazonaws.auth.RegionAwareSigner
intf com.amazonaws.auth.ServiceAwareSigner
meth protected boolean shouldExcludeHeaderFromSigning(java.lang.String)
meth protected byte[] newSigningKey(com.amazonaws.auth.AWSCredentials,java.lang.String,java.lang.String,java.lang.String)
meth protected final byte[] computeSignature(java.lang.String,byte[],com.amazonaws.auth.internal.AWS4SignerRequestParams)
meth protected java.lang.String calculateContentHash(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String calculateContentHashPresign(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String createCanonicalRequest(com.amazonaws.SignableRequest<?>,java.lang.String)
meth protected java.lang.String createStringToSign(java.lang.String,com.amazonaws.auth.internal.AWS4SignerRequestParams)
meth protected java.lang.String getCanonicalizedHeaderString(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String getSignedHeadersString(com.amazonaws.SignableRequest<?>)
meth protected void addHostHeader(com.amazonaws.SignableRequest<?>)
meth protected void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth protected void processRequestPayload(com.amazonaws.SignableRequest<?>,byte[],byte[],com.amazonaws.auth.internal.AWS4SignerRequestParams)
meth public java.lang.String getRegionName()
meth public java.lang.String getServiceName()
meth public java.util.Date getOverriddenDate()
meth public void presignRequest(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials,java.util.Date)
meth public void setEndpointPrefix(java.lang.String)
meth public void setOverrideDate(java.util.Date)
meth public void setRegionName(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds SIGNER_CACHE_MAX_SIZE,clock,endpointPrefix,listOfHeadersToIgnoreInLowerCase,signerCache

CLSS public com.amazonaws.auth.AWS4UnsignedPayloadSigner
cons public init()
cons public init(com.amazonaws.auth.SdkClock)
meth protected java.lang.String calculateContentHash(com.amazonaws.SignableRequest<?>)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AWS4Signer

CLSS public abstract interface com.amazonaws.auth.AWSCredentials
meth public abstract java.lang.String getAWSAccessKeyId()
meth public abstract java.lang.String getAWSSecretKey()

CLSS public abstract interface com.amazonaws.auth.AWSCredentialsProvider
meth public abstract com.amazonaws.auth.AWSCredentials getCredentials()
meth public abstract void refresh()

CLSS public com.amazonaws.auth.AWSCredentialsProviderChain
cons public !varargs init(com.amazonaws.auth.AWSCredentialsProvider[])
cons public init(java.util.List<? extends com.amazonaws.auth.AWSCredentialsProvider>)
intf com.amazonaws.auth.AWSCredentialsProvider
meth public boolean getReuseLastProvider()
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public void refresh()
meth public void setReuseLastProvider(boolean)
supr java.lang.Object
hfds credentialsProviders,lastUsedProvider,log,reuseLastProvider

CLSS public abstract interface com.amazonaws.auth.AWSRefreshableSessionCredentials
intf com.amazonaws.auth.AWSSessionCredentials
meth public abstract void refreshCredentials()

CLSS public abstract interface com.amazonaws.auth.AWSSessionCredentials
intf com.amazonaws.auth.AWSCredentials
meth public abstract java.lang.String getSessionToken()

CLSS public abstract interface com.amazonaws.auth.AWSSessionCredentialsProvider
intf com.amazonaws.auth.AWSCredentialsProvider
meth public abstract com.amazonaws.auth.AWSSessionCredentials getCredentials()

CLSS public com.amazonaws.auth.AWSStaticCredentialsProvider
cons public init(com.amazonaws.auth.AWSCredentials)
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public void refresh()
supr java.lang.Object
hfds credentials

CLSS public abstract com.amazonaws.auth.AbstractAWSSigner
cons public init()
fld public final static java.lang.String EMPTY_STRING_SHA256_HEX
intf com.amazonaws.auth.Signer
meth protected abstract void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth protected byte[] getBinaryRequestPayload(com.amazonaws.SignableRequest<?>)
meth protected byte[] getBinaryRequestPayloadWithoutQueryParams(com.amazonaws.SignableRequest<?>)
meth protected byte[] hash(java.io.InputStream)
meth protected byte[] sign(byte[],byte[],com.amazonaws.auth.SigningAlgorithm)
meth protected com.amazonaws.auth.AWSCredentials sanitizeCredentials(com.amazonaws.auth.AWSCredentials)
meth protected int getTimeOffset(com.amazonaws.SignableRequest<?>)
 anno 0 java.lang.Deprecated()
meth protected java.io.InputStream getBinaryRequestPayloadStream(com.amazonaws.SignableRequest<?>)
meth protected java.io.InputStream getBinaryRequestPayloadStreamWithoutQueryParams(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String getCanonicalizedEndpoint(java.net.URI)
meth protected java.lang.String getCanonicalizedQueryString(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String getCanonicalizedQueryString(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth protected java.lang.String getCanonicalizedResourcePath(java.lang.String)
meth protected java.lang.String getCanonicalizedResourcePath(java.lang.String,boolean)
meth protected java.lang.String getRequestPayload(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String getRequestPayloadWithoutQueryParams(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String newString(byte[])
meth protected java.lang.String signAndBase64Encode(byte[],java.lang.String,com.amazonaws.auth.SigningAlgorithm)
meth protected java.lang.String signAndBase64Encode(java.lang.String,java.lang.String,com.amazonaws.auth.SigningAlgorithm)
meth protected java.util.Date getSignatureDate(int)
meth public byte[] hash(byte[])
meth public byte[] hash(java.lang.String)
meth public byte[] sign(java.lang.String,byte[],com.amazonaws.auth.SigningAlgorithm)
meth public byte[] signWithMac(java.lang.String,javax.crypto.Mac)
supr java.lang.Object
hfds SHA256_MESSAGE_DIGEST

CLSS public com.amazonaws.auth.AnonymousAWSCredentials
cons public init()
intf com.amazonaws.auth.AWSCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
supr java.lang.Object

CLSS public final com.amazonaws.auth.AwsChunkedEncodingInputStream
cons public init(java.io.InputStream,byte[],java.lang.String,java.lang.String,java.lang.String,com.amazonaws.auth.AWS4Signer)
cons public init(java.io.InputStream,int,byte[],java.lang.String,java.lang.String,java.lang.String,com.amazonaws.auth.AWS4Signer)
fld protected final static java.lang.String DEFAULT_ENCODING = "UTF-8"
meth protected java.io.InputStream getWrappedInputStream()
meth public boolean markSupported()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public static long calculateStreamContentLength(long)
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkInputStream
hfds CHUNK_SIGNATURE_HEADER,CHUNK_STRING_TO_SIGN_PREFIX,CRLF,DEFAULT_BUFFER_SIZE,DEFAULT_CHUNK_SIZE,FINAL_CHUNK,SIGNATURE_LENGTH,aws4Signer,currentChunkIterator,dateTime,decodedStreamBuffer,headerSignature,hmacSha256,is,isAtStart,isTerminating,keyPath,log,maxBufferSize,priorChunkSignature,sha256

CLSS public com.amazonaws.auth.BasicAWSCredentials
cons public init(java.lang.String,java.lang.String)
intf com.amazonaws.auth.AWSCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
supr java.lang.Object
hfds accessKey,secretKey

CLSS public com.amazonaws.auth.BasicSessionCredentials
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.auth.AWSSessionCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
meth public java.lang.String getSessionToken()
supr java.lang.Object
hfds awsAccessKey,awsSecretKey,sessionToken

CLSS public abstract interface com.amazonaws.auth.CanHandleNullCredentials

CLSS public com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
cons public init()
cons public init(java.lang.String)
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public java.lang.String toString()
meth public void refresh()
supr java.lang.Object
hfds DEFAULT_PROPERTIES_FILE,credentialsFilePath

CLSS public com.amazonaws.auth.ContainerCredentialsProvider
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.internal.CredentialsEndpointProvider)
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public java.util.Date getCredentialsExpiration()
meth public void refresh()
supr java.lang.Object
hfds ALLOWED_FULL_URI_HOSTS,CONTAINER_AUTHORIZATION_TOKEN,CONTAINER_CREDENTIALS_FULL_URI,ECS_CONTAINER_CREDENTIALS_PATH,ECS_CREDENTIALS_ENDPOINT,credentialsFetcher
hcls ECSCredentialsEndpointProvider,FullUriCredentialsEndpointProvider

CLSS public com.amazonaws.auth.DefaultAWSCredentialsProviderChain
cons public init()
meth public static com.amazonaws.auth.DefaultAWSCredentialsProviderChain getInstance()
supr com.amazonaws.auth.AWSCredentialsProviderChain
hfds INSTANCE

CLSS public com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper
cons public init()
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public void refresh()
supr java.lang.Object
hfds LOG,provider

CLSS public abstract interface com.amazonaws.auth.EndpointPrefixAwareSigner
intf com.amazonaws.auth.Signer
meth public abstract void setEndpointPrefix(java.lang.String)

CLSS public com.amazonaws.auth.EnvironmentVariableCredentialsProvider
cons public init()
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public java.lang.String toString()
meth public void refresh()
supr java.lang.Object

CLSS public com.amazonaws.auth.InstanceProfileCredentialsProvider
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(boolean)
intf com.amazonaws.auth.AWSCredentialsProvider
intf java.io.Closeable
meth protected void finalize() throws java.lang.Throwable
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public static com.amazonaws.auth.InstanceProfileCredentialsProvider createAsyncRefreshingProvider(boolean)
meth public static com.amazonaws.auth.InstanceProfileCredentialsProvider getInstance()
meth public void close() throws java.io.IOException
meth public void refresh()
supr java.lang.Object
hfds ASYNC_REFRESH_INTERVAL_TIME_MINUTES,INSTANCE,LOG,credentialsFetcher,executor,shouldRefresh
hcls InstanceMetadataCredentialsEndpointProvider

CLSS public com.amazonaws.auth.NoOpSigner
cons public init()
intf com.amazonaws.auth.Signer
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.auth.Presigner
meth public abstract void presignRequest(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials,java.util.Date)

CLSS public final com.amazonaws.auth.ProcessCredentialsProvider
innr public static Builder
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public org.joda.time.DateTime getCredentialExpirationTime()
meth public static com.amazonaws.auth.ProcessCredentialsProvider$Builder builder()
meth public void refresh()
supr java.lang.Object
hfds command,credentialExpirationTime,credentialLock,credentials,expirationBufferUnit,expirationBufferValue,processOutputLimit

CLSS public static com.amazonaws.auth.ProcessCredentialsProvider$Builder
 outer com.amazonaws.auth.ProcessCredentialsProvider
meth public com.amazonaws.auth.ProcessCredentialsProvider build()
meth public com.amazonaws.auth.ProcessCredentialsProvider$Builder withCommand(java.lang.String)
meth public com.amazonaws.auth.ProcessCredentialsProvider$Builder withCredentialExpirationBuffer(int,java.util.concurrent.TimeUnit)
meth public com.amazonaws.auth.ProcessCredentialsProvider$Builder withProcessOutputLimit(long)
meth public void setCredentialExpirationBuffer(int,java.util.concurrent.TimeUnit)
supr java.lang.Object
hfds command,expirationBufferUnit,expirationBufferValue,processOutputLimit

CLSS public com.amazonaws.auth.PropertiesCredentials
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
intf com.amazonaws.auth.AWSCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
supr java.lang.Object
hfds accessKey,secretAccessKey

CLSS public com.amazonaws.auth.PropertiesFileCredentialsProvider
cons public init(java.lang.String)
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public java.lang.String toString()
meth public void refresh()
supr java.lang.Object
hfds credentialsFilePath

CLSS public com.amazonaws.auth.QueryStringSigner
cons public init()
intf com.amazonaws.auth.Signer
meth protected void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.SignatureVersion,com.amazonaws.auth.SigningAlgorithm,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds overriddenDate

CLSS public abstract interface com.amazonaws.auth.RegionAwareSigner
intf com.amazonaws.auth.Signer
meth public abstract void setRegionName(java.lang.String)

CLSS public abstract interface com.amazonaws.auth.RequestSigner
meth public abstract void sign(com.amazonaws.SignableRequest<?>)

CLSS public abstract interface com.amazonaws.auth.SdkClock
fld public final static com.amazonaws.auth.SdkClock STANDARD
innr public final static Instance
innr public final static MockClock
meth public abstract long currentTimeMillis()

CLSS public final static com.amazonaws.auth.SdkClock$Instance
 outer com.amazonaws.auth.SdkClock
cons public init()
meth public static com.amazonaws.auth.SdkClock get()
meth public static void reset()
meth public static void set(com.amazonaws.auth.SdkClock)
supr java.lang.Object
hfds clock

CLSS public final static com.amazonaws.auth.SdkClock$MockClock
 outer com.amazonaws.auth.SdkClock
cons public init(java.util.Date)
cons public init(long)
intf com.amazonaws.auth.SdkClock
meth public long currentTimeMillis()
supr java.lang.Object
hfds mockedTime

CLSS public abstract interface com.amazonaws.auth.ServiceAwareSigner
intf com.amazonaws.auth.Signer
meth public abstract void setServiceName(java.lang.String)

CLSS public final !enum com.amazonaws.auth.SignatureVersion
fld public final static com.amazonaws.auth.SignatureVersion V1
fld public final static com.amazonaws.auth.SignatureVersion V2
meth public java.lang.String toString()
meth public static com.amazonaws.auth.SignatureVersion valueOf(java.lang.String)
meth public static com.amazonaws.auth.SignatureVersion[] values()
supr java.lang.Enum<com.amazonaws.auth.SignatureVersion>
hfds value

CLSS public abstract interface com.amazonaws.auth.Signer
meth public abstract void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)

CLSS public final com.amazonaws.auth.SignerAsRequestSigner
cons public init(com.amazonaws.auth.Signer,com.amazonaws.auth.AWSCredentialsProvider)
intf com.amazonaws.auth.RequestSigner
meth public void sign(com.amazonaws.SignableRequest<?>)
supr java.lang.Object
hfds credentialsProvider,signer

CLSS public final com.amazonaws.auth.SignerFactory
fld public final static java.lang.String NO_OP_SIGNER = "NoOpSignerType"
fld public final static java.lang.String QUERY_STRING_SIGNER = "QueryStringSignerType"
fld public final static java.lang.String VERSION_FOUR_SIGNER = "AWS4SignerType"
fld public final static java.lang.String VERSION_FOUR_UNSIGNED_PAYLOAD_SIGNER = "AWS4UnsignedPayloadSignerType"
fld public final static java.lang.String VERSION_THREE_SIGNER = "AWS3SignerType"
meth public static com.amazonaws.auth.Signer createSigner(java.lang.String,com.amazonaws.auth.SignerParams)
meth public static com.amazonaws.auth.Signer getSigner(java.lang.String,java.lang.String)
meth public static com.amazonaws.auth.Signer getSignerByTypeAndService(java.lang.String,java.lang.String)
meth public static void registerSigner(java.lang.String,java.lang.Class<? extends com.amazonaws.auth.Signer>)
supr java.lang.Object
hfds S3_V4_SIGNER,SIGNERS

CLSS public com.amazonaws.auth.SignerParams
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getRegionName()
meth public java.lang.String getServiceName()
supr java.lang.Object
hfds regionName,serviceName

CLSS public abstract interface com.amazonaws.auth.SignerTypeAware
meth public abstract java.lang.String getSignerType()

CLSS public final !enum com.amazonaws.auth.SigningAlgorithm
fld public final static com.amazonaws.auth.SigningAlgorithm HmacSHA1
fld public final static com.amazonaws.auth.SigningAlgorithm HmacSHA256
meth public javax.crypto.Mac getMac()
meth public static com.amazonaws.auth.SigningAlgorithm valueOf(java.lang.String)
meth public static com.amazonaws.auth.SigningAlgorithm[] values()
supr java.lang.Enum<com.amazonaws.auth.SigningAlgorithm>
hfds macReference

CLSS public com.amazonaws.auth.StaticSignerProvider
cons public init(com.amazonaws.auth.Signer)
meth public com.amazonaws.auth.Signer getSigner(com.amazonaws.internal.auth.SignerProviderContext)
supr com.amazonaws.internal.auth.SignerProvider
hfds signer

CLSS public com.amazonaws.auth.SystemPropertiesCredentialsProvider
cons public init()
intf com.amazonaws.auth.AWSCredentialsProvider
meth public com.amazonaws.auth.AWSCredentials getCredentials()
meth public java.lang.String toString()
meth public void refresh()
supr java.lang.Object

CLSS public abstract com.amazonaws.client.AwsAsyncClientParams
cons public init()
meth public abstract java.util.concurrent.ExecutorService getExecutor()
supr com.amazonaws.client.AwsSyncClientParams

CLSS public abstract com.amazonaws.client.AwsSyncClientParams
cons public init()
meth public abstract com.amazonaws.ClientConfiguration getClientConfiguration()
meth public abstract com.amazonaws.auth.AWSCredentialsProvider getCredentialsProvider()
meth public abstract com.amazonaws.metrics.RequestMetricCollector getRequestMetricCollector()
meth public abstract com.amazonaws.monitoring.CsmConfigurationProvider getClientSideMonitoringConfigurationProvider()
meth public abstract com.amazonaws.monitoring.MonitoringListener getMonitoringListener()
meth public abstract java.util.List<com.amazonaws.handlers.RequestHandler2> getRequestHandlers()
meth public com.amazonaws.client.builder.AdvancedConfig getAdvancedConfig()
meth public com.amazonaws.internal.auth.SignerProvider getSignerProvider()
meth public com.amazonaws.retry.v2.RetryPolicy getRetryPolicy()
meth public java.net.URI getEndpoint()
supr java.lang.Object

CLSS public com.amazonaws.client.ClientExecutionParams<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init()
meth public com.amazonaws.RequestConfig getRequestConfig()
meth public com.amazonaws.client.ClientExecutionParams<{com.amazonaws.client.ClientExecutionParams%0},{com.amazonaws.client.ClientExecutionParams%1}> withErrorResponseHandler(com.amazonaws.http.HttpResponseHandler<? extends com.amazonaws.SdkBaseException>)
meth public com.amazonaws.client.ClientExecutionParams<{com.amazonaws.client.ClientExecutionParams%0},{com.amazonaws.client.ClientExecutionParams%1}> withInput({com.amazonaws.client.ClientExecutionParams%0})
meth public com.amazonaws.client.ClientExecutionParams<{com.amazonaws.client.ClientExecutionParams%0},{com.amazonaws.client.ClientExecutionParams%1}> withMarshaller(com.amazonaws.transform.Marshaller<com.amazonaws.Request<{com.amazonaws.client.ClientExecutionParams%0}>,{com.amazonaws.client.ClientExecutionParams%0}>)
meth public com.amazonaws.client.ClientExecutionParams<{com.amazonaws.client.ClientExecutionParams%0},{com.amazonaws.client.ClientExecutionParams%1}> withRequestConfig(com.amazonaws.RequestConfig)
meth public com.amazonaws.client.ClientExecutionParams<{com.amazonaws.client.ClientExecutionParams%0},{com.amazonaws.client.ClientExecutionParams%1}> withResponseHandler(com.amazonaws.http.HttpResponseHandler<{com.amazonaws.client.ClientExecutionParams%1}>)
meth public com.amazonaws.http.HttpResponseHandler<? extends com.amazonaws.SdkBaseException> getErrorResponseHandler()
meth public com.amazonaws.http.HttpResponseHandler<{com.amazonaws.client.ClientExecutionParams%1}> getResponseHandler()
meth public com.amazonaws.transform.Marshaller<com.amazonaws.Request<{com.amazonaws.client.ClientExecutionParams%0}>,{com.amazonaws.client.ClientExecutionParams%0}> getMarshaller()
meth public {com.amazonaws.client.ClientExecutionParams%0} getInput()
supr java.lang.Object
hfds errorResponseHandler,input,marshaller,requestConfig,responseHandler

CLSS public abstract com.amazonaws.client.ClientHandler
cons public init()
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} execute(com.amazonaws.client.ClientExecutionParams<{%%0},{%%1}>)
meth public abstract void shutdown()
supr java.lang.Object

CLSS public com.amazonaws.client.ClientHandlerImpl
 anno 0 com.amazonaws.annotation.Immutable()
 anno 0 com.amazonaws.annotation.ThreadSafe()
cons public init(com.amazonaws.client.ClientHandlerParams)
meth protected final <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} beforeMarshalling({%%0})
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} execute(com.amazonaws.client.ClientExecutionParams<{%%0},{%%1}>)
meth public void shutdown()
supr com.amazonaws.client.ClientHandler
hfds awsCredentialsProvider,client,clientLevelMetricCollector,endpoint,requestHandler2s,signerProvider

CLSS public com.amazonaws.client.ClientHandlerParams
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init()
meth public boolean isDisableStrictHostnameVerification()
meth public com.amazonaws.client.AwsSyncClientParams getClientParams()
meth public com.amazonaws.client.ClientHandlerParams withClientParams(com.amazonaws.client.AwsSyncClientParams)
meth public com.amazonaws.client.ClientHandlerParams withDisableStrictHostnameVerification(boolean)
supr java.lang.Object
hfds clientParams,disableStrictHostnameVerification

CLSS public final com.amazonaws.client.builder.AdvancedConfig
 anno 0 com.amazonaws.annotation.Immutable()
fld public final static com.amazonaws.client.builder.AdvancedConfig EMPTY
innr public static Builder
innr public static Key
meth public <%0 extends java.lang.Object> {%%0} get(com.amazonaws.client.builder.AdvancedConfig$Key<{%%0}>)
meth public static com.amazonaws.client.builder.AdvancedConfig$Builder builder()
supr java.lang.Object
hfds config

CLSS public static com.amazonaws.client.builder.AdvancedConfig$Builder
 outer com.amazonaws.client.builder.AdvancedConfig
meth public <%0 extends java.lang.Object> com.amazonaws.client.builder.AdvancedConfig$Builder put(com.amazonaws.client.builder.AdvancedConfig$Key<{%%0}>,{%%0})
meth public <%0 extends java.lang.Object> {%%0} get(com.amazonaws.client.builder.AdvancedConfig$Key<{%%0}>)
meth public com.amazonaws.client.builder.AdvancedConfig build()
supr java.lang.Object
hfds config

CLSS public static com.amazonaws.client.builder.AdvancedConfig$Key<%0 extends java.lang.Object>
 outer com.amazonaws.client.builder.AdvancedConfig
cons public init()
supr java.lang.Object

CLSS public abstract com.amazonaws.client.builder.AwsAsyncClientBuilder<%0 extends com.amazonaws.client.builder.AwsAsyncClientBuilder, %1 extends java.lang.Object>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons protected init(com.amazonaws.ClientConfigurationFactory)
cons protected init(com.amazonaws.ClientConfigurationFactory,com.amazonaws.regions.AwsRegionProvider)
innr protected AsyncBuilderParams
meth protected abstract {com.amazonaws.client.builder.AwsAsyncClientBuilder%1} build(com.amazonaws.client.AwsAsyncClientParams)
meth protected final com.amazonaws.client.AwsAsyncClientParams getAsyncClientParams()
meth public final com.amazonaws.client.builder.ExecutorFactory getExecutorFactory()
meth public final void setExecutorFactory(com.amazonaws.client.builder.ExecutorFactory)
meth public final {com.amazonaws.client.builder.AwsAsyncClientBuilder%0} withExecutorFactory(com.amazonaws.client.builder.ExecutorFactory)
meth public final {com.amazonaws.client.builder.AwsAsyncClientBuilder%1} build()
supr com.amazonaws.client.builder.AwsClientBuilder<{com.amazonaws.client.builder.AwsAsyncClientBuilder%0},{com.amazonaws.client.builder.AwsAsyncClientBuilder%1}>
hfds executorFactory

CLSS protected com.amazonaws.client.builder.AwsAsyncClientBuilder$AsyncBuilderParams
 outer com.amazonaws.client.builder.AwsAsyncClientBuilder
cons protected init(com.amazonaws.client.builder.AwsAsyncClientBuilder,com.amazonaws.client.builder.ExecutorFactory)
meth public java.util.concurrent.ExecutorService getExecutor()
supr com.amazonaws.client.builder.AwsClientBuilder$SyncBuilderParams
hfds _executorService

CLSS public abstract com.amazonaws.client.builder.AwsClientBuilder<%0 extends com.amazonaws.client.builder.AwsClientBuilder, %1 extends java.lang.Object>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons protected init(com.amazonaws.ClientConfigurationFactory)
cons protected init(com.amazonaws.ClientConfigurationFactory,com.amazonaws.regions.AwsRegionProvider)
innr protected SyncBuilderParams
innr public final static EndpointConfiguration
meth protected final <%0 extends java.lang.Object> void putAdvancedConfig(com.amazonaws.client.builder.AdvancedConfig$Key<{%%0}>,{%%0})
meth protected final <%0 extends java.lang.Object> {%%0} getAdvancedConfig(com.amazonaws.client.builder.AdvancedConfig$Key<{%%0}>)
meth protected final com.amazonaws.client.AwsSyncClientParams getSyncClientParams()
meth protected final com.amazonaws.client.builder.AdvancedConfig getAdvancedConfig()
meth protected final {com.amazonaws.client.builder.AwsClientBuilder%0} getSubclass()
meth public !varargs final void setRequestHandlers(com.amazonaws.handlers.RequestHandler2[])
meth public !varargs final {com.amazonaws.client.builder.AwsClientBuilder%0} withRequestHandlers(com.amazonaws.handlers.RequestHandler2[])
meth public abstract {com.amazonaws.client.builder.AwsClientBuilder%1} build()
meth public com.amazonaws.monitoring.CsmConfigurationProvider getClientSideMonitoringConfigurationProvider()
meth public final com.amazonaws.ClientConfiguration getClientConfiguration()
meth public final com.amazonaws.auth.AWSCredentialsProvider getCredentials()
meth public final com.amazonaws.client.builder.AwsClientBuilder$EndpointConfiguration getEndpoint()
meth public final com.amazonaws.metrics.RequestMetricCollector getMetricsCollector()
meth public final com.amazonaws.monitoring.MonitoringListener getMonitoringListener()
meth public final java.lang.String getRegion()
meth public final java.util.List<com.amazonaws.handlers.RequestHandler2> getRequestHandlers()
meth public final void setClientConfiguration(com.amazonaws.ClientConfiguration)
meth public final void setCredentials(com.amazonaws.auth.AWSCredentialsProvider)
meth public final void setEndpointConfiguration(com.amazonaws.client.builder.AwsClientBuilder$EndpointConfiguration)
meth public final void setMetricsCollector(com.amazonaws.metrics.RequestMetricCollector)
meth public final void setMonitoringListener(com.amazonaws.monitoring.MonitoringListener)
meth public final void setRegion(java.lang.String)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withClientConfiguration(com.amazonaws.ClientConfiguration)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withCredentials(com.amazonaws.auth.AWSCredentialsProvider)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withEndpointConfiguration(com.amazonaws.client.builder.AwsClientBuilder$EndpointConfiguration)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withMetricsCollector(com.amazonaws.metrics.RequestMetricCollector)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withMonitoringListener(com.amazonaws.monitoring.MonitoringListener)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withRegion(com.amazonaws.regions.Regions)
meth public final {com.amazonaws.client.builder.AwsClientBuilder%0} withRegion(java.lang.String)
meth public void setClientSideMonitoringConfigurationProvider(com.amazonaws.monitoring.CsmConfigurationProvider)
meth public {com.amazonaws.client.builder.AwsClientBuilder%0} withClientSideMonitoringConfigurationProvider(com.amazonaws.monitoring.CsmConfigurationProvider)
supr java.lang.Object
hfds DEFAULT_REGION_PROVIDER,advancedConfig,clientConfig,clientConfigFactory,credentials,csmConfig,endpointConfiguration,metricsCollector,monitoringListener,region,regionProvider,requestHandlers

CLSS public final static com.amazonaws.client.builder.AwsClientBuilder$EndpointConfiguration
 outer com.amazonaws.client.builder.AwsClientBuilder
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getServiceEndpoint()
meth public java.lang.String getSigningRegion()
supr java.lang.Object
hfds serviceEndpoint,signingRegion

CLSS protected com.amazonaws.client.builder.AwsClientBuilder$SyncBuilderParams
 outer com.amazonaws.client.builder.AwsClientBuilder
cons protected init(com.amazonaws.client.builder.AwsClientBuilder)
meth public com.amazonaws.ClientConfiguration getClientConfiguration()
meth public com.amazonaws.auth.AWSCredentialsProvider getCredentialsProvider()
meth public com.amazonaws.client.builder.AdvancedConfig getAdvancedConfig()
meth public com.amazonaws.metrics.RequestMetricCollector getRequestMetricCollector()
meth public com.amazonaws.monitoring.CsmConfigurationProvider getClientSideMonitoringConfigurationProvider()
meth public com.amazonaws.monitoring.MonitoringListener getMonitoringListener()
meth public java.util.List<com.amazonaws.handlers.RequestHandler2> getRequestHandlers()
meth public java.util.concurrent.ExecutorService getExecutor()
supr com.amazonaws.client.AwsAsyncClientParams
hfds _advancedConfig,_clientConfig,_credentials,_csmConfig,_metricsCollector,_monitoringListener,_requestHandlers

CLSS public abstract com.amazonaws.client.builder.AwsSyncClientBuilder<%0 extends com.amazonaws.client.builder.AwsSyncClientBuilder, %1 extends java.lang.Object>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons protected init(com.amazonaws.ClientConfigurationFactory)
cons protected init(com.amazonaws.ClientConfigurationFactory,com.amazonaws.regions.AwsRegionProvider)
meth protected abstract {com.amazonaws.client.builder.AwsSyncClientBuilder%1} build(com.amazonaws.client.AwsSyncClientParams)
meth public final {com.amazonaws.client.builder.AwsSyncClientBuilder%1} build()
supr com.amazonaws.client.builder.AwsClientBuilder<{com.amazonaws.client.builder.AwsSyncClientBuilder%0},{com.amazonaws.client.builder.AwsSyncClientBuilder%1}>

CLSS public abstract interface com.amazonaws.client.builder.ExecutorFactory
meth public abstract java.util.concurrent.ExecutorService newExecutor()

CLSS public abstract interface com.amazonaws.event.DeliveryMode
innr public static Check
meth public abstract boolean isSyncCallSafe()

CLSS public com.amazonaws.event.ProgressEvent
 anno 0 com.amazonaws.annotation.Immutable()
cons public init(com.amazonaws.event.ProgressEventType)
cons public init(com.amazonaws.event.ProgressEventType,long)
cons public init(long)
 anno 0 java.lang.Deprecated()
fld public final static int CANCELED_EVENT_CODE = 16
 anno 0 java.lang.Deprecated()
fld public final static int COMPLETED_EVENT_CODE = 4
 anno 0 java.lang.Deprecated()
fld public final static int FAILED_EVENT_CODE = 8
 anno 0 java.lang.Deprecated()
fld public final static int PART_COMPLETED_EVENT_CODE = 2048
 anno 0 java.lang.Deprecated()
fld public final static int PART_FAILED_EVENT_CODE = 4096
 anno 0 java.lang.Deprecated()
fld public final static int PART_STARTED_EVENT_CODE = 1024
 anno 0 java.lang.Deprecated()
fld public final static int PREPARING_EVENT_CODE = 1
 anno 0 java.lang.Deprecated()
fld public final static int RESET_EVENT_CODE = 32
 anno 0 java.lang.Deprecated()
fld public final static int STARTED_EVENT_CODE = 2
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.event.ProgressEventType getEventType()
meth public int getEventCode()
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public long getBytes()
meth public long getBytesTransferred()
supr java.lang.Object
hfds bytes,eventType,legacyEventCodes

CLSS public abstract interface com.amazonaws.event.ProgressListener
fld public final static com.amazonaws.event.ProgressListener NOOP
innr public static ExceptionReporter
innr public static NoOpProgressListener
meth public abstract void progressChanged(com.amazonaws.event.ProgressEvent)

CLSS public abstract interface com.amazonaws.http.HttpResponseHandler<%0 extends java.lang.Object>
fld public final static java.lang.String X_AMZN_EXTENDED_REQUEST_ID_HEADER = "x-amz-id-2"
fld public final static java.lang.String X_AMZN_REQUEST_ID_HEADER = "x-amzn-RequestId"
meth public abstract boolean needsConnectionLeftOpen()
meth public abstract {com.amazonaws.http.HttpResponseHandler%0} handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception

CLSS public abstract interface com.amazonaws.internal.MetricAware
meth public abstract boolean isMetricActivated()

CLSS public abstract interface com.amazonaws.internal.Releasable
meth public abstract void release()

CLSS public com.amazonaws.internal.SdkDigestInputStream
cons public init(java.io.InputStream,java.security.MessageDigest)
intf com.amazonaws.internal.MetricAware
intf com.amazonaws.internal.Releasable
meth public final boolean isMetricActivated()
meth public final long skip(long) throws java.io.IOException
meth public final void release()
supr java.security.DigestInputStream
hfds SKIP_BUF_SIZE

CLSS public com.amazonaws.internal.SdkFilterInputStream
cons protected init(java.io.InputStream)
intf com.amazonaws.internal.MetricAware
intf com.amazonaws.internal.Releasable
meth protected boolean isAborted()
meth protected final void abortIfNeeded()
meth public boolean isMetricActivated()
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.io.InputStream getDelegateStream()
meth public long skip(long) throws java.io.IOException
meth public void abort()
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void release()
meth public void reset() throws java.io.IOException
supr java.io.FilterInputStream
hfds aborted

CLSS public abstract com.amazonaws.internal.SdkInputStream
cons public init()
intf com.amazonaws.internal.MetricAware
intf com.amazonaws.internal.Releasable
meth protected abstract java.io.InputStream getWrappedInputStream()
meth protected final void abortIfNeeded()
meth protected void abort() throws java.io.IOException
meth public final boolean isMetricActivated()
meth public void release()
supr java.io.InputStream

CLSS public abstract com.amazonaws.internal.SdkPredicate<%0 extends java.lang.Object>
cons public init()
meth public abstract boolean test({com.amazonaws.internal.SdkPredicate%0})
supr java.lang.Object

CLSS public abstract com.amazonaws.internal.ServiceEndpointBuilder
cons public init()
meth public abstract com.amazonaws.internal.ServiceEndpointBuilder withRegion(com.amazonaws.regions.Region)
meth public abstract com.amazonaws.regions.Region getRegion()
meth public abstract java.net.URI getServiceEndpoint()
supr java.lang.Object

CLSS public abstract com.amazonaws.internal.auth.SignerProvider
cons public init()
meth public abstract com.amazonaws.auth.Signer getSigner(com.amazonaws.internal.auth.SignerProviderContext)
supr java.lang.Object

CLSS public final com.amazonaws.retry.RetryPolicy
 anno 0 com.amazonaws.annotation.Immutable()
cons public init(com.amazonaws.retry.RetryPolicy$RetryCondition,com.amazonaws.retry.RetryPolicy$BackoffStrategy,int,boolean)
innr public abstract interface static BackoffStrategy
innr public abstract interface static RetryCondition
meth public boolean isMaxErrorRetryInClientConfigHonored()
meth public com.amazonaws.retry.RetryPolicy$BackoffStrategy getBackoffStrategy()
meth public com.amazonaws.retry.RetryPolicy$RetryCondition getRetryCondition()
meth public int getMaxErrorRetry()
supr java.lang.Object
hfds backoffStrategy,honorMaxErrorRetryInClientConfig,maxErrorRetry,retryCondition

CLSS public abstract interface static com.amazonaws.retry.RetryPolicy$RetryCondition
 outer com.amazonaws.retry.RetryPolicy
fld public final static com.amazonaws.retry.RetryPolicy$RetryCondition NO_RETRY_CONDITION
meth public abstract boolean shouldRetry(com.amazonaws.AmazonWebServiceRequest,com.amazonaws.AmazonClientException,int)

CLSS public abstract interface com.amazonaws.retry.internal.AuthErrorRetryStrategy
meth public abstract com.amazonaws.retry.internal.AuthRetryParameters shouldRetryWithAuthParam(com.amazonaws.Request<?>,com.amazonaws.http.HttpResponse,com.amazonaws.AmazonServiceException)

CLSS public abstract interface com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
fld public final static java.lang.String ENDPOINT_PREFIX = "elasticbeanstalk"
meth public abstract com.amazonaws.ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult applyEnvironmentManagedAction(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult checkDNSAvailability(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult composeEnvironments(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult createApplication(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult createApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult createConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult createEnvironment(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult createPlatformVersion(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult deleteApplication(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult deleteApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult deleteConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult deleteEnvironmentConfiguration(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult deletePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult describeAccountAttributes(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult describeConfigurationOptions(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult describeConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult describeEnvironmentHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult describeEnvironmentManagedActionHistory(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult describeEnvironmentManagedActions(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult describeEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult describeInstancesHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult describePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult listPlatformVersions(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult listTagsForResource(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult rebuildEnvironment(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult requestEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult restartAppServer(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult retrieveEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult terminateEnvironment(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult updateApplicationResourceLifecycle(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult updateApplication(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult updateApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult updateConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult updateEnvironment(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult updateTagsForResource(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult validateConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public abstract void setEndpoint(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract void setRegion(com.amazonaws.regions.Region)
 anno 0 java.lang.Deprecated()
meth public abstract void shutdown()

CLSS public abstract interface com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync()
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult>)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult>)

CLSS public com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClient
 anno 0 com.amazonaws.annotation.ThreadSafe()
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration,java.util.concurrent.ExecutorService)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,java.util.concurrent.ExecutorService)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration,java.util.concurrent.ExecutorService)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,java.util.concurrent.ExecutorService)
 anno 0 java.lang.Deprecated()
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync
meth public java.util.concurrent.ExecutorService getExecutorService()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult>)
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClientBuilder asyncBuilder()
meth public void shutdown()
supr com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
hfds DEFAULT_THREAD_POOL_SIZE,executorService

CLSS public final com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClientBuilder
 anno 0 com.amazonaws.annotation.NotThreadSafe()
meth protected com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync build(com.amazonaws.client.AwsAsyncClientParams)
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync defaultClient()
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClientBuilder standard()
supr com.amazonaws.client.builder.AwsAsyncClientBuilder<com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClientBuilder,com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync>
hfds CLIENT_CONFIG_FACTORY

CLSS public com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
 anno 0 com.amazonaws.annotation.ThreadSafe()
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.metrics.RequestMetricCollector)
 anno 0 java.lang.Deprecated()
fld protected final java.util.List<com.amazonaws.transform.Unmarshaller<com.amazonaws.AmazonServiceException,org.w3c.dom.Node>> exceptionUnmarshallers
fld protected final static com.amazonaws.ClientConfigurationFactory configFactory
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public com.amazonaws.ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate()
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult applyEnvironmentManagedAction(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult checkDNSAvailability(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult composeEnvironments(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult createApplication(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult createApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult createConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult createEnvironment(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult createPlatformVersion(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult deleteApplication(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult deleteApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult deleteConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult deleteEnvironmentConfiguration(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult deletePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult describeAccountAttributes(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult describeConfigurationOptions(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult describeConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult describeEnvironmentHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult describeEnvironmentManagedActionHistory(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult describeEnvironmentManagedActions(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult describeEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult describeInstancesHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult describePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks()
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult listPlatformVersions(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult listTagsForResource(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult rebuildEnvironment(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult requestEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult restartAppServer(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult retrieveEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs()
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult terminateEnvironment(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult updateApplicationResourceLifecycle(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult updateApplication(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult updateApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult updateConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult updateEnvironment(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult updateTagsForResource(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult validateConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder builder()
supr com.amazonaws.AmazonWebServiceClient
hfds DEFAULT_SIGNING_NAME,advancedConfig,awsCredentialsProvider,log

CLSS public final com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder
 anno 0 com.amazonaws.annotation.NotThreadSafe()
meth protected com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk build(com.amazonaws.client.AwsSyncClientParams)
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk defaultClient()
meth public static com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder standard()
supr com.amazonaws.client.builder.AwsSyncClientBuilder<com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder,com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk>
hfds CLIENT_CONFIG_FACTORY

CLSS public com.amazonaws.services.elasticbeanstalk.AbstractAWSElasticBeanstalk
cons protected init()
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public com.amazonaws.ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate()
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult abortEnvironmentUpdate(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult applyEnvironmentManagedAction(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult checkDNSAvailability(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult composeEnvironments(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult createApplication(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult createApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult createConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult createEnvironment(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult createPlatformVersion(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult deleteApplication(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult deleteApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult deleteConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult deleteEnvironmentConfiguration(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult deletePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult describeAccountAttributes(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult describeConfigurationOptions(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult describeConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult describeEnvironmentHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult describeEnvironmentManagedActionHistory(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult describeEnvironmentManagedActions(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult describeEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult describeInstancesHealth(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult describePlatformVersion(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks()
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult listPlatformVersions(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult listTagsForResource(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult rebuildEnvironment(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult requestEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult restartAppServer(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult retrieveEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs()
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult swapEnvironmentCNAMEs(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult terminateEnvironment(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult updateApplicationResourceLifecycle(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult updateApplication(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult updateApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult updateConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult updateEnvironment(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult updateTagsForResource(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult validateConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public void setEndpoint(java.lang.String)
meth public void setRegion(com.amazonaws.regions.Region)
meth public void shutdown()
supr java.lang.Object

CLSS public com.amazonaws.services.elasticbeanstalk.AbstractAWSElasticBeanstalkAsync
cons protected init()
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult> abortEnvironmentUpdateAsync(com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest,com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult> applyEnvironmentManagedActionAsync(com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest,com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest,com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult> composeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult> createPlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest,com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest,com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult> deletePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult> describeAccountAttributesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult> describeEnvironmentHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult> describeEnvironmentManagedActionHistoryAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult> describeEnvironmentManagedActionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult> describeInstancesHealthAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest,com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult> describePlatformVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest,com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest,com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult> listPlatformVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest,com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult> listTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest,com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest,com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult> swapEnvironmentCNAMEsAsync(com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest,com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult> updateApplicationResourceLifecycleAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult> updateTagsForResourceAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest,com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult>)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.handlers.AsyncHandler<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest,com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult>)
supr com.amazonaws.services.elasticbeanstalk.AbstractAWSElasticBeanstalk

CLSS public com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.AbortEnvironmentUpdateResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus Completed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus Failed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus Unknown
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ActionStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionStatus Pending
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionStatus Running
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionStatus Scheduled
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionStatus Unknown
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ActionStatus>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ActionType
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionType InstanceRefresh
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionType PlatformUpdate
fld public final static com.amazonaws.services.elasticbeanstalk.model.ActionType Unknown
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ActionType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ActionType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withConfigurationTemplates(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withVersions(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withApplicationArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withConfigurationTemplates(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withVersions(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig getResourceLifecycleConfig()
meth public int hashCode()
meth public java.lang.String getApplicationArn()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<java.lang.String> getConfigurationTemplates()
meth public java.util.List<java.lang.String> getVersions()
meth public void setApplicationArn(java.lang.String)
meth public void setApplicationName(java.lang.String)
meth public void setConfigurationTemplates(java.util.Collection<java.lang.String>)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public void setVersions(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds applicationArn,applicationName,configurationTemplates,dateCreated,dateUpdated,description,resourceLifecycleConfig,versions

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics withDuration(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics withLatency(com.amazonaws.services.elasticbeanstalk.model.Latency)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics withRequestCount(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics withStatusCodes(com.amazonaws.services.elasticbeanstalk.model.StatusCodes)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency getLatency()
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes getStatusCodes()
meth public int hashCode()
meth public java.lang.Integer getDuration()
meth public java.lang.Integer getRequestCount()
meth public java.lang.String toString()
meth public void setDuration(java.lang.Integer)
meth public void setLatency(com.amazonaws.services.elasticbeanstalk.model.Latency)
meth public void setRequestCount(java.lang.Integer)
meth public void setStatusCodes(com.amazonaws.services.elasticbeanstalk.model.StatusCodes)
supr java.lang.Object
hfds duration,latency,requestCount,statusCodes

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig withServiceRole(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig withVersionLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig getVersionLifecycleConfig()
meth public int hashCode()
meth public java.lang.String getServiceRole()
meth public java.lang.String toString()
meth public void setServiceRole(java.lang.String)
meth public void setVersionLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig)
supr java.lang.Object
hfds serviceRole,versionLifecycleConfig

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withApplicationVersionArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withBuildArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withSourceBuildInformation(com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withStatus(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location getSourceBundle()
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation getSourceBuildInformation()
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getApplicationVersionArn()
meth public java.lang.String getBuildArn()
meth public java.lang.String getDescription()
meth public java.lang.String getStatus()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setApplicationVersionArn(java.lang.String)
meth public void setBuildArn(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setSourceBuildInformation(com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation)
meth public void setSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus)
meth public void setStatus(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,applicationVersionArn,buildArn,dateCreated,dateUpdated,description,sourceBuildInformation,sourceBundle,status,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig withMaxAgeRule(com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionLifecycleConfig withMaxCountRule(com.amazonaws.services.elasticbeanstalk.model.MaxCountRule)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule getMaxAgeRule()
meth public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule getMaxCountRule()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setMaxAgeRule(com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule)
meth public void setMaxCountRule(com.amazonaws.services.elasticbeanstalk.model.MaxCountRule)
supr java.lang.Object
hfds maxAgeRule,maxCountRule

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus Building
fld public final static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus Failed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus Processed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus Processing
fld public final static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus Unprocessed
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionStatus>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest withActionId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getActionId()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setActionId(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds actionId,environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult withActionDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult withActionId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult withActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult withActionType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplyEnvironmentManagedActionResult withStatus(java.lang.String)
meth public int hashCode()
meth public java.lang.String getActionDescription()
meth public java.lang.String getActionId()
meth public java.lang.String getActionType()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public void setActionDescription(java.lang.String)
meth public void setActionId(java.lang.String)
meth public void setActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public void setActionType(java.lang.String)
meth public void setStatus(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds actionDescription,actionId,actionType,status

CLSS public com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup clone()
meth public com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup withName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration clone()
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withArtifactName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withCodeBuildServiceRole(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withComputeType(com.amazonaws.services.elasticbeanstalk.model.ComputeType)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withComputeType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withImage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration withTimeoutInMinutes(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Integer getTimeoutInMinutes()
meth public java.lang.String getArtifactName()
meth public java.lang.String getCodeBuildServiceRole()
meth public java.lang.String getComputeType()
meth public java.lang.String getImage()
meth public java.lang.String toString()
meth public void setArtifactName(java.lang.String)
meth public void setCodeBuildServiceRole(java.lang.String)
meth public void setComputeType(com.amazonaws.services.elasticbeanstalk.model.ComputeType)
meth public void setComputeType(java.lang.String)
meth public void setImage(java.lang.String)
meth public void setTimeoutInMinutes(java.lang.Integer)
supr java.lang.Object
hfds artifactName,codeBuildServiceRole,computeType,image,timeoutInMinutes

CLSS public com.amazonaws.services.elasticbeanstalk.model.Builder
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Builder clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Builder withARN(java.lang.String)
meth public int hashCode()
meth public java.lang.String getARN()
meth public java.lang.String toString()
meth public void setARN(java.lang.String)
supr java.lang.Object
hfds aRN

CLSS public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withIOWait(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withIRQ(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withIdle(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withNice(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withPrivileged(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withSoftIRQ(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withSystem(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization withUser(java.lang.Double)
meth public int hashCode()
meth public java.lang.Double getIOWait()
meth public java.lang.Double getIRQ()
meth public java.lang.Double getIdle()
meth public java.lang.Double getNice()
meth public java.lang.Double getPrivileged()
meth public java.lang.Double getSoftIRQ()
meth public java.lang.Double getSystem()
meth public java.lang.Double getUser()
meth public java.lang.String toString()
meth public void setIOWait(java.lang.Double)
meth public void setIRQ(java.lang.Double)
meth public void setIdle(java.lang.Double)
meth public void setNice(java.lang.Double)
meth public void setPrivileged(java.lang.Double)
meth public void setSoftIRQ(java.lang.Double)
meth public void setSystem(java.lang.Double)
meth public void setUser(java.lang.Double)
supr java.lang.Object
hfds iOWait,iRQ,idle,nice,privileged,softIRQ,system,user

CLSS public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest withCNAMEPrefix(java.lang.String)
meth public int hashCode()
meth public java.lang.String getCNAMEPrefix()
meth public java.lang.String toString()
meth public void setCNAMEPrefix(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds cNAMEPrefix

CLSS public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult withAvailable(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult withFullyQualifiedCNAME(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getAvailable()
meth public java.lang.Boolean isAvailable()
meth public java.lang.String getFullyQualifiedCNAME()
meth public java.lang.String toString()
meth public void setAvailable(java.lang.Boolean)
meth public void setFullyQualifiedCNAME(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds available,fullyQualifiedCNAME

CLSS public com.amazonaws.services.elasticbeanstalk.model.CodeBuildNotInServiceRegionException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest withVersionLabels(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest withGroupName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsRequest withVersionLabels(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getGroupName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getVersionLabels()
meth public void setApplicationName(java.lang.String)
meth public void setGroupName(java.lang.String)
meth public void setVersionLabels(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,groupName,versionLabels

CLSS public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult withEnvironments(com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult withEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.ComposeEnvironmentsResult withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription> getEnvironments()
meth public void setEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds environments,nextToken

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ComputeType
fld public final static com.amazonaws.services.elasticbeanstalk.model.ComputeType BUILD_GENERAL1_LARGE
fld public final static com.amazonaws.services.elasticbeanstalk.model.ComputeType BUILD_GENERAL1_MEDIUM
fld public final static com.amazonaws.services.elasticbeanstalk.model.ComputeType BUILD_GENERAL1_SMALL
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ComputeType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ComputeType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ComputeType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ComputeType>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus Deployed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus Failed
fld public final static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus Pending
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueOptions(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withChangeSeverity(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withDefaultValue(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withMaxLength(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withMaxValue(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withMinValue(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withRegex(com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withUserDefined(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueOptions(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueType(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex getRegex()
meth public int hashCode()
meth public java.lang.Boolean getUserDefined()
meth public java.lang.Boolean isUserDefined()
meth public java.lang.Integer getMaxLength()
meth public java.lang.Integer getMaxValue()
meth public java.lang.Integer getMinValue()
meth public java.lang.String getChangeSeverity()
meth public java.lang.String getDefaultValue()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getValueType()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getValueOptions()
meth public void setChangeSeverity(java.lang.String)
meth public void setDefaultValue(java.lang.String)
meth public void setMaxLength(java.lang.Integer)
meth public void setMaxValue(java.lang.Integer)
meth public void setMinValue(java.lang.Integer)
meth public void setName(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setRegex(com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex)
meth public void setUserDefined(java.lang.Boolean)
meth public void setValueOptions(java.util.Collection<java.lang.String>)
meth public void setValueType(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType)
meth public void setValueType(java.lang.String)
supr java.lang.Object
hfds changeSeverity,defaultValue,maxLength,maxValue,minValue,name,namespace,regex,userDefined,valueOptions,valueType

CLSS public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withOptionName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withResourceName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String getResourceName()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
meth public void setResourceName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds namespace,optionName,resourceName,value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType
fld public final static com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType List
fld public final static com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType Scalar
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionValueType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,platformArn,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig getResourceLifecycleConfig()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTags()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public void setTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,resourceLifecycleConfig,tags

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription getApplication()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult withApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds application

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration getBuildConfiguration()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withAutoCreateApplication(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withBuildConfiguration(com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withProcess(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withSourceBuildInformation(com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location getSourceBundle()
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation getSourceBuildInformation()
meth public int hashCode()
meth public java.lang.Boolean getAutoCreateApplication()
meth public java.lang.Boolean getProcess()
meth public java.lang.Boolean isAutoCreateApplication()
meth public java.lang.Boolean isProcess()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTags()
meth public void setApplicationName(java.lang.String)
meth public void setAutoCreateApplication(java.lang.Boolean)
meth public void setBuildConfiguration(com.amazonaws.services.elasticbeanstalk.model.BuildConfiguration)
meth public void setDescription(java.lang.String)
meth public void setProcess(java.lang.Boolean)
meth public void setSourceBuildInformation(com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation)
meth public void setSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public void setTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,autoCreateApplication,buildConfiguration,description,process,sourceBuildInformation,sourceBundle,tags,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription getApplicationVersion()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult withApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationVersion

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withSourceConfiguration(com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration getSourceConfiguration()
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTags()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setSourceConfiguration(com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration)
meth public void setTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,environmentId,optionSettings,platformArn,solutionStackName,sourceConfiguration,tags,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,platformArn,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withCNAMEPrefix(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withGroupName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAMEPrefix()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getGroupName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptionsToRemove()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTags()
meth public void setApplicationName(java.lang.String)
meth public void setCNAMEPrefix(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setGroupName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,cNAMEPrefix,description,environmentName,groupName,optionSettings,optionsToRemove,platformArn,solutionStackName,tags,templateName,tier,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentLinks(com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withAbortableOperationInProgress(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription getResources()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public int hashCode()
meth public java.lang.Boolean getAbortableOperationInProgress()
meth public java.lang.Boolean isAbortableOperationInProgress()
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentArn()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink> getEnvironmentLinks()
meth public void setAbortableOperationInProgress(java.lang.Boolean)
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentArn(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public void setHealth(java.lang.String)
meth public void setHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public void setHealthStatus(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds abortableOperationInProgress,applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentArn,environmentId,environmentLinks,environmentName,health,healthStatus,platformArn,resources,solutionStackName,status,templateName,tier,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withPlatformDefinitionBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withPlatformName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withPlatformVersion(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionRequest withTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location getPlatformDefinitionBundle()
meth public int hashCode()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getPlatformName()
meth public java.lang.String getPlatformVersion()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTags()
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setPlatformDefinitionBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public void setPlatformName(java.lang.String)
meth public void setPlatformVersion(java.lang.String)
meth public void setTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentName,optionSettings,platformDefinitionBundle,platformName,platformVersion,tags

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Builder getBuilder()
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult withBuilder(com.amazonaws.services.elasticbeanstalk.model.Builder)
meth public com.amazonaws.services.elasticbeanstalk.model.CreatePlatformVersionResult withPlatformSummary(com.amazonaws.services.elasticbeanstalk.model.PlatformSummary)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary getPlatformSummary()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setBuilder(com.amazonaws.services.elasticbeanstalk.model.Builder)
meth public void setPlatformSummary(com.amazonaws.services.elasticbeanstalk.model.PlatformSummary)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds builder,platformSummary

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult withS3Bucket(java.lang.String)
meth public int hashCode()
meth public java.lang.String getS3Bucket()
meth public java.lang.String toString()
meth public void setS3Bucket(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds s3Bucket

CLSS public com.amazonaws.services.elasticbeanstalk.model.CustomAmi
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CustomAmi clone()
meth public com.amazonaws.services.elasticbeanstalk.model.CustomAmi withImageId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CustomAmi withVirtualizationType(java.lang.String)
meth public int hashCode()
meth public java.lang.String getImageId()
meth public java.lang.String getVirtualizationType()
meth public java.lang.String toString()
meth public void setImageId(java.lang.String)
meth public void setVirtualizationType(java.lang.String)
supr java.lang.Object
hfds imageId,virtualizationType

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest withTerminateEnvByForce(java.lang.Boolean)
meth public int hashCode()
meth public java.lang.Boolean getTerminateEnvByForce()
meth public java.lang.Boolean isTerminateEnvByForce()
meth public java.lang.String getApplicationName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setTerminateEnvByForce(java.lang.Boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,terminateEnvByForce

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withDeleteSourceBundle(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getDeleteSourceBundle()
meth public java.lang.Boolean isDeleteSourceBundle()
meth public java.lang.String getApplicationName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setDeleteSourceBundle(java.lang.Boolean)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,deleteSourceBundle,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionRequest withPlatformArn(java.lang.String)
meth public int hashCode()
meth public java.lang.String getPlatformArn()
meth public java.lang.String toString()
meth public void setPlatformArn(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds platformArn

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DeletePlatformVersionResult withPlatformSummary(com.amazonaws.services.elasticbeanstalk.model.PlatformSummary)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary getPlatformSummary()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setPlatformSummary(com.amazonaws.services.elasticbeanstalk.model.PlatformSummary)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds platformSummary

CLSS public com.amazonaws.services.elasticbeanstalk.model.Deployment
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment withDeploymentId(java.lang.Long)
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment withDeploymentTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Long getDeploymentId()
meth public java.lang.String getStatus()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDeploymentTime()
meth public void setDeploymentId(java.lang.Long)
meth public void setDeploymentTime(java.util.Date)
meth public void setStatus(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds deploymentId,deploymentTime,status,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesRequest clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeAccountAttributesResult withResourceQuotas(com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas getResourceQuotas()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setResourceQuotas(com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds resourceQuotas

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withVersionLabels(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withMaxRecords(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withVersionLabels(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.Integer getMaxRecords()
meth public java.lang.String getApplicationName()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getVersionLabels()
meth public void setApplicationName(java.lang.String)
meth public void setMaxRecords(java.lang.Integer)
meth public void setNextToken(java.lang.String)
meth public void setVersionLabels(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,maxRecords,nextToken,versionLabels

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult withApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult withApplicationVersions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription> getApplicationVersions()
meth public void setApplicationVersions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription>)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationVersions,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest withApplicationNames(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest withApplicationNames(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getApplicationNames()
meth public void setApplicationNames(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationNames

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult withApplications(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult withApplications(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription> getApplications()
meth public void setApplications(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applications

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withOptions(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptions()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName,options,platformArn,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withOptions(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withSolutionStackName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription> getOptions()
meth public void setOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds options,platformArn,solutionStackName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult withConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult withConfigurationSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription> getConfigurationSettings()
meth public void setConfigurationSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds configurationSettings

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest withAttributeNames(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest withAttributeNames(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest withAttributeNames(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getAttributeNames()
meth public void setAttributeNames(java.util.Collection<java.lang.String>)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds attributeNames,environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withCauses(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics getApplicationMetrics()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withApplicationMetrics(com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withCauses(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withColor(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withInstancesHealth(com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withRefreshedAt(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary getInstancesHealth()
meth public int hashCode()
meth public java.lang.String getColor()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public java.util.Date getRefreshedAt()
meth public java.util.List<java.lang.String> getCauses()
meth public void setApplicationMetrics(com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics)
meth public void setCauses(java.util.Collection<java.lang.String>)
meth public void setColor(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealthStatus(java.lang.String)
meth public void setInstancesHealth(com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary)
meth public void setRefreshedAt(java.util.Date)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public void setStatus(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationMetrics,causes,color,environmentName,healthStatus,instancesHealth,refreshedAt,status

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest withMaxItems(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryRequest withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.Integer getMaxItems()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setMaxItems(java.lang.Integer)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,maxItems,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult withManagedActionHistoryItems(com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult withManagedActionHistoryItems(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionHistoryResult withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem> getManagedActionHistoryItems()
meth public void setManagedActionHistoryItems(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem>)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds managedActionHistoryItems,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest withStatus(com.amazonaws.services.elasticbeanstalk.model.ActionStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsRequest withStatus(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.ActionStatus)
meth public void setStatus(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,status

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult withManagedActions(com.amazonaws.services.elasticbeanstalk.model.ManagedAction[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentManagedActionsResult withManagedActions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ManagedAction>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ManagedAction> getManagedActions()
meth public void setManagedActions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ManagedAction>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds managedActions

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult withEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription getEnvironmentResources()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds environmentResources

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentIds(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentNames(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentIds(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentNames(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withIncludeDeleted(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withIncludedDeletedBackTo(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withMaxRecords(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getIncludeDeleted()
meth public java.lang.Boolean isIncludeDeleted()
meth public java.lang.Integer getMaxRecords()
meth public java.lang.String getApplicationName()
meth public java.lang.String getNextToken()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getIncludedDeletedBackTo()
meth public java.util.List<java.lang.String> getEnvironmentIds()
meth public java.util.List<java.lang.String> getEnvironmentNames()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentIds(java.util.Collection<java.lang.String>)
meth public void setEnvironmentNames(java.util.Collection<java.lang.String>)
meth public void setIncludeDeleted(java.lang.Boolean)
meth public void setIncludedDeletedBackTo(java.util.Date)
meth public void setMaxRecords(java.lang.Integer)
meth public void setNextToken(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentIds,environmentNames,includeDeleted,includedDeletedBackTo,maxRecords,nextToken,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult withEnvironments(com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult withEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription> getEnvironments()
meth public void setEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds environments,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEndTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withMaxRecords(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withRequestId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withSeverity(com.amazonaws.services.elasticbeanstalk.model.EventSeverity)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withSeverity(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withStartTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Integer getMaxRecords()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getNextToken()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getRequestId()
meth public java.lang.String getSeverity()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getEndTime()
meth public java.util.Date getStartTime()
meth public void setApplicationName(java.lang.String)
meth public void setEndTime(java.util.Date)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setMaxRecords(java.lang.Integer)
meth public void setNextToken(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setRequestId(java.lang.String)
meth public void setSeverity(com.amazonaws.services.elasticbeanstalk.model.EventSeverity)
meth public void setSeverity(java.lang.String)
meth public void setStartTime(java.util.Date)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,endTime,environmentId,environmentName,maxRecords,nextToken,platformArn,requestId,severity,startTime,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withEvents(com.amazonaws.services.elasticbeanstalk.model.EventDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withEvents(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EventDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EventDescription> getEvents()
meth public void setEvents(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EventDescription>)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds events,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withAttributeNames(com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withAttributeNames(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withAttributeNames(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthRequest withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getAttributeNames()
meth public void setAttributeNames(java.util.Collection<java.lang.String>)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds attributeNames,environmentId,environmentName,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult withInstanceHealthList(com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult withInstanceHealthList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeInstancesHealthResult withRefreshedAt(java.util.Date)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.Date getRefreshedAt()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth> getInstanceHealthList()
meth public void setInstanceHealthList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth>)
meth public void setNextToken(java.lang.String)
meth public void setRefreshedAt(java.util.Date)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds instanceHealthList,nextToken,refreshedAt

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionRequest withPlatformArn(java.lang.String)
meth public int hashCode()
meth public java.lang.String getPlatformArn()
meth public java.lang.String toString()
meth public void setPlatformArn(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds platformArn

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribePlatformVersionResult withPlatformDescription(com.amazonaws.services.elasticbeanstalk.model.PlatformDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription getPlatformDescription()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setPlatformDescription(com.amazonaws.services.elasticbeanstalk.model.PlatformDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds platformDescription

CLSS public com.amazonaws.services.elasticbeanstalk.model.ElasticBeanstalkServiceException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentLinks(com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withAbortableOperationInProgress(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription getResources()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public int hashCode()
meth public java.lang.Boolean getAbortableOperationInProgress()
meth public java.lang.Boolean isAbortableOperationInProgress()
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentArn()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink> getEnvironmentLinks()
meth public void setAbortableOperationInProgress(java.lang.Boolean)
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentArn(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public void setHealth(java.lang.String)
meth public void setHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public void setHealthStatus(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds abortableOperationInProgress,applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentArn,environmentId,environmentLinks,environmentName,health,healthStatus,platformArn,resources,solutionStackName,status,templateName,tier,versionLabel

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth Green
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth Grey
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth Red
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth Yellow
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute All
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute ApplicationMetrics
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute Causes
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute Color
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute HealthStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute InstancesHealth
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute RefreshedAt
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute Status
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthAttribute>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Degraded
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Info
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus NoData
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Ok
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Pending
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Severe
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Suspended
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Unknown
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus Warning
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withEc2InstanceId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withInfoType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withSampleTimestamp(java.util.Date)
meth public int hashCode()
meth public java.lang.String getEc2InstanceId()
meth public java.lang.String getInfoType()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.util.Date getSampleTimestamp()
meth public void setEc2InstanceId(java.lang.String)
meth public void setInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public void setInfoType(java.lang.String)
meth public void setMessage(java.lang.String)
meth public void setSampleTimestamp(java.util.Date)
supr java.lang.Object
hfds ec2InstanceId,infoType,message,sampleTimestamp

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType Bundle
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType Tail
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink withLinkName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getLinkName()
meth public java.lang.String toString()
meth public void setEnvironmentName(java.lang.String)
meth public void setLinkName(java.lang.String)
supr java.lang.Object
hfds environmentName,linkName

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withAutoScalingGroups(com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withInstances(com.amazonaws.services.elasticbeanstalk.model.Instance[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchConfigurations(com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchTemplates(com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLoadBalancers(com.amazonaws.services.elasticbeanstalk.model.LoadBalancer[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withQueues(com.amazonaws.services.elasticbeanstalk.model.Queue[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withTriggers(com.amazonaws.services.elasticbeanstalk.model.Trigger[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withAutoScalingGroups(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withInstances(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Instance>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchConfigurations(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchTemplates(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLoadBalancers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withQueues(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Queue>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withTriggers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Trigger>)
meth public int hashCode()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup> getAutoScalingGroups()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Instance> getInstances()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration> getLaunchConfigurations()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate> getLaunchTemplates()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer> getLoadBalancers()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Queue> getQueues()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Trigger> getTriggers()
meth public void setAutoScalingGroups(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup>)
meth public void setEnvironmentName(java.lang.String)
meth public void setInstances(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Instance>)
meth public void setLaunchConfigurations(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration>)
meth public void setLaunchTemplates(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate>)
meth public void setLoadBalancers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer>)
meth public void setQueues(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Queue>)
meth public void setTriggers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Trigger>)
supr java.lang.Object
hfds autoScalingGroups,environmentName,instances,launchConfigurations,launchTemplates,loadBalancers,queues,triggers

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription withLoadBalancer(com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription getLoadBalancer()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setLoadBalancer(com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription)
supr java.lang.Object
hfds loadBalancer

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus Launching
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus Ready
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus Terminated
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus Terminating
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus Updating
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier withName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier withType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier withVersion(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
meth public void setType(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds name,type,version

CLSS public com.amazonaws.services.elasticbeanstalk.model.EventDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withEventDate(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withRequestId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withSeverity(com.amazonaws.services.elasticbeanstalk.model.EventSeverity)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withSeverity(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getMessage()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getRequestId()
meth public java.lang.String getSeverity()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getEventDate()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setEventDate(java.util.Date)
meth public void setMessage(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setRequestId(java.lang.String)
meth public void setSeverity(com.amazonaws.services.elasticbeanstalk.model.EventSeverity)
meth public void setSeverity(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,environmentName,eventDate,message,platformArn,requestId,severity,templateName,versionLabel

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EventSeverity
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity DEBUG
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity ERROR
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity FATAL
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity INFO
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity TRACE
fld public final static com.amazonaws.services.elasticbeanstalk.model.EventSeverity WARN
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EventSeverity fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EventSeverity valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EventSeverity[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EventSeverity>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.FailureType
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType CancellationFailed
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType InternalFailure
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType InvalidEnvironmentState
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType PermissionsError
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType RollbackFailed
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType RollbackSuccessful
fld public final static com.amazonaws.services.elasticbeanstalk.model.FailureType UpdateCancelled
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.FailureType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.FailureType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.FailureType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.FailureType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.Instance
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Instance clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Instance withId(java.lang.String)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public void setId(java.lang.String)
supr java.lang.Object
hfds id

CLSS public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary clone()
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withDegraded(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withInfo(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withNoData(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withOk(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withPending(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withSevere(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withUnknown(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.InstanceHealthSummary withWarning(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Integer getDegraded()
meth public java.lang.Integer getInfo()
meth public java.lang.Integer getNoData()
meth public java.lang.Integer getOk()
meth public java.lang.Integer getPending()
meth public java.lang.Integer getSevere()
meth public java.lang.Integer getUnknown()
meth public java.lang.Integer getWarning()
meth public java.lang.String toString()
meth public void setDegraded(java.lang.Integer)
meth public void setInfo(java.lang.Integer)
meth public void setNoData(java.lang.Integer)
meth public void setOk(java.lang.Integer)
meth public void setPending(java.lang.Integer)
meth public void setSevere(java.lang.Integer)
meth public void setUnknown(java.lang.Integer)
meth public void setWarning(java.lang.Integer)
supr java.lang.Object
hfds degraded,info,noData,ok,pending,severe,unknown,warning

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute All
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute ApplicationMetrics
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute AvailabilityZone
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute Causes
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute Color
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute Deployment
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute HealthStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute InstanceType
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute LaunchedAt
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute RefreshedAt
fld public final static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute System
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.InstancesHealthAttribute>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.InsufficientPrivilegesException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.InvalidRequestException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.Latency
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP10(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP50(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP75(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP85(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP90(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP95(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP99(java.lang.Double)
meth public com.amazonaws.services.elasticbeanstalk.model.Latency withP999(java.lang.Double)
meth public int hashCode()
meth public java.lang.Double getP10()
meth public java.lang.Double getP50()
meth public java.lang.Double getP75()
meth public java.lang.Double getP85()
meth public java.lang.Double getP90()
meth public java.lang.Double getP95()
meth public java.lang.Double getP99()
meth public java.lang.Double getP999()
meth public java.lang.String toString()
meth public void setP10(java.lang.Double)
meth public void setP50(java.lang.Double)
meth public void setP75(java.lang.Double)
meth public void setP85(java.lang.Double)
meth public void setP90(java.lang.Double)
meth public void setP95(java.lang.Double)
meth public void setP99(java.lang.Double)
meth public void setP999(java.lang.Double)
supr java.lang.Object
hfds p10,p50,p75,p85,p90,p95,p99,p999

CLSS public com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration clone()
meth public com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration withName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate clone()
meth public com.amazonaws.services.elasticbeanstalk.model.LaunchTemplate withId(java.lang.String)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public void setId(java.lang.String)
supr java.lang.Object
hfds id

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStackDetails(com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStacks(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStackDetails(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStacks(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription> getSolutionStackDetails()
meth public java.util.List<java.lang.String> getSolutionStacks()
meth public void setSolutionStackDetails(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription>)
meth public void setSolutionStacks(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds solutionStackDetails,solutionStacks

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest withFilters(com.amazonaws.services.elasticbeanstalk.model.PlatformFilter[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest withFilters(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformFilter>)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest withMaxRecords(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsRequest withNextToken(java.lang.String)
meth public int hashCode()
meth public java.lang.Integer getMaxRecords()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.PlatformFilter> getFilters()
meth public void setFilters(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformFilter>)
meth public void setMaxRecords(java.lang.Integer)
meth public void setNextToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds filters,maxRecords,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult withPlatformSummaryList(com.amazonaws.services.elasticbeanstalk.model.PlatformSummary[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ListPlatformVersionsResult withPlatformSummaryList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformSummary>)
meth public int hashCode()
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.PlatformSummary> getPlatformSummaryList()
meth public void setNextToken(java.lang.String)
meth public void setPlatformSummaryList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformSummary>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds nextToken,platformSummaryList

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceRequest withResourceArn(java.lang.String)
meth public int hashCode()
meth public java.lang.String getResourceArn()
meth public java.lang.String toString()
meth public void setResourceArn(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds resourceArn

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult withResourceTags(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult withResourceArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ListTagsForResourceResult withResourceTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public int hashCode()
meth public java.lang.String getResourceArn()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getResourceTags()
meth public void setResourceArn(java.lang.String)
meth public void setResourceTags(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds resourceArn,resourceTags

CLSS public com.amazonaws.services.elasticbeanstalk.model.Listener
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Listener clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Listener withPort(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.Listener withProtocol(java.lang.String)
meth public int hashCode()
meth public java.lang.Integer getPort()
meth public java.lang.String getProtocol()
meth public java.lang.String toString()
meth public void setPort(java.lang.Integer)
meth public void setProtocol(java.lang.String)
supr java.lang.Object
hfds port,protocol

CLSS public com.amazonaws.services.elasticbeanstalk.model.LoadBalancer
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancer clone()
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancer withName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription withListeners(com.amazonaws.services.elasticbeanstalk.model.Listener[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription withDomain(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription withListeners(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Listener>)
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancerDescription withLoadBalancerName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getDomain()
meth public java.lang.String getLoadBalancerName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Listener> getListeners()
meth public void setDomain(java.lang.String)
meth public void setListeners(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Listener>)
meth public void setLoadBalancerName(java.lang.String)
supr java.lang.Object
hfds domain,listeners,loadBalancerName

CLSS public com.amazonaws.services.elasticbeanstalk.model.ManagedAction
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withActionDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withActionId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withActionType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withStatus(com.amazonaws.services.elasticbeanstalk.model.ActionStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedAction withWindowStartTime(java.util.Date)
meth public int hashCode()
meth public java.lang.String getActionDescription()
meth public java.lang.String getActionId()
meth public java.lang.String getActionType()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public java.util.Date getWindowStartTime()
meth public void setActionDescription(java.lang.String)
meth public void setActionId(java.lang.String)
meth public void setActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public void setActionType(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.ActionStatus)
meth public void setStatus(java.lang.String)
meth public void setWindowStartTime(java.util.Date)
supr java.lang.Object
hfds actionDescription,actionId,actionType,status,windowStartTime

CLSS public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withActionDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withActionId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withActionType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withExecutedTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withFailureDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withFailureType(com.amazonaws.services.elasticbeanstalk.model.FailureType)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withFailureType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withFinishedTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withStatus(com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.ManagedActionHistoryItem withStatus(java.lang.String)
meth public int hashCode()
meth public java.lang.String getActionDescription()
meth public java.lang.String getActionId()
meth public java.lang.String getActionType()
meth public java.lang.String getFailureDescription()
meth public java.lang.String getFailureType()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public java.util.Date getExecutedTime()
meth public java.util.Date getFinishedTime()
meth public void setActionDescription(java.lang.String)
meth public void setActionId(java.lang.String)
meth public void setActionType(com.amazonaws.services.elasticbeanstalk.model.ActionType)
meth public void setActionType(java.lang.String)
meth public void setExecutedTime(java.util.Date)
meth public void setFailureDescription(java.lang.String)
meth public void setFailureType(com.amazonaws.services.elasticbeanstalk.model.FailureType)
meth public void setFailureType(java.lang.String)
meth public void setFinishedTime(java.util.Date)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.ActionHistoryStatus)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds actionDescription,actionId,actionType,executedTime,failureDescription,failureType,finishedTime,status

CLSS public com.amazonaws.services.elasticbeanstalk.model.ManagedActionInvalidStateException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule clone()
meth public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule withDeleteSourceFromS3(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule withEnabled(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxAgeRule withMaxAgeInDays(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Boolean getDeleteSourceFromS3()
meth public java.lang.Boolean getEnabled()
meth public java.lang.Boolean isDeleteSourceFromS3()
meth public java.lang.Boolean isEnabled()
meth public java.lang.Integer getMaxAgeInDays()
meth public java.lang.String toString()
meth public void setDeleteSourceFromS3(java.lang.Boolean)
meth public void setEnabled(java.lang.Boolean)
meth public void setMaxAgeInDays(java.lang.Integer)
supr java.lang.Object
hfds deleteSourceFromS3,enabled,maxAgeInDays

CLSS public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule clone()
meth public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule withDeleteSourceFromS3(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule withEnabled(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.MaxCountRule withMaxCount(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Boolean getDeleteSourceFromS3()
meth public java.lang.Boolean getEnabled()
meth public java.lang.Boolean isDeleteSourceFromS3()
meth public java.lang.Boolean isEnabled()
meth public java.lang.Integer getMaxCount()
meth public java.lang.String toString()
meth public void setDeleteSourceFromS3(java.lang.Boolean)
meth public void setEnabled(java.lang.Boolean)
meth public void setMaxCount(java.lang.Integer)
supr java.lang.Object
hfds deleteSourceFromS3,enabled,maxCount

CLSS public com.amazonaws.services.elasticbeanstalk.model.OperationInProgressException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex clone()
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex withLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex withPattern(java.lang.String)
meth public int hashCode()
meth public java.lang.String getLabel()
meth public java.lang.String getPattern()
meth public java.lang.String toString()
meth public void setLabel(java.lang.String)
meth public void setPattern(java.lang.String)
supr java.lang.Object
hfds label,pattern

CLSS public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification clone()
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification withOptionName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification withResourceName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String getResourceName()
meth public java.lang.String toString()
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
meth public void setResourceName(java.lang.String)
supr java.lang.Object
hfds namespace,optionName,resourceName

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withCustomAmiList(com.amazonaws.services.elasticbeanstalk.model.CustomAmi[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withFrameworks(com.amazonaws.services.elasticbeanstalk.model.PlatformFramework[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withProgrammingLanguages(com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withSupportedAddonList(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withSupportedTierList(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withCustomAmiList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.CustomAmi>)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withFrameworks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformFramework>)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withMaintainer(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withOperatingSystemName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withOperatingSystemVersion(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformCategory(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformOwner(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformStatus(com.amazonaws.services.elasticbeanstalk.model.PlatformStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withPlatformVersion(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withProgrammingLanguages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage>)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withSupportedAddonList(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformDescription withSupportedTierList(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getMaintainer()
meth public java.lang.String getOperatingSystemName()
meth public java.lang.String getOperatingSystemVersion()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getPlatformCategory()
meth public java.lang.String getPlatformName()
meth public java.lang.String getPlatformOwner()
meth public java.lang.String getPlatformStatus()
meth public java.lang.String getPlatformVersion()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.CustomAmi> getCustomAmiList()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.PlatformFramework> getFrameworks()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage> getProgrammingLanguages()
meth public java.util.List<java.lang.String> getSupportedAddonList()
meth public java.util.List<java.lang.String> getSupportedTierList()
meth public void setCustomAmiList(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.CustomAmi>)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setFrameworks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformFramework>)
meth public void setMaintainer(java.lang.String)
meth public void setOperatingSystemName(java.lang.String)
meth public void setOperatingSystemVersion(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setPlatformCategory(java.lang.String)
meth public void setPlatformName(java.lang.String)
meth public void setPlatformOwner(java.lang.String)
meth public void setPlatformStatus(com.amazonaws.services.elasticbeanstalk.model.PlatformStatus)
meth public void setPlatformStatus(java.lang.String)
meth public void setPlatformVersion(java.lang.String)
meth public void setProgrammingLanguages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage>)
meth public void setSolutionStackName(java.lang.String)
meth public void setSupportedAddonList(java.util.Collection<java.lang.String>)
meth public void setSupportedTierList(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds customAmiList,dateCreated,dateUpdated,description,frameworks,maintainer,operatingSystemName,operatingSystemVersion,platformArn,platformCategory,platformName,platformOwner,platformStatus,platformVersion,programmingLanguages,solutionStackName,supportedAddonList,supportedTierList

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformFilter
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformFilter withValues(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFilter clone()
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFilter withOperator(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFilter withType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFilter withValues(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String getOperator()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getValues()
meth public void setOperator(java.lang.String)
meth public void setType(java.lang.String)
meth public void setValues(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds operator,type,values

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformFramework
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFramework clone()
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFramework withName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformFramework withVersion(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds name,version

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage clone()
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage withName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformProgrammingLanguage withVersion(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds name,version

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.PlatformStatus
fld public final static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus Creating
fld public final static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus Deleted
fld public final static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus Deleting
fld public final static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus Failed
fld public final static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus Ready
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.PlatformStatus[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.PlatformStatus>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withSupportedAddonList(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withSupportedTierList(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary clone()
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withOperatingSystemName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withOperatingSystemVersion(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withPlatformCategory(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withPlatformOwner(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withPlatformStatus(com.amazonaws.services.elasticbeanstalk.model.PlatformStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withPlatformStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withSupportedAddonList(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.PlatformSummary withSupportedTierList(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String getOperatingSystemName()
meth public java.lang.String getOperatingSystemVersion()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getPlatformCategory()
meth public java.lang.String getPlatformOwner()
meth public java.lang.String getPlatformStatus()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getSupportedAddonList()
meth public java.util.List<java.lang.String> getSupportedTierList()
meth public void setOperatingSystemName(java.lang.String)
meth public void setOperatingSystemVersion(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setPlatformCategory(java.lang.String)
meth public void setPlatformOwner(java.lang.String)
meth public void setPlatformStatus(com.amazonaws.services.elasticbeanstalk.model.PlatformStatus)
meth public void setPlatformStatus(java.lang.String)
meth public void setSupportedAddonList(java.util.Collection<java.lang.String>)
meth public void setSupportedTierList(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds operatingSystemName,operatingSystemVersion,platformArn,platformCategory,platformOwner,platformStatus,supportedAddonList,supportedTierList

CLSS public com.amazonaws.services.elasticbeanstalk.model.PlatformVersionStillReferencedException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.Queue
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Queue clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Queue withName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.Queue withURL(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getURL()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
meth public void setURL(java.lang.String)
supr java.lang.Object
hfds name,uRL

CLSS public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest
cons public init()
cons public init(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withInfoType(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getInfoType()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public void setInfoType(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,infoType

CLSS public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.ResourceNotFoundException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota withMaximum(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Integer getMaximum()
meth public java.lang.String toString()
meth public void setMaximum(java.lang.Integer)
supr java.lang.Object
hfds maximum

CLSS public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota getApplicationQuota()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota getApplicationVersionQuota()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota getConfigurationTemplateQuota()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota getCustomPlatformQuota()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuota getEnvironmentQuota()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas withApplicationQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas withApplicationVersionQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas withConfigurationTemplateQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas withCustomPlatformQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public com.amazonaws.services.elasticbeanstalk.model.ResourceQuotas withEnvironmentQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplicationQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public void setApplicationVersionQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public void setConfigurationTemplateQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public void setCustomPlatformQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
meth public void setEnvironmentQuota(com.amazonaws.services.elasticbeanstalk.model.ResourceQuota)
supr java.lang.Object
hfds applicationQuota,applicationVersionQuota,configurationTemplateQuota,customPlatformQuota,environmentQuota

CLSS public com.amazonaws.services.elasticbeanstalk.model.ResourceTypeNotSupportedException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest withEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest
cons public init()
cons public init(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withInfoType(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getInfoType()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setInfoType(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType)
meth public void setInfoType(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,infoType

CLSS public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult withEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult withEnvironmentInfo(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription> getEnvironmentInfo()
meth public void setEnvironmentInfo(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds environmentInfo

CLSS public com.amazonaws.services.elasticbeanstalk.model.S3Location
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location clone()
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location withS3Bucket(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location withS3Key(java.lang.String)
meth public int hashCode()
meth public java.lang.String getS3Bucket()
meth public java.lang.String getS3Key()
meth public java.lang.String toString()
meth public void setS3Bucket(java.lang.String)
meth public void setS3Key(java.lang.String)
supr java.lang.Object
hfds s3Bucket,s3Key

CLSS public com.amazonaws.services.elasticbeanstalk.model.S3LocationNotInServiceRegionException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.S3SubscriptionRequiredException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withCauses(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics getApplicationMetrics()
meth public com.amazonaws.services.elasticbeanstalk.model.Deployment getDeployment()
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withApplicationMetrics(com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withAvailabilityZone(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withCauses(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withColor(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withDeployment(com.amazonaws.services.elasticbeanstalk.model.Deployment)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withInstanceId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withInstanceType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withLaunchedAt(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.SingleInstanceHealth withSystem(com.amazonaws.services.elasticbeanstalk.model.SystemStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.SystemStatus getSystem()
meth public int hashCode()
meth public java.lang.String getAvailabilityZone()
meth public java.lang.String getColor()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getInstanceId()
meth public java.lang.String getInstanceType()
meth public java.lang.String toString()
meth public java.util.Date getLaunchedAt()
meth public java.util.List<java.lang.String> getCauses()
meth public void setApplicationMetrics(com.amazonaws.services.elasticbeanstalk.model.ApplicationMetrics)
meth public void setAvailabilityZone(java.lang.String)
meth public void setCauses(java.util.Collection<java.lang.String>)
meth public void setColor(java.lang.String)
meth public void setDeployment(com.amazonaws.services.elasticbeanstalk.model.Deployment)
meth public void setHealthStatus(java.lang.String)
meth public void setInstanceId(java.lang.String)
meth public void setInstanceType(java.lang.String)
meth public void setLaunchedAt(java.util.Date)
meth public void setSystem(com.amazonaws.services.elasticbeanstalk.model.SystemStatus)
supr java.lang.Object
hfds applicationMetrics,availabilityZone,causes,color,deployment,healthStatus,instanceId,instanceType,launchedAt,system

CLSS public com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription withPermittedFileTypes(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription withPermittedFileTypes(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.SolutionStackDescription withSolutionStackName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getPermittedFileTypes()
meth public void setPermittedFileTypes(java.util.Collection<java.lang.String>)
meth public void setSolutionStackName(java.lang.String)
supr java.lang.Object
hfds permittedFileTypes,solutionStackName

CLSS public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation withSourceLocation(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation withSourceRepository(com.amazonaws.services.elasticbeanstalk.model.SourceRepository)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation withSourceRepository(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation withSourceType(com.amazonaws.services.elasticbeanstalk.model.SourceType)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceBuildInformation withSourceType(java.lang.String)
meth public int hashCode()
meth public java.lang.String getSourceLocation()
meth public java.lang.String getSourceRepository()
meth public java.lang.String getSourceType()
meth public java.lang.String toString()
meth public void setSourceLocation(java.lang.String)
meth public void setSourceRepository(com.amazonaws.services.elasticbeanstalk.model.SourceRepository)
meth public void setSourceRepository(java.lang.String)
meth public void setSourceType(com.amazonaws.services.elasticbeanstalk.model.SourceType)
meth public void setSourceType(java.lang.String)
supr java.lang.Object
hfds sourceLocation,sourceRepository,sourceType

CLSS public com.amazonaws.services.elasticbeanstalk.model.SourceBundleDeletionException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,templateName

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.SourceRepository
fld public final static com.amazonaws.services.elasticbeanstalk.model.SourceRepository CodeCommit
fld public final static com.amazonaws.services.elasticbeanstalk.model.SourceRepository S3
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceRepository fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceRepository valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceRepository[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.SourceRepository>
hfds value

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.SourceType
fld public final static com.amazonaws.services.elasticbeanstalk.model.SourceType Git
fld public final static com.amazonaws.services.elasticbeanstalk.model.SourceType Zip
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.SourceType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.SourceType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.StatusCodes
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes clone()
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes withStatus2xx(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes withStatus3xx(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes withStatus4xx(java.lang.Integer)
meth public com.amazonaws.services.elasticbeanstalk.model.StatusCodes withStatus5xx(java.lang.Integer)
meth public int hashCode()
meth public java.lang.Integer getStatus2xx()
meth public java.lang.Integer getStatus3xx()
meth public java.lang.Integer getStatus4xx()
meth public java.lang.Integer getStatus5xx()
meth public java.lang.String toString()
meth public void setStatus2xx(java.lang.Integer)
meth public void setStatus3xx(java.lang.Integer)
meth public void setStatus4xx(java.lang.Integer)
meth public void setStatus5xx(java.lang.Integer)
supr java.lang.Object
hfds status2xx,status3xx,status4xx,status5xx

CLSS public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest withDestinationEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest withDestinationEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest withSourceEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest withSourceEnvironmentName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getDestinationEnvironmentId()
meth public java.lang.String getDestinationEnvironmentName()
meth public java.lang.String getSourceEnvironmentId()
meth public java.lang.String getSourceEnvironmentName()
meth public java.lang.String toString()
meth public void setDestinationEnvironmentId(java.lang.String)
meth public void setDestinationEnvironmentName(java.lang.String)
meth public void setSourceEnvironmentId(java.lang.String)
meth public void setSourceEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds destinationEnvironmentId,destinationEnvironmentName,sourceEnvironmentId,sourceEnvironmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.SystemStatus
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.SystemStatus withLoadAverage(java.lang.Double[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.CPUUtilization getCPUUtilization()
meth public com.amazonaws.services.elasticbeanstalk.model.SystemStatus clone()
meth public com.amazonaws.services.elasticbeanstalk.model.SystemStatus withCPUUtilization(com.amazonaws.services.elasticbeanstalk.model.CPUUtilization)
meth public com.amazonaws.services.elasticbeanstalk.model.SystemStatus withLoadAverage(java.util.Collection<java.lang.Double>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Double> getLoadAverage()
meth public void setCPUUtilization(com.amazonaws.services.elasticbeanstalk.model.CPUUtilization)
meth public void setLoadAverage(java.util.Collection<java.lang.Double>)
supr java.lang.Object
hfds cPUUtilization,loadAverage

CLSS public com.amazonaws.services.elasticbeanstalk.model.Tag
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Tag clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Tag withKey(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.Tag withValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String getKey()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setKey(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds key,value

CLSS public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withForceTerminate(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withTerminateResources(java.lang.Boolean)
meth public int hashCode()
meth public java.lang.Boolean getForceTerminate()
meth public java.lang.Boolean getTerminateResources()
meth public java.lang.Boolean isForceTerminate()
meth public java.lang.Boolean isTerminateResources()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setForceTerminate(java.lang.Boolean)
meth public void setTerminateResources(java.lang.Boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,forceTerminate,terminateResources

CLSS public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentLinks(com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription getResources()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withAbortableOperationInProgress(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getAbortableOperationInProgress()
meth public java.lang.Boolean isAbortableOperationInProgress()
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentArn()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink> getEnvironmentLinks()
meth public void setAbortableOperationInProgress(java.lang.Boolean)
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentArn(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public void setHealth(java.lang.String)
meth public void setHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public void setHealthStatus(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds abortableOperationInProgress,applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentArn,environmentId,environmentLinks,environmentName,health,healthStatus,platformArn,resources,solutionStackName,status,templateName,tier,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyApplicationVersionsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyApplicationsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyBucketsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyConfigurationTemplatesException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyEnvironmentsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyPlatformsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyTagsException
cons public init(java.lang.String)
supr com.amazonaws.services.elasticbeanstalk.model.AWSElasticBeanstalkException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.Trigger
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.Trigger clone()
meth public com.amazonaws.services.elasticbeanstalk.model.Trigger withName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest withDescription(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig getResourceLifecycleConfig()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleRequest withResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,resourceLifecycleConfig

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig getResourceLifecycleConfig()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResourceLifecycleResult withResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setResourceLifecycleConfig(com.amazonaws.services.elasticbeanstalk.model.ApplicationResourceLifecycleConfig)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationName,resourceLifecycleConfig

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription getApplication()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult withApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds application

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription getApplicationVersion()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult withApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationVersion

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptionsToRemove()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,optionSettings,optionsToRemove,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(com.amazonaws.services.elasticbeanstalk.model.ConfigurationDeploymentStatus)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,platformArn,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withGroupName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getGroupName()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptionsToRemove()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setGroupName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setPlatformArn(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,environmentId,environmentName,groupName,optionSettings,optionsToRemove,platformArn,solutionStackName,templateName,tier,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentLinks(com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription getResources()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier getTier()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withAbortableOperationInProgress(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withHealthStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withPlatformArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withVersionLabel(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getAbortableOperationInProgress()
meth public java.lang.Boolean isAbortableOperationInProgress()
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentArn()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getHealthStatus()
meth public java.lang.String getPlatformArn()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink> getEnvironmentLinks()
meth public void setAbortableOperationInProgress(java.lang.Boolean)
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentArn(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentLinks(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentLink>)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth)
meth public void setHealth(java.lang.String)
meth public void setHealthStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealthStatus)
meth public void setHealthStatus(java.lang.String)
meth public void setPlatformArn(java.lang.String)
meth public void setResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourcesDescription)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setTier(com.amazonaws.services.elasticbeanstalk.model.EnvironmentTier)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds abortableOperationInProgress,applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentArn,environmentId,environmentLinks,environmentName,health,healthStatus,platformArn,resources,solutionStackName,status,templateName,tier,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest withTagsToAdd(com.amazonaws.services.elasticbeanstalk.model.Tag[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest withTagsToRemove(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest withResourceArn(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest withTagsToAdd(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceRequest withTagsToRemove(java.util.Collection<java.lang.String>)
meth public int hashCode()
meth public java.lang.String getResourceArn()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Tag> getTagsToAdd()
meth public java.util.List<java.lang.String> getTagsToRemove()
meth public void setResourceArn(java.lang.String)
meth public void setTagsToAdd(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Tag>)
meth public void setTagsToRemove(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds resourceArn,tagsToAdd,tagsToRemove

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateTagsForResourceResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest
cons public init()
cons public init(java.lang.String,java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withTemplateName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName,optionSettings,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult withMessages(com.amazonaws.services.elasticbeanstalk.model.ValidationMessage[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult withMessages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage> getMessages()
meth public void setMessages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage>)
supr com.amazonaws.AmazonWebServiceResult<com.amazonaws.ResponseMetadata>
hfds messages

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage clone()
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withOptionName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withSeverity(com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withSeverity(java.lang.String)
meth public int hashCode()
meth public java.lang.String getMessage()
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String getSeverity()
meth public java.lang.String toString()
meth public void setMessage(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
meth public void setSeverity(com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity)
meth public void setSeverity(java.lang.String)
supr java.lang.Object
hfds message,namespace,optionName,severity

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity
fld public final static com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity Error
fld public final static com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity Warning
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.ValidationSeverity>
hfds value

CLSS public abstract com.amazonaws.services.s3.AbstractAmazonS3
cons public init()
intf com.amazonaws.services.s3.AmazonS3
meth public boolean doesBucketExist(java.lang.String)
meth public boolean doesBucketExistV2(java.lang.String)
meth public boolean doesObjectExist(java.lang.String,java.lang.String)
meth public boolean isRequesterPaysEnabled(java.lang.String)
meth public com.amazonaws.services.s3.S3ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getBucketAcl(com.amazonaws.services.s3.model.GetBucketAclRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getBucketAcl(java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(com.amazonaws.services.s3.model.GetObjectAclRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(com.amazonaws.services.s3.model.CreateBucketRequest)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,com.amazonaws.services.s3.model.Region)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(com.amazonaws.services.s3.model.GetBucketPolicyRequest)
meth public com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(com.amazonaws.services.s3.model.CopyObjectRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingResult deleteObjectTagging(com.amazonaws.services.s3.model.DeleteObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.DeleteObjectsResult deleteObjects(com.amazonaws.services.s3.model.DeleteObjectsRequest)
meth public com.amazonaws.services.s3.model.DeletePublicAccessBlockResult deletePublicAccessBlock(com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(com.amazonaws.services.s3.model.GetBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusResult getBucketPolicyStatus(com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest)
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldResult getObjectLegalHold(com.amazonaws.services.s3.model.GetObjectLegalHoldRequest)
meth public com.amazonaws.services.s3.model.GetObjectLockConfigurationResult getObjectLockConfiguration(com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetObjectRetentionResult getObjectRetention(com.amazonaws.services.s3.model.GetObjectRetentionRequest)
meth public com.amazonaws.services.s3.model.GetObjectTaggingResult getObjectTagging(com.amazonaws.services.s3.model.GetObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockResult getPublicAccessBlock(com.amazonaws.services.s3.model.GetPublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.HeadBucketResult headBucket(com.amazonaws.services.s3.model.HeadBucketRequest)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(com.amazonaws.services.s3.model.ListObjectsV2Request)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.MultipartUploadListing listMultipartUploads(com.amazonaws.services.s3.model.ListMultipartUploadsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ObjectListing)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(com.amazonaws.services.s3.model.ListObjectsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(com.amazonaws.services.s3.model.GetObjectMetadataRequest)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Owner getS3AccountOwner()
meth public com.amazonaws.services.s3.model.Owner getS3AccountOwner(com.amazonaws.services.s3.model.GetS3AccountOwnerRequest)
meth public com.amazonaws.services.s3.model.PartListing listParts(com.amazonaws.services.s3.model.ListPartsRequest)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadResult download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadResult upload(com.amazonaws.services.s3.model.PresignedUrlUploadRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Region getRegion()
meth public com.amazonaws.services.s3.model.RestoreObjectResult restoreObjectV2(com.amazonaws.services.s3.model.RestoreObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentResult selectObjectContent(com.amazonaws.services.s3.model.SelectObjectContentRequest)
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(java.lang.String,com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketEncryptionResult setBucketEncryption(com.amazonaws.services.s3.model.SetBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(java.lang.String,com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(java.lang.String,com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldResult setObjectLegalHold(com.amazonaws.services.s3.model.SetObjectLegalHoldRequest)
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationResult setObjectLockConfiguration(com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetObjectRetentionResult setObjectRetention(com.amazonaws.services.s3.model.SetObjectRetentionRequest)
meth public com.amazonaws.services.s3.model.SetObjectTaggingResult setObjectTagging(com.amazonaws.services.s3.model.SetObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockResult setPublicAccessBlock(com.amazonaws.services.s3.model.SetPublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest)
meth public com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.VersionListing)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(com.amazonaws.services.s3.model.ListVersionsRequest)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public com.amazonaws.services.s3.waiters.AmazonS3Waiters waiters()
meth public java.lang.String getBucketLocation(com.amazonaws.services.s3.model.GetBucketLocationRequest)
meth public java.lang.String getBucketLocation(java.lang.String)
meth public java.lang.String getObjectAsString(java.lang.String,java.lang.String)
meth public java.lang.String getRegionName()
meth public java.net.URL generatePresignedUrl(com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date,com.amazonaws.HttpMethod)
meth public java.net.URL getUrl(java.lang.String,java.lang.String)
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets()
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets(com.amazonaws.services.s3.model.ListBucketsRequest)
meth public void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public void changeObjectStorageClass(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.StorageClass)
meth public void deleteBucket(com.amazonaws.services.s3.model.DeleteBucketRequest)
meth public void deleteBucket(java.lang.String)
meth public void deleteBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest)
meth public void deleteBucketCrossOriginConfiguration(java.lang.String)
meth public void deleteBucketLifecycleConfiguration(com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest)
meth public void deleteBucketLifecycleConfiguration(java.lang.String)
meth public void deleteBucketPolicy(com.amazonaws.services.s3.model.DeleteBucketPolicyRequest)
meth public void deleteBucketPolicy(java.lang.String)
meth public void deleteBucketReplicationConfiguration(com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest)
meth public void deleteBucketReplicationConfiguration(java.lang.String)
meth public void deleteBucketTaggingConfiguration(com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest)
meth public void deleteBucketTaggingConfiguration(java.lang.String)
meth public void deleteBucketWebsiteConfiguration(com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest)
meth public void deleteBucketWebsiteConfiguration(java.lang.String)
meth public void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public void deleteObject(java.lang.String,java.lang.String)
meth public void deleteVersion(com.amazonaws.services.s3.model.DeleteVersionRequest)
meth public void deleteVersion(java.lang.String,java.lang.String,java.lang.String)
meth public void disableRequesterPays(java.lang.String)
meth public void download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest,java.io.File)
meth public void enableRequesterPays(java.lang.String)
meth public void restoreObject(com.amazonaws.services.s3.model.RestoreObjectRequest)
meth public void restoreObject(java.lang.String,java.lang.String,int)
meth public void setBucketAccelerateConfiguration(com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest)
meth public void setBucketAccelerateConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public void setBucketAcl(com.amazonaws.services.s3.model.SetBucketAclRequest)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest)
meth public void setBucketCrossOriginConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
meth public void setBucketLifecycleConfiguration(com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest)
meth public void setBucketLifecycleConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
meth public void setBucketLoggingConfiguration(com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest)
meth public void setBucketNotificationConfiguration(com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest)
meth public void setBucketNotificationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public void setBucketPolicy(com.amazonaws.services.s3.model.SetBucketPolicyRequest)
meth public void setBucketPolicy(java.lang.String,java.lang.String)
meth public void setBucketReplicationConfiguration(com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest)
meth public void setBucketReplicationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketReplicationConfiguration)
meth public void setBucketTaggingConfiguration(com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest)
meth public void setBucketTaggingConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketTaggingConfiguration)
meth public void setBucketVersioningConfiguration(com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest)
meth public void setBucketWebsiteConfiguration(com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest)
meth public void setBucketWebsiteConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public void setEndpoint(java.lang.String)
meth public void setObjectAcl(com.amazonaws.services.s3.model.SetObjectAclRequest)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectRedirectLocation(java.lang.String,java.lang.String,java.lang.String)
meth public void setRegion(com.amazonaws.regions.Region)
meth public void setS3ClientOptions(com.amazonaws.services.s3.S3ClientOptions)
meth public void shutdown()
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.AmazonS3
fld public final static java.lang.String ENDPOINT_PREFIX = "s3"
intf com.amazonaws.services.s3.internal.S3DirectSpi
meth public abstract boolean doesBucketExist(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract boolean doesBucketExistV2(java.lang.String)
meth public abstract boolean doesObjectExist(java.lang.String,java.lang.String)
meth public abstract boolean isRequesterPaysEnabled(java.lang.String)
meth public abstract com.amazonaws.services.s3.S3ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getBucketAcl(com.amazonaws.services.s3.model.GetBucketAclRequest)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getBucketAcl(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getObjectAcl(com.amazonaws.services.s3.model.GetObjectAclRequest)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(com.amazonaws.services.s3.model.CreateBucketRequest)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,com.amazonaws.services.s3.model.Region)
 anno 0 java.lang.Deprecated()
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(com.amazonaws.services.s3.model.GetBucketPolicyRequest)
meth public abstract com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.CopyObjectResult copyObject(com.amazonaws.services.s3.model.CopyObjectRequest)
meth public abstract com.amazonaws.services.s3.model.CopyObjectResult copyObject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.DeleteObjectTaggingResult deleteObjectTagging(com.amazonaws.services.s3.model.DeleteObjectTaggingRequest)
meth public abstract com.amazonaws.services.s3.model.DeleteObjectsResult deleteObjects(com.amazonaws.services.s3.model.DeleteObjectsRequest)
meth public abstract com.amazonaws.services.s3.model.DeletePublicAccessBlockResult deletePublicAccessBlock(com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest)
meth public abstract com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(com.amazonaws.services.s3.model.GetBucketEncryptionRequest)
meth public abstract com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.GetBucketPolicyStatusResult getBucketPolicyStatus(com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest)
meth public abstract com.amazonaws.services.s3.model.GetObjectLegalHoldResult getObjectLegalHold(com.amazonaws.services.s3.model.GetObjectLegalHoldRequest)
meth public abstract com.amazonaws.services.s3.model.GetObjectLockConfigurationResult getObjectLockConfiguration(com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.GetObjectRetentionResult getObjectRetention(com.amazonaws.services.s3.model.GetObjectRetentionRequest)
meth public abstract com.amazonaws.services.s3.model.GetObjectTaggingResult getObjectTagging(com.amazonaws.services.s3.model.GetObjectTaggingRequest)
meth public abstract com.amazonaws.services.s3.model.GetPublicAccessBlockResult getPublicAccessBlock(com.amazonaws.services.s3.model.GetPublicAccessBlockRequest)
meth public abstract com.amazonaws.services.s3.model.HeadBucketResult headBucket(com.amazonaws.services.s3.model.HeadBucketRequest)
meth public abstract com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest)
meth public abstract com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest)
meth public abstract com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest)
meth public abstract com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(com.amazonaws.services.s3.model.ListObjectsV2Request)
meth public abstract com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.MultipartUploadListing listMultipartUploads(com.amazonaws.services.s3.model.ListMultipartUploadsRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ObjectListing)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(com.amazonaws.services.s3.model.ListObjectsRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(com.amazonaws.services.s3.model.GetObjectMetadataRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Owner getS3AccountOwner()
meth public abstract com.amazonaws.services.s3.model.Owner getS3AccountOwner(com.amazonaws.services.s3.model.GetS3AccountOwnerRequest)
meth public abstract com.amazonaws.services.s3.model.PartListing listParts(com.amazonaws.services.s3.model.ListPartsRequest)
meth public abstract com.amazonaws.services.s3.model.PresignedUrlDownloadResult download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest)
meth public abstract com.amazonaws.services.s3.model.PresignedUrlUploadResult upload(com.amazonaws.services.s3.model.PresignedUrlUploadRequest)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.File)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Region getRegion()
meth public abstract com.amazonaws.services.s3.model.RestoreObjectResult restoreObjectV2(com.amazonaws.services.s3.model.RestoreObjectRequest)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.SelectObjectContentResult selectObjectContent(com.amazonaws.services.s3.model.SelectObjectContentRequest)
meth public abstract com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(java.lang.String,com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public abstract com.amazonaws.services.s3.model.SetBucketEncryptionResult setBucketEncryption(com.amazonaws.services.s3.model.SetBucketEncryptionRequest)
meth public abstract com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(java.lang.String,com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
meth public abstract com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(java.lang.String,com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
meth public abstract com.amazonaws.services.s3.model.SetObjectLegalHoldResult setObjectLegalHold(com.amazonaws.services.s3.model.SetObjectLegalHoldRequest)
meth public abstract com.amazonaws.services.s3.model.SetObjectLockConfigurationResult setObjectLockConfiguration(com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.SetObjectRetentionResult setObjectRetention(com.amazonaws.services.s3.model.SetObjectRetentionRequest)
meth public abstract com.amazonaws.services.s3.model.SetObjectTaggingResult setObjectTagging(com.amazonaws.services.s3.model.SetObjectTaggingRequest)
meth public abstract com.amazonaws.services.s3.model.SetPublicAccessBlockResult setPublicAccessBlock(com.amazonaws.services.s3.model.SetPublicAccessBlockRequest)
meth public abstract com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public abstract com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest)
meth public abstract com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.VersionListing)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(com.amazonaws.services.s3.model.ListVersionsRequest)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public abstract com.amazonaws.services.s3.waiters.AmazonS3Waiters waiters()
meth public abstract java.lang.String getBucketLocation(com.amazonaws.services.s3.model.GetBucketLocationRequest)
meth public abstract java.lang.String getBucketLocation(java.lang.String)
meth public abstract java.lang.String getObjectAsString(java.lang.String,java.lang.String)
meth public abstract java.lang.String getRegionName()
meth public abstract java.net.URL generatePresignedUrl(com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
meth public abstract java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date)
meth public abstract java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date,com.amazonaws.HttpMethod)
meth public abstract java.net.URL getUrl(java.lang.String,java.lang.String)
meth public abstract java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets()
meth public abstract java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets(com.amazonaws.services.s3.model.ListBucketsRequest)
meth public abstract void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public abstract void changeObjectStorageClass(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.StorageClass)
 anno 0 java.lang.Deprecated()
meth public abstract void deleteBucket(com.amazonaws.services.s3.model.DeleteBucketRequest)
meth public abstract void deleteBucket(java.lang.String)
meth public abstract void deleteBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest)
meth public abstract void deleteBucketCrossOriginConfiguration(java.lang.String)
meth public abstract void deleteBucketLifecycleConfiguration(com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest)
meth public abstract void deleteBucketLifecycleConfiguration(java.lang.String)
meth public abstract void deleteBucketPolicy(com.amazonaws.services.s3.model.DeleteBucketPolicyRequest)
meth public abstract void deleteBucketPolicy(java.lang.String)
meth public abstract void deleteBucketReplicationConfiguration(com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest)
meth public abstract void deleteBucketReplicationConfiguration(java.lang.String)
meth public abstract void deleteBucketTaggingConfiguration(com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest)
meth public abstract void deleteBucketTaggingConfiguration(java.lang.String)
meth public abstract void deleteBucketWebsiteConfiguration(com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest)
meth public abstract void deleteBucketWebsiteConfiguration(java.lang.String)
meth public abstract void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public abstract void deleteObject(java.lang.String,java.lang.String)
meth public abstract void deleteVersion(com.amazonaws.services.s3.model.DeleteVersionRequest)
meth public abstract void deleteVersion(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void disableRequesterPays(java.lang.String)
meth public abstract void download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest,java.io.File)
meth public abstract void enableRequesterPays(java.lang.String)
meth public abstract void restoreObject(com.amazonaws.services.s3.model.RestoreObjectRequest)
 anno 0 java.lang.Deprecated()
meth public abstract void restoreObject(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public abstract void setBucketAccelerateConfiguration(com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest)
meth public abstract void setBucketAccelerateConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public abstract void setBucketAcl(com.amazonaws.services.s3.model.SetBucketAclRequest)
meth public abstract void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public abstract void setBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest)
meth public abstract void setBucketCrossOriginConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
meth public abstract void setBucketLifecycleConfiguration(com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest)
meth public abstract void setBucketLifecycleConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
meth public abstract void setBucketLoggingConfiguration(com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest)
meth public abstract void setBucketNotificationConfiguration(com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest)
meth public abstract void setBucketNotificationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public abstract void setBucketPolicy(com.amazonaws.services.s3.model.SetBucketPolicyRequest)
meth public abstract void setBucketPolicy(java.lang.String,java.lang.String)
meth public abstract void setBucketReplicationConfiguration(com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest)
meth public abstract void setBucketReplicationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketReplicationConfiguration)
meth public abstract void setBucketTaggingConfiguration(com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest)
meth public abstract void setBucketTaggingConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketTaggingConfiguration)
meth public abstract void setBucketVersioningConfiguration(com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest)
meth public abstract void setBucketWebsiteConfiguration(com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest)
meth public abstract void setBucketWebsiteConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public abstract void setEndpoint(java.lang.String)
meth public abstract void setObjectAcl(com.amazonaws.services.s3.model.SetObjectAclRequest)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public abstract void setObjectRedirectLocation(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract void setRegion(com.amazonaws.regions.Region)
meth public abstract void setS3ClientOptions(com.amazonaws.services.s3.S3ClientOptions)
meth public abstract void shutdown()

CLSS public abstract com.amazonaws.services.s3.AmazonS3Builder<%0 extends com.amazonaws.services.s3.AmazonS3Builder, %1 extends com.amazonaws.services.s3.AmazonS3>
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons protected init()
fld protected final com.amazonaws.internal.SdkFunction<com.amazonaws.services.s3.AmazonS3ClientParamsWrapper,com.amazonaws.services.s3.AmazonS3> clientFactory
meth protected com.amazonaws.services.s3.S3ClientOptions resolveS3ClientOptions()
meth public java.lang.Boolean isAccelerateModeEnabled()
meth public java.lang.Boolean isChunkedEncodingDisabled()
meth public java.lang.Boolean isDualstackEnabled()
meth public java.lang.Boolean isForceGlobalBucketAccessEnabled()
meth public java.lang.Boolean isPathStyleAccessEnabled()
meth public java.lang.Boolean isPayloadSigningEnabled()
meth public void setAccelerateModeEnabled(java.lang.Boolean)
meth public void setChunkedEncodingDisabled(java.lang.Boolean)
meth public void setDualstackEnabled(java.lang.Boolean)
meth public void setForceGlobalBucketAccessEnabled(java.lang.Boolean)
meth public void setPathStyleAccessEnabled(java.lang.Boolean)
meth public void setPayloadSigningEnabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} disableChunkedEncoding()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} enableAccelerateMode()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} enableDualstack()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} enableForceGlobalBucketAccess()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} enablePathStyleAccess()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} enablePayloadSigning()
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withAccelerateModeEnabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withChunkedEncodingDisabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withDualstackEnabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withForceGlobalBucketAccessEnabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withPathStyleAccessEnabled(java.lang.Boolean)
meth public {com.amazonaws.services.s3.AmazonS3Builder%0} withPayloadSigningEnabled(java.lang.Boolean)
supr com.amazonaws.client.builder.AwsSyncClientBuilder<{com.amazonaws.services.s3.AmazonS3Builder%0},{com.amazonaws.services.s3.AmazonS3Builder%1}>
hfds CLIENT_CONFIG_FACTORY,DEFAULT_CLIENT_FACTORY,accelerateModeEnabled,chunkedEncodingDisabled,dualstackEnabled,forceGlobalBucketAccessEnabled,pathStyleAccessEnabled,payloadSigningEnabled

CLSS public com.amazonaws.services.s3.AmazonS3Client
 anno 0 com.amazonaws.annotation.ThreadSafe()
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.metrics.RequestMetricCollector)
 anno 0 java.lang.Deprecated()
fld protected final com.amazonaws.auth.AWSCredentialsProvider awsCredentialsProvider
fld protected final com.amazonaws.services.s3.internal.S3ErrorResponseHandler errorResponseHandler
fld protected final static com.amazonaws.ClientConfigurationFactory configFactory
fld public final static java.lang.String S3_SERVICE_NAME = "s3"
intf com.amazonaws.services.s3.AmazonS3
meth protected <%0 extends com.amazonaws.AmazonWebServiceRequest> com.amazonaws.Request<{%%0}> createRequest(java.lang.String,java.lang.String,{%%0},com.amazonaws.http.HttpMethodName)
meth protected <%0 extends com.amazonaws.AmazonWebServiceRequest> com.amazonaws.Request<{%%0}> createRequest(java.lang.String,java.lang.String,{%%0},com.amazonaws.http.HttpMethodName,java.net.URI)
meth protected <%0 extends java.lang.Object> void presignRequest(com.amazonaws.Request<{%%0}>,com.amazonaws.HttpMethod,java.lang.String,java.lang.String,java.util.Date,java.lang.String)
meth protected boolean useStrictHostNameVerification()
meth protected com.amazonaws.auth.Signer createSigner(com.amazonaws.Request<?>,java.lang.String,java.lang.String)
meth protected com.amazonaws.auth.Signer createSigner(com.amazonaws.Request<?>,java.lang.String,java.lang.String,boolean)
meth protected final com.amazonaws.internal.auth.SignerProvider createSignerProvider(com.amazonaws.auth.Signer)
meth protected final com.amazonaws.services.s3.model.InitiateMultipartUploadRequest newInitiateMultipartUploadRequest(com.amazonaws.services.s3.model.UploadObjectRequest)
meth protected static void populateRequestMetadata(com.amazonaws.Request<?>,com.amazonaws.services.s3.model.ObjectMetadata)
meth protected static void populateRequesterPaysHeader(com.amazonaws.Request<?>,boolean)
meth public boolean doesBucketExist(java.lang.String)
meth public boolean doesBucketExistV2(java.lang.String)
meth public boolean doesObjectExist(java.lang.String,java.lang.String)
meth public boolean isRequesterPaysEnabled(java.lang.String)
meth public com.amazonaws.services.s3.S3ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getBucketAcl(com.amazonaws.services.s3.model.GetBucketAclRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getBucketAcl(java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(com.amazonaws.services.s3.model.GetObjectAclRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(com.amazonaws.services.s3.model.CreateBucketRequest)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,com.amazonaws.services.s3.model.Region)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration getBucketAccelerateConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration getBucketLifecycleConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(com.amazonaws.services.s3.model.GetBucketPolicyRequest)
meth public com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration getBucketReplicationConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketTaggingConfiguration getBucketTaggingConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(com.amazonaws.services.s3.model.CopyObjectRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult deleteBucketEncryption(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingResult deleteObjectTagging(com.amazonaws.services.s3.model.DeleteObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.DeleteObjectsResult deleteObjects(com.amazonaws.services.s3.model.DeleteObjectsRequest)
meth public com.amazonaws.services.s3.model.DeletePublicAccessBlockResult deletePublicAccessBlock(com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(com.amazonaws.services.s3.model.GetBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult getBucketEncryption(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusResult getBucketPolicyStatus(com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest)
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldResult getObjectLegalHold(com.amazonaws.services.s3.model.GetObjectLegalHoldRequest)
meth public com.amazonaws.services.s3.model.GetObjectLockConfigurationResult getObjectLockConfiguration(com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest)
meth public com.amazonaws.services.s3.model.GetObjectRetentionResult getObjectRetention(com.amazonaws.services.s3.model.GetObjectRetentionRequest)
meth public com.amazonaws.services.s3.model.GetObjectTaggingResult getObjectTagging(com.amazonaws.services.s3.model.GetObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockResult getPublicAccessBlock(com.amazonaws.services.s3.model.GetPublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.HeadBucketResult headBucket(com.amazonaws.services.s3.model.HeadBucketRequest)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(com.amazonaws.services.s3.model.ListObjectsV2Request)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Result listObjectsV2(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.MultipartUploadListing listMultipartUploads(com.amazonaws.services.s3.model.ListMultipartUploadsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ObjectListing)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(com.amazonaws.services.s3.model.ListObjectsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(com.amazonaws.services.s3.model.GetObjectMetadataRequest)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Owner getS3AccountOwner()
meth public com.amazonaws.services.s3.model.Owner getS3AccountOwner(com.amazonaws.services.s3.model.GetS3AccountOwnerRequest)
meth public com.amazonaws.services.s3.model.PartListing listParts(com.amazonaws.services.s3.model.ListPartsRequest)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadResult download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadResult upload(com.amazonaws.services.s3.model.PresignedUrlUploadRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Region getRegion()
meth public com.amazonaws.services.s3.model.RestoreObjectResult restoreObjectV2(com.amazonaws.services.s3.model.RestoreObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentResult selectObjectContent(com.amazonaws.services.s3.model.SelectObjectContentRequest)
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(java.lang.String,com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketEncryptionResult setBucketEncryption(com.amazonaws.services.s3.model.SetBucketEncryptionRequest)
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(java.lang.String,com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(java.lang.String,com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldResult setObjectLegalHold(com.amazonaws.services.s3.model.SetObjectLegalHoldRequest)
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationResult setObjectLockConfiguration(com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest)
meth public com.amazonaws.services.s3.model.SetObjectRetentionResult setObjectRetention(com.amazonaws.services.s3.model.SetObjectRetentionRequest)
meth public com.amazonaws.services.s3.model.SetObjectTaggingResult setObjectTagging(com.amazonaws.services.s3.model.SetObjectTaggingRequest)
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockResult setPublicAccessBlock(com.amazonaws.services.s3.model.SetPublicAccessBlockRequest)
meth public com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest)
meth public com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.VersionListing)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(com.amazonaws.services.s3.model.ListVersionsRequest)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public com.amazonaws.services.s3.waiters.AmazonS3Waiters waiters()
meth public java.lang.String getBucketLocation(com.amazonaws.services.s3.model.GetBucketLocationRequest)
meth public java.lang.String getBucketLocation(java.lang.String)
meth public java.lang.String getObjectAsString(java.lang.String,java.lang.String)
meth public java.lang.String getRegionName()
meth public java.lang.String getResourceUrl(java.lang.String,java.lang.String)
meth public java.net.URL generatePresignedUrl(com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date,com.amazonaws.HttpMethod)
meth public java.net.URL getUrl(java.lang.String,java.lang.String)
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets()
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets(com.amazonaws.services.s3.model.ListBucketsRequest)
meth public static com.amazonaws.services.s3.AmazonS3ClientBuilder builder()
meth public void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public void changeObjectStorageClass(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.StorageClass)
meth public void deleteBucket(com.amazonaws.services.s3.model.DeleteBucketRequest)
meth public void deleteBucket(java.lang.String)
meth public void deleteBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest)
meth public void deleteBucketCrossOriginConfiguration(java.lang.String)
meth public void deleteBucketLifecycleConfiguration(com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest)
meth public void deleteBucketLifecycleConfiguration(java.lang.String)
meth public void deleteBucketPolicy(com.amazonaws.services.s3.model.DeleteBucketPolicyRequest)
meth public void deleteBucketPolicy(java.lang.String)
meth public void deleteBucketReplicationConfiguration(com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest)
meth public void deleteBucketReplicationConfiguration(java.lang.String)
meth public void deleteBucketTaggingConfiguration(com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest)
meth public void deleteBucketTaggingConfiguration(java.lang.String)
meth public void deleteBucketWebsiteConfiguration(com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest)
meth public void deleteBucketWebsiteConfiguration(java.lang.String)
meth public void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public void deleteObject(java.lang.String,java.lang.String)
meth public void deleteVersion(com.amazonaws.services.s3.model.DeleteVersionRequest)
meth public void deleteVersion(java.lang.String,java.lang.String,java.lang.String)
meth public void disableRequesterPays(java.lang.String)
meth public void download(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest,java.io.File)
meth public void enableRequesterPays(java.lang.String)
meth public void restoreObject(com.amazonaws.services.s3.model.RestoreObjectRequest)
meth public void restoreObject(java.lang.String,java.lang.String,int)
meth public void setBucketAccelerateConfiguration(com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest)
meth public void setBucketAccelerateConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public void setBucketAcl(com.amazonaws.services.s3.model.SetBucketAclRequest)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList,com.amazonaws.metrics.RequestMetricCollector)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList,com.amazonaws.metrics.RequestMetricCollector)
meth public void setBucketCrossOriginConfiguration(com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest)
meth public void setBucketCrossOriginConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
meth public void setBucketLifecycleConfiguration(com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest)
meth public void setBucketLifecycleConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
meth public void setBucketLoggingConfiguration(com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest)
meth public void setBucketNotificationConfiguration(com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest)
meth public void setBucketNotificationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public void setBucketPolicy(com.amazonaws.services.s3.model.SetBucketPolicyRequest)
meth public void setBucketPolicy(java.lang.String,java.lang.String)
meth public void setBucketReplicationConfiguration(com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest)
meth public void setBucketReplicationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketReplicationConfiguration)
meth public void setBucketTaggingConfiguration(com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest)
meth public void setBucketTaggingConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketTaggingConfiguration)
meth public void setBucketVersioningConfiguration(com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest)
meth public void setBucketWebsiteConfiguration(com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest)
meth public void setBucketWebsiteConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public void setEndpoint(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setObjectAcl(com.amazonaws.services.s3.model.SetObjectAclRequest)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList,com.amazonaws.metrics.RequestMetricCollector)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList,com.amazonaws.metrics.RequestMetricCollector)
meth public void setObjectRedirectLocation(java.lang.String,java.lang.String,java.lang.String)
meth public void setRegion(com.amazonaws.regions.Region)
 anno 0 java.lang.Deprecated()
meth public void setS3ClientOptions(com.amazonaws.services.s3.S3ClientOptions)
supr com.amazonaws.AmazonWebServiceClient
hfds BUCKET_REGION_CACHE_SIZE,S3_SIGNER,S3_V4_SIGNER,SERVICE_ID,bucketConfigurationXmlFactory,bucketRegionCache,clientOptions,clientRegion,completeMultipartUploadRetryCondition,log,requestPaymentConfigurationXmlFactory,skipMd5CheckStrategy,voidResponseHandler,waiters
hcls PresignedUrlUploadStrategy,PutObjectStrategy

CLSS public final com.amazonaws.services.s3.AmazonS3ClientBuilder
 anno 0 com.amazonaws.annotation.NotThreadSafe()
meth protected com.amazonaws.services.s3.AmazonS3 build(com.amazonaws.client.AwsSyncClientParams)
meth public static com.amazonaws.services.s3.AmazonS3 defaultClient()
meth public static com.amazonaws.services.s3.AmazonS3ClientBuilder standard()
supr com.amazonaws.services.s3.AmazonS3Builder<com.amazonaws.services.s3.AmazonS3ClientBuilder,com.amazonaws.services.s3.AmazonS3>

CLSS public abstract interface com.amazonaws.services.s3.AmazonS3Encryption
intf com.amazonaws.services.s3.AmazonS3

CLSS public com.amazonaws.services.s3.AmazonS3EncryptionClient
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.services.s3.model.EncryptionMaterialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration,com.amazonaws.metrics.RequestMetricCollector)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.kms.AWSKMSClient,com.amazonaws.auth.AWSCredentialsProvider,com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration,com.amazonaws.metrics.RequestMetricCollector)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.model.EncryptionMaterials)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.model.EncryptionMaterialsProvider)
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.model.EncryptionMaterialsProvider,com.amazonaws.services.s3.model.CryptoConfiguration)
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String USER_AGENT
intf com.amazonaws.services.s3.AmazonS3Encryption
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult uploadObject(com.amazonaws.services.s3.model.UploadObjectRequest) throws java.io.IOException,java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectResult putInstructionFile(com.amazonaws.services.s3.model.PutInstructionFileRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public static com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder encryptionBuilder()
meth public void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public void shutdown()
supr com.amazonaws.services.s3.AmazonS3Client
hfds crypto,isKMSClientInternal,kms
hcls S3DirectImpl

CLSS public final com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder
cons public init()
meth protected com.amazonaws.services.s3.AmazonS3Encryption build(com.amazonaws.client.AwsSyncClientParams)
meth public com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder withCryptoConfiguration(com.amazonaws.services.s3.model.CryptoConfiguration)
meth public com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder withEncryptionMaterials(com.amazonaws.services.s3.model.EncryptionMaterialsProvider)
meth public com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder withKmsClient(com.amazonaws.services.kms.AWSKMS)
meth public static com.amazonaws.services.s3.AmazonS3Encryption defaultClient()
meth public static com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder standard()
meth public void setCryptoConfiguration(com.amazonaws.services.s3.model.CryptoConfiguration)
meth public void setEncryptionMaterials(com.amazonaws.services.s3.model.EncryptionMaterialsProvider)
meth public void setKms(com.amazonaws.services.kms.AWSKMS)
supr com.amazonaws.services.s3.AmazonS3Builder<com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder,com.amazonaws.services.s3.AmazonS3Encryption>
hfds cryptoConfig,encryptionMaterials,kms

CLSS public final com.amazonaws.services.s3.AmazonS3EncryptionClientParamsWrapper
 anno 0 com.amazonaws.annotation.Immutable()
meth public com.amazonaws.client.AwsSyncClientParams getClientParams()
meth public com.amazonaws.services.s3.S3ClientOptions getS3ClientOptions()
supr java.lang.Object
hfds cryptoConfiguration,encryptionMaterials,getClientParams,getS3ClientOptions,kms

CLSS public com.amazonaws.services.s3.AmazonS3URI
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.net.URI)
meth public boolean equals(java.lang.Object)
meth public boolean isPathStyle()
meth public int hashCode()
meth public java.lang.String getBucket()
meth public java.lang.String getKey()
meth public java.lang.String getRegion()
meth public java.lang.String getVersionId()
meth public java.lang.String toString()
meth public java.net.URI getURI()
supr java.lang.Object
hfds ENDPOINT_PATTERN,VERSION_ID_PATTERN,bucket,isPathStyle,key,region,uri,versionId

CLSS public abstract interface com.amazonaws.services.s3.Headers
fld public final static java.lang.String ABORT_DATE = "x-amz-abort-date"
fld public final static java.lang.String ABORT_RULE_ID = "x-amz-abort-rule-id"
fld public final static java.lang.String AMAZON_PREFIX = "x-amz-"
fld public final static java.lang.String BYPASS_GOVERNANCE_RETENTION = "x-amz-bypass-governance-retention"
fld public final static java.lang.String CACHE_CONTROL = "Cache-Control"
fld public final static java.lang.String CLOUD_FRONT_ID = "X-Amz-Cf-Id"
fld public final static java.lang.String CONNECTION = "Connection"
fld public final static java.lang.String CONTENT_DISPOSITION = "Content-Disposition"
fld public final static java.lang.String CONTENT_ENCODING = "Content-Encoding"
fld public final static java.lang.String CONTENT_LANGUAGE = "Content-Language"
fld public final static java.lang.String CONTENT_LENGTH = "Content-Length"
fld public final static java.lang.String CONTENT_MD5 = "Content-MD5"
fld public final static java.lang.String CONTENT_RANGE = "Content-Range"
fld public final static java.lang.String CONTENT_TYPE = "Content-Type"
fld public final static java.lang.String COPY_PART_RANGE = "x-amz-copy-source-range"
fld public final static java.lang.String COPY_SOURCE_IF_MATCH = "x-amz-copy-source-if-match"
fld public final static java.lang.String COPY_SOURCE_IF_MODIFIED_SINCE = "x-amz-copy-source-if-modified-since"
fld public final static java.lang.String COPY_SOURCE_IF_NO_MATCH = "x-amz-copy-source-if-none-match"
fld public final static java.lang.String COPY_SOURCE_IF_UNMODIFIED_SINCE = "x-amz-copy-source-if-unmodified-since"
fld public final static java.lang.String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM = "x-amz-copy-source-server-side-encryption-customer-algorithm"
fld public final static java.lang.String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY = "x-amz-copy-source-server-side-encryption-customer-key"
fld public final static java.lang.String COPY_SOURCE_SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5 = "x-amz-copy-source-server-side-encryption-customer-key-MD5"
fld public final static java.lang.String CRYPTO_CEK_ALGORITHM = "x-amz-cek-alg"
fld public final static java.lang.String CRYPTO_INSTRUCTION_FILE = "x-amz-crypto-instr-file"
fld public final static java.lang.String CRYPTO_IV = "x-amz-iv"
fld public final static java.lang.String CRYPTO_KEY = "x-amz-key"
fld public final static java.lang.String CRYPTO_KEYWRAP_ALGORITHM = "x-amz-wrap-alg"
fld public final static java.lang.String CRYPTO_KEY_V2 = "x-amz-key-v2"
fld public final static java.lang.String CRYPTO_TAG_LENGTH = "x-amz-tag-len"
fld public final static java.lang.String DATE = "Date"
fld public final static java.lang.String ETAG = "ETag"
fld public final static java.lang.String EXPIRATION = "x-amz-expiration"
fld public final static java.lang.String EXPIRES = "Expires"
fld public final static java.lang.String EXTENDED_REQUEST_ID = "x-amz-id-2"
fld public final static java.lang.String GET_OBJECT_IF_MATCH = "If-Match"
fld public final static java.lang.String GET_OBJECT_IF_MODIFIED_SINCE = "If-Modified-Since"
fld public final static java.lang.String GET_OBJECT_IF_NONE_MATCH = "If-None-Match"
fld public final static java.lang.String GET_OBJECT_IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
fld public final static java.lang.String LAST_MODIFIED = "Last-Modified"
fld public final static java.lang.String MATERIALS_DESCRIPTION = "x-amz-matdesc"
fld public final static java.lang.String METADATA_DIRECTIVE = "x-amz-metadata-directive"
fld public final static java.lang.String OBJECT_LOCK_ENABLED_FOR_BUCKET = "x-amz-bucket-object-lock-enabled"
fld public final static java.lang.String OBJECT_LOCK_LEGAL_HOLD_STATUS = "x-amz-object-lock-legal-hold"
fld public final static java.lang.String OBJECT_LOCK_MODE = "x-amz-object-lock-mode"
fld public final static java.lang.String OBJECT_LOCK_RETAIN_UNTIL_DATE = "x-amz-object-lock-retain-until-date"
fld public final static java.lang.String OBJECT_LOCK_TOKEN = "x-amz-bucket-object-lock-token"
fld public final static java.lang.String OBJECT_REPLICATION_STATUS = "x-amz-replication-status"
fld public final static java.lang.String RANGE = "Range"
fld public final static java.lang.String REDIRECT_LOCATION = "x-amz-website-redirect-location"
fld public final static java.lang.String REMOVE_SELF_BUCKET_ACCESS = "x-amz-confirm-remove-self-bucket-access"
fld public final static java.lang.String REQUESTER_CHARGED_HEADER = "x-amz-request-charged"
fld public final static java.lang.String REQUESTER_PAYS_HEADER = "x-amz-request-payer"
fld public final static java.lang.String REQUEST_ID = "x-amz-request-id"
fld public final static java.lang.String RESTORE = "x-amz-restore"
fld public final static java.lang.String S3_ALTERNATE_DATE = "x-amz-date"
fld public final static java.lang.String S3_BUCKET_REGION = "x-amz-bucket-region"
fld public final static java.lang.String S3_CANNED_ACL = "x-amz-acl"
fld public final static java.lang.String S3_MFA = "x-amz-mfa"
fld public final static java.lang.String S3_PARTS_COUNT = "x-amz-mp-parts-count"
fld public final static java.lang.String S3_RESTORE_OUTPUT_PATH = "x-amz-restore-output-path"
fld public final static java.lang.String S3_SERVING_REGION = "x-amz-region"
fld public final static java.lang.String S3_TAGGING = "x-amz-tagging"
fld public final static java.lang.String S3_TAGGING_COUNT = "x-amz-tagging-count"
fld public final static java.lang.String S3_USER_METADATA_PREFIX = "x-amz-meta-"
fld public final static java.lang.String S3_VERSION_ID = "x-amz-version-id"
fld public final static java.lang.String SECURITY_TOKEN = "x-amz-security-token"
fld public final static java.lang.String SERVER = "Server"
fld public final static java.lang.String SERVER_SIDE_ENCRYPTION = "x-amz-server-side-encryption"
fld public final static java.lang.String SERVER_SIDE_ENCRYPTION_AWS_KMS_KEYID = "x-amz-server-side-encryption-aws-kms-key-id"
fld public final static java.lang.String SERVER_SIDE_ENCRYPTION_CUSTOMER_ALGORITHM = "x-amz-server-side-encryption-customer-algorithm"
fld public final static java.lang.String SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY = "x-amz-server-side-encryption-customer-key"
fld public final static java.lang.String SERVER_SIDE_ENCRYPTION_CUSTOMER_KEY_MD5 = "x-amz-server-side-encryption-customer-key-MD5"
fld public final static java.lang.String STORAGE_CLASS = "x-amz-storage-class"
fld public final static java.lang.String TAGGING_DIRECTIVE = "x-amz-tagging-directive"
fld public final static java.lang.String UNENCRYPTED_CONTENT_LENGTH = "x-amz-unencrypted-content-length"
fld public final static java.lang.String UNENCRYPTED_CONTENT_MD5 = "x-amz-unencrypted-content-md5"

CLSS public com.amazonaws.services.s3.KeyWrapException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.SecurityException
hfds serialVersionUID

CLSS public abstract interface com.amazonaws.services.s3.OnFileDelete
meth public abstract void onFileDelete(com.amazonaws.services.s3.internal.FileDeletionEvent)

CLSS public com.amazonaws.services.s3.S3ClientOptions
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.S3ClientOptions)
 anno 0 java.lang.Deprecated()
fld public final static boolean DEFAULT_ACCELERATE_MODE_ENABLED = false
fld public final static boolean DEFAULT_CHUNKED_ENCODING_DISABLED = false
fld public final static boolean DEFAULT_DUALSTACK_ENABLED = false
fld public final static boolean DEFAULT_FORCE_GLOBAL_BUCKET_ACCESS_ENABLED = false
fld public final static boolean DEFAULT_PATH_STYLE_ACCESS = false
fld public final static boolean DEFAULT_PAYLOAD_SIGNING_ENABLED = false
innr public static Builder
meth public boolean isAccelerateModeEnabled()
meth public boolean isChunkedEncodingDisabled()
meth public boolean isDualstackEnabled()
meth public boolean isForceGlobalBucketAccessEnabled()
meth public boolean isPathStyleAccess()
meth public boolean isPayloadSigningEnabled()
meth public com.amazonaws.services.s3.S3ClientOptions disableChunkedEncoding()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.S3ClientOptions withChunkedEncodingDisabled(boolean)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.S3ClientOptions withPathStyleAccess(boolean)
 anno 0 java.lang.Deprecated()
meth public static com.amazonaws.services.s3.S3ClientOptions$Builder builder()
meth public void setChunkedEncodingDisabled(boolean)
 anno 0 java.lang.Deprecated()
meth public void setPathStyleAccess(boolean)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds accelerateModeEnabled,chunkedEncodingDisabled,dualstackEnabled,forceGlobalBucketAccessEnabled,pathStyleAccess,payloadSigningEnabled

CLSS public static com.amazonaws.services.s3.S3ClientOptions$Builder
 outer com.amazonaws.services.s3.S3ClientOptions
meth public com.amazonaws.services.s3.S3ClientOptions build()
meth public com.amazonaws.services.s3.S3ClientOptions$Builder disableChunkedEncoding()
meth public com.amazonaws.services.s3.S3ClientOptions$Builder enableDualstack()
meth public com.amazonaws.services.s3.S3ClientOptions$Builder enableForceGlobalBucketAccess()
meth public com.amazonaws.services.s3.S3ClientOptions$Builder setAccelerateModeEnabled(boolean)
meth public com.amazonaws.services.s3.S3ClientOptions$Builder setPathStyleAccess(boolean)
meth public com.amazonaws.services.s3.S3ClientOptions$Builder setPayloadSigningEnabled(boolean)
supr java.lang.Object
hfds accelerateModeEnabled,chunkedEncodingDisabled,dualstackEnabled,forceGlobalBucketAccessEnabled,pathStyleAccess,payloadSigningEnabled

CLSS public com.amazonaws.services.s3.S3ResponseMetadata
cons public init(com.amazonaws.ResponseMetadata)
cons public init(java.util.Map<java.lang.String,java.lang.String>)
fld public final static java.lang.String CLOUD_FRONT_ID = "CLOUD_FRONT_ID"
fld public final static java.lang.String HOST_ID = "HOST_ID"
meth public java.lang.String getCloudFrontId()
meth public java.lang.String getHostId()
supr com.amazonaws.ResponseMetadata

CLSS public com.amazonaws.services.s3.UploadObjectObserver
cons public init()
meth protected <%0 extends com.amazonaws.AmazonWebServiceRequest> {%%0} appendUserAgent({%%0},java.lang.String)
meth protected com.amazonaws.services.s3.AmazonS3 getAmazonS3()
meth protected com.amazonaws.services.s3.internal.S3DirectSpi getS3DirectSpi()
meth protected com.amazonaws.services.s3.model.InitiateMultipartUploadRequest newInitiateMultipartUploadRequest(com.amazonaws.services.s3.model.UploadObjectRequest)
meth protected com.amazonaws.services.s3.model.UploadObjectRequest getRequest()
meth protected com.amazonaws.services.s3.model.UploadPartRequest newUploadPartRequest(com.amazonaws.services.s3.internal.PartCreationEvent,java.io.File)
meth protected com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth protected java.lang.String getUploadId()
meth protected java.util.concurrent.ExecutorService getExecutorService()
meth public com.amazonaws.services.s3.UploadObjectObserver init(com.amazonaws.services.s3.model.UploadObjectRequest,com.amazonaws.services.s3.internal.S3DirectSpi,com.amazonaws.services.s3.AmazonS3,java.util.concurrent.ExecutorService)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult onCompletion(java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public java.lang.String onUploadInitiation(com.amazonaws.services.s3.model.UploadObjectRequest)
meth public java.util.List<java.util.concurrent.Future<com.amazonaws.services.s3.model.UploadPartResult>> getFutures()
meth public void onAbort()
meth public void onPartCreate(com.amazonaws.services.s3.internal.PartCreationEvent)
supr java.lang.Object
hfds es,futures,req,s3,s3direct,uploadId

CLSS public com.amazonaws.services.s3.internal.AWSS3V4Signer
cons public init()
meth protected java.lang.String calculateContentHash(com.amazonaws.SignableRequest<?>)
meth protected java.lang.String calculateContentHashPresign(com.amazonaws.SignableRequest<?>)
meth protected void processRequestPayload(com.amazonaws.SignableRequest<?>,byte[],byte[],com.amazonaws.auth.internal.AWS4SignerRequestParams)
supr com.amazonaws.auth.AWS4Signer
hfds CONTENT_SHA_256,UNSIGNED_PAYLOAD

CLSS public abstract com.amazonaws.services.s3.internal.AbstractRepeatableCipherInputStream<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
cons protected init(java.io.InputStream,java.io.FilterInputStream,{com.amazonaws.services.s3.internal.AbstractRepeatableCipherInputStream%0})
meth protected abstract java.io.FilterInputStream createCipherInputStream(java.io.InputStream,{com.amazonaws.services.s3.internal.AbstractRepeatableCipherInputStream%0})
meth public boolean markSupported()
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds cipherFactory,hasBeenAccessed,unencryptedDataStream

CLSS public abstract com.amazonaws.services.s3.internal.AbstractS3ResponseHandler<%0 extends java.lang.Object>
cons public init()
intf com.amazonaws.http.HttpResponseHandler<com.amazonaws.AmazonWebServiceResponse<{com.amazonaws.services.s3.internal.AbstractS3ResponseHandler%0}>>
meth protected com.amazonaws.AmazonWebServiceResponse<{com.amazonaws.services.s3.internal.AbstractS3ResponseHandler%0}> parseResponseMetadata(com.amazonaws.http.HttpResponse)
meth protected void populateObjectMetadata(com.amazonaws.http.HttpResponse,com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean needsConnectionLeftOpen()
supr java.lang.Object
hfds ignoredHeaders,log

CLSS public com.amazonaws.services.s3.internal.AmazonS3ExceptionBuilder
cons public init()
meth public com.amazonaws.services.s3.model.AmazonS3Exception build()
meth public int getStatusCode()
meth public java.lang.String getCloudFrontId()
meth public java.lang.String getErrorCode()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getErrorResponseXml()
meth public java.lang.String getExtendedRequestId()
meth public java.lang.String getRequestId()
meth public java.util.Map<java.lang.String,java.lang.String> getAdditionalDetails()
meth public void addAdditionalDetail(java.lang.String,java.lang.String)
meth public void setAdditionalDetails(java.util.Map<java.lang.String,java.lang.String>)
meth public void setCloudFrontId(java.lang.String)
meth public void setErrorCode(java.lang.String)
meth public void setErrorMessage(java.lang.String)
meth public void setErrorResponseXml(java.lang.String)
meth public void setExtendedRequestId(java.lang.String)
meth public void setRequestId(java.lang.String)
meth public void setStatusCode(int)
supr java.lang.Object
hfds additionalDetails,cloudFrontId,errorCode,errorMessage,errorResponseXml,extendedRequestId,requestId,statusCode

CLSS public final !enum com.amazonaws.services.s3.internal.BucketNameUtils
meth public static boolean isDNSBucketName(java.lang.String)
meth public static boolean isValidV2BucketName(java.lang.String)
meth public static com.amazonaws.services.s3.internal.BucketNameUtils valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.internal.BucketNameUtils[] values()
meth public static void validateBucketName(java.lang.String)
supr java.lang.Enum<com.amazonaws.services.s3.internal.BucketNameUtils>
hfds MAX_BUCKET_NAME_LENGTH,MIN_BUCKET_NAME_LENGTH,ipAddressPattern

CLSS public com.amazonaws.services.s3.internal.CompleteMultipartUploadRetryCondition
cons public init()
intf com.amazonaws.retry.RetryPolicy$RetryCondition
meth public boolean shouldRetry(com.amazonaws.AmazonWebServiceRequest,com.amazonaws.AmazonClientException,int)
supr java.lang.Object
hfds MAX_RETRY_ATTEMPTS,completeMultipartRetryablePredicate,maxCompleteMultipartUploadRetries

CLSS public com.amazonaws.services.s3.internal.CompleteMultipartUploadRetryablePredicate
cons public init()
meth public boolean test(com.amazonaws.services.s3.model.AmazonS3Exception)
supr com.amazonaws.internal.SdkPredicate<com.amazonaws.services.s3.model.AmazonS3Exception>
hfds ERROR_CODE,RETYABLE_ERROR_MESSAGE

CLSS public com.amazonaws.services.s3.internal.Constants
cons public init()
fld public final static int BUCKET_ACCESS_FORBIDDEN_STATUS_CODE = 403
fld public final static int BUCKET_REDIRECT_STATUS_CODE = 301
fld public final static int DEFAULT_STREAM_BUFFER_SIZE = 131073
fld public final static int FAILED_PRECONDITION_STATUS_CODE = 412
fld public final static int KB = 1024
fld public final static int MAXIMUM_UPLOAD_PARTS = 10000
fld public final static int MB = 1048576
fld public final static int NO_SUCH_BUCKET_STATUS_CODE = 404
fld public final static java.lang.String DEFAULT_ENCODING = "UTF-8"
fld public final static java.lang.String HMAC_SHA1_ALGORITHM = "HmacSHA1"
fld public final static java.lang.String NULL_VERSION_ID = "null"
fld public final static java.lang.String REQUESTER_PAYS = "requester"
fld public final static java.lang.String S3_ACCELERATE_DUALSTACK_HOSTNAME = "s3-accelerate.dualstack.amazonaws.com"
fld public final static java.lang.String S3_ACCELERATE_HOSTNAME = "s3-accelerate.amazonaws.com"
fld public final static java.lang.String S3_DUALSTACK_QUALIFIER = "dualstack"
fld public final static java.lang.String S3_EXTERNAL_1_HOSTNAME = "s3-external-1.amazonaws.com"
fld public final static java.lang.String S3_HOSTNAME = "s3.amazonaws.com"
fld public final static java.lang.String S3_SERVICE_DISPLAY_NAME = "Amazon S3"
fld public final static java.lang.String SSE_AWS_KMS_ENCRYPTION_SCHEME
fld public final static java.lang.String URL_ENCODING = "url"
fld public final static java.lang.String XML_NAMESPACE = "http://s3.amazonaws.com/doc/2006-03-01/"
fld public final static long GB = 1073741824
meth public static int getStreamBufferSize()
 anno 0 java.lang.Deprecated()
meth public static java.lang.Integer getS3StreamBufferSize()
supr java.lang.Object
hfds log

CLSS public com.amazonaws.services.s3.internal.DeleteObjectTaggingHeaderHandler
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<com.amazonaws.services.s3.model.DeleteObjectTaggingResult>
meth public void handle(com.amazonaws.services.s3.model.DeleteObjectTaggingResult,com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.internal.DeleteObjectsResponse
cons public init()
cons public init(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject>,java.util.List<com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError>)
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
meth public boolean isRequesterCharged()
meth public java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject> getDeletedObjects()
meth public java.util.List<com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError> getErrors()
meth public void setDeletedObjects(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject>)
meth public void setErrors(java.util.List<com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError>)
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds deletedObjects,errors,isRequesterCharged

CLSS public com.amazonaws.services.s3.internal.DigestValidationInputStream
cons public init(java.io.InputStream,java.security.MessageDigest,byte[])
meth public byte[] getMD5Checksum()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
supr com.amazonaws.internal.SdkDigestInputStream
hfds digestValidated,expectedHash

CLSS public com.amazonaws.services.s3.internal.DualstackEndpointBuilder
cons public init(java.lang.String,java.lang.String,com.amazonaws.regions.Region)
meth public com.amazonaws.regions.Region getRegion()
meth public com.amazonaws.services.s3.internal.DualstackEndpointBuilder withRegion(com.amazonaws.regions.Region)
meth public java.net.URI getServiceEndpoint()
supr com.amazonaws.internal.ServiceEndpointBuilder
hfds protocol,region,serviceName

CLSS public com.amazonaws.services.s3.internal.FileDeletionEvent
cons public init()
supr java.lang.Object

CLSS public final !enum com.amazonaws.services.s3.internal.FileLocks
 anno 0 com.amazonaws.annotation.ThreadSafe()
meth public static boolean isFileLocked(java.io.File)
meth public static boolean lock(java.io.File)
meth public static boolean unlock(java.io.File)
meth public static com.amazonaws.services.s3.internal.FileLocks valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.internal.FileLocks[] values()
supr java.lang.Enum<com.amazonaws.services.s3.internal.FileLocks>
hfds EXTERNAL_LOCK,lockedFiles,log

CLSS public com.amazonaws.services.s3.internal.GetObjectTaggingResponseHeaderHandler
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<com.amazonaws.services.s3.model.GetObjectTaggingResult>
meth public void handle(com.amazonaws.services.s3.model.GetObjectTaggingResult,com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.HeaderHandler<%0 extends java.lang.Object>
meth public abstract void handle({com.amazonaws.services.s3.internal.HeaderHandler%0},com.amazonaws.http.HttpResponse)

CLSS public com.amazonaws.services.s3.internal.InitiateMultipartUploadHeaderHandler
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<com.amazonaws.services.s3.model.InitiateMultipartUploadResult>
meth public void handle(com.amazonaws.services.s3.model.InitiateMultipartUploadResult,com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public final com.amazonaws.services.s3.internal.InputSubstream
cons public init(java.io.InputStream,long,long,boolean)
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds MAX_SKIPS,closeSourceStream,currentPosition,markedPosition,requestedLength,requestedOffset

CLSS public com.amazonaws.services.s3.internal.IsSigV4RetryablePredicate
cons public init()
meth public boolean test(com.amazonaws.AmazonServiceException)
supr com.amazonaws.internal.SdkPredicate<com.amazonaws.AmazonServiceException>
hfds AUTH_ERROR_CODES,AUTH_ERROR_MESSAGES

CLSS public com.amazonaws.services.s3.internal.ListPartsHeaderHandler
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<com.amazonaws.services.s3.model.PartListing>
meth public void handle(com.amazonaws.services.s3.model.PartListing,com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.internal.MD5DigestCalculatingInputStream
cons public init(java.io.InputStream)
meth public boolean markSupported()
meth public byte[] getMd5Digest()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds digest,digestCanBeCloned,digestLastMarked,log

CLSS public com.amazonaws.services.s3.internal.Mimetypes
fld public final static java.lang.String MIMETYPE_GZIP = "application/x-gzip"
fld public final static java.lang.String MIMETYPE_HTML = "text/html"
fld public final static java.lang.String MIMETYPE_OCTET_STREAM = "application/octet-stream"
fld public final static java.lang.String MIMETYPE_XML = "application/xml"
meth public java.lang.String getMimetype(java.io.File)
meth public java.lang.String getMimetype(java.lang.String)
meth public static com.amazonaws.services.s3.internal.Mimetypes getInstance()
meth public void loadAndReplaceMimetypes(java.io.InputStream) throws java.io.IOException
supr java.lang.Object
hfds extensionToMimetypeMap,log,mimetypes

CLSS public com.amazonaws.services.s3.internal.MultiFileOutputStream
cons public init()
cons public init(java.io.File,java.lang.String)
intf com.amazonaws.services.s3.OnFileDelete
meth public boolean isClosed()
meth public com.amazonaws.services.s3.internal.MultiFileOutputStream init(com.amazonaws.services.s3.UploadObjectObserver,long,long)
meth public int getNumFilesWritten()
meth public java.io.File getFile(int)
meth public java.io.File getRoot()
meth public java.lang.String getNamePrefix()
meth public long getDiskLimit()
meth public long getPartSize()
meth public long getTotalBytesWritten()
meth public void cleanup()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void onFileDelete(com.amazonaws.services.s3.internal.FileDeletionEvent)
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds DEFAULT_PART_SIZE,closed,currFileBytesWritten,diskLimit,diskPermits,filesCreated,namePrefix,observer,os,partSize,root,totalBytesWritten

CLSS public com.amazonaws.services.s3.internal.ObjectExpirationHeaderHandler<%0 extends com.amazonaws.services.s3.internal.ObjectExpirationResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.ObjectExpirationHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.ObjectExpirationHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object
hfds datePattern,log,rulePattern

CLSS public abstract interface com.amazonaws.services.s3.internal.ObjectExpirationResult
meth public abstract java.lang.String getExpirationTimeRuleId()
meth public abstract java.util.Date getExpirationTime()
meth public abstract void setExpirationTime(java.util.Date)
meth public abstract void setExpirationTimeRuleId(java.lang.String)

CLSS public com.amazonaws.services.s3.internal.ObjectRestoreHeaderHandler<%0 extends com.amazonaws.services.s3.internal.ObjectRestoreResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.ObjectRestoreHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.ObjectRestoreHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object
hfds datePattern,log,ongoingPattern

CLSS public abstract interface com.amazonaws.services.s3.internal.ObjectRestoreResult
meth public abstract java.lang.Boolean getOngoingRestore()
meth public abstract java.util.Date getRestoreExpirationTime()
meth public abstract void setOngoingRestore(boolean)
meth public abstract void setRestoreExpirationTime(java.util.Date)

CLSS public com.amazonaws.services.s3.internal.PartCreationEvent
meth public boolean isLastPart()
meth public com.amazonaws.services.s3.OnFileDelete getFileDeleteObserver()
meth public int getPartNumber()
meth public java.io.File getPart()
supr java.lang.Object
hfds fileDeleteObserver,isLastPart,part,partNumber

CLSS public com.amazonaws.services.s3.internal.RepeatableFileInputStream
 anno 0 java.lang.Deprecated()
cons public init(java.io.File) throws java.io.FileNotFoundException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.io.File getFile()
meth public java.io.InputStream getWrappedInputStream()
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkInputStream
hfds bytesReadPastMarkPoint,file,fis,log,markPoint

CLSS public com.amazonaws.services.s3.internal.RepeatableInputStream
 anno 0 java.lang.Deprecated()
cons public init(java.io.InputStream,int)
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.io.InputStream getWrappedInputStream()
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkInputStream
hfds buffer,bufferOffset,bufferSize,bytesReadPastMark,hasWarnedBufferOverflow,is,log

CLSS public com.amazonaws.services.s3.internal.RequestCopyUtils
cons public init()
meth public static com.amazonaws.services.s3.model.GetObjectMetadataRequest createGetObjectMetadataRequestFrom(com.amazonaws.services.s3.model.GetObjectRequest)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.internal.ResponseHeaderHandlerChain<%0 extends java.lang.Object>
cons public !varargs init(com.amazonaws.transform.Unmarshaller<{com.amazonaws.services.s3.internal.ResponseHeaderHandlerChain%0},java.io.InputStream>,com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.ResponseHeaderHandlerChain%0}>[])
meth public com.amazonaws.AmazonWebServiceResponse<{com.amazonaws.services.s3.internal.ResponseHeaderHandlerChain%0}> handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception
supr com.amazonaws.services.s3.internal.S3XmlResponseHandler<{com.amazonaws.services.s3.internal.ResponseHeaderHandlerChain%0}>
hfds headerHandlers

CLSS public com.amazonaws.services.s3.internal.RestUtils
cons public init()
meth public static <%0 extends java.lang.Object> java.lang.String makeS3CanonicalString(java.lang.String,java.lang.String,com.amazonaws.SignableRequest<{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> java.lang.String makeS3CanonicalString(java.lang.String,java.lang.String,com.amazonaws.SignableRequest<{%%0}>,java.lang.String,java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds SIGNED_PARAMETERS

CLSS public final com.amazonaws.services.s3.internal.S3AbortableInputStream
cons public init(java.io.InputStream,org.apache.http.client.methods.HttpRequestBase,long)
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void abort()
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds LOG,bytesRead,contentLength,eofReached,httpRequest,markedBytes

CLSS public abstract com.amazonaws.services.s3.internal.S3Direct
cons public init()
intf com.amazonaws.services.s3.internal.S3DirectSpi
meth public abstract com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public abstract com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public abstract com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public abstract void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.S3DirectSpi
meth public abstract com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.CopyPartResult copyPart(com.amazonaws.services.s3.model.CopyPartRequest)
meth public abstract com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public abstract com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public abstract void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)

CLSS public com.amazonaws.services.s3.internal.S3ErrorResponseHandler
cons public init()
intf com.amazonaws.http.HttpResponseHandler<com.amazonaws.AmazonServiceException>
meth public boolean needsConnectionLeftOpen()
meth public com.amazonaws.AmazonServiceException handle(com.amazonaws.http.HttpResponse) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds log
hcls S3ErrorTags

CLSS public com.amazonaws.services.s3.internal.S3MetadataResponseHandler
cons public init()
meth public com.amazonaws.AmazonWebServiceResponse<com.amazonaws.services.s3.model.ObjectMetadata> handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception
supr com.amazonaws.services.s3.internal.AbstractS3ResponseHandler<com.amazonaws.services.s3.model.ObjectMetadata>

CLSS public com.amazonaws.services.s3.internal.S3ObjectResponseHandler
cons public init()
meth public boolean needsConnectionLeftOpen()
meth public com.amazonaws.AmazonWebServiceResponse<com.amazonaws.services.s3.model.S3Object> handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception
supr com.amazonaws.services.s3.internal.AbstractS3ResponseHandler<com.amazonaws.services.s3.model.S3Object>

CLSS public com.amazonaws.services.s3.internal.S3QueryStringSigner
cons public init(java.lang.String,java.lang.String,java.util.Date)
meth protected void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds expiration,httpVerb,resourcePath

CLSS public com.amazonaws.services.s3.internal.S3RequestEndpointResolver
cons public init(com.amazonaws.internal.ServiceEndpointBuilder,boolean,java.lang.String,java.lang.String)
meth public java.lang.String getBucketName()
meth public void resolveRequestEndpoint(com.amazonaws.Request<?>)
meth public void resolveRequestEndpoint(com.amazonaws.Request<?>,java.lang.String)
supr java.lang.Object
hfds bucketName,endpointBuilder,isPathStyleAccess,key

CLSS public com.amazonaws.services.s3.internal.S3RequesterChargedHeaderHandler<%0 extends com.amazonaws.services.s3.internal.S3RequesterChargedResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.S3RequesterChargedHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.S3RequesterChargedHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.S3RequesterChargedResult
meth public abstract boolean isRequesterCharged()
meth public abstract void setRequesterCharged(boolean)

CLSS public com.amazonaws.services.s3.internal.S3RestoreOutputPathHeaderHandler<%0 extends com.amazonaws.services.s3.internal.S3RestoreOutputPathResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.S3RestoreOutputPathHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.S3RestoreOutputPathHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.S3RestoreOutputPathResult
meth public abstract java.lang.String getRestoreOutputPath()
meth public abstract void setRestoreOutputPath(java.lang.String)

CLSS public com.amazonaws.services.s3.internal.S3Signer
cons public init()
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.util.Collection<java.lang.String>)
meth protected void addSessionCredentials(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSSessionCredentials)
meth public void sign(com.amazonaws.SignableRequest<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds additionalQueryParamsToSign,httpVerb,log,resourcePath

CLSS public com.amazonaws.services.s3.internal.S3StringResponseHandler
cons public init()
meth public com.amazonaws.AmazonWebServiceResponse<java.lang.String> handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception
supr com.amazonaws.services.s3.internal.AbstractS3ResponseHandler<java.lang.String>

CLSS public com.amazonaws.services.s3.internal.S3V4AuthErrorRetryStrategy
 anno 0 com.amazonaws.annotation.Immutable()
cons public init(com.amazonaws.services.s3.internal.S3RequestEndpointResolver)
intf com.amazonaws.retry.internal.AuthErrorRetryStrategy
meth public com.amazonaws.retry.internal.AuthRetryParameters shouldRetryWithAuthParam(com.amazonaws.Request<?>,com.amazonaws.http.HttpResponse,com.amazonaws.AmazonServiceException)
supr java.lang.Object
hfds V4_REGION_WARNING,endpointResolver,log,sigV4RetryPredicate

CLSS public com.amazonaws.services.s3.internal.S3VersionHeaderHandler<%0 extends com.amazonaws.services.s3.internal.S3VersionResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.S3VersionHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.S3VersionHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.S3VersionResult
meth public abstract java.lang.String getVersionId()
meth public abstract void setVersionId(java.lang.String)

CLSS public com.amazonaws.services.s3.internal.S3XmlResponseHandler<%0 extends java.lang.Object>
cons public init(com.amazonaws.transform.Unmarshaller<{com.amazonaws.services.s3.internal.S3XmlResponseHandler%0},java.io.InputStream>)
meth public com.amazonaws.AmazonWebServiceResponse<{com.amazonaws.services.s3.internal.S3XmlResponseHandler%0}> handle(com.amazonaws.http.HttpResponse) throws java.lang.Exception
meth public java.util.Map<java.lang.String,java.lang.String> getResponseHeaders()
supr com.amazonaws.services.s3.internal.AbstractS3ResponseHandler<{com.amazonaws.services.s3.internal.S3XmlResponseHandler%0}>
hfds log,responseHeaders,responseUnmarshaller

CLSS public abstract com.amazonaws.services.s3.internal.SSEResultBase
cons public init()
intf com.amazonaws.services.s3.internal.ServerSideEncryptionResult
meth public final java.lang.String getSSEAlgorithm()
meth public final java.lang.String getSSECustomerAlgorithm()
meth public final java.lang.String getSSECustomerKeyMd5()
meth public final java.lang.String getServerSideEncryption()
 anno 0 java.lang.Deprecated()
meth public final void setSSEAlgorithm(java.lang.String)
meth public final void setSSECustomerAlgorithm(java.lang.String)
meth public final void setSSECustomerKeyMd5(java.lang.String)
supr java.lang.Object
hfds sseAlgorithm,sseCustomerAlgorithm,sseCustomerKeyMD5

CLSS public com.amazonaws.services.s3.internal.ServerSideEncryptionHeaderHandler<%0 extends com.amazonaws.services.s3.internal.ServerSideEncryptionResult>
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<{com.amazonaws.services.s3.internal.ServerSideEncryptionHeaderHandler%0}>
meth public void handle({com.amazonaws.services.s3.internal.ServerSideEncryptionHeaderHandler%0},com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.ServerSideEncryptionResult
meth public abstract java.lang.String getSSEAlgorithm()
meth public abstract java.lang.String getSSECustomerAlgorithm()
meth public abstract java.lang.String getSSECustomerKeyMd5()
meth public abstract void setSSEAlgorithm(java.lang.String)
meth public abstract void setSSECustomerAlgorithm(java.lang.String)
meth public abstract void setSSECustomerKeyMd5(java.lang.String)

CLSS public com.amazonaws.services.s3.internal.ServiceUtils
cons public init()
fld protected final static com.amazonaws.util.DateUtils dateUtils
 anno 0 java.lang.Deprecated()
fld public final static boolean APPEND_MODE = true
fld public final static boolean OVERWRITE_MODE = false
innr public abstract interface static RetryableS3DownloadTask
meth public static boolean isS3AccelerateEndpoint(java.lang.String)
meth public static boolean isS3USEastEndpiont(java.lang.String)
meth public static boolean isS3USStandardEndpoint(java.lang.String)
meth public static byte[] toByteArray(java.lang.String)
meth public static com.amazonaws.services.s3.model.S3Object retryableDownloadS3ObjectToFile(java.io.File,com.amazonaws.services.s3.internal.ServiceUtils$RetryableS3DownloadTask,boolean)
meth public static java.lang.Integer getPartCount(com.amazonaws.services.s3.model.GetObjectRequest,com.amazonaws.services.s3.AmazonS3)
meth public static java.lang.String formatIso8601Date(java.util.Date)
meth public static java.lang.String formatRfc822Date(java.util.Date)
meth public static java.lang.String join(java.util.List<java.lang.String>)
meth public static java.lang.String removeQuotes(java.lang.String)
meth public static java.net.URL convertRequestToUrl(com.amazonaws.Request<?>)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL convertRequestToUrl(com.amazonaws.Request<?>,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL convertRequestToUrl(com.amazonaws.Request<?>,boolean,boolean)
meth public static java.util.Date parseIso8601Date(java.lang.String)
meth public static java.util.Date parseRfc822Date(java.lang.String)
meth public static long getLastByteInPart(com.amazonaws.services.s3.AmazonS3,com.amazonaws.services.s3.model.GetObjectRequest,java.lang.Integer)
meth public static long getPartSize(com.amazonaws.services.s3.model.GetObjectRequest,com.amazonaws.services.s3.AmazonS3,int)
meth public static void appendFile(java.io.File,java.io.File)
meth public static void createParentDirectoryIfNecessary(java.io.File)
meth public static void downloadObjectToFile(com.amazonaws.services.s3.model.S3Object,java.io.File,boolean,boolean)
meth public static void downloadToFile(com.amazonaws.services.s3.model.S3Object,java.io.File,boolean,boolean,long)
supr java.lang.Object
hfds LOG,skipMd5CheckStrategy

CLSS public abstract interface static com.amazonaws.services.s3.internal.ServiceUtils$RetryableS3DownloadTask
 outer com.amazonaws.services.s3.internal.ServiceUtils
meth public abstract boolean needIntegrityCheck()
meth public abstract com.amazonaws.services.s3.model.S3Object getS3ObjectStream()

CLSS public com.amazonaws.services.s3.internal.SetObjectTaggingResponseHeaderHandler
cons public init()
intf com.amazonaws.services.s3.internal.HeaderHandler<com.amazonaws.services.s3.model.SetObjectTaggingResult>
meth public void handle(com.amazonaws.services.s3.model.SetObjectTaggingResult,com.amazonaws.http.HttpResponse)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.internal.SkipMd5CheckStrategy
fld public final static com.amazonaws.services.s3.internal.SkipMd5CheckStrategy INSTANCE
fld public final static java.lang.String DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY = "com.amazonaws.services.s3.disableGetObjectMD5Validation"
fld public final static java.lang.String DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY = "com.amazonaws.services.s3.disablePutObjectMD5Validation"
meth public boolean skipClientSideValidation(com.amazonaws.services.s3.model.GetObjectRequest,com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean skipClientSideValidation(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest,com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean skipClientSideValidationPerGetResponse(com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean skipClientSideValidationPerPutResponse(com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean skipClientSideValidationPerRequest(com.amazonaws.services.s3.model.GetObjectRequest)
meth public boolean skipClientSideValidationPerRequest(com.amazonaws.services.s3.model.PresignedUrlDownloadRequest)
meth public boolean skipClientSideValidationPerRequest(com.amazonaws.services.s3.model.PresignedUrlUploadRequest)
meth public boolean skipClientSideValidationPerRequest(com.amazonaws.services.s3.model.PutObjectRequest)
meth public boolean skipClientSideValidationPerRequest(com.amazonaws.services.s3.model.UploadPartRequest)
meth public boolean skipClientSideValidationPerUploadPartResponse(com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean skipServerSideValidation(com.amazonaws.services.s3.model.PutObjectRequest)
meth public boolean skipServerSideValidation(com.amazonaws.services.s3.model.UploadPartRequest)
supr java.lang.Object

CLSS public abstract interface com.amazonaws.services.s3.internal.UploadObjectStrategy<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata invokeServiceCall(com.amazonaws.Request<{com.amazonaws.services.s3.internal.UploadObjectStrategy%0}>)
meth public abstract java.lang.String md5ValidationErrorSuffix()
meth public abstract {com.amazonaws.services.s3.internal.UploadObjectStrategy%1} createResult(com.amazonaws.services.s3.model.ObjectMetadata,java.lang.String)

CLSS public com.amazonaws.services.s3.internal.XmlWriter
cons public init()
meth public byte[] getBytes()
meth public com.amazonaws.services.s3.internal.XmlWriter end()
meth public com.amazonaws.services.s3.internal.XmlWriter start(java.lang.String)
meth public com.amazonaws.services.s3.internal.XmlWriter start(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.internal.XmlWriter start(java.lang.String,java.lang.String[],java.lang.String[])
meth public com.amazonaws.services.s3.internal.XmlWriter value(java.lang.String)
meth public java.lang.String toString()
supr java.lang.Object
hfds sb,tags

CLSS public final com.amazonaws.services.s3.internal.XmlWriterUtils
meth public static void addIfNotNull(com.amazonaws.services.s3.internal.XmlWriter,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload
cons public init()
intf java.io.Serializable
meth protected com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload clone() throws java.lang.CloneNotSupportedException
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload withDaysAfterInitiation(int)
meth public int getDaysAfterInitiation()
meth public int hashCode()
meth public void setDaysAfterInitiation(int)
supr java.lang.Object
hfds daysAfterInitiation

CLSS public com.amazonaws.services.s3.model.AbortMultipartUploadRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withUploadId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,isRequesterPays,key,uploadId

CLSS public abstract com.amazonaws.services.s3.model.AbstractPutObjectRequest
cons protected init(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.S3DataSource
intf com.amazonaws.services.s3.model.SSEAwsKeyManagementParamsProvider
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
intf java.lang.Cloneable
meth protected final <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} copyPutObjectBaseTo({%%0})
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withBucketName(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withFile(java.io.File)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withInputStream(java.io.InputStream)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withKey(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withRedirectLocation(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public <%0 extends com.amazonaws.services.s3.model.AbstractPutObjectRequest> {%%0} withStorageClass(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withObjectLockLegalHoldStatus(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withObjectLockMode(java.lang.String)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withObjectLockRetainUntilDate(java.util.Date)
meth public <%0 extends com.amazonaws.services.s3.model.PutObjectRequest> {%%0} withTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public com.amazonaws.services.s3.model.AbstractPutObjectRequest clone()
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.ObjectMetadata getMetadata()
meth public com.amazonaws.services.s3.model.ObjectTagging getTagging()
meth public com.amazonaws.services.s3.model.ProgressListener getProgressListener()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.SSEAwsKeyManagementParams getSSEAwsKeyManagementParams()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getObjectLockLegalHoldStatus()
meth public java.lang.String getObjectLockMode()
meth public java.lang.String getRedirectLocation()
meth public java.lang.String getStorageClass()
meth public java.util.Date getObjectLockRetainUntilDate()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketName(java.lang.String)
meth public void setCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setFile(java.io.File)
meth public void setInputStream(java.io.InputStream)
meth public void setKey(java.lang.String)
meth public void setMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public void setObjectLockLegalHoldStatus(java.lang.String)
meth public void setObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public void setObjectLockMode(java.lang.String)
meth public void setObjectLockRetainUntilDate(java.util.Date)
meth public void setProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public void setRedirectLocation(java.lang.String)
meth public void setSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
meth public void setTagging(com.amazonaws.services.s3.model.ObjectTagging)
supr com.amazonaws.AmazonWebServiceRequest
hfds accessControlList,bucketName,cannedAcl,file,inputStream,key,metadata,objectLockLegalHoldStatus,objectLockMode,objectLockRetainUntilDate,redirectLocation,sseAwsKeyManagementParams,sseCustomerKey,storageClass,tagging

CLSS public com.amazonaws.services.s3.model.AccessControlList
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public !varargs void grantAllPermissions(com.amazonaws.services.s3.model.Grant[])
meth public boolean equals(java.lang.Object)
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.Grant> getGrantsAsList()
meth public java.util.Set<com.amazonaws.services.s3.model.Grant> getGrants()
 anno 0 java.lang.Deprecated()
meth public void grantPermission(com.amazonaws.services.s3.model.Grantee,com.amazonaws.services.s3.model.Permission)
meth public void revokeAllPermissions(com.amazonaws.services.s3.model.Grantee)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds grantList,grantSet,isRequesterCharged,owner,serialVersionUID

CLSS public com.amazonaws.services.s3.model.AccessControlTranslation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.AccessControlTranslation clone()
meth public com.amazonaws.services.s3.model.AccessControlTranslation withOwner(com.amazonaws.services.s3.model.OwnerOverride)
meth public com.amazonaws.services.s3.model.AccessControlTranslation withOwner(java.lang.String)
meth public int hashCode()
meth public java.lang.String getOwner()
meth public java.lang.String toString()
meth public void setOwner(java.lang.String)
supr java.lang.Object
hfds owner

CLSS public com.amazonaws.services.s3.model.AmazonS3Exception
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public java.lang.String getCloudFrontId()
meth public java.lang.String getErrorResponseXml()
meth public java.lang.String getExtendedRequestId()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getAdditionalDetails()
meth public void setAdditionalDetails(java.util.Map<java.lang.String,java.lang.String>)
meth public void setCloudFrontId(java.lang.String)
meth public void setExtendedRequestId(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds additionalDetails,cloudFrontId,errorResponseXml,extendedRequestId,serialVersionUID

CLSS public com.amazonaws.services.s3.model.Bucket
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Date getCreationDate()
meth public void setCreationDate(java.util.Date)
meth public void setName(java.lang.String)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
supr java.lang.Object
hfds creationDate,name,owner,serialVersionUID

CLSS public com.amazonaws.services.s3.model.BucketAccelerateConfiguration
cons public init(com.amazonaws.services.s3.model.BucketAccelerateStatus)
cons public init(java.lang.String)
intf java.io.Serializable
meth public boolean isAccelerateEnabled()
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration withStatus(com.amazonaws.services.s3.model.BucketAccelerateStatus)
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration withStatus(java.lang.String)
meth public java.lang.String getStatus()
meth public void setStatus(com.amazonaws.services.s3.model.BucketAccelerateStatus)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds status

CLSS public final !enum com.amazonaws.services.s3.model.BucketAccelerateStatus
fld public final static com.amazonaws.services.s3.model.BucketAccelerateStatus Enabled
fld public final static com.amazonaws.services.s3.model.BucketAccelerateStatus Suspended
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.BucketAccelerateStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.BucketAccelerateStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.BucketAccelerateStatus[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.BucketAccelerateStatus>
hfds accelerateStatus

CLSS public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration
cons public init()
cons public init(java.util.List<com.amazonaws.services.s3.model.CORSRule>)
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.BucketCrossOriginConfiguration withRules(com.amazonaws.services.s3.model.CORSRule[])
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration withRules(java.util.List<com.amazonaws.services.s3.model.CORSRule>)
meth public java.util.List<com.amazonaws.services.s3.model.CORSRule> getRules()
meth public void setRules(java.util.List<com.amazonaws.services.s3.model.CORSRule>)
supr java.lang.Object
hfds rules

CLSS public com.amazonaws.services.s3.model.BucketLifecycleConfiguration
cons public init()
cons public init(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule>)
fld public final static java.lang.String DISABLED = "Disabled"
fld public final static java.lang.String ENABLED = "Enabled"
innr public static NoncurrentVersionTransition
innr public static Rule
innr public static Transition
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.BucketLifecycleConfiguration withRules(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule[])
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration withRules(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule>)
meth public java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule> getRules()
meth public void setRules(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule>)
supr java.lang.Object
hfds rules

CLSS public static com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition
 outer com.amazonaws.services.s3.model.BucketLifecycleConfiguration
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition withDays(int)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.StorageClass getStorageClass()
 anno 0 java.lang.Deprecated()
meth public int getDays()
meth public java.lang.String getStorageClassAsString()
meth public void setDays(int)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
supr java.lang.Object
hfds days,storageClass

CLSS public static com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule
 outer com.amazonaws.services.s3.model.BucketLifecycleConfiguration
cons public init()
intf java.io.Serializable
meth public boolean isExpiredObjectDeleteMarker()
meth public com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload getAbortIncompleteMultipartUpload()
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition getNoncurrentVersionTransition()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule addNoncurrentVersionTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule addTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withAbortIncompleteMultipartUpload(com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withExpirationDate(java.util.Date)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withExpirationInDays(int)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withExpiredObjectDeleteMarker(boolean)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withFilter(com.amazonaws.services.s3.model.lifecycle.LifecycleFilter)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withId(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withNoncurrentVersionExpirationInDays(int)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withNoncurrentVersionTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withNoncurrentVersionTransitions(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition>)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withPrefix(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withStatus(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Rule withTransitions(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition>)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition getTransition()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.lifecycle.LifecycleFilter getFilter()
meth public int getExpirationInDays()
meth public int getNoncurrentVersionExpirationInDays()
meth public java.lang.String getId()
meth public java.lang.String getPrefix()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getStatus()
meth public java.util.Date getExpirationDate()
meth public java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition> getNoncurrentVersionTransitions()
meth public java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition> getTransitions()
meth public void setAbortIncompleteMultipartUpload(com.amazonaws.services.s3.model.AbortIncompleteMultipartUpload)
meth public void setExpirationDate(java.util.Date)
meth public void setExpirationInDays(int)
meth public void setExpiredObjectDeleteMarker(boolean)
meth public void setFilter(com.amazonaws.services.s3.model.lifecycle.LifecycleFilter)
meth public void setId(java.lang.String)
meth public void setNoncurrentVersionExpirationInDays(int)
meth public void setNoncurrentVersionTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition)
 anno 0 java.lang.Deprecated()
meth public void setNoncurrentVersionTransitions(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$NoncurrentVersionTransition>)
meth public void setPrefix(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setStatus(java.lang.String)
meth public void setTransition(com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition)
 anno 0 java.lang.Deprecated()
meth public void setTransitions(java.util.List<com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition>)
supr java.lang.Object
hfds abortIncompleteMultipartUpload,expirationDate,expirationInDays,expiredObjectDeleteMarker,filter,id,noncurrentVersionExpirationInDays,noncurrentVersionTransitions,prefix,status,transitions

CLSS public static com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition
 outer com.amazonaws.services.s3.model.BucketLifecycleConfiguration
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition withDate(java.util.Date)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition withDays(int)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration$Transition withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.StorageClass getStorageClass()
 anno 0 java.lang.Deprecated()
meth public int getDays()
meth public java.lang.String getStorageClassAsString()
meth public java.util.Date getDate()
meth public void setDate(java.util.Date)
meth public void setDays(int)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
supr java.lang.Object
hfds date,days,storageClass

CLSS public com.amazonaws.services.s3.model.BucketLoggingConfiguration
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isLoggingEnabled()
meth public java.lang.String getDestinationBucketName()
meth public java.lang.String getLogFilePrefix()
meth public java.lang.String toString()
meth public void setDestinationBucketName(java.lang.String)
meth public void setLogFilePrefix(java.lang.String)
supr java.lang.Object
hfds destinationBucketName,logFilePrefix

CLSS public com.amazonaws.services.s3.model.BucketNotificationConfiguration
cons public init()
cons public init(java.lang.String,com.amazonaws.services.s3.model.NotificationConfiguration)
cons public init(java.util.Collection<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration>)
innr public static TopicConfiguration
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.BucketNotificationConfiguration withTopicConfigurations(com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration[])
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration addConfiguration(java.lang.String,com.amazonaws.services.s3.model.NotificationConfiguration)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration withNotificationConfiguration(java.util.Map<java.lang.String,com.amazonaws.services.s3.model.NotificationConfiguration>)
meth public com.amazonaws.services.s3.model.NotificationConfiguration getConfigurationByName(java.lang.String)
meth public com.amazonaws.services.s3.model.NotificationConfiguration removeConfiguration(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration> getTopicConfigurations()
meth public java.util.Map<java.lang.String,com.amazonaws.services.s3.model.NotificationConfiguration> getConfigurations()
meth public void setConfigurations(java.util.Map<java.lang.String,com.amazonaws.services.s3.model.NotificationConfiguration>)
meth public void setTopicConfigurations(java.util.Collection<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration>)
supr java.lang.Object
hfds configurations

CLSS public static com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration
 outer com.amazonaws.services.s3.model.BucketNotificationConfiguration
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getEvent()
meth public java.lang.String getTopic()
meth public java.lang.String toString()
supr com.amazonaws.services.s3.model.TopicConfiguration

CLSS public com.amazonaws.services.s3.model.BucketPolicy
cons public init()
intf java.io.Serializable
meth public java.lang.String getPolicyText()
meth public void setPolicyText(java.lang.String)
supr java.lang.Object
hfds policyText

CLSS public com.amazonaws.services.s3.model.BucketReplicationConfiguration
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration addRule(java.lang.String,com.amazonaws.services.s3.model.ReplicationRule)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration removeRule(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration withRoleARN(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration withRules(java.util.Map<java.lang.String,com.amazonaws.services.s3.model.ReplicationRule>)
meth public com.amazonaws.services.s3.model.ReplicationRule getRule(java.lang.String)
meth public java.lang.String getRoleARN()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,com.amazonaws.services.s3.model.ReplicationRule> getRules()
meth public void setRoleARN(java.lang.String)
meth public void setRules(java.util.Map<java.lang.String,com.amazonaws.services.s3.model.ReplicationRule>)
supr java.lang.Object
hfds roleARN,rules

CLSS public com.amazonaws.services.s3.model.BucketTaggingConfiguration
cons public init()
cons public init(java.util.Collection<com.amazonaws.services.s3.model.TagSet>)
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.BucketTaggingConfiguration withTagSets(com.amazonaws.services.s3.model.TagSet[])
meth public com.amazonaws.services.s3.model.TagSet getTagSet()
meth public com.amazonaws.services.s3.model.TagSet getTagSetAtIndex(int)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.TagSet> getAllTagSets()
meth public void setTagSets(java.util.Collection<com.amazonaws.services.s3.model.TagSet>)
supr java.lang.Object
hfds tagSets

CLSS public com.amazonaws.services.s3.model.BucketVersioningConfiguration
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String OFF = "Off"
fld public final static java.lang.String SUSPENDED = "Suspended"
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration withMfaDeleteEnabled(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration withStatus(java.lang.String)
meth public java.lang.Boolean isMfaDeleteEnabled()
meth public java.lang.String getStatus()
meth public void setMfaDeleteEnabled(java.lang.Boolean)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds isMfaDeleteEnabled,status

CLSS public com.amazonaws.services.s3.model.BucketWebsiteConfiguration
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration withRedirectAllRequestsTo(com.amazonaws.services.s3.model.RedirectRule)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration withRoutingRules(java.util.List<com.amazonaws.services.s3.model.RoutingRule>)
meth public com.amazonaws.services.s3.model.RedirectRule getRedirectAllRequestsTo()
meth public java.lang.String getErrorDocument()
meth public java.lang.String getIndexDocumentSuffix()
meth public java.util.List<com.amazonaws.services.s3.model.RoutingRule> getRoutingRules()
meth public void setErrorDocument(java.lang.String)
meth public void setIndexDocumentSuffix(java.lang.String)
meth public void setRedirectAllRequestsTo(com.amazonaws.services.s3.model.RedirectRule)
meth public void setRoutingRules(java.util.List<com.amazonaws.services.s3.model.RoutingRule>)
supr java.lang.Object
hfds errorDocument,indexDocumentSuffix,redirectAllRequestsTo,routingRules

CLSS public com.amazonaws.services.s3.model.CORSRule
cons public init()
innr public final static !enum AllowedMethods
intf java.io.Serializable
meth public !varargs void setAllowedHeaders(java.lang.String[])
meth public !varargs void setAllowedMethods(com.amazonaws.services.s3.model.CORSRule$AllowedMethods[])
meth public !varargs void setAllowedOrigins(java.lang.String[])
meth public !varargs void setExposedHeaders(java.lang.String[])
meth public com.amazonaws.services.s3.model.CORSRule withAllowedHeaders(java.util.List<java.lang.String>)
meth public com.amazonaws.services.s3.model.CORSRule withAllowedMethods(java.util.List<com.amazonaws.services.s3.model.CORSRule$AllowedMethods>)
meth public com.amazonaws.services.s3.model.CORSRule withAllowedOrigins(java.util.List<java.lang.String>)
meth public com.amazonaws.services.s3.model.CORSRule withExposedHeaders(java.util.List<java.lang.String>)
meth public com.amazonaws.services.s3.model.CORSRule withId(java.lang.String)
meth public com.amazonaws.services.s3.model.CORSRule withMaxAgeSeconds(int)
meth public int getMaxAgeSeconds()
meth public java.lang.String getId()
meth public java.util.List<com.amazonaws.services.s3.model.CORSRule$AllowedMethods> getAllowedMethods()
meth public java.util.List<java.lang.String> getAllowedHeaders()
meth public java.util.List<java.lang.String> getAllowedOrigins()
meth public java.util.List<java.lang.String> getExposedHeaders()
meth public void setAllowedHeaders(java.util.List<java.lang.String>)
meth public void setAllowedMethods(java.util.List<com.amazonaws.services.s3.model.CORSRule$AllowedMethods>)
meth public void setAllowedOrigins(java.util.List<java.lang.String>)
meth public void setExposedHeaders(java.util.List<java.lang.String>)
meth public void setId(java.lang.String)
meth public void setMaxAgeSeconds(int)
supr java.lang.Object
hfds allowedHeaders,allowedMethods,allowedOrigins,exposedHeaders,id,maxAgeSeconds

CLSS public final static !enum com.amazonaws.services.s3.model.CORSRule$AllowedMethods
 outer com.amazonaws.services.s3.model.CORSRule
fld public final static com.amazonaws.services.s3.model.CORSRule$AllowedMethods DELETE
fld public final static com.amazonaws.services.s3.model.CORSRule$AllowedMethods GET
fld public final static com.amazonaws.services.s3.model.CORSRule$AllowedMethods HEAD
fld public final static com.amazonaws.services.s3.model.CORSRule$AllowedMethods POST
fld public final static com.amazonaws.services.s3.model.CORSRule$AllowedMethods PUT
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.CORSRule$AllowedMethods fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.CORSRule$AllowedMethods valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CORSRule$AllowedMethods[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CORSRule$AllowedMethods>
hfds AllowedMethod

CLSS public com.amazonaws.services.s3.model.CSVInput
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.CSVInput clone()
meth public com.amazonaws.services.s3.model.CSVInput withAllowQuotedRecordDelimiter(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.CSVInput withComments(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVInput withComments(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVInput withFieldDelimiter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVInput withFieldDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVInput withFileHeaderInfo(com.amazonaws.services.s3.model.FileHeaderInfo)
meth public com.amazonaws.services.s3.model.CSVInput withFileHeaderInfo(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVInput withQuoteCharacter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVInput withQuoteCharacter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVInput withQuoteEscapeCharacter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVInput withQuoteEscapeCharacter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVInput withRecordDelimiter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVInput withRecordDelimiter(java.lang.String)
meth public int hashCode()
meth public java.lang.Boolean getAllowQuotedRecordDelimiter()
meth public java.lang.Character getComments()
meth public java.lang.Character getFieldDelimiter()
meth public java.lang.Character getQuoteCharacter()
meth public java.lang.Character getQuoteEscapeCharacter()
meth public java.lang.Character getRecordDelimiter()
meth public java.lang.String getCommentsAsString()
meth public java.lang.String getFieldDelimiterAsString()
meth public java.lang.String getFileHeaderInfo()
meth public java.lang.String getQuoteCharacterAsString()
meth public java.lang.String getQuoteEscapeCharacterAsString()
meth public java.lang.String getRecordDelimiterAsString()
meth public java.lang.String toString()
meth public void setAllowQuotedRecordDelimiter(java.lang.Boolean)
meth public void setComments(java.lang.Character)
meth public void setComments(java.lang.String)
meth public void setFieldDelimiter(java.lang.Character)
meth public void setFieldDelimiter(java.lang.String)
meth public void setFileHeaderInfo(com.amazonaws.services.s3.model.FileHeaderInfo)
meth public void setFileHeaderInfo(java.lang.String)
meth public void setQuoteCharacter(java.lang.Character)
meth public void setQuoteCharacter(java.lang.String)
meth public void setQuoteEscapeCharacter(java.lang.Character)
meth public void setQuoteEscapeCharacter(java.lang.String)
meth public void setRecordDelimiter(java.lang.Character)
meth public void setRecordDelimiter(java.lang.String)
supr java.lang.Object
hfds allowQuotedRecordDelimiter,comments,fieldDelimiter,fileHeaderInfo,quoteCharacter,quoteEscapeCharacter,recordDelimiter

CLSS public com.amazonaws.services.s3.model.CSVOutput
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.CSVOutput withFieldDelimiter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVOutput withFieldDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteCharacter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteCharacter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteEscapeCharacter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteEscapeCharacter(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteFields(com.amazonaws.services.s3.model.QuoteFields)
meth public com.amazonaws.services.s3.model.CSVOutput withQuoteFields(java.lang.String)
meth public com.amazonaws.services.s3.model.CSVOutput withRecordDelimiter(java.lang.Character)
meth public com.amazonaws.services.s3.model.CSVOutput withRecordDelimiter(java.lang.String)
meth public int hashCode()
meth public java.lang.Character getFieldDelimiter()
meth public java.lang.Character getQuoteCharacter()
meth public java.lang.Character getQuoteEscapeCharacter()
meth public java.lang.Character getRecordDelimiter()
meth public java.lang.Object clone()
meth public java.lang.String getFieldDelimiterAsString()
meth public java.lang.String getQuoteCharacterAsString()
meth public java.lang.String getQuoteEscapeCharacterAsString()
meth public java.lang.String getQuoteFields()
meth public java.lang.String getRecordDelimiterAsString()
meth public java.lang.String toString()
meth public void setFieldDelimiter(java.lang.Character)
meth public void setFieldDelimiter(java.lang.String)
meth public void setQuoteCharacter(java.lang.Character)
meth public void setQuoteCharacter(java.lang.String)
meth public void setQuoteEscapeCharacter(java.lang.Character)
meth public void setQuoteEscapeCharacter(java.lang.String)
meth public void setQuoteFields(com.amazonaws.services.s3.model.QuoteFields)
meth public void setQuoteFields(java.lang.String)
meth public void setRecordDelimiter(java.lang.Character)
meth public void setRecordDelimiter(java.lang.String)
supr java.lang.Object
hfds fieldDelimiter,quoteCharacter,quoteEscapeCharacter,quoteFields,recordDelimiter

CLSS public final !enum com.amazonaws.services.s3.model.CannedAccessControlList
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList AuthenticatedRead
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList AwsExecRead
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList BucketOwnerFullControl
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList BucketOwnerRead
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList LogDeliveryWrite
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList Private
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList PublicRead
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList PublicReadWrite
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.CannedAccessControlList valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CannedAccessControlList[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CannedAccessControlList>
hfds cannedAclHeader

CLSS public com.amazonaws.services.s3.model.CanonicalGrantee
cons public init(java.lang.String)
intf com.amazonaws.services.s3.model.Grantee
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getIdentifier()
meth public java.lang.String getTypeIdentifier()
meth public void setDisplayName(java.lang.String)
meth public void setIdentifier(java.lang.String)
supr java.lang.Object
hfds displayName,id

CLSS public com.amazonaws.services.s3.model.CloudFunctionConfiguration
 anno 0 java.lang.Deprecated()
cons public !varargs init(java.lang.String,java.lang.String,java.lang.String[])
cons public init(java.lang.String,java.lang.String,java.util.EnumSet<com.amazonaws.services.s3.model.S3Event>)
intf java.io.Serializable
meth public java.lang.String getCloudFunctionARN()
meth public java.lang.String getInvocationRoleARN()
supr com.amazonaws.services.s3.model.NotificationConfiguration
hfds cloudFunctionARN,invocationRoleARN

CLSS public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.List<com.amazonaws.services.s3.model.PartETag>)
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(com.amazonaws.services.s3.model.UploadPartResult[])
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(java.util.Collection<com.amazonaws.services.s3.model.UploadPartResult>)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withUploadId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public java.util.List<com.amazonaws.services.s3.model.PartETag> getPartETags()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setPartETags(java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public void setRequesterPays(boolean)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,isRequesterPays,key,partETags,uploadId

CLSS public com.amazonaws.services.s3.model.CompleteMultipartUploadResult
cons public init()
intf com.amazonaws.services.s3.internal.ObjectExpirationResult
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public java.lang.String getBucketName()
meth public java.lang.String getETag()
meth public java.lang.String getExpirationTimeRuleId()
meth public java.lang.String getKey()
meth public java.lang.String getLocation()
meth public java.lang.String getVersionId()
meth public java.util.Date getExpirationTime()
meth public void setBucketName(java.lang.String)
meth public void setETag(java.lang.String)
meth public void setExpirationTime(java.util.Date)
meth public void setExpirationTimeRuleId(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setLocation(java.lang.String)
meth public void setRequesterCharged(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds bucketName,eTag,expirationTime,expirationTimeRuleId,isRequesterCharged,key,location,versionId

CLSS public final !enum com.amazonaws.services.s3.model.CompressionType
fld public final static com.amazonaws.services.s3.model.CompressionType BZIP2
fld public final static com.amazonaws.services.s3.model.CompressionType GZIP
fld public final static com.amazonaws.services.s3.model.CompressionType NONE
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.CompressionType fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.CompressionType valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CompressionType[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CompressionType>
hfds compressionType

CLSS public com.amazonaws.services.s3.model.CopyObjectRequest
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf com.amazonaws.services.s3.model.SSEAwsKeyManagementParamsProvider
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAccessControlList()
meth public com.amazonaws.services.s3.model.CopyObjectRequest withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withCannedAccessControlList(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withDestinationBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withDestinationKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withDestinationSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withMatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withMetadataDirective(com.amazonaws.services.s3.model.MetadataDirective)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withMetadataDirective(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withModifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withNewObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withNewObjectTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withNonmatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withObjectLockLegalHoldStatus(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withObjectLockMode(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withObjectLockRetainUntilDate(java.util.Date)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withRedirectLocation(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withUnmodifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.ObjectMetadata getNewObjectMetadata()
meth public com.amazonaws.services.s3.model.ObjectTagging getNewObjectTagging()
meth public com.amazonaws.services.s3.model.SSEAwsKeyManagementParams getSSEAwsKeyManagementParams()
meth public com.amazonaws.services.s3.model.SSECustomerKey getDestinationSSECustomerKey()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSourceSSECustomerKey()
meth public java.lang.String getDestinationBucketName()
meth public java.lang.String getDestinationKey()
meth public java.lang.String getMetadataDirective()
meth public java.lang.String getObjectLockLegalHoldStatus()
meth public java.lang.String getObjectLockMode()
meth public java.lang.String getRedirectLocation()
meth public java.lang.String getSourceBucketName()
meth public java.lang.String getSourceKey()
meth public java.lang.String getSourceVersionId()
meth public java.lang.String getStorageClass()
meth public java.util.Date getModifiedSinceConstraint()
meth public java.util.Date getObjectLockRetainUntilDate()
meth public java.util.Date getUnmodifiedSinceConstraint()
meth public java.util.List<java.lang.String> getMatchingETagConstraints()
meth public java.util.List<java.lang.String> getNonmatchingETagConstraints()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setCannedAccessControlList(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setDestinationBucketName(java.lang.String)
meth public void setDestinationKey(java.lang.String)
meth public void setDestinationSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setMatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setMetadataDirective(java.lang.String)
meth public void setModifiedSinceConstraint(java.util.Date)
meth public void setNewObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setNewObjectTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public void setNonmatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public void setObjectLockLegalHoldStatus(java.lang.String)
meth public void setObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public void setObjectLockMode(java.lang.String)
meth public void setObjectLockRetainUntilDate(java.util.Date)
meth public void setRedirectLocation(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public void setSourceBucketName(java.lang.String)
meth public void setSourceKey(java.lang.String)
meth public void setSourceSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setSourceVersionId(java.lang.String)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
meth public void setUnmodifiedSinceConstraint(java.util.Date)
supr com.amazonaws.AmazonWebServiceRequest
hfds accessControlList,cannedACL,destinationBucketName,destinationKey,destinationSSECustomerKey,isRequesterPays,matchingETagConstraints,metadataDirective,modifiedSinceConstraint,newObjectMetadata,newObjectTagging,nonmatchingEtagConstraints,objectLockLegalHoldStatus,objectLockMode,objectLockRetainUntilDate,redirectLocation,sourceBucketName,sourceKey,sourceSSECustomerKey,sourceVersionId,sseAwsKeyManagementParams,storageClass,unmodifiedSinceConstraint

CLSS public com.amazonaws.services.s3.model.CopyObjectResult
cons public init()
intf com.amazonaws.services.s3.internal.ObjectExpirationResult
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf com.amazonaws.services.s3.internal.S3VersionResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public java.lang.String getETag()
meth public java.lang.String getExpirationTimeRuleId()
meth public java.lang.String getVersionId()
meth public java.util.Date getExpirationTime()
meth public java.util.Date getLastModifiedDate()
meth public void setETag(java.lang.String)
meth public void setExpirationTime(java.util.Date)
meth public void setExpirationTimeRuleId(java.lang.String)
meth public void setLastModifiedDate(java.util.Date)
meth public void setRequesterCharged(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds etag,expirationTime,expirationTimeRuleId,isRequesterCharged,lastModifiedDate,versionId

CLSS public com.amazonaws.services.s3.model.CopyPartRequest
cons public init()
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.CopyPartRequest withDestinationBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withDestinationKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withDestinationSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.CopyPartRequest withFirstByte(java.lang.Long)
meth public com.amazonaws.services.s3.model.CopyPartRequest withLastByte(java.lang.Long)
meth public com.amazonaws.services.s3.model.CopyPartRequest withMatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withMatchingETagConstraints(java.util.List<java.lang.String>)
meth public com.amazonaws.services.s3.model.CopyPartRequest withModifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.CopyPartRequest withNonmatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withNonmatchingETagConstraints(java.util.List<java.lang.String>)
meth public com.amazonaws.services.s3.model.CopyPartRequest withPartNumber(int)
meth public com.amazonaws.services.s3.model.CopyPartRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.CopyPartRequest withSourceBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withSourceKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withSourceSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.CopyPartRequest withSourceVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyPartRequest withUnmodifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.CopyPartRequest withUploadId(java.lang.String)
meth public com.amazonaws.services.s3.model.SSECustomerKey getDestinationSSECustomerKey()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSourceSSECustomerKey()
meth public int getPartNumber()
meth public java.lang.Long getFirstByte()
meth public java.lang.Long getLastByte()
meth public java.lang.String getDestinationBucketName()
meth public java.lang.String getDestinationKey()
meth public java.lang.String getSourceBucketName()
meth public java.lang.String getSourceKey()
meth public java.lang.String getSourceVersionId()
meth public java.lang.String getUploadId()
meth public java.util.Date getModifiedSinceConstraint()
meth public java.util.Date getUnmodifiedSinceConstraint()
meth public java.util.List<java.lang.String> getMatchingETagConstraints()
meth public java.util.List<java.lang.String> getNonmatchingETagConstraints()
meth public void setDestinationBucketName(java.lang.String)
meth public void setDestinationKey(java.lang.String)
meth public void setDestinationSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setFirstByte(java.lang.Long)
meth public void setLastByte(java.lang.Long)
meth public void setMatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setModifiedSinceConstraint(java.util.Date)
meth public void setNonmatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setPartNumber(int)
meth public void setRequesterPays(boolean)
meth public void setSourceBucketName(java.lang.String)
meth public void setSourceKey(java.lang.String)
meth public void setSourceSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setSourceVersionId(java.lang.String)
meth public void setUnmodifiedSinceConstraint(java.util.Date)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds destinationBucketName,destinationKey,destinationSSECustomerKey,firstByte,isRequesterPays,lastByte,matchingETagConstraints,modifiedSinceConstraint,nonmatchingEtagConstraints,partNumber,sourceBucketName,sourceKey,sourceSSECustomerKey,sourceVersionId,unmodifiedSinceConstraint,uploadId

CLSS public com.amazonaws.services.s3.model.CopyPartResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.PartETag getPartETag()
meth public int getPartNumber()
meth public java.lang.String getETag()
meth public java.lang.String getVersionId()
meth public java.util.Date getLastModifiedDate()
meth public void setETag(java.lang.String)
meth public void setLastModifiedDate(java.util.Date)
meth public void setPartNumber(int)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds etag,lastModifiedDate,partNumber,versionId

CLSS public com.amazonaws.services.s3.model.CreateBucketRequest
cons public init(java.lang.String)
cons public init(java.lang.String,com.amazonaws.services.s3.model.Region)
cons public init(java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf java.io.Serializable
meth public boolean getObjectLockEnabledForBucket()
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.CreateBucketRequest withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.CreateBucketRequest withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.CreateBucketRequest withObjectLockEnabledForBucket(boolean)
meth public java.lang.String getBucketName()
meth public java.lang.String getRegion()
 anno 0 java.lang.Deprecated()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketName(java.lang.String)
meth public void setCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectLockEnabledForBucket(boolean)
meth public void setRegion(java.lang.String)
 anno 0 java.lang.Deprecated()
supr com.amazonaws.AmazonWebServiceRequest
hfds accessControlList,bucketName,cannedAcl,objectLockEnabled,region

CLSS public com.amazonaws.services.s3.model.CryptoConfiguration
cons public init()
cons public init(com.amazonaws.services.s3.model.CryptoMode)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean getAlwaysUseCryptoProvider()
meth public boolean isIgnoreMissingInstructionFile()
meth public boolean isReadOnly()
meth public com.amazonaws.regions.Region getAwsKmsRegion()
meth public com.amazonaws.regions.Regions getKmsRegion()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.CryptoConfiguration clone()
meth public com.amazonaws.services.s3.model.CryptoConfiguration readOnly()
meth public com.amazonaws.services.s3.model.CryptoConfiguration withAlwaysUseCryptoProvider(boolean)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withAwsKmsRegion(com.amazonaws.regions.Region)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withCryptoMode(com.amazonaws.services.s3.model.CryptoMode)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withCryptoProvider(java.security.Provider)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withIgnoreMissingInstructionFile(boolean)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withKmsRegion(com.amazonaws.regions.Regions)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.CryptoConfiguration withSecureRandom(java.security.SecureRandom)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withStorageMode(com.amazonaws.services.s3.model.CryptoStorageMode)
meth public com.amazonaws.services.s3.model.CryptoMode getCryptoMode()
meth public com.amazonaws.services.s3.model.CryptoStorageMode getStorageMode()
meth public java.security.Provider getCryptoProvider()
meth public java.security.SecureRandom getSecureRandom()
meth public void setAlwaysUseCryptoProvider(boolean)
meth public void setAwsKmsRegion(com.amazonaws.regions.Region)
meth public void setCryptoMode(com.amazonaws.services.s3.model.CryptoMode)
meth public void setCryptoProvider(java.security.Provider)
meth public void setIgnoreMissingInstructionFile(boolean)
meth public void setKmsRegion(com.amazonaws.regions.Regions)
 anno 0 java.lang.Deprecated()
meth public void setSecureRandom(java.security.SecureRandom)
meth public void setStorageMode(com.amazonaws.services.s3.model.CryptoStorageMode)
supr java.lang.Object
hfds SRAND,alwaysUseCryptoProvider,awskmsRegion,cryptoMode,cryptoProvider,ignoreMissingInstructionFile,secureRandom,serialVersionUID,storageMode
hcls ReadOnly

CLSS public final !enum com.amazonaws.services.s3.model.CryptoMode
fld public final static com.amazonaws.services.s3.model.CryptoMode AuthenticatedEncryption
fld public final static com.amazonaws.services.s3.model.CryptoMode EncryptionOnly
fld public final static com.amazonaws.services.s3.model.CryptoMode StrictAuthenticatedEncryption
meth public static com.amazonaws.services.s3.model.CryptoMode valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CryptoMode[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CryptoMode>

CLSS public final !enum com.amazonaws.services.s3.model.CryptoStorageMode
fld public final static com.amazonaws.services.s3.model.CryptoStorageMode InstructionFile
fld public final static com.amazonaws.services.s3.model.CryptoStorageMode ObjectMetadata
meth public static com.amazonaws.services.s3.model.CryptoStorageMode valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CryptoStorageMode[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CryptoStorageMode>

CLSS public com.amazonaws.services.s3.model.DefaultRetention
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DefaultRetention withDays(java.lang.Integer)
meth public com.amazonaws.services.s3.model.DefaultRetention withMode(com.amazonaws.services.s3.model.ObjectLockRetentionMode)
meth public com.amazonaws.services.s3.model.DefaultRetention withMode(java.lang.String)
meth public com.amazonaws.services.s3.model.DefaultRetention withYears(java.lang.Integer)
meth public java.lang.Integer getDays()
meth public java.lang.Integer getYears()
meth public java.lang.String getMode()
meth public void setDays(java.lang.Integer)
meth public void setMode(com.amazonaws.services.s3.model.ObjectLockRetentionMode)
meth public void setMode(java.lang.String)
meth public void setYears(java.lang.Integer)
supr java.lang.Object
hfds days,mode,years

CLSS public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest clone()
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest withBucketName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.DeleteBucketEncryptionResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.DeleteBucketPolicyRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteBucketPolicyRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest
cons public init(java.lang.String)
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.DeleteBucketRequest
cons public init(java.lang.String)
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf java.io.Serializable
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.DeleteMarkerReplication
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.DeleteMarkerReplication clone()
meth public com.amazonaws.services.s3.model.DeleteMarkerReplication withStatus(com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus)
meth public com.amazonaws.services.s3.model.DeleteMarkerReplication withStatus(java.lang.String)
meth public int hashCode()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds status

CLSS public final !enum com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus
fld public final static com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus DISABLED
fld public final static com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus ENABLED
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.DeleteMarkerReplicationStatus>
hfds value

CLSS public com.amazonaws.services.s3.model.DeleteObjectRequest
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.DeleteObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectRequest withRequesterPays(boolean)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,isRequesterPays,key

CLSS public com.amazonaws.services.s3.model.DeleteObjectTaggingRequest
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,versionId

CLSS public com.amazonaws.services.s3.model.DeleteObjectTaggingResult
cons public init()
meth public com.amazonaws.services.s3.model.DeleteObjectTaggingResult withVersionId(java.lang.String)
meth public java.lang.String getVersionId()
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds versionId

CLSS public com.amazonaws.services.s3.model.DeleteObjectsRequest
cons public init(java.lang.String)
innr public static KeyVersion
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.DeleteObjectsRequest withKeys(java.lang.String[])
meth public boolean getBypassGovernanceRetention()
meth public boolean getQuiet()
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withBypassGovernanceRetention(boolean)
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withKeys(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsRequest$KeyVersion>)
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withQuiet(boolean)
meth public com.amazonaws.services.s3.model.DeleteObjectsRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication getMfa()
meth public java.lang.String getBucketName()
meth public java.util.List<com.amazonaws.services.s3.model.DeleteObjectsRequest$KeyVersion> getKeys()
meth public void setBucketName(java.lang.String)
meth public void setBypassGovernanceRetention(boolean)
meth public void setKeys(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsRequest$KeyVersion>)
meth public void setMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public void setQuiet(boolean)
meth public void setRequesterPays(boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,bypassGovernanceRetention,isRequesterPays,keys,mfa,quiet

CLSS public static com.amazonaws.services.s3.model.DeleteObjectsRequest$KeyVersion
 outer com.amazonaws.services.s3.model.DeleteObjectsRequest
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public java.lang.String getKey()
meth public java.lang.String getVersion()
supr java.lang.Object
hfds key,version

CLSS public com.amazonaws.services.s3.model.DeleteObjectsResult
cons public init(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject>)
cons public init(java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject>,boolean)
innr public static DeletedObject
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject> getDeletedObjects()
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds deletedObjects,isRequesterCharged

CLSS public static com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject
 outer com.amazonaws.services.s3.model.DeleteObjectsResult
cons public init()
intf java.io.Serializable
meth public boolean isDeleteMarker()
meth public java.lang.String getDeleteMarkerVersionId()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setDeleteMarker(boolean)
meth public void setDeleteMarkerVersionId(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds deleteMarker,deleteMarkerVersionId,key,versionId

CLSS public com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest clone()
meth public com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest withBucketName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeletePublicAccessBlockResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.DeletePublicAccessBlockResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.DeleteVersionRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.MultiFactorAuthentication)
intf java.io.Serializable
meth public boolean getBypassGovernanceRetention()
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withBypassGovernanceRetention(boolean)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication getMfa()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setBypassGovernanceRetention(boolean)
meth public void setKey(java.lang.String)
meth public void setMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,bypassGovernanceRetention,key,mfa,versionId

CLSS public com.amazonaws.services.s3.model.EmailAddressGrantee
cons public init(java.lang.String)
intf com.amazonaws.services.s3.model.Grantee
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getIdentifier()
meth public java.lang.String getTypeIdentifier()
meth public java.lang.String toString()
meth public void setIdentifier(java.lang.String)
supr java.lang.Object
hfds emailAddress

CLSS public com.amazonaws.services.s3.model.EncryptedGetObjectRequest
cons public init(com.amazonaws.services.s3.model.S3ObjectId)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean isKeyWrapExpected()
meth public com.amazonaws.services.s3.model.EncryptedGetObjectRequest withExtraMaterialsDescription(com.amazonaws.services.s3.model.ExtraMaterialsDescription)
meth public com.amazonaws.services.s3.model.EncryptedGetObjectRequest withExtraMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
meth public com.amazonaws.services.s3.model.EncryptedGetObjectRequest withInstructionFileSuffix(java.lang.String)
meth public com.amazonaws.services.s3.model.EncryptedGetObjectRequest withKeyWrapExpected(boolean)
meth public com.amazonaws.services.s3.model.ExtraMaterialsDescription getExtraMaterialDescription()
meth public int hashCode()
meth public java.lang.String getInstructionFileSuffix()
meth public void setExtraMaterialDescription(com.amazonaws.services.s3.model.ExtraMaterialsDescription)
meth public void setInstructionFileSuffix(java.lang.String)
meth public void setKeyWrapExpected(boolean)
supr com.amazonaws.services.s3.model.GetObjectRequest
hfds instructionFileSuffix,keyWrapExpected,supplemental

CLSS public com.amazonaws.services.s3.model.EncryptedInitiateMultipartUploadRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.ObjectMetadata)
intf com.amazonaws.services.s3.model.MaterialsDescriptionProvider
intf java.io.Serializable
meth public boolean isCreateEncryptionMaterial()
meth public com.amazonaws.services.s3.model.EncryptedInitiateMultipartUploadRequest withCreateEncryptionMaterial(boolean)
meth public com.amazonaws.services.s3.model.EncryptedInitiateMultipartUploadRequest withMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public void setCreateEncryptionMaterial(boolean)
meth public void setMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
supr com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
hfds createEncryptionMaterial,materialsDescription

CLSS public com.amazonaws.services.s3.model.EncryptedPutObjectRequest
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.MaterialsDescriptionProvider
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.EncryptedPutObjectRequest clone()
meth public com.amazonaws.services.s3.model.EncryptedPutObjectRequest withMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public void setMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
supr com.amazonaws.services.s3.model.PutObjectRequest
hfds materialsDescription

CLSS public com.amazonaws.services.s3.model.Encryption
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.Encryption clone()
meth public com.amazonaws.services.s3.model.Encryption withEncryptionType(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public com.amazonaws.services.s3.model.Encryption withEncryptionType(java.lang.String)
meth public com.amazonaws.services.s3.model.Encryption withKmsContext(java.lang.String)
meth public com.amazonaws.services.s3.model.Encryption withKmsKeyId(java.lang.String)
meth public int hashCode()
meth public java.lang.String getEncryptionType()
meth public java.lang.String getKmsContext()
meth public java.lang.String getKmsKeyId()
meth public java.lang.String toString()
meth public void setEncryptionType(java.lang.String)
meth public void setKmsContext(java.lang.String)
meth public void setKmsKeyId(java.lang.String)
supr java.lang.Object
hfds encryptionType,kmsContext,kmsKeyId

CLSS public com.amazonaws.services.s3.model.EncryptionConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.EncryptionConfiguration clone()
meth public com.amazonaws.services.s3.model.EncryptionConfiguration withReplicaKmsKeyID(java.lang.String)
meth public int hashCode()
meth public java.lang.String getReplicaKmsKeyID()
meth public java.lang.String toString()
meth public void setReplicaKmsKeyID(java.lang.String)
supr java.lang.Object
hfds replicaKmsKeyID

CLSS public com.amazonaws.services.s3.model.EncryptionMaterials
cons protected init(java.security.KeyPair,javax.crypto.SecretKey)
cons public init(java.security.KeyPair)
cons public init(javax.crypto.SecretKey)
intf java.io.Serializable
meth protected java.lang.String getDescription(java.lang.String)
meth public boolean isKMSEnabled()
meth public com.amazonaws.services.s3.model.EncryptionMaterials addDescription(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.EncryptionMaterials addDescriptions(java.util.Map<java.lang.String,java.lang.String>)
meth public com.amazonaws.services.s3.model.EncryptionMaterialsAccessor getAccessor()
meth public java.lang.String getCustomerMasterKeyId()
meth public java.security.KeyPair getKeyPair()
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public javax.crypto.SecretKey getSymmetricKey()
supr java.lang.Object
hfds desc,keyPair,symmetricKey

CLSS public abstract interface com.amazonaws.services.s3.model.EncryptionMaterialsAccessor
meth public abstract com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface com.amazonaws.services.s3.model.EncryptionMaterialsFactory
meth public abstract com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials()

CLSS public abstract interface com.amazonaws.services.s3.model.EncryptionMaterialsProvider
intf com.amazonaws.services.s3.model.EncryptionMaterialsAccessor
intf com.amazonaws.services.s3.model.EncryptionMaterialsFactory
meth public abstract void refresh()

CLSS public final !enum com.amazonaws.services.s3.model.ExpressionType
fld public final static com.amazonaws.services.s3.model.ExpressionType SQL
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.ExpressionType fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.ExpressionType valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ExpressionType[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ExpressionType>
hfds expressionType

CLSS public com.amazonaws.services.s3.model.ExtraMaterialsDescription
cons public init(java.util.Map<java.lang.String,java.lang.String>)
cons public init(java.util.Map<java.lang.String,java.lang.String>,com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution)
fld public final static com.amazonaws.services.s3.model.ExtraMaterialsDescription NONE
innr public final static !enum ConflictResolution
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution getConflictResolution()
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialDescription()
meth public java.util.Map<java.lang.String,java.lang.String> mergeInto(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds extra,resolve

CLSS public final static !enum com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution
 outer com.amazonaws.services.s3.model.ExtraMaterialsDescription
fld public final static com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution FAIL_FAST
fld public final static com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution OVERRIDDEN
fld public final static com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution OVERRIDE
meth public static com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ExtraMaterialsDescription$ConflictResolution>

CLSS public final !enum com.amazonaws.services.s3.model.FileHeaderInfo
fld public final static com.amazonaws.services.s3.model.FileHeaderInfo IGNORE
fld public final static com.amazonaws.services.s3.model.FileHeaderInfo NONE
fld public final static com.amazonaws.services.s3.model.FileHeaderInfo USE
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.FileHeaderInfo fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.FileHeaderInfo valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.FileHeaderInfo[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.FileHeaderInfo>
hfds headerInfo

CLSS public com.amazonaws.services.s3.model.Filter
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Filter withS3KeyFilter(com.amazonaws.services.s3.model.S3KeyFilter)
meth public com.amazonaws.services.s3.model.S3KeyFilter getS3KeyFilter()
meth public void setS3KeyFilter(com.amazonaws.services.s3.model.S3KeyFilter)
supr java.lang.Object
hfds s3KeyFilter

CLSS public com.amazonaws.services.s3.model.FilterRule
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.FilterRule withName(java.lang.String)
meth public com.amazonaws.services.s3.model.FilterRule withValue(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds name,value

CLSS public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,com.amazonaws.HttpMethod)
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
meth public boolean isZeroByteContent()
meth public com.amazonaws.HttpMethod getMethod()
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withContentMd5(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withContentType(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withExpiration(java.util.Date)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withKmsCmkId(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withMethod(com.amazonaws.HttpMethod)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withSSEAlgorithm(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withSSEAlgorithm(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withSSECustomerKeyAlgorithm(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withZeroByteContent(boolean)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides getResponseHeaders()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public java.lang.String getBucketName()
meth public java.lang.String getContentMd5()
meth public java.lang.String getContentType()
meth public java.lang.String getKey()
meth public java.lang.String getKmsCmkId()
meth public java.lang.String getSSEAlgorithm()
meth public java.lang.String getVersionId()
meth public java.util.Date getExpiration()
meth public java.util.Map<java.lang.String,java.lang.String> getRequestParameters()
meth public void addRequestParameter(java.lang.String,java.lang.String)
meth public void rejectIllegalArguments()
meth public void setBucketName(java.lang.String)
meth public void setContentMd5(java.lang.String)
meth public void setContentType(java.lang.String)
meth public void setExpiration(java.util.Date)
meth public void setKey(java.lang.String)
meth public void setKmsCmkId(java.lang.String)
meth public void setMethod(com.amazonaws.HttpMethod)
meth public void setResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public void setSSEAlgorithm(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public void setSSEAlgorithm(java.lang.String)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setSSECustomerKeyAlgorithm(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public void setVersionId(java.lang.String)
meth public void setZeroByteContent(boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,contentMd5,contentType,expiration,key,kmsCmkId,method,requestParameters,responseHeaders,sseAlgorithm,sseCustomerKey,versionId,zeroByteContent

CLSS public com.amazonaws.services.s3.model.GenericBucketRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GenericBucketRequest withBucketName(java.lang.String)
meth public java.lang.String getBucket()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest
cons public init(java.lang.String)
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketAclRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public java.lang.String getBucketName()
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult withAnalyticsConfiguration(com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration getAnalyticsConfiguration()
meth public void setAnalyticsConfiguration(com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
supr java.lang.Object
hfds analyticsConfiguration

CLSS public com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketEncryptionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionRequest clone()
meth public com.amazonaws.services.s3.model.GetBucketEncryptionRequest withBucketName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketEncryptionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult clone()
meth public com.amazonaws.services.s3.model.GetBucketEncryptionResult withServerSideEncryptionConfiguration(com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration getServerSideEncryptionConfiguration()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setServerSideEncryptionConfiguration(com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration)
supr java.lang.Object
hfds serverSideEncryptionConfiguration

CLSS public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult
cons public init()
meth public com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult withInventoryConfiguration(com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
meth public com.amazonaws.services.s3.model.inventory.InventoryConfiguration getInventoryConfiguration()
meth public void setInventoryConfiguration(com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
supr java.lang.Object
hfds inventoryConfiguration

CLSS public com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketLocationRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketLocationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest withId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getId()
meth public void setBucketName(java.lang.String)
meth public void setId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,id

CLSS public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult withMetricsConfiguration(com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
meth public com.amazonaws.services.s3.model.metrics.MetricsConfiguration getMetricsConfiguration()
meth public void setMetricsConfiguration(com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
supr java.lang.Object
hfds metricsConfiguration

CLSS public com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketPolicyRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketPolicyRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest clone()
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest withBucketName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketPolicyStatusResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusResult clone()
meth public com.amazonaws.services.s3.model.GetBucketPolicyStatusResult withPolicyStatus(com.amazonaws.services.s3.model.PolicyStatus)
meth public com.amazonaws.services.s3.model.PolicyStatus getPolicyStatus()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setPolicyStatus(com.amazonaws.services.s3.model.PolicyStatus)
supr java.lang.Object
hfds policyStatus

CLSS public com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.GenericBucketRequest

CLSS public com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetObjectAclRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GetObjectAclRequest withBucket(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectAclRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectAclRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.GetObjectAclRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds isRequesterPays,s3ObjectIdBuilder

CLSS public com.amazonaws.services.s3.model.GetObjectLegalHoldRequest
cons public init()
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,isRequesterPays,key,versionId

CLSS public com.amazonaws.services.s3.model.GetObjectLegalHoldResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetObjectLegalHoldResult withLegalHold(com.amazonaws.services.s3.model.ObjectLockLegalHold)
meth public com.amazonaws.services.s3.model.ObjectLockLegalHold getLegalHold()
meth public void setLegalHold(com.amazonaws.services.s3.model.ObjectLockLegalHold)
supr java.lang.Object
hfds legalHold

CLSS public com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket

CLSS public com.amazonaws.services.s3.model.GetObjectLockConfigurationResult
cons public init()
meth public com.amazonaws.services.s3.model.GetObjectLockConfigurationResult withObjectLockConfiguration(com.amazonaws.services.s3.model.ObjectLockConfiguration)
meth public com.amazonaws.services.s3.model.ObjectLockConfiguration getObjectLockConfiguration()
meth public void setObjectLockConfiguration(com.amazonaws.services.s3.model.ObjectLockConfiguration)
supr java.lang.Object
hfds objectLockConfiguration

CLSS public com.amazonaws.services.s3.model.GetObjectMetadataRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withPartNumber(java.lang.Integer)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public java.lang.Integer getPartNumber()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setPartNumber(java.lang.Integer)
meth public void setRequesterPays(boolean)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,isRequesterPays,key,partNumber,sseCustomerKey,versionId

CLSS public com.amazonaws.services.s3.model.GetObjectRequest
cons public init(com.amazonaws.services.s3.model.S3ObjectId)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GetObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withMatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withModifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.GetObjectRequest withNonmatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withPartNumber(java.lang.Integer)
meth public com.amazonaws.services.s3.model.GetObjectRequest withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.GetObjectRequest withRange(long)
meth public com.amazonaws.services.s3.model.GetObjectRequest withRange(long,long)
meth public com.amazonaws.services.s3.model.GetObjectRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.GetObjectRequest withResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public com.amazonaws.services.s3.model.GetObjectRequest withS3ObjectId(com.amazonaws.services.s3.model.S3ObjectId)
meth public com.amazonaws.services.s3.model.GetObjectRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.GetObjectRequest withUnmodifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.GetObjectRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.ProgressListener getProgressListener()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides getResponseHeaders()
meth public com.amazonaws.services.s3.model.S3ObjectId getS3ObjectId()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public int hashCode()
meth public java.lang.Integer getPartNumber()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public java.util.Date getModifiedSinceConstraint()
meth public java.util.Date getUnmodifiedSinceConstraint()
meth public java.util.List<java.lang.String> getMatchingETagConstraints()
meth public java.util.List<java.lang.String> getNonmatchingETagConstraints()
meth public long[] getRange()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setMatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setModifiedSinceConstraint(java.util.Date)
meth public void setNonmatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setPartNumber(java.lang.Integer)
meth public void setProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public void setRange(long)
meth public void setRange(long,long)
meth public void setRequesterPays(boolean)
meth public void setResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public void setS3ObjectId(com.amazonaws.services.s3.model.S3ObjectId)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setUnmodifiedSinceConstraint(java.util.Date)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds isRequesterPays,matchingETagConstraints,modifiedSinceConstraint,nonmatchingEtagConstraints,partNumber,range,responseHeaders,s3ObjectIdBuilder,sseCustomerKey,unmodifiedSinceConstraint

CLSS public com.amazonaws.services.s3.model.GetObjectRetentionRequest
cons public init()
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GetObjectRetentionRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRetentionRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRetentionRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.GetObjectRetentionRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,isRequesterPays,key,versionId

CLSS public com.amazonaws.services.s3.model.GetObjectRetentionResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetObjectRetentionResult withRetention(com.amazonaws.services.s3.model.ObjectLockRetention)
meth public com.amazonaws.services.s3.model.ObjectLockRetention getRetention()
meth public void setRetention(com.amazonaws.services.s3.model.ObjectLockRetention)
supr java.lang.Object
hfds retention

CLSS public com.amazonaws.services.s3.model.GetObjectTaggingRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GetObjectTaggingRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectTaggingRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectTaggingRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,versionId

CLSS public com.amazonaws.services.s3.model.GetObjectTaggingResult
cons public init(java.util.List<com.amazonaws.services.s3.model.Tag>)
meth public com.amazonaws.services.s3.model.GetObjectTaggingResult withTagSet(java.util.List<com.amazonaws.services.s3.model.Tag>)
meth public com.amazonaws.services.s3.model.GetObjectTaggingResult withVersionId(java.lang.String)
meth public java.lang.String getVersionId()
meth public java.util.List<com.amazonaws.services.s3.model.Tag> getTagSet()
meth public void setTagSet(java.util.List<com.amazonaws.services.s3.model.Tag>)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds tagSet,versionId

CLSS public com.amazonaws.services.s3.model.GetPublicAccessBlockRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockRequest clone()
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockRequest withBucketName(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetPublicAccessBlockResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockResult clone()
meth public com.amazonaws.services.s3.model.GetPublicAccessBlockResult withPublicAccessBlockConfiguration(com.amazonaws.services.s3.model.PublicAccessBlockConfiguration)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration getPublicAccessBlockConfiguration()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setPublicAccessBlockConfiguration(com.amazonaws.services.s3.model.PublicAccessBlockConfiguration)
supr java.lang.Object
hfds publicAccessBlockConfiguration

CLSS public com.amazonaws.services.s3.model.GetRequestPaymentConfigurationRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetS3AccountOwnerRequest
cons public init()
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf java.io.Serializable
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.s3.model.GlacierJobParameters
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.GlacierJobParameters withTier(com.amazonaws.services.s3.model.Tier)
meth public com.amazonaws.services.s3.model.GlacierJobParameters withTier(java.lang.String)
meth public java.lang.String getTier()
meth public void setTier(com.amazonaws.services.s3.model.Tier)
meth public void setTier(java.lang.String)
supr java.lang.Object
hfds tier

CLSS public com.amazonaws.services.s3.model.Grant
cons public init(com.amazonaws.services.s3.model.Grantee,com.amazonaws.services.s3.model.Permission)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.Grantee getGrantee()
meth public com.amazonaws.services.s3.model.Permission getPermission()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds grantee,permission

CLSS public abstract interface com.amazonaws.services.s3.model.Grantee
meth public abstract java.lang.String getIdentifier()
meth public abstract java.lang.String getTypeIdentifier()
meth public abstract void setIdentifier(java.lang.String)

CLSS public final !enum com.amazonaws.services.s3.model.GroupGrantee
fld public final static com.amazonaws.services.s3.model.GroupGrantee AllUsers
fld public final static com.amazonaws.services.s3.model.GroupGrantee AuthenticatedUsers
fld public final static com.amazonaws.services.s3.model.GroupGrantee LogDelivery
intf com.amazonaws.services.s3.model.Grantee
meth public java.lang.String getIdentifier()
meth public java.lang.String getTypeIdentifier()
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.GroupGrantee parseGroupGrantee(java.lang.String)
meth public static com.amazonaws.services.s3.model.GroupGrantee valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.GroupGrantee[] values()
meth public void setIdentifier(java.lang.String)
supr java.lang.Enum<com.amazonaws.services.s3.model.GroupGrantee>
hfds groupUri

CLSS public com.amazonaws.services.s3.model.HeadBucketRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.HeadBucketResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.HeadBucketResult withBucketRegion(java.lang.String)
meth public java.lang.String getBucketRegion()
meth public void setBucketRegion(java.lang.String)
supr java.lang.Object
hfds bucketRegion

CLSS public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.ObjectMetadata)
fld public com.amazonaws.services.s3.model.ObjectMetadata objectMetadata
intf com.amazonaws.services.s3.model.SSEAwsKeyManagementParamsProvider
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedACL()
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withCannedACL(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectLockLegalHoldStatus(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectLockMode(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectLockRetainUntilDate(java.util.Date)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withRedirectLocation(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata()
meth public com.amazonaws.services.s3.model.ObjectTagging getTagging()
meth public com.amazonaws.services.s3.model.SSEAwsKeyManagementParams getSSEAwsKeyManagementParams()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public com.amazonaws.services.s3.model.StorageClass getStorageClass()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getObjectLockLegalHoldStatus()
meth public java.lang.String getObjectLockMode()
meth public java.lang.String getRedirectLocation()
meth public java.util.Date getObjectLockRetainUntilDate()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketName(java.lang.String)
meth public void setCannedACL(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setKey(java.lang.String)
meth public void setObjectLockLegalHoldStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public void setObjectLockLegalHoldStatus(java.lang.String)
meth public void setObjectLockMode(com.amazonaws.services.s3.model.ObjectLockMode)
meth public void setObjectLockMode(java.lang.String)
meth public void setObjectLockRetainUntilDate(java.util.Date)
meth public void setObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setRedirectLocation(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setTagging(com.amazonaws.services.s3.model.ObjectTagging)
supr com.amazonaws.AmazonWebServiceRequest
hfds accessControlList,bucketName,cannedACL,isRequesterPays,key,objectLockLegalHoldStatus,objectLockMode,objectLockRetainUntilDate,redirectLocation,sseAwsKeyManagementParams,sseCustomerKey,storageClass,tagging

CLSS public com.amazonaws.services.s3.model.InitiateMultipartUploadResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public java.lang.String getAbortRuleId()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public java.util.Date getAbortDate()
meth public void setAbortDate(java.util.Date)
meth public void setAbortRuleId(java.lang.String)
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setRequesterCharged(boolean)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds abortDate,abortRuleId,bucketName,isRequesterCharged,key,uploadId

CLSS public com.amazonaws.services.s3.model.InputSerialization
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.CSVInput getCsv()
meth public com.amazonaws.services.s3.model.InputSerialization clone()
meth public com.amazonaws.services.s3.model.InputSerialization withCompressionType(com.amazonaws.services.s3.model.CompressionType)
meth public com.amazonaws.services.s3.model.InputSerialization withCompressionType(java.lang.String)
meth public com.amazonaws.services.s3.model.InputSerialization withCsv(com.amazonaws.services.s3.model.CSVInput)
meth public com.amazonaws.services.s3.model.InputSerialization withJson(com.amazonaws.services.s3.model.JSONInput)
meth public com.amazonaws.services.s3.model.InputSerialization withParquet(com.amazonaws.services.s3.model.ParquetInput)
meth public com.amazonaws.services.s3.model.JSONInput getJson()
meth public com.amazonaws.services.s3.model.ParquetInput getParquet()
meth public int hashCode()
meth public java.lang.String getCompressionType()
meth public java.lang.String toString()
meth public void setCompressionType(com.amazonaws.services.s3.model.CompressionType)
meth public void setCompressionType(java.lang.String)
meth public void setCsv(com.amazonaws.services.s3.model.CSVInput)
meth public void setJson(com.amazonaws.services.s3.model.JSONInput)
meth public void setParquet(com.amazonaws.services.s3.model.ParquetInput)
supr java.lang.Object
hfds compressionType,csv,json,parquet

CLSS public final com.amazonaws.services.s3.model.InstructionFileId
 anno 0 com.amazonaws.annotation.Immutable()
fld public final static java.lang.String DEFAULT_INSTRUCTION_FILE_SUFFIX = "instruction"
fld public final static java.lang.String DEFAULT_INSTURCTION_FILE_SUFFIX = "instruction"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String DOT = "."
meth public com.amazonaws.services.s3.model.InstructionFileId instructionFileId()
meth public com.amazonaws.services.s3.model.InstructionFileId instructionFileId(java.lang.String)
supr com.amazonaws.services.s3.model.S3ObjectId

CLSS public com.amazonaws.services.s3.model.JSONInput
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.JSONInput withType(com.amazonaws.services.s3.model.JSONType)
meth public com.amazonaws.services.s3.model.JSONInput withType(java.lang.String)
meth public java.lang.String getType()
meth public void setType(com.amazonaws.services.s3.model.JSONType)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds type

CLSS public com.amazonaws.services.s3.model.JSONOutput
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.JSONOutput withRecordDelimiter(java.lang.Character)
meth public com.amazonaws.services.s3.model.JSONOutput withRecordDelimiter(java.lang.String)
meth public java.lang.Character getRecordDelimiter()
meth public java.lang.String getRecordDelimiterAsString()
meth public void setRecordDelimiter(java.lang.Character)
meth public void setRecordDelimiter(java.lang.String)
supr java.lang.Object
hfds recordDelimiter

CLSS public final !enum com.amazonaws.services.s3.model.JSONType
fld public final static com.amazonaws.services.s3.model.JSONType DOCUMENT
fld public final static com.amazonaws.services.s3.model.JSONType LINES
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.JSONType fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.JSONType valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.JSONType[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.JSONType>
hfds jsonType

CLSS public com.amazonaws.services.s3.model.KMSEncryptionMaterials
cons public init(java.lang.String)
fld public final static java.lang.String CUSTOMER_MASTER_KEY_ID = "kms_cmk_id"
intf java.io.Serializable
meth public final boolean isKMSEnabled()
meth public final java.security.KeyPair getKeyPair()
meth public final javax.crypto.SecretKey getSymmetricKey()
meth public java.lang.String getCustomerMasterKeyId()
meth public java.lang.String toString()
supr com.amazonaws.services.s3.model.EncryptionMaterials

CLSS public com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider
cons public init(com.amazonaws.services.s3.model.KMSEncryptionMaterials)
cons public init(java.lang.String)
intf java.io.Serializable
supr com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider

CLSS public com.amazonaws.services.s3.model.LambdaConfiguration
cons public !varargs init(java.lang.String,java.lang.String[])
cons public init(java.lang.String,java.util.EnumSet<com.amazonaws.services.s3.model.S3Event>)
intf java.io.Serializable
meth public java.lang.String getFunctionARN()
supr com.amazonaws.services.s3.model.NotificationConfiguration
hfds functionARN

CLSS public com.amazonaws.services.s3.model.LegacyS3ProgressListener
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.services.s3.model.ProgressListener)
intf com.amazonaws.event.DeliveryMode
intf com.amazonaws.event.ProgressListener
meth public boolean isSyncCallSafe()
meth public com.amazonaws.services.s3.model.ProgressListener unwrap()
meth public void progressChanged(com.amazonaws.event.ProgressEvent)
supr java.lang.Object
hfds listener,syncCallSafe

CLSS public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest withContinuationToken(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getContinuationToken()
meth public void setBucketName(java.lang.String)
meth public void setContinuationToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,continuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult withAnalyticsConfigurationList(java.util.List<com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration>)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult withContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult withNextContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult withTruncated(boolean)
meth public java.lang.String getContinuationToken()
meth public java.lang.String getNextContinuationToken()
meth public java.util.List<com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration> getAnalyticsConfigurationList()
meth public void setAnalyticsConfigurationList(java.util.List<com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration>)
meth public void setContinuationToken(java.lang.String)
meth public void setNextContinuationToken(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds analyticsConfigurationList,continuationToken,isTruncated,nextContinuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest withContinuationToken(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getContinuationToken()
meth public void setBucketName(java.lang.String)
meth public void setContinuationToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,continuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult withContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult withInventoryConfigurationList(java.util.List<com.amazonaws.services.s3.model.inventory.InventoryConfiguration>)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult withNextContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult withTruncated(boolean)
meth public java.lang.String getContinuationToken()
meth public java.lang.String getNextContinuationToken()
meth public java.util.List<com.amazonaws.services.s3.model.inventory.InventoryConfiguration> getInventoryConfigurationList()
meth public void setContinuationToken(java.lang.String)
meth public void setInventoryConfigurationList(java.util.List<com.amazonaws.services.s3.model.inventory.InventoryConfiguration>)
meth public void setNextContinuationToken(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds continuationToken,inventoryConfigurationList,isTruncated,nextContinuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest withContinuationToken(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getContinuationToken()
meth public void setBucketName(java.lang.String)
meth public void setContinuationToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,continuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult withContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult withMetricsConfigurationList(java.util.List<com.amazonaws.services.s3.model.metrics.MetricsConfiguration>)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult withNextContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult withTruncated(boolean)
meth public java.lang.String getContinuationToken()
meth public java.lang.String getNextContinuationToken()
meth public java.util.List<com.amazonaws.services.s3.model.metrics.MetricsConfiguration> getMetricsConfigurationList()
meth public void setContinuationToken(java.lang.String)
meth public void setMetricsConfigurationList(java.util.List<com.amazonaws.services.s3.model.metrics.MetricsConfiguration>)
meth public void setNextContinuationToken(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds continuationToken,isTruncated,metricsConfigurationList,nextContinuationToken

CLSS public com.amazonaws.services.s3.model.ListBucketsRequest
cons public init()
intf com.amazonaws.services.s3.model.S3AccelerateUnsupported
intf java.io.Serializable
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.s3.model.ListMultipartUploadsRequest
cons public init(java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withEncodingType(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withKeyMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withMaxUploads(int)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withUploadIdMarker(java.lang.String)
meth public java.lang.Integer getMaxUploads()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getUploadIdMarker()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxUploads(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setUploadIdMarker(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,encodingType,keyMarker,maxUploads,prefix,uploadIdMarker

CLSS public com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest
cons public init(com.amazonaws.services.s3.model.ObjectListing)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest withPreviousObjectListing(com.amazonaws.services.s3.model.ObjectListing)
meth public com.amazonaws.services.s3.model.ListObjectsRequest toListObjectsRequest()
meth public com.amazonaws.services.s3.model.ObjectListing getPreviousObjectListing()
meth public void setPreviousObjectListing(com.amazonaws.services.s3.model.ObjectListing)
supr com.amazonaws.AmazonWebServiceRequest
hfds previousObjectListing

CLSS public com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest
cons public init(com.amazonaws.services.s3.model.VersionListing)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest withPreviousVersionListing(com.amazonaws.services.s3.model.VersionListing)
meth public com.amazonaws.services.s3.model.ListVersionsRequest toListVersionsRequest()
meth public com.amazonaws.services.s3.model.VersionListing getPreviousVersionListing()
meth public void setPreviousVersionListing(com.amazonaws.services.s3.model.VersionListing)
supr com.amazonaws.AmazonWebServiceRequest
hfds previousVersionListing

CLSS public com.amazonaws.services.s3.model.ListObjectsRequest
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ListObjectsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withEncodingType(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withMaxKeys(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withRequesterPays(boolean)
meth public java.lang.Integer getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getMarker()
meth public java.lang.String getPrefix()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setMarker(java.lang.String)
meth public void setMaxKeys(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setRequesterPays(boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,encodingType,isRequesterPays,marker,maxKeys,prefix

CLSS public com.amazonaws.services.s3.model.ListObjectsV2Request
cons public init()
intf java.io.Serializable
meth public boolean isFetchOwner()
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withContinuationToken(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withEncodingType(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withFetchOwner(boolean)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withMaxKeys(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.ListObjectsV2Request withStartAfter(java.lang.String)
meth public java.lang.Integer getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getContinuationToken()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getPrefix()
meth public java.lang.String getStartAfter()
meth public void setBucketName(java.lang.String)
meth public void setContinuationToken(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setFetchOwner(boolean)
meth public void setMaxKeys(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setStartAfter(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,continuationToken,delimiter,encodingType,fetchOwner,isRequesterPays,maxKeys,prefix,startAfter

CLSS public com.amazonaws.services.s3.model.ListObjectsV2Result
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public int getKeyCount()
meth public int getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getContinuationToken()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getNextContinuationToken()
meth public java.lang.String getPrefix()
meth public java.lang.String getStartAfter()
meth public java.util.List<com.amazonaws.services.s3.model.S3ObjectSummary> getObjectSummaries()
meth public java.util.List<java.lang.String> getCommonPrefixes()
meth public void setBucketName(java.lang.String)
meth public void setCommonPrefixes(java.util.List<java.lang.String>)
meth public void setContinuationToken(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKeyCount(int)
meth public void setMaxKeys(int)
meth public void setNextContinuationToken(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setStartAfter(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds bucketName,commonPrefixes,continuationToken,delimiter,encodingType,isTruncated,keyCount,maxKeys,nextContinuationToken,objectSummaries,prefix,startAfter

CLSS public com.amazonaws.services.s3.model.ListPartsRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ListPartsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withEncodingType(java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withMaxParts(int)
meth public com.amazonaws.services.s3.model.ListPartsRequest withPartNumberMarker(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListPartsRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.ListPartsRequest withUploadId(java.lang.String)
meth public java.lang.Integer getMaxParts()
meth public java.lang.Integer getPartNumberMarker()
meth public java.lang.String getBucketName()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public void setBucketName(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setMaxParts(int)
meth public void setPartNumberMarker(java.lang.Integer)
meth public void setRequesterPays(boolean)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,encodingType,isRequesterPays,key,maxParts,partNumberMarker,uploadId

CLSS public com.amazonaws.services.s3.model.ListVersionsRequest
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ListVersionsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withEncodingType(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withKeyMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withMaxResults(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withVersionIdMarker(java.lang.String)
meth public java.lang.Integer getMaxResults()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getVersionIdMarker()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxResults(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setVersionIdMarker(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,encodingType,keyMarker,maxResults,prefix,versionIdMarker

CLSS public abstract interface com.amazonaws.services.s3.model.MaterialsDescriptionProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()

CLSS public final !enum com.amazonaws.services.s3.model.MetadataDirective
fld public final static com.amazonaws.services.s3.model.MetadataDirective COPY
fld public final static com.amazonaws.services.s3.model.MetadataDirective REPLACE
meth public static com.amazonaws.services.s3.model.MetadataDirective fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.MetadataDirective valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.MetadataDirective[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.MetadataDirective>
hfds value

CLSS public com.amazonaws.services.s3.model.MetadataEntry
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.MetadataEntry clone()
meth public com.amazonaws.services.s3.model.MetadataEntry withName(java.lang.String)
meth public com.amazonaws.services.s3.model.MetadataEntry withValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds name,value

CLSS public com.amazonaws.services.s3.model.MultiFactorAuthentication
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication withDeviceSerialNumber(java.lang.String)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication withToken(java.lang.String)
meth public java.lang.String getDeviceSerialNumber()
meth public java.lang.String getToken()
meth public void setDeviceSerialNumber(java.lang.String)
meth public void setToken(java.lang.String)
supr java.lang.Object
hfds deviceSerialNumber,token

CLSS public com.amazonaws.services.s3.model.MultiObjectDeleteException
cons public init(java.util.Collection<com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError>,java.util.Collection<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject>)
innr public static DeleteError
intf java.io.Serializable
meth public java.lang.String getErrorCode()
meth public java.util.List<com.amazonaws.services.s3.model.DeleteObjectsResult$DeletedObject> getDeletedObjects()
meth public java.util.List<com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError> getErrors()
supr com.amazonaws.services.s3.model.AmazonS3Exception
hfds deletedObjects,errors,serialVersionUID

CLSS public static com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError
 outer com.amazonaws.services.s3.model.MultiObjectDeleteException
cons public init()
intf java.io.Serializable
meth public java.lang.String getCode()
meth public java.lang.String getKey()
meth public java.lang.String getMessage()
meth public java.lang.String getVersionId()
meth public void setCode(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setMessage(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds code,key,message,versionId

CLSS public com.amazonaws.services.s3.model.MultipartUpload
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Owner getInitiator()
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public java.lang.String getUploadId()
meth public java.util.Date getInitiated()
meth public void setInitiated(java.util.Date)
meth public void setInitiator(com.amazonaws.services.s3.model.Owner)
meth public void setKey(java.lang.String)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setStorageClass(java.lang.String)
meth public void setUploadId(java.lang.String)
supr java.lang.Object
hfds initiated,initiator,key,owner,storageClass,uploadId

CLSS public com.amazonaws.services.s3.model.MultipartUploadListing
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public int getMaxUploads()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getNextKeyMarker()
meth public java.lang.String getNextUploadIdMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getUploadIdMarker()
meth public java.util.List<com.amazonaws.services.s3.model.MultipartUpload> getMultipartUploads()
meth public java.util.List<java.lang.String> getCommonPrefixes()
meth public void setBucketName(java.lang.String)
meth public void setCommonPrefixes(java.util.List<java.lang.String>)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxUploads(int)
meth public void setMultipartUploads(java.util.List<com.amazonaws.services.s3.model.MultipartUpload>)
meth public void setNextKeyMarker(java.lang.String)
meth public void setNextUploadIdMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
meth public void setUploadIdMarker(java.lang.String)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,encodingType,isTruncated,keyMarker,maxUploads,multipartUploads,nextKeyMarker,nextUploadIdMarker,prefix,uploadIdMarker

CLSS public abstract com.amazonaws.services.s3.model.NotificationConfiguration
cons protected !varargs init(java.lang.String[])
cons protected init()
cons protected init(java.util.EnumSet<com.amazonaws.services.s3.model.S3Event>)
meth public !varargs com.amazonaws.services.s3.model.NotificationConfiguration withObjectPrefixes(java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.Filter getFilter()
meth public com.amazonaws.services.s3.model.NotificationConfiguration withEvents(java.util.Set<java.lang.String>)
meth public com.amazonaws.services.s3.model.NotificationConfiguration withFilter(com.amazonaws.services.s3.model.Filter)
meth public java.util.List<java.lang.String> getObjectPrefixes()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> getEvents()
meth public void addEvent(com.amazonaws.services.s3.model.S3Event)
meth public void addEvent(java.lang.String)
meth public void addObjectPrefix(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setEvents(java.util.Set<java.lang.String>)
meth public void setFilter(com.amazonaws.services.s3.model.Filter)
meth public void setObjectPrefixes(java.util.List<java.lang.String>)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds events,filter,objectPrefixes

CLSS public com.amazonaws.services.s3.model.ObjectListing
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public int getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getMarker()
meth public java.lang.String getNextMarker()
meth public java.lang.String getPrefix()
meth public java.util.List<com.amazonaws.services.s3.model.S3ObjectSummary> getObjectSummaries()
meth public java.util.List<java.lang.String> getCommonPrefixes()
meth public void setBucketName(java.lang.String)
meth public void setCommonPrefixes(java.util.List<java.lang.String>)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setMarker(java.lang.String)
meth public void setMaxKeys(int)
meth public void setNextMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,encodingType,isTruncated,marker,maxKeys,nextMarker,objectSummaries,prefix

CLSS public com.amazonaws.services.s3.model.ObjectLockConfiguration
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ObjectLockConfiguration withObjectLockEnabled(com.amazonaws.services.s3.model.ObjectLockEnabled)
meth public com.amazonaws.services.s3.model.ObjectLockConfiguration withObjectLockEnabled(java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectLockConfiguration withRule(com.amazonaws.services.s3.model.ObjectLockRule)
meth public com.amazonaws.services.s3.model.ObjectLockRule getRule()
meth public java.lang.String getObjectLockEnabled()
meth public void setObjectLockEnabled(com.amazonaws.services.s3.model.ObjectLockEnabled)
meth public void setObjectLockEnabled(java.lang.String)
meth public void setRule(com.amazonaws.services.s3.model.ObjectLockRule)
supr java.lang.Object
hfds objectLockEnabled,rule

CLSS public final !enum com.amazonaws.services.s3.model.ObjectLockEnabled
fld public final static com.amazonaws.services.s3.model.ObjectLockEnabled ENABLED
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.ObjectLockEnabled fromString(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockEnabled valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockEnabled[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ObjectLockEnabled>
hfds objectLockEnabled

CLSS public com.amazonaws.services.s3.model.ObjectLockLegalHold
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ObjectLockLegalHold withStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public com.amazonaws.services.s3.model.ObjectLockLegalHold withStatus(java.lang.String)
meth public java.lang.String getStatus()
meth public void setStatus(com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds status

CLSS public final !enum com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus
fld public final static com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus OFF
fld public final static com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus ON
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus fromString(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ObjectLockLegalHoldStatus>
hfds objectLockLegalHoldStatus

CLSS public final !enum com.amazonaws.services.s3.model.ObjectLockMode
fld public final static com.amazonaws.services.s3.model.ObjectLockMode COMPLIANCE
fld public final static com.amazonaws.services.s3.model.ObjectLockMode GOVERNANCE
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.ObjectLockMode fromString(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockMode valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockMode[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ObjectLockMode>
hfds objectLockMode

CLSS public com.amazonaws.services.s3.model.ObjectLockRetention
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ObjectLockRetention withMode(com.amazonaws.services.s3.model.ObjectLockRetentionMode)
meth public com.amazonaws.services.s3.model.ObjectLockRetention withMode(java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectLockRetention withRetainUntilDate(java.util.Date)
meth public java.lang.String getMode()
meth public java.util.Date getRetainUntilDate()
meth public void setMode(com.amazonaws.services.s3.model.ObjectLockRetentionMode)
meth public void setMode(java.lang.String)
meth public void setRetainUntilDate(java.util.Date)
supr java.lang.Object
hfds mode,retainUntilDate

CLSS public final !enum com.amazonaws.services.s3.model.ObjectLockRetentionMode
fld public final static com.amazonaws.services.s3.model.ObjectLockRetentionMode COMPLIANCE
fld public final static com.amazonaws.services.s3.model.ObjectLockRetentionMode GOVERNANCE
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.ObjectLockRetentionMode fromString(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockRetentionMode valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ObjectLockRetentionMode[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ObjectLockRetentionMode>
hfds objectLockRetentionMode

CLSS public com.amazonaws.services.s3.model.ObjectLockRule
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DefaultRetention getDefaultRetention()
meth public com.amazonaws.services.s3.model.ObjectLockRule withDefaultRetention(com.amazonaws.services.s3.model.DefaultRetention)
meth public void setDefaultRetention(com.amazonaws.services.s3.model.DefaultRetention)
supr java.lang.Object
hfds defaultRetention

CLSS public com.amazonaws.services.s3.model.ObjectMetadata
cons public init()
fld public final static java.lang.String AES_256_SERVER_SIDE_ENCRYPTION
intf com.amazonaws.services.s3.internal.ObjectExpirationResult
intf com.amazonaws.services.s3.internal.ObjectRestoreResult
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf com.amazonaws.services.s3.internal.ServerSideEncryptionResult
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.ObjectMetadata clone()
meth public java.lang.Boolean getOngoingRestore()
meth public java.lang.Integer getPartCount()
meth public java.lang.Long[] getContentRange()
meth public java.lang.Object getRawMetadataValue(java.lang.String)
meth public java.lang.String getCacheControl()
meth public java.lang.String getContentDisposition()
meth public java.lang.String getContentEncoding()
meth public java.lang.String getContentLanguage()
meth public java.lang.String getContentMD5()
meth public java.lang.String getContentType()
meth public java.lang.String getETag()
meth public java.lang.String getExpirationTimeRuleId()
meth public java.lang.String getObjectLockLegalHoldStatus()
meth public java.lang.String getObjectLockMode()
meth public java.lang.String getReplicationStatus()
meth public java.lang.String getSSEAlgorithm()
meth public java.lang.String getSSEAwsKmsKeyId()
meth public java.lang.String getSSECustomerAlgorithm()
meth public java.lang.String getSSECustomerKeyMd5()
meth public java.lang.String getServerSideEncryption()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getStorageClass()
meth public java.lang.String getUserMetaDataOf(java.lang.String)
meth public java.lang.String getVersionId()
meth public java.util.Date getExpirationTime()
meth public java.util.Date getHttpExpiresDate()
meth public java.util.Date getLastModified()
meth public java.util.Date getObjectLockRetainUntilDate()
meth public java.util.Date getRestoreExpirationTime()
meth public java.util.Map<java.lang.String,java.lang.Object> getRawMetadata()
meth public java.util.Map<java.lang.String,java.lang.String> getUserMetadata()
meth public long getContentLength()
meth public long getInstanceLength()
meth public void addUserMetadata(java.lang.String,java.lang.String)
meth public void setCacheControl(java.lang.String)
meth public void setContentDisposition(java.lang.String)
meth public void setContentEncoding(java.lang.String)
meth public void setContentLanguage(java.lang.String)
meth public void setContentLength(long)
meth public void setContentMD5(java.lang.String)
meth public void setContentType(java.lang.String)
meth public void setExpirationTime(java.util.Date)
meth public void setExpirationTimeRuleId(java.lang.String)
meth public void setHeader(java.lang.String,java.lang.Object)
meth public void setHttpExpiresDate(java.util.Date)
meth public void setLastModified(java.util.Date)
meth public void setOngoingRestore(boolean)
meth public void setRequesterCharged(boolean)
meth public void setRestoreExpirationTime(java.util.Date)
meth public void setSSEAlgorithm(java.lang.String)
meth public void setSSECustomerAlgorithm(java.lang.String)
meth public void setSSECustomerKeyMd5(java.lang.String)
meth public void setServerSideEncryption(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setUserMetadata(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds expirationTime,expirationTimeRuleId,httpExpiresDate,metadata,ongoingRestore,restoreExpirationTime,userMetadata

CLSS public com.amazonaws.services.s3.model.ObjectTagging
cons public init(java.util.List<com.amazonaws.services.s3.model.Tag>)
intf java.io.Serializable
meth public java.util.List<com.amazonaws.services.s3.model.Tag> getTagSet()
meth public void setTagSet(java.util.List<com.amazonaws.services.s3.model.Tag>)
supr java.lang.Object
hfds tagSet

CLSS public com.amazonaws.services.s3.model.OutputLocation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.OutputLocation clone()
meth public com.amazonaws.services.s3.model.OutputLocation withS3(com.amazonaws.services.s3.model.S3Location)
meth public com.amazonaws.services.s3.model.S3Location getS3()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setS3(com.amazonaws.services.s3.model.S3Location)
supr java.lang.Object
hfds s3

CLSS public com.amazonaws.services.s3.model.OutputSerialization
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.CSVOutput getCsv()
meth public com.amazonaws.services.s3.model.JSONOutput getJson()
meth public com.amazonaws.services.s3.model.OutputSerialization clone()
meth public com.amazonaws.services.s3.model.OutputSerialization withCsv(com.amazonaws.services.s3.model.CSVOutput)
meth public com.amazonaws.services.s3.model.OutputSerialization withJson(com.amazonaws.services.s3.model.JSONOutput)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setCsv(com.amazonaws.services.s3.model.CSVOutput)
meth public void setJson(com.amazonaws.services.s3.model.JSONOutput)
supr java.lang.Object
hfds csv,json

CLSS public com.amazonaws.services.s3.model.Owner
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public void setDisplayName(java.lang.String)
meth public void setId(java.lang.String)
supr java.lang.Object
hfds displayName,id,serialVersionUID

CLSS public final !enum com.amazonaws.services.s3.model.OwnerOverride
fld public final static com.amazonaws.services.s3.model.OwnerOverride DESTINATION
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.OwnerOverride fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.OwnerOverride valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.OwnerOverride[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.OwnerOverride>
hfds id

CLSS public com.amazonaws.services.s3.model.ParquetInput
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.PartETag
cons public init(int,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.PartETag withETag(java.lang.String)
meth public com.amazonaws.services.s3.model.PartETag withPartNumber(int)
meth public int getPartNumber()
meth public java.lang.String getETag()
meth public void setETag(java.lang.String)
meth public void setPartNumber(int)
supr java.lang.Object
hfds eTag,partNumber

CLSS public com.amazonaws.services.s3.model.PartListing
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public boolean isTruncated()
meth public com.amazonaws.services.s3.model.Owner getInitiator()
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.Integer getMaxParts()
meth public java.lang.Integer getNextPartNumberMarker()
meth public java.lang.Integer getPartNumberMarker()
meth public java.lang.String getAbortRuleId()
meth public java.lang.String getBucketName()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public java.lang.String getUploadId()
meth public java.util.Date getAbortDate()
meth public java.util.List<com.amazonaws.services.s3.model.PartSummary> getParts()
meth public void setAbortDate(java.util.Date)
meth public void setAbortRuleId(java.lang.String)
meth public void setBucketName(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setInitiator(com.amazonaws.services.s3.model.Owner)
meth public void setKey(java.lang.String)
meth public void setMaxParts(int)
meth public void setNextPartNumberMarker(int)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setPartNumberMarker(int)
meth public void setParts(java.util.List<com.amazonaws.services.s3.model.PartSummary>)
meth public void setRequesterCharged(boolean)
meth public void setStorageClass(java.lang.String)
meth public void setTruncated(boolean)
meth public void setUploadId(java.lang.String)
supr java.lang.Object
hfds abortDate,abortRuleId,bucketName,encodingType,initiator,isRequesterCharged,isTruncated,key,maxParts,nextPartNumberMarker,owner,partNumberMarker,parts,storageClass,uploadId

CLSS public com.amazonaws.services.s3.model.PartSummary
cons public init()
intf java.io.Serializable
meth public int getPartNumber()
meth public java.lang.String getETag()
meth public java.util.Date getLastModified()
meth public long getSize()
meth public void setETag(java.lang.String)
meth public void setLastModified(java.util.Date)
meth public void setPartNumber(int)
meth public void setSize(long)
supr java.lang.Object
hfds eTag,lastModified,partNumber,size

CLSS public final !enum com.amazonaws.services.s3.model.Permission
fld public final static com.amazonaws.services.s3.model.Permission FullControl
fld public final static com.amazonaws.services.s3.model.Permission Read
fld public final static com.amazonaws.services.s3.model.Permission ReadAcp
fld public final static com.amazonaws.services.s3.model.Permission Write
fld public final static com.amazonaws.services.s3.model.Permission WriteAcp
meth public java.lang.String getHeaderName()
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.Permission parsePermission(java.lang.String)
meth public static com.amazonaws.services.s3.model.Permission valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.Permission[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.Permission>
hfds headerName,permissionString

CLSS public com.amazonaws.services.s3.model.PolicyStatus
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.PolicyStatus clone()
meth public com.amazonaws.services.s3.model.PolicyStatus withIsPublic(java.lang.Boolean)
meth public int hashCode()
meth public java.lang.Boolean getIsPublic()
meth public java.lang.String toString()
meth public void setIsPublic(java.lang.Boolean)
supr java.lang.Object
hfds isPublic

CLSS public final com.amazonaws.services.s3.model.PresignedUrlDownloadConfig
cons public init()
meth public boolean isResumeOnRetry()
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadConfig withDownloadSizePerRequest(long)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadConfig withResumeOnRetry(boolean)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadConfig withS3progressListener(com.amazonaws.services.s3.transfer.internal.S3ProgressListener)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadConfig withTimeoutMillis(long)
meth public com.amazonaws.services.s3.transfer.internal.S3ProgressListener getS3progressListener()
meth public long getDownloadSizePerRequest()
meth public long getTimeoutMillis()
meth public void setDownloadSizePerRequest(long)
meth public void setResumeOnRetry(boolean)
meth public void setS3progressListener(com.amazonaws.services.s3.transfer.internal.S3ProgressListener)
meth public void setTimeoutMillis(long)
supr java.lang.Object
hfds DEFAULT_DOWNLOAD_SIZE,DEFAULT_TIMEOUT,downloadSizePerRequest,resumeOnRetry,s3progressListener,timeoutMillis

CLSS public com.amazonaws.services.s3.model.PresignedUrlDownloadRequest
cons public init(java.net.URL)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadRequest clone()
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadRequest withPresignedUrl(java.net.URL)
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadRequest withRange(long,long)
meth public java.net.URL getPresignedUrl()
meth public long[] getRange()
meth public void setPresignedUrl(java.net.URL)
meth public void setRange(long,long)
supr com.amazonaws.AmazonWebServiceRequest
hfds presignedUrl,range

CLSS public final com.amazonaws.services.s3.model.PresignedUrlDownloadResult
cons public init()
meth public com.amazonaws.services.s3.model.PresignedUrlDownloadResult withS3Object(com.amazonaws.services.s3.model.S3Object)
meth public com.amazonaws.services.s3.model.S3Object getS3Object()
meth public void setS3Object(com.amazonaws.services.s3.model.S3Object)
supr java.lang.Object
hfds s3Object

CLSS public com.amazonaws.services.s3.model.PresignedUrlUploadRequest
cons public init(java.net.URL)
intf com.amazonaws.services.s3.model.S3DataSource
intf java.io.Serializable
meth public com.amazonaws.http.HttpMethodName getHttpMethodName()
meth public com.amazonaws.services.s3.model.ObjectMetadata getMetadata()
meth public com.amazonaws.services.s3.model.PresignedUrlUploadRequest withFile(java.io.File)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadRequest withHttpMethodName(com.amazonaws.http.HttpMethodName)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadRequest withInputStream(java.io.InputStream)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadRequest withMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadRequest withPresignedUrl(java.net.URL)
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream()
meth public java.net.URL getPresignedUrl()
meth public void setFile(java.io.File)
meth public void setHttpMethodName(com.amazonaws.http.HttpMethodName)
meth public void setInputStream(java.io.InputStream)
meth public void setMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setPresignedUrl(java.net.URL)
supr com.amazonaws.AmazonWebServiceRequest
hfds file,httpMethodName,inputStream,metadata,presignedUrl,serialVersionUID

CLSS public com.amazonaws.services.s3.model.PresignedUrlUploadResult
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ObjectMetadata getMetadata()
meth public com.amazonaws.services.s3.model.PresignedUrlUploadResult withContentMd5(java.lang.String)
meth public com.amazonaws.services.s3.model.PresignedUrlUploadResult withMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public java.lang.String getContentMd5()
meth public void setContentMd5(java.lang.String)
meth public void setMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
supr java.lang.Object
hfds contentMd5,metadata

CLSS public com.amazonaws.services.s3.model.Progress
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Progress withBytesProcessed(java.lang.Long)
meth public com.amazonaws.services.s3.model.Progress withBytesReturned(java.lang.Long)
meth public com.amazonaws.services.s3.model.Progress withBytesScanned(java.lang.Long)
meth public java.lang.Long getBytesProcessed()
meth public java.lang.Long getBytesReturned()
meth public java.lang.Long getBytesScanned()
meth public void setBytesProcessed(java.lang.Long)
meth public void setBytesReturned(java.lang.Long)
meth public void setBytesScanned(java.lang.Long)
supr java.lang.Object
hfds bytesProcessed,bytesReturned,bytesScanned

CLSS public com.amazonaws.services.s3.model.ProgressEvent
 anno 0 java.lang.Deprecated()
cons public init(com.amazonaws.event.ProgressEventType)
cons public init(int)
cons public init(long)
meth public int getBytesTransfered()
 anno 0 java.lang.Deprecated()
supr com.amazonaws.event.ProgressEvent

CLSS public abstract interface com.amazonaws.services.s3.model.ProgressListener
 anno 0 java.lang.Deprecated()
meth public abstract void progressChanged(com.amazonaws.services.s3.model.ProgressEvent)

CLSS public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration clone()
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration withBlockPublicAcls(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration withBlockPublicPolicy(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration withIgnorePublicAcls(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration withRestrictPublicBuckets(java.lang.Boolean)
meth public int hashCode()
meth public java.lang.Boolean getBlockPublicAcls()
meth public java.lang.Boolean getBlockPublicPolicy()
meth public java.lang.Boolean getIgnorePublicAcls()
meth public java.lang.Boolean getRestrictPublicBuckets()
meth public java.lang.String toString()
meth public void setBlockPublicAcls(java.lang.Boolean)
meth public void setBlockPublicPolicy(java.lang.Boolean)
meth public void setIgnorePublicAcls(java.lang.Boolean)
meth public void setRestrictPublicBuckets(java.lang.Boolean)
supr java.lang.Object
hfds blockPublicAcls,blockPublicPolicy,ignorePublicAcls,restrictPublicBuckets

CLSS public final com.amazonaws.services.s3.model.PutInstructionFileRequest
cons public init(com.amazonaws.services.s3.model.S3ObjectId,com.amazonaws.services.s3.model.EncryptionMaterials,java.lang.String)
cons public init(com.amazonaws.services.s3.model.S3ObjectId,java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
intf com.amazonaws.services.s3.model.EncryptionMaterialsFactory
intf com.amazonaws.services.s3.model.MaterialsDescriptionProvider
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials()
meth public com.amazonaws.services.s3.model.PutInstructionFileRequest withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.PutInstructionFileRequest withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.PutInstructionFileRequest withRedirectLocation(java.lang.String)
meth public com.amazonaws.services.s3.model.PutInstructionFileRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.PutInstructionFileRequest withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest createPutObjectRequest(com.amazonaws.services.s3.model.S3Object)
meth public com.amazonaws.services.s3.model.S3ObjectId getS3ObjectId()
meth public java.lang.String getRedirectLocation()
meth public java.lang.String getStorageClass()
meth public java.lang.String getSuffix()
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setRedirectLocation(java.lang.String)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds accessControlList,cannedAcl,encryptionMaterials,matDesc,redirectLocation,s3ObjectId,storageClass,suffix

CLSS public com.amazonaws.services.s3.model.PutObjectRequest
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.PutObjectRequest clone()
meth public com.amazonaws.services.s3.model.PutObjectRequest withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.PutObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.PutObjectRequest withFile(java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectRequest withInputStream(java.io.InputStream)
meth public com.amazonaws.services.s3.model.PutObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.PutObjectRequest withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.PutObjectRequest withRedirectLocation(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.PutObjectRequest withSSEAwsKeyManagementParams(com.amazonaws.services.s3.model.SSEAwsKeyManagementParams)
meth public com.amazonaws.services.s3.model.PutObjectRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.PutObjectRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.PutObjectRequest withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public void setRequesterPays(boolean)
supr com.amazonaws.services.s3.model.AbstractPutObjectRequest
hfds isRequesterPays

CLSS public com.amazonaws.services.s3.model.PutObjectResult
cons public init()
intf com.amazonaws.services.s3.internal.ObjectExpirationResult
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf com.amazonaws.services.s3.internal.S3VersionResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.ObjectMetadata getMetadata()
meth public java.lang.String getContentMd5()
meth public java.lang.String getETag()
meth public java.lang.String getExpirationTimeRuleId()
meth public java.lang.String getVersionId()
meth public java.util.Date getExpirationTime()
meth public void setContentMd5(java.lang.String)
meth public void setETag(java.lang.String)
meth public void setExpirationTime(java.util.Date)
meth public void setExpirationTimeRuleId(java.lang.String)
meth public void setMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setRequesterCharged(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds contentMd5,eTag,expirationTime,expirationTimeRuleId,isRequesterCharged,metadata,versionId

CLSS public com.amazonaws.services.s3.model.QueueConfiguration
cons public !varargs init(java.lang.String,java.lang.String[])
cons public init()
cons public init(java.lang.String,java.util.EnumSet<com.amazonaws.services.s3.model.S3Event>)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.QueueConfiguration withQueueARN(java.lang.String)
meth public java.lang.String getQueueARN()
meth public void setQueueARN(java.lang.String)
supr com.amazonaws.services.s3.model.NotificationConfiguration
hfds queueARN

CLSS public final !enum com.amazonaws.services.s3.model.QuoteFields
fld public final static com.amazonaws.services.s3.model.QuoteFields ALWAYS
fld public final static com.amazonaws.services.s3.model.QuoteFields ASNEEDED
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.QuoteFields fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.QuoteFields valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.QuoteFields[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.QuoteFields>
hfds value

CLSS public com.amazonaws.services.s3.model.RedirectRule
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RedirectRule withHostName(java.lang.String)
meth public com.amazonaws.services.s3.model.RedirectRule withHttpRedirectCode(java.lang.String)
meth public com.amazonaws.services.s3.model.RedirectRule withProtocol(java.lang.String)
meth public com.amazonaws.services.s3.model.RedirectRule withReplaceKeyPrefixWith(java.lang.String)
meth public com.amazonaws.services.s3.model.RedirectRule withReplaceKeyWith(java.lang.String)
meth public java.lang.String getHostName()
meth public java.lang.String getHttpRedirectCode()
meth public java.lang.String getReplaceKeyPrefixWith()
meth public java.lang.String getReplaceKeyWith()
meth public java.lang.String getprotocol()
meth public void setHostName(java.lang.String)
meth public void setHttpRedirectCode(java.lang.String)
meth public void setProtocol(java.lang.String)
meth public void setReplaceKeyPrefixWith(java.lang.String)
meth public void setReplaceKeyWith(java.lang.String)
supr java.lang.Object
hfds hostName,httpRedirectCode,protocol,replaceKeyPrefixWith,replaceKeyWith

CLSS public final !enum com.amazonaws.services.s3.model.Region
fld public final static com.amazonaws.services.s3.model.Region AP_HongKong
fld public final static com.amazonaws.services.s3.model.Region AP_Mumbai
fld public final static com.amazonaws.services.s3.model.Region AP_Seoul
fld public final static com.amazonaws.services.s3.model.Region AP_Singapore
fld public final static com.amazonaws.services.s3.model.Region AP_Sydney
fld public final static com.amazonaws.services.s3.model.Region AP_Tokyo
fld public final static com.amazonaws.services.s3.model.Region CA_Central
fld public final static com.amazonaws.services.s3.model.Region CN_Beijing
fld public final static com.amazonaws.services.s3.model.Region CN_Northwest_1
fld public final static com.amazonaws.services.s3.model.Region EU_Frankfurt
fld public final static com.amazonaws.services.s3.model.Region EU_Ireland
fld public final static com.amazonaws.services.s3.model.Region EU_London
fld public final static com.amazonaws.services.s3.model.Region EU_North_1
fld public final static com.amazonaws.services.s3.model.Region EU_Paris
fld public final static com.amazonaws.services.s3.model.Region SA_SaoPaulo
fld public final static com.amazonaws.services.s3.model.Region US_East_2
fld public final static com.amazonaws.services.s3.model.Region US_GovCloud
fld public final static com.amazonaws.services.s3.model.Region US_Gov_East_1
fld public final static com.amazonaws.services.s3.model.Region US_Standard
fld public final static com.amazonaws.services.s3.model.Region US_West
fld public final static com.amazonaws.services.s3.model.Region US_West_2
fld public final static java.util.regex.Pattern S3_REGIONAL_ENDPOINT_PATTERN
meth public com.amazonaws.regions.Region toAWSRegion()
meth public java.lang.String getFirstRegionId()
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.Region fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.Region valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.Region[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.Region>
hfds regionIds

CLSS public com.amazonaws.services.s3.model.ReplicationDestinationConfig
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.AccessControlTranslation getAccessControlTranslation()
meth public com.amazonaws.services.s3.model.EncryptionConfiguration getEncryptionConfiguration()
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withAccessControlTranslation(com.amazonaws.services.s3.model.AccessControlTranslation)
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withAccount(java.lang.String)
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withBucketARN(java.lang.String)
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withEncryptionConfiguration(com.amazonaws.services.s3.model.EncryptionConfiguration)
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig withStorageClass(java.lang.String)
meth public java.lang.String getAccount()
meth public java.lang.String getBucketARN()
meth public java.lang.String getStorageClass()
meth public java.lang.String toString()
meth public void setAccessControlTranslation(com.amazonaws.services.s3.model.AccessControlTranslation)
meth public void setAccount(java.lang.String)
meth public void setBucketARN(java.lang.String)
meth public void setEncryptionConfiguration(com.amazonaws.services.s3.model.EncryptionConfiguration)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
supr java.lang.Object
hfds accessControlTranslation,account,bucketARN,encryptionConfiguration,storageClass

CLSS public com.amazonaws.services.s3.model.ReplicationRule
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.DeleteMarkerReplication getDeleteMarkerReplication()
meth public com.amazonaws.services.s3.model.ReplicationDestinationConfig getDestinationConfig()
meth public com.amazonaws.services.s3.model.ReplicationRule withDeleteMarkerReplication(com.amazonaws.services.s3.model.DeleteMarkerReplication)
meth public com.amazonaws.services.s3.model.ReplicationRule withDestinationConfig(com.amazonaws.services.s3.model.ReplicationDestinationConfig)
meth public com.amazonaws.services.s3.model.ReplicationRule withFilter(com.amazonaws.services.s3.model.replication.ReplicationFilter)
meth public com.amazonaws.services.s3.model.ReplicationRule withPrefix(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.ReplicationRule withPriority(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ReplicationRule withSourceSelectionCriteria(com.amazonaws.services.s3.model.SourceSelectionCriteria)
meth public com.amazonaws.services.s3.model.ReplicationRule withStatus(com.amazonaws.services.s3.model.ReplicationRuleStatus)
meth public com.amazonaws.services.s3.model.ReplicationRule withStatus(java.lang.String)
meth public com.amazonaws.services.s3.model.SourceSelectionCriteria getSourceSelectionCriteria()
meth public com.amazonaws.services.s3.model.replication.ReplicationFilter getFilter()
meth public java.lang.Integer getPriority()
meth public java.lang.String getPrefix()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public void setDeleteMarkerReplication(com.amazonaws.services.s3.model.DeleteMarkerReplication)
meth public void setDestinationConfig(com.amazonaws.services.s3.model.ReplicationDestinationConfig)
meth public void setFilter(com.amazonaws.services.s3.model.replication.ReplicationFilter)
meth public void setPrefix(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setPriority(java.lang.Integer)
meth public void setSourceSelectionCriteria(com.amazonaws.services.s3.model.SourceSelectionCriteria)
meth public void setStatus(com.amazonaws.services.s3.model.ReplicationRuleStatus)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds deleteMarkerReplication,destinationConfig,filter,prefix,priority,sourceSelectionCriteria,status

CLSS public final !enum com.amazonaws.services.s3.model.ReplicationRuleStatus
fld public final static com.amazonaws.services.s3.model.ReplicationRuleStatus Disabled
fld public final static com.amazonaws.services.s3.model.ReplicationRuleStatus Enabled
meth public java.lang.String getStatus()
meth public static com.amazonaws.services.s3.model.ReplicationRuleStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.ReplicationRuleStatus[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.ReplicationRuleStatus>
hfds status

CLSS public com.amazonaws.services.s3.model.RequestPaymentConfiguration
cons public init(com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer)
innr public final static !enum Payer
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer getPayer()
meth public void setPayer(com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer)
supr java.lang.Object
hfds payer

CLSS public final static !enum com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer
 outer com.amazonaws.services.s3.model.RequestPaymentConfiguration
fld public final static com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer BucketOwner
fld public final static com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer Requester
meth public static com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.RequestPaymentConfiguration$Payer>

CLSS public com.amazonaws.services.s3.model.RequestProgress
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RequestProgress withEnabled(java.lang.Boolean)
meth public java.lang.Boolean getEnabled()
meth public void setEnabled(java.lang.Boolean)
supr java.lang.Object
hfds enabled

CLSS public com.amazonaws.services.s3.model.ResponseHeaderOverrides
cons public init()
fld public final static java.lang.String RESPONSE_HEADER_CACHE_CONTROL = "response-cache-control"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_DISPOSITION = "response-content-disposition"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_ENCODING = "response-content-encoding"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_LANGUAGE = "response-content-language"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_TYPE = "response-content-type"
fld public final static java.lang.String RESPONSE_HEADER_EXPIRES = "response-expires"
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withCacheControl(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withContentDisposition(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withContentEncoding(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withContentLanguage(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withContentType(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides withExpires(java.lang.String)
meth public java.lang.String getCacheControl()
meth public java.lang.String getContentDisposition()
meth public java.lang.String getContentEncoding()
meth public java.lang.String getContentLanguage()
meth public java.lang.String getContentType()
meth public java.lang.String getExpires()
meth public void setCacheControl(java.lang.String)
meth public void setContentDisposition(java.lang.String)
meth public void setContentEncoding(java.lang.String)
meth public void setContentLanguage(java.lang.String)
meth public void setContentType(java.lang.String)
meth public void setExpires(java.lang.String)
supr java.lang.Object
hfds PARAMETER_ORDER,cacheControl,contentDisposition,contentEncoding,contentLanguage,contentType,expires

CLSS public com.amazonaws.services.s3.model.RestoreObjectRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,int)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.GlacierJobParameters getGlacierJobParameters()
meth public com.amazonaws.services.s3.model.OutputLocation getOutputLocation()
meth public com.amazonaws.services.s3.model.RestoreObjectRequest clone()
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withDescription(java.lang.String)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withExpirationInDays(int)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withGlacierJobParameters(com.amazonaws.services.s3.model.GlacierJobParameters)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withOutputLocation(com.amazonaws.services.s3.model.OutputLocation)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withSelectParameters(com.amazonaws.services.s3.model.SelectParameters)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withTier(com.amazonaws.services.s3.model.Tier)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withTier(java.lang.String)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withType(com.amazonaws.services.s3.model.RestoreRequestType)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withType(java.lang.String)
meth public com.amazonaws.services.s3.model.RestoreObjectRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectParameters getSelectParameters()
meth public int getExpirationInDays()
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String getDescription()
meth public java.lang.String getKey()
meth public java.lang.String getTier()
meth public java.lang.String getType()
meth public java.lang.String getVersionId()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setExpirationInDays(int)
meth public void setGlacierJobParameters(com.amazonaws.services.s3.model.GlacierJobParameters)
meth public void setKey(java.lang.String)
meth public void setOutputLocation(com.amazonaws.services.s3.model.OutputLocation)
meth public void setRequesterPays(boolean)
meth public void setSelectParameters(com.amazonaws.services.s3.model.SelectParameters)
meth public void setTier(java.lang.String)
meth public void setType(java.lang.String)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,description,expirationInDays,glacierJobParameters,isRequesterPays,key,outputLocation,selectParameters,tier,type,versionId

CLSS public com.amazonaws.services.s3.model.RestoreObjectResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf com.amazonaws.services.s3.internal.S3RestoreOutputPathResult
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean isRequesterCharged()
meth public int hashCode()
meth public java.lang.String getRestoreOutputPath()
meth public java.lang.String toString()
meth public void setRequesterCharged(boolean)
meth public void setRestoreOutputPath(java.lang.String)
supr java.lang.Object
hfds isRequesterCharged,restoreOutputPath

CLSS public final !enum com.amazonaws.services.s3.model.RestoreRequestType
fld public final static com.amazonaws.services.s3.model.RestoreRequestType SELECT
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.RestoreRequestType fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.RestoreRequestType valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.RestoreRequestType[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.RestoreRequestType>
hfds type

CLSS public com.amazonaws.services.s3.model.RoutingRule
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RedirectRule getRedirect()
meth public com.amazonaws.services.s3.model.RoutingRule withCondition(com.amazonaws.services.s3.model.RoutingRuleCondition)
meth public com.amazonaws.services.s3.model.RoutingRule withRedirect(com.amazonaws.services.s3.model.RedirectRule)
meth public com.amazonaws.services.s3.model.RoutingRuleCondition getCondition()
meth public void setCondition(com.amazonaws.services.s3.model.RoutingRuleCondition)
meth public void setRedirect(com.amazonaws.services.s3.model.RedirectRule)
supr java.lang.Object
hfds condition,redirect

CLSS public com.amazonaws.services.s3.model.RoutingRuleCondition
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RoutingRuleCondition withHttpErrorCodeReturnedEquals(java.lang.String)
meth public com.amazonaws.services.s3.model.RoutingRuleCondition withKeyPrefixEquals(java.lang.String)
meth public java.lang.String getHttpErrorCodeReturnedEquals()
meth public java.lang.String getKeyPrefixEquals()
meth public void setHttpErrorCodeReturnedEquals(java.lang.String)
meth public void setKeyPrefixEquals(java.lang.String)
supr java.lang.Object
hfds httpErrorCodeReturnedEquals,keyPrefixEquals

CLSS public abstract interface com.amazonaws.services.s3.model.S3AccelerateUnsupported

CLSS public abstract interface com.amazonaws.services.s3.model.S3DataSource
innr public final static !enum Utils
meth public abstract java.io.File getFile()
meth public abstract java.io.InputStream getInputStream()
meth public abstract void setFile(java.io.File)
meth public abstract void setInputStream(java.io.InputStream)

CLSS public final static !enum com.amazonaws.services.s3.model.S3DataSource$Utils
 outer com.amazonaws.services.s3.model.S3DataSource
meth public static com.amazonaws.services.s3.model.S3DataSource$Utils valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.S3DataSource$Utils[] values()
meth public static void cleanupDataSource(com.amazonaws.services.s3.model.S3DataSource,java.io.File,java.io.InputStream,java.io.InputStream,org.apache.commons.logging.Log)
supr java.lang.Enum<com.amazonaws.services.s3.model.S3DataSource$Utils>

CLSS public final !enum com.amazonaws.services.s3.model.S3Event
fld public final static com.amazonaws.services.s3.model.S3Event ObjectCreated
fld public final static com.amazonaws.services.s3.model.S3Event ObjectCreatedByCompleteMultipartUpload
fld public final static com.amazonaws.services.s3.model.S3Event ObjectCreatedByCopy
fld public final static com.amazonaws.services.s3.model.S3Event ObjectCreatedByPost
fld public final static com.amazonaws.services.s3.model.S3Event ObjectCreatedByPut
fld public final static com.amazonaws.services.s3.model.S3Event ObjectRemoved
fld public final static com.amazonaws.services.s3.model.S3Event ObjectRemovedDelete
fld public final static com.amazonaws.services.s3.model.S3Event ObjectRemovedDeleteMarkerCreated
fld public final static com.amazonaws.services.s3.model.S3Event ObjectRestoreCompleted
fld public final static com.amazonaws.services.s3.model.S3Event ObjectRestorePost
fld public final static com.amazonaws.services.s3.model.S3Event ReducedRedundancyLostObject
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.S3Event valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.S3Event[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.S3Event>
hfds event

CLSS public com.amazonaws.services.s3.model.S3KeyFilter
cons public init()
innr public final static !enum FilterRuleName
intf java.io.Serializable
meth public !varargs com.amazonaws.services.s3.model.S3KeyFilter withFilterRules(com.amazonaws.services.s3.model.FilterRule[])
meth public com.amazonaws.services.s3.model.S3KeyFilter withFilterRules(java.util.List<com.amazonaws.services.s3.model.FilterRule>)
meth public java.util.List<com.amazonaws.services.s3.model.FilterRule> getFilterRules()
meth public void addFilterRule(com.amazonaws.services.s3.model.FilterRule)
meth public void setFilterRules(java.util.List<com.amazonaws.services.s3.model.FilterRule>)
supr java.lang.Object
hfds filterRules

CLSS public final static !enum com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName
 outer com.amazonaws.services.s3.model.S3KeyFilter
fld public final static com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName Prefix
fld public final static com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName Suffix
meth public com.amazonaws.services.s3.model.FilterRule newRule()
meth public com.amazonaws.services.s3.model.FilterRule newRule(java.lang.String)
meth public static com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.S3KeyFilter$FilterRuleName>

CLSS public com.amazonaws.services.s3.model.S3Location
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.AccessControlList getAccessControlList()
meth public com.amazonaws.services.s3.model.Encryption getEncryption()
meth public com.amazonaws.services.s3.model.ObjectTagging getTagging()
meth public com.amazonaws.services.s3.model.S3Location clone()
meth public com.amazonaws.services.s3.model.S3Location withAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public com.amazonaws.services.s3.model.S3Location withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.S3Location withCannedACL(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.S3Location withCannedACL(java.lang.String)
meth public com.amazonaws.services.s3.model.S3Location withEncryption(com.amazonaws.services.s3.model.Encryption)
meth public com.amazonaws.services.s3.model.S3Location withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.S3Location withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.S3Location withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.S3Location withTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public com.amazonaws.services.s3.model.S3Location withUserMetaData(java.util.List<com.amazonaws.services.s3.model.MetadataEntry>)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String getCannedACL()
meth public java.lang.String getPrefix()
meth public java.lang.String getStorageClass()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.MetadataEntry> getUserMetadata()
meth public void setAccessControlList(com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketName(java.lang.String)
meth public void setCannedACL(java.lang.String)
meth public void setEncryption(com.amazonaws.services.s3.model.Encryption)
meth public void setPrefix(java.lang.String)
meth public void setStorageClass(java.lang.String)
meth public void setTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public void setUserMetadata(java.util.List<com.amazonaws.services.s3.model.MetadataEntry>)
supr java.lang.Object
hfds accessControlList,bucketName,cannedACL,encryption,prefix,storageClass,tagging,userMetadata

CLSS public com.amazonaws.services.s3.model.S3Object
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Closeable
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata()
meth public com.amazonaws.services.s3.model.S3ObjectInputStream getObjectContent()
meth public java.lang.Integer getTaggingCount()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getRedirectLocation()
meth public java.lang.String toString()
meth public void close() throws java.io.IOException
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setObjectContent(com.amazonaws.services.s3.model.S3ObjectInputStream)
meth public void setObjectContent(java.io.InputStream)
meth public void setObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setRedirectLocation(java.lang.String)
meth public void setRequesterCharged(boolean)
meth public void setTaggingCount(java.lang.Integer)
supr java.lang.Object
hfds bucketName,isRequesterCharged,key,metadata,objectContent,redirectLocation,serialVersionUID,taggingCount

CLSS public com.amazonaws.services.s3.model.S3ObjectId
 anno 0 com.amazonaws.annotation.Immutable()
cons public init(com.amazonaws.services.s3.model.S3ObjectIdBuilder)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.InstructionFileId instructionFileId()
meth public com.amazonaws.services.s3.model.InstructionFileId instructionFileId(java.lang.String)
meth public java.lang.String getBucket()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public java.lang.String toString()
supr java.lang.Object
hfds bucket,key,versionId

CLSS public final com.amazonaws.services.s3.model.S3ObjectIdBuilder
cons public init()
cons public init(com.amazonaws.services.s3.model.S3ObjectId)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.S3ObjectId build()
meth public com.amazonaws.services.s3.model.S3ObjectIdBuilder withBucket(java.lang.String)
meth public com.amazonaws.services.s3.model.S3ObjectIdBuilder withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.S3ObjectIdBuilder withVersionId(java.lang.String)
meth public int hashCode()
meth public java.lang.String getBucket()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucket(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds bucket,key,versionId

CLSS public com.amazonaws.services.s3.model.S3ObjectInputStream
cons public init(java.io.InputStream,org.apache.http.client.methods.HttpRequestBase)
cons public init(java.io.InputStream,org.apache.http.client.methods.HttpRequestBase,boolean)
meth public int available() throws java.io.IOException
meth public org.apache.http.client.methods.HttpRequestBase getHttpRequest()
meth public void abort()
meth public void close() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds httpRequest

CLSS public com.amazonaws.services.s3.model.S3ObjectSummary
cons public init()
fld protected com.amazonaws.services.s3.model.Owner owner
fld protected java.lang.String bucketName
fld protected java.lang.String eTag
fld protected java.lang.String key
fld protected java.lang.String storageClass
fld protected java.util.Date lastModified
fld protected long size
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getBucketName()
meth public java.lang.String getETag()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public java.lang.String toString()
meth public java.util.Date getLastModified()
meth public long getSize()
meth public void setBucketName(java.lang.String)
meth public void setETag(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setLastModified(java.util.Date)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setSize(long)
meth public void setStorageClass(java.lang.String)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.S3VersionSummary
cons public init()
fld protected java.lang.String bucketName
intf java.io.Serializable
meth public boolean isDeleteMarker()
meth public boolean isLatest()
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getBucketName()
meth public java.lang.String getETag()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public java.lang.String getVersionId()
meth public java.util.Date getLastModified()
meth public long getSize()
meth public void setBucketName(java.lang.String)
meth public void setETag(java.lang.String)
meth public void setIsDeleteMarker(boolean)
meth public void setIsLatest(boolean)
meth public void setKey(java.lang.String)
meth public void setLastModified(java.util.Date)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setSize(long)
meth public void setStorageClass(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds eTag,isDeleteMarker,isLatest,key,lastModified,owner,size,storageClass,versionId

CLSS public final !enum com.amazonaws.services.s3.model.SSEAlgorithm
fld public final static com.amazonaws.services.s3.model.SSEAlgorithm AES256
fld public final static com.amazonaws.services.s3.model.SSEAlgorithm KMS
meth public java.lang.String getAlgorithm()
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.SSEAlgorithm fromString(java.lang.String)
meth public static com.amazonaws.services.s3.model.SSEAlgorithm getDefault()
meth public static com.amazonaws.services.s3.model.SSEAlgorithm valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.SSEAlgorithm[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.SSEAlgorithm>
hfds algorithm

CLSS public com.amazonaws.services.s3.model.SSEAwsKeyManagementParams
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
meth public java.lang.String getAwsKmsKeyId()
meth public java.lang.String getEncryption()
supr java.lang.Object
hfds awsKmsKeyId

CLSS public abstract interface com.amazonaws.services.s3.model.SSEAwsKeyManagementParamsProvider
meth public abstract com.amazonaws.services.s3.model.SSEAwsKeyManagementParams getSSEAwsKeyManagementParams()

CLSS public com.amazonaws.services.s3.model.SSECustomerKey
cons public init(byte[])
cons public init(java.lang.String)
cons public init(javax.crypto.SecretKey)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.SSECustomerKey withAlgorithm(java.lang.String)
meth public com.amazonaws.services.s3.model.SSECustomerKey withMd5(java.lang.String)
meth public java.lang.String getAlgorithm()
meth public java.lang.String getKey()
meth public java.lang.String getMd5()
meth public static com.amazonaws.services.s3.model.SSECustomerKey generateSSECustomerKeyForPresignUrl(java.lang.String)
meth public void setAlgorithm(java.lang.String)
meth public void setMd5(java.lang.String)
supr java.lang.Object
hfds algorithm,base64EncodedKey,base64EncodedMd5

CLSS public abstract interface com.amazonaws.services.s3.model.SSECustomerKeyProvider
meth public abstract com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()

CLSS public com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
innr public static ContinuationEvent
innr public static EndEvent
innr public static ProgressEvent
innr public static RecordsEvent
innr public static StatsEvent
intf java.io.Serializable
intf java.lang.Cloneable
meth public com.amazonaws.services.s3.model.SelectObjectContentEvent clone()
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr java.lang.Object

CLSS public static com.amazonaws.services.s3.model.SelectObjectContentEvent$ContinuationEvent
 outer com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr com.amazonaws.services.s3.model.SelectObjectContentEvent

CLSS public static com.amazonaws.services.s3.model.SelectObjectContentEvent$EndEvent
 outer com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr com.amazonaws.services.s3.model.SelectObjectContentEvent

CLSS public static com.amazonaws.services.s3.model.SelectObjectContentEvent$ProgressEvent
 outer com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
meth public com.amazonaws.services.s3.model.Progress getDetails()
meth public com.amazonaws.services.s3.model.SelectObjectContentEvent$ProgressEvent withDetails(com.amazonaws.services.s3.model.Progress)
meth public void setDetails(com.amazonaws.services.s3.model.Progress)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr com.amazonaws.services.s3.model.SelectObjectContentEvent
hfds details

CLSS public static com.amazonaws.services.s3.model.SelectObjectContentEvent$RecordsEvent
 outer com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
meth public com.amazonaws.services.s3.model.SelectObjectContentEvent$RecordsEvent withPayload(java.nio.ByteBuffer)
meth public java.nio.ByteBuffer getPayload()
meth public void setPayload(java.nio.ByteBuffer)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr com.amazonaws.services.s3.model.SelectObjectContentEvent
hfds payload

CLSS public static com.amazonaws.services.s3.model.SelectObjectContentEvent$StatsEvent
 outer com.amazonaws.services.s3.model.SelectObjectContentEvent
cons public init()
meth public com.amazonaws.services.s3.model.SelectObjectContentEvent$StatsEvent withDetails(com.amazonaws.services.s3.model.Stats)
meth public com.amazonaws.services.s3.model.Stats getDetails()
meth public void setDetails(com.amazonaws.services.s3.model.Stats)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr com.amazonaws.services.s3.model.SelectObjectContentEvent
hfds details

CLSS public final com.amazonaws.services.s3.model.SelectObjectContentEventException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.String getErrorCode()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getMessage()
meth public void setErrorCode(java.lang.String)
meth public void setErrorMessage(java.lang.String)
supr com.amazonaws.SdkClientException
hfds errorCode,errorMessage

CLSS public com.amazonaws.services.s3.model.SelectObjectContentEventStream
 anno 0 com.amazonaws.annotation.NotThreadSafe()
cons public init(com.amazonaws.internal.SdkFilterInputStream)
intf java.io.Closeable
meth public com.amazonaws.services.s3.model.SelectRecordsInputStream getRecordsInputStream()
meth public com.amazonaws.services.s3.model.SelectRecordsInputStream getRecordsInputStream(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
meth public java.util.Iterator<com.amazonaws.services.s3.model.SelectObjectContentEvent> getEventsIterator()
meth public java.util.List<com.amazonaws.services.s3.model.SelectObjectContentEvent> getAllEvents()
meth public void abort()
meth public void close() throws java.io.IOException
meth public void visitAllEvents(com.amazonaws.services.s3.model.SelectObjectContentEventVisitor)
supr java.lang.Object
hfds EMPTY_INPUT_STREAM,inputStream,readOptionChosen
hcls EventStreamEnumeration,LazyLoadedIterator,SelectEventIterator

CLSS public abstract com.amazonaws.services.s3.model.SelectObjectContentEventVisitor
cons public init()
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEvent$ContinuationEvent)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEvent$EndEvent)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEvent$ProgressEvent)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEvent$RecordsEvent)
meth public void visit(com.amazonaws.services.s3.model.SelectObjectContentEvent$StatsEvent)
meth public void visitDefault(com.amazonaws.services.s3.model.SelectObjectContentEvent)
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SelectObjectContentRequest
cons public init()
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
intf java.lang.Cloneable
meth public com.amazonaws.services.s3.model.InputSerialization getInputSerialization()
meth public com.amazonaws.services.s3.model.OutputSerialization getOutputSerialization()
meth public com.amazonaws.services.s3.model.RequestProgress getRequestProgress()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withExpression(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withExpressionType(com.amazonaws.services.s3.model.ExpressionType)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withExpressionType(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withInputSerialization(com.amazonaws.services.s3.model.InputSerialization)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withOutputSerialization(com.amazonaws.services.s3.model.OutputSerialization)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withRequestProgress(com.amazonaws.services.s3.model.RequestProgress)
meth public com.amazonaws.services.s3.model.SelectObjectContentRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public java.lang.String getBucketName()
meth public java.lang.String getExpression()
meth public java.lang.String getExpressionType()
meth public java.lang.String getKey()
meth public void setBucketName(java.lang.String)
meth public void setExpression(java.lang.String)
meth public void setExpressionType(com.amazonaws.services.s3.model.ExpressionType)
meth public void setExpressionType(java.lang.String)
meth public void setInputSerialization(com.amazonaws.services.s3.model.InputSerialization)
meth public void setKey(java.lang.String)
meth public void setOutputSerialization(com.amazonaws.services.s3.model.OutputSerialization)
meth public void setRequestProgress(com.amazonaws.services.s3.model.RequestProgress)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,expression,expressionType,inputSerialization,key,outputSerialization,requestProgress,sseCustomerKey

CLSS public com.amazonaws.services.s3.model.SelectObjectContentResult
cons public init()
intf java.io.Closeable
meth public com.amazonaws.services.s3.model.SelectObjectContentEventStream getPayload()
meth public com.amazonaws.services.s3.model.SelectObjectContentResult withPayload(com.amazonaws.services.s3.model.SelectObjectContentEventStream)
meth public void close() throws java.io.IOException
meth public void setPayload(com.amazonaws.services.s3.model.SelectObjectContentEventStream)
supr java.lang.Object
hfds payload

CLSS public com.amazonaws.services.s3.model.SelectParameters
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.InputSerialization getInputSerialization()
meth public com.amazonaws.services.s3.model.OutputSerialization getOutputSerialization()
meth public com.amazonaws.services.s3.model.SelectParameters clone()
meth public com.amazonaws.services.s3.model.SelectParameters withExpression(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectParameters withExpressionType(com.amazonaws.services.s3.model.ExpressionType)
meth public com.amazonaws.services.s3.model.SelectParameters withExpressionType(java.lang.String)
meth public com.amazonaws.services.s3.model.SelectParameters withInputSerialization(com.amazonaws.services.s3.model.InputSerialization)
meth public com.amazonaws.services.s3.model.SelectParameters withOutputSerialization(com.amazonaws.services.s3.model.OutputSerialization)
meth public int hashCode()
meth public java.lang.String getExpression()
meth public java.lang.String getExpressionType()
meth public java.lang.String toString()
meth public void setExpression(java.lang.String)
meth public void setExpressionType(java.lang.String)
meth public void setInputSerialization(com.amazonaws.services.s3.model.InputSerialization)
meth public void setOutputSerialization(com.amazonaws.services.s3.model.OutputSerialization)
supr java.lang.Object
hfds expression,expressionType,inputSerialization,outputSerialization

CLSS public com.amazonaws.services.s3.model.SelectRecordsInputStream
meth public void abort()
meth public void close() throws java.io.IOException
supr com.amazonaws.internal.SdkFilterInputStream
hfds abortableHttpStream

CLSS public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault clone()
meth public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault withKMSMasterKeyID(java.lang.String)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault withSSEAlgorithm(com.amazonaws.services.s3.model.SSEAlgorithm)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault withSSEAlgorithm(java.lang.String)
meth public int hashCode()
meth public java.lang.String getKMSMasterKeyID()
meth public java.lang.String getSSEAlgorithm()
meth public java.lang.String toString()
meth public void setKMSMasterKeyID(java.lang.String)
meth public void setSSEAlgorithm(java.lang.String)
supr java.lang.Object
hfds kmsMasterKeyID,sseAlgorithm

CLSS public com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public !varargs com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration withRules(com.amazonaws.services.s3.model.ServerSideEncryptionRule[])
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration clone()
meth public com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration withRules(java.util.Collection<com.amazonaws.services.s3.model.ServerSideEncryptionRule>)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.ServerSideEncryptionRule> getRules()
meth public void setRules(java.util.Collection<com.amazonaws.services.s3.model.ServerSideEncryptionRule>)
supr java.lang.Object
hfds rules

CLSS public com.amazonaws.services.s3.model.ServerSideEncryptionRule
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionByDefault getApplyServerSideEncryptionByDefault()
meth public com.amazonaws.services.s3.model.ServerSideEncryptionRule clone()
meth public com.amazonaws.services.s3.model.ServerSideEncryptionRule withApplyServerSideEncryptionByDefault(com.amazonaws.services.s3.model.ServerSideEncryptionByDefault)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setApplyServerSideEncryptionByDefault(com.amazonaws.services.s3.model.ServerSideEncryptionByDefault)
supr java.lang.Object
hfds applyServerSideEncryptionByDefault

CLSS public com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public com.amazonaws.services.s3.model.BucketAccelerateConfiguration getAccelerateConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest withAccelerateConfiguration(com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setAccelerateConfiguration(com.amazonaws.services.s3.model.BucketAccelerateConfiguration)
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds accelerateConfiguration,bucketName

CLSS public com.amazonaws.services.s3.model.SetBucketAclRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
cons public init(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.AccessControlList getAcl()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public java.lang.String getBucketName()
supr com.amazonaws.AmazonWebServiceRequest
hfds acl,bucketName,cannedAcl

CLSS public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest
cons public init()
cons public init(java.lang.String,com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest withAnalyticsConfiguration(com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration getAnalyticsConfiguration()
meth public java.lang.String getBucketName()
meth public void setAnalyticsConfiguration(com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration)
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds analyticsConfiguration,bucketName

CLSS public com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketCrossOriginConfiguration getCrossOriginConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest withCrossOriginConfiguration(com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setCrossOriginConfiguration(com.amazonaws.services.s3.model.BucketCrossOriginConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,crossOriginConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketEncryptionRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration getServerSideEncryptionConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketEncryptionRequest clone()
meth public com.amazonaws.services.s3.model.SetBucketEncryptionRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketEncryptionRequest withServerSideEncryptionConfiguration(com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
meth public void setServerSideEncryptionConfiguration(com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,serverSideEncryptionConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketEncryptionResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.SetBucketEncryptionResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest
cons public init()
cons public init(java.lang.String,com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest withInventoryConfiguration(com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
meth public com.amazonaws.services.s3.model.inventory.InventoryConfiguration getInventoryConfiguration()
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setInventoryConfiguration(com.amazonaws.services.s3.model.inventory.InventoryConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,inventoryConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketLifecycleConfiguration getLifecycleConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest withLifecycleConfiguration(com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setLifecycleConfiguration(com.amazonaws.services.s3.model.BucketLifecycleConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,lifecycleConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketLoggingConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getLoggingConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest withLoggingConfiguration(com.amazonaws.services.s3.model.BucketLoggingConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setLoggingConfiguration(com.amazonaws.services.s3.model.BucketLoggingConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,loggingConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest
cons public init()
cons public init(java.lang.String,com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest withMetricsConfiguration(com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
meth public com.amazonaws.services.s3.model.metrics.MetricsConfiguration getMetricsConfiguration()
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setMetricsConfiguration(com.amazonaws.services.s3.model.metrics.MetricsConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,metricsConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest
cons public init(com.amazonaws.services.s3.model.BucketNotificationConfiguration,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getNotificationConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest withNotificationConfiguration(com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public java.lang.String getBucket()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getBucketName()
meth public void setBucket(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setBucketName(java.lang.String)
meth public void setBucketNotificationConfiguration(com.amazonaws.services.s3.model.BucketNotificationConfiguration)
 anno 0 java.lang.Deprecated()
meth public void setNotificationConfiguration(com.amazonaws.services.s3.model.BucketNotificationConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,notificationConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketPolicyRequest
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.SetBucketPolicyRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketPolicyRequest withConfirmRemoveSelfBucketAccess(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.SetBucketPolicyRequest withPolicyText(java.lang.String)
meth public java.lang.Boolean getConfirmRemoveSelfBucketAccess()
meth public java.lang.String getBucketName()
meth public java.lang.String getPolicyText()
meth public void setBucketName(java.lang.String)
meth public void setConfirmRemoveSelfBucketAccess(java.lang.Boolean)
meth public void setPolicyText(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,confirmRemoveSelfBucketAccess,policyText

CLSS public com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest
cons public init()
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketReplicationConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketReplicationConfiguration getReplicationConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest withReplicationConfiguration(com.amazonaws.services.s3.model.BucketReplicationConfiguration)
meth public com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest withToken(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getToken()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
meth public void setReplicationConfiguration(com.amazonaws.services.s3.model.BucketReplicationConfiguration)
meth public void setToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,replicationConfiguration,token

CLSS public com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketTaggingConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketTaggingConfiguration getTaggingConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest withTaggingConfiguration(com.amazonaws.services.s3.model.BucketTaggingConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setTaggingConfiguration(com.amazonaws.services.s3.model.BucketTaggingConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,taggingConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketVersioningConfiguration)
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketVersioningConfiguration,com.amazonaws.services.s3.model.MultiFactorAuthentication)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getVersioningConfiguration()
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication getMfa()
meth public com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest withMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest withVersioningConfiguration(com.amazonaws.services.s3.model.BucketVersioningConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public void setVersioningConfiguration(com.amazonaws.services.s3.model.BucketVersioningConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,mfa,versioningConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest withConfiguration(com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setConfiguration(com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,configuration

CLSS public com.amazonaws.services.s3.model.SetObjectAclRequest
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
cons public init(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
cons public init(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.AccessControlList getAcl()
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.SetObjectAclRequest withRequesterPays(boolean)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setRequesterPays(boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds acl,bucketName,cannedAcl,isRequesterPays,key,versionId

CLSS public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest
cons public init()
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ObjectLockLegalHold getLegalHold()
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest withLegalHold(com.amazonaws.services.s3.model.ObjectLockLegalHold)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setLegalHold(com.amazonaws.services.s3.model.ObjectLockLegalHold)
meth public void setRequesterPays(boolean)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,isRequesterPays,key,legalHold,versionId

CLSS public com.amazonaws.services.s3.model.SetObjectLegalHoldResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.SetObjectLegalHoldResult withRequesterCharged(java.lang.Boolean)
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds requesterCharged

CLSS public com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest
cons public init()
intf java.io.Serializable
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ObjectLockConfiguration getObjectLockConfiguration()
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest withObjectLockConfiguration(com.amazonaws.services.s3.model.ObjectLockConfiguration)
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest withToken(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getToken()
meth public void setBucketName(java.lang.String)
meth public void setObjectLockConfiguration(com.amazonaws.services.s3.model.ObjectLockConfiguration)
meth public void setRequesterPays(boolean)
meth public void setToken(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,isRequesterPays,objectLockConfiguration,token

CLSS public com.amazonaws.services.s3.model.SetObjectLockConfigurationResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.SetObjectLockConfigurationResult withRequesterCharged(java.lang.Boolean)
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds requesterCharged

CLSS public com.amazonaws.services.s3.model.SetObjectRetentionRequest
cons public init()
intf java.io.Serializable
meth public boolean getBypassGovernanceRetention()
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ObjectLockRetention getRetention()
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withBypassGovernanceRetention(boolean)
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withRetention(com.amazonaws.services.s3.model.ObjectLockRetention)
meth public com.amazonaws.services.s3.model.SetObjectRetentionRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setBypassGovernanceRetention(boolean)
meth public void setKey(java.lang.String)
meth public void setRequesterPays(boolean)
meth public void setRetention(com.amazonaws.services.s3.model.ObjectLockRetention)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,bypassGovernanceRetention,isRequesterPays,key,retention,versionId

CLSS public com.amazonaws.services.s3.model.SetObjectRetentionResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.SetObjectRetentionResult withRequesterCharged(java.lang.Boolean)
meth public void setRequesterCharged(boolean)
supr java.lang.Object
hfds requesterCharged

CLSS public com.amazonaws.services.s3.model.SetObjectTaggingRequest
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.ObjectTagging)
cons public init(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.ObjectTagging)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.ObjectTagging getTagging()
meth public com.amazonaws.services.s3.model.SetObjectTaggingRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectTaggingRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.SetObjectTaggingRequest withTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public com.amazonaws.services.s3.model.SetObjectTaggingRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setTagging(com.amazonaws.services.s3.model.ObjectTagging)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,tagging,versionId

CLSS public com.amazonaws.services.s3.model.SetObjectTaggingResult
cons public init()
meth public com.amazonaws.services.s3.model.SetObjectTaggingResult withVersionId(java.lang.String)
meth public java.lang.String getVersionId()
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds versionId

CLSS public com.amazonaws.services.s3.model.SetPublicAccessBlockRequest
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.PublicAccessBlockConfiguration getPublicAccessBlockConfiguration()
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockRequest clone()
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockRequest withPublicAccessBlockConfiguration(com.amazonaws.services.s3.model.PublicAccessBlockConfiguration)
meth public int hashCode()
meth public java.lang.String getBucketName()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
meth public void setPublicAccessBlockConfiguration(com.amazonaws.services.s3.model.PublicAccessBlockConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,publicAccessBlockConfiguration

CLSS public com.amazonaws.services.s3.model.SetPublicAccessBlockResult
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.SetPublicAccessBlockResult clone()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.services.s3.model.SetRequestPaymentConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.RequestPaymentConfiguration)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.RequestPaymentConfiguration getConfiguration()
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setConfiguration(com.amazonaws.services.s3.model.RequestPaymentConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,configuration

CLSS public com.amazonaws.services.s3.model.SimpleMaterialProvider
cons public init()
intf com.amazonaws.services.s3.model.EncryptionMaterialsProvider
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials()
meth public com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials(java.util.Map<java.lang.String,java.lang.String>)
meth public com.amazonaws.services.s3.model.SimpleMaterialProvider addMaterial(com.amazonaws.services.s3.model.EncryptionMaterials)
meth public com.amazonaws.services.s3.model.SimpleMaterialProvider removeMaterial(java.util.Map<java.lang.String,java.lang.String>)
meth public com.amazonaws.services.s3.model.SimpleMaterialProvider withLatest(com.amazonaws.services.s3.model.EncryptionMaterials)
meth public int size()
meth public void refresh()
supr java.lang.Object
hfds latest,map

CLSS public com.amazonaws.services.s3.model.SourceSelectionCriteria
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.SourceSelectionCriteria clone()
meth public com.amazonaws.services.s3.model.SourceSelectionCriteria withSseKmsEncryptedObjects(com.amazonaws.services.s3.model.SseKmsEncryptedObjects)
meth public com.amazonaws.services.s3.model.SseKmsEncryptedObjects getSseKmsEncryptedObjects()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setSseKmsEncryptedObjects(com.amazonaws.services.s3.model.SseKmsEncryptedObjects)
supr java.lang.Object
hfds sseKmsEncryptedObjects

CLSS public com.amazonaws.services.s3.model.SseKmsEncryptedObjects
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.SseKmsEncryptedObjects clone()
meth public com.amazonaws.services.s3.model.SseKmsEncryptedObjects withStatus(com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus)
meth public com.amazonaws.services.s3.model.SseKmsEncryptedObjects withStatus(java.lang.String)
meth public int hashCode()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds status

CLSS public final !enum com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus
fld public final static com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus DISABLED
fld public final static com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus ENABLED
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.SseKmsEncryptedObjectsStatus>
hfds value

CLSS public com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider
cons public init(com.amazonaws.services.s3.model.EncryptionMaterials)
intf com.amazonaws.services.s3.model.EncryptionMaterialsProvider
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials()
meth public com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials(java.util.Map<java.lang.String,java.lang.String>)
meth public void refresh()
supr java.lang.Object
hfds materials

CLSS public com.amazonaws.services.s3.model.Stats
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.Stats withBytesProcessed(java.lang.Long)
meth public com.amazonaws.services.s3.model.Stats withBytesReturned(java.lang.Long)
meth public com.amazonaws.services.s3.model.Stats withBytesScanned(java.lang.Long)
meth public java.lang.Long getBytesProcessed()
meth public java.lang.Long getBytesReturned()
meth public java.lang.Long getBytesScanned()
meth public void setBytesProcessed(java.lang.Long)
meth public void setBytesReturned(java.lang.Long)
meth public void setBytesScanned(java.lang.Long)
supr java.lang.Object
hfds bytesProcessed,bytesReturned,bytesScanned

CLSS public final !enum com.amazonaws.services.s3.model.StorageClass
fld public final static com.amazonaws.services.s3.model.StorageClass DeepArchive
fld public final static com.amazonaws.services.s3.model.StorageClass Glacier
fld public final static com.amazonaws.services.s3.model.StorageClass IntelligentTiering
fld public final static com.amazonaws.services.s3.model.StorageClass OneZoneInfrequentAccess
fld public final static com.amazonaws.services.s3.model.StorageClass ReducedRedundancy
fld public final static com.amazonaws.services.s3.model.StorageClass Standard
fld public final static com.amazonaws.services.s3.model.StorageClass StandardInfrequentAccess
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.StorageClass fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.StorageClass valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.StorageClass[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.StorageClass>
hfds storageClassId

CLSS public com.amazonaws.services.s3.model.Tag
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.Tag withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.Tag withValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String getKey()
meth public java.lang.String getValue()
meth public void setKey(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds key,value

CLSS public com.amazonaws.services.s3.model.TagSet
cons public init()
cons public init(java.util.Map<java.lang.String,java.lang.String>)
intf java.io.Serializable
meth public java.lang.String getTag(java.lang.String)
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getAllTags()
meth public void setTag(java.lang.String,java.lang.String)
supr java.lang.Object
hfds tags

CLSS public final !enum com.amazonaws.services.s3.model.Tier
fld public final static com.amazonaws.services.s3.model.Tier Bulk
fld public final static com.amazonaws.services.s3.model.Tier Expedited
fld public final static com.amazonaws.services.s3.model.Tier Standard
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.Tier fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.Tier valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.Tier[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.Tier>
hfds value

CLSS public com.amazonaws.services.s3.model.TopicConfiguration
cons public !varargs init(java.lang.String,java.lang.String[])
cons public init()
cons public init(java.lang.String,java.util.EnumSet<com.amazonaws.services.s3.model.S3Event>)
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.TopicConfiguration withTopicARN(java.lang.String)
meth public java.lang.String getTopicARN()
meth public void setTopicARN(java.lang.String)
supr com.amazonaws.services.s3.model.NotificationConfiguration
hfds topicARN

CLSS public com.amazonaws.services.s3.model.UploadObjectRequest
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
intf com.amazonaws.services.s3.model.MaterialsDescriptionProvider
intf java.io.Serializable
meth public <%0 extends com.amazonaws.services.s3.model.UploadObjectRequest> {%%0} withUploadPartMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.UploadObjectObserver getUploadObjectObserver()
meth public com.amazonaws.services.s3.internal.MultiFileOutputStream getMultiFileOutputStream()
meth public com.amazonaws.services.s3.model.ObjectMetadata getUploadPartMetadata()
meth public com.amazonaws.services.s3.model.UploadObjectRequest clone()
meth public com.amazonaws.services.s3.model.UploadObjectRequest withDiskLimit(long)
meth public com.amazonaws.services.s3.model.UploadObjectRequest withExecutorService(java.util.concurrent.ExecutorService)
meth public com.amazonaws.services.s3.model.UploadObjectRequest withMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
meth public com.amazonaws.services.s3.model.UploadObjectRequest withMultiFileOutputStream(com.amazonaws.services.s3.internal.MultiFileOutputStream)
meth public com.amazonaws.services.s3.model.UploadObjectRequest withPartSize(long)
meth public com.amazonaws.services.s3.model.UploadObjectRequest withUploadObjectObserver(com.amazonaws.services.s3.UploadObjectObserver)
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public java.util.concurrent.ExecutorService getExecutorService()
meth public long getDiskLimit()
meth public long getPartSize()
meth public void setMaterialsDescription(java.util.Map<java.lang.String,java.lang.String>)
meth public void setUploadPartMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
supr com.amazonaws.services.s3.model.AbstractPutObjectRequest
hfds MIN_PART_SIZE,diskLimit,executorService,materialsDescription,multiFileOutputStream,partSize,serialVersionUID,uploadObjectObserver,uploadPartMetadata

CLSS public com.amazonaws.services.s3.model.UploadPartRequest
cons public init()
intf com.amazonaws.services.s3.model.S3DataSource
intf com.amazonaws.services.s3.model.SSECustomerKeyProvider
intf java.io.Serializable
meth public boolean isLastPart()
meth public boolean isRequesterPays()
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata()
meth public com.amazonaws.services.s3.model.ProgressListener getProgressListener()
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.SSECustomerKey getSSECustomerKey()
meth public com.amazonaws.services.s3.model.UploadPartRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withFile(java.io.File)
meth public com.amazonaws.services.s3.model.UploadPartRequest withFileOffset(long)
meth public com.amazonaws.services.s3.model.UploadPartRequest withInputStream(java.io.InputStream)
meth public com.amazonaws.services.s3.model.UploadPartRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withLastPart(boolean)
meth public com.amazonaws.services.s3.model.UploadPartRequest withMD5Digest(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.UploadPartRequest withPartNumber(int)
meth public com.amazonaws.services.s3.model.UploadPartRequest withPartSize(long)
meth public com.amazonaws.services.s3.model.UploadPartRequest withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public com.amazonaws.services.s3.model.UploadPartRequest withRequesterPays(boolean)
meth public com.amazonaws.services.s3.model.UploadPartRequest withSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public com.amazonaws.services.s3.model.UploadPartRequest withUploadId(java.lang.String)
meth public int getPartNumber()
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getMd5Digest()
meth public java.lang.String getUploadId()
meth public long getFileOffset()
meth public long getPartSize()
meth public void setBucketName(java.lang.String)
meth public void setFile(java.io.File)
meth public void setFileOffset(long)
meth public void setInputStream(java.io.InputStream)
meth public void setKey(java.lang.String)
meth public void setLastPart(boolean)
meth public void setMd5Digest(java.lang.String)
meth public void setObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setPartNumber(int)
meth public void setPartSize(long)
meth public void setProgressListener(com.amazonaws.services.s3.model.ProgressListener)
 anno 0 java.lang.Deprecated()
meth public void setRequesterPays(boolean)
meth public void setSSECustomerKey(com.amazonaws.services.s3.model.SSECustomerKey)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,file,fileOffset,inputStream,isLastPart,isRequesterPays,key,md5Digest,objectMetadata,partNumber,partSize,serialVersionUID,sseCustomerKey,uploadId

CLSS public com.amazonaws.services.s3.model.UploadPartResult
cons public init()
intf com.amazonaws.services.s3.internal.S3RequesterChargedResult
intf java.io.Serializable
meth public boolean isRequesterCharged()
meth public com.amazonaws.services.s3.model.PartETag getPartETag()
meth public int getPartNumber()
meth public java.lang.String getETag()
meth public void setETag(java.lang.String)
meth public void setPartNumber(int)
meth public void setRequesterCharged(boolean)
supr com.amazonaws.services.s3.internal.SSEResultBase
hfds eTag,isRequesterCharged,partNumber

CLSS public com.amazonaws.services.s3.model.VersionListing
cons public init()
intf java.io.Serializable
meth public boolean isTruncated()
meth public int getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getEncodingType()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getNextKeyMarker()
meth public java.lang.String getNextVersionIdMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getVersionIdMarker()
meth public java.util.List<com.amazonaws.services.s3.model.S3VersionSummary> getVersionSummaries()
meth public java.util.List<java.lang.String> getCommonPrefixes()
meth public void setBucketName(java.lang.String)
meth public void setCommonPrefixes(java.util.List<java.lang.String>)
meth public void setDelimiter(java.lang.String)
meth public void setEncodingType(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxKeys(int)
meth public void setNextKeyMarker(java.lang.String)
meth public void setNextVersionIdMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
meth public void setVersionIdMarker(java.lang.String)
meth public void setVersionSummaries(java.util.List<com.amazonaws.services.s3.model.S3VersionSummary>)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,encodingType,isTruncated,keyMarker,maxKeys,nextKeyMarker,nextVersionIdMarker,prefix,versionIdMarker,versionSummaries

CLSS public com.amazonaws.services.s3.model.WebsiteConfiguration
cons public init()
intf java.io.Serializable
meth public com.amazonaws.services.s3.model.WebsiteConfiguration withIndexDocumentSuffix(java.lang.String)
meth public com.amazonaws.services.s3.model.WebsiteConfiguration withRedirectAllRequestsTo(java.lang.String)
meth public com.amazonaws.services.s3.model.WebsiteConfiguration withRoutingRule(java.util.List<com.amazonaws.services.s3.model.RoutingRule>)
meth public com.amazonaws.services.s3.model.WebsiteConfiguration witherrorDocument(java.lang.String)
meth public java.lang.String getErrorDocument()
meth public java.lang.String getIndexDocumentSuffix()
meth public java.lang.String getRedirectAllRequestsTo()
meth public java.util.List<com.amazonaws.services.s3.model.RoutingRule> getRoutingRule()
meth public void setErrorDocument(java.lang.String)
meth public void setIndexDocumentSuffix(java.lang.String)
meth public void setRedirectAllRequestsTo(java.lang.String)
meth public void setRoutingRules(java.util.List<com.amazonaws.services.s3.model.RoutingRule>)
supr java.lang.Object
hfds errorDocument,indexDocumentSuffix,redirectAllRequestsTo,routingRules

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FilterInputStream
cons protected init(java.io.InputStream)
fld protected volatile java.io.InputStream in
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.SecurityException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

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

CLSS public java.security.DigestInputStream
cons public init(java.io.InputStream,java.security.MessageDigest)
fld protected java.security.MessageDigest digest
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.lang.String toString()
meth public java.security.MessageDigest getMessageDigest()
meth public void on(boolean)
meth public void setMessageDigest(java.security.MessageDigest)
supr java.io.FilterInputStream

