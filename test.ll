; ModuleID = 'test'
source_filename = "test"

%Vec = type { i32, i32 }

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define %Vec @scale(%Vec %0, i32 %1) {
entry:
  %scale-retval = alloca %Vec, align 8
  %tmp.Vec = alloca %Vec, align 8
  %x = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 0
  %tmp = alloca %Vec, align 8
  store %Vec %0, %Vec* %tmp, align 4
  %x1 = getelementptr inbounds %Vec, %Vec* %tmp, i32 0, i32 0
  %lhs = load i32, i32* %x1, align 4
  %smultmp = mul i32 %lhs, %1
  store i32 %smultmp, i32* %x, align 4
  %y = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 1
  %tmp2 = alloca %Vec, align 8
  store %Vec %0, %Vec* %tmp2, align 4
  %y3 = getelementptr inbounds %Vec, %Vec* %tmp2, i32 0, i32 1
  %lhs4 = load i32, i32* %y3, align 4
  %smultmp5 = mul i32 %lhs4, %1
  store i32 %smultmp5, i32* %y, align 4
  %tmp.Vec6 = load %Vec, %Vec* %tmp.Vec, align 4
  store %Vec %tmp.Vec6, %Vec* %scale-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %scale-retval7 = load %Vec, %Vec* %scale-retval, align 4
  ret %Vec %scale-retval7
}

define i32 @sum(%Vec %0) {
entry:
  %sum-retval = alloca i32, align 4
  %tmp = alloca %Vec, align 8
  store %Vec %0, %Vec* %tmp, align 4
  %x = getelementptr inbounds %Vec, %Vec* %tmp, i32 0, i32 0
  %lhs = load i32, i32* %x, align 4
  %tmp1 = alloca %Vec, align 8
  store %Vec %0, %Vec* %tmp1, align 4
  %y = getelementptr inbounds %Vec, %Vec* %tmp1, i32 0, i32 1
  %rhs = load i32, i32* %y, align 4
  %saddtmp = add i32 %lhs, %rhs
  store i32 %saddtmp, i32* %sum-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %sum-retval2 = load i32, i32* %sum-retval, align 4
  ret i32 %sum-retval2
}

define void @printX(%Vec %0) {
entry:
  %tmp = alloca %Vec, align 8
  store %Vec %0, %Vec* %tmp, align 4
  %x = getelementptr inbounds %Vec, %Vec* %tmp, i32 0, i32 0
  %print = load i32, i32* %x, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  ret void
}

define i32 @main() {
entry:
  %v = alloca %Vec, align 8
  %tmp.Vec = alloca %Vec, align 8
  %x = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 0
  store i32 2, i32* %x, align 4
  %y = getelementptr inbounds %Vec, %Vec* %tmp.Vec, i32 0, i32 1
  store i32 3, i32* %y, align 4
  %tmp.Vec1 = load %Vec, %Vec* %tmp.Vec, align 4
  store %Vec %tmp.Vec1, %Vec* %v, align 4
  %x2 = getelementptr inbounds %Vec, %Vec* %v, i32 0, i32 0
  store i32 5, i32* %x2, align 4
  %arg = load %Vec, %Vec* %v, align 4
  call void @printX(%Vec %arg)
  %sum = alloca i32, align 4
  %arg3 = load %Vec, %Vec* %v, align 4
  %scale = call %Vec @scale(%Vec %arg3, i32 2)
  %sum4 = call i32 @sum(%Vec %scale)
  store i32 %sum4, i32* %sum, align 4
  %print = load i32, i32* %sum, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  ret i32 0
}
