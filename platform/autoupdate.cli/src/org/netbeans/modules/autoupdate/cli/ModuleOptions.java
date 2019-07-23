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
package org.netbeans.modules.autoupdate.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.*;

import static org.netbeans.modules.autoupdate.cli.Bundle.*;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorker;

/**
 *
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.sendopts.OptionProcessor.class)
public class ModuleOptions extends OptionProcessor {
    private static final Logger LOG = Logger.getLogger(ModuleOptions.class.getName());
    
    private Option list;
    private Option install;
    private Option disable;
    private Option enable;
    private Option update;
    private Option refresh;
    private Option updateAll;
    private Option both;
    private Option extraUC;
    
    private Collection<UpdateUnitProvider> ownUUP = new HashSet<UpdateUnitProvider> ();
    
    /** Creates a new instance of ModuleOptions */
    public ModuleOptions() {
    }

    @NbBundle.Messages({
        "MSG_UpdateModules=Updates all or specified modules",
        "MSG_UpdateAll=Updates all modules",
        "MSG_Refresh=Refresh all catalogs",
        "MSG_ExtraUC=Add a extra Update Center (URL)"
    })
    private Option init() {
        if (both != null) {
            return both;
        }

        String b = "org.netbeans.modules.autoupdate.cli.Bundle";
        list = Option.shortDescription(
            Option.withoutArgument(Option.NO_SHORT_NAME, "list"), b, "MSG_ListModules"); // NOI18N
        install = Option.shortDescription(
            Option.additionalArguments(Option.NO_SHORT_NAME, "install"), b, "MSG_InstallModules"); // NOI18N
        disable = Option.shortDescription(
            Option.additionalArguments(Option.NO_SHORT_NAME, "disable"), b, "MSG_DisableModules"); // NOI18N
        enable = Option.shortDescription(
            Option.additionalArguments(Option.NO_SHORT_NAME, "enable"), b, "MSG_EnableModules"); // NOI18N
        update = Option.shortDescription(
            Option.additionalArguments(Option.NO_SHORT_NAME, "update"), b, "MSG_UpdateModules"); // NOI18N
        refresh = Option.shortDescription(
            Option.withoutArgument(Option.NO_SHORT_NAME, "refresh"), b, "MSG_Refresh"); // NOI18N
        updateAll = Option.shortDescription(
            Option.withoutArgument(Option.NO_SHORT_NAME, "update-all"), b, "MSG_UpdateAll"); // NOI18N
        extraUC = Option.shortDescription(
            Option.requiredArgument(Option.NO_SHORT_NAME, "extra-uc"), b, "MSG_ExtraUC"); // NOI18N
        
        Option oper = OptionGroups.someOf(refresh, list, install, disable, enable, update, updateAll, extraUC);
        Option modules = Option.withoutArgument(Option.NO_SHORT_NAME, "modules");
        both = OptionGroups.allOf(modules, oper);
        return both;
    }

    @Override
    public Set<Option> getOptions() {
        return Collections.singleton(init());
    }
    
    private void refresh(Env env) throws CommandException {
        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
            try {
                env.getOutputStream().println("Refreshing " + p.getDisplayName());
                p.refresh(null, true);
            } catch (IOException ex) {
                throw (CommandException)new CommandException(31, ex.getMessage()).initCause(ex);
            }
        }
    }

    @NbBundle.Messages({
        "MSG_ListHeader_CodeName=Code Name",
        "MSG_ListHeader_Version=Version",
        "MSG_ListHeader_State=State"
    })
    private void listAllModules(PrintStream out) {
        List<UpdateUnit> modules = UpdateManager.getDefault().getUpdateUnits();
        
        PrintTable table = new PrintTable(
            MSG_ListHeader_CodeName(), MSG_ListHeader_Version(), MSG_ListHeader_State()
        );
        table.setLimits(50, -1, -1);
        for (UpdateUnit uu : modules) {
            table.addRow(Status.toArray(uu));
        }
        table.write(out);
        out.flush();
    }

    private static <T extends Throwable> T initCause(T t, Throwable cause) {
        t.initCause(cause);
        return t;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        try {
            if (optionValues.containsKey(extraUC)) {
                extraUC(env, optionValues.get(extraUC));
            }
            if (optionValues.containsKey(refresh)) {
                refresh(env);
            }

            if (optionValues.containsKey(list)) {
                listAllModules(env.getOutputStream());
            }

            if (optionValues.containsKey(install)) {
                install(env, optionValues.get(install));
            }

            try {

                if (optionValues.containsKey(disable)) {
                    changeModuleState(optionValues.get(disable), false);
                }

                if (optionValues.containsKey(enable)) {
                    changeModuleState(optionValues.get(enable), true);
                }
            } catch (InterruptedException ex) {
                throw initCause(new CommandException(4), ex);
            } catch (IOException ex) {
                throw initCause(new CommandException(4), ex);
            } catch (OperationException ex) {
                throw initCause(new CommandException(4), ex);
            }

            if (optionValues.containsKey(updateAll)) {
                updateAll(env);
            }
            if (optionValues.containsKey(update)) {
                updateModules(env, optionValues.get(update));
            }
        } finally {
            for (UpdateUnitProvider uuc : ownUUP) {
                UpdateUnitProviderFactory.getDefault().remove(uuc);
            }
        }
        
    }

    private void changeModuleState(String[] cnbs, boolean enable) throws IOException, CommandException, InterruptedException, OperationException {
        for (String cnb : cnbs) {
            int slash = cnb.indexOf('/');
            if (slash >= 0) {
                cnb = cnb.substring(0, slash);
            }
        }
        
        Set<String> all = new HashSet<String>(Arrays.asList(cnbs));

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits();
        OperationContainer<OperationSupport> operate = enable ? OperationContainer.createForEnable() : OperationContainer.createForDisable();
        for (UpdateUnit updateUnit : units) {
            if (all.contains(updateUnit.getCodeName())) {
                if (enable) {
                    operate.add(updateUnit, updateUnit.getInstalled());
                } else {
                    operate.add(updateUnit, updateUnit.getInstalled());
                }
            }
        }
        OperationSupport support = operate.getSupport();
        support.doOperation(null);
    }

    @NbBundle.Messages({
        "MSG_UpdateNotFound=Updates not found.",
        "# {0} - pattern",
        "MSG_UpdateNoMatchPattern=Nothing to update. The pattern {0} has no match among available updates.",
        "# {0} - module name",
        "# {1} - installed version",
        "# {2} - available version",
        "MSG_Update=Will update {0}@{1} to version {2}",
        "# {0} - plugin name",
        "MSG_Download=Downloading {0}"
    })
    private void updateModules(final Env env, String... pattern) throws CommandException {
        if (! initialized()) {
            refresh(env);
        }
        Pattern[] pats = findMatcher(env, pattern);
        
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        final Collection <String> firstClass = getFirstClassModules();
        boolean firstClassHasUpdates = false;
        OperationContainer<InstallSupport> operate = OperationContainer.createForUpdate();
        if (! firstClass.isEmpty() && pattern.length == 0) {
            for (UpdateUnit uu : units) {
                if (uu.getInstalled() == null) {
                    continue;
                }
                final List<UpdateElement> updates = uu.getAvailableUpdates();
                if (updates.isEmpty()) {
                    continue;
                }
                if (! firstClass.contains (uu.getCodeName ())) {
                    continue;
                }
                final UpdateElement ue = updates.get(0);
                env.getOutputStream().println(
                    Bundle.MSG_Update(uu.getCodeName(), uu.getInstalled().getSpecificationVersion(), ue.getSpecificationVersion()
                ));
                if (operate.canBeAdded(uu, ue)) {
                    LOG.fine("  ... update " + uu.getInstalled() + " -> " + ue);
                    firstClassHasUpdates = true;
                    OperationInfo<InstallSupport> info = operate.add(ue);
                    if (info != null) {
                        Set<UpdateElement> requiredElements = info.getRequiredElements();
                        LOG.fine("      ... add required elements: " + requiredElements);
                        operate.add(requiredElements);
                    }
                }
            }
        } 
        if (! firstClassHasUpdates) {
            for (UpdateUnit uu : units) {
                if (uu.getInstalled() == null) {
                    continue;
                }
                final List<UpdateElement> updates = uu.getAvailableUpdates();
                if (updates.isEmpty()) {
                    continue;
                }
                if (pattern.length > 0 && !matches(uu.getCodeName(), pats)) {
                    continue;
                }
                final UpdateElement ue = updates.get(0);
                env.getOutputStream().println(
                    Bundle.MSG_Update(uu.getCodeName(), uu.getInstalled().getSpecificationVersion(), ue.getSpecificationVersion()
                ));
                if (operate.canBeAdded(uu, ue)) {
                    LOG.fine("  ... update " + uu.getInstalled() + " -> " + ue);
                    OperationInfo<InstallSupport> info = operate.add(ue);
                    if (info != null) {
                        Set<UpdateElement> requiredElements = info.getRequiredElements();
                        LOG.fine("      ... add required elements: " + requiredElements);
                        operate.add(requiredElements);
                    }
                }
            }
        }
        final InstallSupport support = operate.getSupport();
        if (support == null) {
            env.getOutputStream().println(pats == null || pats.length == 0 ? Bundle.MSG_UpdateNotFound() : Bundle.MSG_UpdateNoMatchPattern(Arrays.asList(pats)));
            env.getOutputStream().println("updates=0"); // NOI18N
            return;
        }
        env.getOutputStream().println("updates=" + operate.listAll().size()); // NOI18N
        ProgressHandle downloadHandle = new CLIInternalHandle("downloading-updates", env).createProgressHandle(); // NOI18N
        downloadHandle.setInitialDelay(0);
        try {
            final Validator res1 = support.doDownload(downloadHandle, null, false);

            Installer res2 = support.doValidate(res1, null);

            ProgressHandle installHandle = new CLIInternalHandle("installing-updates", env).createProgressHandle(); // NOI18N
            installHandle.setInitialDelay(0);
            Restarter res3 = support.doInstall(res2, installHandle);
            if (res3 != null) {
                support.doRestart(res3, null);
            }
        } catch (OperationException ex) {
            try {
                support.doCancel();
                throw (CommandException)new CommandException(33, ex.getMessage()).initCause(ex);
            } catch (OperationException ex1) {
                throw (CommandException)new CommandException(33, ex1.getMessage()).initCause(ex1);
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - regexp",
        "MSG_CantCompileRegex=Cannot understand regular expession ''{0}''"
    })
    private static Pattern[] findMatcher(Env env, String[] pattern) {
        Pattern[] arr = new Pattern[pattern.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                arr[i] = Pattern.compile(pattern[i]);
            } catch (PatternSyntaxException e) {
                env.getErrorStream().println(Bundle.MSG_CantCompileRegex(pattern[i]));
            }
        }
        return arr;
    }

    private static boolean matches(String txt, Pattern[] pats) {
        for (Pattern p : pats) {
            if (p == null) {
                continue;
            }
            if (p.matcher(txt).matches()) {
                return true;
            }
        }
        return false;
    }

    @NbBundle.Messages({
        "# {0} - module name",
        "# {1} - module version",
        "MSG_Installing=Installing {0}@{1}",
        "# {0} - paterns",
        "MSG_InstallNoMatch=Cannot install. No match for {0}."
    })
    private void install(final Env env, String... pattern) throws CommandException {
        if (! initialized()) {
            refresh(env);
        }

        Pattern[] pats = findMatcher(env, pattern);

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits();
        OperationContainer<InstallSupport> operate = OperationContainer.createForInstall();
        for (UpdateUnit uu : units) {
            if (uu.getInstalled() != null) {
                continue;
            }
            if (!matches(uu.getCodeName(), pats)) {
                continue;
            }
            if (uu.getAvailableUpdates().isEmpty()) {
                continue;
            }
            UpdateElement ue = uu.getAvailableUpdates().get(0);
            env.getOutputStream().println(
                    Bundle.MSG_Installing(uu.getCodeName(), ue.getSpecificationVersion()));
            operate.add(ue);
        }
        final InstallSupport support = operate.getSupport();
        if (support == null) {
            env.getOutputStream().println(Bundle.MSG_InstallNoMatch(Arrays.asList(pats)));
            return;
        }
        try {
            env.getOutputStream().println("modules=" + operate.listAll().size()); // NOI18N
            ProgressHandle downloadHandle = new CLIInternalHandle("downloading-modules", env).createProgressHandle(); // NOI18N
            downloadHandle.setInitialDelay(0);
            final Validator res1 = support.doDownload(downloadHandle, null, false);

            Installer res2 = support.doValidate(res1, null);

            ProgressHandle installHandle = new CLIInternalHandle("installing-modules", env).createProgressHandle(); // NOI18N
            installHandle.setInitialDelay(0);
            Restarter res3 = support.doInstall(res2, installHandle);
            if (res3 != null) {
                support.doRestart(res3, null);
            }
        } catch (OperationException ex) {
            // a hack
            if (OperationException.ERROR_TYPE.INSTALL.equals(ex.getErrorType())) {
                // probably timeout of loading
                env.getErrorStream().println(ex.getLocalizedMessage());
                throw (CommandException) new CommandException(34, ex.getMessage()).initCause(ex);
            } else {
                try {
                    support.doCancel();
                    throw (CommandException) new CommandException(32, ex.getMessage()).initCause(ex);
                } catch (OperationException ex1) {
                    throw (CommandException) new CommandException(32, ex1.getMessage()).initCause(ex1);
                }
            }
        }
    }
    
    private void updateAll(Env env) throws CommandException {
        updateModules(env);
    }

    
    @NbBundle.Messages({
        "MSG_NoURL=None extra Update Center (URL) specified."
    })
    private void extraUC(Env env, String... urls) throws CommandException {
        List<URL> url2UC = new ArrayList<URL> (urls.length);
        for (String spec : urls) {
            try {
                url2UC.add(new URL(spec));
            } catch (MalformedURLException ex) {
                throw initCause(new CommandException(4), ex);
            }
        }
        for (URL url : url2UC) {
            ownUUP.add(UpdateUnitProviderFactory.getDefault().create(Long.toString(System.currentTimeMillis()), url.toExternalForm(), url));
        }
        refresh(env);        
    }

    private boolean initialized() {
        Preferences pref = NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
        long last = pref.getLong("lastCheckTime", -1);
        return last != -1;
    }    

    private static final String PLUGIN_MANAGER_FIRST_CLASS_MODULES = "plugin.manager.first.class.modules"; // NOI18N
    
    private Collection<String> getFirstClassModules() {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate"); // NOI18N
        String names = p.get(PLUGIN_MANAGER_FIRST_CLASS_MODULES, "");
        Set<String> res = new HashSet<String> ();
        StringTokenizer en = new StringTokenizer (names, ","); // NOI18N
        while (en.hasMoreTokens ()) {
            res.add (en.nextToken ().trim ());
        }
        return res;
    }
    
    private class CLIInternalHandle extends InternalHandle {
        public CLIInternalHandle(String displayName, Env env) {
            super(displayName, null, false);
            setController(new Controller(new CLIProgressUIWorker(env)));
        }
    }

    private class CLIProgressUIWorker implements ProgressUIWorker { 
        private final Env env;
        public CLIProgressUIWorker(Env env) {
            this.env = env;
        }
        @Override
        public void processProgressEvent(ProgressEvent event) {
            env.getOutputStream().println(event.getMessage());
        }
        @Override
        public void processSelectedProgressEvent(ProgressEvent event) {
            env.getOutputStream().println(event.getMessage());
        }
    }
    
}
