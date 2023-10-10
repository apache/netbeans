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

package org.netbeans.modules.j2ee.core.api.support.java;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 * <code>GenerationUtils</code> is a helper class for creating classes,
 * methods, variables, annotations and types using the Java source model.
 *
 * @author Andrei Badea
 */
public final class GenerationUtils {

    // PENDING use CharSequence instead of String where possible

    /**
     * The templates for regular Java class and interface.
     */
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N

    private final WorkingCopy copy;

    // <editor-fold desc="Constructors and factory methods">

    private GenerationUtils(WorkingCopy copy) {
        this.copy = copy;
    }

    /**
     * Creates a new instance of <code>GenerationUtils</code>.
     *
     * @param  copy a <code>WorkingCopy</code>. It must be in {@link Phase#RESOLVED}.
     * @return a new instance of <code>GenerationUtils</code>.
     */
    public static GenerationUtils newInstance(WorkingCopy copy) {
        Parameters.notNull("copy", copy); // NOI18N
        return new GenerationUtils(copy);
    }

    // </editor-fold>

    // <editor-fold desc="Public static methods">

    /**
     * Creates a new Java class based on the default template for classes.
     *
     * @param  targetFolder the folder the new class should be created in;
     *         cannot be null.
     * @param  className the name of the new class (a valid Java identifier);
     *         cannot be null.
     * @param  javadoc the new class's Javadoc; can be null.
     * @return the FileObject for the new Java class; never null.
     * @throws IOException if an error occurred while creating the class.
     */
    public static FileObject createClass(FileObject targetFolder, String className, final String javadoc) throws IOException{
        return createClass(CLASS_TEMPLATE, targetFolder, className, javadoc, Collections.<String, Object>emptyMap());
    }

    /**
     * Creates a new Java class based on the default template for interfaces.
     *
     * @param  targetFolder the folder the new interface should be created in;
     *         cannot be null.
     * @param  interfaceName the name of the new interface (a valid Java identifier);
     *         cannot be null.
     * @param  javadoc the new interface's Javadoc; can be null.
     * @return the FileObject for the new Java interface; never null.
     * @throws IOException if an error occurred while creating the class.
     */
    public static FileObject createInterface(FileObject targetFolder, String interfaceName, final String javadoc) throws IOException{
        return createClass(INTERFACE_TEMPLATE, targetFolder, interfaceName, javadoc, Collections.<String, Object>emptyMap());
    }

