; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@str.1 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1
@str.2 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1
@str.3 = private unnamed_addr constant [6 x i8] c"%.*s\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define { { i32, i8* }, i1 } @multiReturn(i32 %0) {
entry:
  %multiReturn-retval = alloca { { i32, i8* }, i1 }, align 8
  %1 = icmp eq i32 %0, 1
  br i1 %1, label %if.then, label %if.else

return:                                           ; preds = %if.else, %if.then
  %multiReturn-retval17 = load { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %multiReturn-retval, align 8
  ret { { i32, i8* }, i1 } %multiReturn-retval17

if.then:                                          ; preds = %entry
  %"tmp.(String, Bool)" = alloca { { i32, i8* }, i1 }, align 8
  %2 = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)", i32 0, i32 0
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
  %3 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %3, { i32, i8* }* %2, align 8
  %4 = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)", i32 0, i32 1
  store i1 true, i1* %4, align 1
  %"tmp.(String, Bool)4" = load { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)", align 8
  store { { i32, i8* }, i1 } %"tmp.(String, Bool)4", { { i32, i8* }, i1 }* %multiReturn-retval, align 8
  br label %return

if.else:                                          ; preds = %entry
  %"tmp.(String, Bool)5" = alloca { { i32, i8* }, i1 }, align 8
  %5 = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)5", i32 0, i32 0
  %.compoundliteral6 = alloca [6 x i8], align 1
  %tmp.array.struct7 = alloca { i32, i8* }, align 8
  %arrayinit.begin8 = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral6, i64 0, i64 0
  store i8 87, i8* %arrayinit.begin8, align 1
  %arrayinit.element9 = getelementptr inbounds i8, i8* %arrayinit.begin8, i64 1
  store i8 111, i8* %arrayinit.element9, align 1
  %arrayinit.element10 = getelementptr inbounds i8, i8* %arrayinit.element9, i64 1
  store i8 114, i8* %arrayinit.element10, align 1
  %arrayinit.element11 = getelementptr inbounds i8, i8* %arrayinit.element10, i64 1
  store i8 108, i8* %arrayinit.element11, align 1
  %arrayinit.element12 = getelementptr inbounds i8, i8* %arrayinit.element11, i64 1
  store i8 100, i8* %arrayinit.element12, align 1
  %arraydecay13 = getelementptr inbounds [6 x i8], [6 x i8]* %.compoundliteral6, i64 0, i64 0
  %size14 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct7, i32 0, i32 0
  store i32 5, i32* %size14, align 4
  %data15 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %tmp.array.struct7, i32 0, i32 1
  store i8* %arraydecay13, i8** %data15, align 8
  %6 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct7, align 8
  store { i32, i8* } %6, { i32, i8* }* %5, align 8
  %7 = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)5", i32 0, i32 1
  store i1 false, i1* %7, align 1
  %"tmp.(String, Bool)16" = load { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %"tmp.(String, Bool)5", align 8
  store { { i32, i8* }, i1 } %"tmp.(String, Bool)16", { { i32, i8* }, i1 }* %multiReturn-retval, align 8
  br label %return
}

