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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.util.datatransfer.PasteType;

/**
 * Extension of {@link NodeModel} with support for Drag and Drop of nodes.
 *
 * @author Martin Entlicher
 * @since 1.24
 */
public interface DnDNodeModel extends NodeModel {

    /**
     * Action constants from {@link java.awt.dnd.DnDConstants}.
     * No actions are allowed by default.
     * @return int representing set of actions which are allowed when dragging from
     * asociated component.
     */
    int getAllowedDragActions();

    /**
     * Action constants from {@link java.awt.dnd.DnDConstants}.
     * No actions are allowed by default.
     * @param t The transferable for which the allowed drop actions are requested,
     *          or <code>null</code> to get actions for the creation of DropTarget for the view.
     * @return int representing set of actions which are allowed when dropping
     * the transferable into the asociated component.
     */
    int getAllowedDropActions(Transferable t);

    /**
     * Initiate a drag operation.
     * @param node The node to drag
     * @return transferable to represent this node during a drag
     * @throws IOException when the drag cannot be performed
     * @throws UnknownTypeException if this model implementation is not
     *          able to perform drag for given node type
     */
    Transferable drag(Object node) throws IOException, UnknownTypeException;

    /**
     * Determines if there is a paste operation that can be performed
     * on provided transferable when drop is done.
     *
     * @param node The node where to drop
     * @param t the transferable to drop
     * @param action the Drag and Drop action from {@link java.awt.dnd.DnDConstants}
     * @param index index between children the drop occured at or -1 if not specified
     * @return null if the transferable cannot be accepted
     * @throws UnknownTypeException if this model implementation is not
     *          able to perform drop for given node type
     */
    PasteType getDropType(Object node, Transferable t, int action, int index)
            throws UnknownTypeException;

}
