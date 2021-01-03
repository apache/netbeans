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
package org.netbeans.modules.python.debugger.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import org.openide.util.Utilities;


import javax.swing.*;
import javax.swing.border.*;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.python.api.PythonFileEncodingQuery;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.debugger.CompositeCallback;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.debugger.PythonDebugParameters;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.modules.python.debugger.backend.DebuggerContextChangeListener;
import org.netbeans.modules.python.debugger.backend.PluginEvent;
import org.netbeans.modules.python.debugger.actions.PluginEventListener;
import org.netbeans.modules.python.debugger.backend.PythonDebugClient;
import org.netbeans.modules.python.debugger.backend.PythonDebugEvent;
import org.netbeans.modules.python.debugger.backend.PythonDebugEventListener;
import org.netbeans.modules.python.debugger.backend.PythonDebugException;
import org.netbeans.modules.python.debugger.backend.PythonThreadInfos;

import org.netbeans.modules.python.debugger.backend.StackInfo;
import org.netbeans.modules.python.debugger.breakpoints.PythonBreakpoint;
import org.netbeans.modules.python.debugger.spi.PythonSession;
import org.netbeans.modules.python.debugger.utils.AnimatedCursor;
import org.netbeans.modules.python.debugger.utils.CommandLineEvent;
import org.netbeans.modules.python.debugger.utils.CommandLineListener;
import org.netbeans.modules.python.debugger.utils.MiscStatic;
import org.netbeans.modules.python.debugger.utils.Swing;
import org.netbeans.modules.python.debugger.utils.SwingEhnStatusBar;
import org.openide.text.Line;

/**
 * Main Python container Panel class
 */
public class PythonDebugContainer implements PythonContainer {

  private final static String _PYTHONPATH_ = "PYTHONPATH.txt";
  private final static String _JYTHONPATH_ = "JYTHONPATH.txt";
  public final static String VERSION = "nbPython debugger v0.0.1-001";
  public final static int INACTIVE = 0;
  public final static int STARTING = 1;
  public final static int STARTED = 2;
  private final static String _END_OF_LINE_ = "\n";
  private final static String _INACTIVE_TEXT_ = "inactive";
  private final static String _READY_TEXT_ = "ready";
  private final static String _BUSY_TEXT_ = "busy";
  // private final static String _STRING_        = "<string>";
  private final static String _EOL_ = "/EOL/";
  private final static String _OK_ = "OK";
  private final static String _INPROGRESS_ = "INPROGRESS";
  private final static String _ENDED_ = "ENDED";
  private final static String _COMMAND_ = "CMD ";
  private final static String _BPSET_ = "BP+ ";
  private final static String _BPCLEAR_ = "BP- ";
  private final static String _DBG_ = "GLBCMD DBG ";
  private final static String _SETARGS_ = "GLBCMD SETARGS ";
  private final static String _READSRC_ = "GLBCMD READSRC ";
  private final static String _NEXT_ = "NEXT ";
  private final static String _STEP_ = "STEP ";
  private final static String _STEP_OUT_ = "STEPOUT ";
  private final static String _RUN_ = "RUN ";
  private final static String _STOP_ = "GLBCMD STOP ";
  private final static String _STACK_ = "STACK ";
  private final static String _THREAD_ = "THREAD ";
  private final static String _GLOBALS_ = "GLOBALS ";
  private final static String _EQUAL_ = "=";
  private final static String _SEMICOLON_ = ";";
  private final static String _SILENT_ = "silent";
  private final static String _LOCALS_ = "LOCALS ";
  private final static String _COMPOSITE_ = "COMPOSITE ";
  private final static String _LASTFRAME_ = "<LastFrame>";
  private final static String _SPACE_ = " ";
  private final static String _NONE_ = "None";
  public final static PythonVariableTreeDataNode ROOTNODE =
          PythonVariableTreeDataNode.buildDataNodes(null, "", "",
          PythonVariableTreeDataNode.COMPOSITE);
  private final static String _SHOWPROD_ =
          "org/netbeans/modules/python/debugger/resources/showprod.gif";
  private final static ImageIcon _DBGTAB_ICON_ =
          new ImageIcon(Utilities.loadImage(_SHOWPROD_), "showprod");
  private final static String _BUSYLL_ =
          "org/netbeans/modules/python/debugger/resources/busy.gif";
  private final static ImageIcon _BUSY_ =
          new ImageIcon(Utilities.loadImage(_BUSYLL_), "busy");
  private final static String _INACTIVEL_ =
          "org/netbeans/modules/python/debugger/resources/stopped.gif";
  private final static ImageIcon _INACTIVE_ =
          new ImageIcon(Utilities.loadImage(_INACTIVEL_), "inactive");
  private final static String _ACTIVEL_ =
          "org/netbeans/modules/python/debugger/resources/running.gif";
  private final static ImageIcon _ACTIVE_ =
          new ImageIcon(Utilities.loadImage(_ACTIVEL_), "active");
  private final static String _IMPNAVL_ =
          "org/netbeans/modules/python/debugger/resources/impnav.gif";
  public final static ImageIcon IMPNAV_ICON =
          new ImageIcon(Utilities.loadImage(_IMPNAVL_), "impnav");
  private final static String _ERRORL_ =
          "org/netbeans/modules/python/debugger/resources/error.gif";
  public final static ImageIcon ERROR_ICON =
          new ImageIcon(Utilities.loadImage(_ERRORL_), "error");
  /** ckient debug agent */
  private PythonDebugClient _pyClient = new PythonDebugClient();
  /** debug pane Stdout container */
  private SwingEhnStatusBar _msgBar = new SwingEhnStatusBar();
  // private DebugToolbar _dbgToolbar = new DebugToolbar();
  private _REPORT_TABPANE_ _reportTab = new _REPORT_TABPANE_();
  private PythonOutputPanel _setoutPane;
  private boolean _insideStack = false;
  private _STATUS_BAR_ _statusBar;
  private boolean _debugging = false;
  private boolean _newSource = false;
  //private ActionListener     _sendCommandListener = new _SEND_COMMAND_();
  // private EditableEnterCombo _command             =
  //  _dbgToolbar.buildCombo(DebugEvent.COMMANDFIELD, true, _sendCommandListener);
  //private JButton            _dbgSend             =
  //  _dbgToolbar.buildButton(DebugEvent.SENDCOMMAND, _COMMAND_ICON_, null);
  private JComboBox _stack = new JComboBox();
  private _STACK_MANAGER_ _stackM = new _STACK_MANAGER_();
  private _LOCAL_VARIABLES_ _locals;
  private _GLOBAL_VARIABLES_ _globals;
  private _DEBUGEVENT_MANAGER_ _evtListener = null;
  // current debugging script start arguments
  private String _scriptArgs = null;
  // private Hashtable _breakpoints      = new Hashtable();
  private Hashtable<String, String> _changedVariables = new Hashtable<>();
  // private boolean   _bpPopulated      = false;
  /** current composite introspection action */
  private CompositeCallback _cCallback = null;
  /** current debugger state */
  private int _state = INACTIVE;
  /** where IDE get populated with debugging events */
  private PluginEventListener _IDEPlug = null;
  /** parent container frame */
  private Container _parent;
  /** current session style */
  private boolean _remoteSession = false;
  private Debuggee _curDebuggee = null;
  /** Python Thread List Vector */
  private _THREAD_MANAGER_ _threads = new _THREAD_MANAGER_();
  /** Python Local Variables Manager */
  private _VARIABLES_MANAGER_ _variables = new _VARIABLES_MANAGER_();

