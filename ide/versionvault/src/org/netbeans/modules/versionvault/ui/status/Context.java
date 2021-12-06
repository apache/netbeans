/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.status;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * This whole class is a hack until we won't have a serializable VCSContext. see issue #129268
 * @author Tomas Stupka
 */
class Context implements Serializable {        
    private static final long serialVersionUID = 1L;

    private final Set<File> rootFiles;
    private final Set<File> exclusions;
    private transient VCSContext vcsContext;

    public Context(VCSContext vcsContext) {

        assert vcsContext != null;

        rootFiles = vcsContext.getRootFiles();
        exclusions = vcsContext.getExclusions();
        this.vcsContext = vcsContext;
    }

    Set<File> getRootFiles() {
        return rootFiles;
    }

    boolean contains(File file) {
        outter : for (File root : rootFiles) {
            if (org.netbeans.modules.versioning.util.Utils.isAncestorOrEqual(root, file)) {
                for (File excluded : exclusions) {
                    if (org.netbeans.modules.versioning.util.Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    VCSContext getVCSContext() {        
        if(vcsContext == null) {                
            // the class must have been deserialized. vcsContext  is transient, we have to reconstruct it somehow. 
            // XXX this way it won't work for project exclusions ... (wontfix, it's a hack anyway)
            List<Node> nodes = new ArrayList<Node>();
            for(File root : rootFiles) {
                nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(root)));    
            }                
            vcsContext = VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
        }
        return vcsContext;
    }                
}
