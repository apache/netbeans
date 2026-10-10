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
package org.netbeans.modules.java.j2seembedded.platform;

import java.io.IOException;
import java.util.stream.Stream;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AntLogger.class, position = 55)
public final class UpdateLogger extends AntLogger {

    private boolean updated = false;

    @Override
    public boolean interestedInSession(AntSession session) {

        synchronized(this) {
            if (!updated) {
                ProjectManager.mutex().postWriteRequest(() -> {

                    Stream.of(JavaPlatformManager.getDefault().getInstalledPlatforms())
                        .filter((p) -> p instanceof RemotePlatform)
                        .forEach((p) -> {
                            try {
                                RemotePlatformProvider.updateBuildProperties((RemotePlatform) p);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });

                });
                updated = true;
            }
        }

        return super.interestedInSession(session);
    }

}
