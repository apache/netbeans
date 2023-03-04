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
package org.netbeans.modules.css.visual;

import java.awt.BorderLayout;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorNode.DeclarationProperty;
import org.netbeans.modules.css.visual.actions.AddPropertyAction;
import org.netbeans.modules.css.visual.actions.GoToSourceAction;
import org.netbeans.modules.css.visual.actions.RemovePropertyAction;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.EditCSSRulesAction;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.ViewMode;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Rule editor panel is a {@link JPanel} component which can be embedded in the
 * client's UI.
 *
 * It can be controlled and observed via {@link RuleEditorPanelController} and
 * {@link PropertyChangeListener}.
 *
 * Open questions/todo-s: -----------------------
 *
 * 2) (P4) related to #1 is how to listen on events happening over the sheet -
 * implementing the mouse hover based "disable" action (maybe not necessary
 * since doesn't make much sense for the rule editor).
 *
 * 3) (P2) add own (propagate the filters) popup menu to the sheet
 *
 * 4) (P4) (#EA) can property categories be programmatically collapsed/expanded?
 *
 * 5) (P3) in the unsorted mode, can be the categories disabled? They seem to
 * disappear only in the "sort by alpha" mode
 *
 * Enhancements: -------------- A) if categorized view enabled, the category
 * name containing a physical properties could be in bold font and the rest is
 * collapsed (possibly configurable by a toolbar toggle)
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "titleLabel.text={0} properties",
    "titleLabel.text.no.selected.rule=No Rule Selected",
    "titleLabel.tooltip.no.selected.rule=Select a css rule in editor or CSS Styles Window to activate the Rule Editor",
    "titleLabel.no.selected.rule=No Rule Selected",
    "label.rule.error.tooltip=The selected rule contains error(s), the listed properties are read only",
    "addPropertyCB.initial.text=Add Property ..."
})
public class RuleEditorPanel extends JPanel {

    static final Logger LOG = Logger.getLogger("rule.editor"); //NOI18N
    
    static RequestProcessor RP = new RequestProcessor(CssCaretAwareSourceTask.class);
    
    private static final Icon ERROR_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/error-glyph.gif")); //NOI18N
    private static final Icon APPLIED_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/database.gif")); //NOI18N
    
    private final JLabel errorLabel, appliedLabel;

    private REPropertySheet sheet;
    private Model model;
    private Rule rule;
    private Action addPropertyAction;
    private Action[] actions;
    private RuleEditorViews views;
    private CustomToolbar toolbar;
    private ViewMode viewMode;
    public RuleEditorNode node;
    private final PropertyChangeSupport CHANGE_SUPPORT = new PropertyChangeSupport(this);
    private boolean addPropertyMode;
   
    private PropertyDeclaration createdDeclaration;
    private PropertyDeclaration editedDeclaration;
    private final List<String> createdDeclarationsIdsList = new ArrayList<>();
    
