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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.editor.BaseDocument;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Translation utility for handling of lines that are shifted during source modifications.
 * 
 * @author Martin Entlicher
 */
class LineTranslations {
    
    private static LineTranslations translations;

    private ChangeListener          changedFilesListener;
    private Map<Object, Registry>   timeStampToRegistry = new WeakHashMap<Object, Registry>();
    private Map<LineBreakpoint, BreakpointLineUpdater> lineUpdaters = new HashMap<LineBreakpoint, BreakpointLineUpdater>();
    private Map<Object, Map<LineBreakpoint, Integer>> originalBreakpointLines = new WeakHashMap<Object, Map<LineBreakpoint, Integer>>();
    private Map<Object, PropertyChangeListener> breakpointListeners = new WeakHashMap<Object, PropertyChangeListener>();
    
    private LineTranslations() {
    }
    
    static synchronized LineTranslations getTranslations() {
        if (translations == null) {
            translations = new LineTranslations();
        }
        return translations;
    }

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    void createTimeStamp (Object timeStamp) {
        Set<DataObject> modifiedDataObjects = DataObject.getRegistry().getModifiedSet();
        Registry r = new Registry ();
        synchronized (this) {
            timeStampToRegistry.put (timeStamp, r);
            for (DataObject dobj : modifiedDataObjects) {
                r.register (dobj);
            }

            if (changedFilesListener == null) {
                changedFilesListener = new ChangedFilesListener ();
                DataObject.getRegistry ().addChangeListener (changedFilesListener);
            }
        }
    }

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    synchronized void disposeTimeStamp (Object timeStamp) {
        timeStampToRegistry.remove (timeStamp);
        if (timeStampToRegistry.isEmpty ()) {
            DataObject.getRegistry ().removeChangeListener (changedFilesListener);
            changedFilesListener = null;
        }
        originalBreakpointLines.remove(timeStamp);
        breakpointListeners.remove(timeStamp);
    }
    
