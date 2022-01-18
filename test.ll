; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @ifbreak(i32 %0) {
entry:
  %ifbreak-retval = alloca i32, align 4
  %slttmp = icmp slt i32 %0, 5
  br i1 %slttmp, label %if.true, label %if.end

return:                                           ; preds = %if.end, %if.true
  %ifbreak-retval1 = load i32, i32* %ifbreak-retval, align 4
  ret i32 %ifbreak-retval1

if.true:                                          ; preds = %entry
  store i32 1, i32* %ifbreak-retval, align 4
  br label %return

if.end:                                           ; preds = %entry
  store i32 2, i32* %ifbreak-retval, align 4
  br label %return
}

define i32 @main() {
entry:
  %ifbreak = call i32 @ifbreak(i32 1)
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %ifbreak)
  %ifbreak1 = call i32 @ifbreak(i32 10)
  %printcall2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %ifbreak1)
  ret i32 0
}
