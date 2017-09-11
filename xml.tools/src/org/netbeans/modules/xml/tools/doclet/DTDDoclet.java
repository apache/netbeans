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
package org.netbeans.modules.xml.tools.doclet;

import java.util.*;
import java.text.DateFormat;



import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;
import org.openide.util.NbBundle;

/**
 * Creates [X]HTML documentation for DTD.
 * <p>There is a special doclet comment preceding element declaration.
 * The comment is delimited by "<!---"  and "-->" i.e. starting
 * delimiter uses three "---".
 *
 * <p>Generated element content model consists of hyperlinked structure.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class DTDDoclet {

    private RefList s; //tmp holding whole result
    
    private String originalFileName;
    
    private RefList elementIndex = new RefList();
    
    private String comment; //last suitable comment or null if none

    /* This ellement is just being commented.
     * Used for "referenced by" creation.
     */
    private String currentElement = null;
    
    /* Hold element name keyed RefLists used at
     * "referenced by" section.
     */
    private HashMap elementRefs = new HashMap();
    
    // configurable output format
    
    private final String HEADLINE1 = "h2"; // NOI18N
    private final String HEADLINE2 = "b"; // NOI18N
    
    private final String LIST = "ul"; // NOI18N
    private final String LIST_ITEM = "li"; // NOI18N
    
    private final String PAR = "p"; // NOI18N
    
    private final String ROOT = "html"; // NOI18N
    private final String BODY = "body"; // NOI18N
    
    /** Creates new DTDDoclet engine*/
    public DTDDoclet(String fileName) {
        this.originalFileName = fileName;
    }

    /**
     * @return a String representing XHTML content
     */
    public String createDoclet (TreeDTD dtd, String encoding) {
        if (dtd == null)
            return ""; // NOI18N
        
        Iterator it;        
        s = new RefList();
        
        s.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"); // NOI18N
        
        s.appendStartTag(ROOT).append("\n<head>\n<title>" + NbBundle.getMessage(DTDDoclet.class, "PAGE_TITLE", originalFileName) + "</title>\n"); // NOI18N
        s.append("<meta http-equiv=\"Content-Type\" content=\"text/xhtml; charset=" + encoding + "\" />\n");
        s.append("</head>\n");
        s.append("\n<!-- Generated on " + DateFormat.getDateInstance().format(new Date()) + " by NetBeans XML module. -->\n"); // NOI18N
        s.appendStartTag(BODY);
        
        s.append("<h1>").append(NbBundle.getMessage(DTDDoclet.class, "DOCUMENT_TITLE", originalFileName)).append("</h1>");
        
        headline1(NbBundle.getMessage(DTDDoclet.class, "TEXT_Element_Index"));
        
        s.append(elementIndex);
        
        headline1(NbBundle.getMessage(DTDDoclet.class, "TEXT_Element_Details"));
                        
        it = dtd.getChildNodes(TreeChild.class, true).iterator();
        
        Vector list = new Vector();
        while (it.hasNext()) {
            TreeNode child = (TreeNode)it.next();
	    if (child instanceof TreeElementDecl) {
		commentElement((TreeElementDecl) child);
		comment = null;
	    } else if (child instanceof TreeComment) {
		comment = decodeComment((TreeComment) child);
            } else if (child instanceof TreeText || child instanceof TreeParameterEntityReference) {
                // do not clear comment #30094
	    } else {
		// disable comment
		comment = null;
            }
        }

        
        // create index at elementIndex reference position.
        // strings are sorted by alphabet
        
        TreeSet index = new TreeSet();  
        it = elementRefs.keySet().iterator();
                
        while (it.hasNext()) {
            index.add(it.next());
        }
        
        elementIndex.appendStartTag(LIST);
        it = index.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            listitem(elementIndex, "<a href=\"#" + next + "\">" + next + "</a>"); // NOI18N
        }
        elementIndex.appendEndTag(LIST);
        
        
        // terminate the html page
        //
        
        s.appendEndTag(BODY).appendEndTag(ROOT);
        return s.toString();
    }

    //~~~~~~~~~~~~~~~ DOCUMENTATION STUFF ~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /** Comment is paired just with the next element declaration and 
     * it must statt with <!---.
     */
    private String decodeComment(TreeComment node) {
        return node.getData().startsWith("-") ? node.getData().substring(1).trim() : null; // NOI18N
    }
    

    
    private void commentElement(TreeElementDecl node) {
        String tag = node.getName();
        currentElement = tag;
                
        headline1(tag, tag);        
        // try to survive various user tags wrap it in <div>
        s.append(comment == null ? "" : "<div>" + comment + "</div>"); // NOI18N
        commentAttributes(node);
        
        headline2(NbBundle.getMessage(DTDDoclet.class, "TEXT_ContentModel"));        
        TreeElementDecl.ContentType type = node.getContentType();
        s.append("<p><tt>"); // NOI18N
        commentContentModel(type);
        s.append("</tt></p>"); // NOI18N

        headline2(NbBundle.getMessage(DTDDoclet.class, "TEXT_Referenced_by"));
        s.append("<p><tt>"); // NOI18N
        s.append(getRefList(tag));
        s.append("</tt></p>"); // NOI18N
        s.append("\n"); // NOI18N
    } 
    
    /*
     * attributes are commented like Java fields declarations.
     *  <modifier> <type> <name> = <default>
     */
    private void commentAttributes(TreeElementDecl element) {
        
        Iterator it = element.getAttributeDefs().iterator();
        
        if (!!! it.hasNext())
	    return;
        
        headline2(NbBundle.getMessage(DTDDoclet.class, "TEXT_Declared_Attributes"));
        s.appendStartTag(LIST); // NOI18N
        
        while (it.hasNext()) {
            TreeAttlistDeclAttributeDef next = (TreeAttlistDeclAttributeDef) it.next();
            
            String defVal  = next.getDefaultValue();
            if ( ( defVal == null ) ||
                 ( defVal.length() == 0 ) ) {
                defVal = "";
            } else {
                defVal = " = " + defVal; // NOI18N
            }
            
            String defType = next.getDefaultTypeName() == null ? "#DEFAULT" : next.getDefaultTypeName(); // NOI18N
            
            String text = "";  // NOI18N
            
            if (next.getType()  == next.TYPE_ENUMERATED) {
                text = defType + " ENUMERATION " +  next.getEnumeratedTypeString(); // NOI18N
            } else {
                text = defType + " " + next.getTypeName(); // NOI18N
            }
            
            text += " " + next.getName() + defVal;  // NOI18N
            
            listitem(s, text);
        }
        
        s.appendEndTag(LIST); // NOI18N
    }

    /*
     * Content model documentation consists of links to element definitions.
     * It is recursive implementation.  
     */
    private void commentContentModel (TreeElementDecl.ContentType type) {

        if (type instanceof EMPTYType) {
            
            s.append("EMPTY"); // NOI18N
            return;
            
        } else if (type instanceof ANYType) {

            s.append("ANY"); // NOI18N
            return;
            
        } 

        
        if (type instanceof LeafType) {
            
            LeafType leaf = (LeafType)type;
            String tag = leaf.getName();
            s.append("<a href=\"#").append(tag).append("\">"); // NOI18N
            s.append(tag).append("</a>").append(leaf.getMultiplicity()); // NOI18N
            
            // append "tag reference from currentElement" as fragment to force uniques // NOI18N
            // because one content model can reference same tag more times
            RefList refs = getRefList(tag);
            String prefixSeparator = ", ";  // NOI18N
            if (refs.isEmpty()) {
                prefixSeparator = "";  // NOI18N
            }
            
            refs.appendUniq(prefixSeparator + "<a href=\"#" + currentElement + "\">" + currentElement + "</a>"); // NOI18N
            
            return;
        }


        s.append("(");  // NOI18N

        if (type.allowText()) {
                    
            s.append("#PCDATA"); // NOI18N
            
        } 
        

        // it is a group, select proper separator  
        
        String separator = "";  // NOI18N
        if (type instanceof ChildrenType) {
            String sepChar = ((ChildrenType)type).getSeparator();
            
            //',' always directly follows previous word otherwise put and extra space
            String prefix = sepChar.equals(",") == false ? "&nbsp;" : "";  // NOI18N
            separator = prefix + sepChar + " "; // NOI18N
        }
        
        // do not prepend separator if first
        boolean prependSeparator = type.allowText();  //#PCDATA  
        
        // recursive descend through given content model groups
        
        if (type instanceof TypeCollection) {
            TypeCollection col = (TypeCollection) type;
            Collection types = col.getTypes();
                                    
            Iterator it = types.iterator();
            while (it.hasNext()) {
                TreeElementDecl.ContentType next = (TreeElementDecl.ContentType) it.next();
                
                if (prependSeparator) {
                    s.append (separator);
                }
                
                prependSeparator = true;
                    
                commentContentModel(next);
            }                                    
        }

        s.append(")" + type.getMultiplicity());  // NOI18N
        
    }

    private void headline1(String text, String id) {
        s.append("\n\n<hr />\n").appendStartTag(HEADLINE1);  //NOI18N
        if (id != null) s.append("<a name=\"" + id + "\"></a>"); // NOI18N
        s.append(text).appendEndTag(HEADLINE1).append("\n"); // NOI18N        
    }
    
    private void headline1(String text) {
        headline1(text, null);
    }

    private void headline2(String text) {
        s.append("\n").appendStartTag(PAR).appendStartTag(HEADLINE2).append(text).appendEndTag(HEADLINE2).appendEndTag(PAR).append("\n"); // NOI18N
    }
    
    private void listitem(RefList s, String text) {
        s.appendStartTag(LIST_ITEM).append("<tt>").append(text).append("</tt>").appendEndTag(LIST_ITEM).append("\n"); // NOI18N
    }
    
    //~~~~~~~~~~~~~~~~~ BACKGROUND STUFF ~~~~~~~~~~~~~~~~~~~~~~~

    
    

    /*
     * @return RefList for given tag, never null
     */
    private RefList getRefList(String element) {
  
        RefList toret = (RefList) elementRefs.get(element);
        
        if (toret == null) {
            toret = new RefList();
            elementRefs.put(element, toret);
        }
        
        return toret;
    }
    
        
    /** Ref list allows embedding an empty list that is contructed later. 
     * The implementation mimics StringBuffer append() behaviour.
     */
    private class RefList extends LinkedList {
        
        /** Serial Version UID */
        private static final long serialVersionUID = 4291872863957329823L;

        /**
         * @param obj accepts String, StringBuffer or RefList instances.
         */
        public boolean add(Object obj) {
            if (obj instanceof StringBuffer || obj instanceof RefList || obj instanceof String) {
                return super.add(obj);
            } else {
                throw new ClassCastException("String or RefList required."); // NOI18N
            }
        }
        
        public RefList append(String s) {
            if (size() != 0 && (getLast() instanceof StringBuffer)) {
                StringBuffer last = (StringBuffer) getLast();
                last.append(s);
            } else {
                add(new StringBuffer(s));                
            }
            return this;
        }

        public RefList append(RefList s) {
            add(s);
            return this;
        }
        
        /** Append it as fragment only if this fragment does not exist */
        public RefList appendUniq(String fragment) {
            if (!contains(fragment)) add(fragment);
            return this;
        }
        
        // append start tag
        RefList appendStartTag(String tag) {
            return append("<" +  tag + ">"); // NOI18N
        }
        
        RefList appendEndTag(String tag) {
            return append("</" + tag + ">"); // NOI18N
        }

        
        public String toString() {
            Iterator it = iterator();
            StringBuffer buf = new StringBuffer();
            while(it.hasNext()) {
                buf.append(it.next().toString());
            }
            return buf.toString();            
        }
                
    }
}
