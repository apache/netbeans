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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.Context;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.JavadocHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class ParameterNameProviderImpl {
    public static boolean DISABLE_ARTIFICAL_PARAMETER_NAMES;

    public static void register(JavacTask task, ClasspathInfo cpInfo) {
        try {
            Class<?> c = Class.forName("com.sun.source.util.ParameterNameProvider");
            ParameterNameProviderImpl impl = new ParameterNameProviderImpl(cpInfo, task);
            InvocationHandler h = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("getParameterName")) {
                        return impl.getParameterName((VariableElement) args[0]);
                    }
                    return null;
                }
            };
            Object proxy = Proxy.newProxyInstance(ParameterNameProviderImpl.class.getClassLoader(), new Class[] {c}, h);
            JavacTask.class.getDeclaredMethod("setParameterNameProvider", c).invoke(task, proxy);
        } catch (ClassNotFoundException ex) {
            //ok
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

            static final int MAX_CACHE_SIZE = 100;
    private static final LinkedHashMap<String, Map<String, List<String>>> source_toplevelClass2method2Parameters = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Map<String, List<String>>> javadoc_class2method2Parameters = new LinkedHashMap<>();
    private static final LinkedHashMap<String, List<String>> artificial_method2Parameters = new LinkedHashMap<>();

    private final ClasspathInfo cpInfo;
    private final JavacTask task;

    public ParameterNameProviderImpl(ClasspathInfo cpInfo, JavacTask task) {
        this.cpInfo = cpInfo;
        this.task = task;
    }

    public CharSequence getParameterName(VariableElement parameter) {
        Element method = parameter.getEnclosingElement();
        String methodKey = computeKey(method);
        List<String> names;

        //from sources:
        {
        Element topLevel = parameter;
        while (topLevel.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            topLevel = topLevel.getEnclosingElement();
        }
        ElementHandle<?> topLevelHandle = ElementHandle.create(topLevel);

        names = source_toplevelClass2method2Parameters.computeIfAbsent(computeKey(topLevel), d -> {
            Map<String, List<String>> parametersInClass = new HashMap<>();
            FileObject source = SourceUtils.getFile(topLevelHandle, cpInfo);
            JavaSource javaSource = source != null ? JavaSource.forFileObject(source) : null;
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        new TreePathScanner<Void, Void>() {
                            public Void visitMethod(MethodTree mt, Void v) {
                                Element el = cc.getTrees().getElement(getCurrentPath());
                                if (el != null && el.getKind() == ElementKind.METHOD) {
                                    parametersInClass.put(computeKey(el), ((ExecutableElement) el).getParameters().stream().map(p -> p.getSimpleName().toString()).collect(Collectors.toList()));
                                }
                                return super.visitMethod(mt, v);
                            }
                        }.scan(cc.getCompilationUnit(), null);
                    }, true);
                } catch (IOException ex) {
                    //ignore
                }
            }
            return parametersInClass;
        }).get(methodKey);
        }

        if (names == null) {
            Element clazzCandidate = method.getEnclosingElement();
            if (clazzCandidate != null && (clazzCandidate.getKind().isClass() || clazzCandidate.getKind().isInterface())) {
                TypeElement clazz = (TypeElement) clazzCandidate;
                names = javadoc_class2method2Parameters.computeIfAbsent(clazz.getQualifiedName().toString(), clz -> {
                    Map<String, List<String>> result = new HashMap<>();
                    fillInParameterNamesFromJavadoc(((JavacTaskImpl) task).getContext(), clazz, (m, n) -> {
                        result.put(computeKey(m), n);
                    });
                    return result;
                }).get(methodKey);
            }
        }

        if (names == null) {
            if (!DISABLE_ARTIFICAL_PARAMETER_NAMES) {
                names = artificial_method2Parameters.computeIfAbsent(methodKey, mk -> {
                    Set<String> usedNames = new HashSet<>();
                    return ((ExecutableElement) method).getParameters().stream().map(p -> generateReadableParameterName(p.asType().toString(), usedNames)).collect(Collectors.toList());
                });
            } else {
                names = Collections.emptyList();
            }
        }

        capCache(source_toplevelClass2method2Parameters);
        capCache(javadoc_class2method2Parameters);
        capCache(artificial_method2Parameters);

        int idx = ((ExecutableElement) method).getParameters().indexOf(parameter);
        return idx != (-1) && idx < names.size() ? names.get(idx) : null;
    }

    private static String computeKey(Element el) {
        return Arrays.stream(SourceUtils.getJVMSignature(ElementHandle.create(el))).collect(Collectors.joining(":"));
    }

    static void capCache(LinkedHashMap<String, ?> map) {
        Iterator<String> it = map.keySet().iterator();
        while (map.size() > MAX_CACHE_SIZE) {
            it.next();
            it.remove();
        }
    }

    //from javadoc:
    private static final boolean ALWAYS_ALLOW_JDOC_ARG_NAMES = Boolean.getBoolean("java.source.args.from.http.jdoc");  //NOI18N

    public static boolean fillInParameterNamesFromJavadoc(Context ctx, TypeElement type, BiConsumer<ExecutableElement, List<String>> setNamesToMethod) {
        JavadocHelper.TextStream page = JavadocHelper.getJavadoc(type, ALWAYS_ALLOW_JDOC_ARG_NAMES, null);
        if (page != null && (!page.isRemote() || !EventQueue.isDispatchThread())) {
            getParamNamesFromJavadocText(ctx, page, (ClassSymbol) type, setNamesToMethod);
            return true;
        }
        return false;
    }

    private static boolean getParamNamesFromJavadocText(Context context, final JavadocHelper.TextStream page, ClassSymbol clazz, BiConsumer<ExecutableElement, List<String>> setNamesToMethod) {
        HTMLEditorKit.Parser parser;
        InputStream is = null;
        String charset = null;
        for (;;) {
            try{
                is = page.openStream();
                Reader reader = charset == null ? new InputStreamReader(is): new InputStreamReader(is, charset);
                parser = new ParserDelegator();
                parser.parse(reader, new HTMLEditorKit.ParserCallback() {

                    private int state = 0; //init
                    private String signature = null;
                    private StringBuilder sb = null;

                    @Override
                    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.A) {
                            String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                            if (attrName != null && ctor_summary_name.matcher(attrName).matches()) {
                                // we have found desired javadoc constructor info anchor
                                state = 10; //ctos open
                            } else if (attrName != null && method_summary_name.matcher(attrName).matches()) {
                                // we have found desired javadoc method info anchor
                                state = 20; //methods open
                            } else if (attrName != null && field_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (attrName != null && ctor_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (attrName != null && method_detail_name.matcher(attrName).matches()) {
                                state = 30; //end
                            } else if (state == 12 || state == 22) {
                                String attrHref = (String)a.getAttribute(HTML.Attribute.HREF);
                                if (attrHref != null) {
                                    int idx = attrHref.indexOf('#');
                                    if (idx >= 0) {
                                        signature = attrHref.substring(idx + 1);
                                        sb = new StringBuilder();
                                    }
                                }
                            }
                        } else if (t == HTML.Tag.TABLE) {
                            if (state == 10 || state == 20)
                                state++;
                        } else if (t == HTML.Tag.CODE) {
                            if (state == 11 || state == 21)
                                state++;
                        } else if (t == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.CLASS, "block")) { //NOI18N
                            if (state == 11 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), true);
                                signature = null;
                                sb = null;
                            } else if (state == 21 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), false);
                                signature = null;
                                sb = null;
                            }
                        }
                    }

                    @Override
                    public void handleEndTag(HTML.Tag t, int pos) {
                        if (t == HTML.Tag.CODE) {
                            if (state == 12 || state == 22)
                                state--;
                        } else if (t == HTML.Tag.TABLE) {
                            if (state == 11 || state == 21)
                                state--;
                        }
                    }

                    @Override
                    public void handleText(char[] data, int pos) {
                        if (signature != null && sb != null && (state == 12 || state == 22))
                            sb.append(data);
                    }

                    @Override
                    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                        if (t == HTML.Tag.BR) {
                            if (state == 11 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), true);
                                signature = null;
                                sb = null;
                            } else if (state == 21 && signature != null && sb != null) {
                                setParamNames(signature, sb.toString().trim(), false);
                                signature = null;
                                sb = null;
                            }
                        }
                    }

                    private void setParamNames(String signature, String names, boolean isCtor) {
                        ArrayList<String> paramTypes = new ArrayList<String>();
                        int idx = -1;
                        for(int i = 0; i < signature.length(); i++) {
                            switch(signature.charAt(i)) {
                                case '-':
                                case '(':
                                case ')':
                                case ',':
                                    if (idx > -1 && idx < i - 1) {
                                        String typeName = signature.substring(idx + 1, i).trim();
                                        if (typeName.endsWith("...")) //NOI18N
                                            typeName = typeName.substring(0, typeName.length() - 3) + "[]"; //NOI18N
                                        paramTypes.add(typeName);
                                    }
                                    idx = i;
                                    break;
                            }
                        }
                        String methodName = null;
                        ArrayList<String> paramNames = new ArrayList<String>();
                        idx = -1;
                        for(int i = 0; i < names.length(); i++) {
                            switch(names.charAt(i)) {
                                case '(':
                                    methodName = names.substring(0, i);
                                    break;
                                case ')':
                                case ',':
                                    if (idx > -1) {
                                        paramNames.add(names.substring(idx + 1, i));
                                        idx = -1;
                                    }
                                    break;
                                case 160: //&nbsp;
                                    idx = i;
                                    break;
                            }
                        }
                        assert methodName != null : "Null methodName. Signature: [" + signature + "], Names: [" + names + "]";
                        assert paramTypes.size() == paramNames.size() : "Inconsistent param types/names. Signature: [" + signature + "], Names: [" + names + "]";
                        if (paramNames.size() > 0) {
                            for (Symbol s : clazz.members().getSymbolsByName(isCtor
                                    ? clazz.name.table.names.init
                                    : clazz.name.table.fromString(methodName))) {
                                if (s.kind == Kinds.Kind.MTH && s.owner == clazz) {
                                    MethodSymbol sym = (MethodSymbol)s;
                                    com.sun.tools.javac.util.List<VarSymbol> params = sym.params;
                                    if (checkParamTypes(params, paramTypes)) {
                                        setNamesToMethod.accept(sym, paramNames);
                                    }
                                }
                            }
                        }
                    }

                    private boolean checkParamTypes(com.sun.tools.javac.util.List<VarSymbol> params, ArrayList<String> paramTypes) {
                        Types types = Types.instance(context);
                        for (String typeName : paramTypes) {
                            if (params.isEmpty())
                                return false;
                            Type type = params.head.type;
                            if (type.isParameterized())
                                type = types.erasure(type);
                            if (!typeName.equals(type.toString()))
                                return false;
                            params = params.tail;
                        }
                        return params.isEmpty();
                    }
                }, charset != null);
                return true;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = JavadocHelper.getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch (InterruptedIOException x) {
                //Http javadoc timeout
                break;
            } catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private static final Pattern ctor_summary_name = Pattern.compile("constructor[_.]summary"); //NOI18N
    private static final Pattern method_summary_name = Pattern.compile("method[_.]summary"); //NOI18N
    private static final Pattern field_detail_name = Pattern.compile("field[_.]detail"); //NOI18N
    private static final Pattern ctor_detail_name = Pattern.compile("constructor[_.]detail"); //NOI18N
    private static final Pattern method_detail_name = Pattern.compile("method[_.]detail"); //NOI18N

    private static final int MAX_LEN = 6;

    /**
     * Utility method for generating method parameter names based on incoming
     * class name when source is unavailable.
     * <p/>
     * This method uses both subjective heuristics to follow common patterns
     * for common JDK classes, acronym creation for bicapitalized names, and
     * vowel and repeated character elision if that fails, to generate
     * readable, programmer-friendly method names.
     *
     * @param typeName The fqn of the parameter class
     * @param used A set of names that have already been used for parameters
     * and should not be reused, to avoid creating uncompilable code
     * @return A programmer-friendly parameter name (i.e. not arg0, arg1...)
     *///public so that TreeLoader can use it:
    public static @NonNull String generateReadableParameterName (@NonNull String typeName, @NonNull Set<String> used) {
        boolean arr = typeName.indexOf ("[") > 0 || typeName.endsWith("..."); //NOI18N
        typeName = trimToSimpleName (typeName);
        String result = typeName.toLowerCase();
        //First, do some common, sane substitutions that are common java parlance
        if ( typeName.endsWith ( "Listener" ) ) { //NOI18N
            result = Character.toLowerCase(typeName.charAt(0)) + "l"; //NOI18N
        } else if ( "Object".equals (typeName)) { //NOI18N
            result = "o"; //NOI18N
        } else if ("Class".equals(typeName)) { //NOI18N
            result = "type"; //NOI18N
        } else if ( "InputStream".equals(typeName)) { //NOI18N
            result = "in"; //NOI18N
        } else if ( "OutputStream".equals(typeName)) {
            result = "out"; //NOI18N
        } else if ( "Runnable".equals(typeName)) {
            result = "r"; //NOI18N
        } else if ( "Lookup".equals(typeName)) {
            result = "lkp"; //NOI18N
        } else if ( typeName.endsWith ( "Stream" )) { //NOI18N
            result = "stream"; //NOI18N
        } else if ( typeName.endsWith ("Writer")) { //NOI18N
            result = "writer"; //NOI18N
        } else if ( typeName.endsWith ("Reader")) { //NOI18N
            result = "reader"; //NOI18N
        } else if ( typeName.endsWith ( "Panel" )) { //NOI18N
            result = "pnl"; //NOI18N
        } else if ( typeName.endsWith ( "Action" )) { //NOI18N
            result = "action"; //NOI18N
        }
        //Now see if we've made a large and unwieldy variable - people
        //usually prefer reasonably short but legible arguments
        if ( result.length () > MAX_LEN ) {
            //See if we can turn, say, NoClassDefFoundError into "ncdfe"
            result = tryToMakeAcronym ( typeName );
            //No luck?  We've probably got one long word like Component or Runnable
            if (result.length() > MAX_LEN) {
                //First, strip out vowels - people easily figure out words
                //missing vowels - common in abbreviations and spam mails
                result = elideVowelsAndRepetitions(result);
                if (result.length() > MAX_LEN) {
                    //Still too long?  Give up and give them a 1 character var name
                    result = new StringBuilder().append(
                            result.charAt(0)).toString().toLowerCase();
                }
            }
        }
        //Make sure we haven't killed everything - if so, use a generic version
        if ( result.trim ().length () == 0 ) {
            result = "value"; //NOI18N
        }
        //If it's an array, pluralize it (english language style - but better than nothing)
        if (arr) {
            result += "s"; //NOI18N
        }
        //Now make sure it's legal;  if not, make it a single letter
        if ( isPrimitiveTypeName ( result ) || !BaseUtilities.isJavaIdentifier ( result ) ) {
            StringBuilder sb = new StringBuilder();
            sb.append (result.charAt(0));
            result = sb.toString();
        }
        //Now make sure we're not duplicating a variable name we already used
        String test = result;
        int revs = 0;
        while ( used.contains ( test ) ) {
            revs++;
            test = result + revs;
        }
        result = test;
        used.add ( result );
        return result;
    }

    /**
     * Trims to the simple class name and removes and generics
     *
     * @param typeName The class name
     * @return A simplified class name
     */
    private static String trimToSimpleName (String typeName) {
        String result = typeName;
        int ix = result.indexOf ("<"); //NOI18N
        if (ix > 0 && ix != typeName.length() - 1) {
            result = typeName.substring(0, ix);
        }
        if (result.endsWith ("...")) { //NOI18N
            result = result.substring (0, result.length() - 3);
        }
        ix = result.lastIndexOf ("$"); //NOI18N
        if (ix > 0 && ix != result.length() - 1) {
            result = result.substring(ix + 1);
        } else {
            ix = result.lastIndexOf("."); //NOI18N
            if (ix > 0 && ix != result.length() - 1) {
                result = result.substring(ix + 1);
            }
        }
        ix = result.indexOf ( "[" ); //NOI18N
        if ( ix > 0 ) {
            result = result.substring ( 0, ix );
        }
        return result;
    }

    /**
     * Removes vowels and repeated letters.  This is used to generate names
     * where the class name a single long word - e.g. abbreviate
     * Runnable to rnbl
     * @param name The name
     * @return A shortened version of it
     */
    private static String elideVowelsAndRepetitions (String name) {
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        char last = 0;
        char lastUsed = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isDigit(c)) {
                continue;
            }
            if (i == 0 || Character.isUpperCase(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            } else if (c != last && !isVowel(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            }
            last = c;
        }
        return sb.toString();
    }

    private static boolean isVowel(char c) {
        return Arrays.binarySearch(VOWELS, c) >= 0;
    }

    /**
     * Vowels in various indo-european-based languages
     */
    private static final char[] VOWELS = new char[] {
    //IMPORTANT:  This array is sorted.  If you add to it,
    //add in the correct place or Arrays.binarySearch will break on it
    '\u0061', '\u0065', '\u0069', '\u006f', '\u0075', '\u0079', '\u00e9', '\u00ea',  //NOI18N
    '\u00e8', '\u00e1', '\u00e2', '\u00e6', '\u00e0', '\u03b1', '\u00e3',  //NOI18N
    '\u00e5', '\u00e4', '\u00eb', '\u00f3', '\u00f4', '\u0153', '\u00f2',  //NOI18N
    '\u03bf', '\u00f5', '\u00f6', '\u00ed', '\u00ee', '\u00ec', '\u03b9',  //NOI18N
    '\u00ef', '\u00fa', '\u00fb', '\u00f9', '\u03d2', '\u03c5', '\u00fc',  //NOI18N
    '\u0430', '\u043e', '\u044f', '\u0438', '\u0439', '\u0435', '\u044b',  //NOI18N
    '\u044d', '\u0443', '\u044e', };

    //PENDING:  The below would be much prettier;  whether it survives
    //cross-platform encoding issues in hg is another question;  the hg diff generated
    //was incorrect
