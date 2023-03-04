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
package org.openide.explorer;

import org.netbeans.modules.openide.explorer.ExplorerActionsImpl;
import org.openide.nodes.*;
import org.openide.util.*;

import javax.swing.Action;
import javax.swing.ActionMap;

/**
 * Helper methods to embed <code>ExplorerManager</code>s and explorer views
 * into Swing component trees.
 * <p>
 * To create a component that displays the content of an {@link ExplorerManager} you
 * should make your component implement {@link ExplorerManager.Provider} and
 * {@link org.openide.util.Lookup.Provider} and register actions in your component's {@link ActionMap}:
<pre>
<span class="keyword">public</span> <span class="keyword">class</span> <span class="type">YourComponent</span> <span class="keyword">extends</span> <span class="type">TopComponent</span>
<span class="keyword">implements</span> <span class="type">ExplorerManager.Provider</span>, <span class="type">Lookup.Provider</span> {
    <span class="keyword">private</span> <span class="type">ExplorerManager</span> <span class="variable-name">manager</span>;
    <span class="keyword">public</span> <span class="type">YourComponent</span>() {
        <span class="keyword">this</span>.manager = <span class="keyword">new</span> <span class="type">ExplorerManager</span>();
        <span class="type">ActionMap</span> map = <span class="keyword">this</span>.getActionMap ();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put(<span class="string">"delete"</span>, ExplorerUtils.actionDelete(manager, <span class="constant">true</span>)); <span class="comment">// or false</span>
 *
        <span class="comment">// following line tells the top component which lookup should be associated with it</span>
        associateLookup (ExplorerUtils.createLookup (manager, map));
</span>    }
    <span class="keyword">public</span> <span class="type">ExplorerManager</span> <span class="function-name">getExplorerManager</span>() {
        <span class="keyword">return</span> manager;
    }
    <span class="comment">// It is good idea to switch all listeners on and off when the
</span>    <span class="comment">// component is shown or hidden. In the case of TopComponent use:
</span>    <span class="keyword">protected</span> <span class="type">void</span> <span class="function-name">componentActivated</span>() {
        ExplorerUtils.activateActions(manager, <span class="constant">true</span>);
    }
    <span class="keyword">protected</span> <span class="type">void</span> <span class="function-name">componentDeactivated</span>() {
        ExplorerUtils.activateActions(manager, <span class="constant">false</span>);
    }
}
</pre>
 * The above code will work in a NetBeans module. For a standalone NetBeans-based application
 * you will need to set up your {@link javax.swing.InputMap} and use different triggers to
 * turn the listeners on and off:
<pre>
<span class="keyword">public</span> <span class="keyword">class</span> <span class="type">YourComponent</span> <span class="keyword">extends</span> <span class="type">JPanel</span>
<span class="keyword">implements</span> <span class="type">ExplorerManager.Provider</span>, <span class="type">Lookup.Provider</span> {
    <span class="keyword">private</span> <span class="type">ExplorerManager</span> <span class="variable-name">manager</span>;
    <span class="keyword">private</span> <span class="type">Lookup</span> <span class="variable-name">lookup</span>;
</span>    <span class="keyword">public</span> <span class="type">YourComponent</span>() {
        <span class="comment">// same as before...</span>
        manager = <span class="keyword">new</span> <span class="type">ExplorerManager</span>();
        <span class="type">ActionMap</span> <span class="variable-name">map</span> = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put(<span class="string">"delete"</span>, ExplorerUtils.actionDelete(manager, <span class="constant">true</span>)); <span class="comment">// or false
</span>
        <span class="comment">// ...but add e.g.:
</span>        <span class="type">InputMap</span> <span class="variable-name">keys</span> = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke(<span class="string">"control C"</span>), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke(<span class="string">"control X"</span>), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke(<span class="string">"control V"</span>), DefaultEditorKit.pasteAction);
        keys.put(KeyStroke.getKeyStroke(<span class="string">"DELETE"</span>), <span class="string">"delete"</span>);

        <span class="comment">// ...and initialization of lookup variable</span>
        lookup = ExplorerUtils.createLookup (manager, map);
    }
    <span class="comment">// ...method as before and getLookup</span>
    <span class="keyword">public</span> <span class="type">ExplorerManager</span> <span class="function-name">getExplorerManager</span>() {
        <span class="keyword">return</span> manager;
    }
    <span class="keyword">public</span> <span class="type">Lookup</span> <span class="function-name">getLookup</span>() {
        <span class="keyword">return</span> lookup;
    }
    <span class="comment">// ...methods as before, but replace componentActivated and
</span>    <span class="comment">// componentDeactivated with e.g.:
</span>    <span class="keyword">public</span> <span class="type">void</span> <span class="function-name">addNotify</span>() {
        <span class="keyword">super</span>.addNotify();
        ExplorerUtils.activateActions(manager, <span class="constant">true</span>);
    }
    <span class="keyword">public</span> <span class="type">void</span> <span class="function-name">removeNotify</span>() {
        ExplorerUtils.activateActions(manager, <span class="constant">false</span>);
        <span class="keyword">super</span>.removeNotify();
    }
}
</pre>
 * @author Jaroslav Tulach
 * @since 4.14
 */
