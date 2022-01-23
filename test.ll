; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@str.1 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define void @putsa({ i32, { i32, i8* }* } %0) {
entry:
  %array-length-177f2338-5b42-4eec-8b82-76f62fd957b0 = alloca i32, align 4
  %access.tmp = alloca { i32, { i32, i8* }* }, align 8
  store { i32, { i32, i8* }* } %0, { i32, { i32, i8* }* }* %access.tmp, align 8
  %size = getelementptr inbounds { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %access.tmp, i32 0, i32 0
  %val = load i32, i32* %size, align 4
  store i32 %val, i32* %array-length-177f2338-5b42-4eec-8b82-76f62fd957b0, align 4
  %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0 = alloca i32, align 4
  store i32 0, i32* %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0, align 4
  %s = alloca { i32, i8* }, align 8
  %access.tmp1 = alloca { i32, { i32, i8* }* }, align 8
  store { i32, { i32, i8* }* } %0, { i32, { i32, i8* }* }* %access.tmp1, align 8
  %arr = getelementptr inbounds { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %access.tmp1, i32 0, i32 1
  %arr2 = load { i32, i8* }*, { i32, i8* }** %arr, align 8
  %arrayidx = getelementptr inbounds { i32, i8* }, { i32, i8* }* %arr2, i32 0
  %1 = load { i32, i8* }, { i32, i8* }* %arrayidx, align 8
  store { i32, i8* } %1, { i32, i8* }* %s, align 8
  br label %while.cond

while.cond:                                       ; preds = %while.body, %entry
  %lhs = load i32, i32* %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0, align 4
  %rhs = load i32, i32* %array-length-177f2338-5b42-4eec-8b82-76f62fd957b0, align 4
  %2 = icmp slt i32 %lhs, %rhs
  br i1 %2, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %access.tmp3 = alloca { i32, { i32, i8* }* }, align 8
  store { i32, { i32, i8* }* } %0, { i32, { i32, i8* }* }* %access.tmp3, align 8
  %idx = load i32, i32* %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0, align 4
  %arr4 = getelementptr inbounds { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %access.tmp3, i32 0, i32 1
  %arr5 = load { i32, i8* }*, { i32, i8* }** %arr4, align 8
  %arrayidx6 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %arr5, i32 %idx
  %3 = load { i32, i8* }, { i32, i8* }* %arrayidx6, align 8
  store { i32, i8* } %3, { i32, i8* }* %s, align 8
  %size7 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 0
  %4 = load i32, i32* %size7, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 1
  %5 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.1, i32 0, i32 0), i32 %4, i8* %5)
  %load = load i32, i32* %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %iteration-counter-ceb9d9b8-fc75-49fa-8214-ef7126eadee0, align 4
  br label %while.cond

while.exit:                                       ; preds = %while.cond
  ret void
}

define i32 @main() {
entry:
  %s = alloca { i32, { i32, i8* }* }, align 8
  %.compoundliteral = alloca [2 x { i32, i8* }], align 8
  %tmp.array.struct = alloca { i32, { i32, i8* }* }, align 8
  %arrayinit.begin = getelementptr inbounds [2 x { i32, i8* }], [2 x { i32, i8* }]* %.compoundliteral, i64 0, i64 0
  %.compoundliteral1 = alloca [6 x i8], align 1
  %tmp.array.struct2 = alloca { i32, i8* }, align 8
  %arrayinit.begin3 = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral1, i64 0, i64 0
  store i8 72, i8* %arrayinit.begin3, align 1
  %arrayinit.element = getelementptr inbounds i8, i8* %arrayinit.begin3, i64 1
  store i8 101, i8* %arrayinit.element, align 1
  %arrayinit.element4 = getelementptr inbounds i8, i8* %arrayinit.element, i64 1
  store i8 108, i8* %arrayinit.element4, align 1
  %arrayinit.element5 = getelementptr inbounds i8, i8* %arrayinit.element4, i64 1
  store i8 108, i8* %arrayinit.element5, align 1
  %arrayinit.element6 = getelementptr inbounds i8, i8* %arrayinit.element5, i64 1
  store i8 111, i8* %arrayinit.element6, align 1
  %arraydecay = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral1, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct2, i32 0, i32 0
  store i32 5, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct2, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %0 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct2, align 8
  store { i32, i8* } %0, { i32, i8* }* %arrayinit.begin, align 8
  %arrayinit.element7 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %arrayinit.begin, i64 1
  %.compoundliteral8 = alloca [7 x i8], align 1
  %tmp.array.struct9 = alloca { i32, i8* }, align 8
  %arrayinit.begin10 = getelementptr inbounds [7 x i8], [7 x i8]* %.compoundliteral8, i64 0, i64 0
  store i8 87, i8* %arrayinit.begin10, align 1
  %arrayinit.element11 = getelementptr inbounds i8, i8* %arrayinit.begin10, i64 1
  store i8 111, i8* %arrayinit.element11, align 1
  %arrayinit.element12 = getelementptr inbounds i8, i8* %arrayinit.element11, i64 1
  store i8 114, i8* %arrayinit.element12, align 1
  %arrayinit.element13 = getelementptr inbounds i8, i8* %arrayinit.element12, i64 1
  store i8 108, i8* %arrayinit.element13, align 1
  %arrayinit.element14 = getelementptr inbounds i8, i8* %arrayinit.element13, i64 1
  store i8 100, i8* %arrayinit.element14, align 1
  %arrayinit.element15 = getelementptr inbounds i8, i8* %arrayinit.element14, i64 1
  store i8 33, i8* %arrayinit.element15, align 1
  %arraydecay16 = getelementptr inbounds [7 x i8], [7 x i8]* %.compoundliteral8, i64 0, i64 0
  %size17 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct9, i32 0, i32 0
  store i32 6, i32* %size17, align 4
  %data18 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct9, i32 0, i32 1
  store i8* %arraydecay16, i8** %data18, align 8
  %1 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct9, align 8
  store { i32, i8* } %1, { i32, i8* }* %arrayinit.element7, align 8
  %arraydecay19 = getelementptr inbounds [2 x { i32, i8* }], [2 x { i32, i8* }]* %.compoundliteral, i64 0, i64 0
  %size20 = getelementptr inbounds { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %tmp.array.struct, i32 0, i32 0
  store i32 2, i32* %size20, align 4
  %data21 = getelementptr inbounds { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %tmp.array.struct, i32 0, i32 1
  store { i32, i8* }* %arraydecay19, { i32, i8* }** %data21, align 8
  %val = load { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %tmp.array.struct, align 8
  store { i32, { i32, i8* }* } %val, { i32, { i32, i8* }* }* %s, align 8
  %arg = load { i32, { i32, i8* }* }, { i32, { i32, i8* }* }* %s, align 8
  call void @putsa({ i32, { i32, i8* }* } %arg)
  ret i32 0
}
