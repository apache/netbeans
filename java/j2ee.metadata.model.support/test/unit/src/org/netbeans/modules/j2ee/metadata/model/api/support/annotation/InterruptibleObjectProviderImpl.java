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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.Collections;
import java.util.List;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Andrei Badea
 */
public class InterruptibleObjectProviderImpl implements ObjectProvider<PersistentObject> {

    private final boolean interruptible;

    public InterruptibleObjectProviderImpl(boolean interruptible) {
        super();
        this.interruptible = interruptible;
    }

    public List<PersistentObject> createInitialObjects() throws InterruptedException {
        if (interruptible) {
            throw new InterruptedException();
        } else {
            return Collections.emptyList();
        }
    }

    public List<PersistentObject> createObjects(TypeElement type) {
        throw new UnsupportedOperationException();
    }

    public boolean modifyObjects(TypeElement type, List<PersistentObject> objects) {
        throw new UnsupportedOperationException();
    }
}
