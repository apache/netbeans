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
package org.netbeans.modules.subversion.remote.client.cli.commands;

import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessageChangePath;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNLogMessageChangePath;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * 
 */
public class LogCommand extends SvnCommand {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //NOI18N
    static {        
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); //NOI18N
    }
    
    private byte[] output;

    private enum LogType {
        file,
        url
    }
    
    private final LogType type;
    private final VCSFileProxy file;
    private final SVNRevision revStart;
    private final SVNRevision revEnd;    
    private final SVNRevision pegRevision;    
    private final boolean stopOnCopy;
    private final boolean fetchChangePath;
    private final long limit;

    private final SVNUrl url;
    private final String[] paths;
    
        
    public LogCommand(FileSystem fileSystem, VCSFileProxy file, SVNRevision revStart, SVNRevision revEnd, SVNRevision pegRevision, boolean stopOnCopy, boolean fetchChangePath, long limit) {
        super(fileSystem);
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

    public LogCommand(FileSystem fileSystem, SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, SVNRevision pegRevision, boolean stopOnCopy, boolean fetchChangePath, long limit) {        
        super(fileSystem);
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
    protected ISVNNotifyListener.Command getCommand() {
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
        arguments.add("log"); //NOI18N
        arguments.add(revStart, revEnd);
        
        switch(type) {
            case file:
                if (pegRevision == null) {
                    arguments.add(file);
                } else {
                    arguments.add(file.getPath() + "@" + pegRevision); //NOI18N
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
                throw new IllegalStateException("Illegal logtype: " + type); //NOI18N
        }             
        arguments.add("--xml"); //NOI18N
        if(fetchChangePath) {
            arguments.add("-v"); //NOI18N    
        }
        if (stopOnCopy) {
            arguments.add("--stop-on-copy"); //NOI18N
        }            
        if (limit > 0) {
            arguments.add("--limit"); //NOI18N
            arguments.add(Long.toString(limit));
        }
    }

    public ISVNLogMessage[] getLogMessages() throws SVNClientException {
        if (output == null || output.length == 0) {
            return new ISVNLogMessage[0];
        }
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
    
    private static class XmlEntriesHandler extends DefaultHandler {

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
        
        private final List<ISVNLogMessage> logs = new ArrayList<>();        
        
        
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
                values = new HashMap<>();
                values.put(REVISION_ATTRIBUTE, elementAttributes.getValue(REVISION_ATTRIBUTE));            
            } else if (PATH_ELEMENT_NAME.equals(qName)) {                                
                List<Path> paths = getPathList();
                Path path = new Path();
                path.action = elementAttributes.getValue(ACTION_ATTRIBUTE).charAt(0);
                path.copyPath = elementAttributes.getValue("copyfrom-path"); //NOI18N
                path.copyRev = elementAttributes.getValue("copyfrom-rev"); //NOI18N
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
                    if(author == null) {
                        author = ""; //NOI18N
                    }
                    Date date = null;
                    String dateValue = (String) values.get(DATE_ELEMENT_NAME);                                                
                    if (dateValue == null) throw new SAXException("'date' tag expected under 'logentry'"); //NOI18N
                    try {
                        date = dateFormat.parse(dateValue);
                    } catch (ParseException ex) {
                        // ignore
                        
                    }
                    String msg = (String) values.get(MSG_ELEMENT_NAME);
                    if(msg == null) {
                        msg = ""; //NOI18N
                    }

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
                
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
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
                paths = new ArrayList<>();
                values.put(PATH_ELEMENT_NAME, paths);
            }
            return paths;
        }        
        
        private SVNRevision.Number getRevision(String revisionValue) {
            SVNRevision.Number rev = null;
            if (revisionValue != null && !revisionValue.trim().equals("")) {
                try {
                    rev = new SVNRevision.Number(Long.parseLong(revisionValue));
                } catch (NumberFormatException e) {
                    // ignore
                    rev = new SVNRevision.Number(-1);
                }
            }
            return rev;
        }        
    }        
    
    private static class LogMessage implements ISVNLogMessage {
        private final String msg;
        private final SVNRevision.Number rev;
        private final String author;
        private final Date date;
        private final ISVNLogMessageChangePath[] paths;
        public LogMessage(String msg, SVNRevision.Number rev, String author, Date date, ISVNLogMessageChangePath[] paths) {
            this.msg = msg;
            this.rev = rev;
            this.author = author;
            this.date = date;
            this.paths = paths;
        }
        @Override
        public SVNRevision.Number getRevision() {
            return rev;
        }
        @Override
        public String getAuthor() {
            return author;
        }
        @Override
        public Date getDate() {
            return date;
        }
        @Override
        public String getMessage() {
            return msg;
        }
        @Override
        public ISVNLogMessageChangePath[] getChangedPaths() {
            return paths;
        }

        @Override
        public long getTimeMicros() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimeMillis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getNumberOfChildren() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ISVNLogMessage[] getChildMessages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addChild(ISVNLogMessage arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasChildren() {
            throw new UnsupportedOperationException();
        }
    }    
            
}
