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
package org.netbeans.api.java.source;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SourceLevelQueryImplementation2.class)
public class MockSourceLevelQuery implements SourceLevelQueryImplementation2 {
    private static final Map<FileObject, String> LEVELS = new WeakHashMap<>();

    static void register(FileObject fo, String level) {
        synchronized ((LEVELS)) {
            LEVELS.put(fo, level);
        }
    }

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        synchronized (LEVELS) {
            String level = LEVELS.get(javaFile);
            if (level != null) {
                return new Result2() {
                    @Override
                    public SourceLevelQuery.Profile getProfile() {
                        return SourceLevelQuery.Profile.DEFAULT;
                    }

                    @Override
                    public String getSourceLevel() {
                        return level;
                    }

                    @Override
                    public void addChangeListener(ChangeListener listener) {
                    }

                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                    }
                };
            } else {
                return null;
            }
        }
    }

}
