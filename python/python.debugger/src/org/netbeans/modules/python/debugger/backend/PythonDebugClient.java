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

import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * This class is the basic TCP/IP client side used to setup and 
 * drive a networked Python shell debugging session 
 */
public class PythonDebugClient {

  private final static String _XML_HEADER_ = "<?xml version=\"1.0\"?>";
  private final static String _JPY_START_ = "<JPY>";
  private final static String _JPY_END_ = "</JPY>";
  private static final String _ENCODING_PROPERTY_ = "file.encoding";
  public final static String VERSION = "V0.00.001";
  private final static String _LOCALHOST_ = "localhost";
  private final static String _LOCALADDRESS_ = "127.0.0.1";
  public final static String DEFAULTHOST = "localhost";
  public final static int DEFAULTPORT = 29000;
  private final static String _ABORTWAITING_ = "<JPY><ABORT/></JPY>";
  private final static String _EOL_ = "\n";
  private Socket _outCnx;
  private Socket _inCnx;
  private BufferedWriter _cmdStream; // Use it to build Python Dbg commands
  private BufferedReader _answStream; // Use it to get Python Dbg returns back
  private JPyDebugXmlParser _parser;
  /** codePage to use for remote incoming connection  for instance MVS will require Cp037 
   * for safe EBCDIC conversions 
   * */
  private String _codePage;

  // private Vector _listeners = new Vector() ; 
  private PythonDebugEventListener _listener = null;
  private boolean _inited = false;

  public boolean has_inited() {
    return _inited;
  }

  public synchronized PythonDebugEventListener get_listener() {
    return _listener;
  }

  public synchronized void setPythonDebugEventListener(PythonDebugEventListener l) {
    _listener = l;
  }

  public synchronized void removePythonDebugEventListener(PythonDebugEventListener l) {
    if (l == _listener) {
      _listener = null;
    }
  }

  /** reading messages  back from Python Debugger side */
  private String getMessage()
          throws PythonDebugException {
    try {
      if (_answStream != null) {
        return _answStream.readLine();
      }
      return null;
    } catch (IOException e) {
      throw new PythonDebugException("Socket read Command error " +
              e.toString());
    }
  }

  /** Sending a command to Python Debugger side */
  public void sendCommand(String cmd)
          throws PythonDebugException {
    if (_cmdStream == null) {
      return;  // not yet in debugging state
    }
    try {
      if (_cmdStream != null)
        _cmdStream.write(cmd + "\n");
      if (_cmdStream != null)
        _cmdStream.flush();
    } catch (IOException e) {
      throw new PythonDebugException("Socket Write Command error " +
              e.toString());
    }
  }

  class _TCP_TASK_
          extends Thread {

    private boolean _inProgress = false;

    private String buildXmlMsg(String msg) {
      StringBuffer buffer = new StringBuffer(_XML_HEADER_);
      buffer.append(_JPY_START_);
      buffer.append(msg);
      buffer.append(_JPY_END_);
      return buffer.toString();
    }

    @Override
    public void run() {
      StringBuffer wkBuffer = new StringBuffer();
      _inProgress = true;
      while (_inProgress) {
        try {
          String lastMsg = getMessage();
          if (lastMsg == null) {
            _inProgress = false;
            lastMsg = buildXmlMsg("<ERROR>null message received form Python server</ERROR>");
          } else if (lastMsg.equals(_ABORTWAITING_)) {
            // user aborting connection wait => silently stop
            _inProgress = false;
            _inited = false;
          } else {
            _inited = true;
          }

          if (_listener != null) {
            wkBuffer.append(lastMsg);
            wkBuffer.append(_EOL_);
            if (lastMsg.endsWith(_JPY_END_)) {
              populateEvent(wkBuffer.toString());
              wkBuffer = new StringBuffer();
            }
          }
        } catch (PythonDebugException e) {
          _inProgress = false;
        }
      }
      //if ( ( _listener != null )  )
      //  _listener.newDebugEvent(new PythonDebugEvent("+++ JPy/Error/message thread ENDING" )) ;
      // proceed with local session termination
      try {
        terminate();
      } catch (PythonDebugException e) {
        e.printStackTrace();
      }
    }
  }

  private void populateEvent(String xmlEvent) {
    if (_listener == null) {
      return;
    }

    try {
      PythonDebugEvent evt = new PythonDebugEvent(_parser, xmlEvent);
      _listener.newDebugEvent(evt);
    } catch (PythonDebugException e) {
      e.printStackTrace();
    }
  }

  private void populateLauncherEvent(PythonDebugEvent evt) {
    if (_listener == null) {
      return;
    }
    _listener.launcherMessage(evt);
  }

  class _LAUNCH_LOCAL_CONNECTOR_
          extends PythonInterpretor
          implements PythonDebugEventListener {

    @Override
    public void newDebugEvent(PythonDebugEvent e) {
    }

    @Override
    public void launcherMessage(PythonDebugEvent e) {
      populateLauncherEvent(e);
    }

    public _LAUNCH_LOCAL_CONNECTOR_(String pgm,
            Vector args,
            int port) {
      super(pgm, args);
      super.addPythonDebugEventListener(this);
    }
  }

