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

package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

/**
 * Support class for creating different types of queries implementations.
 * @author Tomas Mysik
 * @author Tomas Zezula
 */
public final class QuerySupport {

    private QuerySupport() {
    }

    /**
     * Create a new query to provide information about where Java sources
     * corresponding to binaries (classfiles) can be found.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining project properties.
     * @param srcRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return {@link SourceForBinaryQueryImplementation} to provide information about where Java sources can be found.
     * @see SourceForBinaryQueryImplementation
     */
    public static SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
        return createCompiledSourceForBinaryQuery(helper,
            evaluator, srcRoots, testRoots, new String[]{"build.classes.dir", "dist.jar"}, new String[]{"build.test.classes.dir"});
    }

    /**
     * Creates a {@link SourceForBinaryQueryImplementation} for multi-module project.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory
     * @param eval {@link PropertyEvaluator} used for obtaining project properties
     * @param sourceModules the module roots
     * @param srcRoots the source roots
     * @param testModules the test module roots
     * @param testRoots the test source roots
     * @return the {@link SourceForBinaryQueryImplementation}
     * @since 1.93
     */
    @NonNull
    public static SourceForBinaryQueryImplementation createMultiModuleSourceForBinaryQuery(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, srcRoots);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleSourceForBinaryQueryImpl(
                helper,
                eval,
                srcModel,
                testModel,
                new String[] {ProjectProperties.DIST_DIR, ProjectProperties.BUILD_MODULES_DIR},
                new String[] {ProjectProperties.BUILD_TEST_MODULES_DIR});
    }

    /**
     * Create a new query to provide information about where Java sources
     * corresponding to binaries (classfiles) can be found.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining project properties.
     * @param srcRoots a list of source roots.
     * @param testRoots a list of test roots, may be null if the project does not support tests.
     * @param binaryProperties array of property names of binary artifacts produced by this project, e.g. dist.jar
     * @param testBinaryProperties array of property names of test binary artifacts produced by this project, e.g. build.test.classes.dir
     * If the testRoots parameter is null this parameter has to be null as well.
     * @return {@link SourceForBinaryQueryImplementation} to provide information about where Java sources can be found.
     * @see SourceForBinaryQueryImplementation
     * @since org.netbeans.modules.java.api.common/0 1.3
     */
    public static SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(
            @NonNull AntProjectHelper helper,
            @NonNull PropertyEvaluator evaluator,
            @NonNull SourceRoots srcRoots,
            @NullAllowed SourceRoots testRoots,
            @NonNull String[] binaryProperties,
            @NullAllowed String[] testBinaryProperties) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("srcRoots", srcRoots); // NOI18N
        Parameters.notNull("binaryProperties", binaryProperties); // NOI18N
        final boolean validTestParams = testRoots == null ? testBinaryProperties == null : testBinaryProperties != null;
        if (!validTestParams) {
            throw new IllegalArgumentException("Both testRoots and testBinaryProperties have to be null or non null");  //NOI18N
        }
        return new CompiledSourceForBinaryQueryImpl(helper, evaluator, srcRoots, testRoots, binaryProperties, testBinaryProperties);
    }

    /**
     * Create a new query to provide information about encoding of a file. The returned query listens to the changes
     * in particular property values.
     * @param eval {@link PropertyEvaluator} used for obtaining the value of source encoding.
     * @param sourceEncodingPropertyName the source encoding property name.
     * @return a {@link FileEncodingQueryImplementation} to provide information about encoding of a file.
     */
    public static FileEncodingQueryImplementation createFileEncodingQuery(PropertyEvaluator eval,
            String sourceEncodingPropertyName) {
        Parameters.notNull("eval", eval); // NOI18N
        Parameters.notNull("sourceEncodingPropertyName", sourceEncodingPropertyName); // NOI18N

        return new FileEncodingQueryImpl(eval, sourceEncodingPropertyName);
    }

    /**
     * Create a new query to find Javadoc. The returned query listens on changes of the Javadoc directory.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining the Javadoc root.
     * @return a {@link JavadocForBinaryQueryImplementation} to find Javadoc.
     */
    public static JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator) {

        return createJavadocForBinaryQuery(helper, evaluator, new String[]{"build.classes.dir", "dist.jar"});
    }

    /**
     * Creates a {@link JavadocForBinaryQueryImplementation} for multi-module project.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory
     * @param eval {@link PropertyEvaluator} used for obtaining project properties
     * @param sourceModules the module roots
     * @param srcRoots the source roots
     * @return the {@link JavadocForBinaryQueryImplementation}
     * @since 1.94
     */
    @NonNull
    public static JavadocForBinaryQueryImplementation createMultiModuleJavadocForBinaryQuery(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots srcRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, srcRoots);
        return new MultiModuleJavadocForBinaryQueryImpl(
                helper,
                eval,
                srcModel,
                new String[] {ProjectProperties.DIST_DIR, ProjectProperties.BUILD_MODULES_DIR},
                ProjectProperties.DIST_JAVADOC_DIR);
    }

    /**
     * Create a new query to find Javadoc. The returned query listens on changes of the Javadoc directory.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining the Javadoc root.
     * @param binaryProperties array of property names of binary artifacts produced by this project, e.g. dist.jar
     * @return a {@link JavadocForBinaryQueryImplementation} to find Javadoc.
     * @since org.netbeans.modules.java.api.common/0 1.3
     */
    public static JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, String[] binaryProperties) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("binaryProperties", binaryProperties); // NOI18N

        return new JavadocForBinaryQueryImpl(helper, evaluator, binaryProperties);
    }

    /**
     * Create a new query to provide information about files sharability. The returned query listens to the changes
     * in particular source roots.
     * @param helper {@link AntProjectHelper} used for creating a query itself.
     * @param evaluator a {@link PropertyEvaluator property evaluator} to interpret paths with.
     * @param srcRoots a list of source roots to treat as sharable.
     * @param testRoots a list of test roots to treat as sharable, may be null if the project does not support tests
     * @param additionalSourceRoots additional paths to treat as sharable (just pure property names, do not
     *          use <i>${</i> and <i>}</i> characters). Can be <code>null</code>.
     * @return a query to provide information about files sharability.
     * @since 1.35
     */
    public static SharabilityQueryImplementation2 createSharabilityQuery2(
            final @NonNull AntProjectHelper helper,
            final @NonNull PropertyEvaluator evaluator,
            final @NonNull SourceRoots srcRoots,
            final @NullAllowed SourceRoots testRoots,
            final @NullAllowed String... additionalSourceRoots) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("srcRoots", srcRoots); // NOI18N

        return new SharabilityQueryImpl(helper, evaluator, srcRoots, testRoots, additionalSourceRoots);
    }
    /**
     * @deprecated since 1.35 use {@link #createSharabilityQuery2} instead
     */
    @Deprecated
    public static SharabilityQueryImplementation createSharabilityQuery(
            final @NonNull AntProjectHelper helper,
            final @NonNull PropertyEvaluator evaluator,
            final @NonNull SourceRoots srcRoots,
            final @NullAllowed SourceRoots testRoots,
            final @NullAllowed String... additionalSourceRoots) {
        final SharabilityQueryImplementation2 sq2 = createSharabilityQuery2(helper, evaluator, srcRoots, testRoots, additionalSourceRoots);
        return new SharabilityQueryImplementation() {
            @Override public int getSharability(File file) {
                return sq2.getSharability(Utilities.toURI(file)).ordinal();
            }
        };
    }
    /**
     * @deprecated since 1.35 use {@link #createSharabilityQuery2} instead
     */
    @Deprecated
    public static SharabilityQueryImplementation createSharabilityQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {

        return createSharabilityQuery(helper, evaluator, srcRoots, testRoots, (String[]) null);
    }

    /**
     * Create a new query to find out specification source level of Java source files.
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @return a {@link SourceLevelQueryImplementation} to find out specification source level of Java source files.
     * @deprecated Use {@link QuerySupport#createSourceLevelQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator)}
     */
    @Deprecated
    public static org.netbeans.spi.java.queries.SourceLevelQueryImplementation createSourceLevelQuery(PropertyEvaluator evaluator) {
        Parameters.notNull("evaluator", evaluator); // NOI18N

        return new SourceLevelQueryImpl(evaluator);
    }

    /**
     * Create a new query to find out source level of Java source files (SourceLevelQueryImplementation2).
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @return a {@link SourceLevelQueryImplementation2} to find out source level of Java source files.
     * @since 1.22
     */
    public static SourceLevelQueryImplementation2 createSourceLevelQuery2(@NonNull PropertyEvaluator evaluator) {        
        return createSourceLevelQuery2(
            evaluator,
            CommonProjectUtils.J2SE_PLATFORM_TYPE);
    }

    /**
     * Create a new query to find out source level of Java source files (SourceLevelQueryImplementation2).
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @param platformType the type of platform, for example "j2se"
     * @return a {@link SourceLevelQueryImplementation2} to find out source level of Java source files.
     * @since 1.72
     */
    public static SourceLevelQueryImplementation2 createSourceLevelQuery2(
        @NonNull PropertyEvaluator evaluator,
        @NonNull String platformType) {
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("platformType", platformType);   //NOI18N
        return new SourceLevelQueryImpl2(evaluator, platformType);
    }

    /**
     * Create a new query to find out explicit compiler options for Java source files.
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties
     * @param additionalCompilerOptionsProperty the property holding the additional compiler options
     * @return a {@link CompilerOptionsQueryImplementation} to find out explicit compiler options of Java source files.
     * @since 1.81
     */
    @NonNull
    public static CompilerOptionsQueryImplementation createCompilerOptionsQuery(
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String additionalCompilerOptionsProperty) {
        return new CompilerOptionsQueryImpl(evaluator, additionalCompilerOptionsProperty);
    }

    /**
     * Create a new query to set up explicit compiler options needed for unit test compilation.
     * @param eval the {@link PropertyEvaluator}
     * @param srcRoots the source roots
     * @param testRoots the test roots
     * @return a {@link CompilerOptionsQueryImplementation} to find out unit test compiler options
     * @since 1.82
     */
    public static CompilerOptionsQueryImplementation createUnitTestsCompilerOptionsQuery(
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testRoots) {
        return new UnitTestsCompilerOptionsQueryImpl(eval, srcRoots, testRoots);
    }

    /**
     * Creates an Automatic-Module-Name query.
     * @param helper the {@link AntProjectHelper}
     * @param eval the {@link PropertyEvaluator}
     * @param srcRoots the source roots
     * @param manifestProperty  the property holding the path to the manifest file
     * @return a {@link CompilerOptionsQueryImplementation} to find out the Automatic-Module-Name
     * @since 1.122
     */
    @NonNull
    public static CompilerOptionsQueryImplementation createAutomaticModuleNameQuery(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots srcRoots,
            @NonNull final String manifestProperty) {
        return new AutomaticModuleNameCompilerOptionsQueryImpl(helper, eval, srcRoots, manifestProperty);
    }

    /**
     * Create a new query to set up explicit compiler options needed for unit test compilation in a multi module project.
     * @param project the project to create a query for
     * @param sourceModules the module roots
     * @param sourceRoots the source roots
     * @param testModules the test module roots
     * @param testRoots the test source roots
     * @return a {@link CompilerOptionsQueryImplementation} to find out unit test compiler options
     * @since 1.107
     */
    @NonNull
    public static CompilerOptionsQueryImplementation createMultiModuleUnitTestsCompilerOptionsQuery(
            @NonNull final Project project,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, sourceRoots);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleUnitTestsCompilerOptionsQueryImpl(project, srcModel, testModel);
    }

    /**
     * Create a new query to find Java package roots of unit tests for Java package root of sources and vice versa.
     * @param sourceRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return a {@link MultipleRootsUnitTestForSourceQueryImplementation} to find Java package roots of unit tests
     *         for Java package root of sources and vice versa.
     */
    public static MultipleRootsUnitTestForSourceQueryImplementation createUnitTestForSourceQuery(
            SourceRoots sourceRoots, SourceRoots testRoots) {
        Parameters.notNull("sourceRoots", sourceRoots); // NOI18N
        Parameters.notNull("testRoots", testRoots); // NOI18N

        return new UnitTestForSourceQueryImpl(sourceRoots, testRoots);
    }

    /**
     * Creates a {@link MultipleRootsUnitTestForSourceQueryImplementation} for a multi-module project.
     * @param sourceModules the module roots
     * @param sourceRoots the source roots
     * @param testModules the test module roots
     * @param testRoots the test source roots
     * @return the newly created {@link MultipleRootsUnitTestForSourceQueryImplementation}
     * @since 1.104
    */
    @NonNull
    public static MultipleRootsUnitTestForSourceQueryImplementation createMultiModuleUnitTestForSourceQuery(
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, sourceRoots);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleUnitTestForSourceQueryImpl(srcModel, testModel);
    }

    /**
     * Create a new query to test whether a file can be considered to be built (up to date). The returned query
     * listens to the changes in particular source roots.
     * @param helper {@link AntProjectHelper} used for creating a query itself.
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @param sourceRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return a {@link FileBuiltQueryImplementation} to test whether a file can be considered to be built (up to date).
     */
    public static FileBuiltQueryImplementation createFileBuiltQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("sourceRoots", sourceRoots); // NOI18N
        Parameters.notNull("testRoots", testRoots); // NOI18N

        return new FileBuiltQueryImpl(helper, evaluator, sourceRoots, testRoots);
    }

    /**
     * Creates a {@link FileBuiltQueryImplementation} for a multi-module project.
     * @param helper the {@link AntProjectHelper}
     * @param evaluator the {@link PropertyEvaluator}
     * @param sourceModules the module roots
     * @param sourceRoots the source roots
     * @param testModules the test module roots
     * @param testRoots the test source roots
     * @return the {@link FileBuiltQueryImplementation} instance
     * @since 1.103
     */
    @NonNull
    public static FileBuiltQueryImplementation createMultiModuleFileBuiltQuery(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, sourceRoots);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleFileBuiltQueryImpl(helper, evaluator, srcModel, testModel);
    }

    /**
     * Creates an implementation of {@link CreateFromTemplateAttributesProvider} providing
     * attributes for the project license and encoding.
     *
     * @param helper {@link AntProjectHelper} used for reading the project properties.
     * @param encodingQuery {@link FileEncodingQueryImplementation} used to obtain an encoding.
     * @return a {@code CreateFromTemplateAttributesProvider}.
     *
     * @since 1.1
     */
    public static CreateFromTemplateAttributesProvider createTemplateAttributesProvider(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        Parameters.notNull("helper", helper);
        Parameters.notNull("encodingQuery", encodingQuery);
        return new TemplateAttributesProviderImpl(helper, encodingQuery);
    }
    
    /**
     * Creates an implementation of {@link BinaryForSourceQueryImplementation} 
     * which maps given project source roots and test roots to given folders
     * with built classes and built test classes.
     *
     * @param src project source roots
     * @param test project test roots
     * @param helper AntProjectHelper
     * @param eval PropertyEvaluator
     * @param sourceProps name of properties pointing to binaries
     * @param testProps name of properties pointing to test binaries
     * @return BinaryForSourceQueryImplementation
     * @since 1.79
     */
    public static BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(
            SourceRoots src, SourceRoots test, AntProjectHelper helper, 
            PropertyEvaluator eval, String[] sourceProps, String[] testProps) {
        return new BinaryForSourceQueryImpl(src, test, helper, eval, 
                sourceProps, testProps);
    }
    
    /**
     * Shortcut version of {@link #createBinaryForSourceQueryImplementation(org.netbeans.modules.java.api.common.SourceRoots, org.netbeans.modules.java.api.common.SourceRoots, org.netbeans.spi.project.support.ant.AntProjectHelper, org.netbeans.spi.project.support.ant.PropertyEvaluator, java.lang.String, java.lang.String) }
     * which assumes that build classes folder is stored in property <code>build.classes.dir</code> and
     * built test classes folder is stored in property <code>build.test.classes.dir</code>.
     *
     * @param src project source roots
     * @param test project test roots
     * @param helper AntProjectHelper
     * @param eval PropertyEvaluator
     * @param sourceProps array of properties pointing to source folders
     * @param testProps array of properties pointing to test folders
     * @return BinaryForSourceQueryImplementation
     * @since org.netbeans.modules.java.api.common/1 1.5
     */
    public static BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(SourceRoots src, SourceRoots test, 
            AntProjectHelper helper, PropertyEvaluator eval) {
        return createBinaryForSourceQueryImplementation(src, test, helper, eval, 
                new String[] {ProjectProperties.BUILD_CLASSES_DIR, ProjectProperties.DIST_JAR},
                new String[] {ProjectProperties.BUILD_TEST_CLASSES_DIR});
    }


    /**
     * Creates a {@link BinaryForSourceQueryImplementation} for multi-module project.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory
     * @param eval {@link PropertyEvaluator} used for obtaining project properties
     * @param sourceModules the module roots
     * @param srcRoots the source roots
     * @param testModules the test module roots
     * @param testRoots the test source roots
     * @return the {@link BinaryForSourceQueryImplementation}
     * @since 1.93
     */
    @NonNull
    public static BinaryForSourceQueryImplementation createMultiModuleBinaryForSourceQuery(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots srcRoots,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots testRoots) {
        final MultiModule srcModel = MultiModule.getOrCreate(sourceModules, srcRoots);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testRoots);
        return new MultiModuleBinaryForSourceQueryImpl(
                helper,
                eval,
                srcModel,
                testModel,
                new String[] {
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_MODULES_DIR),   //NOI18N
                    String.format("${%s}/${module.name}.jar",ProjectProperties.DIST_DIR)       //NOI18N
                },
                new String[] {
                    String.format("${%s}/${module.name}",ProjectProperties.BUILD_TEST_MODULES_DIR),   //NOI18N
                });
    }

    /**Create a new query to provide annotation processing configuration data.
     * 
     * @param helper project's AntProjectHelper
     * @param evaluator project's evaluator
     * @param annotationProcessingEnabledProperty property whose value says whether the annotation processing is enabled for the given project at all
     *                                                    (will be returned from {@link Result#annotationProcessingEnabled()})
     * @param annotationProcessingEnabledInEditorProperty property whose value says whether the annotation processing should be enabled
     *                                                    in the editor (will be returned from {@link Result#annotationProcessingEnabled())}
     * @param runAllAnnotationProcessorsProperty when true, {@link Result#annotationProcessorsToRun()} will return null
     * @param annotationProcessorsProperty should contain comma separated list of annotation processors to run (will be returned from  {@link Result#annotationProcessorsToRun()})
     * @param sourceOutputProperty directory to which the annotation processors generate source files (will be returned from  {@link Result#sourceOutputProperty()})
     * @param processorOptionsProperty options passed to the annotation processors (-Akey=value)
     * @return a {@link AnnotationProcessingQueryImplementation} to provide annotation processing configuration data for this project.
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static AnnotationProcessingQueryImplementation createAnnotationProcessingQuery(AntProjectHelper helper, PropertyEvaluator evaluator,
            String annotationProcessingEnabledProperty, String annotationProcessingEnabledInEditorProperty, String runAllAnnotationProcessorsProperty, String annotationProcessorsProperty, String sourceOutputProperty, String processorOptionsProperty) {
        return new AnnotationProcessingQueryImpl(helper, evaluator, annotationProcessingEnabledProperty, annotationProcessingEnabledInEditorProperty, runAllAnnotationProcessorsProperty, annotationProcessorsProperty, sourceOutputProperty, processorOptionsProperty);
    }

    public static ProjectInformation createProjectInformation(AntProjectHelper projectHelper, Project project, Icon icon) {
        return new QuerySupport.AntHelper(projectHelper, project, icon, ProjectInfoImpl.DEFAULT_ELEMENT_NAME);
    }

    public static ProjectInformation createProjectInformation(UpdateHelper updateHelper, Project project, Icon icon) {
        return new QuerySupport.AntUpdateHelper(updateHelper, project, icon, ProjectInfoImpl.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Returns {@link Sources} implementation designed for projects that supports adding
     * or removing of the source roots. The returned instance also implements {@link SourceGroupModifierImplementation}
     * @param project the {@link Project} for which the {@link Sources} should be created
     * @param helper the {@link AntProjectHelper} of the project, used only to resolve files
     * @param evaluator the {@link PropertyEvaluator} to evaluate the properties
     * @param roots the array of {@link Roots} providing the roots of given type
     * @return the {@link Sources} instance implementing also the {@link SourceGroupModifierImplementation} interface
     * @since 1.21
     */
    public static Sources createSources(@NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Roots... roots) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("roots", roots); //NOI18N
        return new SourcesImpl(project, helper, evaluator, roots);
    }
    
    /**
     * Returns {@link MultiModuleGroupQuery} implementation suitable for modular Java projects.
     * @param helper the {@link AntProjectHelper} of the project, used only to resolve files
     * @param evaluator the {@link PropertyEvaluator} to evaluate the properties
     * @param roots the array of {@link Roots} providing the roots 
     * @param src Sources objects to track changes to project definition
     * @return multi-module implementation of MultiModuleGroupQuery
     * @since 1.97
     */
    public static MultiModuleGroupQuery createMultiModuleGroupQuery(AntProjectHelper helper, PropertyEvaluator evaluator, Sources src, Roots... roots) {
        return new MultiModuleGroupQueryImpl(helper, evaluator, src, roots);
    }

    /**
     * Creates a {@link AccessibilityQueryImplementation2} based on the module-info.
     * @param sources the project source roots
     * @param tests the project test roots
     * @return the {@link AccessibilityQueryImplementation2} instance
     * @since 1.85
     */
    @NonNull
    public static AccessibilityQueryImplementation2 createModuleInfoAccessibilityQuery(
            @NonNull final SourceRoots sources,
            @NonNull final SourceRoots tests) {
        return new ModuleInfoAccessibilityQueryImpl(null, sources, null, tests);
    }

    /**
     * Creates a {@link AccessibilityQueryImplementation2} based on the module-info for multi module projects.
     * @param sourceModules the module roots
     * @param sources the source roots
     * @param testModules the test module roots
     * @param tests the test roots
     * @return the {@link AccessibilityQueryImplementation2} instance
     * @since 1.97
     */
    @NonNull
    public static AccessibilityQueryImplementation2 createModuleInfoAccessibilityQuery(
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots sources,
            @NonNull final SourceRoots testModules,
            @NonNull final SourceRoots tests) {
        Parameters.notNull("sourceModules", sourceModules);     //NOI18N
        Parameters.notNull("testModules", testModules);         //NOI18N
        return new ModuleInfoAccessibilityQueryImpl(
                sourceModules, sources, testModules, tests);
    }

    /**
     * Creates a {@link AntArtifactProvider} for multi-module project.
     * @param helper the {@link AntProjectHelper}
     * @param eval the {@link PropertyEvaluator}
     * @param sourceModules the module roots
     * @param sources the source roots
     * @return the {@link AntArtifactProvider} instance
     * @since 1.98
     */
    @NonNull
    public static AntArtifactProvider createMultiModuleAntArtifactProvider(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final SourceRoots sourceModules,
            @NonNull final SourceRoots sources) {
        return new MultiModuleAntArtifactProvider(
                helper,
                eval,
                MultiModule.getOrCreate(sourceModules, sources),
                "jar",      //NOI18N
                "clean");   //NOI18N
    }

    private static class AntHelper extends ProjectInfoImpl {

        private final AntProjectHelper projectHelper;

        @SuppressWarnings("LeakingThisInConstructor")
        public AntHelper(AntProjectHelper projectHelper, Project project, Icon icon, String elementName) {
            super(project, icon, elementName);
            this.projectHelper = projectHelper;

            projectHelper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, projectHelper));
        }


        @Override
        protected Element getPrimaryConfigurationData() {
            return projectHelper.getPrimaryConfigurationData(true);
        }
    }

    private static class AntUpdateHelper extends ProjectInfoImpl {

        private final UpdateHelper updateHelper;

        @SuppressWarnings("LeakingThisInConstructor")
        public AntUpdateHelper(UpdateHelper updateHelper, Project project, Icon icon, String elementName) {
            super(project, icon, elementName);
            this.updateHelper = updateHelper;

            AntProjectHelper projectHelper = updateHelper.getAntProjectHelper();
            projectHelper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, projectHelper));
        }

        @Override
        protected Element getPrimaryConfigurationData() {
            return updateHelper.getPrimaryConfigurationData(true);
        }
    }
}
