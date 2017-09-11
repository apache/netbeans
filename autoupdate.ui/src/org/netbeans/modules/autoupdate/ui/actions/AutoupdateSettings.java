/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static String tempIdeIdentity = null;
    private static final Logger err = Logger.getLogger (AutoupdateSettings.class.getName ());
    private static final String PROP_IDE_IDENTITY = "ideIdentity"; // NOI18N
    private static final String PROP_SUPER_IDENTITY = "superId"; // NOI18N
    private static final String PROP_QUALIFIED_IDENTITY = "qualifiedId"; // NOI18N
    private static final String PROP_PERIOD = "period"; // NOI18N
    private static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N
    private static final String DEFAULT_NETBEANS_DIR = ".netbeans"; // NOI18N
    private static final String SUPER_IDENTITY_FILE_NAME = ".superId"; // NOI18N
    private static final char IDE_ID_DELIMETER = '0'; // NOI18N
    private static final char QUALIFIED_ID_DELIMETER = '_'; // NOI18N
    
    public static final int EVERY_STARTUP = 0;
    public static final int EVERY_DAY = 1;
    public static final int EVERY_WEEK = 2;
    public static final int EVERY_2WEEKS = 3;
    public static final int EVERY_MONTH = 4;
    public static final int NEVER = 5;
    public static final int CUSTOM_CHECK_INTERVAL = 6;
    private static final String EXPIRATION_RECORD = "expiration"; // NOI18N
    private static final String IMPORTED = "imported"; // NOI18N
    private static final String [] VERSIONS_FOR_IMPORT = new String[0];
    
    private static final String [][] KNOWN = {
        {"EVERY_STARTUP", "0"},
        {"EVERY_DAY", "1"},
        {"EVERY_WEEK", "2"},
        {"EVERY_2WEEKS", "3"},
        {"EVERY_MONTH", "4"},
        {"NEVER", "5"},
    };
    
    private static int checkInterval = 0;
    private static String superId;

    private AutoupdateSettings () {
    }
    
    public static void generateIdentity () {
        expirationCheck ();
        if (tempIdeIdentity instanceof String) {
            return;
        }
        Object oldIdeIdentity = getPreferences ().get (PROP_IDE_IDENTITY, null);
        String newIdeIdentity = null;
        if (oldIdeIdentity == null) {
            newIdeIdentity = modifyIdeIdentityIfNeeded (IDE_ID_DELIMETER + generateNewId ());
        } else {
            newIdeIdentity = modifyIdeIdentityIfNeeded ((String) oldIdeIdentity);
        }
        tempIdeIdentity = newIdeIdentity;
        if (! newIdeIdentity.equals (oldIdeIdentity) || ! existsSuperIdentity () || getPreferences ().get (PROP_QUALIFIED_IDENTITY, null) == null) {
            err.log (Level.FINE, "Put new value of PROP_IDE_IDENTITY to " + newIdeIdentity);
            
            //catching strange IDs like
            //unique=-n+NB0c15fdc4f-2182-40c3-b6d8-ae09ef28922a_526df012-fe24-4849-b343-b4d77b11f6e6
            assert !newIdeIdentity.startsWith("-n+") : "Generated identity (" + newIdeIdentity + ") is of wrong format. " +
                    "Old identity was [" + oldIdeIdentity + "].";

            getPreferences ().put (PROP_IDE_IDENTITY, newIdeIdentity);
            String sid = getSuperIdentity ();
            if(sid!=null) {
                getPreferences ().put (PROP_SUPER_IDENTITY, sid);
            }
            getPreferences ().put (PROP_QUALIFIED_IDENTITY, getQualifiedIdentity (newIdeIdentity));
        }
        return;
    }
    
    public static int getPeriod () {
        boolean stillDefault = getPreferences ().get (PROP_PERIOD, null) == null;
        Integer defaultCheckInterval = null;
        if (stillDefault) {
            defaultCheckInterval = parse (Utilities.getCustomCheckIntervalInMinutes ());
        }
        if (defaultCheckInterval == null) {
            defaultCheckInterval = EVERY_WEEK;
        }
        err.log (Level.FINEST, "getPeriod () returns " + getPreferences ().getInt (PROP_PERIOD, defaultCheckInterval));
        return getPreferences ().getInt (PROP_PERIOD, defaultCheckInterval);
    }

    public static int getCheckInterval () {
        err.log (Level.FINEST, "getCheckInterval () returns " + checkInterval + "ms");
        return checkInterval;
    }
    
    public static void setPeriod (int period) {
        err.log (Level.FINEST, "Called setPeriod (" + period +")");
        getPreferences ().putInt (PROP_PERIOD, period);
    }
    
    public static Date getLastCheck() {        
        long t = getPreferences ().getLong (PROP_LAST_CHECK, -1);
        return (t > 0) ? new Date (t) : null;

    }

    public static void setLastCheck (Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (PROP_LAST_CHECK);
        }
    }
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    // helper methods
    private static String modifyIdeIdentityIfNeeded (String oldIdeIdentity) {
        int idx = oldIdeIdentity.indexOf (IDE_ID_DELIMETER);
        String [] ideIdentityArr = oldIdeIdentity.split ("\\d"); // NOI18N
        String id = null;
        String oldPrefix = null;
        
        // easy way -> no need to modify
        if (ideIdentityArr.length == 0 || idx == 0) {
            id = oldIdeIdentity;
            oldPrefix = "";
        // a way for UUID    
        } else if (idx != -1 && oldIdeIdentity.substring (ideIdentityArr [0].length ()).startsWith (new StringBuffer (IDE_ID_DELIMETER).toString ())) {
            oldPrefix = oldIdeIdentity.substring (0, idx);
            id = oldIdeIdentity.substring (oldPrefix.length ());
        // old way for stored IDs Random.nextInt()
        } else {
            oldPrefix = ideIdentityArr [0];
            id = oldIdeIdentity.substring (oldPrefix.length ());
        }
        err.log (Level.FINER, "Old IDE Identity Prefix: " + oldPrefix); // NOI18N
        err.log (Level.FINER, "Old IDE Identity ID: " + id); // NOI18N
        String newPrefix = "";
        try {
            FileObject fo = FileUtil.getConfigFile("productid"); // NOI18N
            if (fo != null) {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader (is));
                    newPrefix = r.readLine().trim();
                    // don't exceed 128 chars for prefix
                    if (newPrefix.length () > 128) {
                        newPrefix = newPrefix.substring (0, 128);
                    }                    
                } finally {
                    is.close();
                }
            }
        } catch (IOException ignore) {
            err.log (Level.FINER, ignore.getMessage(), ignore);
        }
        if (!newPrefix.equals (oldPrefix)) {
            err.log (Level.FINER, "New IDE Identity Prefix: " + newPrefix); // NOI18N
        } else {
            err.log (Level.FINER, "No new prefix."); // NOI18N
        }
        //catching strange IDs like
        //unique=-n+NB0c15fdc4f-2182-40c3-b6d8-ae09ef28922a_526df012-fe24-4849-b343-b4d77b11f6e6
        assert !(newPrefix + id).startsWith("-n+") : 
            "Product Indentity is of wrong format, prefix=" +
                newPrefix + ", id=" +
                id + ", oldPrefix=" +
                oldPrefix +
                ",oldIdeIdentity=" + oldIdeIdentity;
        
        return newPrefix + id;
    }

    private static String generateNewId () {
        return UUID.randomUUID ().toString ();
    }
    
    private static Integer parse (String s) {
        if (s == null || s.trim ().length () == 0) {
            return null;
        }
        Integer period = null;
        for (String [] pair: KNOWN) {
            if (pair [0].equalsIgnoreCase (s)) {
                try {
                    period = Integer.parseInt (pair[1]);
                } catch (NumberFormatException nfe) {
                    assert false : "Invalid value " + pair + " throws " + nfe;
                }
            }
        }
        if (period == null) {
            try {
                checkInterval = Long.parseLong (s) * 1000 * 60 > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.parseInt (s) * 1000 * 60;
                period = CUSTOM_CHECK_INTERVAL;
                err.log (Level.FINE, "Custom value of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " is " + s + " minutes.");
            } catch (NumberFormatException nfe) {
                err.log (Level.FINE, "Invalid value " + s + " of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " throws " + nfe);
            }
        } else {
            err.log (Level.FINE, "Custom value of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " is " + s);
        }
        return period;
    }
    
    private static void expirationCheck () {
        Preferences p = getPreferences ();
        String exp = p.get (EXPIRATION_RECORD, null);
        Collection<String> forImport = new HashSet<String> (Arrays.asList (VERSIONS_FOR_IMPORT));
        String currentVersion = new File (System.getProperty ("netbeans.user")).getName ();
        forImport.add (currentVersion);
        if (exp != null && ! forImport.contains (exp)) {
            try {
                final int period = getPeriod();
                boolean shared = Utilities.isGlobalInstallation() == null ? false : Utilities.isGlobalInstallation();
                p.removeNode ();
                getPreferences ().put (IMPORTED, Boolean.toString (true));
                getPreferences ().putInt(PROP_PERIOD, period);
                if (shared) {
                    // Check that we can use 'shared' flag imported from previous installation with a new one.
                    // Obtained from SettingsTab.cbGlobalInstallActionPerformed
                    Collection<File> dirs = Utilities.sharedDirs();
                    if (!dirs.isEmpty()) {
                        for (File f : dirs) {
                            if (f.exists() && f.isDirectory() && !Utilities.canWriteInCluster(f)) {
                                shared = false;
                                break;
                            }
                        }
                        if (shared) {
                            Utilities.setGlobalInstallation(shared);
                        }
                    }
                }
                err.log (Level.FINE, "Don't read preferences from userdir " + exp);
            } catch (BackingStoreException ex) {
                err.log (Level.INFO, ex.getLocalizedMessage (), ex);
            }
        } else if (exp == null) {
            err.log (Level.FINEST, "No preferences imported from previous versoin.");
        } else {
            err.log (Level.FINEST, "Read preferences from userdir " + exp);
        }
        err.log (Level.FINEST, "Store current version " + currentVersion + " for future import.");
        getPreferences ().put (EXPIRATION_RECORD, currentVersion);
    }
    
    private static boolean existsSuperIdentity () {
        File superFile = getSuperFile ();
        if (superFile == null) {
            // IDE is lauchned as JNLP
            return true;
        }
        err.log (Level.FINE, "Does " + superFile + " exist? " + superFile.exists ());
        return superFile.exists ();
    }
    
    private static File getSuperFile () {
        // 1. check if IDE is lauchned as JNLP
        String home = System.getProperty ("user.home"); // NOI18N
        if ("memory".equals (home)) { // NOI18N
            err.log (Level.INFO, "IDE launched as JNLP");
            return null;
        }
        
        // 2. get superId from OS specific place
        File superFile = getSuperFileOsSpecific();
        if (superFile != null) {
            err.log (Level.FINE, "Does OS specific " + superFile + " exist? " + superFile.exists ());

            if (superFile != null && superFile.exists()) {
                return superFile;
            }
        }
        
        // 3. get superIf from .netbeans (a old place)
        File nbDir = new File (home, DEFAULT_NETBEANS_DIR);
        err.log (Level.FINE, "Does " + nbDir + " exist? " + nbDir.exists());
        if (nbDir.exists() && superFile != null) {
            File oldPlaceSuperFile = new File (nbDir, SUPER_IDENTITY_FILE_NAME);
            err.log (Level.FINE, "Does " + oldPlaceSuperFile + " exist on the old place? " + oldPlaceSuperFile.exists ());
            if (oldPlaceSuperFile.exists()) {
                // copy id to new place
                String id = readSuperFile(oldPlaceSuperFile);
                if (id != null) {
                    writeSuperFile(superFile, id);
                }
            }
        } else if (nbDir.exists() && superFile == null) {
            superFile = new File (nbDir, SUPER_IDENTITY_FILE_NAME);
        }
        return superFile;
    }

    
    private static File getSuperFileOsSpecific () {
        String defaultUserdirRoot = System.getProperty ("netbeans.default_userdir_root", null); // NOI18N
        err.log (Level.FINER, "netbeans.default_userdir_root: " + defaultUserdirRoot);
        if (defaultUserdirRoot == null) {
            err.warning("The property \"netbeans.default_userdir_root\" was not set!");
            return null;
        }
        File nbDir = new File (defaultUserdirRoot);
        nbDir.mkdirs ();
        return new File (nbDir, SUPER_IDENTITY_FILE_NAME);
    }
    
    private static void writeSuperFile(File superFile, String id) {
        Writer os = null;
        try {
            os = new BufferedWriter (new FileWriter (superFile));
            os.write (id);
            superId = id;
            err.log (Level.FINE, "Wrote Super Id: " + superId + " in " + superFile);
        } catch (IOException ex) {
            // let's ignore it
            err.log (Level.FINER, null, ex);
        } finally {
            try {
                if(os!=null) {
                    os.close ();
                }
            } catch (IOException ex) {
                // let's ignore it
                err.log (Level.FINER, null, ex);
            }
        }
    }
    
    private static String readSuperFile(File superFile) {
        String res = null;
        // read existing super Id
        InputStream is = null;
        try {
            is = new FileInputStream (superFile);
            BufferedReader r = new BufferedReader (new InputStreamReader (is));
            res = r.readLine ().trim ();
            err.log (Level.FINE, "Read Super Id: " + res + " from " + superFile);
        } catch (IOException ex) {
            // let's ignore it
            err.log (Level.FINER, null, ex);
        } finally {
            try {
                if(is!=null) {
                    is.close ();
                }
            } catch (IOException ex) {
                // let's ignore it
                err.log (Level.FINER, null, ex);
            }
        }
        return res;
    }

    
    private static String getSuperIdentity () {
        if (superId != null) {
            return superId;
        }
        File superFile = getSuperFile ();
        if (superFile == null) {
            err.log (Level.FINE, "superFile was returns null.");
            return null;
        }
        if (superFile.exists ()) {
            superId = readSuperFile(superFile);
        } else {
            // generate new one and store it
            superId = generateNewId ();
            writeSuperFile(superFile, superId);
        }
        if (superId != null) {
            err.log (Level.FINE, "Returns Super Id: " + superId);
            return superId;
        } else {
            err.log (Level.FINE, "Was problem while handling Super Id. Returns null");
            return null;
        }
    }
    
    private static String getQualifiedIdentity (String ideIdentity) {
        if (getSuperIdentity () != null) {
            err.log (Level.FINE, "Returns Qualified Id: " + ideIdentity + QUALIFIED_ID_DELIMETER + getSuperIdentity ());
            return ideIdentity + QUALIFIED_ID_DELIMETER + getSuperIdentity ();
        } else {
            err.log (Level.FINE, "Was problem while handling Qualified Id. Returns only original Id: " + ideIdentity);
            return ideIdentity;
        }

    }
    
}