  public PythonDebugContainer(Container container) {
    _parent = container;
    if (container != null) {

      // check if parent want beeing populated with debug events
      if (_parent instanceof PluginEventListener) {
        _IDEPlug = (PluginEventListener) _parent;
      }
      container.setLayout(new BorderLayout());
      container.add(BorderLayout.CENTER, new _MAIN_PANEL_());
    }
  }

  private void setContext(int context) {
    switch (context) {
      case STARTED:
        _statusBar.setRunning();

        break;

      case INACTIVE:
        _statusBar.setNotRunning();
        break;
    }
  }

  /**
   * populate a beakpoint setting to Python DBG server
   *
   * @param source
   * @param line
   */
  public void setBreakPoint(String source,
          int line,
          boolean temp,
          String condition,
          int filter,
          HIT_COUNT_FILTERING_STYLE fst) {
    // storeBreakPoint(source, line);
    if (_state != INACTIVE) {
      String fstStyle = null;
      if (fst != null) {
        fstStyle = fst.toString();
      }
      // server running populate breakpoint
      breakpointSubcommand(_BPSET_, source, line, temp, condition, filter, fstStyle);
    }
  }

  /**
   * clear a breakpoint on server
   *
   * @param source
   * @param line
   */
  public void clearBreakPoint(String source, int line) {
    // releaseBreakPoint(source, line);
    if (_state != INACTIVE) // server running populate breakpoint
    {
      breakpointSubcommand(_BPCLEAR_, source, line, false, null, -1, null);
    }
  }

  private void debugSubcommand(String subcommand) {
    if (    subcommand.equals(_STEP_) ||
            subcommand.equals(_STEP_OUT_) ||
            subcommand.equals(_RUN_) ||
            subcommand.equals(_NEXT_)) {
      _statusBar.setBusy();
    }
    try {

      // populate user's variable changes though debugging interface
      populateVariableChanges();
      _pyClient.sendCommand(subcommand);
    } catch (PythonDebugException ex) {
      _msgBar.setError("debug subcommand failed : " + ex.getMessage());
    }
  }

  private String buildConditionString(String condition) {
    char quote = '\'';
    StringBuffer returned = new StringBuffer();

    if (condition.indexOf('\'') != -1) {
      quote = '"';
    }

    returned.append(quote);
    returned.append(condition);
    returned.append(quote);

    return returned.toString();
  }

  private void breakpointSubcommand(String fx,
          String source,
          int line,
          boolean temp,
          String condition,
          int filter,
          String filterStyle) {
    StringBuffer sent = new StringBuffer(fx);

    // make local / remote fname translation
    if (_evtListener != null) {
      source = _evtListener.getBpFName(source);
    }

    sent.append(source);

    if (line != -1) {
      sent.append(_SPACE_);
      sent.append(line);
    }

    // temporary breakpoints
    sent.append(_SPACE_);
    if (temp) {
      sent.append('1');
    } else {
      sent.append('0');
    }

    sent.append(_SPACE_);
    // conditional stuff
    if (condition == null) {
      sent.append(_NONE_);
    } else {
      sent.append(buildConditionString(condition));
    }

    if (filter > 0) {
      sent.append(_SPACE_);
      sent.append(filter);
      sent.append(_SPACE_);
      sent.append(filterStyle);
    }

    try {
      _pyClient.sendCommand(sent.toString());
    } catch (PythonDebugException ex) {
      _msgBar.setError("breakpoint subcommand failed : " + ex.getMessage());
    }

  }

  private String convertDosFiles(String candidate) {
    // safelly convert dos \ in / to avoid later control character unwanted
    // conversion
    if (System.getProperty("os.name").startsWith("Windows")) {
      return candidate.replace('\\', '/');
    }
    return candidate;
  }

  private void launchDebug(String candidate) {
    try {
      // send DBG fname
      _pyClient.sendCommand(_DBG_ + convertDosFiles(candidate));
      _debugging = true;
    } catch (PythonDebugException ex) {
      _msgBar.setError("launchDebug failed : " + ex.getMessage());
    }
  }

  private void readSrc(String candidate, int lineno) {
    try {
      _pyClient.sendCommand(_READSRC_ + candidate + _SPACE_ +
              Integer.toString(lineno));
    } catch (PythonDebugException ex) {
      _msgBar.setError("readDebug failed : " + ex.getMessage());
    }
  }

