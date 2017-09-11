/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.apache.tools.ant.module.run;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * Trick to let {@link AntSession}, {@link AntEvent}, and {@link TaskStructure}
 * be final classes when naturally they should be interfaces because their
 * implementation is elsewhere.
 * @see "#45491"
 * @author Jesse Glick
 */
public final class LoggerTrampoline {
    
    private LoggerTrampoline() {}
    
    public interface Creator {
        AntSession makeAntSession(AntSessionImpl impl);
        AntEvent makeAntEvent(AntEventImpl impl);
        TaskStructure makeTaskStructure(TaskStructureImpl impl);
    }
    
    public static Creator ANT_SESSION_CREATOR, ANT_EVENT_CREATOR, TASK_STRUCTURE_CREATOR;
    static {
        try {
            Class c = AntSession.class;
            Class.forName(c.getName(), true, c.getClassLoader());
            c = AntEvent.class;
            Class.forName(c.getName(), true, c.getClassLoader());
            c = TaskStructure.class;
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            assert false : e;
        }
        assert ANT_SESSION_CREATOR != null && ANT_EVENT_CREATOR != null && TASK_STRUCTURE_CREATOR != null;
    }
    
    public interface AntSessionImpl {
        File getOriginatingScript();
        String[] getOriginatingTargets();
        Object getCustomData(AntLogger logger);
        void putCustomData(AntLogger logger, Object data);
        void println(String message, boolean err, OutputListener listener);
        void deliverMessageLogged(AntEvent originalEvent, String message, int level);
        void consumeException(Throwable t) throws IllegalStateException;
        boolean isExceptionConsumed(Throwable t);
        int getVerbosity();
        String getDisplayName();
        OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2);
        InputOutput getIO();
        Map<String,String> getProperties();
        boolean isConcealed(String propertyName);
    }
    
    public interface AntEventImpl {
        AntSession getSession();
        void consume() throws IllegalStateException;
        boolean isConsumed();
        File getScriptLocation();
        int getLine();
        String getTargetName();
        String getTaskName();
        TaskStructure getTaskStructure();
        String getMessage();
        int getLogLevel();
        Throwable getException();
        String getProperty(String name);
        Set<String> getPropertyNames();
        String evaluate(String text);
    }
    
    public interface TaskStructureImpl {
        String getName();
        String getAttribute(String name);
        Set<String> getAttributeNames();
        String getText();
        TaskStructure[] getChildren();
    }
    
}