    /**
     * Creates a new Java class based on the provided template.
     *
     * @param  template the template to base the new class on.
     * @param  targetFolder the folder the new class should be created in;
     *         cannot be null.
     * @param  className the name of the new class (a valid Java identifier);
     *         cannot be null.
     * @param  javadoc the new class's Javadoc; can be null.
     * @param  parameters map of named objects that are going to be used when creating the new object
     * @return the FileObject for the new Java class; never null.
     * @throws IOException if an error occurred while creating the class.
     */
    public static FileObject createClass(String template, FileObject targetFolder, String className, final String javadoc, Map<String, ? extends Object> parameters) throws IOException {
        Parameters.notNull("template", template); // NOI18N
        Parameters.notNull("targetFolder", targetFolder); // NOI18N
        Parameters.javaIdentifier("className", className); // NOI18N

        FileObject classFO = createDataObjectFromTemplate(template, targetFolder, className, parameters).getPrimaryFile();
        // JavaSource javaSource = JavaSource.forFileObject(classFO);
        // final boolean[] commit = { false };
        // ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
        //     public void run(WorkingCopy copy) throws IOException {
        //         GenerationUtils genUtils = GenerationUtils.newInstance(copy);
        //         if (javadoc != null) {
        //             genUtils.setJavadoc(copy, mainType, javadoc);
        //         }
        //     }
        // });
        // if (commit[0]) {
        //     modification.commit();
        // }

        return classFO;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Creates a data object from a given template path in the system
     * file system.
     *
     * @return the <code>DataObject</code> of the newly created file.
     * @throws IOException if an error occured while creating the file.
     */
    private static DataObject createDataObjectFromTemplate(String template, FileObject targetFolder, String targetName,
            Map<String,? extends Object> parameters) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;

        FileObject templateFO = FileUtil.getConfigFile(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        return templateDO.createFromTemplate(dataFolder, targetName, parameters);
    }

    // </editor-fold>

    // <editor-fold desc="Public methods">

    public Tree createType(String typeName, TypeElement scope) {
        TreeMaker make = getTreeMaker();
        TypeKind primitiveTypeKind = null;
        if ("boolean".equals(typeName)) {           // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) {        // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        } else if ("void".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        }
        if (primitiveTypeKind != null) {
            return getTreeMaker().PrimitiveType(primitiveTypeKind);
        }
        Tree typeTree = tryCreateQualIdent(typeName);
        if (typeTree == null) {
            // XXX does not handle imports; temporary until issue 102149 is fixed
            TypeMirror typeMirror = copy.getTreeUtilities().parseType(typeName, scope);
            if ( typeMirror == null || typeMirror.getKind() == TypeKind.ERROR ){
                typeTree = getTreeMaker().QualIdent( typeName );
            }
            else {
                typeTree = make.Type(typeMirror);
            }
        }
        return typeTree;
    }

    public ModifiersTree createModifiers(Modifier modifier) {
        return getTreeMaker().Modifiers(EnumSet.of(modifier), Collections.<AnnotationTree>emptyList());
    }

    /**
     * Creates a new annotation.
     *
     * @param  annotationType the fully-qualified name of the annotation type;
     *         cannot be null.
     * @return the new annotation; never null.
     */
    public AnnotationTree createAnnotation(String annotationType) {
        Parameters.notNull("annotationType", annotationType); // NOI18N

        return createAnnotation(annotationType, Collections.<ExpressionTree>emptyList());
    }

    /**
     * Creates a new annotation.
     *
     * @param  annotationType the fully-qualified name of the annotation type;
     *         cannot be null.
     *         <code>java.lang.SuppressWarnings</code>; cannot be null.
     * @param arguments the arguments of the new annotation; cannot be null.
     * @return the new annotation; never null.
     */
    public AnnotationTree createAnnotation(String annotationType, List<? extends ExpressionTree> arguments) {
        Parameters.notNull("annotationType", annotationType); // NOI18N
        Parameters.notNull("arguments", arguments); // NOI18N

        ExpressionTree annotationTypeTree = createQualIdent(annotationType);
        return getTreeMaker().Annotation(annotationTypeTree, arguments);
    }

    /**
     * Creates a new annotation argument whose value is a literal.
     *
     * @param  argumentName the argument name; cannot be null.
     * @param  argumentValue the argument value; cannot be null. The semantics
     *         of this parameter is the same as of the parameters of
     *         {@link TreeMaker#Literal(Object)}.
     * @return the new annotation argument; never null.
     */
    public ExpressionTree createAnnotationArgument(String argumentName, Object argumentValue) {
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentValue", argumentValue); // NOI18N

        TreeMaker make = getTreeMaker();
        ExpressionTree argumentValueTree = make.Literal(argumentValue);
        if (argumentName == null) {
            return argumentValueTree;
        } else {
            return make.Assignment(make.Identifier(argumentName), argumentValueTree);
        }
    }

    /**
     * Creates a new annotation argument whose value is an array.
     *
     * @param argumentName the argument name; cannot be null.
     * @param argumentValues the argument values to initialize the array with; cannot be null.
     * @return the new annotation argument; never null.
     */
    public ExpressionTree createAnnotationArgument(String argumentName, List<? extends ExpressionTree> argumentValues) {
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentValues", argumentValues); // NOI18N

        TreeMaker make = getTreeMaker();
        ExpressionTree argumentValuesTree = make.NewArray(null, Collections.<ExpressionTree>emptyList(), argumentValues);
        if (argumentName == null) {
            return argumentValuesTree;
        } else {
            return make.Assignment(make.Identifier(argumentName), argumentValuesTree);
        }
    }

    /**
     * Creates a new annotation argument whose value is a member of a type. For
     * example it can be used to generate <code>@Target(ElementType.CONSTRUCTOR)</code>.
     *
     * @param  argumentName the argument name; cannot be null.
     * @param  argumentType the fully-qualified name of the type whose member is to be invoked
     *         (e.g. <code>java.lang.annotations.ElementType</code> in the previous
     *         example); cannot be null.
     * @param  argumentTypeField a field of <code>argumentType</code>
     *         (e.g. <code>CONSTRUCTOR</code> in the previous example);
     *         cannot be null.
     * @return the new annotation argument; never null.
     */
    public ExpressionTree createAnnotationArgument(String argumentName, String argumentType, String argumentTypeField) {
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentType", argumentType); // NOI18N
        Parameters.javaIdentifier("argumentTypeField", argumentTypeField); // NOI18N

        TreeMaker make = getTreeMaker();
        MemberSelectTree argumentValueTree = make.MemberSelect(createQualIdent(argumentType), argumentTypeField);
        if (argumentName == null) {
            return argumentValueTree;
        } else {
            return make.Assignment(make.Identifier(argumentName), argumentValueTree);
        }
    }

    /**
     * Ensures the given class has a public no-arg constructor.
     *
     * @param  classTree the class to ensure the constructor for; cannot be null.
     * @return a modified class if a no-arg constructor was added, the original
     *         class otherwise; never null.
     */
    public ClassTree ensureNoArgConstructor(ClassTree classTree) {
        TypeElement typeElement = SourceUtils.classTree2TypeElement(copy, classTree);
        if (typeElement == null) {
            throw new IllegalArgumentException("No TypeElement for ClassTree " + classTree.getSimpleName());
        }
        ExecutableElement constructor = SourceUtils.getNoArgConstructor(copy, typeElement);
        MethodTree constructorTree = constructor != null ? copy.getTrees().getTree(constructor) : null;
        MethodTree newConstructorTree = null;
        TreeMaker make = getTreeMaker();
        if (constructor != null) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                ModifiersTree oldModifiersTree = constructorTree.getModifiers();
                Set<Modifier> newModifiers = EnumSet.of(Modifier.PUBLIC);
                for (Modifier modifier : oldModifiersTree.getFlags()) {
                    if (Modifier.PROTECTED != modifier && Modifier.PRIVATE != modifier) {
                        newModifiers.add(modifier);
                    }
                }
                newConstructorTree = make.Constructor(
                    make.Modifiers(newModifiers),
                    constructorTree.getTypeParameters(),
                    constructorTree.getParameters(),
                    constructorTree.getThrows(),
                    constructorTree.getBody());
            }
        } else {
            newConstructorTree = make.Constructor(
                    createModifiers(Modifier.PUBLIC),
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    "{ }"); // NOI18N
        }
        ClassTree newClassTree = classTree;
        if (newConstructorTree != null) {
            if (constructorTree != null) {
                newClassTree = make.removeClassMember(newClassTree, constructorTree);
            }
            newClassTree = make.addClassMember(newClassTree, newConstructorTree);
        }
        return newClassTree;
    }

