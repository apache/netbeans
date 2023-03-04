package org.netbeans.modules.java.editor.base.javadoc;

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



import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 */
public final class Utilities {

    private static final String EMPTY = ""; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N
    private static final String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"; // NOI18N
    private static final boolean COMPLETION_CASE_SENSITIVE_DEFAULT = true;
    private static final String SHOW_DEPRECATED_MEMBERS = "show-deprecated-members"; // NOI18N
    private static final boolean SHOW_DEPRECATED_MEMBERS_DEFAULT = true;
    private static final String JAVA_COMPLETION_WHITELIST = "javaCompletionWhitelist"; //NOI18N
    private static final String JAVA_COMPLETION_BLACKLIST = "javaCompletionBlacklist"; //NOI18N
    private static final String JAVA_COMPLETION_BLACKLIST_DEFAULT = ""; //NOI18N
    private static final String JAVA_COMPLETION_EXCLUDER_METHODS = "javaCompletionExcluderMethods"; //NOI18N
    private static final boolean JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT = false;
    private static final String JAVA_COMPLETION_SUBWORDS = "javaCompletionSubwords"; //NOI18N
    private static final boolean JAVA_COMPLETION_SUBWORDS_DEFAULT = false;

    private static boolean caseSensitive = COMPLETION_CASE_SENSITIVE_DEFAULT;
    private static boolean showDeprecatedMembers = SHOW_DEPRECATED_MEMBERS_DEFAULT;
    private static boolean javaCompletionExcluderMethods = JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT;
    private static boolean javaCompletionSubwords = JAVA_COMPLETION_SUBWORDS_DEFAULT;

