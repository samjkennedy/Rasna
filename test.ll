; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i1 @greaterThan3(i32 %0) {
entry:
  %greaterThan3-retval = alloca i1, align 1
  %1 = icmp sgt i32 %0, 3
  store i1 %1, i1* %greaterThan3-retval, align 1
  br label %return

return:                                           ; preds = %entry
  %greaterThan3-retval1 = load i1, i1* %greaterThan3-retval, align 1
  ret i1 %greaterThan3-retval1
}

define i32 @main() {
entry:
  %t = alloca { i32, i1 }, align 8
  %tmp.Tuple = alloca { i32, i1 }, align 8
  %0 = getelementptr inbounds { i32, i1 }, { i32, i1 }* %tmp.Tuple, i32 0, i32 0
  store i32 1, i32* %0, align 4
  %1 = getelementptr inbounds { i32, i1 }, { i32, i1 }* %tmp.Tuple, i32 0, i32 1
  store i1 true, i1* %1, align 1
  %tmp.Tuple1 = load { i32, i1 }, { i32, i1 }* %tmp.Tuple, align 4
  store { i32, i1 } %tmp.Tuple1, { i32, i1 }* %t, align 4
  %"0" = getelementptr inbounds { i32, i1 }, { i32, i1 }* %t, i32 0, i32 0
  %print = load i32, i32* %"0", align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %"1" = getelementptr inbounds { i32, i1 }, { i32, i1 }* %t, i32 0, i32 1
  %print2 = load i1, i1* %"1", align 1
  call void @printb(i1 %print2)
  %"03" = getelementptr inbounds { i32, i1 }, { i32, i1 }* %t, i32 0, i32 0
  %arg = load i32, i32* %"03", align 4
  %greaterThan3 = call i1 @greaterThan3(i32 %arg)
  call void @printb(i1 %greaterThan3)
  ret i32 0
}
