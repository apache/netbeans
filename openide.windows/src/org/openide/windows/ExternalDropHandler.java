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

package org.openide.windows;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * When an implementation of this class is available in the global Lookup and
 * an object is being dragged over some parts of the main window of the IDE then
 * the window system may call methods of this class to decide whether it can
 * accept or reject the drag operation. And when the object is actually dropped
 * into the IDE then this class will be asked to handle the drop.
 *
 * @since 6.7
 *
 * @author S. Aubrecht
 */
public abstract class ExternalDropHandler {

    /**
     * @return True if the dragged object can be dropped into the IDE, false
     * if the DataFlavor(s) are not supported.
     */
    public abstract boolean canDrop( DropTargetDragEvent e );

    /**
     * This method is called when the dragged object is already dropped to decide
     * whether the drop can be accepted.
     *
     * @return True if the dropped object is supported (i.e. handleDrop method
     * can process the object), false otherwise.
     */
    public abstract boolean canDrop( DropTargetDropEvent e );

    /**
     * When an object is dropped into the IDE this method must process it (e.g.
     * open the dropped file in a new editor tab).
     *
     * @return True if the dropped object was processed successfully, false otherwise.
     */
    public abstract boolean handleDrop( DropTargetDropEvent e );
}
