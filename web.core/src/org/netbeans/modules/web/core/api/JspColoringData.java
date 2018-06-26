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


package org.netbeans.modules.web.core.api;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Iterator;

import javax.servlet.jsp.tagext.TagLibraryInfo;

/** Holds data relevant to the JSP coloring for one JSP page. The main purposes
 * of this class are
 * to report which prefixes are tag library prefixes in the page, and allows
 * listening on the change of the prefixes, at which point the page needs to be
 * recolored.
 *
 * @author Petr Jiricka
 */
public final class JspColoringData extends PropertyChangeSupport {
    
    /** An property whose change is fired every time the tag library 
    *  information changes in such a way that recoloring of the document is required. 
    */
    public static final String PROP_COLORING_CHANGE = "coloringChange"; // NOI18N
    public static final String PROP_PARSING_SUCCESSFUL = "parsingSuccessful"; //NOI18N
    public static final String PROP_PARSING_IN_PROGRESS = "parsingInProgress"; //NOI18N
    
    /** Taglib id -> TagLibraryInfo */
    private Map taglibs;
    
    /** Prefix -> Taglib id */
    private Map prefixMapper;
    
    private boolean elIgnored = false;
    
    private boolean xmlSyntax = false;

    private boolean initialized = false;
    
    /** Creates a new instance of JspColoringData. */
    public JspColoringData(Object sourceBean) {
        super(sourceBean);
    }
    
    public Map getPrefixMapper(){
        return prefixMapper;
    }
    
    public String toString() {
        return "JSPColoringData, taglibMap:\n" +
          (prefixMapper == null ?
            "null" :
            mapToString(prefixMapper, "  ")
          );
    }
    
    private static String mapToString(Map m, String indent) {
        StringBuffer sb = new StringBuffer();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            sb.append(indent).append(key).append(" -> ").append(m.get(key)).append("\n");
        }
        return sb.toString();
    }

    /** Returns true if the given tag library prefix is known in this page.
     */
    public boolean isTagLibRegistered(String prefix) {
        if ((taglibs == null) || (prefixMapper == null)) {
            return false;
        }
        return prefixMapper.containsKey(prefix);
    }

    /** returns true if the JspColoringInfo has already been updated based on parser result. */
    public boolean isInitialized() {
        return initialized;
    }

    /** Returns true if the EL is ignored in this page.
     */
    public boolean isELIgnored() {
        return elIgnored;
    }
    
    /** Returns true if the page is in xml syntax (JSP Documnet). 
     * If the page is in standard syntax, returns false.
     */
    public boolean isXMLSyntax(){
        return xmlSyntax;
    }
    /*public boolean isBodyIntepretedByTag(String prefix, String tagName) {
    }*/
        
    public void parsingStarted() {
        firePropertyChange(PROP_PARSING_IN_PROGRESS, null, new Boolean(true));
    }
    
    /** Incorporates new parse data from the parser, possibly firing a change about coloring.
     * @param newTaglibs the new map of (uri -> TagLibraryInfo)
     * @param newPrefixMapper the new map of (prefix, uri)
     * @param parseSuccessful wherher parsing was successful. If false, then the new information is partial only
     */
    public void applyParsedData(Map newTaglibs, Map newPrefixMapper, boolean newELIgnored, boolean newXMLSyntax, boolean parseSuccessful) {

        initialized = true;

        // check whether coloring has not changed
        boolean coloringSame = equalsColoringInformation(taglibs, prefixMapper, newTaglibs, newPrefixMapper);
        
        firePropertyChange(PROP_PARSING_SUCCESSFUL, null, new Boolean(parseSuccessful));
        
        // check and apply EL data
        if (parseSuccessful) {
            coloringSame = coloringSame && (elIgnored == newELIgnored);
            elIgnored = newELIgnored;
        }
        
        //An additional check for the coloring change -> 
        //if the elIgnored and xmlSyntax have default values and the taglibs and prefixes are empty,
        //there is no need to repaint the editor (fire the property change).        
        //Test if this is a first call of this method - after opening of the editor
        if((taglibs == null) && (prefixMapper == null)) {
            coloringSame = ((newELIgnored == elIgnored) && 
                           (newXMLSyntax == xmlSyntax) &&
                           newTaglibs.isEmpty() &&
                           newPrefixMapper.isEmpty());
        }

        if (newXMLSyntax != xmlSyntax){
            xmlSyntax = newXMLSyntax;
            coloringSame = false;
        }        
        
        // appy taglib data
        if (parseSuccessful || (taglibs == null) || (prefixMapper == null)) {
            // overwrite
            taglibs = newTaglibs;
            prefixMapper = newPrefixMapper;
        }
        else {
            // merge
            Iterator it = newPrefixMapper.keySet().iterator();
            while (it.hasNext()) {
                Object prefix = it.next();
                Object uri = newPrefixMapper.get(prefix);
                Object uriOld = prefixMapper.get(prefix);
                if ((uriOld == null) || !uri.equals(uriOld)) {
                    Object newTaglib = newTaglibs.get(uri);
                    if (newTaglib != null) {
                        // change - merge it
                        prefixMapper.put(prefix, uri);
                        taglibs.put(uri, newTaglib);
                    }
                }
            }
        }
        // possibly fire the change
        if (!coloringSame) {
            firePropertyChange(PROP_COLORING_CHANGE, null, null);
        }
    }

    private static boolean equalsColoringInformation(Map taglibs1, Map prefixMapper1, Map taglibs2, Map prefixMapper2) {
        if ((taglibs1 == null) != (taglibs2 == null)) {
            return false;
        }
        if ((prefixMapper1 == null) != (prefixMapper2 == null)) {
            return false;
        }
        if (prefixMapper1.size() != prefixMapper2.size()) {
            return false;
        }
        else {
            Iterator it = prefixMapper1.keySet().iterator();
            while (it.hasNext()) {
                Object prefix = it.next();
                Object key1 = prefixMapper1.get(prefix);
                Object key2 = prefixMapper2.get(prefix);
                if ((key1 == null) || (key2 == null)) {
                    return false;
                }
                TagLibraryInfo tli1 = (TagLibraryInfo)taglibs1.get(key1);
                TagLibraryInfo tli2 = (TagLibraryInfo)taglibs2.get(key2);
                if ((tli1 == null) || (tli2 == null)) {
                    return false;
                }
                if (!equalsColoringInformation(tli1, tli2)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean equalsColoringInformation(TagLibraryInfo tli1, TagLibraryInfo tli2) {
        /** PENDING
         * should be going through all tags and checking whether the value 
         * returned by tagInfo.getBodyContent() has not changed.
         */
        return true;
    }
    
}
