lexer grammar BladePhpCommonLexer;

tokens {
    HTML, 
    BLADE_COMMENT
}

fragment Identifier 
    : [a-z_\u0080-\ufffe][a-z0-9_\u0080-\ufffe]*;    

fragment HtmlIdentifier 
    : [a-z_\u0080-\ufffe][a-z0-9_\u0080-\ufffe-]*;    

fragment ESC_DOUBLE_QUOTED_STRING 
    : [\\"];

fragment DOUBLE_QUOTED_STRING_FRAGMENT 
    : '"' (ESC_DOUBLE_QUOTED_STRING | . )*? '"';

fragment SINGLE_QUOTED_STRING_FRAGMENT 
    : '\'' (~('\'' | '\\') | '\\' . )* '\'';

fragment StringLiteral : DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT;

fragment LineComment
    : '//' ~ [\r\n]*
    ;

fragment PhpVariable
    : '$' Identifier;


fragment Digit
    : ('0'..'9');

BLADE_COMMENT_START : '{{--' ->pushMode(INSIDE_BLADE_COMMENT), skip;

EMAIL_SUBSTRING : ('@' Identifier '.')->skip;

VERSION_WITH_AT: '@' (Digit '.'?)+->skip;

//escapes
D_ESCAPES 
    : (
      '{{{'
    |  '@@' '@'?
    | '@{' '{'?
    | '@media' [ ]+ ('screen' [ ]+ 'and'?)?
    | ( '@charset' | '@import' | '@namespace' | '@document' | '@font-face'
       | '@page' | '@layer' | '@supports' | '@tailwind' | '@apply' | '@-webkit-keyframes' 
       | '@keyframes' | '@counter-style' | '@font-feature-values' | '@property'
       | '@scope' | '@starting-style' | '@supports' | '@view-transition'
       | '@container' | '@color-profile' | '@styleset' | '@font-palette-values' | '@media'
      ) [ ]*
    )->type(HTML);

mode INSIDE_BLADE_COMMENT;

BLADE_COMMENT_END : '--}}'->popMode, skip;

//hack to merge all php inputs into one token
BLADE_COMMENT_PEEK : . {
        this._input.LA(1) == '-' &&
        this._input.LA(2) == '-' &&
        this._input.LA(3) == '}' &&
        this._input.LA(4) == '}'
      }? ->skip;
BLADE_COMMENT_MORE : . ->skip;

BLADE_COMMENT_EOF : EOF->popMode, skip;