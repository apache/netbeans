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

package org.netbeans.modules.xml.wizard.impl;

import java.util.Collection;
import java.util.Collections;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * A placeholder node that displays a "please wait" message while the
 * task to generate the final node is performed.
 *
 * @author  Nathan Fiedler
 */
public class WaitNode extends AbstractNode {
    /** A child key for this node, to be used with Children.Key.setKeys(). */
    public static final Object WAIT_KEY = new Object();

    /**
     * Creates a new instance of WaitNode.
     */
    public WaitNode() {
        super(Children.LEAF);
        setName(NbBundle.getMessage(WaitNode.class, "LBL_WaitNode_Wait"));
        setIconBaseWithExtension("org/netbeans/modules/xml/xam/ui/resources/wait.gif");
    }

    /**
     * Convenience method that creates an array with a single WaitNode.
     *
     * @return  array with a WaitNode.
     */
    public static Node[] createNode() {
        return new Node[] { new WaitNode() };
    }

    /**
     * Convenience method that creates a collection with a single child key
     * entry, that being the WAIT_KEY value.
     *
     * @return  collection with WAIT_KEY.
     */
    public static Collection getKeys() {
        return Collections.singletonList(WaitNode.WAIT_KEY);
    }
}
