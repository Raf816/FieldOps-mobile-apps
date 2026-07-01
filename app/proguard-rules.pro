# ═══════════════════════════════════════════════════════════════════════
# FieldOps ProGuard / R8 Rules
# ═══════════════════════════════════════════════════════════════════════
#
# These rules prevent R8 from breaking the app in release builds.
# R8 performs shrinking (removing unused code), obfuscation (renaming),
# and optimisation. Without these rules, it would rename Firestore data
# class fields and strip Hilt constructors — breaking the app silently.
#
# Reference: Lecture 11 — Security (Build Pipeline & R8 Configuration)
# ═══════════════════════════════════════════════════════════════════════

# ── Preserve line numbers for crash reporting ────────────────────────
# Keeps source file names and line numbers in stack traces so crashes
# in production can be traced back to the original source code.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Firestore Data Classes ───────────────────────────────────────────
# Firestore's toObject() mapper uses reflection to map document fields
# to Kotlin property names. If R8 renames 'title' to 'a', Firestore
# can't find the field and deserialisation fails silently (returns null).
#
# We keep ALL fields and constructors for our data model classes.

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

# ── Hilt Dependency Injection ────────────────────────────────────────
# Hilt uses reflection to find @Inject constructors at runtime.
# R8 might strip these as "unused" since they're never called directly
# in application code — they're invoked by the generated Hilt components.

-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Hilt-generated components (prevents stripping of DI graph)
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Firebase ─────────────────────────────────────────────────────────
# Firebase SDK uses reflection internally. These rules prevent R8 from
# breaking Firebase Auth and Firestore operations.

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ── Kotlin Serialisation Safety ──────────────────────────────────────
# Prevents R8 from removing default parameter values in data classes
# (Firestore needs no-arg constructors for toObject() mapping).

-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}
