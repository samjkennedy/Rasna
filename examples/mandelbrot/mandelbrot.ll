; ModuleID = 'examples/mandelbrot/mandelbrot'
source_filename = "examples/mandelbrot/mandelbrot"

%Complex = type { double, double }
%Color = type { double, double, double }

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@str.1 = private unnamed_addr constant [5 x i8] c"%.*s\00", align 1
@real = private unnamed_addr constant [3 x i8] c"%c\00", align 1

declare i32 @printf(i8*, ...)

declare i16 @snprintf(i8*, i32, i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define %Complex @cMul(%Complex %0, %Complex %1) {
entry:
  %cMul-retval = alloca %Complex, align 8
  %access.tmp = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp, align 8
  %r = getelementptr inbounds %Complex, %Complex* %access.tmp, i32 0, i32 0
  %access.tmp1 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp1, align 8
  %r2 = getelementptr inbounds %Complex, %Complex* %access.tmp1, i32 0, i32 0
  %lhs = load double, double* %r, align 8
  %rhs = load double, double* %r2, align 8
  %smultmp = fmul double %lhs, %rhs
  %access.tmp3 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp3, align 8
  %i = getelementptr inbounds %Complex, %Complex* %access.tmp3, i32 0, i32 1
  %access.tmp4 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp4, align 8
  %i5 = getelementptr inbounds %Complex, %Complex* %access.tmp4, i32 0, i32 1
  %lhs6 = load double, double* %i, align 8
  %rhs7 = load double, double* %i5, align 8
  %smultmp8 = fmul double %lhs6, %rhs7
  %ssubtmp = fsub double %smultmp, %smultmp8
  %realComp = alloca double, align 8
  store double %ssubtmp, double* %realComp, align 8
  %access.tmp9 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp9, align 8
  %r10 = getelementptr inbounds %Complex, %Complex* %access.tmp9, i32 0, i32 0
  %access.tmp11 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp11, align 8
  %i12 = getelementptr inbounds %Complex, %Complex* %access.tmp11, i32 0, i32 1
  %lhs13 = load double, double* %r10, align 8
  %rhs14 = load double, double* %i12, align 8
  %smultmp15 = fmul double %lhs13, %rhs14
  %access.tmp16 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp16, align 8
  %i17 = getelementptr inbounds %Complex, %Complex* %access.tmp16, i32 0, i32 1
  %access.tmp18 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp18, align 8
  %r19 = getelementptr inbounds %Complex, %Complex* %access.tmp18, i32 0, i32 0
  %lhs20 = load double, double* %i17, align 8
  %rhs21 = load double, double* %r19, align 8
  %smultmp22 = fmul double %lhs20, %rhs21
  %saddtmp = fadd double %smultmp15, %smultmp22
  %imagComp = alloca double, align 8
  store double %saddtmp, double* %imagComp, align 8
  %tmp.Complex = alloca %Complex, align 8
  %r23 = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 0
  %2 = load double, double* %realComp, align 8
  store double %2, double* %r23, align 8
  %i24 = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 1
  %3 = load double, double* %imagComp, align 8
  store double %3, double* %i24, align 8
  %tmp.Complex25 = load %Complex, %Complex* %tmp.Complex, align 8
  store %Complex %tmp.Complex25, %Complex* %cMul-retval, align 8
  br label %return

return:                                           ; preds = %entry
  %cMul-retval26 = load %Complex, %Complex* %cMul-retval, align 8
  ret %Complex %cMul-retval26
}

define %Complex @cAdd(%Complex %0, %Complex %1) {
entry:
  %cAdd-retval = alloca %Complex, align 8
  %access.tmp = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp, align 8
  %r = getelementptr inbounds %Complex, %Complex* %access.tmp, i32 0, i32 0
  %access.tmp1 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp1, align 8
  %r2 = getelementptr inbounds %Complex, %Complex* %access.tmp1, i32 0, i32 0
  %lhs = load double, double* %r, align 8
  %rhs = load double, double* %r2, align 8
  %saddtmp = fadd double %lhs, %rhs
  %realComp = alloca double, align 8
  store double %saddtmp, double* %realComp, align 8
  %access.tmp3 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp3, align 8
  %i = getelementptr inbounds %Complex, %Complex* %access.tmp3, i32 0, i32 1
  %access.tmp4 = alloca %Complex, align 8
  store %Complex %1, %Complex* %access.tmp4, align 8
  %i5 = getelementptr inbounds %Complex, %Complex* %access.tmp4, i32 0, i32 1
  %lhs6 = load double, double* %i, align 8
  %rhs7 = load double, double* %i5, align 8
  %saddtmp8 = fadd double %lhs6, %rhs7
  %imagComp = alloca double, align 8
  store double %saddtmp8, double* %imagComp, align 8
  %tmp.Complex = alloca %Complex, align 8
  %r9 = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 0
  %2 = load double, double* %realComp, align 8
  store double %2, double* %r9, align 8
  %i10 = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 1
  %3 = load double, double* %imagComp, align 8
  store double %3, double* %i10, align 8
  %tmp.Complex11 = load %Complex, %Complex* %tmp.Complex, align 8
  store %Complex %tmp.Complex11, %Complex* %cAdd-retval, align 8
  br label %return

return:                                           ; preds = %entry
  %cAdd-retval12 = load %Complex, %Complex* %cAdd-retval, align 8
  ret %Complex %cAdd-retval12
}

define i32 @mandelbrot(%Complex %0) {
entry:
  %mandelbrot-retval = alloca i32, align 4
  %ITERS = alloca i32, align 4
  store i32 50, i32* %ITERS, align 4
  %tmp.Complex = alloca %Complex, align 8
  %r = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 0
  store double 0.000000e+00, double* %r, align 8
  %i = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 1
  store double 0.000000e+00, double* %i, align 8
  %tmp.Complex1 = load %Complex, %Complex* %tmp.Complex, align 8
  %z = alloca %Complex, align 8
  store %Complex %tmp.Complex1, %Complex* %z, align 8
  %i2 = alloca i32, align 4
  store i32 0, i32* %i2, align 4
  br label %for.cond

return:                                           ; preds = %for.exit, %if.then
  %mandelbrot-retval16 = load i32, i32* %mandelbrot-retval, align 4
  ret i32 %mandelbrot-retval16

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load i32, i32* %i2, align 4
  %rhs = load i32, i32* %ITERS, align 4
  %1 = icmp slt i32 %lhs, %rhs
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %arg = load %Complex, %Complex* %z, align 8
  %arg3 = load %Complex, %Complex* %z, align 8
  %cMul = call %Complex @cMul(%Complex %arg, %Complex %arg3)
  %cAdd = call %Complex @cAdd(%Complex %cMul, %Complex %0)
  store %Complex %cAdd, %Complex* %z, align 8
  %r4 = getelementptr inbounds %Complex, %Complex* %z, i32 0, i32 0
  %r5 = getelementptr inbounds %Complex, %Complex* %z, i32 0, i32 0
  %lhs6 = load double, double* %r4, align 8
  %rhs7 = load double, double* %r5, align 8
  %smultmp = fmul double %lhs6, %rhs7
  %i8 = getelementptr inbounds %Complex, %Complex* %z, i32 0, i32 1
  %i9 = getelementptr inbounds %Complex, %Complex* %z, i32 0, i32 1
  %lhs10 = load double, double* %i8, align 8
  %rhs11 = load double, double* %i9, align 8
  %smultmp12 = fmul double %lhs10, %rhs11
  %saddtmp = fadd double %smultmp, %smultmp12
  %2 = fcmp ogt double %saddtmp, 4.000000e+00
  br i1 %2, label %if.then, label %if.end

for.incr:                                         ; preds = %if.end
  %lhs13 = load i32, i32* %i2, align 4
  %saddtmp14 = add i32 %lhs13, 1
  store i32 %saddtmp14, i32* %i2, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  %retVal15 = load i32, i32* %ITERS, align 4
  store i32 %retVal15, i32* %mandelbrot-retval, align 4
  br label %return

if.then:                                          ; preds = %for.body
  %retVal = load i32, i32* %i2, align 4
  store i32 %retVal, i32* %mandelbrot-retval, align 4
  br label %return

if.end:                                           ; preds = %for.body
  br label %for.incr
}

define double @sin(double %0) {
entry:
  %sin-retval = alloca double, align 8
  %_PI = alloca double, align 8
  store double 0x400921FB54442EEA, double* %_PI, align 8
  %precision = alloca i32, align 4
  store i32 60, i32* %precision, align 4
  %sign = alloca i32, align 4
  store i32 -1, i32* %sign, align 4
  %rhs = load double, double* %_PI, align 8
  %smultmp = fmul double 2.000000e+00, %rhs
  %sremtmp = frem double %0, %smultmp
  %x1 = alloca double, align 8
  store double %sremtmp, double* %x1, align 8
  %term = alloca double, align 8
  store double 1.000000e+00, double* %term, align 8
  %sum = alloca double, align 8
  store double 0.000000e+00, double* %sum, align 8
  %iter = alloca i32, align 4
  store i32 1, i32* %iter, align 4
  br label %for.cond

return:                                           ; preds = %for.exit
  %sin-retval17 = load double, double* %sin-retval, align 8
  ret double %sin-retval17

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load i32, i32* %iter, align 4
  %rhs1 = load i32, i32* %precision, align 4
  %1 = icmp slt i32 %lhs, %rhs1
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %2 = load i32, i32* %iter, align 4
  %3 = sitofp i32 %2 to double
  %lhs2 = load double, double* %x1, align 8
  %sdivtmp = fdiv double %lhs2, %3
  %lhs3 = load double, double* %term, align 8
  %smultmp4 = fmul double %lhs3, %sdivtmp
  store double %smultmp4, double* %term, align 8
  %lhs5 = load i32, i32* %iter, align 4
  %sremtmp6 = srem i32 %lhs5, 4
  %4 = icmp eq i32 %sremtmp6, 1
  br i1 %4, label %if.then, label %if.end

for.incr:                                         ; preds = %if.end10
  %lhs15 = load i32, i32* %iter, align 4
  %saddtmp16 = add i32 %lhs15, 1
  store i32 %saddtmp16, i32* %iter, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  %retVal = load double, double* %sum, align 8
  store double %retVal, double* %sin-retval, align 8
  br label %return

if.then:                                          ; preds = %for.body
  %lhs7 = load double, double* %sum, align 8
  %rhs8 = load double, double* %term, align 8
  %saddtmp = fadd double %lhs7, %rhs8
  store double %saddtmp, double* %sum, align 8
  br label %if.end

if.end:                                           ; preds = %if.then, %for.body
  %lhs11 = load i32, i32* %iter, align 4
  %sremtmp12 = srem i32 %lhs11, 4
  %5 = icmp eq i32 %sremtmp12, 3
  br i1 %5, label %if.then9, label %if.end10

if.then9:                                         ; preds = %if.end
  %lhs13 = load double, double* %sum, align 8
  %rhs14 = load double, double* %term, align 8
  %ssubtmp = fsub double %lhs13, %rhs14
  store double %ssubtmp, double* %sum, align 8
  br label %if.end10

if.end10:                                         ; preds = %if.then9, %if.end
  br label %for.incr
}

define double @cos(double %0) {
entry:
  %cos-retval = alloca double, align 8
  %_PI = alloca double, align 8
  store double 0x400921FB54442EEA, double* %_PI, align 8
  %rhs = load double, double* %_PI, align 8
  %saddtmp = fadd double %0, %rhs
  %sin = call double @sin(double %saddtmp)
  store double %sin, double* %cos-retval, align 8
  br label %return

return:                                           ; preds = %entry
  %cos-retval1 = load double, double* %cos-retval, align 8
  ret double %cos-retval1
}

define %Color @toColor(i32 %0) {
entry:
  %toColor-retval = alloca %Color, align 8
  %1 = icmp sge i32 %0, 50
  br i1 %1, label %if.then, label %if.end

return:                                           ; preds = %if.end, %if.then
  %toColor-retval19 = load %Color, %Color* %toColor-retval, align 8
  ret %Color %toColor-retval19

if.then:                                          ; preds = %entry
  %tmp.Color = alloca %Color, align 8
  %r = getelementptr inbounds %Color, %Color* %tmp.Color, i32 0, i32 0
  store double 0.000000e+00, double* %r, align 8
  %g = getelementptr inbounds %Color, %Color* %tmp.Color, i32 0, i32 1
  store double 0.000000e+00, double* %g, align 8
  %b = getelementptr inbounds %Color, %Color* %tmp.Color, i32 0, i32 2
  store double 0.000000e+00, double* %b, align 8
  %tmp.Color1 = load %Color, %Color* %tmp.Color, align 8
  store %Color %tmp.Color1, %Color* %toColor-retval, align 8
  br label %return

if.end:                                           ; preds = %entry
  %2 = sitofp i32 %0 to double
  %smultmp = fmul double %2, 2.000000e-01
  %a = alloca double, align 8
  store double %smultmp, double* %a, align 8
  %arg = load double, double* %a, align 8
  %sin = call double @sin(double %arg)
  %saddtmp = fadd double %sin, 1.000000e+00
  %sdivtmp = fdiv double %saddtmp, 2.000000e+00
  %r2 = alloca double, align 8
  store double %sdivtmp, double* %r2, align 8
  %lhs = load double, double* %a, align 8
  %saddtmp3 = fadd double %lhs, 2.100000e+00
  %sin4 = call double @sin(double %saddtmp3)
  %saddtmp5 = fadd double %sin4, 1.000000e+00
  %sdivtmp6 = fdiv double %saddtmp5, 2.000000e+00
  %g7 = alloca double, align 8
  store double %sdivtmp6, double* %g7, align 8
  %lhs8 = load double, double* %a, align 8
  %saddtmp9 = fadd double %lhs8, 4.200000e+00
  %sin10 = call double @sin(double %saddtmp9)
  %saddtmp11 = fadd double %sin10, 1.000000e+00
  %sdivtmp12 = fdiv double %saddtmp11, 2.000000e+00
  %b13 = alloca double, align 8
  store double %sdivtmp12, double* %b13, align 8
  %tmp.Color14 = alloca %Color, align 8
  %r15 = getelementptr inbounds %Color, %Color* %tmp.Color14, i32 0, i32 0
  %3 = load double, double* %r2, align 8
  store double %3, double* %r15, align 8
  %g16 = getelementptr inbounds %Color, %Color* %tmp.Color14, i32 0, i32 1
  %4 = load double, double* %g7, align 8
  store double %4, double* %g16, align 8
  %b17 = getelementptr inbounds %Color, %Color* %tmp.Color14, i32 0, i32 2
  %5 = load double, double* %b13, align 8
  store double %5, double* %b17, align 8
  %tmp.Color18 = load %Color, %Color* %tmp.Color14, align 8
  store %Color %tmp.Color18, %Color* %toColor-retval, align 8
  br label %return
}

define i8 @toAscii(i32 %0) {
entry:
  %toAscii-retval = alloca i8, align 1
  %1 = icmp sge i32 %0, 50
  br i1 %1, label %if.then, label %if.end

return:                                           ; preds = %if.end4, %if.then3, %if.then1, %if.then
  %toAscii-retval5 = load i8, i8* %toAscii-retval, align 1
  ret i8 %toAscii-retval5

if.then:                                          ; preds = %entry
  store i8 64, i8* %toAscii-retval, align 1
  br label %return

if.end:                                           ; preds = %entry
  %2 = icmp sge i32 %0, 25
  br i1 %2, label %if.then1, label %if.end2

if.then1:                                         ; preds = %if.end
  store i8 43, i8* %toAscii-retval, align 1
  br label %return

if.end2:                                          ; preds = %if.end
  %3 = icmp sge i32 %0, 7
  br i1 %3, label %if.then3, label %if.end4

if.then3:                                         ; preds = %if.end2
  store i8 46, i8* %toAscii-retval, align 1
  br label %return

if.end4:                                          ; preds = %if.end2
  store i8 32, i8* %toAscii-retval, align 1
  br label %return
}

define void @printCol(%Color %0, double %1) {
entry:
  %access.tmp = alloca %Color, align 8
  store %Color %0, %Color* %access.tmp, align 8
  %r = getelementptr inbounds %Color, %Color* %access.tmp, i32 0, i32 0
  %lhs = load double, double* %r, align 8
  %smultmp = fmul double %lhs, %1
  %2 = fptosi double %smultmp to i32
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %2)
  %access.tmp1 = alloca %Color, align 8
  store %Color %0, %Color* %access.tmp1, align 8
  %g = getelementptr inbounds %Color, %Color* %access.tmp1, i32 0, i32 1
  %lhs2 = load double, double* %g, align 8
  %smultmp3 = fmul double %lhs2, %1
  %3 = fptosi double %smultmp3 to i32
  %printcall4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %3)
  %access.tmp5 = alloca %Color, align 8
  store %Color %0, %Color* %access.tmp5, align 8
  %b = getelementptr inbounds %Color, %Color* %access.tmp5, i32 0, i32 2
  %lhs6 = load double, double* %b, align 8
  %smultmp7 = fmul double %lhs6, %1
  %4 = fptosi double %smultmp7 to i32
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %4)
  ret void
}

