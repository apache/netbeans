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

package org.netbeans.modules.j2ee.core.api.support.progress;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.utilities.ProgressPanel;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class ProgressSupportTest extends NbTestCase {

    // this class uses Mutex.EVENT.readAccess(Mutex.Action) in several places
    // instead of the more obvious SwingUtilities.invokeAndWait()
    // just because the former method has simpler exception handling

    public ProgressSupportTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testInvoke() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        // incremented each time an action is invoked
        final AtomicInteger invokeCount = new AtomicInteger();
        // incremented each time an action is invoked in the correct thread
        // (EDT for event thread actions, !EDT for background actions)
        final AtomicInteger correctThreadCount = new AtomicInteger();
        // incremented each time the progress panel state is correct during an action invocation
        // (closed for event thread actions, open for background actions)
        final AtomicInteger correctPanelStateCount = new AtomicInteger();

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount.addAndGet(isEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isClosed(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
            }
        });

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount.addAndGet(isEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isClosed(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
            }
        });

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress("Testing a background action.");

                correctThreadCount.addAndGet(isNotEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isOpen(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress("Testing the second background action.");

                correctThreadCount.addAndGet(isNotEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isOpen(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount.addAndGet(isEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isClosed(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
            }
        });

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress("Testing the third background action.");

                correctThreadCount.addAndGet(isNotEventDispatchThread());
                correctPanelStateCount.addAndGet(Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
                    public Integer run() {
                        return isOpen(actionContext.getPanel());
                    }
                }));
                invokeCount.incrementAndGet();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        ProgressSupport.invoke(actions);
        assertEquals(6, invokeCount.get());
        assertEquals(6, correctThreadCount.get());
        assertEquals(6, correctPanelStateCount.get());

        ProgressSupport.invoke(actions);
        assertEquals(12, invokeCount.get());
        assertEquals(12, correctThreadCount.get());
        assertEquals(12, correctPanelStateCount.get());
    }

    private int isEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread() ? 1 : 0;
    }

    private int isNotEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread() ? 0 : 1;
    }

    private int isOpen(ProgressPanel panel) {
        return panel.isOpen() ? 1 : 0;
    }

    private int isClosed(ProgressPanel panel) {
        return panel.isOpen() ? 0 : 1;
    }

    public void testProgressMessage() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicReference<String> progressMessage = new AtomicReference<String>();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress("Progress message");

                progressMessage.set(Mutex.EVENT.readAccess(new Mutex.Action<String>() {
                    public String run() {
                        return actionContext.getPanel().getText();
                    }
                }));
            }
        });

        ProgressSupport.invoke(actions);
        assertEquals("Progress message", progressMessage.get());
    }

    public void testDisabledActionDoesNotCauseAnInfiniteLoop() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final CountDownLatch sync = new CountDownLatch(1);
        final AtomicBoolean ran = new AtomicBoolean();

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(ProgressSupport.Context actionContext) {
                synchronized (sync) {
                    ran.set(true);
                    sync.countDown();
                }
            }

            public boolean isEnabled() {
                return false;
            }
        });

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    sync.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {}
                if (!ran.get()) {
                    // hmm, anything better?
                    System.exit(1);
                }
            }
        });

        ProgressSupport.invoke(actions);
    }

    public void testExceptionInEventThreadActionPropagates() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(final ProgressSupport.Context actionContext) {
                throw new RuntimeException("Error");
            }
        });

        try {
            ProgressSupport.invoke(actions);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
        }
    }

    public void testExceptionInBackgroundActionPropagatesAndProgressPanelCloses() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicReference<ProgressPanel> progressPanel = new AtomicReference<ProgressPanel>();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress("Background");

                // get the panel
                progressPanel.set(actionContext.getPanel());

                // and simulate an error
                throw new AssertionError("Error");
            }
        });

        try {
            ProgressSupport.invoke(actions);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof AssertionError);
        }

        assertFalse(progressPanel.get().isOpen());
    }

    public void testNoCancelButtonWhenNonCancellableInvocation() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean cancelVisible = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                cancelVisible.set(Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
                    public Boolean run() {
                        return actionContext.getPanel().isCancelVisible();
                    }
                }));
            }
        });

        ProgressSupport.invoke(actions);
        assertFalse(cancelVisible.get());
    }

    public void testCancelButtonWhenCancellableInvocation() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean cancelVisible1 = new AtomicBoolean();
        final AtomicBoolean cancelEnabled1 = new AtomicBoolean();
        final AtomicBoolean cancelVisible2 = new AtomicBoolean();
        final AtomicBoolean cancelEnabled2 = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getPanel();
                        cancelVisible1.set(progressPanel.isCancelVisible());
                        cancelEnabled1.set(progressPanel.isCancelEnabled());
                        return null;
                    }
                });
            }
        });

        actions.add(new ProgressSupport.BackgroundAction(true) {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getPanel();
                        cancelVisible2.set(progressPanel.isCancelVisible());
                        cancelEnabled2.set(progressPanel.isCancelEnabled());
                        return null;
                    }
                });
            }
        });

        // calling invoke() with cancellable == true to make the Cancel button visible
        // invoke() should return true, meaning all actions were invoked
        assertTrue(ProgressSupport.invoke(actions, true));

        // the first action was not cancellable
        assertTrue(cancelVisible1.get());
        assertFalse(cancelEnabled1.get());

        // the second action was cancellable
        assertTrue(cancelVisible2.get());
        assertTrue(cancelEnabled2.get());
    }

    public void testCancelWorks() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean secondActionInvoked = new AtomicBoolean();
        final AtomicBoolean cancelInvoked = new AtomicBoolean();
        final AtomicBoolean cancelInvokedInEDT = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction(true) {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        actionContext.getPanel().getCancelButton().doClick();
                        return null;
                    }
                });
                // at this point cancel() should already have been called
            }

            @Override
            public boolean cancel() {
                cancelInvokedInEDT.set(SwingUtilities.isEventDispatchThread());
                cancelInvoked.set(true);
                return true;
            }
        });

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(ProgressSupport.Context actionContext) {
                secondActionInvoked.set(true);
            }
        });

        assertFalse(ProgressSupport.invoke(actions, true));
        assertFalse(secondActionInvoked.get());
        assertTrue(cancelInvokedInEDT.get());
        assertTrue(cancelInvoked.get());
    }

    public void testNoCancelWhenTheCancelMethodReturnsFalse() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean secondActionInvoked = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction(true) {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        actionContext.getPanel().getCancelButton().doClick();
                        return null;
                    }
                });
                // at this point cancel() should already have been called
            }

            public boolean cancel() {
                return false;
            }
        });

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(ProgressSupport.Context actionContext) {
                secondActionInvoked.set(true);
            }
        });

        assertTrue(ProgressSupport.invoke(actions, true));
        assertTrue(secondActionInvoked.get());
    }

    public void testEscapeDoesNotCloseDialogForBackgroundNonCancellableActions() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean panelOpen = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        // fake an escape key press
                        JRootPane rootPane = actionContext.getPanel().getRootPane();
                        KeyEvent event = new KeyEvent(rootPane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                        rootPane.dispatchEvent(event);

                        panelOpen.set(actionContext.getPanel().isOpen());
                        return null;
                    }
                });
            }
        });

        ProgressSupport.invoke(actions);
        assertTrue(panelOpen.get());
    }

    public void testEscapeCancelsCancellableActions() {
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final AtomicBoolean panelOpen = new AtomicBoolean();
        final AtomicBoolean cancelEnabled = new AtomicBoolean();

        actions.add(new ProgressSupport.BackgroundAction(true) {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        // fake an escape key press
                        JRootPane rootPane = actionContext.getPanel().getRootPane();
                        KeyEvent event = new KeyEvent(rootPane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                        rootPane.dispatchEvent(event);

                        panelOpen.set(actionContext.getPanel().isOpen());
                        cancelEnabled.set(actionContext.getPanel().getCancelButton().isEnabled());
                        return null;
                    }
                });
            }
        });

        ProgressSupport.invoke(actions, true);
        assertTrue(panelOpen.get());
        assertFalse(cancelEnabled.get());
    }
}
