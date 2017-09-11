/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.spi.actions;

import org.openide.util.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * An action which operates in the global <i>selection context</i> (a
 * Lookup such as the return value of Utilities.actionsGlobalContext()).
 * Context actions are sensitive to the presence or absence of a particular
 * Java type in a Lookup.  The presence or absence of objects of that type
 * can changed based on things like what the user has selected, or what
 * logical window contains focus, or both of those things.
 * <p/>
 * Context actions implement <code>org.openide.util.ContextAwareAction</code>,
 * which has the method <code>createContextAwareInstance()</code> to create
 * an instance of the action for a specific context (for example, a popup
 * menu - the selection might change while the popup is on-screen, but what
 * the action in the popup menu operates on should not).
 * <p/>
 * Context actions make it possible to have a global action which doesn't
 * have to know anything in particular about which object going to operate on;
 * it just knows the type of that object.  When invoked, it acts on whatever
 * is selected at that moment in time.
 * <p/>
 * This class and its subclasses are a replacement for most uses of
 * NodeAction and CookieAction from the Nodes API.
 * In the NetBeans IDE, to declaratively install a global ContextAction, simply write
 * a static method that contains the above code, and reference it in your
 * layer file as the <code>methodvalue</code> for your <code>.instance</code>
 * or similar file.
 * <p/>
 * <h4>Using ContextAction in place of NodeAction or CookieAction</h4>
 * Replacing CookieAction with ContextAction is fairly straightforward.
 * <p/>
 * The most straightforward replacement for <code>CookieAction</code> is
 * <code>LookupProviderAction</code>.  It handles one layer of indirection -
 * that is, a <code>CookieAction</code> first needs to have at least one <code>Node</code>
 * selected;  a <code>Node</code> is itself a <code>Lookup.Provider</code>,
 * and so the <code>CookieAction</code>
 * is sensitive to some object type in the lookup of one or more selected
 * <code>Node</code>s.  So a <code>CookieAction</code> is an action sensitive
 * to an object (the cookie) in the lookup of an object in the selection (the node).
 * <p/>
 * <code>LookupProviderAction</code> accomplishes the same thing, without
 * requiring that the second <code>Lookup.Provider</code> be a <code>Node</code>.
 * <p>
 * CookieAction supported a number of different <i>mode</i>s
 * which determined its enablement, based on how many objects were in the
 * selection and if there were any lookups in the selection that did <i>not</i>
 * contain an instance of the type it was sensitive to.  In <code>LookupProviderAction</code>,
 * this distinction is split into its two constituents:  To disallow enablement
 * if there is some Lookup.Provider that does <i>not</i> contain the type you
 * care about, pass true to the superclass constructor for the <code>all</code>
 * parameter.  To control for the number of selected items, override
 * <code>checkQuantity(int)</code>.
 * <p/>
 * It is also possible with <code>ContextAction</code>s to do deeper indirection.
 * E.g., say the selection will contain a <code>Node</code>.  <code>Node</code>
 * implements <code>Lookup.Provider</code>.  You are interested in <code>Node</code>s
 * which have a <code>Project</code> in their lookup, and <code>Project</code>
 * is also a <code>Lookup.Provider</code> too.  What you really want to do is
 * write an action that is enabled
 * <ul><li>When the selection contains a <code>Node</code>
 *   <ul><li>which contains a <code>Project</code> in its lookup</code>
 *      <ul><li>which contains a <code>Foo</code> in its lookup</code>
 *      </ul</li>
 *   </ul></li>
 * </ul></li>
 * The only actually interesting logic here is what you will do with a
 * <code>Foo</code> if you can get hold of one.  Handling this is easy:
 * <pre>
 * ContextAction<Foo> theRealAction = new MyContextAction(); //this is what you wrote
 * Action<Node> theGlobalAction = createIndirectAction(Node.class,
 *   createIndirectAction(Project.class, theRealAction));
 * </pre>
 * If you had a CookieAction which was sensitive to more than one cookie type,
 * that scenario is not supported directly by ContextAction.  But what you
 * do instead will likely be simpler and more testable than your original
 * code, and will do the same thing:
 * <ul>
 * <li>Write one ContextAction subclass for each of the types you want to
 * support</li>
 * <li>Create your global action by calling 
 * <code>ContextAction.merge (actionOne, actionTwo, actionThree);
 * </li>
 * </ul>
 * <h4>A Note about Equality</h4>
 * ContextAction and any subclasses implement equals() and hashCode() such that
 * if the <em>type</em> of the passed object is the same, then the objects are treated
 * as identical.  This enables multi-selection to work.  This means that any
 * instance of a subclass of ContextAction is effectively a singleton and any
 * other instance of the same class may be substituted for it.
 * <p/>
 * If you want to create a single subclass of ContextAction which is passed
 * different parameters or create instances of the same subtype which
 * have different behaviors, you
 * are probably choosing the wrong parent class for your action.  The point
 * of ContextAction is that the entirety of the &quot;context&quot; is the
 * content of the Lookup that is passed to it.
 * <p/>
 * To, for example, change the display name depending on number of objects
 * selected, simply override <code>change()</code> to do that.
 *
 * @see org.openide.util.ContextAwareAction
 * @see org.openide.util.Lookup
 * @see org.openide.util.Utilities#actionsGlobalContext
 * @author Tim Boudreau
 */
