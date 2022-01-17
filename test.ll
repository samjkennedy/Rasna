; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @puti(i32 %0) {
entry:
  %i = alloca i32, align 4
  store i32 %0, i32* %i, align 4
  %i1 = load i32, i32* %i, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %i1)
  ret void
}

define void @print1() {
entry:
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 1)
  ret void
}

define i32 @main() {
entry:
  %k = alloca i32, align 4
  store i32 3, i32* %k, align 4
  %k1 = load i32, i32* %k, align 4
  call void @puti(i32 %k1)
  call void @print1()
  ret i32 0
}
