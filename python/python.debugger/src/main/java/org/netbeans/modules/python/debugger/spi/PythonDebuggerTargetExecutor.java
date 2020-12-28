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
package org.netbeans.modules.python.debugger.spi;

import org.netbeans.modules.python.debugger.PythonSourceDebuggee;
import org.openide.execution.ExecutorTask;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jean-yves Mengant
 */
public class PythonDebuggerTargetExecutor {

  private final Env _env;

  /** Creates a new instance of PythonTargetExecutor */
  public PythonDebuggerTargetExecutor(Env env) {
    _env = env;
  }

  /** Factory method for creation of AntTargetExecutor with the given environment.
   * The factory does not clone Env what means that any change to Env will
   * influence the factory.
   * @param env a configuration for the executor
   * @return an executor which can run projects with the given configuration
   */
  public static PythonDebuggerTargetExecutor createTargetExecutor(Env env) {
    return new PythonDebuggerTargetExecutor(env);
  }

  /** Execute given target(s).
   * <p>The {@link AntProjectCookie#getFile} must not be null, since Ant can only
   * run files present on disk.</p>
   * <p>The returned task may be used to wait for completion of the script
   * and check result status.</p>
   * <p class="nonnormative">
   * The easiest way to get the project cookie is to get a <code>DataObject</code>
   * representing an Ant build script and to ask it for this cookie. Alternatively,
   * you may implement the cookie interface directly, where
   * <code>getFile</code> is critical and other methods may do nothing
   * (returning <code>null</code> as needed).
   * While the specification for <code>AntProjectCookie</code> says that
   * <code>getDocument</code> and <code>getParseException</code> cannot
   * both return <code>null</code> simultaneously, the <em>current</em>
   * executor implementation does not care; to be safe, return an
   * {@link UnsupportedOperationException} from <code>getParseException</code>.
   * </p>
   * @param antProject a representation of the project to run
   * @param targets non-empty list of target names to run; may be null to indicate default target
   * @return task for tracking of progress of execution
   * @throws IOException if there is a problem running the script
   */
  public ExecutorTask execute(PythonSourceDebuggee pyProject) throws IOException {
    TargetExecutor te = new TargetExecutor(pyProject);
    //te.setVerbosity(env.getVerbosity());
    //te.setProperties(env.getProperties());
    if (_env.getLogger() == null) {
      return te.execute();
    } else {
      return te.execute(_env.getLogger());
    }
  }

  /** 
  Class describing the environment in which the Python script will be executed.
   */
  final public static class Env {

    private OutputStream _outputStream = null;

    /** Create instance of Env class describing environment for Ant target execution.
     */
    public Env() {
    }

    public OutputStream getLogger() {
      return _outputStream;
    }

    public void getLogger(OutputStream logger) {
      _outputStream = logger;
    }
  }
}
