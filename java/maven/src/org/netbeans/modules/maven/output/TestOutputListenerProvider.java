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
package org.netbeans.modules.maven.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputUtils;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.modules.maven.api.output.TestOutputObserver;
import static org.netbeans.modules.maven.output.Bundle.*;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


/**
 * processing test (surefire) output
 * @author  Milos Kleint
 */
public class TestOutputListenerProvider implements OutputProcessor {
        
    private static final String[] TESTGOALS = new String[] {
        "mojo-execute#surefire:test",
        "mojo-execute#failsafe:integration-test" 
    };
    private final Pattern failSeparatePattern;
    private final Pattern failWindowsPattern1;
    private final Pattern failWindowsPattern2;
    private final Pattern outDirPattern;
    private final Pattern outDirPattern2;
    private final Pattern runningPattern;
    private final Pattern runningPattern2;
    
    private static final Logger LOG = Logger.getLogger(TestOutputListenerProvider.class.getName());

    
    String outputDir;
    String runningTestClass;
    private String delayedLine;
    
    /** Creates a new instance of TestOutputListenerProvider */
    public TestOutputListenerProvider() {
        failSeparatePattern = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*(?:\\[surefire\\] )?Tests run.*[<]* FAILURE[!]*(.*)", Pattern.DOTALL); //NOI18N
        failWindowsPattern1 = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*(?:\\[surefire\\] )?Tests run.*", Pattern.DOTALL); //NOI18N
        failWindowsPattern2 = Pattern.compile("(?:\\[[a-zA-Z]+\\])?.*[<]* FAILURE [!]*.*", Pattern.DOTALL); //NOI18N
        runningPattern = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*(?:\\[surefire\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*(?:Surefire)?(?:Failsafe)? report directory\\: (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*Setting reports dir\\: (.*)", Pattern.DOTALL); //NOI18N
        runningPattern2 = Pattern.compile("(?:\\[[a-zA-Z]+\\])?\\s*---\\smaven-surefire-plugin:\\d+(?:.\\d+)+[-_a-zA-Z0-9]*:test\\s.*", Pattern.DOTALL);
    }
    
    public String[] getWatchedGoals() {
        return TESTGOALS;
    }

    @Override
    public void processLine(String line, OutputVisitor visitor) {
        if (delayedLine != null) {
            Matcher match = failWindowsPattern2.matcher(line);
            if (match.matches()) {
                visitor.setOutputListener(new TestOutputListener(runningTestClass, outputDir), true);
                visitor.setLine(delayedLine + line);
            } else {
                visitor.setLine(delayedLine + "\n" + line);
            }
            delayedLine = null;
        }
        Matcher match = outDirPattern.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        match = runningPattern.matcher(line);
        if (match.matches()) {
            runningTestClass = match.group(1);
            return;
        }
        match = runningPattern2.matcher(line);
        if (match.matches()) {
            try {
                OutputVisitor.Context context = visitor.getContext();
                if (context != null) {
                    Project currentProject = context.getCurrentProject();
                    if (currentProject != null) {
                        Object defaultValue = PluginPropertyUtils.createEvaluator(currentProject)
                                                                 .evaluate("${project.build.directory}/surefire-reports");
                        if (defaultValue instanceof String) {
                            outputDir = (String) defaultValue;
                            // don't want to create link on the surefire line
        //                    visitor.setOutputListener(new TestOutputListener(runningTestClass, outputDir), true);
                        }
                    }
                }
                return;
            } catch (ExpressionEvaluationException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        match = failSeparatePattern.matcher(line);
        if (match.matches()) {
            visitor.setOutputListener(new TestOutputListener(runningTestClass, outputDir), true);
            return;
        }
        match = failWindowsPattern1.matcher(line);
        if (match.matches()) {
            //we should not get here but possibly can on windows..
            visitor.skipLine();
            delayedLine = line;
        }
        
    }
    
    @Override
    public String[] getRegisteredOutputSequences() {
        return TESTGOALS;
    }
    
    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }
    
    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
    }
    
    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class TestOutputListener implements OutputListener {
        private final String testname;
        private final String outputDir;
        private final Pattern testNamePattern = Pattern.compile(".*\\((.*)\\).*<<< (?:FAILURE)?(?:ERROR)?!\\s*"); //NOI18N
        
        public TestOutputListener(String test, String outDir) {
            testname = test;
            outputDir = outDir;
        }
        /** Called when a line is selected.
         * @param ev the event describing the line
         */
        @Override
        public void outputLineSelected(OutputEvent ev) {
        }
        
        /** Called when some sort of action is performed on a line.
         * @param ev the event describing the line
         */
        @Messages({
            "MSG_CannotFollowLink1=Cannot follow link. Test output directory is missing.",
            "MSG_CannotFollowLink2=Cannot follow link. Test report file is missing.",
            "MSG_CannotFollowLink3=Cannot follow link. Report file now owned by a maven project."
        })
        @Override
        public void outputLineAction(OutputEvent ev) {
            FileObject outDir = null;
            if (outputDir != null) {
                File fl = FileUtil.normalizeFile(new File(outputDir));
                FileUtil.refreshFor(fl);
                outDir = FileUtil.toFileObject(fl);
            } 
            if (outDir == null) {
                LOG.log(Level.INFO, "Cannot find path {0} to follow link in Output Window.", outputDir); //NOI18N
                StatusDisplayer.getDefault().setStatusText(MSG_CannotFollowLink1());
                return;
            }
            outDir.refresh();

            Project prj = FileOwnerQuery.getOwner(outDir);
            if (prj != null) {
                NbMavenProjectImpl nbprj = prj.getLookup().lookup(NbMavenProjectImpl.class);
                
                if (nbprj == null) {
                    LOG.log(Level.INFO, "Cannot find owning maven project for {0} to follow link in Output Window.", outputDir); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(MSG_CannotFollowLink3());                    
                    return;
                }
            
                String reportNameSuffix = PluginPropertyUtils.getPluginProperty(nbprj.getOriginalMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "reportNameSuffix", "test", "surefire.reportNameSuffix");
                String suffix = reportNameSuffix;
                if (suffix == null) {
                    suffix = "";
                } else {
                    //204480
                    suffix = "-" + suffix;
                }
                FileObject report = outDir.getFileObject(testname + suffix + ".txt"); //NOI18N
                String tsd = nbprj.getOriginalMavenProject().getBuild().getTestSourceDirectory();
                if (tsd == null) {
                    //#205722 while we were executing tests, someone broke the pom and we don't get the proper test source directory.
                    //try getting away with the default location
                    tsd = new File(FileUtil.toFile(prj.getProjectDirectory()), "src" + File.separator + "test" + File.separator + "java").getAbsolutePath();
                }
                File testDir = new File(tsd);

                if (report != null) {
                    String nm = testname.lastIndexOf('.') > -1  //NOI18N
                            ? testname.substring(testname.lastIndexOf('.'))  //NOI18N
                            : testname;
                    openLog(report, nm, testDir);
                } else {
                    LOG.log(Level.INFO, "Cannot find report path {0}{1}.txt to follow link in Output Window.", new Object[]{outputDir, testname}); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(MSG_CannotFollowLink2());
                }
            }
        }
        
        /** Called when a line is cleared from the buffer of known lines.
         * @param ev the event describing the line
         */
        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
        
        private void openLog(final FileObject fo, String title, final File testDir) {
            try {
                IOProvider.getDefault().getIO(title, false).getOut().reset();
            } catch (Exception exc) {
                ErrorManager.getDefault().notify(exc);
            }
            final InputOutput io = IOProvider.getDefault().getIO(title, false);
            io.select();

            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader = null;
                    OutputWriter writer = io.getOut();
                    String line;
                    Collection<? extends TestOutputObserver> observers = getObservers();
                    try {
                        reader = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                        ClassPath classPath = null;
                        Project project = null;
                        while ((line = reader.readLine()) != null) {
                            Matcher m = testNamePattern.matcher(line);
                            if (m.matches()) {
                                String testClassName = m.group(1).replace('.', File.separatorChar) + ".java"; //NOI18N
                                File testClassFile = new File(testDir, testClassName);
                                FileObject testFileObject = FileUtil.toFileObject(testClassFile);
                                if (testFileObject != null) {
                                    classPath = ClassPath.getClassPath(testFileObject, ClassPath.EXECUTE);
                                    project = FileOwnerQuery.getOwner(testFileObject);
                                }
                            }
                            // call observers
                            for (TestOutputObserver o : observers) {
                                o.processLine(line, project);
                            }
                            if (classPath != null) {
                                OutputListener list = OutputUtils.matchStackTraceLine(line, classPath);
                                if (list != null) {
                                    writer.println(line, list, true);
                                } else {
                                    writer.println(line);
                                }
                            } else {
                                writer.println(line);
                            }
                        }
                    } catch (IOException exc) {
                        ErrorManager.getDefault().notify(exc);
                    } finally {
                        writer.close();
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }

                private Collection<? extends TestOutputObserver> getObservers() {
                    Lookup.Result<TestOutputObserver> result = Lookup.getDefault().lookupResult(TestOutputObserver.class);
                    return result.allInstances();
                }
            });
        }
    }
}
