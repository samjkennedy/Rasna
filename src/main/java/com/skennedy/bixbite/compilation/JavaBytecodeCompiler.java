package com.skennedy.bixbite.compilation;

import com.skennedy.bixbite.exceptions.UndefinedVariableException;
import com.skennedy.bixbite.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.bixbite.lowering.BoundConditionalGotoExpression;
import com.skennedy.bixbite.lowering.BoundGotoExpression;
import com.skennedy.bixbite.lowering.BoundLabel;
import com.skennedy.bixbite.lowering.BoundLabelExpression;
import com.skennedy.bixbite.typebinding.BoundAssignmentExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryExpression;
import com.skennedy.bixbite.typebinding.BoundBlockExpression;
import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundLiteralExpression;
import com.skennedy.bixbite.typebinding.BoundPrintExpression;
import com.skennedy.bixbite.typebinding.BoundProgram;
import com.skennedy.bixbite.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundVariableExpression;
import com.skennedy.bixbite.typebinding.VariableSymbol;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.F_NEW;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INTEGER;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

public class JavaBytecodeCompiler {

    private static final Logger log = LogManager.getLogger(JavaBytecodeCompiler.class);

    private final ClassWriter classWriter;

    private MethodVisitor mainMethodVisitor;
    private int variableIndex;
    private int variableCount;

    //TODO: Scopes
    private Map<String, Integer> variables;
    private Map<BoundLabel, Label> boundLabelToProgramLabel;

    public JavaBytecodeCompiler() {
        this.classWriter = new ClassWriter(0);

        // Variable0 is reserved for args[] in :  `main(String[] var0)`
        this.variableIndex = 1;
        this.variableCount = 0;
        variables = new HashMap<>();
        boundLabelToProgramLabel = new HashMap<>();
    }

    public void compile(BoundProgram program, String outputFileName) throws IOException {

        classWriter.visit(
                V1_8, // Java 1.8
                ACC_PUBLIC + ACC_SUPER, // public static
                outputFileName, // Class Name
                null, // Generics <T>
                "java/lang/Object", // Interface extends Object (Super Class),
                null // interface names
        );

        /** ASM = CODE : public static void main(String args[]). */
        // BEGIN 2: creates a MethodVisitor for the 'main' method
        mainMethodVisitor = classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        for (BoundExpression expression : program.getExpressions()) {

            visit(expression);
        }
        mainMethodVisitor.visitInsn(RETURN);

        //TODO: determine how much memory the program requires and set accordingly
        mainMethodVisitor.visitMaxs(1024, 1024);

        // END 2: Close main()
        mainMethodVisitor.visitEnd();
        // END 1: Close class()
        classWriter.visitEnd();

        byte[] code = classWriter.toByteArray();

        File outputFile = new File(outputFileName + ".class");
        outputFile.createNewFile();
        FileUtils.writeByteArrayToFile(outputFile, code);
    }

    private void visit(BoundExpression expression) {
        switch (expression.getBoundExpressionType()) {

            case ASSIGNMENT_EXPRESSION:

                visit((BoundAssignmentExpression) expression);
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

                visit(label, 0);

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
            mainMethodVisitor.visitIntInsn(BIPUSH, Integer.parseInt(value.toString()));
        } else if (value instanceof Boolean) {
            mainMethodVisitor.visitIntInsn(BIPUSH, (boolean) value ? 1 : 0);
        }
    }

    private void visit(BoundVariableDeclarationExpression variableDeclarationExpression) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        String identifer = variable.getName();

        //Check for reassignment
        if (variables.containsKey(identifer)) {
            throw new VariableAlreadyDeclaredException(identifer);
        }

        //Evaluate the initialiser
        visit(variableDeclarationExpression.getInitialiser());

        //Store that in memory at the current variable IDX
        mainMethodVisitor.visitIntInsn(ISTORE, variableIndex);

        //Keep track of variable for later use
        variables.put(identifer, variableIndex);

