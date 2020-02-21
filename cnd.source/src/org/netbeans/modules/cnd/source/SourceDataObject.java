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
package org.netbeans.modules.cnd.source;

import java.io.IOException;

import org.netbeans.modules.cnd.spi.CndCookieProvider;
import org.netbeans.modules.cnd.spi.CndCookieProvider.InstanceContentOwner;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *  Abstract superclass of a C/C++/Fortran DataObject.
 */
public abstract class SourceDataObject extends MultiDataObject implements InstanceContentOwner {
    //private static final Logger LOG = Logger.getLogger(SourceDataObject.class.getName());

    /** Serial version number */
    static final long serialVersionUID = -6788084224129713370L;
    private InstanceContent ic;
    private Lookup myLookup;

    public SourceDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    @Override
    public final synchronized InstanceContent getInstanceContent(){
        return ic;
    }

    @Override
    public final synchronized Lookup getLookup() {
        if (myLookup == null) {
            ic = new InstanceContent();
            ic.add(this);
            ic.add(getPrimaryFile());
            ic.add(this, CppEditorSupportProvider.staticFactory);
            ic.add(this, CppEditorSupportProvider.saveAsStaticFactory);
            CndCookieProvider.getDefault().addLookup(this);
            myLookup = new AbstractLookup(ic);
        }
        return myLookup;
    }
    
    @Override
    public final <T extends Cookie> T getCookie(Class<T> type) {
        if (!Cookie.class.isAssignableFrom(type)) {
            //Exception exception = new Exception("Class "+Cookie.class.getName()+" does not AssignableFrom "+type.getName()); //NOI18N
            //LOG.log(Level.INFO, exception.getMessage(), exception);
            return null;
        }
        Object lookupResult = getLookup().lookup(type);
        if (lookupResult != null) {
            if (!type.isInstance(lookupResult)) {
                //Exception exception = new Exception("Class "+lookupResult.getClass().getName()+" is not instance of "+type.getName()); //NOI18N
                //LOG.log(Level.INFO, exception.getMessage(), exception);
                return null;
            }
        }
        @SuppressWarnings("unchecked")
        T res = (T) lookupResult;
        return res;
    }

    @Override
    protected abstract Node createNodeDelegate();

    /**
     *  Creates new object from template. Check to make sure the user
     *  has entered a valid string.
     *
     *  @param df Folder to create the template in
     *  @param name New template name
     *  @exception IOException
     */
    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name)
            throws IOException {

        if ((name != null) && (!isValidName(name))) {
            throw new IOException(NbBundle.getMessage(SourceDataObject.class,
                    "FMT_Not_Valid_FileName", name)); // NOI18N
        }
        return super.handleCreateFromTemplate(df, name);
    }

    /**
     * Is the given name a valid template name for our module?
     * In other words, is it a valid basename for a source/data file
     * created by our templates, or is it even a valid filename we will
     * allow you to rename source files to?
     * <p>
     * Note that Unix allows you to name files anything (except for null
     * characters and the slash character) but we're making a stricter
     * restriction here. We Want To Help You (tm). No blank file names.
     * No control characters in the filename. No meta characters in the
     * filename.   (Possibly controversial: no whitespace in filename)
     *
     * @param name Name to check
     */
    static boolean isValidName(String name) {
        int len = name.length();

        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (Character.isISOControl(c)) {
                return false;
            }
        }
        return true;
    }
}
