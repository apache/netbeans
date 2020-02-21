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

package org.netbeans.modules.cnd.asm.core.editor;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor.ParseListener;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.lang.LabelElement;
import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.AsmSyntax;
import org.netbeans.modules.cnd.asm.model.lang.BranchElement;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmParser;
import org.netbeans.modules.cnd.asm.model.util.AsmModelUtilities;
import org.netbeans.modules.cnd.asm.model.util.EmptyAsmState;
import org.openide.util.Pair;

public class AsmModelAccessorImpl implements AsmModelAccessor {            
    
    private static final Logger LOGGER = 
            Logger.getLogger(AsmModelAccessorImpl.class.getName());
    
    private static final RequestProcessor reqProc = 
            new RequestProcessor ("asm parser",1); // NOI18N
    
    private static final int INPUT_REACTION_DELAY = 500;
            
    private AsmState lastState;   
    private final Object stateLock;
    
    private final AbstractAsmModel model;
    private final AsmSyntax synt;
    
    private final Document doc;
    private final String docName;    
    
    private final AsmDocumentListener docListener;
               
    private final RequestProcessor.Task reparseTask;
      
    private final List<ParseListener> listeners;
       
    public AsmModelAccessorImpl(AsmModel model, AsmSyntax synt, 
                                Document doc) {        
        
        this.model = (AbstractAsmModel) model;
        this.synt = synt;           
        this.doc = doc;
        
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        docName = fo != null ? fo.getName() : "";
        
        reparseTask = reqProc.create(new ReparseTask(), true);
        listeners = new LinkedList<ParseListener>();
        notifyChange(true);
        
        docListener = new AsmDocumentListener(doc);
        stateLock = new Object();
        
        lastState = new EmptyAsmState();
    }

    public AsmState getState() {
        synchronized(stateLock) {            
            return lastState;
        }
    }
                 
    public void addParseListener(AsmModelAccessor.ParseListener list) {
        synchronized(listeners) {
            listeners.add(list);
        }
    }
            
    public void removeParseListener(AsmModelAccessor.ParseListener list) {
        synchronized(listeners) {
            listeners.remove(list);
        }
    }
    
    private void notifyChange(boolean immediate) {
        
        int delay = immediate ? 0 : INPUT_REACTION_DELAY;                        
        
        reparseTask.schedule(delay);               
    }        
    
    private void fireParsed() {
        synchronized(listeners) {
            for (ParseListener l: listeners) {
                l.notifyParsed();
            }
        }
    }        
    
    private class ReparseTask implements Runnable {
                                                                
        public void run() {
            
            long start = System.currentTimeMillis();
            
            AsmParser parser = synt.createParser();
            AsmElement res = parser.parse(     
                        new StringReader(AsmObjectUtilities.getText(doc)) );
            
            long end = System.currentTimeMillis();
            
            
            {
                LOGGER.log(Level.FINE, 
                           String.format("%s parse time: %d ms", docName, (end - start)) // NOI18N
                          );
            }
                        
            if (!Thread.currentThread().isInterrupted()) {                
                synchronized(stateLock) {
                    lastState = new X86AsmState(res, parser.getServices());               
                }
                
                fireParsed();
            }
        }        
    }
        
    
    private class X86AsmState implements AsmState {
       
        private final AsmElement elements;
        private final Lookup lookup;
                        
        public X86AsmState(AsmElement elements, Lookup lookup) {
            this.elements = elements;                        
            this.lookup = lookup;
        }
        
        public Lookup getServices() {
            return lookup;
        }
        
        public AsmElement getElements() {
            return elements;
        }

        public boolean isActual() {
            return lastState == this;
        }
        
        public Pair<AsmElement, AsmElement> resolveLink(int pos) {
            AsmElement inital = AsmModelUtilities.findAtRecursive(elements, pos);
            
            if (inital instanceof BranchElement) {
                final String name = ((BranchElement) inital).getName();  
                LabelLinkResolver resolver = new LabelLinkResolver(name);
                AsmModelUtilities.walkCompound(elements, resolver);
                
                AsmElement result = resolver.getResult();
                
                if (result != null) {
                    return Pair.<AsmElement, AsmElement>of(inital, result);
                }
            }
                
            return null;            
        }
                          
        
        private class LabelLinkResolver implements  AsmModelUtilities.AsmVisitor {
            
            private AsmElement result;
            private final String name;
            
            public LabelLinkResolver(String name) {
                this.name = name;
            }
            
            public boolean visit(AsmElement comp) {
                if (comp instanceof LabelElement) {
                    LabelElement t_label = (LabelElement) comp;
                    if (t_label.getName().equals(name)) {
                        result = t_label;
                        return false;
                    } 
                }
                return true;
            }  
            
            public AsmElement getResult() {
                return result;
            }
        }      
    }
        
    ////////////////////////

    private class AsmDocumentListener implements DocumentListener {
        
        public AsmDocumentListener(Document doc) {
            doc.addDocumentListener(WeakListeners.document(this, doc));
        }
        
        public void insertUpdate(DocumentEvent e) {
            notifyChange(false);
        }

        public void removeUpdate(DocumentEvent e) {
            notifyChange(false);        
        }

        public void changedUpdate(DocumentEvent e) {
            // nothing
        }
    }
    
    
    
}
