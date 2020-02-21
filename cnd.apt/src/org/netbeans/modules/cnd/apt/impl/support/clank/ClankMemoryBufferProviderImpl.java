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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.clang.tools.services.spi.ClankMemoryBufferProvider;
import org.llvm.support.MemoryBuffer;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.spi.APTBufferProvider;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ClankMemoryBufferProvider.class, position = 100)
public class ClankMemoryBufferProviderImpl implements ClankMemoryBufferProvider{

    @Override
    public Map<String, MemoryBuffer> getRemappedBuffers() {
        Map<String, MemoryBuffer> result = Collections.<String, MemoryBuffer>emptyMap();
        APTBufferProvider provider = Lookup.getDefault().lookup(APTBufferProvider.class);
        if (provider != null) {
            Collection<APTFileBuffer> buffers = provider.getUnsavedBuffers();
            if (buffers != null && !buffers.isEmpty()) {
                result = new HashMap<String, MemoryBuffer>();
                for (APTFileBuffer buf : buffers) {
                    String pathAsUrl = CndFileSystemProvider.toUrl(buf.getFileSystem(), buf.getAbsolutePath()).toString();
                    ClankMemoryBufferImpl mb;
                    try {
                        mb = ClankMemoryBufferImpl.create(pathAsUrl, buf.getCharBuffer());
                        result.put(pathAsUrl, mb);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex); //TODO: error processing!!!!
                    }
                }
            }
        }
        return result;
    }
    
}
