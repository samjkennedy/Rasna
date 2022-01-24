; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i32 @sum(i32 %0, i32 %1) {
entry:
  %sum-retval = alloca i32, align 4
  %saddtmp = add i32 %0, %1
  store i32 %saddtmp, i32* %sum-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %sum-retval1 = load i32, i32* %sum-retval, align 4
  ret i32 %sum-retval1
}

define i32 @main() {
entry:
  %i = alloca i32, align 4
  store i32 5, i32* %i, align 4
  %s = alloca i32, align 4
  %arg = load i32, i32* %i, align 4
  %sum = call i32 @sum(i32 -2, i32 %arg)
  store i32 %sum, i32* %s, align 4
  %0 = load i32, i32* %s, align 4
  %1 = sub i32 0, %0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %1)
  ret i32 0
}
