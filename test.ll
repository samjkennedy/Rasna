; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1

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
  %.compoundliteral = alloca [13 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [13 x i8], [13 x i8]* %.compoundliteral, i64 0, i64 0
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
  %arrayinit.element6 = getelementptr inbounds i8, i8* %arrayinit.element5, i64 1
  store i8 87, i8* %arrayinit.element6, align 1
  %arrayinit.element7 = getelementptr inbounds i8, i8* %arrayinit.element6, i64 1
  store i8 111, i8* %arrayinit.element7, align 1
  %arrayinit.element8 = getelementptr inbounds i8, i8* %arrayinit.element7, i64 1
  store i8 114, i8* %arrayinit.element8, align 1
  %arrayinit.element9 = getelementptr inbounds i8, i8* %arrayinit.element8, i64 1
  store i8 108, i8* %arrayinit.element9, align 1
  %arrayinit.element10 = getelementptr inbounds i8, i8* %arrayinit.element9, i64 1
  store i8 100, i8* %arrayinit.element10, align 1
  %arrayinit.element11 = getelementptr inbounds i8, i8* %arrayinit.element10, i64 1
  store i8 33, i8* %arrayinit.element11, align 1
  %arraydecay = getelementptr inbounds [13 x i8], [13 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 13, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %val = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %val, { i32, i8* }* %s, align 8
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 1
  %0 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.1, i32 0, i32 0), i8* %0)
  ret i32 0
}
