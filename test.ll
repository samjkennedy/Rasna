; ModuleID = 'test'
source_filename = "test"

%Vec = type { double, double }

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @mut(%Vec %0) {
entry:
  %access.tmp = alloca %Vec, align 8
  store %Vec %0, %Vec* %access.tmp, align 8
  %x = getelementptr inbounds %Vec, %Vec* %access.tmp, i32 0, i32 0
  store double 1.000000e+00, double* %x, align 8
  ret void
}

define i32 @main() {
entry:
  %v = alloca %Vec, align 8
  %tmp.Vec = alloca %Vec, align 8
  %x = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 0
  store double 2.000000e+00, double* %x, align 8
  %y = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 1
  store double 3.000000e+00, double* %y, align 8
  %tmp.Vec1 = load %Vec, %Vec* %tmp.Vec, align 8
  store %Vec %tmp.Vec1, %Vec* %v, align 8
  %arg = load %Vec, %Vec* %v, align 8
  call void @mut(%Vec %arg)
  %x2 = getelementptr inbounds %Vec, %Vec* %v, i32 0, i32 0
  %print = load double, double* %x2, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), double %print)
  ret i32 0
}
