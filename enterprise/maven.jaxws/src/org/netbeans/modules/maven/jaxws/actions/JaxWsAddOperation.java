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

package org.netbeans.modules.maven.jaxws.actions;

import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.maven.jaxws._RetoucheUtil;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import java.io.IOException;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** JaxWsAddOperation.java.
 * Created on December 12, 2006, 4:36 PM
 *
 * @author mkuchtiak
 */
public class JaxWsAddOperation implements AddOperationCookie {
    private FileObject implClassFo;

    /** Creates a new instance of JaxWsAddOperation.
     */
    public JaxWsAddOperation(FileObject implClassFo) {
        this.implClassFo=implClassFo;
    }

    @Override
    public void addOperation() {
        final AddWsOperationHelper strategy = new AddWsOperationHelper(
                NbBundle.getMessage(JaxWsAddOperation.class, "TITLE_OperationAction"));  //NOI18N
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    String className = _RetoucheUtil.getMainClassName(implClassFo);
                    if (className != null) {
                        strategy.addMethod(implClassFo, className);
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
    }

    @Override
    public boolean isEnabledInEditor(Lookup nodeLookup) {
        CompilationController controller = nodeLookup.lookup(CompilationController.class);
        if (controller != null) {
            TypeElement classEl = SourceUtils.getPublicTopLevelElement(controller);
            if (classEl != null) {
                return isJaxWsImplementationClass(classEl, controller);
            }
        }
        return false;
    }

    private boolean isJaxWsImplementationClass(TypeElement classEl, CompilationController controller) {
        TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
        if (wsElement != null) {
            List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
            for (AnnotationMirror anMirror : annotations) {
                if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("wsdlLocation")) { //NOI18N
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
//
//    private Service getService(){
//        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(implClassFo);
//        if (jaxWsSupport!=null) {
//            List services = jaxWsSupport.getServices();
//            for (int i=0;i<services.size();i++) {
//                Service serv = (Service)services.get(i);
//                if (serv.getWsdlUrl()==null) {
//                    String implClass = serv.getImplementationClass();
//                    if (implClass.equals(getPackageName(implClassFo))) {
//                        return serv;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private boolean isFromWSDL() {
//        if(service != null){
//            return service.getWsdlUrl()!=null;
//        }
//        return false;
//    }
//
//    private boolean isProvider() {
//        if(service != null){
//            return service.isUseProvider();
//        }
//        return false;
//    }
//
//    private String getPackageName(FileObject fo) {
//        Project project = FileOwnerQuery.getOwner(fo);
//        Sources sources = project.getLookup().lookup(Sources.class);
//        if (sources!=null) {
//            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//            if (groups!=null) {
//                for (SourceGroup group: groups) {
//                    FileObject rootFolder = group.getRootFolder();
//                    if (FileUtil.isParentOf(rootFolder, fo)) {
//                        String relativePath = FileUtil.getRelativePath(rootFolder, fo).replace('/', '.');
//                        return (relativePath.endsWith(".java")? //NOI18N
//                            relativePath.substring(0,relativePath.length()-5):
//                            relativePath);
//                    }
//                }
//            }
//        }
//        return null;
//    }

}
