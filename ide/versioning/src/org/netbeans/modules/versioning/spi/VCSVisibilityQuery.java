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
package org.netbeans.modules.versioning.spi;


import java.io.File;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;

/**
 * Provides the visibility service according to {@link VisibilityQueryImplementation2}
 * for a particular VersioningSystem
 * 
 * @author Tomas Stupka
 */
public abstract class VCSVisibilityQuery {

    /**
     * Check whether a file is recommended to be visible.
     * @param file a file to considered
     * @return true if it is recommended to display this file
     */
    public abstract boolean isVisible(File file);

    /**
     * Notify a visibility change
     */
    protected final void fireVisibilityChanged() {
        Utils.fireVisibilityChanged();
    }
    
    /**
     * Notify a visibility change
     * 
     * @param files the files with a changed visibility
     * @since 1.37
     */
    protected final void fireVisibilityChanged(File... files) {
        Utils.fireVisibilityChanged(files);
    }
    
}
