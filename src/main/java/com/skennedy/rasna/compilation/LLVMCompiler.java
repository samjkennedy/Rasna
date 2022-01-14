package com.skennedy.rasna.compilation;

import com.skennedy.rasna.typebinding.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.skennedy.rasna.typebinding.TypeSymbol.INT;
import static org.bytedeco.llvm.global.LLVM.LLVMAbortProcessAction;
import static org.bytedeco.llvm.global.LLVM.LLVMAddFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAdd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAnd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCall;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildLoad;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMul;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildOr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildRet;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSDiv;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSRem;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildStore;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSub;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildXor;
import static org.bytedeco.llvm.global.LLVM.LLVMCCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMConstInt;
import static org.bytedeco.llvm.global.LLVM.LLVMContextCreate;
import static org.bytedeco.llvm.global.LLVM.LLVMContextDispose;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateBuilderInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeBuilder;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeModule;
import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;
import static org.bytedeco.llvm.global.LLVM.LLVMGetGlobalPassRegistry;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeCore;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.llvm.global.LLVM.LLVMInt32TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt8TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.llvm.global.LLVM.LLVMModuleCreateWithNameInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMPointerType;
import static org.bytedeco.llvm.global.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;
import static org.bytedeco.llvm.global.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyModule;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;

public class LLVMCompiler implements Compiler {

    private static final Logger log = LogManager.getLogger(LLVMCompiler.class);

    public static final BytePointer error = new BytePointer();

    private LLVMTypeRef i32Type;
    private LLVMValueRef printf;
    private LLVMValueRef formatStr; //"%d\n"

    private Map<VariableSymbol, LLVMValueRef> variables;

