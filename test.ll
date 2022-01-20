; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @printNoAssign(i32* %0) {
entry:
  %printNoAssign-retval = alloca i32, align 4
  %print = load i32, i32* %0, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %retVal = load i32, i32* %0, align 4
  store i32 %retVal, i32* %printNoAssign-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %printNoAssign-retval1 = load i32, i32* %printNoAssign-retval, align 4
  ret i32 %printNoAssign-retval1
}

define i32 @inc(i32* %0) {
entry:
  %inc-retval = alloca i32, align 4
  %load = load i32, i32* %0, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %0, align 4
  %retVal = load i32, i32* %0, align 4
  store i32 %retVal, i32* %inc-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %inc-retval1 = load i32, i32* %inc-retval, align 4
  ret i32 %inc-retval1
}

define i32 @main() {
entry:
  %n = alloca i32, align 4
  store i32 1, i32* %n, align 4
  %printNoAssign = call i32 @printNoAssign(i32* %n)
  %access.tmp = alloca i32, align 4
  store i32 %printNoAssign, i32* %access.tmp, align 4
  %inc = call i32 @inc(i32* %access.tmp)
  %access.tmp1 = alloca i32, align 4
  store i32 %inc, i32* %access.tmp1, align 4
  %printNoAssign2 = call i32 @printNoAssign(i32* %access.tmp1)
  ret i32 0
}
