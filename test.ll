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

define i32 @main() {
entry:
  %xs = alloca { i32, i32* }, align 8
  %arr = alloca { i32, i32* }*, align 8
  %.compoundliteral = alloca [4 x i32], align 4
  %arrayinit.begin = getelementptr inbounds [4 x i32], [4 x i32]* %.compoundliteral, i64 0, i64 0
  store i32 0, i32* %arrayinit.begin, align 4
  %arrayinit.element = getelementptr inbounds i32, i32* %arrayinit.begin, i64 1
  store i32 1, i32* %arrayinit.element, align 4
  %arrayinit.element1 = getelementptr inbounds i32, i32* %arrayinit.element, i64 1
  store i32 2, i32* %arrayinit.element1, align 4
  %arrayinit.element2 = getelementptr inbounds i32, i32* %arrayinit.element1, i64 1
  store i32 3, i32* %arrayinit.element2, align 4
  %arraydecay = getelementptr inbounds [4 x i32], [4 x i32]* %.compoundliteral, i64 0, i64 0
  %tmp.array.struct = alloca { i32, i32* }, align 8
  %size = getelementptr inbounds { i32, i32* }, { i32, i32* }* %tmp.array.struct, i32 0, i32 0
  store i32 4, i32* %size, align 4
  %data = getelementptr inbounds { i32, i32* }, { i32, i32* }* %tmp.array.struct, i32 0, i32 1
  store i32* %arraydecay, i32** %data, align 8
  %val = load { i32, i32* }, { i32, i32* }* %tmp.array.struct, align 8
  store { i32, i32* } %val, { i32, i32* }* %xs, align 8
  %arr3 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 1
  %arr4 = load i32*, i32** %arr3, align 8
  %arrayidx = getelementptr inbounds i32, i32* %arr4, i32 0
  %0 = load i32, i32* %arrayidx, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
  %arr5 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 1
  %arr6 = load i32*, i32** %arr5, align 8
  %arrayidx7 = getelementptr inbounds i32, i32* %arr6, i32 1
  %1 = load i32, i32* %arrayidx7, align 4
  %printcall8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %1)
  %arr9 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 1
  %arr10 = load i32*, i32** %arr9, align 8
  %arrayidx11 = getelementptr inbounds i32, i32* %arr10, i32 2
  %2 = load i32, i32* %arrayidx11, align 4
  %printcall12 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %2)
  %size13 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 0
  %print = load i32, i32* %size13, align 4
  %printcall14 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %array-length-3770d055-46bc-4369-8bd0-a83dea036d6b = alloca i32, align 4
  %size15 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 0
  %val16 = load i32, i32* %size15, align 4
  store i32 %val16, i32* %array-length-3770d055-46bc-4369-8bd0-a83dea036d6b, align 4
  %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5 = alloca i32, align 4
  store i32 0, i32* %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5, align 4
  %x = alloca i32, align 4
  %arr17 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 1
  %arr18 = load i32*, i32** %arr17, align 8
  %arrayidx19 = getelementptr inbounds i32, i32* %arr18, i32 0
  %3 = load i32, i32* %arrayidx19, align 4
  store i32 %3, i32* %x, align 4
  br label %while.cond

while.cond:                                       ; preds = %while.body, %entry
  %lhs = load i32, i32* %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5, align 4
  %rhs = load i32, i32* %array-length-3770d055-46bc-4369-8bd0-a83dea036d6b, align 4
  %4 = icmp slt i32 %lhs, %rhs
  br i1 %4, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %idx = load i32, i32* %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5, align 4
  %arr20 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %xs, i32 0, i32 1
  %arr21 = load i32*, i32** %arr20, align 8
  %arrayidx22 = getelementptr inbounds i32, i32* %arr21, i32 %idx
  %5 = load i32, i32* %arrayidx22, align 4
  store i32 %5, i32* %x, align 4
  %print23 = load i32, i32* %x, align 4
  %printcall24 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print23)
  %load = load i32, i32* %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %iteration-counter-d480bcf1-c146-4fd8-a19b-8612a6de3ba5, align 4
  br label %while.cond

while.exit:                                       ; preds = %while.cond
  ret i32 0
}
