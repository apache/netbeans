/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
