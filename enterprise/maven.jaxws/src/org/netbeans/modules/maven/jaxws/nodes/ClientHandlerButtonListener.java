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
package org.netbeans.modules.maven.jaxws.nodes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;

/**
 *
 * @author Roderico Cruz
 */
public class ClientHandlerButtonListener implements ActionListener {

    private MessageHandlerPanel panel;
    private BindingsModel bindingsModel;
    private JaxWsService client;
    private Node node;
//    private JaxWsModel jaxWsModel;
    //private FileObject bindingHandlerFO;
    private String bindingsHandlerFile;

    public ClientHandlerButtonListener(MessageHandlerPanel panel,
            BindingsModel bindingsModel, JaxWsService client, Node node) {

        this.panel = panel;
        this.bindingsModel = bindingsModel;
        this.client = client;
        this.node = node;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == NotifyDescriptor.OK_OPTION) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    configureHandler();
                }
            });
        }
    }

    private void configureHandler() {
        if (!panel.isChanged()) {
            return;
        }
        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ClientHandlerButtonListener.class, "MSG_ConfigureHandler")); //NOI18N
        handle.start();
        handle.switchToIndeterminate();
        JAXWSLightSupport support = node.getLookup().lookup(JAXWSLightSupport.class);
        FileObject bindingsFolder = support.getBindingsFolder(true);
        assert client != null;

        try {
            bindingsHandlerFile = client.getHandlerBindingFile();
            if (bindingsHandlerFile == null) {
                String baseBindingsHandlerFile = node.getName() + "_handler";
                bindingsHandlerFile = FileUtil.findFreeFileName(bindingsFolder, baseBindingsHandlerFile, "xml") +
                        ".xml";
                client.setHandlerBindingFile(bindingsHandlerFile);
            }
            final FileObject bindingHandlerFO = FileUtil.createData(bindingsFolder, bindingsHandlerFile);
            //if bindingsModel is null, create it
            if (bindingsModel == null) {
                InputStream is = FileUtil.getConfigFile("jax-ws/default-binding-handler.xml").getInputStream();
                final String bindingsContent = readResource(is); //NOI18N
                is.close();

                bindingsFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                    @Override
                    public void run() throws IOException {
                        BufferedWriter bw = null;
                        OutputStream os = null;
                        OutputStreamWriter osw = null;
                        FileLock lock = bindingHandlerFO.lock();
                        try {
                            os = bindingHandlerFO.getOutputStream(lock);
                            osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                            bw = new BufferedWriter(osw);
                            bw.write(bindingsContent);
                        } finally {
                            try {
                                if (bw != null) {
                                    bw.close();
                                }
                                if (os != null) {
                                    os.close();
                                }
                                if (osw != null) {
                                    osw.close();
                                }
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(e);
                            }

                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }
                });

                //now load the model and add the entry
                ModelSource ms = Utilities.getModelSource(bindingHandlerFO, true);
                bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
                //get the relative path of the wsdl
                FileObject localWsdlFile =
                        support.getWsdlFolder(false).getFileObject(client.getLocalWsdl());
                File f = FileUtil.toFile(bindingHandlerFO);
                String relativePath = Utilities.relativize(f.toURI(), new URI(localWsdlFile.toURL().toExternalForm()));
                GlobalBindings gb = bindingsModel.getGlobalBindings();
                try {
                    bindingsModel.startTransaction();
                    gb.setWsdlLocation(relativePath);
                } finally {
                    try {
                        bindingsModel.endTransaction();  //becomes locked here
                    } catch (IllegalStateException ex) {
                        ErrorManager.getDefault().notify(ex);
                        return;
                    }
                }

                DataObject dobj = DataObject.find(bindingHandlerFO);
                if (dobj.isModified()) {
                    SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                    saveCookie.save();
                }

            }//end if bindingsModel == null

            //get handler chain
            TableModel tableModel = panel.getHandlerTableModel();
            GlobalBindings gb = bindingsModel.getGlobalBindings();
            DefinitionsBindings db = gb.getDefinitionsBindings();
            BindingsHandlerChains bhc = db.getHandlerChains();
            BindingsHandlerChain chain = bhc.getHandlerChains().iterator().next();

            //refresh handlers
            try {
                bindingsModel.startTransaction();
                Collection<BindingsHandler> handlers = chain.getHandlers();
                for (BindingsHandler handler : handlers) {
                    chain.removeHandler(handler);
                }

                if (tableModel.getRowCount() > 0) {
                    BindingsComponentFactory factory = bindingsModel.getFactory();
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String className = (String) tableModel.getValueAt(i, 0);
                        BindingsHandler handler = factory.createHandler();

                        String handlerName = className.substring(className.indexOf(".") + 1);
                        BindingsHandlerName name = factory.createHandlerName();
                        name.setHandlerName(handlerName);
                        handler.setHandlerName(name);

                        BindingsHandlerClass handlerClass = factory.createHandlerClass();
                        handlerClass.setClassName(className);
                        handler.setHandlerClass(handlerClass);

                        chain.addHandler(handler);
                    }
                }
            } finally {
                try {
                    bindingsModel.endTransaction();
                } catch (IllegalStateException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }

            //save bindingshandler file
            DataObject dobj = DataObject.find(bindingHandlerFO);
            if (dobj.isModified()) {
                SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);
                saveCookie.save(); //becomes false here
            }

            // adding binding file to 
            Project project = FileOwnerQuery.getOwner(bindingHandlerFO);
            if (project != null) {
                JaxWsClientNode clientNode = node.getLookup().lookup(JaxWsClientNode.class);
                final FileObject wsdlFo = clientNode.getLocalWsdl();
                ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        MavenModelUtils.addBindingFile(model, wsdlFo.getName(), bindingsHandlerFile);
                    }
                };
                FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(
                        pom, Collections.singletonList(oper));

                // execute wsimport goal
                RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(project.getProjectDirectory()), project, "wsimport", //NOI18N
                        Collections.singletonList("compile")); //NOI18N
                RunUtils.executeMaven(cfg);
            }

        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        handle.finish();

    }

    private void removeHandlerAnnotation() {
        JaxWsClientNode clientNode = node.getLookup().lookup(JaxWsClientNode.class);
        WsdlModel wsdlModel = clientNode.getWsdlModel();
        WsdlService service = wsdlModel.getServices().get(0);
        String serviceName = service.getJavaName();
        Project project = FileOwnerQuery.getOwner(clientNode.getLocalWsdl());
        
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath cp = ClassPath.getClassPath(groups[0].getRootFolder(), ClassPath.SOURCE);
        final FileObject serviceFO = cp.findResource(serviceName.replaceAll("\\.", "/")  + ".java");  //NOI18N

        //if serviceFO is null, the Service interface has not been generated, so no need to remove any annotation
        if(serviceFO == null) return;

        final JavaSource javaSource = JavaSource.forFileObject(serviceFO);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);

                AnnotationTree handlerAnnotation = null;
                List<? extends AnnotationTree> annots = javaClass.getModifiers().getAnnotations();
                for (AnnotationTree an : annots) {
                    IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                    TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                    TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                    if (anElement != null && anElement.getQualifiedName().contentEquals("javax.jws.HandlerChain")) {  //NOI18N
                        handlerAnnotation = an;
                        break;
                    }
                }
                ModifiersTree modifiers = javaClass.getModifiers();
                ModifiersTree newModifiers = make.removeModifiersAnnotation(modifiers, handlerAnnotation);
                workingCopy.rewrite(modifiers, newModifiers);
                CompilationUnitTree compileUnitTree = workingCopy.getCompilationUnit();
                List<? extends ImportTree> imports = compileUnitTree.getImports();
                for (ImportTree imp : imports) {
                    Tree impTree = imp.getQualifiedIdentifier();
                    TreePath impTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), impTree);
                    TypeElement impElement = (TypeElement) workingCopy.getTrees().getElement(impTreePath);
                    if (impElement != null && impElement.getQualifiedName().contentEquals("javax.jws.HandlerChain")) {  //NOI18N
                        CompilationUnitTree newCompileUnitTree = make.removeCompUnitImport(compileUnitTree, imp);
                        workingCopy.rewrite(compileUnitTree, newCompileUnitTree);
                        break;
                    }
                }
            }

            @Override
            public void cancel() {
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    try {
                        javaSource.runModificationTask(modificationTask).commit();
                        saveFile(serviceFO);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
        } else {
            try {
                javaSource.runModificationTask(modificationTask).commit();
                saveFile(serviceFO);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    private static void saveFile(FileObject file) throws IOException {
        DataObject dataObject = DataObject.find(file);
        if (dataObject != null) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }


//TODO: close all streams properly
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        BufferedReader br = null;
        InputStreamReader isr = null;
        StringBuilder sb = new StringBuilder();
        try {
            String lineSep = System.getProperty("line.separator");//NOI18N
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(isr);

            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(lineSep);
                line = br.readLine();
            }
        } finally {
            if ( isr!= null ){
                isr.close();
            }
            if ( br!= null ){
                br.close();
            }
            if ( is!= null ){
                is.close();
            }
        }
        return sb.toString();
    }
}
