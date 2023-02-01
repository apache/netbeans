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

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;

/**Specify a different JDK on which selected Java editor features may be run.
 *
 * @since 2.62
 */
public interface RemoteEditorPlatform {

    /**
     * Returns true if and only if remote platforms for Java editor features are supported.
     *
     * @return true if this run supports remote platforms
     */
    public static boolean isRemoteEditorPlatformSupported() {
        return !Impl.REMOTE_PLATFORM_RUNNING;
    }

    /**
     * Returns true if Java editor features should be run on this platform.
     *
     * @return true if the Java editor features should be run on this platform.
     */
    public boolean isEnabled();

    /**
     * Returns the executable that should be run to start the target platform.
     *
     * @return the executable to start the target platform
     */
    public String getJavaCommand();

    /**
     * Registers a listener that will be notified is the above parameters change.
     *
     * @param l the listener
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Unregisters a listener previously registered using {@code addChangeListener}.
     *
     * @param l the listener
     */
    public void removeChangeListener(ChangeListener l);

    /**
     * An optional provider returning the {@code RemotePlatform} for the given file.
     */
    public static interface Provider {
        /**
         * Return the {@code RemotePlatform} for the given file, if any.
         *
         * @param source the file for which the {@code RemotePlatform} should be returned.
         * @return the {@code RemotePlatform} for the given file,
         *         or {@code null} if none should be used.
         */
        public @CheckForNull RemoteEditorPlatform findPlatform(FileObject source);
    }

    class Impl {
        static final boolean REMOTE_PLATFORM_RUNNING = Boolean.getBoolean("remote.editor.platform.running");
    }
}
