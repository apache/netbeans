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

package org.netbeans.modules.javaee.beanvalidation.api;

import java.util.HashMap;
import org.openide.filesystems.FileObject;

/**
 *
 * @author alexey butenko
 */
public interface BeanValidationConfig {
    
    String getName();

    HashMap<FileObject, ConstraintMapping> getConstraintMappings();

    /**
     * Add Constraing Mapping to the validation.xml
     * @param fileObject - file to add
     */
    void addConstraintMapping(FileObject fileObject);
    
    /**
     * Remove Constraint mapping from the validation.xml
     * @param fileObject - file to remove
     */
    void removeConstraintMapping(FileObject fileObject);

    interface ConstraintMapping {
        FileObject getFileObject();
        int getStartOffset();
        int getEndOffset();
    }
}
