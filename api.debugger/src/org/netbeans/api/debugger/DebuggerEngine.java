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

package org.netbeans.api.debugger;

import java.util.Collections;
import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Debugger Engine represents implementation of one debugger (Java Debugger,
 * CPP Debugger). It can support debugging of one or more
 * {@link Session}s, in one or more languages.
 * It provides root of threads hierarchy (call stacks, locals)
 * and manages debugger actions.
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td>
 *
 * <b>Support for actions:</b>
 *    DebuggerEngine manages list of actions ({@link #getActionsManager}). 
 *    Debugger action (implemented by 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}) can be registerred to 
 *    DebuggerEngine during a start of debugger. See 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}.
 *    ActionsManager can be used to call some debugger action 
 *    ({@link ActionsManager#doAction}) and to distinguish availability of action 
 *    ({@link ActionsManager#isEnabled}).
 *    Example how to call Kill Action on this engine:
 *    <pre>
 *    engine.getActionsManager ().doAction (ActionsManager.ACTION_KILL);</pre>
 *
 * <br>
 * <b>Support for aditional services:</b>
 *    DebuggerEngine is final class. That is why the standard method how to 
 *    extend its functionality is using lookup methods ({@link #lookup} and 
 *    {@link #lookupFirst}).
 *    There are two ways how to register some service provider for some
 *    type of DebuggerEngine:
 *    <ul>
 *      <li>Register 'live' instance of service provider during creation of 
 *        new instance of DebuggerEngine (see method
 *        {@link org.netbeans.spi.debugger.DebuggerEngineProvider#getServices}).
 *      </li>
 *      <li>Register service provider in Manifest-inf/debugger/&lt;type ID&gt;
 *        folder. See Debugger SPI for more information about
 *        registration.</li>
 *    </ul>
 *
 * <br>
 * <b>Support for listening:</b>
 *    DebuggerEngine propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link ActionsManagerListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td>
 *
 * This class is final, so it does not have any external provider.
 * Debugger Plug-ins and UI modules are clients of this class.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td>
 *
 * A new instance(s) of DebuggerEngine class are created in Debugger Core 
 * module only, during the process of starting of debugging (see
 * {@link DebuggerManager#startDebugging}.
 *
 * DebuggerEngine is removed automatically from {@link DebuggerManager} when the 
 * the last action is ({@link ActionsManager#ACTION_KILL}).
 *
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can
 * be added in future.
 *
 * </td></tr></tbody></table>
 *
 * @author   Jan Jancura
 */
public final class DebuggerEngine implements ContextProvider {
    
    
    // variables ...............................................................

    private Lookup                  privateLookup;
    private Lookup                  lookup;
    private ActionsManager          actionsManager;
    private Session                 s;

    
    DebuggerEngine (
        String typeID, 
        Session s, 
        Object[] services,
        Lookup sessionLookup
    ) {
        Object[] services1 = new Object [services.length + 1];
        System.arraycopy (services, 0, services1, 0, services.length);
        services1 [services1.length - 1] = this;
        this.privateLookup = new Lookup.Compound (
                new Lookup.Instance (services1),
                new Lookup.MetaInf (typeID)
            );
        this.lookup = new Lookup.Compound (
            privateLookup,
            sessionLookup
        );
        this.s = s;
    }

    Lookup getLookup() {
        return lookup;
    }

    Lookup getPrivateLookup() {
        return privateLookup;
    }
    
//    /**
//     * Returns list of services of given type.
//     *
//     * @param service a type of service to look for
//     * @return list of services of given type
//     */
//    public List lookup (Class service) {
//        return lookup.lookup (null, service);
//    }
//    
//    /**
//     * Returns one service of given type.
//     *
//     * @param service a type of service to look for
//     * @return ne service of given type
//     */
//    public Object lookupFirst (Class service) {
//        return lookup.lookupFirst (null, service);
//    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        if (service.equals(Session.class)) {
            return (List<? extends T>) Collections.singletonList(s);
        }
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        if (service.equals(Session.class)) {
            return (T) s;
        }
        return lookup.lookupFirst (folder, service);
    }
    
    
    // main public methods .....................................................


    public synchronized ActionsManager getActionsManager () {
        if (actionsManager == null)
            actionsManager = new ActionsManager (lookup);
        return actionsManager;
    }
    
    
    // innerclasses ............................................................

    /**
     * This class notifies about DebuggerEngine remove from the system, and
     * about changes in language support. Instance of Destructor can be 
     * obtained from: {@link org.netbeans.spi.debugger.DebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}, or
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}.
     */
    public class Destructor {
        
        /**
         * Removes DebuggerEngine form all sessions.
         */
        public void killEngine () {
            Session[] ss = DebuggerManager.getDebuggerManager ().getSessions ();
            int i, k = ss.length;
            for (i = 0; i < k; i++)
                ss [i].removeEngine (DebuggerEngine.this);
            getActionsManager ().destroy ();
        }
        
        /**
         * Removes given language support from given session.
         *
         * @param s a session
         * @param language a language to be removed
         */
        public void killLanguage (Session s, String language) {
            s.removeLanguage (language, DebuggerEngine.this);
            getActionsManager ().destroy ();
        }
    }
}

