; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%c\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i8 @next(i8 %0) {
entry:
  %next-retval = alloca i8, align 1
  %saddtmp = add i8 %0, 1
  store i8 %saddtmp, i8* %next-retval, align 1
  br label %return

return:                                           ; preds = %entry
  %next-retval1 = load i8, i8* %next-retval, align 1
  ret i8 %next-retval1
}

define i8 @prev(i8 %0) {
entry:
  %prev-retval = alloca i8, align 1
  %ssubtmp = sub i8 %0, 1
  store i8 %ssubtmp, i8* %prev-retval, align 1
  br label %return

return:                                           ; preds = %entry
  %prev-retval1 = load i8, i8* %prev-retval, align 1
  ret i8 %prev-retval1
}

define i32 @main() {
entry:
  %c = alloca i8, align 1
  store i8 65, i8* %c, align 1
  br label %for.cond

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load i8, i8* %c, align 1
  %0 = icmp sle i8 %lhs, 90
  br i1 %0, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %arg = load i8, i8* %c, align 1
  %next = call i8 @next(i8 %arg)
  %next1 = call i8 @next(i8 %next)
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), i8 %next1)
  br label %for.incr

for.incr:                                         ; preds = %for.body
  %lhs2 = load i8, i8* %c, align 1
  %saddtmp = add i8 %lhs2, 1
  store i8 %saddtmp, i8* %c, align 1
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  ret i32 0
}
