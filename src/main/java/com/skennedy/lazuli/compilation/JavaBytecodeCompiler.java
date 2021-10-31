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
import com.skennedy.lazuli.typebinding.BoundBlockExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundVariableExpression;
import com.skennedy.lazuli.typebinding.TypeSymbol;
import com.skennedy.lazuli.typebinding.VariableSymbol;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Textifier;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INTEGER;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_8;

public class JavaBytecodeCompiler {

    private static final Logger log = LogManager.getLogger(JavaBytecodeCompiler.class);

    private final ClassWriter classWriter;

    private MethodVisitor mainMethodVisitor;
    private Textifier textifierVisitor;

    //TODO: once we have more than just Int we need to track the types of the variables too
    private int variableIndex;
    private Map<Integer, Object> stackMapTypes;
    private int localStackSize;

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
        stackMapTypes = new HashMap<>();
    }

    public void compile(BoundProgram program, String outputFileName) throws IOException {

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

        /** ASM = CODE : public static void main(String args[]). */
        // BEGIN 2: creates a MethodVisitor for the 'main' method
        mainMethodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        textifierVisitor.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        for (BoundExpression expression : program.getExpressions()) {

            visit(expression);
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

    private void visit(BoundExpression expression) {
        switch (expression.getBoundExpressionType()) {

            case ASSIGNMENT_EXPRESSION:

                visit((BoundAssignmentExpression) expression);
                break;
            case ARRAY_LITERAL_EXPRESSION:

                visit((BoundArrayLiteralExpression) expression);
                break;
            case ARRAY_ACCESS_EXPRESSION:

                visit((BoundArrayAccessExpression) expression);
                break;
            case ARRAY_LENGTH_EXPRESSION:

                visit((BoundArrayLengthExpression) expression);
                break;
            case LITERAL:

                visit((BoundLiteralExpression) expression);
                break;
            case BINARY_EXPRESSION:

                visit((BoundBinaryExpression) expression);
                break;
            case BLOCK:

                for (BoundExpression expr : ((BoundBlockExpression) expression).getExpressions()) {
                    visit(expr);
                }
                break;
            case VARIABLE_DECLARATION:

                visit((BoundVariableDeclarationExpression) expression);
                break;
            case VARIABLE_EXPRESSION:

                visit((BoundVariableExpression) expression);
                break;
            case PRINT_INTRINSIC:

                visit((BoundPrintExpression) expression);
                break;
            case CONDITIONAL_GOTO:
                visit((BoundConditionalGotoExpression) expression);
                break;
            case GOTO:
                visit((BoundGotoExpression) expression);
                break;
            case LABEL:
                BoundLabelExpression labelExpression = (BoundLabelExpression) expression;
                Label label;
                if (!boundLabelToProgramLabel.containsKey(labelExpression.getLabel())) {
                    label = new Label();
                    boundLabelToProgramLabel.put(labelExpression.getLabel(), label);
                }
                label = boundLabelToProgramLabel.get(labelExpression.getLabel());

                visit(label, localStackSize);

                break;
            case NOOP:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private void visit(BoundLiteralExpression boundLiteralExpression) {

        Object value = boundLiteralExpression.getValue();
        if (value instanceof Integer) {
            switch ((int) value) {
                case 0:
                    mainMethodVisitor.visitInsn(ICONST_0);
                    textifierVisitor.visitInsn(ICONST_0);
                    break;
                case 1:
                    mainMethodVisitor.visitInsn(ICONST_1);
                    textifierVisitor.visitInsn(ICONST_1);
                    break;
                case 2:
                    mainMethodVisitor.visitInsn(ICONST_2);
                    textifierVisitor.visitInsn(ICONST_2);
                    break;
                case 3:
                    mainMethodVisitor.visitInsn(ICONST_3);
                    textifierVisitor.visitInsn(ICONST_3);
                    break;
                case 4:
                    mainMethodVisitor.visitInsn(ICONST_4);
                    textifierVisitor.visitInsn(ICONST_4);
                    break;
                case 5:
                    mainMethodVisitor.visitInsn(ICONST_5);
                    textifierVisitor.visitInsn(ICONST_5);
                    break;
                default:
                    //TODO: This is pushing a short - maximum value is 32767
                    if (Integer.parseInt(value.toString()) > Short.MAX_VALUE) {
                        throw new UnsupportedOperationException("Currently only shorts can be stored");
                    }
                    mainMethodVisitor.visitIntInsn(SIPUSH, Integer.parseInt(value.toString()));
                    textifierVisitor.visitIntInsn(SIPUSH, Integer.parseInt(value.toString()));
            }
        } else if (value instanceof Boolean) {
            mainMethodVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
            textifierVisitor.visitInsn((boolean) value ? ICONST_1 : ICONST_0);
        }
        localStackSize++;
    }

    private void visit(BoundVariableDeclarationExpression variableDeclarationExpression) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        String identifer = variable.getName();

        //No need to check for reassignment as the type binder already did so

        //Evaluate the initialiser
        visit(variableDeclarationExpression.getInitialiser());

        //Arrays get stored automagically
        if (variableDeclarationExpression.getType() != TypeSymbol.ARRAY) {
            //Store that in memory at the current variable IDX
            mainMethodVisitor.visitIntInsn(ISTORE, variableIndex);
            textifierVisitor.visitIntInsn(ISTORE, variableIndex);
        }
        localStackSize--;

        //Keep track of variable for later use
        variables.put(identifer, variableIndex);

        //Update the stackmap
        stackMapTypes.put(variableIndex, getTypeCode(variableDeclarationExpression.getType()));
        variableIndex++;

    }

    private Object getTypeCode(TypeSymbol type) {
        if (type == TypeSymbol.INT) {
            return INTEGER;
        } else if (type == TypeSymbol.ARRAY) {
            return "[I"; //TODO: only Int Arrays for now
        } else {
            throw new UnsupportedOperationException("Compilation is not yet supported for type: " + type.getName());
        }
    }

    private void visit(BoundVariableExpression boundVariableExpression) {

        VariableSymbol variable = boundVariableExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        int variableIdx = variables.get(variable.getName());
        if (boundVariableExpression.getType() == TypeSymbol.INT) {
            mainMethodVisitor.visitIntInsn(ILOAD, variableIdx);
            textifierVisitor.visitIntInsn(ILOAD, variableIdx);
        } else if (boundVariableExpression.getType() == TypeSymbol.ARRAY) {
            mainMethodVisitor.visitIntInsn(ALOAD, variableIdx);
            textifierVisitor.visitIntInsn(ALOAD, variableIdx);
        }
        localStackSize++;
    }

    private void visit(BoundAssignmentExpression assignmentExpression) {

        VariableSymbol variable = assignmentExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        visit(assignmentExpression.getExpression());

        int variableIdx = variables.get(variable.getName());
        mainMethodVisitor.visitIntInsn(ISTORE, variableIdx);
        textifierVisitor.visitIntInsn(ISTORE, variableIdx);
        localStackSize--;
    }

    private void visit(BoundArrayLiteralExpression arrayLiteralExpression) {

        List<BoundExpression> elements = arrayLiteralExpression.getElements();

        mainMethodVisitor.visitIntInsn(BIPUSH, elements.size());
        textifierVisitor.visitIntInsn(BIPUSH, elements.size());

        mainMethodVisitor.visitIntInsn(NEWARRAY, T_INT);
        textifierVisitor.visitIntInsn(NEWARRAY, T_INT);

        localStackSize++;

        //Store that in memory at the current variable IDX
        mainMethodVisitor.visitIntInsn(ASTORE, variableIndex);
        textifierVisitor.visitIntInsn(ASTORE, variableIndex);
        int arrayRefIndex = variableIndex;

        int index = 0;
        for (BoundExpression element : elements) {
            mainMethodVisitor.visitIntInsn(ALOAD, arrayRefIndex);
            textifierVisitor.visitIntInsn(ALOAD, arrayRefIndex);
            mainMethodVisitor.visitIntInsn(BIPUSH, index);
            textifierVisitor.visitIntInsn(BIPUSH, index);
            visit(element);
            mainMethodVisitor.visitInsn(IASTORE);
            textifierVisitor.visitInsn(IASTORE);
            index++;
            localStackSize--;
        }
    }

    private void visit(BoundArrayAccessExpression arrayAccessExpression) {

        visit(arrayAccessExpression.getArray());
        visit(arrayAccessExpression.getIndex());

        mainMethodVisitor.visitInsn(IALOAD);
        textifierVisitor.visitInsn(IALOAD);

        localStackSize--;
    }

    private void visit(BoundArrayLengthExpression arrayLengthExpression) {

        visit(arrayLengthExpression.getIterable());

        mainMethodVisitor.visitInsn(ARRAYLENGTH);
        textifierVisitor.visitInsn(ARRAYLENGTH);
    }

    private void visit(BoundBinaryExpression binaryExpression) {

        visit(binaryExpression.getLeft());
        visit(binaryExpression.getRight());

        switch (binaryExpression.getOperator().getBoundOpType()) {
            case ADDITION:
                mainMethodVisitor.visitInsn(IADD);
                textifierVisitor.visitInsn(IADD);
                break;
            case SUBTRACTION:
                mainMethodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);
                break;
            case MULTIPLICATION:
                mainMethodVisitor.visitInsn(IMUL);
                textifierVisitor.visitInsn(IMUL);
                break;
            case DIVISION:
                mainMethodVisitor.visitInsn(IDIV);
                textifierVisitor.visitInsn(IDIV);
                break;
            case REMAINDER:
                mainMethodVisitor.visitInsn(IREM);
                textifierVisitor.visitInsn(IREM);
                break;
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_OR_EQUAL:
            case GREATER_THAN_OR_EQUAL:
            case EQUALS:
            case NOT_EQUALS:
                //Seems a little odd but it compares to 0
                mainMethodVisitor.visitInsn(ISUB);
                textifierVisitor.visitInsn(ISUB);
                break;
            case BOOLEAN_OR:
                mainMethodVisitor.visitInsn(IOR);
                textifierVisitor.visitInsn(IOR);
                break;
            case BOOLEAN_AND:
                mainMethodVisitor.visitInsn(IAND);
                textifierVisitor.visitInsn(IAND);
                break;
            default:
                throw new IllegalStateException("Unhandled binary operation: " + binaryExpression.getOperator().getBoundOpType());
        }
        localStackSize--;
    }

    private void visit(BoundConditionalGotoExpression conditionalGotoExpression) {

        visit(conditionalGotoExpression.getCondition());

        Label label;
        if (boundLabelToProgramLabel.containsKey(conditionalGotoExpression.getLabel())) {
            label = boundLabelToProgramLabel.get(conditionalGotoExpression.getLabel());
        } else {
            label = new Label();
            boundLabelToProgramLabel.put(conditionalGotoExpression.getLabel(), label);
        }

        //Assuming only LT right now
        //TODO other ops
        mainMethodVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLT, label);
        textifierVisitor.visitJumpInsn(conditionalGotoExpression.jumpIfFalse() ? IFGT : IFLT, label);
        localStackSize--;
    }

    private void visit(Label label, int stackCount) {
        mainMethodVisitor.visitLabel(label);
        textifierVisitor.visitLabel(label);

        Object[] local = new Object[stackMapTypes.size()+1];
        local[0] = "[Ljava/lang/String;";
        for (int i = 0; i < stackMapTypes.size(); i++) {
            local[i + 1] = stackMapTypes.get(i+1);
        }
        Object[] stack = new Object[stackCount];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = INTEGER; //TODO we only have integers now
        }
        mainMethodVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
        textifierVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
    }

    private void visit(BoundGotoExpression gotoExpression) {
        BoundLabel boundLabel = gotoExpression.getLabel();

        Label label;
        if (boundLabelToProgramLabel.containsKey(boundLabel)) {
            label = boundLabelToProgramLabel.get(boundLabel);
        } else {
            label = new Label();
            boundLabelToProgramLabel.put(boundLabel, label);
        }

        mainMethodVisitor.visitJumpInsn(GOTO, label);
        textifierVisitor.visitJumpInsn(GOTO, label);
    }

    private void visit(BoundPrintExpression printExpression) {

        visit(printExpression.getExpression());

        //Store top of the stack in memory
        mainMethodVisitor.visitIntInsn(ISTORE, variableIndex);
        textifierVisitor.visitIntInsn(ISTORE, variableIndex);

        //CODE : System.out
        mainMethodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        textifierVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        //Load the stored value back out to put it on top of the System classpath
        mainMethodVisitor.visitVarInsn(ILOAD, variableIndex);
        textifierVisitor.visitVarInsn(ILOAD, variableIndex);
        //INVOKE: print(int) with variable on top of the stack. /
        mainMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        textifierVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        localStackSize--;
    }
}
