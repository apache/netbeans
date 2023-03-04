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

package org.netbeans.modules.db.metadata.model.test.api;

import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.MetadataModelImplementation;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;

/**
 *
 * @author Andrei Badea
 */
public class MetadataModelTestUtilities {

    private MetadataModelTestUtilities() {}

    public static MetadataModel createSimpleMetadataModel(Metadata metadata) {
        return MetadataAccessor.getDefault().createMetadataModel(new SimpleMetadataModel(metadata));
    }

    private static final class SimpleMetadataModel implements MetadataModelImplementation {

        private final ReentrantLock lock = new ReentrantLock();
        private final Metadata metadata;

        public SimpleMetadataModel(Metadata metadata) {
            this.metadata = metadata;
        }

        public void runReadAction(Action<Metadata> action) throws MetadataModelException {
            lock.lock();
            try {
                action.run(metadata);
            } catch (MetadataException e) {
                throw new MetadataModelException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
