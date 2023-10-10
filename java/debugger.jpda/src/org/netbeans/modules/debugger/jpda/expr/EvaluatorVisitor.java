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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatType;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.source.tree.AnnotatedTypeTree;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import javax.lang.model.type.TypeVariable;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext.ScriptVariable;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext.VariableInfo;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InterfaceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.openide.util.NbBundle;

/**
 * Mirror is either Value or ReferenceType
 *
 * @author Martin Entlicher
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public class EvaluatorVisitor extends ErrorAwareTreePathScanner<Mirror, EvaluationContext> {

    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N

    private static final Value NO_VALUE = new NoValue();
    
    private Type newArrayType;
    private JavaExpression expression;
    private Map<Tree, Type> subExpressionTypes = new IdentityHashMap<Tree, Type>();

    public EvaluatorVisitor(JavaExpression expression) {
        this.expression = expression;
    }

    private Type getSubExpressionType(Tree t) {
        Type type = subExpressionTypes.get(t);
        if (type != null) {
            return type;
        } else {
            if (t.getKind() == Tree.Kind.PARENTHESIZED) {
                return getSubExpressionType(((ParenthesizedTree) t).getExpression());
            }
            return null;
        }
    }

    @Override
    public Mirror scan(Tree tree, EvaluationContext evaluationContext) {
        Mirror result = super.scan(tree, evaluationContext);
        if (result instanceof ArtificialMirror) {
            return ((ArtificialMirror) result).getVMMirror();
        }
        return result;
    }

    @Override
    public Mirror scan(TreePath path, EvaluationContext evaluationContext) {
        Mirror result = super.scan(path, evaluationContext);
        if (result instanceof ArtificialMirror) {
            return ((ArtificialMirror) result).getVMMirror();
        }
        return result;
    }

    @Override
    public Mirror scan(Iterable<? extends Tree> nodes, EvaluationContext evaluationContext) {
        Mirror result = super.scan(nodes, evaluationContext);
        if (result instanceof ArtificialMirror) {
            return ((ArtificialMirror) result).getVMMirror();
        }
        return result;
    }

    @Override
    public Mirror visitAnnotation(AnnotationTree arg0, EvaluationContext evaluationContext) {
        return null;
    }

    @Override
    public Mirror visitMethodInvocation(MethodInvocationTree arg0, EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert.error(arg0, "canNotInvokeMethods");
        }
        if (loggerMethod.isLoggable(Level.FINE)) {
            loggerMethod.log(Level.FINE, "STARTED : {0} in thread {1}", new Object[]{arg0, evaluationContext.getFrame().thread()});
        }
        Mirror object = null;
        String methodName;
        Boolean isStatic = null;
        ExpressionTree expr = arg0.getMethodSelect();
        Element elm;
        Type preferredType = null;
        TreePath currentPath = getCurrentPath();
        if (expr.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mst = (MemberSelectTree) expr;
            object = mst.getExpression().accept(this, evaluationContext);
            methodName = mst.getIdentifier().toString();
            if (object == null) {
                Assert.error(arg0, "methodCallOnNull", methodName);
            }
            if (mst.getExpression().toString().equals("super")) {
                preferredType = getSubExpressionType(mst.getExpression());
            }
            if (currentPath != null) {
                TreePath memberSelectPath = TreePath.getPath(currentPath, mst);
                if (memberSelectPath == null) {
                    memberSelectPath = currentPath;
                }
                elm = evaluationContext.getTrees().getElement(memberSelectPath);
            } else {
                elm = null;
            }
        } else {
            if (currentPath != null) {
                TreePath methodInvokePath = TreePath.getPath(currentPath, arg0);
                if (methodInvokePath == null) {
                    methodInvokePath = currentPath;
                }
                elm = evaluationContext.getTrees().getElement(methodInvokePath);
                if (elm != null) {
                    methodName = elm.getSimpleName().toString();
                } else {
                    methodName = expr.toString();
                }
            } else {
                elm = null;
                methodName = expr.toString();
            }
        }
        List<? extends TypeMirror> paramTypes = null;
        String enclosingClass = null;
        boolean isVarArgs = false;
        if (elm != null) {
            TypeMirror typeMirror = elm.asType();
            TypeKind kind = typeMirror.getKind();
            if (kind == TypeKind.ERROR) { // In case of error type resolution we do not know parameter types
                elm = null;
            } else {
                if (kind != TypeKind.EXECUTABLE) {
                    Assert.error(arg0, "noSuchMethod", elm.getSimpleName().toString(), elm.getEnclosingElement().getSimpleName().toString());
                }
                ExecutableElement methodElement = (ExecutableElement) elm;
                isVarArgs = methodElement.isVarArgs();
                ExecutableType execTypeMirror = (ExecutableType) typeMirror;
                paramTypes = execTypeMirror.getParameterTypes();
                isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
                Element enclosing = methodElement.getEnclosingElement();
                if (enclosing.getKind() == ElementKind.CLASS) {
                    TypeElement enclosingClassElement = (TypeElement) enclosing;
                    enclosingClass = ElementUtilities.getBinaryName(enclosingClassElement);
                }
            }
        }

        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if (argValue != null && !(argValue instanceof Value)) {
                Assert.error(arg, "Not a value");
            }
            if (argValue instanceof ArtificialMirror) {
                argValue = ((ArtificialMirror)argValue).getVMMirror();
            }
            argVals.add((Value) argValue);
        }
        List<Type> argTypes = null;
        if (elm == null) {
            argTypes = new ArrayList<Type>(argVals.size());
            for (Value value : argVals) {
                if (value == null) {
                    ClassType objectClass = (ClassType) evaluationContext.getVMCache()
                            .getClass(Object.class.getName());
                    if (objectClass == null) {
                        return null;
                    }
                    argTypes.add(objectClass);
                } else {
                    argTypes.add(value.type());
                }
            }
        }
        ObjectReference objectReference;
        ReferenceType type;
        Method method = null;
        if (isStatic == null) {
            if (object instanceof ClassType || object instanceof ArrayType) {
                type = (ReferenceType) object;
                objectReference = null;
                isStatic = Boolean.TRUE;
            } else if (object instanceof ObjectReference) {
                objectReference = (ObjectReference) object;
                type = (ReferenceType) preferredType;
                if (type == null) {
                    type = (ReferenceType) objectReference.type();
                }
            } else {
                objectReference = evaluationContext.getContextObject();
                if (objectReference != null) {
                    type = objectReference.referenceType();
                } else {
                    Assert.error(arg0, "invokeInstanceMethodAsStatic", methodName);
                    type = (ReferenceType) evaluationContext.getFrame().location().declaringType();
                }
            }
        } else if (isStatic) {
            objectReference = null;
            if (object instanceof ClassType || object instanceof InterfaceType || object instanceof ArrayType) {
                type = (ReferenceType) object;
            } else if (object instanceof ObjectReference) {
                type = (ReferenceType) ((ObjectReference) object).type();
            } else {
                type = evaluationContext.getFrame().location().declaringType();
                if (enclosingClass != null) {
                    ReferenceType dt = findEnclosingType(type, enclosingClass,
                                                         evaluationContext.getVMCache());
                    if (dt != null) {
                        type = dt;
                    }
                }
            }
        } else {
            if (object != null) {
                if (object instanceof ClassType) {
                    Assert.error(arg0, "invokeInstanceMethodAsStatic", methodName);
                    objectReference = null;
                    type = null;
                } else if (object instanceof InterfaceType) {
                    Assert.error(arg0, "invokeInstanceMethodAsStatic", methodName);
                    objectReference = null;
                    type = null;
                } else {
                    objectReference = (ObjectReference) object;
                    type = (ReferenceType) preferredType;
                    if (type == null) {
                        type = objectReference.referenceType();
                        if (enclosingClass != null) {
                            Method[] methodPtr = new Method[] { null };
                            ReferenceType enclType = findEnclosingTypeWithMethod(
                                    type, enclosingClass, methodName, paramTypes,
                                    argTypes, methodPtr, evaluationContext.getVMCache());
                            if (enclType != null) {
                                method = methodPtr[0];
                                type = enclType;
                            }
                        }
                    }
                }
            } else {
                objectReference = evaluationContext.getContextObject();
                if (objectReference != null) {
                    type = objectReference.referenceType();
                    if (enclosingClass != null) {
                        ReferenceType enclType = findEnclosingType(type, enclosingClass,
                                                                   evaluationContext.getVMCache());
                        if (enclType != null) {
                            ObjectReference enclObject = findEnclosingObject(arg0, objectReference, enclType, null, methodName);
                            if (enclObject != null) {
                                type = enclObject.referenceType();
                            } else {
                                Assert.error(arg0, "noSuchMethod", methodName, type.name());
                            }
                        }
                    }
                } else {
                    type = null;
                }
            }
            if (objectReference == null) {
                Assert.error(arg0, "methodCallOnNull", methodName);
            }
        }
        ClassType cType;
        InterfaceType iType = null;
        if (type instanceof ArrayType) {
            Assert.error(arg0, "methOnArray");
            return null;
        } else if (type instanceof ClassType) {
            cType = (ClassType) type;
        } else {
            if (JPDAUtils.IS_JDK_180_40 && (type instanceof InterfaceType) && isStatic) {
                cType = null;
                iType = (InterfaceType) type;
            } else {
                Assert.error(arg0, "methOnInterface");
                return null;
            }
        }
        if (method == null) {
            method = getConcreteMethodAndReportProblems(arg0, type, methodName, null, paramTypes, argTypes);
        }
        if (isVarArgs) {
            transformVarArgsValues(arg0, argVals, paramTypes, evaluationContext);
        }
        if (cType != null) {
            return invokeMethod(arg0, method, isStatic, cType, objectReference, argVals, evaluationContext, preferredType != null);
        } else {
            return invokeMethod(arg0, method, iType, argVals, evaluationContext);
        }
    }

    /**
     * Transform the var-arg arguments to an array.
     * @param argVals The arguments, where the last items are replaced with an array.
     * @param paramTypes Appropriate parameter types.
     * @param evaluationContext
     * @return The created array, with disabled collection, which needs to get enabled collection after it's use.
     */
    private ArrayReference transformVarArgsValues(Tree arg0, List<Value> argVals, List<? extends TypeMirror> paramTypes, EvaluationContext evaluationContext) {
        int varIndex = paramTypes.size() - 1;
        TypeMirror tm = paramTypes.get(varIndex);
        if (tm.getKind() != TypeKind.ARRAY) {
            return null;
        }
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        String typeName = getTypeName(((javax.lang.model.type.ArrayType) tm).getComponentType());
        int length = argVals.size() - varIndex;
        ArrayType at = (ArrayType) getOrLoadClass(vm, typeName+"[]", evaluationContext);
        if (at == null) {
            Assert.error(arg0, "unknownType", typeName+"[]");
        }
        if (length == 1) {
            Value varArg = argVals.get(varIndex);
            if (varArg instanceof ArrayReference) {
                if (dimension(at) == dimension((ArrayType) ((ArrayReference) varArg).type())) {
                    // The argument is already an array corresponding to vararg
                    return null;
                }
            }
        }
        ArrayReference array = createArrayMirrorWithDisabledCollection(at, length, evaluationContext);
        List<Value> elements = new ArrayList<Value>(length);
        for (int i = 0; i < length; i++) {
            elements.add(argVals.get(varIndex + i));
        }
        try {
            autoboxElements(null, at.componentType(), elements, evaluationContext);
        } catch (ClassNotLoadedException cnlex) {}
        try {
            array.setValues(elements);
        } catch (InvalidTypeException ex) {
            throw new IllegalStateException("ArrayType "+at+" can not have "+elements+" elements.");
        } catch (ClassNotLoadedException ex) {
            throw new IllegalStateException(ex);
        }
        for (int i = argVals.size() - 1; i >= varIndex; i--) {
            argVals.remove(i);
        }
        argVals.add(array);
        return array;
    }
    
    // Find out the array dimension
    private static int dimension(ArrayType at) {
        int d = 1;
        Type ct;
        try {
            while ((ct = at.componentType()) instanceof ArrayType) {
                d++;
                at = (ArrayType) ct;
            }
        } catch (ClassNotLoadedException ex) {}
        return d;
    }
    
    /*private Method getConcreteMethod(ReferenceType type, String methodName, List<? extends ExpressionTree> typeArguments) {
        List<Method> methods = type.methodsByName(methodName);
        String signature = createSignature(typeArguments);
        for (Method method : methods) {
            if (egualMethodSignatures(method.signature(), signature)) {
                return method;
            }
        }
        return null;
    }*/

    private ReferenceType findEnclosingTypeWithMethod(ReferenceType type,
                                                      String enclosingClass,
                                                      String methodName,
                                                      List<? extends TypeMirror> paramTypes,
                                                      List<? extends Type> argTypes,
                                                      Method[] methodPtr,
                                                      VMCache vmCache) {
        ReferenceType etype = findEnclosingType(type, enclosingClass, vmCache);
        if (etype == null) {
            return null;
        }
        Method method;
        try {
            if (paramTypes != null) {
                method = getConcreteMethod(etype, methodName, null, paramTypes);
            } else {
                method = getConcreteMethod2(etype, methodName, argTypes);
            }
        } catch (UnsuitableArgumentsException uaex) {
            method = null;
        }
        if (method != null) {
            methodPtr[0] = method;
            return etype;
        } else {
            return type;
        }
    }

    private static Method getConcreteMethodAndReportProblems(Tree arg0,
                                                             ReferenceType type,
                                                             String methodName,
                                                             String firstParamSignature,
                                                             List<? extends TypeMirror> paramTypes,
                                                             List<? extends Type> argTypes) {
        Method method;
        try {
            if (paramTypes != null) {
                method = getConcreteMethod(type, methodName, firstParamSignature, paramTypes);
            } else {
                method = getConcreteMethod2(type, methodName, argTypes);
            }
        } catch (UnsuitableArgumentsException uaex) {
            StringBuilder methodArgs = new StringBuilder("(");
            if (paramTypes != null) {
                 for (TypeMirror paramType : paramTypes) {
                     if (methodArgs.length() > 1) {
                         methodArgs.append(", ");
                     }
                     methodArgs.append(paramType.toString());
                 }
            } else {
                for (Type argType : argTypes) {
                    if (methodArgs.length() > 1) {
                        methodArgs.append(", ");
                    }
                    methodArgs.append(argType.name());
                }
            }
            methodArgs.append(")");
            if ("<init>".equals(methodName)) {
                Assert.error(arg0, "noSuchConstructorWithArgs", type.name(), methodArgs.toString());
            }
            if (methodArgs.length() == 2) {
                Assert.error(arg0, "noSuchMethod", methodName+methodArgs, type.name());
            } else {
                Assert.error(arg0, "noSuchMethodWithArgs", methodName, type.name(), methodArgs.toString());
            }
            method = null;
        }
        if (method == null) {
            Assert.error(arg0, "noSuchMethod", methodName, type.name());
        }
        return method;
    }

    private static Method getConcreteMethod(ReferenceType type, String methodName, List<? extends TypeMirror> typeArguments) throws UnsuitableArgumentsException {
        return getConcreteMethod(type, methodName, null, typeArguments);
    }

    private static Method getConcreteMethod(ReferenceType type, String methodName,
                                            String firstParamSignature,
                                            List<? extends TypeMirror> typeArguments) throws UnsuitableArgumentsException {
        List<Method> methods = type.methodsByName(methodName);
        String signature = createSignature(firstParamSignature, typeArguments);
        boolean constructor = "<init>".equals(methodName);
        for (Method method : methods) {
            if (!method.isAbstract() &&
                (!constructor || type.equals(method.declaringType())) &&
                equalMethodSignatures(method.signature(), signature)) {
                return method;
            }
        }
        if (methods.size() > 0) {
            throw new UnsuitableArgumentsException();
        }
        return null;
    }

    private static Method getConcreteMethod2(ReferenceType type, String methodName, List<? extends Type> typeArguments) throws UnsuitableArgumentsException {
        List<Method> methods = type.methodsByName(methodName);
        List<Method> possibleMethods = new ArrayList<Method>();
        List<Method> methodsWithArgTypesNotLoaded = null;
        boolean constructor = "<init>".equals(methodName);
        for (Method method : methods) {
            if (!method.isAbstract() &&
                (!constructor || type.equals(method.declaringType()))) {
                try {
                    if (equalTypes(method.argumentTypes(), typeArguments)) {
                        return method;
                    }
                    if (acceptTypes(method.argumentTypes(), typeArguments)) {
                        possibleMethods.add(method);
                    }
                } catch (ClassNotLoadedException ex) {
                    if (method.argumentTypeNames().size() == typeArguments.size()) {
                        if (methodsWithArgTypesNotLoaded == null) {
                            methodsWithArgTypesNotLoaded = new ArrayList<Method>();
                        }
                        methodsWithArgTypesNotLoaded.add(method);
                    }
                }
            }
        }
        if (possibleMethods.isEmpty()) {
            if (methods.size() > 0) {
                if (methodsWithArgTypesNotLoaded != null) {
                    // Workaround for cases when we're not able to test method types.
                    return methodsWithArgTypesNotLoaded.get(0);
                }
                throw new UnsuitableArgumentsException();
            }
            return null;
        }
        return possibleMethods.get(0);
    }

    private static boolean equalTypes(List<? extends Type> methodTypes, List<? extends Type> argumentTypes) {
        if (methodTypes.size() != argumentTypes.size()) {
            return false;
        }
        int n = methodTypes.size();
        for (int i = 0; i < n; i++) {
            if (!methodTypes.get(i).equals(argumentTypes.get(i)) &&
                !unboxType(methodTypes.get(i)).equals(unboxType(argumentTypes.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean acceptTypes(List<? extends Type> methodTypes, List<? extends Type> argumentTypes) {
        if (methodTypes.size() != argumentTypes.size()) {
            return false;
        }
        int n = methodTypes.size();
        for (int i = 0; i < n; i++) {
            Type methodType = unboxType(methodTypes.get(i));
            Type argType = unboxType(argumentTypes.get(i));
            if (!methodType.equals(argType)) {
                if (!extendsType(argType, methodType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean extendsType(Type argType, Type methodType) {
        if (methodType instanceof ReferenceType && argType instanceof ReferenceType) {
            return extendsType((ReferenceType) argType, (ReferenceType) methodType);
        } else if (methodType instanceof PrimitiveType && argType instanceof PrimitiveType) {
            return extendsType((PrimitiveType) argType, (PrimitiveType) methodType);
        }
        return false;
    }

    /** @return true if t1 extends t2 */
    private static boolean extendsType(ReferenceType t1, ReferenceType t2) {
        if (t2 instanceof InterfaceType) {
            List<InterfaceType> superInterfaces;
            if (t1 instanceof ClassType) {
                superInterfaces = ((ClassType) t1).allInterfaces();
            } else if (t1 instanceof InterfaceType) {
                superInterfaces = ((InterfaceType) t1).superinterfaces();
            } else {
                return false;
            }
            return superInterfaces.contains((InterfaceType) t2);
        }
        if (t2 instanceof ClassType) {
            if (t1 instanceof ClassType) {
                ClassType superClass = ((ClassType) t1).superclass();
                if (superClass != null) {
                    if (superClass.equals((ClassType) t2)) {
                        return true;
                    } else {
                        return extendsType(superClass, t2);
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (t2 instanceof ArrayType) {
            if (t1 instanceof ArrayType) {
                try {
                    Type ct1 = ((ArrayType) t1).componentType();
                    Type ct2 = ((ArrayType) t2).componentType();
                    return extendsType(ct1, ct2);
                } catch (ClassNotLoadedException cnlex) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            throw new IllegalStateException("Unknown ReferenceType: "+t2);
        }
    }

    /** @return true if t2 is an extension of t1 */
    private static boolean extendsType(PrimitiveType t1, PrimitiveType t2) {
        // BooleanType, ByteType and CharType can be matched together only.
        if (t2 instanceof ShortType) {
            return t2 instanceof ByteType || t2 instanceof ShortType;
        }
        if (t2 instanceof IntegerType) {
            return t2 instanceof ByteType || t2 instanceof ShortType || t2 instanceof IntegerType;
        }
        if (t2 instanceof LongType) {
            return t2 instanceof ByteType || t2 instanceof ShortType ||
                   t2 instanceof IntegerType || t2 instanceof LongType;
        }
        if (t2 instanceof FloatType) {
            return !(t2 instanceof BooleanType || t2 instanceof CharType || t2 instanceof DoubleType);
        }
        if (t2 instanceof DoubleType) {
            return !(t2 instanceof BooleanType || t2 instanceof CharType);
        }
        return false;
    }

    private static Type unboxType(Type t) {
        if (t instanceof ClassType) {
            String name = ((ClassType) t).name();
            if (name.equals("java.lang.Boolean")) {
                t = t.virtualMachine().mirrorOf(true).type();
            } else if (name.equals("java.lang.Byte")) {
                t = t.virtualMachine().mirrorOf((byte) 10).type();
            } else if (name.equals("java.lang.Character")) {
                t = t.virtualMachine().mirrorOf('a').type();
            } else if (name.equals("java.lang.Integer")) {
                t = t.virtualMachine().mirrorOf(10).type();
            } else if (name.equals("java.lang.Long")) {
                t = t.virtualMachine().mirrorOf(10l).type();
            } else if (name.equals("java.lang.Short")) {
                t = t.virtualMachine().mirrorOf((short)10).type();
            } else if (name.equals("java.lang.Float")) {
                t = t.virtualMachine().mirrorOf(10f).type();
            } else if (name.equals("java.lang.Double")) {
                t = t.virtualMachine().mirrorOf(10.0).type();
            }
        }
        return t;
    }

    private static boolean equalMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) {
            s1 = s1.substring(0, i);
        }
        i = s2.lastIndexOf(")");
        if (i > 0) {
            s2 = s2.substring(0, i);
        }
        return s1.equals(s2);
    }

    private static String createSignature(String firstParamSignature, List<? extends TypeMirror> typeArguments) {
        StringBuilder signature = new StringBuilder("(");
        if (firstParamSignature != null) {
            signature.append(firstParamSignature);
        }
        for (TypeMirror param : typeArguments) {
            String paramType = getTypeName(param);//getSimpleName().toString();
            signature.append(getSignature(paramType));
        }
        signature.append(')');
        //String returnType = elm.getReturnType().toString();
        //signature.append(getSignature(returnType));
        return signature.toString();
    }

    private static String getTypeName(TypeMirror type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return getTypeName(((javax.lang.model.type.ArrayType) type).getComponentType())+"[]";
        }
        if (type.getKind() == TypeKind.TYPEVAR) {
            TypeVariable tv = (TypeVariable) type;
            return getTypeName(tv.getUpperBound());
        }
        if (type.getKind() == TypeKind.DECLARED) {
            return ElementUtilities.getBinaryName((TypeElement) ((DeclaredType) type).asElement());
        }
        return type.toString();
    }

    private static String getSignature(String javaType) {
        if (javaType.equals("boolean")) {
            return "Z";
        } else if (javaType.equals("byte")) {
            return "B";
        } else if (javaType.equals("char")) {
            return "C";
        } else if (javaType.equals("short")) {
            return "S";
        } else if (javaType.equals("int")) {
            return "I";
        } else if (javaType.equals("long")) {
            return "J";
        } else if (javaType.equals("float")) {
            return "F";
        } else if (javaType.equals("double")) {
            return "D";
        } else if (javaType.endsWith("[]")) {
            return "["+getSignature(javaType.substring(0, javaType.length() - 2));
        } else {
            return "L"+javaType.replace('.', '/')+";";
        }
    }

    private static ReferenceType getClassType(Tree tree, TypeMirror type, EvaluationContext evaluationContext) {
        String className = ElementUtilities.getBinaryName((TypeElement) ((DeclaredType) type).asElement());
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        ReferenceType clazz = getOrLoadClass(vm, className, evaluationContext);
        if (clazz == null) {
            Assert.error(tree, "unknownType", className);
        }
        return clazz;
    }

    public static boolean instanceOf(Type left, Type right) {
        if (left == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        
        if (right instanceof IntersectionType) {
            Type[] types = ((IntersectionType) right).getTypes();
            for (Type type : types) {
                if (!instanceOf(left, type)) {
                    return false;
                }
            }
            return true;
        }

        if (right instanceof ArrayType) {
            if (!(left instanceof ArrayType)) {
                return false;
            } else {
                ArrayType leftArray = (ArrayType) left;
                ArrayType rightArray = (ArrayType) right;
                Type leftType;
                Type rightType;
                try {
                    leftType = leftArray.componentType();
                    rightType = rightArray.componentType();
                } catch (ClassNotLoadedException e) {
                    // TODO: load missing classes
                    return false;
                }
                return instanceOf(leftType, rightType);
            }
        }

        if (left instanceof ClassType) {
            ClassType classLeft = (ClassType) left;
            if (right instanceof InterfaceType) {
                List<InterfaceType> ifaces;
                try {
                    ifaces = classLeft.allInterfaces();
                } catch (ClassNotPreparedException cnpex) {
                    return false;
                }
                for (Iterator<InterfaceType> i = ifaces.iterator(); i.hasNext();) {
                    InterfaceType type = i.next();
                    if (type.equals(right)) {
                        return true;
                    }
                }
                return false;
            } else {  // right instanceof ClassType
                for (;;) {
                    classLeft = classLeft.superclass();
                    if (classLeft == null) {
                        return false;
                    }
                    if (classLeft.equals(right)) {
                        return true;
                    }
                }
            }
        }

        if (left instanceof InterfaceType) {
            InterfaceType intLeft = (InterfaceType) left;
            if (right instanceof InterfaceType) {
                List<InterfaceType> ifaces;
                try {
                    ifaces = intLeft.superinterfaces();
                } catch (ClassNotPreparedException cnpex) {
                    return false;
                }
                for (Iterator<InterfaceType> i = ifaces.iterator(); i.hasNext();) {
                    InterfaceType type = i.next();
                    if (type.equals(right)) {
                        return true;
                    }
                }
                return false;
            }
        }

        return false;
    }


    @Override
    public Mirror visitAssert(AssertTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitAssignment(AssignmentTree arg0, EvaluationContext evaluationContext) {
        Mirror var = arg0.getVariable().accept(this, evaluationContext);
        Mirror exp = arg0.getExpression().accept(this, evaluationContext);
        Value value = (Value) exp;
        return setToMirror(arg0.getVariable(), value, evaluationContext);
    }

    @Override
    public Mirror visitCompoundAssignment(CompoundAssignmentTree arg0, EvaluationContext evaluationContext) {
        Mirror var = arg0.getVariable().accept(this, evaluationContext);
        Mirror exp = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        Tree.Kind kind = arg0.getKind();
        if (var instanceof ObjectReference) {
            var = unboxIfCan(arg0, (ObjectReference) var, evaluationContext);
        }
        if (var == null) {
            TreePath currentPath = getCurrentPath();
            Element elm = null;
            if (currentPath != null) {
                TreePath identifierPath = TreePath.getPath(currentPath, arg0.getVariable());
                if (identifierPath == null) {
                    identifierPath = getCurrentPath();
                }
                elm = evaluationContext.getTrees().getElement(identifierPath);
                if (elm instanceof TypeElement && ((TypeElement) elm).asType() instanceof ErrorType) {
                    elm = null; // Elements not resolved correctly
                }
            }
            if (elm == null) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
                //throw new IllegalStateException("Unknown assignment var type: "+arg0.getVariable());
            }
            TypeMirror type = elm.asType();
            switch(type.getKind()) {
                case BOOLEAN:
                    var = vm.mirrorOf(false);
                    break;
                case BYTE:
                    var = vm.mirrorOf((byte) 0);
                    break;
                case CHAR:
                    var = vm.mirrorOf((char) 0);
                    break;
                case DOUBLE:
                    var = vm.mirrorOf((double) 0);
                    break;
                case FLOAT:
                    var = vm.mirrorOf((float) 0);
                    break;
                case INT:
                    var = vm.mirrorOf((int) 0);
                    break;
                case LONG:
                    var = vm.mirrorOf((long) 0);
                    break;
                case SHORT:
                    var = vm.mirrorOf((short) 0);
                    break;
                default:
                    if (type.toString().equals("java.lang.String")) {
                        try {
                            var = vm.mirrorOf("null");
                        } catch (UnsupportedOperationException e) {
                            Assert.error(arg0, "unsupportedStringCreation");
                        }
                    } else {
                        Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
                        //throw new IllegalStateException("Unknown assignment var type: "+type);
                    }
            }
        }
        if (var instanceof BooleanValue) {
            boolean v = ((BooleanValue) var).value();
            if (!(exp instanceof BooleanValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            boolean e = ((BooleanValue) exp).value();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof DoubleValue) {
            double v = ((DoubleValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            double e = ((PrimitiveValue) exp).doubleValue();
            switch (kind) {
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof FloatValue) {
            float v = ((FloatValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            float e = ((PrimitiveValue) exp).floatValue();
            switch (kind) {
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof LongValue) {
            long v = ((LongValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            long e = ((PrimitiveValue) exp).longValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof IntegerValue) {
            int v = ((IntegerValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            int e = ((PrimitiveValue) exp).intValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof ShortValue) {
            short v = ((ShortValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            int e = ((PrimitiveValue) exp).intValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof CharValue) {
            char v = ((CharValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            int e = ((PrimitiveValue) exp).intValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof ByteValue) {
            byte v = ((ByteValue) var).value();
            if (!(exp instanceof PrimitiveValue)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            int e = ((PrimitiveValue) exp).intValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            Value value = mirrorOf(vm, v);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        if (var instanceof StringReference) {
            String v = ((StringReference) var).value();
            if (exp != null && !(exp instanceof StringReference)) {
                Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            String e = (exp != null) ? ((StringReference) exp).value() : null;
            switch (kind) {
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
            }
            StringReference value = createStringMirrorWithDisabledCollection(v, vm, evaluationContext);
            return setToMirror(arg0.getVariable(), value, evaluationContext);
        }
        Assert.error(arg0, "evaluateError", arg0.getVariable(), operatorToString(kind), arg0.getExpression());
        throw new IllegalStateException("Unknown assignment var type: "+var);
    }

    @Override
    public Mirror visitBinary(BinaryTree arg0, EvaluationContext evaluationContext) {
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        Tree.Kind kind = arg0.getKind();
        Mirror left = arg0.getLeftOperand().accept(this, evaluationContext);
        Mirror right = null; // postpone evaluation of right operand due to boolean binary operators
        if ((left instanceof ObjectReference) && (kind == Tree.Kind.EQUAL_TO ||
                kind == Tree.Kind.NOT_EQUAL_TO)) {
            right = arg0.getRightOperand().accept(this, evaluationContext);
            if (right instanceof ObjectReference) {
                if (kind == Tree.Kind.EQUAL_TO) {
                    return mirrorOf(vm, left.equals(right));
                } else {
                    return mirrorOf(vm, !left.equals(right));
                }
            }
        }
        if (left instanceof ObjectReference) {
            left = unboxIfCan(arg0, (ObjectReference) left, evaluationContext);
        }
        if ((left instanceof BooleanValue)) {
            // check whether result of && or || is determined by the left operand
            boolean op1 = ((BooleanValue) left).booleanValue();
            if (kind == Tree.Kind.CONDITIONAL_AND && !op1) {
                return mirrorOf(vm, false);
            } else if (kind == Tree.Kind.CONDITIONAL_OR && op1) {
                return mirrorOf(vm, true);
            }
        }
        if (right == null) {
            right = arg0.getRightOperand().accept(this, evaluationContext);
        }
        if (right instanceof ObjectReference) {
            right = unboxIfCan(arg0, (ObjectReference) right, evaluationContext);
        }
        if ((left instanceof BooleanValue) && (right instanceof BooleanValue)) {
            boolean op1 = ((BooleanValue) left).booleanValue();
            boolean op2 = ((BooleanValue) right).booleanValue();
            boolean res;
            switch (kind) {
                case AND: res = op1 & op2; break;
                case CONDITIONAL_AND: res = op1 && op2; break;
                case CONDITIONAL_OR: res = op1 || op2; break;
                case EQUAL_TO: res = op1 == op2; break;
                case NOT_EQUAL_TO: res = op1 != op2; break;
                case OR: res = op1 | op2; break;
                case XOR: res = op1 ^ op2; break;
                default:
                    reportCannotApplyOperator(arg0);
                    return null;
            }
            return mirrorOf(vm, res);
        }
        boolean isLeftNumeric = left instanceof PrimitiveValue && !(left instanceof BooleanValue);
        boolean isRightNumeric = right instanceof PrimitiveValue && !(right instanceof BooleanValue);
        if (isLeftNumeric && isRightNumeric) {
            if ((left instanceof DoubleValue) || (right instanceof DoubleValue)) {
                double l = ((PrimitiveValue) left).doubleValue();
                double r = ((PrimitiveValue) right).doubleValue();
                double v = 0.;
                boolean b = false;
                boolean isBoolean = true;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; isBoolean = false; break;
                    case MINUS:
                        v = l - r; isBoolean = false; break;
                    case MULTIPLY:
                        v = l * r; isBoolean = false; break;
                    case PLUS:
                        v = l + r; isBoolean = false; break;
                    case REMAINDER:
                        v = l % r; isBoolean = false; break;
                    case EQUAL_TO:
                        b = l == r; break;
                    case GREATER_THAN:
                        b = l > r; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; break;
                    case LESS_THAN:
                        b = l < r; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; break;
                    case NOT_EQUAL_TO:
                        b = l != r; break;
                    default:
                        reportCannotApplyOperator(arg0);
                        return null;
                }
                if (isBoolean) {
                    return mirrorOf(vm, b);
                } else {
                    return mirrorOf(vm, v);
                }
            }
            if ((left instanceof FloatValue) || (right instanceof FloatValue)) {
                float l = ((PrimitiveValue) left).floatValue();
                float r = ((PrimitiveValue) right).floatValue();
                float v = 0.f;
                boolean b = false;
                boolean isBoolean = true;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; isBoolean = false; break;
                    case MINUS:
                        v = l - r; isBoolean = false; break;
                    case MULTIPLY:
                        v = l * r; isBoolean = false; break;
                    case PLUS:
                        v = l + r; isBoolean = false; break;
                    case REMAINDER:
                        v = l % r; isBoolean = false; break;
                    case EQUAL_TO:
                        b = l == r; break;
                    case GREATER_THAN:
                        b = l > r; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; break;
                    case LESS_THAN:
                        b = l < r; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; break;
                    case NOT_EQUAL_TO:
                        b = l != r; break;
                    default:
                        reportCannotApplyOperator(arg0);
                        return null;
                }
                if (isBoolean) {
                    return mirrorOf(vm, b);
                } else {
                    return mirrorOf(vm, v);
                }
            }
            if ((left instanceof LongValue) || (right instanceof LongValue)) {
                long l = ((PrimitiveValue) left).longValue();
                long r = ((PrimitiveValue) right).longValue();
                long v = 0l;
                boolean b = false;
                boolean isBoolean = false;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; break;
                    case MINUS:
                        v = l - r; break;
                    case MULTIPLY:
                        v = l * r; break;
                    case PLUS:
                        v = l + r; break;
                    case REMAINDER:
                        v = l % r; break;
                    case LEFT_SHIFT:
                        v = l << r; break;
                    case RIGHT_SHIFT:
                        v = l >> r; break;
                    case UNSIGNED_RIGHT_SHIFT:
                        v = l >>> r; break;
                    case AND:
                        v = l & r; break;
                    case OR:
                        v = l | r; break;
                    case XOR:
                        v = l ^ r; break;
                    case EQUAL_TO:
                        b = l == r; isBoolean = true; break;
                    case GREATER_THAN:
                        b = l > r; isBoolean = true; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; isBoolean = true; break;
                    case LESS_THAN:
                        b = l < r; isBoolean = true; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; isBoolean = true; break;
                    case NOT_EQUAL_TO:
                        b = l != r; isBoolean = true; break;
                    default:
                        reportCannotApplyOperator(arg0);
                        return null;
                }
                if (isBoolean) {
                    return mirrorOf(vm, b);
                } else {
                    return mirrorOf(vm, v);
                }
            }
            //if ((left instanceof IntegerValue) || (right instanceof IntegerValue)) {
            // int, short, char and byte - operations have int result
                int l = ((PrimitiveValue) left).intValue();
                int r = ((PrimitiveValue) right).intValue();
                int v = 0;
                boolean b = false;
                boolean isBoolean = false;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; break;
                    case MINUS:
                        v = l - r; break;
                    case MULTIPLY:
                        v = l * r; break;
                    case PLUS:
                        v = l + r; break;
                    case REMAINDER:
                        v = l % r; break;
                    case LEFT_SHIFT:
                        v = l << r; break;
                    case RIGHT_SHIFT:
                        v = l >> r; break;
                    case UNSIGNED_RIGHT_SHIFT:
                        v = l >>> r; break;
                    case AND:
                        v = l & r; break;
                    case OR:
                        v = l | r; break;
                    case XOR:
                        v = l ^ r; break;
                    case EQUAL_TO:
                        b = l == r; isBoolean = true; break;
                    case GREATER_THAN:
                        b = l > r; isBoolean = true; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; isBoolean = true; break;
                    case LESS_THAN:
                        b = l < r; isBoolean = true; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; isBoolean = true; break;
                    case NOT_EQUAL_TO:
                        b = l != r; isBoolean = true; break;
                    default:
                        reportCannotApplyOperator(arg0);
                        return null;
                }
                if (isBoolean) {
                    return mirrorOf(vm, b);
                } else {
                    return mirrorOf(vm, v);
                }
            //}
        }
        if (((left == null || left instanceof StringReference) && (right == null || right instanceof StringReference))
            && kind == Tree.Kind.PLUS) {
            String s1 = (left == null) ? null : ((StringReference) left).value();
            String s2 = (right == null) ? null : ((StringReference) right).value();
            switch (kind) {
                case PLUS:
                    return createStringMirrorWithDisabledCollection(s1 + s2, vm, evaluationContext);
                default:
                    reportCannotApplyOperator(arg0);
                    return null;
            }
        }
        if ((left instanceof StringReference || right instanceof StringReference) && kind == Tree.Kind.PLUS) {
            String s1 = (left instanceof StringReference) ? ((StringReference) left).value() : toString(arg0, left, evaluationContext);
            String s2 = (right instanceof StringReference) ? ((StringReference) right).value() : toString(arg0, right, evaluationContext);
            return createStringMirrorWithDisabledCollection(s1 + s2, vm, evaluationContext);
        }
        if (left == null && right instanceof PrimitiveValue ||
            right == null && left instanceof PrimitiveValue) {
            Throwable ex = new NullPointerException("");
            ex.setStackTrace(new StackTraceElement[] {});
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(ex.getLocalizedMessage(), ieex);
        }
        switch (kind) {
            case EQUAL_TO:
                return mirrorOf(vm, left == right || (left != null && left.equals(right)));
            case NOT_EQUAL_TO:
                return mirrorOf(vm, left == null && right != null || (left != null && !left.equals(right)));
            default:
                reportCannotApplyOperator(arg0);
                return null;
        }
    }

    @Override
    public Mirror visitBlock(BlockTree arg0, EvaluationContext evaluationContext) {
        Mirror lastResult = null;
        try {
            evaluationContext.pushBlock();
            for (StatementTree statementTree : arg0.getStatements()) {
                Mirror res = statementTree.accept(this, evaluationContext);
                if (res != null) {
                    lastResult = res;
                }
                if (res instanceof CommandMirror) {
                    break;
                }
            }
        } finally {
            evaluationContext.popBlock();
        }
        return lastResult;
    }

    @Override
    public Mirror visitBreak(BreakTree arg0, EvaluationContext evaluationContext) {
        Name label = arg0.getLabel();
        if (label != null) {
            Assert.error(arg0, "unsupported");
            return null;
        }
        return new Break();
    }

    @Override
    public Mirror visitCase(CaseTree arg0, EvaluationContext evaluationContext) {
        // case is handled within visitSwitch method
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitCatch(CatchTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitClass(ClassTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitConditionalExpression(ConditionalExpressionTree arg0, EvaluationContext evaluationContext) {
        boolean isTrue = evaluateCondition(arg0, evaluationContext, arg0.getCondition());
        if (isTrue) {
            return arg0.getTrueExpression().accept(this, evaluationContext);
        } else {
            return arg0.getFalseExpression().accept(this, evaluationContext);
        }
    }

    @Override
    public Mirror visitContinue(ContinueTree arg0, EvaluationContext evaluationContext) {
        Name label = arg0.getLabel();
        if (label != null) {
            Assert.error(arg0, "unsupported");
            return null;
        }
        return new Continue();
    }

    @Override
    public Mirror visitDoWhileLoop(DoWhileLoopTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree condition = arg0.getCondition();
        Tree statement = arg0.getStatement();
        Mirror result = null;
        do {
            try {
                evaluationContext.pushBlock();
                Mirror res = statement.accept(this, evaluationContext);
                if (res instanceof Break) {
                    break;
                } else if (res instanceof Continue) {
                    continue;
                }
                if (res != null) {
                    result = res;
                }
            } finally {
                evaluationContext.popBlock();
            }
        } while (evaluateCondition(arg0, evaluationContext, condition));
        return result;
    }

    @Override
    public Mirror visitErroneous(ErroneousTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "errorneous");
        return null;
    }

    @Override
    public Mirror visitExpressionStatement(ExpressionStatementTree arg0, EvaluationContext evaluationContext) {
        return arg0.getExpression().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitEnhancedForLoop(EnhancedForLoopTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree exprTree = arg0.getExpression();
        Mirror exprValue = exprTree.accept(this, evaluationContext);
        Method nextMethod = null, hasNextMethod = null;
        ObjectReference iterator = null;
        if (!(exprValue instanceof ObjectReference)) {
            Assert.error(arg0, "forEachNotApplicable");
        }
        boolean isArray = true;
        int arrayLength = 0;
        if (!(exprValue instanceof ArrayReference)) {
            isArray = false;
            if (!evaluationContext.canInvokeMethods()) {
                Assert.error(arg0, "canNotInvokeMethods");
            }
            ObjectReference objRef = (ObjectReference)exprValue;
            ReferenceType objType = objRef.referenceType();
            ReferenceType collType = evaluationContext.getVMCache().getClass("java.util.Collection");
            if (collType == null) {
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) {
                    return null;
                }
                collType = getOrLoadClass(vm, "java.util.Collection", evaluationContext);
            }
            if (!instanceOf(objRef.type(), collType)) {
                Assert.error(arg0, "forEachNotApplicable");
            }
            Method iteratorMethod = null;
            try {
                iteratorMethod = getConcreteMethod(objType, "iterator", Collections.<TypeMirror>emptyList());
            } catch (UnsuitableArgumentsException ex) {
            }
            iterator = (ObjectReference)invokeMethod(arg0, iteratorMethod, Boolean.FALSE, (ClassType)objRef.type(),
                    objRef, Collections.<Value>emptyList(), evaluationContext, false);
            try {
                ReferenceType iteratorType = iterator.referenceType();
                nextMethod = getConcreteMethod(iteratorType, "next", Collections.<TypeMirror>emptyList());
                hasNextMethod = getConcreteMethod(iteratorType, "hasNext", Collections.<TypeMirror>emptyList());
            } catch (UnsuitableArgumentsException ex) {
            }
        } else {
            arrayLength = ((ArrayReference) exprValue).length();
        }

        Mirror result = null;
        try {
            evaluationContext.pushBlock();
            VariableTree varTree = arg0.getVariable();
            varTree.accept(this, evaluationContext); // declare variable
            ScriptVariable scriptVar = evaluationContext.getScriptVariableByName(varTree.getName().toString());
            StatementTree statementTree = arg0.getStatement();
            int index = 0;
            do {
                Value value;
                if (isArray) {
                    if (index >= arrayLength) {
                        break;
                    }
                    value = ((ArrayReference)exprValue).getValue(index);
                    index++;
                } else {
                    value = invokeMethod(arg0, hasNextMethod, Boolean.FALSE, (ClassType)iterator.type(),
                        iterator, Collections.<Value>emptyList(), evaluationContext, false);
                    if (!((BooleanValue)value).value()) {
                        break;
                    }
                    value = invokeMethod(arg0, nextMethod, Boolean.FALSE, (ClassType)iterator.type(),
                        iterator, Collections.<Value>emptyList(), evaluationContext, false);
                }
                scriptVar.setValue(value); // [TODO] check if value is assignable to variable
                try {
                    evaluationContext.pushBlock();
                    Mirror returnValue = statementTree.accept(this, evaluationContext);
                    if (returnValue instanceof Break) {
                        break;
                    } else if (returnValue instanceof Continue) {
                        continue;
                    }
                    if (returnValue != null) {
                        result = returnValue;
                    }
                } finally {
                    evaluationContext.popBlock();
                }
            } while(true);
        } finally {
            evaluationContext.popBlock();
        }
        return result;
    }

    @Override
    public Mirror visitForLoop(ForLoopTree arg0, EvaluationContext evaluationContext) {
        try {
            evaluationContext.pushBlock();
            for (StatementTree st : arg0.getInitializer()) {
                st.accept(this, evaluationContext);
            }
            Mirror result = null;
            ExpressionTree condition = arg0.getCondition();
            List<? extends ExpressionStatementTree> updateList = arg0.getUpdate();
            StatementTree statement = arg0.getStatement();
            while (condition == null || evaluateCondition(arg0, evaluationContext, condition)) {
                Mirror value = null;
                try {
                    evaluationContext.pushBlock();
                    value = statement.accept(this, evaluationContext);
                    if (value instanceof Break) {
                        break;
                    } else if (value instanceof Continue) {
                        continue;
                    }
                    if (value != null) {
                        result = value;
                    }
                } finally {
                    evaluationContext.popBlock();
                    if ((value instanceof Continue) || !(value instanceof CommandMirror)) {
                        for (Tree tree : updateList) {
                            tree.accept(this, evaluationContext);
                        } // for
                    } // if
                } // finally
            } // while
            return result;
        } finally {
            evaluationContext.popBlock();
        }
    }
    
    private boolean evaluateCondition(Tree arg0, EvaluationContext evaluationContext, ExpressionTree condition) {
        Mirror conditionValue = condition.accept(this, evaluationContext);
        if (conditionValue instanceof ObjectReference) {
            conditionValue = unboxIfCan(arg0, (ObjectReference) conditionValue, evaluationContext);
        }
        if (!(conditionValue instanceof BooleanValue)) {
            String type = "N/A";    // NOI18N
            if (conditionValue instanceof Value) {
                type = ((Value) conditionValue).type().name();
            }
            Assert.error(arg0, "notABoolean", condition.toString(), conditionValue, type);
        }
        return ((BooleanValue) conditionValue).value();
    }

    private Mirror getIdentifierByName(IdentifierTree arg0, EvaluationContext evaluationContext) {
        String name = arg0.getName().toString();
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        List<ReferenceType> classes = vm.classesByName(name);
        if (classes.size() > 0) {
            try {
                ReferenceType preferredType = JPDAUtils.getPreferredReferenceType(classes, null);
                if (preferredType != null) {
                    return preferredType;
                }
            } catch (VMDisconnectedExceptionWrapper ex) {
                throw ex.getCause();
            }
            return classes.get(0);
        }
        // Class not found. If the source is not fully resolved, we may
        // get a field or local variable here:
        if (name.equals("this")) {
            return evaluationContext.getContextObject();
        }
        if (name.equals("super")) {
            ReferenceType thisType = evaluationContext.getFrame().location().declaringType();
            if (thisType instanceof ClassType) {
                ClassType superClass = ((ClassType) thisType).superclass();
                ObjectReference thisObject = evaluationContext.getContextObject();
                if (thisObject == null) {
                    return superClass;
                } else {
                    subExpressionTypes.put(arg0, superClass);
                    return thisObject;
                }
            }
        }
        ScriptVariable var = evaluationContext.getScriptVariableByName(name);
        if (var != null) {
            evaluationContext.putScriptVariable(arg0, var);
            return var.getValue();
        }
        boolean localVarsAbsent = false;
        try {
            LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(name);
            if (lv != null) {
                evaluationContext.putLocalVariable(arg0, lv);
                return evaluationContext.getFrame().getValue(lv);
            }
        } catch (AbsentInformationException aiex) {
            localVarsAbsent = true;
        }
        Field field;
        if (evaluationContext.getContextObject() != null) {
            field = evaluationContext.getContextObject().referenceType().fieldByName(name);
        } else {
            field = evaluationContext.getFrame().location().declaringType().fieldByName(name);
        }
        if (field != null) {
            if (field.isStatic()) {
                evaluationContext.putField(arg0, field, null);
                Value v = field.declaringType().getValue(field);
                if (v instanceof ObjectReference) {
                    evaluationContext.disableCollectionOf((ObjectReference) v);
                }
                return v;
            }
            ObjectReference thisObject = evaluationContext.getContextObject();
            if (thisObject != null) {
                evaluationContext.putField(arg0, field, thisObject);
                Value v = thisObject.getValue(field);
                if (v instanceof ObjectReference) {
                    evaluationContext.disableCollectionOf((ObjectReference) v);
                }
                return v;
            }
        }
        ObjectReference thiz = evaluationContext.getContextObject();
        if (thiz != null) {
            Value val = getOuterValue(thiz, name, evaluationContext, arg0);
            if (val != NO_VALUE) {
                return val;
            }
        }
        // Try to load the class as a last try...
        ReferenceType rt = getOrLoadClass(vm, name, evaluationContext);
        if (rt != null) {
            return rt;
        }
        if (localVarsAbsent) {
            Assert.error(arg0, "unknownVarNoDebugInfo", name);
        } else {
            Assert.error(arg0, "unknownVariable", name);
        }
        return null;
    }
    
    private Value getOuterValue(ObjectReference thiz, String name, EvaluationContext evaluationContext, IdentifierTree arg0) {
        Field outer = thiz.referenceType().fieldByName("val$"+name);
        if (outer != null) {
            Value val = thiz.getValue(outer);
            evaluationContext.putField(arg0, outer, thiz);
            return val;
        }
        // If the field is not there, we'll look for this$0 etc.
        List<Field> fields = thiz.referenceType().fields();
        for (Field f : fields) {
            if (f.name().startsWith("this$")) {
                Value val = thiz.getValue(f);
                if (!(val instanceof ObjectReference)) {
                    continue;
                }
                val = getOuterValue((ObjectReference) val, name, evaluationContext, arg0);
                if (val != NO_VALUE) {
                    return val;
                }
            }
        }
        return NO_VALUE;
    }

    @Override
    public Mirror visitIdentifier(IdentifierTree arg0, EvaluationContext evaluationContext) {
        String identifier = arg0.getName().toString();
        // class special variable
        if (expression.classReplaced().equals(identifier)) {
            ReferenceType refType = evaluationContext.getFrame().location().declaringType();
            JPDAClassType classType = evaluationContext.getDebugger().getClassType(refType);
            return ((JDIVariable) classType.classObject()).getJDIValue();
            // UnsupportedOperationException is catched at TreeEvaluator.evaluate().
        }

        // return special variable
        if (expression.returnReplaced().equals(identifier)) {
            ThreadReference tr = evaluationContext.getFrame().thread();
            JPDAThreadImpl thread = evaluationContext.getDebugger().getThread(tr);
            JDIVariable returnVar = (JDIVariable) thread.getReturnVariable();
            if (returnVar != null) {
                return returnVar.getJDIValue();
            } else {
                return null;
            }
        }

        // Label Variables
        ObjectVariable labeledVar = evaluationContext.getDebugger().getLabeledVariable(identifier);
        if (labeledVar != null) {
            return ((JDIVariable)labeledVar).getJDIValue();
        }

        TreePath currentPath = getCurrentPath();
        Element elm = null;
        if (currentPath != null) {
            TreePath identifierPath = TreePath.getPath(currentPath, arg0);
            if (identifierPath == null) {
                identifierPath = getCurrentPath();
            }
            elm = evaluationContext.getTrees().getElement(identifierPath);
            if (elm instanceof TypeElement && ((TypeElement) elm).asType().getKind() == TypeKind.ERROR) {
                currentPath = null; // Elements not resolved correctly
            }
        }
        if (currentPath == null || elm == null) {
            return getIdentifierByName(arg0, evaluationContext);
        }
        switch(elm.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                TypeElement te = (TypeElement) elm;
                String className = ElementUtilities.getBinaryName(te);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) {
            return null;
        }
                ReferenceType rt = getOrLoadClass(vm, className, evaluationContext);
                if (rt != null) {
                    return rt;
                }
                Assert.error(arg0, "unknownType", className);
            case TYPE_PARAMETER:
                TypeParameterElement tpel = (TypeParameterElement) elm;
                List<? extends TypeMirror> bounds = tpel.getBounds();
                for (TypeMirror tm : bounds) {
                    //if (tm.getKind() == TypeKind.)
                    String typeName = getTypeName(tm);
                    vm = evaluationContext.getDebugger().getVirtualMachine();
                    if (vm == null) {
                        return null;
                    }
                    rt = getOrLoadClass(vm, typeName, evaluationContext);
                    if (rt != null) {
                        return rt;
                    }
                }
                Assert.error(arg0, "unknownType", tpel.getSimpleName().toString());
                //String className = ElementUtilities.getBinaryName(tpel.);
            case ENUM_CONSTANT:
                return getEnumConstant(arg0, (VariableElement) elm, evaluationContext);
            case FIELD:
                VariableElement ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                if (fieldName.equals("this")) {
                    return evaluationContext.getContextObject();
                }
                if (fieldName.equals("super")) {
                    ReferenceType thisType = evaluationContext.getFrame().location().declaringType();
                    if (thisType instanceof ClassType) {
                        ClassType superClass = ((ClassType) thisType).superclass();
                        ObjectReference thisObject = evaluationContext.getContextObject();
                        if (thisObject == null) {
                            return superClass;
                        } else {
                            subExpressionTypes.put(arg0, superClass);
                            return thisObject;
                        }
                    }
                }
                Element enclosing = ve.getEnclosingElement();
                String enclosingClass = null;
                if (enclosing.getKind() == ElementKind.CLASS) {
                    TypeElement enclosingClassElement = (TypeElement) enclosing;
                    enclosingClass = ElementUtilities.getBinaryName(enclosingClassElement);
                }
                ReferenceType declaringType = evaluationContext.getFrame().location().declaringType();
                if (enclosingClass != null) {
                    ReferenceType dt = findEnclosingType(declaringType, enclosingClass, evaluationContext.getVMCache());
                    if (dt != null) {
                        declaringType = dt;
                    }
                }
                Field field = declaringType.fieldByName(fieldName);
                if (field == null) {
                    // Check the enclosing class...
                    if (enclosingClass != null && !enclosingClass.equals(declaringType.name())) {
                        vm = evaluationContext.getDebugger().getVirtualMachine();
                        if (vm == null) {
                            return null;
                        }
                        declaringType = getOrLoadClass(vm, enclosingClass, evaluationContext);
                        if (declaringType != null) {
                            field = declaringType.fieldByName(fieldName);
                        } else {
                            // Unknown class, try to get a constant value, if it's a constant...
                            Object constantValue = ve.getConstantValue();
                            if (constantValue != null) {
                                return mirrorOf(vm, constantValue);
                            } else {
                                Assert.error(arg0, "unknownType", enclosingClass);
                            }
                        }
                    }
                }
                if (field == null) {
                    Assert.error(arg0, "unknownVariable", fieldName);
                }
                if (field.isStatic()) {
                    evaluationContext.putField(arg0, field, null);
                    Value v = declaringType.getValue(field);
                    if (v instanceof ObjectReference) {
                        evaluationContext.disableCollectionOf((ObjectReference) v);
                    }
                    return v;
                }
                ObjectReference thisObject = evaluationContext.getContextObject();
                if (thisObject != null) {
                    if (field.isPrivate()) {
                        ObjectReference to = findEnclosingObject(arg0, thisObject, declaringType, field.name(), null);
                        thisObject = to;
                    } else {
                        if (!instanceOf(thisObject.referenceType(), declaringType)) {
                            ObjectReference to = findEnclosingObject(arg0, thisObject, declaringType, field.name(), null);
                            thisObject = to;
                        }
                    }
                }
                if (thisObject != null) {
                    evaluationContext.putField(arg0, field, thisObject);
                    try {
                        Value v = thisObject.getValue(field);
                        if (v instanceof ObjectReference) {
                            evaluationContext.disableCollectionOf((ObjectReference) v);
                        }
                        return v;
                    } catch (IllegalArgumentException iaex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "field = {0}, thisObject = {1}", new Object[]{field, thisObject}); // NOI18N
                        throw iaex;
                    }
                } else {
                    Assert.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
                    throw new IllegalStateException("No current instance available.");
                }
            case LOCAL_VARIABLE:
            case EXCEPTION_PARAMETER:
            case RESOURCE_VARIABLE:
            case BINDING_VARIABLE:
                ve = (VariableElement) elm;
                String varName = ve.getSimpleName().toString();
                ScriptVariable var = evaluationContext.getScriptVariableByName(varName);
                if (var != null) {
                    evaluationContext.putScriptVariable(arg0, var);
                    return var.getValue();
                }
                try {
                    LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(varName);
                    if (lv == null) {
                        ObjectReference thiz;
                        try {
                            thiz = evaluationContext.getFrame().thisObject();
                        } catch (com.sun.jdi.InternalException iex) {
                            if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=173327
                                thiz = null;
                            } else {
                                throw iex; // re-throw the original
                            }
                        }
                        if (thiz != null) {
                            Value val = getOuterValue(thiz, varName, evaluationContext, arg0);
                            if (val != NO_VALUE) {
                                return val;
                            }
                        }
                        Assert.error(arg0, "unknownVariable", varName);
                    }
                    evaluationContext.putLocalVariable(arg0, lv);
                    return evaluationContext.getFrame().getValue(lv);
                } catch (AbsentInformationException aiex) {
                    return (Value) Assert.error(arg0, "unknownVarNoDebugInfo", varName);
                }
            case PARAMETER:
                ve = (VariableElement) elm;
                String paramName = ve.getSimpleName().toString();
                StackFrame frame = evaluationContext.getFrame();
                try {
                    LocalVariable lv = frame.visibleVariableByName(paramName);
                    if (lv == null) {
                        ObjectReference thiz;
                        try {
                            thiz = frame.thisObject();
                        } catch (com.sun.jdi.InternalException iex) {
                            if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=173327
                                thiz = null;
                            } else {
                                throw iex; // re-throw the original
                            }
                        }
                        if (thiz != null) {
                            Value val = getOuterValue(thiz, paramName, evaluationContext, arg0);
                            if (val != NO_VALUE) {
                                return val;
                            }
                        }
                        Assert.error(arg0, "unknownVariable", paramName);
                    }
                    evaluationContext.putLocalVariable(arg0, lv);
                    return frame.getValue(lv);
                } catch (AbsentInformationException aiex) {
                    org.netbeans.api.debugger.jpda.LocalVariable[] lvs;
                    lvs = new CallStackFrameImpl(((JPDADebuggerImpl) evaluationContext.getDebugger()).getThread(frame.thread()),
                                                 frame, 0, evaluationContext.getDebugger()).getMethodArguments();
                    if (lvs != null) {
                        for (org.netbeans.api.debugger.jpda.LocalVariable lv : lvs) {
                            if (paramName.equals(lv.getName())) {
                                return ((JDIVariable) lv).getJDIValue();
                            }
                        }
                    }
                    return (Value) Assert.error(arg0, "unknownVarNoDebugInfo", paramName);
                }
            case PACKAGE:
                return (Value) Assert.error(arg0, "notExpression");
            default:
                throw new UnsupportedOperationException("Not supported element kind:"+elm.getKind()+" Tree = '"+arg0+"'");
        }
    }

    private ReferenceType findEnclosingType(ReferenceType type, String name, VMCache cache) {
        if (type.name().equals(name)) {
            return type;
        }
        ReferenceType enclosingType;
        enclosingType = cache.getEnclosingType(type, name);
        if (enclosingType == null) {
            List<ReferenceType> classes = type.virtualMachine().classesByName(name);
            if (classes.size() == 1) {
                enclosingType = classes.get(0);
            } else {
                for (ReferenceType clazz : classes) {
                    if (isNestedOf(clazz, type)) {
                        enclosingType = clazz;
                        break;
                    }
                }
            }
            if (enclosingType != null) {
                cache.setEnclosingType(type, name, enclosingType);
            }
        }
        long end = System.nanoTime();
        return enclosingType;
    }

    private boolean isNestedOf(ReferenceType nested, ReferenceType type) {
        if (nested.equals(type)) {
            return true;
        }
        for (ReferenceType n : nested.nestedTypes()) {
            if (isNestedOf(n, type)) {
                return true;
            }
        }
        return false;
    }

    private ObjectReference findEnclosingObject(Tree arg0, ObjectReference object, ReferenceType type, String fieldName, String methodName) {
        if (instanceOf(object.referenceType(), type)) {
            return object;
        }
        if (((ReferenceType) object.type()).isStatic()) {
            // instance fields/methods can not be accessed from static context.
            if (fieldName != null) {
                Assert.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
            }
            if (methodName != null) {
                Assert.error(arg0, "invokeInstanceMethodAsStatic", methodName);
            }
            return null;
        }
        object = findOuterObject(object);
        if (object == null) {
            return null;
        }
        return findEnclosingObject(arg0, object, type, fieldName, methodName);
    }
    
    private static ObjectReference findOuterObject(ObjectReference object) {
        Field outerRef = null;
        for (int i = 0; i < 10; i++) {
            outerRef = object.referenceType().fieldByName("this$"+i);           // NOI18N
            if (outerRef != null) {
                break;
            }
        }
        if (outerRef == null) {
            return null;
        }
        ObjectReference outerObject = (ObjectReference) object.getValue(outerRef);
        return outerObject;
    }

    @Override
    public Mirror visitIf(IfTree arg0, EvaluationContext evaluationContext) {
        boolean evaluatedCondition = evaluateCondition(arg0, evaluationContext, arg0.getCondition());
        StatementTree statement;
        if (evaluatedCondition) {
            statement = arg0.getThenStatement();
        } else {
            statement = arg0.getElseStatement();
        }
        if (statement != null) {
            try {
                evaluationContext.pushBlock();
                return statement.accept(this, evaluationContext);
            } finally {
                evaluationContext.popBlock();
            }
        } else {
            return null;
        }
    }

    @Override
    public Mirror visitImport(ImportTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitArrayAccess(ArrayAccessTree arg0, EvaluationContext evaluationContext) {
        Mirror array = arg0.getExpression().accept(this, evaluationContext);
        if (array == null) {
            Assert.error(arg0, "arrayIsNull", arg0.getExpression());
        }
        Mirror index = arg0.getIndex().accept(this, evaluationContext);
        if (!(array instanceof ArrayReference)) {
            Assert.error(arg0, "notArrayType", arg0.getExpression());
        }
        if (!(index instanceof PrimitiveValue)) {
            Assert.error(arg0, "arraySizeBadType", index);
        }
        int i = ((PrimitiveValue) index).intValue();
        if (i < 0 || i >= ((ArrayReference) array).length()) {
            Assert.error(arg0, "arrayIndexOutOfBounds", array, i);
        }
        evaluationContext.putArrayAccess(arg0, (ArrayReference)array, i);
        return ((ArrayReference) array).getValue(i);
    }

    @Override
    public Mirror visitLabeledStatement(LabeledStatementTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitLiteral(LiteralTree arg0, EvaluationContext evaluationContext) {
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        Object value = arg0.getValue();
        if (value instanceof Boolean) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Byte) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Character) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Double) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Float) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Integer) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Long) {
            return mirrorOf(vm, value);
        }
        if (value instanceof Short) {
            return mirrorOf(vm, value);
        }
        if (value instanceof String) {
            StringReference str = createStringMirrorWithDisabledCollection((String) value, vm, evaluationContext);
            VMCache cache = evaluationContext.getVMCache();
            ClassType strClass = (ClassType) cache.getClass(String.class.getName());
            if (strClass == null) {
                return str;
            }
            try {
                List<Value> args = Collections.emptyList();
                return invokeMethod(arg0, strClass.methodsByName("intern").get(0),
                                    false, strClass, str, args, evaluationContext, false);
            } catch (Exception ex) {
                return str;
            }
        }
        if (value == null) {
            return null;
        }
        throw new UnsupportedOperationException("Unsupported value: "+value);
    }

    @Override
    public Mirror visitMethod(MethodTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitModifiers(ModifiersTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitNewArray(NewArrayTree arg0, EvaluationContext evaluationContext) {
        Type type;
        Tree typeTree = arg0.getType();
        if (typeTree == null) {
            if (newArrayType == null) {
                throw new IllegalStateException("No type info for "+arg0);
            }
            type = newArrayType;
        } else {
            type = (Type) arg0.getType().accept(this, evaluationContext);
        }
        List<? extends ExpressionTree> dimensionTrees = arg0.getDimensions();
        int numDimensions = dimensionTrees.size();
        if (numDimensions > 0) {
            int[] dimensions = new int[numDimensions];
            ArrayType[] arrayTypes = new ArrayType[numDimensions];
            String arrayClassName = type.name()+"[]";
            for (int i = 0; i < numDimensions; i++, arrayClassName += "[]") {
                dimensions[i] = ((PrimitiveValue) dimensionTrees.get(numDimensions - 1 - i).accept(this, evaluationContext)).intValue();
                ReferenceType rt = getOrLoadClass(type.virtualMachine(), arrayClassName, evaluationContext);
                if (rt == null) {
                    Assert.error(arg0, "unknownType", arrayClassName);
                }
                arrayTypes[i] = (ArrayType) rt;
            }
            return constructNewArray(arrayTypes, dimensions, numDimensions - 1, evaluationContext);
        } else {
            List<? extends ExpressionTree> initializerTrees = arg0.getInitializers();
            return constructNewArray(arg0, type, initializerTrees, evaluationContext);
        }
    }

    private ArrayReference constructNewArray(ArrayType[] arrayTypes, int[] dimensions, int dimension, EvaluationContext evaluationContext) {
        ArrayReference array = createArrayMirrorWithDisabledCollection(arrayTypes[dimension], dimensions[dimension], evaluationContext);
        if (dimension > 0) {
            List<ArrayReference> elements = new ArrayList<ArrayReference>(dimensions[dimension]);
            for (int i = 0; i < dimensions[dimension]; i++) {
                ArrayReference subArray = constructNewArray(arrayTypes, dimensions, dimension - 1, evaluationContext);
                elements.add(subArray);
            }
            try {
                array.setValues(elements);
            } catch (InvalidTypeException ex) {
                throw new IllegalStateException("ArrayType "+arrayTypes[dimension]+" can not have "+elements+" elements.");
            } catch (ClassNotLoadedException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return array;
    }

    private ArrayReference constructNewArray(NewArrayTree arg0, Type type, List<? extends ExpressionTree> initializerTrees, EvaluationContext evaluationContext) {
        int n = initializerTrees.size();
        List<Value> elements = new ArrayList<Value>(n);
        for (int i = 0; i < n; i++) {
            ExpressionTree exp = initializerTrees.get(i);
            newArrayType = getSubArrayType(arg0, type, evaluationContext);
            // might call visitNewArray()
            Value elementValue = (Value) exp.accept(this, evaluationContext);
            newArrayType = null;
            if (elementValue instanceof ArtificialMirror) {
                elementValue = (Value)((ArtificialMirror)elementValue).getVMMirror();
            }
            elements.add(elementValue);
        }
        int depth = 1;
        ArrayReference array = createArrayMirrorWithDisabledCollection(getArrayType(arg0, type, depth, evaluationContext), n, evaluationContext);
        autoboxElements(arg0, type, elements, evaluationContext);
        try {
            array.setValues(elements);
        } catch (InvalidTypeException ex) {
            throw new IllegalStateException("ArrayType "+getArrayType(arg0, type, depth, evaluationContext)+" can not have "+elements+" elements.");
        } catch (ClassNotLoadedException ex) {
            throw new IllegalStateException(ex);
        }
        return array;
    }

    private static final String BRACKETS = "[][][][][][][][][][][][][][][][][][][][]"; // NOI18N

    private ArrayType getArrayType(NewArrayTree arg0, Type type, int depth, EvaluationContext evaluationContext) {
        String arrayClassName;
        if (depth < BRACKETS.length()/2) {
            arrayClassName = type.name() + BRACKETS.substring(0, 2*depth);
        } else {
            arrayClassName = type.name() + BRACKETS;
            for (int i = BRACKETS.length()/2; i < depth; i++) {
                arrayClassName += "[]"; // NOI18N
            }
        }
        ReferenceType rt = getOrLoadClass(type.virtualMachine(), arrayClassName, evaluationContext);
        if (rt == null) {
            Assert.error(arg0, "unknownType", arrayClassName);
        }
        return (ArrayType) rt;
    }

    private Type getSubArrayType(Tree arg0, Type type, EvaluationContext evaluationContext) {
        String name = type.name();
        if (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
            if (!name.endsWith("[]")) {
                Type pType = getPrimitiveType(name, type.virtualMachine());
                if (pType != null) {
                    return pType;
                }
            }
            type = getOrLoadClass(type.virtualMachine(), name, evaluationContext);
            if (type == null) {
                Assert.error(arg0, "unknownType", name);
            }
        }
        return type;
    }

    private Type getPrimitiveType(String name, VirtualMachine vm) {
        if (name.equals(Boolean.TYPE.getName())) {
            return vm.mirrorOf(true).type();
        }
        if (name.equals((Byte.TYPE.getName()))) {
            return vm.mirrorOf((byte) 0).type();
        }
        if (name.equals((Character.TYPE.getName()))) {
            return vm.mirrorOf('a').type();
        }
        if (name.equals((Double.TYPE.getName()))) {
            return vm.mirrorOf(0.).type();
        }
        if (name.equals((Float.TYPE.getName()))) {
            return vm.mirrorOf(0f).type();
        }
        if (name.equals((Integer.TYPE.getName()))) {
            return vm.mirrorOf(0).type();
        }
        if (name.equals((Long.TYPE.getName()))) {
            return vm.mirrorOf(0l).type();
        }
        if (name.equals((Short.TYPE.getName()))) {
            return vm.mirrorOf((short) 0).type();
        }
        return null;
    }

    @Override
    public Mirror visitNewClass(NewClassTree arg0, EvaluationContext evaluationContext) {
        ClassTree ct = arg0.getClassBody();
        if (ct != null) {
            Assert.error(arg0, "noNewClassWithBody");
        }
        TreePath currentPath = getCurrentPath();
        TypeMirror cType;
        boolean isVarArgs = false;
        if (currentPath != null) {
            TreePath identifierPath = TreePath.getPath(currentPath, arg0);
            if (identifierPath == null) {
                identifierPath = currentPath;
            }
            Element elm = evaluationContext.getTrees().getElement(identifierPath);
            if (elm != null) {
                if (elm.asType().getKind() == TypeKind.ERROR) {
                    cType = null;
                } else {
                    if (elm.getKind() != ElementKind.CONSTRUCTOR) {
                        throw new IllegalStateException("Element "+elm+" is of "+elm.getKind()+" kind. Tree = "+arg0);
                    }
                    ExecutableElement cElem = (ExecutableElement) elm;
                    cType = cElem.asType();
                    isVarArgs = cElem.isVarArgs();
                }
            } else {
                // Unresolved class
                cType = null;
            }
        } else {
            cType = null;
        }
        ExpressionTree enclosingExpression = arg0.getEnclosingExpression();
        Mirror enclosing = null;
        if (enclosingExpression != null) {
            enclosing = enclosingExpression.accept(this, evaluationContext);
        }
        ExpressionTree classIdentifier = arg0.getIdentifier();
        Mirror clazz = classIdentifier.accept(this, evaluationContext);
        if (clazz instanceof ClassObjectReference) {
            clazz = ((ClassObjectReference) clazz).reflectedType();
        }
        if (!(clazz instanceof ClassType)) {
            Assert.error(classIdentifier, "unknownType", classIdentifier.toString());
        }
        ClassType classType = (ClassType) clazz;
        //ReferenceType classType = getClassType(arg0, cType, evaluationContext);
        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if ((argValue != null) && !(argValue instanceof Value)) {
                Assert.error(arg, "Not a value");
            }
            if (argValue instanceof ArtificialMirror) {
                argValue = ((ArtificialMirror) argValue).getVMMirror();
            }
            argVals.add((Value) argValue);
        }
        List<? extends TypeMirror> paramTypes = null;
        String firstParamSignature = null;
        List<Type> argTypes = null;
        if (cType != null) {
            paramTypes = ((ExecutableType) cType).getParameterTypes();
            ObjectReference thisObject;
            try {
                thisObject = evaluationContext.getFrame().thisObject();
            } catch (com.sun.jdi.InternalException iex) {
                if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=173327
                    thisObject = null;
                } else {
                    throw iex; // re-throw the original
                }
            }
            if (enclosing instanceof ObjectReference) {
                ObjectReference enclosingObject = (ObjectReference) enclosing;
                argVals.add(0, enclosingObject);
                firstParamSignature = enclosingObject.referenceType().signature();
            } else if (thisObject != null) {
                String className = classType.name();
                if (className.contains("$") && !classType.isStatic()) { // An inner class
                    // A constructor of a non-static inner class always needs
                    // a reference to the instance of the outer class.
                    // 1) If 'this' is an instance of the outer class, use it directly.
                    // 2) If 'this' is an instance of the inner class, use this$0 (this$1,...)
                    ClassType thisClass = (ClassType) thisObject.type();
                    ReferenceType outerClass = findEnclosingType(classType,
                                                                 className.substring(0, className.lastIndexOf('$')),
                                                                 evaluationContext.getVMCache());
                    if (outerClass != null) {
                        if (instanceOf(thisClass, outerClass)) {
                            // 'this' is an instance of the outer class, use it directly.
                            argVals.add(0, thisObject);
                            firstParamSignature = outerClass.signature();
                        } else {
                            // 'this' is likely an instance of some inner class, use this$0 (this$1,...)
                            ObjectReference enclosingObject = findEnclosingObject(arg0, thisObject, outerClass, null, null);
                            if (enclosingObject != null) {
                                argVals.add(0, enclosingObject);
                                firstParamSignature = outerClass.signature();
                            }
                        }
                        /*
                        if (instanceOf(thisClass, classType)) {
                            // 'this' is an instance of the inner class, use this$0 (this$1,...)
                            ObjectReference enclosingObject = findEnclosingObject(arg0, thisObject, outerClass, null, null);
                            if (enclosingObject != null) {
                                argVals.add(0, enclosingObject);
                                firstParamSignature = outerClass.signature();
                            }
                        } else if (instanceOf(thisClass, outerClass)) {
                            // 'this' is an instance of the outer class, use it directly.
                            argVals.add(0, thisObject);
                            firstParamSignature = outerClass.signature();
                        }
                        */
                    }
                    /*
                    while (thisClass != null) {
                        List<ReferenceType> nestedTypes = thisClass.nestedTypes();
                        for (ReferenceType nested : nestedTypes) {
                            if (!nested.isStatic() && nested.equals(classType)) {
                                argVals.add(0, thisObject);
                                firstParamSignature = thisClass.signature();
                                break;
                            }
                        }
                        if (firstParamSignature != null) {
                            break;
                        }
                        thisClass = thisClass.superclass();
                    }
                    */
                }
            }
        } else {
            argTypes = new ArrayList<Type>(argVals.size());
            for (Value value : argVals) {
                if (value == null) {
                    ClassType objectClass = (ClassType) evaluationContext.getVMCache().getClass(Object.class.getName());
                    if (objectClass == null) {
                        return null;
                    }
                    argTypes.add(objectClass);
                } else {
                    argTypes.add(value.type());
                }
            }
            ObjectReference thisObject;
            try {
                thisObject = evaluationContext.getFrame().thisObject();
            } catch (com.sun.jdi.InternalException iex) {
                if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=173327
                    thisObject = null;
                } else {
                    throw iex; // re-throw the original
                }
            }
            if (enclosing instanceof ObjectReference) {
                ObjectReference enclosingObject = (ObjectReference) enclosing;
                argVals.add(0, enclosingObject);
                argTypes.add(0, enclosingObject.referenceType());
                firstParamSignature = enclosingObject.referenceType().signature();
            } else if (thisObject != null) {
                String className = classType.name();
                if (className.contains("$") && !classType.isStatic()) { // An inner class
                    // A constructor of a non-static inner class always needs
                    // a reference to the instance of the outer class.
                    // 1) If 'this' is an instance of the outer class, use it directly.
                    // 2) If 'this' is an instance of the inner class, use this$0 (this$1,...)
                    ClassType thisClass = (ClassType) thisObject.type();
                    ReferenceType outerClass = findEnclosingType(classType,
                                                                 className.substring(0, className.lastIndexOf('$')),
                                                                 evaluationContext.getVMCache());
                    if (outerClass != null) {
                        if (instanceOf(thisClass, outerClass)) {
                            // 'this' is an instance of the outer class, use it directly.
                            argVals.add(0, thisObject);
                            argTypes.add(0, outerClass);
                            firstParamSignature = outerClass.signature();
                        } else {
                            // 'this' is likely an instance of some inner class, use this$0 (this$1,...)
                            ObjectReference enclosingObject = findEnclosingObject(arg0, thisObject, outerClass, null, null);
                            if (enclosingObject != null) {
                                argVals.add(0, enclosingObject);
                                argTypes.add(0, outerClass);
                                firstParamSignature = outerClass.signature();
                            }
                        }
                    }
                }
                /*
                List<ReferenceType> nestedTypes = ((ReferenceType) thisObject.type()).nestedTypes();
                for (ReferenceType nested : nestedTypes) {
                    if (!nested.isStatic() && nested.equals(classType)) {
                        argVals.add(0, thisObject);
                        argTypes.add(0, thisObject.type());
                    }
                }*/
            }
        }
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "STARTED : {0}.<init> ({1}) in thread {2}", new Object[]{classType, argVals, evaluationContext.getFrame().thread()});
            }
            evaluationContext.methodToBeInvoked();
            Method constructorMethod = getConcreteMethodAndReportProblems(arg0, classType, "<init>", firstParamSignature, paramTypes, argTypes);
            if (isVarArgs) {
                transformVarArgsValues(arg0, argVals, paramTypes, evaluationContext);
            }
            ObjectReference o = classType.newInstance(evaluationContext.getFrame().thread(),
                                                      constructorMethod,
                                                      argVals,
                                                      ObjectReference.INVOKE_SINGLE_THREADED);
            //evaluationContext.disableCollectionOf(o); - Not necessary, new instances are not collected!
            return o;
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(cnlex);
        } catch (IncompatibleThreadStateException |
                 UnsupportedOperationException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(ieex);
        } finally {
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                throw new IllegalStateException(ieex);
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "FINISHED: {0}.<init> ({1}) in thread {2}", new Object[]{classType, argVals, evaluationContext.getFrame().thread()});
            }
        }
    }

    @Override
    public Mirror visitParenthesized(ParenthesizedTree arg0, EvaluationContext evaluationContext) {
        return arg0.getExpression().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitReturn(ReturnTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree exprTree = arg0.getExpression();
        Mirror result;
        if (exprTree == null) {
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (vm == null) {
                return null;
            }
            // vm.mirrorOfVoid(); [TODO]
            result = null;
        } else {
            result = exprTree.accept(this, evaluationContext);
        }
        return new Return(result);
    }

    private Value getEnumConstant(Tree arg0, VariableElement ve, EvaluationContext evaluationContext) {
        String constantName = ve.getSimpleName().toString();
        ReferenceType enumType = getClassType(arg0, ve.asType(), evaluationContext);
        Method valueOfMethod = enumType.methodsByName("valueOf").get(0);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        StringReference constantNameRef = createStringMirrorWithDisabledCollection(constantName, vm, evaluationContext);
        Value enumValue = invokeMethod(arg0, valueOfMethod, true, (ClassType) enumType, null,
                     Collections.singletonList((Value) constantNameRef), evaluationContext, false);
        return enumValue;
    }
    
    private static boolean hasBinaryName(TypeElement te) {
        try {
            String className = ElementUtilities.getBinaryName(te);
            return className != null;
        } catch (IllegalArgumentException ex) {}
        return false;
    }

    @Override
    public Mirror visitMemberSelect(MemberSelectTree arg0, EvaluationContext evaluationContext) {
        TreePath currentPath = getCurrentPath();
        Element elm = null;
        if (currentPath != null) {
            // We have the path and resolved elements
            TreePath memberSelectPath = TreePath.getPath(currentPath, arg0);
            if (memberSelectPath == null) {
                memberSelectPath = currentPath;
            }
            elm = evaluationContext.getTrees().getElement(memberSelectPath);
            if (elm instanceof TypeElement &&
                ((TypeElement) elm).asType() instanceof ErrorType &&
                !hasBinaryName((TypeElement) elm)) {
                
                currentPath = null; // Elements not resolved correctly
            }
        }
        if (currentPath == null || elm == null) {
            String name = arg0.getIdentifier().toString();
            if (name.equals("class")) { // It's a class file
                String className = arg0.getExpression().toString();
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) {
                    return null;
                }
                ReferenceType rt = getOrLoadClass(vm, className, evaluationContext);
                if (rt == null) {
                    Assert.error(arg0, "unknownType", className);
                }
                return rt.classObject();
            }
            Mirror expr = arg0.getExpression().accept(this, evaluationContext);
            // try field:
            if (expr instanceof ClassType) {
                ClassType clazz = (ClassType) expr;
                if (name.equals("this")) {
                    ObjectReference thisObject = evaluationContext.getContextObject();
                    while (thisObject != null && !((ReferenceType) thisObject.type()).equals(clazz)) {
                        thisObject = findOuterObject(thisObject);
                    }
                    if (thisObject == null) {
                        Assert.error(arg0, "unknownOuterClass", clazz.name());
                    } else {
                        return thisObject;
                    }
                }
                if (name.equals("class")) {
                    return clazz.classObject();
                }
                Field f = clazz.fieldByName(name);
                if (f != null) {
                    if (!f.isStatic()) {
                        Assert.error(arg0, "accessInstanceVariableFromStaticContext", name);
                        return null;
                    }
                    evaluationContext.putField(arg0, f, null);
                    Value v = clazz.getValue(f);
                    if (v instanceof ObjectReference) {
                        evaluationContext.disableCollectionOf((ObjectReference) v);
                    }
                    return v;
                }
            } else if (expr instanceof InterfaceType) {
                if (name.equals("class")) {
                    return ((InterfaceType) expr).classObject();
                }
            } else if (expr instanceof ArrayType) {
                if (name.equals("class")) {
                    return ((ArrayType) expr).classObject();
                }
            } else if (expr instanceof ObjectReference) {
                if (expr instanceof ArrayReference && "length".equals(name)) {
                    return expr.virtualMachine().mirrorOf(((ArrayReference) expr).length());
                }
                ReferenceType type = (ReferenceType) getSubExpressionType(arg0.getExpression());
                if (type == null) {
                    type = ((ObjectReference) expr).referenceType();
                }
                Field f = type.fieldByName(name);
                if (f != null) {
                    evaluationContext.putField(arg0, f, (ObjectReference) expr);
                    Value v = ((ObjectReference) expr).getValue(f);
                    if (v instanceof ObjectReference) {
                        evaluationContext.disableCollectionOf((ObjectReference) v);
                    }
                    return v;
                }
            }
            if (expr == null) {
                Assert.error(arg0, "fieldOnNull", name);
            }
            // try class
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (vm == null) {
                return null;
            }
            ReferenceType rt = getOrLoadClass(vm, name, evaluationContext);
            if (rt == null) {
                Assert.error(arg0, "unknownType", name);
            }
            return rt;
        }
        // We have the path and resolved elements
        switch(elm.getKind()) {
            case ENUM_CONSTANT:
                return getEnumConstant(arg0, (VariableElement) elm, evaluationContext);
            case FIELD:
                VariableElement ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                Mirror expr = arg0.getExpression().accept(this, evaluationContext);
                if (expr instanceof ClassType) {
                    ClassType clazz = (ClassType) expr;
                    if (fieldName.equals("this")) {
                        ObjectReference thisObject = evaluationContext.getContextObject();
                        // Need to check sub-classes also (clazz equal or sub-class of thisObject.type()
                        while (thisObject != null && !instanceOf((ReferenceType) thisObject.type(), clazz)) {
                            thisObject = findOuterObject(thisObject);
                        }
                        if (thisObject == null) {
                            Assert.error(arg0, "unknownOuterClass", clazz.name());
                        } else {
                            return thisObject;
                        }
                    }
                    if (fieldName.equals("class")) {
                        return clazz.classObject();
                    }
                    Field f = clazz.fieldByName(fieldName);
                    if (f != null) {
                        if (!f.isStatic()) {
                            Assert.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
                            return null;
                        }
                        evaluationContext.putField(arg0, f, null);
                        Value v = clazz.getValue(f);
                        if (v instanceof ObjectReference) {
                            evaluationContext.disableCollectionOf((ObjectReference) v);
                        }
                        return v;
                    } else {
                        Assert.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                if (expr instanceof InterfaceType) {
                    InterfaceType intrfc = (InterfaceType) expr;
                    if (fieldName.equals("class")) {
                        return intrfc.classObject();
                    }
                    Field f = intrfc.fieldByName(fieldName);
                    if (f != null) {
                        Value v = intrfc.getValue(f);
                        if (v instanceof ObjectReference) {
                            evaluationContext.disableCollectionOf((ObjectReference) v);
                        }
                        return v;
                    } else {
                        Assert.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                if (expr instanceof ArrayType) {
                    ArrayType arr = (ArrayType) expr;
                    if (fieldName.equals("class")) {
                        return arr.classObject();
                    }
                }
                if (expr instanceof ObjectReference) {
                    if (expr instanceof ArrayReference && "length".equals(fieldName)) {
                        return expr.virtualMachine().mirrorOf(((ArrayReference) expr).length());
                    }
                    ReferenceType type = (ReferenceType) getSubExpressionType(arg0.getExpression());
                    if (type == null) {
                        type = ((ObjectReference) expr).referenceType();
                    }
                    TypeMirror classType = ve.getEnclosingElement().asType(); // Class type of the object we retrieve the field on.
                    // Find the sub-type that is declared in the source code
                    ReferenceType preferredType = findPreferedType(type, classType.toString());
                    Field f = null;
                    if (preferredType != null) {
                        f = preferredType.fieldByName(fieldName);
                    }
                    if (f == null) {
                        f = type.fieldByName(fieldName);
                    }
                    if (f != null) {
                        evaluationContext.putField(arg0, f, (ObjectReference) expr);
                        Value v = ((ObjectReference) expr).getValue(f);
                        if (v instanceof ObjectReference) {
                            evaluationContext.disableCollectionOf((ObjectReference) v);
                        }
                        return v;
                    } else {
                        Assert.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                if (expr == null) {
                    Assert.error(arg0, "fieldOnNull", fieldName);
                }
                Assert.error(arg0, "invalidMemberReference", arg0.toString());
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
                TypeElement te = (TypeElement) elm;
                String className = ElementUtilities.getBinaryName(te);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) {
                    return null;
                }
                ReferenceType rt = getOrLoadClass(vm, className, evaluationContext);
                if (rt == null) {
                    Assert.error(arg0, "unknownType", className);
                }
                return rt;
            case PACKAGE:
                return (Value) Assert.error(arg0, "notExpression");
            default:
                throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"', element kind = "+elm.getKind());
        }
    }

    @Override
    public Mirror visitEmptyStatement(EmptyStatementTree arg0, EvaluationContext evaluationContext) {
        return null;
    }

    @Override
    public Mirror visitSwitch(SwitchTree arg0, EvaluationContext evaluationContext) {
        Mirror switchValue = arg0.getExpression().accept(this, evaluationContext);
        CaseTree defaultTree = null;
        Mirror result = null;
        boolean caseMatched = false;
        outer:
        for (CaseTree caseTree : arg0.getCases()) {
            Tree caseExpr = caseTree.getExpression();
            if (caseExpr == null) {
                defaultTree = caseTree;
                continue;
            }
            if (!caseMatched) {
                Mirror value = caseExpr.accept(this, evaluationContext);
                if (isEqual(switchValue, value, evaluationContext)) {
                    caseMatched = true;
                } // if
            } // if
            if (caseMatched) {
                try {
                    evaluationContext.pushBlock();
                    for (StatementTree tree : caseTree.getStatements()) {
                        Mirror res = tree.accept(this, evaluationContext);
                        if (res != null) {
                            result = res;
                            if (result instanceof CommandMirror) {
                                break outer;
                            }
                        }
                    } // for
                } finally {
                    evaluationContext.popBlock();
                }
            } // if
        } // for
        if (!caseMatched && defaultTree != null) {
            try {
                evaluationContext.pushBlock();
                for (StatementTree tree : defaultTree.getStatements()) {
                    Mirror res = tree.accept(this, evaluationContext);
                    if (res != null) {
                        result = res;
                    }
                } // for
            } finally {
                evaluationContext.popBlock();
            }
        }
        return result;
    }

    @Override
    public Mirror visitSynchronized(SynchronizedTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitThrow(ThrowTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitCompilationUnit(CompilationUnitTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitTry(TryTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitParameterizedType(ParameterizedTypeTree arg0, EvaluationContext evaluationContext) {
        return arg0.getType().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitArrayType(ArrayTypeTree arg0, EvaluationContext evaluationContext) {
        Mirror arrayType = arg0.getType().accept(this, evaluationContext);
        if (!(arrayType instanceof Type)) {
            return arrayType;
        }
        Type type = (Type) arrayType;
        String arrayClassName = type.name()+"[]";
        ReferenceType aType = getOrLoadClass(type.virtualMachine(), arrayClassName, evaluationContext);
        if (aType != null) {
            return aType;
        } else {
            Assert.error(arg0, "unknownType", arrayClassName);
            return null;
        }
    }

    @Override
    public Mirror visitTypeCast(TypeCastTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree expTree = arg0.getExpression();
        Mirror expr = expTree.accept(this, evaluationContext);
        if (expr == null) {
            return null;
        }
        Tree typeTree = arg0.getType();
        Mirror type;
        if (getCurrentPath() == null) {
            // We do not have elements, we'll not be able to parse the class name.
            String className = typeTree.toString();
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (vm == null) {
                return null;
            }
            ReferenceType rt = getOrLoadClass(vm, className, evaluationContext);
            if (rt == null) {
                Assert.error(arg0, "unknownType", className);
            }
            type = rt;
        } else { // We have elements, we can resolve the type
            type = typeTree.accept(this, evaluationContext);
        }
        if (expr instanceof PrimitiveValue) {
            PrimitiveValue primValue = (PrimitiveValue) expr;
            if (primValue instanceof BooleanValue) {
                Assert.assertAssignable(type, BooleanType.class, arg0, "castToBooleanRequired", primValue, type);
                return primValue;
            }
            Assert.assertNotAssignable(type, BooleanType.class, arg0, "castFromBooleanRequired", primValue, type);
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (vm == null) {
                return null;
            }
            if (type instanceof PrimitiveType) {
                if (type instanceof ByteType) {
                    return mirrorOf(vm, primValue.byteValue());
                } else if (type instanceof CharType) {
                    return mirrorOf(vm, primValue.charValue());
                } else if (type instanceof DoubleType) {
                    return mirrorOf(vm, primValue.doubleValue());
                } else if (type instanceof FloatType) {
                    return mirrorOf(vm, primValue.floatValue());
                } else if (type instanceof IntegerType) {
                    return mirrorOf(vm, primValue.intValue());
                } else if (type instanceof LongType) {
                    return mirrorOf(vm, primValue.longValue());
                } else if (type instanceof ShortType) {
                    return mirrorOf(vm, primValue.shortValue());
                } else {
                    Assert.error(arg0, "unknownType", type.toString());
                }
            } else if (type instanceof ReferenceType) {
                // Box the primitive type:
                List<Value> element = new LinkedList<>();
                element.add(primValue);
                autoboxElements(typeTree, (ReferenceType) type, element, evaluationContext);
                expr = element.get(0);
            } else {
                Assert.error(arg0, "unknownType", type.toString());
            }
        }
        if (!instanceOf(((ObjectReference) expr).type(), (Type) type)) {
            Assert.error(arg0, "castError", ((ObjectReference) expr).type(), type);
        }
        subExpressionTypes.put(arg0, (Type) type);
        return expr;
    }

    @Override
    public Mirror visitPrimitiveType(PrimitiveTypeTree arg0, EvaluationContext evaluationContext) {
        TypeKind type = arg0.getPrimitiveTypeKind();
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        switch(type) {
            case BOOLEAN:
                return vm.mirrorOf(true).type();
            case BYTE:
                return vm.mirrorOf((byte) 0).type();
            case CHAR:
                return vm.mirrorOf('a').type();
            case DOUBLE:
                return vm.mirrorOf(0.).type();
            case FLOAT:
                return vm.mirrorOf(0f).type();
            case INT:
                return vm.mirrorOf(0).type();
            case LONG:
                return vm.mirrorOf(0l).type();
            case SHORT:
                return vm.mirrorOf((short) 0).type();
            case VOID:
                return vm.mirrorOfVoid().type();
            default:
                throw new IllegalStateException("Tree = "+arg0);
        }
    }

    @Override
    public Mirror visitTypeParameter(TypeParameterTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitInstanceOf(InstanceOfTree arg0, EvaluationContext evaluationContext) {
        Mirror expr = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        if (expr == null) {
            return mirrorOf(vm, false);
        }
        Assert.assertAssignable(expr, ObjectReference.class, arg0, "instanceOfLeftOperandNotAReference", expr);

        ReferenceType expressionType = ((ObjectReference) expr).referenceType();
        Type type = (Type) arg0.getType().accept(this, evaluationContext);

        return mirrorOf(vm, instanceOf(expressionType, type));
    }

    @Override
    public Mirror visitUnary(UnaryTree arg0, EvaluationContext evaluationContext) {
        Mirror expr = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return null;
        }
        if (expr == null) {
            return null;
        }
        if (expr instanceof ObjectReference) {
            expr = unboxIfCan(arg0, (ObjectReference) expr, evaluationContext);
        }
        Tree.Kind kind = arg0.getKind();
        if (expr instanceof BooleanValue) {
            boolean v = ((BooleanValue) expr).value();
            switch (kind) {
                case LOGICAL_COMPLEMENT:
                    v = !v;
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(boolean) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof ByteValue) {
            byte v = ((ByteValue) expr).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return mirrorOf(vm, i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return mirrorOf(vm, i);
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(byte) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof CharValue) {
            char v = ((CharValue) expr).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return mirrorOf(vm, i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return mirrorOf(vm, i);
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(char) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof ShortValue) {
            short v = ((ShortValue) expr).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return mirrorOf(vm, i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return mirrorOf(vm, i);
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(short) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof IntegerValue) {
            int v = ((IntegerValue) expr).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    v = ~v;
                    break;
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(int) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof LongValue) {
            long v = ((LongValue) expr).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    v = ~v;
                    break;
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(long) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof DoubleValue) {
            double v = ((DoubleValue) expr).value();
            switch (kind) {
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(double) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        if (expr instanceof FloatValue) {
            float v = ((FloatValue) expr).value();
            switch (kind) {
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), mirrorOf(vm, v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default:
                    Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), "(float) "+arg0.getExpression());
                    //throw new IllegalStateException("Tree = "+arg0);
            }
            return mirrorOf(vm, v);
        }
        Assert.error(arg0, "evaluateErrorUnary", operatorToString(kind), arg0.getExpression());
        throw new IllegalStateException("Bad expression type: "+expr);
    }

    private static String operatorToString(Tree.Kind kind) {
        switch (kind) {
            case AND:
                return "&"; // NOI18N
            case AND_ASSIGNMENT:
                return "&="; // NOI18N
            case CONDITIONAL_AND:
                return "&&"; // NOI18N
            case CONDITIONAL_OR:
                return "||"; // NOI18N
            case LEFT_SHIFT:
                return "<<"; // NOI18N
            case LEFT_SHIFT_ASSIGNMENT:
                return "<<="; // NOI18N
            case LOGICAL_COMPLEMENT:
                return "!"; // NOI18N
            case MINUS:
                return "-"; // NOI18N
            case MINUS_ASSIGNMENT:
                return "-="; // NOI18N
            case MULTIPLY:
                return "*"; // NOI18N
            case MULTIPLY_ASSIGNMENT:
                return "*="; // NOI18N
            case OR:
                return "|"; // NOI18N
            case OR_ASSIGNMENT:
                return "|="; // NOI18N
            case PLUS:
                return "+"; // NOI18N
            case PLUS_ASSIGNMENT:
                return "+="; // NOI18N
            case PREFIX_DECREMENT:
                return "--"; // NOI18N
            case PREFIX_INCREMENT:
                return "++"; // NOI18N
            case REMAINDER:
                return "%"; // NOI18N
            case REMAINDER_ASSIGNMENT:
                return "%="; // NOI18N
            case RIGHT_SHIFT:
                return ">>"; // NOI18N
            case RIGHT_SHIFT_ASSIGNMENT:
                return ">>="; // NOI18N
            case UNARY_MINUS:
                return "-"; // NOI18N
            case UNARY_PLUS:
                return "+"; // NOI18N
            case UNSIGNED_RIGHT_SHIFT:
                return ">>>"; // NOI18N
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                return ">>>="; // NOI18N
            case XOR:
                return "^"; // NOI18N
            case XOR_ASSIGNMENT:
                return "^="; // NOI18N
            default:
                return kind.toString();
        }
    }

    @Override
    public Mirror visitVariable(VariableTree arg0, EvaluationContext evaluationContext) {
        // Variable declaration
        String name = arg0.getName().toString();
        if (evaluationContext.getScriptVariableByName(name) != null) {
            Assert.error(arg0, "localVariableAlreadyDefined", name);
        }
        Tree typeTree = arg0.getType();
        Mirror evaluatedType = typeTree.accept(this, evaluationContext);
        if (!(evaluatedType instanceof Type)) {
            Assert.error(typeTree, "unknownType", typeTree.toString());
        }
        Type type = (Type) evaluatedType;
        ScriptVariable var = evaluationContext.createScriptLocalVariable(name, type);
        ExpressionTree initializer = arg0.getInitializer();
        if (initializer != null) {
            if (Tree.Kind.NEW_ARRAY == initializer.getKind()) {
                try {
                    newArrayType = ArrayTypeWrapper.componentType((ArrayType) type);
                } catch (ClassNotLoadedException cnlex) {
                    throw new IllegalStateException(cnlex);
                } catch (InternalExceptionWrapper ex) {
                    // What can we do? Ignore...
                } catch (VMDisconnectedExceptionWrapper ex) {
                    throw ex.getCause();
                }
            }
            Mirror initialValue = initializer.accept(this, evaluationContext);
            newArrayType = null;
            var.setValue(initialValue);
            return initialValue;
        }
        return null;
    }

    @Override
    public Mirror visitWhileLoop(WhileLoopTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree condition = arg0.getCondition();
        Tree statement = arg0.getStatement();
        Mirror result = null;
        while (evaluateCondition(arg0, evaluationContext, condition)) {
            try {
                evaluationContext.pushBlock();
                Mirror res = statement.accept(this, evaluationContext);
                if (res instanceof Break) {
                    break;
                } else if (res instanceof Continue) {
                    continue;
                }
                if (res != null) {
                    result = res;
                }
            } finally {
                evaluationContext.popBlock();
            }
        }
        return result;
    }

    @Override
    public Mirror visitWildcard(WildcardTree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitOther(Tree arg0, EvaluationContext evaluationContext) {
        Assert.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitUnionType(UnionTypeTree node, EvaluationContext p) {
        // union type expression in a multicatch var declaration
        // unsupported, since catch is unsupported
        Assert.error(node, "unsupported");
        return null;
    }

    // JDK 8 language features:

    @Override
    public Mirror visitAnnotatedType(AnnotatedTypeTree node, EvaluationContext p) {
        // Annotations are ignored and super delegates to the underlying type
        return super.visitAnnotatedType(node, p);
    }
    
    @Override
    public Mirror visitIntersectionType(IntersectionTypeTree node, EvaluationContext p) {
        // intersection type in a cast expression
        List<? extends Tree> bounds = node.getBounds();
        List<ReferenceType> typeList = new ArrayList<ReferenceType>();
        for (Tree type : bounds) {
            Mirror typeMirror = type.accept(this, p);
            if (typeMirror instanceof ReferenceType) {
                typeList.add((ReferenceType) typeMirror);
            }
        }
        Type intersectionType = new IntersectionType(typeList.toArray(new ReferenceType[] {}));
        subExpressionTypes.put(node, intersectionType);
        return intersectionType;
    }
    
    @Override
    public Mirror visitLambdaExpression(LambdaExpressionTree node, EvaluationContext p) {
        /**
         * A tree node for a lambda expression.
         * It creates a new class, which is unsupported.
         */
        
        Assert.error(node, "noNewClassWithBody");
        return super.visitLambdaExpression(node, p);
    }

    @Override
    public Mirror visitMemberReference(MemberReferenceTree node, EvaluationContext p) {
        /**
         * A tree node for a member reference expression.
         * There are two kinds of member references:
         *   method references (ReferenceMode.INVOKE) and
         *   constructor references (ReferenceMode.NEW).
         * It creates a new class, which is unsupported.
         */

        Assert.error(node, "noNewClassWithBody");
        return super.visitMemberReference(node, p);
    }


    // Unsupported Jigsaw modules visitors:

    @Override
    public Mirror visitModule(ModuleTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitModule(node, p);
    }

    @Override
    public Mirror visitExports(ExportsTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitExports(node, p);
    }

    @Override
    public Mirror visitOpens(OpensTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitOpens(node, p);
    }

    @Override
    public Mirror visitProvides(ProvidesTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitProvides(node, p);
    }

    @Override
    public Mirror visitRequires(RequiresTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitRequires(node, p);
    }

    @Override
    public Mirror visitUses(UsesTree node, EvaluationContext p) {
        Assert.error(node, "noModules");
        return super.visitUses(node, p);
    }

    private Value setToMirror(Tree var, Value value, EvaluationContext evaluationContext) {
        VariableInfo varInfo = evaluationContext.getVariableInfo(var);
        if (varInfo == null) {
            Assert.error(var, "unknownVariable", var.toString());
            // EvaluationException will be thrown from the Assert
            throw new IllegalStateException("Unknown variable "+var);
        }
        if (value instanceof ArtificialMirror) {
            value = (Value)((ArtificialMirror) value).getVMMirror();
        }
        try {
            List<Value> valuePtr = new ArrayList<Value>(1);
            valuePtr.add(value);
            autoboxElements(var, varInfo.getType(), valuePtr, evaluationContext);
            value = valuePtr.get(0);
        } catch (ClassNotLoadedException ex) {}
        varInfo.setValue(value);
        return value;
    }

    @NbBundle.Messages("MSG_IncompatibleThreadStateMessage=Current thread can not invoke methods.\nProbably was not suspended by an event (breakpoint, step, etc.).")
    private Value invokeMethod(Tree arg0, Method method, Boolean isStatic, ClassType type,
                               ObjectReference objectReference, List<Value> argVals,
                               EvaluationContext evaluationContext, boolean nonVirtual) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert.error(arg0, "canNotInvokeMethods");
        }
        ThreadReference evaluationThread = null;
        try {
            evaluationThread = evaluationContext.getFrame().thread();
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "STARTED : {0}.{1} ({2}) in thread {3}", new Object[]{objectReference, method, argVals, evaluationThread});
            }
            evaluationContext.methodToBeInvoked();
            Value value;
            try {
                autoboxArguments(method.argumentTypes(), argVals, evaluationThread, evaluationContext);
            } catch (ClassNotLoadedException cnlex) {
                // TODO: Try to autobox/unbox arguments based on string types...
            }
            if (Boolean.TRUE.equals(isStatic)) {
                value = type.invokeMethod(evaluationThread, method, argVals,
                                          ObjectReference.INVOKE_SINGLE_THREADED);
            } else {
                ObjectReference object = objectReference;
                if (type != null) {
                    if (method.isPrivate()) {
                        object = findEnclosingObject(arg0, objectReference, type, null, method.name());
                    } else {
                        if (!instanceOf(objectReference.referenceType(), type)) {
                            object = findEnclosingObject(arg0, objectReference, type, null, method.name());
                        }
                    }
                }
                if (object == null) {
                    Assert.error(arg0, "noSuchMethod", method.name(), objectReference.referenceType().name());
                }
                value = object.invokeMethod(evaluationThread, method,
                                            argVals,
                                            ObjectReference.INVOKE_SINGLE_THREADED |
                                            ((nonVirtual) ? ObjectReference.INVOKE_NONVIRTUAL : 0));
            }
            if (value instanceof ObjectReference) {
                //evaluationContext.disableCollectionOf((ObjectReference) value); - Not necessary, values returned from methods are not collected!
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "   return = {0}", value);
            }
            return value;
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            String message = Bundle.MSG_IncompatibleThreadStateMessage();
            InvalidExpressionException ieex = new InvalidExpressionException(message, itsex);
            throw new IllegalStateException(ieex);
        } catch (InvalidStackFrameException isfex) {
            InvalidExpressionException ieex = new InvalidExpressionException (isfex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            loggerMethod.info("InvocationException ("+iex.getLocalizedMessage()+") has occured when there were following VMs:\n"+
                    "evaluationThread VM: "+InvocationExceptionTranslated.printVM(evaluationThread.virtualMachine())+"\n"+
                    ((objectReference != null) ?
                     ("objectReference VM: "+InvocationExceptionTranslated.printVM(objectReference.virtualMachine())) :
                     "objectReference = null")+"\n"+
                    "method VM: "+InvocationExceptionTranslated.printVM(method.virtualMachine())+"\n"+
                    ((type != null) ?
                     ("type VM: "+InvocationExceptionTranslated.printVM(type.virtualMachine())) :
                     "type = null")+"\n");
            for (Value v : argVals) {
                if (v != null) {
                    loggerMethod.info(" arg value VM: "+InvocationExceptionTranslated.printVM(v.virtualMachine()));
                }
            }
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(iex.getLocalizedMessage(), ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            throw new IllegalStateException(ieex);
        } catch (InternalException inex) {
            if (inex.errorCode() == 502) {
                inex = (com.sun.jdi.InternalException) org.openide.util.Exceptions.attachLocalizedMessage(inex, org.openide.util.NbBundle.getMessage(org.netbeans.modules.debugger.jpda.JPDADebuggerImpl.class, "JDWPError502"));
            }
            throw inex;
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "FINISHED: {0}.{1} ({2}) in thread {3}", new Object[]{objectReference, method, argVals, evaluationThread});
            }
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                throw new IllegalStateException(ieex);
            }
        }
    }

    private Value invokeMethod(Tree arg0, Method method, InterfaceType type,
                               List<Value> argVals,
                               EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert.error(arg0, "canNotInvokeMethods");
        }
        ThreadReference evaluationThread = null;
        try {
            evaluationThread = evaluationContext.getFrame().thread();
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "STARTED : {0}.{1} ({2}) in thread {3}", new Object[]{type, method, argVals, evaluationThread});
            }
            evaluationContext.methodToBeInvoked();
            Value value;
            try {
                autoboxArguments(method.argumentTypes(), argVals, evaluationThread, evaluationContext);
            } catch (ClassNotLoadedException cnlex) {
                // TODO: Try to autobox/unbox arguments based on string types...
            }
            value = InterfaceTypeWrapper.invokeMethod(type, evaluationThread,
                                                      method, argVals,
                                                      ObjectReference.INVOKE_SINGLE_THREADED);
            if (value instanceof ObjectReference) {
                //evaluationContext.disableCollectionOf((ObjectReference) value); - Not necessary, values returned from methods are not collected!
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "   return = {0}", value);
            }
            return value;
        } catch (VMDisconnectedExceptionWrapper vmdw) {
            throw vmdw.getCause();
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            String message = Bundle.MSG_IncompatibleThreadStateMessage();
            InvalidExpressionException ieex = new InvalidExpressionException(message, itsex);
            throw new IllegalStateException(ieex);
        } catch (InvalidStackFrameException isfex) {
            InvalidExpressionException ieex = new InvalidExpressionException (isfex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            loggerMethod.info("InvocationException has occured when there were following VMs:\n"+
                    "evaluationThread VM: "+InvocationExceptionTranslated.printVM(evaluationThread.virtualMachine())+"\n"+
                    ("objectReference = null")+"\n"+
                    "method VM: "+InvocationExceptionTranslated.printVM(method.virtualMachine())+"\n"+
                    ("type VM: "+InvocationExceptionTranslated.printVM(type.virtualMachine()))+"\n");
            for (Value v : argVals) {
                if (v != null) {
                    loggerMethod.info(" arg value VM: "+InvocationExceptionTranslated.printVM(v.virtualMachine()));
                }
            }
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(iex.getLocalizedMessage(), ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            throw new IllegalStateException(ieex);
        } catch (InternalExceptionWrapper inexw) {
            InternalException inex = inexw.getCause();
            if (inex.errorCode() == 502) {
                inex = (com.sun.jdi.InternalException) org.openide.util.Exceptions.attachLocalizedMessage(inex, org.openide.util.NbBundle.getMessage(org.netbeans.modules.debugger.jpda.JPDADebuggerImpl.class, "JDWPError502"));
            }
            throw inex;
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.log(Level.FINE, "FINISHED: {0}.{1} ({2}) in thread {3}", new Object[]{type, method, argVals, evaluationThread});
            }
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                throw new IllegalStateException(ieex);
            }
        }
    }

    /**
     * Auto-boxes or un-boxes arguments of a method.
     */
    static void autoboxArguments(List<Type> types, List<Value> argVals,
                                 ThreadReference evaluationThread,
                                 EvaluationContext evaluationContext) throws InvalidTypeException,
                                                                             ClassNotLoadedException,
                                                                             IncompatibleThreadStateException,
                                                                             InvocationException {
        if (types.size() != argVals.size()) {
            return ;
        }
        int n = types.size();
        for (int i = 0; i < n; i++) {
            Type t = types.get(i);
            Value v = argVals.get(i);
            if (v instanceof ObjectReference && t instanceof PrimitiveType) {
                argVals.set(i, unbox((ObjectReference) v, (PrimitiveType) t, evaluationThread, evaluationContext));
            }
            if (v instanceof PrimitiveValue && t instanceof ReferenceType) {
                argVals.set(i, box((PrimitiveValue) v, (ReferenceType) t, evaluationThread, evaluationContext));
            }
        }
    }

    /**
     * Auto-boxes or un-boxes elements of an array.
     */
    private void autoboxElements(Tree arg0, Type type, List<Value> elements,
                                 EvaluationContext evaluationContext) {
        boolean methodCalled = false;
        ThreadReference evaluationThread = null;
        try {
            if (type instanceof PrimitiveType) {
                for (int i = 0; i < elements.size(); i++) {
                    Value v = elements.get(i);
                    if (v instanceof ObjectReference) {
                        if (!methodCalled) {
                            if (!evaluationContext.canInvokeMethods()) {
                                Assert.error(arg0, "canNotInvokeMethods");
                            }
                            evaluationThread = evaluationContext.getFrame().thread();
                            if (loggerMethod.isLoggable(Level.FINE)) {
                                loggerMethod.log(Level.FINE, "STARTED : Unbox {0} in thread {1}", new Object[]{v, evaluationThread});
                            }
                            evaluationContext.methodToBeInvoked();
                            methodCalled = true;
                        }
                        elements.set(i, unbox((ObjectReference) v, (PrimitiveType) type, evaluationThread, evaluationContext));
                    }
                }
            } else if (type instanceof ReferenceType) {
                for (int i = 0; i < elements.size(); i++) {
                    Value v = elements.get(i);
                    if (v instanceof PrimitiveValue) {
                        if (!methodCalled) {
                            if (!evaluationContext.canInvokeMethods()) {
                                Assert.error(arg0, "canNotInvokeMethods");
                            }
                            evaluationThread = evaluationContext.getFrame().thread();
                            if (loggerMethod.isLoggable(Level.FINE)) {
                                loggerMethod.log(Level.FINE, "STARTED : Autobox {0} in thread {1}", new Object[]{v, evaluationThread});
                            }
                            evaluationContext.methodToBeInvoked();
                            methodCalled = true;
                        }
                        elements.set(i, box((PrimitiveValue) v, (ReferenceType) type, evaluationThread, evaluationContext));
                    }
                }
            }
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            throw new IllegalStateException(ieex);
        } finally {
            if (methodCalled) {
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.log(Level.FINE, "FINISHED: Autobox/unbox in thread {0}", evaluationThread);
                }
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    throw new IllegalStateException(ieex);
                }
            }
        }
    }

    private static void unboxMethodToBeCalled(Tree arg0, Mirror v, EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert.error(arg0, "canNotInvokeMethods");
        }
        if (loggerMethod.isLoggable(Level.FINE)) {
            loggerMethod.log(Level.FINE, "STARTED : Unbox {0} in thread {1}", new Object[]{v, evaluationContext.getFrame().thread()});
        }
        evaluationContext.methodToBeInvoked();
    }

    private static Mirror unboxIfCan(Tree arg0, ObjectReference r, EvaluationContext evaluationContext) {
        String name = ((ReferenceType) r.type()).name();
        boolean methodCalled = false;
        try {
            if (name.equals(Boolean.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "booleanValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Byte.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "byteValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Character.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "charValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Short.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "shortValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Integer.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "intValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Long.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "longValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Float.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "floatValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            if (name.equals(Double.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "doubleValue", evaluationContext.getFrame().thread(), evaluationContext);
            }
            return r;
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            throw new IllegalStateException(ieex);
        } finally {
            if (methodCalled) {
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.log(Level.FINE, "FINISHED: unbox in thread {0}", evaluationContext.getFrame().thread());
                }
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    throw new IllegalStateException(ieex);
                }
            }
        }
    }

    public static Value unbox(ObjectReference val, PrimitiveType type,
                              ThreadReference thread,
                              EvaluationContext context) throws InvalidTypeException,
                                                                ClassNotLoadedException,
                                                                IncompatibleThreadStateException,
                                                                InvocationException {
        ReferenceType rt = val.referenceType();
        String classType = rt.name();
        PrimitiveValue pv;
        if (classType.equals("java.lang.Boolean")) {
            pv = invokeUnboxingMethod(val, "booleanValue", thread, context);
        } else if (classType.equals("java.lang.Byte")) {
            pv = invokeUnboxingMethod(val, "byteValue", thread, context);
        } else if (classType.equals("java.lang.Character")) {
            pv = invokeUnboxingMethod(val, "charValue", thread, context);
        } else if (classType.equals("java.lang.Short")) {
            pv = invokeUnboxingMethod(val, "shortValue", thread, context);
        } else if (classType.equals("java.lang.Integer")) {
            pv = invokeUnboxingMethod(val, "intValue", thread, context);
        } else if (classType.equals("java.lang.Long")) {
            pv = invokeUnboxingMethod(val, "longValue", thread, context);
        } else if (classType.equals("java.lang.Float")) {
            pv = invokeUnboxingMethod(val, "floatValue", thread, context);
        } else if (classType.equals("java.lang.Double")) {
            pv = invokeUnboxingMethod(val, "doubleValue", thread, context);
        //throw new RuntimeException("Invalid type while unboxing: " + type.signature());    // never happens
        } else {
            return val;
        }
        VirtualMachine vm = pv.virtualMachine();
        if (type instanceof BooleanType && !(pv instanceof BooleanValue)) {
            return vm.mirrorOf(pv.booleanValue());
        }
        if (type instanceof ByteType && !(pv instanceof ByteValue)) {
            return vm.mirrorOf(pv.byteValue());
        }
        if (type instanceof CharType && !(pv instanceof CharValue)) {
            return vm.mirrorOf(pv.charValue());
        }
        if (type instanceof ShortType && !(pv instanceof ShortValue)) {
            return vm.mirrorOf(pv.shortValue());
        }
        if (type instanceof IntegerType && !(pv instanceof IntegerValue)) {
            return vm.mirrorOf(pv.intValue());
        }
        if (type instanceof LongType && !(pv instanceof LongValue)) {
            return vm.mirrorOf(pv.longValue());
        }
        if (type instanceof FloatType && !(pv instanceof FloatValue)) {
            return vm.mirrorOf(pv.floatValue());
        }
        if (type instanceof DoubleType && !(pv instanceof DoubleValue)) {
            return vm.mirrorOf(pv.doubleValue());
        }
        return pv;
    }

    public static ReferenceType adjustBoxingType(ReferenceType type, PrimitiveType primitiveType,
                                                  EvaluationContext evaluationContext) {
        Class typeClass = null;
        if (primitiveType instanceof BooleanType) {
            typeClass = Boolean.class;
        } else
        if (primitiveType instanceof ByteType) {
            typeClass = Byte.class;
        } else
        if (primitiveType instanceof CharType) {
            typeClass = Character.class;
        } else
        if (primitiveType instanceof ShortType) {
            typeClass = Short.class;
        } else
        if (primitiveType instanceof IntegerType) {
            typeClass = Integer.class;
        } else
        if (primitiveType instanceof LongType) {
            typeClass = Long.class;
        } else
        if (primitiveType instanceof FloatType) {
            typeClass = Float.class;
        } else
        if (primitiveType instanceof DoubleType) {
            typeClass = Double.class;
        }
        if (typeClass != null && evaluationContext != null && evaluationContext.getVMCache() != null) {
            type = evaluationContext.getVMCache().getClass(typeClass.getName());
        }
        return type;
    }

    private static final Set<String> PRIMITIVE_CLASS_NAMES = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList(new String[] {
                java.lang.Boolean.class.getName(),
                java.lang.Byte.class.getName(),
                java.lang.Character.class.getName(),
                java.lang.Short.class.getName(),
                java.lang.Integer.class.getName(),
                java.lang.Long.class.getName(),
                java.lang.Float.class.getName(),
                java.lang.Double.class.getName(),
            })));

    public static Value box(PrimitiveValue v, ReferenceType type,
                            ThreadReference thread,
                            EvaluationContext evaluationContext) throws InvalidTypeException,
                                                                        ClassNotLoadedException,
                                                                        IncompatibleThreadStateException,
                                                                        InvocationException {
        try {
            Method constructor = null;
            String classType = type.name();
            if (!PRIMITIVE_CLASS_NAMES.contains(classType)) {
                type = adjustBoxingType(type, (PrimitiveType) v.type(), evaluationContext);
            } else {
                VirtualMachine vm = type.virtualMachine();
                if (classType.equals("java.lang.Boolean") && (v instanceof ArtificialMirror || !(v instanceof BooleanValue))) {
                    v = vm.mirrorOf(v.booleanValue());
                } else if (classType.equals("java.lang.Byte") && (v instanceof ArtificialMirror || !(v instanceof ByteValue))) {
                    v = vm.mirrorOf(v.byteValue());
                } else if (classType.equals("java.lang.Character") && (v instanceof ArtificialMirror || !(v instanceof CharValue))) {
                    v = vm.mirrorOf(v.charValue());
                } else if (classType.equals("java.lang.Short") && (v instanceof ArtificialMirror || !(v instanceof ShortValue))) {
                    v = vm.mirrorOf(v.shortValue());
                } else if (classType.equals("java.lang.Integer") && (v instanceof ArtificialMirror || !(v instanceof IntegerValue))) {
                    v = vm.mirrorOf(v.intValue());
                } else if (classType.equals("java.lang.Long") && (v instanceof ArtificialMirror || !(v instanceof LongValue))) {
                    v = vm.mirrorOf(v.longValue());
                } else if (classType.equals("java.lang.Float") && (v instanceof ArtificialMirror || !(v instanceof FloatValue))) {
                    v = vm.mirrorOf(v.floatValue());
                } else if (classType.equals("java.lang.Double") && (v instanceof ArtificialMirror || !(v instanceof DoubleValue))) {
                    v = vm.mirrorOf(v.doubleValue());
                }
            }
            List<Method> methods = type.methodsByName("<init>");
            String signature = "("+v.type().signature()+")";
            for (Method method : methods) {
                if (!method.isAbstract() && equalMethodSignatures(method.signature(), signature)) {
                    constructor = method;
                }
            }
            if (constructor == null) {
                throw new RuntimeException("No constructor "+type+" "+signature);
            }
            if (evaluationContext != null) {
                evaluationContext.methodToBeInvoked();
            }
            ObjectReference o = ((ClassType) type).newInstance(thread, constructor, Arrays.asList(new Value[] { v }), ObjectReference.INVOKE_SINGLE_THREADED);
            //evaluationContext.disableCollectionOf(o); - Not necessary, new objects are not collected!
            return o;
        } catch (InvalidTypeException itex) {
            throw itex;
        } catch (ClassNotLoadedException cnlex) {
            throw cnlex;
        } catch (IncompatibleThreadStateException itsex) {
            throw itsex;
        } catch (InvocationException iex) {
            throw iex;
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking boxing method", e);
        } finally {
            if (evaluationContext != null) {
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    throw new IllegalStateException(ieex);
                }
            }
        }
    }

    private static PrimitiveValue invokeUnboxingMethod(ObjectReference reference, String methodName,
                                                       ThreadReference thread,
                                                       EvaluationContext evaluationContext) throws InvalidTypeException,
                                                                                                   ClassNotLoadedException,
                                                                                                   IncompatibleThreadStateException,
                                                                                                   InvocationException {
        Method toCall = (Method) reference.referenceType().methodsByName(methodName).get(0);
        if (evaluationContext != null) {
            evaluationContext.methodToBeInvoked();
        }
        try {
            return (PrimitiveValue) reference.invokeMethod(thread, toCall, new ArrayList<Value>(0), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (InvalidTypeException itex) {
            throw itex;
        } catch (ClassNotLoadedException cnlex) {
            throw cnlex;
        } catch (IncompatibleThreadStateException itsex) {
            throw itsex;
        } catch (InvocationException iex) {
            throw iex;
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking unboxing method", e);
        } finally {
            if (evaluationContext != null) {
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    throw new IllegalStateException(ieex);
                }
            }
        }
    }

    private boolean isEqual(Mirror left, Mirror right, EvaluationContext evaluationContext) {
        if (left instanceof ObjectReference) {
            return left.equals(right);
        }
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) {
            return false;
        }
        if ((left instanceof BooleanValue) && (right instanceof BooleanValue)) {
            boolean op1 = ((BooleanValue) left).booleanValue();
            boolean op2 = ((BooleanValue) right).booleanValue();
            return op1 == op2;
        }
        boolean isLeftNumeric = left instanceof PrimitiveValue && !(left instanceof BooleanValue);
        boolean isRightNumeric = right instanceof PrimitiveValue && !(right instanceof BooleanValue);
        if (isLeftNumeric && isRightNumeric) {
            if ((left instanceof DoubleValue) || (right instanceof DoubleValue)) {
                double l = ((PrimitiveValue) left).doubleValue();
                double r = ((PrimitiveValue) right).doubleValue();
                return l == r;
            }
            if ((left instanceof FloatValue) || (right instanceof FloatValue)) {
                float l = ((PrimitiveValue) left).floatValue();
                float r = ((PrimitiveValue) right).floatValue();
                return l == r;
            }
            long l = ((PrimitiveValue) left).longValue();
            long r = ((PrimitiveValue) right).longValue();
            return l == r;
        }
        return false; // [TODO]
    }

    private String toString(Tree arg0, Mirror v, EvaluationContext evaluationContext) {
        if (v instanceof PrimitiveValue) {
            PrimitiveValue pv = (PrimitiveValue) v;
            PrimitiveType t = (PrimitiveType) pv.type();
            if (t instanceof ByteType) {
                return Byte.toString(pv.byteValue());
            }
            if (t instanceof BooleanType) {
                return Boolean.toString(pv.booleanValue());
            }
            if (t instanceof CharType) {
                return Character.toString(pv.charValue());
            }
            if (t instanceof ShortType) {
                return Short.toString(pv.shortValue());
            }
            if (t instanceof IntegerType) {
                return Integer.toString(pv.intValue());
            }
            if (t instanceof LongType) {
                return Long.toString(pv.longValue());
            }
            if (t instanceof FloatType) {
                return Float.toString(pv.floatValue());
            }
            if (t instanceof DoubleType) {
                return Double.toString(pv.doubleValue());
            }
            throw new IllegalStateException("Unknown primitive type: "+t);
        }
        if (v == null) {
            return "" + null;
        }
        ObjectReference ov = (ObjectReference) v;
        if (ov instanceof ArrayReference) {
            return "#" + ov.uniqueID() +
                " " + ov.type().name() +
                "(length=" + ((ArrayReference) ov).length() + ")";
        }
        if (ov instanceof StringReference) {
            return ((StringReference) ov).value();
        }
        // Call toString() method:
        List<? extends TypeMirror> typeArguments = Collections.emptyList();
        Method method;
        try {
            method = getConcreteMethod((ReferenceType) ov.type(), "toString", typeArguments);
        } catch (UnsuitableArgumentsException uaex) {
            throw new IllegalStateException(uaex);
        }
        ((ClassType) ov.type()).methodsByName("toString");
        List<Value> argVals = Collections.emptyList();
        Value sv = invokeMethod(arg0, method, false, null, ov, argVals, evaluationContext, false);
        if (sv instanceof StringReference) {
            return ((StringReference) sv).value();
        } else if (sv == null) {
            return null;
        } else {
            return "Result of toString() call on "+ov+" is not a String, but: "+sv; // NOI18N - should not ever happen.
        }
    }

    private Value mirrorOf(VirtualMachine vm, Object value) {
        if (value instanceof Boolean) {
            return new BooleanVal(vm, ((Boolean)value).booleanValue());
        } else if (value instanceof Character) {
            return new CharVal(vm, ((Character)value).charValue());
        } else if (value instanceof Byte) {
            return new ByteVal(vm, ((Byte)value).byteValue());
        } else if (value instanceof Integer) {
            return new IntVal(vm, ((Integer)value).intValue());
        } else if (value instanceof Short) {
            return new ShortVal(vm, ((Short)value).shortValue());
        } else if (value instanceof Long) {
            return new LongVal(vm, ((Long)value).longValue());
        } else if (value instanceof Float) {
            return new FloatVal(vm, ((Float)value).floatValue());
        } else if (value instanceof Double) {
            return new DoubleVal(vm, ((Double)value).doubleValue());
        } else if (value instanceof String) {
            return vm.mirrorOf((String) value);
        }
        return null;
    }

    /**
     * Find a class by it's name. If the class is not found loaded in the virtual
     * machine, an attempt to load it is made.
     * 
     * @param name The class name
     * @return Found or loaded ReferenceType
     */
    private static ReferenceType getOrLoadClass(VirtualMachine vm, String name,
                                                EvaluationContext evaluationContext) {
        List<ReferenceType> types = vm.classesByName(name);
        if (types.size() > 0) {
            if (types.size() == 1) {
                return types.get(0);
            }
            if (evaluationContext != null) {
                ClassLoaderReference contextClassLoader = evaluationContext.getFrame().location().declaringType().classLoader();
                // Return the class which was loaded by the context class loader
                for (ReferenceType type : types) {
                    ClassLoaderReference typeClassLoader = type.classLoader();
                    if (contextClassLoader == null && typeClassLoader == null ||
                        contextClassLoader != null && contextClassLoader.equals(typeClassLoader)) {
                        return type;
                    }
                }
            }
            // No type was loaded by our context classloader, select the preferred one:
            try {
                ReferenceType preferedType = JPDAUtils.getPreferredReferenceType(types, null);
                if (preferedType != null) {
                    return preferedType;
                }
            } catch (VMDisconnectedExceptionWrapper ex) {
                throw ex.getCause();
            }
            // No preferred, just take the first one:
            return types.get(0);
        }
        // DO NOT TRY TO LOAD THE CLASS ON JDK 5 AND OLDER!
        // See http://www.netbeans.org/issues/show_bug.cgi?id=50315
        // The bug is in JVMTI code, therefore the target VM must be JDK 6 at least.
        String targetVersion = vm.version();
        if (targetVersion.startsWith("1.5") || // NOI18N
            targetVersion.startsWith("1.4") || // NOI18N
            targetVersion.startsWith("1.3") || // NOI18N
            targetVersion.startsWith("1.2") || // NOI18N
            targetVersion.startsWith("1.1") || // NOI18N
            targetVersion.startsWith("1.0")) { // NOI18N
            return null;
        }

        ClassType clazz = (ClassType) evaluationContext.getVMCache().getClass(Class.class.getName());
        if (clazz == null) {
            return null;
        }
        evaluationContext.methodToBeInvoked();
        try {
            com.sun.jdi.Method forName = clazz.concreteMethodByName("forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
            StackFrame frame = evaluationContext.getFrame();
            ClassLoaderReference executingClassloader = frame.location().declaringType().classLoader();
            List<Value> args = new ArrayList<>();
            StringReference className = createStringMirrorWithDisabledCollection(name, vm, evaluationContext);
            args.add(className);
            args.add(vm.mirrorOf(true));
            args.add(executingClassloader);
            ClassObjectReference cor = (ClassObjectReference) clazz.invokeMethod(frame.thread(), forName, args, ObjectReference.INVOKE_SINGLE_THREADED);
            //evaluationContext.disableCollectionOf(cor); - Not necessary, values returned from methods are not collected!
            return cor.reflectedType();
        } catch (IncompatibleThreadStateException itsex) {
            String message = Bundle.MSG_IncompatibleThreadStateMessage();
            InvalidExpressionException ieex = new InvalidExpressionException(message, itsex);
            throw new IllegalStateException(ieex);
        } catch (Exception ex) {
            return null;
        } finally {
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                ieex.initCause(itsex);
                throw new IllegalStateException(ieex);
            }
        }
    }

    private static StringReference createStringMirrorWithDisabledCollection(String s, VirtualMachine vm, EvaluationContext evaluationContext) {
        StringReference sr;
        do {
            try {
                sr = vm.mirrorOf(s);
            } catch (UnsupportedOperationException e) {
                Assert.error(null, "unsupportedStringCreation");
                return null;
            }
            try {
                evaluationContext.disableCollectionOf(sr);
            } catch (ObjectCollectedException oce) {
                sr = null; // Already collected! Create a new value and try again...
            }
        } while (sr == null);
        return sr;
    }

    private static ArrayReference createArrayMirrorWithDisabledCollection(ArrayType arrayType, int dimension, EvaluationContext evaluationContext) {
        ArrayReference array;
        do {
            array = arrayType.newInstance(dimension);
            try {
                evaluationContext.disableCollectionOf(array);
            } catch (ObjectCollectedException oce) {
                array = null; // Already collected! Create a new value and try again...
            }
        } while (array == null);
        return array;
    }

    private void reportCannotApplyOperator(BinaryTree binaryTree) {
        Assert.error(binaryTree, "cannotApplyOperator", binaryTree.toString());
    }

    private ReferenceType findPreferedType(ReferenceType type, String className) {
        ReferenceType t = type;
        try {
            do {
                String name = ReferenceTypeWrapper.name(t);
                name = name.replace('$', '.');
                if (name.equals(className)) {
                    return t;
                }
                if (t instanceof ClassType) {
                    t = ClassTypeWrapper.superclass((ClassType) t);
                } else
                if (t instanceof InterfaceType) {
                    List<InterfaceType> superinterfaces = InterfaceTypeWrapper.superinterfaces((InterfaceType) t);
                    if (superinterfaces.isEmpty()) {
                        t = null;
                    } else {
                        t = superinterfaces.get(0);
                        for (int i = 1; i < superinterfaces.size(); i++) {
                            ReferenceType pt = findPreferedType(superinterfaces.get(i), className);
                            if (pt != null) {
                                return pt;
                            }
                        }
                    }
                } else {
                    t = null;
                }
            } while (t != null);
        } catch (InternalExceptionWrapper iew) {
        } catch (VMDisconnectedExceptionWrapper vmdew) {
        } catch (ObjectCollectedExceptionWrapper ocew) {
        } catch (ClassNotPreparedExceptionWrapper cnpew) {
        }
        return null;
    }

    // *************************************************************************
    // inner classes
    // *************************************************************************

    private static final class UnsuitableArgumentsException extends Exception {
        public UnsuitableArgumentsException() {}
    }

    abstract static class ArtificialMirror implements Mirror {

        @Override
        public VirtualMachine virtualMachine() {
            return null;
        }

        public abstract Mirror getVMMirror();

    }

    private abstract static class CommandMirror extends ArtificialMirror {

    }

    // used to drive code execution after 'break' statement is encountered
    private static final class Break extends CommandMirror {

        @Override
        public String toString() {
            return "break"; // NOI18N
        }

        @Override
        public Mirror getVMMirror() {
            return null;
        }

    }

    // used to drive code execution after 'continue' statement is encountered
    private static final class Continue extends CommandMirror {

        @Override
        public String toString() {
            return "continue"; // NOI18N
        }

        @Override
        public Mirror getVMMirror() {
            return null;
        }

    }

    private static final class Return extends CommandMirror {

        private Mirror value;

        Return (Mirror value) {
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return (value instanceof ArtificialMirror) ?
                ((ArtificialMirror)value).getVMMirror() : value;
        }

        @Override
        public String toString() {
            return "return"; // NOI18N
        }

    }

    private static class BooleanVal extends ArtificialMirror implements BooleanValue {

        private boolean value;
        private VirtualMachine vm;

        BooleanVal(VirtualMachine vm, boolean value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public boolean value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value;
        }

        @Override
        public byte byteValue() {
            return (byte)(value ? 1 : 0);
        }

        @Override
        public char charValue() {
            return (char)byteValue();
        }

        @Override
        public short shortValue() {
            return (short)(value ? 1 : 0);
        }

        @Override
        public int intValue() {
            return value ? 1 : 0;
        }

        @Override
        public long longValue() {
            return value ? 1 : 0;
        }

        @Override
        public float floatValue() {
            return value ? 1 : 0;
        }

        @Override
        public double doubleValue() {
            return value ? 1 : 0;
        }

        @Override
        public Type type() {
            return vm.mirrorOf(true).type();
        }

    } // BoolVal

    private static class ByteVal extends ArtificialMirror implements ByteValue {

        private byte value;
        private VirtualMachine vm;

        ByteVal(VirtualMachine vm, byte value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public byte value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf((byte)0).type();
        }

        @Override
        public int compareTo(ByteValue o) {
            return value - ((ByteValue)o).value();
        }

    } // ByteVal

    private static class ShortVal extends ArtificialMirror implements ShortValue {

        private short value;
        private VirtualMachine vm;

        ShortVal(VirtualMachine vm, short value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public short value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf((short)0).type();
        }

        @Override
        public int compareTo(ShortValue o) {
            return value - ((ShortValue)o).value();
        }

    } // ShortVal

    private static class IntVal extends ArtificialMirror implements IntegerValue {

        private int value;
        private VirtualMachine vm;

        IntVal(VirtualMachine vm, int value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public int value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf(0).type();
        }

        @Override
        public int compareTo(IntegerValue o) {
            return value - ((IntegerValue)o).value();
        }

    } // IntVal

    private static class LongVal extends ArtificialMirror implements LongValue {

        private long value;
        private VirtualMachine vm;

        LongVal(VirtualMachine vm, long value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public long value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return (int)value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf((long)0).type();
        }

        @Override
        public int compareTo(LongValue o) {
            return (int)(value - ((LongValue) o).value());
        }

    } // LongVal

    private static class CharVal extends ArtificialMirror implements CharValue {

        private char value;
        private VirtualMachine vm;

        CharVal(VirtualMachine vm, char value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public char value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf(' ').type();
        }

        @Override
        public int compareTo(CharValue o) {
            return value - ((CharValue)o).value();
        }

    } // CharVal

    private static class FloatVal extends ArtificialMirror implements FloatValue {

        private float value;
        private VirtualMachine vm;

        FloatVal(VirtualMachine vm, float value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public float value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return (int)value;
        }

        @Override
        public long longValue() {
            return (long)value;
        }

        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf((float) 0).type();
        }

        @Override
        public int compareTo(FloatValue o) {
            return (int)(value - ((FloatValue) o).value());
        }

    } // FloatVal

    private static class DoubleVal extends ArtificialMirror implements DoubleValue {

        private double value;
        private VirtualMachine vm;

        DoubleVal(VirtualMachine vm, double value) {
            this.vm = vm;
            this.value = value;
        }

        @Override
        public Mirror getVMMirror() {
            return vm.mirrorOf(value);
        }

        @Override
        public double value() {
            return value;
        }

        @Override
        public boolean booleanValue() {
            return value != 0;
        }

        @Override
        public byte byteValue() {
            return (byte)value;
        }

        @Override
        public char charValue() {
            return (char)value;
        }

        @Override
        public short shortValue() {
            return (short)value;
        }

        @Override
        public int intValue() {
            return (int)value;
        }

        @Override
        public long longValue() {
            return (long)value;
        }

        @Override
        public float floatValue() {
            return (float)value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public Type type() {
            return vm.mirrorOf((double) 0).type();
        }

        @Override
        public int compareTo(DoubleValue o) {
            return (int)(value - ((DoubleValue) o).value());
        }

    } // DoubleVal
    
    private static final class NoValue implements Value {
        
        public NoValue() {}

        @Override
        public Type type() {
            throw new UnsupportedOperationException("No value.");
        }

        @Override
        public VirtualMachine virtualMachine() {
            throw new UnsupportedOperationException("No value.");
        }
        
    }
    
    private static final class IntersectionType extends ArtificialMirror implements ReferenceType {
        
        private final ReferenceType[] types;
        
        public IntersectionType(ReferenceType[] types) {
            this.types = types;
        }

        @Override
        public String name() {
            return types[0].name();
        }
        
        @Override
        public String signature() {
            return types[0].signature();
        }

        @Override
        public Mirror getVMMirror() {
            return types[0];
        }
        
        public Type[] getTypes() {
            return types;
        }

        @Override
        public String genericSignature() {
            return types[0].genericSignature();
        }

        @Override
        public ClassLoaderReference classLoader() {
            return types[0].classLoader();
        }

        @Override
        public String sourceName() throws AbsentInformationException {
            return types[0].sourceName();
        }

        @Override
        public List<String> sourceNames(String string) throws AbsentInformationException {
            return types[0].sourceNames(string);
        }

        @Override
        public List<String> sourcePaths(String string) throws AbsentInformationException {
            return types[0].sourcePaths(string);
        }

        @Override
        public String sourceDebugExtension() throws AbsentInformationException {
            return types[0].sourceDebugExtension();
        }

        @Override
        public boolean isStatic() {
            boolean isStatic = true;
            for (ReferenceType t : types) {
                if (!t.isStatic()) {
                    isStatic = false;
                    break;
                }
            }
            return isStatic;
        }

        @Override
        public boolean isAbstract() {
            return true;
        }

        @Override
        public boolean isFinal() {
            boolean isFinal = true;
            for (ReferenceType t : types) {
                if (!t.isFinal()) {
                    isFinal = false;
                    break;
                }
            }
            return isFinal;
        }

        @Override
        public boolean isPrepared() {
            boolean isPrepared = true;
            for (ReferenceType t : types) {
                if (!t.isPrepared()) {
                    isPrepared = false;
                    break;
                }
            }
            return isPrepared;
        }

        @Override
        public boolean isVerified() {
            boolean isVerified = true;
            for (ReferenceType t : types) {
                if (!t.isVerified()) {
                    isVerified = false;
                    break;
                }
            }
            return isVerified;
        }

        @Override
        public boolean isInitialized() {
            boolean isInitialized = true;
            for (ReferenceType t : types) {
                if (!t.isInitialized()) {
                    isInitialized = false;
                    break;
                }
            }
            return isInitialized;
        }

        @Override
        public boolean failedToInitialize() {
            boolean failedToInitialize = false;
            for (ReferenceType t : types) {
                if (t.failedToInitialize()) {
                    failedToInitialize = true;
                    break;
                }
            }
            return failedToInitialize;
        }

        @Override
        public List<Field> fields() {
            List<Field> fields = new ArrayList<Field>();
            for (ReferenceType t : types) {
                fields.addAll(t.fields());
            }
            return Collections.unmodifiableList(fields);
        }

        @Override
        public List<Field> visibleFields() {
            List<Field> visibleFields = new ArrayList<Field>();
            for (ReferenceType t : types) {
                visibleFields.addAll(t.visibleFields());
            }
            return Collections.unmodifiableList(visibleFields);
        }

        @Override
        public List<Field> allFields() {
            List<Field> allFields = new ArrayList<Field>();
            for (ReferenceType t : types) {
                allFields.addAll(t.allFields());
            }
            return Collections.unmodifiableList(allFields);
        }

        @Override
        public Field fieldByName(String string) {
            for (ReferenceType t : types) {
                Field field = t.fieldByName(string);
                if (field != null) {
                    return field;
                }
            }
            return null;
        }

        @Override
        public List<Method> methods() {
            List<Method> methods = new ArrayList<Method>();
            for (ReferenceType t : types) {
                methods.addAll(t.methods());
            }
            return Collections.unmodifiableList(methods);
        }

        @Override
        public List<Method> visibleMethods() {
            List<Method> visibleMethods = new ArrayList<Method>();
            for (ReferenceType t : types) {
                visibleMethods.addAll(t.visibleMethods());
            }
            return Collections.unmodifiableList(visibleMethods);
        }

        @Override
        public List<Method> allMethods() {
            List<Method> allMethods = new ArrayList<Method>();
            for (ReferenceType t : types) {
                allMethods.addAll(t.allMethods());
            }
            return Collections.unmodifiableList(allMethods);
        }

        @Override
        public List<Method> methodsByName(String string) {
            List<Method> methodsByName = new ArrayList<Method>();
            for (ReferenceType t : types) {
                methodsByName.addAll(t.methodsByName(string));
            }
            return Collections.unmodifiableList(methodsByName);
        }

        @Override
        public List<Method> methodsByName(String string, String string1) {
            List<Method> methodsByName = new ArrayList<Method>();
            for (ReferenceType t : types) {
                methodsByName.addAll(t.methodsByName(string, string1));
            }
            return Collections.unmodifiableList(methodsByName);
        }

        @Override
        public List<ReferenceType> nestedTypes() {
            return Collections.<ReferenceType>emptyList();
        }

        @Override
        public Value getValue(Field field) {
            String name = field.name();
            for (ReferenceType t : types) {
                if (field.equals(t.fieldByName(name))) {
                    return t.getValue(field);
                }
            }
            return types[0].getValue(field); // Likely throws some appropriate error.
        }

        @Override
        public Map<Field, Value> getValues(List<? extends Field> list) {
            List<Field>[] listByTypes = new List[types.length];
            for (int i = 0; i < types.length; i++) {
                listByTypes[i] = new ArrayList();
                ReferenceType t = types[i];
                for (Field field : list) {
                    String name = field.name();
                    if (field.equals(t.fieldByName(name))) {
                        listByTypes[i].add(field);
                    }
                }
            }
            Map<Field, Value> map = null;
            Map<Field, Value> singleMap = null;
            for (int i = 0; i < types.length; i++) {
                if (!listByTypes[i].isEmpty()) {
                    Map<Field, Value> tmap = types[i].getValues(listByTypes[i]);
                    if (singleMap == null) {
                        singleMap = tmap;
                    } else {
                        if (map == null) {
                            map = new HashMap<>(list.size());
                            map.putAll(singleMap);
                        }
                        map.putAll(tmap);
                    }
                }
            }
            if (map != null) {
                return map;
            } else {
                return singleMap;
            }
        }

        @Override
        public ClassObjectReference classObject() {
            return types[0].classObject();
        }

        @Override
        public List<Location> allLineLocations() throws AbsentInformationException {
            throw new AbsentInformationException("IntersectionType");
        }

        @Override
        public List<Location> allLineLocations(String string, String string1) throws AbsentInformationException {
            throw new AbsentInformationException("IntersectionType");
        }

        @Override
        public List<Location> locationsOfLine(int i) throws AbsentInformationException {
            throw new AbsentInformationException("IntersectionType");
        }

        @Override
        public List<Location> locationsOfLine(String string, String string1, int i) throws AbsentInformationException {
            throw new AbsentInformationException("IntersectionType");
        }

        @Override
        public List<String> availableStrata() {
            List<String> mstrata = null;
            List<String> strata = null;
            for (ReferenceType t : types) {
                List<String> tstrata = t.availableStrata();
                if (strata == null) {
                    strata = tstrata;
                } else if (!tstrata.containsAll(strata)) {
                    if (mstrata == null) {
                        mstrata = new ArrayList<String>(strata);
                    }
                    mstrata.retainAll(tstrata);
                }
            }
            if (mstrata != null) {
                return mstrata;
            } else {
                return strata;
            }
        }

        @Override
        public String defaultStratum() {
            return types[0].defaultStratum();
        }

        @Override
        public List<ObjectReference> instances(long l) {
            return Collections.<ObjectReference>emptyList();
        }

        @Override
        public int majorVersion() {
            return types[0].majorVersion();
        }

        @Override
        public int minorVersion() {
            return types[0].minorVersion();
        }

        @Override
        public int constantPoolCount() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] constantPool() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IntersectionType)) {
                return false;
            }
            IntersectionType it = (IntersectionType) obj;
            return Arrays.equals(types, it.types);
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (ReferenceType t : types) {
                h += t.hashCode();
            }
            return h;
        }
        
        @Override
        public int compareTo(ReferenceType o) {
            if (!(o instanceof IntersectionType)) {
                return +1;
            }
            IntersectionType it = (IntersectionType) o;
            if (types.length != it.types.length) {
                return types.length - it.types.length;
            }
            if (Arrays.equals(types, it.types)) {
                return 0;
            }
            int d = 0;
            int nd = 0;
            for (int i = 0; i < types.length && i < it.types.length; i++) {
                d += types[i].compareTo(it.types[i]);
                if (nd == 0 && d != 0) {
                    nd = d;
                }
            }
            if (d == 0) {
                // Must not return 0 when not equal.
                return nd;
            } else {
                return d;
            }
        }

        @Override
        public int modifiers() {
            int modifiers = -1;
            for (ReferenceType t : types) {
                modifiers &= t.modifiers();
            }
            return modifiers;
        }

        @Override
        public boolean isPrivate() {
            return true;
        }

        @Override
        public boolean isPackagePrivate() {
            return false;
        }

        @Override
        public boolean isProtected() {
            return false;
        }

        @Override
        public boolean isPublic() {
            return false;
        }

    }

}
