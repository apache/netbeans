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
package org.netbeans.modules.analysis.spi;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.AnalysisProblem;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.analysis.ui.AdjustConfigurationPanel.ErrorListener;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**A static analyzer. Called by the infrastructure on a given {@link Scope} to perform
 * the analysis and return the found warnings as {@link ErrorDescription}s.
 *
 * It is intended to be installed in the global lookup, using e.g. {@link ServiceProvider}.
 *
 * @author lahvac
 */
public interface Analyzer extends Cancellable {

    /**Perform the analysis over the {@link Scope} defined in the {@link Context}
     * given while constructing the {@link Analyzer}.
     *
     * @return the found warnings
     */
    public Iterable<? extends ErrorDescription> analyze();

    public abstract static class AnalyzerFactory {
        private final String id;
        private final String displayName;
        private final String iconPath;
        private final Image icon;

        /**
         *
         * @param id a unique id of the analyzer
         * @param displayName the display name of the analyzer
         * @param iconPath a path to icon associated with this analyzer
         */
        public AnalyzerFactory(String id, String displayName, String iconPath) {
            this.id = id;
            this.displayName = displayName;
            this.iconPath = iconPath;
            this.icon = null;
        }
        
        /**
         *
         * @param id a unique id of the analyzer
         * @param displayName the display name of the analyzer
         * @param icon an icon associated with this analyzer
         * @since 1.6
         */
        public AnalyzerFactory(String id, String displayName, Image icon) {
            this.id = id;
            this.displayName = displayName;
            this.iconPath = null;
            this.icon = icon;
        }

        /**If additional modules are required to run the analysis (for the given {@code context}),
         * return their description.
         *
         * @param context over which the analysis is going to be performed
         * @return descriptions of the missing plugins, if any
         */
        public Collection<? extends MissingPlugin> requiredPlugins(Context context) {
            return Collections.emptyList();
        }

        public abstract Iterable<? extends WarningDescription> getWarnings();

        public abstract @CheckForNull <D, C extends JComponent> CustomizerProvider<D, C> getCustomizerProvider();

        /**
         *
         * @param context containing the required {@link Scope}
         * @return
         */
        public abstract Analyzer createAnalyzer(Context context);

        /**
         * Creates a new {@link Analyzer} with a context and warning collector.
         * @param context the {@link Context} of the analysis
         * @param result the warning collector
         * @return the {@link Analyzer}
         * @since 1.16
         */
        public Analyzer createAnalyzer(Context context, Result result) {
            return createAnalyzer(context);
        }

        //XXX: should be protected
        public void warningOpened(ErrorDescription warning) {}
    }

    /**
     * Collector of the analysis problems.
     * The waring added into the {@link Result} are merged with
     * warnings returned by the {@link Analyzer#analyze()} methods.
     * @since 1.16
     */
    public static final class Result {
                
        private final List<ErrorDescription> errors;
        private final Map<ErrorDescription, Project> errorsToProjects;
        private final Collection<AnalysisProblem> analysisProblems;

        Result(List<ErrorDescription> errors, Map<ErrorDescription, Project> errorsToProjects, Collection<AnalysisProblem> analysisProblems) {
            this.errors = errors;
            this.errorsToProjects = errorsToProjects;
            this.analysisProblems = analysisProblems;
        }

        /**
         * Reports an analysis problem.
         * @param displayName the display name of the problem
         * @param description the more detailed description of the problem
         */
        public void reportAnalysisProblem(String displayName, CharSequence description) {
            analysisProblems.add(new AnalysisProblem(displayName, description));
        }

        /**
         * Reports a new warning.
         * @param errorDescription the warning
         */
        public void reportError(@NonNull final ErrorDescription errorDescription) {
            errors.add(errorDescription);
        }

        /**
         * Reports a new warning related to the given project.
         * The method should be used only for warning in project dependencies
         * which are not owned by analyzed project.
         * @param owner the project to which the problem is related
         * @param errorDescription the warning
         */
        public void reportError(@NonNull final Project owner, @NonNull final ErrorDescription errorDescription) {
            errors.add(errorDescription);            
            errorsToProjects.put(errorDescription, owner);
        }
    }

    public static final class Context {
        private final Scope scope;
        private final Preferences settings;
        private final String singleWarningId;
        private final ProgressHandle progress;
        private final int bucketStart;
        private final int bucketSize;
        private final Collection<AnalysisProblem> problems = new ArrayList<AnalysisProblem>();
        private int totalWork;

        Context(Scope scope, Preferences settings, String singleWarningId, ProgressHandle progress, int bucketStart, int bucketSize) {
            this.scope = scope;
            this.settings = settings;
            this.singleWarningId = singleWarningId;
            this.progress = progress;
            this.bucketStart = bucketStart;
            this.bucketSize = bucketSize;
        }

        public Scope getScope() {
            return scope;
        }

