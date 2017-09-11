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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.openide.xml.XMLUtil;
import org.xml.sax.XMLReader;

/**
 * Parses default JRE containers from Eclipse workspace.
 *
 * @author mkrauskopf
 */
final class PreferredVMParser extends DefaultHandler {
    
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(PreferredVMParser.class.getName());
    
    // elements names
    private static final String VM_SETTINGS = "vmSettings"; // NOI18N
    private static final String VM_TYPE = "vmType"; // NOI18N
    private static final String VM = "vm"; // NOI18N
    private static final String LIBRARY_LOCATIONS = "libraryLocations"; // NOI18N
    private static final String LIBRARY_LOCATION = "libraryLocation"; // NOI18N
    
    // attributes names
    private static final String DEFAULT_VM_ATTR = "defaultVM"; // NOI18N
    private static final String ID_ATTR = "id"; // NOI18N
    private static final String NAME_ATTR = "name"; // NOI18N
    private static final String PATH_ATTR = "path"; // NOI18N
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_VM_SETTINGS = 1;
    private static final int POSITION_VM_TYPE = 2;
    private static final int POSITION_VM = 3;
    private static final int POSITION_LIBRARY_LOCATIONS = 4;
    private static final int POSITION_LIBRARY_LOCATION = 5;
    
    private int position = POSITION_NONE;
    private StringBuffer chars;
    private String defaultId;
    
    private Map<String,String> jdks;
    
    private PreferredVMParser() {/* empty constructor */}
    
    /** Returns vmMap of JDKs */
    static Map<String,String> parse(String vmXML) throws ProjectImporterException {
        PreferredVMParser parser = new PreferredVMParser();
        parser.load(new InputSource(new StringReader(vmXML)));
        return parser.jdks;
    }
    
    /** Parses a given InputSource and fills up jdk vmMap */
    private void load(InputSource vmXMLIS) throws ProjectImporterException{
        try {
            XMLReader reader = XMLUtil.createXMLReader(false, true);
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            chars = new StringBuffer(); // initialization
            reader.parse(vmXMLIS); // start parsing
        } catch (IOException e) {
            throw new ProjectImporterException(e);
        } catch (SAXException e) {
            throw new ProjectImporterException(e);
        }
    }
    
    @Override
    public void characters(char ch[], int offset, int length) throws SAXException {
        chars.append(ch, offset, length);
    }
    
    @Override
    public void startElement(String uri, String localName,
            String qName, Attributes attributes) throws SAXException {
        
        chars.setLength(0);
        switch (position) {
            case POSITION_NONE:
                if (localName.equals(VM_SETTINGS)) {
                    position = POSITION_VM_SETTINGS;
                    // default vm id seems to be after the last comma
                    String defaultVMAttr = attributes.getValue(DEFAULT_VM_ATTR);
                    defaultId = defaultVMAttr.substring(defaultVMAttr.lastIndexOf(',') + 1);
                    jdks = new HashMap<String,String>();
                } else {
                    throw (new SAXException("First element has to be " // NOI18N
                            + VM_SETTINGS + ", but is " + localName)); // NOI18N
                }
                break;
            case POSITION_VM_SETTINGS:
                if (localName.equals(VM_TYPE)) {
                    position = POSITION_VM_TYPE;
                }
                break;
            case POSITION_VM_TYPE:
                if (localName.equals(VM)) {
                    position = POSITION_VM;
                    addJDK(attributes.getValue(ID_ATTR),
                            attributes.getValue(NAME_ATTR),
                            attributes.getValue(PATH_ATTR));
                }
                break;
            case POSITION_VM:
                if (localName.equals(LIBRARY_LOCATIONS)) {
                    position = POSITION_LIBRARY_LOCATIONS;
                    logger.info("JRE used by your project presuambly contains additional jars. This is not supported (imported) yet. " + // NOI18N
                            "CC yourself to issue http://www.netbeans.org/issues/show_bug.cgi?id=70733 to watch a progress."); // NOI18N
                    // XXX this means that additional jars were added to the
                    // JDK used by a project.
                    // See Preferences --> Java --> Installed JREs --> Choose
                    // JDK --> Edit --> Uncheck "Use default system libraries"
                    // --> Add External Jar
                    // Than take a look at .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.launching.prefs
                }
                break;
            case POSITION_LIBRARY_LOCATIONS:
                if (localName.equals(LIBRARY_LOCATION)) {
                    position = POSITION_LIBRARY_LOCATION;
                    // XXX See comment above - "case POSITION_VM"
                }
                break;
            default:
                throw (new SAXException("Unknown position reached: " // NOI18N
                        + position + " (element: " + localName + ")")); // NOI18N
        }
    }
    
    // XXX use array[x] array[x-1] or 1.5 enumerations(?) here and for similar
    // cases or consider DOM
    @Override
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        switch (position) {
            case POSITION_VM_SETTINGS:
                // parsing ends
                position = POSITION_NONE;
                break;
            case POSITION_VM_TYPE:
                position = POSITION_VM_SETTINGS;
                break;
            case POSITION_VM:
                position = POSITION_VM_TYPE;
                break;
            case POSITION_LIBRARY_LOCATIONS:
                position = POSITION_VM;
                break;
            case POSITION_LIBRARY_LOCATION:
                position = POSITION_LIBRARY_LOCATIONS;
                break;
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in ClassPathParser, " + // NOI18N
                        "position: " + position); // NOI18N
        }
        chars.setLength(0);
    }
    
    @Override
    public void error(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Error occurres: " + e); // NOI18N
        throw e;
    }
    
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Fatal error occurres: " + e); // NOI18N
        throw e;
    }
    
    private void addJDK(String id, String name, String value) {
        if (id.equals(defaultId)) {
            // put the default twice under two names. It seems that under some
            // circumstances there is full name in .classpath con entry even
            // if the currently used container is default one.
            jdks.put(Workspace.DEFAULT_JRE_CONTAINER, value);
        }
        jdks.put(name, value);
    }
}
