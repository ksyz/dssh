#ifdef __cplusplus
extern "C" {
#endif

#include "jniconsole.h"
#include "ttysettings.h"
#include <termios.h>

JNIEXPORT void JNICALL Java_dssh_Terminal_initConsole
  (JNIEnv *env, jobject obj)
{
        enter_raw_mode();
	install_window_change_handler();
}

JNIEXPORT jbyte JNICALL Java_dssh_Terminal_getCh
  (JNIEnv *env, jobject obj)
{
//        return (jbyte) 
}


JNIEXPORT void JNICALL Java_dssh_Terminal_finishConsole
  (JNIEnv *env, jobject obj)
{
        leave_raw_mode();
}


JNIEXPORT jint JNICALL Java_dssh_Terminal_getWsRow (JNIEnv *env, jobject obj) {
	return (jint) get_ws_row();
}

JNIEXPORT jint JNICALL Java_dssh_Terminal_getWsCol (JNIEnv *env, jobject obj) {
	return (jint) get_ws_col();
}

JNIEXPORT jint JNICALL Java_dssh_Terminal_getWsXPixel (JNIEnv *env, jobject obj) {
	return (jint) get_ws_xpixel();
}

JNIEXPORT jint JNICALL Java_dssh_Terminal_getWsYPixel (JNIEnv *env, jobject obj) {
	return (jint) get_ws_ypixel();
}

JNIEXPORT jboolean JNICALL Java_dssh_Terminal_shouldChangeWindowSize (JNIEnv *env, jobject obj) {
	if (! is_window_size_changed())
		return (jboolean) JNI_FALSE;
	else {
		update_window_size();
		return (jboolean) JNI_TRUE;
	}
	
}

#ifdef __cplusplus
}
#endif

