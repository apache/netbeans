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
package org.netbeans.modules.html.editor.lib.dtd;


import java.util.*;
import org.netbeans.modules.html.editor.lib.api.model.NamedCharRef;

/** The interface representing SGMLish Document Type Definition. There is separate
 * instance for every DTD ID.
 * The DTD in whole provides informations about Elements, Attributes, their types,
 * possible Values, Character references and Content models.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface DTD {

    /** Identify this instance of DTD
     * @return the name under which should be this DTD registered in DTD registry.
     */
    public String getIdentifier();
    
    /** Get List of all Elements whose names starts with given prefix
     * @param prefix the prefix all returned Elements must start with. For empty
     *    string or <CODE>null</CODE>, List of all Elements from this DTD will be returned.
     *    The implementation <B>must</B> handle <CODE>null</CODE> correctly.
     * @return List of all Elements from this DTD starting with <CODE>prefix</CODE>,
     * or empty List if no such Element found. Never returns <CODE>null</CODE>.
     */
    public List getElementList( String prefix );
    
    /** Get the Element of given name.
     * @return DTD.Element for given name or <CODE>null</CODE>, if no such Element
     * exists in this DTD.
     */
    public DTD.Element getElement( String name );
    
    /** Get List of all CharRefs whose aliases starts with given prefix.
     * @param prefix the requred prefix of CharRefs. For empty string
     *   or <CODE>null</CODE>, List of all CharRefs from this DTD is returned.
     *   The implementation <B>must</B> handle <CODE>null</CODE> correctly.
     * @return List of all such CharRefs, maybe empty, never <CODE>null</CODE>.
     */
    public List getCharRefList( String prefix );
    
   /** Get the CharRef of given name.
    * @return DTD.CharRef for given name or <CODE>null</CODE>, if no such CharRef
    * exists in this DTD.
    */
    public DTD.CharRef getCharRef( String name );
    
    
    /** Element is the interface providing informations about HTML Element
     * and its content model.
     */
    public static interface Element {
        
        /** Get the name of this Element
         */
        public String getName();
        
        /** Shorthand to resolving if content model of this Element is EMPTY
         * @return true iff content model of this Element is EMPTY.
         */
        public boolean isEmpty();
        
        /** Tells if this Element has optional Start Tag. */
        public boolean hasOptionalStart();
        
        /** Tells if this Element has optional End Tag. */
        public boolean hasOptionalEnd();
        
        /** Get the List of Attributes of this Element, which starts with
         * given <CODE>prefix</CODE>.
         * @param prefix the requred prefix of Attributes. For empty string
         *   or <CODE>null</CODE>, List of all Attributes of this Element is returned.
         *   The implementation <B>must</B> handle <CODE>null</CODE> correctly.
         * @return List of all such Attributes, maybe empty, never <CODE>null</CODE>.
         */
        public List getAttributeList( String prefix );
        
        /** Get the Attribute of given name.
         * @return DTD.Attribute for given name or <CODE>null</CODE>, if no such
         * Attribute exists in this Element.
         */
        public DTD.Attribute getAttribute( String name );
        
        /** Get the content model of this Element */
        public DTD.ContentModel getContentModel();
        
    }
    
    
    /**
     * Interface providing informations about one type of attribute.
     * Every Element provides List of its' Attributes, which in turn provide.
     * information about their types and possible values.
     */
    public static interface Attribute {
        
        /** attribute of boolean type - the one which can't have "= smgt." after it */
        public static final int TYPE_BOOLEAN = 0;
        /** attribute of one-of-set type - the one which can complete value */
        public static final int TYPE_SET = 1;
        /** attribute of some base type like NUMBER, CDATA, ID, NAME,... */
        public static final int TYPE_BASE = 2;
        
        public static final String MODE_IMPLIED = "#IMPLIED"; // NOI18N
        public static final String MODE_REQUIRED = "#REQUIRED"; // NOI18N
        public static final String MODE_FIXED = "#FIXED"; // NOI18N
        
        
        /** @return name of this attribute */
        public String getName();
        
        /** @return type of this attribute, could be TYPE_BOOLEAN,
         * TYPE_SET or TYPE_BASE
         */
        public int getType();
        
        /** The base type of this attribute. Used only for TYPE_BASE
         * attributes.
         * @return the base type, like CDATA, NUMBER, ID, if known
         * (getType() == TYPE_BASE, null elsewhere.
         */
        public String getBaseType();
        
        /** Only helper method, should return the last entity name through
         * which was this Attribute's type defined. e.g. for color attrib in:
         * <!ENTITY % Color "CDATA"> <ATTLIST FONT color %Color #IMPLIED>
         * should this method return "Color".
         * May return <CODE>null</CODE>.
         */
        public String getTypeHelper();
        
        /** This method is used to obtain default value information.
         * @returns the default value or one of MODE_IMPLIED, MODE_REQUIRED
         *    or MODE_FIXED constants.
         */
        public String getDefaultMode();
        
        /** Shorthand for determining if defaultMode is "#REQUIRED" */
        public boolean isRequired();
        
        /** The way how to obtain possible values for TYPE_SET Attributes
         * @param prefix required prefix, or <CODE>null</CODE>, if all
         *   possible values are required.
         * @return List of Values starting with prefix, from this attribute
         * if it is of TYPE_SET. For other types, it doesn't make a sense
         * and returns null.
         */
        public List getValueList( String prefix );
        
        /** Get the value of given name.
         */
        public Value getValue( String name );
        
    }
    
    /* Simple shell for value, maybe there will be some additional info about
     * value in future */
    public static interface Value {
        public String getName();
    }
    
    /** The interface representing Character reference. Provides its name
     * and character it refers to
     */
    public static interface CharRef extends NamedCharRef {
        /** @return alias to this CharRef */
        @Override
        public String getName();
        
        /** @return the character this alias is for */
        @Override
        public char getValue();
    }
    
    
    /** The interface representing Content model of an Element. Content model
     * is based on expression matching some sequence of Elements (the Content)
     * and Set of added and excluded Elements. The point of added and excluded
     * Elements is that they are "sticky" - are propagated down the hierarchy
     * of Elements.
     */
    public static interface ContentModel {
        
        /** @return the Content tree part of this model */
        public Content getContent();
        
        /** @return Set of Elements which are additionally possible anywhewe
         * (recursively) in the content of the Element which has this
         * ContentModel, unless explicitely excluded. Inclusion can not
         * override explicit exclusion.
         */
        public Set getIncludes();
        
        /** @return Set of Elements which are recursively excluded from
         * ContentModel of all Elements inside the Element with this ContentModel.
         * Exclusion overrieds inclusion, but not otherwise.
         */
        public Set getExcludes();
        
    }
    
    /** This interface represents an element of content tree. Its instances
     * should be either instances of ContetLeaf or instances of ContentNode.
     */
    public static interface Content {
        static class EmptyContent implements Content {
            public boolean isDiscardable() { return true; }
            public Content reduce( String name ) { return null; }
            public Set getPossibleElements() { return new TreeSet(); }
        }
        
        public static Content EMPTY_CONTENT = new EmptyContent();
        
/*        public static Content EMPTY_CONTENT = new Content() {
            public boolean isDiscardable() { return true; }
            public Content reduce( String name ) { return null; }
            public Set getPossibleElements() { return new TreeSet(); }
        };
*/                
        /** Tells whether this content can be discarded - i.e. matches
         * empty sequence of elements.
         * @return true iff this Content matches empty sequence */
        public boolean isDiscardable();
        
        /** Make a left reduction of this Content. Match the given element
         * and create a content model of the rest. Notify caller, when given
         * element doesn't match this Content
         * @return reduced Content, if element left-reduces the content,
         * in the case the reduction lead  to empty content, return
         * <CODE>EMPTY_CONTENT</CODE>.
         * If the element doesn't reduce the content, return <CODE>null</CODE>.
         */
        public Content reduce( String elementName );        

        /** Return the Set of all DTD.Elements that are permitted by this Content */
        public Set getPossibleElements();
    }
    
    /** ContentLeaf is leaf of content tree, matches just one Element name (String)*/
    public static interface ContentLeaf extends Content {
        /** get the Element of this leaf Content */
        public Element getElement();

        /** @return name of the leaf element. */
        public String getElementName();
    }
    
    /** ContentNode is node of content tree, contains one operator (either unary
     * or n-ary) and sequence of elements on which this operator is applied.
     */
    public static interface ContentNode extends Content {

        /** Get the operator for this node, could be unary ('+', '*', '?')
         * or n-ary ('|', '&', ',')
         */
        public char getType();
    }
}
