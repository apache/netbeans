parser grammar BladeHtmlAntlrParser;

@header{
  /**
   * Parser generated for netbeans blade editor
   * Some elements have been simplified to optimize parser speed
   * For example
   * - switch statement have a loos validation
   * - generic block statement "@isset" | "@unless" are grouped togehter
   * - the start match and end match will be checked in the parser
   */
  package org.netbeans.modules.php.blade.syntax.antlr4.html_components;
}

options { tokenVocab = BladeHtmlAntlrLexer; }

root : element* EOF;

element :
    HTML_COMPONENT_OPEN_TAG;