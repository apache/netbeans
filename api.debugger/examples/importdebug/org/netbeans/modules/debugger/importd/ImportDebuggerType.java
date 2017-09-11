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

package org.netbeans.modules.debugger.importd;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

import org.netbeans.modules.debugger.AbstractDebuggerType;


/**
* Default debugger type for Import debugger.
*/
public class ImportDebuggerType extends AbstractDebuggerType {

    static final long serialVersionUID = 5234304898551299437L;

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger_Type");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ImportDebuggerType.class);
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain) 
    throws DebuggerException {
        TopManager.getDefault ().getDebugger ().startDebugger (
            new ImportDebuggerInfo (
                info.getClassName (),
                info.getArguments (),
                stopOnMain ? info.getClassName () : null
            )
        );
        return;
    }
    
    /**
     * Should return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}.
     *
     * @param obj DataObject to test
     * @return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}
     */
    public boolean supportsDebuggingOf (DataObject obj) {
        return obj.getPrimaryFile ().getMIMEType ().equals ("text/x-java");
    }
    
    /**
     * Starts debugging for a dataobject. Debugging should stop on given line.
     * This method is called from RunToCursorAction.
     *
     * @param obj object to run
     * @param stopOnLine should the debugging stop on given line or go to
     * first breakpoint (if stopOnLine == <code>null</code>)
     * @exception DebuggerException if debugger is not installed or cannot
     * be started
     */
    public void startDebugger (DataObject obj, Line stopOnline) throws DebuggerException {
        startDebugger (obj, false);
    }
    
}
