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

package org.netbeans.modules.remote.impl.fs.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public final class FSSRequest {
    
    private static final AtomicInteger nextId = new AtomicInteger(0);
    
    private final int id;
    private final FSSRequestKind kind;
    private final String path;
    private final String path2;

    public FSSRequest(FSSRequestKind kind, String path) {
        this(kind, path, null);
    }

    public FSSRequest(FSSRequestKind kind, String path, String path2) {
        this.id = nextId.incrementAndGet();
        this.kind = kind;
        this.path = path;
        this.path2 = path2;
    }
    
    /*package*/ FSSRequest(FSSRequestKind kind, String path, boolean zeroId) {
        this.id = zeroId ? 0 : nextId.incrementAndGet();
        this.kind = kind;
        this.path = path;
        this.path2 = null;
    }

    public int getId() {
        return id;
    }

    public FSSRequestKind getKind() {
        return kind;
    }

    public String getPath() {
        return path;
    }

    public String getPath2() {
        return path2;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + kind + ' ' + id + ' ' + path + ' ' + ((path2 == null) ? "" : path2); //NOI18N
    }

    /*package*/ boolean needsResponse() {
        return id > 0;
    }
}