public abstract class ContextAction<T> extends NbAction {
    final Class<T> type;
    private final StubListener stubListener = new StubListener();
    //A context aware instance which we use internally to trigger
    //enabled changes as long as there is at least one property change
    //listener attached to us.
    //By having the same thing we return from createContextAwareInstance handle
    //all internal state, we make it easy to make a survives-focus-change
    //subclass just by overriding createStub() to make a stub which retains
    //the last usable collection of objects

    //The action stub is the thing that really does the heavy lifting for
    //all ContextActions.
    ActionStub<T> stub;
    static boolean unitTest;
    
    /**
     * Create a new ContextAction which will operate on instances of
     * type <code>type</code>
     * @param type The type this action needs in its context in order to be
     * invoked
     */
    protected ContextAction(Class<T> type) {
        this (type, null, null);
    }

    /**
     * Create a new ContextAction which will operate on a type <code>type</code>,
     * with the specified display name and icon.
     *
     * @param type The type this action needs in its context in order to be
     * invoked
     * @param displayName A localized display name
     * @param icon An image to use as the action's icon
     */
    protected ContextAction(Class<T> type, String displayName, Image icon) {
        this.type = type;
        Parameters.notNull("type", type);
        if (displayName != null) {
            putValue (Action.NAME, displayName);
        }
        if (icon != null) {
            putValue (Action.SMALL_ICON, new ImageIcon (icon));
        }
        putValue ("noIconInMenu", true);
    }

    /**
     * Whether or not the action is enabled.  By default, determines if there
     * are any instances of type <code>type</code> in the selection context
     * lookup, and if there are, returns true.  To refine this behavior further,
     * override <code>enabled (java.util.Collection)</code>.
     * @return whether or not the action should be enabled
     */
    @Override
    public final boolean isEnabled() {
        return _isEnabled();
    }

    boolean _isEnabled() { //For override in MergeAction
        ActionStub<T> stubAction = getStub();
        Collection <? extends T> targets = stubAction.collection();
        boolean result = checkQuantity(targets) && stubAction.isEnabled();
        return result;
    }

    boolean checkQuantity(Collection<? extends T> targets) {
        return checkQuantity (targets.size());
    }

    /**
     * Determine if this action should be enabled, based on the number
     * of objects of type <code>type</code> available in the selection.
     * Some actions will want to be enabled only if there is a single
     * object of a given type, or some specific number (for example,
     * an action which diffs two files should only be enabled if exactly
     * two files are selected).
     * <p/>
     * This method is called prior to the (potentially)
     * more expensive check of <code>enabled(Collection&lt;T&gt;)</code>.  If it
     * returns false, no further tests are done;  the action is disabled.
     *
     * @param numberOfObjects The number of objects of type <code>T</code>
     * in the selection
     * @return True if the action should be enabled.
     */
    protected boolean checkQuantity (int numberOfObjects) {
        return numberOfObjects > 0;
    }

