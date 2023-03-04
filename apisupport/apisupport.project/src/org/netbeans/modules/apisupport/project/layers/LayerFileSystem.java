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

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import java.util.Locale;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.StatusDecorator;

public class LayerFileSystem extends MultiFileSystem {

    protected final BadgingSupport status;

    public LayerFileSystem(final FileSystem[] layers) {
        super(layers);
        status = new BadgingSupport(this);
        status.setSuffix("_" + Locale.getDefault());
        setPropagateMasks(true);
    }

    @Override
    public StatusDecorator getDecorator() {
        return status;
    }


    public FileSystem[] getLayerFileSystems() {
        return getDelegates();
    }

    @Override
    protected FileSystem createWritableOn(String name) throws IOException {
        if( name.endsWith(LayerUtil.HIDDEN) ) {
            FileObject fo = findResource(name);
            if( null != fo ) {
                try {
                    FileSystem fs = findSystem(fo);
                    if( fs.isReadOnly() )
                        throw new IOException();
                } catch( IllegalArgumentException e ) {
                    //ignore
                }
            }
        }
        return super.createWritableOn(name);
    }
}
