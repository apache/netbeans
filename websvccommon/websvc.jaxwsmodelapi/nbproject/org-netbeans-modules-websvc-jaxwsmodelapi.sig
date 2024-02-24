#Signature file v4.1
#Version 1.50

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

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

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation
fld public final static int TYPE_ASYNC_CALLBACK = 2
fld public final static int TYPE_ASYNC_POLLING = 1
fld public final static int TYPE_NORMAL = 0
meth public abstract int getOperationType()
meth public abstract java.lang.Object getInternalJAXWSOperation()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOperationName()
meth public abstract java.lang.String getReturnTypeName()
meth public abstract java.util.Iterator<java.lang.String> getExceptions()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter> getParameters()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod getJavaMethod()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter
meth public abstract boolean isHolder()
meth public abstract java.lang.Object getInternalJAXWSParameter()
meth public abstract java.lang.String getHolderName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTypeName()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSPort
fld public final static java.lang.String SOAP_VERSION_11 = "http://schemas.xmlsoap.org/wsdl/soap/"
fld public final static java.lang.String SOAP_VERSION_12 = "http://schemas.xmlsoap.org/wsdl/soap12/"
fld public final static java.lang.String STYLE_DOCUMENT = "document"
fld public final static java.lang.String STYLE_RPC = "rpc"
meth public abstract boolean isProvider()
meth public abstract java.lang.Object getInternalJAXWSPort()
meth public abstract java.lang.String getAddress()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getPortGetter()
meth public abstract java.lang.String getSOAPVersion()
meth public abstract java.lang.String getStyle()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation> getOperations()

CLSS public org.netbeans.modules.websvc.jaxwsmodelapi.WSReference
cons public init(java.net.URL,java.lang.String,java.lang.String)
meth public java.lang.String getModuleName()
meth public java.lang.String getWebServiceName()
meth public java.net.URL getWsdlURL()
supr java.lang.Object
hfds moduleName,webServiceName,wsdlURL

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.WSService
meth public abstract java.lang.Object getInternalJAXWSService()
meth public abstract java.lang.String getJavaName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSPort> getPorts()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getPortByName(java.lang.String)

CLSS public org.netbeans.modules.websvc.jaxwsmodelapi.WSTransferable
cons public init(org.netbeans.modules.websvc.jaxwsmodelapi.WSReference)
fld public final static java.awt.datatransfer.DataFlavor WS_FLAVOR
meth protected java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr org.openide.util.datatransfer.ExTransferable$Single
hfds ref

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod
meth public abstract boolean hasParameter(java.lang.String)
meth public abstract java.lang.Object getInternalJAXWSJavaMethod()
meth public abstract java.lang.String getName()
meth public abstract java.util.Iterator getExceptions()
meth public abstract java.util.List<org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter> getParametersList()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter getParameter(java.lang.String)
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType getReturnType()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter
meth public abstract boolean isHolder()
meth public abstract boolean isIN()
meth public abstract boolean isINOUT()
meth public abstract boolean isOUT()
meth public abstract java.lang.Object getInternalJAXWSJavaParameter()
meth public abstract java.lang.String getHolderName()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType getType()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType
meth public abstract boolean isHolder()
meth public abstract boolean isHolderPresent()
meth public abstract boolean isPresent()
meth public abstract java.lang.Object getInternalJAXWSJavaType()
meth public abstract java.lang.String getFormalName()
meth public abstract java.lang.String getHolderName()
meth public abstract java.lang.String getInitString()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getRealName()

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel
meth public abstract java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSService> getServices()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.WSService getServiceByName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider
meth public abstract boolean canAccept(java.net.URL)
meth public abstract java.lang.String getEffectivePackageName()
meth public abstract java.lang.Throwable getCreationException()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel getWsdlModel(java.net.URL,java.lang.String,java.net.URL,boolean)

CLSS public org.openide.util.datatransfer.ExTransferable
fld public final static java.awt.datatransfer.DataFlavor multiFlavor
fld public final static java.awt.datatransfer.Transferable EMPTY
innr public abstract static Single
innr public static Multi
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void addTransferListener(org.openide.util.datatransfer.TransferListener)
meth public final void removeTransferListener(org.openide.util.datatransfer.TransferListener)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public static org.openide.util.datatransfer.ExTransferable create(java.awt.datatransfer.Transferable)
meth public void put(org.openide.util.datatransfer.ExTransferable$Single)
meth public void remove(java.awt.datatransfer.DataFlavor)
supr java.lang.Object
hfds listeners,map
hcls Empty

CLSS public abstract static org.openide.util.datatransfer.ExTransferable$Single
 outer org.openide.util.datatransfer.ExTransferable
cons public init(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavor

