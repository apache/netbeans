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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Represents one item in AST tree.
 * 
 * @author jan Jancura
 */
public class ASTItem {
   
    private Language        language;
    private int             offset;
    private int             length = -1;
    private List<ASTItem>   children;

    
    @SuppressWarnings("unchecked")
    ASTItem (
        Language            language,
        int                 offset,
        int                 length,
        List<? extends ASTItem> children
    ) {
        this.language =     language;
        this.offset =       offset;
        this.length =       length;

        // [PENDING]
//        this.children = new ArrayList<ASTItem> ();
//        if (children != null) {
//            Iterator<ASTItem> it = children.iterator ();
//            while (it.hasNext ()) {
//                ASTItem item = it.next ();
//                if (item == null)
//                    throw new NullPointerException ();
//                this.children.add (item);
//            }
//        }
        if (children != null) 
            this.children = (List<ASTItem>) children;
        else
            this.children = Collections.<ASTItem>emptyList ();
    }

    /**
     * Returns offset of this item.
     * 
     * @return offset of this item
     */
    public int getOffset () {
        return offset;
    }
    
    public Language getLanguage () {
        return language;
    }

    /**
     * Returns MIME type of this item.
     * 
     * @return MIME type of this item
     */
    public String getMimeType () {
        if (language == null) return null;
        return language.getMimeType ();
    }

    /**
     * Returns list of all subitems (ASTItem).
     * 
     * @return list of all subitems (ASTItem)
     */
    public List<ASTItem> getChildren () {
        return children;
    }
    
    /**
     * Adds child to the end of list of children.
     * 
     * @param item a child to be added
     */
    void addChildren (ASTItem item) {
        if (children == Collections.<ASTItem>emptyList ())
            children = new ArrayList<ASTItem> ();
        children.add (item);
        if (childrenMap != null)
            childrenMap.put (item.getOffset (), item);
        length = -1;
    }
    
    /**
     * Adds child to the end of list of children.
     * 
     * @param item a child to be added
     */
    void removeChildren (ASTItem item) {
        if (children == Collections.<ASTItem>emptyList ())
            return;
        children.remove (item);
        if (childrenMap != null)
            childrenMap.remove (item.getOffset ());
        length = -1;
    }
    
    /**
     * Adds child to the end of list of children.
     * 
     * @param item a child to be added
     */
    void setChildren (int index, ASTItem item) {
        if (children == Collections.<ASTItem>emptyList ())
            return;
        int oldOffset = children.get (index).getOffset ();
        children.set (index, item);
        if (childrenMap != null) {
            childrenMap.remove (oldOffset);
            childrenMap.put (item.getOffset (), item);
        }
        length = -1;
    }
    
    /**
     * Locks children for any modifications.
     */
    public void lock () {
        children = Collections.<ASTItem>unmodifiableList (children);
    }
    
    /**
     * Returns end offset of this item. Tt is the first offset that is not part 
     * of this node.
     * 
     * @return end offset of this item
     */
    public int getEndOffset () {
        return getOffset () + getLength ();
    }
    
    /**
     * Returns length of this item (end offset - start offset).
     * 
     * @return length of this item (end offset - start offset)
     */
    public int getLength () {
        if (length < 0) {
            List<ASTItem> l = getChildren ();
            if (l.isEmpty ())
                length = 0;
            else {
                ASTItem last = l.get (l.size () - 1);
                length = last.getEndOffset () - getOffset ();
            }
        }
        return length;
    }
    
    /**
     * Returns path from this item to the item on given offset.
     * 
     * @param offset offset
     * 
     * @return path from this item to the item on given offset
     */
    public ASTPath findPath (int offset) {
        return findPath (new ArrayList<ASTItem> (), offset);
    }
    
    ASTPath findPath (List<ASTItem> path, int offset) {
        if (offset < getOffset ()) return ASTPath.create (path);
        if (offset > getEndOffset ()) return ASTPath.create (path);
        path.add (this);
        if (getChildren ().isEmpty ())
            return ASTPath.create (path);
        if (getChildren ().size () > 10)
            return findPath2 (path, offset);
        Iterator<ASTItem> it = getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (offset < item.getEndOffset () &&
                item.getOffset () <= offset
            )
                return item.findPath (path, offset);
        }
        return ASTPath.create (path);
    }

    private ASTPath findPath2 (List<ASTItem> path, int offset) {
        TreeMap<Integer,ASTItem> childrenMap = getChildrenMap ();
        SortedMap<Integer,ASTItem> headMap = childrenMap.headMap (new Integer (offset + 1));
        if (headMap.isEmpty ())
            return ASTPath.create (path);
        Integer key = headMap.lastKey ();
        ASTItem item = childrenMap.get (key);
        ASTPath path2 =  item.findPath (path, offset);
        if (path2 == null)
            return ASTPath.create (path);
        return path2;
    }
    
    private TreeMap<Integer,ASTItem> childrenMap = null;
    
    private TreeMap<Integer,ASTItem> getChildrenMap () {
        if (childrenMap == null) {
            childrenMap = new TreeMap<Integer,ASTItem> ();
            Iterator<ASTItem> it = getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                childrenMap.put (new Integer (item.getOffset ()), item);
            }
        }
        return childrenMap;
    }
}





