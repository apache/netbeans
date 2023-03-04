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
package org.netbeans.modules.websvc.core.jaxws.saas;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.ui.ProgressDialog;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author rico
 */
public class RestResourceGenerator {

    public static final String RESOURCE_TEMPLATE = "Templates/WebServices/GenericResource.java"; //NOI18N
    private FileObject folder;
    private URI wsdlURL;
    private WsdlModel wsdlModel;
    private ProgressHandle pHandle;
    private int totalWorkUnits;
    private int workUnits;
    private Task generatorTask;
    private String packageName;
    private ProgressDialog dialog;

    public RestResourceGenerator(FileObject folder, URI wsdlURL, String packageName) {
        this.folder = folder;
        this.wsdlURL = wsdlURL;
        this.packageName = packageName;
    //
    }

    public void generate() {
        String mes = NbBundle.getMessage(RestResourceGenerator.class, "MSG_GENERATING_REST_RESOURCE");
        dialog = new ProgressDialog(mes);
        generatorTask = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                try {
                    initProgressReporting(dialog.getProgressHandle());

                    final Project project = FileOwnerQuery.getOwner(folder);
                    //try {
                    String clientPackageName = getPackageName(packageName + "_client"); //TODO Uniquify this
                    JaxWsModel jaxwsModel = project.getLookup().lookup(JaxWsModel.class);
                    String clientName = getWsdlName(wsdlURL.toString());
                    Client c = clientExists(jaxwsModel, clientName);
                    if (c == null) {
                        String mes = NbBundle.getMessage(RestResourceGenerator.class, "MSG_GENERATING_CLIENT_ARTIFACTS");
                        reportProgress(mes);
                        clientName = generateClient(project, wsdlURL.toString(), clientPackageName);
                    } else {
                        clientPackageName = c.getPackageName();
                    }

                    Client client = jaxwsModel.findClientByName(clientName);
                    if (client == null) {
                        finishProgressReporting();
                        dialog.close();
                        return;
                    }
                    JAXWSClientSupport clientSupport = JAXWSClientSupport.getJaxWsClientSupport(folder);
                    FileObject localWsdlFolder = clientSupport.getLocalWsdlFolderForClient(clientName, false);

                    FileObject localWsdl = localWsdlFolder.getFileObject(client.getLocalWsdlFile());
                    WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(localWsdl.toURL());
                    wsdlModeler.setPackageName(clientPackageName);
                    wsdlModeler.setCatalog(clientSupport.getCatalog());
                    WsdlModel model = wsdlModeler.getAndWaitForWsdlModel();
                    if (model == null) {
                        finishProgressReporting();
                        dialog.close();
                        return;
                    }
                    JavaSource targetSource = null;

                    final RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
                    try {
                        restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.IDE);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify();
                    }
                    List<WsdlService> services = model.getServices();
                    for (WsdlService service : services) {
                        List<WsdlPort> ports = service.getPorts();
                        for (final WsdlPort port : ports) {
                            final FileObject fo = folder.getFileObject(port.getName(), "java");
                            if (fo != null) {
                                final NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(RestResourceGenerator.class, "MSG_CONFIRM_DELETE", port.getName()), NbBundle.getMessage(RestResourceGenerator.class, "TITLE_CONFIRM_DELETE"), NotifyDescriptor.YES_NO_OPTION);
                                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                                    FileLock lock = null;
                                    try {
                                        lock = fo.lock();
                                        fo.delete(lock);
                                    } catch (IOException ex) {
                                        ErrorManager.getDefault().notify(ex);
                                    } finally {
                                        if (lock != null) {
                                            lock.releaseLock();
                                        }
                                    }
                                } else {
                                    continue;
                                }
                            }
                            String mes = NbBundle.getMessage(RestResourceGenerator.class, "MSG_GENERATING_RESOURCE_FILE");
                            reportProgress(mes);
                            targetSource = JavaSourceHelper.createJavaSource(RESOURCE_TEMPLATE, folder, packageName, port.getName());
                            List<WsdlOperation> operations = port.getOperations();
                            for (WsdlOperation operation : operations) {
                                try {
                                    new RestWrapperForSoapGenerator(service, port, operation, project, targetSource.getFileObjects().iterator().next(), wsdlURL.toString()).generate();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ex);
                                    try {
                                        restSupport.getRestServicesModel().
                                            runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() 
                                            {

                                            public Void run(RestServicesMetadata metadata) throws IOException {
                                                RestServices root = metadata.getRoot();

                                                if (root.sizeRestServiceDescription() < 1) {
                                                    //restSupport.removeRestDevelopmentReadiness();
                                                }

                                                return null;
                                            }
                                        });
                                    } catch (IOException e) {
                                        Exceptions.printStackTrace(e);
                                    }
                                }
                            }
                            try {
                                initializeClient(service, port, wsdlURL.toString(), targetSource);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            try {
                                FileObject targetFile = targetSource.getFileObjects().iterator().next();
                                openFileInEditor(DataObject.find(targetFile)); //display in the editor
                            } catch (DataObjectNotFoundException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    }
              
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    finishProgressReporting();
                    dialog.close();
                }
          
            }
        });
        generatorTask.schedule(50);
        dialog.open();
    }

    private String getPackageName(String name) {
        List<String> names = new ArrayList<String>();
        String pName = name;
        int suffix = 0;
        Project project = FileOwnerQuery.getOwner(this.folder);
        JaxWsModel model = project.getLookup().lookup(JaxWsModel.class);
        Client[] clients = model.getClients();
        for (Client client : clients) {
            names.add(client.getPackageName());
        }
        for (String n : names) {
            if (pName.equals(n)) {
                pName = name + String.valueOf(++suffix);
            }
        }
        return pName;
    }

    private void initializeClient(final WsdlService service, 
            final WsdlPort port, final String wsdlUrl, JavaSource targetSource)
            throws IOException 
    {
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                TreeMaker make = workingCopy.getTreeMaker();
                MethodTree constructor = JavaSourceHelper.
                    getDefaultConstructor(workingCopy);
                String body = "\n{\nport = getPort();\n}\n";        // NOI18N
                ModifiersTree publicTree = GenerationUtils.
                    newInstance(workingCopy).createModifiers(Modifier.PUBLIC);
                List<TypeParameterTree> params = Collections.emptyList();
                List<VariableTree> vars = Collections.emptyList();
                List<ExpressionTree> thrws = Collections.emptyList();
                MethodTree modifiedConstructor = make.Constructor(publicTree, 
                        params, vars, thrws, body);
                workingCopy.rewrite(constructor, modifiedConstructor);
                ClassTree modifiedClass = JavaSourceHelper.
                    addField(workingCopy, javaClass, new Modifier[]{
                            Modifier.PRIVATE}, null, null, "port",  // NOI18N
                                port.getJavaName(), null);
                workingCopy.rewrite(javaClass, modifiedClass);

                ClassTree modifiedJavaClass = JavaSourceHelper.
                    addMethod(workingCopy, modifiedClass,
                        new Modifier[]{Modifier.PRIVATE}, null, null,
                        "getPort", port.getJavaName(), null, null,  // NOI18N
                        null, null,
                        generateGetPort(service, port), "");      //NOI18N
                workingCopy.rewrite(javaClass, modifiedJavaClass);
            }

            @Override
            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
    }

    private String generateGetPort(WsdlService service, WsdlPort port) {
        String getPort = "";
        String serviceVar = "service";  //NOI18N
        String serviceJavaName = service.getJavaName();
        String portJavaName = port.getJavaName();
        String portGetterMethod = port.getPortGetter();
        Object[] args = new String[]{serviceJavaName, portJavaName, portGetterMethod, "", "", serviceVar};
        String body = RestWrapperForSoapGenerator.JAVA_TRY +
                RestWrapperForSoapGenerator.JAVA_SERVICE_DEF +
                RestWrapperForSoapGenerator.JAVA_PORT_DEF +
                "\nreturn p;\n" +
                RestWrapperForSoapGenerator.JAVA_CATCH;
        body += "\nreturn null;\n";
        getPort = "\n{\n" + MessageFormat.format(body, args) + "\n}\n";
        return getPort;
    }

    private Client clientExists(JaxWsModel jaxwsModel, String clientName) {
        Client[] clients = jaxwsModel.getClients();
        for (int i = 0; i <
                clients.length; i++) {
            if (clients[i].getName().equals(clientName)) {
                return clients[i];
            }

        }
        return null;
    }

    private String generateClient(Project project, String wsdlUrl, String packageName) throws IOException {
        JAXWSClientSupport jaxWsClientSupport = null;
        if (project != null) {
            jaxWsClientSupport = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        }

        if (jaxWsClientSupport == null) {
            String mes = NbBundle.getMessage(RestResourceGenerator.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return null;
        }
        if (packageName != null && packageName.length() == 0) {
            packageName = null;
        }

        return jaxWsClientSupport.addServiceClient(getWsdlName(wsdlUrl), wsdlUrl, packageName, true);

    }

    private String getWsdlName(String wsdlUrl) {
        int ind = wsdlUrl.lastIndexOf("/"); //NOI18N
        String wsdlName = ind >= 0 ? wsdlUrl.substring(ind + 1) : wsdlUrl;
        if (wsdlName.toUpperCase().endsWith("?WSDL")) {
            wsdlName = wsdlName.substring(0, wsdlName.length() - 5);
        } //NOI18N

        ind = wsdlName.lastIndexOf("."); //NOI18N
        if (ind > 0) {
            wsdlName = wsdlName.substring(0, ind);
        }
// replace special characters with '_'
        return convertAllSpecialChars(wsdlName);
    }

    private String convertAllSpecialChars(String resultStr) {
        StringBuffer sb = new StringBuffer(resultStr);
        for (int i = 0; i <
                sb.length(); i++) {
            char c = sb.charAt(i);
            if (Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')) {
                continue;
            } else {
                sb.setCharAt(i, '_');
            }

        }
        return sb.toString();
    }

    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);

        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    openCookie.open();
                }
            }, 1000);
        } else {
            final EditorCookie ec = dobj.getCookie(EditorCookie.class);
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ec.open();
                }
            }, 1000);
        }
    }

    public void initProgressReporting(ProgressHandle pHandle) {
        initProgressReporting(pHandle, true);
    }

    public void initProgressReporting(ProgressHandle pHandle, boolean start) {
        this.pHandle = pHandle;
        this.totalWorkUnits = getTotalWorkUnits();
        this.workUnits = 0;

        if (pHandle != null && start) {
            if (totalWorkUnits > 0) {
                pHandle.start(totalWorkUnits);
            } else {
                pHandle.start();
            }

        }
    }

    public void reportProgress(String message) {
        if (pHandle != null) {
            if (totalWorkUnits > 0) {
                pHandle.progress(message, ++workUnits);
            } else {
                pHandle.progress(message);
            }

        }
    }

    public void finishProgressReporting() {
        if (pHandle != null) {
            pHandle.finish();
        }

    }

    public int getTotalWorkUnits() {
        return 0;
    }

    protected ProgressHandle getProgressHandle() {
        return pHandle;
    }
}
