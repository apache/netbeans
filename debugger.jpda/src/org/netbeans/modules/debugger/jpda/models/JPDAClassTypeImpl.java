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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InternalException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluatorVisitor;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InterfaceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAClassTypeImpl implements JPDAClassType {
    
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N
    
    private final JPDADebuggerImpl debugger;
    private final ReferenceType classType;
//    private long cachedInstanceCount = -1L;
    
    /**
     * Creates a new instance of JPDAClassTypeImpl
     */
    public JPDAClassTypeImpl(JPDADebuggerImpl debugger, ReferenceType classType) {
        this.debugger = debugger;
        this.classType = classType;
    }
    
    protected final JPDADebuggerImpl getDebugger() {
        return debugger;
    }
    
    public ReferenceType getType() {
        return classType;
    }

    @Override
    public String getName() {
        return classType.name();
    }

    @Override
    public String getSourceName() throws AbsentInformationException {
        return classType.sourceName();
    }

    @Override
    public ClassVariable classObject() {
        ClassObjectReference co;
        try {
            co = ReferenceTypeWrapper.classObject(classType);
        } catch (InternalExceptionWrapper | ObjectCollectedExceptionWrapper |
                 VMDisconnectedExceptionWrapper | UnsupportedOperationExceptionWrapper ex) {
            co = null;
        }
        return new ClassVariableImpl(debugger, co, "");
    }
    
    @Override
    public ObjectVariable getClassLoader() {
        ClassLoaderReference cl;
        try {
            cl = ReferenceTypeWrapper.classLoader(classType);
        } catch (InternalExceptionWrapper | ObjectCollectedExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
            cl = null;
        }
        return new AbstractObjectVariable(debugger, cl, "Loader "+getName());
    }
    
    @Override
    public Super getSuperClass() {
        if (classType instanceof ClassType) {
            try {
                ClassType superClass = ClassTypeWrapper.superclass((ClassType) classType);
                if (superClass == null) {
                    return null;
                } else {
                    return new SuperVariable(debugger, null, superClass, getName());
                }
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    @Override
    public List<JPDAClassType> getSubClasses() {
        if (classType instanceof ClassType) {
            List<ClassType> subclasses = ClassTypeWrapper.subclasses0((ClassType) classType);
            return getTypes(subclasses);
        }
        if (classType instanceof InterfaceType) {
            List<InterfaceType> subinterfaces = InterfaceTypeWrapper.subinterfaces0((InterfaceType) classType);
            List<ClassType> implementors = InterfaceTypeWrapper.implementors0((InterfaceType) classType);
            int ss = subinterfaces.size();
            int is = implementors.size();
            if (ss > 0 || is > 0) {
                List<JPDAClassType> subClasses = new ArrayList(ss + is);
                for (InterfaceType subclass : subinterfaces) {
                    subClasses.add(debugger.getClassType(subclass));
                }
                for (ClassType subclass : implementors) {
                    subClasses.add(debugger.getClassType(subclass));
                }
                return Collections.unmodifiableList(subClasses);
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<JPDAClassType> getAllInterfaces() {
        if (classType instanceof ClassType) {
            try {
                List<InterfaceType> allInterfaces = ClassTypeWrapper.allInterfaces0((ClassType) classType);
                return getTypes(allInterfaces);
            } catch (ClassNotPreparedExceptionWrapper ex) {
                // Nothing to return
            }
        } else if (classType instanceof InterfaceType) {
            try {
                List<InterfaceType> allInterfaces = new ArrayList<>();
                List<InterfaceType> processedInterfaces = new ArrayList<>();
                List<InterfaceType> directInterfaces = InterfaceTypeWrapper.superinterfaces0((InterfaceType) classType);
                allInterfaces.addAll(directInterfaces);
                while (processedInterfaces.size() < allInterfaces.size()) {
                    InterfaceType it = allInterfaces.get(processedInterfaces.size());
                    directInterfaces = InterfaceTypeWrapper.superinterfaces0(it);
                    for (InterfaceType di : directInterfaces) {
                        if (!allInterfaces.contains(di)) {
                            allInterfaces.add(di);
                        }
                    }
                    processedInterfaces.add(it);
                }
                return getTypes(allInterfaces);
            } catch (ClassNotPreparedExceptionWrapper ex) {
                // Nothing to return
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<JPDAClassType> getDirectInterfaces() {
        if (classType instanceof ClassType) {
            try {
                List<InterfaceType> directInterfaces = ClassTypeWrapper.interfaces0((ClassType) classType);
                return getTypes(directInterfaces);
            } catch (ClassNotPreparedExceptionWrapper ex) {
                // Nothing to return
            }
        } else if (classType instanceof InterfaceType) {
            try {
                List<InterfaceType> directInterfaces = InterfaceTypeWrapper.superinterfaces0((InterfaceType) classType);
                return getTypes(directInterfaces);
            } catch (ClassNotPreparedExceptionWrapper ex) {
                // Nothing to return
            }
        }
        return Collections.EMPTY_LIST;
    }

    private List<JPDAClassType> getTypes(Collection<? extends ReferenceType> types) {
        if (types.size() > 0) {
            types = new LinkedHashSet<>(types); // To remove duplicities
            List<JPDAClassType> interfaces = new ArrayList(types.size());
            for (ReferenceType intrfc : types) {
                interfaces.add(debugger.getClassType(intrfc));
            }
            return Collections.unmodifiableList(interfaces);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public boolean isInstanceOf(String className) {
        List<ReferenceType> classTypes;
        try {
            classTypes = VirtualMachineWrapper.classesByName(MirrorWrapper.virtualMachine(classType), className);
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        for (ReferenceType rt : classTypes) {
            try {
                if (EvaluatorVisitor.instanceOf(classType, rt)) {
                    return true;
                }
            } catch (VMDisconnectedException vmdex) {
                return false;
            } catch (InternalException iex) {
                // procceed
            }
        }
        return false;
    }

    @Override
    public List<Field> staticFields() {
        List<com.sun.jdi.Field> allFieldsOrig;
        try {
            allFieldsOrig = ReferenceTypeWrapper.allFields0(classType);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return Collections.emptyList();
        }
        List<Field> staticFields = new ArrayList<>();
        String parentID = getName();
        for (int i = 0; i < allFieldsOrig.size(); i++) {
            Value value = null;
            com.sun.jdi.Field origField = allFieldsOrig.get(i);
            try {
                if (TypeComponentWrapper.isStatic(origField)) {
                    if (origField.signature().length() == 1) {
                        // Must be a primitive type or the void type
                        staticFields.add(new FieldVariable(debugger, origField, parentID, null));
                    } else {
                        ObjectFieldVariable ofv;
                        if (TypeComponentWrapper.declaringType(origField) instanceof ClassType) {
                            ofv = new ClassFieldVariable(debugger, origField, parentID,
                                    JPDADebuggerImpl.getGenericSignature(origField), null);
                        } else {
                            ofv = new ObjectFieldVariable(debugger, origField, parentID,
                                    JPDADebuggerImpl.getGenericSignature(origField), null);
                        }
                        staticFields.add(ofv);
                    }
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            }
        }
        return staticFields;
    }

    @Override
    public Variable invokeMethod(String methodName, String signature, Variable[] arguments)
                                throws NoSuchMethodException, InvalidExpressionException {
        return invokeMethod(null, methodName, signature, arguments);
    }
    
    public Variable invokeMethod(JPDAThread thread, String methodName, String signature, Variable[] arguments)
                                throws NoSuchMethodException, InvalidExpressionException {
        Value v;
        try {
            if (!(classType instanceof ClassType)) {
                throw new NoSuchMethodException(classType+" is not ClassType");
            }
            ClassType ct = (ClassType) classType;
            // 1) find corrent method
            Method method = null;
            if (signature != null) {
                method = ClassTypeWrapper.concreteMethodByName(
                        ct,
                        methodName, signature);
            } else {
                List l = ReferenceTypeWrapper.methodsByName(
                        ct,
                        methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++) {
                    if ( !MethodWrapper.isAbstract((Method) l.get (j)) &&
                         MethodWrapper.argumentTypeNames((Method) l.get (j)).isEmpty()
                    ) {
                        method = (Method) l.get (j);
                        break;
                    }
                }
            }
            
            // 2) method not found => print all method signatures
            if (method == null) {
                List l = ReferenceTypeWrapper.methodsByName(
                        ct, methodName);
                int j, jj = l.size ();
                loggerValue.log(Level.INFO, "No method {0}.{1} with signature {2}",
                           new Object[]{ TypeWrapper.name(ct), methodName, signature });
                loggerValue.info("Found following methods with signatures:");
                for (j = 0; j < jj; j++) {
                    loggerValue.info (TypeComponentWrapper.signature((Method) l.get (j)));
                }
                throw new NoSuchMethodException (
                    TypeWrapper.name(ct) + "." +
                        methodName + " : " + signature
                );
            }
            
            // 3) call this method
            Value[] vs = new Value [arguments.length];
            int i, k = arguments.length;
            for (i = 0; i < k; i++) {
                if (arguments[i] == null) {
                    vs[i] = null;
                } else {
                    vs[i] = ((AbstractVariable) arguments [i]).getInnerValue ();
                }
            }
            v = getDebugger().invokeMethod (
                (JPDAThreadImpl) thread,
                ct,
                method,
                vs
            );
            
            // 4) encapsulate result
            if (v instanceof ObjectReference) {
                return new AbstractObjectVariable ( // It's also ObjectVariable
                        getDebugger(),
                        (ObjectReference) v,
                        getName() + method + "^"
                    );
            }
            return new AbstractVariable (getDebugger(), v, getName() + method);
        } catch (InternalExceptionWrapper | ClassNotPreparedExceptionWrapper |
                 VMDisconnectedExceptionWrapper | ObjectCollectedExceptionWrapper ex) {
            return null;
        }
    }
    
    @Override
    public long getInstanceCount() {//boolean refresh) {
            /*synchronized (this) {
                if (!refresh && cachedInstanceCount > -1L) {
                    return cachedInstanceCount;
                }
            }*/
            //assert !java.awt.EventQueue.isDispatchThread() : "Instance counts retrieving in AWT Event Queue!";
            try {
                long[] counts = VirtualMachineWrapper.instanceCounts(MirrorWrapper.virtualMachine(classType), Collections.singletonList(classType));
                return counts[0];
            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
                return 0L;
            }
            /*synchronized (this) {
                cachedInstanceCount = counts[0];
            }*/
    }
    
    @Override
    public List<ObjectVariable> getInstances(long maxInstances) {
            //assert !java.awt.EventQueue.isDispatchThread() : "Instances retrieving in AWT Event Queue!";
            final List<ObjectReference> instances;
            try {
                instances = ReferenceTypeWrapper.instances(classType, maxInstances);
            } catch (ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper |
                     InternalExceptionWrapper ex) {
                return Collections.emptyList();
            }
            return new AbstractList<ObjectVariable>() {
                @Override
                public ObjectVariable get(int i) {
                    ObjectReference obj = instances.get(i);
                    return new AbstractObjectVariable(debugger, obj, classType.name()+" instance "+i);
                }

                @Override
                public int size() {
                    return instances.size();
                }
            };
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JPDAClassTypeImpl)) {
            return false;
        }
        return classType.equals(((JPDAClassTypeImpl) o).classType);
    }
    
    @Override
    public int hashCode() {
        return classType.hashCode() + 1000;
    }
}
