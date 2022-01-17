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
  br label %end

end:                                              ; preds = %if-false, %if-true
  %2 = phi i32 [ %0, %if-true ], [ %1, %if-false ]
  ret i32 %2
}

define i32 @main() {
entry:
  %max = call i32 @max(i32 2, i32 1)
  %printcall = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @formatStr, i32 0, i32 0), i32 %max)
  ret i32 0
}
