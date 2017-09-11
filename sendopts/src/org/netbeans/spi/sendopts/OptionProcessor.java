/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * or read the <a href="@TOP@/architecture-summary.html#answer-usecases">
     * usecases</a> for the sendopts API.
     * <p>
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
