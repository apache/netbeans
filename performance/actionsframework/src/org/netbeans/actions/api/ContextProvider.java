/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * ContextProvider.java
 *
 * Created on January 24, 2004, 1:29 AM
 */

package org.netbeans.actions.api;

import java.util.Map;

/** A ContextProvider is supplied to the engine.  When the engine needs
 * to produce or update presenters, it will request the context from the
 * current contextProvider.  This context will be passed in calls to
 * the system ActionProvider when fetching the enablement & visibility
 * of actions.  The Map's contents is a private contract defined by
 * an implementation  of the actions framework.
 *
 * @author  Tim Boudreau
 */
public interface ContextProvider {
    /** Key for an object which can be fetched from the context map,
     * representing the identity of the context in some unique way.
     * If non null, the Engine will compare the result of this with
     * the previous result to determine if it needs to do any work
     * at all, or if nothing has changed, and any previous state it
     * set on presenters is still valid.
     * getContext().get(IDENTITY).equals(previousContext.get(IDENTITY)) if
     * the keysets of the last-fetched map and the current map contain the
     * same elements (which must be strings).  This makes it possible to
     * avoid updating actions if a context change has not made any change
     * that would affect the enablement of actions - i.e. selecting one
     * text file, then selecting another should cause the engine to be advised
     * that it may need to update actions, but in fact, no change in state
     * is needed.  So the engine will first test if the last identity equals
     * the current identity, and if so, never iterate the visible presenters
     * and try to change their state to the state they're already in.
     */
    static String IDENTITY = "identity"; //NOI18N
    
    /** Get a map representing the current application context.  All the keys
     * in this map must be Strings.  The values are up to the application.  In
     * determining if an action should be enabled, only the presence of a key
     * is used as the test.  Objects needed for the invocation of actions 
     * should be put into the map as values, with a well-defined key to test
     * against for whether the action should be enabled.  The invoker can
     * then retrieve the object whose method should be called and invoke
     * the method.
     * <p>
     * Implementations should preferably return a meaningful value from 
     * getContext(IDENTITY) which will be the same if the key set is the
     * same.
     * <p>
     * Note that the actions framework reserves the string &quot;identity&quot;
     * (ContextProvider.IDENTITY) as a map key - implementations of ContextProvider
     * should not use it as a map key or unpredictable things may happen.
     */
    public Map getContext();
}
