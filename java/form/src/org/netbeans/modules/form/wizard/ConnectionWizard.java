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

package org.netbeans.modules.form.wizard;

import java.beans.*;
import java.util.HashSet;
import javax.swing.event.*;
import java.lang.reflect.Method;

import java.util.Set;
import org.openide.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;


/** This class manages the Connection Wizard. The wizard has three (or two)
 * steps in which user can specify event on source component, operation
 * on target component and (if needed) parameters of the operation (method).
 *
 * @author Tomas Pavek
 */
public class ConnectionWizard extends WizardDescriptor {

    ConnectionIterator iterator;

    private boolean finished = false;  // whether the wizard was finished successfully

    private Event selEvent;         // selected activating event
    private String eventName;       // selected event handler name
    private int actionType;         // type of action on target
    private Method targetMethod;    // method selected to be invoked on target
    private String paramsText;      // text of parameters for target method
    private Object[] paramValues;   // values for target method's parameters

    // constructor
    public ConnectionWizard(FormModel model, RADComponent source, RADComponent target) {
        this(model, source, target, 
             new ConnectionIterator(model, source, target));
    }

    // constructor
    private ConnectionWizard(FormModel model, RADComponent source, RADComponent target, 
                             ConnectionIterator it) {
        super(it);
        iterator = it;
        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        setTitle(NbBundle.getBundle(ConnectionWizard.class).getString("CTL_CW_Title")); // NOI18N
        java.text.MessageFormat format = new java.text.MessageFormat("{0}"); // NOI18N
        setTitleFormat(format);
    }

    /** Shows the wizard to the user.
     * @returns whether the wizard was finished (not canceled)
     */
    public boolean show() {
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog(this);

        finished = false;
        d.setVisible(true);
        finished = getValue() == FINISH_OPTION;

        if (finished) {
            selEvent = iterator.panel1.getSelectedEvent();
            eventName = iterator.panel1.getEventName();
            actionType = iterator.panel2.getActionType();
            targetMethod = iterator.getMethodFromPanel2();
            paramsText = iterator.anyParameters() ?
                         iterator.panel3.getParametersText() : null;
            paramValues = iterator.anyParameters() ?
                          iterator.panel3.getParameters() : null;
        }

        d.dispose();

        return finished;
    }

    /** @returns whether the wizard was finished (processed and not canceled)
     */
    public boolean isFinished() {
        return finished;
    }

    // -----------------

    public RADComponent getSource() {
        return iterator.source;
    }

    public RADComponent getTarget() {
        return iterator.target;
    }

    public FormModel getFormModel() {
        return iterator.formModel;
    }

    public Event getSelectedEvent() {
        return finished ? selEvent : null;
    }

    public String getEventName() {
        return finished ? eventName : null;
    }

    /** @returns source code for the connection
     */
    public String getGeneratedCode() {
        if (!finished || actionType == ConnectionWizardPanel2.CODE_TYPE)
            return null;

        StringBuilder buf = new StringBuilder();
        Set<Class> allExceptions = new HashSet<Class>();

        // params can be specified as method calls which in turn may produce exceptions
        if (paramValues != null) {
            for (int i=0; i < paramValues.length; i++) {
                if (paramValues[i] instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                    RADConnectionPropertyEditor.RADConnectionDesignValue val = (RADConnectionPropertyEditor.RADConnectionDesignValue) paramValues[i];
                    if (val.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD) {
                        Class [] except = val.getMethod().getMethod().getExceptionTypes();
                        for (int j=0; j < except.length; j++) {
                            allExceptions.add(except[j]);
                        }
                    }
                }
            }
        }

        Class[] methodExceptions = targetMethod.getExceptionTypes();
        for (int k=0; k < methodExceptions.length; k++) {
            allExceptions.add(methodExceptions[k]);
        }

        // if either the setter or some of the methods that get parameters 
        // throw checked exceptions, we must generate try/catch block around it
        if (allExceptions.size() > 0) {
            buf.append("try {\n  "); // NOI18N
        }

        if (getTarget() != getFormModel().getTopRADComponent()) { // not generated for the form
            buf.append(getTarget().getName());
            buf.append("."); // NOI18N
        }
        buf.append(targetMethod.getName());
        buf.append("("); // NOI18N
        if (paramsText != null) 
            buf.append(paramsText);
        buf.append(");\n"); // NOI18N

        int varCount = 1;

        // add the catch for all checked exceptions
        for (java.util.Iterator it = allExceptions.iterator(); it.hasNext(); ) {
            Class exceptionClass = (Class) it.next();
            buf.append("} catch ("); // NOI18N
            buf.append(exceptionClass.getName());
            buf.append(" "); // NOI18N
            String excName = "e"+varCount; // NOI18N
            varCount++;
/*            VariablePool varPool = getFormModel().getVariablePool();
            while (varPool.isReserved(excName)) {
                excName = "e"+varCount; // NOI18N
                varCount++;
            } */
            buf.append(excName);
            buf.append(") {\n"); // NOI18N
            buf.append("  ").append(excName); // NOI18N
            buf.append(".printStackTrace();\n"); // NOI18N
        }
        if (!allExceptions.isEmpty())
            buf.append("}\n"); // NOI18N

        return buf.toString();
    }

