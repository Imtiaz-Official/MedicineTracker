#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_medicinetracker_util_AssetEncryptionUtil_getNativeKey(JNIEnv* env, jobject /* this */) {
    // Hidden Key
    std::string key = "MedicineTrackerD";
    return env->NewStringUTF(key.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_medicinetracker_util_AssetEncryptionUtil_getNativeIV(JNIEnv* env, jobject /* this */) {
    // Hidden IV
    std::string iv = "DataIV1234567890";
    return env->NewStringUTF(iv.c_str());
}