    private final PropertyChangeListener MODEL_LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    switch (evt.getPropertyName()) {
                        case Model.NO_CHANGES_APPLIED_TO_DOCUMENT:
                            //Model.applyChanges() requested, but no changes were done,
                            //select & edit the property corresponding to the created declaration
                            node.fireContextChanged(false);
                            break;
                        case Model.CHANGES_APPLIED_TO_DOCUMENT:
                            northWestPanel.add(appliedLabel);
                            northWestPanel.revalidate();
                            northWestPanel.repaint();
                            //XXX this should not be called in EDT, but due to the current "increment support" design it has to.
                            refreshModel();
                            break;
                        case Model.MODEL_WRITE_TASK_FINISHED:
                            if (createdDeclaration != null) {
                                //select & edit the property corresponding to the created declaration
                                node.fireContextChanged(false);
                                editCreatedDeclaration();
                            }
                            break;
                    }
                }
            });
        }
    };
    
    public RuleEditorPanel() {
        this(false);
    }

    public RuleEditorPanel(boolean addPropertyMode) {
        this.addPropertyMode = addPropertyMode;
        
        //initialize actions
        addPropertyAction = new AddPropertyAction(this);

        //init default components
        initComponents();

        errorLabel = new JLabel(ERROR_ICON);
        errorLabel.setToolTipText(Bundle.label_rule_error_tooltip());
        appliedLabel = new JLabel(APPLIED_ICON);
        
        node = new RuleEditorNode(this);

        viewMode = addPropertyMode ? ViewMode.CATEGORIZED : ViewMode.UPDATED_ONLY; //default view
        views = new RuleEditorViews(this);

        //create toolbar
        toolbar = new CustomToolbar();
        
        if(!addPropertyMode) {
            toolbar.addButton(filterToggleButton);
            toolbar.addLineSeparator();
            toolbar.addButton(views.getUpdatedOnlyToggleButton());
            toolbar.addSpaceSeparator();
        } else {
            toolbar.addLineSeparator();
        }
        toolbar.addButton(views.getCategorizedToggleButton());
        toolbar.addSpaceSeparator();
        toolbar.addButton(views.getAllToggleButton());
        
        //keep actions status
        addRuleEditorListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(RuleEditorController.PropertyNames.RULE_SET.name())) {
                    addPropertyAction.setEnabled(evt.getNewValue() != null && model.canApplyChanges());
                }
            }
        });

        Action[] viewActions = new ViewActions(views).getActions();
        
        actions = new Action[]{
            addPropertyAction,
            null,
            viewActions[0],
            viewActions[1],
            viewActions[2]
        };

        //custom popop for the whole panel
        //TODO possibly use some NB way, but I don't know it, no time for exploring now...
        JPopupMenu pm = new JPopupMenu();
        for (Action action : actions) {
            if (action != null) {
                if (action instanceof Presenter.Popup) {
                    pm.add(((Presenter.Popup) action).getPopupPresenter());
                } else {
                    pm.add(action);
                }
            } else {
                pm.addSeparator();
            }
        }
       
        //the popup menu for the "build toolbar button"
        final JPopupMenu buildButtonPopup = new JPopupMenu();
        
        if (!addPropertyMode) {
            setComponentPopupMenu(pm);
            
            buildButtonPopup.add(addPropertyAction);
            
            toolbar.addLineSeparator();
            toolbar.addButton(createPropertyToggleButton);
            
        }

        titleLabel.setText(null);

        //add the property sheet to the center
        sheet = new REPropertySheet(buildButtonPopup);
        try {
            sheet.setSortingMode(PropertySheet.UNSORTED);
        } catch (PropertyVetoException ex) {
            //no-op
        }
        sheet.setPopupEnabled(true);
        sheet.setDescriptionAreaVisible(false);
        sheet.setNodes(new Node[]{node});

        add(sheet, BorderLayout.CENTER);

        //add document listener to the filter text field 
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
 
            private void contentChanged() {
                node.setFilterText(filterTextField.getText());
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        
        setFilterVisible(addPropertyMode);
        
        northEastPanel.add(toolbar, BorderLayout.WEST);
       
    }
    
    /**
     * Explicitly refreshes the CSS Source Model.
     */
    public void refreshModel() {
        if (model != null) {
            Document doc = model.getLookup().lookup(Document.class);
            if (doc != null) {
                try {
                    Source source = Source.create(doc);
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                            if (resultIterator != null) {
                                CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                                final Model model = Model.getModel(result);
                                LOG.log(Level.FINE, "Model.CHANGES_APPLIED_TO_DOCUMENT event handler - setting new model {0}", model);
                                setModel(model);
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    //called fro the containing TC's componentDeactivated();
    public void componentDeactivated() {
        //Support for clearing the "created declarations list".
        //
        //When user adds new properties using the "Add Property" item 
        //at the end of the PS the items stays at the positions unsorted
        //At some point we need to resort the items according to their 
        //alphabetical order - lets do that when the TopComponent containing
        //rhe RuleEditor panel lost focus.
        createdDeclarationsIdsList.clear();
        createdDeclaration = null;
        node.fireContextChanged(true);
    }
    
    public FeatureDescriptor getSelected() {
        return sheet.getSelectedFeatureDescriptor();
    }
    
    void setCreatedDeclaration(Rule rule, PropertyDeclaration declaration) {
        createdDeclaration = declaration;
        String declarationId = PropertyUtils.getDeclarationId(rule, declaration);
        createdDeclarationsIdsList.add(declarationId);
    }
    
    /**
     * User used "Add Property" item to add a new property, but then in the value, 
     * pressed esc.
     * 
     * So we need to remove the latest declaration from the model as it has no value.
     * 
     */
    public void disposeEditedDeclaration() {
       final PropertyDeclaration remove = editedDeclaration;
       if(remove != null) {
           //1.remove from model
           model.runWriteTask(new Model.ModelTask() {
               @Override
               public void run(StyleSheet styleSheet) {
                   Declaration declaration = (Declaration)remove.getParent();
                   Declarations declarations = (Declarations)declaration.getParent();
                   declaration.removeElement(remove);
                   declarations.removeDeclaration(declaration);
               }
           });
           node.fireContextChanged(true);
       }
    }
    
    public void editingFinished() {
        editedDeclaration = null;
    }
    
    PropertyDeclaration getCreatedDeclaration() {
        return this.createdDeclaration;
    }
    
    List<String> getCreatedDeclarationsIdsList() {
        return createdDeclarationsIdsList;
    }
    
    private void editCreatedDeclaration() {
        DeclarationProperty descriptor = node.getDeclarationProperty(createdDeclaration);
        if(descriptor != null) {
            sheet.requestFocus();
    //        sheet.select(descriptor, true);
            try {
                call_PropertySheet_select(sheet, descriptor, true);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }

            editedDeclaration = createdDeclaration;
            createdDeclaration = null;
        }
    }
    
    private void call_PropertySheet_select(PropertySheet sheet, FeatureDescriptor descriptor, boolean edit) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //private so far, will be public later
        Class clz = PropertySheet.class;
        Method select_method = clz.getDeclaredMethod("select", FeatureDescriptor.class, boolean.class); //NOI18N
        select_method.setAccessible(true);
        select_method.invoke(sheet, descriptor, edit);
    }

    public boolean isAddPropertyMode() {
        return addPropertyMode;
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode mode) {
        if (this.viewMode == mode) {
            return; //no change
        }
        this.viewMode = mode;
        node.fireContextChanged(true);
    }

    public Model getModel() {
        return model;
    }
    
    public void releaseModel() {
        if(model == null) {
            return ;
        }
        setNoRuleState();
        model.removePropertyChangeListener(MODEL_LISTENER);
        this.model = null;
    }

    //runs in EDT
    public void setModel(final Model model) {
        assert SwingUtilities.isEventDispatchThread();
        if (model == null) {
            throw new NullPointerException();
        }

        if (this.model != null) {
            //new model for the same file, check if the model is not the same
            //as the current one
            if(model.getSerialNumber() == this.model.getSerialNumber()) {
                LOG.log(Level.FINE, "attempt to set the same model");
                return; //no change
            }
            
            //check if the set model is not even older than the curren one
            //if the model is for the same file
            FileObject old = this.model.getLookup().lookup(FileObject.class);
            FileObject neww = model.getLookup().lookup(FileObject.class);
            assert old != null; 
            assert neww != null;
            if(neww != null && neww.equals(old)) {
                if(model.getSerialNumber() < this.model.getSerialNumber()) { //or even older!
                    LOG.log(Level.WARNING, "attempt to set the older model {0} while the current is {1}!!!", new Object[]{model, this.model});
                    return; //no change
                }
            }
            
            this.model.removePropertyChangeListener(MODEL_LISTENER);
        }

        final Model oldModel = this.model;
        final Rule oldRule = this.rule;

        this.model = model;
        LOG.log(Level.FINE, "set new model ({0})", model);

        this.model.addPropertyChangeListener(MODEL_LISTENER);

        //remove the "applied changes mark"
        northWestPanel.remove(appliedLabel);
        northWestPanel.validate();
        northWestPanel.repaint();

        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.MODEL_SET.name(), oldModel, this.model);

        //update the context in create rule action
        EditCSSRulesAction.getDefault().setContext(model.getLookup().lookup(FileObject.class));
        
        if (this.rule != null) {
            //resolve the old rule from the previous model to corresponding rule in the new model
            final AtomicReference<Rule> rule_ref = new AtomicReference<>();
            this.model.runReadTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    ModelUtils utils = new ModelUtils(model);
                    rule_ref.set(utils.findMatchingRule(oldModel, oldRule));
                }
            });
            
            Rule match = rule_ref.get();
            if (match == null) {
                setNoRuleState();
            } else {
                setRule(match);
            }
            
            //isn't this unnecessary as we already called setNoRuleState() or setRule(...)?!?!
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, match);

        } else {
            LOG.log(Level.FINER, "no rule was set before");
            //no rule was set - fire event anyway
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, rule);
        }

        //do not fire change event since it is required
        //to call setRule(...) subsequently which will 
        //fire the change even
    }

    public Rule getRule() {
        return rule;
    }
    
    public void setRule(final Rule rule) {
        assert SwingUtilities.isEventDispatchThread();
        if (rule == null) {
            throw new NullPointerException();
        }
        if (model == null) {
            throw new IllegalStateException("you must call setModel(Model model) beforehand!"); //NOI18N
        }

        Model ruleModel = rule.getModel();
        if(ruleModel != this.model) {
            LOG.log(Level.FINE, "attempt to set rule from different model {0}, while the current is {1}!", new Object[]{ruleModel, this.model});
            return; //no change
        }
        
        if (this.rule == rule) {
            LOG.log(Level.FINE, "attempt to set the same rule");
            return; //no change
        }
        Rule old = this.rule;
        this.rule = rule;
        LOG.log(Level.FINE, "set new rule ({0})", rule);
        
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, this.rule);

        northWestPanel.revalidate();

        //force property sets refresh if the rule is erroneous or 
        //the rule is valid, but the previous was erroneous.
        node.fireContextChanged(old == null || !(old.isValid() && rule.isValid())); 

        final AtomicReference<String> ruleNameRef = new AtomicReference<>();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet stylesheet) {
                ruleNameRef.set(model.getElementSource(rule.getSelectorsGroup()).toString());
            }
        });
        titleLabel.setText(Bundle.titleLabel_text(ruleNameRef.get()));
        titleLabel.setToolTipText(null);
        titleLabel.setEnabled(true);

        messageLabel.setText(null);
    }

    public void setNoRuleState() {
        LOG.log(Level.FINER, "setNoRuleState()");

        assert SwingUtilities.isEventDispatchThread();
        Rule old = this.rule;
        this.rule = null;
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, null);
 
        titleLabel.setText(Bundle.titleLabel_no_selected_rule());
        titleLabel.setToolTipText(Bundle.titleLabel_tooltip_no_selected_rule());
        titleLabel.setEnabled(false);
        
        messageLabel.setText(null);
        
        node.fireContextChanged(false);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setDeclarationInfo(PropertyDeclaration declaration, DeclarationInfo declarationInfo) {
        node.fireDeclarationInfoChanged(declaration, declarationInfo);
    }

    /**
     * Registers an instance of {@link PropertyChangeListener} to the component.
     *
     * @param listener
     */
    public final void addRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters an instance of {@link PropertyChangeListener} from the
     * component.
     *
     * @param listener
     */
    public final void removeRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }

    public Action[] getActions() {
        return actions;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelFilterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        filterToggleButton = new javax.swing.JToggleButton();
        createPropertyToggleButton = new javax.swing.JToggleButton();
        northPanel = new javax.swing.JPanel();
        northEastPanel = new javax.swing.JPanel();
        northWestPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel() {
            @Override
            public java.awt.Dimension getMinimumSize() {
                // Issue 234489
                java.awt.Dimension dim = super.getMinimumSize();
                return new java.awt.Dimension(0, dim.height);
            }
        };
        messageLabel = new javax.swing.JLabel();

        cancelFilterLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/cancel.png"))); // NOI18N
        cancelFilterLabel.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.cancelFilterLabel.text")); // NOI18N
        cancelFilterLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelFilterLabelMouseClicked(evt);
            }
        });

        filterTextField.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.filterTextField.text")); // NOI18N
        filterTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "DocumentViewPanel.filterToggleButton.toolTipText")); // NOI18N
        filterTextField.setMaximumSize(new java.awt.Dimension(32767, 32767));
        filterTextField.setMinimumSize(new java.awt.Dimension(60, 28));

        filterToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/find.png"))); // NOI18N
        filterToggleButton.setText(null);
        filterToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "DocumentViewPanel.filterToggleButton.toolTipText")); // NOI18N
        filterToggleButton.setFocusable(false);
        filterToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterToggleButtonActionPerformed(evt);
            }
        });

        createPropertyToggleButton.setAction(addPropertyAction);
        createPropertyToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/newProperty.png"))); // NOI18N
        createPropertyToggleButton.setText(null);
        createPropertyToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.createPropertyToggleButton.toolTipText")); // NOI18N
        createPropertyToggleButton.setFocusable(false);
        createPropertyToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPropertyToggleButtonActionPerformed(evt);
            }
        });

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.BorderLayout());

        northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        northPanel.setLayout(new java.awt.BorderLayout());

        northEastPanel.setLayout(new java.awt.BorderLayout());
        northPanel.add(northEastPanel, java.awt.BorderLayout.EAST);

        northWestPanel.setLayout(new javax.swing.BoxLayout(northWestPanel, javax.swing.BoxLayout.LINE_AXIS));

        titleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        titleLabel.setPreferredSize(new java.awt.Dimension(100, 16));
        northWestPanel.add(titleLabel);

        northPanel.add(northWestPanel, java.awt.BorderLayout.CENTER);

        messageLabel.setForeground(new java.awt.Color(255, 0, 0));
        northPanel.add(messageLabel, java.awt.BorderLayout.PAGE_END);

        add(northPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelFilterLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelFilterLabelMouseClicked
        //clear out the filter text, this will fire change event and clear the filter text in the node.
        filterTextField.setText(null);
    }//GEN-LAST:event_cancelFilterLabelMouseClicked

    private void setFilterVisible(boolean visible) {
        northWestPanel.removeAll();
        if(visible) {
            //update the UI
            northWestPanel.add(filterTextField, BorderLayout.CENTER);
            cancelFilterLabel.setBorder(new EmptyBorder(0,4,0,0));
            if(addPropertyMode) {
                northWestPanel.add(cancelFilterLabel, BorderLayout.WEST);
            }
            //set the filter text to the node
            node.setFilterText(filterTextField.getText());

            filterTextField.requestFocus();
        } else {
            //update the UI
            northWestPanel.add(titleLabel);
            
            //just remove the filter text from the node, but keep it in the field
            //so next time it is opened it will contain the old value
            node.setFilterText(null);
        }
        northWestPanel.revalidate();
        northWestPanel.repaint();
    }
    
    private void filterToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterToggleButtonActionPerformed
        setFilterVisible(filterToggleButton.isSelected());
        
    }//GEN-LAST:event_filterToggleButtonActionPerformed

    private void createPropertyToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPropertyToggleButtonActionPerformed
        createPropertyToggleButton.setSelected(false);
    }//GEN-LAST:event_createPropertyToggleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cancelFilterLabel;
    private javax.swing.JToggleButton createPropertyToggleButton;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JToggleButton filterToggleButton;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel northEastPanel;
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel northWestPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    private class REPropertySheet extends PropertySheet {

        private final JPopupMenu genericPopupMenu;

        public REPropertySheet(JPopupMenu genericPopupMenu) {
            this.genericPopupMenu = genericPopupMenu;
        }
        
        public FeatureDescriptor getSelectedFeatureDescriptor() {
            return super.getSelection();
        }
        
        @Override
        protected JPopupMenu createPopupMenu() {
            FeatureDescriptor fd = getSelection();
            if (fd != null) {
                if (fd instanceof RuleEditorNode.DeclarationProperty) {
                    //property
                    //
                    //actions:
                    //remove
                    //hide
                    //????
                    //custom popop for the whole panel
                    JPopupMenu pm = new JPopupMenu();

                    if(!addPropertyMode) {
                        pm.add(new GoToSourceAction(RuleEditorPanel.this, (RuleEditorNode.DeclarationProperty) fd));
                        pm.addSeparator();
                        pm.add(new RemovePropertyAction(RuleEditorPanel.this, (RuleEditorNode.DeclarationProperty) fd));
                    }

                    return pm;

                } else if (fd instanceof RuleEditorNode.PropertyCategoryPropertySet) {
                    //property category
                    //TODO possibly add "add property" action which would
                    //preselect the css category in the "add property dialog".
                }
            }

            //no context popup - create the generic popup
            return genericPopupMenu;
        }

        
    }
}