    @Override
    public void compile(BoundProgram program, String outputFileName) throws IOException {

        // Stage 1: Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        variables = new HashMap<>();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext(outputFileName, context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        i32Type = LLVMInt32TypeInContext(context);

        //Declare printf function and string formatter once
        printf = LLVMAddFunction(module, "printf", LLVMFunctionType(i32Type, LLVMPointerType(LLVMInt8TypeInContext(context), 0), 1, 1));//No idea what AddressSpace is for yet

        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundFunctionDeclarationExpression) {

                BoundFunctionDeclarationExpression functionDeclarationExpression = (BoundFunctionDeclarationExpression) expression;

                if (functionDeclarationExpression.getFunctionSymbol().getName().equals("main")) {
                    //TODO: main args
                    LLVMTypeRef mainType = LLVMFunctionType(i32Type, LLVMVoidType(), /* argumentCount */ 0, /* isVariadic */ 0);

                    LLVMValueRef main = LLVMAddFunction(module, "main", mainType);
                    LLVMSetFunctionCallConv(main, LLVMCCallConv);

                    LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, main, "entry");
                    LLVMPositionBuilderAtEnd(builder, entry);

                    visitMainMethod((BoundFunctionDeclarationExpression) expression, builder);

                    LLVMValueRef returnCode = LLVMConstInt(i32Type, 0, 0);
                    LLVMBuildRet(builder, returnCode);

                    LLVMVerifyFunction(main, LLVMAbortProcessAction);
                }
            }
        }

        //LLVMDumpModule(module);
        if (LLVMVerifyModule(module, LLVMAbortProcessAction, error) != 0) {
            log.error("Failed to validate module: " + error.getString());
            return;
        }

        BytePointer llFile = new BytePointer("./" + outputFileName + ".ll");
        if (LLVMPrintModuleToFile(module, llFile, error) != 0) {
            log.error("Failed to write module to file");
            LLVMDisposeMessage(error);
            return;
        }
        log.info("Wrote IR to " + outputFileName + ".ll");
        Process process = Runtime.getRuntime().exec("C:\\Program Files\\LLVM\\bin\\clang " + outputFileName + ".ll -o " + outputFileName + ".exe");
        InputStream inputStream = process.getInputStream();
        StringBuilder stdout = new StringBuilder();
        char c = (char) inputStream.read();
        while (c != '\uFFFF') {
            stdout.append(c);
            c = (char) inputStream.read();
        }
        if (stdout.length() != 0) {
            log.info(stdout.toString());
        }

        StringBuilder stderr = new StringBuilder();
        InputStream errorStream = process.getErrorStream();
        c = (char) errorStream.read();
        while (c != '\uFFFF') {
            stderr.append(c);
            c = (char) errorStream.read();
        }
        if (stderr.length() != 0) {
            log.warn(stderr.toString()); //gcc likes to put warnings in stderr, if there was an error we probably wouldn't have gotten this far
        }
        log.info("Compiled IR to " + outputFileName + ".exe");

        // Stage 5: Dispose of allocated resources
        LLVMDisposeModule(module);
        LLVMDisposeBuilder(builder);
        LLVMContextDispose(context);
    }

    private LLVMValueRef visit(BoundExpression expression, LLVMBuilderRef builder) {
        switch (expression.getBoundExpressionType()) {
            case LITERAL:
                return visit((BoundLiteralExpression) expression, builder);
            case PRINT_INTRINSIC:
                return visit((BoundPrintExpression) expression, builder);
            case BINARY_EXPRESSION:
                return visit((BoundBinaryExpression) expression, builder);
            case VARIABLE_DECLARATION:
                return visit((BoundVariableDeclarationExpression) expression, builder);
            case VARIABLE_EXPRESSION:
                return visit((BoundVariableExpression) expression, builder);
            default:
                throw new UnsupportedOperationException("Compilation for `" + expression.getBoundExpressionType() + "` is not yet implemented in LLVM");
        }
    }

    private LLVMValueRef visit(BoundVariableDeclarationExpression variableDeclarationExpression, LLVMBuilderRef builder) {

        LLVMTypeRef type;
        if (variableDeclarationExpression.getType() == INT) {
            type = i32Type;
        } else {
            throw new UnsupportedOperationException("Variables of type `" + variableDeclarationExpression.getType() + "` are not yet implemented in LLVM");
        }
        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, variableDeclarationExpression.getVariable().getName());

        variables.put(variableDeclarationExpression.getVariable(), ptr);

        LLVMValueRef val = visit(variableDeclarationExpression.getInitialiser(), builder);

        return LLVMBuildStore(builder, val, ptr);
    }

    private LLVMValueRef visit(BoundVariableExpression variableExpression, LLVMBuilderRef builder) {

        if (!variables.containsKey(variableExpression.getVariable())) {
            throw new IllegalStateException("Variable `" + variableExpression.getVariable().getName() + "` has not been declared");
        }
        LLVMValueRef ptr = variables.get(variableExpression.getVariable());
        return LLVMBuildLoad(builder, ptr, variableExpression.getVariable().getName());
    }

    private LLVMValueRef visit(BoundBinaryExpression binaryExpression, LLVMBuilderRef builder) {

        LLVMValueRef lhs = visit(binaryExpression.getLeft(), builder);
        LLVMValueRef rhs = visit(binaryExpression.getRight(), builder);

        switch (binaryExpression.getOperator().getBoundOpType()) {

            case ADDITION:
                return LLVMBuildAdd(builder, lhs, rhs, "");
            case SUBTRACTION:
                return LLVMBuildSub(builder, lhs, rhs, "");
            case MULTIPLICATION:
                return LLVMBuildMul(builder, lhs, rhs, "");
            case DIVISION:
                return LLVMBuildSDiv(builder, lhs, rhs, ""); //Signed integer division
            case REMAINDER:
                return LLVMBuildSRem(builder, lhs, rhs, ""); //Signed integer remainder
            case GREATER_THAN:
            case LESS_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN_OR_EQUAL:
            case EQUALS:
            case NOT_EQUALS:
            case BOOLEAN_OR:
                return LLVMBuildOr(builder, lhs, rhs, "");
            case BOOLEAN_AND:
                return LLVMBuildAnd(builder, lhs, rhs, "");
            case BOOLEAN_XOR:
                return LLVMBuildXor(builder, lhs, rhs, "");
            case ERROR:
            default:
                throw new UnsupportedOperationException("Compilation for binary operation `" + binaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM");
        }
    }

    private LLVMValueRef visit(BoundLiteralExpression literalExpression, LLVMBuilderRef builder) {

        if (literalExpression.getType() == INT) {
            return LLVMConstInt(i32Type, (int) literalExpression.getValue(), 0);
        }
        if (literalExpression.getType() == TypeSymbol.BOOL) {
            return LLVMConstInt(i32Type, (boolean) literalExpression.getValue() ? 1 : 0, 0);
        }
        throw new UnsupportedOperationException("Literals of type `" + literalExpression.getType() + "` are not yet supported in LLVM");
    }

    private LLVMValueRef visit(BoundPrintExpression printExpression, LLVMBuilderRef builder) {

        if (formatStr == null) {
            formatStr = LLVMBuildGlobalStringPtr(builder, "%d\n", "formatStr");
        }

        LLVMValueRef res = visit(printExpression.getExpression(), builder);
        PointerPointer<Pointer> printArgs = new PointerPointer<>(2)
                .put(0, formatStr)
                .put(1, res);

        return LLVMBuildCall(builder, this.printf, printArgs, 2, "");
    }

    private void visitMainMethod(BoundFunctionDeclarationExpression mainMethodDeclaration, LLVMBuilderRef builder) {
        List<TypeSymbol> argumentTypes = mainMethodDeclaration.getArguments().stream()
                .map(BoundFunctionArgumentExpression::getType)
                .collect(Collectors.toList());

        if (argumentTypes.size() == 1) {
            throw new UnsupportedOperationException("Main method args are not yet implemented in LLVM");
        }

        for (BoundExpression expression : mainMethodDeclaration.getBody().getExpressions()) {
            visit(expression, builder);
        }
    }
}
