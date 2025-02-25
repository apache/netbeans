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
package org.netbeans.modules.maven.execute;

import org.netbeans.modules.maven.execute.cmd.ExecutionEventObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.project.MavenProject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.output.OutputUtils;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor.ResumeFromFinder;

import static org.netbeans.modules.maven.execute.AbstractOutputHandler.PRJ_EXECUTE;
import static org.netbeans.modules.maven.execute.AbstractOutputHandler.SESSION_EXECUTE;

import org.netbeans.modules.maven.execute.cmd.ExecMojo;
import org.netbeans.modules.maven.execute.cmd.ExecProject;
import org.netbeans.modules.maven.execute.cmd.ExecSession;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.IOPosition;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * handling of output coming from maven commandline builds
 * @author Milos Kleint
 */
public class CommandLineOutputHandler extends AbstractOutputHandler {

    //32 means 16 paralel builds, one for input, one for output. #229904
    private static final RequestProcessor PROCESSOR = new RequestProcessor("Maven ComandLine Output Redirection", Integer.getInteger("maven.concurrent.builds", 16) * 2); //NOI18N
    private static final Logger LOG = Logger.getLogger(CommandLineOutputHandler.class.getName());
    private InputOutput inputOutput;

    /*
     * example: '[WARN] [stderr] Exception in thread "main" java.lang.UnsupportedOperationException'
     * @see Output#mapLevel for details
     */
    private static final Pattern linePattern = Pattern.compile("(\\[(DEBUG|TRACE|INFO|WARN|WARNING|ERROR|FATAL)\\]\\s)?(?:\\[(?:stderr|stdout)\\]\\s)?(.*)"); // NOI18N

    public static final Pattern startPatternM2 = Pattern.compile("\\[INFO\\] \\[([\\w]*):([\\w]*)[ ]?.*\\]"); // NOI18N
    public static final Pattern startPatternM3 = Pattern.compile("\\[INFO\\] --- (\\S+):\\S+:(\\S+)(?: [(]\\S+[)])? @ \\S+ ---"); // ExecutionEventLogger.mojoStarted NOI18N

    private static final Pattern mavenSomethingPlugin = Pattern.compile("maven-(.+)-plugin"); // NOI18N
    private static final Pattern somethingMavenPlugin = Pattern.compile("(.+)-maven-plugin"); // NOI18N

    /** @see org.apache.maven.cli.ExecutionEventLogger#logReactorSummary */
    static final Pattern reactorFailure = Pattern.compile("\\[INFO\\] (.+) [.]* FAILURE \\[.+\\]"); // NOI18N
    public static final Pattern reactorSummaryLine = Pattern.compile("(.+) [.]* (FAILURE|SUCCESS) (\\[.+\\])?"); // NOI18N

    private static final Pattern stackTraceElement = OutputUtils.linePattern;

    private OutputWriter stdOut;
    private String currentProject;
    private String currentTag;
    Task outTask;
    private Input inp;
    private ProgressHandle handle;
    /** {@link MavenProject#getName} of first project in reactor to fail, if any */
    String firstFailure;
    private final JSONParser parser;
    private ContextImpl contextImpl;
    //the depth is as follows
    //Session -> Project -> [Fork -> ForkedProject] -> Mojo                
    private final ExecutionEventObject.Tree executionTree = new ExecutionEventObject.Tree(null, null);

    private ExecutionEventObject.Tree currentTreeNode = executionTree;
    private boolean inStackTrace = false;
    private boolean addMojoFold = false;
    private boolean addProjectFold = false;
    private boolean foldsBroken;
    private URL[] mavencoreurls;

    public CommandLineOutputHandler(InputOutput io, Project proj, ProgressHandle hand, RunConfig config, boolean createVisitorContext) {
        super(proj, hand, config, createVisitorContext ? new OutputVisitor(new ContextImpl()) : new OutputVisitor());
        if (createVisitorContext) {
            contextImpl = (ContextImpl) visitor.getContext();
            assert contextImpl != null;
            contextImpl.setExecutionTree(executionTree);
        }
        this.parser = new JSONParser();
        handle = hand;
        inputOutput = io;
        stdOut = inputOutput.getOut();
//        logger = new Logger();
        initProcessorList(proj, config);
    }
    
