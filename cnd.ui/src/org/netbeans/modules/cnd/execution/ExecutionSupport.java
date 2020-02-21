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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.execution;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.openide.loaders.MultiDataObject;

import org.openide.nodes.Node;

import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/** Support for execution of a data object.
 * @since 3.14
 */
public class ExecutionSupport implements Node.Cookie {

    /** extended attribute for attributes */
    private static final String EA_ARGUMENTS = "NetBeansAttrArguments"; // NOI18N
    /** extended attribute for attributes */
    private static final String EA_ENVIRONMENT_VARIABLES = "NetBeansAttrEnvironment"; // NOI18N

    // copy from JavaNode
    /** Name of property providing argument parameter list. */
    public static final String PROP_FILE_PARAMS = "params"; // NOI18N
    /** Name of property providing a custom {@link Executor} for a file. */
    public static final String PROP_EXECUTION = "execution"; // NOI18N
    /** entry to be associated with */
    private final MultiDataObject.Entry entry;
    /**  readOnlyAttrs is name of virtual attribute. This name of virtual attribute 
     * is shared between classes (and should be changed everywhere): 
     * - org.openide.filesystems.DefaultAttributes
     * - org.openide.loaders.ExecutionSupport
     * - org.openide.loaders.CompilerSupport
     * - org.netbeans.core.ExJarFileSystem
     */
    protected final static String READONLY_ATTRIBUTES = "readOnlyAttrs"; //NOI18N

    /** Create new support for given entry. The file is taken from the
     * entry and is updated if the entry moves or renames itself.
     * @param entry entry to create instance from
     */
    public ExecutionSupport(MultiDataObject.Entry entry) {
        this.entry = entry;
    }

    /** Get the associated file that can be executed.
     * @return the file that can be executed
     */
    protected MultiDataObject.Entry getEntry() {
        return entry;
    }

    /* Starts the class.
     */
    public void start() {
        throw new UnsupportedOperationException();
    }

    /** Called when invocation of the executor fails. Allows to do some
     * modifications to the type of execution and try it again.
     *
     * @param ex exception that occurred during execution
     * @return true if the execution should be restarted
     */
    protected boolean startFailed(IOException ex) {
        return false;
    }

    /* Sets execution arguments for the associated entry.
     * @param args array of arguments
     * @exception IOException if arguments cannot be set
     */
    public void setArguments(String[] args) throws IOException {
        entry.getFile().setAttribute(EA_ARGUMENTS, args);
    }

    /** Set execution arguments for a given entry.
     * @param entry the entry
     * @param args array of arguments
     * @exception IOException if arguments cannot be set
     */
    public static void setArguments(MultiDataObject.Entry entry, String[] args) throws IOException {
        entry.getFile().setAttribute(EA_ARGUMENTS, args);
    }

    /* Getter for arguments associated with given file.
     * @return the arguments or empty array if no arguments associated
     */
    public String[] getArguments() {
        return getArguments(entry);
    }

    /** Get the arguments associated with a given entry.
     * @param entry the entry
     * @return the arguments, or an empty array if no arguments are specified
     */
    public static String[] getArguments(MultiDataObject.Entry entry) {
        Object o = entry.getFile().getAttribute(EA_ARGUMENTS);
        if (o != null && (o instanceof String[])) {
            return (String[]) o;
        } else {
            return new String[]{};
        }
    }

    /* Sets execution arguments for the associated entry.
     * @param args array of arguments
     * @exception IOException if arguments cannot be set
     */
    public void setEnvironmentVariables(String[] args) throws IOException {
        entry.getFile().setAttribute(EA_ENVIRONMENT_VARIABLES, args);
    }

    /* Getter for arguments associated with given file.
     * @return the arguments or empty array if no arguments associated
     */
    public String[] getEnvironmentVariables() {
        Object o = entry.getFile().getAttribute(EA_ENVIRONMENT_VARIABLES);
        if (o != null && (o instanceof String[])) {
            return (String[]) o;
        } else {
            return new String[]{};
        }
    }

    /** Helper method that creates default properties for execution of
     * a given support.
     * Includes properties to set the executor; debugger; and arguments.
     *
     * @param set sheet set to add properties to
     */
    public void addProperties(Sheet.Set set) {
        set.put(createParamsProperty(PROP_FILE_PARAMS, getString("PROP_fileParams"), getString("HINT_fileParams"))); // NOI18N
    }

    protected PropertySupport<String> createParamsProperty(String propertyName, String displayName, String description) {
                PropertySupport<String> result = new PropertySupport.ReadWrite<String>(
                propertyName, String.class, displayName, description) {

//            public String getValue() {
//                String[] args = getArguments();
//                /*
//                StringBuffer b = new StringBuffer(50);
//                for (int i = 0; i < args.length; i++) {
//                b.append(args[i]).append(' ');
//                }
//                return b.toString();
//                 */
//                return Utilities.escapeParameters(args);
//            }
//
//            public void setValue(String val) throws InvocationTargetException {
//                if (val != null) {
//                    try {
//                        setArguments(Utilities.parseParameters(val));
//                    } catch (IOException e) {
//                        throw new InvocationTargetException(e);
//                    }
//                } else {
//                    throw new IllegalArgumentException();
//                }
//            }

            @Override
            public String getValue() {
                String[] args = getArguments();
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    b.append(args[i]).append(' ');
                }
                return b.toString();
            }

            @Override
            public void setValue(String val) throws InvocationTargetException {
                if (val != null) {
                    try {
                        // Keep user arguments as is in args[0]
                        setArguments(new String[]{val});
                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public boolean supportsDefaultValue() {
                return true;
            }

            @Override
            public void restoreDefaultValue() throws InvocationTargetException {
                try {
                    setArguments(null);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
            }

            @Override
            public boolean canWrite() {
                Boolean isReadOnly = (Boolean) entry.getFile().getAttribute(READONLY_ATTRIBUTES);
                return (isReadOnly == null) ? false : (!isReadOnly.booleanValue());
            }
        };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.  Arguments can't be multiline anyway.
        result.setValue("oneline", Boolean.TRUE); // NOI18N
        return result;
    }

    protected PropertySupport<String> createEnvironmentProperty(String propertyName, String displayName, String description) {
                PropertySupport<String> result = new PropertySupport.ReadWrite<String>(
                propertyName, String.class, displayName, description) {
            @Override
            public String getValue() {
                String[] args = getEnvironmentVariables();
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < args.length; i++) {
                    list.add(args[i]);
                }
                list = ImportUtils.quoteList(list);
                StringBuilder b = new StringBuilder();
                for (String s : list) {
                    b.append(s).append(' '); // NOI18N
                }
                return b.toString();
            }
            @Override
            public void setValue(String val) throws InvocationTargetException {
                if (val != null) {
                    try {
                        List<String> vars = ImportUtils.parseEnvironment(val);
                        setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
            @Override public boolean supportsDefaultValue() {
                return true;
            }
            @Override public void restoreDefaultValue() throws InvocationTargetException {
                try {
                    setEnvironmentVariables(null);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                }
            }
            @Override public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.
        result.setValue("oneline", Boolean.TRUE); // NOI18N
        return result;
    }

    /** @return a localized String */
    private static String getString(String s) {
        return NbBundle.getMessage(ExecutionSupport.class, s);
    }
}
