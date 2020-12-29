/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.templates;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class PanelConfigureSources implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel{

    private PanelConfigureSourcesVisual component;
    private WizardDescriptor wizardDescriptor;
    private final ChangeSupport changeSupport;
    private final NewPythonProjectWizardIterator.WizardType type;
    private final String[] steps;

    public PanelConfigureSources (final NewPythonProjectWizardIterator.WizardType type, final String[] steps) {
        assert type != null;
        assert steps != null;
        this.type = type;
        this.steps = steps;
        this.changeSupport = new ChangeSupport(this);
    }

    @Override
    public PanelConfigureSourcesVisual getComponent() {
        if (component == null) {
            component = new PanelConfigureSourcesVisual ();
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 1);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(PanelConfigureSources.class);
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        getComponent().store(d);
    }

    @Override
    public boolean isValid() {
        return getComponent().valid(wizardDescriptor);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        assert l != null;
        this.changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        assert l != null;
        this.changeSupport.removeChangeListener(l);
    }

    @Override
    public void validate() throws WizardValidationException {
        getComponent().validate(wizardDescriptor);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
