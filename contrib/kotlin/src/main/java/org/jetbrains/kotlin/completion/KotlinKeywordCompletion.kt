/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.completion

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.filters.ClassFilter
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.NotFilter
import com.intellij.psi.filters.OrFilter
import com.intellij.psi.filters.position.PositionElementFilter
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.ANNOTATION_CLASS
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.CLASS_ONLY
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.ENUM_CLASS
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.ENUM_ENTRY
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.FUNCTION
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.INNER_CLASS
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.INTERFACE
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.MEMBER_FUNCTION
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.MEMBER_PROPERTY
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.OBJECT
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.PROPERTY
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.TOP_LEVEL_FUNCTION
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.TOP_LEVEL_PROPERTY
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.TYPE_PARAMETER
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget.VALUE_PARAMETER
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.ModifierCheckerCore
import java.util.ArrayList
import org.jetbrains.kotlin.idea.util.CallTypeAndReceiver
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.netbeans.modules.csl.api.CompletionProposal

// This code is mostly copied from Idea plugin
object KeywordCompletion {
    private val ALL_KEYWORDS = (KEYWORDS.types + SOFT_KEYWORDS.types).map { it as KtKeywordToken }

    private val KEYWORDS_TO_IGNORE_PREFIX = TokenSet.create(OVERRIDE_KEYWORD /* it's needed to complete overrides that should be work by member name too */)

    private val COMPOUND_KEYWORDS = mapOf<KtKeywordToken, KtKeywordToken>(
            COMPANION_KEYWORD to OBJECT_KEYWORD,
            ENUM_KEYWORD to CLASS_KEYWORD,
            ANNOTATION_KEYWORD to CLASS_KEYWORD
    )

    fun complete(position: PsiElement, prefix: String, isJvmModule: Boolean, consumer: (String) -> Unit) {
        if (!GENERAL_FILTER.isAcceptable(position, position)) return

        val parserFilter = buildFilter(position)
        for (keywordToken in ALL_KEYWORDS) {
            var keyword = keywordToken.value

            val nextKeyword = COMPOUND_KEYWORDS[keywordToken]
            if (nextKeyword != null) {
                fun PsiElement.isSpace() = this is PsiWhiteSpace && '\n' !in getText()

                var next = position.nextLeaf { !(it.isSpace() || it.text == "$") }?.text
                if (next != null && next.startsWith("$")) {
                    next = next.substring(1)
                }
                if (next != nextKeyword.value) {
                    keyword += " ${nextKeyword.value}"
                }
            }

            if (keywordToken == DYNAMIC_KEYWORD && isJvmModule) continue // not supported for JVM

            // we use simple matching by prefix, not prefix matcher from completion
            if (!keyword.startsWith(prefix) && keywordToken !in KEYWORDS_TO_IGNORE_PREFIX) continue

            if (!parserFilter(keywordToken)) continue

            consumer(keyword)
        }
    }

    private val GENERAL_FILTER = NotFilter(OrFilter(
            CommentFilter(),
            ParentFilter(ClassFilter(KtLiteralStringTemplateEntry::class.java)),
            ParentFilter(ClassFilter(KtConstantExpression::class.java)),
            LeftNeighbour(TextFilter(".")),
            LeftNeighbour(TextFilter("?."))
    ))

    private class CommentFilter : ElementFilter {
        override fun isAcceptable(element: Any?, context: PsiElement?)
                = (element is PsiElement) && KtPsiUtil.isInComment(element)

        override fun isClassAcceptable(hintClass: Class<out Any?>)
                = true
    }

    private class ParentFilter(filter: ElementFilter) : PositionElementFilter() {
        init {
            setFilter(filter)
        }

