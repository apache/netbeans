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

package org.netbeans.modules.java.project.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOColorPrint;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Ant logger which handles Java- and Java-project-specific UI.
 * Specifically, handles stack traces hyperlinking and suppresses
 * hyperlinking of nbproject/build-impl.xml files.
 * @author Jesse Glick
 * @see "#42525"
 */
@ServiceProvider(service=AntLogger.class, position=50)
public final class JavaAntLogger extends AntLogger {
    
    public static final int LOGGER_MAX_LINE_LENGTH = Integer.getInteger("logger.max.line.length", 3000); //NOI18N
    private static final String MODULE_INFO_CLZ = "module-info.class";  //NOI18N
    
    static final class StackTraceParse {
        final String line;
        final String resource;
        final int lineNumber;
        final String prePart, midPart, endPart;
        StackTraceParse(String line, String resource, int lineNumber, String prePart, String midPart, String endPart) {
            this.line = line;
            this.resource = resource;
            this.lineNumber = lineNumber;
            this.prePart = prePart;
            this.midPart = midPart;
            this.endPart = endPart;
        }
        public @Override String toString() {
            return resource + ":" + lineNumber;
        }
        void hyperlink(AntSession session, AntEvent event, FileObject source,
                int messageLevel, int sessionLevel, SessionData data) {
            if (messageLevel <= sessionLevel && !event.isConsumed()) {
                final OutputListener hyperlink = session.createStandardHyperlink(source.toURL(), guessExceptionMessage(data), lineNumber, -1, -1, -1);
                event.consume();
                InputOutput io = session.getIO();
                if (IOColorPrint.isSupported(io)) {
                    try {
                        OutputWriter out = messageLevel <= AntEvent.LOG_WARN ? io.getErr() : /* #174781 1/2 */io.getOut();
                        boolean important = prePart.contains(/* #174781 2/2 */"at ") && /* e.g. InstalledFileLocatorImpl.findCaller */!prePart.contains("WARNING"); // NOI18N
                        out.print(prePart);
                        IOColorPrint.print(io, midPart, hyperlink, important, null);
                        out.println(endPart);
                        return;
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                session.println(line, true, hyperlink);
            }
        }
    }
    /** Java identifier */
    private static final String JIDENT = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"; // NOI18N
    // should be consistent with org.apache.tools.ant.module.bridge.impl.ForkedJavaOverride
    // should be consistent with org.netbeans.modules.java.j2seembedded.project.RemoteJavaAntLogger
    // would be nice to match org.netbeans.modules.hudson.impl.JavaHudsonLogger.STACK_TRACE, but would need to copy more
    /**
     * <ol>
     * <li>non-hyperlinkable initial portion
     * <li>package
     * <li>class
     * <li>hyperlinkable portion
     * <li>filename
     * <li>line number
     * <li>non-hyperlinkable final portion
     * </ol>
     */
    private static final Pattern STACK_TRACE = Pattern.compile(
            "(.*?((?:" + JIDENT + "[.])*)(" + JIDENT + ")[.](?:" + JIDENT + "|<init>|<clinit>)" + // NOI18N
            "[(])(((?:"+JIDENT+"(?:\\."+JIDENT+")*/)?" + JIDENT + "[.]java):([0-9]+)|Unknown Source)([)].*)"); // NOI18N
    static StackTraceParse/*|null*/ parseStackTraceLine(String line) {
        if (line.length() >= LOGGER_MAX_LINE_LENGTH) { // too long message, probably coming from user, so do not check for stacktrace
            return null;
        }
        Matcher m = STACK_TRACE.matcher(line);
        if (m.matches()) {
            // We have a stack trace.
            String pkg = m.group(2);
            String filename = m.group(5);
            int lineNumber;
            if (filename != null) {
                final int index = filename.indexOf('/');    //NOI18N
                if (index >= 0) {
                    filename = filename.substring(index+1);
                    if (filename.isEmpty()) {
                        filename = null;
                    }
                }
            }
            if (filename == null) {
                filename = m.group(3).replaceFirst("[$].+", "") + ".java"; // NOI18N
                lineNumber = 1;
            } else {
                lineNumber = Integer.parseInt(m.group(6));
            }
            return new StackTraceParse(line, pkg.replace('.', '/') + filename, lineNumber, m.group(1), m.group(4), m.group(7));
        } else {
            return null;
        }
    }

    /**
     * Regexp matching the first line of a stack trace, with the exception message.
     * Captured groups:
     * <ol>
     * <li>unqualified name of exception class plus possible message
     * </ol>
     */
    private static final Pattern EXCEPTION_MESSAGE = Pattern.compile(
    // #42894: JRockit uses "Main Thread" not "main"
    "(?:Exception in thread \"(?:main|Main Thread)\" )?(?:(?:" + JIDENT + "\\.)+)(" + JIDENT + "(?:: .+)?)"); // NOI18N
    
    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specifies the classpath.
     * Hack to find the classpath an Ant task is using.
     * Cf. Commandline.describeArguments, issue #28190.
     * Captured groups:
     * <ol>
     * <li>the classpath
     * </ol>
     */
    private static final Pattern CLASSPATH_ARGS = Pattern.compile("\r?\n'-classpath'\r?\n'(.*)'\r?\n"); // NOI18N
    private static final Pattern MODULEPATH_ARGS = Pattern.compile("\r?\n'--module-path'\r?\n'(.*)'\r?\n"); // NOI18N
    private static final Pattern UPGRADE_MODULEPATH_ARGS = Pattern.compile("\r?\n'--upgrade-module-path'\r?\n'(.*)'\r?\n"); // NOI18N
    
    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specifies java executable.
     * Hack to find JDK used for execution.
     */
    private static final Pattern JAVA_EXECUTABLE = Pattern.compile("^Executing '(.*)' with arguments:$", Pattern.MULTILINE); // NOI18N
    
    /**
     * Ant task names we will pay attention to.
     */
    private static final String[] TASKS_OF_INTEREST = {
        // XXX should this really be restricted? what about stack traces printed during shutdown?
        "java", // NOI18N
        // #44328: unit tests run a different task:
        "junit", // NOI18N
        "testng", // NOI18N
        // Nice to handle stack traces from e.g. NB's own build system too!
        "exec", // NOI18N
        // #63065: Mobility execution
        "nb-run",     //NOI18N
    };
    
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_VERBOSE, // for CLASSPATH_ARGS
        AntEvent.LOG_INFO, // for some stack traces
        AntEvent.LOG_WARN, // for most stack traces
        AntEvent.LOG_ERR, // for some stack traces, incl. those redelivered from StandardLogger
    };
    
