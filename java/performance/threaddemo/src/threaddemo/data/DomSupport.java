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

package threaddemo.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import threaddemo.locking.RWLock;
import threaddemo.locking.LockAction;
import threaddemo.locking.LockExceptionAction;
import threaddemo.model.Phadhail;
import threaddemo.util.ClobberException;
import threaddemo.util.DocumentParseSupport;
import threaddemo.util.DocumentParseSupportDelta;
import threaddemo.util.TwoWayEvent;
import threaddemo.util.TwoWayListener;

// XXX should maybe show stale value during delays

/**
 * Support class for DOM provider interface.
 * <p>The derived model is a {@link Document}. The deltas to the derived model
 * are the same {@link Document}s - this class does not model structural diffs
 * using the {@link TwoWaySupport} semantics.
 * @author Jesse Glick
 */
public final class DomSupport extends DocumentParseSupport<Document,Document> implements DomProvider, ErrorHandler, TwoWayListener<Document,DocumentParseSupportDelta,Document>, EntityResolver, EventListener {
    
    private static final Logger logger = Logger.getLogger(DomSupport.class.getName());
    
    private final Phadhail ph;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    private boolean inIsolatingChange = false;
    private boolean madeIsolatedChanges;
    
    public DomSupport(Phadhail ph, EditorCookie.Observable edit, RWLock lock) {
        super(edit, lock);
        this.ph = ph;
        addTwoWayListener(this);
    }
    
    public Document getDocument() throws IOException {
        return getLock().read(new LockExceptionAction<Document,IOException>() {
            public Document run() throws IOException {
                assert !inIsolatingChange;
                try {
                    Document v = getValueBlocking();
                    logger.log(Level.FINER, "getDocument: {0}", v);
                    return v;
                } catch (InvocationTargetException e) {
                    throw (IOException) e.getCause();
                }
            }
        });
    }
    
    public void setDocument(final Document d) throws IOException {
        if (d == null) throw new NullPointerException();
        getLock().write(new LockExceptionAction<Void,IOException>() {
            public Void run() throws IOException {
                assert !inIsolatingChange;
                Document old = (Document)getStaleValueNonBlocking();
                if (old != null && old != d) {
                    ((EventTarget)old).removeEventListener("DOMSubtreeModified", DomSupport.this, false);
                    ((EventTarget)d).addEventListener("DOMSubtreeModified", DomSupport.this, false);
                }
                try {
                    mutate(d);
                    return null;
                } catch (InvocationTargetException e) {
                    throw (IOException) e.getCause();
                } catch (ClobberException e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                }
            }
        });
    }
    
    public boolean isReady() {
        return getLock().read(new LockAction<Boolean>() {
            public Boolean run() {
                assert !inIsolatingChange;
                return getValueNonBlocking() != null;
            }
        });
    }
    
    public void start() {
        initiate();
    }
    
    public RWLock lock() {
        return getLock();
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange() {
        final List<ChangeListener> ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                logger.log(Level.FINER, "DomSupport change with no listeners: {0}", ph);
                return;
            }
            ls = new ArrayList<ChangeListener>(listeners);
        }
        final ChangeEvent ev = new ChangeEvent(this);
        getLock().read(new Runnable() {
            public void run() {
                assert !inIsolatingChange;
                logger.log(Level.FINER, "DomSupport change: {0}", ph);
                for (ChangeListener l : ls) {
                    l.stateChanged(ev);
                }
            }
        });
    }
    
    protected boolean requiresUnmodifiedDocument() {
        return false;
    }
    
    protected final DerivationResult<Document,Document> doDerive(StyledDocument document, List<DocumentEvent> documentEvents, Document oldValue) throws IOException {
        assert !inIsolatingChange;
        // ignoring documentEvents
        logger.log(Level.FINER, "DomSupport doDerive: {0}", ph);
        if (oldValue != null) {
            ((EventTarget)oldValue).removeEventListener("DOMSubtreeModified", this, false);
        }
        InputSource source;
        if (document != null) {
            String text;
            try {
                text = document.getText(0, document.getLength());
            } catch (BadLocationException e) {
                assert false : e;
                text = "";
            }
            source = new InputSource(new StringReader(text));
        } else {
            // From disk.
            source = new InputSource(ph.getInputStream());
        }
        Document newValue;
        try {
            newValue = XMLUtil.parse(source, false, false, this, this);
        } catch (SAXException e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
        ((EventTarget)newValue).addEventListener("DOMSubtreeModified", this, false);
        // This impl does not compute structural diffs, so newValue == derivedDelta when modified:
        return new DerivationResult<Document,Document>(newValue, oldValue != null ? newValue : null);
    }
    
    protected final Document doRecreate(StyledDocument document, Document oldValue, Document newDom) throws IOException {
        assert !inIsolatingChange;
        logger.log(Level.FINER, "DomSupport doRecreate: {0}", ph);
        // ignoring oldValue, returning same newDom
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XMLUtil.write(newDom, baos, "UTF-8");
        } catch (IOException ioe) {
            assert false : ioe;
            throw ioe;
        }
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            assert false : e;
        }
        try {
            document.insertString(0, baos.toString("UTF-8"), null);
        } catch (UnsupportedEncodingException e) {
            assert false : e;
            throw e;
        } catch (BadLocationException e) {
            assert false : e;
        }
        return newDom;
    }
    
    protected long delay() {
        return 5000L;
    }
    
    public String toString() {
        return "DomSupport[" + ph + "]";
    }
    
    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        // Ignore external entities.
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    public void broken(TwoWayEvent.Broken evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        fireChange();
    }
    
    public void clobbered(TwoWayEvent.Clobbered evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        assert false;
    }
    
    public void derived(TwoWayEvent.Derived evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        fireChange();
    }
    
    public void forgotten(TwoWayEvent.Forgotten evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        assert false;
    }
    
    public void invalidated(TwoWayEvent.Invalidated evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        // just wait...
        initiate();
    }
    
    public void recreated(TwoWayEvent.Recreated evt) {
        logger.log(Level.FINER, "Received: {0}", evt);
        fireChange();
    }
    
    public void handleEvent(final Event evt) {
        try {
            getLock().write(new Runnable() {
                public void run() {
                    Document d = (Document)evt.getCurrentTarget();
                    Document old = (Document)getValueNonBlocking();
                    assert old == null || old == d;
                    logger.log(Level.FINEST, "DomSupport got DOM event {0} on {1}, inIsolatingChange={2}", new Object[] {evt, ph, inIsolatingChange ? Boolean.TRUE : Boolean.FALSE});
                    if (!inIsolatingChange) {
                        try {
                            setDocument(d);
                        } catch (IOException e) {
                            assert false : e;
                        }
                    } else {
                        madeIsolatedChanges = true;
                    }
                }
            });
        } catch (RuntimeException e) {
            // Xerces ignores them.
            e.printStackTrace();
        }
    }
    
    public void isolatingChange(Runnable r) {
        assert getLock().canWrite();
        assert !inIsolatingChange;
        madeIsolatedChanges = false;
        inIsolatingChange = true;
        try {
            r.run();
        } finally {
            inIsolatingChange = false;
            logger.log(Level.FINER, "Finished isolatingChange on {0}; madeIsolatedChanges={1}", new Object[] {ph, madeIsolatedChanges ? Boolean.TRUE : Boolean.FALSE});
            if (madeIsolatedChanges) {
                Document d = getValueNonBlocking();
                if (d != null) {
                    try {
                        setDocument(d);
                    } catch (IOException e) {
                        assert false : e;
                    }
                } else {
                    // ???
                    fireChange();
                }
            }
        }
    }
    
}
