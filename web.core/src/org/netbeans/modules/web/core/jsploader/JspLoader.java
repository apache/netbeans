/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        if ((data == null) || !(data instanceof JspDataObject)) {
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
