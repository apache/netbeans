/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.conifg.impl;

import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.action.FutureAction;
import org.netbeans.modules.dlight.sendto.api.OutputMode;
import org.netbeans.modules.dlight.sendto.api.ScriptsRegistry;
import org.netbeans.modules.dlight.sendto.output.OutputConvertorFactory;
import org.netbeans.modules.dlight.sendto.spi.Handler;
import org.netbeans.modules.dlight.sendto.util.ScriptExecutor;
import org.netbeans.modules.dlight.sendto.util.Utils;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = Handler.class)
public final class DefaultScriptHandler extends Handler<DefaultConfigurationPanel> {

    public static final String VALIDATION_SCRIPT = "validation_script"; // NOI18N
    public static final String VALIDATION_SCRIPT_EXECUTOR = "validation_script_executor"; // NOI18N
    public static final String SCRIPT = "script"; // NOI18N
    public static final String SCRIPT_EXECUTOR = "script_executor"; // NOI18N
    public static final String OUTPUT_MODE = "output_type"; // NOI18N
    private final Object cacheLock = new Object();
    private final Map<Integer, FutureAction> cachedActions = new HashMap<Integer, FutureAction>();
    private final Map<Integer, FileChangeListener> flisteners = new HashMap<Integer, FileChangeListener>();
    private final Map<Integer, Collection<? extends FileObject>> cachedFileObjects = new HashMap<Integer, Collection<? extends FileObject>>();

    public DefaultScriptHandler() {
        super("default"); // NOI18N
    }

    @Override
    public FutureAction createActionFor(final Lookup actionContext, final Configuration cfg) {
        assert SwingUtilities.isEventDispatchThread();

        final Collection<? extends FileObject> fos = actionContext.lookupAll(FileObject.class);

        if (fos == null || fos.isEmpty()) {
            return null;
        }

        ExecutionEnvironment _env = null;

        for (FileObject fo : fos) {
            if (_env == null) {
                _env = Utils.getExecutionEnvironment(fo);
            } else {
                if (!_env.equals(Utils.getExecutionEnvironment(fo))) {
                    // cannot run on different exec envs...
                    return null;
                }
            }
        }

        final ExecutionEnvironment env = _env;

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            return new FutureAction(NbBundle.getMessage(DefaultScriptHandler.class, "DefaultConfigurationPanel.hostNotConnected", env.getDisplayName()));
        }

        final Integer cfgID = cfg.getID();
        FutureAction action;

        // Lock could be made smaller.. 
        // But these operations are fast enought.. 

        synchronized (cacheLock) {
            if (cachedActions.containsKey(cfgID)
                    && cachedFileObjects.get(cfgID).containsAll(fos)
                    && fos.containsAll(cachedFileObjects.get(cfgID))) {
                return cachedActions.get(cfgID);
            }

            final FileChangeListener cl = new FileChangeAdapter() {
                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    synchronized (cacheLock) {
                        if (cachedActions.containsKey(cfgID)) {
                            cachedActions.remove(cfgID);
                        }
                    }
                }
            };

            flisteners.put(cfgID, cl);

            for (FileObject fo : fos) {
                fo.addFileChangeListener(WeakListeners.create(FileChangeListener.class, cl, fo));
            }

            cachedFileObjects.put(cfgID, fos);

            action = new FutureAction(new Callable<Action>() {
                @Override
                public Action call() throws Exception {
                    return createAction(fos, cfg, env);
                }
            });

            cachedActions.put(cfgID, action);
        }

        return action;
    }

    private Action createAction(final Collection<? extends FileObject> fos, final Configuration cfg, final ExecutionEnvironment env) {
        final String script = cfg.get(SCRIPT).trim();

        if (script.isEmpty()) {
            return null;
        }

        String validationScript = cfg.get(VALIDATION_SCRIPT).trim();
        String validationScriptExecutor = Utils.substituteShell(cfg.get(VALIDATION_SCRIPT_EXECUTOR).trim(), env);

        final ArrayList<String> args = new ArrayList<String>();
        args.add(null); // will be replaced with a script.. validation and then real...

        for (FileObject fo : fos) {
            args.add(fo.getPath());
        }

        if (!validationScript.isEmpty()) {
            String validationScriptFile = ScriptsRegistry.getScriptFile(cfg, env, validationScript);

            if (validationScriptFile == null) {
                return null;
            }

            args.set(0, validationScriptFile);

            ExitStatus rc = ProcessUtils.execute(env, validationScriptExecutor, args.toArray(new String[args.size()]));

            if (!rc.isOK()) {
                return null;
            }
        }

        return new AbstractAction(cfg.getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String scriptPath = ScriptsRegistry.getScriptFile(cfg, env, script);

                if (scriptPath == null) {
                    return;
                }

                args.set(0, scriptPath);
                String scriptExecutor = Utils.substituteShell(cfg.get(SCRIPT_EXECUTOR).trim(), env);

                List<String> cmd = new ArrayList<String>();
                cmd.add(scriptExecutor);
                cmd.addAll(args);

                ScriptExecutor executor = new ScriptExecutor(env, cmd);
                executor.execute(cfg.getName(), OutputMode.parse(cfg.get(OUTPUT_MODE)), new OutputConvertorFactory(fos));

                for (FileObject f : fos) {
                    f.refresh(true);
                }
            }
        };
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DefaultScriptHandler.class, "DefaultScriptConfigurator.description.text"); // NOI18N
    }

    @Override
    protected DefaultConfigurationPanel createConfigurationPanel() {
        return new DefaultConfigurationPanel();
    }

    @Override
    public void applyChanges(Configuration cfg) {
        if (cfg.isModified()) {
            ScriptsRegistry.invalidate(cfg);
            synchronized (cacheLock) {
                cachedActions.clear();
            }
        }
    }
}