define i32 @main() {
entry:
  %t = alloca { { i32, i8* }, i32, i1 }, align 8
  %"tmp.(String, Int, Bool)" = alloca { { i32, i8* }, i32, i1 }, align 8
  %0 = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %"tmp.(String, Int, Bool)", i32 0, i32 0
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
  %1 = load { i32, i8* }, { i32, i8* }* %tmp.array.struct, align 8
  store { i32, i8* } %1, { i32, i8* }* %0, align 8
  %2 = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %"tmp.(String, Int, Bool)", i32 0, i32 1
  store i32 1, i32* %2, align 4
  %3 = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %"tmp.(String, Int, Bool)", i32 0, i32 2
  store i1 true, i1* %3, align 1
  %"tmp.(String, Int, Bool)4" = load { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %"tmp.(String, Int, Bool)", align 8
  store { { i32, i8* }, i32, i1 } %"tmp.(String, Int, Bool)4", { { i32, i8* }, i32, i1 }* %t, align 8
  %s = alloca { i32, i8* }, align 8
  %"0" = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %t, i32 0, i32 0
  %val = load { i32, i8* }, { i32, i8* }* %"0", align 8
  store { i32, i8* } %val, { i32, i8* }* %s, align 8
  %size5 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 0
  %4 = load i32, i32* %size5, align 4
  %string = getelementptr inbounds { i32, i8* }, { i32, i8* }* %s, i32 0, i32 1
  %5 = load i8*, i8** %string, align 8
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.1, i32 0, i32 0), i32 %4, i8* %5)
  %"06" = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %t, i32 0, i32 0
  %size7 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %"06", i32 0, i32 0
  %6 = load i32, i32* %size7, align 4
  %string8 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %"06", i32 0, i32 1
  %7 = load i8*, i8** %string8, align 8
  %printcall9 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.2, i32 0, i32 0), i32 %6, i8* %7)
  %"1" = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %t, i32 0, i32 1
  %print = load i32, i32* %"1", align 4
  %printcall10 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %"2" = getelementptr inbounds { { i32, i8* }, i32, i1 }, { { i32, i8* }, i32, i1 }* %t, i32 0, i32 2
  %print11 = load i1, i1* %"2", align 1
  call void @printb(i1 %print11)
  %sb = alloca { { i32, i8* }, i1 }, align 8
  %multiReturn = call { { i32, i8* }, i1 } @multiReturn(i32 2)
  store { { i32, i8* }, i1 } %multiReturn, { { i32, i8* }, i1 }* %sb, align 8
  %"012" = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %sb, i32 0, i32 0
  %size13 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %"012", i32 0, i32 0
  %8 = load i32, i32* %size13, align 4
  %string14 = getelementptr inbounds { i32, i8* }, { i32, i8* }* %"012", i32 0, i32 1
  %9 = load i8*, i8** %string14, align 8
  %printcall15 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @str.3, i32 0, i32 0), i32 %8, i8* %9)
  %"116" = getelementptr inbounds { { i32, i8* }, i1 }, { { i32, i8* }, i1 }* %sb, i32 0, i32 1
  %print17 = load i1, i1* %"116", align 1
  call void @printb(i1 %print17)
  %singleton = alloca { i32 }, align 8
  %"tmp.(Int)" = alloca { i32 }, align 8
  %10 = getelementptr inbounds { i32 }, { i32 }* %"tmp.(Int)", i32 0, i32 0
  store i32 10, i32* %10, align 4
  %"tmp.(Int)18" = load { i32 }, { i32 }* %"tmp.(Int)", align 4
  store { i32 } %"tmp.(Int)18", { i32 }* %singleton, align 4
  %"019" = getelementptr inbounds { i32 }, { i32 }* %singleton, i32 0, i32 0
  %print20 = load i32, i32* %"019", align 4
  %printcall21 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print20)
  %nested = alloca { i32, { i32, { i32, i1 } } }, align 8
  %"tmp.(Int, (Int, (Int, Bool)))" = alloca { i32, { i32, { i32, i1 } } }, align 8
  %11 = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %"tmp.(Int, (Int, (Int, Bool)))", i32 0, i32 0
  store i32 1, i32* %11, align 4
  %12 = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %"tmp.(Int, (Int, (Int, Bool)))", i32 0, i32 1
  %"tmp.(Int, (Int, Bool))" = alloca { i32, { i32, i1 } }, align 8
  %13 = getelementptr inbounds { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"tmp.(Int, (Int, Bool))", i32 0, i32 0
  store i32 2, i32* %13, align 4
  %14 = getelementptr inbounds { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"tmp.(Int, (Int, Bool))", i32 0, i32 1
  %"tmp.(Int, Bool)" = alloca { i32, i1 }, align 8
  %15 = getelementptr inbounds { i32, i1 }, { i32, i1 }* %"tmp.(Int, Bool)", i32 0, i32 0
  store i32 3, i32* %15, align 4
  %16 = getelementptr inbounds { i32, i1 }, { i32, i1 }* %"tmp.(Int, Bool)", i32 0, i32 1
  store i1 true, i1* %16, align 1
  %"tmp.(Int, Bool)22" = load { i32, i1 }, { i32, i1 }* %"tmp.(Int, Bool)", align 4
  store { i32, i1 } %"tmp.(Int, Bool)22", { i32, i1 }* %14, align 4
  %"tmp.(Int, (Int, Bool))23" = load { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"tmp.(Int, (Int, Bool))", align 4
  store { i32, { i32, i1 } } %"tmp.(Int, (Int, Bool))23", { i32, { i32, i1 } }* %12, align 4
  %"tmp.(Int, (Int, (Int, Bool)))24" = load { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %"tmp.(Int, (Int, (Int, Bool)))", align 4
  store { i32, { i32, { i32, i1 } } } %"tmp.(Int, (Int, (Int, Bool)))24", { i32, { i32, { i32, i1 } } }* %nested, align 4
  %"025" = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %nested, i32 0, i32 0
  %print26 = load i32, i32* %"025", align 4
  %printcall27 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print26)
  %"128" = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %nested, i32 0, i32 1
  %"029" = getelementptr inbounds { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"128", i32 0, i32 0
  %print30 = load i32, i32* %"029", align 4
  %printcall31 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print30)
  %"132" = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %nested, i32 0, i32 1
  %"133" = getelementptr inbounds { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"132", i32 0, i32 1
  %"034" = getelementptr inbounds { i32, i1 }, { i32, i1 }* %"133", i32 0, i32 0
  %print35 = load i32, i32* %"034", align 4
  %printcall36 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print35)
  %"137" = getelementptr inbounds { i32, { i32, { i32, i1 } } }, { i32, { i32, { i32, i1 } } }* %nested, i32 0, i32 1
  %"138" = getelementptr inbounds { i32, { i32, i1 } }, { i32, { i32, i1 } }* %"137", i32 0, i32 1
  %"139" = getelementptr inbounds { i32, i1 }, { i32, i1 }* %"138", i32 0, i32 1
  %print40 = load i1, i1* %"139", align 1
  call void @printb(i1 %print40)
  ret i32 0
}