/*
    'a', 'e', 'i', 'o', 'u', 'y', 'à', 'á', //NOI18N
    'â', 'ã', 'ä', 'å', 'æ', 'è', 'é', //NOI18N
    'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ò', //NOI18N
    'ó', 'ô', 'õ', 'ö', 'ù', 'ú', 'û', //NOI18N
    'ü', 'œ', 'α', 'ι', 'ο', 'υ', 'ϒ', //NOI18N
    'а', 'е', 'и', 'й', 'о', 'у', 'ы', //NOI18N
    'э', 'ю', 'я'}; //NOI18N
*/
    /**
     * Determine if a string matches a java primitive type.  Used in generating reasonable variable names.
     */
    private static boolean isPrimitiveTypeName (String typeName) {
        return (
                //Whoa, ascii art!
                "void".equals ( typeName ) || //NOI18N
                "int".equals ( typeName ) || //NOI18N
                "long".equals ( typeName ) || //NOI18N
                "float".equals ( typeName ) || //NOI18N
                "double".equals ( typeName ) || //NOI18N
                "short".equals ( typeName ) || //NOI18N
                "char".equals ( typeName ) || //NOI18N
                "boolean".equals ( typeName ) ); //NOI18N
    }

    /**
     * Try to create an acronym-style variable name from a string - i.e.,
     * "JavaDataObject" becomes "jdo".
     */
    private static String tryToMakeAcronym (String s) {
        char[] c = s.toCharArray ();
        StringBuilder sb = new StringBuilder ();
        for ( int i = 0; i < c.length; i++ ) {
            if ( Character.isUpperCase (c[i])) {
                sb.append ( c[ i ] );
            }
        }
        if ( sb.length () > 1 ) {
            return sb.toString ().toLowerCase ();
        } else {
            return s.toLowerCase();
        }
    }
}