define i32 @main() {
entry:
  %SCALE = alloca double, align 8
  store double 2.000000e+01, double* %SCALE, align 8
  %.compoundliteral = alloca [3 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [3 x i8], [3 x i8]* %.compoundliteral, i64 0, i64 0
  store i8 80, i8* %arrayinit.begin, align 1
  %arrayinit.element = getelementptr inbounds i8, i8* %arrayinit.begin, i64 1
  store i8 51, i8* %arrayinit.element, align 1
  %arrayinit.element1 = getelementptr inbounds i8, i8* %arrayinit.element, i64 1
  store i8 0, i8* %arrayinit.element1, align 1
  %arraydecay = getelementptr inbounds [3 x i8], [3 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 2, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %FORMAT = alloca { i32, i8* }, align 8
  %val = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %val, { i32, i8* }* %FORMAT, align 8
  %rhs = load double, double* %SCALE, align 8
  %smultmp = fmul double 1.600000e+01, %rhs
  %WIDTH = alloca double, align 8
  store double %smultmp, double* %WIDTH, align 8
  %rhs2 = load double, double* %SCALE, align 8
  %smultmp3 = fmul double 8.000000e+00, %rhs2
  %HEIGHT = alloca double, align 8
  store double %smultmp3, double* %HEIGHT, align 8
  %MAX_VAL = alloca double, align 8
  store double 2.550000e+02, double* %MAX_VAL, align 8
  %size4 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %FORMAT, i32 0, i32 0
  %0 = load i32, i32* %size4, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %FORMAT, i32 0, i32 1
  %1 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @str.1, i32 0, i32 0), i32 %0, i8* %1)
  %printcall5 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @real, i32 0, i32 0), i8 10)
  %2 = load double, double* %WIDTH, align 8
  %3 = fptosi double %2 to i32
  %printcall6 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %3)
  %4 = load double, double* %HEIGHT, align 8
  %5 = fptosi double %4 to i32
  %printcall7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %5)
  %6 = load double, double* %MAX_VAL, align 8
  %7 = fptosi double %6 to i32
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %7)
  %rhs9 = load double, double* %HEIGHT, align 8
  %sdivtmp = fdiv double 2.000000e+00, %rhs9
  %yStep = alloca double, align 8
  store double %sdivtmp, double* %yStep, align 8
  %rhs10 = load double, double* %WIDTH, align 8
  %sdivtmp11 = fdiv double 2.500000e+00, %rhs10
  %xStep = alloca double, align 8
  store double %sdivtmp11, double* %xStep, align 8
  %y = alloca double, align 8
  store double -1.000000e+00, double* %y, align 8
  br label %for.cond

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load double, double* %y, align 8
  %8 = fcmp olt double %lhs, 1.050000e+00
  br i1 %8, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %x = alloca double, align 8
  store double -2.000000e+00, double* %x, align 8
  br label %for.cond12

