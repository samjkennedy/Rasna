; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@Red = private unnamed_addr constant [4 x i8] c"Red\00", align 1
@str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@Blue = private unnamed_addr constant [5 x i8] c"Blue\00", align 1
@str.2 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@Green = private unnamed_addr constant [6 x i8] c"Green\00", align 1
@str.3 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define void @printColor(i32* %0) {
entry:
  %lhs = load i32, i32* %0, align 4
  %1 = icmp eq i32 %lhs, 0
  br i1 %1, label %if.then, label %if.end

if.then:                                          ; preds = %entry
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.1, i32 0, i32 0), i8* getelementptr inbounds ([4 x i8], [4 x i8]* @Red, i32 0, i32 0))
  br label %if.end

if.end:                                           ; preds = %if.then, %entry
  %lhs3 = load i32, i32* %0, align 4
  %2 = icmp eq i32 %lhs3, 1
  br i1 %2, label %if.then1, label %if.end2

if.then1:                                         ; preds = %if.end
  %printcall4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.2, i32 0, i32 0), i8* getelementptr inbounds ([5 x i8], [5 x i8]* @Blue, i32 0, i32 0))
  br label %if.end2

if.end2:                                          ; preds = %if.then1, %if.end
  %lhs7 = load i32, i32* %0, align 4
  %3 = icmp eq i32 %lhs7, 2
  br i1 %3, label %if.then5, label %if.end6

if.then5:                                         ; preds = %if.end2
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.3, i32 0, i32 0), i8* getelementptr inbounds ([6 x i8], [6 x i8]* @Green, i32 0, i32 0))
  br label %if.end6

if.end6:                                          ; preds = %if.then5, %if.end2
  ret void
}

define void @sort({ i32, i32* }* %0) {
entry:
  %n = alloca i32, align 4
  %size = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 0
  %val = load i32, i32* %size, align 4
  store i32 %val, i32* %n, align 4
  %i = alloca i32, align 4
  store i32 1, i32* %i, align 4
  br label %for.cond

for.cond:                                         ; preds = %for.incr, %entry
  %lhs = load i32, i32* %i, align 4
  %rhs = load i32, i32* %n, align 4
  %1 = icmp slt i32 %lhs, %rhs
  br i1 %1, label %for.body, label %for.exit

for.body:                                         ; preds = %for.cond
  %j = alloca i32, align 4
  store i32 0, i32* %j, align 4
  br label %for.cond1

for.incr:                                         ; preds = %for.exit4
  %lhs33 = load i32, i32* %i, align 4
  %saddtmp34 = add i32 %lhs33, 1
  store i32 %saddtmp34, i32* %i, align 4
  br label %for.cond

for.exit:                                         ; preds = %for.cond
  ret void

for.cond1:                                        ; preds = %for.incr3, %for.body
  %lhs5 = load i32, i32* %j, align 4
  %lhs6 = load i32, i32* %n, align 4
  %rhs7 = load i32, i32* %i, align 4
  %ssubtmp = sub i32 %lhs6, %rhs7
  %2 = icmp slt i32 %lhs5, %ssubtmp
  br i1 %2, label %for.body2, label %for.exit4

for.body2:                                        ; preds = %for.cond1
  %idx = load i32, i32* %j, align 4
  %arr = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr8 = load i32*, i32** %arr, align 8
  %arrayidx = getelementptr inbounds i32, i32* %arr8, i32 %idx
  %3 = load i32, i32* %arrayidx, align 4
  %lhs9 = load i32, i32* %j, align 4
  %saddtmp = add i32 %lhs9, 1
  %arr10 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr11 = load i32*, i32** %arr10, align 8
  %arrayidx12 = getelementptr inbounds i32, i32* %arr11, i32 %saddtmp
  %4 = load i32, i32* %arrayidx12, align 4
  %5 = icmp sgt i32 %3, %4
  br i1 %5, label %if.then, label %if.end

for.incr3:                                        ; preds = %if.end
  %lhs31 = load i32, i32* %j, align 4
  %saddtmp32 = add i32 %lhs31, 1
  store i32 %saddtmp32, i32* %j, align 4
  br label %for.cond1

for.exit4:                                        ; preds = %for.cond1
  br label %for.incr

if.then:                                          ; preds = %for.body2
  %tmp = alloca i32, align 4
  %idx13 = load i32, i32* %j, align 4
  %arr14 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr15 = load i32*, i32** %arr14, align 8
  %arrayidx16 = getelementptr inbounds i32, i32* %arr15, i32 %idx13
  %6 = load i32, i32* %arrayidx16, align 4
  store i32 %6, i32* %tmp, align 4
  %idx17 = load i32, i32* %j, align 4
  %arr18 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr19 = load i32*, i32** %arr18, align 8
  %arrayidx20 = getelementptr inbounds i32, i32* %arr19, i32 %idx17
  %lhs21 = load i32, i32* %j, align 4
  %saddtmp22 = add i32 %lhs21, 1
  %arr23 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr24 = load i32*, i32** %arr23, align 8
  %arrayidx25 = getelementptr inbounds i32, i32* %arr24, i32 %saddtmp22
  %7 = load i32, i32* %arrayidx25, align 4
  store i32 %7, i32* %arrayidx20, align 4
  %lhs26 = load i32, i32* %j, align 4
  %saddtmp27 = add i32 %lhs26, 1
  %arr28 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %0, i32 0, i32 1
  %arr29 = load i32*, i32** %arr28, align 8
  %arrayidx30 = getelementptr inbounds i32, i32* %arr29, i32 %saddtmp27
  %value = load i32, i32* %tmp, align 4
  store i32 %value, i32* %arrayidx30, align 4
  br label %if.end

if.end:                                           ; preds = %if.then, %for.body2
  br label %for.incr3
}

