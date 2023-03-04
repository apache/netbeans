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
package org.netbeans.modules.masterfs.watcher;

import java.io.IOException;
import org.netbeans.modules.masterfs.providers.Notifier;

/** Access to protected methods of {@link Notifier}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class NotifierAccessor {
    private static NotifierAccessor DEFAULT;
    static {
        try {
            Class.forName(Notifier.class.getName(), true, Notifier.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }
    protected NotifierAccessor() {
        DEFAULT = this;
    }
    
    public static NotifierAccessor getDefault() {
        assert DEFAULT != null;
        return DEFAULT;
    }
    
    protected abstract <KEY> KEY addWatch(Notifier<KEY> n, String path) throws IOException;
    protected abstract <KEY> void removeWatch(Notifier<KEY> n, KEY key) throws IOException;
    protected abstract String nextEvent(Notifier<?> n) throws IOException, InterruptedException;
    protected abstract void start(Notifier<?> n) throws IOException;    
    protected abstract void stop(Notifier<?> n) throws IOException;    
}
