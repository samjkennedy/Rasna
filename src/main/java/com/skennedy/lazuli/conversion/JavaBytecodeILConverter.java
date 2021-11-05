package com.skennedy.lazuli.conversion;

import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.lowering.BoundConditionalGotoExpression;
import com.skennedy.lazuli.lowering.BoundGotoExpression;
import com.skennedy.lazuli.lowering.BoundLabel;
import com.skennedy.lazuli.lowering.BoundLabelExpression;
import com.skennedy.lazuli.typebinding.BoundArrayAccessExpression;
import com.skennedy.lazuli.typebinding.BoundArrayAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundArrayLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryOperator;
import com.skennedy.lazuli.typebinding.BoundBlockExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundExpressionType;
import com.skennedy.lazuli.typebinding.BoundFunctionArgumentExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionCallExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundIncrementExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundReturnExpression;
import com.skennedy.lazuli.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundVariableExpression;
import com.skennedy.lazuli.typebinding.FunctionSymbol;
import com.skennedy.lazuli.typebinding.TypeSymbol;
import com.skennedy.lazuli.typebinding.VariableSymbol;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Textifier;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DOUBLE;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INTEGER;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V11;

public class JavaBytecodeILConverter implements ILConverter {

    private static final Logger log = LogManager.getLogger(JavaBytecodeILConverter.class);

    private final ClassWriter classWriter;
    private Textifier textifierVisitor;

    private BoundProgram program;

    private String className;

    private int variableIndex;

    //TODO: Scopes
    private Map<String, Integer> variables;
    private Map<BoundLabel, Label> boundLabelToProgramLabel;


    //Multiple branches can visit the same label, e.g. nested if/else if/else if/else
    //but we only want to visit them once
    //This keeps track of each label already visited so we don't revisit them
    private Set<Label> visitedLabels;

    private static int ip = 0;
    private int constantIdx;
    private Map<Object, Integer> constantPool;

    private Scope scope;

    public JavaBytecodeILConverter() {
        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // Variable0 is reserved for args[] in :  `main(String[] var0)`
        this.variableIndex = 1;
        variables = new HashMap<>();
        boundLabelToProgramLabel = new HashMap<>();

        visitedLabels = new HashSet<>();
        constantIdx = 2;
        constantPool = new HashMap<>();

        scope = new Scope(null, null);
    }

