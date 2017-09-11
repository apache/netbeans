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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form;

import java.util.*;
import java.beans.*;
import java.awt.event.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;

/**
 * Property implementation class for events of metacomponents.
 * (Events are treated as properties on Events tab of Component Inspector.)
 *
 * @author Tomas Pavek
 */

class EventProperty extends PropertySupport.ReadWrite {

    private static String NO_EVENT = FormUtils.getBundleString("CTL_NoEvent"); // NOI18N

    private static boolean somethingChanged; // flag for "postSetAction" relevance
    private static boolean invalidValueTried; // flag for "postSetAction" relevance

    private Event event;

    private String selectedEventHandler;

    EventProperty(Event event, String eventId) {
        super(eventId,
              String.class,
              event.getListenerMethod().getName(),
              event.getListenerMethod().getName());
        this.event = event;
        setShortDescription(
            event.getEventSetDescriptor().getListenerType().getName());
    }

    Event getEvent() {
        return event;
    }

    private FormEvents getFormEvents() {
        return event.getComponent().getFormModel().getFormEvents();
    }

    private java.lang.reflect.Method getListenerMethod() {
        return event.getListenerMethod();
    }

    String[] getEventHandlers() {
        return event.getEventHandlers();
    }

    // -------

    /** Getter for the value of the property. It returns name of the last
     * selected event handler (for property sheet), not the Event object.
     * @return String name of the selected event handler attached to the event
     */
    @Override
    public Object getValue() {
        if (selectedEventHandler == null && event.hasEventHandlers())
            selectedEventHandler = (String) event.getEventHandlerList().get(0);
        return selectedEventHandler;
    }

    void resetSelectedEventHandler(String candidate) {
        if ((selectedEventHandler != null) && (selectedEventHandler.equals(candidate))) {
            selectedEventHandler = null;
        }
    }

