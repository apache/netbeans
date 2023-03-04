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

package org.openidex.search;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Primitive implementation of {@link VisibilityQuery}.
 *
 * @author  Marian Petras
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class VisibilityQueryImpl implements VisibilityQueryImplementation {

    private static final String INVISIBLE_SUFFIX = "_invisible";

    public boolean isVisible(FileObject file) {
        final String name = file.getName();
        return !name.endsWith(INVISIBLE_SUFFIX);
    }

    public void addChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

    public void removeChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

}