  private void setArgsCommand(String args) {
    try {
      _pyClient.sendCommand(_SETARGS_ + args);
    } catch (PythonDebugException ex) {
      _msgBar.setError("SETARGS command failed : " + ex.getMessage());
    }
  }

  private void threadCommand() {
    try {
      _pyClient.sendCommand(_THREAD_);
    } catch (PythonDebugException ex) {
      _msgBar.setError("THREAD command failed : " + ex.getMessage());
    }
  }

  private void stackCommand() {
    try {
      _pyClient.sendCommand(_STACK_);
    } catch (PythonDebugException ex) {
      _msgBar.setError("STACK command failed : " + ex.getMessage());
    }
  }

  private void variableCommand(String command) {
    try {
      _pyClient.sendCommand(command);
    } catch (PythonDebugException ex) {
      _msgBar.setError("VARIABLE command failed : " + ex.getMessage());
    }
  }

  @Override
  public void inspectCompositeCommand(CompositeCallback callBack,
          String varName) {
    try {
      _cCallback = callBack;
      if (varName.length() == 0) // ROOT
      {
        _pyClient.sendCommand(_LOCALS_);
      } else {
        _pyClient.sendCommand(_COMPOSITE_ + varName);
      }
    } catch (PythonDebugException ex) {
      _msgBar.setError("INSPECT command failed : " + ex.getMessage());
    }
  }

  private void populateVariableChanges() {
    if (_changedVariables.isEmpty()) {
      return; // nothing to do
    }
    StringBuffer buffer = new StringBuffer(_COMMAND_);
    buffer.append(_SPACE_);
    buffer.append(_SILENT_);
    buffer.append(_SPACE_);

    Enumeration keys = _changedVariables.keys();
    while (keys.hasMoreElements()) {
      String var = (String) keys.nextElement();
      buffer.append(var);
      buffer.append(_EQUAL_);
      buffer.append((String) _changedVariables.get(var));
      buffer.append(_SEMICOLON_);
    }
    try {
      _pyClient.sendCommand(buffer.toString());

      // cleanup container
      _changedVariables = new Hashtable<>();
    } catch (PythonDebugException ex) {
      _msgBar.setError("SET VARIABLE command failed : " + ex.getMessage());
    }
  }


  @Override
  public void dbgVariableChanged(String name, String value, boolean global) {
    _changedVariables.put(name, value);
  }

  /**
   * entered when Editor has changed parameters
   */
  public void parametersChanged(PythonOptions options) {
    _setoutPane.checkColoringChanges(options);
  }

  public PythonDebugClient get_pyClient() {
    return _pyClient;
  }

  public void terminate() throws PythonDebugException {

    // if debugger connection is alive
    if (_state != INACTIVE) {
      _pyClient.sendCommand(_STOP_ + _END_OF_LINE_);
    }
  }

  /**
   * used for SHORTCUT activation
   */
  public void activateStepInto() {
    if (_state != INACTIVE) {
      debugSubcommand(_STEP_);
    }

  }

  public void activateStepOut() {
    if (_state != INACTIVE) {
      debugSubcommand(_STEP_OUT_);
    }

  }

  public void activateNext() {
    if (_state != INACTIVE) {
      debugSubcommand(_NEXT_);
    }
  }

  public void activateContinue( boolean toCursor ) {
    if (_state != INACTIVE) {
      if (toCursor) {
        // just put a temporary breakpoint at current cursor line location
        // if Run To Cursor is requested
        Line cursor = Utils.getCurrentLine();
        int lineLoc = cursor.getLineNumber()+1;
        setBreakPoint(_curDebuggee.getScript(),
                lineLoc,
                true,
                null,
                -1,
                HIT_COUNT_FILTERING_STYLE.EQUAL);
      }
      debugSubcommand(_RUN_);
    }
  }

  public void writeLog(String msg) {
    _setoutPane.writeLog(msg);
  }

  /**
   * start a new JpyDbg session (NB:calling THREAD will be blocked on the tcpip
   * )
   */
  public void startDebuggerInstance(PythonSession debugSession) {
    _scriptArgs = debugSession.get_scriptArgs();
    _curDebuggee = debugSession.getDebuggee();
    _DEBUGGING_STARTER_ starter =
            new _DEBUGGING_STARTER_(
            _curDebuggee,
            debugSession.isRemote() // PythonDebugParameters.get_jpydbgScript()
            //_curDebuggee.getScript()
            //debugSession.getOriginatingScript().getAbsolutePath()
            );
    starter.toRun();
  }

  public void terminateSession() {
    _DEBUGGING_TERMINATOR_ terminator = new _DEBUGGING_TERMINATOR_();
    terminator.start();
  }

  class _DEBUGEVENT_MANAGER_ implements PythonDebugEventListener {

    private PluginEventListener _plug;
    /** current source under debug */
    private String _currentSource = null;
    /** current debugging source is remote */
    private boolean _remoteSource = false;
    /** remote source name / local tmp source location table */
    private Hashtable<String, String> _hashSource = new Hashtable<>();
    /** remote local tmp source / remote source name location table */
    private Hashtable<String, String> _remoteHashSource = new Hashtable<>();
    private int _currentLine = -1;

    /* debug over FTP connection if not null */
    private String _ftpSource = null;

    public _DEBUGEVENT_MANAGER_(PluginEventListener pluggin) {
      _plug = pluggin;
    }

    private void dealWithCall(PythonDebugEvent e) {
      if (_plug == null) {
        _setoutPane.writeMessage(e.toString());
      }
    }

    public String getBpFName(String fName) {
      String remoteRef =
              (String) _remoteHashSource.get(MiscStatic.nonSensitiveFileName(fName));
      if (remoteRef != null) {
        return remoteRef;
      }

      return fName;
    }

    private void dealWithReturn(PythonDebugEvent e) {
      //_callLevel--;

      // end of python Program Reached
      if (e.get_retVal().equals(_LASTFRAME_)) {
        _DEBUGGING_TERMINATOR_ terminator = new _DEBUGGING_TERMINATOR_();
        terminator.start();
      }
      if (_plug == null) {
        _setoutPane.writeMessage(e.toString());
      }
    }