        override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
            val parent = (element as? PsiElement)?.parent
            return parent != null && (filter?.isAcceptable(parent, context) ?: true)
        }
    }

    private fun buildFilter(position: PsiElement): (KtKeywordToken) -> Boolean {
        var parent = position.parent
        var prevParent = position
        while (parent != null) {
            when (parent) {
                is KtBlockExpression -> {
                    var prefixText = "fun foo() { "
                    if (prevParent is KtExpression) {
                        // check that we are right after a try-expression without finally-block
                        val prevLeaf = prevParent.prevLeaf { it !is PsiWhiteSpace && it !is PsiComment && it !is PsiErrorElement }
                        if (prevLeaf?.node?.elementType == KtTokens.RBRACE) {
                            val blockParent = (prevLeaf?.parent as? KtBlockExpression)?.parent
                            when (blockParent) {
                                is KtTryExpression -> prefixText += "try {}\n"
                                is KtCatchClause -> prefixText += "try {} catch (e: E) {}\n"
                            }
                        }

                        return buildFilterWithContext(prefixText, prevParent, position)
                    } else {
                        val lastExpression = prevParent
                                .siblings(forward = false, withItself = false)
                                .firstIsInstanceOrNull<KtExpression>()
                        if (lastExpression != null) {
                            val contextAfterExpression = lastExpression
                                    .siblings(forward = true, withItself = false)
                                    .takeWhile { it != prevParent }
                                    .joinToString { it.text }
                            return buildFilterWithContext("${prefixText}x$contextAfterExpression", prevParent, position)
                        }
                    }
                }

                is KtDeclarationWithInitializer -> {
                    val initializer = parent.initializer
                    if (initializer != null && prevParent == initializer) {
                        return buildFilterWithContext("val v = ", initializer, position)
                    }
                }

                is KtParameter -> {
                    val default = parent.defaultValue
                    if (default != null && prevParent == default) {
                        return buildFilterWithContext("val v = ", default, position)
                    }
                }
            }

            if (parent is KtDeclaration) {
                val scope = parent.parent
                when (scope) {
                    is KtClassOrObject -> {
                        return if (parent is KtPrimaryConstructor) {
                            buildFilterWithReducedContext("class X ", parent, position)
                        } else {
                            buildFilterWithReducedContext("class X { ", parent, position)
                        }
                    }

                    is KtFile -> return buildFilterWithReducedContext("", parent, position)
                }
            }

            prevParent = parent
            parent = parent.parent
        }

        return buildFilterWithReducedContext("", null, position)
    }

    private fun buildFilterWithContext(prefixText: String,
                                       contextElement: PsiElement,
                                       position: PsiElement): (KtKeywordToken) -> Boolean {
        val offset = position.getStartOffsetInAncestor(contextElement)
        val truncatedContext = contextElement.text!!.substring(0, offset)
        return buildFilterByText("$prefixText$truncatedContext", contextElement.project)
    }

    private fun buildFilterWithReducedContext(prefixText: String,
                                              contextElement: PsiElement?,
                                              position: PsiElement): (KtKeywordToken) -> Boolean {
        val builder = StringBuilder()
        buildReducedContextBefore(builder, position, contextElement)
        return buildFilterByText("$prefixText$builder", position.project)
    }


    private fun buildFilterByText(prefixText: String, project: Project): (KtKeywordToken) -> Boolean {
        val psiFactory = KtPsiFactory(project)
        return fun(keywordTokenType): Boolean {
            val postfix = if (prefixText.endsWith("@")) ":X" else " X"
            val file = psiFactory.createFile("$prefixText${keywordTokenType.value}$postfix")
            val elementAt = file.findElementAt(prefixText.length)!!

            when {
                !elementAt.node!!.elementType.matchesKeyword(keywordTokenType) -> return false

                elementAt.getNonStrictParentOfType<PsiErrorElement>() != null -> return false

                isErrorElementBefore(elementAt) -> return false

                keywordTokenType !is KtModifierKeywordToken -> return true

                else -> {
                    if (elementAt.parent !is KtModifierList) return true
                    val container = elementAt.parent.parent
                    val possibleTargets = when (container) {
                        is KtParameter -> {
                            if (container.ownerFunction is KtPrimaryConstructor)
                                listOf(VALUE_PARAMETER, MEMBER_PROPERTY)
                            else
                                listOf(VALUE_PARAMETER)
                        }

                        is KtTypeParameter -> listOf(TYPE_PARAMETER)

                        is KtEnumEntry -> listOf(ENUM_ENTRY)

                        is KtClassBody -> listOf(CLASS_ONLY, INTERFACE, OBJECT, ENUM_CLASS, ANNOTATION_CLASS, INNER_CLASS, MEMBER_FUNCTION, MEMBER_PROPERTY, FUNCTION, PROPERTY)

                        is KtFile -> listOf(CLASS_ONLY, INTERFACE, OBJECT, ENUM_CLASS, ANNOTATION_CLASS, TOP_LEVEL_FUNCTION, TOP_LEVEL_PROPERTY, FUNCTION, PROPERTY)

                        else -> null
                    }
                    val modifierTargets = ModifierCheckerCore.possibleTargetMap[keywordTokenType]
                    if (modifierTargets != null && possibleTargets != null && possibleTargets.none { it in modifierTargets }) return false

                    val ownerDeclaration = container?.getParentOfType<KtDeclaration>(strict = true)
                    val parentTarget = when (ownerDeclaration) {
                        null -> KotlinTarget.FILE

                        is KtClass -> {
                            when {
                                ownerDeclaration.isInterface() -> KotlinTarget.INTERFACE
                                ownerDeclaration.isEnum() -> KotlinTarget.ENUM_CLASS
                                ownerDeclaration.isAnnotation() -> KotlinTarget.ANNOTATION_CLASS
                                ownerDeclaration.isInner() -> KotlinTarget.INNER_CLASS
                                else -> KotlinTarget.CLASS_ONLY
                            }
                        }

                        is KtObjectDeclaration -> if (ownerDeclaration.isObjectLiteral()) KotlinTarget.OBJECT_LITERAL else KotlinTarget.OBJECT

                        else -> return true
                    }

                    val modifierParents = ModifierCheckerCore.possibleParentTargetMap[keywordTokenType]
                    if (modifierParents != null && parentTarget !in modifierParents) return false

                    val deprecatedParents = ModifierCheckerCore.deprecatedParentTargetMap[keywordTokenType]
                    if (deprecatedParents != null && parentTarget in deprecatedParents) return false

                    return true
                }
            }
        }
    }

    private fun isErrorElementBefore(token: PsiElement): Boolean {
        for (leaf in token.prevLeafs) {
            if (leaf is PsiWhiteSpace || leaf is PsiComment) continue
            if (leaf.parentsWithSelf.any { it is PsiErrorElement }) return true
            if (leaf.textLength != 0) break
        }
        return false
    }

    private fun IElementType.matchesKeyword(keywordType: KtKeywordToken): Boolean =
        when (this) {
            keywordType -> true
            NOT_IN -> keywordType == IN_KEYWORD
            NOT_IS -> keywordType == IS_KEYWORD
            else -> false
        }


    // builds text within scope (or from the start of the file) before position element excluding almost all declarations
    private fun buildReducedContextBefore(builder: StringBuilder, position: PsiElement, scope: PsiElement?) {
        if (position == scope) return
        val parent = position.parent ?: return

        buildReducedContextBefore(builder, parent, scope)

        val prevDeclaration = position.siblings(forward = false, withItself = false).firstOrNull { it is KtDeclaration }

        var child = parent.firstChild
        while (child != position) {
            if (child is KtDeclaration) {
                if (child == prevDeclaration) {
                    builder.appendReducedText(child)
                }
            } else {
                builder.append(child!!.text)
            }

            child = child.nextSibling
        }
    }

    private fun StringBuilder.appendReducedText(element: PsiElement) {
        var child = element.firstChild
        if (child == null) {
            append(element.text!!)
        } else {
            while (child != null) {
                when (child) {
                    is KtBlockExpression, is KtClassBody -> append("{}")
                    else -> appendReducedText(child)
                }

                child = child.nextSibling
            }
        }
    }

    private fun PsiElement.getStartOffsetInAncestor(ancestor: PsiElement): Int {
        if (ancestor == this) return 0
        return parent!!.getStartOffsetInAncestor(ancestor) + startOffsetInParent
    }
}

