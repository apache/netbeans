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

package org.netbeans.api.extexecution;

import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.spi.extexecution.open.OptionOpenHandler;
import org.openide.windows.InputOutput;

/**
 * Descriptor for the execution service. Describes the runtime attributes
 * of the {@link ExecutionService}.
 * <p>
 * <i>Thread safety</i> of this class depends on type of objects passed to its
 * configuration methods. If these objects are immutable, resulting descriptor
 * is immutable as well. It these objects are thread safe, resulting descriptor
 * is thread safe as well.
 *
 * @author Petr Hejl
 * @see ExecutionService
 */
public final class ExecutionDescriptor {

    private static final Logger LOGGER = Logger.getLogger(ExecutionDescriptor.class.getName());

    // TODO provide constants for common descriptors (are there any?)

    private final Runnable preExecution;

    private final Consumer<? super Integer> postExecution;

    private final boolean suspend;

    private final boolean progress;

    private final boolean front;

    private final boolean input;

    private final boolean controllable;

    private final boolean noReset;

    private final boolean outLineBased;

    private final boolean errLineBased;

    private final boolean frontWindowOnError;

    private final LineConvertorFactory outConvertorFactory;

    private final LineConvertorFactory errConvertorFactory;

    private final InputProcessorFactory outProcessorFactory;
    
    private final InputProcessorFactory2 outProcessorFactory2;

    private final InputProcessorFactory errProcessorFactory;
    
    private final InputProcessorFactory2 errProcessorFactory2;

    private final InputOutput inputOutput;

    private final RerunCondition rerunCondition;

    private final RerunCallback rerunCallback;

    private final String optionsPath;

    private final Charset charset;

    /**
     * Creates the new descriptor. All properties are initalized to
     * <code>null</code> or <code>false</code>.
     */
    public ExecutionDescriptor() {
        this(new DescriptorData());
    }

    private ExecutionDescriptor(DescriptorData data) {
        this.preExecution = data.preExecution;
        this.postExecution = data.postExecution;
        this.suspend = data.suspend;
        this.progress = data.progress;
        this.front = data.front;
        this.input = data.input;
        this.controllable = data.controllable;
        this.outLineBased = data.outLineBased;
        this.errLineBased = data.errLineBased;
        this.frontWindowOnError = data.frontWindowOnError;
        this.outConvertorFactory = data.outConvertorFactory;
        this.errConvertorFactory = data.errConvertorFactory;
        this.outProcessorFactory = data.outProcessorFactory;
        this.outProcessorFactory2 = data.outProcessorFactory2;
        this.errProcessorFactory = data.errProcessorFactory;
        this.errProcessorFactory2 = data.errProcessorFactory2;
        this.inputOutput = data.inputOutput;
        this.rerunCondition = data.rerunCondition;
        this.rerunCallback = data.rerunCallback;
        this.optionsPath = data.optionsPath;
        this.charset = data.charset;
        this.noReset = data.noReset;
    }

