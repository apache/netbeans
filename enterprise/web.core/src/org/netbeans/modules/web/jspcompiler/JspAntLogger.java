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

package org.netbeans.modules.web.jspcompiler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Ant logger which handles compilation of JSPs, both the JSP -> Java and 
 * the Java -> class compilation stages.
 * Specifically, handles hyperlinking of errors from JspC and from Javac run on 
 * classes generated from JSPs.
 * @author Petr Jiricka, Jesse Glick
 * @see "#42525"
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class, position=20)
public final class JspAntLogger extends AntLogger {
    
    /**
     * Regexp matching the compilation error from JspC. Sample message could look like this:
     * org.apache.jasper.JasperException: file:C:/project/AntParseTestProject2/build/web/index.jsp(6,0) Include action: Mandatory attribute page missing
     */
    private static final Pattern JSP_COMPILER_ERROR = Pattern.compile(
        "(.*)(org.apache.jasper.JasperException: file:)(.*)"); // NOI18N

    private static final Pattern FILE_PATTERN = Pattern.compile(
        "([^\\(]*)\\(([0-9]+),([0-9]+)\\)"); // NOI18N

    private static final String[] TASKS_OF_INTEREST = AntLogger.ALL_TASKS;
    
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO, // XXX is this needed?
        AntEvent.LOG_WARN, // XXX is this needed?
        AntEvent.LOG_ERR, // XXX is this needed?
    };
    
    private static final Logger ERR = Logger.getLogger(JspAntLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(Level.FINE);
    
    /** Default constructor for lookup. */
    public JspAntLogger() {
    }
    
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
    public boolean interestedInScript(File script, AntSession session) {
        return true;
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

    @Override
    public void messageLogged(AntEvent event) {
        AntSession session = event.getSession();
        int messageLevel = event.getLogLevel();
        int sessionLevel = session.getVerbosity();
        String line = event.getMessage();
        assert line != null;

        // XXX only check when the task is correct
        Matcher m = JSP_COMPILER_ERROR.matcher(line);
        if (m.matches()) { //it's our error
            if (LOGGABLE) ERR.log(Level.FINE, "matched line: " + line);
            // print the exception and error statement first
            String jspErrorText = line.substring(line.lastIndexOf(')')+1);
            session.println(line.substring(0, line.indexOf("file:")) + jspErrorText, true, null);
            
            // get the files from the line
            String filePart = line.substring(line.indexOf("file"), line.lastIndexOf(')')+1);
            if (LOGGABLE) ERR.log(Level.FINE, "file part: " + filePart);
            
            // now create hyperlinks for all the files
            int startIndex = 0;
            while (filePart.indexOf("file:", startIndex) > -1) { 
                int start = filePart.indexOf("file:", startIndex) + 5;
                int end = filePart.indexOf(')', startIndex) + 1;
                startIndex = end;
                String file = filePart.substring(start, end);
                if (LOGGABLE) ERR.log(Level.FINE, "file: " + file);

                // we've got the info for one file extracted, now extract the line/column and actual filename
                Matcher fileMatcher = FILE_PATTERN.matcher(file);
                if (fileMatcher.matches()) {
                    String jspFile      = fileMatcher.group(1).trim();
                    int lineNumber      = Integer.parseInt(fileMatcher.group(2));
                    int columnNumber    = Integer.parseInt(fileMatcher.group(3)) + 1;
                    if (LOGGABLE)  ERR.log(Level.FINE, "linking line: " + lineNumber + ", column: " + columnNumber);
                    
                    File f = new File(jspFile);
                    FileObject fo = FileUtil.toFileObject(f);
                    // Check to see if this JSP is in the web module.
                    FileObject jspSource = getResourceInSources(fo);
                    // and create the hyperlink if needed
                    if (jspSource != null) {
                        if (messageLevel <= sessionLevel && !event.isConsumed()) {
                            session.println(file, true, session.createStandardHyperlink(jspSource.toURL(), jspErrorText, lineNumber, columnNumber, -1, -1));
                        }
                    }
                }
            }
            event.consume();
        }
    }
    
    
    /** Given a resource in the build directory, returns the corresponding resource in the source directory.
     */
    private static FileObject getResourceInSources(FileObject inBuild) {
        if (inBuild == null) {
            return null;
        }
        WebModule wm = WebModule.getWebModule(inBuild);
        if (wm != null) {
            // get the mirror of this page in sources
            FileObject webBuildDir = guessWebModuleOutputRoot(wm, inBuild);
            if (webBuildDir != null) {
                String jspResourcePath = FileUtil.getRelativePath(webBuildDir, inBuild);
                return wm.getDocumentBase().getFileObject(jspResourcePath);
            }

        }
        return null;
    }
    
    private static FileObject guessWebModuleOutputRoot(WebModule wm, FileObject fo) {
        FileObject potentialRoot = fo.getParent();
        while (potentialRoot != null) {
            if (potentialRoot.getFileObject("WEB-INF") != null) {
                return potentialRoot;
            }
            potentialRoot = potentialRoot.getParent();
        }
        return null;
    }
   
}
