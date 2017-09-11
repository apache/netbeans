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

import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.openide.util.datatransfer.PasteType;


/**
 * Provides extension to {@link NodeModel} with cut/copy/paste and rename
 * functionality, and also allowing to set icons with extension.
 *
 * @author   Martin Entlicher
 * @since 1.12
 */
public interface ExtendedNodeModel extends NodeModel {
    
    /**
     * Test whether this node permits renaming.
     * @return <code>true</code> if so
     */
    public abstract boolean canRename(Object node) throws UnknownTypeException;

    /**
     * Test whether this node permits copying.
     * @return <code>true</code> if so
     */
    public abstract boolean canCopy(Object node) throws UnknownTypeException;

    /**
     * Test whether this node permits cutting.
     * @return <code>true</code> if so
     */
    public abstract boolean canCut(Object node) throws UnknownTypeException;

    /**
     * Called when a node is to be copied to the clipboard.
     * @param node The node object
     * @return the transferable object representing the content of the clipboard
     * @exception IOException when the copy cannot be performed
     */
    public abstract Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException;

    /**
     * Called when a node is to be cut to the clipboard.
     * @param node The node object
     * @return the transferable object representing the content of the clipboard
     * @exception IOException when the cut cannot be performed
     */
    public abstract Transferable clipboardCut(Object node) throws IOException, UnknownTypeException;

    /**
     * Called when a drag is started with this node.
     * The node can attach a transfer listener to ExTransferable and
     * will be then notified about progress of the drag (accept/reject).
     *
     * @param node The node object
     * @return transferable to represent this node during a drag
     * @exception IOException if a drag cannot be started
     *
    public abstract Transferable drag(Object node) throws IOException, UnknownTypeException;
     */

    /**
     * Determine which paste operations are allowed when a given transferable is in the clipboard.
     * For example, a node representing a Java package will permit classes to be pasted into it.
     * @param node The node object
     * @param t the transferable in the clipboard
     * @return array of operations that are allowed
     */
    public abstract PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException;

    /** Determine if there is a paste operation that can be performed
     * on provided transferable. Used by drag'n'drop code to check
     * whether the drop is possible.
     *
     * @param node The node object
     * @param t the transferable
     * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
     * @param index index between children the drop occurred at or -1 if not specified
     * @return null if the transferable cannot be accepted or the paste type
     *    to execute when the drop occurs
     *
    public abstract PasteType getDropType(Object node, Transferable t, int action, int index) throws UnknownTypeException;
     */

    /**
     * Sets a new name for given node.
     *
     * @param node The object to set the new name to.
     * @param name The new name for the given node
     */
    public abstract void setName (Object node, String name) throws UnknownTypeException;

    /**
     * Returns icon resource with extension for given node.
     * This is the preferred way of icon specification over {@link org.netbeans.spi.viewmodel.NodeModel.getIconBase}
     *
     * @param node The node object
     * @return The base resouce name with extension (no initial slash)
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     */
    public abstract String getIconBaseWithExtension (Object node) throws UnknownTypeException;
    
}
