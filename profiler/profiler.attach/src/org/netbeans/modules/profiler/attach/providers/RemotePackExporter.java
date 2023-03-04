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
package org.netbeans.modules.profiler.attach.providers;

import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.profiler.attach.spi.AbstractRemotePackExporter;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "RemotePackExporter_GeneratingRemotePack=Generating Remote Pack to {0}" // NOI18N
})
public final class RemotePackExporter {

    private static final class Singleton {

        private static final RemotePackExporter INSTANCE = new RemotePackExporter();
    }

    public static RemotePackExporter getInstance() {
        return Singleton.INSTANCE;
    }

    private AbstractRemotePackExporter impl = null;
    
    private RemotePackExporter() {
        impl = Lookup.getDefault().lookup(AbstractRemotePackExporter.class);
    }

    public String export(final String exportPath, final String hostOS, final String jvm) throws IOException {
        if (impl == null) {
            throw new IOException();
        }
        
        ProgressHandle ph = ProgressHandle.createHandle(
                Bundle.RemotePackExporter_GeneratingRemotePack(impl.getRemotePackPath(exportPath, hostOS)));
        ph.setInitialDelay(500);
        ph.start();
        try {
            return impl.export(exportPath, hostOS, jvm);
        } finally {
            ph.finish();
        }
    }

    public void export(String hostOS, final String jvm) throws IOException {
        export(null, hostOS, jvm);
    }
    
    public boolean isAvailable() {
        return impl != null;
    }
}
