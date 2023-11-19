# Licensed to the Apache Software Foundation (ASF) under one
        # or more contributor license agreements.  See the NOTICE file
        # distributed with this work for additional information
        # regarding copyright ownership.  The ASF licenses this file
        # to you under the Apache License, Version 2.0 (the
        # "License"); you may not use this file except in compliance
        # with the License.  You may obtain a copy of the License at
        #
        #   http://www.apache.org/licenses/LICENSE-2.0
        #
        # Unless required by applicable law or agreed to in writing,
        # software distributed under the License is distributed on an
        # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        # KIND, either express or implied.  See the License for the
        # specific language governing permissions and limitations
        # under the License.



        ########### tokens #############################################################

        TOKEN:operator: (
        '?' | '(' | ')' | '[' | ']' | '{' | '}' | ':' | ',' | '.' | '=' | "<=>" |
        "==" | '!' | '~' | "!=" | '/' | "/=" | '+' | "+=" | "++" | '-' | "-=" |
        "--" | '*' | "*=" | '%' | "%=" | ">>" | ">>=" | ">>>" | ">>>=" | ">=" |
        ">" | "<<" | "<<=" | "<=" | '<' | '^' | "^=" | '|' | "|=" | "||" | '&' |
        "&=" | "&&" | ';' | '$' | ".." | "..<" | "..." | "*." | "?." | ".&" |
        "=~" | "==~" | "**" | "**=" | "->" | '@'
        )

        TOKEN:number:(
        (
        '0' ( (['x' 'X'] ['0'-'9' 'a'-'f' 'A'-'F']+) | ['0'-'9']*) |
        ['1'-'9'] ['0'-'9']*
        )
        (
        ['l' 'L' 'i' 'I' 'g' 'G'] |
        (
        '.' ['0'-'9']+ (['e' 'E'] ['+' '-']? ['0'-'9']+)? ['f' 'F' 'd' 'D' 'g' 'G']?  |
        ['e' 'E'] ['+' '-']? ['0'-'9']+ ['f' 'F' 'd' 'D' 'g' 'G']? |
        ['f' 'F' 'd' 'D']
        )
        )?
        )

        TOKEN:keyword: (
        "abstract" |
        "as" |
        "assert" |
        "boolean" |
        "break" |
        "byte" |
        "case" |
        "catch" |
        "char" |
        "class" |
        "const" |
        "continue" |
        "default" |
        "def" |
        "do" |
        "double" |
        "else" |
        "enum" |
        "extends" |
        "false" |
        "final" |
        "finally" |
        "float" |
        "for" |
        "goto" |
        "if" |
        "implements" |
        "import" |
        "in" |
        "instanceof" |
        "int" |
        "interface" |
        "long" |
        "native" |
        "new" |
        "null" |
        "package" |
        "private" |
        "property"
        "protected" |
        "public" |
        "return" |
        "short" |
        "static" |
        "strictfp" |
        "super" |
        "switch" |
        "synchronized" |
        "this" |
        "throw" |
        "throws" |
        "transient" |
        "true" |
        "try" |
        "void" |
        "volatile" |
        "while"
        )

        TOKEN:identifier:(
        ['a'- 'z' 'A'-'Z' '_'] # '\u0100'-'\uFFFE'
        (['a'- 'z' 'A'-'Z' '_' '0'-'9'])*  # '\u0100'-'\uFFFE'
        )

        TOKEN:new_line:( "\r\n" | '\r' | '\n')

        TOKEN:whitespace:( (' ' | '\t' |  #'\f' |
        '\\' ( "\r\n" | '\r' | '\n')
        )+
        )

        TOKEN:comment:("/*"):<IN_COMMENT>

