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

package org.netbeans.modules.j2ee.ejbcore;

import java.util.ArrayList;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class Utils {

    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(':');
            }
        }
        return classpath.toString();
    }

    public static void notifyError(Exception exception) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(exception.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static boolean areInSameJ2EEApp(Project project1, Project project2) {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            Project project = openProjects[i];
            Object j2eeAppProvider = project.getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider != null) { // == it is j2ee app
                J2eeApplicationProvider j2eeApp = (J2eeApplicationProvider)j2eeAppProvider;
                J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                if ((j2eeModules != null) && (j2eeModules.length > 0)) { // == there are some modules in the j2ee app
                    J2eeModuleProvider affectedPrjProvider1 = project1.getLookup().lookup(J2eeModuleProvider.class);
                    J2eeModuleProvider affectedPrjProvider2 = project2.getLookup().lookup(J2eeModuleProvider.class);
                    if (affectedPrjProvider1 != null && affectedPrjProvider2 != null) {
                        List childModules = Arrays.asList(j2eeModules);
                        if (childModules.contains(affectedPrjProvider1) &&
                                childModules.contains(affectedPrjProvider2)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if j2eeModuleProvider is part of some open J2EE application
     */
    public static boolean isPartOfJ2eeApp(J2eeModuleProvider j2eeModuleProvider) {
        for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
            J2eeApplicationProvider j2eeAppProvider = openProject.getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider != null) {
                if (Arrays.asList(j2eeAppProvider.getChildModuleProviders()).contains(j2eeModuleProvider)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Project getNestingJ2eeApp(Project project){
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
            J2eeApplicationProvider j2eeAppProvider = openProject.getLookup().lookup(J2eeApplicationProvider.class);
            if (j2eeAppProvider != null) {
                if (Arrays.asList(j2eeAppProvider.getChildModuleProviders()).contains(j2eeModuleProvider)) {
                    return openProject;
                }
            }
        }
        return null;
    }

    // =========================================================================

    // utils for ejb code synchronization

    public static boolean canExposeInLocal(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement executableElement = methodHandle.resolve(workingCopy);
                Set<Modifier> modifiers = executableElement.getModifiers();
                boolean signatureOk = modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC);
                if (signatureOk) {
                    Element enclosingElement = executableElement.getEnclosingElement();
                    ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                    methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, executableElement);
                }
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            return ejbMethodController != null && ejbMethodController.hasLocal() && !ejbMethodController.hasMethodInInterface(methodModel[0], ejbMethodController.getMethodTypeFromImpl(methodModel[0]), true);
        }
        return false;
    }

    public static void exposeInLocal(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = methodHandle.resolve(workingCopy);
                Element enclosingElement = method.getEnclosingElement();
                ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, method);
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            ejbMethodController.createAndAddInterface(methodModel[0], true);
        }
    }

    public static boolean canExposeInRemote(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement executableElement = methodHandle.resolve(workingCopy);
                Set<Modifier> modifiers = executableElement.getModifiers();
                boolean signatureOk = modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC);
                if (signatureOk) {
                    Element enclosingElement = executableElement.getEnclosingElement();
                    ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                    methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, executableElement);
                }
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            return ejbMethodController != null && ejbMethodController.hasRemote() && !ejbMethodController.hasMethodInInterface(methodModel[0], ejbMethodController.getMethodTypeFromImpl(methodModel[0]), true);
        }
        return false;
    }

    public static void exposeInRemote(FileObject ejbClassFO, final ElementHandle<ExecutableElement> methodHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final String[] ejbClassName = new String[1];
        final MethodModel[] methodModel = new MethodModel[1];
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = methodHandle.resolve(workingCopy);
                Element enclosingElement = method.getEnclosingElement();
                ejbClassName[0] = ((TypeElement) enclosingElement).getQualifiedName().toString();
                methodModel[0] = MethodModelSupport.createMethodModel(workingCopy, method);
            }
        });
        if (methodModel[0] != null) {
            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(ejbClassFO, ejbClassName[0]);
            ejbMethodController.createAndAddInterface(methodModel[0], false);
        }
    }

    /** Returns list of all EJB projects that can be called from the caller project.
     *
     * @param enterpriseProject the caller enterprise project
     */
    public static Project [] getCallableEjbProjects(Project enterpriseProject) {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();

        // TODO: HACK - this must be solved by freeform's own implementation of EnterpriseReferenceContainer, see issue 57003
        // call ejb should not make this check, all should be handled in EnterpriseReferenceContainer
        boolean isCallerFreeform = enterpriseProject.getClass().getName().equals("org.netbeans.modules.ant.freeform.FreeformProject");

        boolean isCallerEE6WebProject = isEE6WebProject(enterpriseProject);

        List<Project> filteredResults = new ArrayList<Project>(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            boolean isEJBModule = false;
            J2eeModuleProvider j2eeModuleProvider = allProjects[i].getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null){
                    Type type = j2eeModuleProvider.getJ2eeModule().getType();
                    EjbJar[] ejbJars = EjbJar.getEjbJars(allProjects[i]);
                    Profile profile = ejbJars.length > 0 ? ejbJars[0].getJ2eeProfile() : null;

                    if (J2eeModule.Type.EJB.equals(type) || (J2eeModule.Type.WAR.equals(type) &&
                                (Profile.JAVA_EE_6_WEB.equals(profile) || Profile.JAVA_EE_6_FULL.equals(profile) ||
                                Profile.JAVA_EE_7_WEB.equals(profile) || Profile.JAVA_EE_7_FULL.equals(profile)))){
                        isEJBModule = true;
                    }
            }

            // If the caller project is NOT a freeform project, include all EJB modules
            // If the caller project is a freeform project, include caller itself only
            // If the caller project is a Java EE 6 web project, include itself in the list
            if ((isEJBModule && !isCallerFreeform) ||
                    (enterpriseProject.equals(allProjects[i]) && (isCallerFreeform || isCallerEE6WebProject) ) ) {
                filteredResults.add(allProjects[i]);
            }
        }
        return filteredResults.toArray(new Project[filteredResults.size()]);
    }

    public static boolean isEE6WebProject(Project enterpriseProject) {
        return J2eeProjectCapabilities.forProject(enterpriseProject).isEjb31LiteSupported();
    }

    public static boolean isAppClient(Project project) {
        J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
        return  (module != null) ? module.getJ2eeModule().getType().equals(J2eeModule.Type.CAR) : false;
    }

    /**
     * Checks if the target is Java SE class.
     * <p>
     * <i>Note: Should run outside EDT!</i>
     * @return true if given <code>target</code> is defined in a Java SE environment.
     */
    public static boolean isTargetJavaSE(FileObject fileObject, final String className) throws IOException{
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner.getLookup().lookup(J2eeModuleProvider.class) == null){
            return true;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        if (javaSource == null) {
            return false;
        }
        final boolean[] result = new boolean[] { false };
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                TypeElement junitTestCase = controller.getElements().getTypeElement("junit.framework.TestCase");
                if (junitTestCase != null && typeElement != null) {
                    result[0] = controller.getTypes().isSubtype(typeElement.asType(), junitTestCase.asType());
                }
            }
        }, true);
        return result[0];
    }

