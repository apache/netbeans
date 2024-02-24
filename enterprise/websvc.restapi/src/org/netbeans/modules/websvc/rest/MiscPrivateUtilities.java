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
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Bunch of static utility methods which were previously bloating RestSupprt API
 * class without actually needed to be in the API. They should be further refactored
 * and moved to the right places.
 */
public class MiscPrivateUtilities {
    // copy pasted from Maven project:
    public static final String DEVNULL = "DEV-NULL"; //NOI18N
    
    private static final String JACKSON_JSON_PROVIDER =
            "org.codehaus.jackson.jaxrs.JacksonJsonProvider"; // NOI18N

    public static void setProjectProperty(final Project project, final AntProjectHelper helper, final String name, final String value, final String propertyPath) {
        if (helper == null) {
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                @Override
                public Object run() throws IOException {
                    try {
                        EditableProperties ep = helper.getProperties(propertyPath);
                        ep.setProperty(name, value);
                        helper.putProperties(propertyPath, ep);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ioe) {
                        Logger.getLogger(MiscPrivateUtilities.class.getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            Logger.getLogger(MiscUtilities.class.getName()).log(Level.INFO, null, e);
        }
    }

    public static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<ClassPath> classPaths = new ArrayList<ClassPath>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            String sourceGroupId = sourceGroup.getName();
            if (sourceGroupId != null && sourceGroupId.contains("test")) {
                // NOI18N
                continue;
            }
            FileObject rootFolder = sourceGroup.getRootFolder();
            ClassPath path = provider.findClassPath(rootFolder, type);
            classPaths.add(path);
        }
        return ClassPathSupport.createProxyClassPath(classPaths.toArray(new ClassPath[0]));
    }

    public static boolean hasResource(Project project, String resource) {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return false;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        if (classPath == null) {
            return false;
        }
        FileObject resourceFile = classPath.findResource(resource);
        return resourceFile != null;
    }

    public static void removeProperty(final AntProjectHelper helper, String[] propertyNames, String propertiesPath) {
        EditableProperties ep = helper.getProperties(propertiesPath);
        for (String name : propertyNames) {
            ep.remove(name);
        }
        helper.putProperties(propertiesPath, ep);
    }

    public static boolean hasApplicationResourceClass(RestSupport restSupport, final String fqn){
        List<RestApplication> applications = restSupport.getRestApplications();
        if ( applications.isEmpty() ){
            return false;
        }
        final String clazz = applications.get(0).getApplicationClass();
        final boolean[] has = new boolean[1];
        try {
            JavaSource javaSource = getJavaSourceFromClassName(restSupport.getProject(), clazz);

            if (javaSource == null ){
                return false;
            }
            javaSource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run( final CompilationController controller )
                        throws Exception
                {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                    TypeElement typeElement = controller.getElements()
                            .getTypeElement(clazz);
                    if (typeElement == null) {
                        return;
                    }
                    TypeElement restResource = controller.getElements()
                            .getTypeElement(fqn);
                    if (restResource == null) {
                        return;
                    }
                    List<ExecutableElement> methods = ElementFilter
                            .methodsIn(typeElement.getEnclosedElements());
                    boolean overridesGetClasses = false;
                    ExecutableElement getClasses = null;
                    for (ExecutableElement method : methods) {
                        if (method.getSimpleName().contentEquals(RestConstants.GET_CLASSES)) {
                            overridesGetClasses = true;
                        }
                        if (method.getSimpleName().contentEquals(ApplicationSubclassGenerator.GET_REST_RESOURCE_CLASSES) &&
                                method.getParameters().isEmpty() && getClasses == null) {
                            getClasses = method;
                        }
                        if (method.getSimpleName().contentEquals(RestConstants.GET_REST_RESOURCE_CLASSES2)) {
                            getClasses = method;
                        }
                    }
                    if (!overridesGetClasses) {
                        // if Application subclass does not override getClasses method then
                        // all classes are scanned automatically
                        has[0] = true;
                        return;
                    }
                    if (getClasses == null) {
                        return;
                    }

                    final String className = restResource.getQualifiedName()
                            .toString() + ".class"; // NOI18N
                    final MethodTree tree = controller.getTrees().getTree(
                            getClasses);
                    final Document doc = controller.getDocument();
                    if ( doc ==null){
                        return;
                    }
                    doc.render(new Runnable() {

                        @Override
                        public void run() {
                            SourcePositions srcPos = controller.getTrees()
                                    .getSourcePositions();
                            int start = (int) srcPos.getStartPosition(
                                    controller.getCompilationUnit(), tree);
                            int end = (int) srcPos.getEndPosition(
                                    controller.getCompilationUnit(), tree);

                            try {
                                String text = doc.getText(start, end - start + 1);
                                if (text.contains(className)) {
                                    has[0] = true;
                                }
                            } catch(BadLocationException blex) {
                                Logger.getLogger(MiscPrivateUtilities.class.getName()).log(Level.INFO, null, blex);
                            }
                        }
                    });

                    /*
                     * List<? extends ImportTree> imports =
                     * controller.getCompilationUnit().getImports(); for
                     * (ImportTree importTree : imports) { importTree. }
                     */
                }

            }, true);
        }
        catch(IOException e ){
            Logger.getLogger(RestSupport.class.getName()).log(
                    Level.INFO, e.getLocalizedMessage(), e);
        }