    public void populateToPlugin(int event,
            String localFName,
            int lineNo) {
      try {
        if (_plug != null) {
          _plug.newDebuggingEvent(new PluginEvent(event, localFName, lineNo));
        }
      } catch (PythonDebugException ex) {
        _msgBar.setError(ex.getMessage());
      }
    }

    private boolean isBreakPoint(String source, int lineNo) {
      Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
      for (Breakpoint bp : breakpoints) {
        if (bp instanceof PythonBreakpoint) {
          PythonBreakpoint pyBp = (PythonBreakpoint) bp;
          if ((pyBp.isEnabled()) &&
                  (pyBp.getFilePath().equals(source)) &&
                  (pyBp.getLineNumber() == lineNo)) {
            return true;
          }
        }
      }
      return false;
    }

    private void populateLocalSource(PythonDebugEvent e) {
      if (e.get_fName() != null) {
        if (_newSource) {
          populateToPlugin(PluginEvent.NEWSOURCE,
                  e.get_fName(),
                  e.get_lineNo());
          // prevent stopAtFirstLine if not requested in pyOptions
          PythonOptions pyOptions = PythonOptions.getInstance();
          if ((!pyOptions.getStopAtFirstLine()) && _state == STARTING) {
            // we must ckeck for BP set on that line before running
            if (isBreakPoint(e.get_fName(), e.get_lineNo())) {
              populateToPlugin(PluginEvent.NEWLINE, e.get_fName(), e.get_lineNo());
            } else {
              debugSubcommand(_RUN_);
            }
          }
        } else if (_currentLine != e.get_lineNo()) {
          populateToPlugin(PluginEvent.NEWLINE, e.get_fName(), e.get_lineNo());
        }
      }
    }

    private void startupSourceInit(PythonDebugEvent e) {
      if (_plug != null) {
        try {
          if (_remoteSource) {
            String localName = buildTempSource(e);

            // remote debugging name switch
            e.set_fName(localName);
          }
          if (_ftpSource != null) // ftp debugging case
          {
            e.set_fName(_ftpSource);
          }
          populateLocalSource(e);
          _newSource = false;
        } catch (PythonDebugException ex) {
          _msgBar.setError(ex.getMessage());
        }
      }

    }

    private void startDebugger(PythonDebugEvent e) {
      if (e.get_lineNo() == 0) // reaching first position in source
      {
        // go ahead on <string> at startup using _STEP_
        debugSubcommand(_STEP_);
        _newSource = true;
      } else {
        startupSourceInit(e);
        // send BP set collection to server side debugger
        //if (!_bpPopulated)
        //  populateBreakPoints();
        setContext(STARTED);
      }
      if (_plug == null) {
        _setoutPane.writeMessage(e.toString());
      }
    }

    private void debuggingSessionIsOver() {
      _state = INACTIVE;
      _ftpSource = null;
      // _bpPopulated = false;
      setContext(INACTIVE);
      _statusBar.display(_INACTIVE_TEXT_);
      try {
        _pyClient.terminate();
      } catch (PythonDebugException evt) {
        _msgBar.setError(evt.getMessage());
      }

      if (_plug != null) {
        populateToPlugin(PluginEvent.ENDING, null, PluginEvent.UNDEFINED);
      }
    }

    @Override
    public void launcherMessage(PythonDebugEvent e) {
      synchronized (this) {
        switch (e.get_type()) {

          case PythonDebugEvent.LAUNCHER_ENDING:
            if (!e.get_msgContent().equals("0")) {
              _setoutPane.writeError("Debug session Abort =" +
                      e.get_msgContent());
              _msgBar.setError("python jpydaemon launcher ABORTED");
            } else {
              _setoutPane.writeHeader("Debug session normal end");
              _setoutPane.pauseShell();
            }
            if (_state != INACTIVE) // force termination on this side
            {
              debuggingSessionIsOver();
            }

            break;

          case PythonDebugEvent.LAUNCHER_ERR:
            _setoutPane.writeError(e.get_msgContent());
            _msgBar.setError("python jpydaemon launcher SEVERE ERROR");

            break;

          case PythonDebugEvent.LAUNCHER_MSG:
            _setoutPane.writeWarning(e.get_msgContent());

            break;

          default:

            // pass message to plugging handle
            _setoutPane.writeMessage("unmanaged DebugEvent : " + e.toString());
        }
      }
    }

    private String extractFileName(String fName) {
      int slashPos = fName.lastIndexOf('/');
      int backslashPos = fName.lastIndexOf('\\');
      if (slashPos != -1) {
        return fName.substring(slashPos + 1);
      }
      if (backslashPos != -1) {
        return fName.substring(backslashPos + 1);
      }

      return fName;
    }

    /**
     * get and build the temporary file resource
     *
     * @param  e current debugging event containing the original source
     *
     * @return the source file stored in local temp file
     */
    private String buildTempSource(PythonDebugEvent e)
            throws PythonDebugException {
      String curTmp = (String) _hashSource.get(e.get_fName());

      // first try to get the resource from the local hash if not the first time
      if (curTmp != null) {
        return curTmp;
      }

      // download and store locally
      String tmpDir = PythonDebugParameters.get_workDir();
      if (tmpDir == null) {
        throw new PythonDebugException("temporary workspace directory undefine check jpydbgoptions");
      }

      File tmpDirFile = new File(tmpDir);
      if (!tmpDirFile.isDirectory()) {
        throw new PythonDebugException(tmpDir +
                " workspace tmp dir is not an existing directory ");
      }

      // store file locally
      File localfName = new File(tmpDirFile, extractFileName(e.get_fName()));
      _hashSource.put(e.get_fName(), localfName.getAbsolutePath());
      _remoteHashSource.put(MiscStatic.nonSensitiveFileName(localfName.getAbsolutePath()),
              e.get_fName());

      readSrc(e.get_fName(), e.get_lineNo());

      return null; // waiting for READSRC event to complete
    }

