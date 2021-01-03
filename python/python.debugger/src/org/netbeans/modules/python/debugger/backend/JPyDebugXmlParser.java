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
package org.netbeans.modules.python.debugger.backend;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.StringReader;

import java.util.TreeMap;
import java.util.Vector;

/**
 * this class is used to parse the network send debugging
 *         XmlStatements
 */
public class JPyDebugXmlParser extends DefaultHandler {

  private static final String _VALIDATION_ =
          "http://xml.org/sax/features/validation";
  private static final String _NAMESPACE_ =
          "http://xml.org/sax/features/namespaces";
  private static final String _SCHEMA_ =
          "http://apache.org/xml/features/validation/schema";
  private final static String _GLOBAL_ = "GLOBALS";

  /* debugger grammar */
  private static final String _WELCOME_ = "WELCOME";
  private static final String _DATAINPUT_ = "DATAINPUT";
  private static final String _TERMINATE_ = "TERMINATE";
  private static final String _STDOUT_ = "STDOUT";
  private static final String _COMMAND_ = "COMMAND";
  private static final String _SETARGS_ = "SETARGS";
  private static final String _READSRC_ = "READSRC";
  private static final String _FILEREAD_ = "FILEREAD";
  private static final String _DEBUG_ = "DEBUG";
  private static final String _COMMANDDETAIL_ = "COMMANDDETAIL";
  private static final String _LINE_ = "LINE";
  private static final String _CALL_ = "CALL";
  private static final String _RETURN_ = "RETURN";
  private static final String _ABORT_ = "ABORT";
  private static final String _STACKLIST_ = "STACKLIST";
  private static final String _THREADLIST_ = "THREADLIST";
  private static final String _STACK_ = "STACK";
  private static final String _THREAD_ = "THREAD";
  private static final String _STATE_ = "state";
  private static final String _SUSPENDED_ = "SUSPENDED";
  private static final String _BREAK_ = "BREAK";
  private static final String _VARIABLES_ = "VARIABLES";
  private static final String _VARIABLE_ = "VARIABLE";
  private static final String _EXCEPTION_ = "EXCEPTION";
  private static final String _CONTENT_ = "content";
  private static final String _RESULT_ = "result";
  private static final String _FN_ = "fn";
  private static final String _LINENO_ = "lineno";
  private static final String _LASTLINENO_ = "lastlineno";
  private static final String _LOCATION_ = "location";
  private static final String _LINECONTENT_ = "line";
  private static final String _NAME_ = "name";
  private static final String _VARTYPE_ = "vartype";
  private static final String _ARGS_ = "args";
  private static final String _RETVAL_ = "retval";
  private static final String _CMD_ = "cmd";
  private static final String _TYPE_ = "type";
  private static final String _DEBUGGEE_ = "debuggee";
  private static final String _CURRENT_ = "current";

  /* inspector grammar */
  private static final String _FILEID_ = "fileid";
  private static final String _REASON_ = "reason";
  private static final String _DOC_ = "doc";
  private final static String _IMPORTED_MODULES_ = "imported modules";
  /** Default parser name. */
  private static final String _DEFAULT_PARSER_NAME_ =
          "org.apache.xerces.parsers.SAXParser";
  /** xml parser debug result production */
  private PythonDebugEvent _production = null;
  private Vector _stackList = new Vector();
  private Vector _threadList = new Vector();
  private TreeMap _variables = new TreeMap();
  private TreeMap _types = new TreeMap();
  private boolean _inited = false;
  /** SAX parsing object */
  private XMLReader _parser;

  /**
   * convert XML replacement character patterns
   *
   * @param  attributeName
   * @param  attr
   *
   * @return
   */
  private String getAttribute(String attributeName,
          Attributes attr) {
    return attr.getValue(attributeName);
  }

  private void parse_WELCOME(Attributes attr) {
    _production.set_debuggee(getAttribute(_DEBUGGEE_, attr));
    _production.set_type(PythonDebugEvent.WELLCOME);
  }

  private void parse_DATAINPUT(Attributes attr) {
    _production.set_type(PythonDebugEvent.DATAINPUT);
  }

  private void parse_STDOUT(Attributes attr) {
    _production.set_type(PythonDebugEvent.STDOUT);
    _production.set_msgContent(getAttribute(_CONTENT_, attr));
  }

  private void parse_COMMAND(Attributes attr) {
    String cmd = getAttribute(_CMD_, attr);
    String result = getAttribute(_RESULT_, attr);
    if (cmd.startsWith(_SETARGS_)) {
      _production.set_type(PythonDebugEvent.SETARGS);
    } else if (cmd.startsWith(_READSRC_)) {
      if (result.equals(PythonDebugEvent.OK)) {
        _production.set_type(PythonDebugEvent.NOP); // just ignore we'll deal
      } // with fileread event
      else {
        _production.set_type(PythonDebugEvent.READSRC); // populate READ error
        _production.set_retval(result);
      }
    } else {
      _production.set_type(PythonDebugEvent.COMMAND);
    }
    _production.set_msgContent(result);
  }

