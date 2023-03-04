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
/*
 * InplaceEditor.java
 *
 * Created on December 22, 2002, 2:50 PM
 */
package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.event.*;

import java.beans.PropertyEditor;

import javax.swing.JComponent;
import javax.swing.KeyStroke;


/** Interface defining the contract of reusable inline cell editors for
 *  properties.  Generally, this interface will be implemented
 *  on a component subclass.  Note
 *  that such components do not have to be concerned about providing
 *  a custom editor button for properties with custom property
 *  editors.  If needed, the rendering infrastructure will provide
 *  one.
 * <P>Inplace editors are designed to be reusable - that is, a single
 *  instance may be reconfigured and reused to edit multiple properties
 *  over its lifespan.  The <code>connect()</code> and <code>clear()</code>
 *  methods provide a means of configuring an instance to represent a
 *  property, and then de-configure it when it is no longer needed.  The
 *  typical lifecycle of an inplace editor is as follows: <OL><LI>The
 *  user clicks a property in the property sheet.</LI><LI>The property
 *  sheet identifies the property clicked, and locates the correct
 *  inplace editor (either a default one or a custom implementation supplied
 *  by the property or property editor).</LI><LI><code>connect()</code> is
 *  called to configure the editor</LI><LI>The component returned from
 *  <code>getComponent()</code> is displayed on screen and given focus</LI>
 *  <LI>The user enters text or otherwise manipulates the component to change
 *  the value</LI><LI>When the component determines that the user has
 *  either concluded editing (usually pressing Enter) or cancelled editing
 *  (pressing Escape), the inplace editor fires <code>ACTION_SUCCESS</code>
 *  or <code>ACTION_FAILURE</code></LI><LI>The property sheet detects this
 *  action event and removes the editor component</LI><LI>The property sheet
 *  updates the property</LI><LI>The property sheet calls <code>clear()</code>
 *  to dispose of any state or references held by the inplace editor</LI></OL>
 *  <P>If you implement this interface to provide a custom inplace
 *  editor for a particular property, it is wise to also write a
 *  custom PropertyEditor whose <code>paint()</code> method will
 *  paint an image identical to what your editor looks like when
 *  it is instantiated.  The simplest way to do this is to create
 *  a renderer instance of your inplace editor, and use it in the
 *  <code>paint()</code> method of your property editor.
 *  <P>The methods of this interface should <strong>never</strong>
 *  be called from any thread except the AWT event thread.  The backing
 *  implementation is not thread-safe.  This includes <code>ActionEvent</code>s
 *  fired by instances of <code>InplaceEditor</code>.
 *  <P>In no cases should an instance of <code>InplaceEditor</code>
 *  attempt to directly update the value of the represented property
 *  or property editor.  If the property should be updated, ensure
 *  that <code>getValue()</code> will return the correct value, and
 *  fire the action command COMMAND_SUCCESS.  Implementations
 *  should also not assume that because one of these events has been
 *  fired, that therefore the property editor has been updated with
 *  the new value.  Components that display inplace editors
 *  are responsible for the timing of and policy for updates to the represented
 *  properties.  Inplace editors merely display the contents of a property
 *  editor, provide a way for the user to edit that value, and notify
 *  the infrastructure when the user has made a change.
 * <P>Standard implementations of this interface for text entry, combo
 *  boxes and checkboxes are provided by the property sheet infrastructure.
 *  There are several ways to provide a custom inplace editor for use in
 *  the property sheet:<UL><LI><B>Globally</B> - a module supplying a
 *  property editor implementing ExPropertyEditor for a given class may call
 *  <code>PropertyEnv.registerInplaceEditorFactory(InplaceEditor.Factory)</code>
 *  in its <code>attachEnv()</code> method.
 *  When the user invokes an editor operation, the returned inplace editor
 *  will be used.</LI><LI><B>On a per-property basis</B> - A
 *  <code>Node.Property</CODE> may provide a custom inplace editor via hinting.
 *  To do this, the <code>Node.Property</CODE> instance should return an
 *  instance of <code>InplaceEditor</code> from <code>getValue
 *  (&quot;inplaceEditor&quot;)</code></LI></UL>
 *  If both methods are used on the same property, the inplace editor provided
 *  by the per-property hint takes precedence.
 * @author Tim Boudreau
 */
public interface InplaceEditor {
    /** Action command that tells the property sheet to update
     *  the property's value with the value from this inplace editor and close
     *  the inplace editor.  */
    public static final String COMMAND_SUCCESS = "success"; //NOI18N

    /** Action command that tells the property sheet that editing
     *  is completed, but the value should not be updated, the
     *  editor should simply be removed.  */
    public static final String COMMAND_FAILURE = "failure"; //NOI18N

