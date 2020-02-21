/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.spi.APTBufferProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 */
@ServiceProvider(service = APTBufferProvider.class)
public class APTBuffersProviderImpl implements APTBufferProvider {
    @Override
    public Collection<APTFileBuffer> getUnsavedBuffers() {
        return ModelSupport.instance().getUnsavedBuffers();
    }

    @Override
    public APTFileBuffer getOrCreateFileBuffer(FileObject fileObject) {
        FileImpl csmFile = (FileImpl) ModelImpl.instance().findFile(FSPath.toFSPath(fileObject), false, false);
        return (csmFile == null) ? ModelSupport.createFileBuffer(fileObject) : csmFile.getBuffer();
    }
}
