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
package org.netbeans.spi.queries;

import javax.swing.event.ChangeEvent;
import org.openide.filesystems.FileObject;

/**
 * ChangeEvent subclass to be used by VisibilityQueryImplementation implementations
 * to fire changes when it's known what files changed visibility. Allows clients to use that information
 * for improved performance when reacting on the event.
 * @author mkleint
 * @since 1.32
 */
public final class VisibilityQueryChangeEvent extends ChangeEvent {
    private final FileObject[] fileObjects;


    /**
     * create new instance of the event, typically called by VisibilityQueryImplementation providers.
     * @param source
     * @param changedFileObjects 
     */
    public VisibilityQueryChangeEvent(Object source, FileObject[] changedFileObjects) {
        super(source);
        fileObjects = changedFileObjects;
    }
    
    /**
     * return the FileObjects that changed visibility
     * @return 
     */
    public FileObject[] getFileObjects() {
        return fileObjects;
    }
}
