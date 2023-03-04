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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
* Loader for JSPs.
*
* @author Petr Jiricka
*/
public class JspLoader extends UniFileLoader {

    /** serialVersionUID */
    private static final long serialVersionUID = 1549250022027438942L;

    /** Extension for JSP files */
    public static final String JSP_EXTENSION = "jsp"; // NOI18N
    /** Recommended extension for JSP fragments */
    public static final String JSPF_EXTENSION = "jspf"; // NOI18N
    /** Recommended extension for JSP fragments */
    public static final String JSF_EXTENSION = "jsf"; // NOI18N
    
    /** Recommended extension for JSP pages in XML syntax */
    public static final String JSPX_EXTENSION = "jspx"; // NOI18N
    /** Extension for tag files */
    public static final String TAG_FILE_EXTENSION = "tag"; // NOI18N
    /** Recommended extension for tag file fragments */
    public static final String TAGF_FILE_EXTENSION = "tagf"; // NOI18N
    /** Recommended extension for tag files in XML syntax */
    public static final String TAGX_FILE_EXTENSION = "tagx"; // NOI18N
    
    public static final String JSP_MIME_TYPE  = "text/x-jsp"; // NOI18N

    public static final String TAG_MIME_TYPE  = "text/x-tag"; // NOI18N
    
    public static String getMimeType(JspDataObject data) {
        if (!(data instanceof JspDataObject)) {
            return "";          // NOI18N
        }
        String ext = data.getPrimaryFile().getExt();
        if (ext.equals(TAG_FILE_EXTENSION) || ext.equals(TAGF_FILE_EXTENSION)
            || ext.equals(TAGX_FILE_EXTENSION)) {
            return TAG_MIME_TYPE;
        } else {
            return JSP_MIME_TYPE;
        }
    }
    
    @Override
    protected void initialize () {
        super.initialize();
        getExtensions().addMimeType(JSP_MIME_TYPE);
        getExtensions().addMimeType(TAG_MIME_TYPE);
    }

    /** Get the default display name of this loader.
     * @return default display name
     */
    protected String defaultDisplayName () {
        return NbBundle.getBundle(JspLoader.class).getString("PROP_JspLoader_Name");
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-jsp/Actions/"; // NOI18N
    }
    
    public JspLoader() {
        super ("org.netbeans.modules.web.core.jsploader.JspDataObject"); // NOI18N
    }

    /** For subclasses. */
    protected JspLoader(String str) {
        super (str);
    }
    
    protected JspDataObject createJspObject(FileObject pf, final UniFileLoader l) 
        throws DataObjectExistsException {
        return new JspDataObject (pf, l);
    }


    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        JspDataObject obj = createJspObject(primaryFile, this);
        // [PENDING] add these from JspDataObject, not from the loader
        obj.getCookieSet0 ().add (new TagLibParseSupport(primaryFile));
        return obj;
    }

}
