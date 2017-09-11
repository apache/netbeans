/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.testng.ui.impl.ProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lukas
 */
public abstract class TestActionT extends NbTestCase {

    protected static final Node[] EMPTY_ARRAY = new Node[0];
    protected static final Node[] EMPTY_NODES = new Node[] {new NodeImpl(), new NodeImpl()};
    protected Node[] DATAOBJECT_NODE;
    protected Node[] PROJECT_NODE;
    protected Node[] FILEOBJECT_NODE;
    protected Project p;

    static {
        //NodeActionsInfraHid.install();
    }

    public TestActionT(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        p = new ProjectImpl(root, Lookup.EMPTY);
        PROJECT_NODE = new Node[] {new NodeImpl(p)};
        FILEOBJECT_NODE = new Node[] {new NodeImpl(root)};
        DATAOBJECT_NODE = new Node[] {new NodeImpl(DataObject.find(root))};
    }

    static class NodeImpl extends AbstractNode {

        NodeImpl(Object... toLookup) {
            super(Children.LEAF, toLookup.length < 1 ? Lookup.EMPTY : Lookups.fixed(toLookup));
        }
    }
}
