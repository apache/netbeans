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
package org.netbeans.modules.javascript2.debug.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.ui.TextLineHandler;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
public class LineDelegate implements TextLineHandler {
    
    private Line line;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private final LineChangesListener lineChangeslistener = new LineChangesListener();
    private PropertyChangeListener lineChangesWeak;
    
    public LineDelegate(Line line) {
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
    }

    @Override
    public Line getLine() {
        return line;
    }
    
    @Override
    public FileObject getFileObject() {
        if (line instanceof FutureLine) {
            URL url = getURL();
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
                    if (lineCookie == null) {
                        return null;
                    }
                    Line l = lineCookie.getLineSet().getCurrent(getLineNumber() - 1);
                    setLine(l);
                } catch (DataObjectNotFoundException ex) {
                }
            }
            return fo;
        } else {
            return line.getLookup().lookup(FileObject.class);
        }
    }

    @Override
    public URL getURL() {
        if (line instanceof FutureLine) {
            return ((FutureLine) line).getURL();
        }
        return line.getLookup().lookup(FileObject.class).toURL();
    }

    @Override
    public int getLineNumber() {
        return line.getLineNumber() + 1;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        lineNumber--; // Line works with 0-based lines.
        if (line.getLineNumber() == lineNumber) {
            return ;
        }
        LineCookie lineCookie = line.getLookup().lookup(LineCookie.class);
        Line.Set lineSet = lineCookie.getLineSet();
        List<? extends Line> lines = lineSet.getLines();
        if (lines.size() > 0) {
            int lastLineNumber = lines.get(lines.size() - 1).getLineNumber();
            if (lineNumber > lastLineNumber) {
                lineNumber = lastLineNumber;
            }
        }
        Line cline;
        try {
            cline = lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioobex) {
            cline = lineSet.getCurrent(0);
        }
        setLine(cline);
    }
    
    private void setLine(Line line) {
        dispose();
        int oldLineNumber = getLineNumber();
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        pchs.firePropertyChange(PROP_LINE_NUMBER, oldLineNumber, getLineNumber());
    }

    @Override
    public void dispose() {
        line.removePropertyChangeListener(lineChangesWeak);
        lineChangesWeak = null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pchl) {
        pchs.addPropertyChangeListener(pchl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pchl) {
        pchs.removePropertyChangeListener(pchl);
    }
    
    private class LineChangesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Line.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                pchs.firePropertyChange(PROP_LINE_NUMBER, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
    
}
