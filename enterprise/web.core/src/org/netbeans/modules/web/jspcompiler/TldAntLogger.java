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
 * Ant logger which handles compilation of JSPs
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class, position=21)
public final class TldAntLogger extends AntLogger {
    
    /**
     * Regexp matching the compilation error from JspC when error is in tld. Sample message could look like this:
     * org.apache.jasper.JasperException: Unable to initialize TldLocationsCache: XML parsing error on file /WEB-INF/jsp2/jsp2-example-taglib.tld: (line 18, col -1)
     */
    private static final Pattern TLD_ERROR = Pattern.compile(
        "(.*)(org.apache.jasper.JasperException:)(.*)( file )(.*)"); // NOI18N

    private static final Pattern FILE_PATTERN = Pattern.compile(
        "([^\\(]*)(: )\\(line ([0-9]+), col ([0-9-]+)\\)"); // NOI18N

    private static final String[] TASKS_OF_INTEREST = AntLogger.ALL_TASKS;
    
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO, // XXX is this needed?
        AntEvent.LOG_WARN, // XXX is this needed?
        AntEvent.LOG_ERR, // XXX is this needed?
    };
    
    private static final Logger ERR = Logger.getLogger(TldAntLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(Level.FINE);
    
    /** Default constructor for lookup. */
    public TldAntLogger() {
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

        Matcher m = TLD_ERROR.matcher(line);
        if (m.matches()) { //it's our error
            if (LOGGABLE) ERR.log(Level.FINE, "matched line: " + line);
            // print the exception and error statement first
            String errorText = m.group(3) + m.group(4);
            session.println(m.group(2) + errorText, true, null);
            
            // get the file from the line
            String filePart = m.group(5).trim();
            if (LOGGABLE) ERR.log(Level.FINE, "file part: " + filePart);
            
            // now create hyperlink for the file
            Matcher fileMatcher = FILE_PATTERN.matcher(filePart);
            if (fileMatcher.matches()) {
                String tldFile = fileMatcher.group(1).trim();
                if (LOGGABLE) ERR.log(Level.FINE, "tld file: " + tldFile);

                int lineNumber = Integer.parseInt(fileMatcher.group(3));
                int columnNumber = Integer.parseInt(fileMatcher.group(4));
                if (LOGGABLE) ERR.log(Level.FINE, "linking line: " + lineNumber + ", column: " + columnNumber);

                File scriptLoc = event.getScriptLocation();
                FileObject scriptLocFO = FileUtil.toFileObject(scriptLoc);
                WebModule wm = WebModule.getWebModule(scriptLocFO);
                if (LOGGABLE) ERR.log(Level.FINE, "wm: " + wm);
                
                if (wm == null) {
                    session.println(tldFile, true, null);
                    event.consume();
                    return;
                }
                
                FileObject tldSource = wm.getDocumentBase().getFileObject(tldFile);
                if (LOGGABLE) ERR.log(Level.FINE, "tldSource: " + tldSource);
                
                if (tldSource == null) {
                    session.println(tldFile, true, null);
                    event.consume();
                    return;
                }
                
                if (messageLevel <= sessionLevel && !event.isConsumed()) {
                    session.println(tldFile, true, session.createStandardHyperlink(tldSource.toURL(), errorText + tldFile, lineNumber, columnNumber, -1, -1));
                }
            }
            event.consume();
        }
    }   
}
