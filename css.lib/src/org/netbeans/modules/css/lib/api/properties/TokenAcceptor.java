/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class TokenAcceptor {

    public static final Collection<TokenAcceptor> ACCEPTORS = 
            new ArrayList<TokenAcceptor>();
    public static final Map<String, TokenAcceptor> ACCEPTORS_MAP = 
            new LinkedHashMap<String, TokenAcceptor> ();
    private static Lookup INSTANCES;

    static {
        ACCEPTORS.add(new Resolution("resolution"));
        ACCEPTORS.add(new Angle("angle"));
        ACCEPTORS.add(new Percentage("percentage"));
        ACCEPTORS.add(new Length("length"));
        ACCEPTORS.add(new HashColor("hash_color_code"));
        ACCEPTORS.add(new HashColorAplha("hash_color_alpha_code")); //javafx
        ACCEPTORS.add(new StringAcceptor("string"));
        ACCEPTORS.add(new NonNegativeInteger("non-negative-integer"));
        ACCEPTORS.add(new Integer("integer"));
        ACCEPTORS.add(new Number("number"));
        ACCEPTORS.add(new Identifier("identifier"));
        ACCEPTORS.add(new Time("time"));
        ACCEPTORS.add(new Date("date"));
        ACCEPTORS.add(new Frequency("frequency"));
        ACCEPTORS.add(new Semitones("semitones"));
        ACCEPTORS.add(new Decibel("decibel"));
        ACCEPTORS.add(new RelativeLength("relative-length"));
        ACCEPTORS.add(new Uri("uri"));
        ACCEPTORS.add(new Anything("anything")); 
        
        ACCEPTORS.add(new GenericFunctionContent("function-content")); 
        
        InstanceContent content = new InstanceContent();
        for(TokenAcceptor ta : ACCEPTORS) {
            ACCEPTORS_MAP.put(ta.id(), ta);
            content.add(ta);
        }
        INSTANCES = new AbstractLookup(content);
        
    } //NOI18N
    
    public static <T extends TokenAcceptor> T getAcceptor(Class<T> acceptorType) {
        return INSTANCES.lookup(acceptorType);
    }
    
    public static TokenAcceptor getAcceptor(String name) {
        return ACCEPTORS_MAP.get(name.toLowerCase());
    }

    private String id;

    public TokenAcceptor(String id) {
        this.id = id;
    }
    
    public final String id() {
        return id;
    }
    
    public abstract boolean accepts(Token token);
    
    Collection<String> getFixedImageTokens() {
        return null;
    }
    
    public static class Resolution extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"dpi", "dppx", "dpcm"}); //NOI18N

        public Resolution(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class Angle extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"deg", "rad", "grad", "turn"}); //NOI18N

        public Angle(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class Anything extends TokenAcceptor {

        public Anything(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            return true;
        }
    }

    public static class Date extends TokenImageAcceptor {

        public Date(String id) {
            super(id);
        }

        @Override
        public boolean accepts(String token) {
            try {
                DateFormat.getDateInstance().parse(token);
                return true;
            } catch (ParseException ex) {
                return false;
            }
        }
    }

    public static class Decibel extends NumberPostfixAcceptor {

        public Decibel(String id) {
            super(id);
        }

        private static final List<String> POSTFIXES = Collections.singletonList("dB"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class Frequency extends NumberPostfixAcceptor {

        public Frequency(String id) {
            super(id);
        }

        private static final List<String> POSTFIXES = Arrays.asList("khz", "hz"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }

    }

    public static class HashColor extends TokenAcceptor {

        public HashColor(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            int len = token.image().length();
            return token.tokenId() == CssTokenId.HASH && (len == 4 || len == 7); //three of six hex digits after hash sign are allowed
        }
    }

    public static class HashColorAplha extends TokenAcceptor {

        public HashColorAplha(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            int len = token.image().length();
            return token.tokenId() == CssTokenId.HASH && len == 9; //#aabbccDD -- DD is aplha
        }
    }

    public static class Identifier extends TokenAcceptor {

        public Identifier(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            return token.tokenId() == CssTokenId.IDENT 
                    && !LexerUtils.equals("inherit",token.image(), true, true); //hack! XXX fix!!!
        }
    }

    public static class Integer extends TokenImageAcceptor {

        public Integer(String id) {
            super(id);
        }

        @Override
        public boolean accepts(String token) {
            return getNumberValue(token) != java.lang.Integer.MIN_VALUE;
        }
        
        public int getNumberValue(String token) {
            try {
                return java.lang.Integer.parseInt(token);
            } catch (NumberFormatException nfe) {
            }
            return java.lang.Integer.MIN_VALUE;
        }
    }

    public static class Length extends NumberPostfixAcceptor {

        /*
         *
         * relative units: em: the 'font-size' of the relevant font ex: the
         * 'x-height' of the relevant font px: pixels, relative to the viewing
         * device gd the grid defined by 'layout-grid' described in the CSS3
         * Text module [CSS3TEXT] rem the font size of the root element vw the
         * viewport's width vh the viewport's height vm the viewport's height or
         * width, whichever is smaller of the two ch The width of the "0" (ZERO,
         * U+0030) glyph found in the font for the font size used to render. If
         * the "0" glyph is not found in the font, the average character width
         * may be used. How is the "average character width" found?
         *
         * absolute units:
         *
         * in: inches -- 1 inch is equal to 2.54 centimeters. cm: centimeters
         * mm: millimeters pt: points -- the points used by CSS2 are equal to
         * 1/72th of an inch. pc: picas -- 1 pica is equal to 12 points.
         */
        
        //!!! if a longer postfix has sub-postfix which equals to any of the shortest
        //postfixes, then it needs to be before the sub-postfix postfix (e.g. rem - em)!!!
        private static final List<String> POSTFIXES = Arrays.asList(new String[]{
            "rem", "vmin", "vmax", "ex", "em", "vw", "vh", "ch",
            "cm", "mm", "in", "pt", "pc", "px"}); //NOI18N

        public Length(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }

        //http://www.w3.org/TR/css3-values/#lengths
        //Lengths refer to distance measurements and are denoted by <length> 
        //in the property definitions. A length is a dimension. A zero length 
        //may be represented instead as the <number> ‘0’. (In other words, 
        //for zero lengths the unit identifier is optional.) 
        @Override
        public boolean accepts(String text) {
            boolean sa = super.accepts(text);
            if (!sa) {
                return "0".equals(text); //NOI18N
            } else {
                return sa;
            }
        }

        @Override
        public Float getNumberValue(CharSequence image) {
            Float f = super.getNumberValue(image);
            if(f == null) {
                if(image.length() > 0 && image.charAt(0) == '0') {
                    f = new Float(0);
                }
            } 
            return f;
        }
        
        
    }

    public static class NonNegativeInteger extends TokenImageAcceptor {

        public NonNegativeInteger(String id) {
            super(id);
        }

        @Override
        public boolean accepts(String token) {
            return getNumberValue(token) != -1;
        }
        
        public int getNumberValue(String token) {
            try {
                int i = java.lang.Integer.parseInt(token);
                if(i >= 0) {
                    return i;
                }
            } catch (NumberFormatException nfe) {
            }
            return -1;
        }
        
        
    }

    public static class Number extends TokenImageAcceptor {

        public Number(String id) {
            super(id);
        }

        @Override
        public boolean accepts(String token) {
            return getNumberValue(token) != null;
        }
        
        public Float getNumberValue(String token) {
            try {
                return Float.parseFloat(token);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    public abstract static class NumberPostfixAcceptor extends TokenImageAcceptor {

        public NumberPostfixAcceptor(String id) {
            super(id);
        }

        /**
         * Please be aware that if there are prefixes that ends with another prefix image
         * the order must be: the longer prefix sooner! (khz, hz)
         */
        protected abstract List<String> postfixes();

        @Override
        public boolean accepts(String image) {
            return getNumberValue(image) != null;
        }
        
        public CharSequence getPostfix(CharSequence image) {
            for (String postfix : postfixes()) {
                if(LexerUtils.endsWith(image, postfix, true, false)) {
                    return postfix;
                }
            }
            return null;
        }
        
        public Float getNumberValue(CharSequence image) {
            CharSequence postfix = getPostfix(image);
            if(postfix == null) {
                return null;
            }
            
            CharSequence numberImage = image.subSequence(0, image.length() - postfix.length());
            try {
                return Float.parseFloat(numberImage.toString());
            } catch (NumberFormatException nfe) {
                return null;
            }

        }

    }

    public static class Percentage extends NumberPostfixAcceptor {
        
        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"%"}); //NOI18N

        public Percentage(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class RelativeLength extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Collections.singletonList("*"); //NOI18N

        public RelativeLength(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class Semitones extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Collections.singletonList("st"); //NOI18N

        public Semitones(String id) {
            super(id);
        }

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    public static class StringAcceptor extends TokenImageAcceptor {

        public StringAcceptor(String id) {
            super(id);
        }

        @Override
        public boolean accepts(String token) {
            return getUnquotedValue(token) != null;
        }
        
        public String getUnquotedValue(String token) {
            if (token.length() < 2) {
                return null;
            }
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);

            if((first == '\'' && last == '\'') || (first == '"' && last == '"')) { //NOI18N
                return token.substring(1, token.length() - 1);
            }

            return null;
        }
    }

    public static class Time extends NumberPostfixAcceptor {

        public Time(String id) {
            super(id);
        }

        private static final List<String> POSTFIXES = Arrays.asList("ms", "s"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
        
    }

    public static abstract class TokenImageAcceptor extends TokenAcceptor {

        public TokenImageAcceptor(String id) {
            super(id);
        }

        public abstract boolean accepts(String valueImage);

        @Override
        public boolean accepts(Token token) {
            String tokenImage = token.image().toString();
            return accepts(tokenImage);
        }

    }

    public static class Uri extends TokenAcceptor {

        private static final String URL_TOKEN_IMAGE = "url";
        
        public Uri(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            return token.tokenId() == CssTokenId.URI;
        }

        @Override
        Collection<String> getFixedImageTokens() {
            return Collections.singleton(URL_TOKEN_IMAGE);
        }
        
    }
    
    private static class GenericFunctionContent extends TokenAcceptor {

        public GenericFunctionContent(String id) {
            super(id);
        }

        @Override
        public boolean accepts(Token token) {
            //consume everything except ")"
            return token.tokenId() != CssTokenId.RPAREN;
        }
        
    }
}
