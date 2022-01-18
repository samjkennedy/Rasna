; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.1 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.2 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.3 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

declare i32 @printf(i8*, ...)

define double @sqrt(double %0) {
entry:
  %sqrt-retval = alloca double, align 8
  %iterations = alloca i32, align 4
  store i32 100, i32* %iterations, align 4
  %sdivtmp = fdiv double %0, 1.000000e+01
  %x0 = alloca double, align 8
  store double %sdivtmp, double* %x0, align 8
  %x01 = load double, double* %x0, align 8
  %x02 = load double, double* %x0, align 8
  %sdivtmp3 = fdiv double %0, %x02
  %saddtmp = fadd double %x01, %sdivtmp3
  %sdivtmp4 = fdiv double %saddtmp, 2.000000e+00
  %x1 = alloca double, align 8
  store double %sdivtmp4, double* %x1, align 8
  %i = alloca i32, align 4
  store i32 1, i32* %i, align 4
  br label %for.cond

return:                                           ; preds = %for.exit
  %sqrt-retval17 = load double, double* %sqrt-retval, align 8
  ret double %sqrt-retval17

for.cond:                                         ; preds = %for.incr, %entry
  %i5 = load i32, i32* %i, align 4
  %iterations6 = load i32, i32* %iterations, align 4
  %1 = icmp slt i32 %i5, %iterations6
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %x07 = load double, double* %x0, align 8
  %tmp = alloca double, align 8
  store double %x07, double* %tmp, align 8
  %x18 = load double, double* %x1, align 8
  %x19 = load double, double* %x1, align 8
  %sdivtmp10 = fdiv double %0, %x19
  %saddtmp11 = fadd double %x18, %sdivtmp10
  %sdivtmp12 = fdiv double %saddtmp11, 2.000000e+00
  store double %sdivtmp12, double* %x0, align 8
  %tmp13 = load double, double* %tmp, align 8
  store double %tmp13, double* %x1, align 8
  br label %for.incr

for.incr:                                         ; preds = %for.body
  %i14 = load i32, i32* %i, align 4
  %saddtmp15 = add i32 %i14, 1
  store i32 %saddtmp15, i32* %i, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  %x016 = load double, double* %x0, align 8
  store double %x016, double* %sqrt-retval, align 8
  br label %return
}

define i32 @main() {
entry:
  %sqrt = call double @sqrt(double 4.000000e+00)
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), double %sqrt)
  %sqrt1 = call double @sqrt(double 5.000000e+00)
  %printcall2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.1, i32 0, i32 0), double %sqrt1)
  %sqrt3 = call double @sqrt(double 3.520000e+01)
  %printcall4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.2, i32 0, i32 0), double %sqrt3)
  %sqrt5 = call double @sqrt(double 1.253480e+05)
  %printcall6 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.3, i32 0, i32 0), double %sqrt5)
  ret i32 0
}
