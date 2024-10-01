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

package org.netbeans.modules.extexecution.process;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mkleint
 */
@ServiceProvider(service=ProcessesImplementation.class, position = 1000)
public class ProcessesImpl implements ProcessesImplementation {

    private static final Logger LOGGER = Logger.getLogger(ProcessesImpl.class.getName());

    @Override
    public void killTree(Process process, Map<String, String> environment) {
        ProcessHandle handle = process.toHandle();
        try (Stream<ProcessHandle> tree = handle.descendants()) {
            destroy("proc", handle);
            tree.forEach(ch -> destroy("child", ch));
        }
    }

    private static void destroy(String kind, ProcessHandle handle) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "destroying {0}: {1}; info: {2}", new Object[]{kind, handle, handle.info()});
        }
        handle.destroy();
    }

}
