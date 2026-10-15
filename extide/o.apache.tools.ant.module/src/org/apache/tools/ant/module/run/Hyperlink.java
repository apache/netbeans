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

package org.apache.tools.ant.module.run;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.module.AntModule;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Represents a linkable line (appears in red in Output Window).
 * Line and column numbers start at 1, and -1 means an unknown value.
 * Careful since org.openide.text seems to assume 0-based line and column numbers.
 * @author Jesse Glick
 */
public final class Hyperlink implements OutputListener {

    static final Set<Hyperlink> hyperlinks = Collections.newSetFromMap(new WeakHashMap<>());
    private static final RequestProcessor RP = new RequestProcessor(Hyperlink.class);

    private final URL url;
    private final String message;
    private final int line1;
    private int col1;
    private final int line2;
    private final int col2;
    private Line liveLine;
    
    /** error manager for Hyperlink logging and error reporting */
    private static final Logger ERR = Logger.getLogger(
            "org.apache.tools.ant.module.run.Hyperlink"); // NOI18N
    
    public Hyperlink(URL url, String message, int line1, int col1, int line2, int col2) {
        this.url = url;
        this.message = message;
        this.line1 = line1;
        this.col1 = col1;
        this.line2 = line2;
        this.col2 = col2;
        synchronized (hyperlinks) {
            hyperlinks.add(this);
        }
    }
    
    /**
     * Enables the column number of the hyperlink to be changed after the fact.
     * If it is already set, this is ignored.
     */
    public void setColumn1(int col1) {
        if (this.col1 == -1) {
            this.col1 = col1;
        }
    }
    
    public void outputLineAction(OutputEvent ev) {
        RP.post(new Runnable() {
            public @Override void run() {
        FileObject file = URLMapper.findFileObject(url);
        if (file == null) { // #13115
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getLookup().lookup(EditorCookie.class);
            if (ed != null && /* not true e.g. for *_ja.properties */file == dob.getPrimaryFile()) {
                if (line1 == -1) {
                    // OK, just open it.
                    ed.open();
                } else {
                    // Fix for IZ#97727 - warning dialogue for opening large files is meaningless if opened via a hyperlink
                    try {
                        ed.openDocument(); // XXX getLineSet does not do it for you!
                    }
                    catch (UserQuestionException exc ){
                        if ( !askUserAndDoOpen( exc , ed) ){
                            return;
                        }
                    }
                    AntModule.err.log("opened document for " + file);
                    try {
                        final Line line = updateLines(ed);
                        if (!line.isDeleted()) {
                            EventQueue.invokeLater(new Runnable() {
                                public @Override void run() {
                                    line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, col1 == -1 ? -1 : col1 - 1);
                                }
                            });
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // Probably harmless. Bogus line number.
                        ed.open();
                    }
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
        }
        catch (IOException ioe) {
            // XXX see above, should not be necessary to call openDocument at all
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
        if (message != null) {
            // Try to do after opening the file, since opening a new file
            // clears the current status message.
            StatusDisplayer.getDefault().setStatusText(message);
        }
            }
        });
    }
    
    // Fix for IZ#97727 - warning dialogue for opening large files is meaningless if opened via a hyperlink
    private boolean askUserAndDoOpen( UserQuestionException e , 
            EditorCookie cookie) 
    {
        while (e != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(e
                    .getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION);
            nd.setOptions(new Object[] { NotifyDescriptor.YES_OPTION,
                    NotifyDescriptor.NO_OPTION });

            Object res = DialogDisplayer.getDefault().notify(nd);

            if (NotifyDescriptor.OK_OPTION.equals(res)) {
                try {
                    e.confirmed();
                }
                catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);

                    return true;
                }
            }
            else {
                return false;
            }

            e = null;

            try {
                cookie.openDocument();
            }
            catch (UserQuestionException ex) {
                e = ex;
            }
            catch (IOException ex) {
                ERR.log(Level.INFO, null, ex);
            }
            catch (Exception ex) {
                ERR.log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }


    /**
     * #62623: record positions in document at time first hyperlink was clicked for this file.
     * Otherwise an intervening save action can mess up line numbers.
     */
    private Line updateLines(EditorCookie ed) {
        Line.Set lineset = ed.getLineSet();
        synchronized (hyperlinks) {
            assert line1 != -1;
            boolean ran = false;
            boolean encounteredThis = false;
            boolean modifiedThis = false;
            if (liveLine == null) {
                ran = true;
                for (Hyperlink h : hyperlinks) {
                    if (h == this) {
                        encounteredThis = true;
                    }
                    if (h.liveLine == null && h.url.equals(url) && h.line1 != -1) {
                        Line l = lineset.getOriginal(h.line1 - 1);
                        assert l != null : h;
                        h.liveLine = l;
                        if (h == this) {
                            modifiedThis = true;
                        }
                    }
                }
            }
            assert liveLine != null : "this=" + this + " ran=" + ran +
                    " encounteredThis=" + encounteredThis + " modifiedThis=" + modifiedThis +
                    " hyperlinks=" + hyperlinks + " hyperlinks.contains(this)=" + hyperlinks.contains(this);
            return liveLine;
        }
    }
    
    public void outputLineSelected(OutputEvent ev) {
        RP.post(new Runnable() {
            public @Override void run() {
        FileObject file = URLMapper.findFileObject(url);
        if (file == null) {
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getLookup().lookup(EditorCookie.class);
            if (ed != null) {
                if (ed.getDocument() == null) {
                    // The document is not opened, don't bother with it.
                    // The Line.Set will be corrupt anyway, currently.
                    AntModule.err.log("no document for " + file);
                    return;
                }
                AntModule.err.log("got document for " + file);
                if (line1 != -1) {
                    final Line line = updateLines(ed);
                    if (!line.isDeleted()) {
                        EventQueue.invokeLater(new Runnable() {
                            public @Override void run() {
                                line.show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE, col1 == -1 ? -1 : col1 - 1);
                            }
                        });
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
        } catch (IndexOutOfBoundsException iobe) {
            // Probably harmless. Bogus line number.
        }
            }
        });
    }
    
    public void outputLineCleared(OutputEvent ev) {
        synchronized (hyperlinks) {
            liveLine = null;
        }
    }
    
    @Override
    public String toString() {
        return "Hyperlink[" + url + ":" + line1 + ":" + col1 + ":" + line2 + ":" + col2 + "]"; // NOI18N
    }
    
}
