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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates the location of a bean definition, that is, a file and
 * an offset in that file.
 *
 * @author Andrei Badea
 */
public interface Location {

    /**
     * Returns the file corresponding to this location.
     *
     * @return the file; never null.
     */
    FileObject getFile();

    /**
     * Returns the offset corresponding to this location.
     *
     * @return the offset or -1 if the offset is not known.
     */
    int getOffset();
}
