#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_github_zhdhr0000_ipscan_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello World from C++";
    return env->NewStringUTF(hello.c_str());
}
