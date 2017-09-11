/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jdk.jshell.Snippet;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * File-based implementation of saved history.
 * 
 * @author sdedic
 */
public class FileHistory implements ShellHistory {
    private static final Logger LOG = Logger.getLogger(FileHistory.class.getName());
    private static final int MAX_HISTORY_ITEMS = 50;
    
    private FileObject              historyFile;
    private List<ChangeListener>    listeners = new ArrayList<>();
    private List<Item>              items;
    private boolean                 fileWarned;
    private int                     maxHistory = MAX_HISTORY_ITEMS;

    protected FileHistory(FileObject historyFile) {
        this.historyFile = historyFile;
    }
    
    protected void setMaxHistoryItems(int max) {
        this.maxHistory = max;
    }
    
    @Override
    public List<Item> getHistory() {
        load();
        return items;
    }
    
    private String item2String(Item item) {
        String k;
        
        if (item.isShellCommand()) {
            k = KIND_COMMAND; // NOI18N
        } else {
            k = item.getKind().name().toUpperCase();
        }
        
        return k + MARKER+ item.getContents();
    }
    
    private Item string2Item(String s) {
        int marker = s.indexOf(MARKER); 
        if (marker == -1) {
            if (s.isEmpty()) {
                return null;
            }
            if (s.charAt(0) == '/') {
                return new Item(null, true, s);
            } else {
                return new Item(Snippet.Kind.ERRONEOUS, false, s);
            }
        }
        String k = s.substring(0, marker);
        String c = s.substring(marker + MARKER.length());
        if (KIND_COMMAND.equals(k)) {
            return new Item(null, true, c);
        }
        Snippet.Kind kind;
        try {
            kind = Snippet.Kind.valueOf(k);
        } catch (IllegalArgumentException ex) {
            kind = Snippet.Kind.ERRONEOUS;
        }
        return new Item(kind, false, c);
    }
    
    private static final String MARKER = "#>";
    
    
    private static final String KIND_COMMAND = "CMD";

    @Override
    public void pushItems(List<Item> newItems) {
        load();
        synchronized (this) {
            items.addAll(newItems);
            if (items.size() > maxHistory) {
                items.removeAll(items.subList(0, items.size() - maxHistory));
            }
        }
        ChangeListener[] ll = null;
        synchronized (this) {
            if (!listeners.isEmpty()) {
                ll = newItems.toArray(new ChangeListener[newItems.size()]);
            }
        }
        if (ll != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }
        save();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized (this) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (this) {
            listeners.remove(l);
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    protected FileObject    createFile() throws IOException {
        return null;
    }
    
    private void load() {
        synchronized (this) {
            if (items != null) {
                return;
            }
        }
        if (historyFile == null) {
            items = new ArrayList<>();
            return;
        }
        List<Item>    loadedItems = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            boolean continuation = false;
            
            for (String s : historyFile.asLines()) {
                int from = 0;
                if (continuation) {
                    // eat leading tab, if present:
                    if (!s.isEmpty() && s.charAt(0) == '\t') {
                        s = s.substring(1);
                    }
                }
                continuation = (!s.isEmpty() && s.charAt(s.length() - 1) == '\\');
                if (!continuation) {
                    sb.append(s);
                    loadedItems.add(string2Item(sb.toString()));
                    sb = new StringBuilder();
                } else {
                    sb.append(s, 0, s.length() - 1);
                    sb.append("\n");
                }
                
            }
            if (continuation) {
                loadedItems.add(string2Item(sb.toString()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                        Exceptions.attachSeverity(ex, Level.INFO),
                        "Unable to load shell history"
            ));
        }
        synchronized (this) {
            if (this.items == null) {
                items = loadedItems;
            }
        }
    }
    
    private void save() {
        try {
            load();
            if (historyFile == null) {
                historyFile = createFile();
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Unable to create history file", 
                    Exceptions.attachSeverity(ex, Level.INFO));
            return;
        }
        List<Item>    itemsToSave;
        
        synchronized (this) {
            itemsToSave = this.items;
        }
        
        try (BufferedWriter wr = new BufferedWriter(
                new OutputStreamWriter(historyFile.getOutputStream(), "UTF-8"))) {
            for (Item item : itemsToSave) {
                String s = item2String(item);
                String[] lines = s.split("\n");
                int count = lines.length;
                
                for (String l : lines) {
                    l.trim();
                    wr.write(l);
                    if (count != 1) {
                        // indent the following line
                        wr.write("\\");
                        wr.newLine();
                        wr.write("\t");
                    } else {
                        wr.newLine();
                    }
                    count--;
                }
            }
            wr.flush();
        } catch (IOException ex) {
            if (!fileWarned) {
                LOG.log(Level.INFO, "Unable to write history file", 
                        Exceptions.attachSeverity(ex, Level.INFO));
                fileWarned = true;
            }
        }
    }
}
