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

package org.netbeans.modules.xml.schema.model.resolver;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nikita Krjukov
 */
public class FileObjectModelAccessProvider implements ModelAccessProvider {

    private static FileObjectModelAccessProvider singleton =
            new FileObjectModelAccessProvider();

    public static FileObjectModelAccessProvider getDefault() {
        return singleton;
    }

    public Object getModelSourceKey(ModelSource source) {
        return source.getLookup().lookup(FileObject.class);
    }

}
