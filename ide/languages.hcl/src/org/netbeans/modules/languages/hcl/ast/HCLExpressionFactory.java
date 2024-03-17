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
package org.netbeans.modules.languages.hcl.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import static org.netbeans.modules.languages.hcl.grammar.HCLLexer.*;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;

/**
 *
 * @author lkishalmi
 */
public final class HCLExpressionFactory extends HCLElementFactory {

    public HCLExpressionFactory(Consumer<CreateContext> createAction) {
        super(createAction);
    }

    public HCLExpressionFactory() {
        this(null);
    }

    
    public final HCLExpression process(HCLParser.ExpressionContext ctx) throws UnsupportedOperationException {
        return expr(ctx);
    }
    
    
    protected HCLExpression expr(HCLParser.ExpressionContext ctx) throws UnsupportedOperationException {
        if (ctx == null) {
            return null;
        }
        if (ctx.op != null) {
            if (ctx.left != null && ctx.right != null) {
                HCLArithmeticOperation.Operator op = binOp(ctx.op.getType());
                return created(new HCLArithmeticOperation.Binary(op, expr(ctx.left), expr(ctx.right)), ctx);
            }
            if (ctx.right != null) {
                return switch (ctx.op.getType()) {
                    case NOT ->  created(new HCLArithmeticOperation.Unary(HCLArithmeticOperation.Operator.NOT, expr(ctx.right)), ctx);
                    case MINUS -> created(new HCLArithmeticOperation.Unary(HCLArithmeticOperation.Operator.MINUS, expr(ctx.right)), ctx);
                    default -> throw new UnsupportedOperationException("Unsupported expression: " + ctx.getText());
                };
            }
            if (ctx.exprCond != null && ctx.exprTrue != null && ctx.exprFalse != null) {
                return created(new HCLConditionalOperation(expr(ctx.exprCond), expr(ctx.exprTrue), expr(ctx.exprFalse)), ctx);
            }
        } else {
            return ctx.exprTerm() != null ? expr(ctx.exprTerm()) : null;
        }
        throw new UnsupportedOperationException("Unsupported expression: " + ctx.getText());
    }

    protected HCLExpression expr(HCLParser.ExprTermContext ctx) {
        if (ctx == null) {
            return null;
        }
        HCLExpression ret = null;
        if (ctx.LPAREN() != null && ctx.RPAREN() != null) {
            ret = expr(ctx.expression());
        } else if (ctx.literalValue() != null) {
            ret = expr(ctx.literalValue());
        } else if (ctx.collectionValue() != null) {
            ret = expr(ctx.collectionValue());
        } else if (ctx.functionCall() != null) {
            ret = expr(ctx.functionCall());
        } else if (ctx.templateExpr() != null) {
            ret = expr(ctx.templateExpr());
        } else if (ctx.forExpr() != null) {
            ret = expr(ctx.forExpr());
        } else if (ctx.variableExpr() != null) {
            ret = expr(ctx.variableExpr());
        } else if (ctx.getAttr() != null) {
            ret = expr(expr(ctx.exprTerm()), ctx.getAttr());
        } else if (ctx.index() != null) {
            ret = expr(expr(ctx.exprTerm()), ctx.index());
        } else if (ctx.splat() != null) {
            HCLParser.SplatContext splat = ctx.splat();
            ret = expr(ctx.exprTerm(), splat);
        }
        if (ctx.exception != null) {
            if (ctx.exception instanceof NoViableAltException nva) {
                if (nva.getStartToken().getType() == HCLLexer.DOT) {
                    //Most probably a single DOT would mean a started attribute resolve expression
                    //Let's create an empty one on the fly
                    return created(new HCLResolveOperation.Attribute(ret, null), nva.getStartToken());
                }
            }
        } 
        return ret;
    }

    protected HCLExpression expr(HCLParser.VariableExprContext ctx) {
        return ctx != null ? created(new HCLVariable(id(ctx.IDENTIFIER())), ctx) : null;
    }
    
    protected HCLExpression expr(HCLParser.LiteralValueContext ctx) throws UnsupportedOperationException {
        if (ctx == null) {
            return null;
        }
        if (ctx.stringLit() != null) {
            return ctx.stringLit().stringContent() != null ? created(new HCLLiteral.StringLit(ctx.stringLit().stringContent().getText()), ctx) : created(new HCLLiteral.StringLit(""), ctx);
        }
        if (ctx.TRUE() != null) {
            return created(HCLLiteral.TRUE, ctx);
        }
        if (ctx.FALSE() != null) {
            return created(HCLLiteral.FALSE, ctx);
        }
        if (ctx.NULL() != null) {
            return created(HCLLiteral.NULL, ctx);
        }
        if (ctx.NUMERIC_LIT() != null) {
            return created(new HCLLiteral.NumericLit(ctx.NUMERIC_LIT().getText()), ctx);
        }
        throw new UnsupportedOperationException("Unsupported literal: " + ctx.getText());
    }