        //Increment the variable IDX
        variableIndex++;
        variableCount++;
    }

    private void visit(BoundVariableExpression boundVariableExpression) {

        VariableSymbol variable = boundVariableExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        int variableIdx = variables.get(variable.getName());
        mainMethodVisitor.visitIntInsn(ILOAD, variableIdx);
    }

    private void visit(BoundAssignmentExpression assignmentExpression) {

        VariableSymbol variable = assignmentExpression.getVariable();

        if (!variables.containsKey(variable.getName())) {
            throw new UndefinedVariableException(variable.getName());
        }
        visit(assignmentExpression.getExpression());

        int variableIdx = variables.get(variable.getName());
        mainMethodVisitor.visitIntInsn(ISTORE, variableIdx);
    }

    private void visit(BoundBinaryExpression binaryExpression) {

        visit(binaryExpression.getLeft());
        visit(binaryExpression.getRight());

        switch (binaryExpression.getOperator().getBoundOpType()) {
            case ADDITION:
                mainMethodVisitor.visitInsn(IADD);
                break;
            case SUBTRACTION:
                mainMethodVisitor.visitInsn(ISUB);
                break;
            case MULTIPLICATION:
                mainMethodVisitor.visitInsn(IMUL);
                break;
            case DIVISION:
                mainMethodVisitor.visitInsn(IDIV);
                break;
            case REMAINDER:
                mainMethodVisitor.visitInsn(IREM);
                break;
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_OR_EQUAL:
            case GREATER_THAN_OR_EQUAL:
            case EQUALS:
            case NOT_EQUALS:
                //Seems a little odd but it compares to 0
                mainMethodVisitor.visitInsn(ISUB);
                break;
            case BOOLEAN_OR:
                mainMethodVisitor.visitInsn(IOR);
                break;
            case BOOLEAN_AND:
                mainMethodVisitor.visitInsn(IAND);
                break;
            default:
                throw new IllegalStateException("Unhandled binary operation: " + binaryExpression.getOperator().getBoundOpType());
        }
    }

    private void visit(BoundConditionalGotoExpression conditionalGotoExpression) {

        int varsBefore = variableCount;
        visit(conditionalGotoExpression.getCondition());
        int varsAfter = variableCount;

        Label label;
        if (boundLabelToProgramLabel.containsKey(conditionalGotoExpression.getLabel())) {
            label = boundLabelToProgramLabel.get(conditionalGotoExpression.getLabel());
        } else {
            label = new Label();
            visit(label, varsAfter - varsBefore);
        }

        //Assuming only LT right now
        //TODO other ops
        mainMethodVisitor.visitJumpInsn(IFLT, label);
    }

    private void visit(Label label, int stackCount) {
        mainMethodVisitor.visitLabel(label);

        //I feel like this shouldn't work but hey ho, magic

        Object[] local = new Object[variableCount + 1];
        local[0] = "[Ljava/lang/String;";
        for (int i = 1; i < local.length; i++) {
            local[i] = INTEGER; //TODO we only have integers now
        }
        Object[] stack = new Object[stackCount];
        for (int i = 0; i < stack.length; i++) {
            stack[i] = INTEGER; //TODO we only have integers now
        }
        mainMethodVisitor.visitFrame(F_NEW, local.length, local, stack.length, stack);
    }

    private void visit(BoundGotoExpression gotoExpression) {
        BoundLabel boundLabel = gotoExpression.getLabel();

        Label label;
        if (boundLabelToProgramLabel.containsKey(boundLabel)) {
            label = boundLabelToProgramLabel.get(boundLabel);
        } else {
            label = new Label();
        }
        boundLabelToProgramLabel.put(boundLabel, label);

        mainMethodVisitor.visitJumpInsn(GOTO, label);
    }

    private void visit(BoundPrintExpression printExpression) {

        visit(printExpression.getExpression());

        //Store top of the stack in memory
        mainMethodVisitor.visitIntInsn(ISTORE, variableIndex);

        //CODE : System.out
        mainMethodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        //Load the stored value back out to put it on top of the System classpath
        mainMethodVisitor.visitVarInsn(ILOAD, variableIndex);
        //INVOKE: print(int) with variable on top of the stack. /
        mainMethodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }
}
