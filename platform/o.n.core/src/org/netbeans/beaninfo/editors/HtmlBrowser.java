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

        @Override
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

        @Override
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

        @Override
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
