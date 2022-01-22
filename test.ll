; ModuleID = 'test'
source_filename = "test"

@str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@"Your favourite colour is red" = private unnamed_addr constant [29 x i8] c"Your favourite colour is red\00", align 1
@str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@"I don't know that colour" = private unnamed_addr constant [25 x i8] c"I don't know that colour\00", align 1
@str.2 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1

declare i32 @printf(i8*, ...)

define void @printb(i1 %0) {
entry:
  %cond = select i1 %0, i32 1, i32 0
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str, i32 0, i32 0), i32 %cond)
  ret void
}

define void @favouriteColour(i32 %0) {
entry:
  %1 = icmp eq i32 %0, 0
  br i1 %1, label %if.then, label %if.else

if.then:                                          ; preds = %entry
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.1, i32 0, i32 0), i8* getelementptr inbounds ([29 x i8], [29 x i8]* @"Your favourite colour is red", i32 0, i32 0))
  br label %if.end

if.else:                                          ; preds = %entry
  %printcall1 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @str.2, i32 0, i32 0), i8* getelementptr inbounds ([25 x i8], [25 x i8]* @"I don't know that colour", i32 0, i32 0))
  br label %if.end

if.end:                                           ; preds = %if.else, %if.then
  ret void
}

define i32 @main() {
entry:
  %c = alloca i32, align 4
  store i32 2, i32* %c, align 4
  %arg = load i32, i32* %c, align 4
  call void @favouriteColour(i32 %arg)
  ret i32 0
}