    /** Setter for the value of the property. It accepts String (for adding
     * new or renaming the last selected event handler), or Change object
     * (describing multiple changes in event handlers), or null (to refresh
     * property sheet due to a change in handlers made outside).
     */
    @Override
    public void setValue(Object val) {
        Change change = null;
        String newSelectedHandler = null;

        if (val instanceof Change) {
            change = (Change) val;
        }
        else if (val instanceof String) {
            String[] handlers = getEventHandlers();
            if (handlers.length > 0) {
                // there are already some handlers attached
                String current = selectedEventHandler != null ?
                                 selectedEventHandler : handlers[0];

                if ("".equals(val)) { // NOI18N
                    // empty String => remove current handler
                    change = new Change();
                    change.getRemoved().add(current);
                    for (int i=0; i < handlers.length; i++)
                        if (!handlers[i].equals(current)) {
                            newSelectedHandler = handlers[i];
                            break;
                        }
                }
                else { // non-empty String => rename current handler
                    newSelectedHandler = (String) val;

                    boolean ignore = false;
                    for (int i=0; i < handlers.length; i++)
                        if (handlers[i].equals(val)) { // not a new name
                            ignore = true;
                            break;
                        }

                    if (!ignore) { // do rename
                        change = new Change();
                        change.getRenamedNewNames().add((String)val);
                        change.getRenamedOldNames().add(current);
                    }
                }
            }
            else { // no handlers yet, add a new one
                if (!"".equals(val)) {
                    change = new Change();
                    change.getAdded().add((String)val);
                    newSelectedHandler = (String) val;
                }
            }
        }
        else if (val == null) {
            if (selectedEventHandler == null)
                return;
        }
        else throw new IllegalArgumentException();

        if (change != null) {
            somethingChanged = true; // something was changed

            FormEvents formEvents = getFormEvents();

            if (change.hasRemoved()) // some handlers to remove
                for (Iterator it=change.getRemoved().iterator(); it.hasNext(); )
                    formEvents.detachEvent(event, (String) it.next());

            if (change.hasRenamed()) // some handlers to rename
                for (int i=0; i < change.getRenamedOldNames().size(); i++) {
                    String oldName = change.getRenamedOldNames().get(i);
                    String newName = change.getRenamedNewNames().get(i);

                    try {
                        formEvents.renameEventHandler(oldName, newName);

                        // hack: update all properties using the renamed handler
                        Event[] events = formEvents.getEventsForHandler(newName);
                        for (int j=0 ; j < events.length; j++) {
                            Node.Property prop = events[j].getComponent()
                                                  .getPropertyByName(getName());
                            if (prop != null && prop != this) {
                                try {
                                    if (oldName.equals(prop.getValue()))
                                        prop.setValue(newName);
                                }
                                catch (Exception ex) { // should not happen
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    catch (IllegalArgumentException ex) { // name already used
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        newSelectedHandler = null;
                    }
                }

            if (change.hasAdded()) // some handlers to add
                for (Iterator it=change.getAdded().iterator(); it.hasNext(); ) {
                    try {
                        formEvents.attachEvent(event, (String) it.next(), null);
                    }
                    catch (IllegalArgumentException ex) { // name already used
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        newSelectedHandler = null;
                    }
                }
        }

        selectedEventHandler = newSelectedHandler;

        RADComponentNode node = event.getComponent().getNodeReference();
        if (node != null)
            node.firePropertyChangeHelper(getName(), null, null);
    }

    @Override
    public Object getValue(String key) {
        if ("canEditAsText".equals(key)) // NOI18N
            return Boolean.TRUE;

        if ("postSetAction".equals(key)) // NOI18N
            return new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    // if Enter was pressed without echange or existing handler
                    // chosen, switch to editor
                    if (!somethingChanged && !invalidValueTried && (selectedEventHandler != null)) {
                        getFormEvents().attachEvent(event,
                                                    selectedEventHandler,
                                                    null);
                    }
                    somethingChanged = false;
                }
            };

        return super.getValue(key);
    }


//    public String getDisplayName() {
//        String displayName = super.getDisplayName();
//        if (selectedEventHandler != null)
//            displayName = "<html><b>" + displayName + "</b>"; // NOI18N
//        return displayName;
//    }
//
    @Override
    public boolean canWrite() {
        return !isReadOnly();
    }

    private boolean isReadOnly() {
        return event.getComponent().isReadOnly();
    }

    /** Returns property editor for this property.
     * @return the property editor for adding/removing/renaming event handlers
     */
    @Override
    public PropertyEditor getPropertyEditor() {
        return new EventEditor();
    }

    // --------

    /** Helper class describing changes in event handlers attached to an event.
     */
    static class Change {
        boolean hasAdded() {
            return added != null && added.size() > 0;
        }
        boolean hasRemoved() {
            return removed != null && removed.size() > 0;
        }
        boolean hasRenamed() {
            return renamedOldName != null && renamedOldName.size() > 0;
        }
        List<String> getAdded() {
            if (added == null)
                added = new ArrayList<String>();
            return added;
        }
        List<String> getRemoved() {
            if (removed == null)
                removed = new ArrayList<String>();
            return removed;
        }
        List<String> getRenamedOldNames() {
            if (renamedOldName == null)
                renamedOldName = new ArrayList<String>();
            return renamedOldName;
        }
        List<String> getRenamedNewNames() {
            if (renamedNewName == null)
                renamedNewName = new ArrayList<String>();
            return renamedNewName;
        }
        private List<String> added;
        private List<String> removed;
        private List<String> renamedOldName;
        private List<String> renamedNewName;
    }

    // --------

    private class EventEditor extends PropertyEditorSupport {

        @Override
        public String getAsText() {
            if (this.getValue() == null) {
                return NO_EVENT;
            }
            return this.getValue().toString();
        }

        @Override
        public void setAsText(String txt) {
            if (NO_EVENT.equals(txt) && (getValue() == null)) {
                invalidValueTried = false;
                setValue(null);
                return;
            }
            if (!"".equals(txt) && !Utilities.isJavaIdentifier(txt)) { // NOI18N
                // invalid handler name entered
                invalidValueTried = true;
                IllegalArgumentException iae = new IllegalArgumentException();
                String annotation = FormUtils.getFormattedBundleString(
                                        "FMT_MSG_InvalidJavaIdentifier", // NOI18N
                                        new Object [] { txt } );
                ErrorManager.getDefault().annotate(
                    iae, ErrorManager.ERROR, "Not a java identifier", // NOI18N
                    annotation, null, null);
                throw iae;
            }
            if ("".equals(txt) && (this.getValue() == null)) {
                // empty string entered when no event handler exist
                invalidValueTried = true;
                IllegalArgumentException iae = new IllegalArgumentException();
                String emptyStringTxt = FormUtils.getBundleString("FMT_MSG_EmptyString"); // NOI18N
                String annotation = FormUtils.getFormattedBundleString(
                                        "FMT_MSG_InvalidJavaIdentifier", // NOI18N
                                        new Object [] { emptyStringTxt } );
                ErrorManager.getDefault().annotate(
                    iae, ErrorManager.ERROR, "Not a java identifier", // NOI18N
                    annotation, null, null);
                throw iae;
            }
            invalidValueTried = false;
            this.setValue(txt);
        }

        @Override
        public String[] getTags() {
            String[] handlers = getEventHandlers();
            if ((handlers.length == 0) && (getValue() == null)) {
                handlers = new String[] {getFormEvents().findFreeHandlerName(event, event.getComponent())};
            }
            return handlers.length > 0 ? handlers : null;
        }

        @Override
        public boolean supportsCustomEditor() {
            return isReadOnly() ? false : true;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            if (isReadOnly())
                return null;

            final EventCustomEditor ed = new EventCustomEditor(EventProperty.this);
            DialogDescriptor dd = new DialogDescriptor(
                ed,
                FormUtils.getFormattedBundleString(
                    "FMT_MSG_HandlersFor", // NOI18N
                    new Object [] { getListenerMethod().getName() }),
                true,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                            ed.doChanges();
                        }
                    }
                });

            return DialogDisplayer.getDefault().createDialog(dd);
        }
    }
}
