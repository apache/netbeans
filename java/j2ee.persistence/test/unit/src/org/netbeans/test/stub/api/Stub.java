/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    
        private static final Map<Object, StubDelegate> STUB_TO_DELEGATE = new WeakHashMap<>();
        
        @Override
        public Object create(Class[] intfs) {
            return create(intfs, new DefaultInvocationHandler());
        }

        @Override
        public Object create(Class[] intfs, StubDelegate delegate) {
            Object stub = create(intfs, new MethodDelegatingInvocationHandler(delegate));
            STUB_TO_DELEGATE.put(stub, delegate);
            return stub;
        }
        
        private Object create(Class[] intfs, InvocationHandler handler) {
            return Proxy.newProxyInstance(Stub.class.getClassLoader(), intfs, handler);
        }
        
        @Override
        public Object getDelegate(Object stub) {
            StubDelegate delegate = (StubDelegate)STUB_TO_DELEGATE.get(stub);
            if (delegate == null) {
                throw new IllegalArgumentException("No delegate for this stub. Is " + stub + " a stub?");
            }
            return delegate;
        }

        @Override
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

        @Override
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
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class[] paramTypes = method.getParameterTypes();

            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            } else if ("equals".equals(methodName) && paramTypes.length == 1 && paramTypes[0] == Object.class) {
                return args[0] == proxy;
            }
                
            Class retClass = method.getReturnType();
            
            if (retClass.isPrimitive()) {
                if (retClass == Byte.TYPE) {
                    return (byte)0;
                } else if (retClass == Short.TYPE) {
                    return (short)0;
                } else if (retClass == Integer.TYPE) {
                    return 0;
                } else if (retClass == Long.TYPE) {
                    return 0L;
                } else if (retClass == Float.TYPE) {
                    return 0F;
                } else if (retClass == Double.TYPE) {
                    return 0D;
                } else if (retClass == Character.TYPE) {
                    return '\0';
                } else if (retClass == Boolean.TYPE) {
                    return Boolean.FALSE;
                }
            }
                
            return null;
        }
    }
}
