; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @whileReturn() {
entry:
  %whileReturn-retval = alloca i32, align 4
  %n = alloca i32, align 4
  store i32 0, i32* %n, align 4
  br label %while.cond

return:                                           ; preds = %while.exit, %if.then
  %whileReturn-retval6 = load i32, i32* %whileReturn-retval, align 4
  ret i32 %whileReturn-retval6

while.cond:                                       ; preds = %if.end, %entry
  %n1 = load i32, i32* %n, align 4
  %0 = icmp slt i32 %n1, 100
  br i1 %0, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %n2 = load i32, i32* %n, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %n2)
  %n3 = load i32, i32* %n, align 4
  %incrtmp = add i32 %n3, 1
  store i32 %incrtmp, i32* %n, align 4
  %n4 = load i32, i32* %n, align 4
  %1 = icmp sgt i32 %n4, 10
  br i1 %1, label %if.then, label %if.end

while.exit:                                       ; preds = %while.cond
  store i32 100, i32* %whileReturn-retval, align 4
  br label %return

if.then:                                          ; preds = %while.body
  %n5 = load i32, i32* %n, align 4
  store i32 %n5, i32* %whileReturn-retval, align 4
  br label %return

if.end:                                           ; preds = %while.body
  br label %while.cond
}

define i32 @main() {
entry:
  %whileReturn = call i32 @whileReturn()
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %whileReturn)
  ret i32 0
}