    /**
     * Data stored in the session.
     */
    private static final class SessionData {
        public ClassPath platformSources = null;
        public String classpath = null;
        public String modulepath = null;
        public String upgradeModulepath = null;
        public volatile Collection<FileObject> searchSourceRoots = null;
        public volatile String possibleExceptionText = null;
        public volatile String lastExceptionMessage = null;
        public SessionData() {}

        public void setClasspath(String cp) {
            classpath = cp;
            searchSourceRoots = null;
        }
        public void setModulepath(@NullAllowed final String mp) {
            modulepath = mp;
            searchSourceRoots = null;
        }
        public void setUpgradeModulepath(@NullAllowed final String ump) {
            upgradeModulepath = ump;
            searchSourceRoots = null;
        }
        public void setPlatformSources(ClassPath platformSources) {
            this.platformSources = platformSources;
            searchSourceRoots = null;
        }
    }
    
    /** Default constructor for lookup. */
    public JavaAntLogger() {}
    
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    @Override
    public String[] interestedInTasks(AntSession session) {
        return TASKS_OF_INTEREST;
    }
    
    @Override
    public int[] interestedInLogLevels(AntSession session) {
        // XXX could exclude those in [INFO..ERR] greater than session.verbosity
        return LEVELS_OF_INTEREST;
    }
    
    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);
        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }
        return data;
    }

    @Override
    public void messageLogged(AntEvent event) {
        AntSession session = event.getSession();
        int messageLevel = event.getLogLevel();
        int sessionLevel = session.getVerbosity();
        SessionData data = getSessionData(session);
        String line = event.getMessage();
        assert line != null;

        StackTraceParse parse = parseStackTraceLine(line);
        if (parse != null) {
            // Check to see if the class is listed in our per-task sourcepath.
            // XXX could also look for -Xbootclasspath etc., but probably less important
            for (FileObject root : getCurrentSourceRootsForClasspath(data)) {
                FileObject source = root.getFileObject(parse.resource);
                if (source != null) {
                    // Got it!
                    parse.hyperlink(session, event, source, messageLevel, sessionLevel, data);
                    break;
                }
            }
            // Also check global sourcepath (sources of open projects, and sources
            // corresponding to compile or boot classpaths of open projects).
            // Fallback in case a JAR file is copied to an unknown location, etc.
            // In this case we can't be sure that this source file really matches
            // the .class used in the stack trace, but it is a good guess.
            if (!event.isConsumed()) {
                FileObject source = GlobalPathRegistry.getDefault().findResource(parse.resource);
                if (source != null) {
                    parse.hyperlink(session, event, source, messageLevel, sessionLevel, data);
                } else if (messageLevel <= sessionLevel && "java".equals(event.getTaskName())) {
                    event.consume();
                    session.println(line, event.getLogLevel() <= AntEvent.LOG_WARN, null);
                }
            }
        } else {
            // Track the last line which was not a stack trace - probably the exception message.            
            data.lastExceptionMessage = null;
            data.possibleExceptionText = line;
        }
        
        // Look for classpaths.
        if (messageLevel == AntEvent.LOG_VERBOSE) {
            if (line.length() >= LOGGER_MAX_LINE_LENGTH) { // too long message, probably coming from user, so do not care about it
                return;
            }
            Matcher m2 = CLASSPATH_ARGS.matcher(line);
            if (m2.find()) {
                String cp = m2.group(1);
                data.setClasspath(cp);
            }
            m2 = MODULEPATH_ARGS.matcher(line);
            if (m2.find()) {
                final String mp = m2.group(1);
                data.setModulepath(mp);
            }
            m2 = UPGRADE_MODULEPATH_ARGS.matcher(line);
            if (m2.find()) {
                final String ump = m2.group(1);
                data.setUpgradeModulepath(ump);
            }
            // XXX should also probably clear classpath when taskFinished called
            m2 = JAVA_EXECUTABLE.matcher(line);
            if (m2.find()) {
                String executable = m2.group(1);
                ClassPath platformSources = findPlatformSources(executable);
                if (platformSources != null) {
                    data.setPlatformSources(platformSources);
                }
            }
        }
    }
    
    private ClassPath findPlatformSources(String javaExecutable) {
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            FileObject fo = p.findTool("java"); // NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f.getAbsolutePath().startsWith(javaExecutable)) {
                    return p.getSourceFolders();
                }
            }
        }
        return null;
    }
    
    /**
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher with -cp).
     */
    private static Collection<FileObject> getCurrentSourceRootsForClasspath(SessionData data) {
        if (data.classpath == null &&
                data.modulepath == null &&
                data.upgradeModulepath == null) {
            return Collections.emptySet();
        }
        Collection<FileObject> result;
        synchronized (data) {
            result = data.searchSourceRoots;
        }
        if (result == null) {
            result = new LinkedHashSet<>();
            addPath(data.classpath, result, false);
            addPath(data.modulepath, result, true);
            addPath(data.upgradeModulepath, result, true);
            if (data.platformSources != null) {
                result.addAll(Arrays.asList(data.platformSources.getRoots()));
            } else {
                // no platform found. use default one:
                JavaPlatform plat = JavaPlatform.getDefault();
                // in unit tests the default platform may be null:
                if (plat != null) {
                    result.addAll(Arrays.asList(plat.getSourceFolders().getRoots()));
                }
            }
            result = Collections.unmodifiableCollection(result);
            synchronized (data) {
                data.searchSourceRoots = result;
            }
        }
        return result;
    }

    private static void addPath(
        @NullAllowed final String path,
        @NonNull Collection<? super FileObject> collector,
        final boolean modulepath) {
        if (path != null) {
            final StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                final String binrootS = tok.nextToken();
                final File f = FileUtil.normalizeFile(new File(binrootS));
                final Collection<? extends File> toAdd = modulepath ?
                        collectModules(f) :
                        Collections.singleton(f);
                toAdd.forEach((e) -> {
                    final URL binroot = FileUtil.urlForArchiveOrDir(f);
                    if (binroot != null) {
                        final FileObject[] someRoots = SourceForBinaryQuery.findSourceRoots(binroot).getRoots();
                        Collections.addAll(collector, someRoots);
                    }
                });
            }
        }
    }

    @NonNull
    private static Collection<? extends File> collectModules(@NonNull final File root) {
        if (root.isDirectory()) {
            if (new File(root,MODULE_INFO_CLZ).isFile()) {
                return Collections.singleton(root);
            } else {
                final File[] children = root.listFiles((e) -> {
                    try {
                        return FileUtil.isArchiveFile(BaseUtilities.toURI(e).toURL());
                    } catch (MalformedURLException mue) {
                        return false;
                    }
                });
                return children == null ?
                        Collections.emptyList() :
                        Arrays.asList(children);
            }
        } else {
            return Collections.singleton(root);
        }
    }
    
    private static String guessExceptionMessage(SessionData data) {
        final String pet = data.possibleExceptionText;
        String lem = data.lastExceptionMessage;
        if (pet != null && pet.length() < LOGGER_MAX_LINE_LENGTH) { // not too long message, so check for exception
            if (lem == null) {
                Matcher m = EXCEPTION_MESSAGE.matcher(pet);
                if (m.matches()) {
                    data.lastExceptionMessage = lem = m.group(1);
                } else {
                    data.possibleExceptionText = null;
                }
            }
            return lem;
        }
        return null;
    }
}
