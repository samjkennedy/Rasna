package com.skennedy.lazuli.compilation;

import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.lowering.BoundConditionalGotoExpression;
import com.skennedy.lazuli.lowering.BoundGotoExpression;
import com.skennedy.lazuli.lowering.BoundLabel;
import com.skennedy.lazuli.lowering.BoundLabelExpression;
import com.skennedy.lazuli.typebinding.BoundArrayAccessExpression;
import com.skennedy.lazuli.typebinding.BoundArrayLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryOperator;
import com.skennedy.lazuli.typebinding.BoundBlockExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionArgumentExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionCallExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundReturnExpression;
import com.skennedy.lazuli.typebinding.BoundTypeofExpression;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BASTORE;
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
import static org.objectweb.asm.Opcodes.V1_8;

public class JavaBytecodeCompiler implements Compiler {

    private static final Logger log = LogManager.getLogger(JavaBytecodeCompiler.class);

    private final ClassWriter classWriter;
    private Textifier textifierVisitor;

    private String className;

    private int variableIndex;
    private Stack<Map<Integer, Object>> stackMapFrames;
    private int localStackSize;
    private Stack<Map<Integer, Object>> localVariables;

    //TODO: Scopes
    private Map<String, Integer> variables;
    private Map<BoundLabel, Label> boundLabelToProgramLabel;

    public JavaBytecodeCompiler() {
        this.classWriter = new ClassWriter(0);

        // Variable0 is reserved for args[] in :  `main(String[] var0)`
        this.variableIndex = 1;
        this.localStackSize = 0;
        variables = new HashMap<>();
        boundLabelToProgramLabel = new HashMap<>();
        stackMapFrames = new Stack<>();
        localVariables = new Stack<>();
        localVariables.push(new HashMap<>());
    }

    @Override
    public void compile(BoundProgram program, String outputFileName) throws IOException {

        className = outputFileName;

        textifierVisitor = new Textifier();

        classWriter.visit(
                V1_8, // Java 1.8
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
                stackMapFrames.push(new HashMap<>());
                visit((BoundFunctionDeclarationExpression) expression);
                stackMapFrames.pop();
            }
        }

        Map<Integer, Object> frame = new HashMap<>();
        frame.put(0, "[Ljava/lang/String;");
        stackMapFrames.push(frame);

        /** ASM = CODE : public static void main(String args[]). */
        // BEGIN 2: creates a MethodVisitor for the 'main' method
        MethodVisitor mainMethodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        textifierVisitor.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        for (BoundExpression expression : program.getExpressions()) {
            //Already declared, skip
            if (expression instanceof BoundFunctionDeclarationExpression) {
                continue;
            }

            visit(expression, mainMethodVisitor);
        }
        mainMethodVisitor.visitInsn(RETURN);
        textifierVisitor.visitInsn(RETURN);

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

