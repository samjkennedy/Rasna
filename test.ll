; ModuleID = 'test'
source_filename = "test"

%Complex = type { double, double }

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@0 = private unnamed_addr constant [2 x i8] c"*\00", align 1
@str.1 = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@1 = private unnamed_addr constant [2 x i8] c" \00", align 1
@str.2 = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@2 = private unnamed_addr constant [2 x i8] c"\0A\00", align 1
@str.3 = private unnamed_addr constant [3 x i8] c"%s\00", align 1

declare i32 @printf(i8*, ...)

declare i16 @snprintf(i8*, i32, i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define double @sqrt(double %0) {
entry:
  %sqrt-retval = alloca double, align 8
  %iterations = alloca i32, align 4
  store i32 100, i32* %iterations, align 4
  %sdivtmp = fdiv double %0, 1.000000e+01
  %x0 = alloca double, align 8
  store double %sdivtmp, double* %x0, align 8
  %rhs = load double, double* %x0, align 8
  %sdivtmp1 = fdiv double %0, %rhs
  %lhs = load double, double* %x0, align 8
  %saddtmp = fadd double %lhs, %sdivtmp1
  %sdivtmp2 = fdiv double %saddtmp, 2.000000e+00
  %x1 = alloca double, align 8
  store double %sdivtmp2, double* %x1, align 8
  %i = alloca i32, align 4
  store i32 1, i32* %i, align 4
  br label %for.cond

return:                                           ; preds = %for.exit
  %sqrt-retval13 = load double, double* %sqrt-retval, align 8
  ret double %sqrt-retval13

for.cond:                                         ; preds = %for.incr, %entry
  %lhs3 = load i32, i32* %i, align 4
  %rhs4 = load i32, i32* %iterations, align 4
  %1 = icmp slt i32 %lhs3, %rhs4
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %tmp = alloca double, align 8
  %val = load double, double* %x0, align 8
  store double %val, double* %tmp, align 8
  %rhs5 = load double, double* %x1, align 8
  %sdivtmp6 = fdiv double %0, %rhs5
  %lhs7 = load double, double* %x1, align 8
  %saddtmp8 = fadd double %lhs7, %sdivtmp6
  %sdivtmp9 = fdiv double %saddtmp8, 2.000000e+00
  store double %sdivtmp9, double* %x0, align 8
  %val10 = load double, double* %tmp, align 8
  store double %val10, double* %x1, align 8
  br label %for.incr

for.incr:                                         ; preds = %for.body
  %lhs11 = load i32, i32* %i, align 4
  %saddtmp12 = add i32 %lhs11, 1
  store i32 %saddtmp12, i32* %i, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  %retVal = load double, double* %x0, align 8
  store double %retVal, double* %sqrt-retval, align 8
  br label %return
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

define double @cAbs(%Complex %0) {
entry:
  %cAbs-retval = alloca double, align 8
  %access.tmp = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp, align 8
  %r = getelementptr inbounds %Complex, %Complex* %access.tmp, i32 0, i32 0
  %access.tmp1 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp1, align 8
  %r2 = getelementptr inbounds %Complex, %Complex* %access.tmp1, i32 0, i32 0
  %lhs = load double, double* %r, align 8
  %rhs = load double, double* %r2, align 8
  %smultmp = fmul double %lhs, %rhs
  %access.tmp3 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp3, align 8
  %i = getelementptr inbounds %Complex, %Complex* %access.tmp3, i32 0, i32 1
  %access.tmp4 = alloca %Complex, align 8
  store %Complex %0, %Complex* %access.tmp4, align 8
  %i5 = getelementptr inbounds %Complex, %Complex* %access.tmp4, i32 0, i32 1
  %lhs6 = load double, double* %i, align 8
  %rhs7 = load double, double* %i5, align 8
  %smultmp8 = fmul double %lhs6, %rhs7
  %saddtmp = fadd double %smultmp, %smultmp8
  %sqrt = call double @sqrt(double %saddtmp)
  store double %sqrt, double* %cAbs-retval, align 8
  br label %return

return:                                           ; preds = %entry
  %cAbs-retval9 = load double, double* %cAbs-retval, align 8
  ret double %cAbs-retval9
}

define i1 @mandelbrot(%Complex %0) {
entry:
  %mandelbrot-retval = alloca i1, align 1
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

return:                                           ; preds = %for.exit
  %mandelbrot-retval6 = load i1, i1* %mandelbrot-retval, align 1
  ret i1 %mandelbrot-retval6

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load i32, i32* %i2, align 4
  %1 = icmp slt i32 %lhs, 50
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %arg = load %Complex, %Complex* %z, align 8
  %arg3 = load %Complex, %Complex* %z, align 8
  %cMul = call %Complex @cMul(%Complex %arg, %Complex %arg3)
  %cAdd = call %Complex @cAdd(%Complex %cMul, %Complex %0)
  store %Complex %cAdd, %Complex* %z, align 8
  br label %for.incr

for.incr:                                         ; preds = %for.body
  %lhs4 = load i32, i32* %i2, align 4
  %saddtmp = add i32 %lhs4, 1
  store i32 %saddtmp, i32* %i2, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  %arg5 = load %Complex, %Complex* %z, align 8
  %cAbs = call double @cAbs(%Complex %arg5)
  %2 = fcmp olt double %cAbs, 2.000000e+00
  store i1 %2, i1* %mandelbrot-retval, align 1
  br label %return
}

define i32 @main() {
entry:
  %y = alloca double, align 8
  store double -1.000000e+00, double* %y, align 8
  br label %for.cond

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load double, double* %y, align 8
  %0 = fcmp olt double %lhs, 1.050000e+00
  br i1 %0, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %x = alloca double, align 8
  store double -2.000000e+00, double* %x, align 8
  br label %for.cond1

for.incr:                                         ; preds = %for.exit4
  %lhs10 = load double, double* %y, align 8
  %saddtmp11 = fadd double %lhs10, 5.000000e-02
  store double %saddtmp11, double* %y, align 8
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  ret i32 0

for.cond1:                                        ; preds = %for.incr3, %for.body
  %lhs5 = load double, double* %x, align 8
  %1 = fcmp olt double %lhs5, 5.000000e-01
  br i1 %1, label %for.body2, label %for.exit4

for.body2:                                        ; preds = %for.cond1
  %tmp.Complex = alloca %Complex, align 8
  %r = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 0
  %2 = load double, double* %x, align 8
  store double %2, double* %r, align 8
  %i = getelementptr inbounds %Complex, %Complex* %tmp.Complex, i32 0, i32 1
  %3 = load double, double* %y, align 8
  store double %3, double* %i, align 8
  %tmp.Complex6 = load %Complex, %Complex* %tmp.Complex, align 8
  %coord = alloca %Complex, align 8
  store %Complex %tmp.Complex6, %Complex* %coord, align 8
  %arg = load %Complex, %Complex* %coord, align 8
  %mandelbrot = call i1 @mandelbrot(%Complex %arg)
  br i1 %mandelbrot, label %if.then, label %if.else

for.incr3:                                        ; preds = %if.end
  %lhs8 = load double, double* %x, align 8
  %saddtmp = fadd double %lhs8, 3.150000e-02
  store double %saddtmp, double* %x, align 8
  br label %for.cond1

for.exit4:                                        ; preds = %for.cond1
  %printcall9 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @str.3, i32 0, i32 0), [2 x i8]* @2)
  br label %for.incr

if.then:                                          ; preds = %for.body2
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @str.1, i32 0, i32 0), [2 x i8]* @0)
  br label %if.end

if.else:                                          ; preds = %for.body2
  %printcall7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @str.2, i32 0, i32 0), [2 x i8]* @1)
  br label %if.end

if.end:                                           ; preds = %if.else, %if.then
  br label %for.incr3
}