        return has[0];
    }

    private static FileObject getFileObjectFromClassName(Project p, String qualifiedClassName)
            throws IOException
    {
        FileObject root = MiscUtilities.findSourceRoot(p);
        ClasspathInfo cpInfo = ClasspathInfo.create(root);
        ClassIndex ci = cpInfo.getClassIndex();
        int beginIndex = qualifiedClassName.lastIndexOf('.')+1;
        String simple = qualifiedClassName.substring(beginIndex);
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes(
                simple, ClassIndex.NameKind.SIMPLE_NAME,
                Collections.singleton(ClassIndex.SearchScope.SOURCE));
        if ( handles == null ){
            return null;
        }
        for (ElementHandle<TypeElement> handle : handles) {
            if (qualifiedClassName.equals(handle.getQualifiedName())) {
                return SourceUtils.getFile(handle, cpInfo);
            }
        }
        return null;
    }

    static JavaSource getJavaSourceFromClassName(Project p, String qualifiedClassName)
            throws IOException
    {
        FileObject fo = getFileObjectFromClassName(p, qualifiedClassName);
        if (fo != null) {
            return JavaSource.forFileObject(fo);
        } else {
            return null;
        }
    }

    public static boolean supportsTargetProfile(Project project, Profile profile){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().
                lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if ( serverInstanceID == null ){
            return false;
        }
        ServerInstance serverInstance = Deployment.getDefault().
                 getServerInstance(serverInstanceID);
        try {
            Set<Profile> profiles = serverInstance.getJ2eePlatform().getSupportedProfiles();
            return profiles.contains( profile);
        }
        catch( InstanceRemovedException e ){
            return false;
        }
    }


    /**
     * Generates test client.  Typically RunTestClientAction would need to call
     * this before invoke the build script target.
     *
     * @param testdir directory to write test client files in.
     * @return test file object, containing token BASE_URL_TOKEN whether used or not.
     * @throws IOException when the generation of some file fails.
     */
    public static FileObject generateTestClient(File testdir) throws IOException {

        if (! testdir.isDirectory()) {
            FileUtil.createFolder(testdir);
        }
        String[] replaceKeys1 = {
            "TTL_TEST_RESBEANS", "MSG_TEST_RESBEANS_INFO"
        };
        String[] replaceKeys2 = {
            "MSG_TEST_RESBEANS_wadlErr", "MSG_TEST_RESBEANS_No_AJAX", "MSG_TEST_RESBEANS_Resource",
            "MSG_TEST_RESBEANS_See", "MSG_TEST_RESBEANS_No_Container", "MSG_TEST_RESBEANS_Content",
            "MSG_TEST_RESBEANS_TabularView", "MSG_TEST_RESBEANS_RawView", "MSG_TEST_RESBEANS_ResponseHeaders",
            "MSG_TEST_RESBEANS_Help", "MSG_TEST_RESBEANS_TestButton", "MSG_TEST_RESBEANS_Loading",
            "MSG_TEST_RESBEANS_Status", "MSG_TEST_RESBEANS_Headers", "MSG_TEST_RESBEANS_HeaderName",
            "MSG_TEST_RESBEANS_HeaderValue", "MSG_TEST_RESBEANS_Insert", "MSG_TEST_RESBEANS_NoContents",
            "MSG_TEST_RESBEANS_AddParamButton", "MSG_TEST_RESBEANS_Monitor", "MSG_TEST_RESBEANS_No_SubResources",
            "MSG_TEST_RESBEANS_SubResources", "MSG_TEST_RESBEANS_ChooseMethod", "MSG_TEST_RESBEANS_ChooseMime",
            "MSG_TEST_RESBEANS_Continue", "MSG_TEST_RESBEANS_AdditionalParams", "MSG_TEST_RESBEANS_INFO",
            "MSG_TEST_RESBEANS_Request", "MSG_TEST_RESBEANS_Sent", "MSG_TEST_RESBEANS_Received",
            "MSG_TEST_RESBEANS_TimeStamp", "MSG_TEST_RESBEANS_Response", "MSG_TEST_RESBEANS_CurrentSelection",
            "MSG_TEST_RESBEANS_DebugWindow", "MSG_TEST_RESBEANS_Wadl", "MSG_TEST_RESBEANS_RequestFailed",
            "MSG_TEST_RESBEANS_NoContent"       // NOI18N

        };
        FileObject testFO = MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_HTML, replaceKeys1, true);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_JS, replaceKeys2, false);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS2);
        MiscUtilities.copyFile(testdir, "expand.gif");
        MiscUtilities.copyFile(testdir, "collapse.gif");
        MiscUtilities.copyFile(testdir, "item.gif");
        MiscUtilities.copyFile(testdir, "cc.gif");
        MiscUtilities.copyFile(testdir, "og.gif");
        MiscUtilities.copyFile(testdir, "cg.gif");
        MiscUtilities.copyFile(testdir, "app.gif");

        File testdir2 = new File(testdir, "images");
        testdir2.mkdir();
        MiscUtilities.copyFile(testdir, "images/background_border_bottom.gif");
        MiscUtilities.copyFile(testdir, "images/pbsel.png");
        MiscUtilities.copyFile(testdir, "images/bg_gradient.gif");
        MiscUtilities.copyFile(testdir, "images/pname.png");
        MiscUtilities.copyFile(testdir, "images/level1_deselect.jpg");
        MiscUtilities.copyFile(testdir, "images/level1_selected-1lvl.jpg");
        MiscUtilities.copyFile(testdir, "images/primary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/masthead.png");
        MiscUtilities.copyFile(testdir, "images/masthead_link_enabled.gif");
        MiscUtilities.copyFile(testdir, "images/masthead_link_roll.gif");
        MiscUtilities.copyFile(testdir, "images/primary-roll.gif");
        MiscUtilities.copyFile(testdir, "images/pbdis.png");
        MiscUtilities.copyFile(testdir, "images/secondary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/pbena.png");
        MiscUtilities.copyFile(testdir, "images/tbsel.png");
        MiscUtilities.copyFile(testdir, "images/pbmou.png");
        MiscUtilities.copyFile(testdir, "images/tbuns.png");
        return testFO;
    }
    
    public static String collectRestResources( Collection<String> classes,
            RestSupport restSupport, final boolean oldVersion) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        if (oldVersion) {
            builder.append("Set<Class<?>> resources = new java.util.HashSet<Class<?>>();");// NOI18N
        }
        for (String clazz : classes) {
            handleResource(clazz, builder);
        }
        if (oldVersion && !restSupport.hasJersey2(true)) {
            builder.append(getJacksonProviderSnippet(restSupport));
        }
        if (oldVersion) {
            builder.append("return resources;");                // NOI18N
        }
        builder.append('}');
        return builder.toString();
    }
    
    public static String getJacksonProviderSnippet(RestSupport restSupport){
        boolean addJacksonProvider = MiscPrivateUtilities.hasResource(restSupport.getProject(),
                "org/codehaus/jackson/jaxrs/JacksonJsonProvider.class");    // NOI18N
        if( !addJacksonProvider) {
            JaxRsStackSupport support = restSupport.getJaxRsStackSupport();
            if (support != null){
                addJacksonProvider = support.isBundled(JACKSON_JSON_PROVIDER);
            }
        }
        StringBuilder builder = new StringBuilder();
        if ( addJacksonProvider ){
            builder.append("\n// following code can be used to customize Jersey 1.x JSON provider: \n");
            builder.append("try {");
            builder.append("Class jacksonProvider = Class.forName(");
            builder.append('"');
            builder.append(JACKSON_JSON_PROVIDER);
            builder.append("\");");
            builder.append("resources.add(jacksonProvider);");
            builder.append("} catch (ClassNotFoundException ex) {");
            builder.append("java.util.logging.Logger.getLogger(getClass().getName())");
            builder.append(".log(java.util.logging.Level.SEVERE, null, ex);}\n");
            return builder.toString();
        }
        else {
            return builder.toString();
        }
    }
    
    private static void handleResource(String className, StringBuilder builder) throws IllegalArgumentException {
        builder.append("resources.add(");       // NOI18N
        builder.append( className );
        builder.append(".class);");             // NOI18N
    }


    public static class JerseyFilter implements FileFilter {
        private final Pattern pattern;

        public JerseyFilter(String regexp) {
            pattern = Pattern.compile(regexp);
        }

        @Override
        public boolean accept(File pathname) {
            return pattern.matcher(pathname.getName()).matches();
        }
    }


}
