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

package org.netbeans.modules.cnd.testrunner.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Jump to action for call stack lines.
 *
 */
final class JumpToCallStackAction extends AbstractAction {

    /** */
    private final Node node;
    /** */
    private final String callstackFrameInfo;

    private int line;

    /** Creates a new instance of JumpAction */
    JumpToCallStackAction(Node node, String callstackFrameInfo) {
        this(node, callstackFrameInfo, -1);
    }

    /**
     * @param node
     * @param callstackFrameInfo a line in call stack representing the location 
     * to jump
     * @param line the line where to jump, if <code>line == -1</code>, then 
     * the line will be computed from the given <code>callstackFrameInfo</code>.
     */
    JumpToCallStackAction(Node node, String callstackFrameInfo, int line) {
        this.node = node;
        this.callstackFrameInfo = callstackFrameInfo;
        this.line = line;
    }


    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return NbBundle.getMessage(JumpToCallStackAction.class, "LBL_GoToSource");
        }
        return super.getValue(key);
    }

    /**
     * If the <code>callstackFrameInfo</code> is not <code>null</code>,
     * tries to jump to the callstack frame source code. Otherwise does nothing.
     */
    public void actionPerformed(ActionEvent e) {
        if (callstackFrameInfo == null) {
            return;
        }
        
        OutputUtils.openCallstackFrame(node, callstackFrameInfo, line);
    }

}