<IN_COMMENT> {

        TOKEN:comment_keyword: (
        "@author" |
        "@code" |
        "@docRoot" |
        "@deprecated" |
        "@exception" |
        "@inheritDoc" |
        "@link" |
        "@linkplain" |
        "@literal" |
        "@param" |
        "@return" |
        "@see" |
        "@serial" |
        "@serialData" |
        "@serialField" |
        "@since" |
        "@throws" |
        "@value" |
        "@version"
        )

        TOKEN:comment: ([^"@""*"]+)
        TOKEN:comment: ("*"[^"/"])
        TOKEN:comment: ("*/"):<DEFAULT>
}

        TOKEN:line_comment: ("//"[^"\n""\r"]*)

        TOKEN:sh_comment:( "#!" [^"\n""\r"]*)

        TOKEN:string: (
        "\""
        ( [^ "\"" "\n" "\r"] |
        ("\\" ["r" "n" "t" "\\" "\'" "\""])
        )*
        "\""
        )

        TOKEN:string: (
        "\'"
        ( [^ "\"" "\n" "\r"] |
        ("\\" ["r" "n" "t" "\\" "\'" "\""])
        )*
        "\'"
        )




        SKIP:whitespace
        SKIP:comment
        SKIP:line_comment

        nls = (<new_line>)*;
        # Zero or more insignificant newlines, all gobbled up and thrown away,
        #  but a warning message is left for the user, if there was a newline.
        #
        nlsWarn = nls;

        /#
        S = [<sh_comment>] nls [packageDefinition | statement] (sep [statement])*;

        sep =	(';' | <new_line>) (<new_line>)*;

        packageDefinition = annotationsOpt "package" identifier;
        annotationsOpt = (annotation nls)*;

        statement = declaration |
        statementLabelPrefix (openOrClosedBlock | statement ) |
        expressionStatement |
        modifiersOpt typeDefinitionInternal |
        "if" '(' strictContextExpression ')' nlsWarn compatibleBodyStatement
        [[sep] "else" nlsWarn compatibleBodyStatement] |
        forStatement |
        "while" '(' strictContextExpression ')' nlsWarn compatibleBodyStatement |
        "with" '(' strictContextExpression ')' nlsWarn compoundStatement |
        '*' nls expressionStatement |
        importStatement |
        "switch" '(' strictContextExpression ')' nlsWarn '{' nls (casesGroup)* '}' |
        tryBlock |
        "synchronized" '(' strictContextExpression ')' nlsWarn compoundStatement |
        branchStatement;

        # A declaration is the creation of a reference or primitive-type variable,
        #  or (if arguments are present) of a method.
        #  Generically, this is called a 'variable' definition, even in the case of a class field or method.
        #  It may start with the modifiers and/or a declaration keyword "def".
        #  It may also start with the modifiers and a capitalized type name.
        #  <p>
