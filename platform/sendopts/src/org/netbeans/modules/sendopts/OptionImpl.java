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

package org.netbeans.modules.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;

/** Internal representtion of an option. It is used in
 * the implementation of the API package, the API shall 
 * be completely separated from the SPI by using this description
 * of an option.
 *
 * @author Jaroslav Tulach
 */
public abstract class OptionImpl implements Cloneable {
    public static final Logger LOG = Logger.getLogger(OptionImpl.class.getName());
    
    public static final Object NO_DEFAULT = new Object();
    public static final String NO_BUNDLE = new String();
    
    static final String[] NO_VALUE = new String[0];
    
    /** the root option */
    public final Option root;
    
    /** our peer option */
    Option option;
    /** 0 - no options
     * 1 - one required option
     * 2 - one voluntary option
     */
    final int argumentType;
    /** provider that is supposed to process the value */
    OptionProcessor provider;
    /** mark appearance */
    private Appearance appear;

    /**
     * Creates a new instance of OptionImpl
     */
    OptionImpl(Option o, OptionProcessor p, int a) {
        this.root = o;
        this.option = o;
        this.provider = p;
        this.argumentType = a;
    }
    
    public int getShortName() {
        return Trampoline.DEFAULT.getShortName(option);
    }
    
    public String getLongName() {
        return Trampoline.DEFAULT.getLongName(option);
    }
    
    /** @return 0 for no argument option, 1 for required argument and 2 for optional argument */
    public int getArgumentType() {
        return this.argumentType;
    }

