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

package org.netbeans.modules.java;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader for .class files.
 * @author Jan Pokorsky
 */
public final class ClassDataLoader extends UniFileLoader {

    /** The standard extension for Java class files. */
    public static final String CLASS_EXTENSION = "class"; // NOI18N

    protected ClassDataLoader() {
        super("org.netbeans.modules.java.ClassDataObject"); // NOI18N
        getExtensions().addExtension(CLASS_EXTENSION);
    }
    
    @Override
    protected String actionsContext () {
        return "Loaders/application/x-class-file/Actions/"; // NOI18N
    }
    

    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {

        if (primaryFile.getExt().equals(CLASS_EXTENSION)) {
            return new ClassDataObject(primaryFile, this);
        }
        return null;
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(ClassDataLoader.class,
                "PROP_ClassLoader_Name"); // NOI18N
    }
    
}