    /**
     * Creates a constructor which assigns its parameters to fields with the
     * same names. For example it can be used to generate:
     *
     * <pre>
     * public void Constructor(String field1, Object field2) {
     *     this.field1 = field1;
     *     this.field2 = field2;
     * }
     * </pre>
     *
     * @param  modifiersTree the constructor modifiers.
     * @param  constructorName the constructor name; cannot be null, it's not used inside except for assertion since 2007 (or before, TODO: remove?)
     * @param  parameters the constructor parameters; cannot be null.
     * @return the new constructor; never null.
     */
    public MethodTree createAssignmentConstructor(ModifiersTree modifiersTree, String constructorName, List<VariableTree> parameters) {
        Parameters.notNull("modifiersTree", modifiersTree);
        Parameters.javaIdentifier("constructorName", constructorName); // NOI18N
        Parameters.notNull("parameters", parameters); // NOI18N

        StringBuilder body = new StringBuilder(parameters.size() * 30);
        body.append("{"); // NOI18N
        for (VariableTree parameter : parameters) {
            String parameterName = parameter.getName().toString();
            body.append("this." + parameterName + " = " + parameterName + ";"); // NOI18N
        }
        body.append("}"); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Constructor(
                modifiersTree,
                Collections.<TypeParameterTree>emptyList(),
                parameters,
                Collections.<ExpressionTree>emptyList(),
                body.toString());
    }

