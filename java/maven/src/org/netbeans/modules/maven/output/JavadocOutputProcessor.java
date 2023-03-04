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