                visit(label, localStackSize, methodVisitor);

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
                    //TODO: This is pushing a short - maximum value is 32767
                    if (Integer.parseInt(value.toString()) > Short.MAX_VALUE) {
                        throw new UnsupportedOperationException("Currently only shorts can be stored");
                    }
                    methodVisitor.visitIntInsn(SIPUSH, Integer.parseInt(value.toString()));
                    textifierVisitor.visitIntInsn(SIPUSH, Integer.parseInt(value.toString()));
            }
        } else if (value instanceof Boolean) {
            methodVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
            textifierVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
        }
        localStackSize++;
    }

    private void visit(BoundVariableDeclarationExpression variableDeclarationExpression, MethodVisitor methodVisitor) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        String identifer = variable.getName();

        //Evaluate the initialiser
        visit(variableDeclarationExpression.getInitialiser(), methodVisitor);

        //Arrays get stored automagically
        if (variableDeclarationExpression.getType() != TypeSymbol.ARRAY) {
            //Store that in memory at the current variable IDX
            if (variableDeclarationExpression.getType() == TypeSymbol.INT || variableDeclarationExpression.getType() == TypeSymbol.BOOL) {
                methodVisitor.visitIntInsn(ISTORE, variableIndex);
                textifierVisitor.visitIntInsn(ISTORE, variableIndex);
            } else {
                throw new UnsupportedOperationException("Variables of type " + variableDeclarationExpression.getType().getName() + " are not yet supported");
            }
        }
        localStackSize--;

        //Keep track of variable for later use
        variables.put(identifer, variableIndex);
        localVariables.peek().put(variableIndex, getStackTypeCode(variable.getType()));

        //Update the stackmap
        stackMapFrames.peek().put(variableIndex, getStackTypeCode(variableDeclarationExpression.getType()));
        variableIndex++;

    }

    private Object getStackTypeCode(TypeSymbol type) {
        if (type == TypeSymbol.INT || type == TypeSymbol.BOOL) {
            return INTEGER;
        } else if (type == TypeSymbol.ARRAY) {
            return "[I"; //TODO: only Int Arrays for now
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
        } else if (boundVariableExpression.getType() == TypeSymbol.ARRAY) {
            methodVisitor.visitIntInsn(ALOAD, variableIdx);
            textifierVisitor.visitIntInsn(ALOAD, variableIdx);
        } else {
            throw new UnsupportedOperationException("Variables of type " + boundVariableExpression.getType().getName() + " are not yet supported");
        }
        localStackSize++;
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
        localStackSize--;
    }

    private void visit(BoundArrayLiteralExpression arrayLiteralExpression, MethodVisitor methodVisitor) {

        List<BoundExpression> elements = arrayLiteralExpression.getElements();

        methodVisitor.visitIntInsn(BIPUSH, elements.size());
        textifierVisitor.visitIntInsn(BIPUSH, elements.size());

        methodVisitor.visitIntInsn(NEWARRAY, T_INT);
        textifierVisitor.visitIntInsn(NEWARRAY, T_INT);

        localStackSize++;

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
            index++;
            localStackSize--;
        }
    }

    private void visit(BoundArrayAccessExpression arrayAccessExpression, MethodVisitor methodVisitor) {

        visit(arrayAccessExpression.getArray(), methodVisitor);
        visit(arrayAccessExpression.getIndex(), methodVisitor);

        methodVisitor.visitInsn(IALOAD);
        textifierVisitor.visitInsn(IALOAD);

        localStackSize--;
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
                break;
            case SUBTRACTION:
                methodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);
                break;
            case MULTIPLICATION:
                methodVisitor.visitInsn(IMUL);
                textifierVisitor.visitInsn(IMUL);
                break;
            case DIVISION:
                methodVisitor.visitInsn(IDIV);
                textifierVisitor.visitInsn(IDIV);
                break;
            case REMAINDER:
                methodVisitor.visitInsn(IREM);
                textifierVisitor.visitInsn(IREM);
                break;
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_OR_EQUAL:
            case GREATER_THAN_OR_EQUAL:
            case EQUALS:
            case NOT_EQUALS:
                //Seems a little odd but it compares to 0
                methodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);
                break;
            case BOOLEAN_OR:
                methodVisitor.visitInsn(IOR);
                textifierVisitor.visitInsn(IOR);
                break;
            case BOOLEAN_AND:
                methodVisitor.visitInsn(IAND);
                textifierVisitor.visitInsn(IAND);
                break;
            default:
                throw new IllegalStateException("Unhandled binary operation: " + binaryExpression.getOperator().getBoundOpType());
        }
        localStackSize--;
    }

    private void visit(BoundConditionalGotoExpression conditionalGotoExpression, MethodVisitor methodVisitor) {

        BoundExpression condition = conditionalGotoExpression.getCondition();
        visit(condition, methodVisitor);

        Label label;
        if (boundLabelToProgramLabel.containsKey(conditionalGotoExpression.getLabel())) {
            label = boundLabelToProgramLabel.get(conditionalGotoExpression.getLabel());
        } else {
            label = new Label();
            boundLabelToProgramLabel.put(conditionalGotoExpression.getLabel(), label);
        }

        //Assuming only LT right now
        //TODO better handle other ops
        if (condition instanceof BoundBinaryExpression) {
            BoundBinaryExpression binaryCondition = (BoundBinaryExpression)condition;
            BoundBinaryOperator operator = binaryCondition.getOperator();
            switch (operator.getBoundOpType()) {
                case GREATER_THAN:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFLE : IFGT, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFLE : IFGT, label);
                    break;
                case LESS_THAN:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGE : IFLT, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGE : IFLT, label);
                    break;
                case GREATER_THAN_OR_EQUAL:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFLT : IFGE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFLT : IFGE, label);
                    break;
                case LESS_THAN_OR_EQUAL:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLE, label);
                    break;
                case EQUALS:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFNE : IFEQ, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFNE : IFEQ, label);
                    break;
                case NOT_EQUALS:
                    methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
                    textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFEQ : IFNE, label);
                    break;
                case BOOLEAN_OR:
                    break;
                case BOOLEAN_AND:
                    break;
                default:
                    throw new IllegalStateException("Unsupported binary condition op type: " + operator.getBoundOpType());
            }
        } else {
            methodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLT, label);
            textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLT, label);
        }
        localStackSize--;
    }

    private void visit(Label label, int stackCount, MethodVisitor methodVisitor) {
        methodVisitor.visitLabel(label);
        textifierVisitor.visitLabel(label);

        Map<Integer, Object> stackMapFrame = stackMapFrames.peek();
        Object[] local = new Object[stackMapFrame.size()];
        int localIdx = 0;
        for (Object typeCode : stackMapFrame.values()) {
            local[localIdx] = typeCode;
            localIdx++;
        }
        Object[] stack = new Object[stackCount];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = INTEGER; //TODO we only have integers now
        }
        methodVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
        textifierVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
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
        MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PRIVATE + ACC_STATIC, //TODO: Access levels
                functionSymbol.getName(),
                getMethodDescriptor(functionDeclarationExpression.getArguments(), functionDeclarationExpression.getFunctionSymbol().getType()),
                null,
                null
        );
        textifierVisitor.visitMethod(
                ACC_PRIVATE + ACC_STATIC,
                functionSymbol.getName(),
                getMethodDescriptor(functionDeclarationExpression.getArguments(), functionDeclarationExpression.getFunctionSymbol().getType()),
                null,
                null
        );

        localVariables.push(new HashMap<>());
        int globalVariableIndex = variableIndex;
        variableIndex = 0;
        for (BoundFunctionArgumentExpression argument : functionDeclarationExpression.getArguments()) {
            variables.put(argument.getArgument().getName(), variableIndex++);
            localVariables.peek().put(variableIndex, argument.getArgument());
        }

        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {
            visit(expression, methodVisitor);
        }
        methodVisitor.visitInsn(RETURN);
        textifierVisitor.visitInsn(RETURN);

        //TODO: Keep track of method locals
        methodVisitor.visitMaxs(1024, variableIndex);
        textifierVisitor.visitMaxs(1024, variableIndex);
        localVariables.pop();
        variableIndex = globalVariableIndex;

        methodVisitor.visitEnd();
        textifierVisitor.visitMethodEnd();
    }

    private String getMethodDescriptor(List<BoundFunctionArgumentExpression> arguments, TypeSymbol returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (BoundFunctionArgumentExpression argumentExpression : arguments) {
            sb.append(getType(argumentExpression.getType()));
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
        }
        throw new UnsupportedOperationException("Type " + type.getName() + " is not yet supported");
    }

    private void visit(BoundFunctionCallExpression functionCallExpression, MethodVisitor methodVisitor) {

        FunctionSymbol function = functionCallExpression.getFunction();

        for (BoundExpression argumentInitialiser : functionCallExpression.getBoundArguments()) {
            visit(argumentInitialiser, methodVisitor);
        }

        methodVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(function.getArguments(), function.getType()), false);
        textifierVisitor.visitMethodInsn(INVOKESTATIC, className, function.getName(), getMethodDescriptor(function.getArguments(), function.getType()), false);
    }

    private void visit(BoundReturnExpression returnExpression, MethodVisitor methodVisitor) {

        visit(returnExpression.getReturnValue(), methodVisitor);
        methodVisitor.visitInsn(IRETURN);
        textifierVisitor.visitInsn(IRETURN);
        localStackSize--;
    }

    private void visit(BoundPrintExpression printExpression, MethodVisitor methodVisitor) {

        visit(printExpression.getExpression(), methodVisitor);

        //Store top of the stack in memory
        methodVisitor.visitIntInsn(ISTORE, variableIndex);
        textifierVisitor.visitIntInsn(ISTORE, variableIndex);

        //CODE : System.out
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        textifierVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        //Load the stored value back out to put it on top of the System classpath
        methodVisitor.visitVarInsn(ILOAD, variableIndex);
        textifierVisitor.visitVarInsn(ILOAD, variableIndex);
        //INVOKE: print(int) with variable on top of the stack. /
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        textifierVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        localStackSize--;
    }
}
