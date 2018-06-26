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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigLocal;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigLocalValidator;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.DebugUrl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.XDebugUrlArguments;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Action implementation for LOCAL configuration.
 * It means running and debugging web pages on a local web server.
 * @author Tomas Mysik
 */
class ConfigActionLocal extends ConfigAction {
    private final FileObject webRoot;

    protected ConfigActionLocal(PhpProject project) {
        super(project);
        webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
    }

    @Override
    public boolean isProjectValid() {
        return isValid(RunConfigLocalValidator.validateConfigAction(RunConfigLocal.forProject(project), true) == null);
    }

    @Override
    public boolean isFileValid() {
        return isValid(RunConfigLocalValidator.validateConfigAction(RunConfigLocal.forProject(project), false) == null);
    }

    protected boolean isValid(boolean valid) {
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, webRoot);
        return file != null;
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (DebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        try {
            showProjectUrl(CommandUtils.urlForProject(project));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void debugProject() {
        final URL[] urlToShow = new URL[2];
        try {
            final URL urlForProject = getUrlToShow(CommandUtils.urlForProject(project));
            if (urlForProject != null) {
                urlToShow[0] = CommandUtils.createDebugUrl(urlForProject, XDebugUrlArguments.XDEBUG_SESSION_START);
                urlToShow[1] = CommandUtils.createDebugUrl(urlForProject, XDebugUrlArguments.XDEBUG_SESSION_STOP_NO_EXEC);
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (StopDebuggingException exc) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (urlToShow[0] != null) {
                    showProjectUrl(urlToShow[0]);
                }
            }
        };

        Cancellable cancellable = new Cancellable() {
            @Override
            public boolean cancel() {
                if (urlToShow[1] != null) {
                    showProjectUrl(urlToShow[1]);
                }
                return true;
            }
        };


        //temporary; after narrowing deps. will be changed
        DebugStarter dbgStarter = DebugStarterFactory.getInstance();
        if (dbgStarter != null) {
            if (dbgStarter.isAlreadyRunning()) {
                if (CommandUtils.warnNoMoreDebugSession()) {
                    dbgStarter.stop();
                    debugProject();
                }
            } else {
                startDebugger(dbgStarter, runnable, cancellable, FileUtil.toFileObject(RunConfigLocal.forProject(project).getIndexFile()));
            }
        }
    }

    @Override
    public void runFile(Lookup context) {
        try {
            // need to fetch these vars _before_ focus changes (can happen in eventuallyUploadFiles() method)
            final URL url = CommandUtils.urlForContext(project, context);
            assert url != null;

            preShowUrl(context);

            showContextUrl(url, context);
        } catch (MalformedURLException ex) {
            //TODO: improve error handling
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void debugFile(Lookup context) {
        // need to fetch these vars _before_ focus changes (can happen in eventuallyUploadFiles() method)
        URL urlForStartDebugging = null;
        URL urlForStopDebugging = null;
        try {
            final URL urlForContext = getUrlToShow(CommandUtils.urlForContext(project, context));
            if (urlForContext != null) {
                urlForStartDebugging = CommandUtils.createDebugUrl(urlForContext, XDebugUrlArguments.XDEBUG_SESSION_START);
                urlForStopDebugging = CommandUtils.createDebugUrl(urlForContext, XDebugUrlArguments.XDEBUG_SESSION_STOP_NO_EXEC);
            }
        } catch (MalformedURLException ex) {
            //TODO improve error handling
            Exceptions.printStackTrace(ex);
            return;
        } catch (StopDebuggingException exc) {
            return;
        }
        preShowUrl(context);
        debugFile(CommandUtils.fileForContextOrSelectedNodes(context, webRoot), urlForStartDebugging, urlForStopDebugging);
    }

    URL getUrlToShow(final URL defaultRunUrl) throws MalformedURLException, StopDebuggingException {
        URL urlToShow = null;
        // XXX run config
        DebugUrl debugUrl = ProjectPropertiesSupport.getDebugUrl(project);
        switch (debugUrl) {
            case DEFAULT_URL:
                urlToShow = defaultRunUrl;
                assert urlToShow != null;
                break;
            case ASK_FOR_URL:
                AskForUrlPanel askForUrlPanel = new AskForUrlPanel(project, defaultRunUrl);
                if (!askForUrlPanel.open()) {
                    throw new StopDebuggingException();
                }
                urlToShow = askForUrlPanel.getUrl();
                assert urlToShow != null;
                break;
            case DO_NOT_OPEN_BROWSER:
                // noop
                break;
            default:
                throw new IllegalStateException("Unknown state for debug URL: " + debugUrl);
        }
        return urlToShow;
    }

    void debugFile(final FileObject selectedFile, final URL urlForStartDebugging, final URL urlForStopDebugging) {
        assert selectedFile != null;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (urlForStartDebugging != null) {
                    showFileUrl(urlForStartDebugging, selectedFile);
                }
            }
        };

        Cancellable cancellable = new Cancellable() {
            @Override
            public boolean cancel() {
                if (urlForStopDebugging != null) {
                    showFileUrl(urlForStopDebugging, selectedFile);
                }
                return true;
            }
        };

        DebugStarter dbgStarter = DebugStarterFactory.getInstance();
        if (dbgStarter != null) {
            if (dbgStarter.isAlreadyRunning()) {
                if (CommandUtils.warnNoMoreDebugSession()) {
                    dbgStarter.stop();
                    debugFile(selectedFile, urlForStartDebugging, urlForStopDebugging);
                }
            } else {
                startDebugger(dbgStarter, runnable, cancellable, selectedFile);
            }
        }
    }

    protected void preShowUrl(Lookup context) {
        // hook for subclasses
    }

    private void startDebugger(final DebugStarter dbgStarter, final Runnable initDebuggingCode, Cancellable cancellable,
            final FileObject debuggedFile) {
        Callable<Cancellable> initDebuggingCallable = Executors.callable(initDebuggingCode, cancellable);
        // XXX run config
        DebugStarter.Properties props = new DebugStarter.Properties.Builder()
                .setStartFile(debuggedFile)
                .setCloseSession(false)
                .setPathMapping(ProjectPropertiesSupport.getDebugPathMapping(project))
                .setDebugProxy(ProjectPropertiesSupport.getDebugProxy(project))
                .setEncoding(ProjectPropertiesSupport.getEncoding(project))
                .build();
        dbgStarter.start(project, initDebuggingCallable, props);
    }

    void showProjectUrl(URL url) {
        showFileUrl(url, CommandUtils.fileForProject(project, webRoot));
    }

    private void showContextUrl(URL url, Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context);
        if (file != null) {
            showFileUrl(url, file);
        } else {
            assert false : "FO should be found for context"; // NOI18N
            showProjectUrl(url);
        }
    }

    void showFileUrl(URL url, FileObject file) {
        project.getLookup().lookup(PhpProject.ClientSideDevelopmentSupport.class).showFileUrl(url, file);
    }

    //~ Inner classes

    private static final class StopDebuggingException extends Exception {
        private static final long serialVersionUID = -22807171434417714L;
    }

}