fun breakOrContinueExpressionItems(position: KtElement, breakOrContinue: String): Collection<String> {
    val result = ArrayList<String>()

    parentsLoop@
    for (parent in position.parentsWithSelf) {
        when (parent) {
            is KtLoopExpression -> {
                if (result.isEmpty()) {
                    result.add(breakOrContinue)
                }

                val label = (parent.parent as? KtLabeledExpression)?.getLabelNameAsName()
                if (label != null) {
                    result.add("$breakOrContinue${label.labelNameToTail()}")
                }
            }

            is KtDeclarationWithBody -> break@parentsLoop //TODO: support non-local break's&continue's when they are supported by compiler
        }
    }
    return result
}

private fun Name?.labelNameToTail(): String = if (this != null) "@${render()}" else ""

inline fun <reified T : Any> Sequence<*>.firstIsInstanceOrNull(): T? {
    for (element in this) if (element is T) return element
    return null
}

inline fun <reified T : PsiElement> PsiElement.getNonStrictParentOfType(): T? =
        PsiTreeUtil.getParentOfType(this, T::class.java, false)

inline fun <reified T : PsiElement> PsiElement.getParentOfType(strict: Boolean): T? =
        PsiTreeUtil.getParentOfType(this, T::class.java, strict)

fun generateKeywordProposals(identifierPart: String,
                             expression: PsiElement, offset: Int, prefix: String): List<CompletionProposal> {
    val callTypeAndReceiver = if (expression is KtSimpleNameExpression) CallTypeAndReceiver.detect(expression) else null

    return arrayListOf<String>().apply {
        KeywordCompletion.complete(expression, identifierPart, true) { keywordProposal ->
            if (!applicableNameFor(identifierPart, keywordProposal)) return@complete

            when (keywordProposal) {
                "break", "continue" -> {
                    if (expression is KtSimpleNameExpression) {
                        addAll(breakOrContinueExpressionItems(expression, keywordProposal))
                    }
                }

                "class" -> {
                    if (callTypeAndReceiver !is CallTypeAndReceiver.CALLABLE_REFERENCE) {
                        add(keywordProposal)
                    }
                }

                "this", "return" -> {
                    if (expression is KtExpression) {
                        add(keywordProposal)
                    }
                }

                else -> add(keywordProposal)
            }
        }
    }.map { KeywordCompletionProposal(it, offset, prefix) }
}