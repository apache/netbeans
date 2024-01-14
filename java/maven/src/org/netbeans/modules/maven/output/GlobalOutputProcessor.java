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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.execution.ExecutionEvent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.modules.maven.execute.CommandLineOutputHandler;
import org.netbeans.modules.maven.execute.cmd.ExecutionEventObject;
import org.netbeans.modules.maven.options.MavenOptionController;
import static org.netbeans.modules.maven.output.Bundle.*;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * processing start, end and steps of build process
 * @author Milos Kleint
 */
public class GlobalOutputProcessor implements OutputProcessor {
    private static final String SECTION_SESSION = "session-execute"; //NOI18N
    private static final Pattern LOW_MVN = Pattern.compile("(.*)Error resolving version for (.*): Plugin requires Maven version (.*)"); //NOI18N
    private static final Pattern HELP = Pattern.compile("(?:\\[ERROR\\] )?\\[Help \\d+\\] (https?://.+)"); // NOI18N
    /**
     * @see org.apache.maven.model.building.ModelProblemUtils#formatLocation
     * @see org.apache.maven.model.building.ModelBuildingException#toMessage
     * @see org.apache.maven.DefaultMaven#collectProjects
     */
    static final Pattern MODEL_PROBLEM = Pattern.compile(".+ @ (?:\\S+, (.+), )?line (\\d+), column (\\d+)");
    static final Pattern MODEL_PROBLEM2 = Pattern.compile(".*Non-parseable POM (.+)pom.xml: .* @ (.*pom.xml)?,? ?line (\\d+), column (\\d+) .*");
    
    private static final Logger LOG = Logger.getLogger(GlobalOutputProcessor.class.getName());

    private final RunConfig config;
    
    private boolean processReactorSummary = false;
    private Iterator<ExecutionEventObject.Tree> projectIterator;
    
    GlobalOutputProcessor(RunConfig config) {
        this.config = config;
    }
    
    @Override public String[] getRegisteredOutputSequences() {
        return new String[] {SECTION_SESSION};
    }