    /**
     * Returns the original line number of a breakpoint.
     *
     * @param url The URL
     * @param currentLineNumber The current line number
     * @param timeStamp a time stamp to be used
     *
     * @return The original line number
     */
    int getOriginalLineNumber (
        final LineBreakpoint lb,
        final Object timeStamp
    ) {
        Map<LineBreakpoint, Integer> bpLines;
        PropertyChangeListener lineNumberListener;
        synchronized (this) {
            bpLines = originalBreakpointLines.get(timeStamp);
            if (bpLines != null) {
                Integer line = bpLines.get(lb);
                if (line != null) {
                    //System.err.println("Original line of "+lb+" IS "+line);
                    return line.intValue();
                }
            } else {
                bpLines = new WeakHashMap<LineBreakpoint, Integer>();
                originalBreakpointLines.put(timeStamp, bpLines);
            }
        }
        int line = getOriginalLineNumber(lb.getURL(), lb.getLineNumber(), timeStamp);
        synchronized (this) {
            bpLines.put(lb, line);
            lineNumberListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (LineBreakpoint.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                        final Map<LineBreakpoint, Integer> bpLines;
                        synchronized (LineTranslations.this) {
                            bpLines = originalBreakpointLines.get(timeStamp);
                            if (bpLines == null) {
                                return ;
                            }
                        }
                        LineBreakpoint lb = (LineBreakpoint) evt.getSource();
                        int line = getOriginalLineNumber(lb.getURL(), lb.getLineNumber(), timeStamp);
                        synchronized (LineTranslations.this) {
                            bpLines.put(lb, line);
                        }
                    }
                }
            };
            breakpointListeners.put(timeStamp, lineNumberListener);
        }
        lb.addPropertyChangeListener(WeakListeners.propertyChange(lineNumberListener, lb));
        return line;
    }
    
    /**
     * Returns the original line number.
     *
     * @param url The URL
     * @param currentLineNumber The current line number
     * @param timeStamp a time stamp to be used
     *
     * @return The original line number
     */
    int getOriginalLineNumber (
        String url,
        int currentLineNumber,
        Object timeStamp
    ) {
        //System.err.println("getOriginalLineNumber("+url+", "+currentLineNumber+", "+timeStamp+")");
        if (timeStamp == null) {
            return currentLineNumber;
        } else {
            Line.Set lineSet = getLineSet (url, timeStamp);
            if (lineSet == null) {
                return currentLineNumber;
            }
            //System.err.println("  lineSet = "+lineSet+"date = "+lineSet.getDate());
            try {
                lineSet.getOriginal(0); // To assure, that the set is updated.
            } catch (IndexOutOfBoundsException ioobex) {}
            try {
                //Line line = lineSet.getCurrent(currentLineNumber);
                //System.err.println("  current line = "+line);
                //System.err.println("  original line = "+lineSet.getOriginalLineNumber(line));
                //System.err.println("  original line2 = "+lineSet.getOriginal(currentLineNumber));
                //System.err.println("Original line of "+currentLineNumber+" IS "+lineSet.getOriginalLineNumber(lineSet.getCurrent(currentLineNumber)));
                Line line = lineSet.getCurrent(currentLineNumber);
                if (line != null) {
                    return lineSet.getOriginalLineNumber(line);
                } else {
                    return currentLineNumber;
                }
            } catch (IndexOutOfBoundsException ioobex) {
                //ioobex.printStackTrace();
                //System.err.println("  getOriginalLineNumber.return "+currentLineNumber);
                return currentLineNumber;
            }
        }
    }
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    void updateTimeStamp (Object timeStamp, String url) {
        //System.err.println("LineTranslations.updateTimeStamp("+timeStamp+", "+url+")");
        DataObject dobj = getDataObject (url);
        synchronized (this) {
            Registry registry = timeStampToRegistry.get (timeStamp);
            registry.register (dobj);
            Map<LineBreakpoint, Integer> bpLines = originalBreakpointLines.get(timeStamp);
            if (bpLines != null) {
                Set<LineBreakpoint> bpts = new HashSet<LineBreakpoint>(bpLines.keySet());
                for (LineBreakpoint bp : bpts) {
                    if (url.equals(bp.getURL())) {
                        bpLines.remove(bp);
                    }
                }
            }
        }
    }

    Line.Set getLineSet (String url, Object timeStamp) {
        DataObject dataObject = getDataObject (url);
        if (dataObject == null) {
            return null;
        }
        
        if (timeStamp != null) {
            // get original
            synchronized (this) {
                Registry registry = timeStampToRegistry.get (timeStamp);
                if (registry != null) {
                    Line.Set ls = registry.getLineSet (dataObject);
                    if (ls != null) {
                        return ls;
                    }
                }
            }
        }
        
        // get current
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            return null;
        }
        return lineCookie.getLineSet ();
    }

    Line getLine (String url, int lineNumber, Object timeStamp) {
        //System.err.println("LineTranslations.getLine("+lineNumber+", "+timeStamp+")");
        Line.Set ls = getLineSet (url, timeStamp);
        if (ls == null) {
            return null;
        }
        try {
            //System.err.println("  Line.Set = "+ls+", date = "+ls.getDate());
            //System.err.println("  current("+(lineNumber-1)+") = "+ls.getCurrent (lineNumber - 1));
            //System.err.println("  originl("+(lineNumber-1)+") = "+ls.getOriginal (lineNumber - 1));
            if (timeStamp == null) {
                return ls.getCurrent (lineNumber - 1);
            } else {
                return ls.getOriginal (lineNumber - 1);
            }
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
    
    void registerForLineUpdates(LineBreakpoint lb) {
        //translatedBreakpoints.add(lb);
        DataObject dobj = getDataObject(lb.getURL());
        if (dobj != null) {
            BreakpointLineUpdater blu = new BreakpointLineUpdater(lb, dobj);
            try {
                blu.attach();
                synchronized (this) {
                    lineUpdaters.put(lb, blu);
                }
            } catch (IOException ioex) {
                // Ignore
            }
        }
    }

    void unregisterFromLineUpdates(LineBreakpoint lb) {
        //translatedBreakpoints.remove(lb);
        BreakpointLineUpdater blu;
        synchronized (this) {
            blu = lineUpdaters.remove(lb);
        }
        if (blu != null) {
            blu.destroy();
        }
        //if (timeStampToRegistry.isEmpty () && translatedBreakpoints.isEmpty()) {
        //    DataObject.getRegistry ().removeChangeListener (changedFilesListener);
        //   changedFilesListener = null;
        //}
    }

    private static DataObject getDataObject (String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }

        if (file == null) {
            return null;
        }
        try {
            return DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }
    
    
    
    
    private static class Registry {
        
        private Map<DataObject, Line.Set> dataObjectToLineSet = new HashMap<DataObject, Line.Set>();
        
        synchronized void register (DataObject dataObject) {
            LineCookie lc = dataObject.getLookup().lookup (LineCookie.class);
            if (lc == null) {
                return;
            }
            dataObjectToLineSet.put (dataObject, lc.getLineSet ());
        }
        
        synchronized void registerIfNotThere(DataObject dataObject) {
            if (!dataObjectToLineSet.containsKey(dataObject)) {
                register(dataObject);
            }
        }
        
        synchronized Line.Set getLineSet (DataObject dataObject) {
            return dataObjectToLineSet.get (dataObject);
        }

    }
    
    private class ChangedFilesListener implements ChangeListener {
        @Override
        public void stateChanged (ChangeEvent e) {
            Set<DataObject> newDOs = new HashSet<DataObject>(
                DataObject.getRegistry ().getModifiedSet()
            );
            synchronized (LineTranslations.this) {
                //newDOs.removeAll (modifiedDataObjects);
                for (Registry r : timeStampToRegistry.values ()) {
                    for (DataObject dobj : newDOs) {
                        r.registerIfNotThere (dobj);
                    }
                }
                //modifiedDataObjects = DataObject.getRegistry().getModifiedSet();
            }
        }
    }
    
    private class BreakpointLineUpdater implements PropertyChangeListener, DocumentListener, ActionListener {
        
        private final LineBreakpoint lb;
        private DataObject dataObject;
        private Line line;
        private final List<Line> lineHasChanged = new ArrayList<Line>();
        private final Timer lineChangePostProcess = new Timer(1000, this);
        private boolean updatingLine = false;
        
        public BreakpointLineUpdater(LineBreakpoint lb, DataObject dataObject) {
            this.lb = lb;
            this.dataObject = dataObject;
            lineChangePostProcess.setRepeats(false);
            DataEditorSupport des = dataObject.getLookup().lookup(DataEditorSupport.class);
            if (des != null) {
                des.addPropertyChangeListener(this);
            }
        }
        
        public void attach() throws IOException {
            DataObject dobj;
            synchronized (this) {
                dobj = this.dataObject;
            }
            LineCookie lc = dobj.getLookup().lookup (LineCookie.class);
            if (lc == null) {
                return ;
            }
            lb.addPropertyChangeListener(this);
            try {
                final Line lineNew = lc.getLineSet().getCurrent(lb.getLineNumber() - 1);
                synchronized (this) {
                    if (line != null) {
                        line.removePropertyChangeListener(this);
                    }
                    this.line = lineNew;
                }
                lineNew.addPropertyChangeListener(this);
                StyledDocument document = NbDocument.getDocument(new Lookup.Provider() {
                                              @Override
                                              public Lookup getLookup() {
                                                  return lineNew.getLookup();
                                              }
                                          });
                if (document instanceof BaseDocument) {
                    BaseDocument bd = (BaseDocument) document;
                    bd.addPostModificationDocumentListener(this);
                }
            } catch (IndexOutOfBoundsException ioobex) {
                // ignore document changes for BP with bad line number
            }
        }
        
        public synchronized void detach() {
            lb.removePropertyChangeListener(this);
            if (line != null) {
                line.removePropertyChangeListener(this);
            }
        }
        
        public void destroy() {
            detach();
            DataEditorSupport des = dataObject.getLookup().lookup(DataEditorSupport.class);
            if (des != null) {
                des.removePropertyChangeListener(this);
            }
        }

        private void update(Line l) {
            try {
                int ln;
                synchronized (this) {
                    updatingLine = true;
                    ln = l.getLineNumber() + 1;
                }
                lb.setLineNumber(ln);
            } finally {
                synchronized (this) {
                    updatingLine = false;
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(propertyName)) {
                DataEditorSupport des = dataObject.getLookup().lookup(DataEditorSupport.class);
                JEditorPane[] openedPanes;
                if (des != null) {
                    openedPanes = des.getOpenedPanes();
                } else {
                    openedPanes = null;
                }
                if (openedPanes == null) {
                    synchronized (this) {
                        if (line != null) {
                            detach();
                            line = null;
                        }
                    }
                } else {
                    synchronized (this) {
                        if (line == null) {
                            try {
                                attach();
                            } catch (IOException ioex) {}
                        }
                    }
                }
                return ;
            }
            Line l;
            boolean ul;
            synchronized (this) {
                l = this.line;
                if (l == null) {
                    return ;
                }
                ul = this.updatingLine;
            }
            if (Line.PROP_LINE_NUMBER.equals(propertyName) && l == evt.getSource()) {
                lineHasChanged(l);
                return ;
            }
            if (Line.PROP_TEXT.equals(propertyName) && l == evt.getSource()) {
                String text = l.getText();
                if (text.trim().length() == 0 && text.indexOf('\n') >= 0) {
                    // Move the breakpoint 'n' lines down:
                    DataObject dobj;
                    synchronized (this) {
                        line.removePropertyChangeListener(this);
                        if (dataObject == null) {
                            return ;
                        }
                        dobj = dataObject;
                    }
                    LineCookie lc = dobj.getLookup().lookup (LineCookie.class);
                    Line newLine;
                    try {
                        int lineNumber = l.getLineNumber();
                        int newLineNumber = lc.getLineSet().getOriginal(lineNumber).getLineNumber();
                        for (int i = lineNumber + 1; i < newLineNumber; i++) {
                            if (lc.getLineSet().getCurrent(i).getText().trim().length() != 0) {
                                newLineNumber = i;
                                break;
                            }
                        }
                        newLine = lc.getLineSet().getCurrent(newLineNumber);
                        newLine.addPropertyChangeListener(this);
                    } catch (IndexOutOfBoundsException ioobex) {
                        return ;
                    }
                    synchronized (this) {
                        line = newLine;
                    }
                    update(newLine);
                }
                return ;
            }
            if (!ul && LineBreakpoint.PROP_LINE_NUMBER.equals(propertyName)) {
                DataObject dobj;
                synchronized (this) {
                    line.removePropertyChangeListener(this);
                    if (dataObject == null) {
                        return ;
                    }
                    dobj = dataObject;
                }
                Line newLine;
                try {
                    LineCookie lc = dobj.getLookup().lookup (LineCookie.class);
                    newLine = lc.getLineSet().getCurrent(lb.getLineNumber() - 1);
                    newLine.addPropertyChangeListener(this);
                } catch (IndexOutOfBoundsException ioobex) {
                    newLine = null;
                }
                synchronized (this) {
                    line = newLine;
                }
            }
            if (LineBreakpoint.PROP_URL.equals(propertyName)) {
                DataEditorSupport des = dataObject.getLookup().lookup(DataEditorSupport.class);
                if (des != null) {
                    des.removePropertyChangeListener(this);
                }

                DataObject newDO = getDataObject(lb.getURL());
                Line newLine;
                if (newDO != null) {
                    LineCookie lc = newDO.getLookup().lookup (LineCookie.class);
                    try {
                        newLine = lc.getLineSet().getCurrent(lb.getLineNumber() - 1);
                        newLine.addPropertyChangeListener(this);
                    } catch (IndexOutOfBoundsException ioobex) {
                        // ignore document changes for BP with bad line number
                        newLine = null;
                    }
                } else {
                    newLine = null;
                }
                synchronized (this) {
                    // detach
                    if (line != null) {
                        line.removePropertyChangeListener(this);
                    }

                    // update DataObject
                    this.dataObject = newDO;

                    // attach
                    this.line = newLine;
                }
                des = newDO.getLookup().lookup(DataEditorSupport.class);
                if (des != null) {
                    des.addPropertyChangeListener(this);
                }
            }
        }
        
        private void lineHasChanged(Line l) {
            if (!lineHasChanged.contains(l)) {
                lineHasChanged.add(l);
            }
            lineChangePostProcess.restart();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            lineMightChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            lineMightChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            lineMightChange();
        }
        
        private void lineMightChange() {
            while (!lineHasChanged.isEmpty()) {
                Line l = lineHasChanged.remove(0);
                update(l);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            lineMightChange();
        }
        
    }
    
}
