/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.shelve.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public final class PatchStorage {
    private static PatchStorage instance;
    private static final Logger LOG = Logger.getLogger(PatchStorage.class.getName());
    private static final String SEP = "<==>"; //NOI18N
    private static final String PROP_EXPLICIT_LOCATION = "versioning.shelve.patchStorage"; //NOI18N

    public static synchronized  PatchStorage getInstance () {
        if (instance == null) {
            instance = new PatchStorage();
        }
        return instance;
    }
    
    public void savePatchInfo (String patchName, File patchFile, File patchContext) {
        Preferences prefs = NbPreferences.forModule(ShelveChangesMenu.class);
        List<String> list = Utils.getStringList(prefs, ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES);
        removeRecord(list, patchName, false);
        list.add(0, new Patch(patchName, patchFile, patchContext).pack());
        Utils.put(prefs, ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES, list);
    }

    public File reservePatchFile (String patchName) {
        patchName = patchName.replace(SEP, ""); //NOI18N
        File storageLocation = getStorageLocation();
        storageLocation.mkdirs();
        int i = 0;
        File patchFile = getPatchFile(storageLocation, patchName);
        try {
            while (!patchFile.createNewFile()) {
                patchFile = getPatchFile(storageLocation, patchName + "-" + ++i); //NOI18N
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return patchFile;
    }

    public void dismissPatchFile (File patchFile) {
        patchFile.delete();
    }

    private static File getStorageLocation () {
        String explicitLocation = System.getProperty(PROP_EXPLICIT_LOCATION, ""); //NOI18N
        if (explicitLocation.isEmpty()) {
            File userDir = Places.getUserDirectory();
            return new File(new File(new File(userDir, "var"), "versioning"), "patch-storage"); //NOI18N
        } else {
            return new File(explicitLocation);
        }
    }

    private File getPatchFile (File storageLocation, String patchName) {
        return new File(storageLocation, patchName + ".patch"); //NOI18N
    }

    void removePatch (String patchName, boolean removePatchFile) {
        Preferences prefs = NbPreferences.forModule(ShelveChangesMenu.class);
        List<String> list = Utils.getStringList(prefs, ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES);
        removeRecord(list, patchName, removePatchFile);
        Utils.put(prefs, ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES, list);
    }
    
    private void removeRecord (List<String> list, String patchName, boolean removePatchFile) {
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String savedPatch = it.next();
            if (savedPatch.startsWith(patchName + SEP)) {
                it.remove();
                if (removePatchFile) {
                    Patch patch = Patch.unpack(savedPatch);
                    if (patch != null) {
                        File f = patch.getPatchFile();
                        f.delete();
                        FileUtil.refreshFor(f);
                    }
                }
            }
        }
    }

    Patch getPatch (String patchName) {
        Preferences prefs = NbPreferences.forModule(ShelveChangesMenu.class);
        List<String> list = Utils.getStringList(prefs, ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES);
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String savedPatch = it.next();
            if (savedPatch.startsWith(patchName + SEP)) {
                return Patch.unpack(savedPatch);
            }
        }
        return null;
    }
    
    public boolean containsPatch (String patchName) {
        return getPatch(patchName) != null;
    }

    List<Patch> getPatches () {
        List<String> list = Utils.getStringList(NbPreferences.forModule(ShelveChangesMenu.class), ShelveChangesMenu.PREF_KEY_SHELVED_PATCHES);
        List<Patch> patches = new LinkedList<Patch>();
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String savedPatch = it.next();
            Patch patch = Patch.unpack(savedPatch);
            if (patch != null) {
                patches.add(patch);
            }
        }
        return patches;
    }
    
    public List<String> getPatchNames () {
        List<Patch> patches = getPatches();
        List<String> patchNames = new ArrayList<String>(patches.size());
        for (Patch p : patches) {
            patchNames.add(p.getPatchName());
        }
        return patchNames;
    }

    static class Patch {

        private final File context;
        private final String name;
        private final File file;

        private Patch (String patchName, File patchFile, File patchContext) {
            this.name = patchName;
            this.file = patchFile;
            this.context = patchContext;
        }

        private String pack () {
            return new StringBuilder(name.replace(SEP, "")).append(SEP).append(file.getName()).append(SEP).append(context.getAbsolutePath()).toString(); //NOI18N
        }

        private static Patch unpack (String savedPatch) {
            String[] unpacked = savedPatch.split(SEP);
            if (unpacked.length < 3) {
                return null;
            }
            String name = unpacked[0];
            String fileName = unpacked[1];
            StringBuilder sb = new StringBuilder(unpacked[2]);
            for (int i = 3; i < unpacked.length; ++i) {
                sb.append(SEP).append(unpacked[i]);
            }
            return new Patch(name, new File(getStorageLocation(), fileName), new File(sb.toString()));
        }

        File getPatchFile () {
            return file;
        }

        File getPatchContext () {
            return context;
        }

        String getPatchName () {
            return name;
        }
    }
}
