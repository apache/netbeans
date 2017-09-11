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

package org.netbeans.modules.openfile;

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.OnStop;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages prioritized set of recently closed files.
 *
 * @author Dafe Simonek
 */
public final class RecentFiles {

    /** List of recently closed files */
    private static List<HistoryItem> history = new ArrayList<HistoryItem>();
    /** Request processor */
    private static RequestProcessor RP = new RequestProcessor(RecentFiles.class);
    /** Preferences node for storing history info */
    private static Preferences prefs;
    private static final Object HISTORY_LOCK = new Object();
    /** Name of preferences node where we persist history */
    private static final String PREFS_NODE = "RecentFilesHistory"; //NOI18N
    /** Prefix of property for recent file URL*/
    private static final String PROP_URL_PREFIX = "RecentFilesURL."; //NOI18N
    /** Prefix of property for recent file icon bytes*/
    private static final String PROP_ICON_PREFIX = "RecentFilesIcon."; //NOI18N
    /** Boundary for items count in history */
    static final int MAX_HISTORY_ITEMS = 15;

    private static PropertyChangeListener windowRegistryListener;
    private static final Logger LOG = Logger.getLogger(
            RecentFiles.class.getName());

    private static final String RECENT_FILE_KEY = "nb.recent.file.path"; // NOI18N

    private RecentFiles() {
    }

