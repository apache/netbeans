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
package org.netbeans.modules.python.debugger.gui;

import java.util.EventObject;
import javax.swing.*;

/**
 *  Python debugging event
 *
 * @author jean-yves
 */
public class DebugEvent
        extends EventObject {

  public final static String STOP = "shutdown Python environment";
  public final static String START = "startup local debugging session";
  public final static String REMOTESTART = "startup remote debugging session";
  public final static String STEPOVER = "Debug statement step Over";
  public final static String STEPINTO = "Debug statement step Into";
  public final static String RUN = "Run";
  public final static String RUNTOCURSOR = "Run to cursor";
  public final static String SENDCOMMAND = "execute Python Command";
  public final static String COMMANDFIELD = "Python command field";
  public final static String TOGGLEJYTHON = "Jython / CPython language switch";
  public final static String PGMARGS = "Add python programs arguments to args table";
  private String _moduleName;
  private Action _action;
  private AbstractButton _guiButton;

  public DebugEvent(Object source,
          String moduleName,
          Action action,
          AbstractButton gui) {
    super(source);
    _action = action;
    _guiButton = gui;
  }

  public String get_moduleName() {
    return _moduleName;
  }

  public Action get_action() {
    return _action;
  }

  public AbstractButton get_guiButton() {
    return _guiButton;
  }
}
