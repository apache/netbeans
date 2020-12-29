/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class PythonExecution {
    // execution commands
    private String command;
    private String workingDirectory;
    private String commandArgs;
    private String path;
    private String javapath;
    private String script;
    private String scriptArgs;
    private String displayName;    
    private boolean redirect;
    private String wrapperCommand;
    private String[] wrapperArgs;
    private String[] wrapperEnv;
    private List<LineConvertor> outConvertors = new ArrayList<>();
    private List<LineConvertor> errConvertors = new ArrayList<>();
    private InputProcessorFactory outProcessorFactory;
    private InputProcessorFactory errProcessorFactory;
    private boolean addStandardConvertors;
    private FileLocator fileLocator;
    private boolean lineBased;
    private Runnable postExecutionHook;
    
    public PythonExecution() {

    }

    public PythonExecution(PythonExecution from) {
        command = from.command;
        workingDirectory = from.workingDirectory;
        commandArgs = from.commandArgs;
        path = from.path;
        javapath = from.javapath;
        script = from.script;
        scriptArgs = from.scriptArgs;
        displayName = from.displayName;
        redirect = from.redirect;
        wrapperCommand = from.wrapperCommand;
        if (from.wrapperArgs != null) {
            wrapperArgs = new String[from.wrapperArgs.length];
            System.arraycopy(from.wrapperArgs, 0, wrapperArgs, 0, from.wrapperArgs.length);
        }
        if (from.wrapperEnv != null) {
            wrapperEnv = new String[from.wrapperEnv.length];
            System.arraycopy(from.wrapperEnv, 0, wrapperEnv, 0, from.wrapperEnv.length);
        }
        fileLocator = from.fileLocator;
        outConvertors = new ArrayList<>(from.outConvertors);
        errConvertors = new ArrayList<>(from.errConvertors);
        setOutProcessorFactory(from.outProcessorFactory);
        setErrProcessorFactory(from.errProcessorFactory);
        lineBased(from.lineBased);
        if (from.addStandardConvertors) {
            addStandardRecognizers();
        }
        postExecutionHook = from.postExecutionHook;
    }

    public ExecutionDescriptor toExecutionDescriptor() {
        return descriptor;
    }

    //internal process control    
    private ExecutionDescriptor descriptor = new ExecutionDescriptor()
            .frontWindow(true).controllable(true).inputVisible(true)
                .showProgress(true).showSuspended(true);
    
    //private InputOutput io;

    /**
     * Execute the process described by this object
     * @return a Future object that provides the status of the running process
     */
    public synchronized Future<Integer> run(){
        try {
            // Setup process Information
            
            
            // Setup Descriptor Information
            //descriptor = buildDescriptor();
            String encoding = null;
            if (script != null) {
                File scriptFile = new File(script);
                FileObject scriptFileObject = FileUtil.toFileObject(scriptFile);
                
                PythonFileEncodingQuery encodingQuery = new PythonFileEncodingQuery();
                encoding = encodingQuery.getPythonFileEncoding(scriptFileObject.getInputStream());
                if (encoding != null) {
                    descriptor = descriptor.charset(Charset.forName(encoding));                    
                }
            }
            
            //process.
            //build Service
            ExecutionService service = 
                    ExecutionService.newService(
                    buildProcess(encoding),
                    descriptor, displayName);
            //io = descriptor.getInputOutput();
            // Start Service
           return service.run();
            //io = InputOutputManager.getInputOutput(displayName, true, path).getInputOutput();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        
    }


    public ExternalProcessBuilder buildProcess(String encoding) throws IOException{
        ExternalProcessBuilder processBuilder =
                    new ExternalProcessBuilder(command);
            processBuilder = processBuilder.workingDirectory(new File(workingDirectory));
            if ( (commandArgs != null) && ( commandArgs.trim().length() > 0 )  )
               processBuilder = processBuilder.addArgument(commandArgs);

            if (wrapperCommand != null) {
                processBuilder = processBuilder.addArgument(wrapperCommand);
                if (wrapperArgs != null && wrapperArgs.length > 0) {
                    for (String arg : wrapperArgs) {
                        processBuilder = processBuilder.addArgument(arg);
                    }
                }
                if (wrapperEnv != null && wrapperEnv.length > 0) {
                    for (String env : wrapperEnv) {
                        int index = env.indexOf('=');
                        assert index != -1;
                        processBuilder = processBuilder.addEnvironmentVariable(env.substring(0, index), env.substring(index+1));
                    }
                }
            }

            if(script != null)
                processBuilder = processBuilder.addArgument(script);
            if(scriptArgs != null) {
                // a natural python tuple on python side
                String args[] = org.openide.util.Utilities.parseParameters(scriptArgs) ;
                for (String arg : args) {
                    processBuilder = processBuilder.addArgument(arg);
                }
            }
            processBuilder = processBuilder.redirectErrorStream(redirect);
            if(path != null){
                if (encoding != null) {
                    processBuilder = 
                            processBuilder.addEnvironmentVariable("PYTHONIOENCODING", encoding); // NOI18N
                }
                if(command.toLowerCase().contains("jython")){
//                    String commandPath = "-Dpython.path=" + path;
//                    processBuilder = processBuilder.addArgument(commandPath);
                     processBuilder =
                            processBuilder.addEnvironmentVariable("JYTHONPATH", path);
                     if(javapath != null) {
                        processBuilder =
                               processBuilder.addEnvironmentVariable("CLASSPATH", javapath);
                     }
                }else{
                    processBuilder =
                            processBuilder.addEnvironmentVariable("PYTHONPATH", path);
                }                
            }

            return processBuilder;
    }
    
    private ExecutionDescriptor buildDescriptor(){
        
            return descriptor;
    }
    public synchronized String getCommand() {
        return command;
    }

    public synchronized void setCommand(String command) {
        this.command = command;
    }

    public synchronized String getCommandArgs() {
        return commandArgs;
    }

    public synchronized void setCommandArgs(String commandArgs) {
        this.commandArgs = commandArgs;
    }

    public synchronized String getPath() {
        return path;
    }

    public synchronized void setJavaPath(String javapath) {
        this.javapath = javapath;
    }

    public synchronized String getJavaPath() {
        return javapath;
    }

    public synchronized void setPath(String path) {
        this.path = path;
    }

    public synchronized String getScript() {
        return script;
    }

    public synchronized void setScript(String script) {
        this.script = script;
    }

    public synchronized String getScriptArgs() {
        return scriptArgs;
    }

    public synchronized void setScriptArgs(String scriptArgs) {
        this.scriptArgs = scriptArgs;
    }

    public synchronized String getWorkingDirectory() {
        return workingDirectory;
    }

    public synchronized void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public synchronized String getDisplayName() {
        return displayName;
    }

    public synchronized void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public synchronized void setWrapperCommand(String wrapperCommand, String[] wrapperArgs, String[] wrapperEnv) {
        this.wrapperCommand = wrapperCommand;
        this.wrapperArgs = wrapperArgs;
        this.wrapperEnv = wrapperEnv;
    }

    public synchronized void setShowControls(boolean showControls) {
       descriptor = descriptor.controllable(showControls);
    }

    public PythonExecution addOutConvertor(LineConvertor convertor) {
        this.outConvertors.add(convertor);
        descriptor = descriptor.outConvertorFactory(lineConvertorFactory(outConvertors));
        return this;
    }

    public PythonExecution addErrConvertor(LineConvertor convertor) {
        this.errConvertors.add(convertor);
        descriptor = descriptor.errConvertorFactory(lineConvertorFactory(errConvertors));
        return this;
    }

    public synchronized void addStandardRecognizers() {
        this.addStandardConvertors = true;
        descriptor = descriptor.outConvertorFactory(lineConvertorFactory(outConvertors));
        descriptor = descriptor.errConvertorFactory(lineConvertorFactory(errConvertors));
    }

    public void setErrProcessorFactory(InputProcessorFactory errProcessorFactory) {
        this.errProcessorFactory = errProcessorFactory;
        descriptor = descriptor.errProcessorFactory(errProcessorFactory);
    }

    public void setOutProcessorFactory(InputProcessorFactory outProcessorFactory) {
        this.outProcessorFactory = outProcessorFactory;
        descriptor = descriptor.outProcessorFactory(outProcessorFactory);
    }

    public PythonExecution lineBased(boolean lineBased) {
        this.lineBased = lineBased;
        if (lineBased) {
            descriptor = descriptor.errLineBased(lineBased).outLineBased(lineBased);
        }

        return this;
    }

    private LineConvertorFactory lineConvertorFactory(List<LineConvertor> convertors) {
        LineConvertor[] convertorArray = convertors.toArray(new LineConvertor[convertors.size()]);
        if (addStandardConvertors) {
            return PythonLineConvertorFactory.withStandardConvertors(fileLocator, convertorArray);
        }
        return PythonLineConvertorFactory.create(fileLocator, convertorArray);
    }


    public synchronized void setShowInput(boolean showInput) {
        descriptor = descriptor.inputVisible(showInput);
    }

    public synchronized void setRedirectError(boolean redirect){
        this.redirect = redirect;
    }

    public synchronized void setShowProgress(boolean showProgress) {
        descriptor = descriptor.showProgress(showProgress);
    }
    /**
     * Can the process be suppended
     * @param showSuspended boolean to set the status 
     */
    public synchronized void setShowSuspended(boolean showSuspended) {
        descriptor = descriptor.showSuspended(showSuspended);
    }    
    /**
     * Show the window of the running process
     * @param showWindow display the windown or not?
     */
    public synchronized void setShowWindow(boolean showWindow) {
        descriptor = descriptor.frontWindow(showWindow);
    }
    
    private final PythonOutputProcessor outProcessor = new PythonOutputProcessor();
    /**
     * Attach a Processor to collect the output of the running process
     */
    public void attachOutputProcessor(){
        descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor() {
                return outProcessor;
            }

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return outProcessor;
            }
        });
    }

    public void setPostExecutionHook(Runnable runnable) {
        postExecutionHook = runnable;
        descriptor = descriptor.postExecution(runnable);
    }

    public Runnable getPostExecutionHook() {
        return postExecutionHook;
    }

    /**
     * Retive the output form the running process
     * @return a string reader for the process
     */
    public Reader getOutput(){
        return new StringReader(outProcessor.getData());
    }
    /**
     * Attach input processor to the running process
     */
    public void attachInputProcessor(){
        //descriptor = descriptor.
    }
    /**
     * Writes data to the running process
     * @return StringWirter
     */
    public Writer getInput(){
        return null;
    }
}
