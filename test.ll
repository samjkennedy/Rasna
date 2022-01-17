; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @add(i32 %0, i32 %1) {
entry:
  %a = alloca i32, align 4
  store i32 %0, i32* %a, align 4
  %b = alloca i32, align 4
  store i32 %1, i32* %b, align 4
  %a1 = load i32, i32* %a, align 4
  %b2 = load i32, i32* %b, align 4
  %saddtmp = add i32 %a1, %b2
  ret i32 %saddtmp
}

define i32 @main() {
entry:
  %0 = call i32 @add(i32 2, i32 3)
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
  ret i32 0
}