    /**
     * Returns a descriptor with configured <i>custom</i> io. When configured
     * to <code>null</code> it means that client is fine with infrustructure
     * provided io (visible as tab in output pane).
     * <p>
     * If configured value is not <code>null</code> values configured via
     * methods {@link #controllable(boolean)}, {@link #rerunCondition(RerunCondition)}
     * and {@code #getOptionsPath()} are ignored by {@link ExecutionService}.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param io custom input output, <code>null</code> allowed
     * @return new descriptor with configured custom io
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor inputOutput(@NullAllowed InputOutput io) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.inputOutput(io));
    }

    InputOutput getInputOutput() {
        return inputOutput;
    }

    /**
     * Returns a descriptor with configured controllable flag. When
     * <code>true</code> the control buttons (rerun, stop) will be available
     * io tab created by {@link ExecutionService}.
     * <p>
     * Note that this property has no meaning when custom io is used
     * (see {@link #inputOutput(org.openide.windows.InputOutput)}).
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param controllable controllable flag
     * @return new descriptor with configured controllable flag
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor controllable(boolean controllable) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.controllable(controllable));
    }

    boolean isControllable() {
        return controllable;
    }

    /**
     * Returns a descriptor with configured front window flag. When
     * <code>true</code> the io tab will be selected before the execution
     * invoked by {@link ExecutionService#run()}.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param frontWindow front window flag
     * @return new descriptor with configured front window flag
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor frontWindow(boolean frontWindow) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.frontWindow(frontWindow));
    }

    boolean isFrontWindow() {
        return front;
    }

    /**
     * Returns a descriptor with configured input visible flag. When configured
     * value is <code>true</code> the input from user will be allowed.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param inputVisible input visible flag
     * @return new descriptor with configured input visible flag
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor inputVisible(boolean inputVisible) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.inputVisible(inputVisible));
    }

    boolean isInputVisible() {
        return input;
    }

    /**
     * Returns a descriptor with configured show progress flag. When configured
     * value is <code>true</code> the progress bar will be visible.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param showProgress show progress flag
     * @return new descriptor with configured show progress flag
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor showProgress(boolean showProgress) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.showProgress(showProgress));
    }

    boolean showProgress() {
        return progress;
    }

    /**
     * Returns a descriptor with configured show suspend flag. When configured
     * value is <code>true</code> the progress bar will be suspended to just
     * "running" message.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param showSuspended show suspended flag
     * @return new descriptor with configured show suspended flag
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor showSuspended(boolean showSuspended) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.showSuspended(showSuspended));
    }

    boolean showSuspended() {
        return suspend;
    }

    /**
     * Returns a descriptor with configured no reset flag. When configured
     * value is <code>true</code> the output window won't be cleared before
     * the execution. <i>Valid only for custom {@link InputOutput} configured via
     * {@link #inputOutput(org.openide.windows.InputOutput)}, ignored in all
     * other cases.</i>
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param noReset no reset flag
     * @return new descriptor with configured no reset flag
     * @since 1.20
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor noReset(boolean noReset) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.noReset(noReset));
    }

    boolean noReset() {
        return noReset;
    }

    /**
     * Returns a descriptor with configured flag indicating line based standard
     * output. When configured value is <code>true</code> the default printing
     * processor will always <i>wait for the whole line before converting and
     * printing it</i>.
     *
     * @param outLineBased line based flag
     * @return descriptor with configured flag indicating line based
     *             standard output
     * @see #outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor outLineBased(boolean outLineBased) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outLineBased(outLineBased));
    }

    boolean isOutLineBased() {
        return outLineBased;
    }

    /**
     * Returns a descriptor with configured flag indicating line based standard
     * error output. When configured value is <code>true</code> the default
     * printing processor will always <i>wait for the whole line before
     * converting and printing it</i>.
     *
     * @param errLineBased line based flag
     * @return descriptor with configured flag indicating line based
     *             standard error output
     * @see #errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor errLineBased(boolean errLineBased) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errLineBased(errLineBased));
    }

    boolean isErrLineBased() {
        return errLineBased;
    }

    /**
     * Returns a descriptor with configured front window on error flag. When
     * configured value is <code>true</code> and the process will return nonzero
     * exit value the output window will be moved to front on execution finish.
     * <p>
     * The default (not configured) value is <code>false</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param frontWindowOnError front window on error flag
     * @return new descriptor with configured front window on error flag
     * @since 1.29
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor frontWindowOnError(boolean frontWindowOnError) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.frontWindowOnError(frontWindowOnError));
    }

    boolean isFrontWindowOnError() {
        return frontWindowOnError;
    }

    /**
     * Returns a descriptor with configured factory for standard output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard output.
     * <p>
     * Note that {@link ExecutionService} automatically uses
     * the printing processor created by
     * {@link org.netbeans.api.extexecution.print.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * or
     * {@link org.netbeans.api.extexecution.print.LineProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * (in case {@link #outLineBased(boolean)} is configured to <code>true</code>)
     * if there is no configured factory.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param outProcessorFactory factory for standard output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard output
     * @deprecated use {@link #outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory2)}
     */
    @Deprecated
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor outProcessorFactory(@NullAllowed InputProcessorFactory outProcessorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outProcessorFactory(outProcessorFactory));
    }

    InputProcessorFactory getOutProcessorFactory() {
        return outProcessorFactory;
    }
    
    /**
     * Returns a descriptor with configured factory for standard output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard output. <i>The configured value will
     * be ignored if you previously configured processor via deprecated
     * {@link #outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)}.</i>
     * <p>
     * Note that {@link ExecutionService} automatically uses
     * the printing processor created by
     * {@link org.netbeans.api.extexecution.print.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * or
     * {@link org.netbeans.api.extexecution.print.LineProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * (in case {@link #outLineBased(boolean)} is configured to <code>true</code>)
     * if there is no configured factory.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param outProcessorFactory factory for standard output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard output
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor outProcessorFactory(@NullAllowed InputProcessorFactory2 outProcessorFactory) {
        if (this.outProcessorFactory != null) {
            LOGGER.log(Level.WARNING, "The factory will be ignored as legacy InputProcessorFactory is already defined");
        }
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outProcessorFactory(outProcessorFactory));
    }

    InputProcessorFactory2 getOutProcessorFactory2() {
        return outProcessorFactory2;
    }

    /**
     * Returns a descriptor with configured factory for standard error output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard error output.
     * <p>
     * Note that {@link ExecutionService} automatically uses
     * the printing processor created by
     * {@link org.netbeans.api.extexecution.print.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * or
     * {@link org.netbeans.api.extexecution.print.LineProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * (in case {@link #errLineBased(boolean)} is configured to <code>true</code>)
     * if there is no configured factory.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param errProcessorFactory factory for standard error output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard error output
     * @deprecated use {@link #errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory2)}
     */
    @Deprecated
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor errProcessorFactory(@NullAllowed InputProcessorFactory errProcessorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errProcessorFactory(errProcessorFactory));
    }

    InputProcessorFactory getErrProcessorFactory() {
        return errProcessorFactory;
    }
    
    /**
     * Returns a descriptor with configured factory for standard error output
     * processor. The factory is used by {@link ExecutionService} to create
     * additional processor for standard error output. <i>The configured value will
     * be ignored if you previously configured processor via deprecated
     * {@link #errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)}.</i>
     * <p>
     * Note that {@link ExecutionService} automatically uses
     * the printing processor created by
     * {@link org.netbeans.api.extexecution.print.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * or
     * {@link org.netbeans.api.extexecution.print.LineProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)}
     * (in case {@link #errLineBased(boolean)} is configured to <code>true</code>)
     * if there is no configured factory.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param errProcessorFactory factory for standard error output processor,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for additional
     *             processor to use for standard error output
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor errProcessorFactory(@NullAllowed InputProcessorFactory2 errProcessorFactory) {
        if (this.errProcessorFactory != null) {
            LOGGER.log(Level.WARNING, "The factory will be ignored as legacy InputProcessorFactory is already defined");
        }
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errProcessorFactory(errProcessorFactory));
    }

    InputProcessorFactory2 getErrProcessorFactory2() {
        return errProcessorFactory2;
    }

    /**
     * Returns a descriptor with configured factory for convertor for standard
     * output. The factory is used by {@link ExecutionService} to create
     * convertor to use with processor printing the standard output.
     * <p>
     * Note that {@link ExecutionService} always uses the printing processor
     * for the standard output. Convertor created by the returned factory will
     * be passed to this default printing processor. See
     * {@link #outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)} too.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param convertorFactory factory for convertor for standard output,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for converter for
     *             standard output
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor outConvertorFactory(@NullAllowed LineConvertorFactory convertorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.outConvertorFactory(convertorFactory));
    }

    LineConvertorFactory getOutConvertorFactory() {
        return outConvertorFactory;
    }

    /**
     * Returns a descriptor with configured factory for convertor for standard
     * error output. The factory is used by {@link ExecutionService} to create
     * convertor to use with processor printing the standard error output.
     * <p>
     * Note that {@link ExecutionService} always uses the printing processor
     * for the standard error output. Convertor created by the returned
     * factory will be passed to this default printing processor. See
     * {@link #errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory)} too.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param convertorFactory factory for convertor for standard error output,
     *             <code>null</code> allowed
     * @return new descriptor with configured factory for converter for
     *             standard error output
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor errConvertorFactory(@NullAllowed LineConvertorFactory convertorFactory) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.errConvertorFactory(convertorFactory));
    }

    LineConvertorFactory getErrConvertorFactory() {
        return errConvertorFactory;
    }

    /**
     * Returns a descriptor with configured pre execution runnable. This
     * runnable is executed <i>before</i> the external execution itself
     * (when invoked by {@link ExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param preExecution pre execution runnable, <code>null</code> allowed
     * @return new descriptor with configured pre execution runnable
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor preExecution(@NullAllowed Runnable preExecution) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.preExecution(preExecution));
    }

    Runnable getPreExecution() {
        return preExecution;
    }

    /**
     * Returns a descriptor with configured post execution runnable. This
     * runnable is executed <i>after</i> the external execution itself
     * (when invoked by {@link ExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param postExecution post execution runnable, <code>null</code> allowed
     * @return new descriptor with configured post execution runnable
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor postExecution(@NullAllowed Runnable postExecution) {
        return postExecution((__) -> {
            postExecution.run();
        });
    }

    /**
     * Returns a descriptor with configured post execution runnable. This
     * runnable is executed <i>after</i> the external execution itself
     * (when invoked by {@link ExecutionService#run()}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param postExecution post execution callback that receives exit code of the
     *    execution, <code>null</code> allowed
     * @return new descriptor with configured post execution callback
     * @since 1.61
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor postExecution(@NullAllowed Consumer<Integer> postExecution) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.postExecution(postExecution));
    }

    Consumer<? super Integer> getPostExecution() {
        return postExecution;
    }

    /**
     * Returns a descriptor with configured rerun condition. The condition
     * is used by {@link ExecutionService} to control the possibility of the
     * rerun action.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param rerunCondition rerun condition, <code>null</code> allowed
     * @return new descriptor with configured rerun condition
     * @see #rerunCallback(org.netbeans.api.extexecution.ExecutionDescriptor.RerunCallback)
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor rerunCondition(@NullAllowed ExecutionDescriptor.RerunCondition rerunCondition) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.rerunCondition(rerunCondition));
    }

    RerunCondition getRerunCondition() {
        return rerunCondition;
    }

    /**
     * Returns a descriptor with configured rerun callback. The callback
     * is invoked when the execution is triggered by the rerun action.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param rerunCallback rerun callback, <code>null</code> allowed
     * @return new descriptor with configured rerun callback
     * @see #rerunCondition(org.netbeans.api.extexecution.ExecutionDescriptor.RerunCondition) 
     * @since 1.46
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor rerunCallback(@NullAllowed ExecutionDescriptor.RerunCallback rerunCallback) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.rerunCallback(rerunCallback));
    }

    RerunCallback getRerunCallback() {
        return rerunCallback;
    }

    /**
     * Returns a descriptor with configured options path. If configured
     * value is not <code>null</code> the {@link ExecutionService} will
     * display the button in the output tab displaying the proper options
     * when pressed. <i>For this to work there has to be
     * a {@link OptionOpenHandler} in the system. Otherwise the options button
     * won't be displayed.</i>
     * <p>
     * Note that this property has no meaning when custom io is used
     * (see {@link #inputOutput(org.openide.windows.InputOutput)}).
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param optionsPath options path, <code>null</code> allowed
     * @return this descriptor with configured options path
     * @see OptionOpenHandler
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor optionsPath(@NullAllowed String optionsPath) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.optionsPath(optionsPath));
    }

    String getOptionsPath() {
        return optionsPath;
    }

    /**
     * Returns a descriptor with configured charset. If configured
     * value is not <code>null</code> the {@link ExecutionService} will
     * use the given charset to decode the process streams. When
     * <code>null</code> the platform default will be used.
     * <p>
     * Note that in the most common scenario of execution of OS native
     * process you shouldn't need to set the charset. The platform default
     * (which is the default used) is just the right choice.
     * <p>
     * The default (not configured) value is <code>null</code>.
     * <p>
     * All other properties of the returned descriptor are inherited from
     * <code>this</code>.
     *
     * @param charset charset, <code>null</code> allowed
     * @return this descriptor with configured charset
     */
    @NonNull
    @CheckReturnValue
    public ExecutionDescriptor charset(@NullAllowed Charset charset) {
        DescriptorData data = new DescriptorData(this);
        return new ExecutionDescriptor(data.charset(charset));
    }

    Charset getCharset() {
        return charset;
    }

    /**
     * Represents the possibility of reruning the action.
     */
    public interface RerunCondition {

        /**
         * Adds a listener to listen for the change in rerun possibility state.
         *
         * @param listener listener that will listen for changes in rerun possibility
         */
        void addChangeListener(@NonNull ChangeListener listener);

        /**
         * Removes previously registered listener.
         *
         * @param listener listener to remove
         */
        void removeChangeListener(@NonNull ChangeListener listener);

        /**
         * Returns <code>true</code> if it is possible to execute the action again.
         *
         * @return <code>true</code> if it is possible to execute the action again
         */
        boolean isRerunPossible();

    }

    /**
     * Provides a callback to be invoked when rerun action is invoked.
     *
     * @since 1.46
     */
    public interface RerunCallback {

        /**
         * Called when rerun action is invoked.
         *
         * @param task the task created by the rerun action
         */
        void performed(Future<Integer> task);

    }

    /**
     * Factory creating the input processor.
     * @deprecated use {@link InputProcessorFactory2}
     */
    @Deprecated
    public interface InputProcessorFactory {

        /**
         * Creates and returns new input processor.
         *
         * @param defaultProcessor default processor created by
         *             infrastructure that is printing chars to the output window
         * @return new input processor
         */
        @NonNull
        InputProcessor newInputProcessor(@NonNull InputProcessor defaultProcessor);

    }
    
    /**
     * Factory creating the input processor.
     */
    public interface InputProcessorFactory2 {

        /**
         * Creates and returns new input processor.
         *
         * @param defaultProcessor default processor created by
         *             infrastructure that is printing chars to the output window
         * @return new input processor
         */
        @NonNull
        org.netbeans.api.extexecution.base.input.InputProcessor newInputProcessor(@NonNull org.netbeans.api.extexecution.base.input.InputProcessor defaultProcessor);

    }

    /**
     * Factory creating the line convertor.
     */
    public interface LineConvertorFactory {

        /**
         * Creates and returns new line convertor.
         *
         * @return new line convertor
         */
        @NonNull
        LineConvertor newLineConvertor();

    }

    private static final class DescriptorData {

        private Runnable preExecution;

        private Consumer<? super Integer> postExecution;

        private boolean suspend;

        private boolean progress;

        private boolean front;

        private boolean input;

        private boolean controllable;

        private boolean noReset;

        private boolean outLineBased;

        private boolean errLineBased;

        private boolean frontWindowOnError;

        private LineConvertorFactory outConvertorFactory;

        private LineConvertorFactory errConvertorFactory;

        private InputProcessorFactory outProcessorFactory;
        
        private InputProcessorFactory2 outProcessorFactory2;

        private InputProcessorFactory errProcessorFactory;
        
        private InputProcessorFactory2 errProcessorFactory2;

        private InputOutput inputOutput;

        private ExecutionDescriptor.RerunCondition rerunCondition;

        private ExecutionDescriptor.RerunCallback rerunCallback;

        private String optionsPath;

        private Charset charset;

        public DescriptorData() {
            super();
        }

        public DescriptorData(ExecutionDescriptor descriptor) {
            this.preExecution = descriptor.preExecution;
            this.postExecution = descriptor.postExecution;
            this.suspend = descriptor.suspend;
            this.progress = descriptor.progress;
            this.front = descriptor.front;
            this.input = descriptor.input;
            this.controllable = descriptor.controllable;
            this.outLineBased = descriptor.outLineBased;
            this.errLineBased = descriptor.errLineBased;
            this.frontWindowOnError = descriptor.frontWindowOnError;
            this.outConvertorFactory = descriptor.outConvertorFactory;
            this.errConvertorFactory = descriptor.errConvertorFactory;
            this.outProcessorFactory = descriptor.outProcessorFactory;
            this.outProcessorFactory2 = descriptor.outProcessorFactory2;
            this.errProcessorFactory = descriptor.errProcessorFactory;
            this.errProcessorFactory2 = descriptor.errProcessorFactory2;
            this.inputOutput = descriptor.inputOutput;
            this.rerunCondition = descriptor.rerunCondition;
            this.rerunCallback = descriptor.rerunCallback;
            this.optionsPath = descriptor.optionsPath;
            this.charset = descriptor.charset;
            this.noReset = descriptor.noReset;
        }

        public DescriptorData inputOutput(InputOutput io) {
            this.inputOutput = io;
            return this;
        }

        public DescriptorData controllable(boolean controllable) {
            this.controllable = controllable;
            return this;
        }

        public DescriptorData frontWindow(boolean frontWindow) {
            this.front = frontWindow;
            return this;
        }

        public DescriptorData inputVisible(boolean inputVisible) {
            this.input = inputVisible;
            return this;
        }

        public DescriptorData showProgress(boolean showProgress) {
            this.progress = showProgress;
            return this;
        }

        public DescriptorData showSuspended(boolean showSuspended) {
            this.suspend = showSuspended;
            return this;
        }

        public DescriptorData noReset(boolean noReset) {
            this.noReset = noReset;
            return this;
        }

        public DescriptorData outLineBased(boolean outLineBased) {
            this.outLineBased = outLineBased;
            return this;
        }

        public DescriptorData errLineBased(boolean errLineBased) {
            this.errLineBased = errLineBased;
            return this;
        }

        public DescriptorData frontWindowOnError(boolean frontWindowOnError) {
            this.frontWindowOnError = frontWindowOnError;
            return this;
        }

        public DescriptorData outProcessorFactory(InputProcessorFactory outProcessorFactory) {
            this.outProcessorFactory = outProcessorFactory;
            return this;
        }
        
        public DescriptorData outProcessorFactory(InputProcessorFactory2 outProcessorFactory) {
            this.outProcessorFactory2 = outProcessorFactory;
            return this;
        }

        public DescriptorData errProcessorFactory(InputProcessorFactory errProcessorFactory) {
            this.errProcessorFactory = errProcessorFactory;
            return this;
        }
        
        public DescriptorData errProcessorFactory(InputProcessorFactory2 errProcessorFactory) {
            this.errProcessorFactory2 = errProcessorFactory;
            return this;
        }

        public DescriptorData outConvertorFactory(LineConvertorFactory convertorFactory) {
            this.outConvertorFactory = convertorFactory;
            return this;
        }

        public DescriptorData errConvertorFactory(LineConvertorFactory convertorFactory) {
            this.errConvertorFactory = convertorFactory;
            return this;
        }

        public DescriptorData preExecution(Runnable preExecution) {
            this.preExecution = preExecution;
            return this;
        }

        public DescriptorData postExecution(Consumer<Integer> postExecution) {
            this.postExecution = postExecution;
            return this;
        }

        public DescriptorData rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
            this.rerunCondition = rerunCondition;
            return this;
        }

        public DescriptorData rerunCallback(ExecutionDescriptor.RerunCallback rerunCallback) {
            this.rerunCallback = rerunCallback;
            return this;
        }

        public DescriptorData optionsPath(String optionsPath) {
            this.optionsPath = optionsPath;
            return this;
        }

        public DescriptorData charset(Charset charset) {
            this.charset = charset;
            return this;
        }
    }
}