    /**
     * Creates a new field.
     *
     * @param  scope the scope in which to create the field (will be e.g. used
     *         to parse <code>fieldType</code>).
     * @param  modifiersTree the field modifiers; cannot be null.
     * @param  fieldType the fully-qualified name of the field type; cannot be null.
     * @param  fieldName the field name; cannot be null.
     * @param  expressionTree expression to initialize the field; can be null.
     * @return the new field; never null.
     */
    public VariableTree createField(TypeElement scope, ModifiersTree modifiersTree, String fieldName, String fieldType, ExpressionTree expressionTree) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("fieldName", fieldName); // NOI18N
        Parameters.notNull("fieldType", fieldType); // NOI18N

        return getTreeMaker().Variable(
                modifiersTree,
                fieldName,
                createType(fieldType, scope),
                expressionTree);
    }

    /**
     * Creates a new variable (a <code>VariableTree</code> with no
     * modifiers nor initializer).
     *
     * @param  scope the scope in which to create the variable (will be e.g. used
     *         to parse <code>variableType</code>).
     * @param  variableType the fully-qualified name of the variable type; cannot be null.
     * @param  variableName the variable name; cannot be null.
     * @return the new variable; never null.
     */
    public VariableTree createVariable(TypeElement scope, String variableName, String variableType) {
        Parameters.javaIdentifier("variableName", variableName); // NOI18N
        Parameters.notNull("variableType", variableType); // NOI18N

        return createField(
                scope,
                createEmptyModifiers(),
                variableName,
                variableType,
                null);
    }

    /**
     * Creates a new variable (a <code>VariableTree</code> with no
     * modifiers nor initializer).
     *
     * @param  variableType the variable type; cannot be null.
     * @param  variableName the variable name; cannot be null.
     * @return the new variable; never null.
     */
    public VariableTree createVariable(String variableName, Tree variableType) {
        Parameters.javaIdentifier("variableName", variableName); // NOI18N
        Parameters.notNull("variableType", variableType); // NOI18N

        return getTreeMaker().Variable(
                createEmptyModifiers(),
                variableName,
                variableType,
                null);
    }

    /**
     * Removes any modifiers from the given <code>VariableTree</code>. This can be e.g.
     * used to create a variable suitable for use as a method parameter.
     *
     * @param  variableTree the <code>VariableTree</code> to remove the modifiers from.
     * @return a <code>VariableTree</code> with the same properties but no modifiers.
     */
    public VariableTree removeModifiers(VariableTree variableTree) {
        Parameters.notNull("variableTree", variableTree);

        TreeMaker make = getTreeMaker();
        return make.Variable(
                createEmptyModifiers(),
                variableTree.getName(),
                variableTree.getType(),
                variableTree.getInitializer());
    }

    /**
     * Creates a new public property getter method.
     *
     * @param  scope the scope in which to create the method (will be e.g. used
     *         to parse <code>propertyType</code>).
     * @param  modifiersTree the method modifiers; cannot be null.
     * @param  propertyType the fully-qualified name of the property type; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertyGetterMethod(TypeElement scope, ModifiersTree modifiersTree, String propertyName, String propertyType) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N
        Parameters.notNull("propertyType", propertyType); // NOI18N

        return createPropertyGetterMethod(modifiersTree, propertyName, createType(propertyType, scope));
    }

    /**
     * Creates a new public property getter method.
     *
     * @param  modifiersTree the method modifiers; cannot be null.
     * @param  propertyType the property type; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertyGetterMethod(ModifiersTree modifiersTree, String propertyName, Tree propertyType) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N
        Parameters.notNull("propertyType", propertyType); // NOI18N

        return getTreeMaker().Method(
                modifiersTree,
                createPropertyAccessorName(propertyName, true),
                propertyType,
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{ return " + propertyName + "; }", // NOI18N
                null);
    }

    /**
     * Creates a new public property setter method.
     *
     * @param  scope the scope in which to create the method (will be e.g. used
     *         to parse <code>propertyType</code>).
     * @param  modifiersTree the method modifiers; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @param  propertyType the fully-qualified name of the property type; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertySetterMethod(TypeElement scope, ModifiersTree modifiersTree, String propertyName, String propertyType) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N
        Parameters.notNull("propertyType", propertyType); // NOI18N

        return createPropertySetterMethod(modifiersTree, propertyName, createType(propertyType, scope));
    }

    /**
     * Creates a new public property setter method.
     *
     * @param  modifiersTree the method modifiers; cannot be null.
     * @param  propertyType the property type; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertySetterMethod(ModifiersTree modifiersTree, String propertyName, Tree propertyType) {
        Parameters.notNull("modifiersTree", modifiersTree); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N
        Parameters.notNull("propertyType", propertyType); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Method(
                modifiersTree,
                createPropertyAccessorName(propertyName, false),
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(createVariable(propertyName, propertyType)),
                Collections.<ExpressionTree>emptyList(),
                "{ this." + propertyName + " = " + propertyName + "; }", // NOI18N
                null);
    }

    /**
     * Adds an annotation to a class. This is equivalent to {@link TreeMaker#addModifiersAnnotation},
     * but it creates and returns a new <code>ClassTree, not a new <code>ModifiersTree</code>.
     *
     * @param  classTree the class to add the annotation to; cannot be null.
     * @param  annotationTree the annotation to add; cannot be null.
     * @return a new class annotated with the new annotation; never null.
     */
    @SuppressWarnings("unchecked") // NOI18N
    public ClassTree addAnnotation(ClassTree classTree, AnnotationTree annotationTree) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("annotationTree", annotationTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Class(
                make.addModifiersAnnotation(classTree.getModifiers(), annotationTree),
                classTree.getSimpleName(),
                classTree.getTypeParameters(),
                classTree.getExtendsClause(),
                (List<ExpressionTree>)classTree.getImplementsClause(),
                classTree.getMembers());
    }

    /**
     * Adds an annotation to a method. This is equivalent to {@link TreeMaker#addModifiersAnnotation},
     * but it creates and returns a new <code>MethodTree, not a new <code>ModifiersTree</code>.
     *
     * @param  methodTree the method to add the annotation to; cannot be null.
     * @param  annotationTree the annotation to add; cannot be null.
     * @return a new method annotated with the new annotation; never null.
     */
    public MethodTree addAnnotation(MethodTree methodTree, AnnotationTree annotationTree) {
        Parameters.notNull("methodTree", methodTree); // NOI18N
        Parameters.notNull("annotationTree", annotationTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Method(
                make.addModifiersAnnotation(methodTree.getModifiers(), annotationTree),
                methodTree.getName(),
                methodTree.getReturnType(),
                methodTree.getTypeParameters(),
                methodTree.getParameters(),
                methodTree.getThrows(),
                methodTree.getBody(),
                (ExpressionTree)methodTree.getDefaultValue());
    }

    /**
     * Adds an annotation to a variable. This is equivalent to {@link TreeMaker#addModifiersAnnotation},
     * but it creates and returns a new <code>VariableTree, not a new <code>ModifiersTree</code>.
     *
     * @param  variableTree the variable to add the annotation to; cannot be null.
     * @param  annotationTree the annotation to add; cannot be null.
     * @return a new variable annotated with the new annotation; never null.
     */
    public VariableTree addAnnotation(VariableTree variableTree, AnnotationTree annotationTree) {
        Parameters.notNull("variableTree", variableTree); // NOI18N
        Parameters.notNull("annotationTree", annotationTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Variable(
                make.addModifiersAnnotation(variableTree.getModifiers(), annotationTree),
                variableTree.getName(),
                variableTree.getType(),
                variableTree.getInitializer());
    }

    /**
     * Inserts the given fields in the given class after any fields already existing
     * in the class (if any, otherwise the fields are inserted at the beginning
     * of the class).
     *
     * @param  classTree the class to add fields to; cannot be null.
     * @param  fieldTrees the fields to be added; cannot be null.
     * @return the class containing the new fields; never null.
     */
    public ClassTree addClassFields(ClassTree classTree, List<? extends VariableTree> fieldTrees) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("fieldTrees", fieldTrees); // NOI18N

        int firstNonFieldIndex = 0;
        Iterator<? extends Tree> memberTrees = classTree.getMembers().iterator();
        while (memberTrees.hasNext() && memberTrees.next().getKind() == Tree.Kind.VARIABLE) {
            firstNonFieldIndex++;
        }
        TreeMaker make = getTreeMaker();
        ClassTree newClassTree = classTree;
        for (VariableTree fieldTree : fieldTrees) {
            newClassTree = make.insertClassMember(newClassTree, firstNonFieldIndex, fieldTree);
            firstNonFieldIndex++;
        }
        return newClassTree;
    }

    // PENDING addClassConstructors(), addClassMethods()

    /**
     * Adds the specified interface to the implements clause of
     * {@link #getClassTree()}.
     *
     * @param  classTree the class to add the implements clause to.
     * @param  interfaceType the fully-qualified name of the interface; cannot be null.
     * @return the class implementing the new interface.
     */
    public ClassTree addImplementsClause(ClassTree classTree, String interfaceType) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("interfaceType", interfaceType); // NOI18N

        ExpressionTree interfaceTree = createQualIdent(interfaceType);
        return getTreeMaker().addClassImplementsClause(classTree, interfaceTree);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    private TreeMaker getTreeMaker() {
        return copy.getTreeMaker();
    }

    private ModifiersTree createEmptyModifiers() {
        return getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
    }

    private ExpressionTree tryCreateQualIdent(String typeName) {
        TypeElement typeElement = copy.getElements().getTypeElement(typeName);
        if (typeElement != null) {
            return getTreeMaker().QualIdent(typeElement);
        }
        return null;

    }

    private ExpressionTree createQualIdent(String typeName) {
        ExpressionTree qualIdent = tryCreateQualIdent(typeName);
        if ( qualIdent == null ){
            qualIdent = getTreeMaker().QualIdent( typeName );
        }
        if (qualIdent == null) {
            throw new IllegalArgumentException("Cannot create a QualIdent for " + typeName); // NOI18N
        }
        return qualIdent;
    }

    private String createPropertyAccessorName(String propertyName, boolean getter) {
        assert propertyName.length() > 0;
        StringBuffer pascalCaseName = new StringBuffer(propertyName);
        pascalCaseName.setCharAt(0, Character.toUpperCase(pascalCaseName.charAt(0)));
        return (getter ? "get" : "set") + pascalCaseName; // NOI18N
    }

    // </editor-fold>
}
