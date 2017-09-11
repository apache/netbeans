/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.settings.storage.api;

import java.util.prefs.Preferences;
import org.netbeans.modules.editor.settings.storage.preferences.InheritedPreferences;
import org.netbeans.modules.editor.settings.storage.preferences.ProxyPreferencesImpl;

/**
 * Preferences with a temporary storage, backed by another Preferences
 * object. The instance tracks modifications done through the
 * {@link Preferences} interface, but do not change the backing store
 * until {@link Preferences#flush} is called.
 * <p/>
 * The MemoryPreferences object serves as an accessor, and offers some
 * additional control for the Preferences tree. It should not be handed
 * away, only the creator who manages the lifecycle should possess
 * the MemoryPreferences instance. Other clients should be given just the
 * Preferences object from {@link #getPreferences}.
 * <p/>
 * The returned Preferences object implements {@link OverridePreferences} extension
 * interface.
 * <p/>
 * This implementation <b>does not support</b> sub-nodes.
 * 
 * @since 1.38
 * 
 * @author sdedic
 * @author Vita Stejskal
 */
public final class MemoryPreferences  {
    /**
     * Returns an instance of Preferences backed by the delegate.
     * A token is used to identify the desired Preferences set. As long as {@link #destroy} is not called,
     * calls which use the same token & delegate will receive the same Preferences objects (though their
     * MemoryPreferences may differ). The returned object implements {@link OverridePreferences}
     * interface.
     * 
     * @param token token that determines the tree of Preferences.
     * @param delegate
     * @return MemoryPreferences accessor instance
     */
    public static MemoryPreferences get(Object token, Preferences delegate) {
        return new MemoryPreferences(ProxyPreferencesImpl.getProxyPreferences(token, delegate));
    }

    /**
     * Creates Preferences, which delegates to both persistent storage and parent (inherited) preferences.
     * The persistent storage takes precedence over the parent. The {@link Preferences#remove} call is redefined
     * for this case to just remove the key-value from the 'delegate', so that 'parent' value (if any) can become
     * effective. Before {@link Preferences#flush}, the returned Preferences object delegates to both 'parent'
     * and 'delegate' so that effective values can be seen. The returned object implements {@link OverridePreferences}
     * interface.
     * 
     * @param token 
     * @param parent
     * @param delegate
     * @return Preferences that respect inheritace from the 'parent'
     */
    public static MemoryPreferences getWithInherited(Object token, Preferences parent, Preferences delegate) {
        if (parent == null) {
            return get(token, delegate);
        }
        InheritedPreferences inh = new InheritedPreferences(parent, delegate);
        return new MemoryPreferences(ProxyPreferencesImpl.getProxyPreferences(token, inh));
    }
    
    /**
     * Provides the Preferences instance.
     * The instance will collect writes in memory, as described in {@link MemoryPreferences} doc.
     * 
     * @return instance of Preferences
     */
    public Preferences  getPreferences() {
        return prefInstance;
    }
    
    /**
     * Destroys the whole tree this Preferences belongs to.
     * Individual Preferences node will not be flushed or cleared, but will become
     * inaccessible from their token
     * 
     */
    public void destroy() {
        prefInstance.destroy();
    }
    
    /**
     * Suppresses events from this Preferences node.
     * During the Runnable execution, the Preferences node will not
     * fire any events.
     * 
     * @param r runnable to execute
     */
    public void runWithoutEvents(Runnable r) {
        try {
            prefInstance.silence();
            r.run();
        } finally {
            prefInstance.noise();
        }
    }
    
    /**
     * Checks whether the Preferences node is changed. 
     * Only value provided by the {@link #getPreferences} and values derived by call to {@link Preferences#node}
     * on that instance are supported. In other words, Preferences object from the tree managed by this
     * MemoryPreferences object. IllegalArgumentException can be thrown when an incompatible Preferences object
     * is used.
     * <p/>
     * True will be returned, if the Preferences object is dirty and not flushed.
     * 
     * @param pref preferences node to check
     * @return true, if the preferences was modified, and not flushed
     * @throws IllegalArgumentException if the pref node is not from the managed tree.
     */
    public boolean isDirty(Preferences pref) {
        if (!(pref instanceof ProxyPreferencesImpl)) {
            throw new IllegalArgumentException("Incompatible PreferencesImpl");
        }
        ProxyPreferencesImpl impl = (ProxyPreferencesImpl)pref;
        if (impl.node(prefInstance.absolutePath()) != prefInstance) {
            throw new IllegalArgumentException("The preferences tree root is not reachable");
        }
        return impl.isDirty();
    }

    private ProxyPreferencesImpl prefInstance;
    
    private MemoryPreferences(ProxyPreferencesImpl delegate) {
        this.prefInstance = delegate;
    }
}