  private void parse_DEBUG(Attributes attr) {
    _production.set_type(PythonDebugEvent.DEBUGCOMMAND);
    _production.set_msgContent(getAttribute(_RESULT_, attr));
  }

  private void parse_COMMANDDETAIL(Attributes attr) {
    _production.set_type(PythonDebugEvent.COMMANDDETAIL);
    _production.set_msgContent(getAttribute(_CONTENT_, attr));
  }

  private void parse_LINE(Attributes attr) {
    _production.set_type(PythonDebugEvent.LINE);
    _production.set_action(getAttribute(_CMD_, attr));
    _production.set_fName(getAttribute(_FN_, attr));
    _production.set_lineNo(getAttribute(_LINENO_, attr));
    _production.set_lineSource(getAttribute(_LINECONTENT_, attr));
    _production.set_name(getAttribute(_NAME_, attr));
    System.out.println("LINE entered : " + _production.get_fName() + ":" + _production.get_lineNo());
  }

  private void parse_CALL(Attributes attr) {
    _production.set_type(PythonDebugEvent.CALL);
    _production.set_action(getAttribute(_CMD_, attr));
    _production.set_fName(getAttribute(_FN_, attr));
    _production.set_args(getAttribute(_ARGS_, attr));
    _production.set_name(getAttribute(_NAME_, attr));
  }

  private void parse_RETURN(Attributes attr) {
    _production.set_type(PythonDebugEvent.RETURN);
    _production.set_action(getAttribute(_CMD_, attr));
    _production.set_fName(getAttribute(_FN_, attr));
    _production.set_retval(getAttribute(_RETVAL_, attr));
  }

  private void parse_STACKLIST(Attributes attr) {
    _production.set_type(PythonDebugEvent.STACKLIST);
    _stackList = new Vector();
    _production.set_stackList(_stackList);
  }

  private void parse_THREADLIST(Attributes attr) {
    _production.set_type(PythonDebugEvent.THREADLIST);
    _threadList = new Vector();
    _production.set_threadList(_threadList);
  }

  private void parse_VARIABLES(Attributes attr) {
    String vType = getAttribute(_TYPE_, attr);

    if (vType == null) {
      _production.set_type(PythonDebugEvent.COMPOSITE);
    } else {
      if (vType.equals(_GLOBAL_)) {
        _production.set_type(PythonDebugEvent.GLOBAL);
      } else {
        _production.set_type(PythonDebugEvent.LOCAL);
      }
    }
    _variables = new TreeMap();
    _production.set_variables(_variables);
    _types = new TreeMap();
    _production.set_types(_types);
  }

  private void parse_VARIABLE(Attributes attr) {
    String name = getAttribute(_NAME_, attr);
    String value = getAttribute(_CONTENT_, attr);
    String type = getAttribute(_VARTYPE_, attr);

    _variables.put(name, value);
    _types.put(name, type);
  }

  private void parse_FILEREAD(Attributes attr) {
    String line = getAttribute(_LINENO_, attr);
    _production.set_retval(PythonDebugEvent.OK);
    _production.set_fName(getAttribute(_FN_, attr));
    _production.set_lineNo(line);
    _production.set_type(PythonDebugEvent.READSRC);
    _production.reset_srcRead(); /* reinit buffer content */
  }

  private void parse_THREAD(Attributes attr) {
    Boolean isCurrent =
            Boolean.valueOf(getAttribute(_CURRENT_, attr));
    String state = (String) getAttribute(_STATE_, attr);
    boolean suspended = state.equals(_SUSPENDED_) ||
            state.equals(_BREAK_);
    PythonThreadInfos thInfos =
            new PythonThreadInfos(
            getAttribute(_NAME_, attr),
            isCurrent,
            suspended);
    _threadList.addElement(thInfos);
  }

  private void parse_STACK(Attributes attr) {
    _stackList.addElement(getAttribute(_CONTENT_, attr));
  }

  private void parse_EXCEPTION(Attributes attr) {
    _production.set_type(PythonDebugEvent.EXCEPTION);
    _production.set_msgContent(getAttribute(_CONTENT_, attr));
  }

  private void parse_ERROR(Attributes attr) {
    String lineNo = getAttribute(_LINENO_, attr);
    String fileId = getAttribute(_FILEID_, attr);
    String reason = getAttribute(_REASON_, attr);

    _production.set_type(PythonDebugEvent.INSPECTOR);
    _production.set_lineSource(lineNo);
    _production.set_fName(fileId);
    _production.set_msgContent(reason);

  }

