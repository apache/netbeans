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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Provide action for using an e-mail
 * 
 * @author Petr Blaha
 */
public class SendEmailCodeGenerator implements CodeGenerator {

    private FileObject srcFile;
    private TypeElement beanClass;

    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? getPathElementOfKind(Tree.Kind.CLASS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element elem = controller.getTrees().getElement(path);
                if (elem != null) {
                    SendEmailCodeGenerator gen = createSendEmailGenerator(component, controller, elem);
                    if (gen != null)
                        ret.add(gen);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return ret;
        }

    }

    static SendEmailCodeGenerator createSendEmailGenerator(JTextComponent component, CompilationController cc, Element el) throws IOException {
        if (el.getKind() != ElementKind.CLASS)
            return null;
        TypeElement typeElement = (TypeElement)el;
        if (!isEnable(cc.getFileObject(), typeElement)) {
            return null;
        }
        return new SendEmailCodeGenerator(cc.getFileObject(), typeElement);
    }

    public SendEmailCodeGenerator(FileObject srcFile, TypeElement beanClass) {
        this.srcFile = srcFile;
        this.beanClass = beanClass;
    }

    public void invoke() {
        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
        
        //make sure configuration is ready
        J2eeModuleProvider pwm = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
        final SendEmailPanel sendEmailPanel = new SendEmailPanel(erc.getServiceLocatorName(), ClasspathInfo.create(srcFile)); //NOI18N
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                sendEmailPanel,
                NbBundle.getMessage(SendEmailCodeGenerator.class, "LBL_SpecifyMailResource"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SendEmailPanel.class),
                null
                );
        final NotificationLineSupport notificationSupport = dialogDescriptor.createNotificationLineSupport();
        
        sendEmailPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(SendEmailPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        boolean isValid = ((Boolean) newvalue).booleanValue();
                        dialogDescriptor.setValid(isValid);
                        if (isValid) {
                            notificationSupport.clearMessages();
                        } else {
                            notificationSupport.setErrorMessage(sendEmailPanel.getErrorMessage());
                        }
                    }
                }
            }
        });
        
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (option == NotifyDescriptor.OK_OPTION) {
            try {
                
                String serviceLocator = sendEmailPanel.getServiceLocator();
                ServiceLocatorStrategy serviceLocatorStrategy = null;
                if (serviceLocator != null) {
                    serviceLocatorStrategy = ServiceLocatorStrategy.create(enterpriseProject, srcFile, serviceLocator);
                }
                
                String jndiName = null;
                if (!ProjectUtil.isJavaEE5orHigher(enterpriseProject) || !InjectionTargetQuery.isInjectionTarget(srcFile, beanClass.getQualifiedName().toString())) {
                    jndiName = generateJNDILookup(sendEmailPanel.getJndiName(), erc, srcFile, beanClass.getQualifiedName().toString());
                }
                
                generateMethods(
                        enterpriseProject, 
                        srcFile, 
                        beanClass.getQualifiedName().toString(),
                        jndiName, 
                        sendEmailPanel.getJndiName(), 
                        serviceLocatorStrategy
                        );
                if (serviceLocator != null) {
                    erc.setServiceLocatorName(serviceLocator);
                }
            } catch (IOException ioe) {
                NotifyDescriptor ndd = new NotifyDescriptor.Message(ioe.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndd);
            }
        }
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(SendEmailCodeGenerator.class, "LBL_SendEmailAction");
    }
    
    private String generateJNDILookup(String jndiName, EnterpriseReferenceContainer erc, FileObject fileObject, String className) throws IOException {
        ResourceReference resourceReference = ResourceReference.create(
                jndiName,
                "javax.mail.Session", // NOI18N
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                null
                );
        return erc.addResourceRef(resourceReference, fileObject, className);
    }
    
    private void generateMethods(Project project, FileObject fileObject, String className, 
            String jndiName, String simpleName, ServiceLocatorStrategy slStrategy) throws IOException{
        String memberName = _RetoucheUtil.uniqueMemberName(fileObject, className, simpleName, "mailResource"); //NOI18N
        if (jndiName == null) {
            generateInjectedField(fileObject, className, simpleName, memberName);
            generateSendMailMethod(fileObject, className, memberName, null);
        } else {
            String sessionGetter = generateLookupMethod(fileObject, className, jndiName, simpleName, slStrategy);
            generateSendMailMethod(fileObject, className, memberName, sessionGetter);
        }
    }
    
    private void generateSendMailMethod(FileObject fileObject, final String className, String sessionVariableName, String sessionGetter) throws IOException{
        
        List<MethodModel.Variable> parameters = Arrays.asList(new MethodModel.Variable[] {
            MethodModel.Variable.create("java.lang.String", "email"),
            MethodModel.Variable.create("java.lang.String", "subject"),
            MethodModel.Variable.create("java.lang.String", "body")
        });
        
        List<String> exceptions = Arrays.asList(new String[] {
            javax.naming.NamingException.class.getName(),
            "javax.mail.MessagingException"
        });
        
        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(fileObject, className, "sendMail", "mailResource"),
                "void",
                getSendCode(sessionVariableName, sessionGetter),
                parameters,
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    private String getSendCode(String sessionVariableName, String sessionGetter){
        return (sessionGetter != null ? "javax.mail.Session " + sessionVariableName + " = " + sessionGetter + "();\n" : "") +
                "javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(" + sessionVariableName + ");\n" +
                "message.setSubject(subject);\n" +
                "message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email, false));\n" +
                "message.setText(body);\n" +
                "javax.mail.Transport.send(message);\n";
    }
    
    private String generateLookupMethod(FileObject fileObject, final String className, String jndiName, String simpleName, 
            ServiceLocatorStrategy slStrategy) throws IOException {
        String sessionGetter = "get" + simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        sessionGetter = _RetoucheUtil.uniqueMemberName(fileObject, className, sessionGetter, "mailResource");
        String body = null;
        if (slStrategy == null) {
            body = getSessionCode(jndiName);
        } else {
            body = getSessionCode(jndiName, slStrategy, fileObject, className);
        }
        final MethodModel methodModel = MethodModel.create(
                sessionGetter,
                "javax.mail.Session",
                body,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList(javax.naming.NamingException.class.getName()),
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        return sessionGetter;
    }
    
    private String getSessionCode(String jndiName, ServiceLocatorStrategy slStrategy, FileObject fileObject, String className) {
        String mailLookupString = slStrategy.genMailSession(jndiName, fileObject, className);
        return "return (javax.mail.Session) " + mailLookupString + ";\n"; // NOI18N
    }
    
    private String getSessionCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.mail.Session) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] {jndiName});
    }
    
    private void generateInjectedField(FileObject fileObject, String className, String jndiName, String simpleName) throws IOException {
        _RetoucheUtil.generateAnnotatedField(
                fileObject,
                className,
                "javax.annotation.Resource",
                simpleName,
                "javax.mail.Session",
                Collections.singletonMap("name", jndiName),
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
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
            Logger.getLogger(SendEmailCodeGenerator.class.getName()).log(Level.FINE, null, ex);
        }
        if (platform == null) {
            return true;
        }
        if (!platform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
            return false;
        }
        return ElementKind.INTERFACE != typeElement.getKind();
    }
    
    public static TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }

    public static TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind()))
                return path;
            path = path.getParentPath();
        }
        return null;
    }

}
