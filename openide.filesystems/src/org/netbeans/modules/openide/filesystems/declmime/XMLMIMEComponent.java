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

package org.netbeans.modules.openide.filesystems.declmime;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This source represents a <b>XML rules</b> core plugin to <tt>MIMEReolverImpl</tt>.
 *
 * @author  Petr Kuzel
 * @version
 */
final class XMLMIMEComponent extends DefaultParser {

    private short parseState = INIT;
    
    // template obtained form parsed description
    private final Smell template = new Smell();

    // cached and reused parser used for sniffing    
    private static final LocalSniffingParser local = new LocalSniffingParser();
    
    public XMLMIMEComponent() {
    }
    
    public XMLMIMEComponent(DataInput in) throws IOException {
        template.readExternal(in);
    }
    
    public XMLMIMEComponent(String elemName, List<String> elemNSs, List<String> dtds) {
        if (!elemName.isEmpty()) {
            template.addElementName(elemName);
        }
        for (String  s : elemNSs) {
            template.addElementNS(s);
        }
        for (String s : dtds) {
            template.addDoctype(s);
        }
    }

    // FileObjectFilter ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public boolean acceptFileObject(FileObject fo) {

        // it may come from arbitrary thread
        // retrive per thread instance
        
        SniffingParser sniffer = local.getParser();
        Smell print = sniffer.sniff(fo);
//        System.err.println("Print of " + fo);
//        System.err.println("print " + print);
//        System.err.println("template " + template);
        return template.match(print);
    }

    public String toString() {
       return template.toString();
    }

    // XML description -> memory representation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // pseudo validation states
    private static final short INIT = 0;
    private static final short IN_ROOT = 1;
    private static final short IN_DOCTYPE = 2;
    private static final short IN_ELEMENT = 3;

    // grammar elements
    private static final String ROOT = "xml-rule"; // NOI18N
    private static final String PI = "pi"; // NOI18N
    private static final String ELEMENT = "element"; // NOI18N
    private static final String DOCTYPE  = "doctype"; // NOI18N
    private static final String PUBLIC_ID = "public-id"; // NOI18N
    private static final String ID = "id"; // NOI18N
    private static final String ATTR = "attr"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String VALUE = "text"; // NOI18N
    private static final String NS = "ns"; // NOI18N
    private static final String TARGET = "target"; // NOI18N


    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        String s;

        switch (parseState) {

            case INIT:
                if (ROOT.equals(qName) ==  false) error();
                parseState = IN_ROOT;
                break;

            case IN_ROOT:
                if (PI.equals(qName)) {
                    s = atts.getValue(TARGET); if (s == null) error();
                    template.addPI(s);

                    //!!! TODO presudo atts

                } else if (DOCTYPE.equals(qName)) {
                    s = atts.getValue(PUBLIC_ID);
                    if (s == null) {
                        parseState = IN_DOCTYPE;
                        break;
                    } else {
                        template.addDoctype(s);
                    }

                } else if (ELEMENT.equals(qName)) {

                    s = atts.getValue(NAME);
                    if (s == null) {
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    } else {
                        template.addElementName(s);
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    }

                    parseState = IN_ELEMENT;

                } else {
                    error();
                }
                break;

            case IN_DOCTYPE:
                if (PUBLIC_ID.equals(qName) == false) error();
                s = atts.getValue(ID); if (s == null) error();
                template.addDoctype(s);
                break;

            case IN_ELEMENT:
                if (ATTR.equals(qName)) {
                    s = atts.getValue(NAME); if (s == null) error();
                    template.addElementAtt(s, atts.getValue(VALUE));

                } else if (NS.equals(qName)) {
                    s = atts.getValue(NS); if (s == null) error();
                    template.addElementNS(s);

                } else {
                    error();
                }

        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {

        switch (parseState) {
            case IN_ELEMENT:
                if (ELEMENT.equals(qName)) parseState = IN_ROOT;
                break;      

            case IN_DOCTYPE:
                if (DOCTYPE.equals(qName)) parseState = IN_ROOT;
                break;
        }
    }
    
    // Sniffing parser ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    void writeExternal(DataOutput out) throws IOException {
        template.writeExternal(out);
    }

    /**
     * Create just one shared parser instance per thread.
     * Consequently one instance cannot be run in paralel eliminating need for sync.
     */
    private static class LocalSniffingParser extends ThreadLocal<WeakReference<SniffingParser>> {
        LocalSniffingParser() {}
        
        private WeakReference<SniffingParser> wref = null;
        
        protected WeakReference<SniffingParser> initialValue() {            
            SniffingParser parser = new SniffingParser();
            wref = new WeakReference<SniffingParser>(parser);
            return wref;
        }
        
        public SniffingParser getParser() {
            WeakReference<SniffingParser> cache = get();
            SniffingParser cached = cache.get();
            if (cached == null) {
                cached = new SniffingParser();
                wref = new WeakReference<SniffingParser>(cached);                
                super.set(wref);
            }
            return cached;            
        }
        
        public void set(WeakReference<SniffingParser> data) {
            // we are read only!
        }
    }

        
    /**
     * Parser that test XML Document header.
     */
    private static class SniffingParser extends DefaultParser implements LexicalHandler {

        SniffingParser() {
            super(null);
        }

        // last succesfully sniffed fileobject
        private FileObject lastFileObject = null;
        
        private Smell print = null;
        
        // the only way how to stop parser is throwing an exception
        private static final SAXException STOP = new SAXException("STOP");  //NOI18N
        private int errors;
        
        /**
         * Go ahead and retrieve a print or null
         */
        protected Smell sniff(FileObject fo) {

            if (fo == null) return null;
            
            if (fo.equals(lastFileObject)) return print;
            
            if (fo.isValid() == false) return null;

            if (fo.getSize() == 0) return null;
            
            print = new Smell();
            errors = 0;
            parse(fo);
            if (this.state == ERROR) {
                return null;
            }
            
            lastFileObject = fo;
            return print;
        }
        
        protected XMLReader createXMLReader() {
            XMLReader parser = null;
            
            try {
                parser = XMLUtil.createXMLReader(false, true);           
                try {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);  //NOI18N
                } catch (SAXException sex) {
                    Logger.getLogger(XMLMIMEComponent.class.getName()).fine(NbBundle.getMessage(XMLMIMEComponent.class, "W-003"));  //NOI18N
                }
            } catch (SAXException ex) {
                Logger.getLogger(XMLMIMEComponent.class.getName()).log(Level.WARNING, null, ex);
            }
            return parser;
        }
        
        @Override
        protected boolean isStopException(Throwable e) {
            return STOP.getMessage().equals(e.getMessage());
        }        
        
        
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {            
            if (namespaceURI != null) {
                print.addElementNS(namespaceURI);
            }
            if ("".equals(localName)) localName = null;  //#16484  //NOI18N
            print.addElementName(localName != null ? localName : qName);
            for (int i = 0; i<atts.getLength(); i++) {
                print.addElementAtt(atts.getQName(i), atts.getValue(i));
            }
            throw STOP;
        }
        
        public void processingInstruction(String target, String data) throws SAXException {
            print.addPI(target);
        }
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
            print.addDoctype(pID);
        }

        public void endDTD() {}

        public void startEntity(String name) {}

        public void endEntity(String name) {}

        public void startCDATA() {}

        public void endCDATA() {}

        public void comment(char[] ch, int start, int length) {}
        
        public void error(SAXParseException exception) throws SAXException {            
            // we are not validating should not occure
            Logger.getLogger(XMLMIMEComponent.class.getName()).warning(exception.getMessage());
            this.state = ERROR;
            throw STOP;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            if (exception.getMessage().contains("Invalid encoding")) { // NOI18N
                // ok, go on, we want to continue parsing anyway
                return;
            }
            
            // it may be caused by wrong user XML documents, notify only in debug mode
            // also see #16484 if the error message makes no sense
            Logger emgr = Logger.getLogger(XMLMIMEComponent.class.getName());
            if (emgr.isLoggable(Level.FINE)) {
                emgr.fine("[while parsing " + fo + "] " + exception.getSystemId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ": " + exception.getMessage()); // NOI18N
            }
            
            // go on anyway, needed to fix 203959
            if (errors++ > 10) {
                state = ERROR;
                throw STOP;
            }
        }
        
        
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Template smell per resolver and print data per FileObject.
     */
    private static class Smell {
        Smell() {}
        
        private String[] doctypes = null;
        private String[] pis = null;
        
        private String   root = null;
        private String[] nss = null;
        
        private String[] attns = null;
        private String[] attvs = null;

        public void writeExternal(DataOutput out) throws IOException {
            Util.writeStrings(out, doctypes);
            Util.writeStrings(out, pis);
            Util.writeUTF(out, root);
            Util.writeStrings(out, nss);
            Util.writeStrings(out, attns);
            Util.writeStrings(out, attvs);
        }
        
        public void readExternal(DataInput in) throws IOException {
            doctypes = Util.readStrings(in);
            pis = Util.readStrings(in);
            root = Util.readUTF(in);
            nss = Util.readStrings(in);
            attns = Util.readStrings(in);
            attvs = Util.readStrings(in);
        }
        
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            int i = 0;
            buf.append("xml-check(");
            
            if (doctypes != null) {
                buf.append("doctypes:");
                for (i = 0; i<doctypes.length; i++)
                    buf.append(doctypes[i]).append(", ");
            }

            if (pis != null) {
                buf.append("PIs:");
                for (i = 0; i<pis.length; i++)
                    buf.append(pis[i]).append(", ");
            }

            if (root != null) {
               buf.append("root:").append(root);
            }

            if (nss != null) {
                buf.append("root-namespaces:");
                for (i = 0; i<nss.length; i++)
                    buf.append(nss[i]).append(", ");
            }

            if (attns != null) {
                buf.append("attributes:");
                for (i = 0; i<attns.length; i++)
                    buf.append(attns[i]).append("='").append(attvs[i]).append("'");
            }

            buf.append(')');
            return buf.toString();

        }

        private void addDoctype(String s) {
            if (doctypes == null) {
                doctypes = new String[] { s };
            } else {
                doctypes = Util.addString(doctypes, s);
            }            
        }
        
        private void addPI(String s) {
            if (pis == null) {
                pis = new String[] { s };
            } else {
                pis = Util.addString(pis, s);
            }                        
        }
        
        private void addElementNS(String s) {
            if (nss == null) {
                nss = new String[] { s };
            } else {
                nss = Util.addString(nss, s);
            }                        
        }
        
        private void addElementName(String name) {
            root = name;
        }
        
        private void addElementAtt(String name, String value) {
            if (attns == null) {
                attns = new String[] {name};
                attvs = new String[] {value};
            } else {
                attns = Util.addString(attns, name);
                attvs = Util.addString(attvs, value);
            }
            
        }

        /**
         * Matches passed data this template?
         * Any of constructs must match.
         */
        public boolean match(Smell t) {

            if (t == null) return false;
            
            // try if a doctype public-id matches
            
            if (doctypes != null && t.doctypes != null) {
                if (Util.contains(doctypes, t.doctypes[0])) return true;
            }
            
            // try root element match
            
            if (root != null && root.equals(t.root)) {
                if (nss == null) {                                                            
                    if (attMatch(t)) return true;
                } else {                                        
                    if (t.nss != null && Util.contains(nss, t.nss[0])) {
                        if (attMatch(t)) return true;
                    }                    
                }
            } else {
                if (root == null && nss != null && t.nss != null && Util.contains(nss, t.nss[0])) {
                    if (attMatch(t)) return true;
                }                                    
            }
            
            // try if a PI matches
            
            if (pis != null && t.pis!=null) {
                for (int i = 0; i<pis.length; i++) {
                    for (int j = 0; j<t.pis.length; j++) {
                        if (pis[i].equals(t.pis[j])) return true;
                    }
                }
            }            
            
            return false;
        }
        
        
        private boolean attMatch(Smell t) {

            if (attns == null) return true;
            if (t.attns == null) return false;
            
            // all attributes must match by name ...
            for (int i = 0 ; i<attns.length; i++) {
                int match = Util.indexOf(t.attns, attns[i]);
                if (match == -1) {
                    return false;
                }

                // ... and value if specified in template

                if (attvs[i] != null && (!attvs[i].equals(t.attvs[match]))) {
                    return  false;
                }
            }
            
            return true;
            
        }

    }
}