//    /**
//     * @return true if given <code>javaClass</code> is a subtype (direct or
//     * indirect) of <code>junit.framework.TestCase</code>.
//     */
//    private static boolean extendsTestCase(CompilationController controller, TypeElement typeElement){
//        if (typeElement == null){
//            return false;
//        }
//        if (typeElement.getQualifiedName().contentEquals("junit.framework.TestCase")){
//            return true;
//        }
//        DeclaredType superClassType = (DeclaredType) typeElement.getSuperclass();
//        return extendsTestCase(controller, (TypeElement) superClassType.asElement());
//    }

    /**
     * Converts the given <code>jndiName</code> to camel case, i.e. removes
     * all <code>/</code> characters and converts characters to upper case appropriately.
     * For example, returns <code>SomeJndiName</code> for <code>some/jndi/name</code> or
     * <code>someJndiName</code> if <code>lowerCaseFirstChar</code> is true.
     * @param jndiName the JNDI name to convert; must not be null.
     * @param lowerCaseFirstChar defines whether the first char of the resulting name
     * should be lower case (note that if all chars of the given <code>jndiName</code> are
     * uppercase characters, its first char will not be converted to lower case even
     * if this param is true).
     * @param prefixToStrip the prefix that will be stripped from the resulting name. If null,
     * nothing will be stripped.
     * @return String representing the converted name.
     */
    public static String jndiNameToCamelCase(String jndiName, boolean lowerCaseFirstChar, String prefixToStrip){

        String strippedJndiName = jndiName;
        if (prefixToStrip != null && jndiName.startsWith(prefixToStrip)){
            strippedJndiName = jndiName.substring(jndiName.indexOf(prefixToStrip) + prefixToStrip.length());
        }

        StringBuilder result = new StringBuilder();

        for (String token : strippedJndiName.split("/")){
            if (token.length() == 0){
                continue;
            }
            char firstChar = token.charAt(0);
            if (lowerCaseFirstChar && result.length() == 0 && !isAllUpperCase(token)){
                firstChar = Character.toLowerCase(firstChar);
            } else {
                firstChar = Character.toUpperCase(firstChar);
            }
            result.append(firstChar);
            result.append(token.substring(1));
        }

        return result.toString();
    }

    public static String makeJavaIdentifierPart(String identifier){
        StringBuilder result = new StringBuilder(identifier.length());
        for (int i = 0; i < identifier.length(); i++){
            if (Character.isJavaIdentifierPart(identifier.charAt(i))){
                result.append(identifier.charAt(i));
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    public static Project getProject(final EjbReference ejbReference, final EjbReference.EjbRefIType refIType) throws IOException {
        FileObject ejbReferenceEjbClassFO = ejbReference.getComponentFO(refIType);
        return ejbReferenceEjbClassFO != null ? FileOwnerQuery.getOwner(ejbReferenceEjbClassFO) : null;
    }

    /**
     * Creates resource name from fully-qualified class name by
     * replacing '.' with '/' and appending ".java"
     */
    public static String toResourceName(String className) {
        assert className != null: "cannot find null className";
        return className.replace('.', '/') + ".java";
    }

    /**
     * @return true if the given <code>str</code> has more than one char
     *  and all its chars are uppercase, false otherwise.
     */
    private static boolean isAllUpperCase(String str){
        if (str.length() <= 1){
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.isLowerCase(c)){
                return false;
            }
        }
        return true;
    }

    public static String getBeanType(final EjbReference ref) throws IOException{
        String type = ref.getEjbModule().getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>(){
            public String run(EjbJarMetadata metadata) throws Exception {
                Session[] sessionEJBs = metadata.getRoot().getEnterpriseBeans().getSession();
                for(Session session: sessionEJBs){
                    if (session.getEjbClass().equals(ref.getEjbClass())){
                        return session.getSessionType();
                    }
                }
                return null;
            }
        });
        return type;
    }

    public static boolean isServlet(FileObject fileObject, final String className){
        final boolean[] result = new boolean[]{false};
        try {
            JavaSource js = JavaSource.forFileObject(fileObject);
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = cc.getElements().getTypeElement(className);
                    TypeElement servletTypeElement = cc.getElements().getTypeElement("javax.servlet.Servlet"); //NOI18N
                    if (typeElement != null && servletTypeElement != null) {
                        result[0] =  cc.getTypes().isSubtype(typeElement.asType(), servletTypeElement.asType());
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result[0];
    }

    public static ClassPath getCompileClassPath(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length > 0) {
            FileObject sourceGroupRoot = sourceGroups[0].getRootFolder();
            ClassPath classPath = ClassPath.getClassPath(sourceGroupRoot, ClassPath.COMPILE);
            return classPath;
        }
        return null;
    }

    public static boolean removeLibraryFromClasspath(Project project, Library... libraries) throws IOException {
        boolean result = false;
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            boolean cpChange = ProjectClassPathModifier.removeLibraries(
                    libraries,
                    sourceGroup.getRootFolder(),
                    JavaClassPathConstants.COMPILE_ONLY);
            if (cpChange) {
                result = true;
            }
        }
        return result;
    }

    public static void addLibraryToClasspath(Project project, Library... libraries) throws IOException {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            ProjectClassPathModifier.addLibraries(
                    libraries,
                    sourceGroup.getRootFolder(),
                    JavaClassPathConstants.COMPILE_ONLY);
        }
    }

//    public static ExecutableElement[] getMethods(EjbMethodController c, boolean checkLocal, boolean checkRemote) {
//        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
//        List features;
//        for (ExecutableElement method : ElementFilter.methodsIn(c.getBeanClass().getEnclosedElements())) {
//            methods.add(method);
//        }
//        if (checkLocal) {
//            for (TypeElement interfaceCE : c.getLocalInterfaces()) {
//                for (ExecutableElement method : ElementFilter.methodsIn(interfaceCE.getEnclosedElements())) {
//                    methods.add(method);
//                }
//            }
//        }
//        if (checkRemote) {
//            for (TypeElement interfaceCE : c.getRemoteInterfaces()) {
//                for (ExecutableElement method : ElementFilter.methodsIn(interfaceCE.getEnclosedElements())) {
//                    methods.add(method);
//                }
//            }
//        }
//        ExecutableElement[] methodsArray = methods.toArray(new ExecutableElement[methods.size()]);
//        return methodsArray;
//    }

}
