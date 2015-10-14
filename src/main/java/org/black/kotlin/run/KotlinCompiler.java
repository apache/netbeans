package org.black.kotlin.run;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.run.output.CompilerOutputData;
import org.black.kotlin.run.output.CompilerOutputElement;
import org.black.kotlin.run.output.CompilerOutputParser;
import org.black.kotlin.run.output.KotlinCompilerResult;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.common.CLICompiler;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.cli.jvm.compiler.CompileEnvironmentException;
import org.jetbrains.kotlin.cli.jvm.compiler.CompilerJarLocator;
import org.jetbrains.kotlin.config.Services;
import org.jetbrains.kotlin.config.Services.Builder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Александр
 */
public class KotlinCompiler {

    public final static KotlinCompiler INSTANCE = new KotlinCompiler();
    private KotlinCompilerResult output;
    
    private KotlinCompiler(){}
  
    
    public void compile(KotlinProject proj) throws IOException{
        output = execCompiler(configureArguments(proj));
        for (CompilerOutputElement el : output.getCompilerOutput().getList()){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(el.getMessage()));
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(output.compiledCorrectly()));
    }
    
    private String[] configureArguments(KotlinProject proj) throws IOException{
        List<String> args = new ArrayList<String>();
        
        args.add("-kotlin-home");
        args.add(ProjectUtils.KT_HOME);
        System.out.println(System.getenv("KT_HOME"));
        args.add(ProjectUtils.findMain(proj.getProjectDirectory().getChildren()));
        args.add("-include-runtime");
        args.add("-d");
        args.add(ProjectUtils.getOutputDir(proj));
     
        
        
        return args.toArray(new String[args.size()]);
    }
    
    private KotlinCompilerResult execCompiler(@NotNull String[] arguments){
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        
        doMain(new K2JVMCompiler(), out, arguments);
    
        BufferedReader reader = new BufferedReader(new StringReader(outputStream.toString()));
        return parseCompilerOutput(reader);
    }
    
    public int doMain(@NotNull CLICompiler<?> compiler, @NotNull PrintStream errorStream, @NotNull String[] args) {
        System.setProperty("java.awt.headless", "true");
        ExitCode exitCode = doMainNoExitWithHtmlOutput(compiler, errorStream, args);
        
        return exitCode.getCode();
    }
    
    private ExitCode doMainNoExitWithHtmlOutput(
            @NotNull CLICompiler<?> compiler, 
            @NotNull PrintStream errorStream, 
            @NotNull String[] args) {
        try {
            Builder builder = new Services.Builder();
            builder.register(CompilerJarLocator.class, new CompilerJarLocator() {
                @NotNull
                @Override
                public File getCompilerJar() {
                    return new File(ProjectUtils.buildLibPath("kotlin-compiler"));
                }
            });
            
            return compiler.execAndOutputXml(errorStream, builder.build(), args);
        } catch (CompileEnvironmentException e) {
            errorStream.println(e.getMessage());
            return ExitCode.INTERNAL_ERROR;
        }
    }
    
        @NotNull
    private KotlinCompilerResult parseCompilerOutput(Reader reader) {
        final CompilerOutputData compilerOutput = new CompilerOutputData(); 
        
        final List<CompilerMessageSeverity> severities = new ArrayList<CompilerMessageSeverity>();
        CompilerOutputParser.parseCompilerMessagesFromReader(
                new MessageCollector() {
                    @Override
                    public void report(@NotNull CompilerMessageSeverity messageSeverity, @NotNull String message,
                            @NotNull CompilerMessageLocation messageLocation) {
                        severities.add(messageSeverity);
                        compilerOutput.add(messageSeverity, message, messageLocation);
                    }
                },
                reader);
        
        boolean result = true;
        for (CompilerMessageSeverity severity : severities) {
            if (severity.equals(CompilerMessageSeverity.ERROR) || severity.equals(CompilerMessageSeverity.EXCEPTION)) {
                result = false;
                break;
            }
        }
        
        return new KotlinCompilerResult(result, compilerOutput);
    }

    
}
