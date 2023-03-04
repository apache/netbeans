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
package org.netbeans.modules.profiler.ui;

import java.io.IOException;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * HPROF heapdump DataObject
 *
 * @author Tomas Hurka
 */
@MIMEResolver.Registration(
    displayName="org.netbeans.modules.profiler.Bundle#HprofResolver",
    position=99900,
    resource="../DummyHprofResolver.xml",
    showInFileChooser = { "#LBL_ProfilerFiles" }
)
@DataObject.Registration(
    iconBase = "org/netbeans/modules/profiler/impl/icons/snapshotDataObject.png",
    mimeType = "application/x-netbeans-profiler-hprof",
    position=1000
)
public class HprofDataObject extends MultiDataObject implements OpenCookie {
    
    public HprofDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    public void open() {
        // display dialog, no viewer available
    }
}
