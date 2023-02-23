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

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.ProgressLineProcessor;
import java.awt.Component;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoriesFactory;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.GrailsArtifacts;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.GrailsTargetChooserPanel;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Grails artifact.
 */
public class GrailsArtifactWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(GrailsArtifactWizardIterator.class.getName());

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private SourceCategory sourceCategory;
    private GrailsProject project;


    private WizardDescriptor.Panel[] createPanels(WizardDescriptor wizardDescriptor) {
        Sources sources = ProjectUtils.getSources(project);

        SourceGroup group = GrailsArtifacts.getSourceGroupForCategory(
                project, GroovySources.getGroovySourceGroups(sources), sourceCategory);

        // fails with enabled assertion, fallback below
        assert group != null;

        SourceGroup[] groups;
        if (group == null) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return new WizardDescriptor.Panel[] {
                Templates.buildSimpleTargetChooser(project, groups).create()
            };
        } else {
            return new WizardDescriptor.Panel[] {
                new GrailsTargetChooserPanel(project, group, null, sourceCategory.getSuffix())
            };
        }
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    @Override
    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set instantiate(final ProgressHandle handle) throws IOException {
        // FIXME some target create multiple artifacts so we should
        // a) use non-interactive
        // b) ask in wizard for rewrite
        FileObject dir = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);
        String packageName = getPackageName(dir);

        String artifactName = (packageName == null || "".equals(packageName.trim()))
                ? targetName
                : packageName + "." + targetName;

        handle.start(100);
        try {
            String serverCommand = sourceCategory.getCommand();

            ProjectInformation inf = ProjectUtils.getInformation(project);
            String displayName = inf.getDisplayName() + " (" + serverCommand + ")"; // NOI18N

            final Callable<Process> grailsCallable = ExecutionSupport.getInstance().createSimpleCommand(
                    serverCommand, GrailsProjectConfig.forProject(project), artifactName);
            final DialogLineProcessor dialogProcessor = new DialogLineProcessor();

            // This is bit hacky, we prepared line processor listening for overwrite
            // question - dialog needs to get OutputStream to put the answer to.
            // This could need a change if the wizard task could rerun.
            Callable<Process> callable = new Callable<Process>() {
                @Override
                public Process call() throws Exception {
                    Process process = grailsCallable.call();
                    dialogProcessor.setWriter(new OutputStreamWriter(process.getOutputStream()));
                    return process;
                }
            };

            // we need a special descriptor here
            ExecutionDescriptor descriptor = new ExecutionDescriptor()
                    .frontWindow(true).inputVisible(true);
            descriptor = descriptor.outProcessorFactory(new InputProcessorFactory() {
                @Override
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new ProgressLineProcessor(handle, 100, 9)));
                }
            });
            descriptor = descriptor.errProcessorFactory(new InputProcessorFactory() {
                @Override
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(dialogProcessor));
                }
            });

            descriptor = descriptor.postExecution(new RefreshProjectRunnable(project));

            ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
            Future<Integer> future = service.run();
            try {
                Integer ret = future.get();
                if (ret.intValue() != 0) {
                    String msg = NbBundle.getMessage(GrailsArtifactWizardIterator.class, "WIZARD_ERROR_MESSAGE_ARTIFACT");
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex.getCause());
            }
        } finally {
            handle.progress(100);
            handle.finish();
        }

        LOG.log(Level.FINEST, "Artifact Name  {0}", artifactName);
        dir.refresh();

        FileObject artifact;
        String suffix = sourceCategory.getSuffix();
        if (suffix == null) {
            artifact = dir.getFileObject(targetName, Templates.getTemplate(wiz).getExt());

            // Even Domain/Controller classes don't have any suffix but still using groovy extension
            if (artifact == null) {
                artifact = dir.getFileObject(targetName, "groovy");
            }
        } else {
            artifact = dir.getFileObject(targetName + suffix);
        }

        if (artifact != null) {
            return Collections.singleton(artifact);
        } else {
            LOG.log(Level.WARNING, "Problem creating FileObject for {0} ", artifactName);
        }

        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;

        FileObject template = Templates.getTemplate(wiz);
        
        project = Templates.getProject(wiz).getLookup().lookup(GrailsProject.class);
        
        SourceCategoriesFactory sourceCategoriesFactory = project.getSourceCategoriesFactory();
        sourceCategory = sourceCategoriesFactory.getSourceCategory(GrailsArtifacts.getCategoryTypeForTemplate(template));
        assert sourceCategory != null;

        index = 0;
        panels = createPanels(wiz);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
        sourceCategory = null;
        project = null;
    }

    @Override
    public String name() {
        //return "" + (index + 1) + " of " + panels.length;
        return ""; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    private static String getPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> groups = GroovySources.getGroovySourceGroups(sources);
        String packageName = null;
        for (int i = 0; i < groups.size() && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups.get(i).getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replace("/", "."); // NOI18N
        }
        return packageName;
    }

    // FIXME should we give up on detection and leave the input up to the user ?
    private static class DialogLineProcessor implements LineProcessor {

        private static final Pattern OVERWRITE_PATTERN =
                Pattern.compile("^.*\\s([^\\s]+\\.groovy) already exists\\. Overwrite\\? \\[y/n\\]$"); // NOI18N

        private static final Pattern DEFAULT_PACKAGE_PATTERN =
                Pattern.compile("^WARNING: You have not specified a package\\. .* Do you want to continue\\? \\(y, n\\)$"); // NOI18N

        private Writer writer;

        @Override
        public void processLine(String line) {
            Writer answerWriter;
            synchronized (this) {
                answerWriter = writer;
            }

            if (answerWriter != null) {
                Matcher matcher = OVERWRITE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(GrailsArtifactWizardIterator.class, "MSG_overwrite_file", matcher.group(1)),
                            NotifyDescriptor.YES_NO_OPTION);

                    try {
                        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                            answerWriter.write("y\n"); // NOI18N
                        } else {
                            answerWriter.write("n\n"); // NOI18N
                        }
                        answerWriter.flush();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    matcher = DEFAULT_PACKAGE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        try {
                            // user has been warned in wizard
                            answerWriter.write("y\n"); // NOI18N
                            answerWriter.flush();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }

        public void setWriter(Writer writer) {
            synchronized (this) {
                this.writer = writer;
            }
        }

        @Override
        public void close() {
            // noop
        }

        @Override
        public void reset() {
            // noop
        }

    }
}
