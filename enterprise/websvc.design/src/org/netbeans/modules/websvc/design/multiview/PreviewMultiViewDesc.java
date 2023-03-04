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
package org.netbeans.modules.websvc.design.multiview;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import org.openide.ErrorManager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Pospisil
 */
public class PreviewMultiViewDesc extends Object implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PREFERRED_ID = "webservice-wsdlpreview";
    private DataObject dataObject;
    private FileObject fo;
    private String serviceName;
    private String implementationClass;

    public PreviewMultiViewDesc() {
    }

    /**
     *
     *
     * @param mvSupport 
     */

    // Web Service from WSDL case constructor - dataobject is WSDL code
    public PreviewMultiViewDesc(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    //************************************************************************************
    // Dead code???
    public PreviewMultiViewDesc(DataObject dataObject, Service service, FileObject fo) {
        this.dataObject = dataObject;
        this.fo = fo;
        if (service != null) {
            this.serviceName = service.getName();
            this.implementationClass = service.getImplementationClass();
        }
    }
    //************************************************************************************

    // Web Service from Java case constructor - dataobject is Java code
    public PreviewMultiViewDesc(DataObject dataObject, Service service) {
        this.dataObject = dataObject;
        if (service != null) {
            this.serviceName = service.getName();
            this.implementationClass = service.getImplementationClass();
        }
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(PreviewMultiViewDesc.class,
                "LBL_wsdlPreview_name");
    }

    public Image getIcon() {
        if (dataObject != null) {
            return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        } else {
            Image fault = ImageUtilities.loadImage("org/netbeans/modules/websvc/design/view/resources/fault.png", false);
            //Image fault = ImageUtilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/error-badge.gif", false);
            return fault;
        }

    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PreviewMultiViewDesc.class);
    }

    public MultiViewElement createElement() {

        if (implementationClass == null) {  //there was no Service that was passed
            if (dataObject == null) {
                //return MultiViewFactory.BLANK_ELEMENT; // Error happened - mo WSDL code object available
                return new PreviewMultiViewElement();
            } else {
                return new PreviewMultiViewElement(dataObject.getLookup().lookup(DataEditorSupport.class));
            }
        } else {
            DataObject wsdl = createPreviewForJava();
//            dataObject.getPrimaryFile().addFileChangeListener(new FileChangeListener() {
//
//                public void fileFolderCreated(FileEvent fe) {
//                }
//
//                public void fileDataCreated(FileEvent fe) {
//                }
//
//                public void fileChanged(FileEvent fe) {
//                    try {
//                        createPreviewForJava(service);
//                    } catch (IllegalArgumentException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//
//                public void fileDeleted(FileEvent fe) {
//                }
//
//                public void fileRenamed(FileRenameEvent fe) {
//                }
//
//                public void fileAttributeChanged(FileAttributeEvent fe) {
//                }
//            });

            /* If some error occurs during wsdl generation (like no method), wsdl object is null,
             * so blank element with error code is displayed.Otherwise  system looks up for wsdl's
             * dataeditor support and passes it to element */

            if ((wsdl == null) || (wsdl.getLookup().lookup(DataEditorSupport.class) == null)) {
                return new PreviewMultiViewElement();
            } else {
                DataEditorSupport des = wsdl.getLookup().lookup(DataEditorSupport.class);
                return new PreviewMultiViewElement(des);

            }
        }
    }

    public DataObject createPreviewForJava() {
        // DataObject created from FileObject of WSDL file - null if WSDL don't exist
        DataObject dataObj = null;
        if (dataObject != null) {
            // Source WSDL file in case of ws from WSDL
            FileObject primaryFile = dataObject.getPrimaryFile();
            // Tempdir path - for generating of WSDL
            String tempdir = System.getProperty("java.io.tmpdir");
            // Web service name
            // FileObject of WSDL file
            FileObject wsdlFile = null;

            final java.util.Properties prop = new java.util.Properties();
            prop.setProperty("build.generated.dir", tempdir);
            Project project = FileOwnerQuery.getOwner(primaryFile);
            //System.out.println("Project = " + project);
            // Test if source java file of web service contains any operation
            // If there is none, no WSDL is generated and resulting DataObject remains null
            JavaSource targetSource = JavaSource.forFileObject(getFileObject(project)); //
            //JavaSource targetSource = JavaSource.forFileObject(primaryFile);
            //JavaSource targetSource = JavaSource.forFileObject(getEditorSupport().getDataObject().getPrimaryFile());
            //System.out.println("javaSource = " + targetSource);
            FindMethodTask fmt = new FindMethodTask();
            try {
                targetSource.runUserActionTask(fmt, true);
                if (fmt.found) {
                    final FileObject jaxwsImplFo = project.getProjectDirectory().getFileObject("build.xml");

                    // For generation of WSDL code, use wsgen target from jaxws-build.xml

                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            try {
                                ExecutorTask wsimportTask =
                                        ActionUtils.runTarget(jaxwsImplFo,
                                        new String[]{"wsgen-" + serviceName}, prop); //NOI18N

                                wsimportTask.waitFinished();
                            } catch (IllegalArgumentException ex) {
                                ErrorManager.getDefault().notify(ex);
                            } catch (java.io.IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    });

                    // Refresh of filesystem after WSDL file generation
                    File temp = new File(tempdir);
                    FileUtil.refreshFor(temp);
                    // Constant part of path,where WSDL generates
                    String constPart = "wsgen/service/resources/"; //NOI18N
                    String suffix = "Service";
                    //Check of module type to found,if Web app part of wsdl filename is needed to add
//                J2eeModuleProvider t = project.getLookup().lookup(J2eeModuleProvider.class);
//                if (t != null) {
//                    if (J2eeModule.WAR.equals(t.getJ2eeModule().getModuleType())) {
//                        //For Web applications, add Service to service name, when generating WSDL

//                    }
//                }
                    // Complete full path to WSDL file
                    String tempTestDestpath = tempdir + constPart + serviceName + suffix + ".wsdl";
                    // File object for generated WSDL file
                    File wsdl = new File(tempTestDestpath);
                    wsdlFile = FileUtil.toFileObject(FileUtil.normalizeFile(wsdl));
                }
                if (wsdlFile != null) {
                    try {
                        dataObj = DataObject.find(wsdlFile);
                        //EditorCookie edck = dataObj.getLookup().lookup(EditorCookie.class);

                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return dataObj;
    }

    public FileObject getFileObject(Project prj) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String implClassResource = implementationClass.replace('.', '/') + ".java"; //NOI18N
        for (SourceGroup srcGroup : srcGroups) {
            FileObject implClassFo = srcGroup.getRootFolder().getFileObject(implClassResource);
            if (implClassFo != null) {
                return implClassFo;
            }
        }
        return null;
    }

    /**
     * Task for ensuring,that web service from Java has at least one method to prevent wsgen fail
     */
    class FindMethodTask implements CancellableTask<CompilationController> {

        public boolean found = false;

        public void cancel() {
        }

        public void run(CompilationController controller) throws Exception {
            TypeElement typeElement = controller.getElements().getTypeElement(implementationClass);
//            String elm = "";
//            String elmknd = "";
            if (typeElement != null) {
                for (Element element : typeElement.getEnclosedElements()) {
//                    elm = element.toString();
//                    elmknd = element.getKind().toString();
                    if (element.getKind() == ElementKind.METHOD) {
//                    List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
//                    System.out.println("Pocet anotaci:" + annotations.size());
//                    System.out.println("Anotace je:" + annotations.get(0).toString());
//                    for (int i = 0; i < annotations.size(); i++) {
//                        DeclaredType t = annotations.get(i).getAnnotationType();
//                        TypeElement te = (TypeElement) t.asElement();
//                        System.out.println("annot: " + te.getQualifiedName());
//                        System.out.println("annot: " + te.getSimpleName());
//                        if (te.getSimpleName().contentEquals("Webmethod")) {
                        found = true;
//                        }
//                    }
                    }
                }
            }
        }
    }
}
