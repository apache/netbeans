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

package org.netbeans.modules.bugtracking.vcs;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchPanel;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.HookType;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushOperation;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;

/**
 *
 * @author Tomas Stupka
 */
public class HgHookTest extends NbTestCase {

    public HgHookTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath() + "/userdir");
    }

    public void testPanel() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setLink(true);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(true);
        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        HookPanel panel = getPanel(hook, getContext());
        assertTrue(panel.pushRadioButton.isVisible());

        assertTrue(panel.linkCheckBox.isSelected());
        assertTrue(panel.resolveCheckBox.isSelected());
        assertTrue(panel.commitRadioButton.isSelected());
        assertFalse(panel.pushRadioButton.isSelected());

        VCSHooksConfig.getInstance(HookType.HG).setLink(false);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(false);
        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(false);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        panel = getPanel(hook, getContext());

        assertFalse(panel.linkCheckBox.isSelected());
        assertFalse(panel.resolveCheckBox.isSelected());
        assertFalse(panel.commitRadioButton.isSelected());
        assertTrue(panel.pushRadioButton.isSelected());
    }

    public void testBeforeCommitNoLink() throws MalformedURLException,  IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setLink(false);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNull(ctx);
    }

    public void testBeforeCommitWithLink() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setLink(true);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNotNull(ctx);
        assertNotNull(ctx.getMessage());
        assertNotSame("", ctx.getMessage());
        assertNotSame(msg, ctx.getMessage()); // issue info was added
    }

    public void testAfterCommitLink() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(true);
        VCSHooksConfig.getInstance(HookType.HG).setLink(true);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(false);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertFalse(HookIssue.getInstance().closed);
    }

    public void testAfterCommitResolve() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(true);
        VCSHooksConfig.getInstance(HookType.HG).setLink(false);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNull(HookIssue.getInstance().comment);
        assertTrue(HookIssue.getInstance().closed);
    }

    public void testAfterCommitLinkResolve() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(true);
        VCSHooksConfig.getInstance(HookType.HG).setLink(true);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        HgHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertTrue(HookIssue.getInstance().closed);
    }

    public void testAfterCommitLinkResolveAfterPush() throws MalformedURLException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HookIssue.getInstance().reset();
        
        HgHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.HG).setAfterCommit(false); // PUSH!
        VCSHooksConfig.getInstance(HookType.HG).setLink(true);
        VCSHooksConfig.getInstance(HookType.HG).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String changeset = "#" + System.currentTimeMillis();
        String msg = "msg";
        HgHookContext ctx = getContext(msg, changeset);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNull(HookIssue.getInstance().comment);
        assertFalse(HookIssue.getInstance().closed);

        PushOperation a = VCSHooksConfig.getInstance(HookType.HG).popPushAction(changeset);
        assertNotNull(a);
        assertNotNull(a.getIssueID());
        assertEquals(HookIssue.getInstance().getID(), a.getIssueID());
        assertNotNull(a.getMsg());
        assertNotSame("", a.getMsg());
        assertNotSame(-1, msg);
    }

    private HgHookImpl getHook() {
        Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);
        for (HgHook hgHook : hooks) {
            if(hgHook instanceof HgHookImpl) {
                assertNotNull(hgHook);
                assertNotNull(hgHook.getDisplayName());
                return (HgHookImpl) hgHook;
            }
        }
        return null;
    }

    public HgHookContext getContext() throws IOException {
        return getContext("msg");
    }

    public HgHookContext getContext(String msg) throws IOException {
        return getContext(msg, "1");
    }

    private HgHookContext getContext(String msg, String changeset) throws IOException {
        return new HgHookContext(new File[]{new File(getWorkDir(), "f")}, "msg", new HgHookContext.LogEntry("msg", "author", changeset, new Date(System.currentTimeMillis())));
    }

    private void setRepository(Repository repository, HookPanel panel) throws IllegalArgumentException, IllegalAccessException {
        Field[] fs = panel.qs.getClass().getDeclaredFields();
        for (Field f : fs) {
            if(f.getType() == QuickSearchPanel.class) {
                f.setAccessible(true);
                QuickSearchPanel qsp = (QuickSearchPanel) f.get(panel.qs);
                fs = qsp.getClass().getDeclaredFields();
                for (Field f2 : fs) {
                    if(f2.getType() == JComboBox.class) {
                        f2.setAccessible(true);
                        JComboBox cmb = (JComboBox) f2.get(qsp);
                        DefaultComboBoxModel model = new DefaultComboBoxModel(new Repository[] {repository});
                        cmb.setModel(model);
                        cmb.setSelectedItem(repository);
                        return;
                    }
                }
            }
        }
    }

    private void setIssue(Repository repository, HookPanel panel) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        panel.qs.setIssue(TestKit.getIssue(repository, HookIssue.getInstance()).getIssue());
    }

    private HookPanel getPanel(final HgHookImpl hook, final HgHookContext ctx) throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        final HookPanel[] p = new HookPanel[] {null};
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                p[0] = (HookPanel)hook.createComponent(ctx);
            }
        });
        assertNotNull(p[0]);
        assertTrue(p[0] instanceof HookPanel);

        preparePanel(p[0]);

        return p[0];
    }

    private void preparePanel(HookPanel panel) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        HookRepository hookRepository = new HookRepository();
        Repository repository = TestKit.getRepository(hookRepository).getRepository();
        setRepository(repository, panel);
        setIssue(repository, panel);
        panel.enableFields(); // emulate event
    }

}
