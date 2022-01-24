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

define i32 @main() {
entry:
  %h = alloca { i32, i8* }, align 8
  %.compoundliteral = alloca [8 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [8 x i8], [8 x i8]* %.compoundliteral, i64 0, i64 0
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
  store i8 44, i8* %arrayinit.element4, align 1
  %arrayinit.element5 = getelementptr inbounds i8, i8* %arrayinit.element4, i64 1
  store i8 32, i8* %arrayinit.element5, align 1
  %arraydecay = getelementptr inbounds [8 x i8], [8 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 7, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %val = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %val, { i32, i8* }* %h, align 8
  %w = alloca { i32, i8* }, align 8
  %.compoundliteral6 = alloca [7 x i8], align 1
  %tmp.array.struct7 = alloca { i32, i8* }, align 8
  %arrayinit.begin8 = getelementptr inbounds [7 x i8], [7 x i8]* %.compoundliteral6, i64 0, i64 0
  store i8 87, i8* %arrayinit.begin8, align 1
  %arrayinit.element9 = getelementptr inbounds i8, i8* %arrayinit.begin8, i64 1
  store i8 111, i8* %arrayinit.element9, align 1
  %arrayinit.element10 = getelementptr inbounds i8, i8* %arrayinit.element9, i64 1
  store i8 114, i8* %arrayinit.element10, align 1
  %arrayinit.element11 = getelementptr inbounds i8, i8* %arrayinit.element10, i64 1
  store i8 108, i8* %arrayinit.element11, align 1
  %arrayinit.element12 = getelementptr inbounds i8, i8* %arrayinit.element11, i64 1
  store i8 100, i8* %arrayinit.element12, align 1
  %arrayinit.element13 = getelementptr inbounds i8, i8* %arrayinit.element12, i64 1
  store i8 33, i8* %arrayinit.element13, align 1
  %arraydecay14 = getelementptr inbounds [7 x i8], [7 x i8]* %.compoundliteral6, i64 0, i64 0
  %size15 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct7, i32 0, i32 0
  store i32 6, i32* %size15, align 4
  %data16 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct7, i32 0, i32 1
  store i8* %arraydecay14, i8** %data16, align 8
  %val17 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct7, align 8
  store { i32, i8* } %val17, { i32, i8* }* %w, align 8
  %size18 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %w, i32 0, i32 0
  %0 = load i32, i32* %size18, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %w, i32 0, i32 1
  %1 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.1, i32 0, i32 0), i32 %0, i8* %1)
  ret i32 0
}
