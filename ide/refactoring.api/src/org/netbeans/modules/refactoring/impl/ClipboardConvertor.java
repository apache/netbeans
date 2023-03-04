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
package org.netbeans.modules.refactoring.impl;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.spi.impl.Util;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard.Convertor;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Jan Becicka
 */

@org.openide.util.lookup.ServiceProvider(service=org.openide.util.datatransfer.ExClipboard.Convertor.class)
public class ClipboardConvertor implements Convertor {
    
    @Override
    public Transferable convert(Transferable t) {
        Node[] nodes = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_CUT);
        
        if (nodes!=null && nodes.length>0) {
            //try to do move refactoring
            InstanceContent ic = new InstanceContent();
            for (Node n:nodes) {
                ic.add((n));
            }
            ExplorerContext td = new ExplorerContext();
            ic.add(td);
            Lookup l = new AbstractLookup(ic);
            if (ActionsImplementationFactory.canMove(l)) {
                Action move = RefactoringActionsFactory.moveAction().createContextAwareInstance(l);
                ExTransferable tr = ExTransferable.create(t);
                tr.put(NodeTransfer.createPaste(new RefactoringPaste(t, ic, move, td)));
                return tr;
            }
        }
        nodes = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_COPY);
        if (nodes!=null && nodes.length>0) {
            //try to do copy refactoring
            InstanceContent ic = new InstanceContent();
            for (Node n:nodes) {
                ic.add((n));
            }
            ExplorerContext td = new ExplorerContext();
            ic.add(td);
            Lookup l = new AbstractLookup(ic);
            if (ActionsImplementationFactory.canCopy(l)) {
                Action copy = RefactoringActionsFactory.copyAction().createContextAwareInstance(l);
                ExTransferable tr = ExTransferable.create(t);
                tr.put(NodeTransfer.createPaste(new RefactoringPaste(t, ic, copy, td)));
                return tr;
            }
        }
        return t;
    }
    
    private class RefactoringPaste implements NodeTransfer.Paste {
        
        private Transferable delegate;
        private InstanceContent ic;
        private Action refactor;
        private ExplorerContext d;
        RefactoringPaste(Transferable t, InstanceContent ic, Action refactor, ExplorerContext d) {
            delegate = t;
            this.ic = ic;
            this.refactor = refactor;
            this.d=d;
        }
        
        @Override
        public PasteType[] types(Node target) {
            RefactoringPasteType refactoringPaste = new RefactoringPasteType(delegate, target);
            if (refactoringPaste.canHandle())
                return new PasteType[] {refactoringPaste};
            return new PasteType[0];
        }
        
        private class RefactoringPasteType extends PasteType {
            RefactoringPasteType(Transferable orig, Node target) {
                d.setTargetNode(target);
                d.setTransferable(orig);
            }
            
            public boolean canHandle() {
                if (refactor==null)
                    return false;
                return (Boolean) refactor.getValue("applicable"); //NOI18N
            }
            @Override
            public Transferable paste() throws IOException {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (refactor!=null) {
                            refactor.actionPerformed(null);
                        }
                    };
                });
                return null;
            }
            @Override
            public String getName() {
                return NbBundle.getMessage(Util.class,"Actions/Refactoring") + " " + refactor.getValue(Action.NAME);
            }
        }
    }
}
