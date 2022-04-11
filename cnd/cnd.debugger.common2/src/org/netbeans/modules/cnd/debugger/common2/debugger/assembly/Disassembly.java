/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.impl.DisassemblyFileEncodingQueryImplementation;
import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.support.ReadOnlySupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 */
public abstract class Disassembly implements StateModel.Listener {
    private static Logger LOG = Logger.getLogger(Disassembly.class.getName());

    private final NativeDebuggerImpl debugger;
    protected volatile static boolean opened = false;
    private volatile static boolean opening = false;
    private static final List<DebuggerAnnotation> bptAnnotations = new ArrayList<DebuggerAnnotation>();
    private final BreakpointModel breakpointModel;
    private int disLength = 0;
    private DisText disText;
    public static final String REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY = "org.netbeans.modules.cnd.debugger.common.RegistersWindow.format"; // NOI18N    
    public static enum DATA_REPRESENTATION {binary, octal,decimal, hexadecimal, natural};
    protected static enum RequestMode {FILE_SRC, FILE_NO_SRC, ADDRESS_SRC, ADDRESS_NO_SRC, NONE};
    protected RequestMode requestMode = RequestMode.FILE_SRC;
    public static final Preferences PREFS = NbPreferences.forModule(NativeDebuggerManager.class);
    
    
    private final BreakpointModel.Listener breakpointListener =
	new BreakpointModel.Listener() {
            @Override
	    public void bptUpdated() {
                if (opened) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateAnnotations(false);
                        }
                    });
                }
	    }
	};

    protected Disassembly(NativeDebuggerImpl debugger, BreakpointModel breakpointModel) {
        this.debugger = debugger;
        this.breakpointModel = breakpointModel;
        breakpointModel.addListener(breakpointListener);
    }

    protected NativeDebuggerImpl getDebugger() {
        return debugger;
    }
    
    private void updateAnnotations(boolean andShow) {
        debugger.annotateDis(andShow);
        for (DebuggerAnnotation annotation : bptAnnotations) {
            annotation.detach();
        }
        bptAnnotations.clear();
        
        NativeBreakpoint[] bs = breakpointModel.getBreakpoints();
        for (NativeBreakpoint bpt : bs) {
            if (bpt instanceof InstructionBreakpoint) {
                InstructionBreakpoint ibpt = (InstructionBreakpoint)bpt;
                try {
                    // breakpoint has an annotation already
                    DebuggerAnnotation[] annotations = ibpt.annotations();
                    if (annotations.length == 0) {
                        continue;
                    }
                    int addressLine = getAddressLine(annotations[0].getAddr());
                    if (addressLine >= 0) {
                        Line line = getLine(addressLine);
                        if (line != null) {
                            bptAnnotations.add(new DebuggerAnnotation(null, ibpt.getAnnotationType(), line, 0, true, ibpt));
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
     static Disassembly getCurrent() {
        NativeDebugger currentDebugger = org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager.get().currentDebugger();
        if (currentDebugger != null) {
            return currentDebugger.getDisassembly();
        }
        return null;
    }
    
    public static boolean isInDisasm() {
        if (opened) {
            //TODO: optimize
            FileObject fobj = EditorContextDispatcher.getDefault().getCurrentFile();
            if (fobj == null) {
                fobj = EditorContextDispatcher.getDefault().getMostRecentFile();
            }
            if (fobj != null) {
                try {
                    return DataObject.find(fobj).equals(getDataObject());
                } catch(DataObjectNotFoundException doe) {
                    Exceptions.printStackTrace(doe);
                }
            }
        }
        return false;
    }
    
    public static boolean isDisasm(String url) {
        //TODO: optimize
        try {
            return getFileObject().getURL().toString().equals(url);
        } catch (FileStateInvalidException fsi) {
            Exceptions.printStackTrace(fsi);
        }
        return false;
    }
    
    public static void open() {
        try {
            ReadOnlySupport ro = getDataObject().getLookup().lookup(ReadOnlySupport.class);
            if (ro != null) {
                ro.setReadOnly(true);
            }
            getDataObject().getNodeDelegate().setDisplayName(NbBundle.getMessage(Disassembly.class, "LBL_Disassembly_Window")); // NOI18N
            final EditorCookie editorCookie = getDataObject().getCookie(EditorCookie.class);
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable)editorCookie).addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                            if (editorCookie.getOpenedPanes() == null) {
                                opened = false;
                                ((EditorCookie.Observable)editorCookie).removePropertyChangeListener(this);
                            }
                        }
                    }
                });
            }
            OpenCookie oc = getDataObject().getLookup().lookup(OpenCookie.class);
            final boolean hasOpenCookie = oc != null;
            if (hasOpenCookie) {
                oc.open();
            }
            opening = hasOpenCookie;
            opened = hasOpenCookie;
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.debugger.registerDisassembly(dis);
                dis.reload();
            } else {
                opening = false;
                opened = false;
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    protected final void reloadFailed() {
        opening = false;
    }

    protected abstract void reload();
    
    public static void close() {
        try {
            final DataObject dataObject = getDataObject();
            if (dataObject != null) {
                dataObject.getCookie(CloseCookie.class).close();
            }
            opened = false;
            // TODO: check for correct close on debug close
            Disassembly dis = getCurrent();
            if (dis != null) {
                dis.debugger.registerDisassembly(null);
            }
        } catch (Exception e) {
                        // #238339
            if (e instanceof NullPointerException) {
                DataObject dObj = getDataObject();
                LOG.log(Level.INFO, "dObj={0}; cookie={1}", new Object[]{dObj, dObj == null ? null : dObj.getCookie(CloseCookie.class)});
            }
            Exceptions.printStackTrace(e);
        }
    }
    
    public static boolean isOpened() {
        return opened;
    }
    
    public static FileObject getFileObject() {
        return FileObjectHolder.FOBJ;
    }
    
    private static class FileObjectHolder {
        static final FileObject FOBJ = createFileObject();
        
        private static FileObject createFileObject() {
            try {
                return FileUtil.createMemoryFileSystem().getRoot().createData("disasm", "s"); // NOI18N
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            return null;
        }
    }
        
    protected static DataObject getDataObject() {
        return DataObjectHolder.DOBJ;
    }
    
    private static class DataObjectHolder {
        static final DataObject DOBJ = createDataObject();
        
        private static DataObject createDataObject() {
            try {
                return DataObject.find(getFileObject());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    
    static Line getLine(int lineNo) throws Exception {
        try {
            return EditorBridge.lineNumberToLine(getDataObject(), lineNo);
        } catch (IndexOutOfBoundsException e) {
            // do nothing
        }
        return null;
    }
    
    public static final DATA_REPRESENTATION getCurrentDataRepresentationFormat() {
        return DATA_REPRESENTATION.valueOf(PREFS.get(REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY, DATA_REPRESENTATION.hexadecimal.toString()));
    }
    
    protected int getAddressLine(String address) {
        return getAddressLine(Address.parseAddr(address));
        // had problems with string comparison: 0x01 is not equal to 0x1
        //TODO : can use binary search
//        synchronized (lines) {
//            for (Line line : lines) {
//                if (line.address.equals(address)) {
//                    return line.idx;
//                }
//            }
//        }
//        return -1;
    }
    
    protected int getAddressLine(long address) {
        if (disText != null) {
            final List<DisLine> lines = disText.lines;
            //TODO : can use binary search
            synchronized (lines) {
                for (DisLine line : lines) {
                    try {
                        if (Address.parseAddr(line.getAddress()) == address) {
                            return line.getIdx();
                        }
                    } catch (NumberFormatException e) {
                        //do nothing
                    }
                }
            }
        }
        return -1;
    }
    
    String getLineAddress(int idx) {
        if (disText != null) {
            final List<DisLine> lines = disText.lines;
            //TODO : can use binary search
            synchronized (lines) {
                for (DisLine line : lines) {
                    if (line.getIdx() == idx) {
                        return line.getAddress();
                    }
                }
            }
        }
        return null;
    }
    
    protected static interface DisLine {
        String getAddress();
        int getIdx();
        void setIdx(int idx);
    }
    
    protected static class CommentLine implements DisLine {
        private final String text;
        
        public CommentLine(String text) {
            this.text = text;
        }

        @Override
        public String getAddress() {
            return ""; //NOI18N
        }

        @Override
        public int getIdx() {
            return -1;
        }

        @Override
        public void setIdx(int idx) {}

        @Override
        public String toString() {
            return text;
        }
    }
    
    protected void attachUpdateListener() {
        DataObject dobj = getDataObject();
        final CloneableEditorSupport cEditorSupport = dobj.getLookup().lookup(CloneableEditorSupport.class);
        if (cEditorSupport == null) {
            return;
        }
        Document doc = cEditorSupport.getDocument();
        if (doc != null) {
            doc.removeDocumentListener(updateListener);
            doc.addDocumentListener(updateListener);
        }
    }
    
    private final DocumentListener updateListener = new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            // update anything on full load only
            if (e.getOffset() + e.getLength() >= disLength) {
                final boolean dis = opening;
                opening = false;
                if (opened) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateAnnotations(dis);
                        }
                    });
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
        }
    };
    
    protected final void setText(DisText text) {
        this.disText = text;
    }
    
    public void reset() {
        DisText emptyText = new DisText();
        emptyText.save();
        setText(emptyText);
    }
    
    protected class DisText {
        private final List<DisLine> lines = new ArrayList<DisLine>();
        private final StringBuilder data = new StringBuilder();

        public DisText() {
        }

        public int size() {
            return lines.size();
        }
        
        public int getLength() {
            return data.length();
        }
        
        public void addLine(DisLine line) {
            lines.add(line);
            line.setIdx(lines.size());
            data.append(line.toString());
        }
        
        public void save() {
            disLength = getLength();
            try {
                Writer writer = new OutputStreamWriter(
                                        getFileObject().getOutputStream(),
                                        DisassemblyFileEncodingQueryImplementation.CHARSET
                                    );
                try {
                    writer.write(data.toString());
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                } finally {
                    writer.close();
                }
            } catch (IOException ex) {
                //do nothing
                LOG.log(Level.INFO, null, ex);
            }
        }
        
        public boolean isEmpty() {
            return lines.isEmpty();
        }
    }
}
