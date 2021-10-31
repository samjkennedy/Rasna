package com.skennedy.lazuli.graphing;

import com.skennedy.lazuli.lowering.BoundConditionalGotoExpression;
import com.skennedy.lazuli.lowering.BoundGotoExpression;
import com.skennedy.lazuli.lowering.BoundLabel;
import com.skennedy.lazuli.lowering.BoundLabelExpression;
import com.skennedy.lazuli.typebinding.BoundAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryOperator;
import com.skennedy.lazuli.typebinding.BoundBlockExpression;
import com.skennedy.lazuli.typebinding.BoundConstDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundTypeofExpression;
import com.skennedy.lazuli.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundVariableExpression;
import com.skennedy.lazuli.typebinding.VariableSymbol;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class LowLevelTreeGrapher {

    private Map<BoundLabel, Integer> labelToIp;

    public LowLevelTreeGrapher() {
        Graphviz.useEngine(new GraphvizV8Engine());
    }

    public void graphAST(BoundProgram program) throws IOException {
        MutableGraph graph = mutGraph("Graph Test")
                .setDirected(true).use((gr, ctx) -> graphProgram(program));

        Graphviz.fromGraph(graph).render(Format.SVG).toFile(new File("graphs/lowlevel.svg"));
    }

    private void graphProgram(BoundProgram program) {

        labelToIp = new HashMap<>();

        int ip = 0;
        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundLabelExpression) {
                labelToIp.put(((BoundLabelExpression) expression).getLabel(), ip);
            }
            ip++;
        }

        List<BoundExpression> expressions = program.getExpressions();
        ip = 0;

        MutableNode last = null;
        for (BoundExpression expression : expressions) {
            MutableNode curr = graphExpression(expression, ip);
            if (last != null) {
                last.addLink(mutNode("next_instruction_" + ip).add(Label.of("NEXT")).addLink(curr));
            }
            last = curr;
            ip++;
        }
        MutableNode halt = mutNode("END").add(Label.of("HALT")).add(Shape.BOX);
        last.addLink(halt);
    }

    private MutableNode graphExpression(BoundExpression expression, int ip) {
        switch (expression.getBoundExpressionType()) {
            case VARIABLE_EXPRESSION:
                return graphSyntaxExpression((BoundVariableExpression) expression, ip);
            case BINARY_EXPRESSION:
                return graphBinaryExpression((BoundBinaryExpression) expression, ip);
            case BLOCK:
                return graphBlockExpression((BoundBlockExpression) expression, ip);
            case TYPEOF_INTRINSIC:
                return graphTypeOfExpression((BoundTypeofExpression) expression, ip);
            case PRINT_INTRINSIC:
                return graphPrintExpression((BoundPrintExpression) expression, ip);
            case VARIABLE_DECLARATION:
                if (expression instanceof BoundConstDeclarationExpression) {
                    return graphConstDeclarationExpression((BoundConstDeclarationExpression) expression, ip);
                }
                return graphVariableDeclarationExpression((BoundVariableDeclarationExpression) expression, ip);
            case ASSIGNMENT_EXPRESSION:
                return graphAssignmentExpression((BoundAssignmentExpression) expression, ip);
            case LITERAL:
                return mutNode("lit_" + ip).add(Label.of(((BoundLiteralExpression) expression).getValue().toString()));
            case NOOP:
                return null;
            case GOTO:
                return graphGoto((BoundGotoExpression) expression, ip);
            case CONDITIONAL_GOTO:
                return graphConditionalGoto((BoundConditionalGotoExpression)expression, ip);
            case LABEL:
                return graphLabel((BoundLabelExpression) expression, ip);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private MutableNode graphConditionalGoto(BoundConditionalGotoExpression conditionalGotoExpression, int ip) {
        MutableNode gotoNode = mutNode(String.valueOf(ip)).add(Label.of("CONDITIONAL GOTO")).add(Shape.DIAMOND);
        MutableNode labelNode = mutNode("label_" + labelToIp.get(conditionalGotoExpression.getLabel())).add(Label.of(conditionalGotoExpression.getLabel().getName())).add(Shape.OVAL);
        MutableNode condition = graphExpression(conditionalGotoExpression.getCondition(), ip);

        gotoNode.addLink(condition);
        gotoNode.addLink(mutNode("to_label_" + ip).add(Label.of(conditionalGotoExpression.jumpIfFalse() ? "false" : "true")).addLink(labelNode));
        gotoNode.addLink(mutNode("to_next_instr_" + ip).add(Label.of(conditionalGotoExpression.jumpIfFalse() ? "true" : "false")).addLink(mutNode(String.valueOf(ip+1))));

        return gotoNode;
    }

    private MutableNode graphGoto(BoundGotoExpression gotoExpression, int ip) {
        MutableNode gotoNode = mutNode(String.valueOf(ip)).add(Label.of("GOTO")).add(Shape.BOX);
        MutableNode labelNode = mutNode("label_" + labelToIp.get(gotoExpression.getLabel())).add(Label.of(gotoExpression.getLabel().getName())).add(Shape.BOX);

        gotoNode.addLink(labelNode);
        return gotoNode;
    }

    private MutableNode graphLabel(BoundLabelExpression labelExpression, int ip) {
        return mutNode("label_" + ip).add(Label.of(labelExpression.getLabel().getName())).add(Shape.BOX);
    }

    private MutableNode graphAssignmentExpression(BoundAssignmentExpression assignmentExpression, int ip) {
        MutableNode root = mutNode(String.valueOf(ip)).add(Label.of("ASSIGN")).add(Shape.BOX);

        VariableSymbol variable = assignmentExpression.getVariable();
        MutableNode identifier = mutNode("id_" + ip).add(Label.of(variable.getType().getName() + ": " + variable.getName()));
        MutableNode initialiser = graphExpression(assignmentExpression.getExpression(), ip);

        root.addLink(identifier);
        root.addLink(initialiser);

        return root;
    }

    private MutableNode graphConstDeclarationExpression(BoundConstDeclarationExpression constDeclarationExpression, int ip) {
        MutableNode root = mutNode(String.valueOf(ip)).add(Label.of("CONST")).add(Shape.BOX);

        MutableNode identifier = mutNode("id_" + ip).add(Label.of(constDeclarationExpression.getVariable().getName()));

        Object constVal = constDeclarationExpression.getConstValue().getValue();
        if (constVal instanceof BoundLiteralExpression) {
            constVal = ((BoundLiteralExpression) constVal).getValue();
        }
        MutableNode constValue = mutNode("const_id_" + ip).attrs().add(Label.of(constVal.toString()));

        root.addLink(identifier);
        root.addLink(constValue);

        return root;
    }

    private MutableNode graphVariableDeclarationExpression(BoundVariableDeclarationExpression variableDeclarationExpression, int ip) {
        MutableNode root;
        if (variableDeclarationExpression.isReadOnly()) {
            root = mutNode(String.valueOf(ip)).add(Label.of("CONST")).add(Shape.BOX);
        } else {
            root = mutNode(String.valueOf(ip)).add(Label.of("VAR")).add(Shape.BOX);
        }

        MutableNode identifier = mutNode("id_" + ip).add(Label.of(variableDeclarationExpression.getType().getName() + ": " + variableDeclarationExpression.getVariable().getName()));
        MutableNode initialiser = graphExpression(variableDeclarationExpression.getInitialiser(), ip);

        root.addLink(identifier);

        if (variableDeclarationExpression.getRange() != null) {
            MutableNode rangeRoot = mutNode("range_" + ip).add(Label.of("RANGE")).add(Shape.BOX);
            MutableNode range = graphExpression(variableDeclarationExpression.getRange(), ip);

            rangeRoot.addLink(range);
            root.addLink(rangeRoot);
        }
        root.addLink(initialiser);

        return root;
    }

    private MutableNode graphBlockExpression(BoundBlockExpression blockExpression, int ip) {
        MutableNode root = mutNode(String.valueOf(ip)).attrs().add(Label.of("SCOPE")).add(Shape.BOX);

        for (BoundExpression expression : blockExpression.getExpressions()) {
            MutableNode node = graphExpression(expression, ip);
            root.addLink(node);
        }
        return root;
    }

    private MutableNode graphSyntaxExpression(BoundVariableExpression expression, int ip) {
        return mutNode("var_" + ip).attrs().add(Label.of(expression.getVariable().getName()));
    }

    private MutableNode graphBinaryExpression(BoundBinaryExpression binaryExpression, int ip) {

        MutableNode left = graphExpression(binaryExpression.getLeft(), ip);
        MutableNode op = mutNode("binop_" + ip).add(Label.of(getSymbol(binaryExpression.getOperator()))).attrs().add(Shape.BOX);
        MutableNode right = graphExpression(binaryExpression.getRight(), ip);

        op.addLink(left);
        op.addLink(right);

        return op;
    }

    private String getSymbol(BoundBinaryOperator operation) {
        switch (operation.getOpType()) {

            case ADD:
                return "+";
            case SUB:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "mod";
            case EQ:
                return "==";
            case NEQ:
                return "!=";
            case GT:
                return ">";
            case LT:
                return "<";
            case GTEQ:
                return ">=";
            case LTEQ:
                return "<=";
            case LAND:
                return "AND";
            case LOR:
                return "OR";
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
    }

    private MutableNode graphPrintExpression(BoundPrintExpression printExpression, int ip) {
        MutableNode expr = graphExpression(printExpression.getExpression(), ip);
        MutableNode call = mutNode(String.valueOf(ip)).add(Label.of("PRINT")).add(Shape.PARALLELOGRAM);

        call.addLink(expr);

        return call;
    }

    private MutableNode graphTypeOfExpression(BoundTypeofExpression typeofExpression, int ip) {
        MutableNode expr = graphExpression(typeofExpression.getExpression(), ip);
        MutableNode call = mutNode(String.valueOf(ip)).add(Label.of("TYPEOF")).add(Shape.BOX);

        call.addLink(expr);

        return call;
    }
}
