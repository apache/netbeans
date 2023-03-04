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

package org.netbeans.modules.maven.output;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;


/**
 * compile error editor annotation
 * @author  Milos Kleint 
 */
public final class CompileAnnotation /*extends Annotation */implements /*PropertyChangeListener,*/ OutputListener {
    
    File clazzfile; //for tests..
    private int lineNum;
    private final String text;
    
    private static final RequestProcessor RP = new RequestProcessor(CompileAnnotation.class);
    
    public CompileAnnotation(File clazz, String line, String textAnn) {
        clazzfile = clazz;
        text = textAnn;
        try {
            lineNum = Integer.parseInt(line);
        } catch (NumberFormatException exc) {
            lineNum = -1;
        }
    }
    
    
    
    @Override
    public void outputLineSelected(OutputEvent ev) {
        //           cookie.getLineSet().getCurrent(line).show(Line.SHOW_SHOW);
    }
    
    /** Called when some sort of action is performed on a line.
     * @param ev the event describing the line
     */
    @Override
    public void outputLineAction(OutputEvent ev) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(clazzfile);
                FileObject file = FileUtil.toFileObject(clazzfile);
                if (file == null) {
                    beep();                   
                    return;
                }
                try {
                    DataObject dob = DataObject.find(file);
                    final EditorCookie ed = dob.getLookup().lookup(EditorCookie.class);
                    if (ed != null && file == dob.getPrimaryFile()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (lineNum == -1) {
                                        ed.open();
                                    } else {
                                        ed.openDocument();
                                        try {
                                            Line l = ed.getLineSet().getOriginal(lineNum - 1);
                                            if (!l.isDeleted()) {
                                                l.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
                                            }
                                        } catch (IndexOutOfBoundsException ioobe) {
                                            // Probably harmless. Bogus line number.
                                            ed.open();
                                        }
                                    }
                                } catch (IOException ioe) {
                                    ErrorManager.getDefault().notify(ioe);
                                }
                            }
                        });

                        //                attachAllInFile(ed, this);
                    } else {
                        beep();
                    }
                } catch (DataObjectNotFoundException donfe) {
                    ErrorManager.getDefault().notify(donfe);
                }
            }
        });
        
    }

    private void beep() {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }
    
//    private static void attachAllInFile(EditorCookie cook, CompileAnnotation annot) {
//        Set<CompileAnnotation> newSet = null;
//        synchronized (hyperlinks) {
//            newSet = new HashSet<CompileAnnotation>(hyperlinks);
//        }
//        Iterator it = newSet.iterator();
//        while (it.hasNext()) {
//            CompileAnnotation ann = (CompileAnnotation)it.next();
//            if (ann.getFile().equals(annot.getFile())) {
//                if (ann.getLine() != -1) {
//                    Line l = cook.getLineSet().getOriginal(ann.getLine() - 1);
//                    if (! l.isDeleted()) {
//                        ann.attachAsNeeded(l);
//                    }
//                }
//
//            }
//        }
//    }
    
    /** Called when a line is cleared from the buffer of known lines.
     * @param ev the event describing the line
     */
    @Override
    public void outputLineCleared(OutputEvent ev) {
//        doDetach();
    }
    
//    void destroy() {
//        doDetach();
//        dead = true;
//    }
//
//    private synchronized void attachAsNeeded(Line l) {
//        if (getAttachedAnnotatable() == null) {
//            Annotatable ann = l;
//            attach(ann);
//            ann.addPropertyChangeListener(this);
//        }
//    }
//
//
//    private synchronized void doDetach() {
//        Annotatable ann = getAttachedAnnotatable();
//        if (ann != null) {
//            ann.removePropertyChangeListener(this);
//            detach();
//        }
//        synchronized (hyperlinks) {
//            hyperlinks.remove(this);
//        }
//    }
//
//    public void propertyChange(PropertyChangeEvent ev) {
//        if (dead) {
//            return;
//        }
//        String prop = ev.getPropertyName();
//        if (    prop == null
//             || prop.equals(Annotatable.PROP_TEXT)
//             || prop.equals(Annotatable.PROP_DELETED)) {
//            doDetach();
//        }
//    }
//
//    public String getAnnotationType() {
//        return "org-codehaus-mevenide-netbeans-project-error"; // NOI18N
//    }
//
//    public String getShortDescription() {
//        return text;
//    }
//
//    public File getFile() {
//        return clazzfile;
//    }
//
//    public int getLine() {
//        return lineNum;
//    }
    
    @Override
    public String toString() {
        return "error[" + clazzfile + ":" + lineNum + ":" + text + "]"; // NOI18N
    }
    
}
