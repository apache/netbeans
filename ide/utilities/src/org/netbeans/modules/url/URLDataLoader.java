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

package org.netbeans.modules.url;

import java.io.IOException;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader which recognizes URL files.
 *
 * @author Ian Formanek
 */
public class URLDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-7407252842873642582L;
    /** MIME-type of URL files */
    private static final String URL_MIME_TYPE = "text/url";             //NOI18N
    /** */
    private static final String PROP_ENCODING_QUERY_IMPL
                                = "org.netbeans.modules.url.encoding";  //NOI18N
    
    
    /** Creates a new URLDataLoader without the extension. */
    public URLDataLoader() {
        super("org.netbeans.modules.url.URLDataObject");                //NOI18N
    }

    /**
     * Returns an instance of {@code FileEncodingQueryImplementation}
     * representing encoding to be used by {@code URLDataObject}s.
     * 
     * @return  an instance of {@code FileEncodingQueryImplementation},
     *          or {@code null} if encoding UTF-8 is not supported
     */
    FileEncodingQueryImplementation getEncoding() {
        return (FileEncodingQueryImplementation)
               getProperty(PROP_ENCODING_QUERY_IMPL);
    }
    
    /**
     * Initializes this loader. This method is called only once the first time
     * this loader is used (not for each instance).
     */
    @Override
    protected void initialize () {
        super.initialize();

        ExtensionList ext = new ExtensionList();
        ext.addMimeType(URL_MIME_TYPE);
        ext.addMimeType("text/x-url");                                  //NOI18N
        setExtensions(ext);

        try {
            putProperty(PROP_ENCODING_QUERY_IMPL, new EncodingQueryImpl());
        } catch (IllegalArgumentException ex) {
            assert false;   //this should not happen
            /* UTF-8 is not supported - use the project's default encoding */
        }
    }

    /** */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(URLDataLoader.class,
                                   "PROP_URLLoader_Name");              //NOI18N
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    @Override
    protected String actionsContext () {
        return "Loaders/text/url/Actions/";                             //NOI18N
    }
    
    /**
     * @return  <code>URLDataObject</code> for the specified file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new URLDataObject(primaryFile, this);
    }

}
