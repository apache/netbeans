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

package org.netbeans.modules.javascript2.debug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
public class JSLineBreakpoint extends Breakpoint {
    
    public static final String PROP_URL = "url";
    /**
     * This property is fired when a new Line object is set to this breakpoint.
     *
    public static final String PROP_LINE = "line";      // NOI18N
    */
    /**
     * This property is fired when a line number of the breakpoint's Line object changes.
     * It's the same as listening on the current Line object's {@link Line#PROP_LINE_NUMBER} events.
     */
    public static final String PROP_LINE_NUMBER = "lineNumber";      // NOI18N
    /**
     * This property is fired when the file changes.
     * Please note that the Line object may stay the same when the file is renamed.
     */
    public static final String PROP_FILE = "fileChanged";           // NOI18N
    public static final String PROP_CONDITION = "condition";
    
    private EditorLineHandler line;
    private boolean isEnabled = true;
    private volatile String condition;
    private final FileRemoveListener myListener = new FileRemoveListener();
    private FileChangeListener myWeakListener;
    private final LineChangesListener lineChangeslistener = new LineChangesListener();
    private PropertyChangeListener lineChangesWeak;
    
    public JSLineBreakpoint(EditorLineHandler line) {
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        FileObject fileObject = line.getFileObject();
        if( fileObject != null ){
            myWeakListener = WeakListeners.create( 
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener( myWeakListener );
        }
    }
    
    public EditorLineHandler getLineHandler() {
        return line;
    }
    
    public void setLineHandler(EditorLineHandler line) {
        dispose();
        EditorLineHandler oldLine = this.line;
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        FileObject fileObject = line.getFileObject();
        if (fileObject != null) {
            myWeakListener = WeakListeners.create(
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener(myWeakListener);
        }
        firePropertyChange(PROP_FILE, oldLine.getFileObject(), line.getFileObject());
        firePropertyChange(PROP_LINE_NUMBER, oldLine.getLineNumber(), line.getLineNumber());
    }

    /*
    public void setLine(Line line) {
        dispose();
        Line oldLine = this.line;
        this.line = line;
        lineChangesWeak = WeakListeners.propertyChange(lineChangeslistener, line);
        line.addPropertyChangeListener(lineChangesWeak);
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if (fileObject != null) {
            myWeakListener = WeakListeners.create(
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener(myWeakListener);
        }
        firePropertyChange(PROP_LINE, oldLine, line);
    }
    */
    
    /**
     * Set a 1-based line number to this breakpoint.
     * @param lineNumber the line number.
     */
    public void setLine(int lineNumber) {
        if (line.getLineNumber() == lineNumber) {
            return ;
        }
        line.setLineNumber(lineNumber);
    }
    
    /**
     * Get the 1-based line number of this breakpoint.
     * 
     * @return the line number.
     */
    public int getLineNumber() {
        return line.getLineNumber();
    }
    
    public FileObject getFileObject() {
        return line.getFileObject();
    }
    
    public URL getURL() {
        return line.getURL();
    }
    
    @Override
    public void disable() {
        if(!isEnabled) {
            return;
        }

        isEnabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }

    @Override
    public void enable() {
        if(isEnabled) {
            return;
        }

        isEnabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    @Override
    protected void dispose() {
        super.dispose();
        line.dispose();
        FileObject fileObject = line.getFileObject();
        if( fileObject != null ){
            fileObject.removeFileChangeListener( myWeakListener );
            myWeakListener = null;
        }
    }

    public final String getCondition() {
        return condition;
    }

    public final void setCondition(String condition) {
        String oldCondition = this.condition;
        if (condition != null && condition.equals(oldCondition) ||
            condition == null && oldCondition == null) {
            return ;
        }
        this.condition = condition;
        firePropertyChange(PROP_CONDITION, oldCondition, condition);
    }
    
    public final boolean isConditional() {
        return condition != null && !condition.isEmpty();
    }
    
    final void setValid(String message) {
        setValidity(VALIDITY.VALID, message);
    }

    final void setInvalid(String message) {
        setValidity(VALIDITY.INVALID, message);
    }
    
    final void resetValidity() {
        setValidity(VALIDITY.UNKNOWN, null);
    }

    @Override
    public String toString() {
        return "JSLineBreakpoint{" + "line=" + line + ", FileObject = " + getFileObject() + ", isEnabled=" + isEnabled + ", condition=" + condition + '}';
    }

    private class FileRemoveListener extends FileChangeAdapter {

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileDeleted(org.openide.filesystems.FileEvent)
         */
        @Override
        public void fileDeleted( FileEvent arg0 ) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(JSLineBreakpoint.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileObject renamedFo = fe.getFile();
            int oldLineNumber = line.getLineNumber();
            EditorLineHandler newLine = EditorLineHandlerFactory.getHandler(renamedFo, oldLineNumber);
            int newLineNumber = newLine.getLineNumber();
            JSLineBreakpoint.this.line = newLine;
            firePropertyChange(PROP_LINE_NUMBER, oldLineNumber, newLineNumber);
            firePropertyChange(PROP_FILE, fe.getName(), renamedFo.getName());
        }

    }
    
    private class LineChangesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorLineHandler.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                firePropertyChange(PROP_LINE_NUMBER, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
    
}
