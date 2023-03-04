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
package org.netbeans.modules.languages.diff;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;


@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-diff",
    position=180,
    displayName="#DiffResolver",
    extension={ "diff", "rej", "patch" }
)
public class DiffDataObject extends MultiDataObject {

    public DiffDataObject (FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super (pf, loader);
        CookieSet cookies = getCookieSet ();
        cookies.add ((Node.Cookie) DataEditorSupport.create (this, getPrimaryEntry (), cookies));
    }

    @Override
    protected Node createNodeDelegate () {
        DataNode node = new DataNode (this, Children.LEAF, getLookup ());
        return node;
    }

    @Override
    public Lookup getLookup () {
        return getCookieSet ().getLookup ();
    }
}
