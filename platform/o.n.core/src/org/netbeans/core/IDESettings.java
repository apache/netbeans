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

package org.netbeans.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.core.startup.CLIOptions;
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

    /** Are we running in GUI or headless mode?
     * 
     * @return true if the GUI mode is on
     */
    public static boolean isGui() {
        return CLIOptions.isGui();
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
