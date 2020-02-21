/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makefile.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class MakefileWizard implements TemplateWizard.Iterator {

    /** Holds list of event listeners */
    private static Vector<MakefileWizardListener> listenerList = null;
    protected TemplateWizard wd;
    /** Current array of panels */
    protected Object[] panels;
    /** Store a pointer to the current panel */
    private WizardDescriptor.Panel currentPanel;
    /** Array of panels for all types except
     *  MakefileData.COMPLEX_MAKEFILE_TYPE.
     */
    protected Object[] normalPanels;
    /** LinkedList of panels for MakefileData.COMPLEX_MAKEFILE_TYPE */
    protected LinkedList<Object> complexPanels;
    /** Makefile data */
    private MakefileData makefileData;
    /** The MakefileWizard */
    private static MakefileWizard makefileWizard;
    /** Wizard title */
    private String title;
    /** Index into the array */
    private int index;
    /** The index into the displayed steps (different than index) */
    private int virtIndex;
    /** Index into complexPanels where new panels should be added */
    private int addIdx;
    /** The Next button is needed to reset the default button */
    private JButton nextButton;
    /** The Finish button is needed to change the label to Last */
    private JButton finishButton;
    /** The cancel button */
    private JButton cancelButton;
    /** Save the Finish button's original label */
    private String finishLabel;
    /** Save the Finish button's original mnemonic too */
    private int finishMnemonic;
    /** Are we doing Last updates to keep it enabled? */
    private boolean finishEnabled;
    /** Tells if we have output the Compilation Preferences yet */
    private boolean haveCompilerFlags;
    /** template wizard property change listener ... */
    private PropertyChangeListener listener = null;
    private WizardDescriptor.Panel targetChooserDescriptorPanel;
    private WizardDescriptor.Panel baseDirectoryDescriptorPanel;
    private WizardDescriptor.Panel targetNameDescriptorPanel;
    private WizardDescriptor.Panel makefileSourcesDescriptorPanel;
    private WizardDescriptor.Panel selectPreferencesDescriptorPanel;
    //private WizardDescriptor.Panel compilerTypeDescriptorPanel;
    private WizardDescriptor.Panel platformTypeDescriptorPanel;
    private WizardDescriptor.Panel createTargetsDescriptorPanel;
    private WizardDescriptor.Panel buildOutputDescriptorPanel;
    private WizardDescriptor.Panel makefileIncludesDescriptorPanel;
    private WizardDescriptor.Panel standardLibsDescriptorPanel;
    private WizardDescriptor.Panel userLibsDescriptorPanel;
    private WizardDescriptor.Panel compilerOptionsDescriptorPanel;
    private WizardDescriptor.Panel basicFlagsDescriptorPanel;
    private WizardDescriptor.Panel compilerPathDescriptorPanel;
    //private WizardDescriptor.Panel makeTargetDescriptorPanel;
    //private WizardDescriptor.Panel customTargetDescriptorPanel;
    private WizardDescriptor.Panel makefileReviewDescriptorPanel;

    /**
     * Constructor Note: A panel object gets created for each panel in this
     * constructor. However, the object creation is very light-weight
     * (mainly just creating the subtitle) and the real creation is deffered
     * until the panel is displayed for the first time.
     */
    public MakefileWizard() {
        makefileWizard = this;
        String aTitle = NbBundle.getBundle(MakefileWizard.class).
                getString("LBL_MakefileWizardTitle");	// NOI18N
        setTitle(aTitle);
        makefileData = new MakefileData();
    }

    private void initPanels() {
        // Panels used in both SIMPLE_* and COMPLEX_* targets
        targetChooserDescriptorPanel = wd.targetChooser();
        baseDirectoryDescriptorPanel = new MakefileWizardDescriptorPanel(new BaseDirectoryPanel(this), "base_directory"); // NOI18N
        targetNameDescriptorPanel = new MakefileWizardDescriptorPanel(new TargetNamePanel(this), "target_name"); // NOI18N
        makefileSourcesDescriptorPanel = new MakefileWizardDescriptorPanel(new MakefileSourcesPanel(this), "source_files"); // NOI18N
        //compilerTypeDescriptorPanel = new MakefileWizardDescriptorPanel(new CompilerTypePanel(this));
        selectPreferencesDescriptorPanel = new MakefileWizardDescriptorPanel(new SelectPreferencesPanel(this), "compiling_preference"); // NOI18N

        // Panels used by just the COMPLEX_* targets
        createTargetsDescriptorPanel = new MakefileWizardDescriptorPanel(new CreateTargetsPanel(this), "list_of_targets"); // NOI18N
        platformTypeDescriptorPanel = new MakefileWizardDescriptorPanel(new PlatformTypePanel(this), "type_and_platform"); // NOI18N
        buildOutputDescriptorPanel = new MakefileWizardDescriptorPanel(new BuildOutputPanel(this), "build_output"); // NOI18N
        makefileIncludesDescriptorPanel = new MakefileWizardDescriptorPanel(new MakefileIncludesPanel(this), "include_directories"); // NOI18N
        standardLibsDescriptorPanel = new MakefileWizardDescriptorPanel(new StandardLibsPanel(this), "standard_libraries"); // NOI18N
        userLibsDescriptorPanel = new MakefileWizardDescriptorPanel(new UserLibsPanel(this), "libraries"); // NOI18N
        basicFlagsDescriptorPanel = new MakefileWizardDescriptorPanel(new BasicFlagsPanel(this), "basic_options"); // NOI18N
        compilerPathDescriptorPanel = new MakefileWizardDescriptorPanel(new CompilerPathPanel(this), "compiler_paths"); // NOI18N
        //makeTargetDescriptorPanel = new MakefileWizardDescriptorPanel(new MakeTargetPanel(this));
        //customTargetDescriptorPanel = new MakefileWizardDescriptorPanel(new CustomTargetPanel(this));
        compilerOptionsDescriptorPanel = new MakefileWizardDescriptorPanel(new CompilerOptionsPanel(this), "advanced_options"); // NOI18N

        // The review/summary panel
        makefileReviewDescriptorPanel = new MakefileWizardDescriptorPanel(new MakefileReviewPanel(this), "review_makefile"); // NOI18N


        normalPanels = new WizardDescriptor.Panel[]{
                    targetChooserDescriptorPanel,
                    //compilerTypeDescriptorPanel,
                    platformTypeDescriptorPanel,
                    targetNameDescriptorPanel,
                    makefileSourcesDescriptorPanel,
                    selectPreferencesDescriptorPanel,
                    makefileReviewDescriptorPanel
                };

        // Use a LinkedList for complexPanels because we add new panels to the
        // middle when new targets are added.
        complexPanels = new LinkedList<Object>();
        complexPanels.addLast(targetChooserDescriptorPanel);
        complexPanels.addLast(platformTypeDescriptorPanel);
        complexPanels.addLast(baseDirectoryDescriptorPanel);
        complexPanels.addLast(createTargetsDescriptorPanel);
        complexPanels.addLast(makefileReviewDescriptorPanel);

        panels = complexToArray();
        currentPanel = targetChooserDescriptorPanel;

        addIdx = 4;
        haveCompilerFlags = false;
        nextButton = null;
        finishButton = null;
        cancelButton = null;
        finishEnabled = false;
    }

    public TemplateWizard getTemplateWizard() {
        return wd;
    }

    /** Getter for the data associated with a panel */
    final public MakefileData getMakefileData() {
        return makefileData;
    }

    /** Setter for the title string */
    final public void setTitle(String title) {
        this.title = title;
    }

    /** Getter for Next button */
    public JButton getNextButton() {
        return nextButton;
    }

    /** Getter for Finish button */
    public JButton getFinishButton() {
        return finishButton;
    }

    /** Getter for Cancel button */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /** Getter for Finish label */
    public String getFinishLabel() {
        return finishLabel;
    }

    /** Getter for Finish mnemonic */
    public int getFinishMnemonic() {
        return finishMnemonic;
    }

    /** Used to determine the current target's TargetData */
    public int getCurrentTargetKey() {
        int key = -1;

        if (makefileData.getMakefileType() <
                MakefileData.COMPLEX_MAKEFILE_TYPE) {
            key = 0;
        } else {
            for (int i = 0; i < panels.length; i++) {
                if (panels[i] instanceof StepHeader) {
                    key = ((StepHeader) panels[i]).getKey();
                }
                if (i == index) {
                    break;
                }
            }
        }

        return key;
    }

    /** The current panel.
     */
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor> res = currentPanel;
        return res;
    }

    /** Current name of the panel */
    public String name() {
        Object[] args = {
            Integer.valueOf(index + 1),
            Integer.valueOf(panels.length)
        };
        MessageFormat mf = new MessageFormat(
                NbBundle.getBundle(WizardDescriptor.class).getString("CTL_ArrayIteratorName"));				// NOI18N

        return mf.format(args);
    }

    /** Updates button state */
    public void updateStateHack() {
        //wd.updateState();
        // FIXUP: HACK - updateState is now protected in WizardDescriptor. Calling
        // FIXUP: setTitleFormat() has the sideeffect of calling updateState, so this works for now....
        if (wd != null) {
            wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        }
    }

    /** Updates button state */
    public void updateState() {
        fireChangeEvent();
    }

    /**
     *  Create the steps array for setting the Steps area in the wizard. Also,
     *  set the virtIndex. The virtIndex is similar to index except it doesn't
     *  count contracted panels. Its the index the WizardDescriptor uses to
     *  correctly highlight the current step in the wizard.
     */
    private String[] getSteps() {
        String[] steps = new String[panels.length + 1];
        StepHeader hdr = null;
        int j = 1;
        int doingTarget = 0;
        int expand = 0;
        int aVirtIndex = 0;

        // Add templateChooser to 0th position...
        steps[0] = wd.templateChooser().getComponent().getName();
        for (int i = 0; i < panels.length; i++) {
            if (panels[i] instanceof StepHeader) {
                if (i < index) {
                    aVirtIndex++;
                }
                hdr = (StepHeader) panels[i];
                steps[j++] = hdr.getTitle();
                doingTarget = hdr.getNum();
                if (index >= i && index <= (i + hdr.getNum())) {
                    expand = hdr.getNum();
                }
            } else if (doingTarget-- > 0) {
                if (expand-- > 0) {
                    if (i < index) {
                        aVirtIndex++;
                    }
                    steps[j++] = NbBundle.getMessage(getClass(),
                            "FMT_TARGET_PANEL", "    ", // NOI18N
                            ((WizardDescriptor.Panel) panels[i]).getComponent().getName());
                }
            } else {
                steps[j++] = ((WizardDescriptor.Panel) panels[i]).getComponent().getName();
                if (i < index) {
                    aVirtIndex++;
                }
            }
        }

        // Now copy the overallocated steps array to nue
        String[] nue = new String[j];
        System.arraycopy(steps, 0, nue, 0, j);
        this.virtIndex = aVirtIndex;
        return nue;
    }

    /**
     *  Set the current Makefile type. This has the side affect of changing the
     *  array of panels wizard uses and the steps shown.
     */
    public void updatePanels(int type) {
        if (type >= MakefileData.COMPLEX_MAKEFILE_TYPE) {
            panels = complexToArray();
        } else {
            panels = normalPanels;
        }
        ((JPanel) currentPanel.getComponent()).putClientProperty(
                WizardDescriptor.PROP_CONTENT_DATA, getSteps()); // NOI18N
        updateState();
    }

    /**
     *  Is there a next panel?
     *
     *  @return true if so
     */
    public boolean hasNext() {
        return (index) < (panels.length - 1);
    }

    /**
     *  Is there a previous panel?
     *
     *  @return true if so
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     *  Moves to the next panel. If the index points to a StepHeader then
     *  skip that and show the next panel.
     *
     *  @exception NoSuchElementException if the panel does not exist
     */
    public synchronized void nextPanel() {
        if (panels[++index] instanceof StepHeader) {
            index++;
        }
        currentPanel = (WizardDescriptor.Panel) panels[index];
        updatePanels(makefileData.getMakefileType());

        ((JPanel) currentPanel.getComponent()).putClientProperty(
                WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                Integer.valueOf(virtIndex));
    }

    /**
     *  Moves to previous panel.
     *  @exception NoSuchElementException if the panel does not exist
     */
    public synchronized void previousPanel() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        if (panels[--index] instanceof StepHeader) {
            index--;
        }
        currentPanel = (WizardDescriptor.Panel) panels[index];
        updatePanels(makefileData.getMakefileType());

        ((JPanel) currentPanel.getComponent()).putClientProperty(
                WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                Integer.valueOf(virtIndex));
    }

    /**
     *  Convert the LinkedList of complexPanels to an array of
     *  MakefileWizardPanel
     */
    private Object[] complexToArray() {
        Object[] p = new Object[complexPanels.size()];

        ListIterator iter = complexPanels.listIterator();
        for (int i = 0; i < complexPanels.size(); i++) {
            p[i] = iter.next();
        }

        return p;
    }

    /** Does a target with this key exist? */
    public boolean targetExists(int key) {

        for (int i = 0; i < complexPanels.size(); i++) {
            Object o = complexPanels.get(i);
            if (o instanceof StepHeader && ((StepHeader) o).getKey() == key) {
                return true;
            }
        }

        return false;
    }

    /** Create new panels for a target, based on the target type */
    public int addTarget(int type, String name, int key) {
        boolean needCompilerFlags = false;
        int count = 0;

        switch (type) {
            case TargetData.COMPLEX_EXECUTABLE:
                needCompilerFlags = true;
                count = 6;
                complexPanels.add(addIdx++, new TargetHeader(name, type, count - 1, key));
                complexPanels.add(addIdx++, buildOutputDescriptorPanel);
                complexPanels.add(addIdx++, makefileSourcesDescriptorPanel);
                complexPanels.add(addIdx++, makefileIncludesDescriptorPanel);
                complexPanels.add(addIdx++, standardLibsDescriptorPanel);
                complexPanels.add(addIdx++, userLibsDescriptorPanel);
                break;

            case TargetData.COMPLEX_ARCHIVE:
                needCompilerFlags = true;
                count = 4;
                complexPanels.add(addIdx++, new TargetHeader(name, type, count - 1, key));
                complexPanels.add(addIdx++, buildOutputDescriptorPanel);
                complexPanels.add(addIdx++, makefileSourcesDescriptorPanel);
                complexPanels.add(addIdx++, makefileIncludesDescriptorPanel);
                break;

            case TargetData.COMPLEX_SHAREDLIB:
                needCompilerFlags = true;
                count = 5;
                complexPanels.add(addIdx++, new TargetHeader(name, type, count - 1, key));
                complexPanels.add(addIdx++, buildOutputDescriptorPanel);
                complexPanels.add(addIdx++, makefileSourcesDescriptorPanel);
                complexPanels.add(addIdx++, makefileIncludesDescriptorPanel);
                complexPanels.add(addIdx++, userLibsDescriptorPanel);
                break;

            case TargetData.COMPLEX_MAKE_TARGET:
                complexPanels.add(addIdx++, new TargetHeader(name, type, 1, key));
                //complexPanels.add(addIdx++, makeTargetDescriptorPanel);
                complexPanels.add(addIdx++, new MakefileWizardDescriptorPanel(new MakeTargetPanel(this), "recursive_make")); // NOI18N
                break;

            case TargetData.COMPLEX_CUSTOM_TARGET:
                complexPanels.add(addIdx++, new TargetHeader(name, type, 1, key));
                //complexPanels.add(addIdx++, customTargetPanel);
                complexPanels.add(addIdx++, new MakefileWizardDescriptorPanel(new CustomTargetPanel(this), "custom_make")); // NOI18N
                break;
        }

        if (needCompilerFlags && !haveCompilerFlags) {
            complexPanels.add(addIdx,
                    new StepHeader(NbBundle.getBundle(getClass()).
                    getString("FMT_COMP_PREFS"), 3, -1));		// NOI18N
            complexPanels.add(addIdx + 1, basicFlagsDescriptorPanel);
            complexPanels.add(addIdx + 2, compilerOptionsDescriptorPanel);
            complexPanels.add(addIdx + 3, compilerPathDescriptorPanel);
            haveCompilerFlags = true;
            count += 4;
        }

        updatePanels(type);

        return count;			    // number of panels added
    }

    /** Delete the panels for the specified target */
    public void deleteTarget(int key) {
        deleteTarget(key, true);
    }

    /** Delete the panels for the specified target */
    public void deleteTarget(int key, boolean doUpdate) {
        boolean changed = false;

        for (int i = 0; i < complexPanels.size(); i++) {
            if (complexPanels.get(i) instanceof TargetHeader) {
                TargetHeader hdr = (TargetHeader) complexPanels.get(i);
                if (key == hdr.getKey()) {
                    for (int j = 0; j <= hdr.getNum(); j++) {
                        complexPanels.remove(i);
                        changed = true;
                    }
                    addIdx -= hdr.getNum() + 1;
                    break;
                }
            }
        }

        if (changed && doUpdate) {
            updatePanels(getMakefileData().getMakefileType());
        }
    }

    /** Change the panel name for the specified target */
    public void changeTarget(int key, String name, int type) {
        boolean changed = false;

        for (int i = 0; i < complexPanels.size(); i++) {
            if (complexPanels.get(i) instanceof TargetHeader) {
                TargetHeader hdr = (TargetHeader) complexPanels.get(i);
                if (key == hdr.getKey()) {
                    if (type != hdr.getType()) {
                        // Changing type of target - delete old target and create new
                        deleteTarget(key, false);
                        int saveIdx = addIdx;
                        addIdx = i;		// control where target is added
                        int count = addTarget(type, name, key);
                        addIdx = saveIdx + count;
                    } else {
                        // Just change the name of the target
                        hdr.setName(name);
                        hdr.setTitle(NbBundle.getMessage(getClass(),
                                "FMT_TARGET_CREATE", name));	// NOI18N
                    }
                    changed = true;
                    break;
                }
            }
        }

        if (changed) {
            updatePanels(getMakefileData().getMakefileType());
        }
    }

    /** Toplevel validation method. Gathers warnings from all panels */
    public ArrayList validateAllData() {
        ArrayList<String> msgs = new ArrayList<String>();
        int key = -1;

        for (int i = 0; i < panels.length; i++) {
            if (panels[i] instanceof WizardDescriptor.Panel &&
                    ((WizardDescriptor.Panel) panels[i]).getComponent() instanceof MakefileWizardPanel) {
                ((MakefileWizardPanel) ((WizardDescriptor.Panel) panels[i]).getComponent()).validateData(msgs, key);
            } else if (panels[i] instanceof StepHeader) {
                key = ((StepHeader) panels[i]).getKey();
            }
        }

        return msgs;
    }
    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // Set<ChangeListener>

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
        Iterator it;

        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    // Wizard methods
    public boolean onFinish() {
        MakefileGenerator gen = new MakefileGenerator(makefileData);

        /*
         * Remove any unused targets. A target is unused if its index > 0 and
         * the first target is a simple target.
         */
        List<TargetData> tlist = getMakefileData().getTargetList();
        if (tlist.size() > 1) {
            TargetData target = tlist.get(1);

            if (!target.isComplex()) {
                for (int i = tlist.size() - 1; i > 0; i--) {
                    tlist.remove(i);
                }
            }
        }
        boolean status = gen.generate();
        return status;
    }

    /**
     *  Returns whether the wizard has been completed and the code should
     *  be generated.
     */
    public void executeWizard() {
        listener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (index == 0) {
                    // FIXUP: don't know how to disable finish button on target chooser panel
                    // FIXUP: this hack will just disable the finish button if on first panel...
                    finishButton.setEnabled(false);
                }
                if (event.getPropertyName().
                        equals(DialogDescriptor.PROP_VALUE)) {
                    Object option = event.getNewValue();
                    if (option == WizardDescriptor.FINISH_OPTION || option == WizardDescriptor.CANCEL_OPTION) {
                        //boolean done = false;

                        if (option == WizardDescriptor.FINISH_OPTION) {
                            if (hasNext()) {
                                // Go to the Review panel
                                index = panels.length - 2;
                                nextPanel();
                                nextButton.setEnabled(false);
                            } else {
                                //done = onFinish();
                            }
                            updateStateHack();
                        } else {
                            // nothing. The iterator will call 'instantiate' where the makefile is created
                        }
                    }
                }
            }
        };

        index = 0;
        currentPanel = (WizardDescriptor.Panel) panels[index];
        //wd = new MakefileWizardDescriptor(this);
	/*
        wd.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE,			// NOI18N
        new Boolean(true));
        wd.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED,			// NOI18N
        new Boolean(true));
        wd.setTitleFormat(new MessageFormat("{0}"));			// NOI18N
        wd.setTitle(title);
         */
        setupWizardButtons(wd);

        updatePanels(getMakefileData().getMakefileType()); //FIXUP
        ((JPanel) (currentPanel.getComponent())).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);

        wd.addPropertyChangeListener(listener);
    /*
    dialog = DialogDisplayer.getDefault().createDialog(wd);
    dialog.show();
    dialog.dispose();
     */
    }

    public void unexecuteWizard() {
        if (listener != null) {
            wd.removePropertyChangeListener(listener);
            listener = null;
        }
    }

    /**
     *  We need to do several things with the buttons. First off, we want to
     *  find the JButton for the Next and Finish buttons. We need the Next
     *  button because we want to make another button default in some panels
     *  but we need to return the 'default' status to the Next button at some
     *  later point. We need the Finish button so we can change its label to
     *  "Last" until we reach the review panel. We also want to reset the
     *  closing buttons so only the Cancel button does a close.
     */
    private void setupWizardButtons(WizardDescriptor wd) {
        Object[] options = wd.getOptions();	    // save original buttons

        wd.setOptions(new Object[]{
                    WizardDescriptor.NEXT_OPTION, WizardDescriptor.FINISH_OPTION, WizardDescriptor.CANCEL_OPTION});
        Object[] objs = wd.getOptions();
        if (objs != null && objs.length == 3) {
            nextButton = (JButton) objs[0];

            finishButton = (JButton) objs[1];
            finishLabel = finishButton.getText();
            finishMnemonic = finishButton.getMnemonic();
//	    finishButton.setText(NbBundle.getBundle(MakefileWizard.class).
//			    getString("BTN_Last"));			// NOI18N
//	    finishButton.setMnemonic(NbBundle.getBundle(MakefileWizard.class).
//			    getString("MNEM_Last").charAt(0));		// NOI18N

            cancelButton = (JButton) objs[2];
        }
        wd.setOptions(options);			    // restor original buttons
        setFinishClosingEnabled(false);
    }

    private void unsetupWizardButtons(WizardDescriptor wd) {
        setFinishClosingEnabled(true);
//	finishButton.setText(finishLabel);
//	finishButton.setMnemonic(finishMnemonic);
    }

    /**
     * Will enable the finish button to close the dialog or not.
     * The cancel button always closes the dialog.
     */
    public void setFinishClosingEnabled(boolean b) {
        if (b) {
            wd.setClosingOptions(new Object[]{finishButton, cancelButton});
        } else {
            wd.setClosingOptions(new Object[]{cancelButton});
        }
    }

    /**
     *  We need to reenable often because each button press disables the
     *  Finish button.
     */
    public void setFinishEnabled(boolean tf) {
        //wd.setFinishEnabled(tf); // FIXUP
        finishButton.setEnabled(tf); // FIXUP ???
    }

    final public static MakefileWizard getMakefileWizard() {
        return makefileWizard;
    }


    //
    // The following is the static portion of the class.
    //
    public static void showWizard() {
        makefileWizard = new MakefileWizard();
        makefileWizard.executeWizard();
    }

    /**
     *  The StepHeader represents a group of panels which are collapsed to a
     *  single line in the complexPanels steps list until one of those panels
     *  is stepped into.
     */
    private class StepHeader {

        /** The line displayed in the Steps panel */
        private String title;
        /** The number of panels making up this step */
        private int num;
        /** A lookup key to map the StepHeader to a TargetData */
        private int key;

        /** The constructor */
        public StepHeader(String title, int num, int key) {

            this.title = title;
            this.num = num;
            this.key = key;
        }

        /** Getter for the title */
        public String getTitle() {
            return title;
        }

        /** Setter for the title */
        protected void setTitle(String title) {
            this.title = title;
        }

        /** Getter for the num */
        public int getNum() {
            return num;
        }

        /** Setter for the num */
        protected void setNum(int num) {
            this.num = num;
        }

        /** Getter for the key */
        public int getKey() {
            return key;
        }

        /** Setter for the key */
        protected void setKey(int key) {
            this.key = key;
        }
    }

    /**
     *  The TargetHeader represents a target in the complexPanels list of steps.
     *  Each target may have multiple panels but these panels are not shown
     *  unless the wizard is ``stepped-into'' the TargetHeader.
     */
    private class TargetHeader extends MakefileWizard.StepHeader {

        /** The name is used to create the title */
        private String name;
        /** The type of the target this header is for */
        private int type;

        /** Constructor */
        public TargetHeader(String name, int type, int num, int key) {

            super(null, num, key);
            this.name = name;
            this.type = type;
            this.setTitle(NbBundle.getMessage(getClass(),
                    "FMT_TARGET_CREATE", name));			// NOI18N
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public void initialize(TemplateWizard wiz) {
        wd = wiz;
        initPanels();
        executeWizard();
    }

    public void uninitialize(TemplateWizard wiz) {
        unexecuteWizard();
        unsetupWizardButtons(wiz);
        wd = null;
    }

    @SuppressWarnings("fallthrough")
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder();
        DataObject template = wiz.getTemplate();
        String makefileName;
        int pos = getMakefileData().getMakefileName().lastIndexOf(File.separatorChar);
        if (pos >= 0) {
            makefileName = getMakefileData().getMakefileName().substring(pos + 1);
        } else {
            makefileName = getMakefileData().getMakefileName();
        }

        DataObject result;
        result = template.createFromTemplate(targetFolder, makefileName);

        if (result != null) {
            MakeExecSupport mes = result.getCookie(MakeExecSupport.class);
            if (mes != null) {
                // Add known targets to node
                // Add "all", "clean", and user defined targets...
                mes.addMakeTargets("all");	// NOI18N
                List<TargetData> tlist = getMakefileData().getTargetList();
                for (int i = 0; i < tlist.size(); i++) {
                    TargetData t = tlist.get(i);
                    switch (t.getTargetType()) {
                        case TargetData.SIMPLE_EXECUTABLE:
                        case TargetData.SIMPLE_ARCHIVE:
                        case TargetData.SIMPLE_SHAREDLIB:
                        case TargetData.COMPLEX_EXECUTABLE:
                        case TargetData.COMPLEX_ARCHIVE:
                        case TargetData.COMPLEX_SHAREDLIB:
                            if (t.getOutputDirectory() != null && t.getOutputDirectory().length() > 0) {
                                mes.addMakeTargets(t.getOutputDirectory() + "/" + t.getName()); // NOI18N
                            } else {
                                mes.addMakeTargets(t.getName()); // NOI18N
                            }
                            break;
                        case TargetData.COMPLEX_MAKE_TARGET:
                        case TargetData.COMPLEX_CUSTOM_TARGET:
                            mes.addMakeTargets(t.getName());
                            break;
                    }
                }
                mes.addMakeTargets("clean");	// NOI18N

                // Set build (base) directory
                String makefileDir = getMakefileData().getMakefileDirName();
                String baseDir = getMakefileData().getBaseDirectory();
                String buildDirectory;
                if (makefileDir.equals(baseDir)) {
                    buildDirectory = ("."); // NOI18N
                } else {
                    buildDirectory = CndPathUtilities.getRelativePath(makefileDir, baseDir);
                }
                mes.setBuildDirectory(buildDirectory);

                String fullMakefilePath = result.getPrimaryFile().getPath();
                String fullBuildDirectoryPath = buildDirectory;
                int aIndex = fullMakefilePath.lastIndexOf(File.separatorChar);
                if (aIndex >= 0) {
                    FileObject parent = result.getPrimaryFile().getParent();
                    fullBuildDirectoryPath = CndPathUtilities.toAbsolutePath(parent, fullBuildDirectoryPath);
                }

                // Send creation event
                ArrayList<String> targets = new ArrayList<String>();
                ArrayList<String> executables = new ArrayList<String>();
                for (int i = 0; i < tlist.size(); i++) {
                    TargetData t = tlist.get(i);
                    String outputDirectory = t.getOutputDirectory();
                    if (outputDirectory == null || outputDirectory.length() == 0) {
                        outputDirectory = "."; // NOI18N
                    }
                    switch (t.getTargetType()) {
                        case TargetData.COMPLEX_EXECUTABLE:
                        case TargetData.SIMPLE_EXECUTABLE:
                            String fullTargetPath = null;

                            if (t.getName().charAt(0) == File.separatorChar) {
                                fullTargetPath = t.getName();
                            } else if (outputDirectory.charAt(0) == File.separatorChar) {
                                fullTargetPath = outputDirectory + File.separator + t.getName();
                            } else {
                                fullTargetPath = fullBuildDirectoryPath + File.separator + outputDirectory + File.separator + t.getName();
                            }
                            executables.add(fullTargetPath);
                        // fall through...
                        case TargetData.SIMPLE_ARCHIVE:
                        case TargetData.SIMPLE_SHAREDLIB:
                        case TargetData.COMPLEX_ARCHIVE:
                        case TargetData.COMPLEX_SHAREDLIB:
                            targets.add(t.getName());
                            break;
                        case TargetData.COMPLEX_MAKE_TARGET:
                        case TargetData.COMPLEX_CUSTOM_TARGET:
                            mes.addMakeTargets(t.getName());
                            break;
                    }
                }
                MakefileWizardEvent wizardEvent = new MakefileWizardEvent(
                        this,
                        MakefileWizardEvent.MAKEFILE_NEW,
                        fullMakefilePath,
                        getMakefileData().getBaseDirectory(),
                        "make", // NOI18N
                        targets.toArray(new String[targets.size()]),
                        executables.toArray(new String[executables.size()]));
                fireMakefileWizardEvent(wizardEvent);
            }
        }

        boolean done = onFinish();
        if (done) {
            OpenCookie open = result.getCookie(OpenCookie.class);
            if (open != null) {
                open.open();
            }
        } else {
            System.err.println("errors generating makefile..."); // FIXUP // NOI18N
        }
        return Collections.<DataObject>singleton(result);
    }

    /*
     * Returns relative (to mounted filesystem) path to fileobject fo. Recursive.
     * Same funtionality as folder.getNameExt, which is now deprecated.
     */
    private String dirPath(FileObject fo, String path) {
        if (fo.getParent() != null && fo.getParent().getName() != null && fo.getParent().getName().length() > 0) {
            path = fo.getParent().getName() + File.separatorChar + path;
            return dirPath(fo.getParent(), path);
        }
        return path;
    }

    /** Set initial data in dialog */
    public void initDirPaths() {
        // Get dir folder and makefile name from targetChooser panel (default wizard panel)
        String fullFolderName = null;
        try {
            DataFolder targetFolder = getTemplateWizard().getTargetFolder();
            FileObject fo = targetFolder.getPrimaryFile();
            fullFolderName = fo.getPath();
        } catch (IOException ioe) {
            // FIXUP
        }
        getMakefileData().setBaseDirectory(fullFolderName);
        getMakefileData().setMakefileDirName(fullFolderName);
    }

    private String uniqDefaultName(String dir, String makefileName) {
        String name = makefileName;
        File f = new File(dir + File.separator + name);
        int n = 1;
        while (f.exists()) {
            name = makefileName + "_" + n++; // NOI18N
            f = new File(dir + File.separator + name);
        }
        return name;
    }

    /** Update MakefileData if the data was changed */
    public void initMakefileName() {
        // Create and set makefile name based on name from targetChooser panel and basedirectory
        MakefileData md = getMakefileData();
        String makefileName = getTemplateWizard().getTargetName();
        String dir = CndPathUtilities.trimSlashes(md.getMakefileDirName());
        if (makefileName == null) {
            makefileName = uniqDefaultName(dir, "Makefile"); // NOI18N
        }
        String fullMakefileName = dir + File.separator + makefileName;

        String useMakefileName = null;
        if (dir.equals(md.getBaseDirectory()) || ".".equals(dir) || dir.length() == 0) // NOI18N
        {
            useMakefileName = makefileName;
        } else {
            useMakefileName = fullMakefileName;
        }
        md.setMakefileName(useMakefileName);
    }

    /** ---------------------------------------------------- */
    protected static void fireMakefileWizardEvent(MakefileWizardEvent e) {
        Vector<MakefileWizardListener> listeners = getListenerList();

        for (int i = listeners.size() - 1; i >= 0; i--) {
            (listeners.elementAt(i)).makefileCreated(e);
        }
    }

    private static Vector<MakefileWizardListener> getListenerList() {
        if (listenerList == null) {
            listenerList = new Vector<MakefileWizardListener>(0);
        }
        return listenerList;
    }

    public static void addMakefileWizardListener(MakefileWizardListener l) {
        getListenerList().add(l);
    }

    public static void removeMakefileWizardListener(MakefileWizardListener l) {
        getListenerList().remove(l);
    }
}

