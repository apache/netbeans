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

package org.netbeans.modules.php.phpdoc;

import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.phpdoc.ui.PhpDocPreferences;
import org.netbeans.modules.php.phpdoc.ui.options.PhpDocOptions;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public final class PhpDocScript {

    private static final Logger LOGGER = Logger.getLogger(PhpDocScript.class.getName());

    public static final String SCRIPT_NAME = "phpdoc"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    public static final String SCRIPT_NAME_PHAR = "phpDocumentor.phar"; // NOI18N
    public static final String OPTIONS_ID = "PhpDoc"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH + "/" + OPTIONS_ID; // NOI18N

    private static final boolean IS_WINDOWS = Utilities.isWindows();

    private final String phpDocPath;


    private PhpDocScript(String phpDocPath) {
        this.phpDocPath = phpDocPath;
    }

    /**
     * Get the default, <b>valid only</b> PhpDoc script.
     * @return the default, <b>valid only</b> PhpDoc script.
     * @throws InvalidPhpExecutableException if PhpDoc script is not valid.
     */
    public static PhpDocScript getDefault() throws InvalidPhpExecutableException {
        String phpDocPath = PhpDocOptions.getInstance().getPhpDoc();
        String error = validate(phpDocPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new PhpDocScript(phpDocPath);
    }

    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + OPTIONS_SUB_PATH; // NOI18N
    }

    public static String validate(String composerPath) {
        return PhpExecutableValidator.validateCommand(composerPath, NbBundle.getMessage(PhpDocScript.class, "LBL_PhpDocScript"));
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "PhpDocScript.run.title=phpDocumentor ({0})",
    })
    public void generateDocumentation(final PhpModule phpModule) {
        assert !EventQueue.isDispatchThread();
        String phpDocTarget = PhpDocPreferences.getPhpDocTarget(phpModule, true);
        if (phpDocTarget == null) {
            // canceled
            return;
        }

        String sanitizedPhpDocTarget = sanitizePath(phpDocTarget);
        Future<Integer> result = new PhpExecutable(phpDocPath)
                .optionsSubcategory(OPTIONS_SUB_PATH)
                .displayName(Bundle.PhpDocScript_run_title(phpModule.getDisplayName()))
                .additionalParameters(getParameters(sanitizedPhpDocTarget, phpModule))
                .run(getExecutionDescriptor(sanitizedPhpDocTarget));

        try {
            if (result != null && result.get() == 0) {
                File index = new File(phpDocTarget, "index.html"); // NOI18N
                if (index.isFile()) {
                    // false for pdf e.g.
                    HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(index).toURL());
                }
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, OPTIONS_SUB_PATH);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
    }

    private ExecutionDescriptor getExecutionDescriptor(String sanitizedPhpDocTarget) {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .frontWindow(false)
                .outConvertorFactory(new ErrorFileLineConvertorFactory(sanitizedPhpDocTarget))
                .optionsPath(getOptionsPath());
    }

    // #199449
    private String sanitizePath(String path) {
        if (IS_WINDOWS) {
            return path.replace(File.separatorChar, '/'); // NOI18N
        }
        return path;
    }

    private List<String> getParameters(String sanitizedPhpDocTarget, PhpModule phpModule) {
        return Arrays.asList(
                // command
                "run", // NOI18N
                // params
                "--ansi", // NOI18N
                "--progressbar", // NOI18N
                // from
                "--directory", // NOI18N
                sanitizePath(FileUtil.toFile(phpModule.getSourceDirectory()).getAbsolutePath()),
                // to
                "--target", // NOI18N
                sanitizedPhpDocTarget,
                // title
                "--title", // NOI18N
                PhpDocPreferences.getPhpDocTitle(phpModule));
    }

    private class ErrorFileLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        private final String docTarget;

        public ErrorFileLineConvertorFactory(String docTarget) {
            this.docTarget = docTarget;
        }

        @Override
        public LineConvertor newLineConvertor() {
            Pattern pattern = Pattern.compile("(.*)(" + Pattern.quote(docTarget) + "/?errors\\.html)(.*)"); // NOI18N
            return new ErrorFileLineConvertor(pattern);
        }

    }

    private class ErrorFileLineConvertor implements LineConvertor {

        private final Pattern pattern;

        public ErrorFileLineConvertor(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                try {
                    URL url = new URL("file://" + matcher.group(2)); // NOI18N
                    List<ConvertedLine> lines = new LinkedList<>();
                    if (!matcher.group(1).trim().isEmpty()) {
                        lines.add(ConvertedLine.forText(matcher.group(1), null));
                    }
                    lines.add(ConvertedLine.forText(matcher.group(2), new ErrorFileOutputListener(url)));
                    if (!matcher.group(3).trim().isEmpty()) {
                        lines.add(ConvertedLine.forText(matcher.group(3), null));
                    }
                    return lines;
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }

            return null;
        }

    }

    private class ErrorFileOutputListener implements OutputListener {

        private final URL url;

        public ErrorFileOutputListener(URL url) {
            this.url = url;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }

    }

}
