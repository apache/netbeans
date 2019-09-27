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
package org.openide.awt;

import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EventListener;
import javax.swing.Action;
import org.openide.util.actions.Presenter;

/**
 * Specifies that the action behaviour is conditional and how the action should obtain the
 * state for its presentation. The annotation is used as value for {@link ActionRegistration#enabledOn}
 * and {@link ActionRegistration#checkedOn} to control action's enabled or checked state. The annotation
 * can be only applied on <b>context actions</b>, which have a single parameter constructor,
 * which accept the model object - see {@link Actions#context(java.lang.Class, boolean, boolean, org.openide.util.ContextAwareAction, java.lang.String, java.lang.String, java.lang.String, boolean)}.
 * <p/>
 * When used as {@link ActionRegistration#checkedOn} value, the annotated action will change
 * to <b>toggle on/off action</b>, represented by a checkbox (menu) or a toggle button (toolbar).
 * The action state will track the model property specified by this
 * annotation. Toggle actions become <b>enabled</b> when the model object is
 * found in the Lookup, and <b>checked</b> (or toggled on) when the model property
 * is set to a defined value (usually {@code true})
 * <p/>
 * The {@link #type} specifies type which is searched for in the {@link Lookup} and
 * if an instance is found, it is used as the model object. If the {@link #type} is not set, 
 * the <b>the type inferred from Action's
 * constructor</b> (see {@link ActionRegistration}) will be used to find the model.
 * <p/>
 * The {@link #property} specifies bean property whose value should be used to
 * determine checked state. The obtained value is compared using {@link #checkedValue}
 * as follows:
 * <ul>
 * <li>a boolean or Boolean value is compared to {@link Boolean#TRUE} or the {@link #checkedValue},
 * if present.
 * <li>if {@link #checkValue} is {@link #NULL_VALUE}, the action is checked if and only if
 * the value is {@code null}. 
 * <li>if {@link #checkValue} is {@link #NON_NULL_VALUE}, the action is checked if and only if
 * the value is not {@code null}. 
 * <li>if the value type is an enum, its {@link Enum#name} is compared to {@link #checkValue}
 * <li>if the value is a {@link Collection} or {@link Map}, state evaluates to the {@link Collection#isEmpty} or
 * {@link Map#isEmpty}, respectively.
 * <li>if the value is a {@link Number}, state will be true if value evaluates to <b>a positive integer</b>.
 * <li>the state will be {@code false} (unchecked) otherwise.
 * <p/>
 * If {@link #type} is set to {@link Action}.class, the annotated element <b>must
 * be an {@link Action}</b> subclass. {@link Action#getValue} will be used to determine
 * the state. The Action delegate <b>will not be instantiated eagerly</b>, but only
 * after the necessary context type becomes available in Lookup. 
 * This support minimizes from premature code loading for custom action implementations. 
 * <b>Important note:</b> if your Action implements {@link ContextAwareAction},
 * or one of the {@link Presenter} interfaces, it is eager and will be loaded immediately !
 * <p/>
 * Changes to the model object will be tracked using event listener pattern. The annotation-supplied
 * delegate attempts to {@link PropertyChangeListener} and {@link ChangeListener} automatically; \
 * other listener interfaces must be specified using {@link #listenOn}
 * value. Finally, {@link #listenOnMethod} specifies which listener method will trigger
 * state update; by default, all listener method calls will update the action state.
 * <p/>
 * The {@link ActionState} annotation may be also used as a value of {@link ActionRegistration#enabledOn()} 
 * and causes the annotated Action to be <b>enabled</b> not only on presence of object of the context type,
 * but also based on the model property. The property, enable value and listener is specified the
 * same way as for "checked" state. See the above text.
 * <p/>
 * If a completely custom behaviour is desired, the system can finally delegate {@link Action#isEnabled} and
 * {@link Action#getValue getValue}({@link Action#SELECTED_KEY}) to the action implementation itself: use {@link #useActionInstance()}
 * value.
 * <p/>
 * Here are several examples of {@code @ActionState} usage:
 * <p/>
 * To define action, which <b>enables on modified DataObjects</b> do the following
 * registration:
 * <code><pre>
 * &#64;ActionID(category = "Example", id = "example.SaveAction")
 * &#64;ActionRegistration(displayName = "Save modified",
 *     enabledOn = @ActionState(property = "modified")
 * )
 * public class ExampleAction implements ActionListener {
 *     public ExampleAction(DataObject d) {
 *         // ...
 *     }
 *     
 *     public void actionPerformed(ActionEvent e) {
 *         // ...
 *     }
 * }
 * </pre></code>
 * The action will be instantiated and run only after:
 * <ul>
 * <li>DataObject becomes available, and
 * <li>its {@code modified} property becomes true
 * </ul>
 * 
 * To create "toggle" action in toolbar or a menu, which changes state based on some property,
 * you can code:
 * <code><pre>
 * enum SelectionMode {
 *     Rectangular,
 *     normal
 * }
 * &#64;ActionID(category = "Example", id = "example.RectSelection")
 * &#64;ActionRegistration(displayName = "Toggle rectangular selection", checkedOn = &#64;ActionState(
 *     property = "selectionMode", checkedValue = "Rectangular", listenOn = EditorStateListener.class)
 * )
 * public class RectangularSelectionAction implements ActionListener {
 *     public RectangularSelectionAction(EditorInterface editor) {
 *         // ...
 *     }
 *     &#64;Override
 *     public void actionPerformed(ActionEvent e) {
 *     }
 * }
 * </pre></code>
 * The action enables when {@code EditorInterface} appears in the action Lookup. Then,
 * its state will be derived from {@code EditorInterface.selectionMode} property. Since
 * there's a custom listener interface for this value, it must be specified using {@link #listenOn}.
 * <p/>
 * Finally, if the action needs to perform its own special magic to check enable state, we 
 * hand over final control to the action, but the annotation-introduced wrappers will still
 * create action instance for a new model object, attach and detach listeners on it and ensure
 * that UI will not be strongly referenced from the model for proper garbage collection:
 * <code><pre>
 * &#64;ActionID(category = "Example", id = "example.SelectPrevious")
 * &#64;ActionRegistration(displayName = "Selects previous item", checkedOn = &#64;ActionState(
 *     listenOn = ListSelectionListener.class, useActionInstance = true)
 * )
 * public class SelectPreviousAction extends AbstractAction {
 *     private final ListSelectionModel model;
 *     
 *     public SelectPreviousAction(ListSelectionModel model) {
 *         this.model = model;
 *     }
 *     &#64;Override
 *     public boolean isEnabled() {
 *         return model.getAnchorSelectionIndex() > 0;
 *     }
 *     &#64;Override
 *     public void actionPerformed(ActionEvent e) {
 *     }
 * }
 * </pre></code>
 * The system will do the necessary bookkeeping, but the action decides using its
 * {@link Action#isEnabled} implementation. 
 * 
 * @author sdedic
 * @since 7.71
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ActionState {

    /**
     * The type which the action will look for in its context. The action
     * becomes checked (enabled) if and only if there's at least one instance of such type
     * present in the context. There are some special values that modify behaviour:
     * <ul>
     * <li><code>Object.class</code> (the default) the context object will be used and the property will be read
     * from the context object. Only applicable if the action accepts single object.
     * <li><code>Action.class</code>: the {@link #property} action value will be used, 
     * as obtained by {@link Action#getValue}.
     * </ul>
     * If {@code @ActionState} is used in {@link ActionRegistration#enabledOn()}, the
 * {@code type} can be left unspecified, defaulting to the context type for the action.
     *
     * @return type to work with.
     */
    public Class<?> type() default Object.class;

    /**
     * Property name whose value represents the state. The property must be a
     * property on the {@link #type()} class; read-only properties are
     * supported. If the target class supports attaching
     * {@link PropertyChangeListener} or {@link ChangeListener}, the action will
     * attach a listener ({@link PropertyChangeListener} takes precedence) and will fire
     * appropriate state events when the property property changes.
     * <p/>
     * In the case that checked state is delegated to {@link Action}, the property
     * default is different depending on the context the annotation is used:
     * <ul>
     * <li>if used to specify enable state ({@link ActionRegistration#enabledOn()}, the property defaults to "enabled"
     * <li>if used as checked state ({@code @ActionState} directly annotates to element}, the property defaults to {@link Action#SELECTED_KEY}.
     * <li>if the model is {@link Action}, {@link Action#getValue} is also used
     * to obtain the value. 
     * </ul>
     * Note that although this value gives more flexibility than {@link #useActionInstance()} for Actions, 
     * in the case where {@link #type}.{@link #property} is used to specify necessary guard condition,
     * {@link #useActionInstance()} is necessary to perform custom check.
     * @return property name.
     */
    public String property() default ""; // NOI18N
    
    /**
     * The value which makes the action checked. Can be one of:
     * <ul>
     * <li><code>"true"</code>, <code>"false"</code> to represent boolean or Boolean values
     * <li>String representation of an enum value, as obtained by {@link Enum#name()}
     * <li><code>{@link #NULL_VALUE}</code> to indicate <code>null</code> value
     * <li><code>{@link #NON_NULL_VALUE}</code> to indicate any non-null value
     * <li>String representation of the value object, as obtained by {@link Object#toString}
     * <li>
     * </ul>
     * @return value which indicates "set" state
     */
    public String checkedValue() default ""; // NOI18N
    
    /**
     * Custom listener interface to monitor for changes. If undefined, then
     * either {@link PropertyChangeListener} or {@link ChangeListener} will be 
     * auto-detected from {@link #type} class.
     * <p/>
     * All listener methods will cause the system to re-evaluate enable and on/off 
     * (if applicable) state for the action, unless {@link #listenOnMethod} is 
     * also used.
     * 
     * @return custom listener interface.
     */
    public Class listenOn() default EventListener.class;
    
    /**
     * Allows to pick one listener method, which will trigger action state update.
     * The update will re-check both enable and on/off state (if applicable). 
     * The action will however fire change events only if the state actually changes
     * from the previous one.
     * <p/>
     * The default (empty) value means that all listener methods will cause
     * state update. The value can be only specified together with {@link #listenOn} value.
     * 
     * @return listener method.
     */
    public String listenOnMethod() default ""; // NOI18N
    
    /**
     * If true, the target system will delegate to the action instance itself.
     * The action instance will not be created until the context object (or {@link #type()}
     * becomes available and the guard {@link #property()} has the {@link #checkedValue() appropriate value}.
     * <p/>
     * After that, the system will delegate to {@link Action#isEnabled()} for enablement, or
     * to {@link Action#getValue getValue}({@link Action#SELECTED_KEY}) for on/off state of the action.
     * <p/>
     * The annotated element <b>must</b> implement {@link Action} interface in order to use
     * this value.
     * @return whether the action instance itself should be ultimately for enable/check status
     */
    public boolean useActionInstance() default false;
    
    /**
     * An explicit {@code null} value for {@link #checkedValue}, represents {@code null}
     */
    public static final String NULL_VALUE = "#null";

    /**
     * An explicit {@code null} value for {@link #checkedValue}, represents {@code non-null} 
     */
    public static final String NON_NULL_VALUE = "#non-null";
}
