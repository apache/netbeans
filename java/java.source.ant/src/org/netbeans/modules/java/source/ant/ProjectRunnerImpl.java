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

package org.netbeans.modules.java.source.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.spi.java.project.runner.JavaRunnerImplementation;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.windows.InputOutput;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import static org.netbeans.api.java.project.runner.JavaRunner.*;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Union2;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.project.runner.JavaRunnerImplementation.class)
public class ProjectRunnerImpl implements JavaRunnerImplementation {

    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9"); //NOI18N
    private static final Runnable NOP = () -> {};
    private static final RequestProcessor RP = new RequestProcessor(ProjectRunnerImpl.class);
    
    public boolean isSupported(String command, Map<String, ?> properties) {
        return locateScript(command) != null;
    }

    @Override
    public ExecutorTask execute(final String command, final Map<String, ?> properties) throws IOException {
        if (QUICK_CLEAN.equals(command)) {
            return clean(properties);
        }
        final Work work = new Work(command, properties);
        final ExecutorTask res = new WrapperTask(work);
        work.setCallback(res);
        RP.execute(work);
        return res;
    }

    static Map<String,String> computeProperties(String command, Map<String, ?> properties, String[] projectNameOut) {
        properties = new HashMap<String, Object>(properties);
        FileObject toRun = getValue(properties, PROP_EXECUTE_FILE, FileObject.class);
        String workDir = getValue(properties, PROP_WORK_DIR, String.class);
        String className = getValue(properties, PROP_CLASSNAME, String.class);
        ClassPath boot = getValue(properties, "boot.classpath", ClassPath.class);
        ClassPath exec = getValue(properties, PROP_EXECUTE_CLASSPATH, ClassPath.class);
        ClassPath execModule = getValue(properties, PROP_EXECUTE_MODULEPATH, ClassPath.class);
        String javaTool = getValue(properties, PROP_PLATFORM_JAVA, String.class);
        String projectName = getValue(properties, PROP_PROJECT_NAME, String.class);
        Iterable<String> args = getMultiValue(properties, PROP_APPLICATION_ARGS, String.class);
        final String tmpDir = getValue(properties, "tmp.dir", String.class);  //NOI18N
        final Boolean javaFailOnError = getValue(properties, "java.failonerror", Boolean.class);    //NOI18N
        if (workDir == null) {
            Parameters.notNull(PROP_EXECUTE_FILE + " or " + PROP_WORK_DIR, toRun);
            Project project = FileOwnerQuery.getOwner(toRun);
            if (project != null) {
                //NOI18N
                FileObject projDirectory = project.getProjectDirectory();
                assert projDirectory != null;
                File file = FileUtil.toFile(projDirectory);
                if (file != null) {
                    workDir = file.getAbsolutePath(); //NOI18N
                }
            }
        } else if (!new File(workDir).isDirectory()) {
            IllegalArgumentException iae = new IllegalArgumentException("The work dir is not a folder.");
            Exceptions.attachLocalizedMessage(iae, NbBundle.getMessage(ProjectRunnerImpl.class, "ERR_NoWorkDir"));
            throw iae;
        }
        if (className == null) {
            Parameters.notNull(PROP_EXECUTE_FILE + " or " + PROP_CLASSNAME, toRun);
            ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);
            if (source == null) {
                throw new IllegalArgumentException("The source classpath for specified toRun parameter has is null. " +
                        "Report against caller module. [toRun = " + toRun + "]");
            }
            className = source.getResourceName(toRun, '.', false);
        }
        if (exec == null) {
            Parameters.notNull(PROP_EXECUTE_FILE + " or " + PROP_EXECUTE_CLASSPATH, toRun);
            final String sl = SourceLevelQuery.getSourceLevel(toRun);
            if (sl != null && JDK9.compareTo(new SpecificationVersion(sl)) <= 0) {
                exec = ClassPath.getClassPath(toRun, JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH);
                if (execModule == null) {
                    execModule = ClassPath.getClassPath(toRun, JavaClassPathConstants.MODULE_EXECUTE_PATH);
                }
            } else {
                exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
                execModule = ClassPath.EMPTY;
            }
        } else {
            if (execModule == null) {
                execModule = ClassPath.EMPTY;
            }
        }
        JavaPlatform p = getValue(properties, PROP_PLATFORM, JavaPlatform.class);