    @Override
    protected void updateState() {
        super.updateState();
        java.util.ResourceBundle bundle = NbBundle.getBundle(ConnectionWizard.class);
        if (iterator.getPanelsCount() > 2) {
            putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                new String[] {
                    bundle.getString("CTL_CW_Step1_Title"), // NOI18N
                    bundle.getString("CTL_CW_Step2_Title"), // NOI18N
                    bundle.getString("CTL_CW_Step3_Title") // NOI18N
                }
            );
        } else {
            putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                new String[] {
                    bundle.getString("CTL_CW_Step1_Title"), // NOI18N
                    bundle.getString("CTL_CW_Step2_Title") // NOI18N
                }
            );
        }            
    }
    
    /** This class manages connection wizard panels.
     */
    static class ConnectionIterator implements WizardDescriptor.Iterator, ChangeListener {

        FormModel formModel;
        RADComponent source;
        RADComponent target;

        ConnectionWizardPanel1 panel1;
        ConnectionWizardPanel2 panel2;
        ConnectionWizardPanel3 panel3;
        WizardDescriptor.Panel[] panels;

        boolean panel2Changed = false;

        int stage;

        private EventListenerList listenerList = null;

        // constructor
        ConnectionIterator(FormModel model,
                           RADComponent source,
                           RADComponent target)
        {
            formModel = model;
            this.source = source;
            this.target = target;

            stage = 1;

            panel1 = new ConnectionWizardPanel1(source);
            panel2 = new ConnectionWizardPanel2(target);
            panel2.addChangeListener(this);
            panel3 = new ConnectionWizardPanel3(formModel);
            panels = new WizardDescriptor.Panel[] { panel1, panel2, panel3 };
        }

        public int getPanelsCount() {
            // the number of panels depends on whether any parameters must 
            // be entered (in the last panel)
            return anyParameters() ? 3 : 2;
        }

        @Override
        public WizardDescriptor.Panel current() {
            return panels[stage-1];
        }

        @Override
        public boolean hasNext() {
            return stage < getPanelsCount();
        }

        @Override
        public boolean hasPrevious() {
            return stage > 1;
        }

        @Override
        public java.lang.String name() {
            return ""; // NOI18N
        }

        @Override
        public void nextPanel() {
            if (stage < getPanelsCount()) {
                if (stage == 1 && panel1.handlerAlreadyExists()) {
                    if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(
                              NbBundle.getBundle(ConnectionWizard.class)
                                        .getString("MSG_RewritingEvent"), // NOI18N
                              NotifyDescriptor.OK_CANCEL_OPTION,
                              NotifyDescriptor.WARNING_MESSAGE))
                          == NotifyDescriptor.CANCEL_OPTION)
                        return;
                }

                stage++;

                if (stage == 3 && panel2Changed) {
                    Method method = getMethodFromPanel2();

                    if (method != null)
                        panel3.setMethod(method);

                    panel2Changed = false;
                }
            }
        }

        @Override
        public void previousPanel() {
            if (stage > 1)
                stage--;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            if (listenerList == null)
                listenerList = new EventListenerList();
            listenerList.add(ChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            if (listenerList != null)
                listenerList.remove(ChangeListener.class, listener);
        }

        @Override
        public void stateChanged(ChangeEvent p1) {
            if (stage == 2) {
                panel2Changed = true;
            }
        }

        private Method getMethodFromPanel2() {
            Method method = null;

            if (panel2.getActionType() == ConnectionWizardPanel2.METHOD_TYPE) {
                MethodDescriptor desc = panel2.getSelectedMethod();
                if (desc != null)
                    method = desc.getMethod();
            }
            else if (panel2.getActionType() == ConnectionWizardPanel2.PROPERTY_TYPE) {
                PropertyDescriptor desc = panel2.getSelectedProperty();
                if (desc != null)
                    method = desc.getWriteMethod();
            }

            return method;
        }

        private boolean anyParameters() {
            Method m = getMethodFromPanel2();
            return m != null && m.getParameterTypes().length > 0;
        }
    }
}
