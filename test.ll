; ModuleID = 'test'
source_filename = "test"

%Vec = type { double, double }

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.1 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.2 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1
@real.3 = private unnamed_addr constant [4 x i8] c"%f\0A\00", align 1

declare i32 @printf(i8*, ...)

define { %Vec, i32 } @reverse({ i32, %Vec } %0) {
entry:
  %reverse-retval = alloca { %Vec, i32 }, align 8
  %tmp.Tuple = alloca { %Vec, i32 }, align 8
  %1 = getelementptr inbounds { %Vec, i32 }, { %Vec, i32 }* %tmp.Tuple, i32 0, i32 0
  %access.tmp = alloca { i32, %Vec }, align 8
  store { i32, %Vec } %0, { i32, %Vec }* %access.tmp, align 8
  %"1" = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %access.tmp, i32 0, i32 1
  %2 = load %Vec, %Vec* %"1", align 8
  store %Vec %2, %Vec* %1, align 8
  %3 = getelementptr inbounds { %Vec, i32 }, { %Vec, i32 }* %tmp.Tuple, i32 0, i32 1
  %access.tmp1 = alloca { i32, %Vec }, align 8
  store { i32, %Vec } %0, { i32, %Vec }* %access.tmp1, align 8
  %"0" = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %access.tmp1, i32 0, i32 0
  %4 = load i32, i32* %"0", align 4
  store i32 %4, i32* %3, align 4
  %tmp.Tuple2 = load { %Vec, i32 }, { %Vec, i32 }* %tmp.Tuple, align 8
  store { %Vec, i32 } %tmp.Tuple2, { %Vec, i32 }* %reverse-retval, align 8
  br label %return

return:                                           ; preds = %entry
  %reverse-retval3 = load { %Vec, i32 }, { %Vec, i32 }* %reverse-retval, align 8
  ret { %Vec, i32 } %reverse-retval3
}

define i32 @main() {
entry:
  %t = alloca { i32, %Vec }, align 8
  %tmp.Tuple = alloca { i32, %Vec }, align 8
  %0 = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %tmp.Tuple, i32 0, i32 0
  store i32 1, i32* %0, align 4
  %1 = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %tmp.Tuple, i32 0, i32 1
  %tmp.Vec = alloca %Vec, align 8
  %x = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 0
  store double 2.000000e+00, double* %x, align 8
  %y = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 1
  store double 3.500000e+00, double* %y, align 8
  %tmp.Vec1 = load %Vec, %Vec* %tmp.Vec, align 8
  store %Vec %tmp.Vec1, %Vec* %1, align 8
  %tmp.Tuple2 = load { i32, %Vec }, { i32, %Vec }* %tmp.Tuple, align 8
  store { i32, %Vec } %tmp.Tuple2, { i32, %Vec }* %t, align 8
  %"0" = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %t, i32 0, i32 0
  %print = load i32, i32* %"0", align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %"1" = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %t, i32 0, i32 1
  %x3 = getelementptr inbounds %Vec, %Vec* %"1", i32 0, i32 0
  %print4 = load double, double* %x3, align 8
  %printcall5 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), double %print4)
  %"16" = getelementptr inbounds { i32, %Vec }, { i32, %Vec }* %t, i32 0, i32 1
  %y7 = getelementptr inbounds %Vec, %Vec* %"16", i32 0, i32 1
  %print8 = load double, double* %y7, align 8
  %printcall9 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.1, i32 0, i32 0), double %print8)
  %r = alloca { %Vec, i32 }, align 8
  %arg = load { i32, %Vec }, { i32, %Vec }* %t, align 8
  %reverse = call { %Vec, i32 } @reverse({ i32, %Vec } %arg)
  store { %Vec, i32 } %reverse, { %Vec, i32 }* %r, align 8
  %"010" = getelementptr inbounds { %Vec, i32 }, { %Vec, i32 }* %r, i32 0, i32 0
  %x11 = getelementptr inbounds %Vec, %Vec* %"010", i32 0, i32 0
  %print12 = load double, double* %x11, align 8
  %printcall13 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.2, i32 0, i32 0), double %print12)
  %"014" = getelementptr inbounds { %Vec, i32 }, { %Vec, i32 }* %r, i32 0, i32 0
  %y15 = getelementptr inbounds %Vec, %Vec* %"014", i32 0, i32 1
  %print16 = load double, double* %y15, align 8
  %printcall17 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.3, i32 0, i32 0), double %print16)
  %"118" = getelementptr inbounds { %Vec, i32 }, { %Vec, i32 }* %r, i32 0, i32 1
  %print19 = load i32, i32* %"118", align 4
  %printcall20 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print19)
  ret i32 0
}
