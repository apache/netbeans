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
package org.netbeans.modules.editor.impl.actions.clipboardhistory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;


public final class ClipboardHistory implements ClipboardListener {
    private final ArrayList<ClipboardHistoryElement> data;
    private static ClipboardHistory instance;
    private static int MAXSIZE = 9;
    private static final int MAX_ITEM_LENGTH = 5000000; //10MB

    private static Preferences prefs;
    /** Name of preferences node where we persist history */
    private static final String PREFS_NODE = "ClipboardHistory";  //NOI18N
    private static final String PROP_ITEM_PREFIX = "item_";  //NOI18N
    private static final boolean PERSISTENT_STATE = Boolean.getBoolean("netbeans.clipboard.history.persistent"); //NOI18N

    static {
        Integer maxsize = Integer.getInteger("netbeans.clipboard.history.maxsize"); //NOI18N
        if (maxsize != null) {
            MAXSIZE = maxsize;
        }
        if (PERSISTENT_STATE) {
            prefs = NbPreferences.forModule(ClipboardHistory.class).node(PREFS_NODE);
        }
    }

    public static synchronized ClipboardHistory getInstance() {
        if (instance == null) {
            instance = new ClipboardHistory();
        }
        return instance;
    }

    private ClipboardHistory() {
        data = new ArrayList<ClipboardHistoryElement>();
        if (PERSISTENT_STATE) {
            load();
        }
    }

    private void addHistory(Transferable transferable, String text) {
        if (transferable == null || text == null || text.length() > MAX_ITEM_LENGTH) {
            return;
        }
        ClipboardHistoryElement newHistory = new ClipboardHistoryElement(transferable, text);
        if (PERSISTENT_STATE) {
            addAndPersist(newHistory);
        } else {
            addHistory(newHistory);
        }
    }

    private synchronized void addHistory(ClipboardHistoryElement newHistory) {
        data.remove(newHistory);
        data.add(0,newHistory);
        if (data.size() > 2 * MAXSIZE) {
            data.remove(data.size()-1);
        }
    }


    public synchronized List<ClipboardHistoryElement> getData() {
        if (data.size() > MAXSIZE) {
            return Collections.unmodifiableList(data.subList(0, MAXSIZE));
        } else {
            return Collections.unmodifiableList(data);
        }
    }


    @Override
    public void clipboardChanged(ClipboardEvent ev) {
        ExClipboard clipboard = ev.getClipboard();

        Transferable transferable = null;
        String clipboardContent = null;
        try {
            transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                clipboardContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (OutOfMemoryError oom) {            
            NotificationDisplayer.getDefault().notify( NbBundle.getBundle(ClipboardHistory.class).getString("clipboard-history-oom"),NotificationDisplayer.Priority.NORMAL.getIcon(), NbBundle.getBundle(ClipboardHistory.class).getString("clipboard-history-oom-details"), null);
            return;
        } catch (IOException ioe) {
            //ignored for bug #218255
        } catch (UnsupportedFlavorException ufe) {
        }

        if (clipboardContent != null) {
            addHistory(transferable, clipboardContent);
        }
    }

    public synchronized int getPosition(ClipboardHistoryElement history) {
        return data.indexOf(history);
    }

    private void load() {
        for(int i=0; i < MAXSIZE; i++){
            String item = prefs.get(PROP_ITEM_PREFIX + i, null);
            if (item != null) {
                data.add(i, new ClipboardHistoryElement(item));
            }
        }
    }

    public synchronized void addAndPersist(ClipboardHistoryElement newHistoryElement) {
        if (data.size() > 0 && newHistoryElement.equals(data.get(0))) {
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            if (newHistoryElement.equals(data.get(i))) {
                data.remove(i);
                break;
            }
        }

        if (data.size() == MAXSIZE){
            data.remove(MAXSIZE-1);
        }
        data.add(0, newHistoryElement);

        int i = 0;
        for (ClipboardHistoryElement elem : data) {
            prefs.put(PROP_ITEM_PREFIX + i, elem.getFullText());
            i++;
        }
    }
}
