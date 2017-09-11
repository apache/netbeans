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

package org.netbeans.modules.palette;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemHandler extends DefaultHandler {

    private static final String XML_ROOT = "editor_palette_item"; // NOI18N
    private static final String ATTR_VERSION = "version"; // NOI18N
    private static final String TAG_BODY = "body"; // NOI18N
    private static final String TAG_CLASS = "class"; // NOI18N
    private static final String ATTR_CLASSNAME = "name"; // NOI18N
    private static final String TAG_CUSTOMIZER = "customizer"; // NOI18N
    private static final String ATTR_CUSTNAME = "name"; // NOI18N
    private static final String TAG_ICON16 = "icon16"; // NOI18N
    private static final String ATTR_URL = "urlvalue"; // NOI18N
    private static final String TAG_ICON32 = "icon32"; // NOI18N
    private static final String TAG_DESCRIPTION = "description"; // NOI18N
    private static final String TAG_INLINE_DESCRIPTION = "inline-description"; // NOI18N
    private static final String TAG_DISPLAY_NAME = "display-name"; // NOI18N
    private static final String TAG_TOOLTIP = "tooltip"; // NOI18N
    private static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    private static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    private static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N

    private LinkedList<String> bodyLines;
    private boolean insideBody = false;
    
    //raw data read from the file
    private String body;
    private String className;
    
    private String icon16URL;
    private String icon32URL;
    private String bundleName;
    private String displayNameKey;
    private String tooltipKey;
    private String displayName;
    private String tooltip;
    
    private StringBuffer textBuffer;

    private boolean error = false;
    
    public String getBody() { return body; }
    public String getClassName() { return className; }
    
    public String getIcon16URL() { return icon16URL; }
    public String getIcon32URL() { return icon32URL; }
    public String getBundleName() { return bundleName; }
    public String getDisplayNameKey() { return displayNameKey; }
    public String getTooltipKey() { return tooltipKey; }
    public String getDisplayName() { return displayName; }
    public String getTooltip() { return tooltip; }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
        throws SAXException 
    {
        if (XML_ROOT.equals(qName)) {
            String version = attributes.getValue(ATTR_VERSION);
            if (version == null) {
                String message = NbBundle.getBundle(PaletteItemHandler.class)
                    .getString("MSG_UnknownEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            } else if (!("1.0".equals(version) || "1.1".equals(version))) { // NOI18N
                String message = NbBundle.getBundle(PaletteItemHandler.class)
                    .getString("MSG_UnsupportedEditorPaletteItemVersion"); // NOI18N
                throw new SAXException(message);
            }
        } else if (TAG_BODY.equals(qName)) {
            bodyLines = new LinkedList<String>();
            insideBody = true;
        } else if (TAG_CLASS.equals(qName)) {
            className = attributes.getValue(ATTR_CLASSNAME);
        } else if (TAG_ICON16.equals(qName)) {
            icon16URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_ICON32.equals(qName)) {
            icon32URL = attributes.getValue(ATTR_URL);
            // TODO support also class resource name for icons
        } else if (TAG_DESCRIPTION.equals(qName)) {
            bundleName = attributes.getValue(ATTR_BUNDLE);
            displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
            tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
        } else if (TAG_INLINE_DESCRIPTION.equals(qName)) {
            bundleName = null;
            displayNameKey = null;
            tooltipKey = null;
        } else if (TAG_DISPLAY_NAME.equals(qName)) {
            textBuffer = new StringBuffer();
        } else if (TAG_TOOLTIP.equals(qName)) {
            textBuffer = new StringBuffer();
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (TAG_BODY.equals(qName)) {
            insideBody = false;
            body = trimSurroundingLines(bodyLines);
        } else if( TAG_DISPLAY_NAME.equals(qName) ) {
            displayName = textBuffer.toString();
            textBuffer = null;
        } else if( TAG_TOOLTIP.equals(qName) ) {
            tooltip = textBuffer.toString();
            textBuffer = null;
        }
    }
    
    @Override
    public void characters(char buf[], int offset, int len)
        throws SAXException
    {
        if (insideBody) {
            String chars = new String(buf, offset, len).trim();
            bodyLines.add(chars + "\n");
        } else if( null != textBuffer ) {
            textBuffer.append( buf, offset, len );
        }
    }

    /**
     * Trims empty lines from the beginning and the end of the line list
     */
    private String trimSurroundingLines(List<String> lines) {
        
        int nlines = lines.size();
        
        int firstNonEmpty = nlines;

        //going from the beginning and skipping empty lines until the first nonempty line occurs
        for (int i = 0; i < firstNonEmpty; i++) {
            String line = (String)lines.get(i);
            if (line.trim().length() != 0)
                firstNonEmpty = i;
        }
            
        int lastNonEmpty = -1;
        
        //going from the end and skipping empty lines until the first nonempty line occurs
        for (int i = nlines - 1; i > lastNonEmpty; i--) {
            String line = lines.get(i);
            if (line.trim().length() != 0)
                lastNonEmpty = i;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = firstNonEmpty; i <= lastNonEmpty; i++)
            sb.append(lines.get(i));
        
        String theBody = sb.toString();
        if (theBody.length() > 0  && theBody.charAt(theBody.length() - 1) == '\n') // cut trailing new-line
            theBody = theBody.substring(0, theBody.length() - 1);
        
        return theBody;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        error = true;
        Logger.getLogger(PaletteItemHandler.class.getName()).log(Level.INFO, null, e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        error = true;
        Logger.getLogger(PaletteItemHandler.class.getName()).log(Level.INFO, null, e);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        Logger.getLogger(PaletteItemHandler.class.getName()).log(Level.FINE, null, e);
    }
    
    boolean isError() {
        return error;
    }
}

