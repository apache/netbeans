/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.git.ui.diff;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.GitVCS;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.spi.diff.DiffControllerProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Real-controller ordering regressions, with gates rather than sleeps/polling. */
public class DiffLifecycleTest extends AbstractGitTestCase {
    private MultiDiffPanelController controller;
    private RequestProcessor rp;
    private PublicationQueue publicationQueue;
    private boolean controllerClosed;
    private FileInformation.Mode previousMode;
    private final CountDownLatch releaseWorker = new CountDownLatch(1);
    private final CountDownLatch releaseReplacement = new CountDownLatch(1);

    public DiffLifecycleTest(String name) { super(name); }

    @Override protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(GitVCS.class, ControlledDiffProvider.class);
        previousMode = GitModuleConfig.getDefault().getLastUsedModificationContext();
        GitModuleConfig.getDefault().setLastUsedModificationContext(FileInformation.Mode.HEAD_VS_WORKING_TREE);
        ControlledDiffProvider.entered = null;
        ControlledDiffProvider.release = null;
        rp = (RequestProcessor) field(MultiDiffPanelController.class, "RP").get(null);
        drainWorker();
    }

    @Override protected void tearDown() throws Exception {
        releaseWorker.countDown();
        releaseReplacement.countDown();
        if (ControlledDiffProvider.release != null) ControlledDiffProvider.release.countDown();
        try {
            onEdt(() -> {
                closeController();
                if (publicationQueue != null) publicationQueue.remove();
            });
            drainWorker();
            onEdt(() -> {});
        } finally {
            GitModuleConfig.getDefault().setLastUsedModificationContext(previousMode);
            super.tearDown();
        }
    }

    private File unstagedFile() throws Exception {
        File file = new File(repositoryLocation, "unstaged.txt");
        write(file, "committed\n");
        add(file); commit(file);
        write(file, "working tree\n");
        getCache().refreshAllRoots(new File[] {file});
        FileInformation status = getCache().getStatus(file);
        assertTrue(status.containsStatus(FileInformation.Status.MODIFIED_HEAD_WORKING_TREE));
        assertFalse(status.containsStatus(FileInformation.Status.MODIFIED_HEAD_INDEX));
        return file;
    }

    private void preparePausedRefresh(File file) throws Exception {
        CountDownLatch occupied = new CountDownLatch(1);
        rp.post(() -> {
            occupied.countDown();
            await(releaseWorker);
        });
        await(occupied);
        onEdt(() -> {
            controller = new MultiDiffPanelController(VCSContext.forNodes(new Node[] {
                    new AbstractNode(Children.LEAF, Lookups.fixed(file))}), Revision.HEAD, Revision.LOCAL);
            publicationQueue = new PublicationQueue(rp);
            Toolkit.getDefaultToolkit().getSystemEventQueue().push(publicationQueue);
        });
    }

    public void testRefreshCannotPublishAfterWindowClosed() throws Exception {
        preparePausedRefresh(unstagedFile());
        onEdt(() -> {
            releaseWorker.countDown();
            await(publicationQueue.posted);
            // The background refresh has passed its cancellation check and
            // queued its publication. Close before the EDT executes it.
            closeController();
        });
        drainWorker();
        onEdt(() -> assertEquals("closed window was repopulated by a stale refresh", 0, nodeCount()));
    }

    public void testOldModeRefreshCannotPublishIntoNewMode() throws Exception {
        preparePausedRefresh(unstagedFile());
        CountDownLatch replacementPaused = new CountDownLatch(1);
        onEdt(() -> {
            releaseWorker.countDown();
            await(publicationQueue.posted);
            // Run after the old publication, before the replacement refresh.
            rp.post(() -> { replacementPaused.countDown(); await(releaseReplacement); });
            panel().tgbHeadVsIndex.doClick(0);
        });
        await(replacementPaused);
        onEdt(() -> {
            assertTrue(panel().tgbHeadVsIndex.isSelected());
            assertEquals("an obsolete working-tree row appeared in HEAD/index mode", 0, nodeCount());
        });
    }

    public void testClosingWindowCancelsRunningDiffPreparationWorker() throws Exception {
        File file = unstagedFile();
        ControlledDiffProvider.entered = new CountDownLatch(1);
        ControlledDiffProvider.release = new CountDownLatch(1);
        final Setup[] selected = new Setup[1];
        final Object[] preparation = new Object[1];
        onEdt(() -> {
            controller = new MultiDiffPanelController(file, Revision.HEAD, Revision.LOCAL, -1);
            try {
                selected[0] = (Setup) field(MultiDiffPanelController.class, "currentSetup").get(controller);
                preparation[0] = field(MultiDiffPanelController.class, "dpt").get(controller);
            }
            catch (ReflectiveOperationException error) { throw new AssertionError(error); }
        });
        await(ControlledDiffProvider.entered);
        onEdt(this::closeController);
        ControlledDiffProvider.release.countDown();
        drainWorker();
        onEdt(() -> assertNull("cancelled preparation installed a diff after close", selected[0].getView()));
        assertTrue("the preparation worker's cancellation flag was not set",
                field(preparation[0].getClass(), "canceled").getBoolean(preparation[0]));
    }

    private void closeController() {
        if (controller != null && !controllerClosed) {
            controller.componentClosed();
            controllerClosed = true;
        }
    }

    private static void onEdt(Runnable runnable) throws Exception {
        try {
            EventQueue.invokeAndWait(runnable);
        } catch (InvocationTargetException error) {
            if (error.getCause() instanceof Error cause) throw cause;
            if (error.getCause() instanceof RuntimeException cause) throw cause;
            throw error;
        }
    }

    private MultiDiffPanel panel() {
        return (MultiDiffPanel) controller.getPanel();
    }

    private int nodeCount() {
        try {
            return ((DiffFileTable) field(MultiDiffPanelController.class, "fileListComponent").get(controller)).getNodes().size();
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private void drainWorker() throws InterruptedException {
        assertTrue("diff worker did not drain", rp.post(() -> {}).waitFinished(15000));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("test gate timed out");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }

    /** Observes the real controller's EDT handoff; does not replace its code. */
    private static final class PublicationQueue extends EventQueue {
        final CountDownLatch posted = new CountDownLatch(1);
        final RequestProcessor rp;

        PublicationQueue(RequestProcessor rp) { this.rp = rp; }

        @Override public void postEvent(AWTEvent event) {
            super.postEvent(event);
            if (event instanceof InvocationEvent && rp.isRequestProcessorThread()) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 1; i < stack.length; i++) {
                    StackTraceElement frame = stack[i];
                    if (frame.getClassName().equals(MultiDiffPanelController.class.getName())
                            && frame.getMethodName().equals("setupsChanged")
                            && stack[i - 1].getClassName().equals("org.openide.util.Mutex")
                            && stack[i - 1].getMethodName().equals("readAccess")) {
                        posted.countDown();
                        break;
                    }
                }
            }
        }

        void remove() { pop(); }
    }

    public static final class ControlledDiffProvider extends DiffControllerProvider {
        static volatile CountDownLatch entered;
        static volatile CountDownLatch release;

        @Override public DiffControllerImpl createDiffController(StreamSource base, StreamSource modified) throws IOException {
            if (entered != null) {
                entered.countDown();
                await(release);
            }
            return new DiffControllerImpl() {
                private final JPanel component = new JPanel();
                @Override public JComponent getJComponent() { return component; }
                @Override public int getDifferenceCount() { return 0; }
            };
        }
    }
}
