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

import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Contains information needed to start new debugging. Process of starting of
 * debugger can create one or more {@link Session} and one or more
 * {@link DebuggerEngine} and register them to {@link DebuggerManager}. For
 * more information about debugger start process see:
 * {@link DebuggerManager#startDebugging}.
 *
 * @author   Jan Jancura
 */
public final class DebuggerInfo implements ContextProvider {

    private Lookup lookup;


    /**
     * Creates a new instance of DebuggerInfo.
     *
     * @param typeID identification of DebuggerInfo type. Is used for 
     *      registration of external services.
     * @param services you can register additional services for this 
     *      DebuggerInfo here
     * @return returns a new instance of DebuggerInfo
     */
    public static DebuggerInfo create (
        String typeID,
        Object[] services
    ) {
        return new DebuggerInfo (
            typeID, 
            services
        );
    }
    
    private DebuggerInfo (
        String typeID,
        Object[] services
    ) {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 0, services.length);
        s [s.length - 1] = this;
        lookup = new Lookup.Compound (
            new Lookup.Instance (s),
            new Lookup.MetaInf (typeID)
        );
    }

    /**
     * Returns type identification of this session. This parameter is used for
     * registration of additional services in Meta-inf/debugger.
     *
     * @return type identification of this session
     */
//    public String getTypeID () {
//        return typeID;
//    }
    
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
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        return lookup.lookupFirst (folder, service);
    }
    
    Lookup getLookup () {
        return lookup;
    }
}

