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

package org.netbeans.modules.maven.output;

import java.io.File;
import java.net.MalformedURLException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import static org.netbeans.modules.maven.output.Bundle.*;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author mkleint
 */
public class SiteOutputProcessor implements OutputProcessor {
    
    private static final String[] SITEGOALS = new String[] {
        "mojo-execute#site:site" //NOI18N
    };
    private Project project;
    
    /** Creates a new instance of SiteOutputProcessor */
    public SiteOutputProcessor(Project prj) {
        this.project = prj;
    }
    
    @Override
    public String[] getRegisteredOutputSequences() {
        return SITEGOALS;
    }
    
    @Override
    public void processLine(String line, OutputVisitor visitor) {
    }
    
    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }
    
    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        //now that in m3 site plugin embeds other plugin's execution, the eventspy will report site as started and within the site execution report other sequences
        // (maybe) ideally we would only let site plugin to process output and start/end sequences not owned by child executions.
        if ("mojo-execute#site:site".equals(sequenceId)) {
            visitor.setLine("     View Generated Project Site"); //NOI18N shows up in maven output.
            OutputVisitor.Context con = visitor.getContext();
            if (con != null && con.getCurrentProject() != null) {
                visitor.setOutputListener(new Listener(con.getCurrentProject()), false);
            } else {
                //hope for the best, but generally the root project might not be the right project to use.
                visitor.setOutputListener(new Listener(project), false);
            }
        }
    }
    
    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class Listener implements OutputListener {
        private final Project prj;
        private Listener(Project prj) {
            this.prj = prj;
        }
        @Override
        public void outputLineSelected(OutputEvent arg0) {
            
        }
        
        @Messages({"# {0} - file name", "SiteOutputProcessor.not_found=No site index created at {0}"})
        @Override
        public void outputLineAction(OutputEvent arg0) {
            File html = new File(FileUtil.toFile(prj.getProjectDirectory()), "target/site/index.html");
            if (html.isFile()) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(html).toURL());
                } catch (MalformedURLException x) {
                    assert false : x;
                }
            } else {
                StatusDisplayer.getDefault().setStatusText(SiteOutputProcessor_not_found(html));
            }
        }
        
        @Override
        public void outputLineCleared(OutputEvent arg0) {
        }
    }
}
