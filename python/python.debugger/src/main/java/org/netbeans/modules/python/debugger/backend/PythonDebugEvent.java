/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.debugger.backend;

import java.util.*;

/**
 *
 * This class manages the java side Python debug event representation :
 * Python debug event are translated into internal Java Event representation
 * The resulting java object is later populated to java python event subscribers
 * 
 * @author jean-yves Mengant
 */
public class PythonDebugEvent {

  public final static String OK = "OK";
  public final static int STDOUT = 0;
  public final static int CALL = 1;
  public final static int WELLCOME = 2;
  public final static int LINE = 3;
  public final static int RETURN = 4;
  public final static int COMMAND = 5;
  public final static int ABORTWAITING = 6;
  public final static int COMMANDDETAIL = 7;
  public final static int DEBUGCOMMAND = 8;
  public final static int TERMINATE = 9;
  public final static int LAUNCHER_MSG = 10;
  public final static int LAUNCHER_ERR = 11;
  public final static int LAUNCHER_ENDING = 12;
  public final static int STACKLIST = 13;
  public final static int GLOBAL = 14;
  public final static int LOCAL = 15;
  public final static int EXCEPTION = 16;
  public final static int SETARGS = 17;
  public final static int READSRC = 18;
  public final static int NOP = 19;
  public final static int INSPECTOR = 20;
  public final static int COMPOSITE = 21;
  public final static int THREADLIST = 22;
  public final static int DATAINPUT = 23;
  /* following values are in sync with constants defined in Jnetpy.py */
  public final static int UNKNOWN = -1;
  public final static int DEBUG = 3;
  public final static int STEP = 4;
  public final static int NEXT = 5;
  public final static int RUN = 6;
  private final static String _EMPTY_ = "";
  /** origional string dbg event received from python side */
  private String _source;
  /** event type infos */
  private int _type;
  /** source file location */
  private String _sourceFile;
  /** source line infos */
  private int _line;
  /** message content */
  private String _msgContent;
  private String _fName;
  private int _lineNo;
  private String _lineSource;
  private String _name;
  private String _args;
  private String _retval;
  private int _action;
  private PythonDebugEventListener _parent = null;
  private Vector _stackList = null;
  private Vector _threadList = null;
  private TreeMap _variables = null;
  private TreeMap _types = null;
  private String _debuggee = null;
  private StringBuffer _srcRead = null;

  /**
   * process rough ip python source event
   * @param source
   */
  private void parse(JPyDebugXmlParser parser,
          String source)
          throws PythonDebugException {
    _source = source;
    _msgContent = _EMPTY_;
    _fName = null;
    _lineNo = -1;
    _lineSource = null;
    _name = null;
    _args = null;
    _retval = null;
    _stackList = null;
    _threadList = null;
    _variables = null;
    _types = null;

    if (!parser.has_inited()) {
      parser.init(null);
    }

    parser.parse(this);
  }

  public StringBuffer get_srcRead() {
    return _srcRead;
  }

  public void reset_srcRead() {
    _srcRead = new StringBuffer();
  }

  public void append_srcRead(char[] candidate, int offset, int length) {
    _srcRead.append(candidate, offset, length);
  }

  public int get_type() {
    return _type;
  }

  public void set_type(int type) {
    _type = type;
  }

  public String get_msgContent() {
    return _msgContent;
  }

  public void set_msgContent(String content) {
    _msgContent = content;
  }

  public String get_fName() {
    return _fName;
  }

  public void set_fName(String fName) {
    _fName = fName;
  }

  public void set_lineNo(String lineno) {
    _lineNo = -1;
    try {
      _lineNo = Integer.parseInt(lineno);
    } catch (NumberFormatException e) {
    }
  }

  public int get_action() {
    return _action;
  }

  public void set_action(String action) {
    _action = UNKNOWN;
    try {
      _action = Integer.parseInt(action);
    } catch (NumberFormatException e) {
    }
  }

  public int get_lineNo() {
    return _lineNo;
  }

  public PythonDebugEvent(JPyDebugXmlParser parser, String source)
          throws PythonDebugException {
    parse(parser, source);
  }

  public PythonDebugEvent(int type, String msg) {
    _type = type;
    _msgContent = msg;
  }

  public void set_lineSource(String lineSource) {
    _lineSource = lineSource;
  }

  public String get_lineSource() {
    return _lineSource;
  }

  public void set_name(String name) {
    _name = name;
  }

  public String get_name() {
    return _name;
  }

  public void set_args(String args) {
    _args = args;
  }

  public String get_args() {
    return _args;
  }

  public void set_retval(String retval) {
    _retval = retval;
  }

  public String get_retVal() {
    return _retval;
  }

  public void set_stackList(Vector stackList) {
    _stackList = stackList;
  }

  public Vector get_stackList() {
    return _stackList;
  }

  public void set_threadList(Vector threadList) {
    _threadList = threadList;
  }

  public Vector get_threadList() {
    return _threadList;
  }

  public void set_variables(TreeMap variables) {
    _variables = variables;
  }

  public TreeMap get_variables() {
    return _variables;
  }

  public void set_types(TreeMap types) {
    _types = types;
  }

  public TreeMap get_types() {
    return _types;
  }

  public void set_debuggee(String debuggee) {
    _debuggee = debuggee;
  }

  public String get_debuggee() {
    return _debuggee;
  }

  @Override
  public String toString() {
    return _source;
  }

  public String get_sourceFile() {
    return _sourceFile;
  }

  public int get_line() {
    return _line;
  }

  public void set_parent(PythonDebugEventListener parent) {
    _parent = parent;
  }

  public PythonDebugEventListener get_parent() {
    return _parent;
  }
}
