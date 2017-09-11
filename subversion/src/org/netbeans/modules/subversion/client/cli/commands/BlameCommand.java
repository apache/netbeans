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
            return annotations.toArray(new Annotation[annotations.size()]);
        }

        private String toString(int length, char[] ch, int start) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            return new String(c);
        }
    }    
}
