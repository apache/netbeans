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

package org.netbeans.api.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.*;
import org.netbeans.modules.sendopts.DefaultProcessor;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A class for clients that have an array of strings and want to process
 * it - e.g. parse it and also invoke associated {@link OptionProcessor}s
 * and {@link org.netbeans.spi.sendopts.Arg declarative options}.
 *
 * @author Jaroslav Tulach
 */
public final class CommandLine {
    /** internal errors of CommandLine start here and end here + 100 */
    private static final int ERROR_BASE = 50345;
    /** associated options providers */
    private final Collection<? extends OptionProcessor> processors;

    /** Use factory methods to create the line. */
    CommandLine(Collection<? extends OptionProcessor> p) {
        this.processors = p;
    }
    
    /** Getter for the default command line processor in the system. List
     * of {@link OptionProcessor}s is taken from default
     * {@link Lookup#getDefault() }.
     */
    public static CommandLine getDefault() {
        return new CommandLine(null);
    }

    /** Creates new command line processor based on options defined in
     * the provided <code>classes</code>. These classes are scanned for
     * fields annotated with {@code @}{@link org.netbeans.spi.sendopts.Arg} 
     * annotation or (since version 2.37) checked whether they implement
     * {@link org.netbeans.spi.sendopts.OptionProcessor @OptionProcessor}.
     * 
     * @param classes classes that declare the options
     * @return new command line object that contains options declared in the
     *   provided classes
     * @since 2.20
     */
    public static CommandLine create(Class<?>... classes) {
        return createImpl(classes);
    }

    /** Creates new command line processor based on options defined in
     * the provided <code>objects</code>. The objects can implement
     * the {@link org.netbeans.spi.sendopts.OptionProcessor processor}
     * interface or (if they don't) their classes are scanned
     * for fields annotated with {@code @}{@link org.netbeans.spi.sendopts.Arg}
     * annotation. Alternatively one can register {@link Class} instances with
     * default constructor - such classes will be instantiated and then
     * processed as described above.
     *
     * @param instances objects that declare the command line options
     * @return new command line object that contains options declared in the
     *   provided classes
     * @since 2.37
     */
    public static CommandLine create(Object... instances) {
        return createImpl(instances);
    }

    private static CommandLine createImpl(Object[] instances) {
        List<OptionProcessor> arr = new ArrayList<>();
        for (Object o : instances) {
            Class<?> c;
            Object instance;
            if (o instanceof Class<?>) {
                c = (Class<?>)o;
                instance = null;
            } else {
                c = o.getClass();
                instance = o;
            }
            if (OptionProcessor.class.isAssignableFrom(c)) {
                try {
                    if (instance == null) {
                        instance = c.getDeclaredConstructor().newInstance();
                    }
                    arr.add((OptionProcessor) instance);
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException(ex);
                }
            } else {
                arr.add(DefaultProcessor.create(c, instance));
            }
        }
        return new CommandLine(arr);
    }

    /** Process the array of arguments and invoke associated {@link OptionProcessor}s.
     * 
     * @param args the array of strings to process
     * @exception CommandException if processing is not possible or failed
     */
    public void process(String... args) throws CommandException {
        process(args, null, null, null, null);
    }
    
    /** Process the array of arguments and invoke associated {@link OptionProcessor}s.
     * 
     * @param args the array of strings to process
     * @param is the input stream that processors can read
     * @param os the output stream that processors can write to
     * @param err the output stream that processors can send error messages to
     * @param currentDir directory that processors should use as current user dir
     * @exception CommandException if processing is not possible or failed
     */
    public void process(String[] args, InputStream is, OutputStream os, OutputStream err, File currentDir) throws CommandException {
        if (is == null) {
            is = System.in;
        }
        if (os == null) {
            os = System.out;
        }
        if (err == null) {
            err = System.err;
        }
        if (currentDir == null) {
            currentDir = new File(System.getProperty("user.dir")); // NOI18N
        }
        Env env = OptionImpl.Trampoline.DEFAULT.create(this, is, os, err, currentDir);
        
        
        ArrayList<String> additionalParams = new ArrayList<String>();
        ArrayList<OptionImpl> opts = new ArrayList<OptionImpl>();
        OptionImpl acceptsAdons = null;
        
        OptionImpl[] mainOptions = getOptions();
        LinkedHashSet<OptionImpl> allOptions = new LinkedHashSet<OptionImpl>();
        for (int i = 0; i < mainOptions.length; i++) {
            mainOptions[i] = mainOptions[i].addWorkingCopy(allOptions);
        }
        OptionImpl[] arr = allOptions.toArray(new OptionImpl[0]);
        
        boolean optionMode = true;
        ARGS: for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue ARGS;
            }

