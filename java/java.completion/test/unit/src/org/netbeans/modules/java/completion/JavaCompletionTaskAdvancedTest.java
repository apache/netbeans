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

package org.netbeans.modules.java.completion;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTaskAdvancedTest extends CompletionTestBase {

    public JavaCompletionTaskAdvancedTest(String testName) {
        super(testName);
    }

    // constructor tests -------------------------------------------------------

    public void testEmptyFileAfterTypingNew() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew ", "declaredTypes.pass");
    }

    public void testAfterTypingNew() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew ", "declaredTypes.pass");
    }
    
    public void testAfterNew() throws Exception {
        performTest("AdvancedMethodBody", 930, null, "declaredTypes.pass");
    }
    
    public void testEmptyFileTypingConstructorName() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew Str", "declaredTypesStartingWithStr.pass");
    }
    
    public void testTypingConstructorName() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew Str", "declaredTypesStartingWithStr.pass");
    }
    
    public void testOnConstructorName() throws Exception {
        performTest("AdvancedMethodBody", 933, null, "declaredTypesStartingWithStr.pass");
    }

    public void testEmptyFileAfterTypingConstructorName() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String", "stringConstructors.pass");
    }
    
    public void testAfterTypingConstructorName() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String", "stringConstructors.pass");
    }
    
    public void testAfterConstructorName() throws Exception {
        performTest("AdvancedMethodBody", 936, null, "stringConstructors.pass");
    }
    
    public void testEmptyFileBeforeTypingConstructorParam() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(", "typesLocalMembersVarsAndSmartString.pass");
    }
    
    public void testBeforeTypingConstructorParam() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(", "typesLocalMembersVarsAndSmartString.pass");
    }
    
    public void testBeforeConstructorParam() throws Exception {
        performTest("AdvancedMethodBody", 937, null, "typesLocalMembersVarsAndSmartString.pass");
    }
    
    public void testEmptyFileTypingConstructorParam() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(fie", "field.pass");
    }
    
    public void testTypingConstructorParam() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(fie", "field.pass");
    }
    
    public void testOnConstructorParam() throws Exception {
        performTest("AdvancedMethodBody", 941, null, "field.pass");
    }
    
    public void testEmptyFileAfterTypingConstructorParam() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(field", "field.pass");
    }
    
    public void testAfterTypingConstructorParam() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(field", "field.pass");
    }
    
    public void testAfterConstructorParam() throws Exception {
        performTest("AdvancedMethodBody", 942, null, "field.pass");
    }
    
    public void testEmptyFileAfterTypingConstructorParamAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(field ", "empty.pass");
    }
    
    public void testAfterTypingConstructorParamAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(field ", "empty.pass");
    }
    
    public void testAfterConstructorParamAndSpace() throws Exception {
        performTest("AdvancedMethodBody", 942, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingConstructor() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(field)", "instanceOf.pass");
    }
    
    public void testAfterTypingConstructor() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(field)", "instanceOf.pass");
    }
    
    public void testAfterConstructor() throws Exception {
        performTest("AdvancedMethodBody", 943, null, "instanceOf.pass");
    }
    
    public void testEmptyFileAfterTypingConstructorAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(field) ", "instanceOf.pass");
    }
    
    public void testAfterTypingConstructorAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(field) ", "instanceOf.pass");
    }
    
    public void testAfterConstructorAndSpace() throws Exception {
        performTest("AdvancedMethodBody", 943, " ", "instanceOf.pass");
    }
    
    public void testEmptyFileAfterTypingConstructorAndDot() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean b;\nnew String(field).", "stringContent.pass");
    }
    
    public void testAfterTypingConstructorAndDot() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean b;\nnew String(field).", "stringContent.pass");
    }
    
    public void testAfterConstructorAndDot() throws Exception {
        performTest("AdvancedMethodBody", 944, null, "stringContent.pass");
    }
    
    // primitive_type.class tests ----------------------------------------------
   
    public void testEmptyFileAfterTypingPrimitiveTypeAndDot() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean.", "classKeyword.pass");
    }
    
    public void testAfterTypingPrimitiveTypeAndDot() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean.", "classKeyword.pass");
    }
    
    public void testAfterPrimitiveTypeAndDot() throws Exception {
        performTest("AdvancedMethodBody", 978, null, "classKeyword.pass");
    }
    
    public void testEmptyFileTypingPrimitiveTypeDotClass() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean.c", "classKeyword.pass");
    }
    
    public void testTypingPrimitiveTypeDotClass() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean.c", "classKeyword.pass");
    }
    
    public void testOnPrimitiveTypeDotClass() throws Exception {
        performTest("AdvancedMethodBody", 979, null, "classKeyword.pass");
    }

    public void testEmptyFileAfterTypingPrimitiveTypeDotClass() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "boolean.class", "classKeyword.pass");
    }
    
    public void testAfterTypingPrimitiveTypeDotClass() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "boolean.class", "classKeyword.pass");
    }
    
    public void testAfterPrimitiveTypeDotClass() throws Exception {
        performTest("AdvancedMethodBody", 983, null, "classKeyword.pass");
    }
    
    // Boolean.FALSE.booleanValue() like tests ---------------------------------
    
    public void testEmptyFileTypingBooleanValue() throws Exception {
        performTest("SimpleMethodBodyStart", 898, "Boolean.FALSE.boolean", "booleanValue.pass");
    }
    
    public void testTypingBooleanValue() throws Exception {
        performTest("SimpleEmptyMethodBody", 898, "Boolean.FALSE.boolean", "booleanValue.pass");
    }
    
    public void testOnBooleanValue() throws Exception {
        performTest("AdvancedMethodBody", 1007, null, "booleanValue.pass");
    }

    // Expression in field init tests ------------------------------------------
    
    public void testEmptyFileTypingParenWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = (", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testTypingParenWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = (", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testAfterParenWithinInitOfField() throws Exception {
        performTest("Field", 950, null, "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testEmptyFileTypingSecondParenWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testTypingSecondParenWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testAfterSecondParenWithinInitOfField() throws Exception {
        performTest("Field", 951, null, "typesLocalMembersAndSmartNumber.pass");
    }

    public void testEmptyFileTypingSecondParenAndSpaceWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = (( ", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testTypingSecondParenAndSpaceWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = (( ", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testAfterSecondParenAndSpaceWithinInitOfField() throws Exception {
        performTest("Field", 951, " ", "typesLocalMembersAndSmartNumber.pass");
    }
    
    public void testEmptyFileTypingCastTypeWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number ", "empty.pass");
    }
    
    public void testTypingCastTypeWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number ", "empty.pass");
    }
    
    public void testOnCastTypeWithinInitOfField() throws Exception {
        performTest("Field", 957, " ", "empty.pass");
    }

    public void testEmptyFileAfterTypingCastTypeWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)", "typesAndLocalMembers1.pass");
    }
    
    public void testAfterTypingCastTypeWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number)", "typesAndLocalMembers1.pass");
    }
    
    public void testAfterCastTypeWithinInitOfField() throws Exception {
        performTest("Field", 958, null, "typesAndLocalMembers1.pass");
    }

    public void testEmptyFileTypingCastAndSpaceWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number) ", "typesAndLocalMembers1.pass");
    }
    
    public void testTypingCastAndSpaceWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number) ", "typesAndLocalMembers1.pass");
    }
    
    public void testAfterCastAndSpaceWithinInitOfField() throws Exception {
        performTest("Field", 958, " ", "typesAndLocalMembers1.pass");
    }

    public void testEmptyFileTypingCastAndMethodWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)h", "hashCode.pass");
    }
    
    public void testTypingCastAndMethodWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number)h", "hashCode.pass");
    }
    
    public void testOnCastAndMethodWithinInitOfField() throws Exception {
        performTest("Field", 959, null, "hashCode.pass");
    }

    public void testEmptyFileAfterTypingCastAndMethodWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode())", "instanceOf.pass");
    }
    
    public void testAfterTypingCastAndMethodWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number)hashCode())", "instanceOf.pass");
    }
    
    public void testAfterCastAndMethodWithinInitOfField() throws Exception {
        performTest("Field", 969, null, "instanceOf.pass");
    }

    public void testEmptyFileAfterTypingCastWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode()).", "numberContent.pass");
    }
    
    public void testAfterTypingCastWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 923, " = ((Number)hashCode()).", "numberContent.pass");
    }
    
    public void testAfterCastWithinInitOfField() throws Exception {
        performTest("Field", 970, null, "numberContent.pass");
    }

    public void testEmptyFileBeforeTypingInstanceofWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode()).intValue();\npublic boolean b = num ", "instanceOf.pass");
    }
    
    public void testBeforeTypingInstanceofWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 945, " = num ", "instanceOf.pass");
    }
    
    public void testBeforeInstanceofWithinInitOfField() throws Exception {
        performTest("Field", 1009, null, "instanceOf.pass");
    }

    public void testEmptyFileTypingInstanceofWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode()).intValue();\npublic boolean b = num i", "instanceOf.pass");
    }
    
    public void testTypingInstanceofWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 945, " = num i", "instanceOf.pass");
    }
    
    public void testInstanceofWithinInitOfField() throws Exception {
        performTest("Field", 1010, null, "instanceOf.pass");
    }

    public void testEmptyFileAfterTypingInstanceofWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode()).intValue();\npublic boolean b = num instanceof ", "declaredTypes.pass");
    }
    
    public void testAfterTypingInstanceofWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 945, " = num instanceof ", "declaredTypes.pass");
    }
    
    public void testAfterInstanceofWithinInitOfField() throws Exception {
        performTest("Field", 1020, null, "declaredTypes.pass");
    }

    public void testEmptyFileTypingInstanceofTypeWithinInitOfField() throws Exception {
        performTest("MethodStart", 849, "public static int staticField;\npublic int field;\npublic Number num = ((Number)hashCode()).intValue();\npublic boolean b = num instanceof I", "javaLangContentStartingWithI.pass");
    }
    
    public void testTypingInstanceofTypeWithinInitOfField() throws Exception {
        performTest("FieldNoInit", 945, " = num instanceof I", "javaLangContentStartingWithI.pass");
    }
    
    public void testOnInstanceofTypeWithinInitOfField() throws Exception {
        performTest("Field", 1021, null, "javaLangContentStartingWithI.pass");
    }

    // For loop tests ----------------------------------------------------------
    
    public void testEmptyFileAfterTypingForKeywordAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for ", "empty.pass");
    }
    
    public void testAfterTypingForKeywordAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for ", "empty.pass");
    }
    
    public void testAfterForKeywordAndSpace() throws Exception {
        performTest("For", 911, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingForKeywordAndParen() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (", "typesLocalFieldsAndVars.pass");
    }
    
    public void testAfterTypingForKeywordAndParen() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (", "typesLocalFieldsAndVars.pass");
    }
    
    public void testAfterForKeywordAndParen() throws Exception {
        performTest("For", 912, null, "typesLocalFieldsAndVars.pass");
    }
    
    public void testEmptyFileAfterTypingForVarTypeAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int ", "intVarName.pass");
    }
    
    public void testAfterTypingForVarTypeAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int ", "intVarName.pass");
    }
    
    public void testAfterForVarTypeAndSpace() throws Exception {
        performTest("For", 916, null, "intVarName.pass");
    }
    
    public void testEmptyFileAfterTypingForVarAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i ", "empty.pass");
    }
    
    public void testAfterTypingForVarAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i ", "empty.pass");
    }
    
    public void testAfterForVarAndSpace() throws Exception {
        performTest("For", 918, null, "empty.pass");
    }
    
    public void testEmptyFileBeforTypingForVarInit() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = ", "typesLocalMembersVarsAndSmartInt.pass");
    }
    
    public void testBeforTypingForVarInit() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = ", "typesLocalMembersVarsAndSmartInt.pass");
    }
    
    public void testBeforeForVarInit() throws Exception {
        performTest("For", 920, null, "typesLocalMembersVarsAndSmartInt.pass");
    }
    
    public void testEmptyFileAfterTypingForVarInitAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0 ", "empty.pass");
    }
    
    public void testAfterTypingForVarInitAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0 ", "empty.pass");
    }
    
    public void testAfterForVarInitAndSpace() throws Exception {
        performTest("For", 921, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforTypingForCondition() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0;", "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testBeforTypingForCondition() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0;", "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testBeforeForCondition() throws Exception {
        performTest("For", 922, null, "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testEmptyFileBeforTypingForConditionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; ", "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testBeforTypingForConditionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; ", "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testBeforeForConditionAndSpace() throws Exception {
        performTest("For", 923, null, "typesLocalMembersVarsAndSmartBoolean.pass");
    }
    
    public void testEmptyFileAfterTypingForConditionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a ", "empty.pass");
    }
    
    public void testAfterTypingForConditionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a ", "empty.pass");
    }
    
    public void testAfterForConditionAndSpace() throws Exception {
        performTest("For", 928, " ", "empty.pass");
    }
    
    public void testEmptyFileBeforTypingForUpdateExpression() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a;", "typesLocalMembersAndVars2.pass");
    }
    
    public void testBeforTypingForUpdateExpression() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a;", "typesLocalMembersAndVars2.pass");
    }
    
    public void testBeforeForUpdateExpression() throws Exception {
        performTest("For", 929, null, "typesLocalMembersAndVars2.pass");
    }
    
    public void testEmptyFileBeforTypingForUpdateExpressionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a; ", "typesLocalMembersAndVars2.pass");
    }
    
    public void testBeforTypingForUpdateExpressionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a; ", "typesLocalMembersAndVars2.pass");
    }
    
    public void testBeforeForUpdateExpressionAndSpace() throws Exception {
        performTest("For", 930, null, "typesLocalMembersAndVars2.pass");
    }
    
    public void testEmptyFileAfterTypingForUpdateExpressionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a; i++ ", "empty.pass");
    }
    
    public void testAfterTypingForUpdateExpressionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0 ; i < a; i++ ", "empty.pass");
    }
    
    public void testAfterForUpdateExpressionAndSpace() throws Exception {
        performTest("For", 933, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingForUpdateExpressionAndParen() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a; i++)", "methodBodyContentAfterFor.pass");
    }
    
    public void testAfterTypingForUpdateExpressionAndParen() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a; i++)", "methodBodyContentAfterFor.pass");
    }
    
    public void testAfterForUpdateExpressionAndParen() throws Exception {
        performTest("For", 934, null, "methodBodyContentAfterFor.pass");
    }
    
    public void testEmptyFileAfterTypingForUpdateExpressionParenAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a; i++) ", "methodBodyContentAfterFor.pass");
    }
    
    public void testAfterTypingForUpdateExpressionParenAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a; i++) ", "methodBodyContentAfterFor.pass");
    }
    
    public void testAfterForUpdateExpressionParenAndSpace() throws Exception {
        performTest("For", 935, null, "methodBodyContentAfterFor.pass");
    }

    public void testEmptyFileInsideForBody() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (int i = 0; i < a; i++) {\n", "blockContentAfterFor.pass");
    }
    
    public void testTypingInsideForBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (int i = 0; i < a; i++) {\n", "blockContentAfterFor.pass");
    }
    
    public void testInsideForBody() throws Exception {
        performTest("For", 936, null, "blockContentAfterFor.pass");
    }
    
    // For-each loop tests -----------------------------------------------------
    
    public void testEmptyFileBeforTypingForEachExpression() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b :", "typesLocalMembersAndVars1.pass");
    }
    
    public void testBeforTypingForEachExpression() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b :", "typesLocalMembersAndVars1.pass");
    }
    
    public void testBeforeForEachExpression() throws Exception {
        performTest("ForEach", 920, null, "typesLocalMembersAndVars1.pass");
    }
    
    public void testEmptyFileBeforTypingForEachExpressionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b : ", "typesLocalMembersAndVars1.pass");
    }
    
    public void testBeforTypingForEachExpressionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b : ", "typesLocalMembersAndVars1.pass");
    }
    
    public void testBeforeForEachExpressionAndSpace() throws Exception {
        performTest("ForEach", 921, null, "typesLocalMembersAndVars1.pass");
    }
    
    public void testEmptyFileAfterTypingForEachExpressionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b : field.getBytes() ", "empty.pass");
    }
    
    public void testAfterTypingForEachExpressionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b : field.getBytes() ", "empty.pass");
    }
    
    public void testAfterForEachExpressionAndSpace() throws Exception {
        performTest("ForEach", 937, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingForEachExpressionAndParen() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b : field.getBytes())", "methodBodyContentAfterForEach.pass");
    }
    
    public void testAfterTypingForEachExpressionAndParen() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b : field.getBytes())", "methodBodyContentAfterForEach.pass");
    }
    
    public void testAfterForEachExpressionAndParen() throws Exception {
        performTest("ForEach", 938, null, "methodBodyContentAfterForEach.pass");
    }
    
    public void testEmptyFileAfterTypingForEachExpressionParenAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b : field.getBytes()) ", "methodBodyContentAfterForEach.pass");
    }
    
    public void testAfterTypingForEachExpressionParenAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b : field.getBytes()) ", "methodBodyContentAfterForEach.pass");
    }
    
    public void testAfterForEachExpressionParenAndSpace() throws Exception {
        performTest("ForEach", 939, null, "methodBodyContentAfterForEach.pass");
    }
    
    public void testEmptyFileInsideForEachBody() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "for (Byte b : field.getBytes()) {\n", "blockContentAfterForEach.pass");
    }
    
    public void testTypingInsideForEachBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "for (Byte b : field.getBytes()) {\n", "blockContentAfterForEach.pass");
    }
    
    public void testInsideForEachBody() throws Exception {
        performTest("ForEach", 940, null, "blockContentAfterForEach.pass");
    }
    
    // Switch-case statement tests ---------------------------------------------
    
    public void testEmptyFileAfterTypingSwitchKeyword() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch", "switchKeyword.pass");
    }
    
    public void testAfterTypingSwitchKeyword() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch", "switchKeyword.pass");
    }
    
    public void testAfterSwitchKeyword() throws Exception {
        performTest("Switch", 913, null, "switchKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingSwitchKeywordAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch ", "empty.pass");
    }
    
    public void testAfterTypingSwitchKeywordAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch ", "empty.pass");
    }
    
    public void testAfterSwitchKeywordAndSpace() throws Exception {
        performTest("Switch", 914, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingSwitchKeywordAndParen() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (", "typesLocalMembersVarsAndSmartEnumAndInt.pass");
    }
    
    public void testAfterTypingSwitchKeywordAndParen() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (", "typesLocalMembersVarsAndSmartEnumAndInt.pass");
    }
    
    public void testAfterSwitchKeywordAndParen() throws Exception {
        performTest("Switch", 915, null, "typesLocalMembersVarsAndSmartEnumAndInt.pass");
    }
    
    public void testEmptyFileAfterTypingSwitchExpression() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a", "a.pass");
    }
    
    public void testAfterTypingSwitchExpression() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a", "a.pass");
    }
    
    public void testAfterSwitchExpression() throws Exception {
        performTest("Switch", 916, null, "a.pass");
    }
    
    public void testEmptyFileAfterTypingSwitchExpressionAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a ", "empty.pass");
    }
    
    public void testAfterTypingSwitchExpressionAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a ", "empty.pass");
    }
    
    public void testAfterSwitchExpressionAndSpace() throws Exception {
        performTest("Switch", 916, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingSwitchExpressionAndParen() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) ", "empty.pass");
    }
    
    public void testAfterTypingSwitchExpressionAndParen() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) ", "empty.pass");
    }
    
    public void testAfterSwitchExpressionAndParen() throws Exception {
        performTest("Switch", 917, null, "empty.pass");
    }
    
    public void testEmptyFileInsideSwitchBody() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\n", "caseAndDefaultKeywords.pass");
    }
    
    public void testTypingInsideSwitchBody() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\n", "caseAndDefaultKeywords.pass");
    }
    
    public void testInsideSwitchBody() throws Exception {
        performTest("Switch", 932, null, "caseAndDefaultKeywords.pass");
    }
    
    public void testEmptyFileAfterTypingCaseKeyword() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase", "caseKeyword.pass");
    }
    
    public void testAfterTypingCaseKeyword() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase", "caseKeyword.pass");
    }
    
    public void testAfterCaseKeyword() throws Exception {
        performTest("Switch", 936, null, "caseKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingCaseKeywordAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase ", "declaredTypesAndSmartInt.pass");
    }
    
    public void testAfterTypingCaseKeywordAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase ", "declaredTypesAndSmartInt.pass");
    }
    
    public void testAfterCaseKeywordAndSpace() throws Exception {
        performTest("Switch", 936, " ", "declaredTypesAndSmartInt.pass");
    }
    
    public void testEmptyFileAfterTypingCaseKeywordAndColon() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase 0:", "methodBodyContentAfterCase.pass");
    }
    
    public void testAfterTypingCaseKeywordAndColon() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase 0:", "methodBodyContentAfterCase.pass");
    }
    
    public void testAfterCaseKeywordAndColon() throws Exception {
        performTest("Switch", 939, null, "methodBodyContentAfterCase.pass");
    }
    
    public void testEmptyFileAfterTypingCaseKeywordAndColonAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase 0: ", "methodBodyContentAfterCase.pass");
    }
    
    public void testAfterTypingCaseKeywordAndColonAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase 0: ", "methodBodyContentAfterCase.pass");
    }
    
    public void testAfterCaseKeywordAndColonAndSpace() throws Exception {
        performTest("Switch", 940, null, "methodBodyContentAfterCase.pass");
    }
    
    public void testEmptyFileAfterTypingVarInCaseBlock() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase 0:\nboolean b;\n", "methodBodyContentAfterCaseAndVar.pass");
    }

    public void testAfterTypingVarInCaseBlock() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase 0:\nboolean b;\n", "methodBodyContentAfterCaseAndVar.pass");
    }

    public void testAfterVarInCaseBlock() throws Exception {
        performTest("Switch", 967, null, "methodBodyContentAfterCaseAndVar.pass");
    }

    public void testEmptyFileAfterTypingBreakKeyword() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ncase 0:\nbreak", "breakKeyword.pass");
    }
    
    public void testAfterTypingBreakKeyword() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ncase 0:\nbreak", "breakKeyword.pass");
    }
    
    public void testAfterBreakKeyword() throws Exception {
        performTest("Switch", 988, null, "breakKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingDefaultKeyword() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ndefault", "defaultKeyword.pass");
    }
    
    public void testAfterTypingDefaultKeyword() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ndefault", "defaultKeyword.pass");
    }
    
    public void testAfterDefaultKeyword() throws Exception {
        performTest("Switch", 1009, null, "defaultKeyword.pass");
    }
    
    public void testEmptyFileAfterTypingDefaultKeywordAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ndefault ", "empty.pass");
    }
    
    public void testAfterTypingDefaultKeywordAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ndefault ", "empty.pass");
    }
    
    public void testAfterDefaultKeywordAndSpace() throws Exception {
        performTest("Switch", 1009, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingDefaultKeywordAndColon() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ndefault:", "methodBodyContentAfterDefault.pass");
    }
    
    public void testAfterTypingDefaultKeywordAndColon() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ndefault:", "methodBodyContentAfterDefault.pass");
    }
    
    public void testAfterDefaultKeywordAndColon() throws Exception {
        performTest("Switch", 1010, null, "methodBodyContentAfterDefault.pass");
    }
    
    public void testEmptyFileAfterTypingDefaultKeywordAndColonAndSpace() throws Exception {
        performTest("SimpleMethodBodyStart", 907, "switch (a) {\ndefault: ", "methodBodyContentAfterDefault.pass");
    }
    
    public void testAfterTypingDefaultKeywordAndColonAndSpace() throws Exception {
        performTest("SimpleEmptyMethodBody", 907, "switch (a) {\ndefault: ", "methodBodyContentAfterDefault.pass");
    }
    
    public void testAfterDefaultKeywordAndColonAndSpace() throws Exception {
        performTest("Switch", 1010, null, "methodBodyContentAfterDefault.pass");
    }
    
    // Static initializer tests ------------------------------------------------
    
    public void testEmptyFileAfterTypingStaticKeyword() throws Exception {
        performTest("InitializersStart", 1029, "static", "staticKeyword.pass");
    }
    
    public void testAfterTypingStaticKeyword() throws Exception {
        performTest("Field", 1029, "static", "staticKeyword.pass");
    }
    
    public void testAfterStaticKeyword() throws Exception {
        performTest("Initializers", 1044, null, "staticKeyword.pass");
    }

    public void testEmptyFileAfterTypingStaticKeywordAndSpace() throws Exception {
        performTest("InitializersStart", 1029, "static ", "memberModifiersAndTypesWithoutStatic.pass");
    }
    
    public void testAfterTypingStaticKeywordAndSpace() throws Exception {
        performTest("Field", 1029, "static ", "memberModifiersAndTypesWithoutStatic.pass");
    }
    
    public void testAfterStaticKeywordAndSpace() throws Exception {
        performTest("Initializers", 1045, null, "memberModifiersAndTypesWithoutStatic.pass");
    }

    public void testEmptyFileTypingStaticBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "static {", "staticBlockContent.pass");
    }
    
    public void testTypingStaticBlockBody() throws Exception {
        performTest("Field", 1029, "static {", "staticBlockContent.pass");
    }
    
    public void testInStaticBlockBody() throws Exception {
        performTest("Initializers", 1046, null, "staticBlockContent.pass");
    }

    public void testEmptyFileTypingVarTypeInStaticBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "static {\nin", "intKeyword.pass");
    }
    
    public void testTypingVarTypeInStaticBlockBody() throws Exception {
        performTest("Field", 1029, "static {\nin", "intKeyword.pass");
    }
    
    public void testOnVarTypeInStaticBlockBody() throws Exception {
        performTest("Initializers", 1057, null, "intKeyword.pass");
    }

    public void testEmptyFileBeforeTypingVarInitInStaticBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "static {\nint i = ", "staticBlockTypesAndLocalMembers.pass");
    }
    
    public void testBeforeTypingVarInitInStaticBlockBody() throws Exception {
        performTest("Field", 1029, "static {\nint i = ", "staticBlockTypesAndLocalMembers.pass");
    }
    
    public void testBeforeVarInitInStaticBlockBody() throws Exception {
        performTest("Initializers", 1063, null, "staticBlockTypesAndLocalMembers.pass");
    }

    public void testEmptyFileTypingVarInitInStaticBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "static {\nint i = f", "falseAndFloatKeywords.pass");
    }
    
    public void testTypingVarInitInStaticBlockBody() throws Exception {
        performTest("Field", 1029, "static {\nint i = f", "falseAndFloatKeywords.pass");
    }
    
    public void testOnVarInitInStaticBlockBody() throws Exception {
        performTest("Initializers", 1064, null, "falseAndFloatKeywords.pass");
    }

    // Instance initializer tests ----------------------------------------------
    
    public void testEmptyFileTypingInitializerBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "{", "initBlockContent.pass");
    }
    
    public void testTypingInitializerBlockBody() throws Exception {
        performTest("Field", 1029, "{", "initBlockContent.pass");
    }
    
    public void testInInitializerBlockBody() throws Exception {
        performTest("Initializers", 1086, null, "initBlockContent.pass");
    }

    public void testEmptyFileTypingVarTypeInInitializerBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "{\nbo", "booleanKeyword.pass");
    }
    
    public void testTypingVarTypeInInitializerBlockBody() throws Exception {
        performTest("Field", 1029, "{\nbo", "booleanKeyword.pass");
    }
    
    public void testOnVarTypeInInitializerBlockBody() throws Exception {
        performTest("Initializers", 1097, null, "booleanKeyword.pass");
    }

    public void testEmptyFileBeforeTypingVarInitInInitializerBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "{\nboolean b1 = ", "initBlockTypesAndLocalMembers.pass");
    }
    
    public void testBeforeTypingVarInitInInitializerBlockBody() throws Exception {
        performTest("Field", 1029, "{\nboolean b1 = ", "initBlockTypesAndLocalMembers.pass");
    }
    
    public void testBeforeVarInitInInitializerBlockBody() throws Exception {
        performTest("Initializers", 1108, null, "initBlockTypesAndLocalMembers.pass");
    }

    public void testEmptyFileTypingVarInitInInitializerBlockBody() throws Exception {
        performTest("InitializersStart", 1029, "{\nboolean b1 = b", "initBlockContentStartingWithB.pass");
    }
    
    public void testTypingVarInitInInitializerBlockBody() throws Exception {
        performTest("Field", 1029, "{\nboolean b1 = b", "initBlockContentStartingWithB.pass");
    }
    
    public void testOnVarInitInInitializerBlockBody() throws Exception {
        performTest("Initializers", 1109, null, "initBlockContentStartingWithB.pass");
    }

    public void testGenericMethod1() throws Exception {
        performTest("GenericMethodInvocation", 1231, "run1(\"\", arg", "GenericMethodInvocation1.pass");
    }

    public void testGenericMethod2() throws Exception {
        performTest("GenericMethodInvocation", 1231, "run2(\"\", arg", "GenericMethodInvocation2.pass");
    }
}
