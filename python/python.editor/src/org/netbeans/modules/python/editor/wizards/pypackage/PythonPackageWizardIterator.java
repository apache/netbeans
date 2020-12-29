/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.editor.wizards.pypackage;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public final class PythonPackageWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor.Panel targetChooserPanel;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        Project project = Templates.getProject(wizard);
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] sg = sources.getSourceGroups(Sources.TYPE_GENERIC);

        if (panels == null) {
            targetChooserPanel =
                    Templates.createSimpleTargetChooser(project, sg);
            panels = new WizardDescriptor.Panel[]{
                        targetChooserPanel
                    };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent)c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    @Override
    public Set instantiate() throws IOException {
        final Set<FileObject> resultSet = new HashSet<>();
        FileObject template = FileUtil.getConfigFile("Templates/Python/_init.py"); // NOI18N
        FileObject dir = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);

        String subdir = targetName.replace('.', '/');
        FileObject pkgDir = dir.getFileObject(subdir);
        if (pkgDir == null) {
            // Must create the directories first
            pkgDir = FileUtil.createFolder(dir, subdir);
        }

        // Create __init__ files. This has to be done in EACH of the
        // directories up to the root!! Otherwise packages cannot
        // be imported.
        if (pkgDir != null) {
            FileObject curr = pkgDir;
            while (curr != dir) {
                if (curr.getFileObject("__init__.py") == null) { // NOI18N
                    if (curr == pkgDir) {
                        FileObject initFile = createInitFile(template, curr, "__init__.py").getPrimaryFile(); // NOI18N
                        resultSet.add(initFile);
                    } else {
                        // Just create empty package files in the intermediate directories
                        curr.createData("__init__", "py"); // NOI18N
                    }
                }
                curr = curr.getParent();
            }
        }

        return resultSet;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
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

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    private DataObject createInitFile(FileObject template, FileObject sourceDir, String name) throws IOException {
        DataFolder dataFolder = DataFolder.findFolder(sourceDir);
        DataObject dataTemplate = DataObject.find(template);
        //Strip extension when needed
        int index = name.lastIndexOf('.');
        if (index > 0 && index < name.length() - 1 && "py".equalsIgnoreCase(name.substring(index + 1))) {
            name = name.substring(0, index);
        }
        return dataTemplate.createFromTemplate(dataFolder, name);
    }
}
