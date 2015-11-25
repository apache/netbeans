package org.black.kotlin.run;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.project.KotlinProjectConstants;
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
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Александр
 */
public class KotlinCompiler {

    public final static KotlinCompiler INSTANCE = new KotlinCompiler();
    private KotlinCompilerResult output;

    private KotlinCompiler() {
    }

    public void antBuild(KotlinProject proj) {
        try {
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"build"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void antRun(KotlinProject proj) throws IOException, InterruptedException {
        try {
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"run"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void run(KotlinProject proj) throws IOException, InterruptedException {
        compile(proj);

        File jar = new File(proj.getProjectDirectory().getFileObject("build").getPath()
                + "/");
        Process runProcess = Runtime.getRuntime().exec("java -jar " + proj.getProjectDirectory().getName() + ".jar", null, jar);
        runProcess.waitFor();

        InputStream in = runProcess.getInputStream();
        InputStream err = runProcess.getErrorStream();

        InputStreamReader inReader = new InputStreamReader(in);
        InputStreamReader errReader = new InputStreamReader(err);
        BufferedReader inBR = new BufferedReader(inReader);
        BufferedReader errBR = new BufferedReader(errReader);
//        Desktop.getDesktop().open(new File(proj.getProjectDirectory().getFileObject("build").getPath()
//                + "/" + proj.getProjectDirectory().getName() + ".jar"));
        InputOutput io = IOProvider.getDefault().getIO("Run (" + proj.getProjectDirectory().getName() + ")", false);
        io.select();
        String line;
        while ((line = inBR.readLine()) != null) {
            io.getOut().println(line);
        }
        while ((line = errBR.readLine()) != null) {
            io.getErr().println(line);
        }

        io.getOut().close();
        io.getErr().close();
    }

    public void compile(KotlinProject proj) throws IOException {

        InputOutput io = IOProvider.getDefault().getIO("Build (" + proj.getProjectDirectory().getName() + ")", false);

        io.select();
        io.getOut().println("Build process started");

        output = execCompiler(configureArguments(proj));

        for (CompilerOutputElement el : output.getCompilerOutput().getList()) {
            io.getOut().println("[" + el.getMessageSeverity() + "] " + el.getMessage());
        }

        if (output.compiledCorrectly()) {
            io.getOut().println("Build process finished successfully");
        } else {
            io.getErr().println("Build process finished with errors");
        }

        io.getOut().close();
        io.getErr().close();
    }

    private String[] configureArguments(KotlinProject proj) throws IOException {
        List<String> args = new ArrayList<String>();

        args.add("-kotlin-home");
        args.add(ProjectUtils.KT_HOME);

        args.add("-include-runtime");
//        args.add("-no-jdk");
//        args.add("-no-stdlib");

        Sources sources = org.netbeans.api.project.ProjectUtils.getSources(proj);

        StringBuilder classPath = new StringBuilder();
        String pathSeparator = System.getProperty("path.separator");

        for (String cp : ProjectUtils.getClasspath()) {
            classPath.append(cp).append(pathSeparator);
        }
//        
//        args.add("-classpath");
//        args.add(classPath.toString());

        args.add("-d");
        args.add(ProjectUtils.getOutputDir(proj));

        for (SourceGroup srcGrp : sources.getSourceGroups(KotlinProjectConstants.KOTLIN_SOURCE.toString())) {
            args.add(srcGrp.getName());
        }

        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(args.toString()));

        return args.toArray(new String[args.size()]);
    }

    private KotlinCompilerResult execCompiler(@NotNull String[] arguments) {

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