for.incr:                                         ; preds = %for.exit15
  %lhs20 = load double, double* %y, align 8
  %rhs21 = load double, double* %yStep, align 8
  %saddtmp22 = fadd double %lhs20, %rhs21
  store double %saddtmp22, double* %y, align 8
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  ret i32 0

for.cond12:                                       ; preds = %for.incr14, %for.body
  %lhs16 = load double, double* %x, align 8
  %9 = fcmp olt double %lhs16, 5.000000e-01
  br i1 %9, label %for.body13, label %for.exit15

for.body13:                                       ; preds = %for.cond12
  %tmp.Complex = alloca %Complex, align 8
  %r = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 0
  %10 = load double, double* %x, align 8
  store double %10, double* %r, align 8
  %i = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 1
  %11 = load double, double* %y, align 8
  store double %11, double* %i, align 8
  %tmp.Complex17 = load %Complex, %Complex* %tmp.Complex, align 8
  %mandelbrot = call i32 @mandelbrot(%Complex %tmp.Complex17)
  %toColor = call %Color @toColor(i32 %mandelbrot)
  call void @printCol(%Color %toColor, double 2.550000e+02)
  br label %for.incr14

for.incr14:                                       ; preds = %for.body13
  %lhs18 = load double, double* %x, align 8
  %rhs19 = load double, double* %xStep, align 8
  %saddtmp = fadd double %lhs18, %rhs19
  store double %saddtmp, double* %x, align 8
  br label %for.cond12

for.exit15:                                       ; preds = %for.cond12
  br label %for.incr
}
