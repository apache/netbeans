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
package org.netbeans.modules.spring.beans.model.impl;

import org.netbeans.modules.spring.api.beans.model.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
class SpringAnnotatedBeanLocation implements Location {

    private final FileObject fileObject;

    public SpringAnnotatedBeanLocation(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public FileObject getFile() {
        return fileObject;
    }

    @Override
    public int getOffset() {
        return 0;
    }

}