    protected HCLExpression expr(HCLParser.CollectionValueContext ctx) throws UnsupportedOperationException {
        if (ctx == null) {
            return null;
        }
        if (ctx.tuple() != null) {
            HCLParser.TupleContext tuple = ctx.tuple();
            List<HCLExpression> elements = new LinkedList<>();
            for (HCLParser.ExpressionContext ec : tuple.expression()) {
                elements.add(expr(ec));
            }
            return new HCLCollection.Tuple(elements);
        }
        if (ctx.object() != null) {
            HCLParser.ObjectContext object = ctx.object();
            List<HCLCollection.ObjectElement> elements = new LinkedList<>();
            int group = 0;
            ParserRuleContext prev = null;
            for (HCLParser.ObjectElemContext ec : object.objectElem()) {
                if ((prev != null) && (ec.key != null)) {
                    group += prev.stop.getLine() + 1 < ec.key.start.getLine() ? 1 : 0;
                }
                HCLCollection.ObjectElement oe = new HCLCollection.ObjectElement(expr(ec.key), expr(ec.value));
                elements.add(created(oe, ec, group));
                prev = ec;
            }
            return new HCLCollection.Object(elements);
        }
        throw new UnsupportedOperationException("Unsupported collection: " + ctx.getText());
    }

    protected HCLExpression expr(HCLParser.FunctionCallContext ctx) {
        if (ctx == null) {
            return null;
        }
        List<HCLExpression> args = Collections.emptyList();
        boolean expand = false;
        if (ctx.arguments() != null) {
            args = new ArrayList<>(ctx.arguments().expression().size());
            for (HCLParser.ExpressionContext ectx : ctx.arguments().expression()) {
                args.add(expr(ectx));
            }
            expand = ctx.arguments().ELLIPSIS() != null;
        }
        return created(new HCLFunction(id(ctx.IDENTIFIER()), args, expand), ctx);
    }

    private static HCLArithmeticOperation.Operator binOp(int tokenType) {
        return switch (tokenType) {
            case STAR -> HCLArithmeticOperation.Operator.MUL;
            case SLASH -> HCLArithmeticOperation.Operator.DIV;
            case PERCENT -> HCLArithmeticOperation.Operator.MOD;
            case PLUS -> HCLArithmeticOperation.Operator.ADD;
            case MINUS -> HCLArithmeticOperation.Operator.SUB;
            case OR -> HCLArithmeticOperation.Operator.OR;
            case AND -> HCLArithmeticOperation.Operator.AND;
            case LT -> HCLArithmeticOperation.Operator.LT;
            case LTE -> HCLArithmeticOperation.Operator.LTE;
            case GT -> HCLArithmeticOperation.Operator.GT;
            case GTE -> HCLArithmeticOperation.Operator.GTE;
            case EQUALS -> HCLArithmeticOperation.Operator.EQUALS;
            case NOT_EQUALS -> HCLArithmeticOperation.Operator.NOT_EQUALS;
            default -> null;
        };
    }

