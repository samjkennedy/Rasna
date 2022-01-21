package com.skennedy.rasna.compilation.llvm;

import com.skennedy.rasna.compilation.Compiler;
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

import static com.skennedy.rasna.typebinding.TypeSymbol.BOOL;
import static com.skennedy.rasna.typebinding.TypeSymbol.CHAR;
import static com.skennedy.rasna.typebinding.TypeSymbol.INT;
import static com.skennedy.rasna.typebinding.TypeSymbol.REAL;
import static com.skennedy.rasna.typebinding.TypeSymbol.STRING;
import static com.skennedy.rasna.typebinding.TypeSymbol.VOID;
import static org.bytedeco.llvm.global.LLVM.LLVMAddFunction;
import static org.bytedeco.llvm.global.LLVM.LLVMAppendBasicBlockInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAdd;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAlloca;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildAnd;
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
import static org.bytedeco.llvm.global.LLVM.LLVMBuildGlobalStringPtr;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildICmp;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildLoad;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMul;
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
import static org.bytedeco.llvm.global.LLVM.LLVMDeleteBasicBlock;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeBuilder;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;
import static org.bytedeco.llvm.global.LLVM.LLVMDisposeModule;
import static org.bytedeco.llvm.global.LLVM.LLVMDoubleTypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMDumpModule;
import static org.bytedeco.llvm.global.LLVM.LLVMFunctionType;
import static org.bytedeco.llvm.global.LLVM.LLVMGetBasicBlockTerminator;
import static org.bytedeco.llvm.global.LLVM.LLVMGetGlobalPassRegistry;
import static org.bytedeco.llvm.global.LLVM.LLVMGetParam;
import static org.bytedeco.llvm.global.LLVM.LLVMGetTypeKind;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeCore;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmParser;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeAsmPrinter;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;
import static org.bytedeco.llvm.global.LLVM.LLVMInt128TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt1TypeInContext;
import static org.bytedeco.llvm.global.LLVM.LLVMInt32TypeInContext;
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

public class LLVMCompiler implements Compiler {

    private static final Logger log = LogManager.getLogger(LLVMCompiler.class);

    public static final BytePointer error = new BytePointer();

    private LLVMTypeRef i1Type;
    private LLVMTypeRef i8Type;
    private LLVMTypeRef i32Type;
    private LLVMTypeRef realType;
    private LLVMValueRef printf;
    private LLVMValueRef printB; //for printing bools nicely
    private LLVMValueRef formatStr; //"%d\n"

    private Scope scope;

    @Override
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
        realType = LLVMDoubleTypeInContext(context);

        //Declare printf function and string formatter once
        printf = LLVMAddFunction(module, "printf", LLVMFunctionType(i32Type, LLVMPointerType(LLVMInt8TypeInContext(context), 0), 1, 1));//No idea what AddressSpace is for yet

        buildPrintbMethod(context, module, builder);

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

                    visitMainMethod((BoundFunctionDeclarationExpression) expression, builder, context, main);

                    LLVMValueRef returnCode = LLVMConstInt(i32Type, 0, 0);
                    LLVMBuildRet(builder, returnCode);

