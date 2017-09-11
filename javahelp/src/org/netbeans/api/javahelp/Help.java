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

package org.netbeans.api.javahelp;

import javax.swing.event.ChangeListener;


import org.openide.util.HelpCtx;

/** An implementation of the JavaHelp system.
* Get the concrete instance using lookup.
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class Help {

    /** constructor for subclasses
     */
    protected Help() {}

    /** Test whether a given ID is valid in some known helpset.
     * In lazy mode, should be a fast operation; if in doubt say you do not know.
     * @param id the ID to check for validity
     * @param force if false, do not do too much work (be lazy) and if necessary return null;
     *              if true, must return non-null (meaning the call may block loading helpsets)
     * @return whether it is valid, if this is known; else may be null (only permitted when force is false)
     */
    public abstract Boolean isValidID(String id, boolean force);
    
    /** Shows help.
     * <p>Note that for basic usage it may suffice to call {@link HelpCtx#display},
     * avoiding any direct dependency on this module.
     * @param ctx help context
     */
    public void showHelp(HelpCtx ctx) {
        showHelp(ctx, /* #15711 */true);
    }

    /** Shows help.
     * @param ctx help context
     * @param showmaster whether to force the master helpset
     * to be shown (full navigators) even
     * though the supplied ID only applies
     * to one subhelpset
     */
    public abstract void showHelp(HelpCtx ctx, boolean showmaster);

    /** Add a change listener for when help sets change.
     * @param l the listener to add
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /** Remove a change listener.
     * @param l the listener to remove
     */
    public abstract void removeChangeListener(ChangeListener l);

}
