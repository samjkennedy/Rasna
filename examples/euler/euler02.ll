; ModuleID = 'examples/euler/euler02'
source_filename = "examples/euler/euler02"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %sum = alloca i32, align 4
  store i32 0, i32* %sum, align 4
  %last = alloca i32, align 4
  store i32 1, i32* %last, align 4
  %curr = alloca i32, align 4
  store i32 1, i32* %curr, align 4
  %curr1 = load i32, i32* %curr, align 4
  %slttmp = icmp slt i32 %curr1, 4000000
  br i1 %slttmp, label %then, label %exit-if

then:                                             ; preds = %latch, %entry
  %curr6 = load i32, i32* %curr, align 4
  %sremtmp = srem i32 %curr6, 2
  %eqtmp = icmp eq i32 %sremtmp, 0
  br i1 %eqtmp, label %then4, label %exit-if5

exit-if:                                          ; preds = %exit-loop, %entry
  %sum14 = load i32, i32* %sum, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %sum14)
  ret i32 0

latch:                                            ; preds = %exit-if5
  %curr2 = load i32, i32* %curr, align 4
  %slttmp3 = icmp slt i32 %curr2, 4000000
  br i1 %slttmp3, label %then, label %exit-loop

exit-loop:                                        ; preds = %latch
  br label %exit-if

then4:                                            ; preds = %then
  %sum7 = load i32, i32* %sum, align 4
  %curr8 = load i32, i32* %curr, align 4
  %saddtmp = add i32 %sum7, %curr8
  store i32 %saddtmp, i32* %sum, align 4
  br label %exit-if5

exit-if5:                                         ; preds = %then4, %then
  %tmp = alloca i32, align 4
  %curr9 = load i32, i32* %curr, align 4
  %last10 = load i32, i32* %last, align 4
  %saddtmp11 = add i32 %curr9, %last10
  store i32 %saddtmp11, i32* %tmp, align 4
  %curr12 = load i32, i32* %curr, align 4
  store i32 %curr12, i32* %last, align 4
  %tmp13 = load i32, i32* %tmp, align 4
  store i32 %tmp13, i32* %curr, align 4
  br label %latch
}