                    if (LLVMVerifyFunction(main, LLVMPrintMessageAction) != 0) {
                        log.error("Error when validating main function:");
                        LLVMDumpModule(module);
                        System.exit(1);
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
                        System.exit(1);
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
            System.exit(1);
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

    private void buildPrintbMethod(LLVMContextRef context, LLVMModuleRef module, LLVMBuilderRef builder) {
        printB = LLVMAddFunction(module, "printb", LLVMFunctionType(LLVMVoidTypeInContext(context), i1Type, 1, 0));
        LLVMSetFunctionCallConv(printB, LLVMCCallConv);
        LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context, printB, "entry");
        LLVMPositionBuilderAtEnd(builder, entry);

        LLVMValueRef x = LLVMGetParam(printB, 0);

        //printf("%s\n, x ? "true" : "false");)

        //TODO: I think the strings need to be char[]*
        //LLVMValueRef cond = LLVMBuildSelect(builder, x, LLVMConstStringInContext(context, "true", 4, 0) , LLVMConstStringInContext(context, "false", 5, 0), "cond");
        LLVMValueRef cond = LLVMBuildSelect(builder, x, LLVMConstInt(i32Type, 1, 0), LLVMConstInt(i32Type, 0, 0), "cond");

        PointerPointer<Pointer> printArgs = new PointerPointer<>(2)
                .put(0, LLVMBuildGlobalStringPtr(builder, "%d\n", "str"))
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
            default:
                throw new UnsupportedOperationException("Compilation for `" + expression.getBoundExpressionType() + "` is not yet implemented in LLVM");
        }
    }

    private LLVMValueRef visit(BoundMemberAccessorExpression memberAccessorExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

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

        BoundLiteralExpression index = tupleIndexExpression.getIndex();

        return LLVMBuildStructGEP(builder, tuple, (int) index.getValue(), index.getValue().toString());
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

        PointerPointer<Pointer> elementTypes = new PointerPointer<>(structDeclarationExpression.getMembers().size());

        List<LLVMTypeRef> memberTypes = structDeclarationExpression.getMembers().stream()
                .map(BoundExpression::getType)
                .map(type -> getLlvmTypeRef(type, context))
                .collect(Collectors.toList());
        for (int i = 0; i < memberTypes.size(); i++) {
            elementTypes.put(i, memberTypes.get(i));
        }

        LLVMTypeRef structTypeRef = LLVMStructCreateNamed(context, structDeclarationExpression.getType().getName());
        LLVMStructSetBody(structTypeRef, elementTypes, memberTypes.size(), 0);

        scope.declareType(structDeclarationExpression.getType(), structTypeRef);

        return null;
    }

    private LLVMValueRef visit(BoundStructLiteralExpression structLiteralExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        //Allocate a tmp variable for this... not the best but what we have to do
        LLVMTypeRef type = getLlvmTypeRef(structLiteralExpression.getType(), context);
        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, "tmp." + structLiteralExpression.getType().getName());

        Collection<VariableSymbol> members = structLiteralExpression.getType().getFields().values();
        int idx = 0;
        for (VariableSymbol member : members) {
            LLVMValueRef elementRef = LLVMBuildStructGEP(builder, ptr, idx, member.getName());
            LLVMValueRef valueRef = visit(structLiteralExpression.getElements().get(idx), builder, context, function);
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

        if (functionSymbol.getType() == VOID) {
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

    //TODO: You cannot mutate a function argument
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

        LLVMBasicBlockRef thenBlock = LLVMAppendBasicBlockInContext(context, function, "if.then");
        LLVMBasicBlockRef elseBlock = LLVMAppendBasicBlockInContext(context, function, "if.else");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlockInContext(context, function, "if.end");

        LLVMValueRef condition = visit(ifExpression.getCondition(), builder, context, function);
        condition = dereference(builder, condition, "");
        LLVMBuildCondBr(builder, condition, thenBlock, elseBlock);

        LLVMPositionBuilderAtEnd(builder, thenBlock);
        visit(ifExpression.getBody(), builder, context, function);

        boolean thenTerminated = LLVMGetBasicBlockTerminator(thenBlock) != null;
        if (!thenTerminated) {
            LLVMBuildBr(builder, endBlock);
        }

        LLVMPositionBuilderAtEnd(builder, elseBlock);
        visit(ifExpression.getElseBody(), builder, context, function);

        boolean elseTerminated = LLVMGetBasicBlockTerminator(elseBlock) != null;
        if (!elseTerminated) {
            LLVMBuildBr(builder, endBlock);
        }

        if (elseTerminated && thenTerminated) {
            LLVMDeleteBasicBlock(endBlock);
        } else {
            LLVMPositionBuilderAtEnd(builder, endBlock);
        }
        return null;
    }

    private LLVMValueRef visit(BoundVariableDeclarationExpression variableDeclarationExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMTypeRef type = getLlvmTypeRef(variableDeclarationExpression.getType(), context);

        LLVMValueRef ptr = LLVMBuildAlloca(builder, type, variableDeclarationExpression.getVariable().getName());

        LLVMValueRef val = visit(variableDeclarationExpression.getInitialiser(), builder, context, function);
        val = dereference(builder, val, "val");
        scope.declarePointer(variableDeclarationExpression.getVariable(), ptr);

        return LLVMBuildStore(builder, val, ptr);
    }

    private LLVMTypeRef getLlvmTypeRef(TypeSymbol typeSymbol, LLVMContextRef context) {
        if (typeSymbol == VOID) {
            return LLVMVoidTypeInContext(context);
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

    private LLVMValueRef visit(BoundBinaryExpression binaryExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        LLVMValueRef lhs = visit(binaryExpression.getLeft(), builder, context, function);
        lhs = dereference(builder, lhs, "lhs");
        LLVMValueRef rhs = visit(binaryExpression.getRight(), builder, context, function);
        rhs = dereference(builder, rhs, "rhs");

        if (binaryExpression.getLeft().getType() == INT && binaryExpression.getRight().getType() == INT) {
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
        throw new UnsupportedOperationException("Compilation for binary operation `" + binaryExpression.getOperator().getBoundOpType() + "` is not yet supported for LLVM for types `" + binaryExpression.getLeft().getType() + "` and `" + binaryExpression.getRight().getType() + "`");
    }

    private LLVMValueRef visitRealBinop(LLVMBuilderRef builder, LLVMValueRef lhs, BoundBinaryOperator.BoundBinaryOperation op, LLVMValueRef rhs) {
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

        if (literalExpression.getType() == TypeSymbol.BOOL) {
            return LLVMConstInt(i1Type, (boolean) literalExpression.getValue() ? 1 : 0, 0);
        }
        if (literalExpression.getType() == CHAR) {
            return LLVMConstInt(i8Type, (char) literalExpression.getValue(), 0);
        }
        if (literalExpression.getType() == INT) {
            return LLVMConstInt(i32Type, (int) literalExpression.getValue(), 0);
        }
        if (literalExpression.getType() == TypeSymbol.REAL) {
            return LLVMConstReal(realType, (double) literalExpression.getValue());
        }
        if (literalExpression.getType() == TypeSymbol.STRING) {
            String value = (String) literalExpression.getValue();
            return LLVMBuildGlobalStringPtr(builder, value, value); //TODO: This needs to be a pointer but not global
        }
        throw new UnsupportedOperationException("Literals of type `" + literalExpression.getType() + "` are not yet supported in LLVM");
    }

    private LLVMValueRef visit(BoundPrintExpression printExpression, LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {

        if (formatStr == null) {
            formatStr = LLVMBuildGlobalStringPtr(builder, "%d\n", "formatStr");
        }

        LLVMValueRef res = visit(printExpression.getExpression(), builder, context, function);
        if (printExpression.getExpression().getType() != STRING && LLVMGetTypeKind(LLVMTypeOf(res)) == LLVMPointerTypeKind) {
            res = LLVMBuildLoad(builder, res, "print");
        }

        PointerPointer<Pointer> printArgs;
        if (printExpression.getExpression().getType() == STRING) {
            printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%s\n", "str"))
                    .put(1, res);
        } else if (printExpression.getExpression().getType() == CHAR) {
            printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%c\n", "real"))
                    .put(1, res);
        } else if (printExpression.getExpression().getType() == REAL) {
            printArgs = new PointerPointer<>(2)
                    .put(0, LLVMBuildGlobalStringPtr(builder, "%f\n", "real"))
                    .put(1, res);
        } else if (printExpression.getExpression().getType() == BOOL) {
            printArgs = new PointerPointer<>(1)
                    .put(0, res);
            return LLVMBuildCall(builder, printB, printArgs, 1, "");
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
            throw new UnsupportedOperationException("Main method args are not yet implemented in LLVM");
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

        if (functionSymbol.getType() != VOID) {
            //Assign return value
            LLVMTypeRef retValType = getLlvmTypeRef(functionSymbol.getType(), context);
            LLVMValueRef retval = LLVMBuildAlloca(builder, retValType, functionSymbol.getName() + "-retval");
            returnStack.push(retval);
            //Create return block
            LLVMBasicBlockRef returnBlock = LLVMAppendBasicBlockInContext(context, function, "return");
            returnBlocks.push(returnBlock);
        }

        //Visit body
        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {

            visit(expression, builder, context, function);
        }

        //Build return value
        TypeSymbol returnType = functionSymbol.getType();
        if (returnType == VOID) {
            return LLVMBuildRetVoid(builder);
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