    @Messages("TXT_ChangeSettings=NetBeans: Click here to change your settings.")
    @Override public void processLine(String line, OutputVisitor visitor) {

        //silly prepend of  [INFO} to reuse the same regexp
        if (CommandLineOutputHandler.startPatternM3.matcher("[INFO] " + line).matches() || CommandLineOutputHandler.startPatternM2.matcher("[INFO] " + line).matches()) {
            visitor.setOutputType(IOColors.OutputType.LOG_DEBUG);
            return;
        }
        if (line.startsWith("BUILD ")) {
            if (line.startsWith("BUILD SUCCESS")) {
                visitor.setOutputType(IOColors.OutputType.LOG_SUCCESS);
                return;
            } else if (line.startsWith("BUILD FAILURE"))  {
                visitor.setOutputType(IOColors.OutputType.LOG_FAILURE);
                return; 
            }
        }

        //reactor summary processing ---- 
        if (line.startsWith("Reactor Summary:")) {
            processReactorSummary = true;
            CommandLineOutputHandler.ContextImpl context = (CommandLineOutputHandler.ContextImpl) visitor.getContext();
            if (context != null) {
                projectIterator = context.getExecutionTree().getChildrenNodes().iterator();
            }
            return;
        }
        if (processReactorSummary && projectIterator != null) {
            if (CommandLineOutputHandler.reactorSummaryLine.matcher(line).matches() && projectIterator.hasNext()) {
                final ExecutionEventObject.Tree next = projectIterator.next();
                boolean projectFailed = next.getEndEvent() != null ? ExecutionEvent.Type.ProjectFailed.equals(next.getEndEvent().type) : false; //#234614 - in some cases the reactor shows before we get the event for project build finish. maybe nested build somehow?
                boolean lineFailed = line.contains(" FAILURE ");
                if (lineFailed != projectFailed) {
                    LOG.log(Level.INFO, "Maven Project Reactor summary out of sync for:" + line);
                } else if (projectFailed) {
                    //visitor.setColor(Color.RED);
                    visitor.setOutputListener(new OutputListener() {

                        @Override
                        public void outputLineSelected(OutputEvent ev) {
                        }
                        @Override 
                        public void outputLineAction(OutputEvent ev) {
                            RequestProcessor.getDefault().post(next.getEndOffset()::scrollTo);
                        }

                        @Override
                        public void outputLineCleared(OutputEvent ev) {
                        }
                    });
                }
            }
        }
        //reactor summary processing ----end 
        
        
        
        if (LOW_MVN.matcher(line).matches()) {
            visitor.setLine(line + '\n' + TXT_ChangeSettings());
            visitor.setOutputType(IOColors.OutputType.LOG_FAILURE);
            visitor.setOutputListener(new OutputListener() {
                @Override public void outputLineSelected(OutputEvent ev) {}
                @Override public void outputLineAction(OutputEvent ev) {
                    OptionsDisplayer.getDefault().open(JavaOptions.JAVA + "/" + MavenOptionController.OPTIONS_SUBPATH); //NOI18N
                }
                @Override public void outputLineCleared(OutputEvent ev) {}
            });
            return;
        }
        final Matcher m = HELP.matcher(line);
        if (m.matches()) {
            visitor.setOutputListener(new OutputListener() {
                public @Override void outputLineAction(OutputEvent ev) {
                    try {
                        URLDisplayer.getDefault().showURLExternal(new URL(m.group(1)));
                    } catch (MalformedURLException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                public @Override void outputLineSelected(OutputEvent ev) {}
                public @Override void outputLineCleared(OutputEvent ev) {}
            });
            return;
        }
        final Matcher m2 = MODEL_PROBLEM.matcher(line);
        if (m2.matches()) {
            visitor.setOutputListener(new OL(m2.group(1), Integer.parseInt(m2.group(2)), Integer.parseInt(m2.group(3)), config));
        }
        final Matcher m3 = MODEL_PROBLEM2.matcher(line);
        if (m3.matches()) {
            //when in parent pom, the 2nd group is the location of the file, otherwise use the first group.
            String loc = m3.group(2) != null ? m3.group(2) : m3.group(1) + "pom.xml";
            visitor.setOutputListener(new OL(loc, Integer.parseInt(m3.group(3)), Integer.parseInt(m3.group(4)), config));
        }
    }
    
    private static class OL implements OutputListener {
        private final int column;
        private final int line;
        private final String loc;
        private final RunConfig config;

        public OL(String loc, int line, int column, RunConfig config) {
            this.loc = loc;
            this.line = line;
            this.column = column;
            this.config = config;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {

            File pom;
            if (loc == null) {
                pom = new File(config.getExecutionDirectory(), "pom.xml");
            } else {
                pom = FileUtilities.convertStringToFile(loc);
            }
            FileObject pomFO = FileUtil.toFileObject(pom);
            if (pomFO == null) {
                LOG.log(Level.WARNING, "no such file: {0}", pom);
                return;
            }

            DataObject pomDO;
            try {
                pomDO = DataObject.find(pomFO);
            } catch (DataObjectNotFoundException x) {
                LOG.log(Level.INFO, null, x);
                return;
            }
            LineCookie lc = pomDO.getLookup().lookup(LineCookie.class);
            if (lc == null) {
                LOG.log(Level.WARNING, "no LineCookie in {0}", pom);
                return;
            }
            try {
                lc.getLineSet().getOriginal(line - 1).show(ShowOpenType.REUSE, ShowVisibilityType.FOCUS, column - 1);
            } catch (IndexOutOfBoundsException x) {
                LOG.log(Level.WARNING, "no such line {0} in {1}: {2}", new Object[]{line, pom, x});
            }
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
            
    }

    @Override public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }

    @Override public void sequenceEnd(String sequenceId, OutputVisitor visitor) {}

    @Override public void sequenceFail(String sequenceId, OutputVisitor visitor) {}
    
}
