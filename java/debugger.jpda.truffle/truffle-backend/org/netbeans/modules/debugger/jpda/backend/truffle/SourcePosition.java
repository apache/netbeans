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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.source.Source;
import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Martin
 */
final class SourcePosition {

    private static final Map<Source, Long> sourceId = new WeakHashMap<>();
    private static long nextId = 0;

    final long id;
    final String name;
    final String path;
    final int line;
    final String code;
    final URI uri;

    public SourcePosition(Source source, String name, String path, int line, String code) {
        this.id = getId(source);
        this.name = name;
        this.path = path;
        this.line = line;
        this.code = code;
        this.uri = source.getURI();
    }

    private static synchronized long getId(Source s) {
        Long id = sourceId.get(s);
        if (id == null) {
            id = new Long(nextId++);
            sourceId.put(s, id);
        }
        return id;
    }

}
