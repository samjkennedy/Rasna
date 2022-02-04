package com.skennedy.rasna.compilation.llvm;

import com.skennedy.rasna.lowering.BoundArrayLengthExpression;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.skennedy.rasna.typebinding.BuiltInFunctions.CLOSE;
import static com.skennedy.rasna.typebinding.BuiltInFunctions.OPEN;
import static com.skennedy.rasna.typebinding.BuiltInFunctions.OPEN_R;
import static com.skennedy.rasna.typebinding.BuiltInFunctions.READ_CHAR;
import static com.skennedy.rasna.typebinding.BuiltInFunctions.WRITE_CHAR;
import static com.skennedy.rasna.typebinding.TypeSymbol.BOOL;
import static com.skennedy.rasna.typebinding.TypeSymbol.CHAR;
import static com.skennedy.rasna.typebinding.TypeSymbol.FILE;
import static com.skennedy.rasna.typebinding.TypeSymbol.INT;
import static com.skennedy.rasna.typebinding.TypeSymbol.REAL;
import static com.skennedy.rasna.typebinding.TypeSymbol.STRING;
import static com.skennedy.rasna.typebinding.TypeSymbol.UNIT;
import static org.bytedeco.llvm.global.LLVM.LLVMAddFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMArrayType;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAdd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAnd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildBitCast;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildBr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCall;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCast;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCondBr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFAdd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFCmp;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFDiv;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFMul;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFRem;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildFSub;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildGlobalString;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildICmp;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildInBoundsGEP;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildLoad;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMemSet;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMul;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildNot;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildOr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildRet;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildRetVoid;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSDiv;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSRem;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSelect;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildStore;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildStructGEP;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSub;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildXor;
import static org.bytedeco.llvm.global.LLVM.LLVMCCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMConstInt;
import static org.bytedeco.llvm.global.LLVM.LLVMConstReal;
import static org.bytedeco.llvm.global.LLVM.LLVMContextCreate;
import static org.bytedeco.llvm.global.LLVM.LLVMContextDispose;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateBuilderInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeBuilder;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeModule;
import static org.bytedeco.llvm.global.LLVM.LLVMDoubleTypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMDumpModule;
import static org.bytedeco.llvm.global.LLVM.LLVMFPToSI;
import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;
import static org.bytedeco.llvm.global.LLVM.LLVMGetBasicBlockTerminator;
import static org.bytedeco.llvm.global.LLVM.LLVMGetGlobalPassRegistry;
import static org.bytedeco.llvm.global.LLVM.LLVMGetInsertBlock;
import static org.bytedeco.llvm.global.LLVM.LLVMGetParam;
import static org.bytedeco.llvm.global.LLVM.LLVMGetTypeKind;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeCore;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.llvm.global.LLVM.LLVMInt1TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt32TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt64TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt8TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMIntEQ;
import static org.bytedeco.llvm.global.LLVM.LLVMIntNE;
import static org.bytedeco.llvm.global.LLVM.LLVMIntSGE;
import static org.bytedeco.llvm.global.LLVM.LLVMIntSGT;
import static org.bytedeco.llvm.global.LLVM.LLVMIntSLE;
import static org.bytedeco.llvm.global.LLVM.LLVMIntSLT;
import static org.bytedeco.llvm.global.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.llvm.global.LLVM.LLVMModuleCreateWithNameInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMPointerType;
import static org.bytedeco.llvm.global.LLVM.LLVMPointerTypeKind;
import static org.bytedeco.llvm.global.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintMessageAction;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;
import static org.bytedeco.llvm.global.LLVM.LLVMRealOEQ;
import static org.bytedeco.llvm.global.LLVM.LLVMRealOGE;
import static org.bytedeco.llvm.global.LLVM.LLVMRealOGT;
import static org.bytedeco.llvm.global.LLVM.LLVMRealOLE;
import static org.bytedeco.llvm.global.LLVM.LLVMRealOLT;
import static org.bytedeco.llvm.global.LLVM.LLVMRealONE;
import static org.bytedeco.llvm.global.LLVM.LLVMSExt;
import static org.bytedeco.llvm.global.LLVM.LLVMSIToFP;
import static org.bytedeco.llvm.global.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMStructCreateNamed;
import static org.bytedeco.llvm.global.LLVM.LLVMStructSetBody;
import static org.bytedeco.llvm.global.LLVM.LLVMStructTypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMTrunc;
import static org.bytedeco.llvm.global.LLVM.LLVMTypeOf;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyModule;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidTypeInContext;

public class LLVMCompiler {

    private static final Logger log = LogManager.getLogger(LLVMCompiler.class);

    public static final BytePointer error = new BytePointer();

    private LLVMTypeRef i1Type;
    private LLVMTypeRef i8Type;
    private LLVMTypeRef i32Type;
    private LLVMTypeRef i64Type;
    private LLVMTypeRef fileType;
    private LLVMTypeRef realType;

    private LLVMValueRef printf;
    private LLVMValueRef printB; //for printing bools nicely
    private LLVMValueRef formatStr; //"%d\n"

    private Scope scope;

