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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.viewmodel;



/**
 * Defines data model for tree.
 *
 * @author   Jan Jancura
 */
public interface TreeModel extends Model {

    /**
     * Constant for root node. This root node should be used if root node
     * does not represent any valuable information and should not be visible in
     * tree.
     */
    public static final String ROOT = "Root";

    /**
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    public abstract Object getRoot ();
    
    /** 
     * Returns children for given parent on given indexes.<p>
     * This method works in pair with {@link #getChildrenCount}, the <code>to</code>
     * parameter is up to the value that is returned from {@link #getChildrenCount}.
     * If the list of children varies over time, the implementation code
     * needs to pay attention to bounds and check the <code>from</code> and
     * <code>to</code> parameters, especially if {@link #getChildrenCount}
     * returns <code>Integer.MAX_VALUE</code>. Caching of the children between
     * {@link #getChildrenCount} and {@link #getChildren} can be used as well,
     * if necessary.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     * @see #getChildrenCount
     */
    public abstract Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException;
    
    /**
     * Returns true if node is leaf.
     * 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (Object node) throws UnknownTypeException;
    
    /**
     * Returns the number of children for given node.<p>
     * This method works in pair with {@link #getChildren}, which gets
     * this returned value (or less) as the <code>to</code> parameter. This method
     * is always called before a call to {@link #getChildren}. This method can
     * return e.g. <code>Integer.MAX_VALUE</code> when all children should be
     * loaded.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  the children count
     * @since 1.1
     * @see #getChildren
     */
    public abstract int getChildrenCount (Object node) 
    throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeModelListener (ModelListener l);
}
