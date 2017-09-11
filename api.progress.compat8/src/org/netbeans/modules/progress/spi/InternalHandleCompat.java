/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.openide.modules.ConstructorDelegate;
import org.openide.modules.PatchFor;
import org.openide.util.*;

/**
 * Resurrects the method removed from InternalHandle. Unline other *Compat
 * classes, we need to bridge all requests to some real implementation. The
 * is theoretically able to instantiate an InternalHandle directly, without
 * using a factory. Still the Handle should work somehow, so a Swing-based delegate
 * is created (old clients are used to that). The InternalHandle itself
 * is changed so if it has a delegate, delegates everything instead of executing
 * its own code. That way, the direct instance of InternalHandle works in the
 * current NetBeans environment - forwards everything to a proper implementation.
 * <p/>
 * Sadly, this module cannot depend on api.progress.nb, as it would create a
 * circular dependency between api.progress.nb and api.progress, so all the work
 * must be done using reflection.
 * 
 * @author sdedic
 */
@PatchFor(InternalHandle.class)
public class InternalHandleCompat {
    static final Class uiClazz;
    static final Constructor ctor;
    static final Method component;
    static final Method detailLabel;
    static final Method mainLabel;
    static {
        try {
            uiClazz = Class.forName("org.netbeans.modules.progress.spi.UIInternalHandle",
                    true, Lookup.getDefault().lookup(ClassLoader.class));
            ctor = uiClazz.getDeclaredConstructor(
                    String.class,
                    Cancellable.class,
                    Boolean.TYPE,
                    javax.swing.Action.class);
            component = uiClazz.getMethod("extractComponent");
            detailLabel = uiClazz.getMethod("extractDetailLabel");
            mainLabel = uiClazz.getMethod("extractMainLabel");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    /**
     * Delegate from InternalHandle, duplicate, but provides better access.
     */
    private InternalHandle delegate;
    
    public synchronized JComponent extractComponent() {
        try {
            return (JComponent)component.invoke(delegate);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public synchronized JLabel extractDetailLabel() {
        try {
            return (JLabel)detailLabel.invoke(delegate);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public synchronized JLabel extractMainLabel() {
        try {
            return (JLabel)mainLabel.invoke(delegate);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    @ConstructorDelegate(delegateParams = { 1, 2, 3 })
    public static void create(InternalHandleCompat c, 
            String displayName, Cancellable cancel, boolean userInitiated, 
            Action action) {
        InternalHandle ih = (InternalHandle)(Object)c;
        if (ih.getClass() != InternalHandle.class) {
            return;
        }
        try {
            c.delegate  = (InternalHandle) ctor.newInstance(displayName, cancel, userInitiated, action);
            ih.del = c.delegate;
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Backports data from public constructor. This method is called from the public
     * constructor of InternalHandle, so the delegate is initialized even when
     * using 'legal' but direct creation 
     * @param s display name
     * @param c cancel detection callback
     * @param u true if user-initiated handle.
     */
    protected void compatInit(String s, Cancellable c, boolean u) {
        create(this, s, c, u, null);
    }
}
