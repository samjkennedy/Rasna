package com.skennedy.rasna.graphing;

import com.skennedy.rasna.typebinding.*;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class HighLevelTreeGrapher {

    private static int id;

    public HighLevelTreeGrapher() {
        Graphviz.useEngine(new GraphvizV8Engine());
        id = 0;
    }

    public void graphAST(BoundProgram program) throws IOException {
        MutableGraph graph = mutGraph("Graph Test")
                .setDirected(true).use((gr, ctx) -> graphProgram(program));

        Graphviz.fromGraph(graph).render(Format.SVG).toFile(new File("graphs/highlevel.svg"));
    }

    private void graphProgram(BoundProgram program) {
        MutableNode root = mutNode(nextId()).add(Label.of("PROGRAM")).add(Shape.BOX);
        List<BoundExpression> expressions = program.getExpressions();
        for (BoundExpression expression : expressions) {
            MutableNode curr = graphExpression(expression);
            if (curr != null) {
                root.addLink(curr);
            }
        }
        MutableNode halt = mutNode(nextId()).add(Label.of("HALT")).add(Shape.BOX);
        root.addLink(halt);
    }

    private MutableNode graphExpression(BoundExpression expression) {
        switch (expression.getBoundExpressionType()) {
            case VARIABLE_EXPRESSION:
                return graphSyntaxExpression((BoundVariableExpression) expression);
            case BINARY_EXPRESSION:
                return graphBinaryExpression((BoundBinaryExpression) expression);
            case BLOCK:
                return graphBlockExpression((BoundBlockExpression) expression);
            case TYPEOF_INTRINSIC:
                return graphTypeOfExpression((BoundTypeofExpression) expression);
            case PRINT_INTRINSIC:
                return graphPrintExpression((BoundPrintExpression) expression);
//            case PARENTHESISED_EXPR:
//                return graphExpression(((ParenthesisedExpression)expression).getExpression());
            case IF:
                return graphIfExpression((BoundIfExpression) expression);
            case WHILE: //TODO: replace with goto
                return graphWhileExpression((BoundWhileExpression) expression);
            case FOR:
                return graphForExpression((BoundForExpression) expression);
            case FOR_IN:
                return graphForInExpression((BoundForInExpression) expression);
            case VARIABLE_DECLARATION:
                if (expression instanceof BoundConstDeclarationExpression) {
                    return graphConstDeclarationExpression((BoundConstDeclarationExpression) expression);
                }
                return graphVariableDeclarationExpression((BoundVariableDeclarationExpression) expression);
            case ASSIGNMENT_EXPRESSION:
                return graphAssignmentExpression((BoundAssignmentExpression) expression);
            case LITERAL:
                return mutNode(nextId()).add(Label.of(((BoundLiteralExpression) expression).getValue().toString()));
            case NOOP:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private MutableNode graphAssignmentExpression(BoundAssignmentExpression assignmentExpression) {
        MutableNode root = mutNode(nextId()).add(Label.of("ASSIGN")).add(Shape.BOX);

        VariableSymbol variable = assignmentExpression.getVariable();
        MutableNode identifier = mutNode(nextId()).add(Label.of(variable.getType().getName() + ": " + variable.getName()));
        MutableNode initialiser = graphExpression(assignmentExpression.getExpression());

        root.addLink(identifier);
        root.addLink(initialiser);

        return root;
    }

    private MutableNode graphConstDeclarationExpression(BoundConstDeclarationExpression constDeclarationExpression) {
        MutableNode root = mutNode(nextId()).add(Label.of("CONST")).add(Shape.BOX);

        MutableNode identifier = mutNode(nextId()).add(Label.of(constDeclarationExpression.getVariable().getName()));

        Object constVal = constDeclarationExpression.getConstValue().getValue();
        if (constVal instanceof BoundLiteralExpression) {
            constVal = ((BoundLiteralExpression) constVal).getValue();
        }
        MutableNode constValue = mutNode(nextId()).attrs().add(Label.of(constVal.toString()));

        root.addLink(identifier);
        root.addLink(constValue);

        return root;
    }

    private MutableNode graphVariableDeclarationExpression(BoundVariableDeclarationExpression variableDeclarationExpression) {
        MutableNode root;
        if (variableDeclarationExpression.isReadOnly()) {
            root = mutNode(nextId()).add(Label.of("CONST")).add(Shape.BOX);
        } else {
            root = mutNode(nextId()).add(Label.of("ANY")).add(Shape.BOX);
        }

        MutableNode identifier = mutNode(nextId()).add(Label.of(variableDeclarationExpression.getVariable().getType() + ": " + variableDeclarationExpression.getVariable().getName()));
        MutableNode initialiser = graphExpression(variableDeclarationExpression.getInitialiser());

        root.addLink(identifier);

        if (variableDeclarationExpression.getGuard() != null) {
            MutableNode guardRoot = mutNode(nextId()).add(Label.of("GUARD")).add(Shape.BOX);
            MutableNode guard = graphExpression(variableDeclarationExpression.getGuard());

            guardRoot.addLink(guard);
            root.addLink(guardRoot);
        }
        root.addLink(initialiser);

        return root;
    }

    private MutableNode graphIfExpression(BoundIfExpression expression) {
        MutableNode root = mutNode(nextId()).attrs().add(Label.of("IF")).add(Shape.DIAMOND);

        MutableNode condition = graphExpression(expression.getCondition());
        MutableNode body = graphExpression(expression.getBody());

        root.addLink(condition);
        root.addLink(body);

        if (expression.getElseBody() != null) {
            MutableNode elseBody = graphExpression(expression.getElseBody());
            root.addLink(elseBody);
        }

        return root;
    }

    private MutableNode graphWhileExpression(BoundWhileExpression whileExpression) {
        MutableNode root = mutNode(nextId()).attrs().add(Label.of("WHILE")).add(Shape.DIAMOND);

        MutableNode condition = graphExpression(whileExpression.getCondition());
        MutableNode body = graphExpression(whileExpression.getBody());

        root.addLink(condition);
        root.addLink(body);
        body.addLink(root);

        return root;
    }

    private MutableNode graphForExpression(BoundForExpression forExpression) {
        MutableNode root = mutNode(nextId()).attrs().add(Label.of("FOR")).add(Shape.DIAMOND);

        MutableNode declaration = mutNode(nextId()).add(Label.of("FROM")).add(Shape.BOX);

        MutableNode identifier = mutNode(nextId()).add(Label.of(forExpression.getRangeExpression().getLowerBound().getType().getName() + ": " + forExpression.getIterator().getName()));
        MutableNode initialiser = graphExpression(forExpression.getRangeExpression().getLowerBound());

        declaration.addLink(identifier);
        declaration.addLink(initialiser);

        MutableNode to = mutNode(nextId()).add(Label.of("TO")).add(Shape.BOX);
        MutableNode terminator = graphExpression(forExpression.getRangeExpression().getUpperBound());

        to.addLink(terminator);
        root.addLink(declaration);
        root.addLink(to);

        if (forExpression.getRangeExpression().getStep() != null) {
            MutableNode by = mutNode(nextId()).add(Label.of("BY")).add(Shape.BOX);
            MutableNode step = graphExpression(forExpression.getRangeExpression().getStep());
            by.addLink(step);
            root.addLink(by);
        }

        if (forExpression.getGuard() != null) {
            MutableNode guardRoot = mutNode(nextId()).add(Label.of("GUARD")).add(Shape.BOX);
            MutableNode guard = graphExpression(forExpression.getGuard());
            guardRoot.addLink(guard);
            root.addLink(guardRoot);
        }

        MutableNode body = graphExpression(forExpression.getBody());
        root.addLink(body);

        return root;
    }

    private MutableNode graphForInExpression(BoundForInExpression forInExpression) {

        MutableNode root = mutNode(nextId()).attrs().add(Label.of("FOR")).add(Shape.DIAMOND);

        MutableNode declaration = mutNode(nextId()).add(Label.of("IN")).add(Shape.BOX);

        MutableNode identifier = mutNode(nextId()).add(Label.of(forInExpression.getIterable().getType().getName() + ": " + forInExpression.getVariable().getName()));
        MutableNode range = graphExpression(forInExpression.getIterable());

        declaration.addLink(identifier);
        declaration.addLink(range);

        root.addLink(declaration);

        MutableNode body = graphExpression(forInExpression.getBody());
        root.addLink(body);

        return root;
    }

    private MutableNode graphBlockExpression(BoundBlockExpression blockExpression) {
        MutableNode root = mutNode(nextId()).attrs().add(Label.of("SCOPE")).add(Shape.BOX);

        for (BoundExpression expression : blockExpression.getExpressions()) {
            MutableNode node = graphExpression(expression);
            root.addLink(node);
        }
        return root;
    }

    private MutableNode graphSyntaxExpression(BoundVariableExpression expression) {
        return mutNode(nextId()).attrs().add(Label.of(expression.getVariable().getName()));
    }

    private MutableNode graphBinaryExpression(BoundBinaryExpression binaryExpression) {

        MutableNode left = graphExpression(binaryExpression.getLeft());
        MutableNode op = mutNode(nextId()).add(Label.of(getSymbol(binaryExpression.getOperator()))).attrs().add(Shape.BOX);
        MutableNode right = graphExpression(binaryExpression.getRight());

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

    private MutableNode graphPrintExpression(BoundPrintExpression printExpression) {
        MutableNode expr = graphExpression(printExpression.getExpression());
        MutableNode call = mutNode(nextId()).add(Label.of("PRINT")).add(Shape.PARALLELOGRAM);

        call.addLink(expr);

        return call;
    }

    private MutableNode graphTypeOfExpression(BoundTypeofExpression typeofExpression) {
        MutableNode expr = graphExpression(typeofExpression.getExpression());
        MutableNode call = mutNode(nextId()).add(Label.of("TYPEOF")).add(Shape.BOX);

        call.addLink(expr);

        return call;
    }

    private static String nextId() {
        return String.valueOf(++id);
    }
}
