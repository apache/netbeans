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
package org.netbeans.modules.openide.loaders;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileEncodingQueryImplementation.class, position=100)
public class DataObjectEncodingQueryImplementation extends FileEncodingQueryImplementation {
    private static ThreadLocal<DataFolder> TARGET = new ThreadLocal<DataFolder>();
    private static final Logger LOG = Logger.getLogger(DataObjectEncodingQueryImplementation.class.getName());
    // whether FEQI found in DataObject lookup for MIME type
    private static final Map<String, Boolean> MIME_TYPE_CHECK_MAP = new HashMap<String, Boolean>();
    
    /** Creates a new instance of DataObjectEncodingQueryImplementation */
    public DataObjectEncodingQueryImplementation() {
    }
    
    public static DataFolder enterIgnoreTargetFolder(DataFolder df) {
        DataFolder prev = TARGET.get();
        TARGET.set(df);
        return prev;
    }
    public static void exitIgnoreTargetFolder(DataFolder prev) {
        TARGET.set(prev);
    }

    /**
     * Gets encoding for given FileObject. According to #155380 algorith is as follows:
     * - Looking for FEQ implementation in the MimeLookup. If found use this FEQ.
     * - Check the map<MimeType,Boolean>.
     * - If map contains FALSE value for the given MimeType then do not look for FEQ in the DataObject lookup.
     * - If map contains TRUE value for the given MimeType then get FEQ from the DataObject lookup.
     * - If map not contains given MimeType then add it to the map with value TRUE if FEQ is found in the DataObject lookup
     */
    @Override
    public Charset getEncoding(FileObject file) {
        assert file != null;
        DataFolder df = TARGET.get();
        String mimeType = file.getMIMEType();
        FileEncodingQueryImplementation impl = MimeLookup.getLookup(mimeType).lookup(FileEncodingQueryImplementation.class);
        if (impl != null) {
            Charset charset = impl.getEncoding(file);
            if (charset != null) {
                return charset;
            }
        }
        if (df != null && df.getPrimaryFile().equals(file.getParent())) {
            // do not create new data objects
            return null;
        }
        Boolean useDataObjectLookup = MIME_TYPE_CHECK_MAP.get(mimeType);
        if (useDataObjectLookup == null || useDataObjectLookup.booleanValue() || "content/unknown".equals(mimeType)) {  //NOI18N
            DataObject dobj;
            try {
                dobj = DataObject.find(file);
            } catch (DataObjectNotFoundException ex) {
                LOG.warning("Invalid DataObject: " + FileUtil.getFileDisplayName(file));
                return null;
            }
            impl = dobj.getLookup().lookup(FileEncodingQueryImplementation.class);
            if (impl != null) {
                MIME_TYPE_CHECK_MAP.put(mimeType, Boolean.TRUE);
                return impl.getEncoding(file);
            } else {
                MIME_TYPE_CHECK_MAP.put(mimeType, Boolean.FALSE);
            }
        }
        return null;
    }
}
