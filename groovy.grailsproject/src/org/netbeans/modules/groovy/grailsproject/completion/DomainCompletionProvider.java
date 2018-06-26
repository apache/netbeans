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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.ContextHelper;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service = CompletionProvider.class)
public class DomainCompletionProvider implements CompletionProvider {

    private static final Logger LOGGER = Logger.getLogger(DomainCompletionProvider.class.getName());

    private static final Map<MethodSignature, String> INSTANCE_METHODS = new HashMap<MethodSignature, String>();

    private static final Map<MethodSignature, String> STATIC_METHODS = new HashMap<MethodSignature, String>();

    private static final String[] NO_PARAMETERS = new String[] {};

    private static final String FIND_BY_METHOD = "findBy"; // NOI18N

    private static final String FIND_ALL_BY_METHOD = "findAllBy"; // NOI18N

    private static final String COUNT_BY_METHOD = "countBy"; // NOI18N

    private static final String LIST_ORDER_BY_METHOD = "listOrderBy"; // NOI18N

    private static final Set<String> QUERY_OPERATOR = new HashSet<String>();

    private static final Set<String> QUERY_COMPARATOR = new HashSet<String>();

    // FIXME move it to some resource file, check the grails version - this is for 1.0.4
    static {
        Collections.addAll(QUERY_OPERATOR, "And", "Or"); // NOI18N

        Collections.addAll(QUERY_COMPARATOR, "LessThan", "LessThanEquals", // NOI18N
                "GreaterThan", "GreaterThanEquals", "Like", "ILike", // NOI18N
                "Equal", "NotEqual", "Between", "IsNotNull", "IsNull"); // NOI18N

        INSTANCE_METHODS.put(new MethodSignature("attach", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("clearErrors", // NOI18N
                NO_PARAMETERS), "org.springframework.validation.Errors"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("delete", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("delete", // NOI18N
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("discard", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("hasErrors", // NOI18N
                NO_PARAMETERS), "java.lang.Boolean"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("ident", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("lock", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("merge", // NOI18N
                new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N // return value ?
        INSTANCE_METHODS.put(new MethodSignature("merge", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N // return value ?
        INSTANCE_METHODS.put(new MethodSignature("refresh", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N // return value null ?
        INSTANCE_METHODS.put(new MethodSignature("save", // NOI18N
                new String[] {"java.lang.Boolean"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("save", // NOI18N
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("save", // NOI18N
                NO_PARAMETERS), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("validate", // NOI18N
                NO_PARAMETERS), "java.lang.Boolean"); // NOI18N // return value ?

        // not documented
        INSTANCE_METHODS.put(new MethodSignature("isAttached", // NOI18N
                NO_PARAMETERS), "java.lang.Boolean"); // NOI18N

        // findBy - see #getQueryMethods()
        // findAllBy - see #getQueryMethods()
        // countBy - see #getQueryMethods()
        // listOrderBy - see #getOrderMethods()
        STATIC_METHODS.put(new MethodSignature("count", // NOI18N
                NO_PARAMETERS), "int"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("createCriteria", // NOI18N
                NO_PARAMETERS), "grails.orm.HibernateCriteriaBuilder"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeQuery", // NOI18N
                new String[] {"java.lang.String"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeQuery", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeQuery", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeQuery", // NOI18N
                new String[] {"java.lang.String", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeQuery", // NOI18N
                new String[] {"java.lang.String", "java.util.Map", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeUpdate", // NOI18N
                new String[] {"java.lang.String"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("executeUpdate", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("exists", // NOI18N
                NO_PARAMETERS), "java.lang.Boolean"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("find", // NOI18N
                new String[] {"java.lang.String"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("find", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("find", // NOI18N
                new String[] {"java.lang.String", "java.util.Map"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("find", // NOI18N
                new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                NO_PARAMETERS), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.String"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.String", "java.util.Collection", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.String", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.String", "java.util.Map", "java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAll", // NOI18N
                new String[] {"java.lang.Object"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findWhere", // NOI18N
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("findAllWhere", // NOI18N
                new String[] {"java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("get", // NOI18N
                new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("getAll", // NOI18N
                NO_PARAMETERS), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("getAll", // NOI18N
                new String[] {"java.util.List"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("list", // NOI18N
                NO_PARAMETERS), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("list", // NOI18N
                new String[] {"java.util.Map"}), "java.util.List"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("withCriteria", // NOI18N
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("withCriteria", // NOI18N
                new String[] {"java.util.Map", "groovy.lang.Closure"}), "java.lang.Object"); // NOI18N
        STATIC_METHODS.put(new MethodSignature("withTransaction", // NOI18N
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        if (isInDomain(context)) {
            result.putAll(getQueryMethods(context));
            result.putAll(getOrderMethods(context));

            for (Map.Entry<MethodSignature, String> entry : INSTANCE_METHODS.entrySet()) {
                result.put(entry.getKey(), CompletionItem.forDynamicMethod(context.getAnchor(), entry.getKey().getName(), entry.getKey().getParameters(), entry.getValue(), false));
            }
        }
        return result;
    }

    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        
        if (isInDomain(context)) {
            for (Map.Entry<MethodSignature, String> entry : STATIC_METHODS.entrySet()) {
                result.put(entry.getKey(), CompletionItem.forDynamicMethod(context.getAnchor(), entry.getKey().getName(), entry.getKey().getParameters(), entry.getValue(), false));
            }
        }
        return result;
    }

    // package access for tests
    Map<MethodSignature, CompletionItem> getOrderMethods(CompletionContext context) {
        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        if (LIST_ORDER_BY_METHOD.startsWith(context.getPrefix()) || context.getPrefix().startsWith(LIST_ORDER_BY_METHOD)) {
            for (String property : ContextHelper.getProperties(context)) {
                String name = LIST_ORDER_BY_METHOD + capitalise(property);
                result.put(new MethodSignature(name, NO_PARAMETERS),
                        CompletionItem.forDynamicMethod(context.getAnchor(), name, NO_PARAMETERS,
                                "java.util.List", false)); // NOI18N
                result.put(new MethodSignature(name, new String[] {"java.util.Map"}), // NOI18N
                        CompletionItem.forDynamicMethod(context.getAnchor(), name, new String[] {"java.util.Map"}, // NOI18N
                                "java.util.List", false)); // NOI18N
            }
        }
        return result;
    }

    // package access for tests
    Map<MethodSignature, CompletionItem> getQueryMethods(CompletionContext context) {
        List<String> properties = ContextHelper.getProperties(context);
        if (properties.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();

        Matcher matcher = getQueryMethodPattern(properties).matcher(context.getPrefix());

        if (matcher.matches()) {
            String prefix = matcher.group(13);
            if (prefix == null) {
                prefix = "";
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                int count = matcher.groupCount();
                for (int i = 1; i <= count; i++) {
                    LOGGER.log(Level.FINE, "Group {0} {1}", new Object[] {Integer.valueOf(i), matcher.group(i)});
                }
            }

            String name = context.getPrefix().substring(0, context.getPrefix().length() - prefix.length());

            Map<String, Integer> names = new HashMap<String, Integer>();
            Set<String> forbidden = new HashSet<String>();
            int paramCount = getUsedComparators(context, forbidden);

            boolean noContinuation = (matcher.group(7) != null && matcher.group(8) != null);

            // comparator
            if (matcher.group(10) != null) {
                // operator + property
                if (!noContinuation) {
                    names.putAll(getSuffixForOperator(name, properties, prefix, paramCount));
                }
            // property
            } else if (matcher.group(9) != null) {
                // comparator or (operator + property)
                names.putAll(getSuffixForComparator(name, context, prefix, matcher.group(9), forbidden, paramCount));
                if (!noContinuation) {
                    names.putAll(getSuffixForOperator(name, properties, prefix, paramCount));
                }
            // operator
            } else if (matcher.group(7) != null) {
                // property
                names.putAll(getSuffixForProperty(name, properties, prefix, paramCount));
            } else {
                // only findBy|findByAll|countBy
                if (!noContinuation) {
                    names.putAll(getSuffixForProperty(name, properties, prefix, paramCount));
                }
            }
            // used for multiple operators
//            // comparator
//            else if (matcher.group(4) != null) {
//                // operator + property
//                //names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
//            // property
//            } else if (matcher.group(3) != null) {
//                // comparator or (operator + property)
//                //names.putAll(getSuffixForComparator(name, context, prefix, matcher.group(3), forbidden, paramCount));
//                //names.putAll(getSuffixForOperator(name, context, prefix, paramCount));
//            }

            for (Map.Entry<String, Integer> entry : names.entrySet()) {
                addQueryEntries(result, context, matcher.group(1),
                        entry.getKey().substring(matcher.group(1).length()), entry.getValue().intValue(), !noContinuation);
            }
            if ("".equals(prefix) && !matcher.group(1).equals(context.getPrefix())) {
                addQueryEntries(result, context, matcher.group(1),
                        name.substring(matcher.group(1).length()), paramCount, false);
            }
        }

        // initial prefix (no property in it)
        if (!matcher.matches() || context.getPrefix().equals(matcher.group(1))){
            // FIXME optimize
            for (String property : properties) {
                String tail = capitalise(property);

                addQueryEntries(result, context, FIND_ALL_BY_METHOD, tail, 1, true);
                addQueryEntries(result, context, FIND_BY_METHOD, tail, 1, true);
                addQueryEntries(result, context, COUNT_BY_METHOD, tail, 1, true);
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForOperator(String prefix, List<String> properties, String tail, int paramCount) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String property : properties) {
            for (String operator : QUERY_OPERATOR) {
                String suffix = operator + capitalise(property);
                if (suffix.startsWith(tail)) {
                    result.put(prefix + suffix, paramCount + 1);
                }
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForComparator(String prefix, CompletionContext context, String tail,
            String property, Set<String> forbidden, int paramCount) {

        Map<String, Integer> result = new HashMap<String, Integer>();

        for (String operator : QUERY_COMPARATOR) {
            int realCount = paramCount;
            String suffix = operator;
            if (suffix.startsWith(tail) && !forbidden.contains(property + suffix)) {
                if ("Between".equals(operator)) { // NOI18N
                    realCount++;
                } else if ("IsNotNull".equals(operator) || "IsNull".equals(operator)) { // NOI18N
                    realCount--;
                }
                result.put(prefix + suffix, realCount);
            }
        }
        return result;
    }

    private Map<String, Integer> getSuffixForProperty(String prefix, List<String> properties, String tail, int paramCount) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (String property : properties) {
            String suffix = capitalise(property);
            if (suffix.startsWith(tail)) {
                result.put(prefix + suffix, paramCount + 1);
            }
        }
        return result;
    }

    private Pattern getQueryMethodPattern(List<String> properties) {
        StringBuilder builder = new StringBuilder("(findBy|findAllBy|countBy)"); // NOI18N
        builder.append("("); // NOI18N

        StringBuilder propertyBuilder = new StringBuilder();
        propertyBuilder.append("("); // NOI18N
        for (String property : properties) {
            propertyBuilder.append(Pattern.quote(capitalise(property)));
            propertyBuilder.append('|'); // NOI18N
        }
        propertyBuilder.setLength(propertyBuilder.length() - 1);
        propertyBuilder.append(")"); // NOI18N

        builder.append(propertyBuilder);
        builder.append("(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?"); // NOI18N
        builder.append("(And|Or)"); // NOI18N
        builder.append(")?"); // NOI18N

        builder.append("("); // NOI18N
        builder.append(propertyBuilder);
        builder.append("(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?"); // NOI18N
        builder.append(")?"); // NOI18N

        builder.append("(.*)"); // NOI18N

        LOGGER.log(Level.FINE, "Method pattern is {0}", builder.toString());
        return Pattern.compile(builder.toString());
    }

    private int getUsedComparators(CompletionContext context, Set<String> result) {
        Matcher matcher = Pattern.compile("(findBy|findAllBy|countBy)(.*)").matcher(context.getPrefix()); // NOI18N
        if (!matcher.matches()) {
            return 0;
        }

        String[] parts = matcher.group(2).split("(And|Or)"); // NOI18N

        int paramCount = 0;
        Pattern pattern = Pattern.compile("(.*)(LessThan(Equals)?|GreaterThan(Equals)?|Like|ILike|Equal|NotEqual|Between|IsNotNull|IsNull)?"); // NOI18N
        for (String part : parts) {
            //result.add(part);

            Matcher singleMatcher = pattern.matcher(part);
            if (singleMatcher.matches()) {
                String comparator = singleMatcher.group(2);
                if ("Between".equals(comparator)) { // NOI18N
                    paramCount += 2;
                } else if (!"IsNotNull".equals(comparator) && !"IsNull".equals(comparator)) { // NOI18N
                    paramCount += 1;
                } else if (comparator == null) {
                    paramCount += 1;
                    //result.add(part + "Equal");
                }
            }
        }

        return paramCount;
    }

    private void addQueryEntries(Map<MethodSignature, CompletionItem> result,
            CompletionContext context, String prefix, String tail, int params, boolean prefixedMethod) {

        String returnType = "java.lang.Object"; // NOI18N
        if (FIND_ALL_BY_METHOD.equals(prefix)) {
            returnType = "java.util.List"; // NOI18N
        } else if (COUNT_BY_METHOD.equals(prefix)) {
            returnType = "int"; // NOI18N
        }
        String name = prefix + tail;

        String[] shortParams = new String[params];
        Arrays.fill(shortParams, "java.lang.Object"); // NOI18N
        result.put(new MethodSignature(name, shortParams),
                CompletionItem.forDynamicMethod(context.getAnchor(), name, shortParams, returnType, false));

        String[] longParams = new String[params + 1];
        Arrays.fill(longParams, "java.lang.Object"); // NOI18N
        longParams[params] = "java.util.Map"; // NOI18N
        result.put(new MethodSignature(name, longParams),
                CompletionItem.forDynamicMethod(context.getAnchor(), name, longParams, returnType, false));

        if (prefixedMethod) {
            result.put(new MethodSignature(name + "_", new String[] {}), // NOI18N
                    CompletionItem.forDynamicMethod(context.getAnchor(), name, new String[] {}, returnType, true));
        }
    }

    private boolean isInDomain(CompletionContext context) {
        if (context.getSourceFile() == null || context.getTypeName() == null) {
            return false;
        }

        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (project == null) {
            return false;
        }

        FileObject grailsDir = project.getProjectDirectory().getFileObject("grails-app"); // NOI18N
        if (grailsDir == null || !grailsDir.isFolder()) {
            return false;
        }

        String typeName = context.getTypeName().replace('.', '/');
        FileObject fo = grailsDir.getFileObject("domain/" + typeName + ".groovy"); // NOI18N
        if (fo != null && fo.isData()) {
            return true;
        } else {
            return false;
        }
    }

    private String capitalise(String property) {
        StringBuilder builder = new StringBuilder();
        String[] parts = property.split("[^\\w\\d]"); // NOI18N
        for (String part : parts) {
            builder.append(part.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(part.substring(1));
        }

        return builder.toString();
    }
}