    public void compile(BoundProgram program, String outputFileName) throws IOException {

        // Stage 1: Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        scope = new Scope(null);

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext(outputFileName, context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        i1Type = LLVMInt1TypeInContext(context);
        i8Type = LLVMInt8TypeInContext(context);
        i32Type = LLVMInt32TypeInContext(context);
        i64Type = LLVMInt64TypeInContext(context);
        realType = LLVMDoubleTypeInContext(context);

        PointerPointer<Pointer> structTypes = new PointerPointer<>(1)
                .put(0, LLVMPointerType(i8Type, 0)); //TODO: Doesn't support beyond utf-8
        fileType = LLVMStructTypeInContext(context, structTypes, 1, 0);

        //Declare printf function and string formatter once
        printf = LLVMAddFunction(module, "printf", LLVMFunctionType(i32Type, LLVMPointerType(LLVMInt8TypeInContext(context), 0), 1, 1));//No idea what AddressSpace is for yet

        buildPrintbMethod(context, module, builder);

        buildBuiltInFunctions(context, module, builder);

        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundFunctionDeclarationExpression) {

                BoundFunctionDeclarationExpression functionDeclarationExpression = (BoundFunctionDeclarationExpression) expression;

                if (functionDeclarationExpression.getFunctionSymbol().getName().equals("main")) {

                    //TODO: need an entry method that takes in argc argv and transforms it to a Rasna String[] and calls main with it

                    LLVMTypeRef mainType;
                    if (functionDeclarationExpression.getFunctionSymbol().getArguments().size() == 1) {
                        mainType = LLVMFunctionType(i32Type, getLlvmTypeRef(new ArrayTypeSymbol(STRING), context), /* argumentCount */ 1, /* isVariadic */ 0);
                    } else {
                        mainType = LLVMFunctionType(i32Type, LLVMVoidType(), /* argumentCount */ 0, /* isVariadic */ 0);
                    }

                    LLVMValueRef main = LLVMAddFunction(module, "main", mainType);
                    LLVMSetFunctionCallConv(main, LLVMCCallConv);

                    LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, main, "entry");
                    LLVMPositionBuilderAtEnd(builder, entry);

                    visitMainMethod((BoundFunctionDeclarationExpression) expression, builder, context, main);

                    LLVMValueRef returnCode = LLVMConstInt(i32Type, 0, 0);
                    LLVMBuildRet(builder, returnCode);

                    if (LLVMVerifyFunction(main, LLVMPrintMessageAction) != 0) {
                        log.error("Error when validating main function:");
                        LLVMDumpModule(module);
                        return;
                    }
                } else {
                    scope = new Scope(scope);
                    FunctionSymbol functionSymbol = functionDeclarationExpression.getFunctionSymbol();

                    TypeSymbol returnType = functionSymbol.getType();

                    LLVMTypeRef functionType = buildFunctionType(functionSymbol.getArguments(), returnType, context);

                    LLVMValueRef func = LLVMAddFunction(module, functionSymbol.getName(), functionType);
                    LLVMSetFunctionCallConv(func, LLVMCCallConv);

                    LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, func, "entry");
                    LLVMPositionBuilderAtEnd(builder, entry);

                    visit((BoundFunctionDeclarationExpression) expression, builder, context, func);

                    if (LLVMVerifyFunction(func, LLVMPrintMessageAction) != 0) {
                        log.error("Error when validating function `" + functionSymbol.getSignature() + "`:");
                        LLVMDumpModule(module);
                        return;
                    }
                    scope = scope.getParentScope();
                    scope.declareFunction(functionSymbol, func);
                }
            } else {
                visit(expression, builder, context, null);
            }
        }

        if (LLVMVerifyModule(module, LLVMPrintMessageAction, error) != 0) {
            log.error("Failed to validate module: " + error.getString());
            LLVMDumpModule(module);
            System.exit(1);
        }

        BytePointer llFile = new BytePointer("./" + outputFileName + ".ll");
        if (LLVMPrintModuleToFile(module, llFile, error) != 0) {
            log.error("Failed to write module to file");
            LLVMDisposeMessage(error);
            return;
        }
        log.debug("Wrote IR to " + outputFileName + ".ll");
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
            log.debug(stderr.toString()); //gcc likes to put warnings in stderr, if there was an error we probably wouldn't have gotten this far
        }
        log.debug("Compiled IR to " + outputFileName + ".exe");

        // Stage 5: Dispose of allocated resources
        LLVMDisposeModule(module);
        LLVMDisposeBuilder(builder);
        LLVMContextDispose(context);
    }

    private void buildBuiltInFunctions(LLVMContextRef context, LLVMModuleRef module, LLVMBuilderRef builder) {

        PointerPointer<Pointer> fopenTypes = new PointerPointer<>(2)
                .put(0, LLVMPointerType(i8Type, 0))
                .put(1, LLVMPointerType(i8Type, 0));
        LLVMValueRef fopen = LLVMAddFunction(module, "fopen", LLVMFunctionType(fileType, fopenTypes, 2, 0));

        for (FunctionSymbol builtInFunction : BuiltInFunctions.getBuiltinFunctions()) {
            if (builtInFunction == READ_CHAR) {
                LLVMValueRef fgetc = LLVMAddFunction(module, "fgetc", LLVMFunctionType(i8Type, getLlvmTypeRef(FILE, context), 1, 0));
                LLVMValueRef readChar = LLVMAddFunction(module, "readChar", LLVMFunctionType(i8Type, getLlvmTypeRef(FILE, context), 1, 0));

                LLVMSetFunctionCallConv(readChar, LLVMCCallConv);
                LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, readChar, "entry");
                LLVMPositionBuilderAtEnd(builder, entry);

                LLVMValueRef fp = LLVMGetParam(readChar, 0);
                PointerPointer<Pointer> readArgs = new PointerPointer<>(1)
                        .put(0, fp);

                LLVMBuildRet(builder, LLVMBuildCall(builder, fgetc, readArgs, 1, "readchar"));

                if (LLVMVerifyFunction(readChar, LLVMPrintMessageAction) != 0) {
                    log.error("Error when validating readChar function:");
                    LLVMDumpModule(module);
                    System.exit(1);
                }

                scope.declareFunction(builtInFunction, readChar);

            } else if (builtInFunction == WRITE_CHAR) {

                LLVMValueRef fputc = LLVMAddFunction(module, "fputc", LLVMFunctionType(LLVMInt32TypeInContext(context), new PointerPointer<>(2)
                        .put(0, getLlvmTypeRef(CHAR, context))
                        .put(1, getLlvmTypeRef(FILE, context)), 2, 0));

                LLVMValueRef writeChar = LLVMAddFunction(module, "writeChar", LLVMFunctionType(LLVMVoidTypeInContext(context), new PointerPointer<>(2)
                        .put(0, getLlvmTypeRef(FILE, context))
                        .put(1, getLlvmTypeRef(CHAR, context)), 2, 0));
                LLVMSetFunctionCallConv(writeChar, LLVMCCallConv);
                LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, writeChar, "entry");
                LLVMPositionBuilderAtEnd(builder, entry);

                LLVMValueRef c = LLVMGetParam(writeChar, 0);
                LLVMValueRef fp = LLVMGetParam(writeChar, 1);
                PointerPointer<Pointer> writeArgs = new PointerPointer<>(2)
                        .put(0, fp)
                        .put(1, c);
                LLVMBuildCall(builder, fputc, writeArgs, 2, "writechar");

                LLVMBuildRetVoid(builder);

                if (LLVMVerifyFunction(writeChar, LLVMPrintMessageAction) != 0) {
                    log.error("Error when validating writeChar function:");
                    LLVMDumpModule(module);
                    System.exit(1);
                }

                scope.declareFunction(builtInFunction, writeChar);
            } else if (builtInFunction == OPEN) {

                PointerPointer<Pointer> openTypes = new PointerPointer<>(2)
                        .put(0, getLlvmTypeRef(STRING, context))
                        .put(1, getLlvmTypeRef(STRING, context));
                LLVMValueRef open = LLVMAddFunction(module, "open", LLVMFunctionType(getLlvmTypeRef(FILE, context), openTypes, 2, 0));

                LLVMSetFunctionCallConv(open, LLVMCCallConv);
                LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, open, "entry");
                LLVMPositionBuilderAtEnd(builder, entry);

                LLVMValueRef filenameStr = LLVMGetParam(open, 0);
                LLVMValueRef filename = LLVMBuildStructGEP(builder, ref(builder, filenameStr, new ArrayTypeSymbol(CHAR), context), 1, "filename");
                LLVMValueRef modeStr = LLVMGetParam(open, 1);
                LLVMValueRef mode = LLVMBuildStructGEP(builder, ref(builder, modeStr, new ArrayTypeSymbol(CHAR), context), 1, "mode");

                PointerPointer<Pointer> fopenArgs = new PointerPointer<>(2)
                        .put(0, dereference(builder, filename, ""))
                        .put(1, dereference(builder, mode, ""));
                LLVMBuildRet(builder, LLVMBuildCall(builder, fopen, fopenArgs, 2, "opencall"));

                if (LLVMVerifyFunction(open, LLVMPrintMessageAction) != 0) {
                    log.error("Error when validating open function:");
                    LLVMDumpModule(module);
                    System.exit(1);
                }

                scope.declareFunction(OPEN, open);

            } else if (builtInFunction == OPEN_R) {

                PointerPointer<Pointer> openTypes = new PointerPointer<>(1)
                        .put(0, getLlvmTypeRef(STRING, context));
                LLVMValueRef open_r = LLVMAddFunction(module, "open", LLVMFunctionType(getLlvmTypeRef(FILE, context), openTypes, 1, 0));

                LLVMSetFunctionCallConv(open_r, LLVMCCallConv);
                LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, open_r, "entry");
                LLVMPositionBuilderAtEnd(builder, entry);

                LLVMValueRef filenameStr = LLVMGetParam(open_r, 0);
                LLVMValueRef filename = LLVMBuildStructGEP(builder, ref(builder, filenameStr, new ArrayTypeSymbol(CHAR), context), 1, "filename");
                LLVMValueRef mode = LLVMBuildGlobalStringPtr(builder, "r", "file_mode_read");

                PointerPointer<Pointer> fopenArgs = new PointerPointer<>(2)
                        .put(0, dereference(builder, filename, ""))
                        .put(1, mode);
                LLVMBuildRet(builder, LLVMBuildCall(builder, fopen, fopenArgs, 2, "opencall"));

                if (LLVMVerifyFunction(open_r, LLVMPrintMessageAction) != 0) {
                    log.error("Error when validating open function:");
                    LLVMDumpModule(module);
                    System.exit(1);
                }

                scope.declareFunction(OPEN_R, open_r);
            } else if (builtInFunction == CLOSE) {

                PointerPointer<Pointer> closeType = new PointerPointer<>(1)
                        .put(0, getLlvmTypeRef(FILE, context));
                LLVMValueRef fclose = LLVMAddFunction(module, "fclose", LLVMFunctionType(i32Type, closeType, 1, 0));

                LLVMValueRef close = LLVMAddFunction(module, "close", LLVMFunctionType(getLlvmTypeRef(BOOL, context), closeType, 1, 0));

                LLVMSetFunctionCallConv(close, LLVMCCallConv);
                LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, close, "entry");
                LLVMPositionBuilderAtEnd(builder, entry);

                LLVMValueRef fp = LLVMGetParam(close, 0);
                PointerPointer<Pointer> closeArgs = new PointerPointer<>(1)
                        .put(0, fp);
                LLVMValueRef code = LLVMBuildCall(builder, fclose, closeArgs, 1, "writechar");

                LLVMBuildRet(builder, LLVMBuildICmp(builder, LLVMIntEQ, code, LLVMConstInt(i32Type, 0, 0), ""));

                if (LLVMVerifyFunction(close, LLVMPrintMessageAction) != 0) {
                    log.error("Error when validating open function:");
                    LLVMDumpModule(module);
                    System.exit(1);
                }

                scope.declareFunction(CLOSE, close);
            } else {
                throw new UnsupportedOperationException("Built in function `" + builtInFunction.getSignature() + "` has not been implemented in LLVM");
            }
        }
    }

    private void buildPrintbMethod(LLVMContextRef context, LLVMModuleRef module, LLVMBuilderRef builder) {
        printB = LLVMAddFunction(module, "printb", LLVMFunctionType(LLVMVoidTypeInContext(context), i1Type, 1, 0));
        LLVMSetFunctionCallConv(printB, LLVMCCallConv);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, printB, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);

        LLVMValueRef x = LLVMGetParam(printB, 0);

        //printf("%s\n, x ? "true" : "false");)
        LLVMValueRef cond = LLVMBuildSelect(builder, x, LLVMBuildGlobalStringPtr(builder, "true", "true") , LLVMBuildGlobalStringPtr(builder, "false", "false"), "cond");

        PointerPointer<Pointer> printArgs = new PointerPointer<>(2)
                .put(0, LLVMBuildGlobalStringPtr(builder, "%s\n", "str"))
                .put(1, cond);

        LLVMBuildCall(builder, printf, printArgs, 2, "printcall");
        LLVMBuildRetVoid(builder);

        if (LLVMVerifyFunction(printB, LLVMPrintMessageAction) != 0) {
            log.error("Error when validating printB function:");
            LLVMDumpModule(module);
            System.exit(1);
        }
    }

    private LLVMTypeRef buildFunctionType(List<BoundFunctionParameterExpression> arguments, TypeSymbol returnType, LLVMContextRef context) {

        List<TypeSymbol> argumentTypes = arguments.stream()
                .map(BoundFunctionParameterExpression::getType)
                .collect(Collectors.toList());

        LLVMTypeRef llvmReturnType = getLlvmTypeRef(returnType, context);

        if (argumentTypes.isEmpty()) {
            return LLVMFunctionType(llvmReturnType, LLVMVoidType(), 0, 0);
        }

        PointerPointer<Pointer> llvmArgumentTypes = new PointerPointer<>(argumentTypes.size());
        for (int i = 0; i < argumentTypes.size(); i++) {
            TypeSymbol argumentType = argumentTypes.get(i);
            LLVMTypeRef llvmTypeRef = getLlvmTypeRef(argumentType, context);
            if (arguments.get(i).isReference()) {
                llvmTypeRef = LLVMPointerType(llvmTypeRef, 0);
            }
            llvmArgumentTypes.put(i, llvmTypeRef);
        }

        return LLVMFunctionType(llvmReturnType, llvmArgumentTypes, argumentTypes.size(), 0);
    }

    private LLVMValueRef visit(BoundExpression expression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        switch (expression.getBoundExpressionType()) {
            case NOOP:
                return LLVMConstInt(i32Type, 0, 0);
            case LITERAL:
                return visit((BoundLiteralExpression) expression, builder, context, function);
            case PRINT_INTRINSIC:
                return visit((BoundPrintExpression) expression, builder, context, function);
            case BINARY_EXPRESSION:
                return visit((BoundBinaryExpression) expression, builder, context, function);
            case VARIABLE_DECLARATION:
                return visit((BoundVariableDeclarationExpression) expression, builder, context, function);
            case VARIABLE_EXPRESSION:
                return visit((BoundVariableExpression) expression, builder, context, function);
            case IF:
                return visit((BoundIfExpression) expression, builder, context, function);
            case BLOCK:
                scope = new Scope(scope);
                LLVMValueRef res = visit((BoundBlockExpression) expression, builder, context, function);
                scope = scope.getParentScope();
                return res;
            case INCREMENT:
                return visit((BoundIncrementExpression) expression, builder, context, function);
            case WHILE:
                return visit((BoundWhileExpression) expression, builder, context, function);
            case ASSIGNMENT_EXPRESSION:
                return visit((BoundAssignmentExpression) expression, builder, context, function);
            case FUNCTION_CALL:
                return visit((BoundFunctionCallExpression) expression, builder, context, function);
            case RETURN:
                return visit((BoundReturnExpression) expression, builder, context, function);
            case C_STYLE_FOR_EXPRESSION:
                return visit((BoundCStyleForExpression) expression, builder, context, function);
            case STRUCT_DECLARATION_EXPRESSION:
                return visit((BoundStructDeclarationExpression) expression, builder, context, function);
            case STRUCT_LITERAL_EXPRESSION:
                return visit((BoundStructLiteralExpression) expression, builder, context, function);
            case MEMBER_ACCESSOR:
                return visit((BoundMemberAccessorExpression) expression, builder, context, function);
            case MEMBER_ASSIGNMENT_EXPRESSION:
                return visit((BoundMemberAssignmentExpression) expression, builder, context, function);
            case TUPLE_LITERAL_EXPRESSION:
                return visit((BoundTupleLiteralExpression) expression, builder, context, function);
            case TUPLE_INDEX_EXPRESSION:
                return visit((BoundTupleIndexExpression) expression, builder, context, function);
            case ARRAY_LITERAL_EXPRESSION:
                return visit((BoundArrayLiteralExpression) expression, builder, context, function);
            case POSITIONAL_ACCESS_EXPRESSION:
                return visit((BoundPositionalAccessExpression) expression, builder, context, function);
            case ARRAY_LENGTH_EXPRESSION:
                return visit((BoundArrayLengthExpression) expression, builder, context, function);
            case ARRAY_ASSIGNMENT_EXPRESSION:
                return visit((BoundArrayAssignmentExpression) expression, builder, context, function);
            case ENUM_DECLARATION_EXPRESSION:
                return visit((BoundEnumDeclarationExpression) expression, builder, context, function);
            case UNARY_EXPRESSION:
                return visit((BoundUnaryExpression) expression, builder, context, function);
            case CAST_EXPRESSION:
                return visit((BoundCastExpression) expression, builder, context, function);
            case ARRAY_DECLARATION_EXPRESSION:
                return visit((BoundArrayDeclarationExpression)expression, builder, context, function);
            default:
                throw new UnsupportedOperationException("Compilation for `" + expression.getBoundExpressionType() + "` is not yet implemented in LLVM");
        }
    }

    private LLVMValueRef visit(BoundCastExpression castExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        LLVMValueRef expression = visit(castExpression.getExpression(), builder, context, function);

        if (castExpression.getType() == castExpression.getExpression().getType()) {
            return expression;
        }

        if (castExpression.getExpression().getType() == INT) {
            if (castExpression.getType() == REAL) {
                return LLVMBuildCast(builder, LLVMSIToFP, dereference(builder, expression, ""), getLlvmTypeRef(REAL, context), "");
            }
            if (castExpression.getType() == CHAR) {
                return LLVMBuildCast(builder, LLVMTrunc, dereference(builder, expression, ""), getLlvmTypeRef(CHAR, context), "");
            }
        }
        if (castExpression.getExpression().getType() == REAL) {
            if (castExpression.getType() == INT) {
                return LLVMBuildCast(builder, LLVMFPToSI, dereference(builder, expression, ""), getLlvmTypeRef(INT, context), "");
            }
        }
        if (castExpression.getExpression().getType() == CHAR) {
            if (castExpression.getType() == INT) {
                return LLVMBuildCast(builder, LLVMSExt, dereference(builder, expression, ""), getLlvmTypeRef(INT, context), "");
            }
        }
        throw new UnsupportedOperationException("Casts from `" + castExpression.getExpression().getType() + "` to `" + castExpression.getType() + "` are not supported");
    }

    //Doesn't emit any LLVM
    private LLVMValueRef visit(BoundEnumDeclarationExpression enumDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        return null;
    }

    /*
        Impl Note: Arrays are implemented in Rasna as an anonymous struct:
        struct _<T> {
            i32 size
            T*  data
        }
     */
    private LLVMValueRef visit(BoundArrayLiteralExpression arrayLiteralExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMTypeRef arrayStructType = getLlvmTypeRef(arrayLiteralExpression.getType(), context);
        LLVMTypeRef dataType = LLVMPointerType(getLlvmTypeRef(arrayLiteralExpression.getType(), context), 0);

        int elementCount = arrayLiteralExpression.getElements().size();

        LLVMTypeRef sizeInBytes = LLVMArrayType(getLlvmTypeRef(((ArrayTypeSymbol) arrayLiteralExpression.getType()).getType(), context), elementCount);
        LLVMValueRef compoundliteral = LLVMBuildAlloca(builder, sizeInBytes, ".compoundliteral");

        List<BoundExpression> elements = arrayLiteralExpression.getElements();

        LLVMValueRef structPtr = LLVMBuildAlloca(builder, arrayStructType, "tmp.array.struct");
        if (elements.isEmpty()) {
            return structPtr;
        }

        //Populate the array
        BoundExpression init = elements.get(0);
        PointerPointer<Pointer> indices = new PointerPointer<>(2)
                .put(0, LLVMConstInt(i64Type, 0, 0))
                .put(1, LLVMConstInt(i64Type, 0, 0));
        LLVMValueRef prev = LLVMBuildInBoundsGEP(builder, compoundliteral, indices, 2, "arrayinit.begin");
        LLVMBuildStore(builder, dereference(builder, visit(init, builder, context, function), ""), prev);

        for (int i = 1; i < elements.size(); i++) {
            BoundExpression element = elements.get(i);
            indices = new PointerPointer<>(1)
                    .put(0, LLVMConstInt(i64Type, 1, 0));
            prev = LLVMBuildInBoundsGEP(builder, prev, indices, 1, "arrayinit.element");
            LLVMBuildStore(builder, dereference(builder, visit(element, builder, context, function), ""), prev);
        }

        indices = new PointerPointer<>(2)
                .put(0, LLVMConstInt(i64Type, 0, 0))
                .put(1, LLVMConstInt(i64Type, 0, 0));
        LLVMValueRef arraydecay = LLVMBuildInBoundsGEP(builder, compoundliteral, indices, 2, "arraydecay");

        LLVMValueRef sizePtr = LLVMBuildStructGEP(builder, structPtr, 0, "size");
        LLVMValueRef sizeVal = LLVMConstInt(i32Type, arrayLiteralExpression.getElements().size(), 0);
        LLVMBuildStore(builder, sizeVal, sizePtr);
        LLVMValueRef dataPtrPtr = LLVMBuildStructGEP(builder, structPtr, 1, "data");
        LLVMBuildStore(builder, arraydecay, dataPtrPtr);

        return structPtr;
    }

    private LLVMValueRef visit(BoundArrayDeclarationExpression arrayDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        TypeSymbol elementType = ((ArrayTypeSymbol) arrayDeclarationExpression.getType()).getType();
        int elementCount = (int) arrayDeclarationExpression.getElementCount().getConstValue();
        LLVMTypeRef sizeInBytes = LLVMArrayType(getLlvmTypeRef(elementType, context), elementCount);
        LLVMValueRef array = LLVMBuildAlloca(builder, sizeInBytes, "array");

        LLVMValueRef ptr = LLVMBuildBitCast(builder, array, LLVMPointerType(getLlvmTypeRef(elementType, context), 0), "");

        //TODO: The i8type only works for Char arrays
        LLVMBuildMemSet(builder, ptr, LLVMConstInt(i8Type, 0, 0), LLVMConstInt(i32Type, elementCount, 0), 0);

        LLVMTypeRef arrayStructType = getLlvmTypeRef(arrayDeclarationExpression.getType(), context);
        LLVMValueRef structPtr = LLVMBuildAlloca(builder, arrayStructType, "tmp.array.struct");
        LLVMValueRef sizePtr = LLVMBuildStructGEP(builder, structPtr, 0, "size");
        LLVMBuildStore(builder, LLVMConstInt(i32Type, elementCount, 0), sizePtr);
        LLVMValueRef arrPtr = LLVMBuildStructGEP(builder, structPtr, 1, "arr");
        LLVMBuildStore(builder, ptr, arrPtr);

        return structPtr;
    }

    private LLVMValueRef visit(BoundArrayLengthExpression arrayLengthExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        LLVMValueRef struct = ref(builder, visit(arrayLengthExpression.getIterable(), builder, context, function), arrayLengthExpression.getIterable().getType(), context);

        return LLVMBuildStructGEP(builder, struct, 0, "size");
    }

    private LLVMValueRef visit(BoundMemberAccessorExpression memberAccessorExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (memberAccessorExpression.getOwner() instanceof BoundTypeExpression) {

            BoundTypeExpression typeExpression = (BoundTypeExpression) memberAccessorExpression.getOwner();
            BoundExpression member = memberAccessorExpression.getMember();

            if (!(member instanceof BoundVariableExpression)) {
                throw new IllegalStateException("Cannot access anything other than variables in enums");
            }
            BoundVariableExpression memberVariable = (BoundVariableExpression) member;

            int ordinal = ((EnumTypeSymbol) typeExpression.getTypeSymbol()).ordinalOf(memberVariable.getVariable().getName());

            return LLVMConstInt(i32Type, ordinal, 0);
        }

        LLVMValueRef owner = visit(memberAccessorExpression.getOwner(), builder, context, function);
        owner = ref(builder, owner, memberAccessorExpression.getOwner().getType(), context);
        assert memberAccessorExpression.getMember() instanceof BoundVariableExpression;
        BoundVariableExpression member = (BoundVariableExpression) memberAccessorExpression.getMember();

        List<VariableSymbol> members = new ArrayList<>(memberAccessorExpression.getOwner().getType().getFields().values());
        int idx = members.indexOf(member.getVariable());

        return LLVMBuildStructGEP(builder, owner, idx, member.getVariable().getName());
    }

    private LLVMValueRef visit(BoundTupleIndexExpression tupleIndexExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMValueRef tuple = visit(tupleIndexExpression.getTuple(), builder, context, function);
        tuple = ref(builder, tuple, tupleIndexExpression.getTuple().getType(), context);

        int index = (int) tupleIndexExpression.getIndex().getValue();

        TypeSymbol memberType = ((TupleTypeSymbol) tupleIndexExpression.getTuple().getType()).getTypes().get(index);

        return ref(builder, LLVMBuildStructGEP(builder, tuple, index, String.valueOf(index)), memberType, context);
    }

    private LLVMValueRef visit(BoundPositionalAccessExpression positionalAccessExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        LLVMValueRef struct = ref(builder, visit(positionalAccessExpression.getArray(), builder, context, function), positionalAccessExpression.getArray().getType(), context);

        LLVMValueRef index = dereference(builder, visit(positionalAccessExpression.getIndex(), builder, context, function), "idx");

        LLVMValueRef array = dereference(builder, LLVMBuildStructGEP(builder, struct, 1, "arr"), "arr");

        PointerPointer<Pointer> indices = new PointerPointer<>(1)
                .put(0, index);
        LLVMValueRef arrayidx = LLVMBuildInBoundsGEP(builder, array, indices, 1, "arrayidx");

        return LLVMBuildLoad(builder, arrayidx, "");
    }

    private LLVMValueRef visit(BoundArrayAssignmentExpression arrayAssignmentExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        BoundPositionalAccessExpression arrayAccessExpression = arrayAssignmentExpression.getArrayAccessExpression();
        LLVMValueRef struct = ref(builder, visit(arrayAccessExpression.getArray(), builder, context, function), arrayAccessExpression.getArray().getType(), context);

        LLVMValueRef index = dereference(builder, visit(arrayAccessExpression.getIndex(), builder, context, function), "idx");

        LLVMValueRef array = dereference(builder, LLVMBuildStructGEP(builder, struct, 1, "arr"), "arr");

        PointerPointer<Pointer> indices = new PointerPointer<>(1)
                .put(0, index);
        LLVMValueRef arrayidx = LLVMBuildInBoundsGEP(builder, array, indices, 1, "arrayidx");

        LLVMValueRef value = visit(arrayAssignmentExpression.getAssignment(), builder, context, function);
        value = dereference(builder, value, "value");

        return LLVMBuildStore(builder, value, arrayidx);
    }

    private LLVMValueRef visit(BoundMemberAssignmentExpression memberAssignmentExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMValueRef owner = visit(memberAssignmentExpression.getMemberAccessorExpression().getOwner(), builder, context, function);
        owner = ref(builder, owner, memberAssignmentExpression.getMemberAccessorExpression().getOwner().getType(), context);

        assert memberAssignmentExpression.getMemberAccessorExpression().getMember() instanceof BoundVariableExpression;
        BoundVariableExpression member = (BoundVariableExpression) memberAssignmentExpression.getMemberAccessorExpression().getMember();

        List<VariableSymbol> members = new ArrayList<>(memberAssignmentExpression.getMemberAccessorExpression().getOwner().getType().getFields().values());
        int idx = members.indexOf(member.getVariable());

        LLVMValueRef element = LLVMBuildStructGEP(builder, owner, idx, member.getVariable().getName());

        LLVMValueRef value = visit(memberAssignmentExpression.getAssignment(), builder, context, function);
        value = dereference(builder, value, "value");

        return LLVMBuildStore(builder, value, element);
    }

    private LLVMValueRef visit(BoundStructDeclarationExpression structDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMTypeRef structTypeRef = LLVMStructCreateNamed(context, structDeclarationExpression.getType().getName());
        scope.declareType(structDeclarationExpression.getType(), structTypeRef);

        if (structDeclarationExpression.getType() instanceof ParameterisedTypeSymbol) {
            return null;
        }

        PointerPointer<Pointer> elementTypes = new PointerPointer<>(structDeclarationExpression.getMembers().size());

        List<LLVMTypeRef> memberTypes = structDeclarationExpression.getMembers().stream()
                .map(BoundExpression::getType)
                .map(type -> getLlvmTypeRef(type, context))
                .collect(Collectors.toList());
        for (int i = 0; i < memberTypes.size(); i++) {
            elementTypes.put(i, memberTypes.get(i));
        }

        LLVMStructSetBody(structTypeRef, elementTypes, memberTypes.size(), 0);

        return null;
    }

    private LLVMValueRef visit(BoundStructLiteralExpression structLiteralExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (structLiteralExpression.getType() instanceof ParameterisedTypeSymbol) {
            LLVMTypeRef structTypeRef = LLVMStructCreateNamed(context, structLiteralExpression.getType().getName());

            PointerPointer<Pointer> llvmTypes = new PointerPointer<>(structLiteralExpression.getElements().size());
            List<BoundExpression> arguments = structLiteralExpression.getElements();
            for (int i = 0; i < arguments.size(); i++) {
                TypeSymbol type = arguments.get(i).getType();
                LLVMTypeRef llvmTypeRef = getLlvmTypeRef(type, context);
                llvmTypes.put(i, llvmTypeRef);
            }
            LLVMValueRef ptr = LLVMBuildAlloca(builder, structTypeRef, "tmp." + structLiteralExpression.getType().getName());
            LLVMStructSetBody(structTypeRef, llvmTypes, arguments.size(), 0);

            Collection<BoundExpression> elements = structLiteralExpression.getElements();
            int idx = 0;
            for (BoundExpression element : elements) {
                LLVMValueRef elementRef = LLVMBuildStructGEP(builder, ptr, idx, "");
                LLVMValueRef valueRef = visit(element, builder, context, function);
                LLVMBuildStore(builder, dereference(builder, valueRef, ""), elementRef);
                idx++;
            }
            return LLVMBuildLoad(builder, ptr, "tmp." + structLiteralExpression.getType().getName());
        }

        //Allocate a tmp variable for this... not the best but what we have to do
        LLVMTypeRef type = getLlvmTypeRef(structLiteralExpression.getType(), context);
        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, "tmp." + structLiteralExpression.getType().getName());

        Collection<VariableSymbol> members = structLiteralExpression.getType().getFields().values();
        int idx = 0;
        for (VariableSymbol member : members) {
            LLVMValueRef elementRef = LLVMBuildStructGEP(builder, ptr, idx, member.getName());
            LLVMValueRef valueRef = dereference(builder, visit(structLiteralExpression.getElements().get(idx), builder, context, function), "");
            LLVMBuildStore(builder, valueRef, elementRef);
            idx++;
        }
        return LLVMBuildLoad(builder, ptr, "tmp." + structLiteralExpression.getType().getName());
    }

    private LLVMValueRef visit(BoundTupleLiteralExpression tupleLiteralExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        //Allocate a tmp variable for this... not the best but what we have to do
        LLVMTypeRef type = getLlvmTypeRef(tupleLiteralExpression.getType(), context);
        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, "tmp." + tupleLiteralExpression.getType().getName());

        Collection<BoundExpression> elements = tupleLiteralExpression.getElements();
        int idx = 0;
        for (BoundExpression element : elements) {
            LLVMValueRef elementRef = LLVMBuildStructGEP(builder, ptr, idx, "");
            LLVMValueRef valueRef = visit(element, builder, context, function);
            LLVMBuildStore(builder, dereference(builder, valueRef, ""), elementRef);
            idx++;
        }
        return LLVMBuildLoad(builder, ptr, "tmp." + tupleLiteralExpression.getType().getName());
    }

    private LLVMValueRef visit(BoundFunctionCallExpression functionCallExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        FunctionSymbol functionSymbol = functionCallExpression.getFunction();

        PointerPointer<Pointer> args = new PointerPointer<>(functionCallExpression.getBoundArguments().size());
        List<BoundExpression> arguments = functionCallExpression.getBoundArguments();
        for (int i = 0; i < arguments.size(); i++) {
            LLVMValueRef arg = visit(arguments.get(i), builder, context, function);
            if (!functionSymbol.getArguments().get(i).isReference()) {
                arg = dereference(builder, arg, "arg");
            } else {
                arg = ref(builder, arg, arguments.get(i).getType(), context);
            }
            args.put(i, arg);
        }

        if (functionSymbol.getType() == UNIT) {
            return scope.tryLookupFunction(functionSymbol).map(func -> LLVMBuildCall(builder, func, args, functionCallExpression.getBoundArguments().size(), ""))
                    .orElseThrow(() -> new IllegalStateException("No such function defined in scope: `" + functionSymbol.getSignature() + "`"));
        }
        return scope.tryLookupFunction(functionSymbol).map(func -> LLVMBuildCall(builder, func, args, functionCallExpression.getBoundArguments().size(), functionSymbol.getName()))
                .orElseThrow(() -> new IllegalStateException("No such function defined in scope: `" + functionSymbol.getSignature() + "`"));
    }

    private LLVMValueRef visit(BoundAssignmentExpression assignmentExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        VariableSymbol variable = assignmentExpression.getVariable();
        LLVMValueRef ptr = null;
        Optional<LLVMValueRef> variableRef = scope.tryLookupVariable(variable);
        if (variableRef.isPresent()) {
            ptr = variableRef.get().getPointer();
        }
        Optional<LLVMValueRef> pointerRef = scope.tryLookupPointer(variable);
        if (pointerRef.isPresent()) {
            ptr = pointerRef.get();
        }
        if (ptr == null) {
            throw new IllegalStateException("Variable `" + variable.getName() + "` has not been declared");
        }

        LLVMValueRef val = visit(assignmentExpression.getExpression(), builder, context, function);
        val = dereference(builder, val, "val");

        return LLVMBuildStore(builder, val, ptr);
    }

    //TODO: You cannot mutate a function argument - callers should clone and pass a reference
    /*
    define void @foo(i32 %0) {
    entry:
      %access.tmp = alloca i32, align 4
      store i32 %0, i32* %access.tmp, align 4
      %load = load i32, i32* %access.tmp, align 4
      %incrtmp = add i32 %load, 1
      store i32 %incrtmp, i32* %access.tmp, align 4
      %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
      ret void
     */
    private LLVMValueRef visit(BoundIncrementExpression incrementExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        VariableSymbol variable = incrementExpression.getVariableSymbol();
        LLVMValueRef ptr = null;
        Optional<LLVMValueRef> variableRef = scope.tryLookupVariable(variable);
        if (variableRef.isPresent()) {
            ptr = ref(builder, variableRef.get(), INT, context);
        }
        Optional<LLVMValueRef> pointerRef = scope.tryLookupPointer(variable);
        if (pointerRef.isPresent()) {
            ptr = pointerRef.get();
        }
        if (ptr == null) {
            throw new IllegalStateException("Variable `" + variable.getName() + "` has not been declared");
        }

        if (incrementExpression.getType() == INT) {
            return LLVMBuildStore(builder, LLVMBuildAdd(builder, LLVMBuildLoad(builder, ptr, "load"), LLVMConstInt(i32Type, (int) incrementExpression.getAmount().getValue(), 1), "incrtmp"), ptr);
        } else if (incrementExpression.getType() == CHAR) {
            return LLVMBuildStore(builder, LLVMBuildAdd(builder, LLVMBuildLoad(builder, ptr, "load"), LLVMConstInt(i8Type, (int) incrementExpression.getAmount().getValue(), 1), "incrtmp"), ptr);
        } else {
            throw new UnsupportedOperationException("Increment expressions for type `" + incrementExpression.getType() + "` are not yet supported in LLVM");
        }
    }

    private LLVMValueRef visit(BoundBlockExpression blockExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        LLVMValueRef lastVal = null;
        for (BoundExpression expression : blockExpression.getExpressions()) {
            lastVal = visit(expression, builder, context, function);
        }
        return lastVal;
    }

    private LLVMValueRef visit(BoundWhileExpression whileExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMBasicBlockRef cond = LLVMAppendBasicBlockInContext(context, function, "while.cond");
        LLVMBasicBlockRef body = LLVMAppendBasicBlockInContext(context, function, "while.body");
        LLVMBasicBlockRef exit = LLVMAppendBasicBlockInContext(context, function, "while.exit");

        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, cond);
        LLVMValueRef condition = visit(whileExpression.getCondition(), builder, context, function);
        LLVMBuildCondBr(builder, condition, body, exit);

        LLVMPositionBuilderAtEnd(builder, body);
        visit(whileExpression.getBody(), builder, context, function);
        LLVMBuildBr(builder, cond);

        LLVMPositionBuilderAtEnd(builder, exit);

        return null;
    }

    private LLVMValueRef visit(BoundCStyleForExpression cStyleForExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        scope = new Scope(scope);

        LLVMBasicBlockRef forCondBlock = LLVMAppendBasicBlockInContext(context, function, "for.cond");
        LLVMBasicBlockRef forBodyBlock = LLVMAppendBasicBlockInContext(context, function, "for.body");
        LLVMBasicBlockRef forIncrBlock = LLVMAppendBasicBlockInContext(context, function, "for.incr");
        LLVMBasicBlockRef forExitBlock = LLVMAppendBasicBlockInContext(context, function, "for.exit");

        visit(cStyleForExpression.getInitialisation(), builder, context, function);
        LLVMBuildBr(builder, forCondBlock);

        LLVMPositionBuilderAtEnd(builder, forCondBlock);
        LLVMValueRef condition = visit(cStyleForExpression.getCondition(), builder, context, function);
        LLVMBuildCondBr(builder, condition, forBodyBlock, forExitBlock);

        LLVMPositionBuilderAtEnd(builder, forBodyBlock);
        LLVMValueRef body = visit(cStyleForExpression.getBody(), builder, context, function);
        LLVMBuildBr(builder, forIncrBlock);

        LLVMPositionBuilderAtEnd(builder, forIncrBlock);
        visit(cStyleForExpression.getPostStep(), builder, context, function);
        LLVMBuildBr(builder, forCondBlock);

        LLVMPositionBuilderAtEnd(builder, forExitBlock);

        scope = scope.getParentScope();

        return body;
    }

    private LLVMValueRef visit(BoundIfExpression ifExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (ifExpression.getElseBody() == null) {
            LLVMBasicBlockRef thenBlock = LLVMAppendBasicBlockInContext(context, function, "if.then");
            LLVMBasicBlockRef endBlock = LLVMAppendBasicBlockInContext(context, function, "if.end");

            LLVMValueRef condition = visit(ifExpression.getCondition(), builder, context, function);
            condition = dereference(builder, condition, "");
            LLVMBuildCondBr(builder, condition, thenBlock, endBlock);

            LLVMPositionBuilderAtEnd(builder, thenBlock);
            LLVMValueRef thenVal = visit(ifExpression.getBody(), builder, context, function);
            if (thenVal == null) {
                throw new IllegalStateException("ThenVal must not be null");
            }
            LLVMValueRef terminator = LLVMGetBasicBlockTerminator(thenBlock);
            if (terminator == null) {
                LLVMBuildBr(builder, endBlock);
            }
            LLVMPositionBuilderAtEnd(builder, endBlock);
            return thenVal;
        }


        /*
        define dso_local i32 @main() #0 {
        entry:
          %retval = alloca i32, align 4
          %n = alloca i32, align 4
          store i32 0, i32* %retval, align 4
          store i32 1, i32* %n, align 4
          %0 = load i32, i32* %n, align 4
          %cmp = icmp eq i32 %0, 0
          br i1 %cmp, label %if.then, label %if.else

        if.then:                                          ; preds = %entry
          %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @"??_C@_03OFAPEBGM@?$CFs?6?$AA@", i64 0, i64 0), i8* getelementptr inbounds ([5 x i8], [5 x i8]* @"??_C@_04PCJGBDOP@Zero?$AA@", i64 0, i64 0))
          br label %if.end6

        if.else:                                          ; preds = %entry
          %1 = load i32, i32* %n, align 4
          %cmp1 = icmp eq i32 %1, 1
          br i1 %cmp1, label %if.then2, label %if.else4

        if.then2:                                         ; preds = %if.else
          %call3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @"??_C@_03OFAPEBGM@?$CFs?6?$AA@", i64 0, i64 0), i8* getelementptr inbounds ([4 x i8], [4 x i8]* @"??_C@_03EHAPBBEA@One?$AA@", i64 0, i64 0))
          br label %if.end

        if.else4:                                         ; preds = %if.else
          %call5 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @"??_C@_03OFAPEBGM@?$CFs?6?$AA@", i64 0, i64 0), i8* getelementptr inbounds ([8 x i8], [8 x i8]* @"??_C@_07NBCGADJA@Unknown?$AA@", i64 0, i64 0))
          br label %if.end

        if.end:                                           ; preds = %if.else4, %if.then2
          br label %if.end6

        if.end6:                                          ; preds = %if.end, %if.then
          %2 = load i32, i32* %retval, align 4
          ret i32 %2
        }
         */
        LLVMBasicBlockRef thenBlock = LLVMAppendBasicBlockInContext(context, function, "if.then");
        LLVMBasicBlockRef elseBlock = LLVMAppendBasicBlockInContext(context, function, "if.else");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlockInContext(context, function, "if.end");

        LLVMValueRef condition = visit(ifExpression.getCondition(), builder, context, function);
        condition = dereference(builder, condition, "");
        LLVMBuildCondBr(builder, condition, thenBlock, elseBlock);

        LLVMPositionBuilderAtEnd(builder, thenBlock);
        LLVMValueRef thenVal = visit(ifExpression.getBody(), builder, context, function);
        thenBlock = LLVMGetInsertBlock(builder);

        boolean thenTerminated = LLVMGetBasicBlockTerminator(thenBlock) != null;
        if (!thenTerminated) {
            LLVMBuildBr(builder, endBlock);
        }

        LLVMPositionBuilderAtEnd(builder, elseBlock);
        LLVMValueRef elseVal = visit(ifExpression.getElseBody(), builder, context, function);

        LLVMBuildBr(builder, endBlock);
        LLVMPositionBuilderAtEnd(builder, endBlock);

        if (elseVal != null && thenVal != null) {
            //return LLVMBuildSelect(builder, condition, thenVal, elseVal, "");
        }
        return null;
    }

    private LLVMValueRef visit(BoundVariableDeclarationExpression variableDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {


        LLVMTypeRef type = getLlvmTypeRef(variableDeclarationExpression.getType(), context);

        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, variableDeclarationExpression.getVariable().getName());
        scope.declarePointer(variableDeclarationExpression.getVariable(), ptr);

        if (variableDeclarationExpression.getInitialiser() == null) {
            return ptr;
        }

        LLVMValueRef val = visit(variableDeclarationExpression.getInitialiser(), builder, context, function);
        val = dereference(builder, val, "val");

        if (variableDeclarationExpression.getType() instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) variableDeclarationExpression.getType();

            int offset = unionTypeSymbol.getTypes().indexOf(variableDeclarationExpression.getInitialiser().getType());

            ptr = LLVMBuildStructGEP(builder, ptr, offset, "");
        }
        LLVMBuildStore(builder, val, ptr);

        return val;
    }

    private LLVMTypeRef getLlvmTypeRef(TypeSymbol typeSymbol, LLVMContextRef context) {
        if (typeSymbol == UNIT) {
            return LLVMStructTypeInContext(context, new PointerPointer(0), 0, 1);
        }
        if (typeSymbol == BOOL) {
            return i1Type;
        }
        if (typeSymbol == CHAR) {
            return i8Type;
        }
        if (typeSymbol == INT) {
            return i32Type;
        }
        if (typeSymbol == REAL) {
            return realType;
        }
        if (typeSymbol == STRING) {

            PointerPointer<Pointer> structTypes = new PointerPointer<>(2)
                    .put(0, i32Type)
                    .put(1, LLVMPointerType(i8Type, 0)); //TODO: Doesn't support beyond ascii);

            return LLVMStructTypeInContext(context, structTypes, 2, 0);
        }
        if (typeSymbol instanceof TupleTypeSymbol) {
            TupleTypeSymbol tupleTypeSymbol = (TupleTypeSymbol) typeSymbol;

            PointerPointer<Pointer> llvmTypes = new PointerPointer<>(tupleTypeSymbol.getTypes().size());
            List<TypeSymbol> types = tupleTypeSymbol.getTypes();
            for (int i = 0; i < types.size(); i++) {
                TypeSymbol type = types.get(i);
                LLVMTypeRef llvmTypeRef = getLlvmTypeRef(type, context);
                llvmTypes.put(i, llvmTypeRef);
            }
            return LLVMStructTypeInContext(context, llvmTypes, types.size(), 0);
        }
        if (typeSymbol instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeSymbol;

            LLVMTypeRef dataType = LLVMPointerType(getLlvmTypeRef(arrayTypeSymbol.getType(), context), 0);

            PointerPointer<Pointer> llvmTypes = new PointerPointer<>(2)
                    .put(0, i32Type)
                    .put(1, dataType);

            return LLVMStructTypeInContext(context, llvmTypes, 2, 0);
        }
        if (typeSymbol instanceof EnumTypeSymbol) {
            return i32Type;
        }
        if (typeSymbol instanceof ErasedParameterisedTypeSymbol) {
            Optional<LLVMTypeRef> maybeType = scope.tryLookupType(typeSymbol);
            if (maybeType.isPresent()) {
                return maybeType.get();
            }
            LLVMTypeRef structTypeRef = LLVMStructCreateNamed(context, typeSymbol.getName());
            scope.declareType(typeSymbol, structTypeRef);

            PointerPointer<Pointer> elementTypes = new PointerPointer<>(typeSymbol.getFields().size());

            List<LLVMTypeRef> memberTypes = typeSymbol.getFields().values().stream()
                    .map(VariableSymbol::getType)
                    .map(type -> getLlvmTypeRef(type, context))
                    .collect(Collectors.toList());
            for (int i = 0; i < memberTypes.size(); i++) {
                elementTypes.put(i, memberTypes.get(i));
            }

            LLVMStructSetBody(structTypeRef, elementTypes, memberTypes.size(), 0);

            return structTypeRef;
        }
        if (typeSymbol instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;

            PointerPointer<Pointer> llvmTypes = new PointerPointer<>(unionTypeSymbol.getTypes().size());
            List<TypeSymbol> types = unionTypeSymbol.getTypes();
            for (int i = 0; i < types.size(); i++) {
                TypeSymbol type = types.get(i);
                LLVMTypeRef llvmTypeRef = getLlvmTypeRef(type, context);
                llvmTypes.put(i, llvmTypeRef);
            }
            return LLVMStructTypeInContext(context, llvmTypes, types.size(), 0);
        }

        if (typeSymbol == FILE) {
            return fileType;
        }

        Optional<LLVMTypeRef> type = scope.tryLookupType(typeSymbol);
        if (type.isPresent()) {
            return type.get();
        }
        throw new UnsupportedOperationException("Variables of type `" + typeSymbol + "` are not yet implemented in LLVM");
    }

    /**
     * Gets the reference to a variable. Note this is a POINTER to the variable, not the value itself.
     */
    private LLVMValueRef visit(BoundVariableExpression variableExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        VariableSymbol variable = variableExpression.getVariable();
        Optional<LLVMValueRef> variableRef = scope.tryLookupVariable(variable);
        if (variableRef.isPresent()) {
            return variableRef.get().getPointer();
        }
        Optional<LLVMValueRef> pointerRef = scope.tryLookupPointer(variable);
        if (pointerRef.isPresent()) {
            return pointerRef.get();
        }
        throw new IllegalStateException("Variable `" + variable.getName() + "` has not been declared");
    }


    private LLVMValueRef visit(BoundUnaryExpression unaryExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        LLVMValueRef operand = visit(unaryExpression.getOperand(), builder, context, function);

        switch (unaryExpression.getOperator().getBoundOpType()) {

            case NOT:
                return LLVMBuildNot(builder, operand, "");
            case NEGATION:
                if (unaryExpression.getOperand().getType() == INT) {
                    return LLVMBuildSub(builder, LLVMConstInt(i32Type, 0, 1), dereference(builder, operand, ""), ""); //lol, lmao
                }
                if (unaryExpression.getOperand().getType() == REAL) {
                    return LLVMBuildFSub(builder, LLVMConstReal(LLVMDoubleTypeInContext(context), 0.0), dereference(builder, operand, ""), ""); //lol, lmao
                }
                throw new UnsupportedOperationException("Compilation for unary operation `" + unaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM for type `" + unaryExpression.getOperand().getType() + "`");
            case ERROR:
            default:
                throw new UnsupportedOperationException("Compilation for unary operation `" + unaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM for type `" + unaryExpression.getOperand().getType() + "`");
        }
    }

    private LLVMValueRef visit(BoundBinaryExpression binaryExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMValueRef lhs = visit(binaryExpression.getLeft(), builder, context, function);
        LLVMValueRef rhs = visit(binaryExpression.getRight(), builder, context, function);

        if (binaryExpression.getLeft().getType() == INT && binaryExpression.getRight().getType() == INT) {
            return visitIntBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), rhs);
        }
        if (binaryExpression.getLeft().getType() instanceof EnumTypeSymbol && binaryExpression.getRight().getType() instanceof EnumTypeSymbol) {
            return visitIntBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), rhs);
        }
        if (binaryExpression.getLeft().getType() instanceof EnumTypeSymbol && binaryExpression.getRight().getType() == INT) {
            return visitIntBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), rhs);
        }
        if (binaryExpression.getLeft().getType() == CHAR && binaryExpression.getRight().getType() == CHAR) {
            return visitIntBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), rhs);
        }
        if (binaryExpression.getLeft().getType() == CHAR && binaryExpression.getRight().getType() == INT) {
            LLVMValueRef i8rhs = LLVMBuildCast(builder, LLVMTrunc, rhs, i8Type, "");
            return visitIntBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), i8rhs);
        }
        if (binaryExpression.getLeft().getType() == REAL && binaryExpression.getRight().getType() == REAL) {
            return visitRealBinop(builder, lhs, binaryExpression.getOperator().getBoundOpType(), rhs);
        }
        if (binaryExpression.getLeft().getType() == BOOL && binaryExpression.getRight().getType() == BOOL) {
            switch (binaryExpression.getOperator().getBoundOpType()) {
                case EQUALS:
                    return LLVMBuildICmp(builder, LLVMIntEQ, lhs, rhs, "");
                case NOT_EQUALS:
                    return LLVMBuildICmp(builder, LLVMIntNE, lhs, rhs, "");
                case BOOLEAN_OR:
                    return LLVMBuildOr(builder, lhs, rhs, "ortmp");
                case BOOLEAN_AND:
                    return LLVMBuildAnd(builder, lhs, rhs, "andtmp");
                case BOOLEAN_XOR:
                    return LLVMBuildXor(builder, lhs, rhs, "xortmp");
                default:
                    throw new UnsupportedOperationException("Compilation for binary operation `" + binaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM");
            }
        }
        if (binaryExpression.getLeft().getType() == STRING && binaryExpression.getRight().getType() == STRING) {
            switch (binaryExpression.getOperator().getBoundOpType()) {
                case CONCATENATION:
                    //return concatenate(lhs, rhs, builder, context, function);
                case EQUALS:
                default:
                    throw new UnsupportedOperationException("Compilation for binary operation `" + binaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM");

            }
        }
        throw new UnsupportedOperationException("Compilation for binary operation `" + binaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM for types `" + binaryExpression.getLeft().getType() + "` and `" + binaryExpression.getRight().getType() + "`");
    }

    private LLVMValueRef visitRealBinop(LLVMBuilderRef builder, LLVMValueRef lhs, BoundBinaryOperator.BoundBinaryOperation op, LLVMValueRef rhs) {
        lhs = dereference(builder, lhs, "lhs");
        rhs = dereference(builder, rhs, "rhs");
        switch (op) {
            case ADDITION:
                return LLVMBuildFAdd(builder, lhs, rhs, "saddtmp");
            case SUBTRACTION:
                return LLVMBuildFSub(builder, lhs, rhs, "ssubtmp");
            case MULTIPLICATION:
                return LLVMBuildFMul(builder, lhs, rhs, "smultmp");
            case DIVISION:
                return LLVMBuildFDiv(builder, lhs, rhs, "sdivtmp");
            case REMAINDER:
                return LLVMBuildFRem(builder, lhs, rhs, "sremtmp");
            case GREATER_THAN:
                return LLVMBuildFCmp(builder, LLVMRealOGT, lhs, rhs, "");
            case LESS_THAN:
                return LLVMBuildFCmp(builder, LLVMRealOLT, lhs, rhs, "");
            case GREATER_THAN_OR_EQUAL:
                return LLVMBuildFCmp(builder, LLVMRealOGE, lhs, rhs, "");
            case LESS_THAN_OR_EQUAL:
                return LLVMBuildFCmp(builder, LLVMRealOLE, lhs, rhs, "");
            case EQUALS:
                return LLVMBuildFCmp(builder, LLVMRealOEQ, lhs, rhs, "");
            case NOT_EQUALS:
                return LLVMBuildFCmp(builder, LLVMRealONE, lhs, rhs, "");
            default:
                throw new UnsupportedOperationException("Compilation for binary operation `" + op + "` is not yet supported for LLVM");
        }
    }

    private LLVMValueRef visitIntBinop(LLVMBuilderRef builder, LLVMValueRef lhs, BoundBinaryOperator.BoundBinaryOperation op, LLVMValueRef rhs) {
        lhs = dereference(builder, lhs, "lhs");
        rhs = dereference(builder, rhs, "rhs");
        switch (op) {
            case ADDITION:
                return LLVMBuildAdd(builder, lhs, rhs, "saddtmp");
            case SUBTRACTION:
                return LLVMBuildSub(builder, lhs, rhs, "ssubtmp");
            case MULTIPLICATION:
                return LLVMBuildMul(builder, lhs, rhs, "smultmp");
            case DIVISION:
                return LLVMBuildSDiv(builder, lhs, rhs, "sdivtmp");
            case REMAINDER:
                return LLVMBuildSRem(builder, lhs, rhs, "sremtmp");
            case GREATER_THAN:
                return LLVMBuildICmp(builder, LLVMIntSGT, lhs, rhs, "");
            case LESS_THAN:
                return LLVMBuildICmp(builder, LLVMIntSLT, lhs, rhs, "");
            case GREATER_THAN_OR_EQUAL:
                return LLVMBuildICmp(builder, LLVMIntSGE, lhs, rhs, "");
            case LESS_THAN_OR_EQUAL:
                return LLVMBuildICmp(builder, LLVMIntSLE, lhs, rhs, "");
            case EQUALS:
                return LLVMBuildICmp(builder, LLVMIntEQ, lhs, rhs, "");
            case NOT_EQUALS:
                return LLVMBuildICmp(builder, LLVMIntNE, lhs, rhs, "");
            default:
                throw new UnsupportedOperationException("Compilation for binary operation `" + op + "` is not yet supported for LLVM");
        }
    }

    private LLVMValueRef visit(BoundLiteralExpression literalExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (literalExpression.getType() == BOOL) {
            return LLVMConstInt(i1Type, (boolean) literalExpression.getValue() ? 1 : 0, 0);
        }
        if (literalExpression.getType() == CHAR) {
            return LLVMConstInt(i8Type, (char) literalExpression.getValue(), 0);
        }
        if (literalExpression.getType() == INT) {
            return LLVMConstInt(i32Type, (int) literalExpression.getValue(), 0);
        }
        if (literalExpression.getType() == REAL) {
            return LLVMConstReal(realType, (double) literalExpression.getValue());
        }
        if (literalExpression.getType() == STRING) {
            return buildString(literalExpression, builder, context);
        }
        if (literalExpression.getType() == UNIT) {
            return buildUnit(builder, context);
        }
        throw new UnsupportedOperationException("Literals of type `" + literalExpression.getType() + "` are not yet supported in LLVM");
    }

    private LLVMValueRef buildUnit(LLVMBuilderRef builder, LLVMContextRef context) {
        LLVMTypeRef unitStructType = LLVMStructTypeInContext(context, new PointerPointer<>(0), 0, 1);
        LLVMValueRef unitStructPtr = LLVMBuildAlloca(builder, unitStructType, "");
        return LLVMBuildLoad(builder, unitStructPtr, "");
    }

    private LLVMValueRef buildString(BoundLiteralExpression literalExpression, LLVMBuilderRef builder, LLVMContextRef context) {
        String value = (String) literalExpression.getValue();
        LLVMValueRef charPtrRef = LLVMBuildGlobalStringPtr(builder, (String) literalExpression.getValue(), "string");

        LLVMTypeRef stringTypeRef = getLlvmTypeRef(STRING, context);

        LLVMValueRef stringPtr = LLVMBuildAlloca(builder, stringTypeRef, "");

        LLVMValueRef sizeVal = LLVMBuildStructGEP(builder, stringPtr, 0, "");
        LLVMBuildStore(builder, LLVMConstInt(i32Type, value.length(), 0), sizeVal);
        LLVMValueRef stringVal = LLVMBuildStructGEP(builder, stringPtr, 1, "");
        LLVMBuildStore(builder, charPtrRef, stringVal);

        return stringPtr;
    }

    private LLVMValueRef visit(BoundPrintExpression printExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (formatStr == null) {
            formatStr = LLVMBuildGlobalStringPtr(builder, "%d\n", "formatStr");
        }

        if (printExpression.getExpression() instanceof BoundLiteralExpression && printExpression.getExpression().getType() == STRING) {
            String literal = (String) ((BoundLiteralExpression) printExpression.getExpression()).getValue();
            LLVMValueRef val = LLVMBuildGlobalString(builder, literal, "");
            //LLVMValueRef string = dereference(builder, LLVMBuildStructGEP(builder, val, 1, "string"), "");
            PointerPointer<Pointer> printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%s", "str"))
                    .put(1, val);
            return LLVMBuildCall(builder, printf, printArgs, 2, "printcall");
        }

        LLVMValueRef res = visit(printExpression.getExpression(), builder, context, function);
        if (printExpression.getExpression().getType() != STRING && LLVMGetTypeKind(LLVMTypeOf(res)) == LLVMPointerTypeKind) {
            res = LLVMBuildLoad(builder, res, "print");
        }

        PointerPointer<Pointer> printArgs;
        if (printExpression.getExpression().getType() == STRING) {
            res = ref(builder, res, STRING, context);
            //TODO: The actual length of the string is stored in there somewhere, use it
            LLVMValueRef size = dereference(builder, LLVMBuildStructGEP(builder, res, 0, "size"), "");
            LLVMValueRef string = dereference(builder, LLVMBuildStructGEP(builder, res, 1, "string"), "");
            printArgs = new PointerPointer<>(3)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%.*s", "str"))
                    .put(1, size)
                    .put(2, string);
            return LLVMBuildCall(builder, printf, printArgs, 3, "printcall");
        } else if (printExpression.getExpression().getType() == CHAR) {
            printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%c", "real"))
                    .put(1, res);
        } else if (printExpression.getExpression().getType() == REAL) {
            printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%f\n", "real"))
                    .put(1, res);
        } else if (printExpression.getExpression().getType() == BOOL) {
            printArgs = new PointerPointer<>(1)
                    .put(0, res);
            return LLVMBuildCall(builder, printB, printArgs, 1, "");
        } else if (printExpression.getExpression().getType() instanceof EnumTypeSymbol) {
            printArgs = new PointerPointer<>(1)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%d\n", "str")) //TODO: Need to associate the name with the ordinal. A struct? struct Color { int ord; char* name; }?
                    .put(1, res);
        } else {
            printArgs = new PointerPointer<>(2)
                    .put(0, formatStr)
                    .put(1, res);
        }

        return LLVMBuildCall(builder, printf, printArgs, 2, "printcall");
    }

    private void visitMainMethod(BoundFunctionDeclarationExpression mainMethodDeclaration, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        List<TypeSymbol> argumentTypes = mainMethodDeclaration.getArguments().stream()
                .map(BoundFunctionParameterExpression::getType)
                .collect(Collectors.toList());

        if (argumentTypes.size() == 1) {
            List<BoundFunctionParameterExpression> arguments = mainMethodDeclaration.getArguments();
            BoundFunctionParameterExpression argument = arguments.get(0);
            LLVMValueRef val = LLVMGetParam(function, 0);

            scope.declareVariable(argument.getArgument(), val);
        } else if (!argumentTypes.isEmpty()) {
            throw new IllegalStateException("Main should only take one arg `String[]`");
        }

        for (BoundExpression expression : mainMethodDeclaration.getBody().getExpressions()) {
            visit(expression, builder, context, function);
        }
    }

    private LLVMValueRef visit(BoundFunctionDeclarationExpression functionDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        FunctionSymbol functionSymbol = functionDeclarationExpression.getFunctionSymbol();

        //Bind args
        List<BoundFunctionParameterExpression> arguments = functionDeclarationExpression.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            BoundFunctionParameterExpression argument = arguments.get(i);
            LLVMValueRef val = LLVMGetParam(function, i);
            scope.declareVariable(argument.getArgument(), val);
        }

        //Assign return value
        LLVMTypeRef retValType = getLlvmTypeRef(functionSymbol.getType(), context);
        LLVMValueRef retval = LLVMBuildAlloca(builder, retValType, functionSymbol.getName() + "-retval");
        returnStack.push(retval);
        //Create return block
        LLVMBasicBlockRef returnBlock = LLVMAppendBasicBlockInContext(context, function, "return");
        returnBlocks.push(returnBlock);

        //Visit body
        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {

            visit(expression, builder, context, function);
        }

        //Build return value
        TypeSymbol returnType = functionSymbol.getType();
        if (returnType == UNIT) {
            LLVMBasicBlockRef block = LLVMGetInsertBlock(builder);
            if (LLVMGetBasicBlockTerminator(block) == null) {
                LLVMBuildBr(builder, returnBlocks.peek());
            }

            LLVMPositionBuilderAtEnd(builder, returnBlocks.pop());
            return LLVMBuildRet(builder, buildUnit(builder, context));
        }
        LLVMPositionBuilderAtEnd(builder, returnBlocks.pop());
        return LLVMBuildRet(builder, LLVMBuildLoad(builder, returnStack.pop(), functionSymbol.getName() + "-retval"));
    }

    private LLVMValueRef visit(BoundReturnExpression returnExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMValueRef retVal = visit(returnExpression.getReturnValue(), builder, context, function);
        retVal = dereference(builder, retVal, "retVal");
        LLVMBuildStore(builder, retVal, returnStack.peek());
        return LLVMBuildBr(builder, returnBlocks.peek());
    }

    private LLVMValueRef dereference(LLVMBuilderRef builder, LLVMValueRef value, String name) {
        if (LLVMGetTypeKind(LLVMTypeOf(value)) == LLVMPointerTypeKind) {
            value = LLVMBuildLoad(builder, value, name);
        }
        return value;
    }

    private LLVMValueRef ref(LLVMBuilderRef builder, LLVMValueRef val, TypeSymbol type, LLVMContextRef context) {
        if (LLVMGetTypeKind(LLVMTypeOf(val)) != LLVMPointerTypeKind) {
            LLVMValueRef ptr = LLVMBuildAlloca(builder, getLlvmTypeRef(type, context), "access.tmp");
            LLVMBuildStore(builder, val, ptr);
            val = ptr;
        }
        return val;
    }

    private static Stack<LLVMValueRef> returnStack = new Stack<>();
    private static Stack<LLVMBasicBlockRef> returnBlocks = new Stack<>();
}
