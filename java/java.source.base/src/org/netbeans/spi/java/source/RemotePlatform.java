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
package org.netbeans.spi.java.source;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public interface RemotePlatform {
    
    public static @CheckForNull RemotePlatform lookupRemotePlatform(FileObject source) {
        for (Provider p : Lookup.getDefault().lookupAll(Provider.class)) {
            RemotePlatform rp = p.findPlatform(source);
            if (rp != null) return rp;
        }
        return null;
    }

    public static boolean hasRemotePlatform(FileObject source) {
        RemotePlatform rp = lookupRemotePlatform(source);
        return rp != null && rp.isEnabled();
    }

    public boolean isEnabled();
    public String getJavaCommand();
    public List<String> getJavaArguments();
    public void addChangeListener(ChangeListener l);
    public void removeChangeListener(ChangeListener l);
    
    public static interface Provider {
        public @CheckForNull RemotePlatform findPlatform(FileObject source);
    }

}
