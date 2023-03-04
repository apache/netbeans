/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.jaxwsruntimemodel;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.wsitconf.util.AbstractTask;
import org.netbeans.modules.websvc.wsitconf.util.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class is compiled from:
 * com.sun.xml.ws.model.RuntimeModeler
 * com.sun.xml.ws.model.SEIModel
 * com.sun.xml.ws.model.AbstractSEIModelImpl
 * com.sun.xml.ws.wsdl.writer.WSDLGenerator
 * in JAX-WS component.
 * @author Martin Grebac
 */
public class JavaWsdlMapper {
    
    public static final String RESPONSE             = "Response";   //NOI18N
    public static final String RETURN               = "return";     //NOI18N
    public static final String BEAN                 = "Bean";       //NOI18N
    public static final String SERVICE              = "Service";    //NOI18N
    public static final String PORT                 = "Port";       //NOI18N
    public static final String PORT_TYPE            = "PortType";   //NOI18N
    public static final String BINDING              = "Binding";    //NOI18N

    private static final String JAVAX_JWS_WEBSERVICE = "javax.jws.WebService"; // NOI18N
    private static final String UTF8 = "UTF-8";
    
    /** Creates a new instance of JavaWsdlMapper */
    public JavaWsdlMapper() { }
    
    /**
     * gets the namespace <code>String</code> for a given <code>packageName</code>
     * @param packageName the name of the package used to find a namespace
     * @return the namespace for the specified <code>packageName</code>
     */
    public static String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i=tokenizer.countTokens()-1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuilder namespace = new StringBuilder("http://");     //NOI18N
        for (int i=0; i<tokens.length; i++) {
            if (i!=0) {
                namespace.append('.');
            }
            namespace.append(tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

    /**
     * gets the <code>wsdl:serviceName</code> for a given implementation class
     * @param implClass the implementation class
     * @return the <code>wsdl:serviceName</code> for the <code>implClass</code>
     */
    public static QName getServiceName(FileObject implClass) {
        final java.lang.String[] serviceNameQNameARR = new String[2];
        if (implClass == null) {
            return null;
        }
        try {    
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js== null){
                return null;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                @Override
                 public void run(CompilationController controller) 
                    throws java.io.IOException 
                 {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     if (sourceUtils == null) return; // see 147028
                     TypeElement te = sourceUtils.getTypeElement();

                     serviceNameQNameARR[0] = te.getSimpleName().toString() + SERVICE;
                     String packageName = getPackageFromClass(te.getQualifiedName().toString());
                     serviceNameQNameARR[1] = getNamespace(packageName);

                     AnnotationMirror annotation = getAnnotation(te , JAVAX_JWS_WEBSERVICE);
                    if (annotation!=null ) { 
                        String serviceNameAnnot = null;
                        String targetNamespaceAnnot = null;

                        Map<? extends ExecutableElement, 
                                ? extends AnnotationValue> expressions = 
                                        annotation.getElementValues();
                        for (Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> ex : 
                                    expressions.entrySet()) 
                        {
                            ExecutableElement el = ex.getKey();
                            String val = (String) ex.getValue().getValue();
                            if (el.getSimpleName().contentEquals("serviceName")) {  //NOI18N
                                serviceNameAnnot = val;
                                if (serviceNameAnnot!=null) {
                                    serviceNameAnnot = URLEncoder.
                                    encode(serviceNameAnnot,UTF8); //NOI18N
                                }
                            } else if (el.getSimpleName().
                                    contentEquals("targetNamespace"))   //NOI18N
                            {
                                targetNamespaceAnnot = val;
                                if (targetNamespaceAnnot!=null) {
                                    targetNamespaceAnnot = URLEncoder.
                                    encode(targetNamespaceAnnot,UTF8); 
                                }
                            }
                            if (targetNamespaceAnnot!=null && serviceNameAnnot!=null) {
                                break;
                            }
                        }
                        if (serviceNameAnnot != null) {
                            serviceNameQNameARR[0] = serviceNameAnnot;
                        }
                        if (targetNamespaceAnnot != null) {
                            serviceNameQNameARR[1] = targetNamespaceAnnot;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (serviceNameQNameARR[0] == null) {
            return null;
        } else {
            return new QName(serviceNameQNameARR[1], serviceNameQNameARR[0]);
        }
    }

    /**
     * gets the <code>wsdl:portName</code> for a given implementation class
     * @param implClass the implementation class
     * @param targetNamespace Namespace URI for service name
     * @return the <code>wsdl:portName</code> for the <code>implClass</code>
     */
    public static String getBindingName(FileObject implClass, String targetNamespace) {
        QName portName = getPortName(implClass, targetNamespace);
        if (portName != null) {
            return (portName.getLocalPart() + BINDING);
        }
        return null;
    }

    public static final int UNKNOWN = -1;
    public static final int OUTPUTINPUT = 0;
    public static final int OUTPUT = 1;
    public static final int INPUT = 2;
    
    @SuppressWarnings("unchecked")
    public static List<String> getOperationFaults(FileObject implClass, final String operationName) {
        if ((implClass == null) || (operationName == null)) {
             return Collections.EMPTY_LIST;
        }
                
        final List<String> faults = new ArrayList<String>();
        try {    
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js== null){
                return faults;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                 @Override
                 public void run(CompilationController controller) 
                     throws java.io.IOException 
                 {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     TypeElement te = sourceUtils.getTypeElement();
                     List<? extends Element> members = te.getEnclosedElements();
                     List<ExecutableElement> methods = ElementFilter.methodsIn(members);
                     for (ExecutableElement method:methods) {
                        Set<Modifier> modifiers = method.getModifiers();
                        if (modifiers.contains(Modifier.PUBLIC)) {
                            AnnotationMirror an = getAnnotation(method, "javax.jws.WebMethod");    // NOI18N
                            boolean hasWebMethodAnnotation=false;
                            String nameAnnot = null;
                            if(hasWebMethodAnnotation) {
                                Map<? extends ExecutableElement,
                                        ? extends AnnotationValue> expressions = 
                                                an.getElementValues();
                                for (Entry<? extends ExecutableElement, 
                                        ? extends AnnotationValue> ex : 
                                            expressions.entrySet()) 
                                {
                                    ExecutableElement el = ex.getKey();
                                    String val = (String) ex.getValue().getValue();
                                    if (el.getSimpleName().contentEquals(
                                            "operationName"))          //NOI18N
                                    {
                                        nameAnnot = val;
                                        if (nameAnnot!=null) nameAnnot = 
                                            URLEncoder.encode(nameAnnot,UTF8); //NOI18N
                                    }
                                    if (nameAnnot!=null) {
                                        break;
                                    }
                                }
                            }
                            String opName = method.getSimpleName().toString();
                            if ((hasWebMethodAnnotation) && (nameAnnot != null)) {
                                opName = nameAnnot;
                            }
                            if (operationName.equals(opName)) {
                                List<? extends TypeMirror> excs = method.getThrownTypes();
                                for (TypeMirror ex:excs) {
                                    String tName = getTypeName(controller, ex);
                                    if (tName != null){
                                        faults.add(tName);
                                    }
                                }
                            }
                        }
                     }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return faults;
    }

    static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN : return "boolean"; // NOI18N
            case BYTE : return "byte"; // NOI18N
            case CHAR : return "char"; // NOI18N
            case DOUBLE : return "double"; // NOI18N
            case FLOAT : return "float"; // NOI18N
            case INT : return "int"; // NOI18N
            case LONG : return "long"; // NOI18N
            case SHORT : return "short"; // NOI18N
            case VOID : return "void"; // NOI18N
            case ERROR :
            case DECLARED :
                if ( typeMirror instanceof DeclaredType ){
                    Element element = controller.getTypes().asElement(typeMirror);
                    return ((TypeElement) element).getSimpleName().toString();
                }
                else {
                    break;
                }
            case ARRAY : 
                ArrayType arrayType = (ArrayType) typeMirror;
                Element componentTypeElement = controller.getTypes().asElement(arrayType.getComponentType());
                return ((TypeElement) componentTypeElement).getSimpleName().toString() + "[]";
            case EXECUTABLE :
            case NONE :
            case NULL :
            case OTHER :
            case PACKAGE :
            case TYPEVAR :
            case WILDCARD :
            default:break;
        }
        return null;
    }
    
    public static List<String> getOperationNames(FileObject implClass) {
        final List<String> operations = new ArrayList<String>();
        if (implClass == null) {
            return operations;
        }
        try {    
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js== null){
                return operations;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                 public void run(CompilationController controller) throws java.io.IOException {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     TypeElement te = sourceUtils.getTypeElement();

                     List<? extends Element> members = te.getEnclosedElements();
                     List<ExecutableElement> methods = ElementFilter.methodsIn(members);
                     boolean foundWebMethodAnnotation=false;
                     for (ExecutableElement method:methods) {
                        Set<Modifier> modifiers = method.getModifiers();
                        if (modifiers.contains(Modifier.PUBLIC)) {
                            List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
                            boolean hasWebMethodAnnotation=false;
                            String nameAnnot = null;
                            AnnotationMirror an = getAnnotation(method, "javax.jws.WebMethod"); // NOI18N
                            hasWebMethodAnnotation=an!=null;
                            if ( hasWebMethodAnnotation ) {
                                Map<? extends ExecutableElement,
                                        ? extends  AnnotationValue> expressions = 
                                            an.getElementValues();
                                for(Entry<? extends ExecutableElement,
                                        ? extends  AnnotationValue> ex : 
                                            expressions.entrySet()) 
                                {
                                    ExecutableElement el = ex.getKey();
                                    String val = (String) ex.getValue().getValue();
                                    if (el.getSimpleName().contentEquals(
                                            "operationName"))          //NOI18N
                                    {
                                        nameAnnot = val;
                                        if (nameAnnot!=null) {
                                            nameAnnot = URLEncoder.encode(
                                                    nameAnnot,UTF8); 
                                        }
                                    }
                                    if (nameAnnot!=null) {
                                        break;
                                    }
                                }
                            }
                            if (hasWebMethodAnnotation) {
                                if (!foundWebMethodAnnotation) {
                                    foundWebMethodAnnotation=true;
                                    // remove all methods added before because only annotated methods should be added
                                    if (!operations.isEmpty()) {
                                        operations.clear();
                                    }
                                }
                                if (nameAnnot != null) {
                                    operations.add(nameAnnot);
                                } else {
                                    operations.add(method.getSimpleName().toString());
                                }
                            } else {
                                // there are only non-annotated methods present until now
                                operations.add(method.getSimpleName().toString());
                            }
                        }
                     }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return operations;
    }
    
    public static QName getPortTypeName(FileObject implClass) {
        final java.lang.String[] portTypeQNameARR = new String[2];
        if (implClass == null) {
            return null;   
        }
        try {    
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js==null){
                return null;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                @Override
                 public void run(CompilationController controller) throws IOException {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     TypeElement te = sourceUtils.getTypeElement();
                     portTypeQNameARR[0] = te.getSimpleName().toString();

                     AnnotationMirror annotation = getAnnotation(te, JAVAX_JWS_WEBSERVICE);
                     if ( annotation!= null ){
                        String nameAnnot = null;
                        String targetNamespaceAnnot = null;

                        Map<? extends ExecutableElement, ? extends AnnotationValue> 
                            expressions = annotation.getElementValues();
                        for(Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> ex : expressions.entrySet()) 
                        {
                            ExecutableElement el = ex.getKey();
                            String val = (String) ex.getValue().getValue();
                            if (el.getSimpleName().contentEquals("name")) {         //NOI18N
                                nameAnnot = val;
                                if (nameAnnot!=null) nameAnnot = 
                                    URLEncoder.encode(nameAnnot,UTF8);
                            } 
                            else if (el.getSimpleName().contentEquals(
                                    "targetNamespace"))    //NOI18N
                            {
                                targetNamespaceAnnot = val;
                                if (targetNamespaceAnnot!=null) {
                                    targetNamespaceAnnot = URLEncoder.encode(
                                            targetNamespaceAnnot,UTF8); 
                                }
                            }
                            if (targetNamespaceAnnot!=null && nameAnnot!=null) {
                                break;
                            }
                        }

                        if ((nameAnnot != null) && (nameAnnot.length() > 0)) {
                            portTypeQNameARR[0] = nameAnnot;
                        }

                        String pkg = getPackageFromClass(
                                te.getQualifiedName().toString());

                        String targetNamespace = targetNamespaceAnnot;
                        if ((targetNamespace == null) || (targetNamespace.length() == 0)) {
                            targetNamespace = getNamespace(pkg);
                        }
                        
                        portTypeQNameARR[1] = targetNamespace;
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new QName(portTypeQNameARR[1], portTypeQNameARR[0]);
    }
    
    public static String getWsdlLocation(FileObject implClass) {        
        final java.lang.String[] wsdlLocARR = new String[1];
        try {
            if (implClass == null) {
                return null;
            }
            
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js== null){
                return null;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                @Override
                 public void run(CompilationController controller) throws IOException {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     TypeElement te = sourceUtils.getTypeElement();
                     AnnotationMirror annotation = getAnnotation(te, JAVAX_JWS_WEBSERVICE);
                    if (annotation != null) {
                        String wsdlLoc = null;
                        Map<? extends ExecutableElement, ? extends AnnotationValue> 
                            expressions = annotation.getElementValues();
                        for (Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> ex : expressions.entrySet())
                        {
                            ExecutableElement el = ex.getKey();
                            String val = (String) ex.getValue().getValue();
                            if (el.getSimpleName()
                                    .contentEquals("wsdlLocation"))             // NOI18N
                            { 
                                wsdlLoc = val;
                                if (wsdlLoc != null)
                                    wsdlLoc = URLEncoder.encode(wsdlLoc, UTF8); 
                            }
                            if (wsdlLoc != null)
                                break;
                        }
                        wsdlLocARR[1] = wsdlLoc;
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return wsdlLocARR[0];
    }
    
    /**
     * gets the <code>wsdl:portName</code> for a given implementation class
     * @param implClass the implementation class
     * @param targetNamespace Namespace URI for service name
     * @return the <code>wsdl:portName</code> for the <code>implClass</code>
     */
    public static QName getPortName(FileObject implClass, final String targetNamespace) {

        final java.lang.String[] portNameQNameARR = new String[2];
        if (implClass == null) {
            return null;   
        }
        try {    
            JavaSource js = JavaSource.forFileObject(implClass);
            if ( js== null){
                return null;
            }
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                @Override
                 public void run(CompilationController controller) throws java.io.IOException {
                     controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                     SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                     TypeElement te = sourceUtils.getTypeElement();
                     String className = te.getSimpleName().toString();
                     AnnotationMirror annotation = getAnnotation(te, JAVAX_JWS_WEBSERVICE);
                    if (annotation != null) {
                        String portNameAnnot = null;
                        String nameAnnot = null;
                        String targetNamespaceAnnot = null;

                        Map<? extends ExecutableElement, 
                                ? extends AnnotationValue> expressions =  annotation
                                        .getElementValues();
                        for (Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> ex : expressions.entrySet())
                        {
                            ExecutableElement el = ex.getKey();
                            String val = (String) ex.getValue().getValue();
                            if (el.getSimpleName().contentEquals("name")) { // NOI18N
                                nameAnnot = val;
                                if (nameAnnot != null)
                                    nameAnnot = URLEncoder.encode(nameAnnot,
                                            UTF8); 
                            }
                            else if (el.getSimpleName().contentEquals(
                                    "portName"))            // NOI18N
                            { 
                                portNameAnnot = val;
                                if (portNameAnnot != null)
                                    portNameAnnot = URLEncoder.encode(
                                            portNameAnnot, UTF8); // NOI18N
                            }
                            else if (el.getSimpleName().contentEquals(
                                    "targetNamespace"))         // NOI18N
                            { 
                                targetNamespaceAnnot = val;
                                if (targetNamespaceAnnot != null)
                                    targetNamespaceAnnot = URLEncoder.encode(
                                            targetNamespaceAnnot, UTF8);
                            }
                            if (targetNamespaceAnnot != null
                                    && nameAnnot != null
                                    && portNameAnnot != null)
                            {
                                break;
                            }
                        }

                        if ((portNameAnnot != null)
                                && (portNameAnnot.length() > 0))
                        {
                            portNameQNameARR[0] = portNameAnnot;
                        }
                        else if ((nameAnnot != null)
                                && (nameAnnot.length() > 0))
                        {
                            portNameQNameARR[0] = nameAnnot + PORT;
                        }
                        else {
                            portNameQNameARR[0] = className + PORT;
                        }

                        if (targetNamespace == null) {
                            if ((targetNamespaceAnnot != null)
                                    && (targetNamespaceAnnot.length() > 0))
                            {
                                portNameQNameARR[1] = targetNamespaceAnnot;
                            }
                            else {
                                String packageName = getPackageFromClass(className);
                                portNameQNameARR[1] = getNamespace(packageName);
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new QName(portNameQNameARR[1], portNameQNameARR[0]);
    }
    
    public static String getPackageFromClass(String fqClassName) {
        return fqClassName.substring(0, fqClassName.lastIndexOf('.'));
    }   
    
    private static AnnotationMirror getAnnotation(Element element, String fqn ){
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotations) {
            Element annotation = annotationMirror.getAnnotationType().asElement();
            if ( annotation instanceof TypeElement ){
                Name name = ((TypeElement)annotation).getQualifiedName();
                if ( fqn.contentEquals( name )){
                    return annotationMirror;
                }
            }
        }
        return null;
    }

}
