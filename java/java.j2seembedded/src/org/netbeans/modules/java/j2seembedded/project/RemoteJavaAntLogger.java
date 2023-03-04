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

package org.netbeans.modules.java.j2seembedded.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
 * @author Tomas Zezula
 * @see "#42525"
 */
@ServiceProvider(service=AntLogger.class, position=50)
public final class RemoteJavaAntLogger extends AntLogger {
    
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
    // should be consistent with java.project.JavaAntLogger.STACK_TRACE
    // should be consistent with org.apache.tools.ant.module.bridge.impl.ForkedJavaOverride
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
        Matcher m = STACK_TRACE.matcher(line);
        if (m.matches()) {
            // We have a stack trace.
            String pkg = m.group(2);
            String filename = m.group(5);
            int lineNumber;
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
     * Ant task names we will pay attention to.
     */
    private static final String[] TASKS_OF_INTEREST = {
        "sshexec", // NOI18N
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
        public ClassPath classpath = null;
        public volatile Collection<FileObject> classpathSourceRoots = null;
        public volatile String possibleExceptionText = null;
        public volatile String lastExceptionMessage = null;
        public SessionData() {}
        public void setClasspath(ClassPath cp) {
            classpath = cp;
            classpathSourceRoots = null;
        }
        public void setPlatformSources(ClassPath platformSources) {
            this.platformSources = platformSources;
            classpathSourceRoots = null;
        }
    }
    
    /** Default constructor for lookup. */
    public RemoteJavaAntLogger() {}
    
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
    public void buildStarted(AntEvent event) {
        final AntSession session = event.getSession();
        final SessionData data = getSessionData(session);
        final String path = event.evaluate("${basedir}");   //NOI18N
        if (path != null) {
            final File projectDir = FileUtil.normalizeFile(new File (path));
            final FileObject projectFolder = FileUtil.toFileObject(projectDir);
            if (projectFolder != null) {
                final Project prj = FileOwnerQuery.getOwner(projectFolder);
                if (prj != null) {
                    ClassPath runCP = null;
                    ClassPath bootCP = null;
                    for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        final FileObject root = sg.getRootFolder();
                        if (!isTest(root)) {
                            runCP = ClassPath.getClassPath(root, ClassPath.EXECUTE);
                            bootCP = ClassPath.getClassPath(root, ClassPath.BOOT);
                            break;
                        }
                    }
                    if (runCP != null) {
                        data.setClasspath(runCP);
                    }
                    if (bootCP != null) {
                        final ClassPath platformSources = findPlatformSources(bootCP);
                        if (platformSources != null) {
                            data.setPlatformSources(platformSources);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void messageLogged(AntEvent event) {
        AntSession session = event.getSession();
        int messageLevel = event.getLogLevel();
        int sessionLevel = session.getVerbosity();
        SessionData data = getSessionData(session);
        String line = removeCRLF(event.getMessage());

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
                } else if (messageLevel <= sessionLevel && TASKS_OF_INTEREST[0].equals(event.getTaskName())) {
                    event.consume();
                    session.println(line, event.getLogLevel() <= AntEvent.LOG_WARN, null);
                }
            }
        } else {
            // Track the last line which was not a stack trace - probably the exception message.
            if (!isEmpty(line)) {
                session.println(line, false, null);
                data.lastExceptionMessage = null;
                data.possibleExceptionText = line;
            }
            event.consume();            
        }        
    }

    /**
     * Removes CR, LF from line.
     * Workaround of sshexec.
     * @param message to remove CR LF from
     * @return stripped line
     */
    private static String removeCRLF(@NonNull final String message) {
        int index = message.length() - 1;
        int len = 0;
        while (index >= 0 && len < 2) {
            if (message.charAt(index) != 13 && message.charAt(index) != 10) {
                break;
            }
            index--;
            len++;
        }
        return index == message.length() - 1 ?
            message :
            message.substring(0, index+1);
    }

    /**
     * Tests if a line contains just white spaces.
     * Workaround of sshexec.
     * @param str the string to test
     * @return true if line contains just white spaces.
     */
    private boolean isEmpty(@NonNull final String str) {
        boolean res = true;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                res = false;
                break;
            }
        }
        return res;
    }

    private static boolean isTest(FileObject root) {
        return UnitTestForSourceQuery.findSources(root).length > 0;
    }

    @CheckForNull
    private ClassPath findPlatformSources(@NullAllowed ClassPath bootCP) {
        if (bootCP == null) {
            return null;
        }
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            final ClassPath platformBootLibs = p.getBootstrapLibraries();
            if (bootCP.toString().equals(platformBootLibs.toString())) {
                return p.getSourceFolders();
            }
        }
        return null;
    }
    
    /**
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher with -cp).
     */
    private static Collection<FileObject> getCurrentSourceRootsForClasspath(SessionData data) {
        if (data.classpath == null) {
            return Collections.emptySet();
        }
        Collection<FileObject> result;
        synchronized (data) {
            result = data.classpathSourceRoots;
        }
        if (result == null) {
            result = new LinkedHashSet<>();
            for (ClassPath.Entry entry : data.classpath.entries()) {
                FileObject[] someRoots = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
                result.addAll(Arrays.asList(someRoots));
            }
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
                data.classpathSourceRoots = result;
            }
        }
        return result;
    }
    
    private static String guessExceptionMessage(SessionData data) {
        final String pet = data.possibleExceptionText;
        String lem = data.lastExceptionMessage;
        if (pet != null) {
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
