/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                            try {
                                session.println(file, true, session.createStandardHyperlink(jspSource.getURL(), jspErrorText, lineNumber, columnNumber, -1, -1));
                            } catch (FileStateInvalidException e) {
                                assert false : e;
                            }
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