    protected HCLExpression expr(HCLParser.TemplateExprContext ctx) {
        if (ctx == null) {
            return null;
        }
        if (ctx.heredoc() != null) {
            return expr(ctx.heredoc());
        }
        if (ctx.quotedTemplate() != null) {
            return expr(ctx.quotedTemplate());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected HCLExpression expr(HCLParser.HeredocContext ctx) {
        if (ctx == null) {
            return null;
        }
        LinkedList<HCLTemplate.Part> parts = new LinkedList<>();

        String startText = ctx.HEREDOC_START().getText();
        boolean indented = startText.startsWith("<<~"); //NOI18N
        int markerStart = indented ? 3 : 2;
        int markerEnd = startText.endsWith("\r\n") ? 2 : 1;
        String marker = startText.substring(markerStart, startText.length() - markerEnd);

        int indent = -1;
        if (indented) {
            String content = ctx.heredocTemplate() != null ? ctx.heredocTemplate().getText() : "";
            AtomicInteger mIndent = new AtomicInteger(Integer.MAX_VALUE);
            content.lines().filter((line) -> !line.isBlank()).forEach((line) -> { 
                int left = 0;
                while (left < line.length() && line.charAt(left) == ' ') { // HCL Specs says <space>
                    left++;
                }
                mIndent.set(Math.min(mIndent.get(), left));
            });
            indent = mIndent.get();
        }

        if (ctx.heredocTemplate() != null && ctx.heredocTemplate().children != null) {
            for (ParseTree pt : ctx.heredocTemplate().children) {
                if (pt instanceof HCLParser.HeredocContentContext) {
                    parts.add(new HCLTemplate.Part.StringPart(pt.getText()));
                }
                if (pt instanceof HCLParser.InterpolationContext) {
                    parts.add(new HCLTemplate.Part.InterpolationPart(pt.getText()));
                }
                if (pt instanceof HCLParser.TemplateContext) {
                    parts.add(new HCLTemplate.Part.TemplatePart(pt.getText()));
                }
            }
        }
        return created(new HCLTemplate.HereDoc(marker, indent, parts), ctx);
    }

    protected HCLExpression expr(HCLParser.QuotedTemplateContext ctx) {
        if (ctx == null) {
            return null;
        }
        LinkedList<HCLTemplate.Part> parts = new LinkedList<>();
        for (ParseTree pt : ctx.children) {
            if (pt instanceof HCLParser.StringContentContext) {
                parts.add(new HCLTemplate.Part.StringPart(pt.getText()));
            }
            if (pt instanceof HCLParser.InterpolationContext) {
                parts.add(new HCLTemplate.Part.InterpolationPart(pt.getText()));
            }
            if (pt instanceof HCLParser.TemplateContext) {
                parts.add(new HCLTemplate.Part.TemplatePart(pt.getText()));
            }
        }
        return new HCLTemplate.StringTemplate(parts);
    }

    protected HCLExpression expr(HCLParser.ForExprContext ctx) {
        if (ctx == null) {
            return null;
        }
        boolean isTuple = ctx.forTupleExpr() != null;

        HCLParser.ForIntroContext intro = isTuple ? ctx.forTupleExpr().forIntro() : ctx.forObjectExpr().forIntro();

        HCLIdentifier keyVar = null;
        HCLIdentifier valueVar;
        if (intro.second != null) {
            keyVar = id(intro.first);
            valueVar = id(intro.second);
        } else {
            valueVar = id(intro.first);
        }
        HCLExpression iterable = expr(intro.expression());

        HCLParser.ForCondContext cond = isTuple ? ctx.forTupleExpr().forCond() : ctx.forObjectExpr().forCond();
        HCLExpression condExpr = cond != null ? expr(cond.expression()) : null;

        if (isTuple) {
            HCLExpression result = expr(ctx.forTupleExpr().expression());
            return created(new HCLForExpression.Tuple(keyVar, valueVar, iterable, condExpr, result), ctx);
        } else {
            boolean grouping = ctx.forObjectExpr().ELLIPSIS() != null;
            HCLExpression resultKey = expr(ctx.forObjectExpr().key);
            HCLExpression resultValue = expr(ctx.forObjectExpr().value);
            return created(new HCLForExpression.Object(keyVar, valueVar, iterable, condExpr, resultKey, resultValue, grouping), ctx);
        }
    }

    protected HCLExpression expr(HCLParser.ExprTermContext exprTerm, HCLParser.SplatContext splat) {
        HCLExpression base = expr(exprTerm);
        if (splat.attrSplat() != null) {
            base = expr(base, splat);
            for (HCLParser.GetAttrContext ac : splat.attrSplat().getAttr()) {
                base = expr(base, ac);
            }
        }
        if (splat.fullSplat() != null) {
            base = expr(base, splat);
            for (ParseTree pt : splat.fullSplat().children) {
                if (pt instanceof HCLParser.GetAttrContext gac) {
                    base = expr(base, gac);
                }
                if (pt instanceof HCLParser.IndexContext ic) {
                    base = expr(base, ic);
                }
            }
        }
        return base;
    }

    protected HCLExpression expr(HCLExpression base, HCLParser.SplatContext ctx) throws UnsupportedOperationException {
        if (ctx.attrSplat() != null) {
            HCLParser.AttrSplatContext splat = ctx.attrSplat();
            return created(new HCLResolveOperation.AttrSplat(base), splat.DOT().getSymbol(), splat.STAR().getSymbol());
        }
        if (ctx.fullSplat() != null) {
            HCLParser.FullSplatContext splat = ctx.fullSplat();
            return created(new HCLResolveOperation.FullSplat(base), splat.LBRACK().getSymbol(), splat.RBRACK().getSymbol());
        }
        throw new UnsupportedOperationException("Unsupported Splat operation. Should not happen. Check the Grammar!");
    }
    
    protected HCLExpression expr(HCLExpression base, HCLParser.GetAttrContext ctx) {
        return created(new HCLResolveOperation.Attribute(base, id(ctx.IDENTIFIER())), ctx);
    }
    
    protected HCLExpression expr(HCLExpression base, HCLParser.IndexContext idx) {
        HCLExpression index = idx.expression() != null
                ? expr(idx.expression())
                : created(new HCLLiteral.NumericLit(idx.LEGACY_INDEX().getText().substring(1)), idx); // Split the dot from .<index>
        return created(new HCLResolveOperation.Index(base, index, idx.LEGACY_INDEX() != null), idx);

    }
}