    /**
     * 
     * @return null if tree is not being built (maven 2.x)
     */
    public @CheckForNull ExecutionEventObject.Tree getExecutionTree() {
        if (contextImpl != null) {
            return executionTree;
        }
        return null;
    }


    @Override
    protected final void checkSleepiness() {
        if (contextImpl == null) { //only perform for maven 2.x now
            handle.progress(currentProject == null ? "" : currentTag == null ? currentProject : currentProject + " " + currentTag); // NOI18N
        }
        super.checkSleepiness();
    }


    void setStdOut(InputStream inStr) {
        outTask = PROCESSOR.post(new Output(inStr));
    }

    void setStdIn(OutputStream in) {
        inp = new Input(in, inputOutput);
        PROCESSOR.post(inp);
    }

    void waitFor() {
        inp.stopInput();
//        if (inTask != null) {
//            inTask.waitFinished();
//        }
        if (outTask != null) {
            outTask.waitFinished();
        }
    }

    @Override
    protected InputOutput getIO() {
        return this.inputOutput;
    }

    private static final String SEC_MOJO_EXEC = "mojo-execute"; //NOI18N
    private void closeCurrentTag() {
        assert contextImpl == null;
        if (currentTag != null) {
            CommandLineOutputHandler.this.processEnd(getEventId(SEC_MOJO_EXEC, currentTag), stdOut);
            currentTag = null;
        }
    }

    private void mergeClasspath(ExecMojo exec, URL[] coreurls) {
        if (coreurls != null) {
            URL[] urls = exec.getClasspathURLs();
            if (urls == null) {
                exec.setClasspathURLs(coreurls);
            } else {
                List<URL> newones = new ArrayList<>(urls.length + coreurls.length);
                newones.addAll(Arrays.asList(urls));
                newones.addAll(Arrays.asList(coreurls));
                exec.setClasspathURLs(newones.toArray(new URL[0]));
            }
        }
    }




    private class Output implements Runnable {
        private static final String INFO_NETBEANS_EXEC_EVENT = "[INFO] NETBEANS-ExecEvent:";

        private final BufferedReader str;
        private boolean skipLF = false;

        public Output(InputStream instream) {
            str = new BufferedReader(new InputStreamReader(instream, getPreferredCharset()));
        }

        private String readLine() throws IOException {
            char[] char1 = new char[1];
            boolean isReady = true;
            int count = 0;
            StringBuilder buf = new StringBuilder();
            while (isReady) {
                count = count + 1;
                if (count > 20000) { //#239847 limit the number of bytes/characters read together in one lump, make sure the stringBuilder doesn't grow out of proportions.
                    break; //make it an incomplete line.
                }
                int ret = str.read(char1);
                if (ret != 1) {
                     if (ret == -1 && buf.length() == 0) {
                         return null;
                     }
                    return buf.toString();
                }
                if (skipLF) {
                    skipLF = false;
                    if (char1[0] == '\n') { //NOI18N
                        continue;
                    }
                }
                if (char1[0] == '\n') { //NOI18N
                    return buf.toString();
                }
                if (char1[0] == '\r') { //NOI18N
                    skipLF = true;
                    buf.append(char1[0]);
                    return buf.toString();
                }
                buf.append(char1[0]);
                isReady = str.ready();
                if (!isReady) {
                    synchronized (this) {
                        try {
                            wait(500);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            if (!str.ready()) {
                                break;
                            }
                            isReady = true;
                        }
                    }

                }
            }
            return "&^#INCOMPLINE:" + buf.toString(); //NOI18N

        }

