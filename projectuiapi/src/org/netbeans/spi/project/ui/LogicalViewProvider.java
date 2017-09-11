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

package org.netbeans.spi.project.ui;

import org.netbeans.spi.project.ProjectIconAnnotator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.nodes.Node;

/**
 * Ability for a {@link org.netbeans.api.project.Project} to supply
 * a logical view of itself.
 * @see org.netbeans.api.project.Project#getLookup
 * @see ProjectIconAnnotator
 * @author Jesse Glick
 */
public interface LogicalViewProvider extends PathFinder {

    /**
     * Create a logical view node.
     * Projects should not attempt to cache this node in any way;
     * this call should always create a fresh node with no parent.
     * The node's lookup should contain the project object.
     * <p>
     * The node need not bother implementing {@linkplain org.openide.filesystems.FileSystem.Status badging}
     * for the root node; any files {@linkplain org.netbeans.api.project.Sources contained} in the project will
     * be considered as badging sources automatically. Other subnodes representing
     * various collections of files may still need explicit badging logic.
     * <em>As of <code>org.netbeans.modules.projectuiapi/1 1.31</code></em>
     * </p>
     * @return a node displaying the contents of the project in an intuitive way
     * @see CommonProjectActions#forType
     */
    Node createLogicalView();        
    
}
