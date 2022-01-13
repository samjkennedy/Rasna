package com.skennedy.rasna.compilation;

import com.skennedy.rasna.Rasna;
import com.skennedy.rasna.typebinding.BoundProgram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.llvm.global.LLVM.LLVMAbortProcessAction;
import static org.bytedeco.llvm.global.LLVM.LLVMAddAggressiveInstCombinerPass;
import static org.bytedeco.llvm.global.LLVM.LLVMAddCFGSimplificationPass;
import static org.bytedeco.llvm.global.LLVM.LLVMAddFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMAddIncoming;
import static org.bytedeco.llvm.global.LLVM.LLVMAddNewGVNPass;
import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAdd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildBr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCall;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCondBr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildICmp;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMul;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildPhi;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildRet;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildSub;
import static org.bytedeco.llvm.global.LLVM.LLVMCCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMCodeModelDefault;
import static org.bytedeco.llvm.global.LLVM.LLVMConstInt;
import static org.bytedeco.llvm.global.LLVM.LLVMContextCreate;
import static org.bytedeco.llvm.global.LLVM.LLVMContextDispose;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateBuilderInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateGenericValueOfInt;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateMCJITCompilerForModule;
import static org.bytedeco.llvm.global.LLVM.LLVMCreatePassManager;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateTargetMachine;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeBuilder;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeModule;
import static org.bytedeco.llvm.global.LLVM.LLVMDumpModule;
import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;
import static org.bytedeco.llvm.global.LLVM.LLVMGenericValueToInt;
import static org.bytedeco.llvm.global.LLVM.LLVMGetDefaultTargetTriple;
import static org.bytedeco.llvm.global.LLVM.LLVMGetGlobalPassRegistry;
import static org.bytedeco.llvm.global.LLVM.LLVMGetParam;
import static org.bytedeco.llvm.global.LLVM.LLVMGetTargetFromTriple;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeCore;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.llvm.global.LLVM.LLVMInt32TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMIntEQ;
import static org.bytedeco.llvm.global.LLVM.LLVMLinkInMCJIT;
import static org.bytedeco.llvm.global.LLVM.LLVMModuleCreateWithNameInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMObjectFile;
import static org.bytedeco.llvm.global.LLVM.LLVMPositionBuilderAtEnd;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintMessageAction;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;
import static org.bytedeco.llvm.global.LLVM.LLVMRelocDefault;
import static org.bytedeco.llvm.global.LLVM.LLVMRunFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMRunPassManager;
import static org.bytedeco.llvm.global.LLVM.LLVMSetFunctionCallConv;
import static org.bytedeco.llvm.global.LLVM.LLVMTargetMachineEmitToFile;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMVerifyModule;
import static org.bytedeco.llvm.global.LLVM.LLVMVoidType;
import static org.bytedeco.llvm.global.LLVM.LLVMWriteBitcodeToFile;

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
        char c = (char) inputStream.read();
        System.out.print(Rasna.ConsoleColors.CYAN_BOLD);
        while (c != '\uFFFF') {
            System.out.print(c);
            c = (char) inputStream.read();
        }
        System.out.print(Rasna.ConsoleColors.RED_BOLD);
        InputStream errorStream = process.getErrorStream();
        c = (char) errorStream.read();
        while (c != '\uFFFF') {
            System.out.print(c);
            c = (char) errorStream.read();
        }
        System.out.print(Rasna.ConsoleColors.RESET);
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
