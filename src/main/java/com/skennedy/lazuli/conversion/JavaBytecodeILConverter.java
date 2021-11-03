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
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.F_NEW;
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
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
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
import static org.objectweb.asm.Opcodes.LDC;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V11;

public class JavaBytecodeILConverter implements ILConverter {

    private static final Logger log = LogManager.getLogger(JavaBytecodeILConverter.class);

    private final ClassWriter classWriter;
    private Textifier textifierVisitor;

    private String className;

    private Stack<Frame> stackMap;

    private int variableIndex;
    private Stack<Map<Integer, Object>> localVariables;

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

    public JavaBytecodeILConverter() {
        this.classWriter = new ClassWriter(0);

        // Variable0 is reserved for args[] in :  `main(String[] var0)`
        this.variableIndex = 1;
        variables = new HashMap<>();
        boundLabelToProgramLabel = new HashMap<>();
        stackMap = new Stack<>();
        localVariables = new Stack<>();
        localVariables.push(new HashMap<>());

        visitedLabels = new HashSet<>();
        constantIdx = 2;
        constantPool = new HashMap<>();
    }

    @Override
    public void convert(BoundProgram program, String outputFileName) throws IOException {

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
                stackMap.push(new Frame());
                visit((BoundFunctionDeclarationExpression) expression);
                stackMap.pop();
            }
        }

        Frame frame = new Frame();
        frame.pushLocal("[Ljava/lang/String;");
        stackMap.push(frame);

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
            visit(expression, mainMethodVisitor);
            ip++;
        }
        mainMethodVisitor.visitInsn(RETURN);
        textifierVisitor.visitInsn(RETURN);
        stackMap.pop();

        //TODO: determine how much memory the program requires and set accordingly
        mainMethodVisitor.visitMaxs(1024, 1024);
        textifierVisitor.visitMaxs(1024, 1024);

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

                    } else if (operand <Integer.MAX_VALUE) {

                        int constIndex;
                        if (constantPool.containsKey(operand)) {
                            constIndex = constantPool.get(operand);
                        } else {
                            constIndex = classWriter.newConst(operand);
                            constantPool.put(operand, constantIdx);
                            constantIdx++;
                        }
                        methodVisitor.visitIntInsn(LDC, constIndex);
                        textifierVisitor.visitIntInsn(LDC, constIndex);
                    }
            }
        } else if (value instanceof Boolean) {
            methodVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
            textifierVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
        } else {
            throw new UnsupportedOperationException("Literals of type " + boundLiteralExpression.getType() + " are not yet supported");
        }
        stackMap.peek().pushStack(boundLiteralExpression.getType());
    }

    private void visit(BoundVariableDeclarationExpression variableDeclarationExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        String identifer = variable.getName();

        //Evaluate the initialiser
        visit(variableDeclarationExpression.getInitialiser(), methodVisitor);

        //Arrays get stored automagically
        if (variableDeclarationExpression.getType() != TypeSymbol.INT_ARRAY) {
            //Store that in memory at the current variable IDX
            if (variableDeclarationExpression.getType() == TypeSymbol.INT || variableDeclarationExpression.getType() == TypeSymbol.BOOL) {
                methodVisitor.visitIntInsn(ISTORE, variableIndex);
                textifierVisitor.visitIntInsn(ISTORE, variableIndex);
                stackMap.peek().popStack();
            } else {
                throw new UnsupportedOperationException("Variables of type " + variableDeclarationExpression.getType().getName() + " are not yet supported");
            }
        }

        //Keep track of variable for later use
        variables.put(identifer, variableIndex);
        localVariables.peek().put(variableIndex, getStackTypeCode(variable.getType()));

        //Update the stackmap
        stackMap.peek().pushLocal(variableDeclarationExpression.getType());
        variableIndex++;

    }

    private Object getStackTypeCode(TypeSymbol type) {
        if (type == TypeSymbol.INT || type == TypeSymbol.BOOL) {
            return INTEGER;
        } else if (type == TypeSymbol.INT_ARRAY) {
            return "[I";
        } else {
            throw new UnsupportedOperationException("Compilation is not yet supported for type: " + type.getName());
        }
    }

    private void visit(BoundVariableExpression boundVariableExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = boundVariableExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        int variableIdx = variables.get(variable.getName());
        if (boundVariableExpression.getType() == TypeSymbol.INT || boundVariableExpression.getType() == TypeSymbol.BOOL) {
            methodVisitor.visitIntInsn(ILOAD, variableIdx);
            textifierVisitor.visitIntInsn(ILOAD, variableIdx);
        } else if (boundVariableExpression.getType() == TypeSymbol.INT_ARRAY) {
            methodVisitor.visitIntInsn(ALOAD, variableIdx);
            textifierVisitor.visitIntInsn(ALOAD, variableIdx);
        } else {
            throw new UnsupportedOperationException("Variables of type " + boundVariableExpression.getType().getName() + " are not yet supported");
        }
        stackMap.peek().pushStack(variable.getType());
    }

    private void visit(BoundAssignmentExpression assignmentExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = assignmentExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        visit(assignmentExpression.getExpression(), methodVisitor);

        int variableIdx = variables.get(variable.getName());
        methodVisitor.visitIntInsn(ISTORE, variableIdx);
        textifierVisitor.visitIntInsn(ISTORE, variableIdx);

        stackMap.peek().popStack();
    }

    private void visit(BoundArrayAssignmentExpression arrayAssignmentExpression, MethodVisitor methodVisitor) {

        visit(arrayAssignmentExpression.getArrayAccessExpression().getArray(), methodVisitor);
        visit(arrayAssignmentExpression.getArrayAccessExpression().getIndex(), methodVisitor);
        visit(arrayAssignmentExpression.getAssignment(), methodVisitor);

        methodVisitor.visitInsn(IASTORE);
        textifierVisitor.visitInsn(IASTORE);
    }

    private void visit(BoundArrayLiteralExpression arrayLiteralExpression, MethodVisitor methodVisitor) {

        List<BoundExpression> elements = arrayLiteralExpression.getElements();

        methodVisitor.visitIntInsn(BIPUSH, elements.size());
        textifierVisitor.visitIntInsn(BIPUSH, elements.size());

        methodVisitor.visitIntInsn(NEWARRAY, T_INT);
        textifierVisitor.visitIntInsn(NEWARRAY, T_INT);

        //Store that in memory at the current variable IDX
        methodVisitor.visitIntInsn(ASTORE, variableIndex);
        textifierVisitor.visitIntInsn(ASTORE, variableIndex);
        int arrayRefIndex = variableIndex;

        int index = 0;
        for (BoundExpression element : elements) {
            methodVisitor.visitIntInsn(ALOAD, arrayRefIndex);
            textifierVisitor.visitIntInsn(ALOAD, arrayRefIndex);
            methodVisitor.visitIntInsn(BIPUSH, index);
            textifierVisitor.visitIntInsn(BIPUSH, index);
            visit(element, methodVisitor);
            methodVisitor.visitInsn(IASTORE);
            textifierVisitor.visitInsn(IASTORE);

            stackMap.peek().popStack();
            index++;
        }
    }

    private void visit(BoundArrayAccessExpression arrayAccessExpression, MethodVisitor methodVisitor) {

        visit(arrayAccessExpression.getArray(), methodVisitor);
        visit(arrayAccessExpression.getIndex(), methodVisitor);

        methodVisitor.visitInsn(IALOAD);
        textifierVisitor.visitInsn(IALOAD);

        stackMap.peek().popStack(); //Array index
    }

    private void visit(BoundArrayLengthExpression arrayLengthExpression, MethodVisitor methodVisitor) {

        visit(arrayLengthExpression.getIterable(), methodVisitor);

        methodVisitor.visitInsn(ARRAYLENGTH);
        textifierVisitor.visitInsn(ARRAYLENGTH);
    }

    private void visit(BoundBinaryExpression binaryExpression, MethodVisitor methodVisitor) {

        visit(binaryExpression.getLeft(), methodVisitor);
        visit(binaryExpression.getRight(), methodVisitor);

        switch (binaryExpression.getOperator().getBoundOpType()) {
            case ADDITION:
                methodVisitor.visitInsn(IADD);
                textifierVisitor.visitInsn(IADD);

                stackMap.peek().popStack();
                break;
            case SUBTRACTION:
                methodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);

                stackMap.peek().popStack();
                break;
            case MULTIPLICATION:
                methodVisitor.visitInsn(IMUL);
                textifierVisitor.visitInsn(IMUL);

                stackMap.peek().popStack();
                break;
            case DIVISION:
                methodVisitor.visitInsn(IDIV);
                textifierVisitor.visitInsn(IDIV);

                stackMap.peek().popStack();
                break;
            case REMAINDER:
                methodVisitor.visitInsn(IREM);
                textifierVisitor.visitInsn(IREM);

                stackMap.peek().popStack();
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

                stackMap.peek().popStack();
                break;
            case BOOLEAN_AND:
                methodVisitor.visitInsn(IAND);
                textifierVisitor.visitInsn(IAND);

                stackMap.peek().popStack();
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
        stackMap.peek().popStack();
        stackMap.peek().popStack();

        methodVisitor.visitInsn(ICONST_0);
        textifierVisitor.visitInsn(ICONST_0);

        methodVisitor.visitJumpInsn(GOTO, end);
        textifierVisitor.visitJumpInsn(GOTO, end);

        visit(ifTrue, methodVisitor);

        methodVisitor.visitInsn(ICONST_1);
        textifierVisitor.visitInsn(ICONST_1);

        stackMap.peek().pushStack(TypeSymbol.BOOL);

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
                case BOOLEAN_OR:
                case BOOLEAN_AND:
                default:
                    throw new IllegalStateException("Unsupported binary condition op type: " + operator.getBoundOpType());
            }
            stackMap.peek().popStack(); //Pop right
            stackMap.peek().popStack(); //Pop left
        } else {
            //Compare true/false to 0
            visit(condition, methodVisitor);
            methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
            textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
            stackMap.peek().popStack(); //Pop condition
        }
    }

    private void visit(Label label, MethodVisitor methodVisitor) {

        if (visitedLabels.contains(label)) {
            return;
        }

        Object[] local = stackMap.peek().getLocals().toArray(new Object[0]);
        Object[] stack = stackMap.peek().getStack().toArray(new Object[0]);
        methodVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
        textifierVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);

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

        localVariables.push(new HashMap<>());
        int globalVariableIndex = variableIndex;
        variableIndex = 0;
        for (BoundFunctionArgumentExpression argument : functionDeclarationExpression.getArguments()) {
            variables.put(argument.getArgument().getName(), variableIndex);
            localVariables.peek().put(variableIndex, argument.getArgument());
            stackMap.peek().pushLocal(argument.getArgument().getType());
            variableIndex++;
        }

        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {
            visit(expression, methodVisitor);
        }
        variableIndex = 0;
        //Bigge just in case - e.g.:
        /*
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
        localVariables.pop();
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
        if (type == TypeSymbol.BOOL) {
            return Type.BOOLEAN_TYPE.getDescriptor();
        } if (type == TypeSymbol.INT_ARRAY) {
            return "[I";
        }
        throw new UnsupportedOperationException("Type " + type.getName() + " is not yet supported");
    }

    private void visit(BoundFunctionCallExpression functionCallExpression, MethodVisitor methodVisitor) {

        FunctionSymbol function = functionCallExpression.getFunction();

        stackMap.push(new Frame());
        for (BoundExpression argumentInitialiser : functionCallExpression.getBoundArguments()) {
            visit(argumentInitialiser, methodVisitor);
        }

        List<TypeSymbol> argumentTypes = functionCallExpression.getBoundArguments().stream()
                .map(BoundExpression::getType)
                .collect(Collectors.toList());

        methodVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(argumentTypes, function.getType()), false);
        textifierVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(argumentTypes, function.getType()), false);

        stackMap.pop();
        //Push return type onto stack
        if (functionCallExpression.getType() != TypeSymbol.VOID) {
            stackMap.peek().pushStack(functionCallExpression.getType());
        }
    }

    private void visit(BoundReturnExpression returnExpression, MethodVisitor methodVisitor) {

        visit(returnExpression.getReturnValue(), methodVisitor);

        methodVisitor.visitInsn(IRETURN);
        textifierVisitor.visitInsn(IRETURN);

        stackMap.peek().popStack();
    }

    private void visit(BoundPrintExpression printExpression, MethodVisitor methodVisitor) {

        visit(printExpression.getExpression(), methodVisitor);

        //Store top of the stack in memory
        methodVisitor.visitIntInsn(ISTORE, variableIndex);
        textifierVisitor.visitIntInsn(ISTORE, variableIndex);
        stackMap.peek().popStack();

        //CODE : System.out
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        textifierVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        //Load the stored value back out to put it on top of the System classpath
        methodVisitor.visitVarInsn(ILOAD, variableIndex);
        textifierVisitor.visitVarInsn(ILOAD, variableIndex);
        //INVOKE: print(int) with variable on top of the stack. /

        String descriptor = getMethodDescriptor(Collections.singletonList(printExpression.getExpression().getType()), TypeSymbol.VOID);

        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
        textifierVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
    }

    private class Frame {

        private Stack<Object> locals;
        private Stack<Object> stack;

        public Frame() {
            locals = new Stack<>();
            stack = new Stack<>();
        }

        public Stack<Object> getLocals() {
            return locals;
        }

        public Stack<Object> getStack() {
            return stack;
        }

        public void pushLocal(TypeSymbol type) {
            locals.push(getStackTypeCode(type));
        }

        public void pushLocal(String type) {
            locals.push(type);
        }

        public Object popLocals() {
            return locals.pop();
        }

        public void pushStack(TypeSymbol type) {
            stack.push(getStackTypeCode(type));
        }

        public void pushStack(String type) {
            stack.push(type);
        }

        public Object popStack() {
            return stack.pop();
        }

    }
}