    /**
     * Determine if this action should be enabled.  This method will only be
     * called if the size of the collection is > 0.  The default implementation
     * returns <code>true</code>.  If you need to do some further
     * test on the collection of objects to determine if the action should
     * really be enabled or not, override this method.
     *
     * @param targets A collection of objects of type <code>type</code>
     * @return Whether or not the action should be enabled.
     */
    protected boolean isEnabled(Collection<? extends T> targets) {
        return true;
    }

    /**
     * Override to actually do whatever this action does.  This method is
     * passed the collection of all objects of type <code>&lt;T&gt;
     * present in the selection context (the lookup).
     * @param targets The objects of type <code>type</code>, which
     * this action will use to do whatever it does
     */
    protected abstract void actionPerformed(Collection<? extends T> targets);

    /**
     * Fetches the collection of objects this action will act on and passes
     * them to <code>actionPerformed(Collection<? extends T>).
     * @param ignored The action event.  Ignored.
     */
    public final void actionPerformed(ActionEvent ignored) {
        getStub().actionPerformed(null);
    }

    /**
     * Create an instance of this action over a particular context.  This is
     * used to handle cases such as popup menus, where a popup menu is created
     * against whatever the selection is at the time of its creation;  if the
     * selection changes <i>while</i> the popup is onscreen, we do not want
     * the popup to operate on the new selection; it should operate on the thing
     * the menu was created for.  So for a popup menu, an instance of this
     * action is created over a snapshot-lookup - a snapshot
     * of the context at the moment it is created.
     * @param actionContext The context this action instance should operate on.
     * @return An action specific to the passed Lookup
     */
    @Override
    protected final NbAction internalCreateContextAwareInstance(Lookup actionContext) {
        return createStub (actionContext);
    }

    /**
     * Equals in ContextAction and its subclasses will return true if the
     * passed object is of the same <em>type</em> as the passed object.  This
     * means that two instances of the same subtype of ContextAction are
     * equal if they are the same subtype (this enables actions to work over
     * multiple selection).
     * <p/>
     * If this is not the behavior you want, it is very likely that
     * ContextAction is not the correct superclass for your action.
     *
     * @param o The foreign object
     * @return whether or not this object is functionally equivalent to the
     * passed object
     */
    @Override
    public boolean equals (Object o) {
        return o != null && o.getClass() == getClass();
    }

    /**
     * Returns getClass().hashCode();
     * @return The hash code of this type.
     */
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    ActionStub<T> createStub(Lookup actionContext) {
        return new ActionStub<T>(actionContext, this);
    }

    private ActionStub<T> createInternalStub () {
        //Don't synchronize, just ensure we are only called from sync methods
        assert Thread.holdsLock(lock());
        ActionStub<T> result = createStub (Utilities.actionsGlobalContext());
        return result;
    }

