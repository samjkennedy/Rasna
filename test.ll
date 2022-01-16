; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @main() {
entry:
  %i = alloca i32, align 4
  store i32 3, i32* %i, align 4
  %ints = alloca [6 x i32], align 4
  store [6 x i32] [i32 0, i32 1, i32 2, i32 3, i32 4, i32 5], [6 x i32]* %ints, align 4
  %ints1 = load [6 x i32], [6 x i32]* %ints, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 -66080936)
  %ints2 = load [6 x i32], [6 x i32]* %ints, align 4
  %printcall3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 481)
  %ints4 = load [6 x i32], [6 x i32]* %ints, align 4
  %printcall5 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 -66083960)
  %ints6 = load [6 x i32], [6 x i32]* %ints, align 4
  %printcall7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 481)
  ret i32 0
}
