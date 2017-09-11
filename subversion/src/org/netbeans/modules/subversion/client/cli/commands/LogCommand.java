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

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Stupka
 */
public class LogCommand extends SvnCommand {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {        
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));       
    }
    
    private byte[] output;

    private enum LogType {
        file,
        url
    }
    
    private final LogType type;
    private final File file;
    private final SVNRevision revStart;
    private final SVNRevision revEnd;    
    private final SVNRevision pegRevision;    
    private final boolean stopOnCopy;
    private final boolean fetchChangePath;
    private final long limit;

    private final SVNUrl url;
    private final String[] paths;
    
        
    public LogCommand(File file, SVNRevision revStart, SVNRevision revEnd, SVNRevision pegRevision, boolean stopOnCopy, boolean fetchChangePath, long limit) {
        this.file = file;
        this.revStart = revStart;
        this.revEnd = revEnd;
        this.pegRevision = pegRevision;
        this.stopOnCopy = stopOnCopy;
        this.fetchChangePath = fetchChangePath;
        this.limit = limit;
           
        this.type = LogType.file;
        
        url = null;
        paths = null;
    }

    public LogCommand(SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, SVNRevision pegRevision, boolean stopOnCopy, boolean fetchChangePath, long limit) {        
        this.revStart = revStart;
        this.revEnd = revEnd;
        this.pegRevision = pegRevision;
        this.stopOnCopy = stopOnCopy;
        this.fetchChangePath = fetchChangePath;
        this.limit = limit;
        this.url = url;
        this.paths = paths;
                        
        this.type = LogType.url;
        
        file = null;
    }

    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }    
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.LOG;
    }
    
    @Override
    public void output(byte[] bytes) {
        super.output(bytes);
        output = bytes;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        // verbose
        arguments.add("log");        
        arguments.add(revStart, revEnd);
        
        switch(type) {
            case file:
                if (pegRevision == null) {
                    arguments.add(file);
                } else {
                    arguments.add(file.getAbsolutePath() + "@" + pegRevision);
                }
                break;
            case url:
                if (pegRevision == null) {
                    arguments.add(url);
                } else {
                    arguments.add(url, pegRevision);
                }
                if (paths != null) {            
                    arguments.addPathArguments(paths);
                }        
                break;
            default:
                throw new IllegalStateException("Illegal logtype: " + type);          
        }             
        arguments.add("--xml");        
        if(fetchChangePath) {
            arguments.add("-v");            
        }
        if (stopOnCopy) {
            arguments.add("--stop-on-copy");   
        }            
        if (limit > 0) {
            arguments.add("--limit");
            arguments.add(Long.toString(limit));
        }
    }

    public ISVNLogMessage[] getLogMessages() throws SVNClientException {
        if (output == null || output.length == 0) return new ISVNLogMessage[0];
        try {
            XMLReader saxReader = XMLUtil.createXMLReader();

            XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
            saxReader.setContentHandler(xmlEntriesHandler);
            saxReader.setErrorHandler(xmlEntriesHandler);
            InputSource source = new InputSource(new ByteArrayInputStream(output));

            saxReader.parse(source);
            return xmlEntriesHandler.getLog();
            
        } catch (SAXException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        
    }
    
    private class XmlEntriesHandler extends DefaultHandler {

        //<logentry revision="6">
        //  <author>geronimo</author>
        //      <date>2008-06-26T20:00:28.132008Z</date>
        //          <paths>
        //              <path action="A">foo</path>
        //              <path action="A">bar</path>
        //          </paths>
        //				<msg>whatever</msg>
        //			</logentry>
                
        //private static final String PATHS_ELEMENT_NAME      = "paths";    // NOI18N
        private static final String PATH_ELEMENT_NAME       = "path";       // NOI18N
        private static final String ENTRY_ELEMENT_NAME      = "logentry";   // NOI18N
        private static final String MSG_ELEMENT_NAME        = "msg";        // NOI18N
        private static final String AUTHOR_ELEMENT_NAME     = "author";     // NOI18N
        private static final String DATE_ELEMENT_NAME       = "date";       // NOI18N
        
        private static final String ACTION_ATTRIBUTE        = "action";     // NOI18N                
        private static final String REVISION_ATTRIBUTE      = "revision";   // NOI18N
        
        private List<ISVNLogMessage> logs = new ArrayList<ISVNLogMessage>();        
        
        
        private Map<String, Object> values;
        private String tag;
        
        private class Path {
            char action;
            String path = "";
            String copyRev;
            String copyPath;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            tag = qName.trim();                
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                        
                values = new HashMap<String, Object>();
                values.put(REVISION_ATTRIBUTE, elementAttributes.getValue(REVISION_ATTRIBUTE));            
            } else if (PATH_ELEMENT_NAME.equals(qName)) {                                
                List<Path> paths = getPathList();
                Path path = new Path();
                path.action = elementAttributes.getValue(ACTION_ATTRIBUTE).charAt(0);
                path.copyPath = elementAttributes.getValue("copyfrom-path");
                path.copyRev = elementAttributes.getValue("copyfrom-rev");
                paths.add(path);                
            } else if(values != null) {
                values.put(tag, "");
            }            
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(values == null || tag == null) {
                return;
            }
            String s = toString(length, ch, start);            
            if(tag.equals(PATH_ELEMENT_NAME)) {
                List<Path> paths = getPathList();
                paths.get(paths.size() - 1).path = paths.get(paths.size() - 1).path + s;
            } else {
                Object v = values.get(tag);
                if(v == null) {
                    values.put(tag, s);
                } else {
                    values.put(tag, v + s);
                }
            }            
        }                
        
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            tag = null;
            if (ENTRY_ELEMENT_NAME.equals(name)) {                      
                if(values != null) {
                                                                                       
                    String author = (String) values.get(AUTHOR_ELEMENT_NAME);
                    if(author == null) author = "";
                    Date date = null;
                    String dateValue = (String) values.get(DATE_ELEMENT_NAME);                                                
                    if (dateValue == null) throw new SAXException("'date' tag expected under 'logentry'");                        
                    try {
                        date = dateFormat.parse(dateValue);
                    } catch (ParseException ex) {
                        // ignore
                        
                    }
                    String msg = (String) values.get(MSG_ELEMENT_NAME);
                    if(msg == null) msg = "";

                    SVNRevision.Number rev = getRevision((String) values.get(REVISION_ATTRIBUTE));
                    
                    List<Path> pathsList = getPathList();
                    ISVNLogMessageChangePath[] paths;  
                    if(pathsList.size() > 0) {
                        paths = new SVNLogMessageChangePath[pathsList.size()];
                        for (int i = 0; i < pathsList.size(); i++) {
                            Path path = pathsList.get(i);
                            paths[i] = new SVNLogMessageChangePath(path.path, getRevision(path.copyRev), path.copyPath, path.action);
                        }
                    } else {
                        paths = new SVNLogMessageChangePath[] {};
                    }
                        
                    logs.add(new LogMessage(msg, rev, author, date, paths));
                    
                    values = null;
                }
            }
        }
                
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public ISVNLogMessage[] getLog() {            
            return logs.toArray(new ISVNLogMessage[logs.size()]);
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }
        
        private List<Path> getPathList() {
            List<Path>paths = (List<Path>) values.get(PATH_ELEMENT_NAME);
            if(paths == null) {
                paths = new ArrayList<Path>();
                values.put(PATH_ELEMENT_NAME, paths);
            }
            return paths;
        }        
        
        private Number getRevision(String revisionValue) {
            Number rev = null;
            if (revisionValue != null && !revisionValue.trim().equals("")) {
                try {
                    rev = new SVNRevision.Number(Long.parseLong(revisionValue));
                } catch (NumberFormatException e) {
                    // ignore
                    new SVNRevision.Number(-1);
                }
            }
            return rev;
        }        
    }        
    
    private class LogMessage implements ISVNLogMessage {
        private final String msg;
        private final Number rev;
        private final String author;
        private final Date date;
        private final ISVNLogMessageChangePath[] paths;
        public LogMessage(String msg, Number rev, String author, Date date, ISVNLogMessageChangePath[] paths) {
            this.msg = msg;
            this.rev = rev;
            this.author = author;
            this.date = date;
            this.paths = paths;
        }
        public Number getRevision() {
            return rev;
        }
        public String getAuthor() {
            return author;
        }
        public Date getDate() {
            return date;
        }
        public String getMessage() {
            return msg;
        }
        public ISVNLogMessageChangePath[] getChangedPaths() {
            return paths;
        }

        public long getTimeMicros() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getTimeMillis() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getNumberOfChildren() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ISVNLogMessage[] getChildMessages() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addChild(ISVNLogMessage arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasChildren() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }    
            
}
