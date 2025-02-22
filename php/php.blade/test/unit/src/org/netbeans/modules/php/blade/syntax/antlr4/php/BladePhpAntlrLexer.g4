lexer grammar BladePhpAntlrLexer;
import BladePhpCommonLexer;

@header{
  package org.netbeans.modules.php.blade.syntax.antlr4.php;
}

@lexer::members {
    int rparenBalance = 0;
    int sqparenBalance = 0;
    int curlyparenBalance = 0;
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true;
}

LINE_COMMENT : LineComment->skip;

ARRAY : 'array';
AS : 'as';
ECHO : 'echo';
IF : 'if';
ELSEIF : 'elseif' | 'else if';
ELSE : 'else';
NEW : 'new';
CLASS : 'class';
FUNCTION : 'function';
LANG_CONSTRUCT : 'empty' | 'isset';
MATCH : 'match';
FOREACH : 'foreach';

COMMA : ',' ;

LPAREN : '(';
RPAREN : ')';

LSQUAREBRACKET: '[';
RSQUAREBRACKET: ']';

LCURLYBRACE: '{';
RCURLYBRACE: '}';

IDENTIFIER : Identifier;

PHP_VARIABLE : PhpVariable;

DOLLAR : '$';

NAMESPACE_SEPARATOR : '\\';
DOUBLE_COLON : '::';
ARROW : '=>';
OBJECT_OPERATOR : '->';

SEMI_COLON : ';';

COMPARISON_OPERATOR : ('==' | '!=' | '>' | '<') '='?;

LOGICAL_UNION_OPERATOR : '&&' | '||';

STRING_LITERAL : StringLiteral;


STYLE_COMMENT : '/*' .*? '*/' [\n\r]*->skip;

WS : ((' ')+ | [\r\n]+)->skip;

//testing purpose
PHP_DIRECTIVE : ('@php' | '@endphp')->skip;

OTHER : . ->skip;

