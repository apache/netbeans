/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    public synchronized static ClipboardHistory getInstance() {
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