define { i32, i32 } @flip({ i32, i32 } %0) {
entry:
  %flip-retval = alloca { i32, i32 }, align 8
  %"tmp.(Color, Color)" = alloca { i32, i32 }, align 8
  %1 = getelementptr inbounds { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", i32 0, i32 0
  %access.tmp = alloca { i32, i32 }, align 8
  store { i32, i32 } %0, { i32, i32 }* %access.tmp, align 4
  %"1" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %access.tmp, i32 0, i32 1
  %2 = load i32, i32* %"1", align 4
  store i32 %2, i32* %1, align 4
  %3 = getelementptr inbounds { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", i32 0, i32 1
  %access.tmp1 = alloca { i32, i32 }, align 8
  store { i32, i32 } %0, { i32, i32 }* %access.tmp1, align 4
  %"0" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %access.tmp1, i32 0, i32 0
  %4 = load i32, i32* %"0", align 4
  store i32 %4, i32* %3, align 4
  %"tmp.(Color, Color)2" = load { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", align 4
  store { i32, i32 } %"tmp.(Color, Color)2", { i32, i32 }* %flip-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %flip-retval3 = load { i32, i32 }, { i32, i32 }* %flip-retval, align 4
  ret { i32, i32 } %flip-retval3
}

define i32 @main() {
entry:
  %t = alloca { i32, i32 }, align 8
  %"tmp.(Color, Color)" = alloca { i32, i32 }, align 8
  %0 = getelementptr inbounds { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", i32 0, i32 0
  store i32 0, i32* %0, align 4
  %1 = getelementptr inbounds { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", i32 0, i32 1
  store i32 1, i32* %1, align 4
  %"tmp.(Color, Color)1" = load { i32, i32 }, { i32, i32 }* %"tmp.(Color, Color)", align 4
  store { i32, i32 } %"tmp.(Color, Color)1", { i32, i32 }* %t, align 4
  %"0" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %t, i32 0, i32 0
  call void @printColor(i32* %"0")
  %"1" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %t, i32 0, i32 1
  call void @printColor(i32* %"1")
  %t2 = alloca { i32, i32 }, align 8
  %arg = load { i32, i32 }, { i32, i32 }* %t, align 4
  %flip = call { i32, i32 } @flip({ i32, i32 } %arg)
  store { i32, i32 } %flip, { i32, i32 }* %t2, align 4
  %"02" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %t2, i32 0, i32 0
  call void @printColor(i32* %"02")
  %"13" = getelementptr inbounds { i32, i32 }, { i32, i32 }* %t2, i32 0, i32 1
  call void @printColor(i32* %"13")
  ret i32 0
}
