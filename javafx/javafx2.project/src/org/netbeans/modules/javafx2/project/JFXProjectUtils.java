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
package org.netbeans.modules.javafx2.project;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.platform.api.JavaFxRuntimeInclusion;
import org.netbeans.modules.javafx2.project.JavaFXProjectWizardIterator.WizardType;
import org.netbeans.modules.javafx2.project.fxml.SourceGroupSupport;
import org.netbeans.modules.javafx2.project.fxml.SourceGroupSupport.SourceGroupProxy;
import org.netbeans.modules.javafx2.project.ui.JSEApplicationClassChooser;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.cookies.CloseCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for JavaFX 2.0+ Project
 * 
 * @author Petr Somol
 */
public final class JFXProjectUtils {

    private static Set<SearchKind> kinds = new HashSet<SearchKind>(Arrays.asList(SearchKind.IMPLEMENTORS));
    private static Set<SearchScope> scopes = new HashSet<SearchScope>(Arrays.asList(SearchScope.SOURCE));
    
    private static final String JFX_BUILD_TEMPLATE = "Templates/JFX/jfx-impl.xml"; //NOI18N
    private static final String CURRENT_EXTENSION = "jfx3";  //NOI18N
    private static final String[] OLD_EXTENSIONS = new String[] {"jfx", "jfx2"}; // NOI18N
    private static final String NBPROJECT = "nbproject"; // NOI18N
    private static final String JFX_BUILD_IMPL_NAME = "jfx-impl"; // NOI18N
    private static final String JFX_BUILD_IMPL_PATH = NBPROJECT + "/" + JFX_BUILD_IMPL_NAME + ".xml";   //NOI18N
    private static volatile String currentJfxImplCRCCache;
    private static final String TRUE = "true"; //NOI18N
    private static final String FALSE = "false"; //NOI18N
    // from J2SEDeployProperties
    private static final String J2SEDEPLOY_EXTENSION = "j2sedeploy";    //NOI18N
    private static final String EXTENSION_BUILD_SCRIPT_PATH = "nbproject/build-native.xml";        //NOI18N
    //Important path types for Java model
    private static final String[] JAVA_PATHS = {
            ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE,
            JavaClassPathConstants.MODULE_BOOT_PATH, JavaClassPathConstants.MODULE_COMPILE_PATH,
            JavaClassPathConstants.MODULE_CLASS_PATH, /*JavaClassPathConstants.MODULE_SOURCE_PATH*/
    };

    // two deprecated properties, to be auto-cleaned from project.properties if present
    @Deprecated
    public static final String PROPERTY_JAVAFX_RUNTIME = "javafx.runtime"; // NOI18N
    @Deprecated
    public static final String PROPERTY_JAVAFX_SDK = "javafx.sdk"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    
    /**
     * Return current name (version) of build-impl.xml "extension spi"
     * where the spi consists of target dependency points between build-impl
     * and jfx-impl.
     * @return name as string
     */
    public static String getCurrentExtensionName() {
        return CURRENT_EXTENSION;
    }

    /**
     * Return list of old names (versions) of build-impl.xml "extension spis". 
     * Used to search for spi version when jfx-impl and build-impl do not match
     * and correct update needs to be determined.
     * @return names as strings
     */
    public static Iterable<? extends String> getOldExtensionNames() {
        return Arrays.asList(OLD_EXTENSIONS);
    }    

    /**
     * Returns list of JavaFX 2.0 JavaScript callback entries.
     * In future should read the list from the current platform
     * (directly from FX SDK or Ant taks).
     * Current list taken from
     * http://javaweb.us.oracle.com/~in81039/new-dt/js-api/Callbacks.html
     * 
     * @param IDE java platform name
     * @return callback entries
     */
    public static Map<String,List<String>/*|null*/> getJSCallbacks(String platformName) {
        final String[][] c = {
            {"onDeployError", "app", "mismatchEvent"}, // NOI18N
            {"onGetNoPluginMessage", "app"}, // NOI18N
            {"onGetSplash", "app"}, // NOI18N
            {"onInstallFinished", "placeholder", "component", "status", "relaunchNeeded"}, // NOI18N
            {"onInstallNeeded", "app", "platform", "cb", "isAutoinstall", "needRelaunch", "launchFunc"}, // NOI18N
            {"onInstallStarted", "placeholder", "component", "isAuto", "restartNeeded"}, // NOI18N
            {"onJavascriptReady", "id"}, // NOI18N
            {"onRuntimeError", "id", "code"} // NOI18N
        };
        Map<String,List<String>/*|null*/> m = new LinkedHashMap<String,List<String>/*|null*/>();
        for(int i = 0; i < c.length; i++) {
            String[] s = c[i];
            assert s.length > 0;
            List<String> l = null;
            if(s.length > 1) {
                l = new ArrayList<String>();
                for(int j = 1; j < s.length; j++) {
                    l.add(s[j]);
                }
            }
            m.put(s[0], l);
        }
        return m;
    }
    
    /**
     * Returns all classpaths relevant for given project. To be used in
     * main class searches.
     * 
     * @param project
     * @return map of classpaths of all project files
     */
    public static Map<FileObject,Map<String,ClassPath>> getClassPathMap(@NonNull Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final Map<FileObject,Map<String,ClassPath>> classpathMap = new HashMap<>();
        final BitSet requiredPaths = new BitSet(3);
        requiredPaths.set(0, 3, true);
        for (SourceGroup srcGroup : srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            final Map<String, ClassPath> sgCps = new HashMap<>();
            final BitSet seen = new BitSet(JAVA_PATHS.length);
            for (int i = 0; i < JAVA_PATHS.length; i++) {
                final ClassPath cp = ClassPath.getClassPath(srcRoot, JAVA_PATHS[i]);
                if (cp != null) {
                    sgCps.put(JAVA_PATHS[i], cp);
                    seen.set(i);
                }
            }
            seen.and(requiredPaths);
            if (seen.cardinality() == 3) {
                classpathMap.put(srcRoot, sgCps);
            }
        }
        return classpathMap;
    }

    /**
     * Returns source roots of a project
     * @param project
     * 
     * @return set of fileobjects representing the source roots
     */
    public static Set<FileObject> getSourceRoots(@NonNull Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final Set<FileObject> sourceRoots = new HashSet<FileObject>();
        for (SourceGroup srcGroup : srcGroups) {
            sourceRoots.add(srcGroup.getRootFolder());
        }
        return sourceRoots;
    }
    
    /**
     * Returns set of names of main classes in the given project
     * 
     * @param project
     * @return set of class names
     */
    public static Set<String> getMainClassNames(@NonNull Project project) {
        final Set<String> mainClassNames = new HashSet<String>();
        FileObject sourceRoots[] = getSourceRoots(project).toArray(new FileObject[0]);
        for (ElementHandle<TypeElement> elemHandle : SourceUtils.getMainClasses(sourceRoots)) {
            mainClassNames.add(elemHandle.getQualifiedName());
        }
        return mainClassNames;
    }
    
