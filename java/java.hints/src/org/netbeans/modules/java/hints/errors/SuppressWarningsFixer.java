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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import static java.util.Map.entry;

/**
 * SupressWarnings hint for standard javac warnings.
 * 
 * @author Jan Lahoda
 */
public class SuppressWarningsFixer implements ErrorRule<Void> {
    
    static final Map<String, String> DIAG2LINT;
    
    static {

        // checked and generated via unit test

        DIAG2LINT = Map.ofEntries(
            entry("compiler.warn.auxiliary.class.accessed.from.outside.of.its.source.file", "auxiliaryclass"),   // NOI18N
            entry("compiler.warn.redundant.cast", "cast"),   // NOI18N
            entry("compiler.warn.annotation.method.not.found", "classfile"),   // NOI18N
            entry("compiler.warn.annotation.method.not.found.reason", "classfile"),   // NOI18N
            entry("compiler.warn.future.attr", "classfile"),   // NOI18N
            entry("compiler.warn.inconsistent.inner.classes", "classfile"),   // NOI18N
            entry("compiler.warn.runtime.invisible.parameter.annotations", "classfile"),   // NOI18N
            entry("compiler.warn.runtime.visible.invisible.param.annotations.mismatch", "classfile"),   // NOI18N
            entry("compiler.warn.unknown.enum.constant", "classfile"),   // NOI18N
            entry("compiler.warn.unknown.enum.constant.reason", "classfile"),   // NOI18N
            entry("compiler.warn.dangling.doc.comment", "dangling-doc-comments"),   // NOI18N
            entry("compiler.warn.missing.deprecated.annotation", "dep-ann"),   // NOI18N
            entry("compiler.warn.deprecated.annotation.has.no.effect", "deprecation"),   // NOI18N
            entry("compiler.warn.has.been.deprecated", "deprecation"),   // NOI18N
            entry("compiler.warn.has.been.deprecated.module", "deprecation"),   // NOI18N
            entry("compiler.warn.div.zero", "divzero"),   // NOI18N
            entry("compiler.warn.empty.if", "empty"),   // NOI18N
            entry("compiler.warn.leaks.not.accessible", "exports"),   // NOI18N
            entry("compiler.warn.leaks.not.accessible.not.required.transitive", "exports"),   // NOI18N
            entry("compiler.warn.leaks.not.accessible.unexported", "exports"),   // NOI18N
            entry("compiler.warn.leaks.not.accessible.unexported.qualified", "exports"),   // NOI18N
            entry("compiler.warn.possible.fall-through.into.case", "fallthrough"),   // NOI18N
            entry("compiler.warn.finally.cannot.complete", "finally"),   // NOI18N
            entry("compiler.warn.attempt.to.synchronize.on.instance.of.value.based.class", "identity"),   // NOI18N
            entry("compiler.warn.attempt.to.use.value.based.where.identity.expected", "identity"),   // NOI18N
            entry("compiler.warn.incubating.modules", "incubating"),   // NOI18N
            entry("compiler.warn.bit.shift.out.of.range", "lossy-conversions"),   // NOI18N
            entry("compiler.warn.possible.loss.of.precision", "lossy-conversions"),   // NOI18N
            entry("compiler.warn.missing-explicit-ctor", "missing-explicit-ctor"),   // NOI18N
            entry("compiler.warn.module.not.found", "module"),   // NOI18N
            entry("compiler.warn.poor.choice.for.module.name", "module"),   // NOI18N
            entry("compiler.warn.package.empty.or.not.found", "opens"),   // NOI18N
            entry("compiler.warn.addopens.ignored", "options"),   // NOI18N
            entry("compiler.warn.module.for.option.not.found", "options"),   // NOI18N
            entry("compiler.warn.option.obsolete.source", "options"),   // NOI18N
            entry("compiler.warn.option.obsolete.suppression", "options"),   // NOI18N
            entry("compiler.warn.option.obsolete.target", "options"),   // NOI18N
            entry("compiler.warn.source.no.bootclasspath", "options"),   // NOI18N
            entry("compiler.warn.source.no.system.modules.path", "options"),   // NOI18N
            entry("compiler.warn.output.file.clash", "output-file-clash"),   // NOI18N
            entry("compiler.warn.potentially.ambiguous.overload", "overloads"),   // NOI18N
            entry("compiler.warn.override.equals.but.not.hashcode", "overrides"),   // NOI18N
            entry("compiler.warn.override.varargs.extra", "overrides"),   // NOI18N
            entry("compiler.warn.override.varargs.missing", "overrides"),   // NOI18N
            entry("compiler.warn.dir.path.element.not.directory", "path"),   // NOI18N
            entry("compiler.warn.dir.path.element.not.found", "path"),   // NOI18N
            entry("compiler.warn.invalid.archive.file", "path"),   // NOI18N
            entry("compiler.warn.invalid.path", "path"),   // NOI18N
            entry("compiler.warn.locn.unknown.file.on.module.path", "path"),   // NOI18N
            entry("compiler.warn.outdir.is.in.exploded.module", "path"),   // NOI18N
            entry("compiler.warn.path.element.not.found", "path"),   // NOI18N
            entry("compiler.warn.unexpected.archive.file", "path"),   // NOI18N
            entry("compiler.warn.declared.using.preview", "preview"),   // NOI18N
            entry("compiler.warn.is.preview", "preview"),   // NOI18N
            entry("compiler.warn.is.preview.reflective", "preview"),   // NOI18N
            entry("compiler.warn.preview.feature.use", "preview"),   // NOI18N
            entry("compiler.warn.preview.feature.use.classfile", "preview"),   // NOI18N
            entry("compiler.warn.preview.feature.use.plural", "preview"),   // NOI18N
            entry("compiler.warn.proc.annotations.without.processors", "processing"),   // NOI18N
            entry("compiler.warn.proc.duplicate.option.name", "processing"),   // NOI18N
            entry("compiler.warn.proc.duplicate.supported.annotation", "processing"),   // NOI18N
            entry("compiler.warn.proc.file.reopening", "processing"),   // NOI18N
            entry("compiler.warn.proc.illegal.file.name", "processing"),   // NOI18N
            entry("compiler.warn.proc.malformed.supported.string", "processing"),   // NOI18N
            entry("compiler.warn.proc.redundant.types.with.wildcard", "processing"),   // NOI18N
            entry("compiler.warn.proc.suspicious.class.name", "processing"),   // NOI18N
            entry("compiler.warn.proc.type.already.exists", "processing"),   // NOI18N
            entry("compiler.warn.proc.type.recreate", "processing"),   // NOI18N
            entry("compiler.warn.raw.class.use", "rawtypes"),   // NOI18N
            entry("compiler.warn.has.been.deprecated.for.removal", "removal"),   // NOI18N
            entry("compiler.warn.has.been.deprecated.for.removal.module", "removal"),   // NOI18N
            entry("compiler.warn.requires.automatic", "requires-automatic"),   // NOI18N
            entry("compiler.warn.requires.transitive.automatic", "requires-transitive-automatic"),   // NOI18N
            entry("compiler.warn.restricted.method", "restricted"),   // NOI18N
            entry("compiler.warn.OSF.array.SPF", "serial"),   // NOI18N
            entry("compiler.warn.SPF.null.init", "serial"),   // NOI18N
            entry("compiler.warn.access.to.member.from.serializable.element", "serial"),   // NOI18N
            entry("compiler.warn.access.to.member.from.serializable.lambda", "serial"),   // NOI18N
            entry("compiler.warn.constant.SVUID", "serial"),   // NOI18N
            entry("compiler.warn.default.ineffective", "serial"),   // NOI18N
            entry("compiler.warn.externalizable.missing.public.no.arg.ctor", "serial"),   // NOI18N
            entry("compiler.warn.improper.SPF", "serial"),   // NOI18N
            entry("compiler.warn.improper.SVUID", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.extern.method.enum", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.externalizable.method.record", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.field.enum", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.field.externalizable", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.field.interface", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.field.record", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.method.enum", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.method.externalizable", "serial"),   // NOI18N
            entry("compiler.warn.ineffectual.serial.method.record", "serial"),   // NOI18N
            entry("compiler.warn.long.SVUID", "serial"),   // NOI18N
            entry("compiler.warn.missing.SVUID", "serial"),   // NOI18N
            entry("compiler.warn.non.private.method.weaker.access", "serial"),   // NOI18N
            entry("compiler.warn.non.serializable.instance.field", "serial"),   // NOI18N
            entry("compiler.warn.non.serializable.instance.field.array", "serial"),   // NOI18N
            entry("compiler.warn.serial.concrete.instance.method", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.no.args", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.not.private", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.one.arg", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.parameter.type", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.static", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.unexpected.exception", "serial"),   // NOI18N
            entry("compiler.warn.serial.method.unexpected.return.type", "serial"),   // NOI18N
            entry("compiler.warn.serializable.missing.access.no.arg.ctor", "serial"),   // NOI18N
            entry("compiler.warn.static.not.qualified.by.type", "static"),   // NOI18N
            entry("compiler.warn.static.not.qualified.by.type2", "static"),   // NOI18N
            entry("compiler.warn.strictfp", "strictfp"),   // NOI18N
            entry("compiler.warn.inconsistent.white.space.indentation", "text-blocks"),   // NOI18N
            entry("compiler.warn.trailing.white.space.will.be.removed", "text-blocks"),   // NOI18N
            entry("compiler.warn.possible.this.escape", "this-escape"),   // NOI18N
            entry("compiler.warn.possible.this.escape.location", "this-escape"),   // NOI18N
            entry("compiler.warn.try.explicit.close.call", "try"),   // NOI18N
            entry("compiler.warn.try.resource.can.throw.interrupted.exc", "try"),   // NOI18N
            entry("compiler.warn.try.resource.not.referenced", "try"),   // NOI18N
            entry("compiler.warn.try.resource.throws.interrupted.exc", "try"),   // NOI18N
            entry("compiler.warn.override.unchecked.ret", "unchecked"),   // NOI18N
            entry("compiler.warn.override.unchecked.thrown", "unchecked"),   // NOI18N
            entry("compiler.warn.prob.found.req", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.assign", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.assign.to.var", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.call.mbr.of.raw.type", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.cast.to.type", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.generic.array.creation", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.meth.invocation.applied", "unchecked"),   // NOI18N
            entry("compiler.warn.unchecked.varargs.non.reifiable.type", "unchecked"),   // NOI18N
            entry("compiler.warn.varargs.redundant.trustme.anno", "varargs"),   // NOI18N
            entry("compiler.warn.varargs.unsafe.use.varargs.param", "varargs")   // NOI18N
        );
    }

    public SuppressWarningsFixer() {
    }

    @Override
    public Set<String> getCodes() {
        return DIAG2LINT.keySet();
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        String suppressKey = DIAG2LINT.get(diagnosticKey);
        return suppressKey != null
                ? FixFactory.createSuppressWarnings(compilationInfo, treePath, suppressKey)
                : List.of();
    }

    @Override
    public void cancel() {
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

}
