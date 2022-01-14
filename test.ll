; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %n = alloca i32, align 4
  store i32 1, i32* %n, align 4
  %n1 = load i32, i32* %n, align 4
  %slttmp = icmp slt i32 %n1, 256
  br i1 %slttmp, label %then, label %exit-if

then:                                             ; preds = %latch, %entry
  %n4 = load i32, i32* %n, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %n4)
  %n5 = load i32, i32* %n, align 4
  %n6 = load i32, i32* %n, align 4
  %saddtmp = add i32 %n5, %n6
  store i32 %saddtmp, i32* %n, align 4
  br label %latch

exit-if:                                          ; preds = %exit-loop, %entry
  ret i32 0

latch:                                            ; preds = %then
  %n2 = load i32, i32* %n, align 4
  %slttmp3 = icmp slt i32 %n2, 256
  br i1 %slttmp3, label %then, label %exit-loop

exit-loop:                                        ; preds = %latch
  br label %exit-if
}
