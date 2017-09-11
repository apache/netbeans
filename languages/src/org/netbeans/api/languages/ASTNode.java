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

package org.netbeans.api.languages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Represents one AST node.
 * 
 * @author Jan Jancura
 */
public class ASTNode extends ASTItem {
   
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param children   list of tokens (ASTToken) and subnodes (ASTNode)
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode createCompoundASTNode (
        Language    language,
        String      nt,
        List<ASTItem> children,
        int         offset
    ) {
        return new CompoundNode (language, nt, offset, children);
    }
   
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param children   list of tokens (ASTToken) and subnodes (ASTNode)
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode create (
        Language    language,
        String      nt,
        List<ASTItem> children,
        int         offset
    ) {
        return new ASTNode (language, nt, offset, children);
    }
    
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode create (
        Language    language,
        String      nt,
        int         offset
    ) {
        return new ASTNode (language, nt, offset, Collections.<ASTItem>emptyList ());
    }

    
    private String      nt;

    private ASTNode (
        Language    language, 
        String      nt, 
        int         offset,
        List<ASTItem> children
    ) {
        super (language, offset, -1, children);
        if ( (!getClass ().equals (ASTNode.class)) &&
             (!getClass ().equals (CompoundNode.class))
        ) throw new IllegalArgumentException ("Do not extend ASTNode!");
        this.nt =       nt;
    }

    /**
     * Returns the name of non terminal.
     * 
     * @return name of non terminal
     */
    public String getNT () {
        return nt;
    }

    /**
     * Finds path to the first token defined by type and identifier or null.
     *
     * @param type          a type of token or null
     * @param identifier    a value of token or null
     * 
     * @return path to the first token defined by type and identifier or null
     */
//    public ASTPath findToken (String type, String identifier) {
//        List<ASTItem> path = new ArrayList<ASTItem> ();
//        findToken (type, identifier, path);
//        if (path.isEmpty ()) return null;
//        return ASTPath.create (path);
//    }
//    
//    private boolean findToken (String type, String identifier, List<ASTItem> path) {
//        path.add (this);
//        Iterator it = getChildren ().iterator ();
//        while (it.hasNext ()) {
//            Object e = it.next ();
//            if (e instanceof ASTToken) {
//                ASTToken t = (ASTToken) e;
//                if (type != null && !type.equals (t.getType ())) continue;
//                if (identifier != null && !identifier.equals (t.getIdentifier ())) continue;
//                return true;
//            } else
//                if (((ASTNode) e).findToken (type, identifier, path))
//                    return true;
//        }
//        path.remove (path.size () - 1);
//        return false;
//    }
    
