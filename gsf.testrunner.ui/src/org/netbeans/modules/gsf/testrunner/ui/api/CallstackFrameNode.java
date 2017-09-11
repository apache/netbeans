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

package org.netbeans.modules.gsf.testrunner.ui.api;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Node representing a callstack
 * @author Marian Petras
 */
public class CallstackFrameNode extends AbstractNode {
    
    /** */
    protected final String frameInfo;
    
    /** Creates a new instance of CallstackFrameNode */
    public CallstackFrameNode(final String frameInfo) {
        this(frameInfo, null);
    }
    
    /**
     * Creates a new instance of CallstackFrameNode
     *
     * @param  frameInfo  line of a callstack, e.g. <code>foo.bar.Baz:314</code>
     * @param  displayName  display name for the node, or <code>null</code>
     *                      to use the default display name for the given
     *                      callstack frame info
     */
    public CallstackFrameNode(final String frameInfo,
                              final String displayName) {
        super(Children.LEAF);
        setDisplayName(displayName != null
                       ? displayName
                       : frameInfo);                            //NOI18N

        this.frameInfo = frameInfo;
    }
    
    /**
     * Gets preferred action.
     * @return preferred action which defaults to {@code null}
     */
    @Override
    public Action getPreferredAction() {
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public String getDisplayName() {
        return TestsuiteNode.cutLine(super.getDisplayName(),
                TestsuiteNode.MAX_MSG_LINE_LENGTH, false); // Issue #172772
    }

}
