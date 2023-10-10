/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.design.javamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam.Mode;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.openide.execution.ExecutorTask;

/**
 *
 * @author mkuchtiak
 */
public class Utils {
    
    public static boolean isEqualTo(String str1, String str2) {
        if (str1==null) return str2==null;
        else return str1.equals(str2);
    }
    
    public static ProjectService getProjectService(DataObject dataObject){
        FileObject fileObject = dataObject.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(fileObject);
        if(project==null) {
            return null;
        }
        String implClass = getImplClass(fileObject);
        JaxWsModel model = project.getLookup().lookup(JaxWsModel.class);
        if(model==null) {
            JAXWSLightSupport support = JAXWSLightSupport.getJAXWSLightSupport(
                    fileObject);
            if ( support == null ){
                return null;
            }
            List<JaxWsService> services = support.getServices();
            for (JaxWsService service : services) {
                String implementationClass = service.getImplementationClass();
                if ( implClass.equals( implementationClass )){
                    return new LightProjectService( support , service , dataObject );
                }
            }
            return null;
        }
        else {
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            Service service = model.findServiceByImplementationClass(implClass);
            return new ConfigProjectService( support , service , dataObject);
        }
    }
    
    private static String getImplClass( FileObject fileObject ){
        Project project = FileOwnerQuery.getOwner(fileObject);
        if(project==null) {
            return null;
        }
        ClassPath classPath = ClassPath.getClassPath(fileObject,
                ClassPath.SOURCE);
        if (classPath == null) {
            return null;
        }
        String implClass = classPath.getResourceName(fileObject, '.', false);
        return implClass;
    }
    
    public static Service getService(ProjectService projectService){
        if ( projectService instanceof ConfigProjectService ){
            return ((ConfigProjectService)projectService).getService();
        }
        return null;
    }
    
