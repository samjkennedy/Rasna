; ModuleID = 'examples/euler/euler01'
source_filename = "examples/euler/euler01"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %sum = alloca i32, align 4
  store i32 0, i32* %sum, align 4
  %N = alloca i32, align 4
  store i32 1, i32* %N, align 4
  %N1 = load i32, i32* %N, align 4
  %slttmp = icmp slt i32 %N1, 1000
  br i1 %slttmp, label %then, label %exit-if

then:                                             ; preds = %latch, %entry
  %N6 = load i32, i32* %N, align 4
  %sremtmp = srem i32 %N6, 3
  %eqtmp = icmp eq i32 %sremtmp, 0
  %N7 = load i32, i32* %N, align 4
  %sremtmp8 = srem i32 %N7, 5
  %eqtmp9 = icmp eq i32 %sremtmp8, 0
  %ortmp = or i1 %eqtmp, %eqtmp9
  br i1 %ortmp, label %then4, label %exit-if5

exit-if:                                          ; preds = %exit-loop, %entry
  %sum14 = load i32, i32* %sum, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %sum14)
  ret i32 0

latch:                                            ; preds = %exit-if5
  %N2 = load i32, i32* %N, align 4
  %slttmp3 = icmp slt i32 %N2, 1000
  br i1 %slttmp3, label %then, label %exit-loop

exit-loop:                                        ; preds = %latch
  br label %exit-if

then4:                                            ; preds = %then
  %sum10 = load i32, i32* %sum, align 4
  %N11 = load i32, i32* %N, align 4
  %saddtmp = add i32 %sum10, %N11
  store i32 %saddtmp, i32* %sum, align 4
  br label %exit-if5

exit-if5:                                         ; preds = %then4, %then
  %N12 = load i32, i32* %N, align 4
  %saddtmp13 = add i32 %N12, 1
  store i32 %saddtmp13, i32* %N, align 4
  br label %latch
}
