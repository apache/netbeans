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

package org.openide.loaders;

import javax.swing.event.ChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.List;

/** Utility to add a loader to the loader pool.
 * Should only be used in external execution mode!
 * Requires core.jar in classpath.
 * @author Jesse Glick
 */
public final class AddLoaderManuallyHid {

    private AddLoaderManuallyHid() {}

    /** Add a loader to the pool (to the front of the free area). */
    public static void addRemoveLoader(DataLoader l, boolean add) throws Exception {
        // Initialize IDE:
//        TopManager.getDefault();
        
        // Now add the loader. Would be easy enough if we could directly access
        // core classes, but then this test would have to be compiled with core.jar
        // in the classpath...
        Class lpnClazz = Class.forName("org.netbeans.core.NbLoaderPool");
        Field loadersF = lpnClazz.getDeclaredField("loaders");
        loadersF.setAccessible(true);
        List loaders = (List)loadersF.get(null);
        if (add) {
            if (loaders.contains(l)) throw new IllegalArgumentException();
            loaders.add(0, l);
        } else {
            if (! loaders.contains(l)) throw new IllegalArgumentException();
            loaders.remove(l);
        }
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        if (add) {
            l.addPropertyChangeListener((PropertyChangeListener)pool);
        } else {
            l.removePropertyChangeListener((PropertyChangeListener)pool);
        }
        // Simulate behavior of update(), but fire pool change immediately:
        Field loadersArrayF = lpnClazz.getDeclaredField("loadersArray");
        loadersArrayF.setAccessible(true);
        loadersArrayF.set(null, null);
        pool.fireChangeEvent(new ChangeEvent(pool));
    }
    
}
