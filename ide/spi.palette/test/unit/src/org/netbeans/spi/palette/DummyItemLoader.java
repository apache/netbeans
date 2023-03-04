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

package org.netbeans.spi.palette;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;

/**
 *
 * @author sa154850
 */
class DummyItemLoader extends UniFileLoader {

    static final String ITEM_EXT = "junit_palette_item"; // NOI18N

    DummyItemLoader() {
        super("org.netbeans.spi.palette.DummyItemDataObject"); // NOI18N

        ExtensionList ext = new ExtensionList();
        ext.addExtension(ITEM_EXT);
        setExtensions(ext);
    }


    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException, IOException
    {
        return new DummyItemDataObject( primaryFile, this );
    }
    
}
