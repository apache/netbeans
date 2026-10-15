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

package org.netbeans.modules.ant.freeform;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.ui.TargetMappingPanel;
import org.netbeans.modules.ant.freeform.ui.UnboundTargetAlert;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Union2;
import org.openide.util.actions.Presenter;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Action bindings for a freeform project.
 * @author Jesse Glick
 */
public final class Actions implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(Actions.class.getName());

    /**
     * Some routine global actions for which we can supply a display name.
     * These are IDE-specific.
     */
    private static final Set<String> COMMON_IDE_GLOBAL_ACTIONS = new HashSet<String>(Arrays.asList(
        ActionProvider.COMMAND_DEBUG,
        ActionProvider.COMMAND_PROFILE,
        ActionProvider.COMMAND_DELETE,
        ActionProvider.COMMAND_COPY,
        ActionProvider.COMMAND_MOVE,
        ActionProvider.COMMAND_RENAME));
    /**
     * Similar to {@link #COMMON_IDE_GLOBAL_ACTIONS}, but these are not IDE-specific.
     * We also mark all of these as bound in the project; if the user
     * does not really have a binding, they are prompted for one when
     * the action is "run".
     */
    private static final Set<String> COMMON_NON_IDE_GLOBAL_ACTIONS = new HashSet<String>(Arrays.asList(
        ActionProvider.COMMAND_BUILD,
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_REBUILD,
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_TEST,
        // XXX JavaProjectConstants.COMMAND_JAVADOC
        "javadoc", // NOI18N
        // XXX WebProjectConstants.COMMAND_REDEPLOY
        // XXX should this really be here? perhaps not, once web part of #46886 is implemented...
        "redeploy",
        // XXX deploy action of EJB freeform project
        "deploy")); // NOI18N
    
    private final FreeformProject project;
    
    /**
     * Create a new action provider.
     * @param project the associated project
     */
    public Actions(FreeformProject project) {
        this.project = project;
    }
    
    public String[] getSupportedActions() {
        final Element genldata = project.getPrimaryConfigurationData();
        final Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        // Use a set, not a list, since when using context you can define one action several times:
        final Set<String> names = new LinkedHashSet<String>();
        if (actionsEl != null) {                            
            for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
                names.add(actionEl.getAttribute("name")); // NOI18N
            }
            // #46886: also always enable all common global actions, in case they should be selected:
            names.addAll(COMMON_NON_IDE_GLOBAL_ACTIONS);
        }
        names.add(COMMAND_RENAME);
        names.add(COMMAND_MOVE);
        names.add(COMMAND_COPY);
        names.add(COMMAND_DELETE);
        return names.toArray(new String[0]);
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            return true;
        }
        if (COMMAND_COPY.equals(command)) {
            return true;
        }
        if (COMMAND_RENAME.equals(command)) {
            return true;
        }
        if (COMMAND_MOVE.equals(command)) {
            return true;
        }
        
        Element genldata = project.getPrimaryConfigurationData();
        Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        boolean foundAction = false;
        for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                // XXX perhaps check also existence of script
                Element contextEl = XMLUtil.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
                if (contextEl != null) {
                    // Check whether the context contains files all in this folder,
                    // matching the pattern if any, and matching the arity (single/multiple).
                    Map<String,FileObject> selection = findSelection(contextEl, context, project,
                            command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD) ? new AtomicReference<String>() : null);
                    LOG.log(Level.FINE, "detected selection {0} for command {1} in {2}", new Object[] {selection, command, project});
                    if (selection.size() == 1) {
                        // Definitely enabled.
                        return true;
                    } else if (!selection.isEmpty()) {
                        // Multiple selection; check arity.
                        Element arityEl = XMLUtil.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                        assert arityEl != null : "No <arity> in <context> for " + command;
                        if (XMLUtil.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL) != null) { // NOI18N
                            // Supports multiple selection, take it.
                            return true;
                        }
                    }
                } else {
                    // Not context-sensitive.
                    return true;
                }
            }
        }
        if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
            // #46886: these are always enabled if they are not specifically bound.
            return true;
        }
        if (foundAction) {
            // Was at least one context-aware variant but did not match.
            return false;
        } else {
            throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
        }
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        Element genldata = project.getPrimaryConfigurationData();
        Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        boolean foundAction = false;
        for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                runConfiguredAction(command, project, actionEl, context);
            }
        }
        if (!foundAction) {
            if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
                // #46886: try to bind it.
                if (addGlobalBinding(command)) {
                    // If bound, run it immediately.
                    invokeAction(command, context);
                }
            } else {
                throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
            }
        }
    }
    
    /**
     * Find a file selection in a lookup context based on a project.xml <context> declaration.
     * If all DataObject's (or FileObject's) in the lookup match the folder named in the declaration,
     * and match any optional pattern declaration, then they are returned as a map from relative
     * path to actual file object. Otherwise an empty map is returned.
     * @param methodName if not null, look for {@link SingleMethod} rather than {@link DataObject}, and set the method name here
     */
    private static Map<String,FileObject> findSelection(Element contextEl, Lookup context, FreeformProject project, AtomicReference<String> methodName) {
        Collection<? extends FileObject> files;
        if (methodName == null) {
            Collection<? extends DataObject> filesDO = context.lookupAll(DataObject.class);
            if (filesDO.isEmpty()) {
                 return Collections.emptyMap();
            }
            Collection<FileObject> _files = new ArrayList<FileObject>(filesDO.size());
            for (DataObject d : filesDO) {
                _files.add(d.getPrimaryFile());
            }
            files = _files;
        } else {
            SingleMethod meth = context.lookup(SingleMethod.class);
            if (meth == null) {
                return Collections.emptyMap();
            }
            methodName.set(meth.getMethodName());
            files = Collections.singleton(meth.getFile());
        }
        Element folderEl = XMLUtil.findElement(contextEl, "folder", FreeformProjectType.NS_GENERAL); // NOI18N
        assert folderEl != null : "Must have <folder> in <context>";
        String rawtext = XMLUtil.findText(folderEl);
        assert rawtext != null : "Must have text contents in <folder>";
        String evaltext = project.evaluator().evaluate(rawtext);
        if (evaltext == null) {
            return Collections.emptyMap();
        }
        FileObject folder = project.helper().resolveFileObject(evaltext);
        if (folder == null) {
            return Collections.emptyMap();
        }
        Pattern pattern = null;
        Element patternEl = XMLUtil.findElement(contextEl, "pattern", FreeformProjectType.NS_GENERAL); // NOI18N
        if (patternEl != null) {
            String text = XMLUtil.findText(patternEl);
            assert text != null : "Must have text contents in <pattern>";
            try {
                pattern = Pattern.compile(text);
            } catch (PatternSyntaxException e) {
                org.netbeans.modules.ant.freeform.Util.err.annotate(e, ErrorManager.UNKNOWN, "From <pattern> in " + FileUtil.getFileDisplayName(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH)), null, null, null); // NOI18N
                org.netbeans.modules.ant.freeform.Util.err.notify(e);
                return Collections.emptyMap();
            }
        }
        Map<String,FileObject> result = new HashMap<String,FileObject>();
        for (FileObject file : files) {
            String path = FileUtil.getRelativePath(folder, file);
            if (path == null) {
                return Collections.emptyMap();
            }
            if (pattern != null && !pattern.matcher(path).find()) {
                return Collections.emptyMap();
            }
            result.put(path, file);
        }
        return result;
    }
    
    /**
     * Run a project action as described by subelements <script> and <target>.
     */
    private static void runConfiguredAction(
            final String command,
            final FreeformProject project,
            final Element actionEl,
            final Lookup context) {

        final List<String> targetNames = new ArrayList<String>();
        final Properties props = new Properties();
        final Union2<FileObject,String> scriptFile = ProjectManager.mutex().readAccess(new Mutex.Action<Union2<FileObject,String>>() {
            @Override
            public Union2<FileObject,String> run() {
                Union2<FileObject,String> result;
                String script;
                Element scriptEl = XMLUtil.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
                if (scriptEl != null) {
                    script = XMLUtil.findText(scriptEl);
                } else {
                    script = "build.xml"; // NOI18N
                }
                String scriptLocation = project.evaluator().evaluate(script);
                final FileObject sf = scriptLocation == null ? null : project.helper().resolveFileObject(scriptLocation);
                if (sf != null) {
                    result = Union2.<FileObject,String>createFirst(sf);
                } else {
                    return Union2.<FileObject,String>createSecond(scriptLocation);
                }
                List<Element> targets = XMLUtil.findSubElements(actionEl);
                for (Element targetEl : targets) {
                    if (!targetEl.getLocalName().equals("target")) { // NOI18N
                        continue;
                    }
                    targetNames.add(XMLUtil.findText(targetEl));
                }
                Element contextEl = XMLUtil.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
                if (contextEl != null) {
                    AtomicReference<String> methodName = SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command) || SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(command) ?
                        new AtomicReference<String>() : null;
                    Map<String,FileObject> selection = findSelection(contextEl, context, project, methodName);
                    if (selection.isEmpty()) {
                        return null;
                    }
                    if (methodName != null && methodName.get() != null) {
                        props.setProperty("method", methodName.get());
                    }
                    String separator = null;
                    if (selection.size() > 1) {
                        // Find the right separator.
                        Element arityEl = XMLUtil.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                        assert arityEl != null : "No <arity> in <context> for " + actionEl.getAttribute("name");
                        Element sepFilesEl = XMLUtil.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL); // NOI18N
                        if (sepFilesEl == null) {
                            // Only handles single files -> skip it.
                            return null;
                        }
                        separator = XMLUtil.findText(sepFilesEl);
                        if(separator == null) {
                            // is set-up to handle multiple files but no separator is found -> skip it.
                            String message = "No separator found for " + command + " command. <separated-files>,</separated-files> could be used.";
                            LOG.log(Level.WARNING, message);
                            StatusDisplayer.getDefault().setStatusText(message);
                            return null;
                        }
                    }
                    Element formatEl = XMLUtil.findElement(contextEl, "format", FreeformProjectType.NS_GENERAL); // NOI18N
                    assert formatEl != null : "No <format> in <context> for " + actionEl.getAttribute("name");
                    String format = XMLUtil.findText(formatEl);
                    StringBuilder buf = new StringBuilder();
                    Iterator<Map.Entry<String,FileObject>> it = selection.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String,FileObject> entry = it.next();
                        if (format.equals("absolute-path")) { // NOI18N
                            File f = FileUtil.toFile(entry.getValue());
                            if (f == null) {
                                // Not a disk file??
                                return null;
                            }
                            buf.append(f.getAbsolutePath());
                        } else if (format.equals("relative-path")) { // NOI18N
                            buf.append(entry.getKey());
                        } else if (format.equals("absolute-path-noext")) { // NOI18N
                            File f = FileUtil.toFile(entry.getValue());
                            if (f == null) {
                                // Not a disk file??
                                return null;
                            }
                            String path = f.getAbsolutePath();
                            int dot = path.lastIndexOf('.');
                            if (dot > path.lastIndexOf('/')) {
                                path = path.substring(0, dot);
                            }
                            buf.append(path);
                        } else if (format.equals("relative-path-noext")) { // NOI18N
                            String path = entry.getKey();
                            int dot = path.lastIndexOf('.');
                            if (dot > path.lastIndexOf('/')) {
                                path = path.substring(0, dot);
                            }
                            buf.append(path);
                        } else {
                            assert format.equals("java-name") : format;
                            String path = entry.getKey();
                            int dot = path.lastIndexOf('.');
                            String dotless;
                            if (dot == -1 || dot < path.lastIndexOf('/')) {
                                dotless = path;
                            } else {
                                dotless = path.substring(0, dot);
                            }
                            String javaname = dotless.replace('/', '.');
                            buf.append(javaname);
                        }
                        if (it.hasNext()) {
                            assert separator != null;
                            buf.append(separator);
                        }
                    }
                    Element propEl = XMLUtil.findElement(contextEl, "property", FreeformProjectType.NS_GENERAL); // NOI18N
                    assert propEl != null : "No <property> in <context> for " + actionEl.getAttribute("name");
                    String prop = XMLUtil.findText(propEl);
                    assert prop != null : "Must have text contents in <property>";
                    props.setProperty(prop, buf.toString());
                }
                for (Element propEl : targets) {
                    if (!propEl.getLocalName().equals("property")) { // NOI18N
                        continue;
                    }
                    String rawtext = XMLUtil.findText(propEl);
                    if (rawtext == null) {
                        // Legal to have e.g. <property name="intentionally-left-blank"/>
                        rawtext = ""; // NOI18N
                    }
                    String evaltext = project.evaluator().evaluate(rawtext); // might be null
                    if (evaltext != null) {
                        props.setProperty(propEl.getAttribute("name"), evaltext); // NOI18N
                    }
                }
                return result;
            }
        });
        if (scriptFile == null) {
            return;
        } else if (scriptFile.hasFirst()) {
            final String[] targetNameArray;
            if (!targetNames.isEmpty()) {
                targetNameArray = targetNames.toArray(new String[0]);
            } else {
                // Run default target.
                targetNameArray = null;
            }
            TARGET_RUNNER.runTarget(scriptFile.first(), targetNameArray, props, ActionProgress.start(context));
        } else {
            assert scriptFile.hasSecond();
            //#57011: if the script does not exist, show a warning:
            final NotifyDescriptor nd = new NotifyDescriptor.Message(
                MessageFormat.format(
                    NbBundle.getMessage(Actions.class, "LBL_ScriptFileNotFoundError"),
                    new Object[] {scriptFile.second()}),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    @ActionID(id = "org.netbeans.modules.ant.freeform.Actions$Custom", category = "Project")
    @ActionRegistration(displayName = "Custom Freeform Actions", lazy=false) // should not be displayed in UI anyway
    @ActionReference(position = 300, path = "Projects/org-netbeans-modules-ant-freeform/Actions")
    public static final class Custom extends AbstractAction implements ContextAwareAction {
        public Custom() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }
        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }
        public @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends FreeformProject> projects = actionContext.lookupAll(FreeformProject.class);
            if (projects.size() != 1) {
                return this;
            }
            final FreeformProject p = projects.iterator().next();
            class A extends AbstractAction implements Presenter.Popup {
                public @Override void actionPerformed(ActionEvent e) {
                    assert false;
                }
                public @Override JMenuItem getPopupPresenter() {
                    class M extends JMenuItem implements DynamicMenuContent {
                        public @Override JComponent[] getMenuPresenters() {
                            Action[] actions = contextMenuCustomActions(p);
                            JComponent[] comps = new JComponent[actions.length];
                            for (int i = 0; i < actions.length; i++) {
                                if (actions[i] != null) {
                                    JMenuItem item = new JMenuItem();
                                    org.openide.awt.Actions.connect(item, actions[i], true);
                                    comps[i] = item;
                                } else {
                                    comps[i] = new JSeparator();
                                }
                            }
                            return comps;
                        }
                        public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
                            return getMenuPresenters();
                        }
                    }
                    return new M();
                }
            }
            return new A();
        }
    }
    
    /**
     * Build the context menu for a project.
     * @param p a freeform project
     * @return a list of actions (or null for separators)
     */
    static Action[] contextMenuCustomActions(FreeformProject p) {
        List<Action> actions = new ArrayList<Action>();
        Element genldata = p.getPrimaryConfigurationData();
        Element viewEl = XMLUtil.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl != null) {
            Element contextMenuEl = XMLUtil.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
            if (contextMenuEl != null) {
                actions.add(null);
                for (Element actionEl : XMLUtil.findSubElements(contextMenuEl)) {
                    if (actionEl.getLocalName().equals("ide-action")) { // NOI18N
                        String cmd = actionEl.getAttribute("name");
                        String displayName;
                        if (COMMON_IDE_GLOBAL_ACTIONS.contains(cmd) || COMMON_NON_IDE_GLOBAL_ACTIONS.contains(cmd)) {
                            displayName = NbBundle.getMessage(Actions.class, "CMD_" + cmd);
                        } else {
                            // OK, fall back to raw name.
                            displayName = cmd;
                        }
                        actions.add(ProjectSensitiveActions.projectCommandAction(cmd, displayName, null));
                    } else if (actionEl.getLocalName().equals("separator")) { // NOI18N
                        actions.add(null);
                    } else {
                        assert actionEl.getLocalName().equals("action") : actionEl;
                        actions.add(new CustomAction(p, actionEl));
                    }
                }
            }
        }
        return actions.toArray(new Action[0]);
    }
    
    private static final class CustomAction extends AbstractAction {

        private final FreeformProject p;
        private final Element actionEl;
        
        public CustomAction(FreeformProject p, Element actionEl) {
            this.p = p;
            this.actionEl = actionEl;
        }
        
        public void actionPerformed(ActionEvent e) {
            runConfiguredAction(null, p, actionEl, Lookup.EMPTY);
        }
        
        public boolean isEnabled() {
            String script;
            Element scriptEl = XMLUtil.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
            if (scriptEl != null) {
                script = XMLUtil.findText(scriptEl);
            } else {
                script = "build.xml"; // NOI18N
            }
            String scriptLocation = p.evaluator().evaluate(script);
            return p.helper().resolveFileObject(scriptLocation) != null;
        }
        
        public Object getValue(String key) {
            if (key.equals(Action.NAME)) {
                Element labelEl = XMLUtil.findElement(actionEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                return XMLUtil.findText(labelEl);
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    // Overridable for unit tests only:
    static TargetRunner TARGET_RUNNER = new TargetRunner();
    
    static class TargetRunner {
        public TargetRunner() {}
        public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props, final ActionProgress listener) {
            try {
                ActionUtils.runTarget(scriptFile, targetNameArray, props).addTaskListener(new TaskListener() {
                    @Override public void taskFinished(Task task) {
                        listener.finished(((ExecutorTask) task).result() == 0);
                    }
                });
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                listener.finished(false);
            }
        }
    }
    
    /**
     * Prompt the user to make a binding for a common global command.
     * Available targets are shown. If one is selected, it is bound
     * (and also added to the context menu of the project), as if the user
     * had picked it in {@link TargetMappingPanel}.
     * @param command the command name as in {@link ActionProvider}
     * @return true if a binding was successfully created, false if it was cancelled
     * @see "#46886"
     */
    private boolean addGlobalBinding(String command) {
        try {
            return new UnboundTargetAlert(project, command).accepted();
        } catch (IOException e) {
            // Problem generating bindings - so skip it.
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
}
