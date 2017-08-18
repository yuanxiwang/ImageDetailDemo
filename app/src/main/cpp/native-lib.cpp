#include <jni.h>
#include <string>
#include <iostream>
#include <cmath>

using namespace std;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_imagedetaildemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//JNIEXPORT jint JNICALL
//Java_com_imagedetaildemo_MainActivity_add(JNIEnv *env, jobject instance, int a, int b) {
//    // TODO
//    int result = a + b;
//    return result;
//}

