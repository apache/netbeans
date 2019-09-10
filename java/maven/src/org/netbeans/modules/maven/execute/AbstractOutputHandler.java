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

package org.netbeans.modules.maven.execute;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.ContextOutputProcessorFactory;
import org.netbeans.modules.maven.api.output.NotifyFinishOutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.project.indexingbridge.IndexingBridge;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public abstract class AbstractOutputHandler {

    public enum Level {DEBUG, INFO, WARNING, ERROR, FATAL}

    protected static final String PRJ_EXECUTE = "project-execute"; //NOI18N
    protected static final String SESSION_EXECUTE = "session-execute"; //NOI18N
    
    protected HashMap<String, Set<OutputProcessor>> processors;
    protected Set<OutputProcessor> currentProcessors;
    protected Set<NotifyFinishOutputProcessor> toFinishProcessors;
    protected OutputVisitor visitor;
    private IndexingBridge.Lock protectedMode; // #211005
    private final Object protectedModeLock = new Object();
    private RequestProcessor.Task sleepTask;
    private static final int SLEEP_DELAY = Integer.getInteger(AbstractOutputHandler.class.getName() + ".SLEEP_DELAY", 15000); // #270005

    protected AbstractOutputHandler(Project proj, final ProgressHandle hand, RunConfig config, OutputVisitor visitor) {
        processors = new HashMap<String, Set<OutputProcessor>>();
        currentProcessors = new HashSet<OutputProcessor>();
        this.visitor = visitor;
        toFinishProcessors = new HashSet<NotifyFinishOutputProcessor>();
        sleepTask = new RequestProcessor(AbstractOutputHandler.class).create(new Runnable() {
            public @Override void run() {
                hand.suspend("");
                exitProtectedMode();
            }
        });
        enterProtectedMode(isProtectedWait(proj, config));
    }

    private void enterProtectedMode(boolean wait) {
        synchronized (protectedModeLock) {
            if (protectedMode == null) {
                protectedMode = IndexingBridge.getDefault().protectedMode(wait);
            }
        }
    }
    private void exitProtectedMode() {
        synchronized (protectedModeLock) {
            if (protectedMode != null) {
                protectedMode.release();
                protectedMode = null;
            }
        }
    }

    private boolean isProtectedWait(Project proj, RunConfig config) {
        String action = config.getActionName();
        if(action == null || proj == null || !RunUtils.isCompileOnSaveEnabled(proj)) {
            return false;
        }
        switch(action) {
            case ActionProvider.COMMAND_RUN: 
            case ActionProvider.COMMAND_RUN_SINGLE:
            case ActionProvider.COMMAND_DEBUG:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
            case ActionProviderImpl.COMMAND_DEBUG_MAIN:
            case ActionProviderImpl.COMMAND_RUN_MAIN:
                return true;
            default:
                return false;
        }
    }
    
    protected abstract InputOutput getIO();

    protected void checkSleepiness() {
        RequestProcessor.Task task = sleepTask;
        if (task != null) {
            task.schedule(SLEEP_DELAY);
            enterProtectedMode(false);
        }
    }


    protected final void quitSleepiness() {
        RequestProcessor.Task task = sleepTask;
        if (task != null) {
            task.cancel();
            sleepTask = null;
            exitProtectedMode();
        }
    }
    
//TODO - replacement?    abstract MavenEmbedderLogger getLogger();

    protected final String getEventId(String eventName, String target) {
        if (PRJ_EXECUTE.equals(eventName) || SESSION_EXECUTE.equals(eventName)) {
            return eventName;
        }
        
        return eventName + "#" + target; //NOI18N
    }
    
    protected final void initProcessorList(Project proj, RunConfig config) {
        // get the registered processors.
        Lookup.Result<OutputProcessorFactory> result  = Lookup.getDefault().lookupResult(OutputProcessorFactory.class);
        Iterator<? extends OutputProcessorFactory> it = result.allInstances().iterator();
        while (it.hasNext()) {
            OutputProcessorFactory factory = it.next();
            Set<? extends OutputProcessor> procs = factory.createProcessorsSet(proj);
            if (factory instanceof ContextOutputProcessorFactory) {
                Set<OutputProcessor> _procs = new HashSet<OutputProcessor>(procs);
                _procs.addAll(((ContextOutputProcessorFactory)factory).createProcessorsSet(proj, config));
                procs = _procs;
            }
            for (OutputProcessor proc : procs) {
                String[] regs = proc.getRegisteredOutputSequences();
                for (int i = 0; i < regs.length; i++) {
                    String str = regs[i];
                    Set<OutputProcessor> set = processors.get(str);
                    if (set == null) {
                        set = new HashSet<OutputProcessor>();
                        processors.put(str, set);
                    }
                    set.add(proc);
                }
            }
        }
    }
    
    protected final void processStart(String id, OutputWriter writer) {
        checkSleepiness();
        Set<OutputProcessor> set = processors.get(id);
        if (set != null) {
            currentProcessors.addAll(set);
        }
        visitor.resetVisitor();
        for (OutputProcessor proc : currentProcessors) {
            proc.sequenceStart(id, visitor);
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                if (visitor.getColor(getIO()) != null && IOColorLines.isSupported(getIO())) {
                    try {
                        IOColorLines.println(getIO(), visitor.getLine(), visitor.getColor(getIO()));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    writer.println(visitor.getLine());
                }
            }
        }
    }
    
    protected final void processEnd(String id, OutputWriter writer) {
        checkSleepiness();
        visitor.resetVisitor();
        Iterator<OutputProcessor> it = currentProcessors.iterator();
        while (it.hasNext()) {
            OutputProcessor proc = it.next();
            proc.sequenceEnd(id, visitor);
            if (proc instanceof NotifyFinishOutputProcessor) {
                toFinishProcessors.add((NotifyFinishOutputProcessor)proc);
            }
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println(visitor.getLine());
            }
        }
        Set set = processors.get(id);
        if (set != null) {
            //TODO a bulletproof way would be to keep a list of currently started
            // sections and compare to the list of getRegisteredOutputSequences fo each of the
            // processors in set..
            currentProcessors.removeAll(set);
        }
    }
    
    protected final void processFail(String id, OutputWriter writer) {
        checkSleepiness();
        visitor.resetVisitor();
        for (OutputProcessor proc : currentProcessors) {
            if (proc instanceof NotifyFinishOutputProcessor) {
                toFinishProcessors.add((NotifyFinishOutputProcessor)proc);
            }
            proc.sequenceFail(id, visitor);
        }
        if (visitor.getLine() != null) {
            if (visitor.getOutputListener() != null) {
                try {
                    writer.println(visitor.getLine(), visitor.getOutputListener(), visitor.isImportant());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                writer.println(visitor.getLine());
            }
        }
        Set<OutputProcessor> set = processors.get(id);
        if (set != null) {
            Set<OutputProcessor> retain = new HashSet<OutputProcessor>();
            retain.addAll(set);
            retain.retainAll(currentProcessors);
            Set<OutputProcessor> remove = new HashSet<OutputProcessor>();
            remove.addAll(set);
            remove.removeAll(retain);
            currentProcessors.removeAll(remove);
        }
        
    }
    
    protected final void buildFinished() {
        quitSleepiness();
        for (NotifyFinishOutputProcessor proc : toFinishProcessors) {
            proc.buildFinished();
        }
    }
    
    protected final void processMultiLine(String input, OutputWriter writer, Level level) {
        if (input == null) {
            return;
        }
        //MEVENIDE-637
        for (String s : splitMultiLine(input)) {
            processLine(s, writer, level);
        }
    }
    
    protected final void processLine(String input, OutputWriter writer, Level level) {
        checkSleepiness();
        
        visitor.resetVisitor();
        for (OutputProcessor proc : currentProcessors) {
            proc.processLine(input, visitor);
        }
        if (!visitor.isLineSkipped()) {
            String line = visitor.getLine() == null ? input : visitor.getLine();
            if (visitor.getColor(getIO()) == null && visitor.getOutputListener() == null) {
                switch (level) {
                case DEBUG:
                    visitor.setOutputType(IOColors.OutputType.LOG_DEBUG);
                    break;
                case WARNING:
                    visitor.setOutputType(IOColors.OutputType.LOG_WARNING);
                    break;
                case ERROR:
                case FATAL:
                    visitor.setOutputType(IOColors.OutputType.LOG_FAILURE);
                    break;
                }
            }
            try {
                if (visitor.getOutputListener() != null) {
                    if (visitor.getColor(getIO()) != null && IOColorPrint.isSupported(getIO())) {
                        IOColorPrint.print(getIO(), line + "\n", visitor.getOutputListener(), visitor.isImportant(), visitor.getColor(getIO()));
                    } else {
                        writer.println(line, visitor.getOutputListener(), visitor.isImportant());
                    }
                } else {
                    if (level.compareTo(Level.ERROR) >= 0 && IOColorPrint.isSupported(getIO())) {
                        IOColorPrint.print(getIO(), line + "\n", null, true, visitor.getColor(getIO()));
                    } else if (visitor.getColor(getIO()) != null && IOColorLines.isSupported(getIO())) {
                        IOColorLines.println(getIO(), line, visitor.getColor(getIO()));
                    } else {
                        writer.println(line);
                    }
                }
            } catch (IOException x) {
                x.printStackTrace();
                writer.println(line); // fallback
            }
        }
    }
    
    //MEVENIDE-637   
    public static List<String> splitMultiLine(String input) {
        List<String> list = new ArrayList<String>();
        String[] strs = input.split("\\r|\\n"); //NOI18N
        for (int i = 0; i < strs.length; i++) {
            if(strs[i].length()>0){
              list.add(strs[i]);
            }
        }
        return list;
    }   
}