#  AST effect: Create a separate Type/Var tree for each var in the var list.
        #  Must be guarded, as in (declarationStart) => declaration.
        #
        declaration = modifiers [typeSpec] variableDefinitions |
        typeSpec variableDefinitions;

        # A list of one or more modifier, annotation, or "def".#
        modifiers = modifiersInternal;
        modifiersInternal = ("def" nls | modifier nls | annotation nls)+;

        typeSpec = classTypeSpec | builtInTypeSpec;

        # A block body is a parade of zero or more statements or expressions.#
        blockBody = [statement] (sep [statement] )*;

        #/
        identifier = <identifier> ('.' nls <identifier>)*;
        /#
        importStatement = "import" ["static"] identifierStar;

        identifierStar = <identifier> ('.' nls <identifier>)* ['.' nls '*' | "as" nls <identifier>];

        typeDefinitionInternal = classDefinition | interfaceDefinition |
        enumDefinition | annotationDefinition;

        classDefinition = "class" <identifier> nls [typeParameters] [superClassClause] [implementsClause] classBlock;

        superClassClause = "extends" nls classOrInterfaceType nls;

        implementsClause = "implements" nls classOrInterfaceType ( ',' nls classOrInterfaceType )* nls;

        interfaceDefinition = "interface" <identifier> nls [typeParameters] [interfaceExtends] interfaceBlock;

        interfaceExtends = "extends" nls classOrInterfaceType (',' nls classOrInterfaceType)* nls;

        enumDefinition = "enum" <identifier> [implementsClause] enumBlock;

        annotationDefinition = '@' "interface" <identifier> annotationBlock;

        # The tail of a declaration.
        # Either v1, v2, ... (with possible initializers) or else m(args){body}.
        # The two arguments are the modifier list (if any) and the declaration head (if any).
        # The declaration head is the variable type, or (for a method) the return type.
        # If it is missing, then the variable type is taken from its initializer (if there is one).
        # Otherwise, the variable type defaults to 'any'.
        # DECIDE:  Method return types default to the type of the method body, as an expression.
        #
        variableDefinitions = variableDeclarator (',' nls variableDeclarator)* |
        (<identifier> | <string>) '(' parameterDeclarationList ')'
        [throwsClause] [nlsWarn openBlock];

        # A declaration with one declarator and no initialization, like a parameterDeclaration.
        #  Used to parse loops like <code>for (int x in y)</code> (up to the <code>in</code> keyword).
        #
        singleDeclarationNoInit = modifiers [typeSpec] singleVariable |
        typeSpec singleVariable;

        # Used in cases where a declaration cannot have commas, or ends with the "in" operator instead of '='.#
        singleVariable = variableName;

        # A declaration with one declarator and optional initialization, like a parameterDeclaration.
        #  Used to parse declarations used for both binding and effect, in places like argument
        #  lists and <code>while</code> statements.
        #
        singleDeclaration = singleDeclarationNoInit [varInitializer];

        # An assignment operator '=' followed by an expression.  (Never empty.)#
        varInitializer = '=' nls expression;

        modifier = "private" | "public" | "protected" | "static" | "transient" |
        "final" | "abstract" | "native" | "threadsafe" | "synchronized" |
        "volatile" | "strictfp";


        # A list of zero or more modifiers, annotations, or "def".#
        modifiersOpt = [modifiersInternal];


        builtInTypeSpec = builtInType declaratorBrackets;



        #/


        typeArguments = '<' nls typeArgument (',' nls typeArgument)* nls [typeArgumentsOrParametersEnd];
        typeArgument = typeArgumentSpec | wildcardType;
        typeArgumentsOrParametersEnd = ">" nls | ">>" nls | ">>>" nls;
        wildcardType = "?" [typeArgumentBounds];
        typeArgumentBounds = ("extends" | "super") nls classOrInterfaceType nls;
        classOrInterfaceType = <identifier> [typeArguments] ('.' <identifier> [typeArguments] )*;
        typeArgumentSpec = classTypeSpec | builtInTypeArraySpec;
        classTypeSpec = classOrInterfaceType declaratorBrackets;
        builtInTypeArraySpec = builtInType [declaratorBrackets];
        # After some type names, where zero or more empty bracket pairs are allowed.
        #  We use ARRAY_DECLARATOR to represent this.
        #  TODO:  Is there some more Groovy way to view this in terms of the indexed property syntax?
        #
        declaratorBrackets = ('[' ']')*;


        annotation = '@' identifier ['(' [annotationArguments] ')'];
        annotationArguments = annotationMemberValueInitializer | anntotationMemberValuePairs;
        annotationMemberValueInitializer = conditionalExpression | annotation;
        anntotationMemberValuePairs = annotationMemberValuePair (',' nls annotationMemberValuePair)*;
        annotationMemberValuePair = <identifier> '=' nls annotationMemberValueInitializer;

        type = classOrInterfaceType | builtInType;

        /#






        typeParameters = '<' nls typeParameter ( ',' nls typeParameter )* nls [typeArgumentsOrParametersEnd];

        classBlock = '{' [classField] (sep [classField])* '}';



        interfaceBlock
        =	'{'
        (	interfaceField
        |
        )
        ( sep
        (	interfaceField
        |
        ) )* '}'
        ;


        enumBlock
        =	'{'
        (	enumConstants
        |	(	classField
        |
        )
        )
        ( sep
        (	classField
        |
        ) )* '}'
        ;


        annotationBlock
        =	'{'
        (	annotationField
        |
        )
        ( sep
        (	annotationField
        |
        ) )* '}'
        ;


        typeParameter
        =	( <identifier> )
        (	typeParameterBounds
        |
        )

        ;


        typeParameterBounds
        =	"extends" nls classOrInterfaceType ( '&' nls classOrInterfaceType )*
        ;


        classField
        =	modifiersOpt constructorDefinition
        |	declaration
        |	modifiersOpt ( typeDefinitionInternal )
        |	"static" compoundStatement
        |	compoundStatement
        ;


        interfaceField
        =	declaration
        |	modifiersOpt ( typeDefinitionInternal )
        ;


        annotationField
        =	modifiersOpt
        (	typeDefinitionInternal
        |	typeSpec
        (	<identifier> '(' ')'
        (	"default" nls annotationMemberValueInitializer
        |
        )

        |	variableDefinitions
        )
        )
        ;



        # Comma-separated list of one or more enum constant definitions. #
        enumConstants
        =	enumConstant ( ',' nls enumConstant )*
        (	',' nls
        |
        )
        ;


        enumConstant
        =	annotationsOpt <identifier>
		(	'(' argList ')'
                |
                )
                (	enumConstantBlock
                |
                )

                ;




                enumConstantBlock
                =	'{'
                (	enumConstantField
                |
                )
                ( sep
                (	enumConstantField
                |
                ) )* '}'
                ;


                enumConstantField
                =	modifiersOpt
                (	typeDefinitionInternal
                |	(	typeParameters
                |
                )
                typeSpec
                (	<identifier> '(' parameterDeclarationList ')'
        (	throwsClause
        |
        )
        (	compoundStatement
        |
        )

        |	variableDefinitions
        )
        )
        |	compoundStatement
        ;


        # A list of zero or more formal parameters.
        #  If a parameter is variable length (e.g. String... myArg) it should be
        #  to the right of any other parameters of the same kind.
        #  General form=  (req, ..., opt, ..., [rest], key, ..., [restKeys], [block]
        #  This must be sorted out after parsing, since the various declaration forms
        #  are impossible to tell apart without backtracking.
        #
        parameterDeclarationList = [parameterDeclaration ( ',' nls parameterDeclaration )*];


        throwsClause
        =	"throws" nls identifier ( ',' nls identifier )* nls
        ;


        compoundStatement
        =	openBlock
        ;


        # I've split out constructors separately; we could maybe integrate back into variableDefinitions
        #  later on if we maybe simplified 'def' to be a type declaration?
        #
        constructorDefinition
        =	<identifier> '(' parameterDeclarationList ')'
        (	throwsClause
        |
        )
        nlsWarn constructorBody
        ;


        constructorBody
        =	'{' nls
        (	explicitConstructorInvocation
        (	sep blockBody
        |
        )
        |	blockBody
        )
        '}'
        ;


        # Catch obvious constructor calls, but not the expr.super(...) calls#
        explicitConstructorInvocation
        =	(	typeArguments
        |
        )
        (	"this" '(' argList ')'
        |	"super" '(' argList ')'
        )
        ;


        # Declaration of a variable. This can be a class/instance variable,
        #  or a local variable in a method
        #  It can also include possible initialization.
        #
        variableDeclarator
        =	variableName
        (	varInitializer
        |
        )

        ;




        # An open block is not allowed to have closure arguments.#
        openBlock
        =	'{' nls blockBody '}'
        ;


        variableName
        =	<identifier>
        ;




        # A formal parameter for a method or closure.#
        parameterDeclaration = parameterModifiersOpt [typeSpec] ["..."] <identifier> [varInitializer];

        parameterModifiersOpt = ("def" nls | "final" nls | annotation nls)*;



        # Simplified formal parameter list for closures.  Never empty.#
        simpleParameterDeclarationList = simpleParameterDeclaration ( ',' nls simpleParameterDeclaration )*;

        # A simplified formal parameter for closures, can occur outside parens.
        #  It is not confused by a lookahead of '|'.
        #  DECIDE:  Is thie necessary, or do we change the closure-bar syntax?
        #
        simpleParameterDeclaration = [typeSpec] <identifier>;


        # Closure parameters are exactly like method parameters,
        #  except that they are not enclosed in parentheses, but rather
        #  are prepended to the front of a block, just after the brace.
        #  They are separated from the closure body by a "->" token '->'.
        #
        closureParametersOpt = [parameterDeclarationList nls "->" nls | oldClosureParameters];

        # Provisional definition of old-style closure params based on BOR '|'.
        #  Going away soon, perhaps...#
        oldClosureParameters = "||" nls  |
        '|' nls '|' nls |
        ['|' nls] '(' parameterDeclarationList ')' nls '|' nls |
        ['|' nls] simpleParameterDeclarationList nls '|' nls;



        # A block which is known to be a closure, even if it has no apparent arguments.
        #  A block inside an expression or after a method call is always assumed to be a closure.
        #  Only labeled, unparameterized blocks which occur directly as substatements are kept open.
        #
        closedBlock
        =	'{' nls closureParametersOpt blockBody '}'
        ;


        # A sub-block of a block can be either open or closed.
        #  It is closed if and only if there are explicit closure arguments.
        #  Compare this to a block which is appended to a method call,
        #  which is given closure arguments, even if they are not explicit in the code.
        #
        openOrClosedBlock
        =	'{' nls closureParametersOpt blockBody '}'
        ;


        # A labeled statement, consisting of a vanilla identifier followed by a colon.#
        statementLabelPrefix
        =	<identifier> ':'
        ;


        # An expression statement can be any general expression.
        #  <p>
        #  An expression statement can also be a <em>command</em>,
        #  which is a simple method call in which the outermost parentheses are omitted.
        #  <p>
        #  Certain "suspicious" looking forms are flagged for the user to disambiguate.
        #
        expressionStatement
        =	(	checkSuspiciousExpressionStatement
        |
        )
        expression
        (	commandArguments
        |
        )

        ;



        # In Java, "if", "while", and "for" statements can take random, non-braced statements as their bodies.
        #  Support this practice, even though it isn't very Groovy.
        #
        compatibleBodyStatement
        =	compoundStatement
        |	statement
        ;


        forStatement = "for" '(' (traditionalForClause | forInClause) ')' nlsWarn compatibleBodyStatement;
        traditionalForClause = forInit ';' forCond ';' forIter;
        forInit = [declaration | controlExpressionList];
        forInClause= (singleDeclarationNoInit |	<identifier>)
        ("in" shiftExpression |':' expression );


        casesGroup
        =	( aCase )+ caseSList
        ;


        tryBlock
        =	"try" nlsWarn compoundStatement ( nls handler )*
        (	nls finallyClause
        |
        )
        ;


        # In Groovy, return, break, continue, throw, and assert can be used in a parenthesized expression context.
        #  Example:  println (x || (return));  println assert x, "won't print a false value!"
        #  If an optional expression is missing, its value is void (this coerces to null when a value is required).
        #
        branchStatement
        =	"return"
        (	expression
        |
        )
        |	(	"break"
        |	"continue"
        )
        (	statementLabelPrefix
        |
        )
        (	expression
        |
        )
        |	"throw" expression
        |	"assert" expression
        (	(	','
        |	':'
        )
        expression
        |
        )
        ;





        forCond
        =	(	strictContextExpression
        |
        )

        ;


        forIter
        =	(	controlExpressionList
        |
        )

        ;




        #
        #  If two statements are separated by newline (not ';'), the second had
        #  better not look like the latter half of an expression.  If it does, issue a warning.
        #  <p>
        #  Also, if the expression starts with a closure, it needs to
        #  have an explicit parameter list, in order to avoid the appearance of a
        #  compound statement.  This is a hard error.
        #  <p>
        #  These rules are different from Java's "dumb expression" restriction.
        #  Unlike Java, Groovy blocks can end with arbitrary (even dumb) expressions,
        #  as a consequence of optional 'return' and 'continue' tokens.
        # <p>
        #  To make the programmer's intention clear, a leading closure must have an
        #  explicit parameter list, and must not follow a previous statement separated
        #  only by newlines.
        #
        checkSuspiciousExpressionStatement
        =	(
        |
        )
        |
        |
        ;


        # A member name (x.y) or element name (x[y]) can serve as a command name,
        #  which may be followed by a list of arguments.
        #  Unlike parenthesized arguments, these must be plain expressions,
        #  without labels or spread operators.
        #
        commandArguments
        =	expression ( ',' nls expression )*
        ;


        aCase
        =	(	"case" expression
        |	"default"
        )
        ':' nls
        ;


        caseSList
        =	statement ( sep
        (	statement
        |
        ) )*
        ;


        controlExpressionList
        =	strictContextExpression ( ',' nls strictContextExpression )*
        ;


        handler
        =	"catch" '(' parameterDeclaration ')' nlsWarn compoundStatement
        ;


        finallyClause
        =	"finally" nlsWarn compoundStatement
        ;

        #/


        # EXPRESSIONS   ****************************************************************

        S = expression*;

        expression = assignmentExpression;
        assignmentExpression = conditionalExpression (assignmentOperator nls conditionalExpression)*;
        assignmentOperator = '=' |'+=' | '-=' | '*=' | '/=' | '%=' | ">>=" | ">>>=" | "<<=" | "&=" | "^=" | "|=" | '**=';
        conditionalExpression = logicalOrExpression ["?" nls assignmentExpression ':' nls conditionalExpression];
        logicalOrExpression = logicalAndExpression ("||" nls logicalAndExpression)*;
        logicalAndExpression = inclusiveOrExpression ("&&" nls inclusiveOrExpression)*;
        inclusiveOrExpression = exclusiveOrExpression ('|' nls exclusiveOrExpression)*;
        exclusiveOrExpression = andExpression ('^' nls andExpression)*;
        andExpression = regexExpression ('&' nls regexExpression)*;
        regexExpression = equalityExpression (("=~" | "==~") nls equalityExpression)*;
        equalityExpression = relationalExpression (("!=" | "==" | "<=>") nls relationalExpression)*;
        relationalExpression = shiftExpression [
        ('<' | ">" | "<=" | ">=" | "in") nls shiftExpression /#!!|
        "instanceof" nls typeSpec |
        "as" nls typeSpec#/];
        shiftExpression = additiveExpression (("<<" | ">>" | ">>>" | ".." | "..<" | "...") nls additiveExpression)*;
        additiveExpression = multiplicativeExpression (('+' | '-') nls multiplicativeExpression)*;
        multiplicativeExpression = [("++" | "--" | "-" | "+") nls] powerExpression (('*' | '/' | '%') nls powerExpression)*;
        powerExpression = unaryExpressionNotPlusMinus ( '**' nls unaryExpression )*;
        unaryExpressionNotPlusMinus = ('~' | '!') nls unaryExpression |
        #!!                              '(' builtInTypeSpec ')' unaryExpression |
        #!!                              '(' classTypeSpec ')' unaryExpressionNotPlusMinus |
        postfixExpression;
        unaryExpression = ("++" | "--" | "-" | "+") nls unaryExpression |
        unaryExpressionNotPlusMinus;
        postfixExpression = pathExpression ["++" | "--"];
        # A "path expression" is a name or other primary, possibly qualified by various
        #  forms of dot, and/or followed by various kinds of brackets.
        #  It can be used for value or assigned to, or else further qualified, indexed, or called.
        #  It is called a "path" because it looks like a linear path through a data structure.
        #  Examples:  x.y, x?.y, x*.y, x.@y; x[], x[y], x[y,z]; x(), x(y), x(y,z); x{s}; a.b[n].c(x).d{s}
        #  (Compare to a C lvalue, or LeftHandSide in the JLS section 15.26.)
        #  General expressions are built up from path expressions, using operators like '+' and '='.
        #
        pathExpression = primaryExpression (nls pathElement | nlsWarn appendedBlock)*;
        primaryExpression = <identifier> |
        constant |
        newExpression |
        "this" |
        "super" |
        parenthesizedExpression |
        closureConstructorExpression |
        listOrMapConstructorExpression |
        <string> | #stringConstructorExpression
        scopeEscapeExpression |
        builtInType;
        pathElement = ("*." | "?." | ".&" | nls '.') nls [typeArguments] namePart |
        methodCallArgs |
        appendedBlock |
        indexPropertyArgs;
        # An appended block follows any expression.
        #  If the expression is not a method call, it is given an empty argument list.
        #
        appendedBlock = ;#!!closedBlock;
        # This is the grammar for what can follow a dot:  x.a, x.@a, x.&a, x.'a', etc.
        #  Note: <code>typeArguments</code> is handled by the caller of <code>namePart</code>.
        #
        namePart = ['@'] (<identifier> | <string> | dynamicMemberName | /#!!openBlock#/ | keywordPropertyNames);
        # An expression may be followed by one or both of (...) and {...}.
        #  Note: If either is (...) or {...} present, it is a method call.
        #  The {...} is appended to the argument list, and matches a formal of type Closure.
        #  If there is no method member, a property (or field) is used instead, and must itself be callable.
        #  <p>
        #  If the methodCallArgs are absent, it is a property reference.
        #  If there is no property, it is treated as a field reference, but never a method reference.
        #  <p>
        #  Arguments in the (...) can be labeled, and the appended block can be labeled also.
        #  If there is a mix of unlabeled and labeled arguments,
        #  all the labeled arguments must follow the unlabeled arguments,
        #  except that the closure (labeled or not) is always a separate final argument.
        #  Labeled arguments are collected up and passed as a single argument to a formal of type Map.
        #  <p>
        #  Therefore, f(x,y, a:p, b:q) {s} is equivalent in all ways to f(x,y, [a:p,b:q], {s}).
        #  Spread arguments of sequence type count as unlabeled arguments,
        #  while spread arguments of map type count as labeled arguments.
        #  (This distinction must sometimes be checked dynamically.)
        #
        #  A plain unlabeled argument is allowed to match a trailing Map or Closure argument:
        #  f(x, a:p) {s}  ===  f(*[ x, [a:p], {s} ])
        #
        methodCallArgs = '(' argList ')';
        # An expression may be followed by [...].
        #  Unlike Java, these brackets may contain a general argument list,
        #  which is passed to the array element operator, which can make of it what it wants.
        #  The brackets may also be empty, as in T[].  This is how Groovy names array types.
        #  <p>Returned AST is [INDEX_OP, indexee, ELIST].
        #
        indexPropertyArgs = '[' argList ']';
        # If a dot is followed by a parenthesized or quoted expression, the member is computed dynamically,
        #  and the member selection is done only at runtime.  This forces a statically unchecked member access.
        #
        dynamicMemberName = parenthesizedExpression | <string>; #stringConstructorExpression
        # Allowed keywords after dot (as a member name) and before colon (as a label).
        #  TODO: What's the rationale for these?
        #
        keywordPropertyNames = "class" | "in" | "as" | "def" | "if" | "else" | "for" |
        "while" | "do" | "switch" | "try" | "catch" | "finally" | builtInType;
        parenthesizedExpression = '(' strictContextExpression ')';
        # Things that can show up as expressions, but only in strict
        #  contexts like inside parentheses, argument lists, and list constructors.
        #
        strictContextExpression = /#!!singleDeclaration#/ | expression | /#!!branchStatement#/ | annotation;
        # Numeric, string, regexp, boolean, or null constant.#
        constant = <number> | <string> | "true" | "false" | "null";
        builtInType = "void" | "boolean" | "byte" | "char" | "short" | "int" |
        "float" |	"long" | "double" | "any";


        # object instantiation.
        #  Trees are built as illustrated by the following input/tree pairs:
        #
        #  new T()
        #
        #  new
        #   |
        #   T --  ELIST
        #                 |
        #                arg1 -- arg2 -- .. -- argn
        #
        #  new int[]
        #
        #  new
        #   |
        #  int -- ARRAY_DECLARATOR
        #
        #  new int[] {1,2}
        #
        #  new
        #   |
        #  int -- ARRAY_DECLARATOR -- ARRAY_INIT
        #                                                                |
        #                                                              EXPR -- EXPR
        #                                                                |   |
        #                                                                1       2
        #
        #  new int[3]
        #  new
        #   |
        #  int -- ARRAY_DECLARATOR
        #                              |
        #                        EXPR
        #                              |
        #                              3
        #
        #  new int[1][2]
        #
        #  new
        #   |
        #  int -- ARRAY_DECLARATOR
        #                         |
        #               ARRAY_DECLARATOR -- EXPR
        #                         |                  |
        #                       EXPR                    1
        #                         |
        #                         2
        #
        #
        newExpression = "new" nls [typeArguments] type
        (nls methodCallArgs [appendedBlock] | appendedBlock | newArrayDeclarator);

        closureConstructorExpression = /#!!closedBlock#/;

        #
        # A list constructor is a argument list enclosed in square brackets, without labels.
        # Any argument can be decorated with a spread operator (*x), but not a label (a=x).
        # Examples:  [], [1], [1,2], [1,*l1,2], [*l1,*l2].
        # (The l1, l2 must be a sequence or null.)
        # <p>
        # A map constructor is an argument list enclosed in square brackets, with labels everywhere,
        # except on spread arguments, which stand for whole maps spliced in.
        # A colon alone between the brackets also forces the expression to be an empty map constructor.
        # Examples: [:], [a:1], [a:1,b:2], [a:1,*:m1,b:2], [*:m1,*:m2]
        # (The m1, m2 must be a map or null.)
        # Values associated with identical keys overwrite from left to right:
        # [a:1,a:2]  ===  [a:2]
        # <p>
        # Some malformed constructor expressions are not detected in the parser, but in a post-pass.
        # Bad examples: [1,b:2], [a:1,2], [:1].
        # (Note that method call arguments, by contrast, can be a mix of keyworded and non-keyworded arguments.)
        #
        listOrMapConstructorExpression = '[' argList ']' | '[' ':' ']';
        argList = [argument (',' argument)*] [','];
        # A single argument in (...) or [...].  Corresponds to to a method or closure parameter.
        #  May be labeled.  May be modified by the spread operator '*' ('*:' for keywords).
        #
        argument = [argumentLabel ':' |	'*' [':']] strictContextExpression;
        # A label for an argument is of the form a:b, 'a':b, "a":b, (a):b, etc..
        #      The labels in (a:b), ('a':b), and ("a":b) are in all ways equivalent,
        #      except that the quotes allow more spellings.
        #  Equivalent dynamically computed labels are (('a'):b) and ("${'a'}":b)
        #  but not ((a):b) or "$a":b, since the latter cases evaluate (a) as a normal identifier.
        #      Bottom line:  If you want a truly variable label, use parens and say ((a):b).
        #
        argumentLabel = <identifier> | keywordPropertyNames | primaryExpression;

        scopeEscapeExpression = '$' (<identifier> | scopeEscapeExpression);

        newArrayDeclarator = ('[' [expression] ']')+;

        #stringConstructorExpression
        #	=	STRING_CTOR_START
        #                stringConstructorValuePart ( STRING_CTOR_MIDDLE stringConstructorValuePart )* STRING_CTOR_END
        #	;
        #stringConstructorValuePart = ('*' | ) (identifier | openOrClosedBlock );

        #annotationMemberArrayValueInitializer = conditionalExpression |	annotation nls;



        ##################### LOOKAHEAD

        # Fast lookahead across balanced brackets of all sorts.#
        #balancedBrackets = '(' balancedTokens ')' | '[' balancedTokens ']' | '{' balancedTokens '}'
        #!	|	STRING_CTOR_START balancedTokens STRING_CTOR_END
        #	;

        #balancedTokens = (balancedBrackets | (	'(' |	'['  |	'{'
        ##			|	STRING_CTOR_START
        #			|	')' |	']'  |	'}'
        ##			|	STRING_CTOR_END
        #			) )*;



        # Lookahead for oldClosureParameters.#
        #oldClosureParametersStart = '|'  | "||" | '(' balancedTokens ')' nls '|' | simpleParameterDeclarationList '|';

        # A block known to be a closure, but which omits its arguments, is given this placeholder.
        #  A subsequent pass is responsible for deciding if there is an implicit 'it' parameter,
        #  or if the parameter list should be empty.
        #
        #implicitParameters =;

        # Lookahead to check whether a block begins with explicit closure arguments.#
        #closureParametersStart = oldClosureParametersStart | parameterDeclarationList nls "->" ;

        # Simple names, as in {x|...}, are completely equivalent to {(def x)|...}.  Build the right AST.#
        #closureParameter = <identifier>;

        # For lookahead only.  Fast approximate parse of an argumentLabel followed by a colon.#
        #argumentLabelStart = (<identifier> | keywordPropertyNames | <number> | <string> | balancedBrackets) ':';

        # Lookahead for suspicious statement warnings and errors.#
        #suspiciousExpressionStatementStart = ( ('+' | '-')  | ('[' | '(' | '{'));


        # Used only as a lookahead predicate, before diving in and parsing a declaration.
        #  A declaration can be unambiguously introduced with "def", an annotation or a modifier token like "final".
        #  It may also be introduced by a simple identifier whose first character is an uppercase letter,
        #  as in {String x}.  A declaration can also be introduced with a built in type like 'int' or 'void'.
        #  Brackets (array and generic) are allowed, as in {List[] x} or {int[][] y}.
        #  Anything else is parsed as a statement of some sort (expression or command).
        #  <p>
        #  (In the absence of explicit method-call parens, we assume a capitalized name is a type name.
        #  Yes, this is a little hacky.  Alternatives are to complicate the declaration or command
        #  syntaxes, or to have the parser query the symbol table.  Parse-time queries are evil.
        #  And we want both {String x} and {println x}.  So we need a syntactic razor-edge to slip
        #  between 'println' and 'String'.)
        #
        #  #TODO* The declarationStart production needs to be strengthened to recognize
        #  things like {List<String> foo}.
        #  Right now it only knows how to skip square brackets after the type, not
        #  angle brackets.
        #  This probably turns out to be tricky because of >> vs. > >. If so,
        #  just put a TODO comment in.
        #
        #declarationStart = "def" | modifier | '@' <identifier> | (upperCaseIdent | builtInType | qualifiedTypeName) ('[' balancedTokens ']' )* <identifier>;

        # Guard for enumConstants. #
        #enumConstantsStart = enumConstant (',' | ';' | (<new_line>)+ | '}' );

        # Used only as a lookahead predicate for nested type declarations.#
        #typeDeclarationStart = modifiersOpt ("class" | "interface" | "enum" | '@' "interface");


        # An <identifier> token whose spelling is required to start with an uppercase letter.
        #  In the case of a simple statement {UpperID name} the identifier is taken to be a type name, not a command name.
        #
        #upperCaseIdent = <identifier>;


        # Used to look ahead for a constructor
        #
        #constructorStart = modifiersOpt <identifier> nls '(';


        # Not yet used - but we could use something like this to look for fully qualified type names
        #
        #qualifiedTypeName = <identifier> ( '.' <identifier> )* '.' upperCaseIdent;

        #pathElementStart = ( nls '.' ) | "*." |	"?." |	".&" |	'['  |	'(' |	'{' ;


        # Definition of lexer GroovyLexer, which is a subclass of CharScanner.





        FOLD:imports:"imports"

        FOLD:ClassDeclaration

        FOLD:block:"{...}"

        HYPERLINK:identifier: org.netbeans.modules.groovysupport.Groovy.hyperlink

        BUNDLE "org.netbeans.modules.groovysupport.Bundle"

        ACTION:run_script: {
        name:"LBL_Run";
        performer:org.netbeans.modules.groovysupport.Groovy.performRun;
        enabled:org.netbeans.modules.groovysupport.Groovy.enabledRun;
        explorer:"false";
        }

        COLOR:DOLLAR: {
        foreground_color: "magenta";
        font_type:"bold";
        }

        ########### brace matching #####################################################

        BRACE "(:)"
        BRACE "{:}"
        BRACE "[:]"


        ########### indentation #####################################################

        INDENT "(:)"
        INDENT "{:}"
        INDENT "[:]"
        INDENT ".*(((->|=)\\s*)[^;,]*)"


        ########### complete #####################################################


        COMPLETE "(:)"
        COMPLETE "{:}"
        COMPLETE "\":\""
        COMPLETE "':'"
        COMPLETE "[:]"


        PROPERTIES {
        projectsViewIcon:"org/netbeans/modules/groovysupport/class.gif";
        }