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

package org.netbeans.test.stub.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.test.stub.spi.StubImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public final class Stub {
    
    private static StubImplementation IMPL;
    
    static {
        IMPL = (StubImplementation)Lookup.getDefault().lookup(StubImplementation.class);
        if (IMPL == null) {
            IMPL = new DefaultStubImplementation();
        }
    }
    
    public static Object create(Class intf) {
        return create(new Class[] { intf });
    }
    
    public static Object create(Class intfs[]) {
        return IMPL.create(intfs);
    }
    
    public static Object create(Class intf, StubDelegate delegate) {
        return create(new Class[] { intf }, delegate);
    }
    
    public static Object create(Class[] intfs, StubDelegate delegate) {
        return IMPL.create(intfs, delegate);
    }
    
    public static Object getDelegate(Object stub) {
        return IMPL.getDelegate(stub);
    }
    
    public static void setProperty(Object stub, Object key, Object value) {
        IMPL.setProperty(stub, key, value);
    }
    
    private static final class DefaultStubImplementation implements StubImplementation {
    
        private static final Map/*<Object,Delegate>*/ STUB_TO_DELEGATE = new WeakHashMap();
        
        public Object create(Class[] intfs) {
            return create(intfs, new DefaultInvocationHandler());
        }

        public Object create(Class[] intfs, StubDelegate delegate) {
            Object stub = create(intfs, new MethodDelegatingInvocationHandler(delegate));
            STUB_TO_DELEGATE.put(stub, delegate);
            return stub;
        }
        
        private Object create(Class[] intfs, InvocationHandler handler) {
            return Proxy.newProxyInstance(Stub.class.getClassLoader(), intfs, handler);
        }
        
        public Object getDelegate(Object stub) {
            StubDelegate delegate = (StubDelegate)STUB_TO_DELEGATE.get(stub);
            if (delegate == null) {
                throw new IllegalArgumentException("No delegate for this stub. Is " + stub + " a stub?");
            }
            return delegate;
        }

        public void setProperty(Object stub, Object key, Object value) {
            ((StubDelegate)getDelegate(stub)).setProperty(key, value);
        }
    }
    
    /**
     * Invocation handler which delegates to another object (a delegate)'s methods.
     */
    private static final class MethodDelegatingInvocationHandler implements InvocationHandler {
        
        public Object delegate;
        
        public MethodDelegatingInvocationHandler(Object delegate) {
            this.delegate = delegate;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Method delegateMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
                return delegateMethod.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } catch (NoSuchMethodException e) {
                // we avoid an UndeclaredThrowableExeception
                // therefore displaying the caller and its stack trace
                throw new RuntimeException("No method " + method.getName() + " with params " + Arrays.asList(method.getParameterTypes()));
            }
        }
    }
    
    /**
     * Invocation handler returning sane values for primitive return types.
     */
    private static final class DefaultInvocationHandler implements InvocationHandler {
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class[] paramTypes = method.getParameterTypes();

            if ("hashCode".equals(methodName)) {
                return new Integer(System.identityHashCode(proxy));
            } else if ("equals".equals(methodName) && paramTypes.length == 1 && paramTypes[0] == Object.class) {
                return Boolean.valueOf(args[0] == proxy);
            }
                
            Class retClass = method.getReturnType();
            
            if (retClass.isPrimitive()) {
                if (retClass == Byte.TYPE) {
                    return new Byte((byte)0);
                } else if (retClass == Short.TYPE) {
                    return new Short((short)0);
                } else if (retClass == Integer.TYPE) {
                    return new Integer(0);
                } else if (retClass == Long.TYPE) {
                    return new Long(0L);
                } else if (retClass == Float.TYPE) {
                    return new Float(0);
                } else if (retClass == Double.TYPE) {
                    return new Double(0.0);
                } else if (retClass == Character.TYPE) {
                    return new Character('\0');
                } else if (retClass == Boolean.TYPE) {
                    return Boolean.FALSE;
                }
            }
                
            return null;
        }
    }
}
