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
package org.netbeans.tax;

import java.util.Collection;

import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;

import org.netbeans.tax.decl.EMPTYType;
import org.netbeans.tax.decl.parser.ParserReader;
import org.netbeans.tax.decl.parser.ContentSpecParser;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeElementDecl extends TreeNodeDecl implements DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    /** */
    public static final String PROP_NAME         = "name"; // NOI18N
    /** */
    public static final String PROP_CONTENT_TYPE = "contentType"; // NOI18N
    
    
    /** */
    private String name;
    
    /** */
    private ContentType contentType;
    
    //      /** */
    //      private String contentTypeString;
    
    
    //
    // init
    //
    
    /** Creates new TreeElementDecl.
     * @throws InvalidArgumentException
     */
    public TreeElementDecl (String name, ContentType contentType) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        checkContentType (contentType);
        
        this.name        = name;
        this.contentType = contentType;
        this.contentType.setNodeDecl (this);
    }
    
    
    //     /** Creates new TreeElementDecl.
    //      * @throws InvalidArgumentException
    //      */
    //     public TreeElementDecl (String name, String contentType) throws InvalidArgumentException {
    //         this (name, new ContentSpecParser().parseModel (new ParserReader (contentType)));
    //     }
    
    /** Creates new TreeElementDecl -- copy constructor. */
    protected TreeElementDecl (TreeElementDecl elementDecl) {
        super (elementDecl);
        
        this.name        = elementDecl.name;
        this.contentType = (ContentType)elementDecl.contentType.clone ();
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeElementDecl (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeElementDecl peer = (TreeElementDecl) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        if (!!! Util.equals (this.contentType, peer.contentType))
            return false;
        
        return true;
    }
    
    /*
     * Merges name and content type properties.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeElementDecl peer = (TreeElementDecl) treeObject;
        
        setNameImpl (peer.getName ());
        setContentTypeImpl (peer.getContentType ());
        //        contentType.merge (peer.contentType);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final String getName () {
        return name;
    }
    
    /**
     */
    private final void setNameImpl (String newName) {
        String oldName = this.name;
        
        this.name = newName;
        
        firePropertyChange (PROP_NAME, oldName, newName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setName (String newName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.name, newName) )
            return;
        checkReadOnly ();
        checkName (newName);
        
        //
        // set new value
        //
        setNameImpl (newName);
    }
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkElementDeclName (name);
    }
    
    /**
     */
    public final ContentType getContentType () {
        return contentType;
    }
    
    /**
     */
    private final void setContentTypeImpl (ContentType newContentType) {
        ContentType oldContentType = this.contentType;
        
        this.contentType = newContentType;
        
        firePropertyChange (PROP_CONTENT_TYPE, oldContentType, newContentType);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setContentType (ContentType newContentType) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.contentType, newContentType) )
            return;
        checkReadOnly ();
        checkContentType (newContentType);
        
        //
        // set new value
        //
        setContentTypeImpl (newContentType);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setContentType (String newContentType) throws ReadOnlyException, InvalidArgumentException {
        setContentType (new ContentSpecParser ().parseModel (new ParserReader (newContentType)));
    }
    
    /**
     */
    protected final void checkContentType (ContentType contentType) throws InvalidArgumentException {
        TreeUtilities.checkElementDeclContentType (contentType);
    }
    
    /** @return true for mixed model. */
    public boolean isMixed () {
        return getContentType ().isMixed ();
    }
    
    /** */
    public boolean isEmpty () {
        return ! (allowText () || allowElements ());
    }
    
    /** */
    public boolean allowText () {
        return getContentType ().allowText ();
    }
    
    /** */
    public boolean allowElements () {
        return getContentType ().allowElements ();
    }
    
    
    /** */
    public Collection getAttributeDefs () {
        if ( getOwnerDTD () == null ) {
            return null;
        }
        return getOwnerDTD ().getAttributeDeclarations (getName ());
    }
    
    
    //
    // ContentType
    //
    
    /**
     *
     */
    public abstract static class ContentType extends Content implements Comparable {
        //  	public static final short TYPE_EMPTY    = 0; // EMPTY
        //  	public static final short TYPE_ANY      = 1; // ANY
        //  	public static final short TYPE_MIXED    = 2; // (#PCDATA|xxx)*
        //  	public static final short TYPE_CHILDREN = 3; // (xxx?,(yyy|zzz*))+
        
        //  	public static final short SEPARATOR_CHOICE   = 10; // xxx|yyy
        //  	public static final short SEPARATOR_SEQUENCE = 11; // xxx,yyy
        
        //  	public static final short OCCURS_ZERO_OR_ONE  = 20; // xxx?
        //  	public static final short OCCURS_EXACTLY_ONE  = 21; // xxx
        //  	public static final short OCCURS_ZERO_OR_MORE = 22; // xxx*
        //  	public static final short OCCURS_ONE_OR_MORE  = 23; // xxx+
        
        /** */
        private String multiplicity;
        
        private int index;  // sample value of counter at creation time
        
        private static int counter = 0; // to be able to create ordered collections
        
        
        //
        // init
        //
        
        /** Creates new ContentType. */
        protected ContentType (TreeElementDecl elementDecl) {
            super (elementDecl);
            
            this.multiplicity = new String ();
            
            this.index = counter++;
        }
        
        
        /** Creates new ContentType. */
        protected ContentType () {
            this ((TreeElementDecl) null);
        }
        
        /** Creates new ContentType -- copy constructor. */
        protected ContentType (ContentType contentType) {
            super (contentType);
            
            this.multiplicity = contentType.multiplicity;
            
            this.index = counter++;
        }
        
        //
        // from TreeObject
        //
        
        /**
         */
        public boolean equals (Object object, boolean deep) {
            if (!!! super.equals (object, deep))
                return false;
            
            ContentType peer = (ContentType) object;
            if (this.index != peer.index)
                return false;
            if (!!! Util.equals (this.getMultiplicity (), peer.getMultiplicity ()))
                return false;
            
            return true;
        }
        
        /*
         * Merges changes from passed object to actual object.
         * @param node merge peer (TreeAttributeDecl)
         * @throws CannotMergeException if can not merge with given node (invalid class)
         */
        public void merge (TreeObject treeObject) throws CannotMergeException {
            super.merge (treeObject);
            
            ContentType peer = (ContentType) treeObject;
            
            index = peer.index;
            setMultiplicity (peer.getMultiplicity ());
        }
        
        
        //
        // context
        //
        
        /**
         */
        public final void removeFromContext () throws ReadOnlyException {
            if ( isInContext () ) {
                getOwnerElementDecl ().setContentTypeImpl (new EMPTYType ());
            }
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public final TreeElementDecl getOwnerElementDecl () {
            return (TreeElementDecl)getNodeDecl ();
        }
        
        
        /**
         */
        public void setMultiplicity (char s) {
            multiplicity = new String (new char[] {s});
        }
        
        /**
         */
        public void setMultiplicity (String s) {
            multiplicity = s;
        }
        
        /** Combines existing multiplicity with new one.
         * <pre>
         *   | 1 | ? | + | *
         * --+---+---+---+---
         * 1 | 1 | ? | + | *
         * --+---+---+---+---
         * ? | ? | ? | * | *
         * --+---+---+---+---
         * + | + | * | + | *
         * --+---+---+---+---
         * * | * | * | * | *
         * </pre>
         */
        public void addMultiplicity (String s) {
            if (multiplicity.equals (s))
                return;
            
            if ("".equals (multiplicity)) { // NOI18N
                multiplicity = s;
            } else if ("".equals (s)) { // NOI18N
                // stay intact
            } else {
                multiplicity = "*"; // NOI18N
            }
        }
        
        
        /** @return multiplicity of given type */
        public String getMultiplicity () {
            return multiplicity;
        }
        
        /** @return true if element itself can contain mixed content. */
        public boolean isMixed () {
            return allowText () && allowElements ();
        }
        
        /** @return true if sub elements are allowed. */
        public abstract boolean allowElements ();
        
        /** @return true if text value is allowed. */
        public abstract boolean allowText ();
        
        /** @return String representation of type. */
        public abstract String toString ();
        
        /** Natural ordering by index. */
        public int compareTo (final Object obj) {
            if (this.equals (obj))
                return 0;
            
            ContentType type = (ContentType) obj;
            
            return index - type.index;
        }
        
    } // end: class ContentType
    
}