    /**
     * Returns set of names of classes of the classType type.
     * 
     * @param classpathMap map of classpaths of all project files
     * @param classType return only classes of this type
     * @return set of class names
     */
    public static Set<String> getAppClassNames(@NonNull Collection<? extends FileObject> roots, final @NonNull String classType) {
        final Set<String> appClassNames = new HashSet<>();
        for (FileObject fo : roots) {
            final ClasspathInfo cpInfo = ClasspathInfo.create(fo);
            final JavaSource js = JavaSource.create(cpInfo);
            if (js != null) {
                try { 
                    js.runUserActionTask(new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController controller) throws Exception {
                            final ClassIndex classIndex = cpInfo.getClassIndex();
                            final Elements elems = controller.getElements();
                            TypeElement fxAppElement = elems.getTypeElement(classType);
                            ElementHandle<TypeElement> appHandle = ElementHandle.create(fxAppElement);
                            Set<ElementHandle<TypeElement>> appHandles = classIndex.getElements(appHandle, kinds, scopes);
                            for (ElementHandle<TypeElement> elemHandle : appHandles) {
                                appClassNames.add(elemHandle.getQualifiedName());
                            }
                        }
                    }, true);
                } catch (Exception e) {

                }
            }
        }
        return appClassNames;
    }


    /** Finds available FX Preloader classes in given JAR files. 
     * Looks for classes specified in the JAR manifest only.
     * 
     * @param jarFile FileObject representing an existing JAR file
     * @param classType return only classes of this type
     * @return set of class names
     */
    public static Set<String> getAppClassNamesInJar(@NonNull FileObject jarFile, final String classType, final String fxrtJarPath) {
        final File jarF = FileUtil.toFile(jarFile);
        if (jarF == null) {
            return null;
        }
        boolean jfxrtExists = false;
        List<URL> toLoad = new ArrayList<URL>();
        try {
            assert jarF.exists();
            toLoad.add(jarF.toURI().toURL());
            final File jfxrt = new File(fxrtJarPath);
            if(jfxrt.exists()) {
                jfxrtExists = true;
            }
            toLoad.add(jfxrt.toURI().toURL());            

        } catch (MalformedURLException ex) {
            return null;
        }        
        URLClassLoader clazzLoader = URLClassLoader.newInstance(toLoad.toArray(new URL[0]));

        final Set<String> appClassNames = new HashSet<String>();
        JarFile jf;
        try {
            jf = new JarFile(jarF);
        } catch (IOException x) {
            return null;
        }
        Enumeration<? extends JarEntry> entries = jf.entries();
        if (entries == null) {
            return null;
        }        
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            // Relative path of file into the jar
            String classFileName = entry.getName();
            
            if(!classFileName.endsWith(".class")) { // NOI18N
                continue;
            }
            if(classFileName.contains("$")) { // NOI18N
                continue;
            }
            // Complete class name
            String className = classFileName.replace(".class", "").replace('\\', '/').replace('/', '.'); // NOI18N
            
            if(clazzLoader != null && jfxrtExists) {
                // Load class definition from JVM
                Class<?> clazz;
                try {
                    clazz = Class.forName(className, true, clazzLoader);
                    Type t = clazz.getGenericSuperclass();
                    if(t.toString().contains(classType)) {
                        if (className.startsWith(".")) { // NOI18N
                            className = className.substring(1);
                        }
                        appClassNames.add(className);
                    }
                } catch (ClassNotFoundException ex) {
                    return null;
                }
            } else {
                if (className.startsWith(".")) { // NOI18N
                    className = className.substring(1);
                }
                appClassNames.add(className);
            }
        }

        return appClassNames;
    }

    /**
     * Checks if the JFX support is enabled for given project
     * @param prj the project to check
     * @return true if project supports JFX
     */
    public static boolean isFXProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

     /**
     * Checks if the project is a Swing project with JFX support enabled
     * @param prj the project to check
     * @return true if project supports FX in Swing
     */
    public static boolean isFXinSwingProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED))
                && JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
    }

     /**
     * Checks if the project is a JavaFX preloader project. Note that in pre-7.2 NB
     * preloader type had been used for fx-in-swing as workaround, hence the logic below
     * @param prj the project to check
     * @return true if project is JavaFX preloader
     */
    public static boolean isFXPreloaderProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED))
                && JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_PRELOADER))
                && !JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
    }

    /**
     * Checks if the project uses Maven build system
     *
     * @param prj the project to check
     * @return true if is Maven project
     */
    public static boolean isMavenProject(@NonNull final Project prj) {
        FileObject fo = prj.getProjectDirectory();
        if (fo == null || !fo.isValid()) {
            return false;
        }
        fo = fo.getFileObject("pom.xml"); //NOI18N
        return fo != null;
    }

    /**
     * Checks if the project uses Gradle build system
     *
     * @param prj the project to check
     * @return true if is Gradle project
     */
    public static boolean isGradleProject(@NonNull final Project prj) {
        FileObject fo = prj.getProjectDirectory();
        if (fo == null || !fo.isValid()) {
            return false;
        }
        fo = fo.getFileObject("build.gradle"); //NOI18N
        return fo != null;
    }

    public static boolean isMavenFXProject(@NonNull final Project prj) {
        if (isMavenProject(prj)) {
            try {
                FileObject pomXml = prj.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(FileUtil.toFile(pomXml));
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression exprJfxrt = xpath.compile("//bootclasspath[contains(text(),'jfxrt')]"); //NOI18N
                XPathExpression exprFxPackager = xpath.compile("//executable[contains(text(),'javafxpackager')]"); //NOI18N
                XPathExpression exprPackager = xpath.compile("//executable[contains(text(),'javapackager')]"); //NOI18N
                boolean jfxrt = (Boolean) exprJfxrt.evaluate(doc, XPathConstants.BOOLEAN);
                boolean packager = (Boolean) exprPackager.evaluate(doc, XPathConstants.BOOLEAN);
                boolean fxPackager = (Boolean) exprFxPackager.evaluate(doc, XPathConstants.BOOLEAN);
                return jfxrt && (packager || fxPackager);
            } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException ex) {
                LOGGER.log(Level.INFO, "Error while parsing pom.xml.", ex);  //NOI18N
                return false;
            }
        }
        return false;
    }

    /**
     * Checks what Run model is selected in current configuration of JFX Run Project Property panel
     * @param prj the project to check
     * @return string value of JFXProjectProperties.RunAsType type or null meaning JFXProjectProperties.RunAsType.STANDALONE
     */
    public static String getFXProjectRunAs(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return null;
        }
        return ep.evaluator().getProperty(JFXProjectProperties.RUN_AS);
    }

    /**
     * Finds the relative path to targetFO from sourceFO. 
     * Unlike FileUtil.getRelativePath() does not require targetFO to be within sourceFO sub-tree
     * Returns null if there is no shared parent directory except root.
     * 
     * @param sourceFO file/dir to which the relative path will be related
     * @param targetFO file whose location will be determined with respect to sourceFO
     * @return string relative path leading from sourceFO to targetFO
     */
    public static String getRelativePath(@NonNull final FileObject sourceFO, @NonNull final FileObject targetFO) {
        String path = ""; //NOI18N
        FileObject src = sourceFO;
        FileObject tgt = targetFO;
        String targetName = null;
        if(!src.isFolder()) {
            src = src.getParent();
        }
        if(!tgt.isFolder()) {
            targetName = tgt.getNameExt();
            tgt = tgt.getParent();
        }
        LinkedList<String> srcSplit = new LinkedList<String>();
        LinkedList<String> tgtSplit = new LinkedList<String>();
        while(!src.isRoot()) {
            srcSplit.addFirst(src.getName());
            src = src.getParent();
        }
        while(!tgt.isRoot()) {
            tgtSplit.addFirst(tgt.getName());
            tgt = tgt.getParent();
        }
        boolean share = false;
        while(!srcSplit.isEmpty() && !tgtSplit.isEmpty()) {
            if(srcSplit.getFirst().equals(tgtSplit.getFirst())) {
                srcSplit.removeFirst();
                tgtSplit.removeFirst();
                share = true;
            } else {
                break;
            }
        }
        if(!share) {
            return null;
        }
        for(int left = 0; left < srcSplit.size(); left++) {
            if(left == 0) {
                path += ".."; //NOI18N
            } else {
                path += "/.."; //NOI18N
            }
        }
        while(!tgtSplit.isEmpty()) {
            if(path.isEmpty()) {
                path += tgtSplit.getFirst();
            } else {
                path += "/" + tgtSplit.getFirst(); //NOI18N
            }
            tgtSplit.removeFirst();
        }
        if(targetName != null) {
            if(!path.isEmpty()) {
                path += "/" + targetName; //NOI18N
            } else {
                path += targetName;
            }
        }
        return path;
    }

    /**
     * Finds the file/dir represented by relPath with respect to sourceDir. 
     * Returns null if the file does not exist.
     * 
     * @param sourceDir file/dir to which the relative path is related
     * @param relPath relative path related to sourceDir
     * @return FileObject or null
     */
    public static FileObject getFileObject(@NonNull final FileObject sourceDir, @NonNull final String relPath) {
        String split[] = relPath.split("[\\\\/]+"); //NOI18N
        FileObject src = sourceDir;
        String path = ""; //NOI18N
        boolean back = true;
        if(split[0].equals("..")) {
            for(int i = 0; i < split.length; i++) {
                if(back && split[i].equals("..")) { //NOI18N
                    src = src.getParent();
                    if(src == null) {
                        return null;
                    }
                } else {
                    if(back) {
                        back = false;
                        path = src.getPath();
                    }
                    path += "/" + split[i]; //NOI18N
                }
            }
        } else {
            path = relPath;
        }
        File f = new File(path);
        if(f.exists()) {
            return FileUtil.toFileObject(f);
        }
        return null;
    }

    /**
     * Initialize project properties for JavaFX Application project type
     * @param ep
     * @param type
     * @param platformName
     * @param mainClass
     * @param preloader
     * @throws MissingResourceException 
     */
    static void initializeJavaFXProperties(@NonNull EditableProperties ep, @NonNull WizardType type, String targetPlatformName, @NonNull String mainClass, String preloader) throws MissingResourceException {
        ep.setProperty(JFXProjectProperties.JAVAFX_ENABLED, TRUE); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_javafx")}, false); // NOI18N
        ep.setProperty("jnlp.enabled", FALSE); // NOI18N
        ep.setComment("jnlp.enabled", new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_oldjnlp")}, false); // NOI18N
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, TRUE); // NOI18N
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX + ".javafx", TRUE); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_BINARY_ENCODE_CSS, FALSE); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_DEPLOY_INCLUDEDT, TRUE); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_DEPLOY_EMBEDJNLP, TRUE); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_REBASE_LIBS, FALSE); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_REBASE_LIBS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_rebase_libs")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_DISABLE_CONCURRENT_RUNS, FALSE); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_DISABLE_CONCURRENT_RUNS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_disable_concurrent_runs")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS, FALSE); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_enable_concurrent_external_runs")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.UPDATE_MODE_BACKGROUND, FALSE); // NOI18N
        ep.setComment(JFXProjectProperties.UPDATE_MODE_BACKGROUND, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, type == WizardType.SWING ? "COMMENT_updatemode_swing" : "COMMENT_updatemode")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.ALLOW_OFFLINE, TRUE); // NOI18N
        if(targetPlatformName == null) {
            targetPlatformName = ep.getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
        }
        Collection<String> extensions = JavaFxRuntimeInclusion.getProjectClassPathExtension(JavaFXPlatformUtils.findJavaPlatform(targetPlatformName));
        if (extensions != null && !extensions.isEmpty()) {
            ep.setProperty(JavaFXPlatformUtils.JAVAFX_CLASSPATH_EXTENSION, JFXProjectUtils.getPaths(extensions));
            ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, new String[]{JavaFXPlatformUtils.getClassPathExtensionProperty()});
        }
        ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, ""); // NOI18N
        ep.setProperty(JFXProjectProperties.RUN_APP_WIDTH, "800"); // NOI18N
        ep.setProperty(JFXProjectProperties.RUN_APP_HEIGHT, "600"); // NOI18N
        if (type == WizardType.PRELOADER) {
            ep.setProperty(JFXProjectProperties.JAVAFX_PRELOADER, TRUE); // NOI18N
            ep.setComment(JFXProjectProperties.JAVAFX_PRELOADER, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_preloader")}, false); // NOI18N
            ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, FALSE); // NOI18N
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_prepreloader")}, false); // NOI18N
        } else {
            ep.setProperty("jar.archive.disabled", TRUE); // NOI18N
            ep.setComment("jar.archive.disabled", new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_oldjar")}, false); // NOI18N
            ep.setProperty(ProjectProperties.MAIN_CLASS, type == WizardType.SWING ? (mainClass == null ? "" : mainClass) : "com.javafx.main.Main"); // NOI18N
            ep.setComment(ProjectProperties.MAIN_CLASS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_main.class")}, false); // NOI18N
            if (type != WizardType.LIBRARY) {
                ep.setProperty(JFXProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass); // NOI18N
                ep.setComment(JFXProjectProperties.MAIN_CLASS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_main.fxclass")}, false); // NOI18N
            }
            if (preloader != null && preloader.length() > 0) {
                String preloaderProjRelative = "../" + preloader; // NOI18N
                String preloaderJarFileName = preloader + ".jar"; // NOI18N
                String copiedPreloaderJarPath = "${dist.dir}/lib/${" + JFXProjectProperties.PRELOADER_JAR_FILENAME + "}"; // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, TRUE); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.PROJECT.getString());
                ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_use_preloader")}, false); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_PROJECT, preloaderProjRelative);
                ep.setProperty(JFXProjectProperties.PRELOADER_CLASS, JavaFXProjectWizardIterator.generatePreloaderClassName(preloader));
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_PATH, copiedPreloaderJarPath);
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME, preloaderJarFileName);
            } else {
                ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, FALSE); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.NONE.getString());
                ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_dontuse_preloader")}, false); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_PROJECT, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_CLASS, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_PATH, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME, ""); // NOI18N
            }
            if (type == WizardType.SWING) {
                ep.setProperty(JFXProjectProperties.JAVAFX_SWING, TRUE); // NOI18N
                ep.setComment(JFXProjectProperties.JAVAFX_SWING, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_use_swing")}, false); // NOI18N
            }
        }
        ep.setProperty(JFXProjectProperties.IMPLEMENTATION_VERSION, JFXProjectProperties.IMPLEMENTATION_VERSION_DEFAULT);
        ep.setProperty(JFXProjectProperties.FALLBACK_CLASS, "com.javafx.main.NoJavaFXFallback"); // NOI18N
        // extended manifest entries
        ep.setProperty(JFXProjectProperties.MANIFEST_CUSTOM_CODEBASE, "*"); // NOI18N
        ep.setComment(JFXProjectProperties.MANIFEST_CUSTOM_CODEBASE, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_manifest_custom_codebase")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.MANIFEST_CUSTOM_PERMISSIONS, ""); // NOI18N
        ep.setComment(JFXProjectProperties.MANIFEST_CUSTOM_PERMISSIONS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_manifest_custom_permissions")}, false); // NOI18N
        // SE->FX conversion cleanup
        ep.remove(JFXProjectProperties.JAVASE_KEEP_JFXRT_ON_CLASSPATH);
    }

    /**
     * Modify project.properties file to contain all javafx relevant properties with initial values
     * @param project
     * @throws IOException 
     */
    public static void resetJavaFXProjectProperties(@NonNull final Project project, @NonNull final WizardType type, final String targetPlatformName, @NonNull final String mainClass, final String preloader) throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final Lookup lookup = project.getLookup();
                    final J2SEPropertyEvaluator eval = lookup.lookup(J2SEPropertyEvaluator.class);
                    String platformNameToCheck = null;
                    if (eval != null) {
                        String currentPlatformName = eval.evaluator().getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
                        platformNameToCheck = currentPlatformName;
                        JavaPlatform platform = null;
                        if(targetPlatformName == null) {
                            platform = JavaFXPlatformUtils.findJavaPlatform(currentPlatformName);
                        } else {
                            platform = JavaFXPlatformUtils.findJavaPlatform(targetPlatformName);
                        }
                        if(platform == null || !JavaFXPlatformUtils.isJavaFXEnabled(platform)) {
                            platform = JavaFXPlatformUtils.findJavaFXPlatform();
                        }
                        if(platform != null && !JFXProjectProperties.isEqual(currentPlatformName, JavaFXPlatformUtils.getPlatformAntName(platform))) {
                            final J2SEProjectPlatform platformSetter = lookup.lookup(J2SEProjectPlatform.class);
                            if(platformSetter != null) {
                                platformSetter.setProjectPlatform(platform);
                                platformNameToCheck = JavaFXPlatformUtils.getPlatformAntName(platform);
                            }
                        }
                    }
                    final InputStream is = projPropsFO.getInputStream();
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    initializeJavaFXProperties(ep, type, platformNameToCheck, mainClass, preloader);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (os != null) {
                            os.close();
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }

    /**
     * Adds FX specific build script jfx-impl.xml to project build system
     * @param p
     * @param dirFO
     * @param type
     * @throws IOException 
     */
    static void createJfxExtension(Project p, FileObject dirFO, WizardType type) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/JFX/jfx-impl.xml"); // NOI18N
        if (templateFO != null) {
            FileObject nbprojectFO = dirFO.getFileObject("nbproject"); // NOI18N
            FileObject jfxBuildFile = FileUtil.copyFile(templateFO, nbprojectFO, "jfx-impl"); // NOI18N
            if (type == JavaFXProjectWizardIterator.WizardType.SWING) {
                FileObject templatesFO = nbprojectFO.getFileObject("templates"); // NOI18N
                if (templatesFO == null) {
                    templatesFO = nbprojectFO.createFolder("templates"); // NOI18N
                }
                FileObject swingTemplateFO1 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplate.html"); // NOI18N
                if (swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO1, templatesFO, "FXSwingTemplate"); // NOI18N
                }
                FileObject swingTemplateFO2 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplateApplet.jnlp"); // NOI18N
                if (swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO2, templatesFO, "FXSwingTemplateApplet"); // NOI18N
                }
                FileObject swingTemplateFO3 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplateApplication.jnlp"); // NOI18N
                if (swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO3, templatesFO, "FXSwingTemplateApplication"); // NOI18N
                }
            }
            JFXProjectUtils.addExtension(p);
        }
    }

    /**
     * Adds dependencies of build-impl targets on jfx-impl targets
     * @param proj
     * @return true if success
     * @throws IOException 
     */
    static boolean addExtension(@NonNull Project proj) throws IOException {
        boolean res = false;
        FileObject projDir = proj.getProjectDirectory();
        FileObject jfxBuildFile = projDir.getFileObject(JFX_BUILD_IMPL_PATH);
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            assert jfxBuildFile != null;
            if (extender.getExtension(CURRENT_EXTENSION) == null) { // NOI18N                
                AntBuildExtender.Extension ext = extender.addExtension(CURRENT_EXTENSION, jfxBuildFile); // NOI18N
                // NOTE: change in dependencies = change of metafile updates API;
                //       do not forget to update CURRENT_EXTENSION and add the old one to OLD_EXTENSIONS
                ext.addDependency("-post-jar", "-jfx-copylibs"); // NOI18N
                ext.addDependency("-post-jar", "-rebase-libs"); //NOI18N
                ext.addDependency("-post-jar", "jfx-deployment"); //NOI18N 
                ext.addDependency("run", "jar"); //NOI18N
                ext.addDependency("debug", "jar");//NOI18N
                ext.addDependency("profile", "jar");//NOI18N
                res = true;
            }
        }
        return res;
    }

    /**
     * Remove SE native packaging extension and build script
     * @param project 
     */
    static void removeSENativeBundlerExtension(@NonNull Project project) throws IOException {
        final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            AntBuildExtender.Extension extension = extender.getExtension(J2SEDEPLOY_EXTENSION);
            if (extension != null) {
                extender.removeExtension(J2SEDEPLOY_EXTENSION);
            }
            final FileObject buildExFo = project.getProjectDirectory().getFileObject(EXTENSION_BUILD_SCRIPT_PATH);
            if (buildExFo != null) {
                buildExFo.delete();
            }
        }
    }
    
    /**
     * Modify Java Application (SE) project to become JavaFX Application, i.e.,
     * set Application class, create FX build scripts, modify properties,
     * and turn on FX mode by setting property javafx.enabled=true;
     * @param project
     * @throws IOException 
     */
    public static void switchProjectToFX(@NonNull final Project project, @NonNull final JSEApplicationClassChooser chooser) throws IOException {
        final FileObject dirFO = project.getProjectDirectory();
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                removeSENativeBundlerExtension(project);
                createJfxExtension(project, dirFO, WizardType.APPLICATION);
                ProjectManager.getDefault().saveProject(project);
                updateJFXExtension(project);
                
                //set main class
                String appClass = chooser.getSelectedExistingClass();
                if(appClass == null) {
                    FileObject dirController = chooser.getCurrentPackageFolder(true);
                    DataFolder packageDataFolder = DataFolder.findFolder(dirController);
                    String newClassName = chooser.getCurrentFileName();
                    if (packageDataFolder != null) {
                        FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXMain.java"); // NOI18N
                        DataObject dJavaTemplate = DataObject.find(javaTemplate);
                        DataObject dobj = dJavaTemplate.createFromTemplate(packageDataFolder, newClassName);
                    }
                    appClass = chooser.getPackageName() + "." + newClassName; // NOI18N
                }
                resetJavaFXProjectProperties(project, WizardType.APPLICATION, null, appClass, null); // NOI18N
                
                for (ProjectIconAnnotator annotator : Lookup.getDefault().lookupAll(ProjectIconAnnotator.class)) {
                    if(annotator instanceof JFXProjectIconAnnotator) {
                        JFXProjectIconAnnotator fxAnnotator = (JFXProjectIconAnnotator) annotator;
                        fxAnnotator.fireChange(project, true);
                    }
                }
            }
        });
        final String headerTemplate = NbBundle.getMessage(JFXProjectUtils.class, "TXT_SWITCHED_SE_TO_FX_HEADER"); //NOI18N
        final String header = MessageFormat.format(headerTemplate, new Object[] {ProjectUtils.getInformation(project).getDisplayName()});
        final String content = NbBundle.getMessage(JFXProjectUtils.class, "TXT_SWITCHED_SE_TO_FX_CONTENT"); //NOI18N
        Notification notePlatformChange = NotificationDisplayer.getDefault().notify(
                header, 
                ImageUtilities.loadImageIcon("org/netbeans/modules/javafx2/project/ui/resources/jfx_project.png", true), //NOI18N
                content, 
                null, 
                NotificationDisplayer.Priority.LOW, 
                NotificationDisplayer.Category.INFO);
        JFXProjectOpenedHook.addNotification(project, notePlatformChange);
    }
    
    /**
     * Update dependencies of build-impl targets on jfx-impl targets
     * @param project
     * @return
     * @throws IOException 
     */
    public static boolean updateJFXExtension(final Project project) throws IOException {
        boolean changed = modifyBuildXml(project);
        return changed;
    }

    /**
     * Update SE buildscript dependencies on FX buildscript
     * @param proj
     * @return
     * @throws IOException 
     */
    private static boolean modifyBuildXml(Project proj) throws IOException {
        boolean res = false;
        final FileObject buildXmlFO = getBuildXml(proj);
        if (buildXmlFO == null) {
            LOGGER.warning("The project build script does not exist, the project cannot be extended by JFX.");     //NOI18N
            return res;
        }
        Document xmlDoc = null;
        try {
            xmlDoc = XMLUtil.parse(new InputSource(buildXmlFO.toURL().toExternalForm()), false, true, null, null);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        if(!addExtension(proj)) {
            LOGGER.log(Level.INFO,
                    "Trying to include JFX build snippet in project type that doesn't support AntBuildExtender API contract."); // NOI18N
        }

        //TODO this piece shall not proceed when the upgrade to j2se-project/4 was cancelled.
        //how to figure..
        Element docElem = xmlDoc.getDocumentElement();
        NodeList nl = docElem.getElementsByTagName("target"); // NOI18N
        boolean changed = false;
        nl = docElem.getElementsByTagName("import"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            if (e.getAttribute("file") != null && JFX_BUILD_IMPL_PATH.equals(e.getAttribute("file"))) { // NOI18N
                e.getParentNode().removeChild(e);
                changed = true;
                break;
            }
        }

        if (changed) {
            final Document fdoc = xmlDoc;
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        FileLock lock = buildXmlFO.lock();
                        try {
                            OutputStream os = buildXmlFO.getOutputStream(lock);
                            try {
                                XMLUtil.write(fdoc, os, "UTF-8"); // NOI18N
                            } finally {
                                os.close();
                            }
                        } finally {
                            lock.releaseLock();
                        }
                        return null;
                    }
                });
            } catch (MutexException mex) {
                throw (IOException) mex.getException();
            }
        }
        return res;
    }

    /**
     * Get build.xml
     * @param prj
     * @return 
     */
    private static FileObject getBuildXml(final Project prj) {
        final J2SEPropertyEvaluator j2sepe = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        assert j2sepe != null;
        final PropertyEvaluator eval = j2sepe.evaluator();
        String buildScriptPath = eval.getProperty(JFXProjectProperties.BUILD_SCRIPT);
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return prj.getProjectDirectory().getFileObject (buildScriptPath);
    }

    /**
     * Update FX build script.
     * @param proj
     * @return
     * @throws IOException 
     */
    public static FileObject updateJfxImpl(final @NonNull Project proj) throws IOException {
        final FileObject projDir = proj.getProjectDirectory();
        final List<FileObject> updates = new ArrayList<FileObject>();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {        
                    projDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {        
                            if(!JFXProjectUtils.isJFXImplCurrent(proj)) {
                                FileObject updated = JFXProjectUtils.doUpdateJfxImpl(proj);
                                if(updated != null) {
                                    updates.add(updated);
                                }
                            }
                        }
                    });
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return updates.isEmpty() ? null : updates.get(0);
    }
    
    /**
     * Checks whether file nbproject/jfx-impl.xml equals current template. 
     * @param proj
     * @return
     * @throws IOException 
     */
    public static boolean isJFXImplCurrent(final @NonNull Project proj) throws IOException {
        Boolean isJfxCurrent = true;
        final FileObject projDir = proj.getProjectDirectory();
        try {
            isJfxCurrent = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws Exception {
                    FileObject jfxBuildFile = projDir.getFileObject(JFX_BUILD_IMPL_PATH); // NOI18N
                    Boolean isCurrent = false;
                    if (jfxBuildFile != null) {
                        final InputStream in = jfxBuildFile.getInputStream();
                        if(in != null) {
                            try {
                                isCurrent = isJfxImplCurrentVer(computeCrc32( in ));
                            } finally {
                                in.close();
                            }
                        }
                    }
                    return isCurrent;
                }
            });
        } catch (MutexException mux) {
            isJfxCurrent = false;
            LOGGER.log(Level.INFO, "Problem reading " + JFX_BUILD_IMPL_PATH, mux.getException()); // NOI18N
        }
        return isJfxCurrent;
    }    

    /**
     * The file nbproject/jfx-impl.xml is backed up and regenerated to the current state
     * and textual commentary is generated to UPDATED.TXT.
     * 
     * @param prj the project to check
     * @return FileObject pointing at generated UPDATED.TXT or null
     */
    static FileObject doUpdateJfxImpl(final @NonNull Project proj) throws IOException {
        FileObject returnFO = null;
        final FileObject projDir = proj.getProjectDirectory();
        try {
            returnFO = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<FileObject>() {
                @Override
                public FileObject run() throws Exception {
                    FileObject returnFO = null;
                    FileObject jfxBuildFile = projDir.getFileObject(JFX_BUILD_IMPL_PATH); // NOI18N
                    if (jfxBuildFile != null) {
                        // try to close the file just in case the file is already opened in editor
                        DataObject dobj = DataObject.find(jfxBuildFile);
                        CloseCookie closeCookie = dobj.getLookup().lookup(CloseCookie.class);
                        if (closeCookie != null) {
                            closeCookie.close();
                        }
                        closeCookie = null;
                        dobj = null;

                        final FileObject nbproject = projDir.getFileObject(NBPROJECT); //NOI18N
                        final String backupName = FileUtil.findFreeFileName(nbproject, JFX_BUILD_IMPL_NAME + "_backup", "xml"); //NOI18N
                        FileUtil.moveFile(jfxBuildFile, nbproject, backupName);
                        LOGGER.log(Level.INFO, "Old build script file " + JFX_BUILD_IMPL_NAME + ".xml has been renamed to: {0}.xml", backupName); // NOI18N
                        jfxBuildFile = null;

                        try {
                            final File readme = new File (FileUtil.toFile(nbproject), NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_NAME")); //NOI18N
                            if (!readme.exists()) {
                                readme.createNewFile();
                            }
                            final FileObject readmeFO = FileUtil.toFileObject(readme);
                            returnFO = readmeFO;
                            OutputStream os = null;
                            FileLock lock = null;
                            try {
                                lock = readmeFO.lock();
                                os = readmeFO.getOutputStream(lock);
                                final PrintWriter out = new PrintWriter ( os );
                                try {
                                    final String headerTemplate = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_CONTENT_HEADER"); //NOI18N
                                    final String header = MessageFormat.format(headerTemplate, new Object[] {ProjectUtils.getInformation(proj).getDisplayName()});
                                    char[] underline = new char[header.length()];
                                    Arrays.fill(underline, '='); // NOI18N
                                    final String content = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_CONTENT"); //NOI18N
                                    out.println(underline);
                                    out.println(header);
                                    out.println(underline);
                                    out.println (MessageFormat.format(content, new Object[] {backupName + ".xml"})); //NOI18N
                                } finally {
                                    if(out != null) {
                                        out.close ();
                                    }
                                }
                            } finally {
                                if (os != null) {
                                    os.close();
                                }
                                if (lock != null) {
                                    lock.releaseLock();
                                }
                            }
                        } catch (IOException ioe) {
                            LOGGER.log(Level.INFO, "Cannot create file readme file. ", ioe); // NOI18N
                        }        
                    }
                    if (jfxBuildFile == null) {
                        FileObject templateFO = FileUtil.getConfigFile(JFX_BUILD_TEMPLATE);
                        if (templateFO != null) {
                            FileUtil.copyFile(templateFO, projDir.getFileObject(NBPROJECT), JFX_BUILD_IMPL_NAME); // NOI18N
                            LOGGER.log(Level.INFO, "Build script " + JFX_BUILD_IMPL_NAME + ".xml has been updated to the latest version supported by this NetBeans installation."); // NOI18N
                        } 
                    }
                    return returnFO;
                } //run()
            });
        } catch(MutexException mux) {
            throw (IOException) mux.getException();
        }
        return returnFO;
    }

    /**
     * Computes CRC code of data from InputStream
     * 
     * @param is InputStream to read data from
     * @return CRC code
     */
    public static String computeCrc32(InputStream is) throws IOException {
        Checksum crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = is.read()) != -1) {
            if (curr != '\n' && last == '\r') {
                crc.update('\n');
            }
            if (curr != '\r') {
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {
            crc.update('\n');
        }
        int val = (int)crc.getValue();
        String hex = Integer.toHexString(val);
        while (hex.length() < 8) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }

    /**
     * Checks whether crc is the CRC code of current jfx-impl.xml template.
     * 
     * @param crc code to be compared against
     * @return true if crc is the CRC code of current jfx-impl.xml template.
     */
    public static boolean isJfxImplCurrentVer(String crc) throws IOException {
        String _currentJfxImplCRC = currentJfxImplCRCCache;
        if (_currentJfxImplCRC == null) {
            final FileObject template = FileUtil.getConfigFile(JFX_BUILD_TEMPLATE);
            final InputStream in = template.getInputStream();
            if(in != null) {
                try {
                    currentJfxImplCRCCache = _currentJfxImplCRC = computeCrc32(in);
                } finally {
                    in.close();
                }
            }
        }
        return _currentJfxImplCRC.equals(crc);
    }

    public static boolean isProjectOpen(@NonNull Project project) {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        if(projects != null) {
            for(Project p : Arrays.asList(projects)) {
                if(p.getProjectDirectory().getPath().equals(project.getProjectDirectory().getPath())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Updates project.properties so that if JFXRT artifacts are explicitly added to
     * compile classpath if they are not on classpath by default. This is a workaround
     * of the fact that JDK1.7 does contain FX RT but does not provide it on classpath.
     * JDK1.8 has FX RT on classpath, but still may not include all relevant artifacts by default
     * and may need this extension.
     * Note that this extension is relevant not only for FX Application projects, but also
     * for SE projects that have the property "keep.javafx.runtime.on.classpath" set 
     * (see SE Deployment category in Project Properties dialog).
     * 
     * @param prj the project to update
     */
    public static void updateClassPathExtension(@NonNull final Project project) throws IOException {
        final boolean hasDefaultJavaFXPlatform[] = new boolean[1];
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if(projPropsFO != null) {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        final Lookup lookup = project.getLookup();
                        final J2SEPropertyEvaluator eval = lookup.lookup(J2SEPropertyEvaluator.class);
                        if (eval != null) {
                            // if project has Default_JavaFX_Platform, change it to default Java Platform
                            String platformName = eval.evaluator().getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
                            hasDefaultJavaFXPlatform[0] = JFXProjectProperties.isEqual(platformName, JavaFXPlatformUtils.DEFAULT_JAVAFX_PLATFORM);
                        }
                        if(hasDefaultJavaFXPlatform[0]) {
                            final J2SEProjectPlatform platformSetter = lookup.lookup(J2SEProjectPlatform.class);
                            if(platformSetter != null) {
                                platformSetter.setProjectPlatform(JavaPlatformManager.getDefault().getDefaultPlatform());
                            }
                        }
                        try (InputStream is = projPropsFO.getInputStream()) {
                            ep.load(is);
                        }
                        boolean cpExtUpdated = updateClassPathExtensionProperties(ep);
                        if (cpExtUpdated) {
                            final FileLock lock = projPropsFO.lock();
                            try (OutputStream os =projPropsFO.getOutputStream(lock)) {
                                ep.store(os);
                            } finally {                                                                
                                lock.releaseLock();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }
            if(hasDefaultJavaFXPlatform[0]) {
                final String headerTemplate = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_DEFAULT_PLATFORM_HEADER"); //NOI18N
                final String header = MessageFormat.format(headerTemplate, new Object[] {ProjectUtils.getInformation(project).getDisplayName()});
                final String content = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_DEFAULT_PLATFORM_CONTENT"); //NOI18N
                Notification notePlatformChange = NotificationDisplayer.getDefault().notify(
                        header, 
                        ImageUtilities.loadImageIcon("org/netbeans/modules/javafx2/project/ui/resources/jfx_project.png", true), //NOI18N
                        content, 
                        null, 
                        NotificationDisplayer.Priority.LOW, 
                        NotificationDisplayer.Category.INFO);
                JFXProjectOpenedHook.addNotification(project, notePlatformChange);
            }
        } else {
            LOGGER.warning("Project metafiles inaccessible - classpath extension could not be verified and updated if needed."); //NOI18N
        }
    }

    /**
     * Update main class property in project metafiles in the current configuration
     * @param project
     * @param mainClass
     * @throws IOException 
     */
    public static void updatePropertyInActiveConfig(@NonNull final Project project, @NonNull final String propName, @NonNull final String propValue) throws IOException {
        final FileObject projectDir = project.getProjectDirectory();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final EditableProperties cep = new EditableProperties(true);
                    final FileObject configFO = projectDir.getFileObject(JFXProjectConfigurations.CONFIG_PROPERTIES_FILE);
                    if(configFO != null) {
                        final InputStream cis = configFO.getInputStream();
                        try {
                            cep.load(cis);
                        } finally {
                            if (cis != null) {
                                cis.close();
                            }
                        }
                    }
                    String config = cep.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
                    
                    final FileObject projPropsFO = config == null ? projectDir.getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH) :
                            projectDir.getFileObject(JFXProjectConfigurations.PROJECT_CONFIGS_DIR + 
                            "/" + config + "." + JFXProjectConfigurations.PROPERTIES_FILE_EXT);  //NOI18N
                    assert projPropsFO != null : "Inaccessible file " + JFXProjectConfigurations.PROJECT_CONFIGS_DIR +  //NOI18N
                            "/" + config + "." + JFXProjectConfigurations.PROPERTIES_FILE_EXT;  //NOI18N
                    final InputStream is = projPropsFO.getInputStream();
                    final EditableProperties ep = new EditableProperties(true);
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    ep.setProperty(propName, propValue);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (os != null) {
                            os.close();
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }

    /**
     * Create array of Strings representing path artifacts that can be set to path property;
     * all but the last String gets : appended.
     * @param artifacts
     * @return array of artifacts
     */
    public static String[] getPaths(@NonNull final Collection<String> artifacts) {
        List<String> l = new ArrayList<String>();
        Iterator<String> i = artifacts.iterator();
        while(i.hasNext()) {
            String s = i.next();
            if(i.hasNext()) {
                if(s.endsWith(":")) { //NOI18N
                    l.add(s);
                } else {
                    l.add(s + ":"); //NOI18N
                }
            } else {
                if(s.endsWith(":")) { //NOI18N
                    l.add(s.substring(0, s.length()-1));
                } else {
                    l.add(s);
                }
            }
        }
        return l.toArray(new String[0]);
    }
    
    /**
     * Remove trailing : from array of path artifacts and return the array as collection
     * @param artifacts
     * @return 
     */
    @NonNull
    public static Set<String> getPaths(@NonNull final String[] artifacts) {        
        final Set<String> l = new LinkedHashSet<>();
        for(String artifact : artifacts) {
            if(artifact.endsWith(":")) { //NOI18N
                l.add(artifact.substring(0, artifact.length()-1));
            } else {
                l.add(artifact);
            }
        }
        return l;
    }
    
    /**
     * Filter out artifacts that contain subString
     * @param artifacts
     * @return set without artifacts that contain subString
     */
    private static Set<String> filterOutArtifacts(@NonNull final Collection<String> artifacts, @NonNull String subString) {
        Set<String> result = new LinkedHashSet<String>();
        for(String artifact : artifacts) {
            if(!artifact.contains(subString)) {
                result.add(artifact);
            }
        }
        return result;
    }
    
    /**
     * Returns existing value of a path property, null if it does not exist
     * @param ep EditableProperties
     * @return collection of artifacts or null
     */
    @CheckForNull
    public static Set<String> getExistingProperty(@NonNull final EditableProperties ep, @NonNull String propName) {
        // existing 
        String currentPropVal = ep.getProperty(propName);
        if(currentPropVal != null) {
            return Collections.unmodifiableSet(getPaths(PropertyUtils.tokenizePath(currentPropVal)));
        }
        return null;
    }
    
    /**
     * Returns updated value of classpath property, null if it should not exist
     * @param ep EditableProperties
     * @return collection of artifacts or null
     */
    public static Set<String> getUpdatedCPProperty(@NonNull final EditableProperties ep, boolean extensionPropertyEmpty) {
        boolean extensionNeeded = JFXProjectProperties.isTrue(ep.getProperty(JFXProjectProperties.JAVAFX_ENABLED)) ||
                JFXProjectProperties.isTrue(ep.getProperty(JFXProjectProperties.JAVASE_KEEP_JFXRT_ON_CLASSPATH));
        // existing
        Set<String> existing = getExistingProperty(ep, ProjectProperties.JAVAC_CLASSPATH);
        Set<String> updated = new LinkedHashSet<String>();
        if(existing != null) {
            updated = filterOutArtifacts(existing, "${javafx.runtime}"); // NOI18N
            updated = filterOutArtifacts(updated, JavaFXPlatformUtils.getClassPathExtensionProperty());
        }
        if(extensionNeeded && !extensionPropertyEmpty) {
            updated.add(JavaFXPlatformUtils.getClassPathExtensionProperty());
        }
        return Collections.unmodifiableSet(updated);
    }

    /**
     * Removes the path entry from path.
     * @param path to remove the netry from
     * @param toRemove the entry to be rmeoved from the path
     * @return new path with removed entry
     */
    @NonNull
    public static String removeFromPath(@NonNull final String path, @NonNull final String toRemove) {
        Parameters.notNull("path", path);   //NOI18N
        Parameters.notNull("toRemove", toRemove); //NOI18N
        final StringBuilder sb = new StringBuilder();
        for (String entry : PropertyUtils.tokenizePath(path)) {
            if (toRemove.equals(entry)) {
                continue;
            }
            sb.append(entry);
            sb.append(':'); //NOI18N
        }
        return sb.length() == 0 ?
            sb.toString() :
            sb.substring(0, sb.length()-1);
    }

    /**
     * Returns new value of FX artifacts extension property if it needs to be updated, null otherwise
     * @param ep EditableProperties
     * @return collection of artifacts or null
     */
    public static Set<String> getUpdatedExtensionProperty(@NonNull final EditableProperties ep) throws IllegalArgumentException {
        boolean propertyNeeded = JFXProjectProperties.isTrue(ep.getProperty(JFXProjectProperties.JAVAFX_ENABLED)) ||
                JFXProjectProperties.isTrue(ep.getProperty(JFXProjectProperties.JAVASE_KEEP_JFXRT_ON_CLASSPATH));
        // expected
        Set<String> updated = null;
        if(propertyNeeded) {
            String platformName = ep.getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
            JavaPlatform platform = JavaFXPlatformUtils.findJavaPlatform(platformName);
            if(platform == null) {
                // platform does not exist, thus no extension property update is to take place
                return null;
            }
            updated = JavaFxRuntimeInclusion.getProjectClassPathExtension(platform);
        }
        return updated == null ? null : Collections.unmodifiableSet(updated);
    }

    /**
     * Compares two Set representations of a path property
     * @param set1
     * @param set2
     * @return true if the two sets represent equal existence and values of path properties
     */
    public static boolean pathPropertiesEqual(Set<String> set1, Set<String> set2) {
        if(set1 == null && set2 == null) {
            return true;
        }
        if(set1 == null || set2 == null) {
            return false;
        }
        return set1.containsAll(set2) && set2.containsAll(set1);
    }
    
    /**
     * Checks whether JFXRT artifacts are properly added to classpath and represented 
     * in project.properties if the current Java Platform needs this workaround
     * @param prj the project to check
     * @return true if project properties correctly represent JFXRT artifacts
     */
    public static boolean hasCorrectClassPathExtension(@NonNull final Project project) throws IOException, IllegalArgumentException  {
        final EditableProperties ep = readFromFile(
            project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH)
                );
        String platformName = ep.getProperty(JFXProjectProperties.PLATFORM_ACTIVE);
        if(JFXProjectProperties.isEqual(platformName, JavaFXPlatformUtils.DEFAULT_JAVAFX_PLATFORM)) {
            // request auto-replace of Default_JavaFX_Platform by default platform
            return false;
        }
        Set<String> existingExt = getExistingProperty(ep, JavaFXPlatformUtils.JAVAFX_CLASSPATH_EXTENSION);
        Set<String> updatedExt = getUpdatedExtensionProperty(ep);
        Set<String> existingCP = getExistingProperty(ep, ProjectProperties.JAVAC_CLASSPATH);
        Set<String> updatedCP = getUpdatedCPProperty(ep, updatedExt == null || updatedExt.isEmpty());
        return pathPropertiesEqual(existingExt, updatedExt) && pathPropertiesEqual(existingCP, updatedCP);
    }
    
    /**
     * Updates EditableProperties so that JFXRT artifacts are explicitly added to
     * compile classpath if they are not on classpath by default. This is a workaround
     * of the fact that JDK1.7 does contain FX RT but does not provide it on classpath.
     * JDK1.8 has FX RT on classpath, but still may not include all relevant artifacts by default
     * and may need this extension.
     * Note that this extension is relevant not only for FX Application projects, but also
     * for SE projects that have the property "keep.javafx.runtime.on.classpath" set 
     * (see SE Deployment category in Project Properties dialog).
     * 
     * @param ep EditableProperties containing properties to be updated
     */
    public static boolean updateClassPathExtensionProperties(@NonNull final EditableProperties ep) {
        boolean changed = false;
        changed = ep.remove(PROPERTY_JAVAFX_RUNTIME) != null ? true : changed;
        changed = ep.remove(PROPERTY_JAVAFX_SDK) != null ? true : changed;
        Collection<String> extendExtProp = getUpdatedExtensionProperty(ep);
        if(extendExtProp != null && !extendExtProp.isEmpty()) {
            Collection<String> currentCpExt = getExistingProperty(ep, JavaFXPlatformUtils.JAVAFX_CLASSPATH_EXTENSION);
            if (!extendExtProp.equals(currentCpExt)) {
                //update javafx.classpath.extension only in case that current value doesn't contain required jars
                ep.setProperty(JavaFXPlatformUtils.JAVAFX_CLASSPATH_EXTENSION, getPaths(extendExtProp));
                changed = true;
            }
        } else {
            changed = ep.remove(JavaFXPlatformUtils.JAVAFX_CLASSPATH_EXTENSION) != null ? true : changed;
            //ep.remove(JFXProjectProperties.JAVASE_KEEP_JFXRT_ON_CLASSPATH);
        }
        Collection<String> extendCPProp = getUpdatedCPProperty(ep, extendExtProp == null || extendExtProp.isEmpty());
        // JAVAC_CLASSPATH to be preserved even if empty (create new project creates it empty by default)
        if(extendCPProp != null) {
            Collection<String> currentJavacCp = getExistingProperty(ep, ProjectProperties.JAVAC_CLASSPATH);
            if (!extendCPProp.equals(currentJavacCp)) {
                ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, getPaths(extendCPProp));
                changed = true;
            }
        }
        // Remove JavaFX endorsed.classpath entries if they are present (#235380, see also #214386)
        final String endorsedCp = ep.get(ProjectProperties.ENDORSED_CLASSPATH);
        if (endorsedCp != null && !endorsedCp.isEmpty()) {
            String[] cpElements = PropertyUtils.tokenizePath(endorsedCp);
            List<String> updatedEndorsedCpList = new ArrayList<String>();
            for (String element : cpElements) {
                if (!element.startsWith("${javafx.runtime}/")) { //NOI18N
                    updatedEndorsedCpList.add(element);
                }
            }
            String[] updatedEndorsedCp = updatedEndorsedCpList.toArray(new String[0]);
            for (int i = 0; i < updatedEndorsedCp.length - 1; i++) {
                updatedEndorsedCp[i] += ":"; //NOI18N
            }
            ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, updatedEndorsedCp);
            changed = true;
        }
        return changed;
    }

    /**
     * Checks the existence of default configuration for given RUN_AS type.
     * If it does not exist, it is created.
     * 
     * @param proj the project to check
     * @param runAs run type whose default configuration is to be updated
     * @param setBrowserProps if true, adds properties representing an existing browser
     * @return true is any update took place
     */
    public static boolean updateDefaultRunAsConfigFile(final @NonNull FileObject projDir, JFXProjectProperties.RunAsType runAs, boolean setBrowserProps) throws IOException {
        boolean updated = false;
        String configName = runAs.getDefaultConfig();
        String configFile = makeSafe(configName);
        String sharedPath = JFXProjectConfigurations.getSharedConfigFilePath(configFile);
        FileObject sharedCfgFO = projDir.getFileObject(sharedPath);
        final EditableProperties sharedCfgProps = sharedCfgFO != null ?
                readFromFile(sharedCfgFO) : new EditableProperties(true);
        assert sharedCfgProps != null;
        if(sharedCfgProps.isEmpty()) {
            sharedCfgProps.setProperty("$label", configName); // NOI18N
            sharedCfgProps.setComment("$label", new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_run_as_defaults")}, false); // NOI18N
            saveToFile(projDir, sharedPath, sharedCfgProps);
            updated = true;
        }
        String privatePath = JFXProjectConfigurations.getPrivateConfigFilePath(configFile);
        FileObject privateCfgFO = projDir.getFileObject(privatePath);
        final EditableProperties privateCfgProps = privateCfgFO != null ?
                readFromFile(projDir, privatePath) : new EditableProperties(true);
        assert privateCfgProps != null;
        if(privateCfgProps.isEmpty() || setBrowserProps) {
            privateCfgProps.setProperty(JFXProjectProperties.RUN_AS, runAs.getString());
            privateCfgProps.setComment(JFXProjectProperties.RUN_AS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_run_as_defaults")}, false); // NOI18N
            if(setBrowserProps) {
                Map<String,String> browserInfo = getDefaultBrowserInfo();
                if(browserInfo != null && !browserInfo.isEmpty()) {
                    for(Map.Entry<String,String> entry : browserInfo.entrySet()) {
                        privateCfgProps.setProperty(JFXProjectProperties.RUN_IN_BROWSER, entry.getKey());
                        privateCfgProps.setProperty(JFXProjectProperties.RUN_IN_BROWSER_PATH, entry.getValue());
                        break;
                    }
                }
            }
            saveToFile(projDir, privatePath, privateCfgProps);       
            updated = true;
        }
        return updated;
    }

    /**
     * @return name and path to default browser
     */
    public static Map<String,String> getDefaultBrowserInfo() {
        Lookup.Result<ExtWebBrowser> allBrowsers = Lookup.getDefault().lookupResult(ExtWebBrowser.class);
        Map<String,String> browserPaths = new HashMap<String, String>();
        for(Lookup.Item<ExtWebBrowser> browser : allBrowsers.allItems()) {
            String name = browser.getDisplayName();
            if(name != null && name.toLowerCase().contains("default")) { // NOI18N
                NbProcessDescriptor proc = browser.getInstance().getBrowserExecutable();
                String path = proc.getProcessName();
                if(JFXProjectProperties.isNonEmpty(path)) {
                    browserPaths.put(name, path);
                }
                break;
            }
        }
        return browserPaths;
    }
    
    /**
     * Returns a copy of existing list of maps, usually used to store application parameters
     * 
     * @param list2Copy
     * @return copy of list2Copy
     */
    public static List<Map<String,String>> copyList(List<Map<String,String>> list2Copy) {
        List<Map<String,String>> list2Return = new ArrayList<Map<String,String>>();
        if(list2Copy != null ) {
            for (Map<String,String> map : list2Copy) {
                list2Return.add(copyMap(map));
            }
        }
        return list2Return;
    }

    /**
     * Returns a copy of existing string map, usually used to store application parameter
     * (keys 'name' and 'value')
     * 
     * @param map2Copy
     * @return copy of map2Copy
     */
    public static Map<String,String> copyMap(Map<String,String> map2Copy) {
        Map<String,String> newMap = new HashMap<String,String>();
        if(map2Copy != null) {
            for(Map.Entry<String, String> entry : map2Copy.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                newMap.put(key, value);
            }
        }
        return newMap;
    }

    /**
     * Modifies name so that it can be used as file name,
     * i.e., replaces problematic characters.
     * 
     * @param name
     * @return modified name usable as file name
     */
    public static String makeSafe(@NonNull String name) {
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N;
    }

    public static EditableProperties readFromFile(final @NonNull Project project, final @NonNull String relativePath) throws IOException {
        final FileObject dirFO = project.getProjectDirectory();
        return readFromFile(dirFO, relativePath);
    }

    public static EditableProperties readFromFile(final @NonNull FileObject dirFO, final @NonNull String relativePath) throws IOException {
        assert dirFO.isFolder();
        final FileObject propsFO = dirFO.getFileObject(relativePath);
        return readFromFile(propsFO);
    }

    public static EditableProperties readFromFile(final @NonNull FileObject propsFO) throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        if(propsFO != null) {
            assert propsFO.isData();
            try {
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        final InputStream is = propsFO.getInputStream();
                        try {
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }
        }
        return ep;
    }

    public static void deleteFile(final @NonNull Project project, final @NonNull String relativePath) throws IOException {
        final FileObject propsFO = project.getProjectDirectory().getFileObject(relativePath);
        deleteFile(propsFO);
    }
    
    public static void deleteFile(final @NonNull FileObject dirFO, final @NonNull String relativePath) throws IOException {
        assert dirFO.isFolder();
        final FileObject propsFO = dirFO.getFileObject(relativePath);
        deleteFile(propsFO);
    }

    public static void deleteFile(final @NonNull FileObject propsFO) throws IOException {
        if(propsFO != null) {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        propsFO.delete();
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }       
        }
    }

    public static void saveToFile(final @NonNull Project project, final @NonNull String relativePath, final @NonNull EditableProperties ep) throws IOException {
        FileObject dirFO = project.getProjectDirectory();
        saveToFile(dirFO, relativePath, ep);
    }
    
    public static void saveToFile(final @NonNull FileObject dirFO, final @NonNull String relativePath, final @NonNull EditableProperties ep) throws IOException {
        assert dirFO.isFolder();
        FileObject f = dirFO.getFileObject(relativePath);
        final FileObject propsFO;
        if(f == null) {
            propsFO = FileUtil.createData(dirFO, relativePath);
            assert propsFO != null : "FU.cD must not return null; called on " + dirFO + " + " + relativePath; // #50802  // NOI18N
        } else {
            propsFO = f;
        }
        saveToFile(propsFO, ep);
    }
    
    public static void saveToFile(final @NonNull FileObject propsFO, final @NonNull EditableProperties ep) throws IOException {
        if(propsFO != null) {
            assert propsFO.isData();
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        OutputStream os = null;
                        FileLock lock = null;
                        try {
                            lock = propsFO.lock();
                            os = propsFO.getOutputStream(lock);
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }
        }
    }

    private static final String MODULE_INFO = "module-info.java"; // NOI18N

    // as in org.netbeans.modules.maven.api.ModuleInfoUtils.hasModuleInfoInSource;
    public static boolean hasModuleInfo(SourceGroupSupport support) {
        for (SourceGroupProxy sourceGroup : support.getSourceGroups()) {
            if(sourceGroup.getRootFolder().getFileObject(MODULE_INFO) != null) {
                return true;
            }
        }
        return false;
    }
    
}