    public static ServiceModel populateModel(final FileObject implClass) 
    {
        final ServiceModel[] model = new ServiceModel[1]; 
        JavaSource javaSource = JavaSource.forFileObject(implClass);
        if (javaSource != null) {
            CancellableTask<CompilationController> task = 
                new CancellableTask<CompilationController>() {
                
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    //CompilationUnitTree cut = controller.getCompilationUnit();

                    ServiceModel serviceModel = ServiceModel.getServiceModel();
                    model[0] = serviceModel;
                    TypeElement classEl = SourceUtils.getPublicTopLevelElement(controller);
                    if (classEl !=null) {
                        //ClassTree javaClass = srcUtils.getClassTree();
                        // find if class is Injection Target
                        List<? extends AnnotationMirror> annotations = 
                            classEl.getAnnotationMirrors();
                        AnnotationMirror webServiceAn=null;
                        for (AnnotationMirror anMirror : annotations) {
                            Element annotationElement = anMirror.getAnnotationType().
                                asElement();
                            if ( annotationElement instanceof TypeElement ){
                                javax.lang.model.element.Name qName = 
                                    ((TypeElement)annotationElement).getQualifiedName();
                                if ( qName.contentEquals("javax.jws.WebService")){  // NOI18N
                                    webServiceAn = anMirror;
                                    break;
                                }
                            }
                        }
                        if (webServiceAn==null) {
                            serviceModel.status = ServiceModel.STATUS_NOT_SERVICE;
                            return;
                        }

                        Map<? extends ExecutableElement, 
                                ? extends AnnotationValue> expressions = 
                                    webServiceAn.getElementValues();
                        boolean nameFound=false;
                        boolean serviceNameFound=false;
                        boolean portNameFound=false;
                        boolean tnsFound = false;
                        for(Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> entry: expressions.entrySet()) 
                        {
                            ExecutableElement ex = entry.getKey();
                            AnnotationValue value = entry.getValue();
                            if (ex.getSimpleName().contentEquals("serviceName")) { //NOI18N
                                serviceModel.serviceName = (String)value.getValue();
                                serviceNameFound=true;
                            } else if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                                serviceModel.name = (String)value.getValue();
                                nameFound=true;
                            } else if (ex.getSimpleName().contentEquals("portName")) { //NOI18N
                                serviceModel.portName = (String)value.getValue();
                                portNameFound=true;
                            } else if (ex.getSimpleName().contentEquals(
                                    "targetNamespace"))  //NOI18N
                            {
                                serviceModel.targetNamespace = (String)value.getValue();
                                tnsFound = true;
                            } else if (ex.getSimpleName().contentEquals(
                                    "endpointInterface")) //NOI18N
                            {
                                serviceModel.endpointInterface = (String)value.getValue();
                            } else if (ex.getSimpleName().contentEquals("wsdlLocation")) { //NOI18N
                                serviceModel.wsdlLocation = (String)value.getValue();
                            }
                        }
                        // set default names
                        if (!nameFound) {
                            serviceModel.name=implClass.getName();
                        }
                        if (!portNameFound) {
                            serviceModel.portName = serviceModel.getName()+"Port"; //NOI18N
                        }
                        if (!serviceNameFound) {
                            serviceModel.serviceName = implClass.getName()+"Service"; //NOI18N
                        }
                        if (!tnsFound) {
                            String qualifName = classEl.getQualifiedName().toString();
                            int ind = qualifName.lastIndexOf(".");
                            String packageName = ind>=0 ? 
                                    qualifName.substring(0,ind) : ""; //NOI18N
                            String ns = getPackageReverseOrder(packageName);
                            serviceModel.targetNamespace = "http://"+ns+"/"; //NOI18N
                        }

                            //TODO: Also have to apply JAXWS/JAXB rules regarding collision of names


                        // use SEI class annotations if endpointInterface annotation is specified
                        TypeElement seiClassEl = null;
                        if (serviceModel.endpointInterface!=null) {
                            seiClassEl = controller.getElements().getTypeElement(
                                    serviceModel.endpointInterface);
                            if (seiClassEl != null) {
                                classEl = seiClassEl;
                            }
                        }

                        boolean foundWebMethodAnnotation=false;
                        TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
                        for (Element member : classEl.getEnclosedElements()) {
                            if (member.getKind() == ElementKind.METHOD) {
                                ExecutableElement methodEl = (ExecutableElement) member;
                                if (methodEl.getModifiers().contains(Modifier.PUBLIC)) {
                                    List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
                                    if (foundWebMethodAnnotation) {
                                        for (AnnotationMirror anMirror : methodAnnotations) {
                                            if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                                methods.add(methodEl);
                                                break;
                                            }
                                        }
                                    } else { // until now no @WebMethod annotations
                                        for (AnnotationMirror anMirror : methodAnnotations) {
                                            if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                                // found first @WebMethod annotation
                                                // need to remove all, previously found, public methods
                                                methods.clear();
                                                foundWebMethodAnnotation=true;
                                                methods.add(methodEl);
                                                break;
                                            }
                                        }
                                        if (!foundWebMethodAnnotation) {
                                            // all methods are supposed to be web methods when missing @WebMethod annotation
                                            methods.add(methodEl);
                                        }
                                    }
                                }
                            }
                        }
                        // populate methods

                        List<MethodModel> operations = new ArrayList<MethodModel>();
                        if (methods.size()==0) {
                            serviceModel.operations=operations;
                            serviceModel.status = ServiceModel.STATUS_INCORRECT_SERVICE;
                            return;
                        }

                        boolean hasEndpointInterfaceAttr = serviceModel.endpointInterface != null;

                        // search for SEI class
                        FileObject seiClass = null;
                        if(hasEndpointInterfaceAttr){
                            // first look for SEI class in src hierarchy (SEI exists but : WS from Java case)
                            ClassPath classPath = ClassPath.getClassPath(implClass, ClassPath.SOURCE);
                            FileObject[] srcRoots = classPath.getRoots();
                            for (FileObject srcRoot:srcRoots) {
                                String seiClassResource = serviceModel.endpointInterface.replace('.', '/')+".java"; //NOI18N
                                seiClass = srcRoot.getFileObject(seiClassResource);
                                if (seiClass != null) {
                                    break;
                                }
                            }                        
                            if (seiClass == null) { // looking for SEI class under build/generated directory
                                final Project project = FileOwnerQuery.getOwner(implClass);
                                String seiPath = "build/generated/wsimport/service/" + serviceModel.endpointInterface.replace('.', '/') + ".java"; //NOI18N
                                seiClass = project.getProjectDirectory().getFileObject(seiPath);
                                if(seiClass == null){
                                    invokeWsImport(project, serviceModel.getName());
                                    seiClass = project.getProjectDirectory().getFileObject(seiPath);
                                }
                            }

                        }

                        // populate methods
                        for (int i=0;i<methods.size();i++) {
                            MethodModel operation = new MethodModel();
                            if (hasEndpointInterfaceAttr && seiClass != null) {
                                operation.setImplementationClass(seiClass);
                            } else {
                                operation.setImplementationClass(implClass);
                            }
                            ElementHandle methodHandle = ElementHandle.create(methods.get(i));
                            operation.setMethodHandle(methodHandle);
                            Utils.populateOperation(controller, methods.get(i), methodHandle, operation, serviceModel.getTargetNamespace());
                            operations.add(operation);
                        }
                        serviceModel.operations=operations;
                    } else {
                        serviceModel.status = ServiceModel.STATUS_INCORRECT_SERVICE;
                    }
                }
                @Override
                public void cancel() {
                }
            };

            try {
                Future<Void> future = javaSource.runWhenScanFinished(task, true);
                future.get();
                return model[0];
            } 
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            catch( CancellationException ex ){
                ErrorManager.getDefault().notify(ex);
            }
            catch( ExecutionException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            catch( InterruptedException ex){
                ErrorManager.getDefault().notify(ex);
            }
        }
        return null;
    }
    
    private static void populateOperation(CompilationController controller, ExecutableElement methodEl, ElementHandle methodHandle, MethodModel methodModel, String targetNamespace) {
        TypeElement methodAnotationEl = controller.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        TypeElement onewayAnotationEl = controller.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
        TypeElement resultAnotationEl = controller.getElements().getTypeElement("javax.jws.WebResult"); //NOI18N
        List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
        
        ResultModel resultModel = new ResultModel();
        
        boolean nameFound=false;
        boolean resultNameFound=false;
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (controller.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
                    expressions.entrySet()) 
                {
                    ExecutableElement ex = entry.getKey();
                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                        methodModel.operationName = (String)entry.getValue().getValue();
                        nameFound=true;
                    } else if (ex.getSimpleName().contentEquals("action")) { //NOI18N
                        methodModel.action = (String)entry.getValue().getValue();
                    }
                }
                
            } else if (controller.getTypes().isSameType(resultAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
                    expressions.entrySet()) 
                {
                    ExecutableElement ex = entry.getKey();
                    if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                        resultModel.setName((String)entry.getValue().getValue());
                        resultNameFound=true;
                    } else if (ex.getSimpleName().contentEquals("partName")) { //NOI18N
                        resultModel.setPartName((String)entry.getValue().getValue());
                    } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                        resultModel.setTargetNamespace((String)entry.getValue().getValue());
                    }
                }
            } else if (controller.getTypes().isSameType(onewayAnotationEl.asType(), anMirror.getAnnotationType())) {
                methodModel.oneWay = true;
            }
        }
        methodModel.javaName = methodEl.getSimpleName().toString();
        if (!nameFound) methodModel.operationName = methodModel.javaName;
        
        
        // Return type
        if (!methodModel.isOneWay()) {
            // set default result name
            if (!resultNameFound) resultModel.setName("return"); //NOI18N
            // set result type
            resultModel.setResultType(methodEl.getReturnType().toString());
        }
        methodModel.setResult(resultModel);
        
        
        // populate faults
        List<? extends TypeMirror> faultTypes = methodEl.getThrownTypes();
        List<FaultModel> faults = new ArrayList<FaultModel>();
        for (TypeMirror faultType:faultTypes) {
            FaultModel faultModel = new FaultModel();
            boolean faultFound=false;
            if (faultType.getKind() == TypeKind.DECLARED) {
                TypeElement faultEl = (TypeElement)((DeclaredType)faultType).asElement();
                TypeElement faultAnotationEl = controller.getElements().getTypeElement("javax.xml.ws.WebFault"); //NOI18N
                List<? extends AnnotationMirror> faultAnnotations = faultEl.getAnnotationMirrors();
                for (AnnotationMirror anMirror : faultAnnotations) {
                    if (controller.getTypes().isSameType(faultAnotationEl.asType(), anMirror.getAnnotationType())) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> it : expressions.entrySet()) {
                            if (it.getKey().getSimpleName().contentEquals("name")) { //NOI18N
                                faultModel.setName((String) it.getValue().getValue());
                                faultFound = true;
                            } else if (it.getKey().getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                                faultModel.setTargetNamespace((String) it.getValue().getValue());
                            }
                        }
                    }
                }
                faultModel.setFaultType(faultEl.getQualifiedName().toString());
            } else {
                faultModel.setFaultType(faultType.toString());
            }
            if (!faultFound) {
                String fullyQualifiedName = faultModel.getFaultType();
                int index = fullyQualifiedName.lastIndexOf("."); //NOI18N
                faultModel.setName(index>=0?fullyQualifiedName.substring(index+1):fullyQualifiedName);
            }
            faults.add(faultModel);
        }
        methodModel.setFaults(faults);
        
        initJavaDoc(controller, methodEl, methodModel);
        
        
        // populate params
        List<? extends VariableElement> paramElements = methodEl.getParameters();
        List<ParamModel> params = new ArrayList<ParamModel>();
        int i=0;
        for (VariableElement paramEl:paramElements) {
            ParamModel param = new ParamModel("arg"+String.valueOf(i++), paramEl.getSimpleName().toString());
            param.setImplementationClass(methodModel.getImplementationClass());
            param.setMethodHandle(methodHandle);
            populateParam(controller, paramEl, param);
            params.add(param);
        }
        methodModel.setParams(params);
        
        // set SOAP Request
        setSoapRequest(methodModel, targetNamespace);
        
        // set SOAP Response
        setSoapResponse(methodModel, targetNamespace);
    }
    
    private static void initJavaDoc( CompilationController controller,
            ExecutableElement methodEl, MethodModel methodModel )
    {
        // populate javadoc
        DocCommentTree javadoc = controller.getDocTrees().getDocCommentTree(methodEl);
        if (javadoc!=null) {
            //methodModel.setJavadoc(javadoc.getRawCommentText());
            JavadocModel javadocModel = new JavadocModel(javadoc.toString());
            methodModel.setJavadoc(javadocModel);
        }
    }

    
    private static void populateParam(CompilationController controller, VariableElement paramEl, ParamModel paramModel) {
        paramModel.setParamType(paramEl.asType().toString());
        TypeElement paramAnotationEl = controller.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
        List<? extends AnnotationMirror> paramAnnotations = paramEl.getAnnotationMirrors();
        for (AnnotationMirror anMirror : paramAnnotations) {
            if (controller.getTypes().isSameType(paramAnotationEl.asType(), anMirror.getAnnotationType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
                    expressions.entrySet()) 
                {
                    ExecutableElement ex = entry.getKey();
                    AnnotationValue value = entry.getValue();
                    if (ex.getSimpleName().contentEquals("name")) { //NOI18N
                        paramModel.name = (String)value.getValue();
                    } else if (ex.getSimpleName().contentEquals("partName")) { //NOI18N
                        paramModel.setPartName((String)value.getValue());
                    } else if (ex.getSimpleName().contentEquals("targetNamespace")) { //NOI18N
                        paramModel.setTargetNamespace((String)value.getValue());
                    } else if (ex.getSimpleName().contentEquals("mode")) { //NOI18N
                        paramModel.setMode(Mode.valueOf(value.getValue().toString()));
                    }
                }
            }
        }
    }
    
    private static void setSoapRequest(MethodModel methodModel, String tns) {
        MessageFactory messageFactory = null;
        try {
            // create a sample SOAP request using SAAJ API
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.FINE, 
                    NbBundle.getMessage(Utils.class, "MSG_SAAJ_PROBLEM"), //NOI18N
                    ex);
        }
        if (messageFactory != null) {
            try {
                SOAPMessage request = messageFactory.createMessage();
                MimeHeaders headers = request.getMimeHeaders();
                String action = methodModel.getAction();
                headers.addHeader("SOAPAction", action==null? "\"\"":action); //NOI18N
                SOAPPart part = request.getSOAPPart();
                SOAPEnvelope envelope = part.getEnvelope();
                String prefix = envelope.getPrefix();
                if (!"soap".equals(prefix)) { //NOI18N
                    envelope.removeAttribute("xmlns:"+prefix); // NOI18N
                    envelope.setPrefix("soap"); //NOI18N
                }
                SOAPBody body = envelope.getBody();
                body.setPrefix("soap"); //NOI18N

                // removing soap header
                SOAPHeader header = envelope.getHeader();
                envelope.removeChild(header);

                // implementing body
                Name methodName = envelope.createName(methodModel.getOperationName());
                SOAPElement methodElement = body.addBodyElement(methodName);
                methodElement.setPrefix("ns0"); //NOI18N
                methodElement.addNamespaceDeclaration("ns0",tns); //NOI18N

                // params
                List<ParamModel> params = methodModel.getParams();
                int i=0;
                for (ParamModel param:params) {
                    String paramNs = param.getTargetNamespace();
                    Name paramName = null;
                    if ( param.getName() == null || param.getName().trim().length() ==0 ){
                        continue;
                    }
                    if (paramNs!=null && paramNs.length()>0) {
                        String pref = "ns"+String.valueOf(++i); //NOI18N
                        paramName = envelope.createName(param.getName(), pref, paramNs);
                        methodElement.addNamespaceDeclaration(pref,paramNs);
                    } else {
                        paramName = envelope.createName(param.getName());
                    }

                    SOAPElement paramElement = methodElement.addChildElement(paramName);

                    String paramType = param.getParamType();
                    if ("javax.xml.namespace.QName".equals(paramType)) {
                        paramElement.addNamespaceDeclaration("sampleNs", "http://www.netbeans.org/sampleNamespace");
                        paramElement.addTextNode("sampleNs:sampleQName");
                    } else {
                        paramElement.addTextNode(getSampleValue(paramType));
                    }
                }

                methodModel.setSoapRequest(request);

            } catch (SOAPException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    private static void setSoapResponse(MethodModel methodModel, String tns) {
        if (methodModel.isOneWay()) return;
        MessageFactory messageFactory = null;
        try {
            // create a sample SOAP request using SAAJ API
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.FINE, 
                    NbBundle.getMessage(Utils.class, "MSG_SAAJ_PROBLEM"), //NOI18N
                    ex);
        }
        if (messageFactory != null) {
            try {
                SOAPMessage response = messageFactory.createMessage();
                SOAPPart part = response.getSOAPPart();
                SOAPEnvelope envelope = part.getEnvelope();
                String prefix = envelope.getPrefix();
                if (!"soap".equals(prefix)) { //NOI18N
                    envelope.removeAttribute("xmlns:"+prefix); // NOI18N
                    envelope.setPrefix("soap"); //NOI18N
                }
                SOAPBody body = envelope.getBody();
                body.setPrefix("soap"); //NOI18N

                // removing soap header
                SOAPHeader header = envelope.getHeader();
                envelope.removeChild(header);

                // implementing body
                Name responseName = envelope.createName(methodModel.getOperationName()+"Response"); //NOI18N
                SOAPElement responseElement = body.addBodyElement(responseName);
                responseElement.setPrefix("ns0"); //NOI18N
                responseElement.addNamespaceDeclaration("ns0",tns); //NOI18N

                // return

                ResultModel resultModel = methodModel.getResult();
                String resultNs = resultModel.getTargetNamespace();

                Name resultName = null;
                if (resultNs!=null && resultNs.length()>0) {
                    responseElement.addNamespaceDeclaration("ns1",resultNs); //NOI18N
                    resultName = envelope.createName(resultModel.getName(), "ns1", resultNs); //NOI18N
                } else {
                    resultName = envelope.createName(resultModel.getName()); //NOI18N
                }

                String resultType = resultModel.getResultType();
                if ("javax.xml.namespace.QName".equals(resultType)) {
                    SOAPElement resultElement = responseElement.addChildElement(resultName);
                    resultElement.addNamespaceDeclaration("sampleNs", "http://www.netbeans.org/sampleNamespace");
                    resultElement.addTextNode("sampleNs:sampleQName");
                } else if (!"void".equals(resultType)) { //NOI18N
                    SOAPElement resultElement = responseElement.addChildElement(resultName);
                    resultElement.addTextNode(getSampleValue(resultType));
                }

                methodModel.setSoapResponse(response);

            } catch (SOAPException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    private static String getSampleValue(String paramType) {
        if ("java.lang.String".equals(paramType)) {
            return "sample text"; //NOI18N
        } else if ("int".equals(paramType) || //NOI18N
                "java.lang.Integer".equals(paramType) || //NOI18N
                "java.math.BigInteger".equals(paramType)) { //NOI18N
            return "99"; //NOI18N
        } else if ("double".equals(paramType) || "java.lang.Double".equals(paramType)) { //NOI18N
            return "999.999"; //NOI18N
        } else if ("float".equals(paramType) || //NOI18N
                "java.lang.Float".equals(paramType) || //NOI18N
                "java.math.BigDecimal".equals(paramType)) {//NOI18N
            return "99.99"; //NOI18N
        } else if ("long".equals(paramType) || "java.lang.Long".equals(paramType)) { //NOI18N
            return "999"; //NOI18N
        } else if ("boolean".equals(paramType) || "java.lang.Boolean".equals(paramType)) { //NOI18N
            return "false"; //NOI18N
        } else if ("char".equals(paramType) || //NOI18N
                "java.lang.Char".equals(paramType) || //NOI18N
                "short".equals(paramType) || //NOI18N
                "java.lang.Short".equals(paramType)) { //NOI18N
            return "65"; //NOI18N
        } else if ("byte[]".equals(paramType)) { //NOI18N
            return "73616D706C652074657874"; //NOI18N
        } else if ("javax.xml.datatype.XMLGregorianCalendar".equals(paramType) || //NOI18N
                "java.util.Date".equals(paramType) || //NOI18N
                "java.util.Calendar".equals(paramType) || //NOI18N
                "java.util.GregorianCalendar".equals(paramType)) { //NOI18N
            return "2007-04-19"; //NOI18N
        } else if ("javax.xml.datatype.Duration".equals(paramType)) { //NOI18N
            return "P2007Y4M"; //NOI18N
        } else if ("java.net.URI".equals(paramType) || "java.net.URL".equals(paramType)) { //NOI18N
            return "http://www.netbeans.org/sampleURI"; //NOI18N
        } else return "..."; //NOI18N
    }
    
    public static void setJavadoc(final FileObject implClass, final MethodModel methodModel, final String text) {
        final JavaSource javaSource = JavaSource.forFileObject(implClass);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(workingCopy);
                List<? extends Tree> members = classTree.getMembers();
                TypeElement methodAnotationEl = workingCopy.getElements().
                    getTypeElement("javax.jws.WebMethod"); //NOI18N
                if (methodAnotationEl==null) {
                    return;
                }
                MethodTree targetMethod=null;
                for (Tree member:members) {
                    if (Tree.Kind.METHOD==member.getKind()) {
                        MethodTree method = (MethodTree)member;
                        TreePath methodPath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), method);
                        ExecutableElement methodEl = (ExecutableElement)workingCopy.getTrees().getElement(methodPath);
                        // browse annotations to find target method
                        List<? extends AnnotationMirror> methodAnnotations = methodEl.getAnnotationMirrors();
                        for (AnnotationMirror anMirror : methodAnnotations) {
                            if (workingCopy.getTypes().isSameType(methodAnotationEl.asType(), anMirror.getAnnotationType())) {
                                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                                for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
                                    expressions.entrySet()) 
                                {
                                    ExecutableElement ex = entry.getKey();
                                    if (ex.getSimpleName().contentEquals("operationName")) { //NOI18N
                                        if (methodModel.getOperationName().equals(
                                                entry.getValue().getValue())) 
                                        {
                                            targetMethod = method;
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        // if annotation not found check method name
                        if (targetMethod!=null) break;
                        else if (method.getName().contentEquals(methodModel.getOperationName())) {
                            targetMethod = method;
                            break;
                        }
                    }
                    
                }
                if (targetMethod!=null) {
                    Comment comment = Comment.create(Style.JAVADOC, 0,0,0, text);
                    MethodTree newMethod = make.setLabel(targetMethod, targetMethod.getName());
                    
                    ElementHandle<?> methodHandle = methodModel.getMethodHandle();
                    Element method = methodHandle.resolve(workingCopy);
                    if ( method == null ){
                        return;
                    }
                    
                    DocCommentTree javadoc = workingCopy.getDocTrees().getDocCommentTree(method);
                    if ( javadoc != null ){
                        make.removeComment(newMethod, 0, true);
                    }
                    /*MethodTree copy = make.Method(targetMethod.getModifiers(),
                            targetMethod.getName(),
                            targetMethod.getReturnType(),
                            targetMethod.getTypeParameters(),
                            targetMethod.getParameters(),
                            targetMethod.getThrows(),
                            targetMethod.getBody(),
                            (ExpressionTree) targetMethod.getDefaultValue()
                    );
                    make.removeComment(copy, 0, true);*/
                    make.addComment(newMethod, comment, true);
                    workingCopy.rewrite(targetMethod, newMethod);
                }
                
            }
            @Override
            public void cancel() {
                
            }
            
        };
        try {
            javaSource.runWhenScanFinished(new Task<CompilationController>(){

                @Override
                public void run(CompilationController controller) throws Exception {
                    javaSource.runModificationTask(modificationTask).commit();
                    
                    controller.toPhase(Phase.RESOLVED);
                    ElementHandle<?> methodHandle = methodModel.getMethodHandle();
                    Element method = methodHandle.resolve(controller);
                    if ( method == null ){
                        return;
                    }
                    initJavaDoc(controller, (ExecutableElement)method, methodModel);
                }

                }, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public static  String getFormatedDocument(SOAPMessage message) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            StreamResult result = new StreamResult(new StringWriter());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            message.writeTo(bos);
            InputStream bis = new ByteArrayInputStream(bos.toByteArray());
            StreamSource source = new StreamSource(bis);
            
            transformer.transform(source, result);
            
            return result.getWriter().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public  static void invokeWsImport(final Project project, final String serviceName) {
        if (project!=null) {
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                // call wsimport only for services from wsdl
                Service service = jaxWsModel.findServiceByName(serviceName);
                if (service != null && service.getWsdlUrl() != null) {
                    final FileObject buildImplFo = project.getProjectDirectory().getFileObject("nbproject/build-impl.xml"); //NOI18N
                    try {
                        ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Boolean>() {
                            public Boolean run() throws IOException {
                                JAXWSSupport support = JAXWSSupport.getJAXWSSupport(
                                        project.getProjectDirectory());
                                Properties props = WSUtils.identifyWsimport(
                                        support.getAntProjectHelper());
                                ExecutorTask wsimportTask =
                                        ActionUtils.runTarget(buildImplFo,
                                                new String[]{"wsimport-service-clean-"+serviceName,
                                                    "wsimport-service-"+serviceName},props); //NOI18N                                       ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-client-"+finalName,"wsimport-client-compile" },null); //NOI18N
                                wsimportTask.waitFinished();
                                return Boolean.TRUE;
                            }
                        });
                    } catch (MutexException e) {
                        ErrorManager.getDefault().log(e.getLocalizedMessage());
                    }
                }
            }
        }
    }
    

    public static String getCurrentJavaName(final MethodModel method){
        final String[] javaName = new String[1];
        final JavaSource javaSource = JavaSource.forFileObject(method.getImplementationClass());
        final CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.RESOLVED);
                ElementHandle<?> methodHandle = method.getMethodHandle();
                Element methodEl = methodHandle.resolve(controller);
                javaName[0] =  methodEl.getSimpleName().toString();
            }
            @Override
            public void cancel() {
            }
        };
        try {
            javaSource.runUserActionTask(task, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return javaName[0];
    }
    
    /**
     * Obtains the value of an annotation's attribute if that attribute is present.
     * @param clazz The Java source to parse
     * @param annotationClass Fully qualified name of the annotation class
     * @param attributeName Name of the attribute whose value is returned
     * @return String Returns the string value of the attribute. Returns empty string if attribute is not found.
     */
    public static String getAttributeValue(FileObject clazz, final String annotationClass, final String attributeName){
        JavaSource javaSource = JavaSource.forFileObject(clazz);
        final String[] attributeValue = new String[]{""};
        if (javaSource!=null) {
            CancellableTask<CompilationController> task = 
                new CancellableTask<CompilationController>() 
                {
                
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                    if(typeElement != null ){
                        List<? extends AnnotationMirror> annotations = 
                            typeElement.getAnnotationMirrors();
                        for (AnnotationMirror anMirror : annotations) {
                            Element annotationElement = anMirror.getAnnotationType().
                                asElement();
                            if ( annotationElement instanceof TypeElement ){
                                javax.lang.model.element.Name fqn  = 
                                    ((TypeElement)annotationElement).getQualifiedName();
                                if ( !fqn.contentEquals( annotationClass )){
                                    continue;
                                }
                            }
                            else {
                                continue;
                            }
                            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                                expressions = anMirror.getElementValues();
                            for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
                                expressions.entrySet()) 
                            {
                                ExecutableElement ex = entry.getKey();
                                if (ex.getSimpleName().contentEquals(attributeName)) {
                                    String interfaceName =  (String)entry.getValue().getValue();
                                    if(interfaceName != null){
                                        attributeValue[0] = URLEncoder.encode(interfaceName,"UTF-8"); //NOI18N
                                        break;
                                    }
                                }
                            }
                            
                        }
                    }
                }
                @Override
                public void cancel() {
                }
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return attributeValue[0];
    }

    private static String getPackageReverseOrder(String packageName) {
        String[] names = packageName.split("\\."); //NOI18N
        StringBuffer buf = new StringBuffer();
        for (int i=names.length-1 ; i >= 0 ; i--) {
            if (i<names.length-1) {
                buf.append("."); //NOI18N
            }
            buf.append(names[i]);
        }
        return buf.toString();
    }
}
