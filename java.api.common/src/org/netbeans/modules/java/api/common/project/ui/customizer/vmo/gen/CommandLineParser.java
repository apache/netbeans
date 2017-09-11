/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen;

import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.JavaVMOption;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.SwitchNode;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.UserPropertyNode;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.UnrecognizedOption;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.UnknownOption;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.ParametrizedNode;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.regex.Pattern;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class CommandLineParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "TEXT", "LETTER", "'-'", "'='", "'\\''", "'\"'"
    };
    public static final int EOF=-1;
    public static final int T__7=7;
    public static final int T__8=8;
    public static final int T__9=9;
    public static final int T__10=10;
    public static final int WS=4;
    public static final int TEXT=5;
    public static final int LETTER=6;

    // delegates
    // delegators


        public CommandLineParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CommandLineParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CommandLineParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g"; }



    	private static final String SERVER = "server";
    	private static final String CLIENT = "client";
    	private static final String ESA = "esa";
    	private static final String ESA_LONG = "enablesystemassertions";
    	private static final String DSA = "dsa";
    	private static final String DSA_LONG = "disablesystemassertions";
    	private static final String EA = "ea";
    	private static final String EA_LONG = "enableassertions";
    	private static final String DA = "da";
    	private static final String DA_LONG = "disableassertions";
    	private static final String VERBOSE = "verbose";
    	private static final String SVERSION = "showversion";
    	private static final String HELP = "?";
    	private static final String HELP_LONG = "help";
    	private static final String X = "X";
    	private static final String XINT = "Xint";
    	private static final String XBATCH = "Xbatch";
    	private static final String XCJNI = "Xcheck";
    	private static final String XFUTURE = "Xfuture";
    	private static final String XNOCLSGC = "Xnoclassgc";
    	private static final String XINCGC = "Xincgc";
    	private static final String XPROF = "Xprof";
    	private static final String XRS = "Xrs";
    	private static final String XSHARE = "Xshare";
    	private static final String JRE_SEARCH="jre-restrict-search";
    	private static final String JRE_NO_SEARCH="jre-no-restrict-search";
    	private static final String SPLASH = "splash";
    	private static final String XLOGGC = "Xloggc";
    	private static final String JAVAAGENT = "javaagent";
    	private static final String AGENTLIB = "agentlib";
    	private static final String AGENTPATH = "agentpath";
    	private static final String BOOTCP = "Xbootclasspath";
    	private static final String BOOTCPAPPEND = "Xbootclasspath/a";
    	private static final String BOOTCPPREPEND = "Xbootclasspath/p";
    	private static final String VERSION = "version";
    	private static final String CLASSPATH = "cp";
    	private static final String CLASSPATH_LONG = "classpath";

    	private static final Set<String> switchOptions = new HashSet<String>() {
    	    {
    	    	this.addAll(Arrays.asList(
    	    	SERVER,
    	    	CLIENT,
    	    	ESA,
    	    	ESA_LONG,
    	    	DSA,
    	    	DSA_LONG,
    	    	EA,
    	    	EA_LONG,
    	    	DA,
    	    	DA_LONG,
    	    	SVERSION,
    	    	HELP,
    	    	HELP_LONG,
    	    	X,
    	    	XINT,
    	    	XBATCH,
    	    	XFUTURE,
    	    	XNOCLSGC,
    	    	XINCGC,
    	    	XPROF,
    	    	XRS,
    	    	JRE_SEARCH,
    	    	JRE_NO_SEARCH));
    	    }
    	};
    	
    	private static final Set<String> paramOptions = new HashSet<String>(){
    	    {
    	        addAll(Arrays.asList(
    	        SPLASH,
    	        XLOGGC,
    	        JAVAAGENT,
    	        AGENTLIB,
    	        AGENTPATH,
    	        BOOTCP,
    	        BOOTCPAPPEND,
    	        BOOTCPPREPEND
    	        ));
    	    }
    	};
    	
    	private static final Pattern memOptions = Pattern.compile("X(m[sx]|ss)\\d+[gGmMkK]");
    	
    	private static boolean isParamOption(final String text) {
    		for (String option : paramOptions) {
    		    if (text.startsWith(option+':')) {
    		    	return true;
    		    }
    		}
    		return false;
    	}
    	
    	//xxx: Wrong! Should use TreeGrammer and not to populate customizer with custom nodes
    	//Should be rewritten but I have no time for this
    	public List<JavaVMOption<?>> parse() {
            Set<JavaVMOption<?>> result = new HashSet<JavaVMOption<?>>(); 
            try {
                vmOptions_return options_return = vmOptions();
                CommonTree root = options_return.tree;
                if (root instanceof JavaVMOption<?>) {
                    result.add((JavaVMOption<?>) root);
                } else if (root != null) {
                    result.addAll(root.getChildren());
                }                                       
            } catch (RecognitionException e) {
                e.printStackTrace();
            }
            result.addAll(getAllOptions());
            return new LinkedList<JavaVMOption<?>>(result); 
        }


        private static enum Kind {
            SWITCH, D, LOOSEPARAM, EQPARAM, COLUMNPARAM, FOLLOWED
        }


        private static class OptionDefinition {
            private OptionDefinition(String name, Kind kind) {
                this.kind = kind;
                this.name = name;
            }

            Kind kind;
            String name;
        }

        private static OptionDefinition[] optionsTemplates = {
                new OptionDefinition("client", Kind.SWITCH),
                new OptionDefinition("server", Kind.SWITCH),
                new OptionDefinition("ea", Kind.SWITCH),
                new OptionDefinition("da", Kind.SWITCH),
                new OptionDefinition("esa", Kind.SWITCH),
                new OptionDefinition("dsa", Kind.SWITCH),
                new OptionDefinition("verbose", Kind.SWITCH),
                new OptionDefinition("verbose:class", Kind.SWITCH),
                new OptionDefinition("verbose:jni", Kind.SWITCH),
                new OptionDefinition("verbose:gc", Kind.SWITCH),
                new OptionDefinition("version", Kind.SWITCH),
                new OptionDefinition("version", Kind.COLUMNPARAM),
                new OptionDefinition("showversion", Kind.SWITCH),
                new OptionDefinition("Xint", Kind.SWITCH),
                new OptionDefinition("Xbatch", Kind.SWITCH),
                new OptionDefinition("Xcheck:jni", Kind.SWITCH),
                new OptionDefinition("Xfuture", Kind.SWITCH),
                new OptionDefinition("Xnoclassgc", Kind.SWITCH),
                new OptionDefinition("Xincgc", Kind.SWITCH),
                new OptionDefinition("Xprof", Kind.SWITCH),
                new OptionDefinition("Xrs", Kind.SWITCH),
                new OptionDefinition("Xshare:off", Kind.SWITCH),
                new OptionDefinition("Xshare:on", Kind.SWITCH),
                new OptionDefinition("Xshare:auto", Kind.SWITCH),
                new OptionDefinition("jre-restrict-search", Kind.SWITCH),
                new OptionDefinition("jre-no-restrict-search", Kind.SWITCH),
                new OptionDefinition("Xmx", Kind.FOLLOWED),
                new OptionDefinition("Xms", Kind.FOLLOWED),
                new OptionDefinition("Xss", Kind.FOLLOWED),
                new OptionDefinition("splash", Kind.COLUMNPARAM),
                new OptionDefinition("javaagent", Kind.COLUMNPARAM),
                new OptionDefinition("agentlib", Kind.COLUMNPARAM),
                new OptionDefinition("agentpath", Kind.COLUMNPARAM),
        };

        public static List<JavaVMOption<?>> getAllOptions() {
            List<JavaVMOption<?>> result = new LinkedList<JavaVMOption<?>>();
            for (OptionDefinition optionsTemplate : optionsTemplates) {
                result.add(createOption(optionsTemplate));
            }
            return result;
        }

        private static JavaVMOption<?> createOption(OptionDefinition definition) {
            switch (definition.kind) {
                case SWITCH:
                    return new SwitchNode(definition.name);
                case D:
                    return new UserPropertyNode();
                case FOLLOWED:
                    return new ParametrizedNode(definition.name, "");
                case COLUMNPARAM:
                    return new ParametrizedNode(definition.name, ":");
                case EQPARAM:
                    return new ParametrizedNode(definition.name, "=");
                case LOOSEPARAM:
                    return new ParametrizedNode(definition.name, " ");
                default:
                    throw new IllegalArgumentException("Invalid definition.");
            }
        }


    public static class vmOptions_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "vmOptions"
    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:251:1: vmOptions : ( ( WS )? option )* ( WS )? -> ( option )* ;
    public final CommandLineParser.vmOptions_return vmOptions() throws RecognitionException {
        CommandLineParser.vmOptions_return retval = new CommandLineParser.vmOptions_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS1=null;
        Token WS3=null;
        CommandLineParser.option_return option2 = null;


        CommonTree WS1_tree=null;
        CommonTree WS3_tree=null;
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleSubtreeStream stream_option=new RewriteRuleSubtreeStream(adaptor,"rule option");
        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:2: ( ( ( WS )? option )* ( WS )? -> ( option )* )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:4: ( ( WS )? option )* ( WS )?
            {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:4: ( ( WS )? option )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==WS) ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1==TEXT||LA2_1==7) ) {
                        alt2=1;
                    }


                }
                else if ( (LA2_0==TEXT||LA2_0==7) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:5: ( WS )? option
            	    {
            	    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:5: ( WS )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==WS) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:5: WS
            	            {
            	            WS1=(Token)match(input,WS,FOLLOW_WS_in_vmOptions75);  
            	            stream_WS.add(WS1);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_option_in_vmOptions77);
            	    option2=option();

            	    state._fsp--;

            	    stream_option.add(option2.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:16: ( WS )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==WS) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:16: WS
                    {
                    WS3=(Token)match(input,WS,FOLLOW_WS_in_vmOptions80);  
                    stream_WS.add(WS3);


                    }
                    break;

            }



            // AST REWRITE
            // elements: option
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 252:20: -> ( option )*
            {
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:252:23: ( option )*
                while ( stream_option.hasNext() ) {
                    adaptor.addChild(root_0, stream_option.nextTree());

                }
                stream_option.reset();

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "vmOptions"

    public static class option_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "option"
    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:254:1: option : ( '-' switchOption -> switchOption | nonSwitchOption -> nonSwitchOption );
    public final CommandLineParser.option_return option() throws RecognitionException {
        CommandLineParser.option_return retval = new CommandLineParser.option_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal4=null;
        CommandLineParser.switchOption_return switchOption5 = null;

        CommandLineParser.nonSwitchOption_return nonSwitchOption6 = null;


        CommonTree char_literal4_tree=null;
        RewriteRuleTokenStream stream_7=new RewriteRuleTokenStream(adaptor,"token 7");
        RewriteRuleSubtreeStream stream_switchOption=new RewriteRuleSubtreeStream(adaptor,"rule switchOption");
        RewriteRuleSubtreeStream stream_nonSwitchOption=new RewriteRuleSubtreeStream(adaptor,"rule nonSwitchOption");
        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:254:8: ( '-' switchOption -> switchOption | nonSwitchOption -> nonSwitchOption )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==7) ) {
                alt4=1;
            }
            else if ( (LA4_0==TEXT) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:254:10: '-' switchOption
                    {
                    char_literal4=(Token)match(input,7,FOLLOW_7_in_option95);  
                    stream_7.add(char_literal4);

                    pushFollow(FOLLOW_switchOption_in_option97);
                    switchOption5=switchOption();

                    state._fsp--;

                    stream_switchOption.add(switchOption5.getTree());


                    // AST REWRITE
                    // elements: switchOption
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 254:27: -> switchOption
                    {
                        adaptor.addChild(root_0, stream_switchOption.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:255:3: nonSwitchOption
                    {
                    pushFollow(FOLLOW_nonSwitchOption_in_option107);
                    nonSwitchOption6=nonSwitchOption();

                    state._fsp--;

                    stream_nonSwitchOption.add(nonSwitchOption6.getTree());


                    // AST REWRITE
                    // elements: nonSwitchOption
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 255:20: -> nonSwitchOption
                    {
                        adaptor.addChild(root_0, stream_nonSwitchOption.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "option"

    public static class switchOption_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchOption"
    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:257:1: switchOption : ({...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT '=' eText -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT WS eText -> | t= TEXT ->);
    public final CommandLineParser.switchOption_return switchOption() throws RecognitionException {
        CommandLineParser.switchOption_return retval = new CommandLineParser.switchOption_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token t=null;
        Token char_literal7=null;
        Token WS9=null;
        CommandLineParser.eText_return eText8 = null;

        CommandLineParser.eText_return eText10 = null;


        CommonTree t_tree=null;
        CommonTree char_literal7_tree=null;
        CommonTree WS9_tree=null;
        RewriteRuleTokenStream stream_WS=new RewriteRuleTokenStream(adaptor,"token WS");
        RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");
        RewriteRuleTokenStream stream_8=new RewriteRuleTokenStream(adaptor,"token 8");
        RewriteRuleSubtreeStream stream_eText=new RewriteRuleSubtreeStream(adaptor,"rule eText");

        	int index = 0;
        	String name = null;
        	String value = null;

        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:263:2: ({...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT '=' eText -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT WS eText -> | t= TEXT ->)
            int alt5=10;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:263:4: {...}? =>t= TEXT
                    {
                    if ( !((switchOptions.contains(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "switchOption", "switchOptions.contains(input.LT(1).getText())");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption135);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 263:78: ->
                    {
                        adaptor.addChild(root_0, new SwitchNode(t));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:264:3: {...}? =>t= TEXT
                    {
                    if ( !((VERBOSE.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERBOSE+':'))) ) {
                        throw new FailedPredicateException(input, "switchOption", "VERBOSE.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERBOSE+':')");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption166);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 264:107: ->
                    {
                        adaptor.addChild(root_0, new SwitchNode(t));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:265:3: {...}? =>t= TEXT
                    {
                    if ( !((VERSION.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERSION+':'))) ) {
                        throw new FailedPredicateException(input, "switchOption", "VERSION.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERSION+':')");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption186);  
                    stream_TEXT.add(t);

                    index = t.getText().indexOf(':'); if (index > 0) {name=t.getText().substring(0,index); value = (index+1) == t.getText().length() ? "" : t.getText().substring(index+1);} else {name=t.getText();} 


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 265:304: ->
                    {
                        adaptor.addChild(root_0,  index < 0 ? new SwitchNode(t) : new ParametrizedNode(t, name, ":", value));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:266:3: {...}? =>t= TEXT
                    {
                    if ( !((input.LT(1).getText().startsWith(XSHARE+':'))) ) {
                        throw new FailedPredicateException(input, "switchOption", "input.LT(1).getText().startsWith(XSHARE+':')");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption203);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 266:60: ->
                    {
                        adaptor.addChild(root_0, new SwitchNode(t));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 5 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:267:3: {...}? =>t= TEXT
                    {
                    if ( !((input.LT(1).getText().startsWith(XCJNI+':'))) ) {
                        throw new FailedPredicateException(input, "switchOption", "input.LT(1).getText().startsWith(XCJNI+':')");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption218);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 267:61: ->
                    {
                        adaptor.addChild(root_0, new SwitchNode(t));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 6 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:268:3: {...}? =>t= TEXT '=' eText
                    {
                    if ( !((input.LT(1).getText().charAt(0) == 'D')) ) {
                        throw new FailedPredicateException(input, "switchOption", "input.LT(1).getText().charAt(0) == 'D'");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption235);  
                    stream_TEXT.add(t);

                    char_literal7=(Token)match(input,8,FOLLOW_8_in_switchOption237);  
                    stream_8.add(char_literal7);

                    pushFollow(FOLLOW_eText_in_switchOption239);
                    eText8=eText();

                    state._fsp--;

                    stream_eText.add(eText8.getTree());


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 268:67: ->
                    {
                        adaptor.addChild(root_0, new UserPropertyNode(t, (eText8!=null?input.toString(eText8.start,eText8.stop):null), (t!=null?t.getCharPositionInLine():0)));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 7 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:269:3: {...}? =>t= TEXT
                    {
                    if ( !((isParamOption(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "switchOption", "isParamOption(input.LT(1).getText())");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption257);  
                    stream_TEXT.add(t);

                    index = t.getText().indexOf(':'); if (index > 0) {name=t.getText().substring(0,index); value = (index+1) == t.getText().length() ? "" : t.getText().substring(index+1);}


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 269:227: ->
                    {
                        adaptor.addChild(root_0, new ParametrizedNode(t, name, ":", value));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 8 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:270:3: {...}? =>t= TEXT
                    {
                    if ( !((memOptions.matcher(input.LT(1).getText()).matches())) ) {
                        throw new FailedPredicateException(input, "switchOption", "memOptions.matcher(input.LT(1).getText()).matches()");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption274);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 270:69: ->
                    {
                        adaptor.addChild(root_0, new ParametrizedNode(t, 3));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 9 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:271:3: {...}? =>t= TEXT WS eText
                    {
                    if ( !((CLASSPATH.equals(input.LT(1).getText()) || CLASSPATH_LONG.equals(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "switchOption", "CLASSPATH.equals(input.LT(1).getText()) || CLASSPATH_LONG.equals(input.LT(1).getText())");
                    }
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption291);  
                    stream_TEXT.add(t);

                    WS9=(Token)match(input,WS,FOLLOW_WS_in_switchOption293);  
                    stream_WS.add(WS9);

                    pushFollow(FOLLOW_eText_in_switchOption295);
                    eText10=eText();

                    state._fsp--;

                    stream_eText.add(eText10.getTree());


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 271:112: ->
                    {
                        adaptor.addChild(root_0, new ParametrizedNode(t, " ", (eText10!=null?input.toString(eText10.start,eText10.stop):null), false));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 10 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:272:3: t= TEXT
                    {
                    t=(Token)match(input,TEXT,FOLLOW_TEXT_in_switchOption307);  
                    stream_TEXT.add(t);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 272:10: ->
                    {
                        adaptor.addChild(root_0, new UnrecognizedOption(t));

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "switchOption"

    public static class eText_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "eText"
    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:274:1: eText : ( '\\'' TEXT '\\'' | '\"' TEXT '\"' | TEXT );
    public final CommandLineParser.eText_return eText() throws RecognitionException {
        CommandLineParser.eText_return retval = new CommandLineParser.eText_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal11=null;
        Token TEXT12=null;
        Token char_literal13=null;
        Token char_literal14=null;
        Token TEXT15=null;
        Token char_literal16=null;
        Token TEXT17=null;

        CommonTree char_literal11_tree=null;
        CommonTree TEXT12_tree=null;
        CommonTree char_literal13_tree=null;
        CommonTree char_literal14_tree=null;
        CommonTree TEXT15_tree=null;
        CommonTree char_literal16_tree=null;
        CommonTree TEXT17_tree=null;

        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:274:7: ( '\\'' TEXT '\\'' | '\"' TEXT '\"' | TEXT )
            int alt6=3;
            switch ( input.LA(1) ) {
            case 9:
                {
                alt6=1;
                }
                break;
            case 10:
                {
                alt6=2;
                }
                break;
            case TEXT:
                {
                alt6=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:275:3: '\\'' TEXT '\\''
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal11=(Token)match(input,9,FOLLOW_9_in_eText324); 
                    char_literal11_tree = (CommonTree)adaptor.create(char_literal11);
                    adaptor.addChild(root_0, char_literal11_tree);

                    TEXT12=(Token)match(input,TEXT,FOLLOW_TEXT_in_eText326); 
                    TEXT12_tree = (CommonTree)adaptor.create(TEXT12);
                    adaptor.addChild(root_0, TEXT12_tree);

                    char_literal13=(Token)match(input,9,FOLLOW_9_in_eText328); 
                    char_literal13_tree = (CommonTree)adaptor.create(char_literal13);
                    adaptor.addChild(root_0, char_literal13_tree);


                    }
                    break;
                case 2 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:276:4: '\"' TEXT '\"'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal14=(Token)match(input,10,FOLLOW_10_in_eText333); 
                    char_literal14_tree = (CommonTree)adaptor.create(char_literal14);
                    adaptor.addChild(root_0, char_literal14_tree);

                    TEXT15=(Token)match(input,TEXT,FOLLOW_TEXT_in_eText335); 
                    TEXT15_tree = (CommonTree)adaptor.create(TEXT15);
                    adaptor.addChild(root_0, TEXT15_tree);

                    char_literal16=(Token)match(input,10,FOLLOW_10_in_eText338); 
                    char_literal16_tree = (CommonTree)adaptor.create(char_literal16);
                    adaptor.addChild(root_0, char_literal16_tree);


                    }
                    break;
                case 3 :
                    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:277:8: TEXT
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    TEXT17=(Token)match(input,TEXT,FOLLOW_TEXT_in_eText347); 
                    TEXT17_tree = (CommonTree)adaptor.create(TEXT17);
                    adaptor.addChild(root_0, TEXT17_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eText"

    public static class nonSwitchOption_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nonSwitchOption"
    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:279:1: nonSwitchOption : t= TEXT ->;
    public final CommandLineParser.nonSwitchOption_return nonSwitchOption() throws RecognitionException {
        CommandLineParser.nonSwitchOption_return retval = new CommandLineParser.nonSwitchOption_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token t=null;

        CommonTree t_tree=null;
        RewriteRuleTokenStream stream_TEXT=new RewriteRuleTokenStream(adaptor,"token TEXT");

        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:280:2: (t= TEXT ->)
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:280:4: t= TEXT
            {
            t=(Token)match(input,TEXT,FOLLOW_TEXT_in_nonSwitchOption359);  
            stream_TEXT.add(t);



            // AST REWRITE
            // elements: 
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 280:11: ->
            {
                adaptor.addChild(root_0, new UnknownOption(t));

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nonSwitchOption"

    // Delegated rules


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\14\uffff";
    static final String DFA5_eofS =
        "\14\uffff";
    static final String DFA5_minS =
        "\1\5\1\4\12\uffff";
    static final String DFA5_maxS =
        "\1\5\1\10\12\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\6\1\11\1\1\1\2\1\3\1\4\1\5\1\7\1\10\1\12";
    static final String DFA5_specialS =
        "\1\uffff\1\0\12\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1",
            "\1\3\3\uffff\1\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "257:1: switchOption : ({...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT '=' eText -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT -> | {...}? =>t= TEXT WS eText -> | t= TEXT ->);";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA5_1 = input.LA(1);

                         
                        int index5_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA5_1==8) && ((input.LT(1).getText().charAt(0) == 'D'))) {s = 2;}

                        else if ( (LA5_1==WS) && ((CLASSPATH.equals(input.LT(1).getText()) || CLASSPATH_LONG.equals(input.LT(1).getText())))) {s = 3;}

                        else if ( ((switchOptions.contains(input.LT(1).getText()))) ) {s = 4;}

                        else if ( ((VERBOSE.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERBOSE+':'))) ) {s = 5;}

                        else if ( ((VERSION.equals(input.LT(1).getText()) || input.LT(1).getText().startsWith(VERSION+':'))) ) {s = 6;}

                        else if ( ((input.LT(1).getText().startsWith(XSHARE+':'))) ) {s = 7;}

                        else if ( ((input.LT(1).getText().startsWith(XCJNI+':'))) ) {s = 8;}

                        else if ( ((isParamOption(input.LT(1).getText()))) ) {s = 9;}

                        else if ( ((memOptions.matcher(input.LT(1).getText()).matches())) ) {s = 10;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index5_1);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 5, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_WS_in_vmOptions75 = new BitSet(new long[]{0x00000000000000A0L});
    public static final BitSet FOLLOW_option_in_vmOptions77 = new BitSet(new long[]{0x00000000000000B2L});
    public static final BitSet FOLLOW_WS_in_vmOptions80 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_7_in_option95 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_switchOption_in_option97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonSwitchOption_in_option107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption235 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_8_in_switchOption237 = new BitSet(new long[]{0x0000000000000620L});
    public static final BitSet FOLLOW_eText_in_switchOption239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption291 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_WS_in_switchOption293 = new BitSet(new long[]{0x0000000000000620L});
    public static final BitSet FOLLOW_eText_in_switchOption295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_switchOption307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_9_in_eText324 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_TEXT_in_eText326 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_9_in_eText328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_10_in_eText333 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_TEXT_in_eText335 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_eText338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_eText347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_nonSwitchOption359 = new BitSet(new long[]{0x0000000000000002L});

}