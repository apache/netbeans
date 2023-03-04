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

package org.netbeans.modules.bugtracking.dummies;

import java.io.IOException;
import org.netbeans.modules.bugtracking.api.Repository;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Marian Petras
 */
public class DummyNode extends AbstractNode {
    static final String TEST_REPO = "testrepo";

    private final String name;
    private final Repository repository;
    private FileObject fo;

    public DummyNode(String name) {
        this(name, null, null);
    }

    public DummyNode(String name, Repository repository, FileObject fo) {
        super(Children.LEAF);
        this.name = name;
        this.repository = repository;
        this.fo = fo;
        if(fo != null) {
            try {
                fo.setAttribute(TEST_REPO, repository);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    Repository getAssociatedRepository() {
        return repository;
    }
    
    FileObject getAssociatedFileObject() {
        return fo;
    }

}
