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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * Helps to translate one tree to another.
 *
 * Used just for ThreadsTreeModel
 *
 * @author   Jan Jancura
 */
public final class ObjectTranslation {
    
    private static final int THREAD_ID = 0;
    private static final int LOCALS_ID = 2;
    
    private JPDADebuggerImpl debugger;
    private int translationID;
    
    /* original Object to a new one.*/
    private final WeakHashMap<Mirror, WeakReference<Object>> cache
            = new WeakHashMap<Mirror, WeakReference<Object>>();
    
    
    /**
     * Creates a new instance of translating tree model for given 
     * tree model.
     *
     * @param model a tree model to be translated
     */
    private ObjectTranslation (JPDADebuggerImpl debugger, int translationID) {
        this.debugger = debugger;
        this.translationID = translationID;
    }
    
    /**
     * Creates a new translated node for given original one.
     *
     * @param o a node to be translated
     * @return a new translated node
     */
    private Object createTranslation (Object o) {
        switch (translationID) {
            case THREAD_ID:
                if (o instanceof ThreadReference) {
                    return new JPDAThreadImpl ((ThreadReference) o, debugger);
                } else if (o instanceof ThreadGroupReference) {
                    return new JPDAThreadGroupImpl ((ThreadGroupReference) o, debugger);
                } else {
                    return null;
                }
            case LOCALS_ID:
                if (o instanceof ArrayType) {
                    return new JPDAArrayTypeImpl(debugger, (ArrayType) o);
                }
                if (o instanceof ReferenceType) {
                    return new JPDAClassTypeImpl(debugger, (ReferenceType) o);
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    private Object createTranslation (Object o, Object v) {
        switch (translationID) {
            case LOCALS_ID:
                if (o instanceof LocalVariable && (v == null || v instanceof Value)) {
                    LocalVariable lv = (LocalVariable) o;
                    org.netbeans.api.debugger.jpda.LocalVariable local;
                    if (v instanceof ObjectReference || v == null) {
                        local = new ObjectLocalVariable (
                            debugger, 
                            (ObjectReference) v, 
                            null, 
                            lv, 
                            JPDADebuggerImpl.getGenericSignature (lv), 
                            null
                        );
                    } else {
                        local = new Local (debugger, (PrimitiveValue) v, null, lv, null);
                    }
                    return local;
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    private void verifyTranslation (Object t, Object o, Object v) {
        switch (translationID) {
            case LOCALS_ID:
                if (t instanceof AbstractVariable) {
                    AbstractVariable local = ((AbstractVariable) t);
                    Value lv = local.getInnerValue();
                    if (lv == null && v != null || lv != null && !lv.equals(v)) {
                        local.setInnerValue((Value) v);
                    }
                    return ;
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object.
     *
     * @param o the Mirror object in the debuggee
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o) {
        Object r = null;
        synchronized (cache) {
            WeakReference wr = cache.get (o);
            if (wr != null)
                r = wr.get ();
            if (r == null) {
                r = createTranslation (o);
                cache.put (o, new WeakReference<Object>(r));
            }
        }
        return r;
    }
    
    /**
     * Gen an existing wrapper object translation of a debuggee Mirror.
     *
     * @param o the Mirror object in the debuggee
     * @return translated object or <code>null</code> when there is no existing
     *         translation.
     */
    public Object translateExisting(Mirror o) {
        Object r = null;
        synchronized (cache) {
            WeakReference wr = cache.get (o);
            if (wr != null)
                r = wr.get ();
        }
        return r;
    }
    
    /**
     * Get all live objects that were translated.
     */
    public Collection getTranslated() {
        Collection translated = new HashSet();
        synchronized (cache) {
            Collection references = cache.values();
            for (Iterator it = references.iterator(); it.hasNext(); ) {
                WeakReference wr = (WeakReference) it.next();
                Object r = wr.get();
                if (r != null) {
                    translated.add(r);
                }
            }
        }
        return translated;
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object.
     *
     * @param o the Mirror object in the debuggee
     * @param v an additional argument used for the translation
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o, Object v) {
        Object r = null;
        boolean verify = false;
        synchronized (cache) {
            WeakReference wr = cache.get (o);
            if (wr != null)
                r = wr.get ();
            if (r == null) {
                r = createTranslation (o, v);
                cache.put (o, new WeakReference<Object>(r));
            } else {
                verify = true;
            }
        }
        if (verify) {
            verifyTranslation(r, o, v);
        }
        return r;
    }
    
    /**
     * Explicitly remove the translation of the mirror object.
     */
    public void remove(Mirror o) {
        synchronized (cache) {
            cache.remove(o);
        }
    }
    
    public static ObjectTranslation createThreadTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, THREAD_ID);
    }
    
    public static ObjectTranslation createLocalsTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, LOCALS_ID);
    }
    
}