  /**
   * starting xml element parsing
   *
   * @param  namespaceURI XML namespace
   * @param  localName    
   * @param  qName        
   * @param  attr         
   *
   * @throws SAXException DOCUMENT ME!
   */
  @Override
  public void startElement(
          String namespaceURI,
          String localName,
          String qName,
          Attributes attr) throws SAXException {

    // System.out.println("localName : "+localName + " qname:" +qName );
    /* DEBUGGER DTD parsing */
    if (qName.equals(_WELCOME_)) {
      parse_WELCOME(attr);
    } else if (qName.equals(_DATAINPUT_)) {
      parse_DATAINPUT(attr);
    } else if ((qName.equals(_STDOUT_))) {
      parse_STDOUT(attr);
    } else if ((qName.equals(_FILEREAD_))) {
      parse_FILEREAD(attr);
    } else if ((qName.equals(_COMMAND_))) {
      parse_COMMAND(attr);
    } else if ((qName.equals(_DEBUG_))) {
      parse_DEBUG(attr);
    } else if ((qName.equals(_COMMANDDETAIL_))) {
      parse_COMMANDDETAIL(attr);
    } else if ((qName.equals(_LINE_))) {
      parse_LINE(attr);
    } else if ((qName.equals(_CALL_))) {
      parse_CALL(attr);
    } else if ((qName.equals(_RETURN_))) {
      parse_RETURN(attr);
    } else if ((qName.equals(_STACK_))) {
      parse_STACK(attr);
    } else if ((qName.equals(_STACKLIST_))) {
      parse_STACKLIST(attr);
    } else if ((qName.equals(_THREADLIST_))) {
      parse_THREADLIST(attr);
    } else if ((qName.equals(_THREAD_))) {
      parse_THREAD(attr);
    } else if ((qName.equals(_VARIABLES_))) {
      parse_VARIABLES(attr);
    } else if ((qName.equals(_EXCEPTION_))) {
      parse_EXCEPTION(attr);
    } else if ((qName.equals(_VARIABLE_))) {
      parse_VARIABLE(attr);
    } else if ((qName.equals(_TERMINATE_))) {
      _production.set_type(PythonDebugEvent.TERMINATE);
    } else if ((qName.equals(_ABORT_))) {
      _production.set_type(PythonDebugEvent.ABORTWAITING);
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param  namespaceURI DOCUMENT ME!
   * @param  localName    DOCUMENT ME!
   * @param  qName        DOCUMENT ME!
   *
   * @throws SAXException DOCUMENT ME!
   */
  @Override
  public void endElement(String namespaceURI,
          String localName,
          String qName) throws SAXException {
  }

  /**
   * DOCUMENT ME!
   *
   * @param  ch     DOCUMENT ME!
   * @param  start  DOCUMENT ME!
   * @param  length DOCUMENT ME!
   *
   * @throws SAXException DOCUMENT ME!
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (_production.get_type() == PythonDebugEvent.READSRC) {
      _production.append_srcRead(ch, start, length);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param  parserName DOCUMENT ME!
   *
   * @throws PythonDebugException DOCUMENT ME!
   */
  public void init(String parserName) throws PythonDebugException {
    if (parserName == null) // use default if not provided
    {
      parserName = _DEFAULT_PARSER_NAME_;
    }
    try {
      _parser = XMLReaderFactory.createXMLReader(parserName);
      _parser.setFeature(_VALIDATION_, false);
      _parser.setFeature(_NAMESPACE_, false);
      _parser.setFeature(_SCHEMA_, false);
      _parser.setContentHandler(this);
      _inited = true;
    } catch (SAXException f) {
      throw new PythonDebugException("JPyDebugXmlParser init SAX INIT ERROR : " + f.getMessage());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean has_inited() {
    return _inited;
  }

  /**
   * DOCUMENT ME!
   *
   * @param  production DOCUMENT ME!
   *
   * @throws PythonDebugException DOCUMENT ME!
   */
  public void parse(PythonDebugEvent production) throws PythonDebugException {
    _production = production;

    String xmlStr = _production.toString();
    try {
      InputSource src = new InputSource();
      src.setCharacterStream(new StringReader(xmlStr));
      try {
        _parser.parse(src);
      } catch (IOException e) {
        throw new PythonDebugException("JPyDebugXmlParser parse SAX parsing IO error : " + e.getMessage());
      }
    } catch (SAXException f) {

      // provide full details on the XML parsing error
      throw new PythonDebugException(
              "NBPython backend Parser parse  SAX PARSING ERROR : " + f.getMessage() + "\n" +
              "****************** XML-SOURCE **********************\n" +
              xmlStr +
              "****************** END OF XML-SOURCE **********************\n");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param args DOCUMENT ME!
   */
  public static void main(String[] args) {
    try {

      JPyDebugXmlParser xmllibrary = new JPyDebugXmlParser();
      xmllibrary.init(null);

      String testXml = "<?xml version=\"1.0\"?>  <JPY> <WELCOME/> </JPY>";
      new PythonDebugEvent(xmllibrary, testXml);
    } catch (PythonDebugException e) {
      System.out.println("ERROR : " + e.getMessage());
    }
  }
}