    /** Starts to listen for recently closed files */
    public static void init() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                List<HistoryItem> loaded = load();
                synchronized (HISTORY_LOCK) {
                    history.addAll(0, loaded);
                    if (windowRegistryListener == null) {
                        windowRegistryListener = new WindowRegistryL();
                        TopComponent.getRegistry().addPropertyChangeListener(
                                windowRegistryListener);
                    }
                }
            }
        });
    }

    /** Returns read-only list of recently closed files */
    static List<HistoryItem> getRecentFiles() {
        synchronized (HISTORY_LOCK) {
            checkHistory();
            return Collections.unmodifiableList(history);
        }
    }
    private static volatile boolean historyProbablyValid;

    /**
     * True if there are probably some recently closed files.
     * Note: will still be true if all of them are in fact invalid,
     * but this is much faster than calling {@link #getRecentFiles}.
     */
    public static boolean hasRecentFiles() {
        if (!historyProbablyValid) {
            synchronized (HISTORY_LOCK) {
                checkHistory();
                return !history.isEmpty();
            }
        }
        return historyProbablyValid;
    }

    /** Loads list of recent files stored in previous system sessions.
     * @return list of stored recent files
     */
    static List<HistoryItem> load() {
        String[] keys;
        Preferences _prefs = getPrefs();
        try {
            keys = _prefs.keys();
        } catch (BackingStoreException ex) {
            Logger.getLogger(RecentFiles.class.getName()).
                    log(Level.FINE, ex.getMessage(), ex);
            return Collections.emptyList();
        }

        List<HistoryItem> result = new ArrayList<HistoryItem>();
        for (String curKey : keys) {
            if (curKey.startsWith(PROP_ICON_PREFIX)) {
                continue;
            }
            String value = _prefs.get(curKey, null);
            if (value != null) {
                try {
                    int id = new Integer(
                         curKey.substring(PROP_URL_PREFIX.length())).intValue();
                    HistoryItem hItem = new HistoryItem(id, value,
                            _prefs.getByteArray(PROP_ICON_PREFIX + id, null));
                    int ind = result.indexOf(hItem);
                    if (ind == -1) {
                        result.add(hItem);
                    } else {
                        _prefs.remove(PROP_URL_PREFIX +
                                      Math.max(result.get(ind).id, id));
                        result.get(ind).id = Math.min(result.get(ind).id, id);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(RecentFiles.class.getName()).
                            log(Level.FINE, ex.getMessage(), ex);
                    _prefs.remove(curKey);
                }
            } else {
                //clear the recent files history file from the old,
                // not known and broken keys
                _prefs.remove(curKey);
            }
        }
        Collections.sort(result);
        store(result);

        return result;
    }

    static void store() {
        store(history);
    }

    static void store(List<HistoryItem> history) {
        Preferences _prefs = getPrefs();
        for (int i = 0; i < history.size(); i++) {
            HistoryItem hi = history.get(i);
            if ((hi.id != i) && (hi.id >= history.size())) {
                _prefs.remove(PROP_URL_PREFIX + hi.id);
                _prefs.remove(PROP_ICON_PREFIX + hi.id);
            }
            hi.id = i;
            _prefs.put(PROP_URL_PREFIX + i, hi.getPath());
            if (hi.getIconBytes() == null) {
                _prefs.remove(PROP_ICON_PREFIX + i);
            } else {
                _prefs.putByteArray(PROP_ICON_PREFIX + i, hi.getIconBytes());
            }
        }
        LOG.log(Level.FINE, "Stored");
    }

    /**
     * Clear the history. Should be called only from tests.
     */
    static void clear() {
        try {
            synchronized (HISTORY_LOCK) {
                history.clear();
                getPrefs().clear();
                getPrefs().flush();
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    static Preferences getPrefs() {
        if (prefs == null) {
            prefs = NbPreferences.forModule(RecentFiles.class).node(PREFS_NODE);
        }
        return prefs;
    }

    /** Adds file represented by given TopComponent to the list,
     * if conditions are met.
     */
    private static void addFile(final TopComponent tc) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                addFile(obtainPath(tc));
            }
        });
    }

    static void addFile(String path) {
        if (path != null) {
            historyProbablyValid = false;
            synchronized (HISTORY_LOCK) {
                // avoid duplicates
                HistoryItem hItem = null;
                do {
                    hItem = findHistoryItem(path);
                } while (history.remove(hItem));

                final HistoryItem newItem = new HistoryItem(0, path);
                history.add(0, newItem);
                for (int i = MAX_HISTORY_ITEMS; i < history.size(); i++) {
                    history.remove(i);
                }
                newItem.setIcon(findIconForPath(newItem.getPath()));
                store();
            }
        }
    }

    /** Removes file represented by given TopComponent from the list */
    private static void removeFile(final TopComponent tc) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                historyProbablyValid = false;
                String path = obtainPath(tc);
                if (path != null) {
                    synchronized (HISTORY_LOCK) {
                        HistoryItem hItem = findHistoryItem(path);
                        if (hItem != null) {
                            history.remove(hItem);
                        }
                        store();
                    }
                }
            }
        });
    }

    private static Icon findIconForPath(String path) {
        FileObject fo = RecentFiles.convertPath2File(path);
        final Icon i;
        if (fo == null) {
            i = null;
        } else {
            DataObject dObj;
            try {
                dObj = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                dObj = null;
            }
            i = dObj == null
                    ? null
                    : new ImageIcon(dObj.getNodeDelegate().getIcon(
                    BeanInfo.ICON_COLOR_16x16));
        }
        return i;
    }

    private static String obtainPath(TopComponent tc) {
        Object file = tc.getClientProperty( RECENT_FILE_KEY );
        if( file instanceof File )
            return ((File)file).getPath();
        if( tc instanceof CloneableTopComponent ) {
            DataObject dObj = tc.getLookup().lookup(DataObject.class);
            if (dObj != null) {
                FileObject fo = dObj.getPrimaryFile();
                if (fo != null) {
                    return convertFile2Path(fo);
                }
            }
        }
        return null;
    }

    private static HistoryItem findHistoryItem(String path) {
        for (HistoryItem hItem : history) {
            if (path.equals(hItem.getPath())) {
                return hItem;
            }
        }
        return null;
    }

    static String convertFile2Path(FileObject fo) {
        File f = FileUtil.toFile(fo);
        return f == null ? null : f.getPath();
    }

    static FileObject convertPath2File(String path) {
        File f = new File(path);
        f = FileUtil.normalizeFile(f);
        return f == null ? null : FileUtil.toFileObject(f);
    }

    /** Checks recent files history and removes non-valid entries */
    private static void checkHistory() {
        assert Thread.holdsLock(HISTORY_LOCK);
        historyProbablyValid = !history.isEmpty();
    }

    private static byte[] iconToBytes(Icon icon) {
        if (icon == null) {
            return null;
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                try {
                    //#138000
                    Icon icn = icon;
                    if (!(icn instanceof Serializable)) {
                        icn = new ImageIcon(ImageUtilities.icon2Image(icn));
                    }
                    objOut.writeObject(icn);
                    return out.toByteArray();
                } finally {
                    objOut.close();
                }
            } catch (IOException ex) {
                return null;
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private static Icon bytesToIcon(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            ObjectInputStream objin = null;
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                objin = new ObjectInputStream(in);
                Object obj = objin.readObject();
                return (obj instanceof Icon) ? (Icon) obj : null;
            } catch (Exception ex) {
                return null;
            } finally {
                try {
                    if (objin != null) {
                        objin.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    static void pruneHistory() {
        synchronized (HISTORY_LOCK) {
            Iterator<HistoryItem> it = history.iterator();
            while (it.hasNext()) {
                HistoryItem historyItem = it.next();
                File f = new File(historyItem.getPath());
                if (!f.exists()) {
                    it.remove();
                }
            }
        }
    }

   /** 
    * One item of the recently closed files history.
    * Comparable by the time field, ascending from most recent to older items.
    */
    static final class HistoryItem implements Comparable<HistoryItem> {

        private int id;
        private String path;
        private String fileName;
        private Icon icon = null;

        HistoryItem(int id, String path) {
            this(id, path, null);
        }

        HistoryItem(int id, String path, byte[] iconBytes) {
            this.path = path;
            this.id = id;
            this.icon = bytesToIcon(iconBytes);
        }

        public String getPath() {
            return path;
        }

        public String getFileName() {
            if (fileName == null) {
                int pos = path.lastIndexOf(File.separatorChar);
                if ((pos != -1) && (pos < path.length())) {
                    fileName = path.substring(pos + 1);
                } else {
                    fileName = path;
                }
            }
            return fileName;
        }

        /**
         * Get specified icon, or a default one if no icon is specified.
         */
        public Icon getIcon() {
            return this.icon == null
                    ? ImageUtilities.loadImageIcon(
                    "org/openide/resources/actions/empty.gif", false) //NOI18N
                    : this.icon;
        }

        /**
         * Set icon of this history item. The icon can be set after
         * initialization, usually after it was loaded in a background thread.
         */
        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        /**
         * Return bytes for the icon, or null if no icon is specified.
         */
        public byte[] getIconBytes() {
            return iconToBytes(icon);
        }

        @Override
        public int compareTo(HistoryItem o) {
            return this.id - o.id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HistoryItem) {
                return ((HistoryItem) obj).getPath().equals(path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.path != null ? this.path.hashCode() : 0);
            return hash;
        }
    }

    /** Receives info about opened and closed TopComponents from window system.
     */
    private static class WindowRegistryL implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (TopComponent.Registry.PROP_TC_CLOSED.equals(name)) {
                addFile((TopComponent) evt.getNewValue());
            }
            if (TopComponent.Registry.PROP_TC_OPENED.equals(name)) {
                removeFile((TopComponent) evt.getNewValue());
            }
        }
    }

    /**
     * {@link Runnable} that will be invoked during shutdown sequence and that
     * will add non-persistent {@link TopComponent}s to the list of recent
     * files. See bug #218695.
     */
    @OnStop
    public static final class NonPersistentDocumentsAdder implements Runnable {

        @Override
        public void run() {
            for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
                if (TopComponent.PERSISTENCE_NEVER == tc.getPersistenceType()) {
                    addFile(tc);
                }
            }
        }
    }
}
