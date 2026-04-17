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

package org.netbeans.spi.sendopts;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;

/** A subclass of this class shall be registered using {@link org.openide.util.lookup.ServiceProvider}
 * in order to register it for participation on handling
 * and processing of command line options initiated by
 * {@link org.netbeans.api.sendopts.CommandLine#getDefault}'s
 * {@link org.netbeans.api.sendopts.CommandLine#process}.
 * When the {@link Option}s provided by this processor are found
 * on the command line and are consistent, this processor's {@link #process}
 * method is going to be called to handle their values and invoke an action.
 * <p>
 * Looking forward: consider using {@link Arg declarative annotations} for 
 * registering your options more effectively.
 * <p>
 * The usual pattern for writing a subclass of processor is:
 * <pre>
 * {@code @}{@link org.openide.util.lookup.ServiceProvider}(service=OptionProcessor.class)
 * public class MyProcessor extends OptionProcessor {
 *   private Option option1 = ...;
 *   private Option option2 = ...;
 *   private Option option3 = ...;
 * 
 *   protected Set&lt;Option> getOptions() {
 *      Set&lt;Option> set = new HashSet&lt;Option>();
 *      set.add(option1);
 *      set.add(option2);
 *      set.add(option3);
 *      return set;
 *   }
 * 
 *   protected void process(<a href="Env.html">Env</a> env, Map&lt;<a href="Option.html">Option</a>,String[]&gt; values) 
 *       throws {@link CommandException} {
 *     if (values.containsKey(option1)) { ... }
 *     if (values.containsKey(option2)) { ... }
 *     if (values.containsKey(option3)) { ... }
 *   }
 * }
 * </pre>
 * 
 * @author Jaroslav Tulach
 */
public abstract class OptionProcessor {
    /** Constructor for subclasses.
     */
    protected OptionProcessor() {
    }
    
    /** Method to override in subclasses to create 
     * the right set of {@link Option}s.
     * See the factory methods that are part of the {@link Option}'s javadoc
     * or read the <a href="@TOP@/architecture-summary.html#answer-arch-usecases">
     * usecases</a> for the sendopts API.
     * 
     * @return a set of options this processor is interested in, if during
     *   processing at least on of the options appears on command line
     *   the {@link OptionProcessor#process} method will be invoked to
     *   handle such option and its values
     */
    protected abstract Set<Option> getOptions();
    
    
    /** Called by the sendopts parsing infrastructure as a result of
     * {@link org.netbeans.api.sendopts.CommandLine#process}. The method shall read the values
     * associated with the option(s) this {@link OptionProcessor} defines
     * and invoke an action to handle them. While doing this it can 
     * communicate with external world using its environment (see {@link Env}).
     * Such environment provides access to current user directory, standard
     * output and error streams, as well standard input stream. In case
     * the processing of options fails, the code shall thrown {@link CommandException}.
     * 
     * @param env the environment to communicate with
     * @param optionValues map of all options that appeared on command line with their values
     * @exception CommandException in case the processing fails1
     */
    protected abstract void process(Env env, Map<Option,String[]> optionValues)
    throws CommandException;
}
