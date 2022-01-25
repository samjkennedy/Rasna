; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@str.1 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1
@str.2 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1
@str.3 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1

declare i32 @printf(i8*, ...)

declare i16 @snprintf(i8*, i32, i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define i32 @main() {
entry:
  %h = alloca { i32, i8* }, align 8
  %.compoundliteral = alloca [5 x i8], align 1
  %tmp.array.struct = alloca { i32, i8* }, align 8
  %arrayinit.begin = getelementptr inbounds [5 x i8], [5 x i8]* %.compoundliteral, i64 0, i64 0
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
  store i8 0, i8* %arrayinit.element4, align 1
  %arraydecay = getelementptr inbounds [5 x i8], [5 x i8]* %.compoundliteral, i64 0, i64 0
  %size = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 0
  store i32 5, i32* %size, align 4
  %data = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct, i32 0, i32 1
  store i8* %arraydecay, i8** %data, align 8
  %val = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %val, { i32, i8* }* %h, align 8
  %w = alloca { i32, i8* }, align 8
  %.compoundliteral5 = alloca [5 x i8], align 1
  %tmp.array.struct6 = alloca { i32, i8* }, align 8
  %arrayinit.begin7 = getelementptr inbounds [5 x i8], [5 x i8]* %.compoundliteral5, i64 0, i64 0
  store i8 87, i8* %arrayinit.begin7, align 1
  %arrayinit.element8 = getelementptr inbounds i8, i8* %arrayinit.begin7, i64 1
  store i8 111, i8* %arrayinit.element8, align 1
  %arrayinit.element9 = getelementptr inbounds i8, i8* %arrayinit.element8, i64 1
  store i8 114, i8* %arrayinit.element9, align 1
  %arrayinit.element10 = getelementptr inbounds i8, i8* %arrayinit.element9, i64 1
  store i8 108, i8* %arrayinit.element10, align 1
  %arrayinit.element11 = getelementptr inbounds i8, i8* %arrayinit.element10, i64 1
  store i8 100, i8* %arrayinit.element11, align 1
  %arrayinit.element12 = getelementptr inbounds i8, i8* %arrayinit.element11, i64 1
  store i8 0, i8* %arrayinit.element12, align 1
  %arraydecay13 = getelementptr inbounds [5 x i8], [5 x i8]* %.compoundliteral5, i64 0, i64 0
  %size14 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct6, i32 0, i32 0
  store i32 5, i32* %size14, align 4
  %data15 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct6, i32 0, i32 1
  store i8* %arraydecay13, i8** %data15, align 8
  %val16 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct6, align 8
  store { i32, i8* } %val16, { i32, i8* }* %w, align 8
  %size17 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %h, i32 0, i32 0
  %0 = load i32, i32* %size17, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %h, i32 0, i32 1
  %1 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.1, i32 0, i32 0), i32 %0, i8* %1)
  %size18 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %w, i32 0, i32 0
  %2 = load i32, i32* %size18, align 4
  %string19 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %w, i32 0, i32 1
  %3 = load i8*, i8** %string19, align 8
  %printcall20 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.2, i32 0, i32 0), i32 %2, i8* %3)
  %arr = getelementptr inbounds { i32, i8* }, { i32, i8* }* %h, i32 0, i32 1
  %arr21 = load i8*, i8** %arr, align 8
  %arrayidx = getelementptr inbounds i8, i8* %arr21, i32 0
  store i8 89, i8* %arrayidx, align 1
  %size22 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %h, i32 0, i32 0
  %4 = load i32, i32* %size22, align 4
  %string23 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %h, i32 0, i32 1
  %5 = load i8*, i8** %string23, align 8
  %printcall24 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.3, i32 0, i32 0), i32 %4, i8* %5)
  ret i32 0
}