    public int hashCode() {
        return option.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof OptionImpl) {
            return option.equals(((OptionImpl)obj).option);
        }
        return false;
    }

    /** Appends to the buffer description of the optionimpl. Called from Option.toString()
     */
    public final void append(StringBuffer sb) {
        String c = getClass().getName();
        int i = c.indexOf('$');
        assert i >= 0;
        sb.append(c.substring(i + 1));
        appendInternals(sb);
    }
    
    void appendInternals(StringBuffer sb) {
    }
    
    public String toString() {
        return "Impl:" + option; // NOI18N
    }
    
    /** Finds a possible name for this option. 
     * @param used if true then one is going to search for real occurence of the option in the command line
     *        otherwise any possible name will do
     * @param args the command line
     */
    public String findName(boolean used, String[] args) {
        if (used) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-" + (char)getShortName())) {
                    return "-" + (char)getShortName(); // NOI18N
                }
                if (args[i].startsWith("--" + getLongName())) {
                    return "--" + getLongName(); // NOI18N
                }
            }
        } else {
            if (getLongName() != null) {
                return "--" + getLongName(); // NOI18N
            }
            if (getShortName() != -1) {
                return "-" + (char)getShortName(); // NOI18N
            }
        }
        
        return null;
    }
    
    /** Obtains the option for this OptionImpl.
     */
    public Option getOption() {
        return option;
    }
    
    /** Provider associated with the Option Impl.
     * @return the provider or null
     */
    public OptionProcessor getProvider() {
        return provider;
    }
    
    
    @SuppressWarnings("unchecked")
    private static <T extends OptionImpl> T doClone(T impl) {
        try {
            return (T)impl.clone();
        } catch (CloneNotSupportedException ex) {
            assert false;
            return null;
        }
    }

    /** Creates a clone of the option impl with new option.
     */
    public static OptionImpl cloneImpl(OptionImpl proto, Option option, OptionProcessor prov) {
        OptionImpl n;
        try {
            n = (OptionImpl) proto.clone();
        } catch (CloneNotSupportedException ex) {
            assert false;
            return null;
        }
        n.option = option;
        if (prov != null) {
            n.provider = prov;
        }
        return n;
    }

    /** Clones itself into a working copy option. For most options just add "this", for 
     * more complex ones override
     * @return the reference to new itself
     */
    public final OptionImpl addWorkingCopy(Collection<OptionImpl> allOptions) {
        if (allOptions.contains(this)) {
            // find element that is equal
            Iterator it = allOptions.iterator();
            for(;;) {
                OptionImpl elem = (OptionImpl) it.next();
                if (elem.equals(this)) {
                    return (OptionImpl)elem;
                }
            }
        } else {
            return handleAdd(allOptions);
        }
    }

    /** Called to add this option to allOptions collection. 
     * It is guaranteed that the option is not there.
     */
    protected abstract OptionImpl handleAdd(Collection<OptionImpl> allOptions);
    
    public static class Appearance {
        private MessageFactory msg;
        
        private Appearance() {}
        
        public static final Appearance YES = new Appearance();
        public static final Appearance NO = new Appearance();
        public static final Appearance MAYBE = new Appearance();
        
        public static final Appearance createError(MessageFactory msg) {
            Appearance a = new Appearance();
            a.msg = msg;
            return a;
        }
        
        
        public final boolean isThere() {
            return this == YES;
        }
        
        public boolean isError() {
            return YES != this && NO != this && MAYBE != this;
        }
        
        public String errorMessage(String[] args) {
            // you are allowed to call this method for on isError() Appearance!
            return msg.errorMessage(args);
        }
        
        public String toString() {
            if (this == YES) return "YES"; // NOI18N
            if (this == NO) return "NO"; // NOI18N
            if (this == MAYBE) return "MAYBE"; // NOI18N
            return "ERROR[" + errorMessage(new String[0]) + "]"; // NOI18N
        }
    } // end of Appearance
    
    public static interface MessageFactory {
        public String errorMessage(String[] args);
    } // end of MessageFactory
    
    
    /** Consistency test, based on the list of leaf options present in the
     * command line.
     * @param presentOptions set of leaf options present in the command line
     * @return Boolean - TRUE option is there, FALSE option is not there, null everything is broken
     */
    public abstract Appearance checkConsistent(Set<OptionImpl> presentOptions);
    
    /** Marks each optionimpl as ok, and used.
     */
    public void markConsistent(Appearance a) {
        assert appear == null || appear == a;
        appear = a;
    }

    /** Fills the map with an option that shall be processed.
     */
    public abstract void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException;

    /** Associates this option with given value */
    public abstract void associateValue(String value) throws CommandException;


    public void usage(PrintWriter w, int max) {
        Trampoline.DEFAULT.usage(w, option, max);
    }

    public OptionImpl findNotUsedOption(Set<OptionImpl> used) {
        return appear == null && used.contains(this) ? this : null;
    }
    public static OptionImpl createAlways(Option o) {
        class Always extends OptionImpl {
            public Always(Option o, OptionProcessor p, int t) {
                super(o, p, 5);
            }
    
            public void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException {
                optionsAndTheirArgs.put(option, NO_VALUE);
            }
            public void associateValue(String value) {
                throw new IllegalStateException();
            }
            
            public Appearance checkConsistent(Set<OptionImpl> presentOptions) {
                return Appearance.YES;
            }
            
            protected OptionImpl handleAdd(Collection<OptionImpl> allOptions) {
                Always n = doClone(this);
                allOptions.add(n);
                return n;
            }
        }
        return new Always(o, null, 0);
    }
    
    /** Creates an impl that delegates to given option and NoArgumentProcessor processor.
     */
    public static OptionImpl createNoArg(Option o) {
        class NoArg extends OptionImpl {
            public NoArg(Option o, OptionProcessor p, int t) {
                super(o, p, 0);
            }
            
            public void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException {
                optionsAndTheirArgs.put(option, NO_VALUE);
            }
            public void associateValue(String value) {
                throw new IllegalStateException();
            }
            
            public Appearance checkConsistent(Set<OptionImpl> presentOptions) {
                return presentOptions.contains(this) ? Appearance.YES : Appearance.NO;
            }
            
            protected OptionImpl handleAdd(Collection<OptionImpl> allOptions) {
                NoArg n = doClone(this);
                allOptions.add(n);
                return n;
            }
        }
        return new NoArg(o, null, 0);
    }
    
    public static  OptionImpl createOneArg(Option option, boolean required) {
        class OneOptionImpl extends OptionImpl {
            private String arg;
            
            public OneOptionImpl(Option option, OptionProcessor p, int type, String value) {
                super(option, p, type);
                this.arg = value;
            }

            public void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException {
                if (arg != null) {
                    optionsAndTheirArgs.put(option, new String[] { arg });
                } else {
                    assert argumentType == 2;
                    optionsAndTheirArgs.put(option, NO_VALUE);
                }
            }

            public void associateValue(String value) throws CommandException {
                this.arg = value;
            }

            public OptionImpl handleAdd(Collection<OptionImpl> allOptions) {
                OneOptionImpl one = doClone(this);
                allOptions.add(one);
                return one;
            }

            public Appearance checkConsistent(Set<OptionImpl> presentOptions) {
                return presentOptions.contains(this) ? Appearance.YES : Appearance.NO;
            }
        }
        
        return new OneOptionImpl(option, null, required ? 1 : 2, null);
    }
    public static  OptionImpl createAdd(Option option, final boolean defaultOption) {
        class AddOptionImpl extends OptionImpl {
            private boolean processed;
            
            public AddOptionImpl(Option option, OptionProcessor p) {
                super(option, p, defaultOption ? 4 : 3);
            }
            public void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException {
                optionsAndTheirArgs.put(option, additionalArgs);
            }
            public void associateValue(String value) throws CommandException {
                throw new IllegalStateException();
            }
            public Appearance checkConsistent(Set<OptionImpl> presentOptions) {
                return presentOptions.contains(this) ? Appearance.YES : Appearance.NO;
            }
            protected OptionImpl handleAdd(Collection<OptionImpl> allOptions) {
                AddOptionImpl n = doClone(this);
                allOptions.add(n);
                return n;
            }
        }
        
        return new AddOptionImpl(option, null);
    }
    
    /** Finds impl for given option.
     */
    public static OptionImpl find(Option op) {
        return Trampoline.DEFAULT.impl(op);
    }

    /** type of the option: 
     * 0 = oneOf
     * 1 = allOf
     * 2 = someOf
     */
    public static  OptionImpl create(Option option, final int type, List<Option> alternatives) {
        class AlternativeOptionsImpl extends OptionImpl {
            /** array of options to select one from */
            private List<OptionImpl> arr;
            /** array of used options */
            private List<OptionImpl> used;
            /** set of missing options */
            private Set<OptionImpl> missing;
            
            public AlternativeOptionsImpl(Option self, OptionProcessor p, List<OptionImpl> arr) {
                super(self, p, 0);
                this.arr = arr;
            }

            public Appearance checkConsistent(Set<OptionImpl> presentOptions) {
                int cnt = 0;
                
                used = new ArrayList<OptionImpl>();
                missing = new HashSet<OptionImpl>();

                Set<OptionImpl> maybe = new HashSet<OptionImpl>();
                for (int i = 0; i < arr.size(); i++) {
                    Appearance check = arr.get(i).checkConsistent(presentOptions);
                    if (check.isError()) {
                        // not consistent
                        return check;
                    }
                    
                    if (Appearance.NO == check) {
                        missing.add(arr.get(i));
                        continue;
                    }
                    if (Appearance.MAYBE == check) {
                        maybe.add(arr.get(i));
                        continue;
                    }
                    
                    cnt++;
                    used.add(arr.get(i));
                }
                
                if (cnt == 0) {
                    // this option is not there
                    return type == 3 ? Appearance.MAYBE : Appearance.NO;
                }

                switch (type) {
                    case 0: if (cnt == 1) {
                        // ok, success => we have exactly one option
                        return Appearance.YES;
                    }
                    break;
                    case 1: if (missing.isEmpty()) {
                        used.addAll(maybe);
                        // ok, success => we have all our options
                        return Appearance.YES;
                    }
                    break;
                    case 3:
                    case 2: if (cnt >= 1) {
                        // ok, success => we have at least one
                        return Appearance.YES;
                    }
                    break;
                }
                
                class MF implements MessageFactory {
                    public String errorMessage(String[] args) {
                        switch (type) {
                        case 0:
                            assert used.size() >= 2 : "At least two: " + used;

                            String n1 = used.get(0).findName(true, args);
                            String n2 = used.get(1).findName(true, args);
                            assert n1 != null;
                            assert n2 != null;

                            return NbBundle.getMessage(CommandLine.class, "MSG_CannotTogether", n1, n2);
                        case 1: {
                                StringBuffer sb = new StringBuffer();
                                String app = "";
                                for (OptionImpl i : arr) {
                                    sb.append(app);
                                    sb.append(i.findName(false, args));
                                    app = ", ";
                                }
                                return NbBundle.getMessage(CommandLine.class, "MSG_MissingOptions", sb.toString());
                            }
                        case 2: {
                                StringBuffer sb = new StringBuffer();
                                String app = "";
                                for (OptionImpl i : arr) {
                                    sb.append(app);
                                    sb.append(i.findName(false, args));
                                    app = ", ";
                                }
                                return NbBundle.getMessage(CommandLine.class, "MSG_NeedAtLeastOne", sb.toString());
                            }
                        default:
                            throw new IllegalStateException("Type: " + type);
                        }
                    }
                }
                
                
                // not everything is ok, more options present
                return Appearance.createError(new MF());
            }

            @Override
            public void markConsistent(Appearance a) {
                super.markConsistent(a);
                for (OptionImpl i : arr) {
                    i.markConsistent(a);
                }
            }

            @Override
            public OptionImpl findNotUsedOption(Set<OptionImpl> used) {
                OptionImpl me = super.findNotUsedOption(used);
                if (me != null) {
                    return me;
                }
                for (OptionImpl i : arr) {
                    me = i.findNotUsedOption(used);
                    if (me != null) {
                        return me;
                    }
                }
                return null;
            }

            public void associateValue(String value) throws CommandException {
            }
            public OptionImpl handleAdd(Collection<OptionImpl> allOptions) {
                List<OptionImpl> copy = new ArrayList<OptionImpl>();
                for (int i = 0; i < arr.size(); i++) {
                    copy.add(arr.get(i).addWorkingCopy(allOptions));
                }

                AlternativeOptionsImpl alt = doClone(this);
                alt.arr = copy;
                allOptions.add(alt);
                return alt;
            }

            public void process(String[] additionalArgs, Map<Option,String[]> optionsAndTheirArgs) throws CommandException {
                optionsAndTheirArgs.put(option, NO_VALUE);
                for (OptionImpl i : used) {
                    i.process(additionalArgs, optionsAndTheirArgs);
                }
            }

            @Override
            public String findName(boolean usedOrAny, String[] args) {
                for (Iterator it = arr.iterator(); it.hasNext();) {
                    OptionImpl elem = (OptionImpl) it.next();
                    String n = elem.findName(usedOrAny, args);
                    if (n != null) {
                        return n;
                    }
                }
                return null;
            }
        }
        
        ArrayList<OptionImpl> list = new ArrayList<OptionImpl>();
        for (int i = 0; i < alternatives.size(); i++) {
            list.add(find(alternatives.get(i)));
        }
        
        return new AlternativeOptionsImpl(option, null, list);
    }

    public abstract static class Trampoline {
        public static Trampoline DEFAULT;
        static {
            try {
                Class.forName(Option.class.getName(), true, Trampoline.class.getClassLoader());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        public abstract OptionImpl impl(Option o);
        public abstract Env create(CommandLine cmd, InputStream is, OutputStream os, OutputStream err, File currentDir);
        public abstract void usage(PrintWriter w, Option o, int max);
        public abstract Option[] getOptions(OptionProcessor p);
        public abstract void process(OptionProcessor provider, Env env, Map<Option,String[]> options)
        throws CommandException;
        public abstract String getLongName(Option o);
        public abstract int getShortName(Option o);
        public abstract String getDisplayName(Option o, Locale l);
    }
}
