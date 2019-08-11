#Signature file v4.1
#Version 1.15

CLSS public com.amazonaws.AmazonClientException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public com.amazonaws.AmazonServiceException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
innr public final static !enum ErrorType
meth public com.amazonaws.AmazonServiceException$ErrorType getErrorType()
meth public int getStatusCode()
meth public java.lang.String getErrorCode()
meth public java.lang.String getRequestId()
meth public java.lang.String getServiceName()
meth public java.lang.String toString()
meth public void setErrorCode(java.lang.String)
meth public void setErrorType(com.amazonaws.AmazonServiceException$ErrorType)
meth public void setRequestId(java.lang.String)
meth public void setServiceName(java.lang.String)
meth public void setStatusCode(int)
supr com.amazonaws.AmazonClientException
hfds errorCode,errorType,requestId,serialVersionUID,serviceName,statusCode

CLSS public final static !enum com.amazonaws.AmazonServiceException$ErrorType
 outer com.amazonaws.AmazonServiceException
fld public final static com.amazonaws.AmazonServiceException$ErrorType Client
fld public final static com.amazonaws.AmazonServiceException$ErrorType Service
fld public final static com.amazonaws.AmazonServiceException$ErrorType Unknown
meth public static com.amazonaws.AmazonServiceException$ErrorType valueOf(java.lang.String)
meth public static com.amazonaws.AmazonServiceException$ErrorType[] values()
supr java.lang.Enum<com.amazonaws.AmazonServiceException$ErrorType>

CLSS public abstract com.amazonaws.AmazonWebServiceClient
cons public init(com.amazonaws.ClientConfiguration)
fld protected final com.amazonaws.ClientConfiguration clientConfiguration
fld protected final com.amazonaws.http.AmazonHttpClient client
fld protected final java.util.List<com.amazonaws.handlers.RequestHandler> requestHandlers
fld protected java.net.URI endpoint
meth protected <%0 extends java.lang.Object> com.amazonaws.http.HttpRequest convertToHttpRequest(com.amazonaws.Request<{%%0}>,com.amazonaws.http.HttpMethodName)
 anno 0 java.lang.Deprecated()
meth protected com.amazonaws.http.ExecutionContext createExecutionContext()
meth public void addRequestHandler(com.amazonaws.handlers.RequestHandler)
meth public void removeRequestHandler(com.amazonaws.handlers.RequestHandler)
meth public void setEndpoint(java.lang.String)
meth public void shutdown()
supr java.lang.Object

CLSS public abstract com.amazonaws.AmazonWebServiceRequest
cons public init()
meth public com.amazonaws.RequestClientOptions getRequestClientOptions()
meth public com.amazonaws.auth.AWSCredentials getRequestCredentials()
meth public java.util.Map<java.lang.String,java.lang.String> copyPrivateRequestParameters()
meth public void setRequestCredentials(com.amazonaws.auth.AWSCredentials)
supr java.lang.Object
hfds credentials,requestClientOptions

CLSS public com.amazonaws.AmazonWebServiceResponse<%0 extends java.lang.Object>
cons public init()
meth public com.amazonaws.ResponseMetadata getResponseMetadata()
meth public java.lang.String getRequestId()
meth public void setResponseMetadata(com.amazonaws.ResponseMetadata)
meth public void setResult({com.amazonaws.AmazonWebServiceResponse%0})
meth public {com.amazonaws.AmazonWebServiceResponse%0} getResult()
supr java.lang.Object
hfds responseMetadata,result