            if (optionMode) {
                if (args[i].startsWith("--")) { //NOI18N
                    if (args[i].length() == 2) {
                        optionMode = false;
                        continue ARGS;
                    }

                    String text = args[i].substring(2);
                    String value = null;
                    int textEqual = text.indexOf('=');
                    if (textEqual >= 0) {
                        // strip the name of the option
                        value = text.substring(textEqual + 1);
                        text = text.substring(0, textEqual);
                    }
                    OptionImpl opt = findByLongName (text, arr);
                    if (opt == null) {
                        throw new CommandException(args[i], ERROR_BASE + 1,
                            getMessage("MSG_Unknown", args[i])
                        );
                    }
                    if (opt.getArgumentType() == 1 && value == null) {
                        // read next value from the argument
                        for(;;) {
                            if (++i == args.length) {
                                throw new CommandException(getMessage("MSG_MissingArgument", "--" + opt.getLongName()), ERROR_BASE + 2); // NOI18N
                            }
                            
                            if (args[i].equals("--")) { //NOI18N
                                optionMode = false;
                                continue;
                            }
                            
                            if (optionMode && args[i].startsWith("-")) { //NOI18N
                                throw new CommandException(getMessage("MSG_MissingArgument", "--" + opt.getLongName()), ERROR_BASE + 2); // NOI18N
                            }

                            break;
                        }
                        
                        
                        
                        value = args[i];
                    }


                    if (value != null) {
                        if (opt.getArgumentType() != 1 && opt.getArgumentType() != 2) {
                            throw new CommandException(getMessage("MSG_OPTION_CANNOT_HAVE_VALUE", opt, value), ERROR_BASE + 2);
                        }

                        opt.associateValue(value);
                    }

                    if (opt.getArgumentType() == 3) {
                        if (acceptsAdons != null) {
                            String oName1 = findOptionName(acceptsAdons, args);
                            String oName2 = findOptionName(opt, args);
                            String msg = getMessage("MSG_CannotTogether", oName1, oName2); // NOI18N
                            throw new CommandException(msg, ERROR_BASE + 3);
                        }
                        acceptsAdons = opt;
                    }

                    opts.add(opt);
                    continue ARGS;
                } else if (args[i].startsWith("-") && args[i].length() > 1) { //NOI18N
                    for (int j = 1; j < args[i].length(); j++) {
                        char ch = args[i].charAt(j);
                        OptionImpl opt = findByShortName(ch, arr);
                        if (opt == null) {
                            throw new CommandException(getMessage("MSG_UNKNOWN_OPTION", args[i]), ERROR_BASE + 1);
                        }
                        if (args[i].length() == j + 1 && opt.getArgumentType() == 1) {
                            throw new CommandException(getMessage("MSG_MissingArgument", args[i]), ERROR_BASE + 2);
                        }

                        if (args[i].length() > j && (opt.getArgumentType() == 1 || opt.getArgumentType() == 2)) {
                            opt.associateValue(args[i].substring(j + 1));
                            j = args[i].length();
                        }
                        if (opt.getArgumentType() == 3) {
                            if (acceptsAdons != null) {
                                String oName1 = findOptionName(acceptsAdons, args);
                                String oName2 = findOptionName(opt, args);
                                String msg = getMessage("MSG_CannotTogether", oName1, oName2); // NOI18N
                                throw new CommandException(msg, ERROR_BASE + 3);
                            }
                            acceptsAdons = opt;
                        }
                        opts.add(opt);
                    }
                    continue ARGS;
                }
            }
            