    private static final AtomicBoolean inited = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || COMPLETION_CASE_SENSITIVE.equals(settingName)) {
                caseSensitive = preferences.getBoolean(COMPLETION_CASE_SENSITIVE, COMPLETION_CASE_SENSITIVE_DEFAULT);
            }
            if (settingName == null || SHOW_DEPRECATED_MEMBERS.equals(settingName)) {
                showDeprecatedMembers = preferences.getBoolean(SHOW_DEPRECATED_MEMBERS, SHOW_DEPRECATED_MEMBERS_DEFAULT);
            }
            if (settingName == null || JAVA_COMPLETION_BLACKLIST.equals(settingName)) {
                String blacklist = preferences.get(JAVA_COMPLETION_BLACKLIST, EMPTY);
                updateExcluder(excludeRef, blacklist);
            }
            if (settingName == null || JAVA_COMPLETION_WHITELIST.equals(settingName)) {
                String whitelist = preferences.get(JAVA_COMPLETION_WHITELIST, EMPTY);
                updateExcluder(includeRef, whitelist);
            }
            if (settingName == null || JAVA_COMPLETION_EXCLUDER_METHODS.equals(settingName)) {
                javaCompletionExcluderMethods = preferences.getBoolean(JAVA_COMPLETION_EXCLUDER_METHODS, JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT);
            }
            if (settingName == null || JAVA_COMPLETION_SUBWORDS.equals(settingName)) {
                javaCompletionSubwords = preferences.getBoolean(JAVA_COMPLETION_SUBWORDS, JAVA_COMPLETION_SUBWORDS_DEFAULT);
            }
        }
    };

    private static String cachedPrefix = null;
    private static Pattern cachedCamelCasePattern = null;
    private static Pattern cachedSubwordsPattern = null;

    public static boolean startsWith(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || ERROR.equals(theString)) {
            return false;
        }
        if (prefix == null || prefix.length() == 0) {
            return true;
        }

        // sub word completion
        if (javaCompletionSubwords) {
            // example:
            // 'out' produces '.*?[o|O].*?[u|U].*?[t|T].*?'
            // org.openide.util.Utilities.acoh -> actionsForPath
            // java.lang.System.out -> setOut
            // argex -> IllegalArgumentException
            // java.util.Collections.que -> asLifoQueue
            // java.lang.System.sin -> setIn, getSecurityManager, setSecurityManager

            // check whether user input matches the regex
            if (!prefix.equals(cachedPrefix)) {
                cachedCamelCasePattern = null;
                cachedSubwordsPattern = null;
            }
            if (cachedSubwordsPattern == null) {
                cachedPrefix = prefix;
                String patternString = createSubwordsPattern(prefix);
                cachedSubwordsPattern = patternString != null ? Pattern.compile(patternString) : null;
            }
            if (cachedSubwordsPattern != null && cachedSubwordsPattern.matcher(theString).matches()) {
                return true;
            }
        }

        return isCaseSensitive() ? theString.startsWith(prefix)
                : theString.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }

    static String createSubwordsPattern(String prefix) {
        StringBuilder sb = new StringBuilder(3 + 8 * prefix.length());
        sb.append(".*?");
        for (int i = 0; i < prefix.length(); i++) {
            char charAt = prefix.charAt(i);
            if (!Character.isJavaIdentifierPart(charAt)) {
                return null;
            }
            if (Character.isLowerCase(charAt)) {
                sb.append("[");
                sb.append(charAt);
                sb.append(Character.toUpperCase(charAt));
                sb.append("]");
            } else {
                //keep uppercase characters as beacons
                // for example: java.lang.System.sIn -> setIn
                sb.append(charAt);
            }
            sb.append(".*?");
        }
        return sb.toString();
    }

    public static boolean startsWithCamelCase(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || prefix == null || prefix.length() == 0) {
            return false;
        }
        if (!prefix.equals(cachedPrefix)) {
            cachedCamelCasePattern = null;
            cachedSubwordsPattern = null;
        }
        if (cachedCamelCasePattern == null) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            do {
                index = findNextUpper(prefix, lastIndex + 1);
                String token = prefix.substring(lastIndex, index == -1 ? prefix.length() : index);
                sb.append(token);
                sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                lastIndex = index;
            } while (index != -1);
            cachedPrefix = prefix;
            cachedCamelCasePattern = Pattern.compile(sb.toString());
        }
        return cachedCamelCasePattern.matcher(theString).matches();
    }

    private static int findNextUpper(String text, int offset) {
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    public static boolean isSubwordSensitive() {
        lazyInit();
        return javaCompletionSubwords;
    }

    public static boolean isShowDeprecatedMembers() {
        lazyInit();
        return showDeprecatedMembers;
    }

    private static final AtomicReference<Collection<String>> excludeRef = new AtomicReference<>();
    private static final AtomicReference<Collection<String>> includeRef = new AtomicReference<>();

    private static void updateExcluder(AtomicReference<Collection<String>> existing, String updated) {
        Collection<String> nue = new LinkedList<>();
        if (updated == null || updated.length() == 0) {
            existing.set(nue);
            return;
        }
        String[] entries = updated.split(","); //NOI18N
        for (String entry : entries) {
            if (entry.length() != 0) {
                nue.add(entry);
            }
        }
        existing.set(nue);
    }

    /**
     * @return the user setting for whether the excluder should operate on
     * methods
     */
    public static boolean isExcludeMethods() {
        lazyInit();
        return javaCompletionExcluderMethods;
    }

    /**
     * @param fqn Fully Qualified Name (including method names). Packages names
     * are expected to end in a trailing "." except the default package.
     * @return
     */
    public static boolean isExcluded(final CharSequence fqn) {
        if (fqn == null || fqn.length() == 0) {
            return true;
        }
        lazyInit();
        String s = fqn.toString();
        Collection<String> include = includeRef.get();
        Collection<String> exclude = excludeRef.get();

        if (include != null && !include.isEmpty()) {
            for (String entry : include) {
                if (s.endsWith(".") && entry.startsWith(s)) {
                    return false;
                }
                if ((entry.endsWith("*") && entry.length() - 1 <= s.length()
                        && s.startsWith(entry.substring(0, entry.length() - 1)))
                        || s.equals(entry)) {
                    return false;
                }
            }
        }

        if (exclude != null && !exclude.isEmpty()) {
            for (String entry : exclude) {
                if ((entry.endsWith("*") && entry.length() - 1 <= s.length() //NOI18N
                        && s.startsWith(entry.substring(0, entry.length() - 1)))
                        || s.equals(entry)) {
                        return true;
                }
            }
        }

        return false;
    }

    public static void exclude(final CharSequence fqn) {
        if (fqn != null && fqn.length() > 0) {
            lazyInit();
            String blacklist = preferences.get(JAVA_COMPLETION_BLACKLIST, JAVA_COMPLETION_BLACKLIST_DEFAULT);
            blacklist += (blacklist.length() > 0 ? "," + fqn : fqn); //NOI18N
            preferences.put(JAVA_COMPLETION_BLACKLIST, blacklist);
        }
    }

    public static List<String> varNamesSuggestions(TypeMirror type, ElementKind kind, Set<Modifier> modifiers, String suggestedName, String prefix, Types types, Elements elements, Iterable<? extends Element> locals, CodeStyle codeStyle) {
        List<String> result = new ArrayList<>();
        if (type == null && suggestedName == null) {
            return result;
        }
        List<String> vnct = suggestedName != null ? Collections.singletonList(suggestedName) : varNamesForType(type, types, elements, prefix);
        boolean isConst = false;
        String namePrefix = null;
        String nameSuffix = null;
        switch (kind) {
            case FIELD:
                if (modifiers.contains(Modifier.STATIC)) {
                    if (codeStyle != null) {
                        namePrefix = codeStyle.getStaticFieldNamePrefix();
                        nameSuffix = codeStyle.getStaticFieldNameSuffix();
                    }
                    isConst = modifiers.contains(Modifier.FINAL);
                } else {
                    if (codeStyle != null) {
                        namePrefix = codeStyle.getFieldNamePrefix();
                        nameSuffix = codeStyle.getFieldNameSuffix();
                    }
                }
                break;
            case LOCAL_VARIABLE:
            case EXCEPTION_PARAMETER:
            case RESOURCE_VARIABLE:
                if (codeStyle != null) {
                    namePrefix = codeStyle.getLocalVarNamePrefix();
                    nameSuffix = codeStyle.getLocalVarNameSuffix();
                }
                break;
            case PARAMETER:
                if (codeStyle != null) {
                    namePrefix = codeStyle.getParameterNamePrefix();
                    nameSuffix = codeStyle.getParameterNameSuffix();
                }
                break;
        }
        if (isConst) {
            List<String> ls = new ArrayList<>(vnct.size());
            for (String s : vnct) {
                ls.add(getConstName(s));
            }
            vnct = ls;
        }
        if (vnct.isEmpty() && prefix != null && prefix.length() > 0
                && (namePrefix != null && namePrefix.length() > 0
                || nameSuffix != null && nameSuffix.length() >0)) {
            vnct = Collections.singletonList(prefix);
        }
        String p = prefix;
        while (p != null && p.length() > 0) {
            List<String> l = new ArrayList<>();
            for (String name : vnct) {
                if (startsWith(name, p)) {
                    l.add(name);
                }
            }
            if (l.isEmpty()) {
                p = nextName(p);
            } else {
                vnct = l;
                prefix = prefix.substring(0, prefix.length() - p.length());
                p = null;
            }
        }
        for (String name : vnct) {
            boolean isPrimitive = type != null && type.getKind().isPrimitive();
            if (prefix != null && prefix.length() > 0) {
                if (isConst) {
                    name = prefix.toUpperCase(Locale.ENGLISH) + '_' + name;
                } else {
                    name = prefix + name.toUpperCase(Locale.ENGLISH).charAt(0) + name.substring(1);
                }
            }
            int cnt = 1;
            String baseName = name;
            name = CodeStyleUtils.addPrefixSuffix(name, namePrefix, nameSuffix);
            while (isClashing(name, type, locals)) {
                if (isPrimitive) {
                    char c = name.charAt(namePrefix != null ? namePrefix.length() : 0);
                    name = CodeStyleUtils.addPrefixSuffix(Character.toString(++c), namePrefix, nameSuffix);
                    if (c == 'z' || c == 'Z') { //NOI18N
                        isPrimitive = false;
                    }
                } else {
                    name = CodeStyleUtils.addPrefixSuffix(baseName + cnt++, namePrefix, nameSuffix);
                }
            }
            result.add(name);
        }
        return result;
    }

    private static List<String> varNamesForType(TypeMirror type, Types types, Elements elements, String prefix) {
        switch (type.getKind()) {
            case ARRAY:
                TypeElement iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                TypeMirror iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                TypeMirror ct = ((ArrayType) type).getComponentType();
                if (ct.getKind() == TypeKind.ARRAY && iterable != null && types.isSubtype(ct, iterable)) {
                    return varNamesForType(ct, types, elements, prefix);
                }
                List<String> vnct = new ArrayList<>();
                for (String name : varNamesForType(ct, types, elements, prefix)) {
                    vnct.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                }
                return vnct;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                String str = type.toString().substring(0, 1);
                return prefix != null && !prefix.equals(str)
                        ? Collections.<String>emptyList()
                        : Collections.<String>singletonList(str);
            case TYPEVAR:
                return Collections.<String>singletonList(type.toString().toLowerCase(Locale.ENGLISH));
            case ERROR:
                String tn = ((ErrorType) type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn)) {
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                }
                StringBuilder sb = new StringBuilder();
                ArrayList<String> al = new ArrayList<>();
                if ("Iterator".equals(tn)) { //NOI18N
                    al.add("it"); //NOI18N
                }
                while ((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix)) {
                        al.add(s);
                    }
                }
                return al;
            case DECLARED:
                iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                tn = ((DeclaredType) type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn)) {
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                }
                sb = new StringBuilder();
                al = new ArrayList<>();
                if ("Iterator".equals(tn)) { //NOI18N
                    al.add("it"); //NOI18N
                }
                while ((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (iterable != null && types.isSubtype(type, iterable)) {
                    List<? extends TypeMirror> tas = ((DeclaredType) type).getTypeArguments();
                    if (tas.size() > 0) {
                        TypeMirror et = tas.get(0);
                        if (et.getKind() == TypeKind.ARRAY || (et.getKind() != TypeKind.WILDCARD && types.isSubtype(et, iterable))) {
                            al.addAll(varNamesForType(et, types, elements, prefix));
                        } else {
                            for (String name : varNamesForType(et, types, elements, prefix)) {
                                al.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                            }
                        }
                    }
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix)) {
                        al.add(s);
                    }
                }
                return al;
            case WILDCARD:
                TypeMirror bound = ((WildcardType) type).getExtendsBound();
                if (bound == null) {
                    bound = ((WildcardType) type).getSuperBound();
                }
                if (bound != null) {
                    return varNamesForType(bound, types, elements, prefix);
                }
        }
        return Collections.<String>emptyList();
    }

    private static String getConstName(String s) {
        StringBuilder sb = new StringBuilder();
        boolean prevUpper = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!prevUpper) {
                    sb.append('_');
                }
                sb.append(c);
                prevUpper = true;
            } else {
                sb.append(Character.toUpperCase(c));
                prevUpper = false;
            }
        }
        return sb.toString();
    }

    private static String nextName(CharSequence name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                char lc = Character.toLowerCase(c);
                sb.append(lc);
                sb.append(name.subSequence(i + 1, name.length()));
                break;
            }
        }
        return sb.toString();
    }

    private static boolean isClashing(String varName, TypeMirror type, Iterable<? extends Element> locals) {
        try {
            if (JavaTokenId.valueOf(varName).primaryCategory().startsWith("keyword")) {
                return true;
            }
        } catch (Exception e) {
        }
        if (type != null && type.getKind() == TypeKind.DECLARED && ((DeclaredType) type).asElement().getSimpleName().contentEquals(varName)) {
            return true;
        }
        for (Element e : locals) {
            if ((e.getKind().isField() || e.getKind() == ElementKind.LOCAL_VARIABLE || e.getKind() == ElementKind.RESOURCE_VARIABLE
                    || e.getKind() == ElementKind.PARAMETER || e.getKind() == ElementKind.EXCEPTION_PARAMETER) && varName.contentEquals(e.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    private Utilities() {
    }
}
