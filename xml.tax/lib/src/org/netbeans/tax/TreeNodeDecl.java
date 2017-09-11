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

import java.util.List;
import java.util.LinkedList;

import org.netbeans.tax.event.TreeEventManager;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeNodeDecl extends TreeChild {

    //      /** */
    //      public static final String PROP_OWNER_DTD = "ownerDTD"; // NOI18N

    /** */
    //      private TokenList tokenList;

    //
    // init
    //

    /**
     * Creates new TreeNodeDecl.
     */
    protected TreeNodeDecl () {
        super ();

        //          tokenList = new TokenList();
    }
    
    
    /** Creates new TreeNodeDecl -- copy constructor. */
    protected TreeNodeDecl (TreeNodeDecl nodeDecl) {
        super (nodeDecl);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeDTDRoot getOwnerDTD () {
        TreeDocumentRoot doc = getOwnerDocument ();
        
        if (doc instanceof TreeDTDRoot)
            return (TreeDTDRoot)doc;
        
        if (doc instanceof TreeDocument)
            return ((TreeDocument)doc).getDocumentType ();
        
        return null;
    }
    
    
    
    //
    // Tokens
    //
    
    /**
     *
     */
    protected static class TokenList {
        /** */
        private List tokenList;
        
        /** */
        //        private Map tokenMap;
        
        
        public TokenList () {
            tokenList = new LinkedList ();
            //            tokenMap = new HashMap();
        }
        
        
        public void add (Object token) {
            tokenList.add (token);
        }
        
/*        public void associate (String property, Object token) {
            if (!!! tokenList.contains (token)) {
                return addToken (token);
            }
            tokenMap.put (property, token);
        }*/
        
        public void remove (Object token) {
            tokenList.remove (token);
        }
        
        public int size () {
            return tokenList.size ();
        }
        
    }
    
    
    //
    // content
    //
    
    /**
     *
     */
    public abstract static class Content extends TreeObject {
        
        /** */
        private TreeNodeDecl nodeDecl;
        
        //
        // init
        //
        
        /** Creates new Content. */
        protected Content (TreeNodeDecl nodeDecl) {
            super ();
            
            this.nodeDecl = nodeDecl;
        }
        
        /**
         * Creates new Content. //??? is such content valid?
         */
        protected Content () {
            this ((TreeNodeDecl)null);
        }
        
        /** Creates new Content -- copy constructor. */
        protected Content (Content content) {
            super (content);
            
            this.nodeDecl = content.nodeDecl;
        }
        
        
        //
        // context
        //
        
        /**
         */
        public final boolean isInContext () {
            return ( getNodeDecl () != null );
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public final TreeNodeDecl getNodeDecl () {
            return nodeDecl;
        }
        
        /**
         */
        protected void setNodeDecl (TreeNodeDecl nodeDecl) {
            this.nodeDecl = nodeDecl;
        }
        
        //
        // event model
        //
        
        /** Get assigned event manager assigned to ownerDocument. If this node does not have its one, it returns null;
         * @return assigned event manager (may be null).
         */
        public final TreeEventManager getEventManager () {
            return nodeDecl.getEventManager ();
        }
        
    } // end: class Content
    
} // end: class TreeNodeDecl
