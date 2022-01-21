; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@"Hello there" = private unnamed_addr constant [12 x i8] c"Hello there\00", align 1
@str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i32 @max(i32 %0, i32 %1) {
entry:
  %max-retval = alloca i32, align 4
  %2 = icmp sgt i32 %0, %1
  br i1 %2, label %if.then, label %if.else

return:                                           ; preds = %if.else2, %if.then1, %if.then
  %max-retval4 = load i32, i32* %max-retval, align 4
  ret i32 %max-retval4

if.then:                                          ; preds = %entry
  store i32 %0, i32* %max-retval, align 4
  br label %return

if.else:                                          ; preds = %entry
  %3 = icmp eq i32 %0, %1
  br i1 %3, label %if.then1, label %if.else2

if.then1:                                         ; preds = %if.else
  store i32 %0, i32* %max-retval, align 4
  br label %return

if.else2:                                         ; preds = %if.else
  store i32 %1, i32* %max-retval, align 4
  br label %return
}

define void @puti(i32 %0) {
entry:
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
  ret void
}

define i32 @main() {
entry:
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.1, i32 0, i32 0), i8* getelementptr inbounds ([12 x i8], [12 x i8]* @"Hello there", i32 0, i32 0))
  %n = alloca i32, align 4
  store i32 1, i32* %n, align 4
  br label %while.cond

while.cond:                                       ; preds = %while.body, %entry
  %lhs = load i32, i32* %n, align 4
  %0 = icmp slt i32 %lhs, 10
  br i1 %0, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %print = load i32, i32* %n, align 4
  %printcall1 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %lhs2 = load i32, i32* %n, align 4
  %rhs = load i32, i32* %n, align 4
  %saddtmp = add i32 %lhs2, %rhs
  store i32 %saddtmp, i32* %n, align 4
  br label %while.cond

while.exit:                                       ; preds = %while.cond
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  br label %while.cond3

while.cond3:                                      ; preds = %while.exit11, %while.exit
  %lhs6 = load i32, i32* %i, align 4
  %1 = icmp slt i32 %lhs6, 10
  br i1 %1, label %while.body4, label %while.exit5

while.body4:                                      ; preds = %while.cond3
  %print7 = load i32, i32* %i, align 4
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print7)
  %j = alloca i32, align 4
  store i32 0, i32* %j, align 4
  br label %while.cond9

while.exit5:                                      ; preds = %while.cond3
  %r = alloca double, align 8
  store double 0.000000e+00, double* %r, align 8
  br label %while.cond17

while.cond9:                                      ; preds = %while.body10, %while.body4
  %lhs12 = load i32, i32* %j, align 4
  %2 = icmp slt i32 %lhs12, 10
  br i1 %2, label %while.body10, label %while.exit11

while.body10:                                     ; preds = %while.cond9
  %print13 = load i32, i32* %j, align 4
  %printcall14 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print13)
  %load = load i32, i32* %j, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %j, align 4
  br label %while.cond9

while.exit11:                                     ; preds = %while.cond9
  %load15 = load i32, i32* %i, align 4
  %incrtmp16 = add i32 %load15, 1
  store i32 %incrtmp16, i32* %i, align 4
  br label %while.cond3

while.cond17:                                     ; preds = %while.body18, %while.exit5
  %lhs20 = load double, double* %r, align 8
  %3 = fcmp ole double %lhs20, 1.000000e+01
  br i1 %3, label %while.body18, label %while.exit19

while.body18:                                     ; preds = %while.cond17
  %print21 = load double, double* %r, align 8
  %printcall22 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), double %print21)
  %lhs23 = load double, double* %r, align 8
  %saddtmp24 = fadd double %lhs23, 5.000000e-01
  store double %saddtmp24, double* %r, align 8
  br label %while.cond17

while.exit19:                                     ; preds = %while.cond17
  %j25 = alloca i32, align 4
  store i32 2, i32* %j25, align 4
  %arg = load i32, i32* %j25, align 4
  %max = call i32 @max(i32 %arg, i32 3)
  call void @puti(i32 %max)
  ret i32 0
}
