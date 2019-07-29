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

package org.netbeans.modules.payara.eecommon.dd.loader;

import java.io.IOException;
import org.netbeans.modules.j2ee.sun.ddloaders.DDType;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataLoader;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_APP_CLI_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_APP_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_EJB_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDDType.PAYARA_WEB_MIME_TYPE;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDescriptorDataObject.DD_ACTION_PATH;
import static org.netbeans.modules.payara.eecommon.dd.loader.PayaraDescriptorDataObject.DD_MIME_TYPE;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import static org.openide.util.NbBundle.getMessage;

/** 
 * Recognizes deployment descriptors of Payara descriptor files.
 *
 * @author Peter Williams
 * @author Gaurav Gupta
 */
@DataObject.Registration(
        displayName = "Bundle#PayaraResolver",
        mimeType = DD_MIME_TYPE,
        position = 1500
)
public class PayaraDescriptorDataLoader extends SunDescriptorDataLoader {

    private static final String[] SUPPORTED_MIME_TYPES = {
        PAYARA_WEB_MIME_TYPE,
        PAYARA_EJB_MIME_TYPE,
        PAYARA_APP_MIME_TYPE,
        PAYARA_APP_CLI_MIME_TYPE
    };

    public PayaraDescriptorDataLoader() {
        this(PayaraDescriptorDataObject.class.getName());
    }

    public PayaraDescriptorDataLoader(String name) {
        super(name);
    }

    @Override
    protected String defaultDisplayName() {
        return getMessage(PayaraDescriptorDataLoader.class, "PayaraResolver"); // NOI18N
    }

    @Override
    protected String actionsContext() {
        return DD_ACTION_PATH;
    }

    @Override
    protected void initialize() {
        super.initialize();
        for (int i = 0; i < SUPPORTED_MIME_TYPES.length; i++) {
            getExtensions().addMimeType(SUPPORTED_MIME_TYPES[i]);
        }
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject result = null;
        if (!fo.isFolder() && (PayaraDDType.getDDType(fo.getNameExt()) != null
                || DDType.getDDType(fo.getNameExt()) != null)) {
            result = fo;
        }
        return result;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new PayaraDescriptorDataObject(primaryFile, this);
    }

}
