/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.jstestdriver.ui.nodes;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript.jstestdriver.util.JsTestDriverUtils;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class JumpToCallStackAction extends AbstractAction {

    private final String[] stacktraces;
    private final Callback callback;


    @NbBundle.Messages("JumpToCallStackAction.name=&Go to Source")
    public JumpToCallStackAction(String[] stacktraces, Callback callback) {
        assert stacktraces != null;
        this.stacktraces = stacktraces;
        this.callback = callback;

        String name = Bundle.JumpToCallStackAction_name();
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (callback != null) {
            // iterate through stacktraces in order to find the correct test case location
            // if stacktraces.lenght == 1, user probably clicked on a stacktrace node
            // if stacktraces.lenght > 1, user probably clicked on a failed test method node
            for (String callstackFrameInfo : stacktraces) {
                Pair<File, int[]> pair = callback.parseLocation(callstackFrameInfo);
                if (pair != null) {
                    JsTestDriverUtils.openFile(pair.first(), pair.second()[0], pair.second()[1]);
                    return;
                }
            }
        }
    }

    //~ Inner classes

    public interface Callback {
        @CheckForNull
        Pair<File, int[]> parseLocation(String callStack);
    }

}
