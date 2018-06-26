/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
