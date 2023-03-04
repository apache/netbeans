/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.refactoring;

import javax.swing.Icon;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

class RepositoryTreeElement implements TreeElement {

    private final RepositoryInfo repo;

    RepositoryTreeElement(RepositoryInfo repo) {
        this.repo = repo;
    }

    @Override public TreeElement getParent(boolean isLogical) {
        return null;
    }

    @Override public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/maven/repository/localrepo.png", true);
    }

    @Messages({"# {0} - repository id", "RepositoryTreeElement.text=Maven Repository: <b>{0}</b>"})
    @Override public String getText(boolean isLogical) {
        return Bundle.RepositoryTreeElement_text(repo.getId());
    }

    @Override public Object getUserObject() {
        return repo;
    }

}
