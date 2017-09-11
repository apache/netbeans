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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.netbeans.core.UIExceptions;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Defines editor for choosing of Web browser.
 *
 * @author  Radim Kubacki
 */
public class HtmlBrowser extends Object {

    public static class FactoryEditor extends PropertyEditorSupport {
        
        /** extended attribute that signals that this object should not be visible to the user */
        private static final String EA_HIDDEN = "hidden"; // NOI18N

        private static final String BROWSER_FOLDER = "Services/Browsers"; // NOI18N
        
        /** Creates new FactoryEditor */
        public FactoryEditor () {
        }
        
        public String getAsText () {
            try {
                org.openide.awt.HtmlBrowser.Factory f = (org.openide.awt.HtmlBrowser.Factory)getValue ();
                
                Lookup.Item<org.openide.awt.HtmlBrowser.Factory> i = Lookup.getDefault().lookupItem(
                    new Lookup.Template<org.openide.awt.HtmlBrowser.Factory> (org.openide.awt.HtmlBrowser.Factory.class, null, f)
                );
                if (i != null)
                    return i.getDisplayName();
            }
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return NbBundle.getMessage (FactoryEditor.class, "CTL_UnspecifiedBrowser"); //NOI18N
        }
        
        public void setAsText (java.lang.String str) throws java.lang.IllegalArgumentException {
            try {
                if (NbBundle.getMessage (FactoryEditor.class, "CTL_UnspecifiedBrowser").equals (str) //NOI18N
                ||  str == null) {
                    setValue (null);
                    return;
                }
                Lookup.Result<org.openide.awt.HtmlBrowser.Factory> r = Lookup.getDefault().lookupResult(org.openide.awt.HtmlBrowser.Factory.class);
		for (Lookup.Item<org.openide.awt.HtmlBrowser.Factory> i: r.allItems()) {
                    if (str.equals(i.getDisplayName())) {
                        setValue (i.getInstance());
                        return;
                    }
                }
            }
            catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = e.getLocalizedMessage();
            if (msg == null) {
                msg = MessageFormat.format(
                    NbBundle.getMessage(
                    HtmlBrowser.class, "FMT_EXC_GENERIC_BAD_VALUE"), //NOI18N
                    new Object[] {str}); 
            }
            UIExceptions.annotateUser(iae, str, msg, e, new java.util.Date());
            throw iae;
            }
        }
        
        public java.lang.String[] getTags () {
            ArrayList<String> list = new ArrayList<String> (6);
            Lookup.Result<org.openide.awt.HtmlBrowser.Factory> r = Lookup.getDefault().lookupResult(org.openide.awt.HtmlBrowser.Factory.class);
            for (Lookup.Item<org.openide.awt.HtmlBrowser.Factory> i: r.allItems()) {
                list.add(i.getDisplayName());
            }
            
            // PENDING need to get rid of this filtering
            FileObject fo = FileUtil.getConfigFile (BROWSER_FOLDER);
            if (fo != null) {
                DataFolder folder = DataFolder.findFolder (fo);
                DataObject [] dobjs = folder.getChildren ();
                for (int i = 0; i<dobjs.length; i++) {
                    // Must not be hidden and have to provide instances (we assume instance is HtmlBrowser.Factory)
                    if (Boolean.TRUE.equals(dobjs[i].getPrimaryFile().getAttribute(EA_HIDDEN)) ||
                            dobjs[i].getCookie(InstanceCookie.class) == null) {
                        FileObject fo2 = dobjs[i].getPrimaryFile();
                        String n = fo2.getName();
                        try {
                            n = fo2.getFileSystem().getDecorator().annotateName(n, dobjs[i].files());
                        } catch (FileStateInvalidException e) {
                            // Never mind.
                        }
                        list.remove(n);
                    }
                }
            }
            String[] retValue = new String[list.size ()];
            
            list.toArray (retValue);
            return retValue;
        }
        
    }
                
}