            additionalParams.add(args[i]);
        }
        
        if (acceptsAdons == null && !additionalParams.isEmpty()) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].getArgumentType() == 4) {
                    if (acceptsAdons != null) {
                        throw new CommandException(getMessage("MSG_TWO_DEFAULT_OPTIONS", acceptsAdons, arr[i]), ERROR_BASE + 3);
                    }
                    acceptsAdons = arr[i];
                    opts.add(acceptsAdons);
                }
            }
            if (acceptsAdons == null) {
                throw new CommandException(getMessage("MSG_NOT_PROCESSED_PARAMS", additionalParams), ERROR_BASE + 2);
            }
            
        }
        
        OptionImpl.Appearance[] postProcess = new OptionImpl.Appearance[mainOptions.length];
        {
            HashSet<OptionImpl> used = new HashSet<OptionImpl>(opts);
            for (int i = 0; i < mainOptions.length; i++) {
                OptionImpl.Appearance res = mainOptions[i].checkConsistent(used);
                postProcess[i] = res;
                if (res.isThere()) {
                    mainOptions[i].markConsistent(res);
                }
/*                
                if (res.isError()) {
                    throw new CommandException(res.errorMessage(args), ERROR_BASE + 4);
                }
 */
            }
        }
        
        
        {
            HashSet<OptionImpl> used = new HashSet<OptionImpl>(opts);
            for (int i = 0; i < mainOptions.length; i++) {
                if (postProcess[i].isError()) {
                    OptionImpl error = mainOptions[i].findNotUsedOption(used);
                    if (error != null) {
                        throw new CommandException(postProcess[i].errorMessage(args), ERROR_BASE + 4);    
                    }
                }
            }
        }

        Map<OptionProcessor,Map<Option,String[]>> providers = new LinkedHashMap<OptionProcessor,Map<Option,String[]>>();
        {
            for (int i = 0; i < mainOptions.length; i++) {
                if (postProcess[i].isThere()) {
                    Map<Option,String[]> param = providers.get(mainOptions[i].getProvider());
                    if (param == null) {
                        param = new HashMap<Option,String[]>();
                        providers.put(mainOptions[i].getProvider(), param);
                    }
                    mainOptions[i].process(additionalParams.toArray(new String[0]), param);
                }
            }
        }
        
        for (Map.Entry<OptionProcessor, Map<Option, String[]>> pair : providers.entrySet()) {
            OptionImpl.Trampoline.DEFAULT.process(pair.getKey(), env, pair.getValue());
        }
    }

    /** Prints the usage information about options provided by associated
     * {@link OptionProcessor}s.
     *
     * @param w the writer to output usage info to
     * @since 1.7
     */
    public void usage(PrintWriter w) {
        OptionImpl[] mainOptions = getOptions();
        LinkedHashSet<OptionImpl> allOptions = new LinkedHashSet<OptionImpl>();
        for (int i = 0; i < mainOptions.length; i++) {
            mainOptions[i].addWorkingCopy(allOptions);
        }
        OptionImpl[] arr = allOptions.toArray(new OptionImpl[0]);

        int max = 25;
        String[] prefixes = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            StringBuilder sb = new StringBuilder();
            
            String ownDisplay = OptionImpl.Trampoline.DEFAULT.getDisplayName(arr[i].getOption(), Locale.getDefault());
            if (ownDisplay != null) {
                sb.append(ownDisplay);
            } else {
                String sep = ""; //NOI18N
                if (arr[i].getShortName() != -1) {
                    sb.append('-');
                    sb.append((char)arr[i].getShortName());
                    sep = ", "; //NOI18N
                }
                if (arr[i].getLongName() != null) {
                    sb.append(sep);
                    sb.append("--"); // NOI18N
                    sb.append(arr[i].getLongName());
                } else {
                    if (sep.length() == 0) {
                        continue;
                    }
                }

                switch (arr[i].getArgumentType()) {
                    case 0: break;
                    case 1:
                        sb.append(' ');
                        sb.append(getMessage("MSG_OneArg")); // NOI18N
                        break;
                    case 2:
                        sb.append(' ');
                        sb.append(getMessage("MSG_OptionalArg")); // NOI18N
                        break;
                    case 3:
                        sb.append(' ');
                        sb.append(getMessage("MSG_AddionalArgs")); // NOI18N
                        break;
                    default:
                        assert false;
                }
            }

            if (sb.length() > max) {
                max = sb.length();
            }

            prefixes[i] = sb.toString();
        }

        for (int i = 0; i < arr.length; i++) {
            if (prefixes[i] != null) {
                w.print("  "); // NOI18N
                w.print(prefixes[i]);
                for (int j = prefixes[i].length(); j < max; j++) {
                    w.print(' ');
                }
                w.print(' ');
                arr[i].usage(w, max);
                w.println();
            }
        }

        w.flush();
    }

    private OptionImpl[] getOptions() {
        ArrayList<OptionImpl> arr = new ArrayList<OptionImpl>();
        
        Iterable<? extends OptionProcessor> proc = processors;
        if (proc == null) {
            try {
                proc = lookupOptionProcessors();
            } catch (LinkageError ex) {
                // OK, running in standalone mode without Lookup
                proc = ServiceLoader.load(OptionProcessor.class);
            }
        }
        
        for (OptionProcessor p : proc) {
            org.netbeans.spi.sendopts.Option[] all = OptionImpl.Trampoline.DEFAULT.getOptions(p);
            for (int i = 0; i < all.length; i++) {
                arr.add(OptionImpl.cloneImpl(OptionImpl.find(all[i]), all[i], p));
            }
        }
        
        return arr.toArray(new OptionImpl[0]);
    }

    private Collection<? extends OptionProcessor> lookupOptionProcessors() {
        return Lookup.getDefault().lookupAll(OptionProcessor.class);
    }
    
    private OptionImpl findByLongName(String lng, OptionImpl[] arr) {
        boolean abbrev = false;
        OptionImpl best = null;
        for (int i = 0; i < arr.length; i++) {
            String on = arr[i].getLongName();
            if (on == null) {
                continue;
            }
            if (lng.equals(on)) {
                return arr[i];
            }
            if (on.startsWith(lng)) {
                abbrev = best == null;
                best = arr[i];
            }
        }
        
        return abbrev ? best : null;
    }
    
    private OptionImpl findByShortName(char ch, OptionImpl[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (ch == arr[i].getShortName()) {
                return arr[i];
            }
        }
        return null;
    }

    private static String findOptionName(OptionImpl opt, String[] args) {
        for(int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) { //NOI18N
                continue;
            }
            
            if (args[i].startsWith("--")) { //NOI18N
                String text = args[i].substring(2);
                int textEqual = text.indexOf('=');
                if (textEqual >= 0) {
                    // strip the name of the option
                    text = text.substring(0, textEqual);
                }
                if (text.startsWith(opt.getLongName())) {
                    return args[i];
                }
            } else {
                if (opt.getShortName() == args[i].charAt(1)) {
                    return "-" + (char)opt.getShortName(); //NOI18N
                }
            }
        }
        
        return opt.toString();
    }

    private static String getMessage(String msg, Object... args) {
        final Class<?> c = CommandException.class;
        try {
            return NbBundle.getMessage(c, msg, args);
        } catch (LinkageError ex) {
            ResourceBundle b = ResourceBundle.getBundle(c.getPackage().getName() + ".Bundle"); // NOI18N
            String res = b.getString(msg);
            if (args != null && args.length > 0) {
                res = MessageFormat.format(res, args);
            }
            return res;
        }
    }

}
