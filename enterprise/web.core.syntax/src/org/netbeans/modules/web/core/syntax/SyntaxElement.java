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

package org.netbeans.modules.web.core.syntax;

import java.util.*;

import javax.swing.text.*;

/**
 *
 * @author  Petr Jiricka, Petr Nejedly
 * @version
 */
public abstract class SyntaxElement extends Object {

    private JspSyntaxSupport support;
    private SyntaxElement previous;
    private SyntaxElement next;

    int offset;
    int length;

    /** Creates new SyntaxElement */
    public SyntaxElement( JspSyntaxSupport support, int from, int to ) {
        this.support = support;
        this.offset = from;
        this.length = to-from;
    }
    
    public abstract int getCompletionContext();

    public int getElementOffset() {
        return offset;
    }

    public int getElementLength() {
        return length;
    }
    
    public SyntaxElement getPrevious() throws BadLocationException {
        if( previous == null ) {
            previous = support.getPreviousElement( offset );
            if( previous != null ) previous.next = this;
        }
        return previous;
    }

    public SyntaxElement getNext() throws BadLocationException {
        if ( next == null ) {
            next = support.getNextElement( offset+length );
            if ( next != null ) next.previous = this;
        }
        return next;
    }

    public String getImage() throws BadLocationException {
        return support.getDocument().getText(offset, length);
    }

    @Override
    public String toString() {
        String content = "???";
        try {
            content = support.getDocument().getText(getElementOffset(), getElementLength());
        }catch(BadLocationException e) {
            //do not handle
        }
        return "Element [" + offset + "," + (offset+length-1) + "] (" + content + ")";    // NOI18N
    }

    public static class Comment extends SyntaxElement {
        public Comment( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.COMMENT_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Comment " + super.toString();   // NOI18N
        }
    }
    
    public static class ExpressionLanguage extends SyntaxElement {
        public ExpressionLanguage( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.EL_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "Expression Language " + super.toString();   // NOI18N
        }
    }

    public static class Text extends SyntaxElement {
        public Text( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.TEXT_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Text " + super.toString();   // NOI18N
        }
    }

    public static class ContentL extends SyntaxElement {
        public ContentL( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.CONTENTL_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Content Language " + super.toString();   // NOI18N
        }
    }

    public static class ScriptingL extends SyntaxElement {
        public ScriptingL( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Scripting Language " + super.toString();   // NOI18N
        }
    }

    public static class Error extends SyntaxElement {
        public Error( JspSyntaxSupport support, int from, int to ) {
            super( support, from, to );
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.ERROR_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Error " + super.toString();   // NOI18N
        }
    }
    
    public abstract static class TagLikeElement extends SyntaxElement {
        String name;

        public TagLikeElement(JspSyntaxSupport support, int from, int to, String name) {
            super( support, from,to );
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return super.toString() + " - '" + name + "'";   // NOI18N
        }
    }

    public static class EndTag extends TagLikeElement {
        public EndTag(JspSyntaxSupport support, int from, int to, String name) {
            super(support, from, to, name);
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP EndTag " + super.toString();   // NOI18N
        }
    }
    

    public abstract static class TagDirective extends TagLikeElement {
        Map attribs;

        public TagDirective( JspSyntaxSupport support, int from, int to, String name, Map attribs ) {
            super(support, from, to, name);
            this.attribs = attribs;
        }

        public Map getAttributes() {
            return attribs;
        }

        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer(super.toString() + " - {" );   // NOI18N

            for( Iterator i = attribs.keySet().iterator(); i.hasNext(); ) {
                Object next = i.next();
                ret.append( next ).
                append( "='" ).   // NOI18N
                append( attribs.get(next) ).
                append( "', "  );   // NOI18N
            }

            ret.append( "}" );   // NOI18N
            return ret.toString();
        }
    }
    
    public static class Tag extends TagDirective {
        /** is tag closed immediately (it has not a body and closing tag) */
        private boolean isClosed;

        public Tag( JspSyntaxSupport support, int from, int to, String name, Map attribs, boolean isClosed) {
            super(support, from, to, name, attribs);
            this.isClosed = isClosed;
        }

        public Tag( JspSyntaxSupport support, int from, int to, String name, Map attribs ) {
            this(support, from, to, name, attribs, false);
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.TAG_COMPLETION_CONTEXT;
        }
        
        public boolean isClosed () {
            return isClosed;
        }
        
        @Override
        public String toString() {
            return "JSP Tag " + super.toString();   // NOI18N
        }
    }

    public static class Directive extends TagDirective {

        public Directive( JspSyntaxSupport support, int from, int to, String name, Map attribs ) {
            super(support, from, to, name, attribs);
        }

        public int getCompletionContext() {
            return JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT;
        }
        
        @Override
        public String toString() {
            return "JSP Directive " + super.toString();   // NOI18N
        }
    }


}
