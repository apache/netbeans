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
package org.netbeans.modules.cnd.source;

import java.io.IOException;

import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;

/**
 *
 */
public class FortranDataLoader extends SourceAbstractDataLoader {

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975686L;

    public FortranDataLoader() {
        super("org.netbeans.modules.cnd.source.FortranDataObject"); // NOI18N
    }

    @Override
    protected String getMimeType() {
        return MIMENames.FORTRAN_MIME_TYPE;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new FortranDataObject(primaryFile, this);
    }
}
