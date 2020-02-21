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
package org.netbeans.modules.cnd.api.xml;

import java.util.HashMap;

import java.util.Map;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;

/**
 * Receive notification of the content of an XML element named via {@link #tag}.
 * <p>
 * XMLDocReader will call {@link #start} upon encountering an element named
 * via {@link #tag}, and will call {@link #end} when the closing tag is
 * encountered.
 * <p>
 * Any elements encountered between those will cause {@link #startElement}
 * and {@link #endElement} to get called, <i>unless</i> a sub-decoder has
 * been registered using {@link #registerXMLDecoder} in which case 
 * all of this recursively applies to the nested element.
 * <p>
 * For example, if <code>FamilyXMLDecoder</code> has <code>tag()</code>
 * return <code>"family"</code> and calls
 * <pre>registerXMLDecoder(new PersonXMLDecoder(...))</pre>
 * then the following trace of XML elements and corresponding callbacks
 * will occur:
 * <pre>
&lt;family&gt;					start(null)
&lt;person firstName="X" lastName="Y"/&gt;	PersonXMLDecoder.start(...)
&lt;heritage&gt;Algebra&lt;/heritage&gt;		startElement("heritage", ...)
endElement("heritage", "Algebra");
&lt;/family&gt;					end();
 * </pre>
 * <p>
 * An XMLDecoder should be extended by a subclass, which would typically also
 * implement {@link XMLEncoder} to create a <b>codec</b>.
 */
public abstract class XMLDecoder {

    abstract protected String tag();

    abstract protected void start(Attributes atts) throws VersionException;

    abstract protected void end();

    abstract protected void startElement(String name, Attributes atts);

    abstract protected void endElement(String name, String currentText);

    protected void registerXMLDecoder(XMLDecoder decoder) {
        tagMap.put(decoder.tag(), decoder);
    }

    protected void deregisterXMLDecoder(XMLDecoder decoder) {
        tagMap.remove(decoder.tag());
    }
    private final Map<String, XMLDecoder> tagMap = new HashMap<String, XMLDecoder>();
    private XMLDecoder currentDecoder;
    private String currentElement;

    public XMLDecoder() {
    }

    void _startElement(String name, Attributes atts) throws VersionException {
        if (checkStartRecursion(name, atts)) {
            return;
        } else {
            startElement(name, atts);
        }
    }

    void _endElement(String name, String currentText) {
        // see if need to terminate the current decoder
        if (checkEndRecursion(name, currentText)) {
            return;
        } else {
            // pass on to current decoder
            endElement(name, currentText);
        }
    }

    private boolean checkStartRecursion(String name, Attributes atts)
            throws VersionException {

        if (currentDecoder != null) {
            currentDecoder._startElement(name, atts);
            return true;
        }

        XMLDecoder tentativeDecoder = tagMap.get(name);
        if (tentativeDecoder != null) {
            /* DEBUG
            System.out.println("Switching to decoder for " + name);
             */
            tentativeDecoder.start(atts);	// throws VersionException
            // everything went fine, commit to it
            currentDecoder = tentativeDecoder;
            currentElement = name;
            return true;
        }
        return false;
    }

    private boolean checkEndRecursion(String name, String currentText) {
        if (currentDecoder != null) {
            if (currentDecoder.checkEndRecursion(name, currentText)) {
                return true;
            } else if (name.equals(currentElement)) {
                // ending this decoder
                currentDecoder.end();
                currentDecoder = null;
                currentElement = null;
                return true;
            } else {
                currentDecoder.endElement(name, currentText);
                return true;
            }
        }
        return false;
    }

    protected int getVersion(Attributes atts) {
        int version = 0;
        String versionString = atts.getValue("version");        // NOI18N
        if (versionString != null) {
            version = Integer.parseInt(versionString);
        }
        return version;
    }

    protected void checkVersion(Attributes atts, String what, int maxVersion)
            throws VersionException {

        int version = getVersion(atts);
        if (version > maxVersion) {
            String title = NbBundle.getMessage(XMLDecoder.class, "MSG_version_ignore_title"); //NOI18N
            String message = NbBundle.getMessage(XMLDecoder.class, "MSG_version_ignore"); //NOI18N
            boolean ignore = CndNotifier.getDefault().notifyAndIgnore(title, message);
            if (ignore) {
                return;
            }
//            if (CndUtils.isStandalone()) {
//                System.err.print(message);
//                System.err.println(NbBundle.getMessage(XMLDecoder.class, "MSG_version_ignore_AUTO")); //NOI18N
//                return;
//            } else {
//                
//                СndErr
//                NotifyDescriptor nd = new NotifyDescriptor(message,
//                        title, NotifyDescriptor.YES_NO_OPTION,
//                        NotifyDescriptor.QUESTION_MESSAGE,
//                        null, NotifyDescriptor.YES_OPTION);
//                Object ret = DialogDisplayer.getDefault().notify(nd);
//                if (ret == NotifyDescriptor.YES_OPTION) {
//                    return;
//                }
//            }
            throw new VersionException(what, false, maxVersion, version);
        }
    }
}

