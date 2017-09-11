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
package org.openide.util.datatransfer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.io.IOException;


/** Describes a type that can be created anew. Used by <a href="@org-openide-nodes/org/openide/nodes.Node#getNewTypes">Node.getNewTypes</a>.
*
* @author Jaroslav Tulach
*/
public abstract class NewType extends Object implements HelpCtx.Provider {
    /** Display name for the creation action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(NewType.class).getString("Create");
    }

    /** Help context for the creation action.
    * @return the help context
    */
    public HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public abstract void create() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Create the object at a specific position.
    * The default implementation simply calls {@link #create()}.
    * Subclasses may
    * allow pastes to a specific index in their
    * children list (if the object has children indexed by integer).
    *
    * @param indx index to insert into, can be ignored if not supported
    * @throws IOException if something fails
    *
    public void createAt (int indx) throws IOException {
      create ();
    }
    */
}
