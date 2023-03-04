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

package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** Recognizes single files in the Repository as being of a certain type.
 *
 * @author nityad
 */
public class SunResourceDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 1L;
    public static final String MIME_TYPE = "text/x-sun-resource+xml";

    public SunResourceDataLoader() {
        this("org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject"); //NOI18N
    }

    // Can be useful for subclasses:
    protected SunResourceDataLoader(String recognizedObjectClass) {
        super(recognizedObjectClass);
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(SunResourceDataLoader.class, "LBL_loaderName"); //NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }

    @Override
    protected String actionsContext () {
        return "Loaders/" + MIME_TYPE + "/Actions"; //NOI18N

    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        if (null == primaryFile) {
            // I hope soembody up the stack will do something smart with this exception
            // since they needed to be prepared for it.  If there were no declared exception
            // on this, I would have thrown an IllegalArgumentException
            throw new IOException("primaryFile can not be NULL");
        }
        return new SunResourceDataObject(primaryFile, this);
    }

}
