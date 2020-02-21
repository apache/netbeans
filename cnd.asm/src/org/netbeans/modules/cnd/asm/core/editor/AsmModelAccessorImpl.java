/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
