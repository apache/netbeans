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
package org.netbeans.spi.whitelist.support;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Union2;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
/*public - needs clean up*/
final class WhiteListImplementationBuilder {

    private static final byte INVOKE = 1;
    private static final byte OVERRIDE = INVOKE << 1;

    private Model model;

    private WhiteListImplementationBuilder() {
        this.model = new Model();
    }

    @NonNull
    public WhiteListImplementationBuilder setDisplayName(@NonNull final String displayName) {
        Parameters.notNull("displayName", displayName); //NOI18N
        checkPreconditions();
        model.setDisplayName(displayName);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addCheckedPackage(@NonNull final String checkedPackage) {
        Parameters.notNull("checkedPackage", checkedPackage);   //NOI18N
        checkPreconditions();
        model.addCheckedPackage(checkedPackage);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addInvocableClass(@NonNull final String classBinaryName) {
        Parameters.notNull("classBinaryName", classBinaryName); //NOI18N
        checkPreconditions();
        model.addClass(classBinaryName, INVOKE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addInvocableMethod(
            @NonNull final String classBinaryName,
            @NonNull final String methodName,
            @NonNull final String... argumentTypes) {
        Parameters.notNull("classBinaryName", classBinaryName);    //NOI18N
        Parameters.notNull("methodName", methodName);   //NOI18N
        Parameters.notNull("argumentTypes", argumentTypes); //NOI18N
        checkPreconditions();
        model.addMethod(classBinaryName, methodName, argumentTypes, INVOKE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addSubclassableClass(@NonNull final String classBinaryName) {
        Parameters.notNull("classBinaryName", classBinaryName); //NOI18N
        checkPreconditions();
        model.addClass(classBinaryName, OVERRIDE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addOverridableMethod(
            @NonNull final String classBinaryName,
            @NonNull final String methodName,
            @NonNull final String... argumentTypes) {
        Parameters.notNull("classBinaryName", classBinaryName);    //NOI18N
        Parameters.notNull("methodName", methodName);   //NOI18N
        Parameters.notNull("argumentTypes", argumentTypes); //NOI18N
        checkPreconditions();
        model.addMethod(classBinaryName, methodName, argumentTypes, OVERRIDE);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addAllowedPackage(@NonNull final String packageName, boolean includingSubpackages) {
        Parameters.notNull("checkedPackage", packageName);   //NOI18N
        checkPreconditions();
        model.addAllowedPackage(includingSubpackages ? packageName + "." : packageName);
        return this;
    }

    @NonNull
    public WhiteListImplementationBuilder addDisallowedPackage(@NonNull final String packageName, boolean includingSubpackages) {
        Parameters.notNull("checkedPackage", packageName);   //NOI18N
        checkPreconditions();
        model.addDisallowedPackage(includingSubpackages ? packageName + "." : packageName);
        return this;
    }

    @NonNull
    public WhiteListQueryImplementation.WhiteListImplementation build() {
        final WhiteList result = new WhiteList(model.build());
        model = null;
        return result;
    }

    @NonNull
    public static WhiteListImplementationBuilder create() {
        return new WhiteListImplementationBuilder();
    }

    //<editor-fold defaultstate="collapsed" desc="Private implementation">
    private void checkPreconditions() {
        if (model == null) {
            throw new IllegalStateException("Modifying already built builder, create a new one");   //NOI18N
        }
    }

    private static final class WhiteList implements WhiteListQueryImplementation.WhiteListImplementation {

        private final Model model;

        private WhiteList(@NonNull final Model model) {
            assert model != null;
            this.model = model;
        }

        @Override
        @NbBundle.Messages({
            "RULE_Class=Class Access",
            "RULE_Meth=Method Invocation",
            "RULE_Cons=Method Invocation",
            "DESC_Class=Access of type {0} is forbidden by {1}.",
            "DESC_Meth=Invocation of method {0} is forbidden by {1}.",
            "DESC_Cons=Invocation of constuctor {0} is forbidden by {1}."
        })
        public WhiteListQuery.Result check(
                @NonNull final ElementHandle<?> element,
                @NonNull final WhiteListQuery.Operation operation) {
            assert element != null;
            assert operation != null;
            boolean b = model.isAllowed(element,INVOKE);
            String ruleName = null;
            String ruleDesc = null;
            if (!b) {
                if (element.getKind().isClass() || element.getKind().isInterface()) {
                    ruleName = Bundle.RULE_Class();
                    ruleDesc = Bundle.DESC_Class(displayName(element), model.getDisplayName());
                } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                    ruleName = Bundle.RULE_Cons();
                    ruleDesc = Bundle.DESC_Cons(displayName(element), model.getDisplayName());
                } else {
                    ruleName = Bundle.RULE_Meth();
                    ruleDesc = Bundle.DESC_Meth(displayName(element), model.getDisplayName());
                }
                return new WhiteListQuery.Result(
                        // TODO: whitelist ID should be configurable via a setter method:
                        Collections.singletonList(new WhiteListQuery.RuleDescription(ruleName, ruleDesc, null))
                        );
            } else {
                return new WhiteListQuery.Result();
            }
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            //immutable no change support
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            //immutable no change support
        }

        @NonNull
        private static String displayName(
            @NonNull final ElementHandle<? extends Element> handle) {
            final ElementKind kind = handle.getKind();
            final String[] vmSig = SourceUtils.getJVMSignature(handle);
            if (kind.isClass() || kind.isInterface()) {
                assert vmSig.length == 1;
                return cannonicalName(vmSig[0]);
            } else if (kind == ElementKind.CONSTRUCTOR) {
                assert vmSig.length == 3;
                final StringBuilder sb = new StringBuilder();
                int index = vmSig[2].lastIndexOf(')');
                assert index > 0;
                sb.append(simpleName(vmSig[0]));
                appendParams(vmSig[2],index,sb);
                return sb.toString();
            } else if (kind == ElementKind.METHOD) {
                assert vmSig.length == 3;
                final StringBuilder sb = new StringBuilder();
                int index = vmSig[2].lastIndexOf(')');
                assert index > 0;
                cannonicalName(vmSig[2].substring(index+1),sb);
                sb.append(' '); //NOI18N
                sb.append(vmSig[1]);
                appendParams(vmSig[2],index,sb);
                return sb.toString();
            } else  {
                throw new UnsupportedOperationException(kind.name());
            }
        }

        private static void appendParams(
                @NonNull final String methodSig,
                @NonNull final int paramsEndIndex,
                @NonNull final StringBuilder sb) {
            sb.append('('); //NOI18N
                int state = 0;
                boolean first = true;
                for (int i=1 ,j = 1; j< paramsEndIndex;) {
                    final char la = methodSig.charAt(j);
                    switch (state) {
                        case 0:
                            if (la=='L') {      //NOI18N
                                state = 1;
                                j++;
                            }
                            else if (la=='[') { //NOI18N
                                j++;
                            } else {
                                j++;
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(", ");    //NOI18N
                                }
                                cannonicalName(methodSig.substring(i,j), sb);
                                i=j;
                            }
                            break;
                        case 1:
                            if (la==';') {      //NOI18N
                                j++;
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append(", ");    //NOI18N
                                }
                                cannonicalName(methodSig.substring(i,j), sb);
                                i=j;
                                state = 0;
                            } else {
                                j++;
                            }
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }
                sb.append(')'); //NOI18N
        }

        @NonNull
        private static String cannonicalName(@NonNull final String binaryName) {
            return binaryName.replace('$', '.').replace('/', '.');    //NOI18N
        }

        @NonNull
        private static String simpleName(@NonNull final String binaryName) {
            final int index =  Math.max(binaryName.lastIndexOf('.'),binaryName.lastIndexOf('$'));
            return index == -1 ? binaryName : binaryName.substring(index+1);
        }

        private static void cannonicalName(
                @NonNull final String type,
                @NonNull final StringBuilder into) {
            final char la = type.charAt(0);
            switch (la) {
                case 'V':                       //NOI18N
                    into.append("void");        //NOI18N
                    break;
                case 'Z':                       //NOI18N
                    into.append("boolean");     //NOI18N
                    break;
                case 'B':                       //NOI18N
                    into.append("byte");        //NOI18N
                    break;
                case 'S':                       //NOI18N
                    into.append("short");       //NOI18N
                    break;
                case 'I':                       //NOI18N
                    into.append("int");         //NOI18N
                    break;
                case 'J':                       //NOI18N
                    into.append("long");        //NOI18N
                    break;
                case 'C':                       //NOI18N
                    into.append("char");        //NOI18N
                    break;
                case 'F':                       //NOI18N
                    into.append("float");       //NOI18N
                    break;
                case 'D':                       //NOI18N
                    into.append("double");      //NOI18N
                    break;
                case 'L':                       //NOI18N
                    into.append(cannonicalName(type.substring(1,type.length()-1)));
                    break;
                case '[':                       //NOI18N
                    cannonicalName(type.substring(1), into);
                    into.append("[]");          //NOI18N
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static final class Model {
        private static final String DEF_NAMES = PackedNames.class.getName();
        private String whiteListName;
        private Union2<StringBuilder,Pattern> checkedPkgs;
        private List<String> allowedPackages; // silly: if package ends with dot then it handles subpackages as well
        private List<String> disallowedPackages; // silly: if package ends with dot then it handles subpackages as well
        private final Names names;
        private final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>> root =
                new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>>();

        private Model() {
                try {
                    names  = (Names) Class.forName(System.getProperty("WhiteListBuilder.names", DEF_NAMES)).getDeclaredConstructor().newInstance();  //NOI18N
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException("Cannot instantiate names", ex);    //NOI18N
                }
                checkedPkgs = Union2.<StringBuilder,Pattern>createFirst(new StringBuilder());
                allowedPackages = new ArrayList<String>();
                disallowedPackages = new ArrayList<String>();
        }

        void addCheckedPackage(
                @NonNull final String pkg) {
            if (checkedPkgs.first().length()>0) {
                checkedPkgs.first().append('|');    //NOI18N
            }
            checkedPkgs.first().append(Pattern.quote(pkg+'.')).append(".*"); //NOI18N
        }

        void addAllowedPackage(
                @NonNull final String pkg) {
            allowedPackages.add(pkg);
        }
        
        void addDisallowedPackage(
                @NonNull final String pkg) {
            disallowedPackages.add(pkg);
        }
        
        void addClass(
                @NonNull final String binaryName,
                final byte mode) {
            final String[] pkgNamePair = splitName(binaryName,'/');
            final Integer pkgId = names.putName(folderToPackage(pkgNamePair[0]));
            @SuppressWarnings("RedundantStringConstructorCall")
            final Integer clsId = names.putName(new String(pkgNamePair[1]));
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode =
                root.putIfAbsent(pkgId, new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>());
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode =
                pkgNode.putIfAbsent(clsId, new IntermediateCacheNode<IntermediateCacheNode<CacheNode>>());
            clsNode.state |= mode;
        }

        void addMethod(
                @NonNull final String clsBinaryName,
                @NonNull final String methodName,
                @NonNull final String[] argTypes,
                @NonNull final byte mode) {
            final String[] pkgNamePair = splitName(clsBinaryName,'/');
            final Integer pkgId = names.putName(folderToPackage(pkgNamePair[0]));
            @SuppressWarnings("RedundantStringConstructorCall")
            final Integer clsId = names.putName(new String(pkgNamePair[1]));
            final Integer methodNameId = names.putName(methodName);
            final Integer metodSigId = names.putName(vmSignature(argTypes));
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode =
                root.putIfAbsent(pkgId, new IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>>());
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode =
                pkgNode.putIfAbsent(clsId, new IntermediateCacheNode<IntermediateCacheNode<CacheNode>>());
            final IntermediateCacheNode<CacheNode> methodNameNode =
                clsNode.putIfAbsent(methodNameId, new IntermediateCacheNode<CacheNode>());
            final CacheNode methodSigNode =
                methodNameNode.putIfAbsent(metodSigId, new CacheNode());
            methodSigNode.state |= mode;
        }

        void setDisplayName(final String name) {
            whiteListName = name;
        }

        @NbBundle.Messages({
            "TXT_UnknownWhiteList=Unknown WhiteList"
        })
        Model build() {
            checkedPkgs = Union2.createSecond(
                    Pattern.compile(checkedPkgs.first().toString()));
            if (whiteListName == null) {
                whiteListName = Bundle.TXT_UnknownWhiteList();
            }
            return this;
        }

        private boolean isThere(List<String> packages, String pkg) {
            String pkg2 = pkg+"."; // silly
            for (String s : packages) {
                if (pkg2.startsWith(s)) {
                    // if subpackages are handled then all is OK
                    if (s.endsWith(".")) {
                        return true;
                    } else if (pkg.equals(s)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        boolean isAllowed(
                @NonNull final ElementHandle<?> element,
                final byte mode) {
            final String[] vmSignatures = SourceUtils.getJVMSignature(element);
            final String[] pkgNamePair = splitName(vmSignatures[0],'.');  //NOI18N
            
            if (isThere(allowedPackages, pkgNamePair[0])) {
                return true;
            }
            if (isThere(disallowedPackages, pkgNamePair[0])) {
                return false;
            }
            
            if (!checkedPkgs.second().matcher(pkgNamePair[0]+'.').matches()) {  //NOI18N
                return true;
            }
            final Integer pkgId = names.getName(pkgNamePair[0]);
            final Integer clsId = names.getName(pkgNamePair[1]);
            final IntermediateCacheNode<IntermediateCacheNode<IntermediateCacheNode<CacheNode>>> pkgNode = root.get(pkgId);
            if (pkgNode == null) {
                return false;
            }
            final IntermediateCacheNode<IntermediateCacheNode<CacheNode>> clsNode = pkgNode.get(clsId);
            if (clsNode == null) {
                return false;
            }
            if ((clsNode.state & mode) == mode) {
                return true;
            }
            if (element.getKind() == ElementKind.METHOD ||
                element.getKind() == ElementKind.CONSTRUCTOR) {
                final Integer methodNameId = names.getName(vmSignatures[1]);
                final Integer methodSigId = names.getName(paramsOnly(vmSignatures[2]));
                final IntermediateCacheNode<CacheNode> methodNameNode = clsNode.get(methodNameId);
                if (methodNameNode == null) {
                    return false;
                }
                final CacheNode methodSigNode = methodNameNode.get(methodSigId);
                if (methodSigNode == null) {
                    return false;
                }
                return (methodSigNode.state & mode) == mode;
            } else if ((element.getKind().isClass() ||
                element.getKind().isInterface()) && clsNode.hasChildren()) {
                //If the request is for type and it has at least one alowed method
                //allow it. It would be strange to black list type usage which method is allowed
                return true;
            }
            return false;
        }

        String getDisplayName() {
            return whiteListName;
        }

        private static class CacheNode {
            byte state;
        }

        private static class IntermediateCacheNode<T extends CacheNode> extends CacheNode {
            private Map<Integer,T> nodes = new HashMap<Integer, T>();

            @NonNull
            final T putIfAbsent (
                    @NonNull Integer id,
                    @NonNull T node) {
                assert id != null;
                assert node != null;
                T result = nodes.get(id);
                if (result == null) {
                    result = node;
                    nodes.put (id, result);
                }
                return result;
            }

            @CheckForNull
            final T get(@NullAllowed Integer id) {
                return id == null ? null : nodes.get(id);
            }

            final boolean hasChildren() {
                return !nodes.isEmpty();
            }
        }

        @NonNull
        private String[] splitName(
                @NonNull final String qName,
                final char separator) {
            int index = qName.lastIndexOf(separator);    //NOI18N
            String pkg, name;
            if (index == -1) {
                pkg = "";   //NOI18N
                name = qName;
            } else {
                pkg = qName.substring(0, index);
                name = qName.substring(index+1);
            }
            return new String[] {pkg, name};
        }

        @NonNull
        private String paramsOnly(
                @NonNull final String name) {
            assert name.charAt(0) == '(';   //NOI18N;
            int index = name.lastIndexOf(')');  //NOI18N
            assert index > 0;
            return name.substring(1, index);
        }

        @NonNull
        private String vmSignature(
                @NonNull final String[] types) {
            final StringBuilder sb = new StringBuilder();
            for (String type : types) {
                encodeType(type,sb);
            }
            return sb.toString();
        }

        private void encodeType(
                @NonNull final String type,
                @NonNull StringBuilder sb) {
            assert type != null;
            assert sb != null;
            if ("void".equals(type)) {  //NOI18N
                sb.append('V');	    // NOI18N
            } else if ("boolean".equals(type)) {    //NOI18N
                sb.append('Z');	    // NOI18N
            } else if ("byte".equals(type)) {   //NOI18N
                sb.append('B');     //NOI18N
            } else if ("short".equals(type)) {    //NOI18N
                sb.append('S');	    // NOI18N
            } else if ("int".equals(type)) { //NOI18N
                sb.append('I');	    // NOI18N
            } else if ("long".equals(type)) {
                sb.append('J');	    // NOI18N
            } else if ("char".equals(type)) { //NOI18N
                sb.append('C');	    // NOI18N
            } else if ("float".equals(type)) {  //NOI18N
                sb.append('F');	    // NOI18N
            } else if ("double".equals(type)) {
                sb.append('D');	    // NOI18N
            } else if (type.charAt(type.length()-1) == ']') {   //NOI18N
                sb.append('[');
                encodeType(type.substring(0,type.length()-2), sb);
            } else {
                sb.append('L'); //NOI18N
                sb.append(type);
                sb.append(';'); //NOI18N
            }
        }

        @NonNull
        private static String folderToPackage(@NonNull final String folder) {
            return folder.replace( '/', '.' );  //NOI18N
        }
    }

    private static interface Names {
        @NonNull
        Integer putName(@NonNull String name);

        @CheckForNull
        Integer getName(@NonNull String name);
    }

    //@NotThreadSafe
    static class SimpleNames implements Names {

        private final Map<String,Integer> names = new HashMap<String, Integer>();
        private int counter = Integer.MIN_VALUE;

        @Override
        @NonNull
        public Integer putName(@NonNull final String name) {
            assert name != null;
            Integer result = names.get(name);
            if (result == null) {
                result = counter++;
                names.put(name, result);
            }
            return result;
        }

        @Override
        @CheckForNull
        public Integer getName(@NonNull final String name) {
            assert name != null;
            return names.get(name);
        }
    }

    //@NotThreadSafe
    static class PackedNames implements Names {

        private static final int DEF_SLOTS = 1<<10;
        private static final int DEF_INIT_BYTES = 1<<16;

        private final int hashMask;
        private Entry[] slots;
        private byte[] storage;
        private int pos;

        PackedNames() {
            slots = new Entry[DEF_SLOTS];
            hashMask = slots.length - 1;
            storage = new byte[DEF_INIT_BYTES];
        }

        @Override
        public Integer putName(@NonNull final String name) {
            assert name != null;
            final int hc = name.hashCode() & hashMask;
            final byte[] sbytes = decode(name);
            Entry entry = slots[hc];
            while (entry != null && !contentEquals(entry,sbytes)) {
                entry = entry.next;
            }
            if (entry == null) {
                if (storage.length < pos+sbytes.length) {
                    int newStorSize = storage.length;
                    while (newStorSize < pos+sbytes.length) {
                        newStorSize<<=1;
                    }
                    final byte[] tmpStorage = new byte[newStorSize];
                    System.arraycopy(storage, 0, tmpStorage, 0, storage.length);
                    storage = tmpStorage;
                }
                System.arraycopy(sbytes, 0, storage, pos, sbytes.length);
                entry = new Entry(pos, sbytes.length, slots[hc]);
                slots[hc] = entry;
                pos+= sbytes.length == 0 ? 1 : sbytes.length;
            }
            return entry.pos;
        }

        @Override
        public Integer getName(@NonNull final String name) {
            assert name != null;
            final int hc = name.hashCode() & hashMask;
            final byte[] sbytes = decode(name);
            Entry entry = slots[hc];
            while (entry != null && !contentEquals(entry,sbytes)) {
                entry = entry.next;
            }
            return entry == null ? null : entry.pos;
        }

        private boolean contentEquals(
            @NonNull final Entry entry,
            @NonNull final byte[] content) {
            assert entry != null;
            assert content != null;
            if (entry.length != content.length) {
                return false;
            }
            for (int i=0; i< entry.length; i++) {
                if (content[i] != storage[entry.pos+i]) {
                    return false;
                }
            }
            return true;
        }

        private byte[] decode(String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }

        private static class Entry {
            private int pos;
            private int length;
            private Entry next;

            private Entry(
                final int pos,
                final int length,
                @NullAllowed final Entry next) {
                this.pos = pos;
                this.length = length;
                this.next = next;
            }
        }
    }
    //</editor-fold>
}
