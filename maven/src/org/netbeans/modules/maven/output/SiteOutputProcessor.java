/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
