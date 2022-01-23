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

define i32 @main({ i32, { i32, i8* }* } %0) {
entry:
  %.compoundliteral = alloca [12 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [12 x i8], [12 x i8]* %.compoundliteral, i64 0, i64 0
  store i8 72, i8* %arrayinit.begin, align 1
  %arrayinit.element = getelementptr inbounds i8, i8* %arrayinit.begin, i64 1
  store i8 101, i8* %arrayinit.element, align 1
  %arrayinit.element1 = getelementptr inbounds i8, i8* %arrayinit.element, i64 1
  store i8 108, i8* %arrayinit.element1, align 1
  %arrayinit.element2 = getelementptr inbounds i8, i8* %arrayinit.element1, i64 1
  store i8 108, i8* %arrayinit.element2, align 1
  %arrayinit.element3 = getelementptr inbounds i8, i8* %arrayinit.element2, i64 1
  store i8 111, i8* %arrayinit.element3, align 1
  %arrayinit.element4 = getelementptr inbounds i8, i8* %arrayinit.element3, i64 1
  store i8 32, i8* %arrayinit.element4, align 1
  %arrayinit.element5 = getelementptr inbounds i8, i8* %arrayinit.element4, i64 1
  store i8 119, i8* %arrayinit.element5, align 1
  %arrayinit.element6 = getelementptr inbounds i8, i8* %arrayinit.element5, i64 1
  store i8 111, i8* %arrayinit.element6, align 1
  %arrayinit.element7 = getelementptr inbounds i8, i8* %arrayinit.element6, i64 1
  store i8 114, i8* %arrayinit.element7, align 1
  %arrayinit.element8 = getelementptr inbounds i8, i8* %arrayinit.element7, i64 1
  store i8 108, i8* %arrayinit.element8, align 1
  %arrayinit.element9 = getelementptr inbounds i8, i8* %arrayinit.element8, i64 1
  store i8 100, i8* %arrayinit.element9, align 1
  %arraydecay = getelementptr inbounds [12 x i8], [12 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 11, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %size10 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  %1 = load i32, i32* %size10, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  %2 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.1, i32 0, i32 0), i32 %1, i8* %2)
  ret i32 0
}
