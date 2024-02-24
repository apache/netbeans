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

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.Annotations.Annotation;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
/**
 *
 * @author Tomas Stupka
 * // XXX merge with list, ...
 */
public class BlameCommand extends SvnCommand {

    // XXX merge
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {        
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));       
    }

    private enum BlameType {
        url,
        file,
    }
    
    private final BlameType type;
    
    private SVNUrl url;
    private File file;    
    private SVNRevision revStart;
    private SVNRevision revEnd;

    private byte[] output;

    public BlameCommand(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) {        
        this.url = url;                
        this.revStart = revStart;        
        this.revEnd = revEnd;        
        this.file = null;
        type = BlameType.url;
    }
    
    public BlameCommand(File file, SVNRevision revStart, SVNRevision revEnd) {        
        this.file = file;
        this.revStart = revStart;        
        this.revEnd = revEnd;        
        this.url = null;                
        type = BlameType.file;
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
        return ISVNNotifyListener.Command.ANNOTATE;
    }
    
    @Override
    public void output(byte[] bytes) {
        output = bytes;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("blame");        
        arguments.add("--xml");        
        if(revStart != null) {
            arguments.add(revStart, revEnd);        
        } else {
            arguments.add(revEnd);           
        }
        switch(type) {
            case url: 
                arguments.add(url);                
                break;
            case file:     
                arguments.add(file);
                setCommandWorkingDirectory(file);
                break;
            default :    
                throw new IllegalStateException("Illegal blametype: " + type);                             
        }
    }  
    
    public Annotation[] getAnnotation() throws SVNClientException {
        if (output == null || output.length == 0) return new Annotation[0];
        try {
            XMLReader saxReader = XMLUtil.createXMLReader();

            XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
            saxReader.setContentHandler(xmlEntriesHandler);
            saxReader.setErrorHandler(xmlEntriesHandler);
            InputSource source = new InputSource(new ByteArrayInputStream(output));

            saxReader.parse(source);
            return xmlEntriesHandler.getAnnotations();
            
        } catch (SAXException ex) {
            throw new SVNClientException(ex);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        
    }
    
    private class XmlEntriesHandler extends DefaultHandler {

        /*         
        <!-- For "svn blame" -->
	<!ELEMENT blame (target*)>
	<!ELEMENT target (entry*)>
	<!ATTLIST target path CDATA #REQUIRED>  <!-- path or URL -->
	<!-- NOTE: The order of entries in a target element is insignificant. -->
	<!ELEMENT entry (commit?)>
	<!ATTLIST entry line-number CDATA #REQUIRED>  <!-- line number: integer -->
	<!ELEMENT commit (author?, date?)>
	<!ATTLIST commit revision CDATA #REQUIRED>  <!-- revision number: integer -->
	<!ELEMENT author (#PCDATA)>  <!-- author -->
	<!ELEMENT date (#PCDATA)>  <!-- date as "yyyy-mm-ddThh:mm:ss.ssssssZ"-->         
        */
        
        private static final String BLAME_ELEMENT_NAME      = "blame";          // NOI18N
        private static final String TARGET_ELEMENT_NAME     = "target";         // NOI18N
        private static final String ENTRY_ELEMENT_NAME      = "entry";          // NOI18N
        private static final String COMMIT_ELEMENT_NAME     = "commit";         // NOI18N
        private static final String AUTHOR_ELEMENT_NAME     = "author";         // NOI18N        
        private static final String DATE_ELEMENT_NAME       = "date";           // NOI18N        
        
        private static final String PATH_ATTRIBUTE          = "path";           // NOI18N        
        private static final String LINE_NUMBER_ATTRIBUTE   = "line-number";    // NOI18N        
        private static final String REVISION_ATTRIBUTE      = "revision";       // NOI18N

        private String REVISION_ATTR                        = "revision_attr";  // NOI18N
        
        private List<Annotation> annotations = new ArrayList<Annotation>();        
        
        
        private Map<String, String> values;
        private String tag;               

        @Override
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            tag = qName.trim();                
            if (TARGET_ELEMENT_NAME.equals(qName)) {                        
                values = new HashMap<String, String>();                
            } else if (COMMIT_ELEMENT_NAME.equals(qName)) {                                
                values.put(REVISION_ATTR, elementAttributes.getValue(REVISION_ATTRIBUTE));
            } else if (ENTRY_ELEMENT_NAME.equals(qName)) {                                
                values.put(LINE_NUMBER_ATTRIBUTE, elementAttributes.getValue(LINE_NUMBER_ATTRIBUTE));
            }
            if(values != null) {
                values.put(tag, "");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(values == null || tag == null) {
                return;
            }
            String s = toString(length, ch, start);
            String v = values.get(tag);
            if(v == null) {
                values.put(tag, s);
            } else {
                values.put(tag, v + s);
            }
        }                
        
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            tag = null;
            if (ENTRY_ELEMENT_NAME.equals(name)) {                      
                if(values != null) {
                                                                    
                    String commit = values.get(COMMIT_ELEMENT_NAME);
                    if (commit != null) {
                    
                        String author = values.get(AUTHOR_ELEMENT_NAME);

                        Date date = null;
                        String dateValue = values.get(DATE_ELEMENT_NAME);
                        if(dateValue != null) {
                            try {
                                date = dateFormat.parse(dateValue);
                            } catch (ParseException ex) {
                                // ignore
                            }
                        }
                        
                        SVNRevision.Number rev = null;
                        String revisionValue = values.get(REVISION_ATTR);
                        if(revisionValue != null && !revisionValue.trim().equals("")) {
                            try {
                                rev = new SVNRevision.Number(Long.parseLong(revisionValue));
                            } catch (NumberFormatException e) {
                                rev = new SVNRevision.Number(-1);
                            }
                        }               
                        annotations.add(new Annotation(rev.getNumber(), author, date, null));
                    } else {
                        annotations.add(null);
                    }                             
                }
            } else if(TARGET_ELEMENT_NAME.equals(name)) {                                                
                values = null;
            }
        }
                
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public Annotation[] getAnnotations() {            
            return annotations.toArray(new Annotation[0]);
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }
    }    
}
