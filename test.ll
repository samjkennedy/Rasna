; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %N = alloca i32, align 4
  store i32 0, i32* %N, align 4
  %N1 = load i32, i32* %N, align 4
  %slttmp = icmp slt i32 %N1, 10
  br i1 %slttmp, label %then, label %exit

then:                                             ; preds = %latch, %entry
  %N2 = load i32, i32* %N, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %N2)
  %N3 = load i32, i32* %N, align 4
  %saddtmp = add i32 %N3, 1
  store i32 %saddtmp, i32* %N, align 4
  br label %latch

exit:                                             ; preds = %break, %entry
  %M = alloca i32, align 4
  store i32 0, i32* %M, align 4
  %M8 = load i32, i32* %M, align 4
  %slttmp9 = icmp slt i32 %M8, 10
  br i1 %slttmp9, label %then6, label %exit7

latch:                                            ; preds = %then
  %N4 = load i32, i32* %N, align 4
  %slttmp5 = icmp slt i32 %N4, 10
  br i1 %slttmp5, label %then, label %break

break:                                            ; preds = %latch
  br label %exit

then6:                                            ; preds = %latch10, %exit
  %M12 = load i32, i32* %M, align 4
  %printcall13 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %M12)
  %M14 = load i32, i32* %M, align 4
  %saddtmp15 = add i32 %M14, 2
  store i32 %saddtmp15, i32* %M, align 4
  br label %latch10

exit7:                                            ; preds = %break11, %exit
  %i = alloca i32, align 4
  store i32 0, i32* %i, align 4
  %i20 = load i32, i32* %i, align 4
  %slttmp21 = icmp slt i32 %i20, 10
  br i1 %slttmp21, label %then18, label %exit19

latch10:                                          ; preds = %then6
  %M16 = load i32, i32* %M, align 4
  %slttmp17 = icmp slt i32 %M16, 10
  br i1 %slttmp17, label %then6, label %break11

break11:                                          ; preds = %latch10
  br label %exit7

then18:                                           ; preds = %latch22, %exit7
  %i24 = load i32, i32* %i, align 4
  %printcall25 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %i24)
  %i26 = load i32, i32* %i, align 4
  %saddtmp27 = add i32 %i26, 1
  store i32 %saddtmp27, i32* %i, align 4
  br label %latch22

exit19:                                           ; preds = %break23, %exit7
  ret i32 0

latch22:                                          ; preds = %then18
  %i28 = load i32, i32* %i, align 4
  %slttmp29 = icmp slt i32 %i28, 10
  br i1 %slttmp29, label %then18, label %break23

break23:                                          ; preds = %latch22
  br label %exit19
}