CLSS public com.amazonaws.ClientConfiguration
cons public init()
fld public final static java.lang.String DEFAULT_USER_AGENT
meth public com.amazonaws.ClientConfiguration withConnectionTimeout(int)
meth public com.amazonaws.ClientConfiguration withMaxConnections(int)
meth public com.amazonaws.ClientConfiguration withMaxErrorRetry(int)
meth public com.amazonaws.ClientConfiguration withProtocol(com.amazonaws.Protocol)
meth public com.amazonaws.ClientConfiguration withProxyDomain(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyHost(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyPassword(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyPort(int)
meth public com.amazonaws.ClientConfiguration withProxyUsername(java.lang.String)
meth public com.amazonaws.ClientConfiguration withProxyWorkstation(java.lang.String)
meth public com.amazonaws.ClientConfiguration withSocketBufferSizeHints(int,int)
meth public com.amazonaws.ClientConfiguration withSocketTimeout(int)
meth public com.amazonaws.ClientConfiguration withUserAgent(java.lang.String)
meth public com.amazonaws.Protocol getProtocol()
meth public int getConnectionTimeout()
meth public int getMaxConnections()
meth public int getMaxErrorRetry()
meth public int getProxyPort()
meth public int getSocketTimeout()
meth public int[] getSocketBufferSizeHints()
meth public java.lang.String getProxyDomain()
meth public java.lang.String getProxyHost()
meth public java.lang.String getProxyPassword()
meth public java.lang.String getProxyUsername()
meth public java.lang.String getProxyWorkstation()
meth public java.lang.String getUserAgent()
meth public void setConnectionTimeout(int)
meth public void setMaxConnections(int)
meth public void setMaxErrorRetry(int)
meth public void setProtocol(com.amazonaws.Protocol)
meth public void setProxyDomain(java.lang.String)
meth public void setProxyHost(java.lang.String)
meth public void setProxyPassword(java.lang.String)
meth public void setProxyPort(int)
meth public void setProxyUsername(java.lang.String)
meth public void setProxyWorkstation(java.lang.String)
meth public void setSocketBufferSizeHints(int,int)
meth public void setSocketTimeout(int)
meth public void setUserAgent(java.lang.String)
supr java.lang.Object
hfds connectionTimeout,maxConnections,maxErrorRetry,protocol,proxyDomain,proxyHost,proxyPassword,proxyPort,proxyUsername,proxyWorkstation,socketReceiveBufferSizeHint,socketSendBufferSizeHint,socketTimeout,userAgent

CLSS public com.amazonaws.DefaultRequest<%0 extends java.lang.Object>
cons public init(com.amazonaws.AmazonWebServiceRequest,java.lang.String)
cons public init(java.lang.String)
intf com.amazonaws.Request<{com.amazonaws.DefaultRequest%0}>
meth public com.amazonaws.AmazonWebServiceRequest getOriginalRequest()
meth public com.amazonaws.Request<{com.amazonaws.DefaultRequest%0}> withParameter(java.lang.String,java.lang.String)
meth public com.amazonaws.http.HttpMethodName getHttpMethod()
meth public java.io.InputStream getContent()
meth public java.lang.String getResourcePath()
meth public java.lang.String getServiceName()
meth public java.lang.String toString()
meth public java.net.URI getEndpoint()
meth public java.util.Map<java.lang.String,java.lang.String> getHeaders()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
meth public void addHeader(java.lang.String,java.lang.String)
meth public void addParameter(java.lang.String,java.lang.String)
meth public void setContent(java.io.InputStream)
meth public void setEndpoint(java.net.URI)
meth public void setHttpMethod(com.amazonaws.http.HttpMethodName)
meth public void setResourcePath(java.lang.String)
supr java.lang.Object
hfds content,endpoint,headers,httpMethod,originalRequest,parameters,resourcePath,serviceName

CLSS public final !enum com.amazonaws.HttpMethod
fld public final static com.amazonaws.HttpMethod DELETE
fld public final static com.amazonaws.HttpMethod GET
fld public final static com.amazonaws.HttpMethod HEAD
fld public final static com.amazonaws.HttpMethod POST
fld public final static com.amazonaws.HttpMethod PUT
meth public static com.amazonaws.HttpMethod valueOf(java.lang.String)
meth public static com.amazonaws.HttpMethod[] values()
supr java.lang.Enum<com.amazonaws.HttpMethod>

CLSS public final !enum com.amazonaws.Protocol
fld public final static com.amazonaws.Protocol HTTP
fld public final static com.amazonaws.Protocol HTTPS
meth public java.lang.String toString()
meth public static com.amazonaws.Protocol valueOf(java.lang.String)
meth public static com.amazonaws.Protocol[] values()
supr java.lang.Enum<com.amazonaws.Protocol>
hfds protocol

CLSS public abstract interface com.amazonaws.Request<%0 extends java.lang.Object>
meth public abstract com.amazonaws.AmazonWebServiceRequest getOriginalRequest()
meth public abstract com.amazonaws.Request<{com.amazonaws.Request%0}> withParameter(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.http.HttpMethodName getHttpMethod()
meth public abstract java.io.InputStream getContent()
meth public abstract java.lang.String getResourcePath()
meth public abstract java.lang.String getServiceName()
meth public abstract java.net.URI getEndpoint()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getHeaders()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getParameters()
meth public abstract void addHeader(java.lang.String,java.lang.String)
meth public abstract void addParameter(java.lang.String,java.lang.String)
meth public abstract void setContent(java.io.InputStream)
meth public abstract void setEndpoint(java.net.URI)
meth public abstract void setHttpMethod(com.amazonaws.http.HttpMethodName)
meth public abstract void setResourcePath(java.lang.String)

CLSS public final com.amazonaws.RequestClientOptions
cons public init()
meth public java.lang.String getClientMarker()
meth public void addClientMarker(java.lang.String)
supr java.lang.Object
hfds clientMarker

CLSS public com.amazonaws.ResponseMetadata
cons public init(com.amazonaws.ResponseMetadata)
cons public init(java.util.Map<java.lang.String,java.lang.String>)
fld protected final java.util.Map<java.lang.String,java.lang.String> metadata
fld public final static java.lang.String AWS_REQUEST_ID = "AWS_REQUEST_ID"
meth public java.lang.String getRequestId()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.amazonaws.auth.AWS3Signer
cons public init()
fld protected final static com.amazonaws.util.DateUtils dateUtils
meth protected java.lang.String getCanonicalizedHeadersForStringToSign(com.amazonaws.Request<?>)
meth protected java.util.List<java.lang.String> getHeadersForStringToSign(com.amazonaws.Request<?>)
meth public void sign(com.amazonaws.Request<?>,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner
hfds AUTHORIZATION_HEADER,HTTPS_SCHEME,HTTP_SCHEME,NONCE_HEADER,log,overriddenDate

CLSS public abstract interface com.amazonaws.auth.AWSCredentials
meth public abstract java.lang.String getAWSAccessKeyId()
meth public abstract java.lang.String getAWSSecretKey()

CLSS public abstract com.amazonaws.auth.AbstractAWSSigner
cons public init()
intf com.amazonaws.auth.Signer
meth protected com.amazonaws.auth.AWSCredentials sanitizeCredentials(com.amazonaws.auth.AWSCredentials)
meth protected java.lang.String getCanonicalizedEndpoint(java.net.URI)
meth protected java.lang.String getCanonicalizedQueryString(java.util.Map<java.lang.String,java.lang.String>)
meth protected java.lang.String getCanonicalizedResourcePath(java.net.URI)
meth protected java.lang.String sign(byte[],java.lang.String,com.amazonaws.auth.SigningAlgorithm)
meth protected java.lang.String sign(java.lang.String,java.lang.String,com.amazonaws.auth.SigningAlgorithm)
supr java.lang.Object
hfds DEFAULT_ENCODING

CLSS public com.amazonaws.auth.BasicAWSCredentials
cons public init(java.lang.String,java.lang.String)
intf com.amazonaws.auth.AWSCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
supr java.lang.Object
hfds accessKey,secretKey

CLSS public com.amazonaws.auth.PropertiesCredentials
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
intf com.amazonaws.auth.AWSCredentials
meth public java.lang.String getAWSAccessKeyId()
meth public java.lang.String getAWSSecretKey()
supr java.lang.Object
hfds accessKey,secretAccessKey

CLSS public com.amazonaws.auth.QueryStringSigner
cons public init()
intf com.amazonaws.auth.Signer
meth public void sign(com.amazonaws.Request<?>,com.amazonaws.auth.AWSCredentials)
meth public void sign(com.amazonaws.Request<?>,com.amazonaws.auth.SignatureVersion,com.amazonaws.auth.SigningAlgorithm,com.amazonaws.auth.AWSCredentials)
supr com.amazonaws.auth.AbstractAWSSigner

CLSS public final !enum com.amazonaws.auth.SignatureVersion
fld public final static com.amazonaws.auth.SignatureVersion V1
fld public final static com.amazonaws.auth.SignatureVersion V2
meth public java.lang.String toString()
meth public static com.amazonaws.auth.SignatureVersion valueOf(java.lang.String)
meth public static com.amazonaws.auth.SignatureVersion[] values()
supr java.lang.Enum<com.amazonaws.auth.SignatureVersion>
hfds value

CLSS public abstract interface com.amazonaws.auth.Signer
meth public abstract void sign(com.amazonaws.Request<?>,com.amazonaws.auth.AWSCredentials)

CLSS public final !enum com.amazonaws.auth.SigningAlgorithm
fld public final static com.amazonaws.auth.SigningAlgorithm HmacSHA1
fld public final static com.amazonaws.auth.SigningAlgorithm HmacSHA256
meth public static com.amazonaws.auth.SigningAlgorithm valueOf(java.lang.String)
meth public static com.amazonaws.auth.SigningAlgorithm[] values()
supr java.lang.Enum<com.amazonaws.auth.SigningAlgorithm>

CLSS public abstract interface com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public abstract com.amazonaws.ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult checkDNSAvailability(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult createApplication(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult createApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult createConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult createEnvironment(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult describeConfigurationOptions(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult describeConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult describeEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks()
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult retrieveEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult terminateEnvironment(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult updateApplication(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult updateApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult updateConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult updateEnvironment(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public abstract com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult validateConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public abstract void deleteApplication(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public abstract void deleteApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public abstract void deleteConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public abstract void deleteEnvironmentConfiguration(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public abstract void rebuildEnvironment(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public abstract void requestEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public abstract void restartAppServer(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
meth public abstract void setEndpoint(java.lang.String)
meth public abstract void shutdown()

CLSS public abstract interface com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public abstract java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public abstract java.util.concurrent.Future<java.lang.Void> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)

CLSS public com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsyncClient
cons public init(com.amazonaws.auth.AWSCredentials)
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration,java.util.concurrent.ExecutorService)
cons public init(com.amazonaws.auth.AWSCredentials,java.util.concurrent.ExecutorService)
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkAsync
meth public java.util.concurrent.ExecutorService getExecutorService()
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult> checkDNSAvailabilityAsync(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult> createApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult> createApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult> createConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult> createEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult> createStorageLocationAsync(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult> describeApplicationVersionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult> describeApplicationsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult> describeConfigurationOptionsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult> describeConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult> describeEnvironmentResourcesAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult> describeEnvironmentsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult> describeEventsAsync(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult> listAvailableSolutionStacksAsync(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult> retrieveEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult> terminateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult> updateApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult> updateApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult> updateConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult> updateEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public java.util.concurrent.Future<com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult> validateConfigurationSettingsAsync(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public java.util.concurrent.Future<java.lang.Void> deleteApplicationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public java.util.concurrent.Future<java.lang.Void> deleteApplicationVersionAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public java.util.concurrent.Future<java.lang.Void> deleteConfigurationTemplateAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public java.util.concurrent.Future<java.lang.Void> deleteEnvironmentConfigurationAsync(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public java.util.concurrent.Future<java.lang.Void> rebuildEnvironmentAsync(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public java.util.concurrent.Future<java.lang.Void> requestEnvironmentInfoAsync(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public java.util.concurrent.Future<java.lang.Void> restartAppServerAsync(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
supr com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
hfds executorService

CLSS public com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient
cons public init(com.amazonaws.auth.AWSCredentials)
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration)
fld protected final java.util.List<com.amazonaws.transform.Unmarshaller<com.amazonaws.AmazonServiceException,org.w3c.dom.Node>> exceptionUnmarshallers
intf com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk
meth public com.amazonaws.ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult checkDNSAvailability(com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult createApplication(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult createApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult createConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult createEnvironment(com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult createStorageLocation(com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult describeApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult describeApplications(com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult describeConfigurationOptions(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult describeConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult describeEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult describeEnvironments(com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult describeEvents(com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks()
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult listAvailableSolutionStacks(com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult retrieveEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult terminateEnvironment(com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult updateApplication(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult updateApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult updateConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult updateEnvironment(com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult validateConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest)
meth public void deleteApplication(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest)
meth public void deleteApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest)
meth public void deleteConfigurationTemplate(com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest)
meth public void deleteEnvironmentConfiguration(com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest)
meth public void rebuildEnvironment(com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest)
meth public void requestEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest)
meth public void restartAppServer(com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest)
supr com.amazonaws.AmazonWebServiceClient
hfds awsCredentials,signer

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withConfigurationTemplates(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withVersions(java.lang.String[])
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withConfigurationTemplates(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription withVersions(java.util.Collection<java.lang.String>)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<java.lang.String> getConfigurationTemplates()
meth public java.util.List<java.lang.String> getVersions()
meth public void setApplicationName(java.lang.String)
meth public void setConfigurationTemplates(java.util.Collection<java.lang.String>)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setVersions(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds applicationName,configurationTemplates,dateCreated,dateUpdated,description,versions

CLSS public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location getSourceBundle()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,dateCreated,dateUpdated,description,sourceBundle,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup withName(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest withCNAMEPrefix(java.lang.String)
meth public java.lang.String getCNAMEPrefix()
meth public java.lang.String toString()
meth public void setCNAMEPrefix(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds cNAMEPrefix

CLSS public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult withAvailable(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult withFullyQualifiedCNAME(java.lang.String)
meth public java.lang.Boolean getAvailable()
meth public java.lang.Boolean isAvailable()
meth public java.lang.String getFullyQualifiedCNAME()
meth public java.lang.String toString()
meth public void setAvailable(java.lang.Boolean)
meth public void setFullyQualifiedCNAME(java.lang.String)
supr java.lang.Object
hfds available,fullyQualifiedCNAME

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
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueOptions(java.lang.String[])
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
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription withValueType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex getRegex()
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
meth public void setValueType(java.lang.String)
supr java.lang.Object
hfds changeSeverity,defaultValue,maxLength,maxValue,minValue,name,namespace,regex,userDefined,valueOptions,valueType

CLSS public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withOptionName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting withValue(java.lang.String)
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds namespace,optionName,value

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
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest withDescription(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription getApplication()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationResult withApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
meth public java.lang.String toString()
meth public void setApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
supr java.lang.Object
hfds application

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withAutoCreateApplication(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest withVersionLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location getSourceBundle()
meth public java.lang.Boolean getAutoCreateApplication()
meth public java.lang.Boolean isAutoCreateApplication()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setAutoCreateApplication(java.lang.Boolean)
meth public void setDescription(java.lang.String)
meth public void setSourceBundle(com.amazonaws.services.elasticbeanstalk.model.S3Location)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,autoCreateApplication,description,sourceBundle,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription getApplicationVersion()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionResult withApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
meth public java.lang.String toString()
meth public void setApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
supr java.lang.Object
hfds applicationVersion

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withSourceConfiguration(com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration getSourceConfiguration()
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setSolutionStackName(java.lang.String)
meth public void setSourceConfiguration(com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description,optionSettings,solutionStackName,sourceConfiguration,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateResult withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withCNAMEPrefix(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAMEPrefix()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptionsToRemove()
meth public void setApplicationName(java.lang.String)
meth public void setCNAMEPrefix(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,cNAMEPrefix,description,environmentName,optionSettings,optionsToRemove,solutionStackName,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentId,environmentName,health,solutionStackName,status,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationRequest
cons public init()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.CreateStorageLocationResult withS3Bucket(java.lang.String)
meth public java.lang.String getS3Bucket()
meth public java.lang.String toString()
meth public void setS3Bucket(java.lang.String)
supr java.lang.Object
hfds s3Bucket

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest withApplicationName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withDeleteSourceBundle(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest withVersionLabel(java.lang.String)
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

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DeleteEnvironmentConfigurationRequest withEnvironmentName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withVersionLabels(java.lang.String[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest withVersionLabels(java.util.Collection<java.lang.String>)
meth public java.lang.String getApplicationName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getVersionLabels()
meth public void setApplicationName(java.lang.String)
meth public void setVersionLabels(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,versionLabels

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult withApplicationVersions(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult withApplicationVersions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription> getApplicationVersions()
meth public void setApplicationVersions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription>)
supr java.lang.Object
hfds applicationVersions

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest withApplicationNames(java.lang.String[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest withApplicationNames(java.util.Collection<java.lang.String>)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getApplicationNames()
meth public void setApplicationNames(java.util.Collection<java.lang.String>)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationNames

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult withApplications(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult withApplications(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription> getApplications()
meth public void setApplications(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription>)
supr java.lang.Object
hfds applications

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withOptions(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsRequest withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptions()
meth public void setApplicationName(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentName,options,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withOptions(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationOptionsResult withSolutionStackName(java.lang.String)
meth public java.lang.String getSolutionStackName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription> getOptions()
meth public void setOptions(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionDescription>)
meth public void setSolutionStackName(java.lang.String)
supr java.lang.Object
hfds options,solutionStackName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsRequest withTemplateName(java.lang.String)
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
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult withConfigurationSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeConfigurationSettingsResult withConfigurationSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription> getConfigurationSettings()
meth public void setConfigurationSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationSettingsDescription>)
supr java.lang.Object
hfds configurationSettings

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesRequest withEnvironmentName(java.lang.String)
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentResourcesResult withEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription getEnvironmentResources()
meth public java.lang.String toString()
meth public void setEnvironmentResources(com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription)
supr java.lang.Object
hfds environmentResources

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentIds(java.lang.String[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentNames(java.lang.String[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentIds(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withEnvironmentNames(java.util.Collection<java.lang.String>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withIncludeDeleted(java.lang.Boolean)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withIncludedDeletedBackTo(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest withVersionLabel(java.lang.String)
meth public java.lang.Boolean getIncludeDeleted()
meth public java.lang.Boolean isIncludeDeleted()
meth public java.lang.String getApplicationName()
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
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,environmentIds,environmentNames,includeDeleted,includedDeletedBackTo,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult withEnvironments(com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult withEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription> getEnvironments()
meth public void setEnvironments(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription>)
supr java.lang.Object
hfds environments

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEndTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withNextToken(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withRequestId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withSeverity(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withStartTime(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsRequest withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getNextToken()
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
meth public void setNextToken(java.lang.String)
meth public void setRequestId(java.lang.String)
meth public void setSeverity(java.lang.String)
meth public void setStartTime(java.util.Date)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,endTime,environmentId,environmentName,nextToken,requestId,severity,startTime,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withEvents(com.amazonaws.services.elasticbeanstalk.model.EventDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withEvents(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EventDescription>)
meth public com.amazonaws.services.elasticbeanstalk.model.DescribeEventsResult withNextToken(java.lang.String)
meth public java.lang.String getNextToken()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EventDescription> getEvents()
meth public void setEvents(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EventDescription>)
meth public void setNextToken(java.lang.String)
supr java.lang.Object
hfds events,nextToken

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentId,environmentName,health,solutionStackName,status,templateName,versionLabel

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

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withEc2InstanceId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withInfoType(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription withSampleTimestamp(java.util.Date)
meth public java.lang.String getEc2InstanceId()
meth public java.lang.String getInfoType()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.util.Date getSampleTimestamp()
meth public void setEc2InstanceId(java.lang.String)
meth public void setInfoType(java.lang.String)
meth public void setMessage(java.lang.String)
meth public void setSampleTimestamp(java.util.Date)
supr java.lang.Object
hfds ec2InstanceId,infoType,message,sampleTimestamp

CLSS public final !enum com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType
fld public final static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType Tail
meth public java.lang.String toString()
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType fromValue(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType valueOf(java.lang.String)
meth public static com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType[] values()
supr java.lang.Enum<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoType>
hfds value

CLSS public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withAutoScalingGroups(com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withInstances(com.amazonaws.services.elasticbeanstalk.model.Instance[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchConfigurations(com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLoadBalancers(com.amazonaws.services.elasticbeanstalk.model.LoadBalancer[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withTriggers(com.amazonaws.services.elasticbeanstalk.model.Trigger[])
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withAutoScalingGroups(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withInstances(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Instance>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLaunchConfigurations(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withLoadBalancers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer>)
meth public com.amazonaws.services.elasticbeanstalk.model.EnvironmentResourceDescription withTriggers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Trigger>)
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup> getAutoScalingGroups()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Instance> getInstances()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration> getLaunchConfigurations()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer> getLoadBalancers()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.Trigger> getTriggers()
meth public void setAutoScalingGroups(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.AutoScalingGroup>)
meth public void setEnvironmentName(java.lang.String)
meth public void setInstances(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Instance>)
meth public void setLaunchConfigurations(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration>)
meth public void setLoadBalancers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.LoadBalancer>)
meth public void setTriggers(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.Trigger>)
supr java.lang.Object
hfds autoScalingGroups,environmentName,instances,launchConfigurations,loadBalancers,triggers

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

CLSS public com.amazonaws.services.elasticbeanstalk.model.EventDescription
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withEventDate(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withRequestId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withSeverity(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.EventDescription withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getMessage()
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
meth public void setRequestId(java.lang.String)
meth public void setSeverity(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,environmentName,eventDate,message,requestId,severity,templateName,versionLabel

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

CLSS public com.amazonaws.services.elasticbeanstalk.model.Instance
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.Instance withId(java.lang.String)
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public void setId(java.lang.String)
supr java.lang.Object
hfds id

CLSS public com.amazonaws.services.elasticbeanstalk.model.InvalidParameterCombinationException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.InvalidParameterValueException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.LaunchConfiguration withName(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksRequest
cons public init()
meth public java.lang.String toString()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStacks(java.lang.String[])
meth public com.amazonaws.services.elasticbeanstalk.model.ListAvailableSolutionStacksResult withSolutionStacks(java.util.Collection<java.lang.String>)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getSolutionStacks()
meth public void setSolutionStacks(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds solutionStacks

CLSS public com.amazonaws.services.elasticbeanstalk.model.LoadBalancer
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.LoadBalancer withName(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.MissingRequiredParameterException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex withLabel(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionRestrictionRegex withPattern(java.lang.String)
meth public java.lang.String getLabel()
meth public java.lang.String getPattern()
meth public java.lang.String toString()
meth public void setLabel(java.lang.String)
meth public void setPattern(java.lang.String)
supr java.lang.Object
hfds label,pattern

CLSS public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.OptionSpecification withOptionName(java.lang.String)
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String toString()
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
supr java.lang.Object
hfds namespace,optionName

CLSS public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RebuildEnvironmentRequest withEnvironmentName(java.lang.String)
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RequestEnvironmentInfoRequest withInfoType(java.lang.String)
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getInfoType()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setInfoType(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,infoType

CLSS public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RestartAppServerRequest withEnvironmentName(java.lang.String)
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName

CLSS public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoRequest withInfoType(java.lang.String)
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getInfoType()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setInfoType(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,infoType

CLSS public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult withEnvironmentInfo(com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription[])
meth public com.amazonaws.services.elasticbeanstalk.model.RetrieveEnvironmentInfoResult withEnvironmentInfo(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription> getEnvironmentInfo()
meth public void setEnvironmentInfo(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.EnvironmentInfoDescription>)
supr java.lang.Object
hfds environmentInfo

CLSS public com.amazonaws.services.elasticbeanstalk.model.S3Location
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location withS3Bucket(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.S3Location withS3Key(java.lang.String)
meth public java.lang.String getS3Bucket()
meth public java.lang.String getS3Key()
meth public java.lang.String toString()
meth public void setS3Bucket(java.lang.String)
meth public void setS3Key(java.lang.String)
supr java.lang.Object
hfds s3Bucket,s3Key

CLSS public com.amazonaws.services.elasticbeanstalk.model.S3SubscriptionRequiredException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.SourceBundleDeletionException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.SourceConfiguration withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest withTerminateResources(java.lang.Boolean)
meth public java.lang.Boolean getTerminateResources()
meth public java.lang.Boolean isTerminateResources()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String toString()
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setTerminateResources(java.lang.Boolean)
supr com.amazonaws.AmazonWebServiceRequest
hfds environmentId,environmentName,terminateResources

CLSS public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentResult withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentId,environmentName,health,solutionStackName,status,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyApplicationVersionsException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyApplicationsException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyBucketsException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyConfigurationTemplatesException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.TooManyEnvironmentsException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.Trigger
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.Trigger withName(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds name

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest withDescription(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public void setApplicationName(java.lang.String)
meth public void setDescription(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds applicationName,description

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription getApplication()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationResult withApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
meth public java.lang.String toString()
meth public void setApplication(com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription)
supr java.lang.Object
hfds application

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionRequest withVersionLabel(java.lang.String)
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
meth public com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription getApplicationVersion()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationVersionResult withApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
meth public java.lang.String toString()
meth public void setApplicationVersion(com.amazonaws.services.elasticbeanstalk.model.ApplicationVersionDescription)
supr java.lang.Object
hfds applicationVersion

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest withTemplateName(java.lang.String)
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
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDeploymentStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateResult withTemplateName(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getDeploymentStatus()
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getTemplateName()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public void setApplicationName(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDeploymentStatus(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setSolutionStackName(java.lang.String)
meth public void setTemplateName(java.lang.String)
supr java.lang.Object
hfds applicationName,dateCreated,dateUpdated,deploymentStatus,description,environmentName,optionSettings,solutionStackName,templateName

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest
cons public init()
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionsToRemove(com.amazonaws.services.elasticbeanstalk.model.OptionSpecification[])
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest withVersionLabel(java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting> getOptionSettings()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification> getOptionsToRemove()
meth public void setDescription(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public void setOptionsToRemove(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.OptionSpecification>)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds description,environmentId,environmentName,optionSettings,optionsToRemove,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withCNAME(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDateCreated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDateUpdated(java.util.Date)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withDescription(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEndpointURL(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentId(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withHealth(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withSolutionStackName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withStatus(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withTemplateName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentResult withVersionLabel(java.lang.String)
meth public java.lang.String getApplicationName()
meth public java.lang.String getCNAME()
meth public java.lang.String getDescription()
meth public java.lang.String getEndpointURL()
meth public java.lang.String getEnvironmentId()
meth public java.lang.String getEnvironmentName()
meth public java.lang.String getHealth()
meth public java.lang.String getSolutionStackName()
meth public java.lang.String getStatus()
meth public java.lang.String getTemplateName()
meth public java.lang.String getVersionLabel()
meth public java.lang.String toString()
meth public java.util.Date getDateCreated()
meth public java.util.Date getDateUpdated()
meth public void setApplicationName(java.lang.String)
meth public void setCNAME(java.lang.String)
meth public void setDateCreated(java.util.Date)
meth public void setDateUpdated(java.util.Date)
meth public void setDescription(java.lang.String)
meth public void setEndpointURL(java.lang.String)
meth public void setEnvironmentId(java.lang.String)
meth public void setEnvironmentName(java.lang.String)
meth public void setHealth(java.lang.String)
meth public void setSolutionStackName(java.lang.String)
meth public void setStatus(java.lang.String)
meth public void setTemplateName(java.lang.String)
meth public void setVersionLabel(java.lang.String)
supr java.lang.Object
hfds applicationName,cNAME,dateCreated,dateUpdated,description,endpointURL,environmentId,environmentName,health,solutionStackName,status,templateName,versionLabel

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest
cons public init()
cons public init(java.lang.String,java.util.List<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withOptionSettings(com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting[])
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withApplicationName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withEnvironmentName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withOptionSettings(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting>)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsRequest withTemplateName(java.lang.String)
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
meth public !varargs com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult withMessages(com.amazonaws.services.elasticbeanstalk.model.ValidationMessage[])
meth public com.amazonaws.services.elasticbeanstalk.model.ValidateConfigurationSettingsResult withMessages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage>)
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage> getMessages()
meth public void setMessages(java.util.Collection<com.amazonaws.services.elasticbeanstalk.model.ValidationMessage>)
supr java.lang.Object
hfds messages

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidationErrorException
cons public init(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds serialVersionUID

CLSS public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage
cons public init()
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withMessage(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withNamespace(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withOptionName(java.lang.String)
meth public com.amazonaws.services.elasticbeanstalk.model.ValidationMessage withSeverity(java.lang.String)
meth public java.lang.String getMessage()
meth public java.lang.String getNamespace()
meth public java.lang.String getOptionName()
meth public java.lang.String getSeverity()
meth public java.lang.String toString()
meth public void setMessage(java.lang.String)
meth public void setNamespace(java.lang.String)
meth public void setOptionName(java.lang.String)
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

CLSS abstract interface com.amazonaws.services.elasticbeanstalk.model.package-info

CLSS abstract interface com.amazonaws.services.elasticbeanstalk.package-info

CLSS public abstract interface com.amazonaws.services.s3.AmazonS3
meth public abstract boolean doesBucketExist(java.lang.String)
meth public abstract com.amazonaws.services.s3.S3ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getBucketAcl(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(com.amazonaws.services.s3.model.CreateBucketRequest)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,com.amazonaws.services.s3.model.Region)
meth public abstract com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest)
meth public abstract com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.CopyObjectResult copyObject(com.amazonaws.services.s3.model.CopyObjectRequest)
meth public abstract com.amazonaws.services.s3.model.CopyObjectResult copyObject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public abstract com.amazonaws.services.s3.model.MultipartUploadListing listMultipartUploads(com.amazonaws.services.s3.model.ListMultipartUploadsRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ObjectListing)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(com.amazonaws.services.s3.model.ListObjectsRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String)
meth public abstract com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(com.amazonaws.services.s3.model.GetObjectMetadataRequest)
meth public abstract com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.Owner getS3AccountOwner()
meth public abstract com.amazonaws.services.s3.model.PartListing listParts(com.amazonaws.services.s3.model.ListPartsRequest)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.File)
meth public abstract com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public abstract com.amazonaws.services.s3.model.S3Object getObject(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public abstract com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.VersionListing)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(com.amazonaws.services.s3.model.ListVersionsRequest)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String)
meth public abstract com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public abstract java.lang.String getBucketLocation(com.amazonaws.services.s3.model.GetBucketLocationRequest)
meth public abstract java.lang.String getBucketLocation(java.lang.String)
meth public abstract java.net.URL generatePresignedUrl(com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
meth public abstract java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date)
meth public abstract java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date,com.amazonaws.HttpMethod)
meth public abstract java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets()
meth public abstract java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets(com.amazonaws.services.s3.model.ListBucketsRequest)
meth public abstract void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public abstract void changeObjectStorageClass(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.StorageClass)
meth public abstract void deleteBucket(com.amazonaws.services.s3.model.DeleteBucketRequest)
meth public abstract void deleteBucket(java.lang.String)
meth public abstract void deleteBucketPolicy(java.lang.String)
meth public abstract void deleteBucketWebsiteConfiguration(com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest)
meth public abstract void deleteBucketWebsiteConfiguration(java.lang.String)
meth public abstract void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public abstract void deleteObject(java.lang.String,java.lang.String)
meth public abstract void deleteVersion(com.amazonaws.services.s3.model.DeleteVersionRequest)
meth public abstract void deleteVersion(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public abstract void setBucketLoggingConfiguration(com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest)
meth public abstract void setBucketNotificationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public abstract void setBucketPolicy(java.lang.String,java.lang.String)
meth public abstract void setBucketVersioningConfiguration(com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest)
meth public abstract void setBucketWebsiteConfiguration(com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest)
meth public abstract void setBucketWebsiteConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public abstract void setEndpoint(java.lang.String)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public abstract void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)

CLSS public com.amazonaws.services.s3.AmazonS3Client
cons public init()
cons public init(com.amazonaws.auth.AWSCredentials)
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.ClientConfiguration)
intf com.amazonaws.services.s3.AmazonS3
meth protected <%0 extends com.amazonaws.AmazonWebServiceRequest> com.amazonaws.Request<{%%0}> createRequest(java.lang.String,java.lang.String,{%%0},com.amazonaws.http.HttpMethodName)
meth protected com.amazonaws.auth.Signer createSigner(com.amazonaws.Request<?>,java.lang.String,java.lang.String)
meth protected static void populateRequestMetadata(com.amazonaws.Request<?>,com.amazonaws.services.s3.model.ObjectMetadata)
meth public boolean doesBucketExist(java.lang.String)
meth public com.amazonaws.services.s3.S3ResponseMetadata getCachedResponseMetadata(com.amazonaws.AmazonWebServiceRequest)
meth public com.amazonaws.services.s3.model.AccessControlList getBucketAcl(java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.AccessControlList getObjectAcl(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(com.amazonaws.services.s3.model.CreateBucketRequest)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,com.amazonaws.services.s3.model.Region)
meth public com.amazonaws.services.s3.model.Bucket createBucket(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getBucketLoggingConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketPolicy getBucketPolicy(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration getBucketVersioningConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest)
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getBucketWebsiteConfiguration(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(com.amazonaws.services.s3.model.CopyObjectRequest)
meth public com.amazonaws.services.s3.model.CopyObjectResult copyObject(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.MultipartUploadListing listMultipartUploads(com.amazonaws.services.s3.model.ListMultipartUploadsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listNextBatchOfObjects(com.amazonaws.services.s3.model.ObjectListing)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(com.amazonaws.services.s3.model.ListObjectsRequest)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectListing listObjects(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(com.amazonaws.services.s3.model.GetObjectMetadataRequest)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.Owner getS3AccountOwner()
meth public com.amazonaws.services.s3.model.PartListing listParts(com.amazonaws.services.s3.model.ListPartsRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public com.amazonaws.services.s3.model.VersionListing listNextBatchOfVersions(com.amazonaws.services.s3.model.VersionListing)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(com.amazonaws.services.s3.model.ListVersionsRequest)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.VersionListing listVersions(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public java.lang.String getBucketLocation(com.amazonaws.services.s3.model.GetBucketLocationRequest)
meth public java.lang.String getBucketLocation(java.lang.String)
meth public java.net.URL generatePresignedUrl(com.amazonaws.services.s3.model.GeneratePresignedUrlRequest)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date)
meth public java.net.URL generatePresignedUrl(java.lang.String,java.lang.String,java.util.Date,com.amazonaws.HttpMethod)
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets()
meth public java.util.List<com.amazonaws.services.s3.model.Bucket> listBuckets(com.amazonaws.services.s3.model.ListBucketsRequest)
meth public void abortMultipartUpload(com.amazonaws.services.s3.model.AbortMultipartUploadRequest)
meth public void addRequestHandler(com.amazonaws.handlers.RequestHandler)
meth public void changeObjectStorageClass(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.StorageClass)
meth public void deleteBucket(com.amazonaws.services.s3.model.DeleteBucketRequest)
meth public void deleteBucket(java.lang.String)
meth public void deleteBucketPolicy(java.lang.String)
meth public void deleteBucketWebsiteConfiguration(com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest)
meth public void deleteBucketWebsiteConfiguration(java.lang.String)
meth public void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
meth public void deleteObject(java.lang.String,java.lang.String)
meth public void deleteVersion(com.amazonaws.services.s3.model.DeleteVersionRequest)
meth public void deleteVersion(java.lang.String,java.lang.String,java.lang.String)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setBucketAcl(java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setBucketLoggingConfiguration(com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest)
meth public void setBucketNotificationConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketNotificationConfiguration)
meth public void setBucketPolicy(java.lang.String,java.lang.String)
meth public void setBucketVersioningConfiguration(com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest)
meth public void setBucketWebsiteConfiguration(com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest)
meth public void setBucketWebsiteConfiguration(java.lang.String,com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.AccessControlList)
meth public void setObjectAcl(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.CannedAccessControlList)
supr com.amazonaws.AmazonWebServiceClient
hfds awsCredentials,bucketConfigurationXmlFactory,bucketNameUtils,errorResponseHandler,log,voidResponseHandler

CLSS public com.amazonaws.services.s3.AmazonS3EncryptionClient
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials)
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.ClientConfiguration,com.amazonaws.services.s3.model.CryptoConfiguration)
cons public init(com.amazonaws.auth.AWSCredentials,com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.services.s3.model.CryptoConfiguration)
cons public init(com.amazonaws.services.s3.model.EncryptionMaterials)
cons public init(com.amazonaws.services.s3.model.EncryptionMaterials,com.amazonaws.services.s3.model.CryptoConfiguration)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadResult completeMultipartUpload(com.amazonaws.services.s3.model.CompleteMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadResult initiateMultipartUpload(com.amazonaws.services.s3.model.InitiateMultipartUploadRequest)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObject(com.amazonaws.services.s3.model.GetObjectRequest,java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectResult putObject(com.amazonaws.services.s3.model.PutObjectRequest)
meth public com.amazonaws.services.s3.model.S3Object getObject(com.amazonaws.services.s3.model.GetObjectRequest)
meth public com.amazonaws.services.s3.model.UploadPartResult uploadPart(com.amazonaws.services.s3.model.UploadPartRequest)
meth public void deleteObject(com.amazonaws.services.s3.model.DeleteObjectRequest)
supr com.amazonaws.services.s3.AmazonS3Client
hfds cryptoConfig,currentMultipartUploadSecretKeys,encryptionMaterials,log

CLSS public abstract interface com.amazonaws.services.s3.Headers
fld public final static java.lang.String AMAZON_PREFIX = "x-amz-"
fld public final static java.lang.String CACHE_CONTROL = "Cache-Control"
fld public final static java.lang.String CONTENT_DISPOSITION = "Content-Disposition"
fld public final static java.lang.String CONTENT_ENCODING = "Content-Encoding"
fld public final static java.lang.String CONTENT_LENGTH = "Content-Length"
fld public final static java.lang.String CONTENT_MD5 = "Content-MD5"
fld public final static java.lang.String CONTENT_TYPE = "Content-Type"
fld public final static java.lang.String COPY_SOURCE_IF_MATCH = "x-amz-copy-source-if-match"
fld public final static java.lang.String COPY_SOURCE_IF_MODIFIED_SINCE = "x-amz-copy-source-if-modified-since"
fld public final static java.lang.String COPY_SOURCE_IF_NO_MATCH = "x-amz-copy-source-if-none-match"
fld public final static java.lang.String COPY_SOURCE_IF_UNMODIFIED_SINCE = "x-amz-copy-source-if-unmodified-since"
fld public final static java.lang.String CRYPTO_INSTRUCTION_FILE = "x-amz-crypto-instr-file"
fld public final static java.lang.String CRYPTO_IV = "x-amz-iv"
fld public final static java.lang.String CRYPTO_KEY = "x-amz-key"
fld public final static java.lang.String DATE = "Date"
fld public final static java.lang.String ETAG = "ETag"
fld public final static java.lang.String EXTENDED_REQUEST_ID = "x-amz-id-2"
fld public final static java.lang.String GET_OBJECT_IF_MATCH = "If-Match"
fld public final static java.lang.String GET_OBJECT_IF_MODIFIED_SINCE = "If-Modified-Since"
fld public final static java.lang.String GET_OBJECT_IF_NONE_MATCH = "If-None-Match"
fld public final static java.lang.String GET_OBJECT_IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
fld public final static java.lang.String LAST_MODIFIED = "Last-Modified"
fld public final static java.lang.String MATERIALS_DESCRIPTION = "x-amz-matdesc"
fld public final static java.lang.String METADATA_DIRECTIVE = "x-amz-metadata-directive"
fld public final static java.lang.String RANGE = "Range"
fld public final static java.lang.String REQUEST_ID = "x-amz-request-id"
fld public final static java.lang.String S3_ALTERNATE_DATE = "x-amz-date"
fld public final static java.lang.String S3_CANNED_ACL = "x-amz-acl"
fld public final static java.lang.String S3_MFA = "x-amz-mfa"
fld public final static java.lang.String S3_USER_METADATA_PREFIX = "x-amz-meta-"
fld public final static java.lang.String S3_VERSION_ID = "x-amz-version-id"
fld public final static java.lang.String SECURITY_TOKEN = "x-amz-security-token"
fld public final static java.lang.String SERVER = "Server"
fld public final static java.lang.String STORAGE_CLASS = "x-amz-storage-class"

CLSS public com.amazonaws.services.s3.S3ResponseMetadata
cons public init(com.amazonaws.ResponseMetadata)
cons public init(java.util.Map<java.lang.String,java.lang.String>)
fld public final static java.lang.String HOST_ID = "HOST_ID"
meth public java.lang.String getHostId()
supr com.amazonaws.ResponseMetadata

CLSS public com.amazonaws.services.s3.model.AbortMultipartUploadRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.AbortMultipartUploadRequest withUploadId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,uploadId

CLSS public com.amazonaws.services.s3.model.AccessControlList
cons public init()
intf java.io.Serializable
meth public !varargs void grantAllPermissions(com.amazonaws.services.s3.model.Grant[])
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String toString()
meth public java.util.Set<com.amazonaws.services.s3.model.Grant> getGrants()
meth public void grantPermission(com.amazonaws.services.s3.model.Grantee,com.amazonaws.services.s3.model.Permission)
meth public void revokeAllPermissions(com.amazonaws.services.s3.model.Grantee)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
supr java.lang.Object
hfds grants,owner,serialVersionUID

CLSS public com.amazonaws.services.s3.model.AmazonS3Exception
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.String getExtendedRequestId()
meth public java.lang.String toString()
meth public void setExtendedRequestId(java.lang.String)
supr com.amazonaws.AmazonServiceException
hfds extendedRequestId,serialVersionUID

CLSS public com.amazonaws.services.s3.model.Bucket
cons public init()
cons public init(java.lang.String)
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Date getCreationDate()
meth public void setCreationDate(java.util.Date)
meth public void setName(java.lang.String)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
supr java.lang.Object
hfds creationDate,name,owner,serialVersionUID

CLSS public com.amazonaws.services.s3.model.BucketLoggingConfiguration
cons public init()
cons public init(java.lang.String,java.lang.String)
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
cons public init(java.util.Collection<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration>)
innr public static TopicConfiguration
meth public !varargs com.amazonaws.services.s3.model.BucketNotificationConfiguration withTopicConfigurations(com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration[])
meth public java.lang.String toString()
meth public java.util.List<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration> getTopicConfigurations()
meth public void setTopicConfigurations(java.util.Collection<com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration>)
supr java.lang.Object
hfds topicConfigurations

CLSS public static com.amazonaws.services.s3.model.BucketNotificationConfiguration$TopicConfiguration
 outer com.amazonaws.services.s3.model.BucketNotificationConfiguration
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getEvent()
meth public java.lang.String getTopic()
meth public java.lang.String toString()
supr java.lang.Object
hfds event,topic

CLSS public com.amazonaws.services.s3.model.BucketPolicy
cons public init()
meth public java.lang.String getPolicyText()
meth public void setPolicyText(java.lang.String)
supr java.lang.Object
hfds policyText

CLSS public com.amazonaws.services.s3.model.BucketVersioningConfiguration
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String OFF = "Off"
fld public final static java.lang.String SUSPENDED = "Suspended"
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration withMfaDeleteEnabled(java.lang.Boolean)
meth public com.amazonaws.services.s3.model.BucketVersioningConfiguration withStatus(java.lang.String)
meth public java.lang.Boolean isMfaDeleteEnabled()
meth public java.lang.String getStatus()
meth public void setMfaDeleteEnabled(java.lang.Boolean)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds isMfaDeleteEnabled,status

CLSS public com.amazonaws.services.s3.model.BucketWebsiteConfiguration
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getErrorDocument()
meth public java.lang.String getIndexDocumentSuffix()
meth public void setErrorDocument(java.lang.String)
meth public void setIndexDocumentSuffix(java.lang.String)
supr java.lang.Object
hfds errorDocument,indexDocumentSuffix

CLSS public final !enum com.amazonaws.services.s3.model.CannedAccessControlList
fld public final static com.amazonaws.services.s3.model.CannedAccessControlList AuthenticatedRead
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
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getIdentifier()
meth public void setDisplayName(java.lang.String)
meth public void setIdentifier(java.lang.String)
supr java.lang.Object
hfds displayName,id

CLSS public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public !varargs com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(com.amazonaws.services.s3.model.UploadPartResult[])
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(java.util.Collection<com.amazonaws.services.s3.model.UploadPartResult>)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withPartETags(java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public com.amazonaws.services.s3.model.CompleteMultipartUploadRequest withUploadId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public java.util.List<com.amazonaws.services.s3.model.PartETag> getPartETags()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setPartETags(java.util.List<com.amazonaws.services.s3.model.PartETag>)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,partETags,uploadId

CLSS public com.amazonaws.services.s3.model.CompleteMultipartUploadResult
cons public init()
meth public java.lang.String getBucketName()
meth public java.lang.String getETag()
meth public java.lang.String getKey()
meth public java.lang.String getLocation()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setETag(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setLocation(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds bucketName,eTag,key,location,versionId

CLSS public com.amazonaws.services.s3.model.CopyObjectRequest
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAccessControlList()
meth public com.amazonaws.services.s3.model.CopyObjectRequest withCannedAccessControlList(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withDestinationBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withDestinationKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withMatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withModifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withNewObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withNonmatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceKey(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withSourceVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withStorageClass(java.lang.String)
meth public com.amazonaws.services.s3.model.CopyObjectRequest withUnmodifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.ObjectMetadata getNewObjectMetadata()
meth public java.lang.String getDestinationBucketName()
meth public java.lang.String getDestinationKey()
meth public java.lang.String getSourceBucketName()
meth public java.lang.String getSourceKey()
meth public java.lang.String getSourceVersionId()
meth public java.lang.String getStorageClass()
meth public java.util.Date getModifiedSinceConstraint()
meth public java.util.Date getUnmodifiedSinceConstraint()
meth public java.util.List<java.lang.String> getMatchingETagConstraints()
meth public java.util.List<java.lang.String> getNonmatchingETagConstraints()
meth public void setCannedAccessControlList(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setDestinationBucketName(java.lang.String)
meth public void setDestinationKey(java.lang.String)
meth public void setMatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setModifiedSinceConstraint(java.util.Date)
meth public void setNewObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setNonmatchingETagConstraints(java.util.List<java.lang.String>)
meth public void setSourceBucketName(java.lang.String)
meth public void setSourceKey(java.lang.String)
meth public void setSourceVersionId(java.lang.String)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
meth public void setUnmodifiedSinceConstraint(java.util.Date)
supr com.amazonaws.AmazonWebServiceRequest
hfds cannedACL,destinationBucketName,destinationKey,matchingETagConstraints,modifiedSinceConstraint,newObjectMetadata,nonmatchingEtagConstraints,sourceBucketName,sourceKey,sourceVersionId,storageClass,unmodifiedSinceConstraint

CLSS public com.amazonaws.services.s3.model.CopyObjectResult
cons public init()
meth public java.lang.String getETag()
meth public java.lang.String getVersionId()
meth public java.util.Date getLastModifiedDate()
meth public void setETag(java.lang.String)
meth public void setLastModifiedDate(java.util.Date)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds etag,lastModifiedDate,versionId

CLSS public com.amazonaws.services.s3.model.CreateBucketRequest
cons public init(java.lang.String)
cons public init(java.lang.String,com.amazonaws.services.s3.model.Region)
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.CreateBucketRequest withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public java.lang.String getBucketName()
meth public java.lang.String getRegion()
meth public void setBucketName(java.lang.String)
meth public void setCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setRegion(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,cannedAcl,region

CLSS public com.amazonaws.services.s3.model.CryptoConfiguration
cons public init()
meth public com.amazonaws.services.s3.model.CryptoConfiguration withCryptoProvider(java.security.Provider)
meth public com.amazonaws.services.s3.model.CryptoConfiguration withStorageMode(com.amazonaws.services.s3.model.CryptoStorageMode)
meth public com.amazonaws.services.s3.model.CryptoStorageMode getStorageMode()
meth public java.security.Provider getCryptoProvider()
meth public void setCryptoProvider(java.security.Provider)
meth public void setStorageMode(com.amazonaws.services.s3.model.CryptoStorageMode)
supr java.lang.Object
hfds cryptoProvider,storageMode

CLSS public final !enum com.amazonaws.services.s3.model.CryptoStorageMode
fld public final static com.amazonaws.services.s3.model.CryptoStorageMode InstructionFile
fld public final static com.amazonaws.services.s3.model.CryptoStorageMode ObjectMetadata
meth public static com.amazonaws.services.s3.model.CryptoStorageMode valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.CryptoStorageMode[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.CryptoStorageMode>

CLSS public com.amazonaws.services.s3.model.DeleteBucketRequest
cons public init(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest
cons public init(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.DeleteObjectRequest
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteObjectRequest withKey(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key

CLSS public com.amazonaws.services.s3.model.DeleteVersionRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public com.amazonaws.services.s3.model.DeleteVersionRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication getMfa()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setMfa(com.amazonaws.services.s3.model.MultiFactorAuthentication)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,mfa,versionId

CLSS public com.amazonaws.services.s3.model.EmailAddressGrantee
cons public init(java.lang.String)
intf com.amazonaws.services.s3.model.Grantee
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getIdentifier()
meth public void setIdentifier(java.lang.String)
supr java.lang.Object
hfds emailAddress

CLSS public com.amazonaws.services.s3.model.EncryptionMaterials
cons protected init(java.security.KeyPair,javax.crypto.SecretKey)
cons public init(java.security.KeyPair)
cons public init(javax.crypto.SecretKey)
meth public com.amazonaws.services.s3.model.EncryptionMaterialsAccessor getAccessor()
meth public java.security.KeyPair getKeyPair()
meth public java.util.Map<java.lang.String,java.lang.String> getMaterialsDescription()
meth public javax.crypto.SecretKey getSymmetricKey()
supr java.lang.Object
hfds keyPair,symmetricKey

CLSS public abstract interface com.amazonaws.services.s3.model.EncryptionMaterialsAccessor
meth public abstract com.amazonaws.services.s3.model.EncryptionMaterials getEncryptionMaterials(java.util.Map<java.lang.String,java.lang.String>)

CLSS public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,com.amazonaws.HttpMethod)
meth public com.amazonaws.HttpMethod getMethod()
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withExpiration(java.util.Date)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withMethod(com.amazonaws.HttpMethod)
meth public com.amazonaws.services.s3.model.GeneratePresignedUrlRequest withResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides getResponseHeaders()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.util.Date getExpiration()
meth public java.util.Map<java.lang.String,java.lang.String> getRequestParameters()
meth public void addRequestParameter(java.lang.String,java.lang.String)
meth public void setBucketName(java.lang.String)
meth public void setExpiration(java.util.Date)
meth public void setKey(java.lang.String)
meth public void setMethod(com.amazonaws.HttpMethod)
meth public void setResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,expiration,key,method,requestParameters,responseHeaders

CLSS public com.amazonaws.services.s3.model.GenericBucketRequest
cons public init(java.lang.String)
meth public java.lang.String getBucket()
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket

CLSS public com.amazonaws.services.s3.model.GetBucketLocationRequest
cons public init(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketLocationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest
cons public init(java.lang.String)
meth public com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest withBucketName(java.lang.String)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName

CLSS public com.amazonaws.services.s3.model.GetObjectMetadataRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectMetadataRequest withVersionId(java.lang.String)
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getVersionId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,versionId

CLSS public com.amazonaws.services.s3.model.GetObjectRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withMatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withModifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.GetObjectRequest withNonmatchingETagConstraint(java.lang.String)
meth public com.amazonaws.services.s3.model.GetObjectRequest withRange(long,long)
meth public com.amazonaws.services.s3.model.GetObjectRequest withResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public com.amazonaws.services.s3.model.GetObjectRequest withUnmodifiedSinceConstraint(java.util.Date)
meth public com.amazonaws.services.s3.model.GetObjectRequest withVersionId(java.lang.String)
meth public com.amazonaws.services.s3.model.ResponseHeaderOverrides getResponseHeaders()
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
meth public void setRange(long,long)
meth public void setResponseHeaders(com.amazonaws.services.s3.model.ResponseHeaderOverrides)
meth public void setUnmodifiedSinceConstraint(java.util.Date)
meth public void setVersionId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,matchingETagConstraints,modifiedSinceConstraint,nonmatchingEtagConstraints,range,responseHeaders,unmodifiedSinceConstraint,versionId

CLSS public com.amazonaws.services.s3.model.Grant
cons public init(com.amazonaws.services.s3.model.Grantee,com.amazonaws.services.s3.model.Permission)
meth public boolean equals(java.lang.Object)
meth public com.amazonaws.services.s3.model.Grantee getGrantee()
meth public com.amazonaws.services.s3.model.Permission getPermission()
meth public int hashCode()
supr java.lang.Object
hfds grantee,permission

CLSS public abstract interface com.amazonaws.services.s3.model.Grantee
meth public abstract java.lang.String getIdentifier()
meth public abstract void setIdentifier(java.lang.String)

CLSS public final !enum com.amazonaws.services.s3.model.GroupGrantee
fld public final static com.amazonaws.services.s3.model.GroupGrantee AllUsers
fld public final static com.amazonaws.services.s3.model.GroupGrantee AuthenticatedUsers
fld public final static com.amazonaws.services.s3.model.GroupGrantee LogDelivery
intf com.amazonaws.services.s3.model.Grantee
meth public java.lang.String getIdentifier()
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.GroupGrantee parseGroupGrantee(java.lang.String)
meth public static com.amazonaws.services.s3.model.GroupGrantee valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.GroupGrantee[] values()
meth public void setIdentifier(java.lang.String)
supr java.lang.Enum<com.amazonaws.services.s3.model.GroupGrantee>
hfds groupUri

CLSS public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,com.amazonaws.services.s3.model.ObjectMetadata)
fld public com.amazonaws.services.s3.model.ObjectMetadata objectMetadata
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedACL()
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withCannedACL(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.InitiateMultipartUploadRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata()
meth public com.amazonaws.services.s3.model.StorageClass getStorageClass()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public void setBucketName(java.lang.String)
meth public void setCannedACL(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setKey(java.lang.String)
meth public void setObjectMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,cannedACL,key,storageClass

CLSS public com.amazonaws.services.s3.model.InitiateMultipartUploadResult
cons public init()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setUploadId(java.lang.String)
supr java.lang.Object
hfds bucketName,key,uploadId

CLSS public com.amazonaws.services.s3.model.ListBucketsRequest
cons public init()
supr com.amazonaws.AmazonWebServiceRequest

CLSS public com.amazonaws.services.s3.model.ListMultipartUploadsRequest
cons public init(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withKeyMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withMaxUploads(int)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListMultipartUploadsRequest withUploadIdMarker(java.lang.String)
meth public java.lang.Integer getMaxUploads()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getUploadIdMarker()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxUploads(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setUploadIdMarker(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,keyMarker,maxUploads,prefix,uploadIdMarker

CLSS public com.amazonaws.services.s3.model.ListObjectsRequest
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withMaxKeys(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListObjectsRequest withPrefix(java.lang.String)
meth public java.lang.Integer getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getMarker()
meth public java.lang.String getPrefix()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setMarker(java.lang.String)
meth public void setMaxKeys(java.lang.Integer)
meth public void setPrefix(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,marker,maxKeys,prefix

CLSS public com.amazonaws.services.s3.model.ListPartsRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.ListPartsRequest withMaxParts(int)
meth public com.amazonaws.services.s3.model.ListPartsRequest withPartNumberMarker(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListPartsRequest withUploadId(java.lang.String)
meth public java.lang.Integer getMaxParts()
meth public java.lang.Integer getPartNumberMarker()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getUploadId()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setMaxParts(int)
meth public void setPartNumberMarker(java.lang.Integer)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,key,maxParts,partNumberMarker,uploadId

CLSS public com.amazonaws.services.s3.model.ListVersionsRequest
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withDelimiter(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withKeyMarker(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withMaxResults(java.lang.Integer)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withPrefix(java.lang.String)
meth public com.amazonaws.services.s3.model.ListVersionsRequest withVersionIdMarker(java.lang.String)
meth public java.lang.Integer getMaxResults()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getKeyMarker()
meth public java.lang.String getPrefix()
meth public java.lang.String getVersionIdMarker()
meth public void setBucketName(java.lang.String)
meth public void setDelimiter(java.lang.String)
meth public void setKeyMarker(java.lang.String)
meth public void setMaxResults(java.lang.Integer)
meth public void setPrefix(java.lang.String)
meth public void setVersionIdMarker(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,delimiter,keyMarker,maxResults,prefix,versionIdMarker

CLSS public com.amazonaws.services.s3.model.MultiFactorAuthentication
cons public init(java.lang.String,java.lang.String)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication withDeviceSerialNumber(java.lang.String)
meth public com.amazonaws.services.s3.model.MultiFactorAuthentication withToken(java.lang.String)
meth public java.lang.String getDeviceSerialNumber()
meth public java.lang.String getToken()
meth public void setDeviceSerialNumber(java.lang.String)
meth public void setToken(java.lang.String)
supr java.lang.Object
hfds deviceSerialNumber,token

CLSS public com.amazonaws.services.s3.model.MultipartUpload
cons public init()
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
meth public boolean isTruncated()
meth public int getMaxUploads()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
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
meth public void setKeyMarker(java.lang.String)
meth public void setMaxUploads(int)
meth public void setMultipartUploads(java.util.List<com.amazonaws.services.s3.model.MultipartUpload>)
meth public void setNextKeyMarker(java.lang.String)
meth public void setNextUploadIdMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
meth public void setUploadIdMarker(java.lang.String)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,isTruncated,keyMarker,maxUploads,multipartUploads,nextKeyMarker,nextUploadIdMarker,prefix,uploadIdMarker

CLSS public com.amazonaws.services.s3.model.ObjectListing
cons public init()
meth public boolean isTruncated()
meth public int getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
meth public java.lang.String getMarker()
meth public java.lang.String getNextMarker()
meth public java.lang.String getPrefix()
meth public java.util.List<com.amazonaws.services.s3.model.S3ObjectSummary> getObjectSummaries()
meth public java.util.List<java.lang.String> getCommonPrefixes()
meth public void setBucketName(java.lang.String)
meth public void setCommonPrefixes(java.util.List<java.lang.String>)
meth public void setDelimiter(java.lang.String)
meth public void setMarker(java.lang.String)
meth public void setMaxKeys(int)
meth public void setNextMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,isTruncated,marker,maxKeys,nextMarker,objectSummaries,prefix

CLSS public com.amazonaws.services.s3.model.ObjectMetadata
cons public init()
meth public java.lang.String getCacheControl()
meth public java.lang.String getContentDisposition()
meth public java.lang.String getContentEncoding()
meth public java.lang.String getContentMD5()
meth public java.lang.String getContentType()
meth public java.lang.String getETag()
meth public java.lang.String getVersionId()
meth public java.util.Date getLastModified()
meth public java.util.Map<java.lang.String,java.lang.Object> getRawMetadata()
meth public java.util.Map<java.lang.String,java.lang.String> getUserMetadata()
meth public long getContentLength()
meth public void addUserMetadata(java.lang.String,java.lang.String)
meth public void setCacheControl(java.lang.String)
meth public void setContentDisposition(java.lang.String)
meth public void setContentEncoding(java.lang.String)
meth public void setContentLength(long)
meth public void setContentMD5(java.lang.String)
meth public void setContentType(java.lang.String)
meth public void setHeader(java.lang.String,java.lang.Object)
meth public void setLastModified(java.util.Date)
meth public void setUserMetadata(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds metadata,userMetadata

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

CLSS public com.amazonaws.services.s3.model.PartETag
cons public init(int,java.lang.String)
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
meth public boolean isTruncated()
meth public com.amazonaws.services.s3.model.Owner getInitiator()
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.Integer getMaxParts()
meth public java.lang.Integer getNextPartNumberMarker()
meth public java.lang.Integer getPartNumberMarker()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public java.lang.String getUploadId()
meth public java.util.List<com.amazonaws.services.s3.model.PartSummary> getParts()
meth public void setBucketName(java.lang.String)
meth public void setInitiator(com.amazonaws.services.s3.model.Owner)
meth public void setKey(java.lang.String)
meth public void setMaxParts(int)
meth public void setNextPartNumberMarker(int)
meth public void setOwner(com.amazonaws.services.s3.model.Owner)
meth public void setPartNumberMarker(int)
meth public void setParts(java.util.List<com.amazonaws.services.s3.model.PartSummary>)
meth public void setStorageClass(java.lang.String)
meth public void setTruncated(boolean)
meth public void setUploadId(java.lang.String)
supr java.lang.Object
hfds bucketName,initiator,isTruncated,key,maxParts,nextPartNumberMarker,owner,partNumberMarker,parts,storageClass,uploadId

CLSS public com.amazonaws.services.s3.model.PartSummary
cons public init()
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
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.Permission parsePermission(java.lang.String)
meth public static com.amazonaws.services.s3.model.Permission valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.Permission[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.Permission>
hfds permissionString

CLSS public com.amazonaws.services.s3.model.ProgressEvent
cons public init(int)
fld public final static int CANCELED_EVENT_CODE = 8
fld public final static int COMPLETED_EVENT_CODE = 2
fld public final static int FAILED_EVENT_CODE = 4
fld public final static int PART_COMPLETED_EVENT_CODE = 2048
fld public final static int PART_FAILED_EVENT_CODE = 4096
fld public final static int PART_STARTED_EVENT_CODE = 1024
fld public final static int STARTED_EVENT_CODE = 1
meth public int getBytesTransfered()
meth public int getEventCode()
meth public void setBytesTransfered(int)
meth public void setEventCode(int)
supr java.lang.Object
hfds bytesTransfered,eventCode

CLSS public abstract interface com.amazonaws.services.s3.model.ProgressListener
meth public abstract void progressChanged(com.amazonaws.services.s3.model.ProgressEvent)

CLSS public com.amazonaws.services.s3.model.PutObjectRequest
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.io.InputStream,com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.CannedAccessControlList getCannedAcl()
meth public com.amazonaws.services.s3.model.ObjectMetadata getMetadata()
meth public com.amazonaws.services.s3.model.ProgressListener getProgressListener()
meth public com.amazonaws.services.s3.model.PutObjectRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public com.amazonaws.services.s3.model.PutObjectRequest withFile(java.io.File)
meth public com.amazonaws.services.s3.model.PutObjectRequest withInputStream(java.io.InputStream)
meth public com.amazonaws.services.s3.model.PutObjectRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.PutObjectRequest withMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public com.amazonaws.services.s3.model.PutObjectRequest withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
meth public com.amazonaws.services.s3.model.PutObjectRequest withStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public com.amazonaws.services.s3.model.PutObjectRequest withStorageClass(java.lang.String)
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
meth public void setBucketName(java.lang.String)
meth public void setCannedAcl(com.amazonaws.services.s3.model.CannedAccessControlList)
meth public void setFile(java.io.File)
meth public void setInputStream(java.io.InputStream)
meth public void setKey(java.lang.String)
meth public void setMetadata(com.amazonaws.services.s3.model.ObjectMetadata)
meth public void setProgressListener(com.amazonaws.services.s3.model.ProgressListener)
meth public void setStorageClass(com.amazonaws.services.s3.model.StorageClass)
meth public void setStorageClass(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,cannedAcl,file,inputStream,key,metadata,progressListener,storageClass

CLSS public com.amazonaws.services.s3.model.PutObjectResult
cons public init()
meth public java.lang.String getETag()
meth public java.lang.String getVersionId()
meth public void setETag(java.lang.String)
meth public void setVersionId(java.lang.String)
supr java.lang.Object
hfds eTag,versionId

CLSS public final !enum com.amazonaws.services.s3.model.Region
fld public final static com.amazonaws.services.s3.model.Region AP_Singapore
fld public final static com.amazonaws.services.s3.model.Region AP_Tokyo
fld public final static com.amazonaws.services.s3.model.Region EU_Ireland
fld public final static com.amazonaws.services.s3.model.Region US_Standard
fld public final static com.amazonaws.services.s3.model.Region US_West
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.Region fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.Region valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.Region[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.Region>
hfds regionId

CLSS public com.amazonaws.services.s3.model.ResponseHeaderOverrides
cons public init()
fld public final static java.lang.String RESPONSE_HEADER_CACHE_CONTROL = "response-cache-control"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_DISPOSITION = "response-content-disposition"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_ENCODING = "response-content-encoding"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_LANGUAGE = "response-content-language"
fld public final static java.lang.String RESPONSE_HEADER_CONTENT_TYPE = "response-content-type"
fld public final static java.lang.String RESPONSE_HEADER_EXPIRES = "response-expires"
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
supr com.amazonaws.AmazonWebServiceRequest
hfds PARAMETER_ORDER,cacheControl,contentDisposition,contentEncoding,contentLanguage,contentType,expires

CLSS public com.amazonaws.services.s3.model.S3Object
cons public init()
meth public com.amazonaws.services.s3.model.ObjectMetadata getObjectMetadata()
meth public java.io.InputStream getObjectContent()
meth public java.lang.String getBucketName()
meth public java.lang.String getKey()
meth public java.lang.String toString()
meth public void setBucketName(java.lang.String)
meth public void setKey(java.lang.String)
meth public void setObjectContent(java.io.InputStream)
supr java.lang.Object
hfds bucketName,key,metadata,objectContent,serialVersionUID

CLSS public com.amazonaws.services.s3.model.S3ObjectSummary
cons public init()
fld protected com.amazonaws.services.s3.model.Owner owner
fld protected java.lang.String bucketName
fld protected java.lang.String eTag
fld protected java.lang.String key
fld protected java.lang.String storageClass
fld protected java.util.Date lastModified
fld protected long size
meth public com.amazonaws.services.s3.model.Owner getOwner()
meth public java.lang.String getBucketName()
meth public java.lang.String getETag()
meth public java.lang.String getKey()
meth public java.lang.String getStorageClass()
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

CLSS public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketLoggingConfiguration)
meth public com.amazonaws.services.s3.model.BucketLoggingConfiguration getLoggingConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest withLoggingConfiguration(com.amazonaws.services.s3.model.BucketLoggingConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setLoggingConfiguration(com.amazonaws.services.s3.model.BucketLoggingConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,loggingConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest
cons public init(com.amazonaws.services.s3.model.BucketNotificationConfiguration,java.lang.String)
meth public com.amazonaws.services.s3.model.BucketNotificationConfiguration getBucketNotificationConfiguration()
meth public java.lang.String getBucket()
meth public void setBucket(java.lang.String)
meth public void setBucketNotificationConfiguration(com.amazonaws.services.s3.model.BucketNotificationConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucket,bucketNotificationConfiguration

CLSS public com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketVersioningConfiguration)
cons public init(java.lang.String,com.amazonaws.services.s3.model.BucketVersioningConfiguration,com.amazonaws.services.s3.model.MultiFactorAuthentication)
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
meth public com.amazonaws.services.s3.model.BucketWebsiteConfiguration getConfiguration()
meth public com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest withConfiguration(com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
meth public java.lang.String getBucketName()
meth public void setBucketName(java.lang.String)
meth public void setConfiguration(com.amazonaws.services.s3.model.BucketWebsiteConfiguration)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,configuration

CLSS public final !enum com.amazonaws.services.s3.model.StorageClass
fld public final static com.amazonaws.services.s3.model.StorageClass ReducedRedundancy
fld public final static com.amazonaws.services.s3.model.StorageClass Standard
meth public java.lang.String toString()
meth public static com.amazonaws.services.s3.model.StorageClass fromValue(java.lang.String)
meth public static com.amazonaws.services.s3.model.StorageClass valueOf(java.lang.String)
meth public static com.amazonaws.services.s3.model.StorageClass[] values()
supr java.lang.Enum<com.amazonaws.services.s3.model.StorageClass>
hfds storageClassId

CLSS public com.amazonaws.services.s3.model.UploadPartRequest
cons public init()
meth public boolean isLastPart()
meth public com.amazonaws.services.s3.model.ProgressListener getProgressListener()
meth public com.amazonaws.services.s3.model.UploadPartRequest withBucketName(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withFile(java.io.File)
meth public com.amazonaws.services.s3.model.UploadPartRequest withFileOffset(long)
meth public com.amazonaws.services.s3.model.UploadPartRequest withInputStream(java.io.InputStream)
meth public com.amazonaws.services.s3.model.UploadPartRequest withKey(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withLastPart(boolean)
meth public com.amazonaws.services.s3.model.UploadPartRequest withMD5Digest(java.lang.String)
meth public com.amazonaws.services.s3.model.UploadPartRequest withPartNumber(int)
meth public com.amazonaws.services.s3.model.UploadPartRequest withPartSize(long)
meth public com.amazonaws.services.s3.model.UploadPartRequest withProgressListener(com.amazonaws.services.s3.model.ProgressListener)
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
meth public void setPartNumber(int)
meth public void setPartSize(long)
meth public void setProgressListener(com.amazonaws.services.s3.model.ProgressListener)
meth public void setUploadId(java.lang.String)
supr com.amazonaws.AmazonWebServiceRequest
hfds bucketName,file,fileOffset,inputStream,isLastPart,key,md5Digest,partNumber,partSize,progressListener,uploadId

CLSS public com.amazonaws.services.s3.model.UploadPartResult
cons public init()
meth public com.amazonaws.services.s3.model.PartETag getPartETag()
meth public int getPartNumber()
meth public java.lang.String getETag()
meth public void setETag(java.lang.String)
meth public void setPartNumber(int)
supr java.lang.Object
hfds eTag,partNumber

CLSS public com.amazonaws.services.s3.model.VersionListing
cons public init()
meth public boolean isTruncated()
meth public int getMaxKeys()
meth public java.lang.String getBucketName()
meth public java.lang.String getDelimiter()
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
meth public void setKeyMarker(java.lang.String)
meth public void setMaxKeys(int)
meth public void setNextKeyMarker(java.lang.String)
meth public void setNextVersionIdMarker(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTruncated(boolean)
meth public void setVersionIdMarker(java.lang.String)
meth public void setVersionSummaries(java.util.List<com.amazonaws.services.s3.model.S3VersionSummary>)
supr java.lang.Object
hfds bucketName,commonPrefixes,delimiter,isTruncated,keyMarker,maxKeys,nextKeyMarker,nextVersionIdMarker,prefix,versionIdMarker,versionSummaries

CLSS abstract interface com.amazonaws.services.s3.model.package-info

CLSS abstract interface com.amazonaws.services.s3.package-info

CLSS public abstract interface java.io.Serializable

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

