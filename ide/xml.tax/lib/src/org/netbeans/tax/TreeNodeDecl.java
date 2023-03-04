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
