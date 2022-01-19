; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @foo(i32 %0) {
entry:
  %access.tmp = alloca i32, align 4
  store i32 %0, i32* %access.tmp, align 4
  %load = load i32, i32* %access.tmp, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %access.tmp, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
  ret void
}

define i32 @main() {
entry:
  %n = alloca i32, align 4
  store i32 1, i32* %n, align 4
  %arg = load i32, i32* %n, align 4
  call void @foo(i32 %arg)
  %print = load i32, i32* %n, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  ret i32 0
}
