lexer grammar BladeHtmlAntlrLexer;

@header{
  package org.netbeans.modules.php.blade.syntax.antlr4.html_components;
}

@lexer::members {
    boolean tagOpened = false;
    boolean insideTag = false;
    int contentTagBalance = 0;
    int rawTagBalance = 0;
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true;
}

fragment Identifier
    : [a-z\u0080-\ufffe][a-z0-9-_\u0080-\ufffe]*;


HTML_COMPONENT_OPEN_TAG : '<x-' (Identifier (('::' | '.') Identifier)?)? {tagOpened = true;insideTag = true;};

COMPONENT_ATTRIBUTE : {insideTag == true && contentTagBalance == 0 && rawTagBalance == 0}? ':' Identifier;

GT : '>' {insideTag = false;};

BLADE_COMMENT_START : '{{--' ->pushMode(INSIDE_BLADE_COMMENT), skip;

BLADE_TAG_ESCAPE : '@' ('{')+->skip;
CONTENT_TAG_OPEN : '{{' {contentTagBalance++;}->skip;
CONTENT_TAG_CLOSE : '}}' {contentTagBalance--;}->skip;

RAW_TAG_OPEN : '{!!' {rawTagBalance++;};
RAW_TAG_CLOSE : '!!}' {rawTagBalance--;};

WS : ((' ')+ | [\r\n]+)->skip;

TAG_PART : '!' | '!!';

OTHER : . ->skip;

//==============================================

mode INSIDE_BLADE_COMMENT;

BLADE_COMMENT_END : '--}}'->popMode, skip;

BLADE_COMMENT_MORE : . ->skip;

BLADE_COMMENT_EOF : EOF->popMode, skip;