  /**
   * check for local launch of Python Debugger stuff
   * @param host
   * @param port
   * @param pythonLoc
   * @param jnetPyLoc
   * @param jnetPyParm
   */
  private void localPythonLaunch(String host,
          int port,
          String pythonPath,
          String classPath,
          String pythonLoc,
          String jnetPyLoc,
          String codePage)
          throws PythonDebugException {
    if (pythonLoc == null) {
      throw new PythonDebugException("python.exe location not specified => check configuration");
    }
    if (jnetPyLoc == null) {
      throw new PythonDebugException("jpydaemon.py location not specified => check configuration");
    }
    String pgm = pythonLoc;
    Vector args = new Vector();
    args.addElement(jnetPyLoc);
    if (host != null) {
      args.addElement(host);
    }
    if (port != -1) {
      args.addElement(Integer.toString(port));
    }
    // starting with jpydbg 0.0.9 the PYTHONPATH file location is appended to
    // after the port
    //if (pyPathLoc != null)
    //{
    //  args.addElement(pyPathLoc);
    //}

    _LAUNCH_LOCAL_CONNECTOR_ launcher = new _LAUNCH_LOCAL_CONNECTOR_(pgm, args, port);
    if (pythonPath != null) {
      launcher.setEnv("PYTHONPATH", pythonPath);
    }
    if (classPath != null) {
      launcher.setEnv("CLASSPATH", classPath);
    }
    if (codePage != null) {
      launcher.setEnv("PYTHONIOENCODING", codePage); //NOI18N
    }
    launcher.start();
  }

  private boolean localHost(String host) {
    if (host == null) {
      return false;  // default to non local local if not set
    }
    if (host.equalsIgnoreCase(_LOCALADDRESS_) ||
            host.equalsIgnoreCase(_LOCALHOST_) ||
            host.length() == 0) {
      return true;
    }
    return false;
  }

  /**
   * proceed with basic client connection protocol
   * @param host
   * @param port
   * @throws PythonDebugException
   */
  public void init(String debuggingHost,
          int listeningPort,
          int connectingPort,
          String pyPath,
          String classPath,
          String pythonLoc,
          String jnetPyLoc,
          String jnetPyParms,
          String codePage)
          throws PythonDebugException {
    try {
      _codePage = codePage==null ? System.getProperty(_ENCODING_PROPERTY_) : codePage;
      // parsing initialization   
      _parser = new JPyDebugXmlParser();
      _parser.init(null);
      if ((debuggingHost != null) && (connectingPort != -1)) // connecting to server daemon
      {
        _outCnx = new Socket(debuggingHost, connectingPort);
        _inCnx = new Socket(debuggingHost, connectingPort);
      } else // listening for incomming connnection
      {
        ServerSocket tcpOutServer = new ServerSocket(listeningPort, 1);
        ServerSocket tcpInServer = new ServerSocket(listeningPort + 1, 1);
        if (localHost(debuggingHost)) {
          localPythonLaunch(debuggingHost, listeningPort, pyPath, classPath, pythonLoc, jnetPyLoc, codePage);
        }
        tcpOutServer.setSoTimeout(20000) ;
        _outCnx = tcpOutServer.accept();
        tcpInServer.setSoTimeout(20000) ;
        _inCnx = tcpInServer.accept();
        // safe to close the ServerSocket once the connection is established
        tcpInServer.close() ;
        tcpInServer = null ;
        tcpOutServer.close() ;
        tcpOutServer = null ; 
      }
      _cmdStream = new BufferedWriter(
              new OutputStreamWriter(_outCnx.getOutputStream(),
              _codePage));
      _answStream = new BufferedReader(
              new InputStreamReader(_inCnx.getInputStream(),
              _codePage));
      _TCP_TASK_ task = new _TCP_TASK_();
      task.start();

    } catch (UnsupportedEncodingException e) {
      throw new PythonDebugException("Unsupported encoding " +
              e.toString());

    } catch (SocketTimeoutException e) {
      throw new PythonDebugException("Server Socket listen for debuggee has timed out(more than 20 seconds wait)  " +
              e.toString());

    } catch (IOException e) {
      throw new PythonDebugException("Socket IO error " +
              e.toString());

    }
  }

  /**
   * abort localhost waiting connection 
   */
  public void abort(int port)
          throws PythonDebugException {
    try {
      Socket clientConnection = new Socket("localhost", port);
      BufferedWriter abortStream = new BufferedWriter(
              new OutputStreamWriter(clientConnection.getOutputStream()));

      abortStream.write(_ABORTWAITING_ + "\n");
      abortStream.flush();
      clientConnection.close();

    } catch (IOException e) {
      throw new PythonDebugException("Abort Command error " +
              e.toString());

    }
  }

  /**
   * terminate DebugClient session 
   */
  public void terminate()
          throws PythonDebugException {
    try {
      if (_outCnx != null) {
        _outCnx.close();
      }
      if (_inCnx != null) {
        _inCnx.close();
      }
    } catch (IOException e) {
      throw new PythonDebugException("termination error : " + e.getMessage());
    }
    _outCnx = null;
    _inCnx = null;
    _cmdStream = null;
    _answStream = null;
  }
}
