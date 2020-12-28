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
package org.netbeans.modules.python.debugger.utils;

import java.util.*;
import java.io.*;

/**

isolate process launching 

@author Jean-Yves Mengant

 */
public class ProcessLauncher {

  public final static int PROCESS_EXCEPTION = 99;
  private final static String _NO_SYSTEM_ERROR_ = "SYSTEM EXCEPTION=none";
  private ProcessBuilder _builder;
  // the launched process component
  private Process _executable;
  private HashMap<String, String> _env = new HashMap<>();
  private String _args[];
  private int _retCode;
  private ExecTerminationListener _listener;
  private InputStream _stdout;
  private InputStream _stderr;

  public Process getProcess() {
    return _executable;
  }

  public void setEnv(String key, String value) {
    _env.put(key, value);
  }

  public InputStream getStdout() {
    return _stdout;
  }

  public InputStream getStderr() {
    return _stderr;
  }

  private String[] buildEnvironment() {
    String returned[] = new String[_env.size()];
    int ii = 0;

    for (String envElem : _env.keySet()) {
      returned[ii] = envElem + '=' + _env.get(envElem);
      ii++;
    }

    return returned;
  }

  /**
  @param command a sting containing the program name followed
  by an optional argument list
   */
  public void setCommand(Vector command)
          throws UtilsError {
    _args = new String[command.size()];

    for (int ii = 0; ii < _args.length; ii++) {
      _args[ii] = (String) command.elementAt(ii);
      System.out.println("_args[" + ii + "]=" + _args[ii]);
    }


    _builder = new ProcessBuilder(_args);
    Map<String, String> env = _builder.environment();

    // populate added environment
    for (String envKey : _env.keySet()) {
      env.put(envKey, _env.get(envKey));
    }

  }

  public void setExecTerminationListener(ExecTerminationListener listener) {
    _listener = listener;
  }

  public void waitForCompletion() {
    try {
      if (_executable != null) {
        _retCode = _executable.waitFor();
      }
    } catch (InterruptedException e) {
    }

    /* broadcast termination event to any subscribed listener */
    if (_listener != null) {
      _listener.processHasEnded(new ExecTerminationEvent(_retCode,
              _NO_SYSTEM_ERROR_));
    }
  }

  public void go() {
    try {
      _executable = _builder.start();
      _stdout = _executable.getInputStream();
      _stderr = _executable.getErrorStream();
    } catch (IOException e) {
      /* broadcast Exception event to any subscribed listener */
      if (_listener != null) {
        _listener.processHasEnded(new ExecTerminationEvent(PROCESS_EXCEPTION,
                e.getMessage()));
      }
    }
  }

  /**
  test process launching
   */
  public static void main(String args[]) {

    ProcessLauncher launcher = new ProcessLauncher();
    try {
      Vector argList = new Vector();
      int ii = 0;
      while (ii < args.length) {
        argList.addElement(args[ii++]);
      }

      launcher.setCommand(argList);
      InputStream stdOut = launcher.getStdout();
      InputStream stdErr = launcher.getStderr();
      WL reader = new WL(stdOut, stdErr);
      reader.start();
      launcher.waitForCompletion();
    } catch (UtilsError e) {
      System.out.println("ERROR :" + e.getMessage());
    }
  }
}

/**
local non public classes used by main
 */
class WL
        extends Thread {

  InputStream _out;
  InputStream _err;

  public WL(InputStream out, InputStream err) {
    _out = out;
    _err = err;
  }

  @Override
  public void run() {
    int c;
    try {
      while ((c = _out.read()) != -1) {
        System.out.print((char) c);
      }

      System.out.println("end of print");
      _out.close();

      while ((c = _err.read()) != -1) {
        System.out.print((char) c);
      }
      _err.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


