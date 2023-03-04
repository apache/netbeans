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
package org.netbeans.modules.php.apigen.commands;

import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.apigen.options.ApiGenOptions;
import org.netbeans.modules.php.apigen.ui.ApiGenPreferences;
import org.netbeans.modules.php.apigen.ui.options.ApiGenOptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents <a href="http://apigen.org/">apigen</a> command line tool.
 */
public final class ApiGenScript {

    static final Logger LOGGER = Logger.getLogger(ApiGenScript.class.getName());

    public static final String SCRIPT_NAME = "apigen"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    public static final String ACCESS_LEVEL_PUBLIC = "public"; // NOI18N
    public static final String ACCESS_LEVEL_PROTECTED = "protected"; // NOI18N
    public static final String ACCESS_LEVEL_PRIVATE = "private"; // NOI18N

    public static final String DEFAULT_CONFIG_NAME = "apigen.neon"; // NOI18N
    public static final String DEFAULT_ACCESS_LEVELS = ACCESS_LEVEL_PUBLIC + "," + ACCESS_LEVEL_PROTECTED; // NOI18N
    public static final boolean DEFAULT_INTERNAL = false;
    public static final boolean DEFAULT_PHP = true;
    public static final boolean DEFAULT_TREE = true;
    public static final boolean DEFAULT_DEPRECATED = false;
    public static final boolean DEFAULT_TODO = false;
    public static final boolean DEFAULT_DOWNLOAD = false;
    public static final boolean DEFAULT_SOURCE_CODE = true;

    private static final String GENERATE_PARAM = "generate"; // NOI18N
    private static final String SOURCE_PARAM = "--source"; // NOI18N
    private static final String DESTINATION_PARAM = "--destination"; // NOI18N
    private static final String TITLE_PARAM = "--title"; // NOI18N
    private static final String CONFIG_PARAM = "--config"; // NOI18N
    private static final String CHARSET_PARAM = "--charset"; // NOI18N
    private static final String EXCLUDE_PARAM = "--exclude"; // NOI18N
    private static final String ACCESS_LEVELS_PARAM = "--access-levels"; // NOI18N
    private static final String INTERNAL_PARAM = "--internal"; // NOI18N
    private static final String PHP_PARAM = "--php"; // NOI18N
    private static final String TREE_PARAM = "--tree"; // NOI18N
    private static final String DEPRECATED_PARAM = "--deprecated"; // NOI18N
    private static final String TODO_PARAM = "--todo"; // NOI18N
    private static final String DOWNLOAD_PARAM = "--download"; // NOI18N
    private static final String NO_SOURCE_CODE_PARAM = "--no-source-code"; // NOI18N

    private final String apiGenPath;


    private ApiGenScript(String apiGenPath) {
        this.apiGenPath = apiGenPath;
    }

    /**
     * Get the default, <b>valid only</b> ApiGen script.
     * @return the default, <b>valid only</b> ApiGen script.
     * @throws InvalidPhpExecutableException if ApiGen script is not valid.
     */
    public static ApiGenScript getDefault() throws InvalidPhpExecutableException {
        String apiGen = ApiGenOptions.getInstance().getApiGen();
        String error = validate(apiGen);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new ApiGenScript(apiGen);
    }