    /** Connect this editor with a property editor.  The
     *  <code>PropertyEditor</code> instance will already be
     *  initialized with the initial value, and if it is an
     *  instance of ExPropertyEditor, <code>ExPropertyEditor.attachEnv(env)</code>
     *  will already have been called.  The <code>PropertyEnv</code>
     *  instance is passed to allow rendering hints to be passed to
     *  the <code>InplaceEditor</code> instance. <P> Implementations
     *  which may be connected to <code>PropertyEditor</code> instances
     *  that do not implement <code>ExPropertyEditor</code> must handle
     *  the case that the <code>env</code> property may be null.
     * @param pe The property editor
     * @param env An instance of PropertyEnv, if the editor is an instance of ExPropertyEditor,
     * or null if it is not  */
    public void connect(PropertyEditor pe, PropertyEnv env);

    /** Returns the physical inplace editor component that should be displayed
     *  on-screen.  Typical implementations of this
     *  interface are <code>JComponent</code> subclasses which implement this interface
     *  and simply return <code>this</code> from this method.  If you
     *  implement this interface separately from the inplace editor
     *  component, it is expected that the same component instance
     *  will be returned from this instance from the first time
     *  <code>connect()</code> is called, until such a time as <code>clear()</code>
     *  is called.
     * @return The component that should be displayed to the user to edit the property  */
    public JComponent getComponent();

    /** Dispose of any state and references to the property or value being
     *  edited, to avoid memory leaks due to held references.  The property display
     *  code will call this once an inplace editor component has been closed.
     *  A call to this method should return the inplace editor to the state it
     *  is in after its constructor is called. */
    public void clear();

    /** Returns the value currently displayed or selected in the editor.  This
     *  may or may not correspond to the current value of the Property being
     *  represented, and may not represent a valid final value for the property,
     *  but rather represents the edit in progress. <P>  This method may return
     *  a <code>String</code>, in which case the property editor will be updated
     *  using its <code>setAsText()</code> method, and the value taken from the
     *  property editor.  Implementations are free to also return either null when
     *  appropriate, a String appropriate for use with the property editor's
     *  <code>setAsText()</code> method, or an object instance compatible with the property in question's
     *  <code>setValue()</code> method.
     * @return The value currently shown in the editor component provided by
     * <code>getComponent()</code>
     */
    public Object getValue();

    /** Set the value to be <i>displayed</i> in the inplace editor.  Implementations
     *  should take care to avoid triggering a property change event in the
     *  property editor connected to this inplace editor.  This method is used
     *  to restore the partial value of an editor in the case that some
     *  external event causes it to be temporarily removed. <P> This method
     *  is optional, and primarily useful for editors that support text entry.
     *  Editors which do not support text entry may supply an empty implementation
     *  of this method. <P> It is required that <code>setValue()</code> for
     *  a given <code>InplaceEditor</code> be able to handle any possible
     *  type that it can return from <code>getValue()</code>, since it is
     *  used to temporarily cache and then restore the value mid-edit.
     * @param o The value that should be displayed in the editor component.  This
     *  should be an object the component is capable of displaying.  It may be
     *  a String or any other object type, provided the component is capable
     *  of displaying it.  This method will only ever be called with a value
     *  object supplied from <code>getValue()</code>, so this method should
     *  be compatible with anything that <code>getValue()</code> on a given
     *  <code>InplaceEditor</code> implementation may return */
    public void setValue(Object o);

    /** Indicates whether an inplace editor supports the direct entry of text or not.
     *  In particular, this method is used to support managing the background
     *  color of the editor component.  The default selection color is
     *  used by the property sheet to indicate selection in the property sheet.  Editors
     *  supporting text entry should not have their background color set to
     *  the default selection color, so that the user may distinguish selected
     *  text (which would otherwise have the same background color whether it
     *  were selected or not).
     * @return True if the editor component supplied by <code>getComponent()</code> supports
     * direct text entry by the user. */
    public boolean supportsTextEntry();

    /** Restore the inplace editor to the value returned by the property editor's
     *  <code>getValue()</code> method,  discarding any edits.
     * @throws NullPointerException If called before a call to <code>connect()</code> or after a call to
     * <code>clear()</code>, since in that case the property editor is null.
     */
    public void reset();

    /** Add an action listener to the InplaceEditor.  Note that the
     *  source property for ActionEvents fired by an InplaceEditor
     *  <strong>must</strong> be an instance of InplaceEditor.  The
     *  property sheet infrastructure will recognize two action
     *  commands:  <code>COMMAND_SUCCESS</code> and <code>COMMAND_FAILURE</code>.
     *  Other action events
     *  (such as may be generated by a component subclass implementing
     *  this interface) may be fired, but will be ignored by the
     *  property sheet infrastructure.
     * @param al The action listener to add   */
    public void addActionListener(ActionListener al);