    ActionStub<T> getStub() {
        synchronized (lock()) {
            if (stub == null && attached()) {
                stub = createInternalStub();
                stub.addPropertyChangeListener(stubListener);
            }
            return stub == null ? createInternalStub() : stub;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[name=" + getValue(NAME) + " type=" + type.getName() + "]";
    }

    /**
     * Called when the set of available objects of type <code>type</code>
     * changes.  Override if you incorporate some aspect of the selection
     * in the display name, and need to change it when the collection changes.
     * <p/>
     * Note that this method may be called with an empty collection.
     * @param collection The collection of objects currently in the selection
     * @param instance The action instance which encountered the change.  <i>
     * Note that <code>instance</code> may or may not == this.</i>  It might
     * be an action returned by createContextAwareInstance() for display in
     * a popup menu, which is responding to a change in the context it
     * operates on.  If you need to change the display name, call
     * <code>putValue(NAME, newName)</code> on <code>instance</code>,
     * not on <code>this</code>.  Do not assume <code>instance</code> is
     * an instance of your <code>ContextAction</code> subclass - it may or
     * it may not be.
     */
    protected void change (Collection <? extends T> collection, Action instance) {
        //do nothing
    }

    @Override
    void internalAddNotify() {
        stub = getStub();
        stub.resultChanged(null);
        super.internalAddNotify();
    }

    @Override
    void internalRemoveNotify() {
        try {
            super.internalRemoveNotify();
        } finally {
            stub.removePropertyChangeListener(stubListener);
            stub = null;
        }
    }

    /**
     * Recompute the enabled state of this action.  Call this method if your
     * action depends on some external value to compute its enabled state,
     * when that value changes.
     */
    protected final void refresh() {
        getStub().resultChanged(null);
    }

    //for unit tests
    Collection<? extends T> stubCollection() {
        synchronized (lock()) {
            return stub == null ? null : stub.collection();
        }
    }

    private class StubListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange (evt.getPropertyName(),
                    evt.getOldValue(), evt.getNewValue());
        }
    }

    /**
     * Create an action which looks for a particular type <code>T</code> in
     * the selection, where T itself is a subclass <code>Lookup.Provider</code>
     * (such as a Node or Project in the NetBeans IDE), and delegates to
     * another action which is sensitive to some object in that lookup.
     * <p/>
     * Such actions can be chained to any depth of lookup providers;
     * you only need to write a ContextAction that is sensitive to the thing
     * you are actually interested in at the end of the chain.
     * So, if you want to write an action which is sensitive to, say, a
     * ClassPathProvider that belongs to the Project found in the selected
     * Node in the global selection, you would do as follows:
     * <pre>
     * ContextAction<ClassPathProvider> theRealAction = new MyContextAction();
     * Action<Node> theGlobalAction = createIndirectAction(Node.class,
     *   createIndirectAction(Project.class, theRealAction));
     * </pre>
     * The returned action will pick up its properties (name, icon, enabled
     * state, etc.) from the action passed in to this one, and it will re-fire
     * property changes from that one.
     * <p>
     * If you need more specific control of enablement logic, either override
     * <code>enabled()</code> in your <code>ContextAction</code>, or use
     * one of the subclasses such as <code>Single</code> or
     * <code>SurviveSelectionChange</code> which provide
     * specific enablement behavior.
     *
     * @param <T> The Lookup.Provider subclass (for example,
     * <code>Node</code>, <code>DataObject</code> or <code>Project</code>)
     * @param lkpProviderType An object which implements Lookup.Provider which
     * will be in the lookup.
     * @param theRealAction The action to invoke if all the conditions are met
     * @param allLookupsMustBeUsable if true, then if any of the Lookup.Providers
     * cannot provide an object of the type <code>theRealAction</code> cares
     * about, do not enable the action.  Otherwise the action will be enabled
     * if any objects <code>theRealAction</code> is interested in are present.
     * If you want to write an action that works on multi-selection, but only
     * if the action can work against all of the selected objects, pass true.
     * If the action should be enabled if only some of the objects are interesting
     * to <code>theRealAction</code>, pass false.
     * @return A ContextAction
     */
    public static <T extends Lookup.Provider, R> ContextAction<T>
            createIndirectAction(Class<T> lkpProviderType, ContextAction<R> theRealAction, boolean allLookupsMustBeUsable) {
        return new IndirectAction<T,R> (lkpProviderType, theRealAction,
                allLookupsMustBeUsable);
    }

    /**
     * Same as <code>createIndirectAction (lkpProviderType, theRealAction,
     * <i>true</i>)</code>.
     * @param <T> The Lookup.Provider subclass (for example,
     * <code>Node</code>, <code>DataObject</code> or <code>Project</code>)
     * @param lkpProviderType An object which implements Lookup.Provider which
     * will be in the lookup.
     * @param theRealAction The action to invoke if all the conditions are met
     * @return A contextAction
     */
    public static <T extends Lookup.Provider, R> ContextAction<T>
            createIndirectAction(Class<T> lkpProviderType, ContextAction<R> theRealAction) {
        return createIndirectAction (lkpProviderType, theRealAction, true);
    }
}
