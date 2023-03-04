/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.core.syntax.*;

/** Support for attribute value completion for JSP tags and directives.
 *
 * @author Petr Jiricka
 * @author Marek Fukala
 */
public abstract class AttributeValueSupport {

    private static Map supports;

    public static void putSupport(AttributeValueSupport support) {
        if (supports == null) {
            initialize();
        }
        // trick so we can construct a 'dummy' key element and get the 'real' element
        supports.put(support, support);
    }

    public static AttributeValueSupport getSupport(boolean tag, String longName, String attrName) {
        if (supports == null) {
            initialize();
        }
        AttributeValueSupport support = new AttributeValueSupport.Default(tag, longName, attrName);
        return (AttributeValueSupport) supports.get(support);
    }

    private static void initialize() {
        supports = new HashMap();
        // jsp:useBean
        //putSupport(new AttrSupports.ScopeSupport(true, "jsp:useBean", "scope"));     // NOI18N
        //putSupport(new AttrSupports.ClassNameSupport(true, "jsp:useBean", "class")); // NOI18N
        // jsp:getProperty, jsp:setProperty
        putSupport(new AttrSupports.GetSetPropertyName(true, "jsp:getProperty", "name")); // NOI18N
        putSupport(new AttrSupports.GetSetPropertyName(true, "jsp:setProperty", "name")); // NOI18N
        putSupport(new AttrSupports.GetPropertyProperty());
        putSupport(new AttrSupports.SetPropertyProperty());
        // @taglib
        putSupport(new AttrSupports.TaglibURI());
        putSupport(new AttrSupports.TaglibTagdir());
        // @page
        //putSupport(new AttrSupports.PackageListSupport(false, "page", "import")); // NOI18N
        //putSupport(new AttrSupports.ClassNameSupport(false, "page", "extends")); // NOI18N
        putSupport(new AttrSupports.PageLanguage());
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "session")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "autoFlush")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isThreadSafe")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isErrorPage")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(false, "page", "errorPage")); //NOI18N
        putSupport(new AttrSupports.EncodingSupport(false, "page", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isELIgnored")); // NOI18N
        // @tag 
        //putSupport(new AttrSupports.PackageListSupport(false, "tag", "import")); // NOI18N
        putSupport(new AttrSupports.EncodingSupport(false, "tag", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "tag", "isELIgnored")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(false, "tag", "small-icon")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(false, "tag", "large-icon")); // NOI18N
        putSupport(new AttrSupports.BodyContentSupport(false, "tag", "body-content")); // NOI18N

        // @attribute
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "required")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "fragment")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "rtexprvalue")); // NOI18N
        //putSupport(new AttrSupports.PackageListSupport(false, "attribute", "type")); // NOI18N
        // @variable
        putSupport(new AttrSupports.TrueFalseSupport(false, "variable", "declare")); // NOI18N
        putSupport(new AttrSupports.VariableScopeSupport(false, "variable", "scope")); // NOI18N
        putSupport(new AttrSupports.ClassNameSupport(false, "variable", "variable-class")); // NOI18N
        // @include
        putSupport(new AttrSupports.FilenameSupport(false, "include", "file")); //NOI18N
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:directive.include", "file")); //NOI18N

        // jsp:include, jsp:forward
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:include", "page")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:forward", "page")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:include", "flush")); // NOI18N

        putSupport(new AttrSupports.ScopeSupport(true, "jsp:doBody", "scope")); // NOI18N

        putSupport(new AttrSupports.ScopeSupport(true, "jsp:invoke", "scope")); // NOI18N
        // PENDING - add supports for known attributes

        // jsp:directive.page
        //putSupport(new AttrSupports.PackageListSupport(true, "jsp:directive.page", "import")); // NOI18N
        putSupport(new AttrSupports.ClassNameSupport(true, "jsp:directive.page", "extends")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "session")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "autoFlush")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isThreadSafe")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isErrorPage")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:directive.page", "errorPage")); //NOI18N
        putSupport(new AttrSupports.EncodingSupport(true, "jsp:directive.page", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isELIgnored")); // NOI18N

        //jsp:directive.attribute
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.attribute", "required")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.attribute", "fragment")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.attribute", "rtexprvalue")); // NOI18N
        putSupport(new AttrSupports.PackageListSupport(true, "jsp:directive.attribute", "type")); // NOI18N

        //jsp:directive.page
        //putSupport(new AttrSupports.PackageListSupport(true, "jsp:directive.tag", "import")); // NOI18N
        putSupport(new AttrSupports.EncodingSupport(true, "jsp:directive.tag", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.tag", "isELIgnored")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:directive.tag", "small-icon")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(true, "jsp:directive.tag", "large-icon")); // NOI18N
        putSupport(new AttrSupports.BodyContentSupport(true, "jsp:directive.tag", "body-content")); // NOI18N


        putSupport(new AttrSupports.YesNoTrueFalseSupport(true, "jsp:output", "omit-xml-declaration")); // NOI18N
        putSupport(new AttrSupports.RootVersionSupport(true, "jsp:root", "version")); // NOI18N
        putSupport(new AttrSupports.PluginTypeSupport(true, "jsp:plugin", "type")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:attribute", "trim")); // NOI18N

    }
    protected boolean tag;
    protected String longName;
    protected String attrName;

    /** Creates new AttributeValueSupport 
     * @param isTag whether this support is for tag or directive
     * @param longName either directive name or tag name including prefix
     * @param attribute name
     */
    public AttributeValueSupport(boolean tag, String longName, String attrName) {
        this.tag = tag;
        this.longName = longName;
        this.attrName = attrName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeValueSupport) {
            AttributeValueSupport sup2 = (AttributeValueSupport) obj;
            return (tag == sup2.tag) &&
                    (longName.equals(sup2.longName)) &&
                    (attrName.equals(sup2.attrName));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return longName.hashCode() + attrName.hashCode();
    }
   
    public abstract void result(JspCompletionQuery.CompletionResultSet result, JTextComponent component,
            int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item,
            String valuePart);

    /** Default implementation of AttributeValueSupport. 
     *  Only getPossibleValues method needs to be overriden for simple
     *  attribute support.
     */
    public static class Default extends AttributeValueSupport {

        /** Creates new DefaultAttributeValueSupport 
         * @param isTag whether this support is for tag or directive
         * @param longName either directive name or tag name including prefix
         * @param attribute name
         */
        public Default(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }

        /** Allows subclasses to override the default title. */
        protected String completionTitle() {
            return NbBundle.getMessage(JspKit.class, "CTL_JSP_Completion_Title");
        }

        /** Builds List of completion items.
         *  It uses results from <CODE>possibleValues</CODE> to build the list.
         */
        protected List<JspCompletionItem> createCompletionItems(int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            //int valuePartLength = valuePart.length();
            List values = JspSyntaxSupport.filterStrings(possibleValues(sup, item), valuePart);
            List items = new ArrayList();
            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get(i);
                items.add(JspCompletionItem.createJspAttributeValueCompletionItem(value, offset - valuePart.length()));
            }
            return items;
        }

        /** Should return a list of Strings containing all possible values 
         * for this attribute. May return null if no options are available.
         */
        protected List<String> possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return Collections.emptyList();
        }

        /** Returns the complete result that contains elements from getCompletionItems.  
         *  This implemantation uses createCompletionItems for obtaing of results but may be 
         *  overriden.
         */
        public void result(JspCompletionQuery.CompletionResultSet result, JTextComponent component, int offset,
                JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List items = createCompletionItems(offset, sup, item, valuePart);
            result.addAllItems(items);
            result.setAnchorOffset(offset - valuePart.length());
//            int valuePartLength = valuePart.length ();
//            
//            return new JspCompletionQuery.JspCompletionResult(component, completionTitle(), 
//                items, offset - valuePartLength, valuePartLength, -1);
        }
    }
}