        public Preferences getSettings() {
            return settings;
        }

        public String getSingleWarningId() {
            return singleWarningId;
        }

        public void start(int workunits) {
            totalWork = workunits;
        }

        public void progress(String message, int unit) {
            progress.progress(message, computeProgress(unit));
        }

        private int computeProgress(int unit) {
            return bucketStart + (int) (((double) unit / totalWork) * bucketSize);
        }

        public void progress(String message) {
            progress.progress(message);
        }

        public void progress(int workunit) {
            progress.progress(computeProgress(workunit));
        }

        public void finish() {
            progress.progress(bucketStart + bucketSize);
        }
        
        public void reportAnalysisProblem(String displayName, CharSequence description) {
            problems.add(new AnalysisProblem(displayName, description));
        }
        
        static {
            SPIAccessor.ACCESSOR = new SPIAccessor() {
                @Override
                public Context createContext(Scope scope, Preferences settings, String singleWarningId, ProgressHandle progress, int bucketStart, int bucketSize) {
                    return new Context(scope, settings, singleWarningId, progress, bucketStart, bucketSize);
                }

                @Override
                public Result createResult(List<ErrorDescription> errors, Map<ErrorDescription, Project> errorsToProjects, Collection<AnalysisProblem> analysisProblem) {
                    return new Result(errors, errorsToProjects, analysisProblem);
                }

                @Override
                public String getDisplayName(MissingPlugin missing) {
                    return missing.displayName;
                }

                @Override
                public String getCNB(MissingPlugin missing) {
                    return missing.cnb;
                }

                @Override
                public String getWarningDisplayName(WarningDescription description) {
                    return description.warningDisplayName;
                }

                @Override
                public String getWarningCategoryId(WarningDescription description) {
                    return description.categoryId;
                }

                @Override
                public String getWarningCategoryDisplayName(WarningDescription description) {
                    return description.categoryDisplayName;
                }

                @Override
                public String getWarningId(WarningDescription description) {
                    return description.warningId;
                }

                @Override
                public String getSelectedId(CustomizerContext<?, ?> cc) {
                    return cc.selectedId;
                }

                @Override
                public String getAnalyzerId(AnalyzerFactory selected) {
                    return selected.id;
                }

                @Override
                public String getAnalyzerDisplayName(AnalyzerFactory a) {
                    return a.displayName;
                }

                @Override
                public Image getAnalyzerIcon(AnalyzerFactory analyzer) {
                    if (analyzer.icon != null) return analyzer.icon;
                    else return ImageUtilities.loadImage(analyzer.iconPath);
                }

                @Override
                public Collection<? extends AnalysisProblem> getAnalysisProblems(Context context) {
                    return context.problems;
                }
            };
        }
    }

    public static final class MissingPlugin {
        private final String cnb;
        private final String displayName;
        public MissingPlugin(String cnb, String displayName) {
            this.cnb = cnb;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MissingPlugin other = (MissingPlugin) obj;
            if ((this.cnb == null) ? (other.cnb != null) : !this.cnb.equals(other.cnb)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.cnb != null ? this.cnb.hashCode() : 0);
            return hash;
        }
        
    }

    public static final class WarningDescription {

        public static WarningDescription create(String warningId, String warningDisplayName, String categoryId, String categoryDisplayName) {
            return new WarningDescription(warningId, warningDisplayName, categoryId, categoryDisplayName);
        }
        
        private final String warningId;
        private final String warningDisplayName;
        private final String categoryId;
        private final String categoryDisplayName;

        private WarningDescription(String warningId, String warningDisplayName, String categoryId, String categoryDisplayName) {
            this.warningId = warningId;
            this.warningDisplayName = warningDisplayName;
            this.categoryId = categoryId;
            this.categoryDisplayName = categoryDisplayName;
        }

    }

    public interface CustomizerProvider<D, C extends JComponent> {
        public D initialize();
        public C createComponent(CustomizerContext<D, C> context);
    }

    public static final class CustomizerContext<D, C extends JComponent> {
        private final Preferences preferences;
        private final String preselectId;
        private final C      previousComponent;
        private final D      data;
        private final ErrorListener errorListener;

        /*XXX*/ public CustomizerContext(Preferences preferences, String preselectId, C previousComponent, D data, ErrorListener errorListener) {
            this.preferences = preferences;
            this.preselectId = preselectId;
            this.previousComponent = previousComponent;
            this.data = data;
            this.errorListener = errorListener;
        }

        public Preferences getSettings() {
            return preferences;
        }

        public String getPreselectId() {
            return preselectId;
        }

        public C getPreviousComponent() {
            return previousComponent;
        }

        public D getData() {
            return data;
        }

        private String selectedId;

        public void setSelectedId(String id) {
            this.selectedId = id;
        }
        
        /**
         * @since 1.17
         */
        public void setError(@NullAllowed String error) {
            errorListener.setError(error);
        }

    }

}
