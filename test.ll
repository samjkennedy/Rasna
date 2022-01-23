; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@real = private unnamed_addr constant [4 x i8] c"%c\0A\00", align 1
@real.1 = private unnamed_addr constant [4 x i8] c"%c\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i32 @main() {
entry:
  %s = alloca { i32, i8* }, align 8
  %.compoundliteral = alloca [6 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral, i64 0, i64 0
  store i8 72, i8* %arrayinit.begin, align 1
  %arrayinit.element = getelementptr inbounds i8, i8* %arrayinit.begin, i64 1
  store i8 101, i8* %arrayinit.element, align 1
  %arrayinit.element1 = getelementptr inbounds i8, i8* %arrayinit.element, i64 1
  store i8 108, i8* %arrayinit.element1, align 1
  %arrayinit.element2 = getelementptr inbounds i8, i8* %arrayinit.element1, i64 1
  store i8 108, i8* %arrayinit.element2, align 1
  %arrayinit.element3 = getelementptr inbounds i8, i8* %arrayinit.element2, i64 1
  store i8 111, i8* %arrayinit.element3, align 1
  %arraydecay = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 5, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %val = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %val, { i32, i8* }* %s, align 8
  %chars = alloca { i32, i8* }, align 8
  %val4 = load { i32, i8* }, { i32, i8* }* %s, align 8
  store { i32, i8* } %val4, { i32, i8* }* %chars, align 8
  %array-length-972c9f9a-6470-4612-8555-c3a4dbc7da4b = alloca i32, align 4
  %size5 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 0
  %val6 = load i32, i32* %size5, align 4
  store i32 %val6, i32* %array-length-972c9f9a-6470-4612-8555-c3a4dbc7da4b, align 4
  %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006 = alloca i32, align 4
  store i32 0, i32* %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006, align 4
  %c = alloca i8, align 1
  %arr = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 1
  %arr7 = load i8*, i8** %arr, align 8
  %arrayidx = getelementptr inbounds i8, i8* %arr7, i32 0
  %0 = load i8, i8* %arrayidx, align 1
  store i8 %0, i8* %c, align 1
  br label %while.cond

while.cond:                                       ; preds = %while.body, %entry
  %lhs = load i32, i32* %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006, align 4
  %rhs = load i32, i32* %array-length-972c9f9a-6470-4612-8555-c3a4dbc7da4b, align 4
  %1 = icmp slt i32 %lhs, %rhs
  br i1 %1, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %idx = load i32, i32* %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006, align 4
  %arr8 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 1
  %arr9 = load i8*, i8** %arr8, align 8
  %arrayidx10 = getelementptr inbounds i8, i8* %arr9, i32 %idx
  %2 = load i8, i8* %arrayidx10, align 1
  store i8 %2, i8* %c, align 1
  %print = load i8, i8* %c, align 1
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real, i32 0, i32 0), i8 %print)
  %load = load i32, i32* %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %iteration-counter-f86bf3aa-af22-4bac-a2b9-bee6b701c006, align 4
  br label %while.cond

while.exit:                                       ; preds = %while.cond
  %array-length-9dae7c61-d040-4d6f-9127-533962e5a6fd = alloca i32, align 4
  %size11 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %chars, i32 0, i32 0
  %val12 = load i32, i32* %size11, align 4
  store i32 %val12, i32* %array-length-9dae7c61-d040-4d6f-9127-533962e5a6fd, align 4
  %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8 = alloca i32, align 4
  store i32 0, i32* %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8, align 4
  %char = alloca i8, align 1
  %arr13 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %chars, i32 0, i32 1
  %arr14 = load i8*, i8** %arr13, align 8
  %arrayidx15 = getelementptr inbounds i8, i8* %arr14, i32 0
  %3 = load i8, i8* %arrayidx15, align 1
  store i8 %3, i8* %char, align 1
  br label %while.cond16

while.cond16:                                     ; preds = %while.body17, %while.exit
  %lhs19 = load i32, i32* %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8, align 4
  %rhs20 = load i32, i32* %array-length-9dae7c61-d040-4d6f-9127-533962e5a6fd, align 4
  %4 = icmp slt i32 %lhs19, %rhs20
  br i1 %4, label %while.body17, label %while.exit18

while.body17:                                     ; preds = %while.cond16
  %idx21 = load i32, i32* %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8, align 4
  %arr22 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %chars, i32 0, i32 1
  %arr23 = load i8*, i8** %arr22, align 8
  %arrayidx24 = getelementptr inbounds i8, i8* %arr23, i32 %idx21
  %5 = load i8, i8* %arrayidx24, align 1
  store i8 %5, i8* %char, align 1
  %print25 = load i8, i8* %char, align 1
  %printcall26 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @real.1, i32 0, i32 0), i8 %print25)
  %load27 = load i32, i32* %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8, align 4
  %incrtmp28 = add i32 %load27, 1
  store i32 %incrtmp28, i32* %iteration-counter-a9c145f7-28ec-4f6d-8f24-dcfa95a620c8, align 4
  br label %while.cond16

while.exit18:                                     ; preds = %while.cond16
  ret i32 0
}
