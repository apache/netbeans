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
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author mkleint
 */
public class JavadocOutputProcessor implements OutputProcessor {
    
    private static final String[] JAVADOCGOALS = new String[] {
        "mojo-execute#javadoc:javadoc" //NOI18N
    };
    private Pattern index;
    private String path;
    
    /** Creates a new instance of JavadocOutputProcessor */
    public JavadocOutputProcessor() {
        index = Pattern.compile("Generating (.*)index\\.html.*", Pattern.DOTALL); //NOI18N
    }
    
    @Override
    public String[] getRegisteredOutputSequences() {
        return JAVADOCGOALS;
    }
    
    @Override
    public void processLine(String line, OutputVisitor visitor) {
        Matcher match = index.matcher(line);
        if (match.matches()) {
            path = match.group(1);
        }
    }
    
    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        path = null;
    }
    
    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        if (path != null) {
            visitor.setLine("View Generated javadoc at " + path); //NOI18N - shows up in maven output.
            visitor.setOutputListener(new Listener(path), false);
        }
    }
    
    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class Listener implements OutputListener {
        private String root;
        private Listener(String path) {
            root = path;
        }
        @Override
        public void outputLineSelected(OutputEvent arg0) {
            
        }
        
       @Override
       public void outputLineAction(OutputEvent arg0) {
            File javadoc = FileUtil.normalizeFile(new File(root));
            FileUtil.refreshFor(javadoc);
            FileObject fo = FileUtil.toFileObject(javadoc);
            if (fo != null) {
                FileObject index = fo.getFileObject("index.html"); //NOI18N
                if (index != null) {
                    URL link = URLMapper.findURL(index, URLMapper.EXTERNAL);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(link);
                }
            }
        }
        
        @Override
        public void outputLineCleared(OutputEvent arg0) {
        }
    }
}
