; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %N = alloca i32, align 4
  store i32 0, i32* %N, align 4
  %N1 = load i32, i32* %N, align 4
  %slttmp = icmp slt i32 %N1, 11
  br i1 %slttmp, label %then, label %exit-if

then:                                             ; preds = %latch, %entry
  %N6 = load i32, i32* %N, align 4
  %sremtmp = srem i32 %N6, 2
  %eqtmp = icmp eq i32 %sremtmp, 0
  %N7 = load i32, i32* %N, align 4
  %slttmp8 = icmp slt i32 %N7, 3
  %N9 = load i32, i32* %N, align 4
  %sgttmp = icmp sgt i32 %N9, 8
  %ortmp = or i1 %slttmp8, %sgttmp
  %andtmp = and i1 %eqtmp, %ortmp
  br i1 %andtmp, label %then4, label %exit-if5

exit-if:                                          ; preds = %exit-loop, %entry
  ret i32 0

latch:                                            ; preds = %exit-if5
  %N2 = load i32, i32* %N, align 4
  %slttmp3 = icmp slt i32 %N2, 11
  br i1 %slttmp3, label %then, label %exit-loop

exit-loop:                                        ; preds = %latch
  br label %exit-if

then4:                                            ; preds = %then
  %N10 = load i32, i32* %N, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %N10)
  br label %exit-if5

exit-if5:                                         ; preds = %then4, %then
  %N11 = load i32, i32* %N, align 4
  %saddtmp = add i32 %N11, 1
  store i32 %saddtmp, i32* %N, align 4
  br label %latch
}