    private void storeRemoteFile(PythonDebugEvent e) {
      String localSource = (String) _hashSource.get(e.get_fName());
      _currentSource = null; // force reloading
      try {
        e.set_fName(localSource);

        PrintWriter wr = new PrintWriter(new FileWriter(localSource));
        wr.write(e.get_srcRead().toString());
        wr.close();

        populateLocalSource(e);
        _currentSource = localSource;
      } catch (IOException ex) {
        _msgBar.setError("IOERROR storing remote dbg file : " + localSource +
                " : " + ex.getMessage());
      }
    }

    private void formatException(String toFormat) {
      StringBuffer fmt = new StringBuffer(toFormat);
      StringBuffer dest = new StringBuffer();
      int ii = 0;
      while (ii < fmt.length()) {
        if ((fmt.charAt(ii) == '\\') && (fmt.charAt(ii + 1) == 'n')) {
          _setoutPane.writeError(dest.toString());
          dest = new StringBuffer();
          ii = ii + 2;
        } else {
          dest.append(fmt.charAt(ii++));
        }
      }
      if (dest.length() > 0) {
        _setoutPane.writeError(dest.toString());
      }
    }

    @Override
    public void newDebugEvent(PythonDebugEvent e) {
      switch (e.get_type()) {

        case PythonDebugEvent.ABORTWAITING:
          _statusBar.display(_INACTIVE_TEXT_);
          _statusBar.setConnected();
          _state = INACTIVE;
          _debugging = false;
          // populate debugging stop
          if (_plug != null) {
            populateToPlugin(PluginEvent.ENDING, null, PluginEvent.UNDEFINED);
          }

          break;

        case PythonDebugEvent.WELLCOME:
          _state = STARTING;
          _newSource = true;

          // cleanup any previous remote session flag
          _remoteSource = false;

          // check for remote source
          if (e.get_debuggee() != null) {
            _remoteSource = true;
            _hashSource = new Hashtable(); // cleanup remote debugging
            // source hash
            _remoteHashSource = new Hashtable();
          }

          // populate debugging stop
          if (_plug != null) {
            populateToPlugin(PluginEvent.STARTING, null, PluginEvent.UNDEFINED);
          }
          _statusBar.setConnected();
          _statusBar.display(_READY_TEXT_);
          setContext(STARTED);


          // check for candidate to debug after initial connection
          String candidate = _curDebuggee.getScript();
          if (candidate.length() != 0) {
            // populate debuggee command line arguments
            setArgsCommand(_scriptArgs);
            launchDebug(candidate);
          } else {
            _msgBar.setWarning("no python debuggee provided ");
          }
          break;

        case PythonDebugEvent.EXCEPTION:
          // debugging session in error
          _statusBar.resetBusy();
          formatException(e.get_msgContent());
          _msgBar.setWarning("exception raising in Debuggee see 'stdout content' for complementary details");
          // Terminate debugging when exception is raised in debuggee
          // by sending a STOP request

          try {
            terminate();
          } catch (PythonDebugException f) {
            _msgBar.setError(f.getMessage());
          }
          break;

        case PythonDebugEvent.STDOUT:
          StringBuffer buf = new StringBuffer();
          String content = e.get_msgContent();
          if (content.length() > 0) {
            if (content.equals(_EOL_)) {
              buf.append(_END_OF_LINE_);
            } else {
              buf.append("[stdout:]");
              buf.append(content);
            }
            _setoutPane.headerAppend(buf.toString());
          }
          break;

        case PythonDebugEvent.DEBUGCOMMAND:

          String result = e.get_msgContent();
          if (result.equals(_INPROGRESS_)) // do Nothing if debugging is in progress
          ; else if (result.equals(_ENDED_)) {
            debuggingSessionIsOver();
          } else // debugging session in error on startup
          {
            _msgBar.setError(e.get_msgContent());
          }

          break;

        case PythonDebugEvent.COMMAND:

          String returned = e.get_msgContent();

          // silent OK returned for internal launched commands
          // are not displayed
          if (!returned.equals(_SILENT_)) {
            if (!returned.equals("OK")) {
              _setoutPane.writeHeader(e.get_msgContent());
            }
          }
          break;

        case PythonDebugEvent.SETARGS:

          // nothing to handle here
          break;

        case PythonDebugEvent.NOP:

          // nothing to handle here
          break;

        case PythonDebugEvent.COMMANDDETAIL:
          _setoutPane.writeError(e.get_msgContent());
          break;

        case PythonDebugEvent.READSRC:
          if (e.get_retVal().equals(PythonDebugEvent.OK)) {
            storeRemoteFile(e);
          } else {
            _msgBar.setError("REMOTE READ Failure on : " + e.get_fName() +
                    " : " + e.get_retVal());
          }

          break;


        case PythonDebugEvent.LINE:
          if (_state == STARTING) {
            startDebugger(e);
          }
          _statusBar.resetBusy();
          _currentLine = e.get_lineNo();
          _currentSource = e.get_fName();
          if (_insideStack) // we must refresh the current stack pane content
          {
            stackCommand();
          }

          if (_threads.isListeningBack()) // refresh threading as well if thread view is active
          {
            _threads.refreshThreads();
          }
          if (_stackM.isListeningBack()) // refresh stack view
          {
            _stackM.refreshStack();
          }
          if (_variables.isListeningBack()) // refresh variables view
          {
            _variables.refreshVars();
          }
          break;

        case PythonDebugEvent.CALL:
          dealWithCall(e);
          break;

        case PythonDebugEvent.RETURN:
          dealWithReturn(e);

          break;

        case PythonDebugEvent.TERMINATE:
          debuggingSessionIsOver();

          break;

        case PythonDebugEvent.STACKLIST:
          assert (e.get_stackList() != null);
          _stack.removeAllItems();
          _stackM.clean();

          Enumeration stackElements = e.get_stackList().elements();
          while (stackElements.hasMoreElements()) {
            String curItem = (String) stackElements.nextElement();
            _stack.addItem(curItem);
            _stackM.add(curItem);
          }
          _stack.setSelectedIndex(0);
          _stackM.notifyBack();

          break;

        case PythonDebugEvent.THREADLIST:
          assert (e.get_threadList() != null);
          _threads.set_threads(e.get_threadList());
          break;

        case PythonDebugEvent.GLOBAL:
          _globals.newValues(e.get_variables(), e.get_types());

          break;

        case PythonDebugEvent.LOCAL:
          _locals.newValues(e.get_variables(), e.get_types());
          _variables.callbackWithRootValuesSet(e.get_variables(), e.get_types());
          _variables.notifyBack();
          break;

        case PythonDebugEvent.COMPOSITE:
          if (_cCallback != null) {
            _cCallback.callbackWithValuesSet(e.get_variables(), e.get_types());
          }

          break;

        default:

          // pass message to plugging handle
          _setoutPane.writeMessage("unmanaged DebugEvent : " + e.toString());

      }
    }
  }

