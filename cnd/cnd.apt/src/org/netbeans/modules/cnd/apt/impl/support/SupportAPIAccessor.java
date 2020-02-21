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

package org.netbeans.modules.cnd.apt.impl.support;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;

/**
 *
 */
public abstract class SupportAPIAccessor {
    public static final Charset INTERNAL_CHARSET;
    static {
        Charset preferred = null;
        try {
            preferred = Charset.forName("UTF-8"); //NOI18N
        } catch (UnsupportedCharsetException e) {
            preferred = Charset.defaultCharset();
        }
        INTERNAL_CHARSET = preferred;
    }
    private static SupportAPIAccessor INSTANCE;

    public static SupportAPIAccessor get() {
        SupportAPIAccessor out = INSTANCE;
        if (out == null) {
            synchronized (SupportAPIAccessor.class) {
                if (INSTANCE == null) {
                    Class<?> c = IncludeDirEntry.class;
                    try {
                        Class.forName(c.getName(), true, c.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
                out = INSTANCE;
            }
        }

        assert INSTANCE != null : "There is no API package accessor available!"; //NOI18N
        return out;
    }

    /**
     * Register the accessor. The method can only be called once
     * - otherwise it throws IllegalStateException.
     *
     * @param accessor instance.
     */
    public static void register(SupportAPIAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }

    public abstract void invalidateFileBasedCache(String file);
    public abstract void invalidateCache();
    public abstract boolean isExistingDirectory(IncludeDirEntry entry);
}