    @Override
    public void convert(BoundProgram program, String outputFileName) throws IOException {

        this.program = program;

        className = outputFileName;

        textifierVisitor = new Textifier();

        classWriter.visit(
                V11, // Java 1.8
                ACC_PUBLIC + ACC_SUPER, // public static
                outputFileName, // Class Name
                null, // Generics <T>
                "java/lang/Object", // Interface extends Object (Super Class),
                null // interface names
        );
        textifierVisitor.visitMainClass(outputFileName);

        //Forward declare all methods
        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundFunctionDeclarationExpression) {
                scope = new Scope(null, null);
                visit((BoundFunctionDeclarationExpression) expression);
            }
        }

        scope = new Scope(null, null);
        scope.pushLocal("[Ljava/lang/String;");

        /** ASM = CODE : public static void main(String args[]). */
        // BEGIN 2: creates a MethodVisitor for the 'main' method
        MethodVisitor mainMethodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        textifierVisitor.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        for (BoundExpression expression : program.getExpressions()) {
            //Already declared, skip
            if (expression instanceof BoundFunctionDeclarationExpression) {
                ip++;
                continue;
            }
            printExpression(expression, 0);
            visit(expression, mainMethodVisitor);
            ip++;
        }
        mainMethodVisitor.visitInsn(RETURN);
        textifierVisitor.visitInsn(RETURN);
        scope = scope.parent;

        //TODO: determine how much memory the program requires and set accordingly
        mainMethodVisitor.visitMaxs(1000, 1000);
        textifierVisitor.visitMaxs(1000, 1000);

        // END 2: Close main()
        mainMethodVisitor.visitEnd();
        textifierVisitor.visitMethodEnd();

        // END 1: Close class()
        classWriter.visitEnd();
        textifierVisitor.visitClassEnd();

        byte[] code = classWriter.toByteArray();

        File bytecodeFile = new File(outputFileName + "_bytecode.txt");
        bytecodeFile.createNewFile();
        PrintWriter fileWriter = new PrintWriter(bytecodeFile);
        textifierVisitor.print(fileWriter);
        fileWriter.close();

        File outputFile = new File(outputFileName + ".class");
        outputFile.createNewFile();
        FileUtils.writeByteArrayToFile(outputFile, code);
    }

    private void printExpression(BoundExpression expression, int depth) {
        StringBuilder pre;
        if (depth == 0) {
            pre = new StringBuilder(ip + ": ");
        } else {
            pre = new StringBuilder();
            pre.append("    ".repeat(Math.max(0, depth - 1)));
            pre.append("  > ");
        }
        log.debug(pre.toString() + expression.getBoundExpressionType());
        for (Iterator<BoundExpression> it = expression.getChildren(); it.hasNext(); ) {
            BoundExpression child = it.next();
            if (child == null) continue;
            printExpression(child, depth + 1);
        }
    }

    private void visit(BoundExpression expression, MethodVisitor methodVisitor) {
        switch (expression.getBoundExpressionType()) {

            case ASSIGNMENT_EXPRESSION:

                visit((BoundAssignmentExpression) expression, methodVisitor);
                break;
            case ARRAY_LITERAL_EXPRESSION:

                visit((BoundArrayLiteralExpression) expression, methodVisitor);
                break;
            case ARRAY_ACCESS_EXPRESSION:

                visit((BoundArrayAccessExpression) expression, methodVisitor);
                break;
            case ARRAY_ASSIGNMENT_EXPRESSION:
                visit((BoundArrayAssignmentExpression) expression, methodVisitor);
                break;
            case ARRAY_LENGTH_EXPRESSION:

                visit((BoundArrayLengthExpression) expression, methodVisitor);
                break;
            case LITERAL:

                visit((BoundLiteralExpression) expression, methodVisitor);
                break;
            case BINARY_EXPRESSION:

                visit((BoundBinaryExpression) expression, methodVisitor);
                break;
            case BLOCK:

                for (BoundExpression expr : ((BoundBlockExpression) expression).getExpressions()) {
                    visit(expr, methodVisitor);
                }
                break;
            case VARIABLE_DECLARATION:

                visit((BoundVariableDeclarationExpression) expression, methodVisitor);
                break;
            case VARIABLE_EXPRESSION:

                visit((BoundVariableExpression) expression, methodVisitor);
                break;
            case PRINT_INTRINSIC:

                visit((BoundPrintExpression) expression, methodVisitor);
                break;
            case CONDITIONAL_GOTO:
                visit((BoundConditionalGotoExpression) expression, methodVisitor);
                break;
            case GOTO:
                visit((BoundGotoExpression) expression, methodVisitor);
                break;
            case LABEL:
                BoundLabelExpression labelExpression = (BoundLabelExpression) expression;
                Label label;
                if (!boundLabelToProgramLabel.containsKey(labelExpression.getLabel())) {
                    label = new Label();
                    boundLabelToProgramLabel.put(labelExpression.getLabel(), label);
                }
                label = boundLabelToProgramLabel.get(labelExpression.getLabel());

                visit(label, methodVisitor);

                break;
            case FUNCTION_DECLARATION:
                visit((BoundFunctionDeclarationExpression) expression);
                break;
            case INCREMENT:
                visit((BoundIncrementExpression) expression, methodVisitor);
                break;
            case FUNCTION_CALL:
                visit((BoundFunctionCallExpression) expression, methodVisitor);
                break;
            case RETURN:
                visit((BoundReturnExpression) expression, methodVisitor);
                break;
            case NOOP:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private void visit(BoundLiteralExpression boundLiteralExpression, MethodVisitor methodVisitor) {

        Object value = boundLiteralExpression.getValue();
        if (value instanceof Integer) {
            switch ((int) value) {
                case 0:
                    methodVisitor.visitInsn(ICONST_0);
                    textifierVisitor.visitInsn(ICONST_0);
                    break;
                case 1:
                    methodVisitor.visitInsn(ICONST_1);
                    textifierVisitor.visitInsn(ICONST_1);
                    break;
                case 2:
                    methodVisitor.visitInsn(ICONST_2);
                    textifierVisitor.visitInsn(ICONST_2);
                    break;
                case 3:
                    methodVisitor.visitInsn(ICONST_3);
                    textifierVisitor.visitInsn(ICONST_3);
                    break;
                case 4:
                    methodVisitor.visitInsn(ICONST_4);
                    textifierVisitor.visitInsn(ICONST_4);
                    break;
                case 5:
                    methodVisitor.visitInsn(ICONST_5);
                    textifierVisitor.visitInsn(ICONST_5);
                    break;
                default:

                    int operand = Integer.parseInt(value.toString());

                    if (operand < Byte.MAX_VALUE) {

                        methodVisitor.visitIntInsn(BIPUSH, operand);
                        textifierVisitor.visitIntInsn(BIPUSH, operand);

                    } else if (operand < Short.MAX_VALUE) {

                        methodVisitor.visitIntInsn(SIPUSH, operand);
                        textifierVisitor.visitIntInsn(SIPUSH, operand);

                    } else if (operand < Integer.MAX_VALUE) {

                        if (!constantPool.containsKey(operand)) {
                            constantPool.put(operand, constantIdx);
                            constantIdx++;
                        }
                        methodVisitor.visitLdcInsn(operand);
                        textifierVisitor.visitLdcInsn(operand);
                    }
            }
        } else if (value instanceof Boolean) {
            methodVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
            textifierVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
        } else if (value instanceof Double) {
            double operand = (double) value;
            if (operand == 0.0d) {
                methodVisitor.visitInsn(DCONST_0);
                textifierVisitor.visitInsn(DCONST_0);
            } else if (operand == 1.0d) {
                methodVisitor.visitInsn(DCONST_1);
                textifierVisitor.visitInsn(DCONST_1);
            } else {
                if (!constantPool.containsKey(operand)) {
                    constantPool.put(operand, constantIdx);
                    constantIdx++;
                }
                methodVisitor.visitLdcInsn(operand);
                textifierVisitor.visitLdcInsn(operand);
            }
        } else if (value instanceof String) {
            String operand = (String) value;
            if (!constantPool.containsKey(operand)) {
                constantPool.put(operand, constantIdx);
                constantIdx++;
            }
            methodVisitor.visitLdcInsn(operand);
            textifierVisitor.visitLdcInsn(operand);
        } else {
            throw new UnsupportedOperationException("Literals of type " + boundLiteralExpression.getType().getName() + " are not yet supported");
        }
        scope.pushStack(boundLiteralExpression.getType());
    }

    private void visit(BoundVariableDeclarationExpression variableDeclarationExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        String identifer = variable.getName();

        //Evaluate the initialiser
        visit(variableDeclarationExpression.getInitialiser(), methodVisitor);

        //Store that in memory at the current variable index
        if (variableDeclarationExpression.getType() == TypeSymbol.INT || variableDeclarationExpression.getType() == TypeSymbol.BOOL) {
            methodVisitor.visitVarInsn(ISTORE, variableIndex);
            textifierVisitor.visitVarInsn(ISTORE, variableIndex);
        } else if (variableDeclarationExpression.getType() == TypeSymbol.INT_ARRAY) {
            methodVisitor.visitVarInsn(ASTORE, variableIndex);
            textifierVisitor.visitVarInsn(ASTORE, variableIndex);
        } else if (variableDeclarationExpression.getType() == TypeSymbol.REAL) {
            methodVisitor.visitVarInsn(DSTORE, variableIndex);
            textifierVisitor.visitVarInsn(DSTORE, variableIndex);
        } else if (variableDeclarationExpression.getType() == TypeSymbol.STRING) {
            methodVisitor.visitVarInsn(ASTORE, variableIndex);
            textifierVisitor.visitVarInsn(ASTORE, variableIndex);
        } else {
            throw new UnsupportedOperationException("Variables of type " + variableDeclarationExpression.getType().getName() + " are not yet supported");
        }
        scope.popStack();

        //Keep track of variable for later use
        variables.put(identifer, variableIndex);
        scope.declareVariable(variable, variableIndex);

        //Update the stackmap
        scope.pushLocal(variableDeclarationExpression.getType());
        if (variableDeclarationExpression.getType() == TypeSymbol.REAL) {
            variableIndex += 2;
        } else {
            variableIndex++;
        }
    }

    private Object getStackTypeCode(TypeSymbol type) {
        if (type == TypeSymbol.REAL) {
            return DOUBLE;
        }
        if (type == TypeSymbol.INT || type == TypeSymbol.BOOL) {
            return INTEGER;
        }
        if (type == TypeSymbol.INT_ARRAY) {
            return "[I";
        }
        if (type == TypeSymbol.STRING) {
            return "Ljava/lang/String;";
        }
        throw new UnsupportedOperationException("Compilation is not yet supported for type: " + type.getName());
    }

    private void visit(BoundIncrementExpression incrementExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = incrementExpression.getVariableSymbol();
        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        int variableIdx = variables.get(variable.getName());

        methodVisitor.visitIincInsn(variableIdx, (int) incrementExpression.getAmount().getValue());
        textifierVisitor.visitIincInsn(variableIdx, (int) incrementExpression.getAmount().getValue());

        //Gotta return something?
    }

    private void visit(BoundVariableExpression boundVariableExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = boundVariableExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        int variableIdx = variables.get(variable.getName());
        if (boundVariableExpression.getType() == TypeSymbol.INT || boundVariableExpression.getType() == TypeSymbol.BOOL) {
            methodVisitor.visitVarInsn(ILOAD, variableIdx);
            textifierVisitor.visitVarInsn(ILOAD, variableIdx);
        } else if (boundVariableExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            methodVisitor.visitVarInsn(ALOAD, variableIdx);
            textifierVisitor.visitVarInsn(ALOAD, variableIdx);
        } else if (boundVariableExpression.getType() == TypeSymbol.REAL) {
            methodVisitor.visitVarInsn(DLOAD, variableIdx);
            textifierVisitor.visitVarInsn(DLOAD, variableIdx);
        } else if (boundVariableExpression.getType() == TypeSymbol.STRING) {
            methodVisitor.visitVarInsn(ALOAD, variableIdx);
            textifierVisitor.visitVarInsn(ALOAD, variableIdx);
        } else {
            throw new UnsupportedOperationException("Variables of type " + boundVariableExpression.getType().getName() + " are not yet supported");
        }
        scope.pushStack(variable.getType());
    }

    private void visit(BoundAssignmentExpression assignmentExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = assignmentExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        visit(assignmentExpression.getExpression(), methodVisitor);

        int variableIdx = variables.get(variable.getName());

        if (assignmentExpression.getType() == TypeSymbol.INT || assignmentExpression.getType() == TypeSymbol.BOOL) {
            methodVisitor.visitVarInsn(ISTORE, variableIdx);
            textifierVisitor.visitVarInsn(ISTORE, variableIdx);
        } else if (assignmentExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            methodVisitor.visitVarInsn(ASTORE, variableIdx);
            textifierVisitor.visitVarInsn(ASTORE, variableIdx);
        } else if (assignmentExpression.getType().isAssignableFrom(TypeSymbol.STRING)) {
            methodVisitor.visitVarInsn(ASTORE, variableIdx);
            textifierVisitor.visitVarInsn(ASTORE, variableIdx);
        } else if (assignmentExpression.getType().isAssignableFrom(TypeSymbol.REAL)) {
            methodVisitor.visitVarInsn(DSTORE, variableIdx);
            textifierVisitor.visitVarInsn(DSTORE, variableIdx);
        } else {
            throw new UnsupportedOperationException("Variables of type " + assignmentExpression.getType().getName() + " are not yet supported");
        }

        scope.popStack();
    }

    private void visit(BoundArrayAssignmentExpression arrayAssignmentExpression, MethodVisitor methodVisitor) {

        visit(arrayAssignmentExpression.getArrayAccessExpression().getArray(), methodVisitor);
        visit(arrayAssignmentExpression.getArrayAccessExpression().getIndex(), methodVisitor);
        visit(arrayAssignmentExpression.getAssignment(), methodVisitor);

        methodVisitor.visitInsn(IASTORE);
        textifierVisitor.visitInsn(IASTORE);

        scope.popStack();
        scope.popStack();
        scope.popStack();
    }

    private void visit(BoundArrayLiteralExpression arrayLiteralExpression, MethodVisitor methodVisitor) {

        List<BoundExpression> elements = arrayLiteralExpression.getElements();

        visit(new BoundLiteralExpression(elements.size()), methodVisitor);

        methodVisitor.visitIntInsn(NEWARRAY, T_INT);
        textifierVisitor.visitIntInsn(NEWARRAY, T_INT);

        int index = 0;
        for (BoundExpression element : elements) {
            methodVisitor.visitInsn(DUP);
            textifierVisitor.visitInsn(DUP);
            visit(new BoundLiteralExpression(index), methodVisitor);
            visit(element, methodVisitor);
            methodVisitor.visitInsn(IASTORE);
            textifierVisitor.visitInsn(IASTORE);

            scope.popStack();
            scope.popStack();
            index++;
        }
    }

    private void visit(BoundArrayAccessExpression arrayAccessExpression, MethodVisitor methodVisitor) {

        visit(arrayAccessExpression.getArray(), methodVisitor);
        visit(arrayAccessExpression.getIndex(), methodVisitor);

        methodVisitor.visitInsn(IALOAD);
        textifierVisitor.visitInsn(IALOAD);

        scope.popStack(); //Array index
        scope.popStack(); //Array ref
        scope.pushStack(TypeSymbol.INT); //Value from array
    }

    private void visit(BoundArrayLengthExpression arrayLengthExpression, MethodVisitor methodVisitor) {

        visit(arrayLengthExpression.getIterable(), methodVisitor);

        methodVisitor.visitInsn(ARRAYLENGTH);
        textifierVisitor.visitInsn(ARRAYLENGTH);

        scope.popStack();
        scope.pushStack(TypeSymbol.INT);
    }

    private void visit(BoundBinaryExpression binaryExpression, MethodVisitor methodVisitor) {

        visit(binaryExpression.getLeft(), methodVisitor);
        visit(binaryExpression.getRight(), methodVisitor);

        if (binaryExpression.getLeft().getType() == TypeSymbol.INT || binaryExpression.getLeft().getType() == TypeSymbol.BOOL) {
            visitIntBinaryExpression(binaryExpression, methodVisitor);
        } else if (binaryExpression.getLeft().getType() == TypeSymbol.REAL) {
            visitRealBinaryExpression(binaryExpression, methodVisitor);
        }
    }

    private void visitIntBinaryExpression(BoundBinaryExpression binaryExpression, MethodVisitor methodVisitor) {
        switch (binaryExpression.getOperator().getBoundOpType()) {
            case ADDITION:
                methodVisitor.visitInsn(IADD);
                textifierVisitor.visitInsn(IADD);

                scope.popStack();
                break;
            case SUBTRACTION:
                methodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);

                scope.popStack();
                break;
            case MULTIPLICATION:
                methodVisitor.visitInsn(IMUL);
                textifierVisitor.visitInsn(IMUL);

                scope.popStack();
                break;
            case DIVISION:
                methodVisitor.visitInsn(IDIV);
                textifierVisitor.visitInsn(IDIV);

                scope.popStack();
                break;
            case REMAINDER:
                methodVisitor.visitInsn(IREM);
                textifierVisitor.visitInsn(IREM);

                scope.popStack();
                break;
            case LESS_THAN:
                visitComparison(methodVisitor, IF_ICMPLT);
                break;
            case GREATER_THAN:
                visitComparison(methodVisitor, IF_ICMPGT);
                break;
            case LESS_THAN_OR_EQUAL:
                visitComparison(methodVisitor, IF_ICMPLE);
                break;
            case GREATER_THAN_OR_EQUAL:
                visitComparison(methodVisitor, IF_ICMPGE);
                break;
            case EQUALS:
                visitComparison(methodVisitor, IF_ICMPEQ);
                break;
            case NOT_EQUALS:
                visitComparison(methodVisitor, IF_ICMPNE);
                break;
            case BOOLEAN_OR:
                methodVisitor.visitInsn(IOR);
                textifierVisitor.visitInsn(IOR);

                scope.popStack();
                break;
            case BOOLEAN_AND:
                methodVisitor.visitInsn(IAND);
                textifierVisitor.visitInsn(IAND);

                scope.popStack();
                break;
            default:
                throw new IllegalStateException("Unhandled binary operation: " + binaryExpression.getOperator().getBoundOpType());
        }
    }

    private void visitRealBinaryExpression(BoundBinaryExpression binaryExpression, MethodVisitor methodVisitor) {
        switch (binaryExpression.getOperator().getBoundOpType()) {
            case ADDITION:
                methodVisitor.visitInsn(DADD);
                textifierVisitor.visitInsn(DADD);

                scope.popStack();
                break;
            case SUBTRACTION:
                methodVisitor.visitInsn(DSUB);
                textifierVisitor.visitInsn(DSUB);

                scope.popStack();
                break;
            case MULTIPLICATION:
                methodVisitor.visitInsn(DMUL);
                textifierVisitor.visitInsn(DMUL);

                scope.popStack();
                break;
            case DIVISION:
                methodVisitor.visitInsn(DDIV);
                textifierVisitor.visitInsn(DDIV);

                scope.popStack();
                break;
            case REMAINDER:
                methodVisitor.visitInsn(DREM);
                textifierVisitor.visitInsn(DREM);

                scope.popStack();
                break;
            default:
                throw new IllegalStateException("Unhandled binary operation: " + binaryExpression.getOperator().getBoundOpType());
        }
    }

    private void visitComparison(MethodVisitor methodVisitor, int instruction) {

        Label ifTrue = new Label();
        Label end = new Label();
        methodVisitor.visitJumpInsn(instruction, ifTrue);

        textifierVisitor.visitJumpInsn(instruction, ifTrue);
        scope.popStack();
        scope.popStack();

        methodVisitor.visitInsn(ICONST_0);
        textifierVisitor.visitInsn(ICONST_0);

        methodVisitor.visitJumpInsn(GOTO, end);
        textifierVisitor.visitJumpInsn(GOTO, end);

        visit(ifTrue, methodVisitor);

        methodVisitor.visitInsn(ICONST_1);
        textifierVisitor.visitInsn(ICONST_1);

        scope.pushStack(TypeSymbol.BOOL);

        visit(end, methodVisitor);
    }

    private void visit(BoundConditionalGotoExpression conditionalGotoExpression, MethodVisitor methodVisitor) {

        BoundExpression condition = conditionalGotoExpression.getCondition();

        Label label;
        if (boundLabelToProgramLabel.containsKey(conditionalGotoExpression.getLabel())) {
            label = boundLabelToProgramLabel.get(conditionalGotoExpression.getLabel());
        } else {
            label = new Label();
            boundLabelToProgramLabel.put(conditionalGotoExpression.getLabel(), label);
        }

        if (condition instanceof BoundBinaryExpression) {
            BoundBinaryExpression binaryCondition = (BoundBinaryExpression) condition;
            BoundBinaryOperator operator = binaryCondition.getOperator();
            visit(binaryCondition.getLeft(), methodVisitor);
            visit(binaryCondition.getRight(), methodVisitor);
            switch (operator.getBoundOpType()) {
                case GREATER_THAN:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPLE : IF_ICMPGT, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPLE : IF_ICMPGT, label);
                    break;
                case LESS_THAN:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPGE : IF_ICMPLT, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPGE : IF_ICMPLT, label);
                    break;
                case GREATER_THAN_OR_EQUAL:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPLT : IF_ICMPGE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPLT : IF_ICMPGE, label);
                    break;
                case LESS_THAN_OR_EQUAL:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPGT : IF_ICMPLE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPGT : IF_ICMPLE, label);
                    break;
                case EQUALS:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPNE : IF_ICMPEQ, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPNE : IF_ICMPEQ, label);
                    break;
                case NOT_EQUALS:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPEQ : IF_ICMPNE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IF_ICMPEQ : IF_ICMPNE, label);
                    break;
                default:
                    throw new IllegalStateException("Unsupported binary condition op type: " + operator.getBoundOpType());
            }
            scope.popStack(); //Pop right
            scope.popStack(); //Pop left
        } else {
            //Compare true/false to 0
            visit(condition, methodVisitor);
            methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
            textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
            scope.popStack(); //Pop condition
        }

        scope = new Scope(scope, label);
    }

    private void visit(Label label, MethodVisitor methodVisitor) {

        if (visitedLabels.contains(label)) {
            return;
        }
//
//        Object[] local = scope.getLocals().toArray(new Object[0]);
//        Object[] stack = scope.getStack().toArray(new Object[0]);
//        methodVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
//        textifierVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);

        if (!scope.isGlobalScope()) {
            scope = scope.parent;
        }

        methodVisitor.visitLabel(label);
        textifierVisitor.visitLabel(label);

        visitedLabels.add(label);
    }

    private void visit(BoundGotoExpression gotoExpression, MethodVisitor methodVisitor) {
        BoundLabel boundLabel = gotoExpression.getLabel();

        Label label;
        if (boundLabelToProgramLabel.containsKey(boundLabel)) {
            label = boundLabelToProgramLabel.get(boundLabel);
        } else {
            label = new Label();
            boundLabelToProgramLabel.put(boundLabel, label);
        }

        methodVisitor.visitJumpInsn(GOTO, label);
        textifierVisitor.visitJumpInsn(GOTO, label);
    }

    private void visit(BoundFunctionDeclarationExpression functionDeclarationExpression) {

        FunctionSymbol functionSymbol = functionDeclarationExpression.getFunctionSymbol();

        List<TypeSymbol> argumentTypes = functionDeclarationExpression.getArguments().stream()
                .map(BoundFunctionArgumentExpression::getType)
                .collect(Collectors.toList());

        MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PRIVATE + ACC_STATIC, //TODO: Access levels
                functionSymbol.getName(),
                getMethodDescriptor(argumentTypes, functionDeclarationExpression.getFunctionSymbol().getType()),
                null,
                null
        );
        textifierVisitor.visitMethod(
                ACC_PRIVATE + ACC_STATIC,
                functionSymbol.getName(),
                getMethodDescriptor(argumentTypes, functionDeclarationExpression.getFunctionSymbol().getType()),
                null,
                null
        );

        int globalVariableIndex = variableIndex;
        variableIndex = 0;
        for (BoundFunctionArgumentExpression argument : functionDeclarationExpression.getArguments()) {
            variables.put(argument.getArgument().getName(), variableIndex);
            scope.declareVariable(argument.getArgument(), variableIndex);
            scope.pushLocal(argument.getArgument().getType());
            if (argument.getType() == TypeSymbol.REAL) {
                variableIndex += 2;
            } else {
                variableIndex++;
            }
        }

        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {
            visit(expression, methodVisitor);
        }
        variableIndex = 0;

        //TODO: A bit of a hack to ensure exhaustive branch returns compile, in future do whatever the JVM does
        /* eg:
        Int choice(Bool check) {
            if (check) {
                return 1
            } else {
                return 2
            }
        }
         */
        if (functionDeclarationExpression.getBody().getExpressions().get(functionDeclarationExpression.getBody().getExpressions().size() - 1).getBoundExpressionType() != BoundExpressionType.RETURN) {
            methodVisitor.visitInsn(RETURN);
            textifierVisitor.visitInsn(RETURN);
        }

        //TODO: Keep track of method locals
        methodVisitor.visitMaxs(1024, 1024);
        textifierVisitor.visitMaxs(1024, 1024);
        variableIndex = globalVariableIndex;

        methodVisitor.visitEnd();
        textifierVisitor.visitMethodEnd();
    }

    private String getMethodDescriptor(List<TypeSymbol> argumentTypes, TypeSymbol returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (TypeSymbol type : argumentTypes) {
            sb.append(getType(type));
        }
        sb.append(")");
        sb.append(getType(returnType));

        return sb.toString();
    }

    private String getType(TypeSymbol type) {
        if (type == TypeSymbol.VOID) {
            return Type.VOID_TYPE.getDescriptor();
        }
        if (type == TypeSymbol.INT) {
            return Type.INT_TYPE.getDescriptor();
        }
        if (type == TypeSymbol.REAL) {
            return Type.DOUBLE_TYPE.getDescriptor();
        }
        if (type == TypeSymbol.BOOL) {
            return Type.BOOLEAN_TYPE.getDescriptor();
        }
        if (type == TypeSymbol.INT_ARRAY) {
            return "[I";
        }
        if (type == TypeSymbol.STRING) {
            return "Ljava/lang/String;";
        }
        throw new UnsupportedOperationException("Type " + type.getName() + " is not yet supported");
    }

    private void visit(BoundFunctionCallExpression functionCallExpression, MethodVisitor methodVisitor) {

        FunctionSymbol function = functionCallExpression.getFunction();

        scope = new Scope(scope, null);
        for (BoundExpression argumentInitialiser : functionCallExpression.getBoundArguments()) {
            visit(argumentInitialiser, methodVisitor);
        }

        List<TypeSymbol> argumentTypes = functionCallExpression.getBoundArguments().stream()
                .map(BoundExpression::getType)
                .collect(Collectors.toList());

        methodVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(argumentTypes, function.getType()), false);
        textifierVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(argumentTypes, function.getType()), false);

        //TODO: remove new variables declared in the variables map?

        scope = scope.parent;
        //Push return type onto stack
        if (functionCallExpression.getType() != TypeSymbol.VOID) {
            scope.pushStack(functionCallExpression.getType());
        }
    }

    private void visit(BoundReturnExpression returnExpression, MethodVisitor methodVisitor) {

        visit(returnExpression.getReturnValue(), methodVisitor);

        if (returnExpression.getReturnValue().getType() == (TypeSymbol.INT) || returnExpression.getReturnValue().getType() == TypeSymbol.BOOL) {
            methodVisitor.visitInsn(IRETURN);
            textifierVisitor.visitInsn(IRETURN);
        } else if (returnExpression.getReturnValue().getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
            methodVisitor.visitInsn(ARETURN);
            textifierVisitor.visitInsn(ARETURN);
        } else if (returnExpression.getReturnValue().getType() == TypeSymbol.REAL) {
            methodVisitor.visitInsn(DRETURN);
            textifierVisitor.visitInsn(DRETURN);
        } else {
            throw new UnsupportedOperationException("Functions that return type " + returnExpression.getReturnValue().getType() + " are not yet supported");
        }
    }

    private void visit(BoundPrintExpression printExpression, MethodVisitor methodVisitor) {

        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        textifierVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        visit(printExpression.getExpression(), methodVisitor);

        String descriptor = getMethodDescriptor(Collections.singletonList(printExpression.getExpression().getType()), TypeSymbol.VOID);

        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
        textifierVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
        scope.popStack();
    }

    private class Scope {

        //The label that caused the new scope
        private Label label;

        private Scope parent;
        private Map<VariableSymbol, Integer> variablesToLocalIndex;
        private Frame stackFrame;

        Scope(Scope parent, Label label) {
            this.parent = parent;
            this.label = label;

            this.stackFrame = new Frame();
            this.variablesToLocalIndex = new HashMap<>();
        }

        public void popLocals() {
            stackFrame.popLocals();
        }

        public Stack<Object> getLocals() {
            Scope s = this;
            Stack<Object> locals = new Stack<>();
            while (s != null) {
                Stack<Object> frameLocals = new Stack<>();
                s.stackFrame.locals.forEach(frameLocals::push);
                for (Object local : locals) {
                    frameLocals.push(local);
                }
                locals = frameLocals;
                s = s.parent;
            }
            return locals;
        }

        void pushLocal(TypeSymbol type) {
            stackFrame.pushLocal(type);
        }

        void pushLocal(String type) {
            stackFrame.pushLocal(type);
        }

        void popStack() {
            Object type = stackFrame.popStack();
            if (type == TypeSymbol.REAL) {
                stackFrame.popStack();
            }
        }

        void pushStack(TypeSymbol type) {
            stackFrame.pushStack(type);
            if (type == TypeSymbol.REAL) {
                stackFrame.pushStack(type);
            }
        }

        public Stack<Object> getStack() {
            Scope s = this;
            Stack<Object> stack = new Stack<>();
            while (s != null) {
                Stack<Object> frameStack = new Stack<>();
                s.stackFrame.stack.forEach(frameStack::push);
                for (Object st : stack) {
                    frameStack.push(st);
                }
                stack = frameStack;
                s = s.parent;
            }
            return stack;
        }

        public boolean isGlobalScope() {
            return parent == null;
        }

        public Label getLabel() {
            return label;
        }

        public Integer getVariableIndex(VariableSymbol variable) {
            if (variablesToLocalIndex.containsKey(variable)) {
                return variablesToLocalIndex.get(variable);
            }
            if (parent != null) {
                return parent.getVariableIndex(variable);
            }
            throw new UndefinedVariableException(variable.getName());
        }

        public void declareVariable(VariableSymbol variable, int index) {
            variablesToLocalIndex.put(variable, index);
        }

        private class Frame {

            private Stack<Object> locals;
            private Stack<Object> stack;

            Frame() {
                locals = new Stack<>();
                stack = new Stack<>();
            }

            public Stack<Object> getLocals() {
                return locals;
            }

            public Stack<Object> getStack() {
                return stack;
            }

            void pushLocal(TypeSymbol type) {
                locals.push(getStackTypeCode(type));
            }

            void pushLocal(String type) {
                locals.push(type);
            }

            Object popLocals() {
                return locals.pop();
            }

            void pushStack(TypeSymbol type) {
                stack.push(getStackTypeCode(type));
            }

            void pushStack(String type) {
                stack.push(type);
            }

            Object popStack() {
                return stack.pop();
            }

        }
    }

}