  class _CANCEL_WAITING_CONNECTION_ implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent evt) {
      try {
        _pyClient.abort(PythonDebugParameters.get_listeningPort());
        setContext(INACTIVE);
      } catch (PythonDebugException e) {
        _msgBar.setError(e.getMessage());
      }
    }
  }

  class _DEBUGGING_STARTER_ {

    private Debuggee _debuggee;

    public _DEBUGGING_STARTER_(Debuggee debuggee,
            boolean remote) {
      _debuggee = debuggee;
      _remoteSession = remote;
    }

    private String buildPythonPathFileName(boolean isJython) {

      StringBuffer wk = new StringBuffer(PythonDebugParameters.getSettingsDirectory());

      wk.append(File.separatorChar);
      if (isJython) {
        wk.append(_JYTHONPATH_);
      } else {
        wk.append(_PYTHONPATH_);
      }
      return wk.toString();
    }

    private String getSourcePath() {
      return _debuggee.getFile().getParent();
    }

    private String guessPythonHome(PythonPlatform platform) {
      File f = new File(platform.getInterpreterCommand());
      return f.getParent();
    }

    public void toRun() {
      _msgBar.reset();
      _statusBar.display("starting...");

      String dbgHost = PythonDebugParameters.get_dbgHost();
      if (_remoteSession) {
        dbgHost = null;

        _statusBar.setWaiting();
        _statusBar.display("waiting for incoming connection ...");
      }
      try {
        if (_evtListener == null) // not yet inited
        {
          _evtListener = new _DEBUGEVENT_MANAGER_(_IDEPlug);
          _pyClient.setPythonDebugEventListener(_evtListener);
        }
        PythonPlatform platform = _debuggee.getPlatform();

        // Use encoding from running file
        PythonFileEncodingQuery pythonEncodingQuery = new PythonFileEncodingQuery();
        String encoding = pythonEncodingQuery.getPythonFileEncoding(_debuggee.getFileObject().getInputStream());

        _pyClient.init(dbgHost,
                PythonDebugParameters.get_listeningPort(),
                PythonDebugParameters.get_connectingPort(),
                _debuggee.getPath(),
                _debuggee.getJavaPath(),
                platform.getInterpreterCommand(),
                PythonDebugParameters.get_jpydbgScript(),
                PythonDebugParameters.get_jpydbgScriptArgs(),
                encoding);
      } catch (PythonDebugException | IOException e) {
        _msgBar.setError(e.getMessage());
      }
    }
  }

  class _DEBUGGING_TERMINATOR_ extends Thread {

    @Override
    public void run() {
      _statusBar.display("ending...");
      try {
        terminate();
        _statusBar.display("ended");
      } catch (PythonDebugException e) {
        _msgBar.setError(e.getMessage());
      }
    }
  }

  class _DEBUGGING_STOP_ implements ActionListener {

    /**
     * debuggger initialization startup action
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
      _DEBUGGING_TERMINATOR_ terminator = new _DEBUGGING_TERMINATOR_();
      terminator.start();
    }
  }

  class _STEP_OVER_ implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent evt) {
      debugSubcommand(_NEXT_);
    }
  }

  class _STEP_INTO_ implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent evt) {
      debugSubcommand(_STEP_);
    }
  }

  class _RUN_ implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent evt) {
      debugSubcommand(_RUN_);
    }
  }

  class _REPORT_TABPANE_ extends JTabbedPane {

    public _REPORT_TABPANE_() {
      super(SwingConstants.BOTTOM);
    }
  }

  class _VARIABLE_TABPANE_ extends JTabbedPane {

    public _VARIABLE_TABPANE_() {
      super(SwingConstants.TOP);
    }
  }

  // TODO : FULLY REVISIT THE CLASS BELLOW TO IMPLEMENT NETBEANS
  // STACK CHANGE
  class _STACK_ITEM_CHANGED_ implements ItemListener {

    private String _fileName;
    private int _lineNum = -1;

    public _STACK_ITEM_CHANGED_() {
    }

    private void set_currentLine(int line) {
    }


    /* warning This tiny method is JEdit dependant */
    private void setCurrentLine(String toParse) {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      switch (e.getStateChange()) {

        // capture current selected stack level in order
        // to refresh the variables display
        case ItemEvent.SELECTED:

          int pos = _stack.getSelectedIndex();
          variableCommand(_GLOBALS_ + Integer.toString(pos));
          variableCommand(_LOCALS_ + Integer.toString(pos));
          setCurrentLine((String) _stack.getSelectedItem());

          break;
      }
    }
  }

  class _CALL_STACK_PANEL_ extends JPanel {

    public _CALL_STACK_PANEL_() {
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, _stack);
      _stack.addItemListener(new _STACK_ITEM_CHANGED_());
      super.setBorder(Swing.buildBorder("Call Stack",
              TitledBorder.LEFT,
              TitledBorder.TOP,
              Swing.BOXBOLDGRAY,
              Swing.BEVELRAISED));

    }
  }

  class _LOCAL_VARIABLES_ {

    private PythonVariableTreeTable _table = new PythonVariableTreeTable(false);

    public _LOCAL_VARIABLES_() {
      _table.set_parent(PythonDebugContainer.this);

    }

    public void newValues(TreeMap values, TreeMap types) {
      _table.set_tableValue(values, types);
    }
  }

  class _GLOBAL_VARIABLES_ {

    private PythonVariableTreeTable _table = new PythonVariableTreeTable(true);

    public _GLOBAL_VARIABLES_() {
      _table.set_parent(PythonDebugContainer.this);
    }

    public void newValues(TreeMap values, TreeMap types) {
      _table.set_tableValue(values, types);
    }
  }

  class _SEND_SHELL_COMMAND_
          implements CommandLineListener {

    @Override
    public void commandEntered(CommandLineEvent e) {
      try {

        // System.out.println("entering _SEND_COMMAND_") ;
        String curCommand = e.getCommand();
        if ((curCommand != null) && (curCommand.length() > 0)) {
          _pyClient.sendCommand(_COMMAND_ + curCommand);
        }
      } catch (PythonDebugException ex) {
        _msgBar.setError("SendCommand Exception occured : " + ex.getMessage());
      }
    }
  }

  class _STATUS_BAR_ extends JPanel {

    private JLabel _elapsedTimeLabel = new JLabel(_INACTIVE_TEXT_);
    private JLabel _cancel = new JLabel(_INACTIVE_);
    // private ActionListener _cancelAction = null ;
    private boolean _busy = false;
    private AnimatedCursor _cursor = new AnimatedCursor(_parent);

    public _STATUS_BAR_() {
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, _elapsedTimeLabel);
      add(BorderLayout.EAST, _cancel);
    }

    public JLabel get_elapsedTimeLabel() {
      return _elapsedTimeLabel;
    }

    public void display(String text) {
      _elapsedTimeLabel.setText(text);
    }

    public void setWaiting() {
      setRunning();
      _cancel.validate();
    }

    public void setConnected() {
      setNotRunning();
      _cancel.validate();
    }

    private void setStatusIcon(final ImageIcon icon) {
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          _cancel.setIcon(icon);
        }
      });

    }

    public void setBusy() {
      _busy = true;
      display(_BUSY_TEXT_);
      setStatusIcon(_BUSY_);

      // Setup mouse cursor animation
      _cursor.startAnimation();
      new Thread(_cursor).start();
      // _cursor.startWaitingCursor() ;
    }

    public void resetBusy() {
      if (_busy) {
        setStatusIcon(_ACTIVE_);
        display(_READY_TEXT_);

        // stop mouse cursor animation
        _cursor.stopAnimation();
        // _cursor.stopWaitingCursor() ;
      }
    }

    public void setRunning() {
      setStatusIcon(_ACTIVE_);
      if (_evtListener != null) {
        _evtListener.populateToPlugin(PluginEvent.BUSY, null, -1);
      }
    }

    public void setNotRunning() {
      setStatusIcon(_INACTIVE_);
      if (_evtListener != null) {
        _evtListener.populateToPlugin(PluginEvent.NOTBUSY, null, -1);
      }
      // reset threads counters
      _threads.cleanup();
      // stop mouse cursor animation
      _cursor.stopAnimation();
      // _cursor.stopWaitingCursor() ;
    }
  }

  class _MAIN_PANEL_ extends JPanel {

    public _MAIN_PANEL_() {
      setLayout(new BorderLayout());
      _setoutPane =
              new PythonOutputPanel(_reportTab, _DBGTAB_ICON_, new _SEND_SHELL_COMMAND_());
      _statusBar = new _STATUS_BAR_();
      _locals = new _LOCAL_VARIABLES_();
      _globals = new _GLOBAL_VARIABLES_();

      add(BorderLayout.NORTH, _statusBar);
      add(BorderLayout.CENTER, _setoutPane);
      add(BorderLayout.SOUTH, _msgBar);

      add(BorderLayout.CENTER, _setoutPane);
      setContext(INACTIVE);
    }
  }

  class _THREAD_MANAGER_ {

    /** implement here a Vector of PythonThreadInfos collected from python env */
    private Hashtable<String, PythonThreadInfos> _threads = new Hashtable<>();
    private DebuggerContextChangeListener _threadListChange = null;

    public boolean isListeningBack() {
      if (_threadListChange != null) {
        return true;
      }
      return false;
    }

    /** feed back here form python side */
    public synchronized void set_threads(Vector threads) {
      Enumeration tlist = threads.elements();
      Vector deleted = new Vector();
      boolean hasChanged = false;
      Enumeration oldList = _threads.elements();

      while (tlist.hasMoreElements()) {
        PythonThreadInfos pyThInf = (PythonThreadInfos) tlist.nextElement();
        PythonThreadInfos compared = (PythonThreadInfos) _threads.get(pyThInf.get_name());
        if (compared == null) {
          hasChanged = true;
          _threads.put(pyThInf.get_name(), pyThInf);
        } else {
          if (compared.isCurrent() != pyThInf.isCurrent()) {
            hasChanged = true;
          }
          _threads.put(pyThInf.get_name(), pyThInf);

        }
      }
      // look for deleted 
      while (oldList.hasMoreElements()) {
        Object cur = oldList.nextElement();
        if (!threads.contains(cur)) {
          deleted.add(cur);
          hasChanged = true;
        }
      }
      Enumeration delList = deleted.elements();
      // proceed with deletions
      while (delList.hasMoreElements()) {
        PythonThreadInfos pyThInf = (PythonThreadInfos) delList.nextElement();
        _threads.remove(pyThInf.get_name());
      }
      // notify model back 
      if ((_threadListChange != null) && (hasChanged)) {
        _threadListChange.fireContextChanged();
      }
    }

    public void cleanup() {
      _threads = new Hashtable();
    }

    public int getThreadCount() {
      return _threads.size();
    }

    public Object[] getThreads() {
      Object[] returned = new Object[_threads.size()];
      Enumeration tList = _threads.elements();
      int ii = 0;
      while (tList.hasMoreElements()) {
        returned[ii++] = tList.nextElement();
      }
      return returned;
    }

    public void refreshThreads() {
      threadCommand();
    }

    /** used to populate back to ThreadModel window */
    public synchronized void addThreadListChangeListener(DebuggerContextChangeListener l) {
      if (_threadListChange == null) {
        _threadListChange = l;
        // refresh the ThreadList now to populate back accurate infos
        refreshThreads();
      }
    }

    /** used to populate back to ThreadModel window */
    public synchronized void removeThreadListChangeListener(DebuggerContextChangeListener l) {
      if (l == _threadListChange) {
        _threadListChange = null;
      }
    }
  }

  class _STACK_MANAGER_ {

    /** implement here a Vector of PythonThreadInfos collected from python env */
    private Vector _stack = new Vector();
    private DebuggerContextChangeListener _stackListChange = null;

    public boolean isListeningBack() {
      if (_stackListChange != null) {
        return true;
      }
      return false;
    }

    public void clean() {
      _stack.removeAllElements();
    }

    public void add(Object o) {
      _stack.addElement(o);
    }

    public void notifyBack() {
      // notify model back 
      if (_stackListChange != null) {
        _stackListChange.fireContextChanged();
      }

    }

    public int getSize() {
      return _stack.size();
    }

    public StackInfo[] getStackList() {
      StackInfo[] returned = new StackInfo[_stack.size()];
      for (int ii = 0; ii < _stack.size(); ii++) {
        if (ii == 0) {
          returned[ii] = new StackInfo((String) _stack.elementAt(ii), true); // current
        } else {
          returned[ii] = new StackInfo((String) _stack.elementAt(ii), false);
        }
      }
      return returned;
    }

    public void refreshStack() {
      stackCommand();
    }

    /** used to populate back to ThreadModel window */
    public synchronized void addStackListChangeListener(DebuggerContextChangeListener l) {
      if (_stackListChange == null) {
        _stackListChange = l;
        // refresh the ThreadList now to populate back accurate infos
        refreshStack();
      }
    }

    /** used to populate back to ThreadModel window */
    public synchronized void removeStackListChangeListener(DebuggerContextChangeListener l) {
      if (l == _stackListChange) {
        _stackListChange = null;
      }
    }
  }

  class _VARIABLES_MANAGER_
          implements CompositeCallback {
    // private TreeMap _values = null ;   

    private DebuggerContextChangeListener _varListChange = null;
    private int _curStackPos = 0;
    private boolean _waiting = false;
    private PythonVariableTreeDataNode _curNode;

    public boolean isListeningBack() {
      if (_varListChange != null) {
        return true;
      }
      return false;
    }

    private synchronized void stopWaiting() {
      _waiting = false;
      this.notify();
    }

    public void callbackWithRootValuesSet(TreeMap values, TreeMap types) {
      ROOTNODE.set_children(values, types);
      stopWaiting();
    }

    @Override
    public void callbackWithValuesSet(TreeMap values, TreeMap types) {
      _curNode.set_children(values, types);
      stopWaiting();
    }

    public int getVariableSize(PythonVariableTreeDataNode curNode) {
      _curNode = curNode;
      // check if children has already expanded 
      // Assume not necessary to expand twice
      if (!_curNode.hasChildren()) {
        // lookup variable content
        _waiting = true;
        String pythonVarName = PythonVariableTreeDataNode.buildPythonName(_curNode.getPath());
        inspectCompositeCommand(this, pythonVarName);
        try {
          synchronized (this) {
            if (_waiting) {
              this.wait();
            }
          }
        } catch (InterruptedException e) {
        }
      }
      return _curNode.get_childrenSize();
    }

    public void notifyBack() {
      // notify model back 
      if (_varListChange != null) {
        _varListChange.fireContextChanged();
      }

    }

    public Object[] getVariableList(PythonVariableTreeDataNode curNode) {
      _curNode = curNode;
      return _curNode.get_children();
    }

    public void refreshVars() {
      variableCommand(_LOCALS_ + Integer.toString(_curStackPos));
    }

    /** used to populate back to ThreadModel window */
    public synchronized void addVarListChangeListener(DebuggerContextChangeListener l) {
      if (_varListChange == null) {
        _varListChange = l;
        // refresh the ThreadList now to populate back accurate infos
        refreshVars();
      }
    }

    /** used to populate back to ThreadModel window */
    public synchronized void removeVarListChangeListener(DebuggerContextChangeListener l) {
      if (l == _varListChange) {
        _varListChange = null;
      }
    }
  }

  /** return current size of the Python stack */
  public int getStackSize() {
    return _stackM.getSize();
  }

  /** return current stack content */
  public Object[] getStack() {
    return _stackM.getStackList();
  }

  /** return the number of currently running Pyhton threads */
  public int getThreadCount() {
    return _threads.getThreadCount();
  }

  /** return the current python Thread list */
  public Object[] getThreads() {
    return _threads.getThreads();
  }

  /** return the current python Variable list */
  public Object[] getVariables(PythonVariableTreeDataNode curNode) {
    return _variables.getVariableList(curNode);
  }

  /** return the current python Variable list count */
  public int getVariableCount(PythonVariableTreeDataNode curNode) {
    return _variables.getVariableSize(curNode);
  }

  public void addThreadListChangeListener(DebuggerContextChangeListener l) {
    _threads.addThreadListChangeListener(l);
  }

  public void removeThreadListChangeListener(DebuggerContextChangeListener l) {
    _threads.removeThreadListChangeListener(l);
  }

  public void addStackListChangeListener(DebuggerContextChangeListener l) {
    _stackM.addStackListChangeListener(l);
  }

  public void removeStackListChangeListener(DebuggerContextChangeListener l) {
    _stackM.removeStackListChangeListener(l);
  }

  public void addVarListChangeListener(DebuggerContextChangeListener l) {
    _variables.addVarListChangeListener(l);
  }

  public void removeVarListChangeListener(DebuggerContextChangeListener l) {
    _variables.removeVarListChangeListener(l);
  }
}