        public @Override void run() {
            CommandLineOutputHandler.this.processStart(getEventId(SESSION_EXECUTE, null), stdOut);
            if (contextImpl == null) {
                CommandLineOutputHandler.this.processStart(getEventId(PRJ_EXECUTE,null), stdOut);
            }
            try {

                String line = readLine();
                while (line != null) {
                    if (line.startsWith("&^#INCOMPLINE:")) { //NOI18N
                        stdOut.print(line.substring("&^#INCOMPLINE:".length())); //NOI18N
                        line = readLine();
                        continue;
                    }
                    int execEventIdx = line.indexOf(INFO_NETBEANS_EXEC_EVENT);
                    if (execEventIdx == 0) {
                        processExecEvent(parseExecEvent(line));
//                        stdOut.println(line); //XXX temporary
                        line = readLine();
                        continue;
                    }
                    
                    String nextLine = null;
                    if(execEventIdx > 0) {
                        nextLine = line.substring(execEventIdx);
                        line = line.substring(0, execEventIdx);
                    }                   
                    if (line.startsWith("[INFO] Final Memory:")) { //NOI18N
                        // previous value [INFO] --------------- is too early, the compilation errors don't get processed in this case.
                        //heuristics..
                        if (contextImpl == null) { //only in m2
                            closeCurrentTag();
                        }
                    }
                    
                    String tag = null;
                    if (contextImpl == null) {
                        Matcher match = startPatternM3.matcher(line);
                        if (match.matches()) {
                            String mojoArtifact = match.group(1);
                            mojoArtifact = goalPrefixFromArtifactId(mojoArtifact);
                            tag = mojoArtifact + ':' + match.group(2);
                        } else {
                            match = startPatternM2.matcher(line);
                            if (match.matches()) {
                                tag = match.group(1) + ':' + match.group(2);
                            }
                        }
                    }
                    if (tag != null) { //only in m2
                        closeCurrentTag();
                        currentTag = tag;
                        CommandLineOutputHandler.this.processStart(getEventId(SEC_MOJO_EXEC, tag), stdOut);
                        checkSleepiness();
                    }
                    
                    if(line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
                        line = line.substring(0, line.length() - 1);
                    }
                    Matcher lineMatcher = linePattern.matcher(line);
                    if (lineMatcher.matches()) {
                        String level_group = lineMatcher.group(1);
                        Level level = mapLevel(lineMatcher.group(2));
                        String msg = lineMatcher.group(3);
                        updateFoldForException(msg);
                        if (MavenSettings.getDefault().isShowLoggingLevel() && level_group != null) {
                            processLine(level_group + msg, stdOut, level);
                        } else {
                            processLine(msg, stdOut, level);
                        }
                        if (level == Level.INFO && contextImpl == null) { //only perform for maven 2.x now
                            checkProgress(msg);
                        }
                    } else {
                        // shouldn't happen since linePattern should match everything
                        updateFoldForException(line);
                        processLine(line, stdOut, Level.INFO);
                    }
                    if (contextImpl == null && firstFailure == null) {
                        Matcher match = reactorFailure.matcher(line);
                        if (match.matches()) {
                            firstFailure = match.group(1);
                        }
                    }
                    //these two are a bit shaky and depend on output details that might not be available in the future.
                    //however there's no other way to have the proper line marked as beginning of a section (as the event comes first)
                    //without this, the last line of previous output would be marked as beginning of the fold.
                    if (addMojoFold && line.startsWith("[INFO] ---")) {     //NOI18N
                        foldsBroken |= currentTreeNode.startFold(inputOutput);
                        addMojoFold = false;
                    }
                    if (addProjectFold && line.startsWith("[INFO] Building")) {
                        foldsBroken |= currentTreeNode.startFold(inputOutput);
                        addProjectFold = false;
                    }
                    line = nextLine != null ? nextLine : readLine();
                }
            } catch (IOException ex) {
                LOG.log(java.util.logging.Level.FINE, null, ex);
            } finally {
                if (contextImpl == null) {
                    CommandLineOutputHandler.this.processEnd(getEventId(PRJ_EXECUTE, null), stdOut);
                } else {
                    completeTreeAtEnd();
                }
                CommandLineOutputHandler.this.processEnd(getEventId(SESSION_EXECUTE, null), stdOut);
                try {
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // mvnd uses standard SLF4J levels while mvn prints WARN as WARNING
        // see MavenSimpleLogger#renderLevel(int) in the maven repo
        private Level mapLevel(String string) {
            if (string == null) {
                return Level.INFO;
            } else if ("WARN".equals(string)) {
                return Level.WARNING;
            } else if ("TRACE".equals(string)) {
                return Level.DEBUG; // where is trace?
            } else {
                try {
                    return Level.valueOf(string);
                } catch (IllegalArgumentException ex) {
                    return Level.INFO;
                }
            }
        }

        private ExecutionEventObject parseExecEvent(String line) {
            String jsonContent = line.substring(INFO_NETBEANS_EXEC_EVENT.length());
            try {
                Object o = parser.parse(jsonContent);
//                System.out.println("o=" + o);
                if (o instanceof JSONObject) {
                    JSONObject json = (JSONObject) o;
                    return ExecutionEventObject.create(json);
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
//                System.out.println("exc=" + ex);
            }
            return null;
        }

 

        /**
         * Check whether the line is start of a stacktrace, inside a stacktrace,
         * or is another text, and update folds accordingly.
         */
        private void updateFoldForException(String line) {
            if (line.endsWith(")") && stackTraceElement.matcher(line).matches()) {
                inStackTrace = true;
                if (!currentTreeNode.hasInnerOutputFold()) {
                    currentTreeNode.startInnerOutputFold(inputOutput);
                }
            } else  if (inStackTrace) {
                currentTreeNode.finishInnerOutputFold();
                inStackTrace = false;
            }
        }

        
    }
    
        private void completeTreeAtEnd() {
            //#229877 at the end of the build, verify that the tree is complete.
            for (ExecutionEventObject.Tree nd : executionTree.getChildrenNodes()) {
                if (nd.getEndEvent() == null) {
                    ExecutionEventObject innerEnd = createEndForStart(nd.getStartEvent());
                    trimTree(innerEnd);
                }
            }
        }
       
        private void processExecEvent(ExecutionEventObject obj) {
            if (obj == null) {
                return;
            }
            checkProgress(obj);
            
            if (ExecutionEvent.Type.SessionStarted.equals(obj.type)) {
                if (currentTreeNode != executionTree) {
                    //we are not at a real start, something restarted the build..
                    completeTreeAtEnd();
                }
                mavencoreurls = ((ExecSession)obj).getMnvcoreurls();
            }
            if (ExecutionEvent.Type.MojoStarted.equals(obj.type)) {
                growTree(obj);
                addMojoFold = true;
                ExecMojo exec = (ExecMojo) obj;
                String tag = goalPrefixFromArtifactId(exec.plugin.artifactId) + ":" + exec.goal;
                ExecutionEventObject.Tree prjNode = currentTreeNode.findParentNodeOfType(ExecutionEvent.Type.ProjectStarted);
                assert prjNode != null;
                ExecProject p = (ExecProject) prjNode.getStartEvent();
                handle.progress(p.gav.artifactId + " " + tag);
                if (contextImpl != null) {
                    Project pr = exec.findProject();
                    contextImpl.setCurrentProject(pr);
                    CommandLineOutputHandler.this.processStart(getEventId(SEC_MOJO_EXEC, tag), stdOut);
                }
            }
            if (ExecutionEvent.Type.MojoSucceeded.equals(obj.type)) {
                if (MavenSettings.getDefault().isCollapseSuccessFolds()) {
                    currentTreeNode.collapseFold();
                }
                currentTreeNode.finishFold();
                ExecMojo exec = (ExecMojo) obj;
                mergeClasspath(exec, mavencoreurls);
                trimTree(exec);
                String tag = goalPrefixFromArtifactId(exec.plugin.artifactId) + ":" + exec.goal;
                if (contextImpl != null) {
                    Project pr = exec.findProject();
                    contextImpl.setCurrentProject(pr);
                    CommandLineOutputHandler.this.processEnd(getEventId(SEC_MOJO_EXEC, tag), stdOut);
                }
            }
            else if (ExecutionEvent.Type.MojoFailed.equals(obj.type)) {
                currentTreeNode.finishFold();
                ExecMojo exec = (ExecMojo) obj;
                mergeClasspath(exec, mavencoreurls);
                trimTree(exec);
                String tag = goalPrefixFromArtifactId(exec.plugin.artifactId) + ":" + exec.goal;
                if (contextImpl != null) {
                    Project pr = exec.findProject();
                    contextImpl.setCurrentProject(pr);
                    CommandLineOutputHandler.this.processFail(getEventId(SEC_MOJO_EXEC, tag), stdOut);
                }
            }
            else if (ExecutionEvent.Type.ProjectStarted.equals(obj.type)) {
                growTree(obj);
                addProjectFold = true;
                if (contextImpl != null) {
                    ExecProject pr = (ExecProject)obj;
                    Project project = pr.findProject();
                    contextImpl.setCurrentProject(project);
                    CommandLineOutputHandler.this.processStart(getEventId(PRJ_EXECUTE, null), stdOut);
                }
            }
            else if (ExecutionEvent.Type.ProjectSkipped.equals(obj.type)) {
                //growTree(obj);
                //trimTree(obj);
                //GlobalOutputProcessor currently depens on skipped projects not being added to tree.
            }
            else if (ExecutionEvent.Type.ProjectSucceeded.equals(obj.type)) {
                if (MavenSettings.getDefault().isCollapseSuccessFolds()) {
                    currentTreeNode.collapseFold();
                }
                currentTreeNode.finishFold();
                trimTree(obj);
                if (contextImpl != null) {
                    ExecProject pr = (ExecProject)obj;
                    Project project = pr.findProject();
                    contextImpl.setCurrentProject(project);
                    CommandLineOutputHandler.this.processEnd(getEventId(PRJ_EXECUTE, null), stdOut);
                }
            }
            else if (ExecutionEvent.Type.ProjectFailed.equals(obj.type)) {
                currentTreeNode.finishFold();
                trimTree(obj);
                if (contextImpl != null) {
                    ExecProject pr = (ExecProject)obj;
                    Project project = pr.findProject();
                    contextImpl.setCurrentProject(project);
                    CommandLineOutputHandler.this.processEnd(getEventId(PRJ_EXECUTE, null), stdOut);
                }
            } else if (ExecutionEvent.Type.ForkStarted.equals(obj.type)) {
                growTree(obj);
            } else if (ExecutionEvent.Type.ForkedProjectStarted.equals(obj.type)) {
                growTree(obj);
            } else if (ExecutionEvent.Type.ForkFailed.equals(obj.type) || ExecutionEvent.Type.ForkSucceeded.equals(obj.type)) {
                trimTree(obj);
            } else if (ExecutionEvent.Type.ForkedProjectFailed.equals(obj.type) || ExecutionEvent.Type.ForkedProjectSucceeded.equals(obj.type)) {
                trimTree(obj);
            } else if (!MavenSettings.getDefault().isAlwaysShowOutput() && ExecutionEvent.Type.SessionEnded.equals(obj.type)) {
                for (ExecutionEventObject.Tree node : executionTree.getChildrenNodes()) {
                    if (node.getEndEvent() != null && ExecutionEvent.Type.ProjectFailed.equals(node.getEndEvent().type)) {
                        getIO().select();
                        break;
                    }
                }
            }
        }

        private String goalPrefixFromArtifactId(String mojoArtifact) {
            // XXX M3 reports artifactId of mojo whereas M2 reports goalPrefix; do not want to force every OutputProcessor to handle both
            // XXX consider searching index on ArtifactInfo.PLUGIN_PREFIX instead
            Matcher match2 = mavenSomethingPlugin.matcher(mojoArtifact);
            if (match2.matches()) {
                mojoArtifact = match2.group(1);
            } else {
                match2 = somethingMavenPlugin.matcher(mojoArtifact);
                if (match2.matches()) {
                    mojoArtifact = match2.group(1);
                }
            }
            return mojoArtifact;
        }
        
    private void growTree(ExecutionEventObject obj) {
        ExecutionEventObject.Tree tn = new ExecutionEventObject.Tree(obj, currentTreeNode);
        //fork events come before the mojo events, we want them as childs, to know what form belongs to which mojo.
        if (tn.getStartEvent().type.equals(ExecutionEvent.Type.MojoStarted) && !currentTreeNode.getChildrenNodes().isEmpty()) {
            //check if the previous fork should be added to this event
            ExecutionEventObject.Tree lastSibling = currentTreeNode.getChildrenNodes().get(currentTreeNode.getChildrenNodes().size() - 1 );
            while (lastSibling != null && lastSibling.getEndEvent() != null && (ExecutionEvent.Type.ForkFailed.equals(lastSibling.getEndEvent().type) || ExecutionEvent.Type.ForkSucceeded.equals(lastSibling.getEndEvent().type))) {
                currentTreeNode.getChildrenNodes().remove(lastSibling);
                tn.getChildrenNodes().add(0, lastSibling);
                lastSibling.reassingParent(tn);
                lastSibling = currentTreeNode.getChildrenNodes().isEmpty() ? null : currentTreeNode.getChildrenNodes().get(currentTreeNode.getChildrenNodes().size() - 1 );
            }
        }
        currentTreeNode.getChildrenNodes().add(tn);
        currentTreeNode = tn;
        currentTreeNode.setStartOffset(IOPosition.currentPosition(inputOutput));
    }

    private void trimTree(ExecutionEventObject obj) {
        if (foldsBroken) {
            return;
        }
        ExecutionEventObject start = currentTreeNode.getStartEvent();
        while (!matchingEvents(obj.type, start.type)) { //#229877
            ExecutionEventObject innerEnd = createEndForStart(start);
            processExecEvent(innerEnd);
            start = currentTreeNode.getStartEvent();
            //potentially never ending+ recursive loop, how to intercept?
        }
        currentTreeNode.setEndOffset(IOPosition.currentPosition(inputOutput));
        currentTreeNode.setEndEvent(obj);
        currentTreeNode = currentTreeNode.getParentNode();
    }

    private boolean matchingEvents(ExecutionEvent.Type typeEnd, ExecutionEvent.Type typeStart) {
        ExecutionEvent.Type match = END_TO_START_Mappings.get(typeEnd);
        assert match != null : "unknown event type:" + typeEnd;
        return typeStart.equals(match);
    }
    
    // an artificial way of creating event objects for events that didn't come the natural way of parsing the output.
    // typically happens when user stops the build or some other cases described in issue 229877
    private ExecutionEventObject createEndForStart(ExecutionEventObject start) {
        ExecutionEventObject toRet;
        if (start instanceof ExecMojo) {
            ExecMojo startEx = (ExecMojo) start;
            toRet = new ExecMojo(startEx.goal, startEx.plugin, startEx.phase, startEx.executionId, ExecutionEvent.Type.MojoFailed);
        } else if (start instanceof ExecProject) {
            ExecProject startPrj = (ExecProject) start;
            toRet = new ExecProject(startPrj.gav, startPrj.currentProjectLocation, ExecutionEvent.Type.ProjectFailed);
        } else if (start instanceof ExecSession) {
            ExecSession ss = (ExecSession) start;
            toRet = new ExecSession(ss.projectCount, ExecutionEvent.Type.SessionEnded);
        } else {
            ExecutionEvent.Type endType;
            if (start.type.equals(ExecutionEvent.Type.ForkStarted)) {
                endType = ExecutionEvent.Type.ForkFailed;
            } else if (start.type.equals(ExecutionEvent.Type.ForkedProjectStarted)) {
                endType = ExecutionEvent.Type.ForkedProjectFailed;
            } else {
                throw new RuntimeException("unknown event type: " + start.type);
            }
            toRet = new ExecutionEventObject(endType);
        }
        return toRet;
    }

    private static final Map<ExecutionEvent.Type, ExecutionEvent.Type> END_TO_START_Mappings;
    static {
        END_TO_START_Mappings = new EnumMap<>(ExecutionEvent.Type.class);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ForkFailed, ExecutionEvent.Type.ForkStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ForkSucceeded, ExecutionEvent.Type.ForkStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ForkedProjectFailed, ExecutionEvent.Type.ForkedProjectStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ForkedProjectSucceeded, ExecutionEvent.Type.ForkedProjectStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.MojoFailed, ExecutionEvent.Type.MojoStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.MojoSucceeded, ExecutionEvent.Type.MojoStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ProjectFailed, ExecutionEvent.Type.ProjectStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.ProjectSucceeded, ExecutionEvent.Type.ProjectStarted);
        END_TO_START_Mappings.put(ExecutionEvent.Type.SessionEnded, ExecutionEvent.Type.SessionEnded);
    }


    ResumeFromFinder createResumeFromFinder() {
        if (contextImpl == null) {
            if (firstFailure != null) {
                return new FindByName(firstFailure);
            }
            return null;
        }
        for (ExecutionEventObject.Tree prj : executionTree.getChildrenNodes()) {
            if (prj.getEndEvent() != null && ExecutionEvent.Type.ProjectFailed.equals(prj.getEndEvent().type)) {
                    //our first failure
                return new FindByEvents( (ExecProject) prj.getStartEvent());
            }
        }
        return null;
    }
    
    
    //old school instance relying on output only.. currently only used in maven 2.x
    private static class FindByName implements ResumeFromFinder {

        private final @NonNull String firstFailure;

        /**
         * @param firstFailure {@link MavenProject#getName}
         */
        private FindByName(@NonNull String firstFailure) {
            this.firstFailure = firstFailure;
        }

        @Override public @CheckForNull NbMavenProject find(@NonNull Project root) {
            // XXX EventSpy (#194090) would make this more reliable and efficient
            for (Project module : root.getLookup().lookup(ProjectContainerProvider.class).getContainedProjects().getProjects()) {
                if (Thread.interrupted()) {
                    break;
                }
                NbMavenProject nbmp = module.getLookup().lookup(NbMavenProject.class);
                if (nbmp == null) {
                    continue;
                }
                MavenProject mp = nbmp.getMavenProject();
                if (firstFailure.equals(mp.getName())) {
                    return nbmp;
                }
            }
            return null;
        }

    }    
    
    private static class FindByEvents implements ResumeFromFinder {
        private final ExecProject execProject;

        private FindByEvents(ExecProject execProject) {
            this.execProject = execProject;
        }
        

        @Override
        public @CheckForNull NbMavenProject find(@NonNull Project root) {
            //we don't need the parameter, we have the exec tree
            Project project = execProject.findProject();
            if (project != null) {
                return  project.getLookup().lookup(NbMavenProject.class);
            }
            return null;
        }
        
    }
    

    static class Input implements Runnable {

        private final InputOutput inputOutput;
        private final OutputStream str;
        private boolean stopIn = false;

        public Input(OutputStream out, InputOutput inputOutput) {
            str = out;
            this.inputOutput = inputOutput;
        }

        public void stopInput() {
            stopIn = true;
            // Do not close synchronously as BufferedReaders waiting on input
            // would block. See https://bugs.openjdk.java.net/browse/JDK-4859836
            PROCESSOR.post(() -> {
                try {
                    inputOutput.getIn().close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }

        public @Override void run() {
            Reader in = inputOutput.getIn();
            try (Writer out = new OutputStreamWriter(str, getPreferredCharset())) {
                while (true) {
                    int read = in.read();
                    if (read != -1) {
                        out.write(read);
                        out.flush();
                    } else {
                        return;
                    }
                    if (stopIn) {
                        return;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void checkProgress(ExecutionEventObject eeo) {
        if (ExecutionEvent.Type.ProjectDiscoveryStarted.equals(eeo.type)) {
            handle.switchToIndeterminate();
        } else if (ExecutionEvent.Type.SessionStarted.equals(eeo.type)) {
            reactorSize = ((ExecSession)eeo).projectCount + 1;
            projectCount = 0;
            handle.switchToDeterminate(reactorSize);
        } else if (ExecutionEvent.Type.ProjectStarted.equals(eeo.type)) {
            handle.progress(((ExecProject)eeo).gav.artifactId, Math.min(++projectCount, reactorSize));
        } else if (ExecutionEvent.Type.ProjectSkipped.equals(eeo.type)) {
            handle.progress(((ExecProject)eeo).gav.artifactId + " " + ((ExecProject)eeo).gav.version, Math.min(++projectCount, reactorSize));
        } else if (ExecutionEvent.Type.SessionEnded.equals(eeo.type)) {
            
        }
    }

    /**
     * #192200: try to indicate progress esp. in a reactor build.
     * @see org.apache.maven.cli.ExecutionEventLogger
     */
    //only done for maven 2.x now.
    private void checkProgress(String text) {
        switch (state) {
        case INITIAL:
            if (text.equals("Reactor Build Order:")) { // NOI18N
                state = ProgressState.GOT_REACTOR_BUILD_ORDER;
            }
            break;
        case GOT_REACTOR_BUILD_ORDER:
            if (text.trim().isEmpty()) {
                state = ProgressState.GETTING_REACTOR_PROJECTS;
            } else {
                state = ProgressState.INITIAL; // ???
            }
            break;
        case GETTING_REACTOR_PROJECTS:
            if (text.trim().isEmpty()) {
                state = ProgressState.NORMAL;
                reactorSize++; // so we do not show 100% completion while building last project
                handle.switchToDeterminate(reactorSize);
                LOG.log(java.util.logging.Level.FINE, "reactor size: {0}", reactorSize);
            } else {
                reactorSize++;
            }
            break;
        case NORMAL:
            if (forkCount == 0 && text.matches("-+")) { // NOI18N
                state = ProgressState.GOT_DASHES;
            } else if (text.startsWith(">>> ")) { // NOI18N
                forkCount++;
                LOG.log(java.util.logging.Level.FINE, "fork count up to {0}", forkCount);
            } else if (forkCount > 0 && text.startsWith("<<< ")) { // NOI18N
                forkCount--;
                LOG.log(java.util.logging.Level.FINE, "fork count down to {0}", forkCount);
            }
            break;
        case GOT_DASHES:
            if (text.startsWith("Building ") && !text.startsWith("Building in ") || text.startsWith("Skipping ")) { // NOI18N
                currentProject = text.substring(9);
                closeCurrentTag();
                handle.progress(currentProject, Math.min(++projectCount, reactorSize));
                LOG.log(java.util.logging.Level.FINE, "got project #{0}: {1}", new Object[] {projectCount, currentProject});
            }
            state = ProgressState.NORMAL;
            break;
        default:
            assert false : state;
        }
    }
    enum ProgressState {
        INITIAL,
        GOT_REACTOR_BUILD_ORDER,
        GETTING_REACTOR_PROJECTS,
        NORMAL,
        GOT_DASHES,
    }
    private ProgressState state = ProgressState.INITIAL;
    private int forkCount;
    private int reactorSize;
    private int projectCount;
    
   public static class ContextImpl implements OutputVisitor.Context {

        private Project currentProject;
        private ExecutionEventObject.Tree executionTree;

        ContextImpl() {
        }

        @Override
        public @CheckForNull Project getCurrentProject() {
            return currentProject;
        }
        
        public void setCurrentProject(@NullAllowed Project currentProject) {
            this.currentProject = currentProject;
        }

        private void setExecutionTree(ExecutionEventObject.Tree executionTree) {
            this.executionTree = executionTree;
        }

        public ExecutionEventObject.Tree getExecutionTree() {
            return executionTree;
        }
        
    }    
    
    /**
     * Returns the preferred Charset that is obtained by checking the following system properties:
     * stdout.encoding, sun.stdout.encoding, native.encoding, Charset.defaultCharset()
     * @see org.netbeans.api.extexecution.base.BaseExecutionService#getInputOutputEncoding
     * @return Charset
     */
    private static Charset getPreferredCharset() {
        // The CommandLineOutputHandler used the default charset to convert
        // output from command line invocations to strings. That encoding is
        // derived from the system file.encoding. From JDK 18 onwards its
        // default value changed to UTF-8.
        // JDK 17+ exposes the native encoding as the new system property
        // native.encoding, prior versions don't have that property and will
        // report NULL for it.
        // To account for the behavior of JEP400 the following order is used to determine the encoding:
        // stdout.encoding, sun.stdout.encoding, native.encoding, Charset.defaultCharset()
        String[] encodingSystemProperties = new String[]{"stdout.encoding", "sun.stdout.encoding", "native.encoding"};

        Charset preferredCharset = null;
        for (String encodingProperty : encodingSystemProperties) {
            String encodingPropertyValue = System.getProperty(encodingProperty);
            if (encodingPropertyValue == null) {
                continue;
            }

            try {
                preferredCharset = Charset.forName(encodingPropertyValue);
            } catch (IllegalArgumentException ex) {
                LOG.log(java.util.logging.Level.WARNING, "Failed to get charset for '" + encodingProperty + "' value : '" + encodingPropertyValue + "'", ex);
            }

            if (preferredCharset != null) {
                return preferredCharset;
            }

        }

        return Charset.defaultCharset();
    }
}