    /**
     * Returns top-most subnode of this node on given offset with given 
     * non terminal name.
     * 
     * @param nt        name of non terminal
     * @param offset    offset of node
     * 
     * @return MIME     top-most subnode of this node on given offset 
     *                  with given non terminal name
     */
    public ASTNode findNode (String nt, int offset) {
        if (nt.equals (getNT ())) return this;
        Iterator<ASTItem> it = getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem e = it.next ();
            if (e instanceof ASTNode) {
                ASTNode node = (ASTNode) e;
                if (node.getOffset () <= offset &&
                    offset < node.getEndOffset ()
                )
                    return node.findNode (nt, offset);
            }
        }
        return null;
    }

    /**
     * Returns identifier of some subtoken with given type.
     * 
     * @param type type of subtoken to be returned
     * 
     * @return identifier of some subtoken with given type
     */
    public String getTokenTypeIdentifier (String type) {
        ASTToken token = getTokenType (type);
        if (token == null) return null;
        return token.getIdentifier ();
    }
    
    /**
     * Returns some subtoken with given type.
     * 
     * @param type type of subtoken to be returned
     * 
     * @return some subtoken with given type
     */
    public ASTToken getTokenType (String type) {
        ASTNode node = this;
        int i = type.lastIndexOf ('.');
        if (i >= 0)
            node = getNode (type.substring (0, i));
        if (node == null) return null;
        Object o = node.getChild ("token-type-" + type.substring (i + 1));
        if (o == null) return null;
        if (!(o instanceof ASTToken)) return null;
        return (ASTToken) o;
    }
    
    /**
     * Returns child node of this node with given path ("foo.goo.boo").
     * 
     * @param path "foo.goo.boo" like path to some subnode
     * 
     * @return child node of this node with given path
     */
    public ASTNode getNode (String path) {
        ASTNode node = this;
        int s = 0, e = path.indexOf ('.');
        while (e >= 0) {
            node = (ASTNode) node.getChild ("node-" + path.substring (s, e));
            if (node == null) return null;
            s = e + 1;
            e = path.indexOf ('.', s);
        }
        return (ASTNode) node.getChild ("node-" + path.substring (s));
    }
    
    /**
     * Adds child to the end of list of children.
     * 
     * @param item a child to be added
     */
    @Override
    public void addChildren (ASTItem item) {
        super.addChildren (item);
        if (nameToChild != null)
            if (item instanceof ASTToken) {
                ASTToken t = (ASTToken) item;
                nameToChild.put ("token-type-" + t.getTypeName (), t);
            } else {
                nameToChild.put (
                    "node-" + ((ASTNode) item).getNT (), 
                    item
                );
            }
    }
    
    /**
     * Removes child.
     * 
     * @param item a child to be added
     */
    @Override
    public void removeChildren (ASTItem item) {
        super.removeChildren (item);
        if (nameToChild != null)
            if (item instanceof ASTToken) {
                ASTToken t = (ASTToken) item;
                nameToChild.remove ("token-type-" + t.getTypeName ());
            } else {
                nameToChild.remove (
                    "node-" + ((ASTNode) item).getNT ()
                );
            }
    }
    
    /**
     * Removes child.
     * 
     * @param item a child to be added
     */
    @Override
    public void setChildren (int index, ASTItem item) {
        ASTItem old = getChildren ().get (index);
        if (nameToChild != null)
            if (old instanceof ASTToken) {
                ASTToken t = (ASTToken) old;
                nameToChild.remove ("token-type-" + t.getTypeName ());
            } else {
                nameToChild.remove (
                    "node-" + ((ASTNode) old).getNT ()
                );
            }
        super.setChildren (index, item);
        if (nameToChild != null)
            if (item instanceof ASTToken) {
                ASTToken t = (ASTToken) item;
                nameToChild.put ("token-type-" + t.getTypeName (), item);
            } else {
                nameToChild.put (
                    "node-" + ((ASTNode) item).getNT (),
                    item
                );
            }
    }
    
    private Map<String,ASTItem> nameToChild = null;
    
    private Object getChild (String name) {
        if (nameToChild == null) {
            nameToChild = new HashMap<String,ASTItem> ();
            Iterator<ASTItem> it = getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                if (item instanceof ASTToken) {
                    ASTToken t = (ASTToken) item;
                    nameToChild.put ("token-type-" + t.getTypeName (), t);
                } else {
                    nameToChild.put (
                        "node-" + ((ASTNode) item).getNT (), 
                        item
                    );
                }
            }
        }
        return nameToChild.get (name);
    }
    
    /**
     * Returns text representation of this node.
     * 
     * @return text representation of this node
     */
    public String print () {
        return print ("");
    }
    
    private String print (String indent) {
        StringBuilder sb = new StringBuilder ();
        sb.append (indent).append ("ASTNode ").append (getNT ()).append (' ').
            append (getOffset ()).append ('-').append (getEndOffset ());
        indent = "  " + indent;
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode) {
                sb.append ('\n').append (((ASTNode) elem).print (indent));
            } else
                sb.append ('\n').append (indent).append (elem);
        }
        return sb.toString ();
    }
    
    /**
     * Returns text content of this node.
     * 
     * @return text content of this node
     */
    public String getAsText () {
        StringBuilder sb = new StringBuilder ();
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode)
                sb.append (((ASTNode) elem).getAsText ());
            else
                sb.append (((ASTToken) elem).getIdentifier ());
        }
        return sb.toString ();
    }
    
    /**
     * Returns string representation of this object.
     * 
     * @return string representation of this object
     */
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("ASTNode ").append (getNT ()).append (' ').
            append (getOffset ()).append ('-').append (getEndOffset ());
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode)
                sb.append ("\n    ").append (((ASTNode) elem).getNT () + "...");
            else
                sb.append ("\n    ").append (elem);
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    private static final class CompoundNode extends ASTNode {
        
        CompoundNode (Language language, String nt, int offset, List<ASTItem> children) {
            super (language, nt, offset, children);
        }
    
        /**
         * Returns path from this item to the item on given offset.
         * 
         * @param offset offset
         * 
         * @return path from this item to the item on given offset
         */
        @Override
        public ASTPath findPath (int offset) {
            ASTPath result = null;
            Iterator<ASTItem> it = getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                if (offset < item.getEndOffset () &&
                    item.getOffset () <= offset
                ) {
                    List<ASTItem> p = new ArrayList<ASTItem> ();
                    p.add (this);
                    ASTPath path = item.findPath (p, offset);
                    if (result == null ||
                        path.getLeaf ().getLength () < result.getLeaf ().getLength ()
                    )
                        result = path;
                }
            }
            if (result == null)
                result = ASTPath.create (this);
            return result;
        }
    }
}

