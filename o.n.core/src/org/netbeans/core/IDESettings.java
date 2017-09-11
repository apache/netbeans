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

package org.netbeans.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/** Stores and loads web browser settings. For visual representation
 * see core.ui/src/org/netbeans/core/ui/options/general.
 */
public class IDESettings  {
    /** Web Browser prefered by user */
    public static final String PROP_WWWBROWSER = "WWWBrowser"; // NOI18N
    public static final String PROP_EXTERNAL_WWWBROWSER = "ExternalWWWBrowser"; // NOI18N

    static Preferences getPreferences() {
        return NbPreferences.forModule(IDESettings.class);
    }

    /** Getter for preffered web browser. The browser may be an internal one
     * (URLs will be rendered in some TopComponent) or external browser like
     * Mozilla or Internet Explorer.
     *
     * First time when this function is called Lookup is used
     * to find prefered browser factory in a browser registry.
     *
     * @return prefered browser,
     * may return null if it is not possible to get the browser
     */
    public static HtmlBrowser.Factory getWWWBrowser () {
        return getBrowser( PROP_WWWBROWSER, false );
    }

    /** Getter for preffered external web browser.
     *
     * First time when this function is called Lookup is used
     * to find prefered browser factory in a browser registry.
     *
     * @return prefered external browser,
     * may return null if it is not possible to get the browser or no external
     * browser is available.
     */
    public static HtmlBrowser.Factory getExternalWWWBrowser () {
        return getBrowser( PROP_EXTERNAL_WWWBROWSER, true );
    }

    /** Setter for preffered browser.
     *
     *  Actually Node.Handle of node that represent browser in lookup folder is stored.
     *
     * @param brow prefered browser capable of providing implementation
     */
    public static void setWWWBrowser (HtmlBrowser.Factory brow) {
        setBrowser( PROP_WWWBROWSER, brow );
        if( isExternal( brow ) ) {
            setExternalWWWBrowser(brow);
        }
    }

    /** Setter for preffered external browser.
     *
     *  Actually Node.Handle of node that represent browser in lookup folder is stored.
     *
     * @param brow prefered browser capable of providing implementation
     */
    public static void setExternalWWWBrowser (HtmlBrowser.Factory brow) {
        setBrowser( PROP_EXTERNAL_WWWBROWSER, brow );
    }

    private static void setBrowser (String prefId, HtmlBrowser.Factory brow) {
        try {
            if (brow == null) {
                getPreferences().put(prefId, "");//NOI18N
                return;
            }

            Lookup.Item<HtmlBrowser.Factory> item =
                    Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, null, brow));
            if (item != null) {
                getPreferences().put(prefId, item.getId ());
            } else {
                // strange
                Logger.getLogger (IDESettings.class.getName ()).warning ("IDESettings: Cannot find browser in lookup");// NOI18N
                getPreferences().put(prefId, "");//NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace (ex);
        }
    }

    private static HtmlBrowser.Factory getBrowser( String prefId, boolean mustBeExternal ) {
        try {
            Object obj = getPreferences().get(prefId, null);

            if (obj instanceof String && !"".equals (obj)) {
                // use new style
                Lookup.Item<HtmlBrowser.Factory> item = Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, (String)obj, null));
                return item == null ? null : item.getInstance ();
            }

            // the browser is not set yet - find the first one
            if (obj == null || "".equals (obj)) {
                Lookup.Result<HtmlBrowser.Factory> res = Lookup.getDefault ().lookupResult (HtmlBrowser.Factory.class);
                java.util.Iterator<? extends HtmlBrowser.Factory> it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    HtmlBrowser.Factory brow = it.next ();

                    // check if it is not set to be hidden
                    FileObject fo = FileUtil.getConfigFile ("Services/Browsers");   // NOI18N

                    DataFolder folder = DataFolder.findFolder (fo);
                    DataObject [] dobjs = folder.getChildren ();
                    for (int i = 0; i < dobjs.length; i++) {
                        Object o = null;

                        try {
                            if (Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("hidden")))
                                continue;
                            if (mustBeExternal && Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("internal")) )
                                continue;
                            InstanceCookie cookie = (InstanceCookie) dobjs[i].getCookie (InstanceCookie.class);

                            if (cookie == null)
                                continue;
                            o = cookie.instanceCreate ();
                            if (o != null && o.equals (brow)) {
                                return brow;
                            }
                        }
                        // exceptions are thrown if module is uninstalled
                        catch (java.io.IOException ex) {
                            Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
                        }
                    }

                }
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace (ex);
        }
        return null;
    }

    private static boolean isExternal( HtmlBrowser.Factory brow ) {
        // check if it is not set to be hidden
        FileObject fo = FileUtil.getConfigFile ("Services/Browsers");   // NOI18N

        DataFolder folder = DataFolder.findFolder (fo);
        DataObject [] dobjs = folder.getChildren ();
        for (int i = 0; i < dobjs.length; i++) {
            Object o = null;

            try {
                if (Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("hidden")))
                    continue;
                if (!Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute ("internal")) )
                    continue;
                InstanceCookie cookie = (InstanceCookie) dobjs[i].getCookie (InstanceCookie.class);

                if (cookie == null)
                    continue;
                o = cookie.instanceCreate ();
                if (o != null && o.equals (brow)) {
                    return false;
                }
            }
            // exceptions are thrown if module is uninstalled
            catch (java.io.IOException ex) {
                Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger (IDESettings.class.getName ()).log (Level.WARNING, null, ex);
            }
        }
        return false;
    }

    /** Used in layer xml to register FactoryEditor. */
    private static org.netbeans.beaninfo.editors.HtmlBrowser.FactoryEditor createHtmlBrowserFactoryEditor()  {
        return new org.netbeans.beaninfo.editors.HtmlBrowser.FactoryEditor(){
            public void setValue(Object value) {
                setWWWBrowser((HtmlBrowser.Factory)value);
            }

            public Object getValue() {
                return getWWWBrowser();
            }
        };
    }
}