public final class ExplorerUtils extends Object {
    /** Just package private subclass
     */
    ExplorerUtils() {
    }

    //
    // Public factory methods
    //

    /** Creates copy action
     * @param em explorer manager the action should be attached to
     * @return action that invokes copy on the explorer
     */
    public static Action actionCopy(ExplorerManager em) {
        return ExplorerManager.findExplorerActionsImpl(em).copyAction();
    }

    /** Creates cut action
     * @param em explorer manager the action should be attached to
     * @return action that invokes cut on the explorer
     */
    public static Action actionCut(ExplorerManager em) {
        return ExplorerManager.findExplorerActionsImpl(em).cutAction();
    }

    /** Creates delete action
     * @param em explorer manager the action should be attached to
     * @param confirm true if a confirmation box should be displayed before actual deletion
     * @return action that invokes delete on the explorer
     */
    public static Action actionDelete(ExplorerManager em, boolean confirm) {
        ExplorerActionsImpl impl = ExplorerManager.findExplorerActionsImpl(em);

        return impl.deleteAction(confirm);
    }

    /** Creates paste action
     * @param em explorer manager the action should be attached to
     * @return action that invokes paste on the explorer
     */
    public static Action actionPaste(ExplorerManager em) {
        return ExplorerManager.findExplorerActionsImpl(em).pasteAction();
    }

    /** Activates or deactivates updates of actions for given <code>ExplorerManager</code>.
     * By default actions created by <code>actionXXX</code> factory methods
     * are started and update itself according to changes in external environment
     * (the explorer manager itself, clipboard content, etc.). This might not
     * be necessary and a bit of resource consuming in case when the component
     * showing the <code>ExplorerManager</code> is not visible. In such case
     * the implementation can disable and then again reenable refresh by calling
     * this method.
     *
     * @param em the explorer manager
     * @param enable true if actions should be updated, false otherwise
     */
    public static void activateActions(ExplorerManager em, boolean enable) {
        if (enable) {
            ExplorerManager.findExplorerActionsImpl(em).attach(em);
        } else {
            ExplorerManager.findExplorerActionsImpl(em).detach();
        }
    }

    /** Creates new lookup containing selected nodes and their lookups.
     * @param em explorer manager which selection to follow
     * @param map additional map to be added into the lookup
     *
     * @return lookup that updates itself according the changes inside the ExplorerManager
     */
    public static Lookup createLookup(ExplorerManager em, ActionMap map) {
        return new DefaultEMLookup(em, map);
    }

    /** Utility method to get context help from a node selection.
     *  Tries to find context helps for selected nodes.
     *  If there are some, and they all agree, uses that.
     *  In all other cases, uses the supplied generic help.
     *
     *  @param sel a list of nodes to search for help in
     *  @param def the default help to use if they have none or do not agree
     *  @return a help context
     * @since 4.40
     */
    public static HelpCtx getHelpCtx(Node[] sel, HelpCtx def) {
        HelpCtx result = null;

        for (int i = 0; i < sel.length; i++) {
            HelpCtx attempt = sel[i].getHelpCtx();

            if ((attempt != null) && !attempt.equals(HelpCtx.DEFAULT_HELP)) {
                if ((result == null) || result.equals(attempt)) {
                    result = attempt;
                } else {
                    // More than one found, and they conflict. Get general help on the Explorer instead.
                    result = null;

                    break;
                }
            }
        }

        return (result != null) ? result : def;
    }
}
