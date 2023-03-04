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

package org.netbeans.modules.beans.beaninfo;

import java.io.IOException;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Pokorsky
 */
final class BIDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-java"; // NOI18N
    private static final String BEANINFO_SFX = "BeanInfo"; // NOI18N
    private static final long serialVersionUID = 1L;

    public BIDataLoader() {
        super("org.netbeans.modules.beans.beaninfo.BIDataObject"); // NOI18N
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(BIDataLoader.class, "LBL_BeanInfo_loader_name");
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new BIDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions"; // NOI18N
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        return isBeanInfo(fo)? fo: null;
    }
    
    public static boolean isBeanInfo(FileObject fo) {
        String name;
        return fo != null
                && !fo.isFolder()
                && ((name = fo.getName()).length() > BEANINFO_SFX.length() ||
                   (name.length() == BEANINFO_SFX.length() && fo.getAttribute("template") != null)) 
                && name.endsWith(BEANINFO_SFX)
                && "java".equals(fo.getExt()); // NOI18N
    }
    
}