    @NbBundle.Messages("ApiGenScript.script.label=ApiGen script")
    public static String validate(String apiGenPath) {
        return PhpExecutableValidator.validateCommand(apiGenPath, Bundle.ApiGenScript_script_label());
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ApiGenScript.api.generating=ApiGen ({0})",
    })
    public void generateDocumentation(PhpModule phpModule) {
        assert !EventQueue.isDispatchThread();
        final String target = ApiGenPreferences.getTarget(phpModule, true);
        if (target == null) {
            // canceled
            return;
        }

        Future<Integer> result = new PhpExecutable(apiGenPath)
                .optionsSubcategory(ApiGenOptionsPanelController.OPTIONS_SUBPATH)
                .workDir(FileUtil.toFile(phpModule.getProjectDirectory()))
                .displayName(Bundle.ApiGenScript_api_generating(phpModule.getDisplayName()))
                .additionalParameters(getGenerateParams(phpModule))
                .run(getDescriptor());
        try {
            File targetDir = new File(target);
            if (result != null && result.get() == 0) {
                if (targetDir.isDirectory()) {
                    File index = new File(target, "index.html"); // NOI18N
                    if (index.isFile()) {
                        // false for pdf e.g.
                        HtmlBrowser.URLDisplayer.getDefault().showURL(Utilities.toURI(index).toURL());
                    }
                }
            }
            // refresh fs
            if (targetDir.isDirectory()) {
                FileUtil.refreshFor(targetDir);
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, ApiGenOptionsPanelController.OPTIONS_SUBPATH);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(ApiGenOptionsPanelController.getOptionsPath())
                .inputVisible(false);
    }

    private List<String> getGenerateParams(PhpModule phpModule) {
        List<String> params = new ArrayList<>();
        params.add(GENERATE_PARAM);
        if (ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.HAS_CONFIG)) {
            addConfig(phpModule, params);
        } else {
            addSource(phpModule, params);
            addDestination(phpModule, params);
            addTitle(phpModule, params);
            addCharsets(phpModule, params);
            addExcludes(phpModule, params);
            addAccessLevels(phpModule, params);
            addInternal(phpModule, params);
            addPhp(phpModule, params);
            addTree(phpModule, params);
            addDeprecated(phpModule, params);
            addTodo(phpModule, params);
            addDownload(phpModule, params);
            addSourceCode(phpModule, params);
        }
        return params;
    }

    private void addSource(PhpModule phpModule, List<String> params) {
        params.add(SOURCE_PARAM);
        params.add(FileUtil.toFile(phpModule.getSourceDirectory()).getAbsolutePath());
    }

    private void addDestination(PhpModule phpModule, List<String> params) {
        params.add(DESTINATION_PARAM);
        params.add(ApiGenPreferences.getTarget(phpModule, false));
    }

    private void addTitle(PhpModule phpModule, List<String> params) {
        params.add(TITLE_PARAM);
        params.add(ApiGenPreferences.get(phpModule, ApiGenPreferences.TITLE));
    }

    private void addConfig(PhpModule phpModule, List<String> params) {
        String config = ApiGenPreferences.get(phpModule, ApiGenPreferences.CONFIG);
        if (StringUtils.hasText(config)) {
            params.add(CONFIG_PARAM);
            params.add(config);
        }
    }

    private void addCharsets(PhpModule phpModule, List<String> params) {
        for (String charset : ApiGenPreferences.getMore(phpModule, ApiGenPreferences.CHARSETS)) {
            params.add(CHARSET_PARAM);
            params.add(charset);
        }
    }

    private void addExcludes(PhpModule phpModule, List<String> params) {
        for (String exclude : ApiGenPreferences.getMore(phpModule, ApiGenPreferences.EXCLUDES)) {
            params.add(EXCLUDE_PARAM);
            params.add(exclude);
        }
    }

    private void addAccessLevels(PhpModule phpModule, List<String> params) {
        for (String level : ApiGenPreferences.getMore(phpModule, ApiGenPreferences.ACCESS_LEVELS)) {
            params.add(ACCESS_LEVELS_PARAM);
            params.add(level);
        }
    }

    private void addInternal(PhpModule phpModule, List<String> params) {
        addBoolean(params, INTERNAL_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.INTERNAL));
    }

    private void addPhp(PhpModule phpModule, List<String> params) {
        addBoolean(params, PHP_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.PHP));
    }

    private void addTree(PhpModule phpModule, List<String> params) {
        addBoolean(params, TREE_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.TREE));
    }

    private void addDeprecated(PhpModule phpModule, List<String> params) {
        addBoolean(params, DEPRECATED_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.DEPRECATED));
    }

    private void addTodo(PhpModule phpModule, List<String> params) {
        addBoolean(params, TODO_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.TODO));
    }

    private void addDownload(PhpModule phpModule, List<String> params) {
        addBoolean(params, DOWNLOAD_PARAM, ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.DOWNLOAD));
    }

    private void addSourceCode(PhpModule phpModule, List<String> params) {
        if (ApiGenPreferences.getBoolean(phpModule, ApiGenPreferences.SOURCE_CODE)) {
            // enabled by default
            return;
        }
        params.add(NO_SOURCE_CODE_PARAM);
    }

    private void addBoolean(List<String> params, String param, boolean value) {
        if (!value) {
            return;
        }
        params.add(param);
    }

}