        if (p == null) {
            p = JavaPlatform.getDefault();
        }
        if (javaTool == null) {
            
            FileObject javaToolFO = p.findTool("java");

            if (javaToolFO == null) {
                IllegalArgumentException iae = new IllegalArgumentException("Cannot find java");

                Exceptions.attachLocalizedMessage(iae, NbBundle.getMessage(ProjectRunnerImpl.class, "ERR_CannotFindJava"));
                throw iae;
            }
            
            javaTool = FileUtil.toFile(javaToolFO).getAbsolutePath();
        }
        if (boot == null) {
            boot = p.getBootstrapLibraries();
        }
        Project project = getValue(properties, "project", Project.class);
        if (project == null && toRun != null) {
            project = FileOwnerQuery.getOwner(toRun);
        }
        if (project == null && workDir != null) {
            FileObject d = FileUtil.toFileObject(FileUtil.normalizeFile(new File(workDir)));
            if (d != null) {
                try {
                    project = ProjectManager.getDefault().findProject(d);
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
        if (projectName == null) {
            if (project != null) {
                projectName = ProjectUtils.getInformation(project).getDisplayName();
            } else {
                projectName = "";
            }
        }
        List<String> runJVMArgs = new ArrayList<String>(getMultiValue(properties, PROP_RUN_JVMARGS, String.class));
        StartupExtender.StartMode mode;
        if (command.equals(QUICK_RUN)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(QUICK_DEBUG)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(QUICK_TEST)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(QUICK_TEST_DEBUG)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else {
            mode = null;
        }
        if (mode != null) {
            InstanceContent ic = new InstanceContent();
            if (project != null) {
                ic.add(project);
            }
            if (p != null) {
                ic.add(p);
            }
            Lookup l = new AbstractLookup(ic);
            for (StartupExtender group : StartupExtender.getExtenders(l, mode)) {
                runJVMArgs.addAll(group.getArguments());
            }
        }

        LOG.log(Level.FINE, "execute classpath={0}", exec);
        Map<String,String> antProps = new TreeMap<String,String>();
        setProperty(antProps, "platform.bootcp", boot.toString(ClassPath.PathConversionMode.FAIL, ClassPath.PathEmbeddingMode.INCLUDE));
        setProperty(antProps, "classpath", pathToString(exec));
        setProperty(antProps, "classname", className);
        setProperty(antProps, "platform.java", javaTool);
        setProperty(antProps, "work.dir", workDir);
        setProperty(antProps, "run.jvmargs", toOneLine(runJVMArgs));
        setProperty(antProps, "run.jvmargs.ide", (String)properties.get("run.jvmargs.ide"));
        if (tmpDir != null) {
            setProperty(antProps, "tmp.dir", tmpDir);   //NOI18N
        }
        if (toRun == null) {
            // #152881 - pass arguments only if not run single
            setProperty(antProps, "application.args", toOneLine(args));
        }
        if (javaFailOnError != null) {
            setProperty(antProps, "java.failonerror", javaFailOnError.toString());  //NOI18N
        }
        {
            //Encoding            
            final Charset charset = getValue(properties, JavaRunner.PROP_RUNTIME_ENCODING, Charset.class);   //NOI18N
            String encoding = charset != null && Charset.isSupported(charset.name()) ? charset.name() : null;
            if (encoding == null) {
                FileObject source = toRun;
                if (source == null) {
                    source = findSource(className, exec, execModule);
                }
                if (source != null) {
                    Charset sourceEncoding = FileEncodingQuery.getEncoding(source);
                    if (Charset.isSupported(sourceEncoding.name())) {
                        encoding = sourceEncoding.name();
                    }
                }
            }
            if (encoding == null) {
                //Encoding still null => fallback to UTF-8
                encoding = "UTF-8"; //NOI18N
            }            
            setProperty(antProps, "encoding", encoding);
        }
        {
            //Modules
            boolean modulesSupported = false;
            final String classNameFin = className;
            final ClassPath execFin = exec, execModuleFin = execModule;
            final Pair<URL,FileObject[]> binSrcRoots = Optional.ofNullable(toRun)
                    .map((src) -> {
                        final ClassPath scp = ClassPath.getClassPath(src, ClassPath.SOURCE);
                        return scp != null ?
                                scp.findOwnerRoot(src) :
                                null;
                    })
                    .map((srcRoot) -> {
                        final URL[] brts = removeArchives(BinaryForSourceQuery.findBinaryRoots(srcRoot.toURL()).getRoots());
                        switch (brts.length) {
                            case 0:
                                return null;
                            case 1:
                                return Pair.of(brts[0], new FileObject[]{srcRoot});
                            default:
                                final ClassPath bcp = ClassPathSupport.createClassPath(brts);
                                final FileObject bcpOwner = findOwnerRoot(classNameFin, new String[]{FileObjects.CLASS}, bcp);
                                return bcpOwner != null ?
                                        Pair.of(bcpOwner.toURL(), new FileObject[]{srcRoot}) :
                                        null;
                        }
                    })
                    .orElseGet(() -> {
                        final Map<URL,Pair<URL,FileObject[]>> dictionary = new HashMap<>();
                        final ClassPath translatedExec = translate(execFin, dictionary);
                        final ClassPath translatedExecModule = translate(execModuleFin, dictionary);
                        final FileObject bcpOwner = findOwnerRoot(
                                classNameFin,
                                new String[] {
                                    FileObjects.SIG,
                                    FileObjects.CLASS
                                },
                                translatedExec,
                                translatedExecModule);
                        if (bcpOwner == null) {
                            return null;
                        }
                        return dictionary.get(bcpOwner.toURL());
                    });
            if (binSrcRoots != null) {
                FileObject slFo;
                if (toRun != null) {
                    slFo = toRun;
                } else if (binSrcRoots.second().length > 0) {
                    slFo = binSrcRoots.second()[0];
                } else if ((slFo = URLMapper.findFileObject(binSrcRoots.first())) != null) {
                } else if (project != null) {
                    slFo = project.getProjectDirectory();
                } else {
                    slFo = null;
                }
                final String sl = slFo != null ?
                        SourceLevelQuery.getSourceLevel(slFo) :
                        null;
                if (sl != null && JDK9.compareTo(new SpecificationVersion(sl)) <= 0) {
                    modulesSupported = true;
                    URL mainBinRoot = null;
                    if (binSrcRoots.second().length > 0) {
                        URL[] mainRootUrls = UnitTestForSourceQuery.findSources(binSrcRoots.second()[0]);
                        if (mainRootUrls.length > 0) {
                            URL[] mainBinRoots = removeArchives(BinaryForSourceQuery.findBinaryRoots(mainRootUrls[0]).getRoots());
                            switch (mainBinRoots.length) {
                                case 0:
                                    break;
                                case 1:
                                     mainBinRoot = mainBinRoots[0];
                                     break;
                                default:
                                     final ClassPath bcp = ClassPathSupport.createClassPath(mainBinRoots);
                                     final FileObject bcpOwner = findOwnerRoot("module-info", new String[] {FileObjects.CLASS}, bcp);
                                     if (bcpOwner != null) {
                                         mainBinRoot = bcpOwner.toURL();
                                     } else {
                                         final FileObject[] brs = bcp.getRoots();
                                         mainBinRoot = brs.length == 0 ?
                                                 null :
                                                 brs[0].toURL();
                                     }
                            }
                        }
                    }
                    setProperty(antProps, "modules.supported.internal", "true");
                    try {
                        setProperty(antProps, "module.root",
                                BaseUtilities.toFile(binSrcRoots.first().toURI()).getAbsolutePath());
                    } catch (URISyntaxException e) {
                        LOG.log(
                                Level.WARNING,
                                "Non local target folder:{0}",  //NOI18N
                                binSrcRoots.first());
                    }
                    final String moduleName = getModuleName(binSrcRoots.first(), binSrcRoots.second());
                    final String mainModuleName = getModuleName(mainBinRoot);
                    if (moduleName != null) {
                        setProperty(antProps, "module.name", moduleName);
                        setProperty(antProps, "named.module.internal", "true");
                    } else {
                        setProperty(antProps, "unnamed.module.internal", "true");
                    }
                    if (mainBinRoot != null) {
                        if (mainModuleName != null) {
                            setProperty(antProps, "related.module.name", mainModuleName);
                        }
                    }
                }
            }
            if (modulesSupported || !execModule.entries().isEmpty()) {
                //When execModule is set explicitelly pass it to script even when modules are not supported
                setProperty(antProps, "modulepath", pathToString(execModule));
            }
        }

        for (Entry<String, ?> e : properties.entrySet()) {
            if (e.getValue() instanceof String) {
                antProps.put(e.getKey(), (String) e.getValue());
            }
        }

        projectNameOut[0] = projectName;

        return antProps;
    }

    @NonNull
    private static String pathToString(@NonNull final ClassPath path) {
        final StringBuilder cpBuilder = new StringBuilder();
        for (ClassPath.Entry entry : path.entries()) {
            final URL u = entry.getURL();
            boolean folder = "file".equals(u.getProtocol());
            File f = FileUtil.archiveOrDirForURL(u);
            if (f != null) {
                if (cpBuilder.length() > 0) {
                    cpBuilder.append(File.pathSeparatorChar);
                }
                cpBuilder.append(f.getAbsolutePath());
                if (folder) {
                    cpBuilder.append(File.separatorChar);
                }
            }
        }
        return cpBuilder.toString();
    }

    @CheckForNull
    private static FileObject findSource(
        @NonNull final String className,
        @NonNull final ClassPath... binCps) {
        final FileObject[] srcRoots = Optional.ofNullable(findOwnerRoot(className, new String[] {FileObjects.CLASS}, binCps))
                .map((root) -> SourceForBinaryQuery.findSourceRoots(root.toURL()).getRoots())
                .orElse(new FileObject[0]);
        final String sourceResource = className.replace('.', '/') + ".java";  //NOI18N
        for (FileObject srcRoot : srcRoots) {
            final FileObject srcFile = srcRoot.getFileObject(sourceResource);
            if (srcFile != null) {
                return srcFile;
            }
        }
        return null;
    }
    
    @NonNull
    private static ClassPath translate(
            @NonNull final ClassPath cp,
            @NonNull final Map<URL,Pair<URL,FileObject[]>> dictionary) {
        final List<URL> roots = new ArrayList<>(cp.entries().size());
        for (ClassPath.Entry e : cp.entries()) {
            final URL orig = e.getURL();
            final SourceForBinaryQuery.Result2 res = SourceForBinaryQuery.findSourceRoots2(orig);
            if (res.preferSources()) {
                final FileObject[] srcs = res.getRoots();
                for (FileObject src : srcs) {
                    try {
                        final URL cacheURL = BaseUtilities.toURI(
                                JavaIndex.getClassFolder(src.toURL())).toURL();
                        dictionary.put(cacheURL,Pair.of(orig,res.getRoots()));
                        roots.add(cacheURL);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            } else {
                dictionary.put(orig, Pair.of(orig,res.getRoots()));
                roots.add(orig);
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
    }
    
    @CheckForNull
    private static FileObject findOwnerRoot(
        @NonNull final String className,
        @NonNull final String[] extensions,
        @NonNull final ClassPath... binCps) {
        final String binaryResource = FileObjects.convertPackage2Folder(className);
        final ClassPath merged = ClassPathSupport.createProxyClassPath(binCps);
        for (String ext : extensions) {
            final FileObject res = merged.findResource(String.format(
                    "%s.%s",    //NOI18N
                    binaryResource,
                    ext));
            if (res != null) {
                return merged.findOwnerRoot(res);
            }
        }
        return null;
    }
    
    @CheckForNull
    private static String getModuleName(
            @NullAllowed final URL binRoot) {
        return binRoot == null ?
                null :
                getModuleName(
                        binRoot,
                        SourceForBinaryQuery.findSourceRoots(binRoot).getRoots());
    }
    
    @CheckForNull
    private static String getModuleName(
            @NonNull final URL binRoot,
            @NonNull final FileObject[] srcRoots) {
        if (Arrays.stream(srcRoots).anyMatch((fo) -> fo.getFileObject("module-info.java") != null)) {
            return SourceUtils.getModuleName(binRoot);
        } else {
            return null;
        }
    }
    
    private static URL[] removeArchives(@NonNull final URL... orig) {
        final List<URL> res = new ArrayList<>(orig.length);
        for (URL url : orig) {
            if (!FileUtil.isArchiveArtifact(url)) {
                res.add(url);
            }
        }
        return res.isEmpty() ?
                orig :
                res.toArray(new URL[0]);
    }

    private static ExecutorTask clean(Map<String, ?> properties) {
        properties = new HashMap<String, Object>(properties);
        String projectName = getValue(properties, PROP_PROJECT_NAME, String.class);
        FileObject toRun = getValue(properties, PROP_EXECUTE_FILE, FileObject.class);
        ClassPath exec = getValue(properties, PROP_EXECUTE_CLASSPATH, ClassPath.class);

        if (exec == null) {
            Parameters.notNull("toRun", toRun);
            exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        }

        if (projectName == null) {
            Project project = getValue(properties, "project", Project.class);
            if (project != null) {
                projectName = ProjectUtils.getInformation(project).getDisplayName();
            }
            if (projectName == null && toRun != null) {
                project = FileOwnerQuery.getOwner(toRun);
                if (project != null) {
                    //NOI18N
                    projectName = ProjectUtils.getInformation(project).getDisplayName();
                }
            }
            if (projectName == null) {
                projectName = "";
            }
        }
        
        LOG.log(Level.FINE, "execute classpath={0}", exec);

        final ClassPath execFin = exec;

        return ExecutionEngine.getDefault().execute(projectName, new Runnable() {
            public void run() {
                try {
                    doClean(execFin);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, InputOutput.NULL);
    }

    private static void setProperty(Map<String,String> antProps, String property, String value) {
        if (value != null) {
            antProps.put(property, value);
        }
    }

    private static <T> T getValue(Map<String, ?> properties, String name, Class<T> type) {
        Object v = properties.remove(name);

        if (v instanceof FileObject && type == String.class) {
            FileObject f = (FileObject) v;
            File file = FileUtil.toFile(f);

            if (file == null) {
                return null;
            }
            
            v = file.getAbsolutePath();
        }

        if (v instanceof File && type == String.class) {
            v = ((File) v).getAbsolutePath();
        }

        return type.cast(v);
    }

    private static <T> List<T> getMultiValue(Map<String, ?> properties, String name, Class<T> type) {
        Iterable<?> v = (Iterable<?>) properties.remove(name);
        List<T> result = new LinkedList<>();

        if (v == null) {
            return Collections.emptyList();
        }
        
        for (Object o : v) {
            result.add(type.cast(o));
        }

        return result;
    }

    private static String toOneLine(Iterable<String> it) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String s : it) {
            if (!first) {
                result.append(' ');
            }
            first = false;
            result.append(s);
        }

        return result.toString();
    }

    private static URL locateScript(String actionName) {
        return ProjectRunnerImpl.class.getResource("/org/netbeans/modules/java/source/ant/resources/" + actionName + "-snippet.xml");
    }

    private static FileObject buildScript(String actionName, boolean forceCopy) throws IOException {
        URL script = locateScript(actionName);

        if (script == null) {
            return null;
        }

        URL thisClassSource = ProjectRunnerImpl.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = FileUtil.archiveOrDirForURL(thisClassSource);
        File scriptFile = Places.getCacheSubfile("executor-snippets/" + actionName + ".xml");
        
        if (forceCopy || !scriptFile.canRead() || (jarFile != null && jarFile.lastModified() > scriptFile.lastModified())) {
            try {
                URLConnection connection = script.openConnection();
                FileObject target = FileUtil.createData(scriptFile);

                copyFile(connection, target);
                return target;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        return FileUtil.toFileObject(scriptFile);
    }

    private static void copyFile(URLConnection source, FileObject target) throws IOException {
        InputStream ins = null;
        OutputStream out = null;

        try {
            ins = source.getInputStream();
            out = target.getOutputStream();

            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static void doClean(ClassPath exec) throws IOException {
        for (ClassPath.Entry entry : exec.entries()) {
            SourceForBinaryQuery.Result2 r = SourceForBinaryQuery.findSourceRoots2(entry.getURL());

            if (r.preferSources() && r.getRoots().length > 0) {
                for (FileObject source : r.getRoots()) {
                    File sourceFile = FileUtil.toFile(source);

                    if (sourceFile == null) {
                        LOG.log(Level.WARNING, "Source URL: {0} cannot be translated to file, skipped", source.toURL().toExternalForm());
                        continue;
                    }

                    BuildArtifactMapperImpl.clean(Utilities.toURI(sourceFile).toURL());
                }
            }
        }
    }

    private static final class FakeAntProjectCookie implements AntProjectCookie, ChangeListener {

        private final AntProjectCookie apc;
        private final String command;
        private final String projectName;
        private final ChangeSupport cs = new ChangeSupport(this);

        public FakeAntProjectCookie(AntProjectCookie apc, String command, String projectName) {
            this.apc = apc;
            this.apc.addChangeListener(WeakListeners.change(this, this.apc));
            this.command = command;
            this.projectName = projectName;
        }

        public File getFile() {
            return this.apc.getFile();
        }

        public FileObject getFileObject() {
            return this.apc.getFileObject();
        }

        public Document getDocument() {
            return this.apc.getDocument();
        }

        public Element getProjectElement() {
            Element element = apc.getProjectElement();
            if (element == null || this.apc.getParseException() != null) {
                final File fo = this.apc.getFile();
                LOG.log(Level.FINE, String.format("Cannot parse: %s exists: %b readable: %b",
                        fo == null ? null  : fo.getAbsolutePath(),
                        fo == null ? false : fo.exists(),
                        fo == null ? false : fo.canRead()));
                try {
                    DataObject od = DataObject.find(buildScript(command, true));
                    //the APC does not refresh itself automatically, need to push it to the refresh:
                    if (od instanceof PropertyChangeListener) {
                        ((PropertyChangeListener) od).propertyChange(new PropertyChangeEvent(od, null, null, null));
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                
                element = apc.getProjectElement();
                
                if (element == null) return null;
            }
            return new FakeElement(element, projectName);
        }

        public Throwable getParseException() {
            return this.apc.getParseException();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }

    }

    private static final class FakeElement implements Element {

        private final Element delegate;
        private final String projectName;
        
        public FakeElement(final Element delegate, String projectName) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            this.delegate = delegate;
            this.projectName = projectName;
        }

        public Object setUserData(String key, Object data, UserDataHandler handler) {
            return delegate.setUserData(key, data, handler);
        }

        public void setTextContent(String textContent) throws DOMException {
            delegate.setTextContent(textContent);
        }

        public void setPrefix(String prefix) throws DOMException {
            delegate.setPrefix(prefix);
        }

        public void setNodeValue(String nodeValue) throws DOMException {
            delegate.setNodeValue(nodeValue);
        }

        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            return delegate.replaceChild(newChild, oldChild);
        }

        public Node removeChild(Node oldChild) throws DOMException {
            return delegate.removeChild(oldChild);
        }

        public void normalize() {
            delegate.normalize();
        }

        public String lookupPrefix(String namespaceURI) {
            return delegate.lookupPrefix(namespaceURI);
        }

        public String lookupNamespaceURI(String prefix) {
            return delegate.lookupNamespaceURI(prefix);
        }

        public boolean isSupported(String feature, String version) {
            return delegate.isSupported(feature, version);
        }

        public boolean isSameNode(Node other) {
            return delegate.isSameNode(other);
        }

        public boolean isEqualNode(Node arg) {
            return delegate.isEqualNode(arg);
        }

        public boolean isDefaultNamespace(String namespaceURI) {
            return delegate.isDefaultNamespace(namespaceURI);
        }

        public Node insertBefore(Node newChild, Node refChild) throws DOMException {
            return delegate.insertBefore(newChild, refChild);
        }

        public boolean hasChildNodes() {
            return delegate.hasChildNodes();
        }

        public boolean hasAttributes() {
            return delegate.hasAttributes();
        }

        public Object getUserData(String key) {
            return delegate.getUserData(key);
        }

        public String getTextContent() throws DOMException {
            return delegate.getTextContent();
        }

        public Node getPreviousSibling() {
            return delegate.getPreviousSibling();
        }

        public String getPrefix() {
            return delegate.getPrefix();
        }

        public Node getParentNode() {
            return delegate.getParentNode();
        }

        public Document getOwnerDocument() {
            return delegate.getOwnerDocument();
        }

        public String getNodeValue() throws DOMException {
            return delegate.getNodeValue();
        }

        public short getNodeType() {
            return delegate.getNodeType();
        }

        public String getNodeName() {
            return delegate.getNodeName();
        }

        public Node getNextSibling() {
            return delegate.getNextSibling();
        }

        public String getNamespaceURI() {
            return delegate.getNamespaceURI();
        }

        public String getLocalName() {
            return delegate.getLocalName();
        }

        public Node getLastChild() {
            return delegate.getLastChild();
        }

        public Node getFirstChild() {
            return delegate.getFirstChild();
        }

        public Object getFeature(String feature, String version) {
            return delegate.getFeature(feature, version);
        }

        public NodeList getChildNodes() {
            return delegate.getChildNodes();
        }

        public String getBaseURI() {
            return delegate.getBaseURI();
        }

        public NamedNodeMap getAttributes() {
            return delegate.getAttributes();
        }

        public short compareDocumentPosition(Node other) throws DOMException {
            return delegate.compareDocumentPosition(other);
        }

        public Node cloneNode(boolean deep) {
            return delegate.cloneNode(deep);
        }

        public Node appendChild(Node newChild) throws DOMException {
            return delegate.appendChild(newChild);
        }

        public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
            delegate.setIdAttributeNode(idAttr, isId);
        }

        public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
            delegate.setIdAttributeNS(namespaceURI, localName, isId);
        }

        public void setIdAttribute(String name, boolean isId) throws DOMException {
            delegate.setIdAttribute(name, isId);
        }

        public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
            return delegate.setAttributeNodeNS(newAttr);
        }

        public Attr setAttributeNode(Attr newAttr) throws DOMException {
            return delegate.setAttributeNode(newAttr);
        }

        public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
            delegate.setAttributeNS(namespaceURI, qualifiedName, value);
        }

        public void setAttribute(String name, String value) throws DOMException {
            delegate.setAttribute(name, value);
        }

        public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
            return delegate.removeAttributeNode(oldAttr);
        }

        public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
            delegate.removeAttributeNS(namespaceURI, localName);
        }

        public void removeAttribute(String name) throws DOMException {
            delegate.removeAttribute(name);
        }

        public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.hasAttributeNS(namespaceURI, localName);
        }

        public boolean hasAttribute(String name) {
            return delegate.hasAttribute(name);
        }

        public String getTagName() {
            return delegate.getTagName();
        }

        public TypeInfo getSchemaTypeInfo() {
            return delegate.getSchemaTypeInfo();
        }

        public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getElementsByTagNameNS(namespaceURI, localName);
        }

        public NodeList getElementsByTagName(String name) {
            return delegate.getElementsByTagName(name);
        }

        public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getAttributeNodeNS(namespaceURI, localName);
        }

        public Attr getAttributeNode(String name) {
            return delegate.getAttributeNode(name);
        }

        public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getAttributeNS(namespaceURI, localName);
        }

        public String getAttribute(String name) {
            if ("name".equals(name)) {
                String pattern = delegate.getAttribute(name);
                
                return MessageFormat.format(pattern, projectName);
            }
            return delegate.getAttribute(name);
        }

    }

    private final class Work implements Runnable {
        private final String command;
        private final Map<String,?> properties;
        private final AtomicReference<Runnable> callBack;
        //@GuardedBy("this")
        private Union2<ExecutorTask,Throwable> result;
        //@GuardedBy("this")
        private boolean stopped;

        Work(
            final String command,
            Map<String,?> properties) {
            this.command = command;
            this.properties = properties;
            this.callBack = new AtomicReference<>(NOP);
        }

        void setCallback(final Runnable callBack) {
            if (!this.callBack.compareAndSet(NOP, callBack)) {
                throw new IllegalStateException("Already set"); //NOI18N
            }
        }

        @Override
        public void run() {
            try {
                String[] projectName = new String[1];
                Map<String,String> antProps = computeProperties(command, properties, projectName);
                FileObject script = buildScript(command, false);
                AntProjectCookie apc = new FakeAntProjectCookie(AntScriptUtils.antProjectCookieFor(script), command, projectName[0]);
                AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
                Properties props = execenv.getProperties();
                props.putAll(antProps);
                props.put("nb.wait.for.caches", "true");
                if (properties.containsKey("maven.disableSources")) {
                    props.put("maven.disableSources", String.valueOf(properties.get("maven.disableSources")));
                }
                execenv.setProperties(props);
                setResult(Union2.<ExecutorTask,Throwable>createFirst(AntTargetExecutor.createTargetExecutor(execenv).execute(apc, null)));
            } catch (Throwable t) {
                setResult(Union2.<ExecutorTask,Throwable>createSecond(t));
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath)t;
                }
            }
        }

        private synchronized void setResult(final Union2<ExecutorTask,Throwable> result) {
            this.result = result;
            if (result.hasFirst()) {
                result.first().addTaskListener(new TaskListener() {
                    @Override
                    public void taskFinished(Task task) {
                        callBack.get().run();
                    }
                });
                if (stopped) {
                    result.first().stop();
                }
            } else {
                callBack.get().run();
            }
            this.notifyAll();
        }

        private synchronized Union2<ExecutorTask,Throwable> getResult() {
            while (result == null) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    return null;
                }
            }
            return result;
        }

        private synchronized void stop() {
            if (result != null && result.hasFirst()) {
                result.first().stop();
            } else {
                stopped = true;
            }
        }
    }

    private final class WrapperTask extends ExecutorTask {
        private final Work work;

        WrapperTask(final Work work) {
            super(NOP);
            this.work = work;
        }

        @Override
        public void stop() {
            work.stop();
        }

        @Override
        public int result() {
            final Union2<ExecutorTask, Throwable> result = work.getResult();
            return result == null || result.hasSecond() ?
                    -1 :
                    result.first().result();
        }

        @Override
        public InputOutput getInputOutput() {
            final Union2<ExecutorTask, Throwable> result = work.getResult();
            return result == null || result.hasSecond() ?
                    InputOutput.NULL :
                    result.first().getInputOutput();
        }
    }
}
