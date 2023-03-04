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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.EditorPaneTesting;
import org.netbeans.lib.editor.util.random.PropertyProvider;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 *
 * @author mmetelka
 */
public final class FoldManagerTesting {

    public static final String ADD_FOLDS = "add-folds";

    public static final String REMOVE_FOLDS = "remove-folds";

    private static final int MAX_HANDLED_FOLDS_COUNT = 5;

    public static RandomTestContainer initContainer(RandomTestContainer container) {
        JEditorPane pane = EditorPaneTesting.getEditorPane(container);
        assert (pane != null) : "JEditorPane is null";

        FoldManagerFactoryProvider.setForceCustomProvider(true);
        FoldManagerFactoryProvider provider = FoldManagerFactoryProvider.getDefault();
        assert (provider instanceof CustomProvider) : "setForceCustomProvider(true) did not ensure CustomProvider use"; // NOI18N

        CustomProvider customProvider = (CustomProvider) provider;
        customProvider.removeAllFactories(); // cleanup all registered factories
        customProvider.registerFactories(pane.getEditorKit().getContentType(),
                new FoldManagerFactory[] { new TestFoldManagerFactory() });

        container.addOp(new AddFoldsOp());
        container.addOp(new RemoveFoldsOp());
        return container;
    }

    private static TestFoldManager getFoldManager(PropertyProvider provider) {
        JEditorPane pane = EditorPaneTesting.getValidEditorPane(provider);
        TestFoldManager foldManager = (TestFoldManager) pane.getClientProperty(TestFoldManager.class);
        assert (foldManager != null) : "Null TestFoldManager"; // NOI18N
        return foldManager;
    }

    private static List<Fold> getFoldList(PropertyProvider provider) {
        List<Fold> foldList = (List<Fold>) provider.getPropertyOrNull("fold-list");
        if (foldList == null) {
            foldList = new ArrayList<Fold>();
            provider.putProperty("fold-list", foldList);
        }
        return foldList;
    }

    public static void addFolds(final Context context, final Integer[] offsetPairs) throws Exception {
        JEditorPane pane = EditorPaneTesting.getValidEditorPane(context);
        FoldHierarchy.get(pane); // Ensure fold hierarchy exists and TestFoldManager gets used
        FoldHierarchyExecution.waitHierarchyInitialized(pane);
        final TestFoldManager foldManager = getFoldManager(context);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                foldManager.startTransaction();
                try {
                    for (int i = 0; i < offsetPairs.length;) {
                        int startOffset = offsetPairs[i++];
                        int endOffset = offsetPairs[i++];
                        assert (startOffset < endOffset) : "addFold: startOffset=" + startOffset + ", endOffset=" + endOffset; // NOI18N
                        Fold fold = foldManager.addFold(startOffset, endOffset);
                        assert (fold != null);
                        getFoldList(context).add(fold);
                        if (context.isLogOp()) {
                            StringBuilder sb = context.logOpBuilder();
                            sb.append(" ADD_FOLDS[").append(i).append("]: <").append(startOffset); // NOI18N
                            sb.append(",").append(endOffset).append(">"); // NOI18N
                            sb.append(" fold: ").append(fold);
                            context.logOp(sb);
                        }
                    }
                } finally {
                    foldManager.commitTransaction();
                }
            }
        });
    }

    public static void removeFolds(final Context context, final Fold... fold) throws Exception {
        removeFolds(true, context, fold);
    }

    public static void removeFolds(final boolean removeFromFoldList,
            final Context context, final Fold... fold) throws Exception
    {
        final List<Fold> foldList = getFoldList(context);
        final TestFoldManager foldManager = getFoldManager(context);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                foldManager.startTransaction();
                try {
                    for (int i = 0; i < fold.length; i++) {
                        Fold f = fold[i];
                        foldManager.removeFold(f);
                        if (removeFromFoldList) {
                            final int index = foldList.indexOf(fold);
                            if (index == -1) {
                                throw new IllegalArgumentException("Fold not present in foldList: " + fold);
                            }
                            foldList.remove(index);
                        }
                        if (context.isLogOp()) {
                            StringBuilder sb = context.logOpBuilder();
                            sb.append(" REMOVE_FOLD[").append(i).append("]: ").append(fold); // NOI18N
                            context.logOp(sb);
                        }
                    }
                } finally {
                    foldManager.commitTransaction();
                }
            }
        });
    }
    
    static final class AddFoldsOp extends RandomTestContainer.Op {

        public AddFoldsOp() {
            super(ADD_FOLDS);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = DocumentTesting.getValidDocument(context);
            int docLen = doc.getLength();
            if (docLen < 1)
                return; // Cannot add a non-empty fold
            Random random = context.container().random();
            int count = random.nextInt(MAX_HANDLED_FOLDS_COUNT) + 1;
            Integer[] offsetPairs = new Integer[count << 1];
            for (int i = 0; i < offsetPairs.length;) {
                int startOffset = (docLen > 1) ? random.nextInt(docLen - 1) : 0;
                int remainLimit = docLen - startOffset - 1;
                int length = ((remainLimit > 0) ? random.nextInt(remainLimit) : 0) + 1;
                offsetPairs[i++] = startOffset;
                offsetPairs[i++] = startOffset + length;
            }
            addFolds(context, offsetPairs);
        }

    }

    static final class RemoveFoldsOp extends RandomTestContainer.Op {

        public RemoveFoldsOp() {
            super(REMOVE_FOLDS);
        }

        @Override
        protected void run(Context context) throws Exception {
            List<Fold> foldList = getFoldList(context);
            if (foldList.isEmpty()) {
                return;
            }
            Random random = context.container().random();
            int count = Math.min(foldList.size(), random.nextInt(MAX_HANDLED_FOLDS_COUNT));
            Fold[] folds = new Fold[count];
            for (int i = 0; i < folds.length; i++) {
                int index = random.nextInt(foldList.size());
                folds[i] = foldList.remove(index);
            }
            removeFolds(false, context, folds);
        }

    }

    private static final class TestFoldManager extends AbstractFoldManager {

        AbstractDocument doc;

        FoldHierarchy hierarchy;

        FoldHierarchyTransaction transaction;

        @Override
        public void init(FoldOperation operation) {
            super.init(operation);
            operation.getHierarchy().getComponent().putClientProperty(TestFoldManager.class, this);
        }

        void startTransaction() {
            FoldOperation op = getOperation();
            hierarchy = op.getHierarchy();
            JTextComponent component = hierarchy.getComponent();
            doc = (AbstractDocument) component.getDocument();
            doc.readLock();
            hierarchy.lock();
            transaction = op.openTransaction();
        }

        void commitTransaction() {
            transaction.commit();
            transaction = null;
            hierarchy.unlock();
            hierarchy = null;
            doc.readUnlock();
            doc = null;
        }

        Fold addFold(int startOffset, int endOffset) {
            FoldOperation op = getOperation();
            assert (transaction != null) : "Transaction is null";
            try {
                return op.addToHierarchy(
                        AbstractFoldManager.REGULAR_FOLD_TYPE, "...", false,
                        startOffset, endOffset, 0, 0, null, transaction);
            } catch (BadLocationException e) {
                throw new IllegalStateException("BadLocationException thrown", e);
            }
        }

        void removeFold(Fold fold) {
            FoldOperation op = getOperation();
            assert (transaction != null) : "Transaction is null";
            op.removeFromHierarchy(fold, transaction);
        }

    }

    private static final class TestFoldManagerFactory implements FoldManagerFactory {
        
        @Override
        public FoldManager createFoldManager() {
            return new TestFoldManager();
        }
        
    }


}
