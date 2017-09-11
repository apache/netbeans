/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.api.IssueQuickSearch;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchPanel;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.HookType;
import org.netbeans.modules.versioning.hooks.SvnHook;
import org.netbeans.modules.versioning.hooks.SvnHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;

/**
 *
 * @author Tomas Stupka
 */
public class SvnHookTest extends NbTestCase {

    public SvnHookTest(String arg0) {
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

    @RandomlyFails
    public void testPanel() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(true);
        VCSHooksConfig.getInstance(HookType.SVN).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        HookPanel panel = getPanel(hook, getContext());
        assertFalse(panel.pushRadioButton.isVisible());
        assertFalse(panel.pushRadioButton.isVisible());

        assertTrue(panel.linkCheckBox.isSelected());
        assertTrue(panel.resolveCheckBox.isSelected());

        VCSHooksConfig.getInstance(HookType.SVN).setLink(false);
        VCSHooksConfig.getInstance(HookType.SVN).setResolve(false);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        panel = getPanel(hook, getContext());

        assertFalse(panel.linkCheckBox.isSelected());
        assertFalse(panel.resolveCheckBox.isSelected());
    }

    public void testBeforeCommitNoLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(false);

        String msg = "msg";
        SvnHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNull(ctx);
    }

    public void testBeforeCommitWithLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(true);

        String msg = "msg";
        SvnHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        ctx = hook.beforeCommit(ctx);
        assertNotNull(ctx);
        assertNotNull(ctx.getMessage());
        assertNotSame("", ctx.getMessage());
        assertNotSame(msg, ctx.getMessage()); // issue info was added
    }

    public void testAfterCommitLink() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(true);
        VCSHooksConfig.getInstance(HookType.SVN).setResolve(false);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        SvnHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertFalse(HookIssue.getInstance().closed);
    }

    public void testAfterCommitResolve() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(false);
        VCSHooksConfig.getInstance(HookType.SVN).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        SvnHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNull(HookIssue.getInstance().comment);
        assertTrue(HookIssue.getInstance().closed);
    }

    public void testAfterCommitLinkResolve() throws MalformedURLException, CoreException, IOException, InterruptedException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SvnHookImpl hook = getHook();

        VCSHooksConfig.getInstance(HookType.SVN).setLink(true);
        VCSHooksConfig.getInstance(HookType.SVN).setResolve(true);

        // give some time to NbPreferences to get back from knees
        // fix for #198665 seems to brought regression and sync issues
        Thread.sleep(2000);

        String msg = "msg";
        SvnHookContext ctx = getContext(msg);
        HookPanel panel = getPanel(hook, ctx); // initiate panel

        hook.afterCommit(ctx);
        assertNotNull(HookIssue.getInstance().comment);
        assertNotSame(-1, HookIssue.getInstance().comment.indexOf(msg));
        assertTrue(HookIssue.getInstance().closed);
    }

    private SvnHookImpl getHook() {
        Collection<SvnHook> hooks = VCSHooks.getInstance().getHooks(SvnHook.class);
        for (SvnHook SvnHook : hooks) {
            if(SvnHook instanceof SvnHookImpl) {
                assertNotNull(SvnHook);
                assertNotNull(SvnHook.getDisplayName());
                return (SvnHookImpl) SvnHook;
            }
        }
        return null;
    }

    public SvnHookContext getContext() throws IOException {
        return getContext("msg");
    }

    public SvnHookContext getContext(String msg) throws IOException {
        return getContext(msg, 1);
    }

    private SvnHookContext getContext(String msg, long revision) throws IOException {
        return new SvnHookContext(new File[]{new File(getWorkDir(), "f")}, "msg", Arrays.asList(new SvnHookContext.LogEntry("msg", "author", revision, new Date(System.currentTimeMillis()))));
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

    private HookPanel getPanel(final SvnHookImpl hook, final SvnHookContext ctx) throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
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
