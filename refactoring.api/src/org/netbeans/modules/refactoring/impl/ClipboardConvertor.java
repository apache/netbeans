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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