    /** Remove an action listener from an InplaceEditor.
     * @param al The action listener to remove  */
    public void removeActionListener(ActionListener al);

    /** Keystrokes that should be ignored by the containing component when
     *  this inplace editor is open, even if they are in the <code>InputMap</code>
     *  of the container.<P>
     *  JTable (and potentially other components) will respond to
     *  keystrokes sent to an embedded component.  In particular, this
     *  is a problem in JDK 1.4 with embedded JComboBoxes - the down
     *  arrow key, used for combo box navigation, also changes the selection
     *  and closes the editor.  Since it is not always possible to determine reliably the
     *  keystrokes an inplace editor will consume at instantiation
     *  time, this allows them to be specified explicitly, so the table
     *  knows what to ignore.
     * @return The keystrokes a container of the editor component should ignore even if they
     * are mapped to actions in it.  */
    public KeyStroke[] getKeyStrokes();

    /** Get the <code>java.beans.PropertyEditor</code>
     *  instance associated with this
     *  inplace editor.  For efficiency, client code uses
     *  this method to cache the property editor being
     *  used, rather than perform gratuitous lookups of the
     *  property editor on the property it represents.
     *  Inplace editor implementations are expected to cache
     *  the property editor they are initialized with until <code>clear()</code>
     *  is called.
     * @return The property editor this InplaceEditor represents   */
    public PropertyEditor getPropertyEditor();

    /** Inplace editors cache the property model used to update a
     *  property value at the conclusion of editing.  After a call to
     *  <code>setPropertyModel()</code> this method should return the
     *  property model that should be updated with the value from this
     *  inplace editor.  After a subsequent call to <code>clear()</code>
     *  this method should return null.  <P>  Under no circumstances
     *  should an InplaceEditor implementation attempt to modify the
     *  property model - this is the job of the infrastructure that
     *  instantiated the InplaceEditor.
     * @return The property model representing the property being edited   */
    public PropertyModel getPropertyModel();

    /** Set the property model that should be updated in the event of a
     *  change.
     * @param pm The property model this inplace editor will represent */
    public void setPropertyModel(PropertyModel pm);

    /** Returns true if a component is one the inplace editor instantiated.
     *  The property sheet tracks focus and will close an inplace editor
     *  if focus is lost to an unknown component.  Since inplace editors may
     *  instantiate popup components that can receive focus, if focus is
     *  lost while an inplace editor is open, the property sheet will query
     *  the current inplace editor to ensure that the recipient of focus is
     *  truly not a child of the inplace editor.  For most InplaceEditor
     *  implementations, it is safe simply to return false from this method.
     * @param c A component which has received focus
     * @return True if the passed component was instantiated by the inplace editor as part of
     * its normal operation (for example, a popup which is not a child of
     * whatever is returned from <code>getComponent()</code>in the component
     * hierarchy, but which is effectively part of the editor).  */
    public boolean isKnownComponent(Component c);

    /** A factory for inplace editor instances.  A module may provide a property
     *  editor which provides a custom inplace editor for any properties
     *  of the type it edits.  This is accomplished as follows:  <UL><LI>The
     *  property editor must implement <code>ExPropertyEditor</code>.</LI><LI>
     *  In the <code>attachEnv()</code> method of that interface, it must call
     *  <code>env.registerInplaceEditorFactory()</code>, passing an instance
     *  of <code>InplaceEditor.Factory</code>.  If a user attempts to edit
     *  the property, the inplace editor returned from <code>Factory.getInplaceEditor()</code>
     *  will be used.</LI></UL>
     *  <p><strong>A note about using InplaceEditor instances to render properties:</strong>
     *  If a custom property editor is, as is encouraged, using an instance of its
     *  InplaceEditor to paint the value rendered in table cells, this method <strong>
     *  must not</strong> return the instance being used for rendering - that instance
     *  may be reconfigured at any time with a different value in order to paint
     *  another cell on the property sheet.   */
    public interface Factory {
        /** Fetch or create an inplace editor instance.  The system guarantees that
         *  there will never be more than one open inplace editor at a time, so it is
         *  safe to return the same static instance repeatedly from this method - when the
         *  editor is opened, it will be configured for the property it is editing.
         *  The optimal approach to implementing this method is to create the editor
         *  on the first call, and maintain a reference to it using a static field,
         *  so a single instance may be shared, but hold the reference to it using
         *  <code>java.lang.ref.WeakReference</code> or
         *  <code>java.lang.ref.SoftReference</code>, so that the instance may be
         *  garbage collected if it is no longer needed.
         * @return An inplace editor instance
         */
        public InplaceEditor getInplaceEditor();
    }
}
