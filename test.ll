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

define i32 @main() {
entry:
  %arr = alloca { i32, i32* }, align 8
  %arr1 = alloca { i32, i32* }*, align 8
  %.compoundliteral = alloca [100 x i32], align 4
  %arrayinit.begin = getelementptr inbounds [100 x i32], [100 x i32]* %.compoundliteral, i64 0, i64 0
  store i32 80, i32* %arrayinit.begin, align 4
  %arrayinit.element = getelementptr inbounds i32, i32* %arrayinit.begin, i64 1
  store i32 42, i32* %arrayinit.element, align 4
  %arrayinit.element2 = getelementptr inbounds i32, i32* %arrayinit.element, i64 1
  store i32 34, i32* %arrayinit.element2, align 4
  %arrayinit.element3 = getelementptr inbounds i32, i32* %arrayinit.element2, i64 1
  store i32 7, i32* %arrayinit.element3, align 4
  %arrayinit.element4 = getelementptr inbounds i32, i32* %arrayinit.element3, i64 1
  store i32 95, i32* %arrayinit.element4, align 4
  %arrayinit.element5 = getelementptr inbounds i32, i32* %arrayinit.element4, i64 1
  store i32 77, i32* %arrayinit.element5, align 4
  %arrayinit.element6 = getelementptr inbounds i32, i32* %arrayinit.element5, i64 1
  store i32 85, i32* %arrayinit.element6, align 4
  %arrayinit.element7 = getelementptr inbounds i32, i32* %arrayinit.element6, i64 1
  store i32 18, i32* %arrayinit.element7, align 4
  %arrayinit.element8 = getelementptr inbounds i32, i32* %arrayinit.element7, i64 1
  store i32 2, i32* %arrayinit.element8, align 4
  %arrayinit.element9 = getelementptr inbounds i32, i32* %arrayinit.element8, i64 1
  store i32 11, i32* %arrayinit.element9, align 4
  %arrayinit.element10 = getelementptr inbounds i32, i32* %arrayinit.element9, i64 1
  store i32 31, i32* %arrayinit.element10, align 4
  %arrayinit.element11 = getelementptr inbounds i32, i32* %arrayinit.element10, i64 1
  store i32 25, i32* %arrayinit.element11, align 4
  %arrayinit.element12 = getelementptr inbounds i32, i32* %arrayinit.element11, i64 1
  store i32 61, i32* %arrayinit.element12, align 4
  %arrayinit.element13 = getelementptr inbounds i32, i32* %arrayinit.element12, i64 1
  store i32 1, i32* %arrayinit.element13, align 4
  %arrayinit.element14 = getelementptr inbounds i32, i32* %arrayinit.element13, i64 1
  store i32 75, i32* %arrayinit.element14, align 4
  %arrayinit.element15 = getelementptr inbounds i32, i32* %arrayinit.element14, i64 1
  store i32 84, i32* %arrayinit.element15, align 4
  %arrayinit.element16 = getelementptr inbounds i32, i32* %arrayinit.element15, i64 1
  store i32 82, i32* %arrayinit.element16, align 4
  %arrayinit.element17 = getelementptr inbounds i32, i32* %arrayinit.element16, i64 1
  store i32 49, i32* %arrayinit.element17, align 4
  %arrayinit.element18 = getelementptr inbounds i32, i32* %arrayinit.element17, i64 1
  store i32 9, i32* %arrayinit.element18, align 4
  %arrayinit.element19 = getelementptr inbounds i32, i32* %arrayinit.element18, i64 1
  store i32 83, i32* %arrayinit.element19, align 4
  %arrayinit.element20 = getelementptr inbounds i32, i32* %arrayinit.element19, i64 1
  store i32 72, i32* %arrayinit.element20, align 4
  %arrayinit.element21 = getelementptr inbounds i32, i32* %arrayinit.element20, i64 1
  store i32 67, i32* %arrayinit.element21, align 4
  %arrayinit.element22 = getelementptr inbounds i32, i32* %arrayinit.element21, i64 1
  store i32 59, i32* %arrayinit.element22, align 4
  %arrayinit.element23 = getelementptr inbounds i32, i32* %arrayinit.element22, i64 1
  store i32 92, i32* %arrayinit.element23, align 4
  %arrayinit.element24 = getelementptr inbounds i32, i32* %arrayinit.element23, i64 1
  store i32 21, i32* %arrayinit.element24, align 4
  %arrayinit.element25 = getelementptr inbounds i32, i32* %arrayinit.element24, i64 1
  store i32 51, i32* %arrayinit.element25, align 4
  %arrayinit.element26 = getelementptr inbounds i32, i32* %arrayinit.element25, i64 1
  store i32 90, i32* %arrayinit.element26, align 4
  %arrayinit.element27 = getelementptr inbounds i32, i32* %arrayinit.element26, i64 1
  store i32 28, i32* %arrayinit.element27, align 4
  %arrayinit.element28 = getelementptr inbounds i32, i32* %arrayinit.element27, i64 1
  store i32 16, i32* %arrayinit.element28, align 4
  %arrayinit.element29 = getelementptr inbounds i32, i32* %arrayinit.element28, i64 1
  store i32 62, i32* %arrayinit.element29, align 4
  %arrayinit.element30 = getelementptr inbounds i32, i32* %arrayinit.element29, i64 1
  store i32 48, i32* %arrayinit.element30, align 4
  %arrayinit.element31 = getelementptr inbounds i32, i32* %arrayinit.element30, i64 1
  store i32 73, i32* %arrayinit.element31, align 4
  %arrayinit.element32 = getelementptr inbounds i32, i32* %arrayinit.element31, i64 1
  store i32 50, i32* %arrayinit.element32, align 4
  %arrayinit.element33 = getelementptr inbounds i32, i32* %arrayinit.element32, i64 1
  store i32 96, i32* %arrayinit.element33, align 4
  %arrayinit.element34 = getelementptr inbounds i32, i32* %arrayinit.element33, i64 1
  store i32 93, i32* %arrayinit.element34, align 4
  %arrayinit.element35 = getelementptr inbounds i32, i32* %arrayinit.element34, i64 1
  store i32 22, i32* %arrayinit.element35, align 4
  %arrayinit.element36 = getelementptr inbounds i32, i32* %arrayinit.element35, i64 1
  store i32 89, i32* %arrayinit.element36, align 4
  %arrayinit.element37 = getelementptr inbounds i32, i32* %arrayinit.element36, i64 1
  store i32 14, i32* %arrayinit.element37, align 4
  %arrayinit.element38 = getelementptr inbounds i32, i32* %arrayinit.element37, i64 1
  store i32 76, i32* %arrayinit.element38, align 4
  %arrayinit.element39 = getelementptr inbounds i32, i32* %arrayinit.element38, i64 1
  store i32 66, i32* %arrayinit.element39, align 4
  %arrayinit.element40 = getelementptr inbounds i32, i32* %arrayinit.element39, i64 1
  store i32 44, i32* %arrayinit.element40, align 4
  %arrayinit.element41 = getelementptr inbounds i32, i32* %arrayinit.element40, i64 1
  store i32 19, i32* %arrayinit.element41, align 4
  %arrayinit.element42 = getelementptr inbounds i32, i32* %arrayinit.element41, i64 1
  store i32 26, i32* %arrayinit.element42, align 4
  %arrayinit.element43 = getelementptr inbounds i32, i32* %arrayinit.element42, i64 1
  store i32 43, i32* %arrayinit.element43, align 4
  %arrayinit.element44 = getelementptr inbounds i32, i32* %arrayinit.element43, i64 1
  store i32 81, i32* %arrayinit.element44, align 4
  %arrayinit.element45 = getelementptr inbounds i32, i32* %arrayinit.element44, i64 1
  store i32 53, i32* %arrayinit.element45, align 4
  %arrayinit.element46 = getelementptr inbounds i32, i32* %arrayinit.element45, i64 1
  store i32 24, i32* %arrayinit.element46, align 4
  %arrayinit.element47 = getelementptr inbounds i32, i32* %arrayinit.element46, i64 1
  store i32 13, i32* %arrayinit.element47, align 4
  %arrayinit.element48 = getelementptr inbounds i32, i32* %arrayinit.element47, i64 1
  store i32 65, i32* %arrayinit.element48, align 4
  %arrayinit.element49 = getelementptr inbounds i32, i32* %arrayinit.element48, i64 1
  store i32 39, i32* %arrayinit.element49, align 4
  %arrayinit.element50 = getelementptr inbounds i32, i32* %arrayinit.element49, i64 1
  store i32 99, i32* %arrayinit.element50, align 4
  %arrayinit.element51 = getelementptr inbounds i32, i32* %arrayinit.element50, i64 1
  store i32 55, i32* %arrayinit.element51, align 4
  %arrayinit.element52 = getelementptr inbounds i32, i32* %arrayinit.element51, i64 1
  store i32 78, i32* %arrayinit.element52, align 4
  %arrayinit.element53 = getelementptr inbounds i32, i32* %arrayinit.element52, i64 1
  store i32 98, i32* %arrayinit.element53, align 4
  %arrayinit.element54 = getelementptr inbounds i32, i32* %arrayinit.element53, i64 1
  store i32 60, i32* %arrayinit.element54, align 4
  %arrayinit.element55 = getelementptr inbounds i32, i32* %arrayinit.element54, i64 1
  store i32 64, i32* %arrayinit.element55, align 4
  %arrayinit.element56 = getelementptr inbounds i32, i32* %arrayinit.element55, i64 1
  store i32 15, i32* %arrayinit.element56, align 4
  %arrayinit.element57 = getelementptr inbounds i32, i32* %arrayinit.element56, i64 1
  store i32 56, i32* %arrayinit.element57, align 4
  %arrayinit.element58 = getelementptr inbounds i32, i32* %arrayinit.element57, i64 1
  store i32 71, i32* %arrayinit.element58, align 4
  %arrayinit.element59 = getelementptr inbounds i32, i32* %arrayinit.element58, i64 1
  store i32 87, i32* %arrayinit.element59, align 4
  %arrayinit.element60 = getelementptr inbounds i32, i32* %arrayinit.element59, i64 1
  store i32 58, i32* %arrayinit.element60, align 4
  %arrayinit.element61 = getelementptr inbounds i32, i32* %arrayinit.element60, i64 1
  store i32 100, i32* %arrayinit.element61, align 4
  %arrayinit.element62 = getelementptr inbounds i32, i32* %arrayinit.element61, i64 1
  store i32 63, i32* %arrayinit.element62, align 4
  %arrayinit.element63 = getelementptr inbounds i32, i32* %arrayinit.element62, i64 1
  store i32 46, i32* %arrayinit.element63, align 4
  %arrayinit.element64 = getelementptr inbounds i32, i32* %arrayinit.element63, i64 1
  store i32 35, i32* %arrayinit.element64, align 4
  %arrayinit.element65 = getelementptr inbounds i32, i32* %arrayinit.element64, i64 1
  store i32 8, i32* %arrayinit.element65, align 4
  %arrayinit.element66 = getelementptr inbounds i32, i32* %arrayinit.element65, i64 1
  store i32 23, i32* %arrayinit.element66, align 4
  %arrayinit.element67 = getelementptr inbounds i32, i32* %arrayinit.element66, i64 1
  store i32 91, i32* %arrayinit.element67, align 4
  %arrayinit.element68 = getelementptr inbounds i32, i32* %arrayinit.element67, i64 1
  store i32 10, i32* %arrayinit.element68, align 4
  %arrayinit.element69 = getelementptr inbounds i32, i32* %arrayinit.element68, i64 1
  store i32 4, i32* %arrayinit.element69, align 4
  %arrayinit.element70 = getelementptr inbounds i32, i32* %arrayinit.element69, i64 1
  store i32 47, i32* %arrayinit.element70, align 4
  %arrayinit.element71 = getelementptr inbounds i32, i32* %arrayinit.element70, i64 1
  store i32 6, i32* %arrayinit.element71, align 4
  %arrayinit.element72 = getelementptr inbounds i32, i32* %arrayinit.element71, i64 1
  store i32 29, i32* %arrayinit.element72, align 4
  %arrayinit.element73 = getelementptr inbounds i32, i32* %arrayinit.element72, i64 1
  store i32 33, i32* %arrayinit.element73, align 4
  %arrayinit.element74 = getelementptr inbounds i32, i32* %arrayinit.element73, i64 1
  store i32 12, i32* %arrayinit.element74, align 4
  %arrayinit.element75 = getelementptr inbounds i32, i32* %arrayinit.element74, i64 1
  store i32 97, i32* %arrayinit.element75, align 4
  %arrayinit.element76 = getelementptr inbounds i32, i32* %arrayinit.element75, i64 1
  store i32 54, i32* %arrayinit.element76, align 4
  %arrayinit.element77 = getelementptr inbounds i32, i32* %arrayinit.element76, i64 1
  store i32 30, i32* %arrayinit.element77, align 4
  %arrayinit.element78 = getelementptr inbounds i32, i32* %arrayinit.element77, i64 1
  store i32 32, i32* %arrayinit.element78, align 4
  %arrayinit.element79 = getelementptr inbounds i32, i32* %arrayinit.element78, i64 1
  store i32 38, i32* %arrayinit.element79, align 4
  %arrayinit.element80 = getelementptr inbounds i32, i32* %arrayinit.element79, i64 1
  store i32 45, i32* %arrayinit.element80, align 4
  %arrayinit.element81 = getelementptr inbounds i32, i32* %arrayinit.element80, i64 1
  store i32 94, i32* %arrayinit.element81, align 4
  %arrayinit.element82 = getelementptr inbounds i32, i32* %arrayinit.element81, i64 1
  store i32 20, i32* %arrayinit.element82, align 4
  %arrayinit.element83 = getelementptr inbounds i32, i32* %arrayinit.element82, i64 1
  store i32 40, i32* %arrayinit.element83, align 4
  %arrayinit.element84 = getelementptr inbounds i32, i32* %arrayinit.element83, i64 1
  store i32 17, i32* %arrayinit.element84, align 4
  %arrayinit.element85 = getelementptr inbounds i32, i32* %arrayinit.element84, i64 1
  store i32 88, i32* %arrayinit.element85, align 4
  %arrayinit.element86 = getelementptr inbounds i32, i32* %arrayinit.element85, i64 1
  store i32 69, i32* %arrayinit.element86, align 4
  %arrayinit.element87 = getelementptr inbounds i32, i32* %arrayinit.element86, i64 1
  store i32 5, i32* %arrayinit.element87, align 4
  %arrayinit.element88 = getelementptr inbounds i32, i32* %arrayinit.element87, i64 1
  store i32 86, i32* %arrayinit.element88, align 4
  %arrayinit.element89 = getelementptr inbounds i32, i32* %arrayinit.element88, i64 1
  store i32 68, i32* %arrayinit.element89, align 4
  %arrayinit.element90 = getelementptr inbounds i32, i32* %arrayinit.element89, i64 1
  store i32 3, i32* %arrayinit.element90, align 4
  %arrayinit.element91 = getelementptr inbounds i32, i32* %arrayinit.element90, i64 1
  store i32 79, i32* %arrayinit.element91, align 4
  %arrayinit.element92 = getelementptr inbounds i32, i32* %arrayinit.element91, i64 1
  store i32 27, i32* %arrayinit.element92, align 4
  %arrayinit.element93 = getelementptr inbounds i32, i32* %arrayinit.element92, i64 1
  store i32 41, i32* %arrayinit.element93, align 4
  %arrayinit.element94 = getelementptr inbounds i32, i32* %arrayinit.element93, i64 1
  store i32 52, i32* %arrayinit.element94, align 4
  %arrayinit.element95 = getelementptr inbounds i32, i32* %arrayinit.element94, i64 1
  store i32 70, i32* %arrayinit.element95, align 4
  %arrayinit.element96 = getelementptr inbounds i32, i32* %arrayinit.element95, i64 1
  store i32 36, i32* %arrayinit.element96, align 4
  %arrayinit.element97 = getelementptr inbounds i32, i32* %arrayinit.element96, i64 1
  store i32 74, i32* %arrayinit.element97, align 4
  %arrayinit.element98 = getelementptr inbounds i32, i32* %arrayinit.element97, i64 1
  store i32 37, i32* %arrayinit.element98, align 4
  %arrayinit.element99 = getelementptr inbounds i32, i32* %arrayinit.element98, i64 1
  store i32 57, i32* %arrayinit.element99, align 4
  %arraydecay = getelementptr inbounds [100 x i32], [100 x i32]* %.compoundliteral, i64 0, i64 0
  %tmp.array.struct = alloca { i32, i32* }, align 8
  %size = getelementptr inbounds { i32, i32* }, { i32, i32* }* %tmp.array.struct, i32 0, i32 0
  store i32 100, i32* %size, align 4
  %data = getelementptr inbounds { i32, i32* }, { i32, i32* }* %tmp.array.struct, i32 0, i32 1
  store i32* %arraydecay, i32** %data, align 8
  %val = load { i32, i32* }, { i32, i32* }* %tmp.array.struct, align 8
  store { i32, i32* } %val, { i32, i32* }* %arr, align 8
  call void @sort({ i32, i32* }* %arr)
  %array-length-45fa5e3b-61fd-40bc-bcb2-cc5bdabe2533 = alloca i32, align 4
  %size100 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %arr, i32 0, i32 0
  %val101 = load i32, i32* %size100, align 4
  store i32 %val101, i32* %array-length-45fa5e3b-61fd-40bc-bcb2-cc5bdabe2533, align 4
  %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d = alloca i32, align 4
  store i32 0, i32* %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d, align 4
  %i = alloca i32, align 4
  %arr102 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %arr, i32 0, i32 1
  %arr103 = load i32*, i32** %arr102, align 8
  %arrayidx = getelementptr inbounds i32, i32* %arr103, i32 0
  %0 = load i32, i32* %arrayidx, align 4
  store i32 %0, i32* %i, align 4
  br label %while.cond

while.cond:                                       ; preds = %while.body, %entry
  %lhs = load i32, i32* %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d, align 4
  %rhs = load i32, i32* %array-length-45fa5e3b-61fd-40bc-bcb2-cc5bdabe2533, align 4
  %1 = icmp slt i32 %lhs, %rhs
  br i1 %1, label %while.body, label %while.exit

while.body:                                       ; preds = %while.cond
  %idx = load i32, i32* %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d, align 4
  %arr104 = getelementptr inbounds { i32, i32* }, { i32, i32* }* %arr, i32 0, i32 1
  %arr105 = load i32*, i32** %arr104, align 8
  %arrayidx106 = getelementptr inbounds i32, i32* %arr105, i32 %idx
  %2 = load i32, i32* %arrayidx106, align 4
  store i32 %2, i32* %i, align 4
  %print = load i32, i32* %i, align 4
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %print)
  %load = load i32, i32* %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d, align 4
  %incrtmp = add i32 %load, 1
  store i32 %incrtmp, i32* %iteration-counter-85892fd8-9f82-4e7e-ae44-731c0488b45d, align 4
  br label %while.cond

while.exit:                                       ; preds = %while.cond
  ret i32 0
}
