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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.action.SendJMSGenerator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageDestinationUiSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * Provide action for sending a JMS Message
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SendJMSMessageCodeGenerator implements CodeGenerator {
    
    private FileObject srcFile;
    private TypeElement beanClass;

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? SendEmailCodeGenerator.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element elem = controller.getTrees().getElement(path);
                if (elem != null) {
                    SendJMSMessageCodeGenerator gen = createSendJMSMessageAction(component, controller, elem);
                    if (gen != null)
                        ret.add(gen);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            return ret;
        }

    }

    static SendJMSMessageCodeGenerator createSendJMSMessageAction(JTextComponent component, CompilationController cc, Element el) throws IOException {
        if (el.getKind() != ElementKind.CLASS)
            return null;
        TypeElement typeElement = (TypeElement)el;
        if (!isEnable(cc.getFileObject(), typeElement)) {
            return null;
        }
        return new SendJMSMessageCodeGenerator(cc.getFileObject(), typeElement);
    }

    public SendJMSMessageCodeGenerator(FileObject srcFile, TypeElement beanClass) {
        this.srcFile = srcFile;
        this.beanClass = beanClass;
    }

    @Override
    public void invoke() {
       try {           
            final Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
            final EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
            J2eeModuleProvider provider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);

            MessageDestinationUiSupport.DestinationsHolder holder = 
                    SendJMSMessageUiSupport.getDestinations(enterpriseProject, provider);
            final SendJmsMessagePanel sendJmsMessagePanel = SendJmsMessagePanel.newInstance(
                    enterpriseProject,
                    provider,
                    holder.getModuleDestinations(),
                    holder.getServerDestinations(),
                    erc.getServiceLocatorName(),
                    ClasspathInfo.create(srcFile));
            final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    sendJmsMessagePanel,
                    NbBundle.getMessage(SendJMSMessageCodeGenerator.class,"LBL_SendJmsMessage"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(SendJMSMessageCodeGenerator.class),
                    null);
            final NotificationLineSupport notificationSupport = dialogDescriptor.createNotificationLineSupport();
            
            sendJmsMessagePanel.addPropertyChangeListener(SendJmsMessagePanel.IS_VALID, new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            Object newvalue = evt.getNewValue();
                            if ((newvalue != null) && (newvalue instanceof Boolean)) {
                                boolean isValid = ((Boolean) newvalue);
                                dialogDescriptor.setValid(isValid);
                                if (isValid) {
                                    if (sendJmsMessagePanel.getWarningMessage() == null) {
                                        notificationSupport.clearMessages();
                                    } else {
                                        notificationSupport.setWarningMessage(sendJmsMessagePanel.getWarningMessage());
                                    }
                                } else {
                                    notificationSupport.setErrorMessage(sendJmsMessagePanel.getErrorMessage());
                                }
                            }
                        }
                    });
            sendJmsMessagePanel.verifyAndFire();

            Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (option != DialogDescriptor.OK_OPTION) {
                return;
            }
            
            String serviceLocator = sendJmsMessagePanel.getServiceLocator();
            final ServiceLocatorStrategy serviceLocatorStrategy = serviceLocator != null ?
                ServiceLocatorStrategy.create(enterpriseProject, srcFile, serviceLocator) : 
                null;

            
            if (serviceLocator != null) {
                erc.setServiceLocatorName(serviceLocator);
            }
            
            MessageDestination messageDestination = sendJmsMessagePanel.getDestination();
            Project mdbHolderProject = sendJmsMessagePanel.getMdbHolderProject();
            final SendJMSGenerator generator = new SendJMSGenerator(messageDestination, mdbHolderProject != null ? mdbHolderProject : enterpriseProject);

            //do that not in AWT, may take some time
            //http://www.netbeans.org/issues/show_bug.cgi?id=164834
            //http://www.netbeans.org/nonav/issues/showattachment.cgi/82529/error.log
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        generator.genMethods(erc, beanClass.getQualifiedName().toString(), sendJmsMessagePanel.getConnectionFactory(), srcFile, serviceLocatorStrategy, enterpriseProject.getLookup().lookup(J2eeModuleProvider.class));
                    } catch (IOException ex) {
                        SendJMSMessageCodeGenerator.notifyExc(ex);
                    }
                }
            });
            
        } catch (IOException ioe) {
            notifyExc(ioe);
        } 
    }

    private static void notifyExc(Exception e) {
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(e.getMessage(),
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
    }

    private static boolean isEnable(FileObject fileObject, TypeElement typeElement) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return false;
        }
        if (project.getLookup().lookup(EnterpriseReferenceContainer.class) == null) {
            return false;
        }
        String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceId == null) {
            return true;
        }
        J2eePlatform platform = null;
        try {
            platform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(SendJMSMessageCodeGenerator.class.getName()).log(Level.FINE, null, ex);
        }
        if (platform == null) {
            return true;
        }
        if (!platform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
            return false;
        }
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        J2eeModule.Type moduleType = j2eeModuleProvider.getJ2eeModule().getType();
        if (ProjectUtil.isJavaEE5orHigher(project) ||
                (J2eeModule.Type.WAR.equals(moduleType) && WebApp.VERSION_2_4.equals(j2eeVersion)) ||
                (J2eeModule.Type.EJB.equals(moduleType) && EjbJar.VERSION_2_1.equals(j2eeVersion)))  {
            return ElementKind.INTERFACE != typeElement.getKind();
        }
        return false;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SendJMSMessageCodeGenerator.class, "LBL_SendJMSMessageAction");
    }
    
}
