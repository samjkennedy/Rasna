; ModuleID = 'test'
source_filename = "test"

%Item = type { i32, i32 }

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@Red = private unnamed_addr constant [4 x i8] c"Red\00", align 1
@str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@Blue = private unnamed_addr constant [5 x i8] c"Blue\00", align 1
@str.2 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@Green = private unnamed_addr constant [6 x i8] c"Green\00", align 1
@str.3 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@str.4 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define { i32, { i1, i32 }, %Item } @getComplexTuple() {
entry:
  %getComplexTuple-retval = alloca { i32, { i1, i32 }, %Item }, align 8
  %"tmp.(Int, (Bool, Color), Item)" = alloca { i32, { i1, i32 }, %Item }, align 8
  %0 = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %"tmp.(Int, (Bool, Color), Item)", i32 0, i32 0
  store i32 1, i32* %0, align 4
  %1 = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %"tmp.(Int, (Bool, Color), Item)", i32 0, i32 1
  %"tmp.(Bool, Color)" = alloca { i1, i32 }, align 8
  %2 = getelementptr inbounds { i1, i32 }, { i1, i32 }* %"tmp.(Bool, Color)", i32 0, i32 0
  store i1 true, i1* %2, align 1
  %3 = getelementptr inbounds { i1, i32 }, { i1, i32 }* %"tmp.(Bool, Color)", i32 0, i32 1
  store i32 0, i32* %3, align 4
  %"tmp.(Bool, Color)1" = load { i1, i32 }, { i1, i32 }* %"tmp.(Bool, Color)", align 4
  store { i1, i32 } %"tmp.(Bool, Color)1", { i1, i32 }* %1, align 4
  %4 = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %"tmp.(Int, (Bool, Color), Item)", i32 0, i32 2
  %tmp.Item = alloca %Item, align 8
  %i = getelementptr inbounds %Item, %Item* %tmp.Item, i32 0, i32 0
  store i32 3, i32* %i, align 4
  %color = getelementptr inbounds %Item, %Item* %tmp.Item, i32 0, i32 1
  store i32 1, i32* %color, align 4
  %tmp.Item2 = load %Item, %Item* %tmp.Item, align 4
  store %Item %tmp.Item2, %Item* %4, align 4
  %"tmp.(Int, (Bool, Color), Item)3" = load { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %"tmp.(Int, (Bool, Color), Item)", align 4
  store { i32, { i1, i32 }, %Item } %"tmp.(Int, (Bool, Color), Item)3", { i32, { i1, i32 }, %Item }* %getComplexTuple-retval, align 4
  br label %return

return:                                           ; preds = %entry
  %getComplexTuple-retval4 = load { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %getComplexTuple-retval, align 4
  ret { i32, { i1, i32 }, %Item } %getComplexTuple-retval4
}

define void @printName(i32* %0) {
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

define i32 @main() {
entry:
  %i = alloca i32, align 4
  store i32 2, i32* %i, align 4
  %j = alloca { i32, { i1, i32 }, %Item }, align 8
  %getComplexTuple = call { i32, { i1, i32 }, %Item } @getComplexTuple()
  store { i32, { i1, i32 }, %Item } %getComplexTuple, { i32, { i1, i32 }, %Item }* %j, align 4
  %c = alloca i32, align 4
  store i32 0, i32* %c, align 4
  call void @printName(i32* %c)
  %print = load i32, i32* %i, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %"0" = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %j, i32 0, i32 0
  %print1 = load i32, i32* %"0", align 4
  %printcall2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print1)
  %"1" = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %j, i32 0, i32 1
  %"03" = getelementptr inbounds { i1, i32 }, { i1, i32 }* %"1", i32 0, i32 0
  %print4 = load i1, i1* %"03", align 1
  call void @printb(i1 %print4)
  %"15" = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %j, i32 0, i32 1
  %"16" = getelementptr inbounds { i1, i32 }, { i1, i32 }* %"15", i32 0, i32 1
  %print7 = load i32, i32* %"16", align 4
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.4, i32 0, i32 0), i32 %print7)
  %"2" = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %j, i32 0, i32 2
  %i9 = getelementptr inbounds %Item, %Item* %"2", i32 0, i32 0
  %print10 = load i32, i32* %i9, align 4
  %printcall11 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print10)
  %"212" = getelementptr inbounds { i32, { i1, i32 }, %Item }, { i32, { i1, i32 }, %Item }* %j, i32 0, i32 2
  %color = getelementptr inbounds %Item, %Item* %"212", i32 0, i32 1
  call void @printName(i32* %color)
  ret i32 0
}
