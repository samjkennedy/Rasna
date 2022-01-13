package com.skennedy.rasna.compilation;

import com.skennedy.rasna.Rasna;
import com.skennedy.rasna.typebinding.BoundProgram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.llvm.global.LLVM.LLVMAbortProcessAction;
import static org.bytedeco.llvm.global.LLVM.LLVMAddFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildRet;
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
import static org.bytedeco.llvm.global.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.llvm.global.LLVM.LLVMModuleCreateWithNameInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;
import static org.bytedeco.llvm.global.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyModule;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;

public class LLVMCompiler implements Compiler {

    private static final Logger log = LogManager.getLogger(LLVMCompiler.class);

    public static final BytePointer error = new BytePointer();

    @Override
    public void compile(BoundProgram program, String outputFileName) throws IOException {

        // Stage 1: Initialize LLVM components
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext(outputFileName, context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
        LLVMTypeRef i32Type = LLVMInt32TypeInContext(context);
        LLVMTypeRef mainType = LLVMFunctionType(i32Type, LLVMVoidType(), /* argumentCount */ 0, /* isVariadic */ 0);

        LLVMValueRef main = LLVMAddFunction(module, "main", mainType);
        LLVMSetFunctionCallConv(main, LLVMCCallConv);

        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, main, "entry");

        LLVMPositionBuilderAtEnd(builder, entry);

        LLVMValueRef returnCode = LLVMConstInt(i32Type, 0, 0);
        LLVMBuildRet(builder, returnCode);

        LLVMVerifyFunction(main, LLVMAbortProcessAction);

        //LLVMDumpModule(module);
        if (LLVMVerifyModule(module, LLVMAbortProcessAction, error) != 0) {
            log.error("Failed to validate module: " + error.getString());
            return;
        }

        //clang test.ll -o test.exe
        BytePointer llFile = new BytePointer("./" + outputFileName + ".ll");
        if (LLVMPrintModuleToFile(module, llFile, error) != 0) {
            log.error("Failed to write module to file");
            LLVMDisposeMessage(error);
            return;
        }
        log.info("Wrote IR to " + outputFileName + ".ll");
        Process process = Runtime.getRuntime().exec("C:\\Program Files\\LLVM\\bin\\clang test.ll -o test.exe");
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
            log.error(stderr.toString());
        }
        log.info("Compiled IR to " + outputFileName + ".exe");

        // Stage 3: Dump the module to file
//        if (LLVMWriteBitcodeToFile(module, "./" + outputFileName + ".bc") != 0) {
//            log.error("Failed to write bitcode to file");
//            return;
//        }
//
//        // Stage 4: Create the relocatable object file
//        BytePointer triple = LLVMGetDefaultTargetTriple();
//        LLVMTargetRef target = new LLVMTargetRef();
//
//        if (LLVMGetTargetFromTriple(triple, target, error) != 0) {
//            log.error("Failed to get target from triple: " + error.getString());
//            LLVMDisposeMessage(error);
//            return;
//        }
//
//        String cpu = "generic";
//        String cpuFeatures = "";
//        int optimizationLevel = 0;
//        LLVMTargetMachineRef tm = LLVMCreateTargetMachine(
//                target, triple.getString(), cpu, cpuFeatures, optimizationLevel,
//                LLVMRelocDefault, LLVMCodeModelDefault
//        );
//
//        BytePointer outputFile = new BytePointer("./" + outputFileName + ".o");
//        if (LLVMTargetMachineEmitToFile(tm, module, outputFile, LLVMObjectFile, error) != 0) {
//            log.error("Failed to emit relocatable object file: " + error.getString());
//            LLVMDisposeMessage(error);
//            return;
//        }

        // Stage 5: Dispose of allocated resources
        LLVMDisposeModule(module);
        LLVMDisposeBuilder(builder);
        LLVMContextDispose(context);
//
//        // Stage 6: Convert .o file to .exe
//        Runtime.getRuntime().exec("gcc " + outputFileName + ".o -o " + outputFileName + ".exe");
    }
}
