; ModuleID = 'test'
source_filename = "test"

%Vec = type { double, double }

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.1 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printv(%Vec* %0) {
entry:
  %x = getelementptr inbounds %Vec, %Vec* %0, i32 0, i32 0
  %print = load double, double* %x, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), double %print)
  %y = getelementptr inbounds %Vec, %Vec* %0, i32 0, i32 1
  %print1 = load double, double* %y, align 8
  %printcall2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.1, i32 0, i32 0), double %print1)
  ret void
}

define i32 @main() {
entry:
  ret i32 0
}
