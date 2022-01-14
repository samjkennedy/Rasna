; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %i = alloca i32, align 4
  store i32 4, i32* %i, align 4
  %i1 = load i32, i32* %i, align 4
  %slttmp = icmp slt i32 %i1, 5
  br i1 %slttmp, label %then, label %else

then:                                             ; preds = %entry
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 5)
  br label %exit

else:                                             ; preds = %entry
  %i5 = load i32, i32* %i, align 4
  %slttmp6 = icmp slt i32 %i5, 4
  br i1 %slttmp6, label %then2, label %else3

exit:                                             ; preds = %exit4, %then
  %result9 = phi i32 [ %printcall, %then ], [ %result, %exit4 ]
  ret i32 0

then2:                                            ; preds = %else
  %printcall7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 4)
  br label %exit4

else3:                                            ; preds = %else
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 10)
  br label %exit4

exit4:                                            ; preds = %else3, %then2
  %result = phi i32 [ %printcall7, %then2 ], [ %printcall8, %else3 ]
  br label %exit
}
