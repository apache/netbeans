/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.spi.viewmodel;

/**
 * Data model for tree that supports reordering
 * of child nodes. The created {@link org.openide.nodes.Node} will contain
 * an implementation of {@link org.openide.nodes.Index} in it's lookup
 * if {@link #canReorder(java.lang.Object)} returns <code>true</code>.
 * <p>
 * When used together with {@link DnDNodeModel}, children can be reordered
 * by Drag and Drop.
 *
 * @author Martin Entlicher
 * @since 1.25
 */
public interface ReorderableTreeModel extends TreeModel {

    /**
     * Provide if this model implementation can reorder children nodes.
     * @param parent The parent node of children that are test for reorder
     * @return <code>true</code> if this model can handle reordering of children,
     *         <code>false</code> otherwise
     * @throws UnknownTypeException if this model implementation is not
     *         able to decide the reorder capability for given node type
     */
    public boolean canReorder(Object parent) throws UnknownTypeException;

    /**
     * Reorder children nodes with a given permutation.
     * @param parent The parent node of children that are being reordered
     * @param perm permutation with the length of current child nodes. The permutation
     * lists the new positions of the original nodes, that is, for nodes
     * <code>[A,B,C,D]</code> and permutation <code>[0,3,1,2]</code>, the final
     * order would be <code>[A,C,D,B]</code>.
     * @throws IllegalArgumentException if the permutation is not valid
     * @throws UnknownTypeException if this model implementation is not
     *         able to perform the reorder for given node type
     */
    public void reorder(Object parent, int[] perm) throws UnknownTypeException;

}
