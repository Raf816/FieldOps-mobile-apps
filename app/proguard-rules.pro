-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepclassmembers class com.raf.fieldops.data.model.User {
    <fields>;
    <init>(...);
}

-keepclassmembers class com.raf.fieldops.data.model.Job {
    <fields>;
    <init>(...);
}

-keepclassmembers class com.raf.fieldops.data.model.Note {
    <fields>;
    <init>(...);
}

-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}
