package org.black.kotlin.run;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.project.KotlinProject;
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

/**
 *
 * @author Александр
 */
public class KotlinCompiler {

    public final static KotlinCompiler INSTANCE = new KotlinCompiler();
    
    private KotlinCompiler(){}
  
    
    public void compile(KotlinProject proj) throws IOException{
        execCompiler(configureArguments(proj));
    }
    
    private String[] configureArguments(KotlinProject proj) throws IOException{
        List<String> args = new ArrayList<String>();
        
        args.add("-kotlin-home");
        args.add(ProjectUtils.KT_HOME);
        args.add(ProjectUtils.findMain(proj.getProjectDirectory().getChildren()));
        args.add("-include-runtime");
        args.add("-d");
        args.add(ProjectUtils.getOutputDir(proj));
        
        return args.toArray(new String[args.size()]);
    }
    
    private void execCompiler(@NotNull String[] arguments){
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outputStream);
        
        doMain(new K2JVMCompiler(), out, arguments);
    
        BufferedReader reader = new BufferedReader(new StringReader(outputStream.toString()));
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
    

    
}
