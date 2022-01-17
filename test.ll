; ModuleID = 'test'
source_filename = "test"

@formatStr = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @printf(i8*, ...)

define i32 @max(i32 %0, i32 %1) {
entry:
  %sgttmp = icmp sgt i32 %0, %1
  br i1 %sgttmp, label %if-true, label %if-false

if-true:                                          ; preds = %entry
  br label %end

if-false:                                         ; preds = %entry
  %eqtmp = icmp eq i32 %0, %1
  br i1 %eqtmp, label %if-true1, label %if-false2

end:                                              ; preds = %end3, %if-true
  %2 = phi i32 [ %0, %if-true ], [ %3, %end3 ]
  ret i32 %2

if-true1:                                         ; preds = %if-false
  br label %end3

if-false2:                                        ; preds = %if-false
  br label %end3

end3:                                             ; preds = %if-false2, %if-true1
  %3 = phi i32 [ %0, %if-true1 ], [ %1, %if-false2 ]
  br label %end
}

define void @puti(i32 %0) {
entry:
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %0)
  ret void
}

define i32 @main() {
entry:
  %i = alloca i32, align 4
  store i32 2, i32* %i, align 4
  %i1 = load i32, i32* %i, align 4
  %max = call i32 @max(i32 %i1, i32 3)
  call void @puti(i32 %max)
  ret i32 0
}
