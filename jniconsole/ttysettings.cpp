#ifdef __cplusplus
extern "C" {
#endif

/*
 * Copyright (c) 2007 Juraj Bednar.  All rights reserved.
 * Copyright (c) 2001 Markus Friedl.  All rights reserved.
 * Copyright (c) 2001 Kevin Steves.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "jniconsole.h"
#include <termios.h>
/* #include <pty.h> */
#ifdef MACOSX
#include <sys/ttycom.h>
#endif
#include <string.h>
#include <sys/ioctl.h>
#include <sys/signal.h>



static struct termios _saved_tio;
static int _in_raw_mode = 0;
static struct winsize ws;

// should be sig_atomic_t type, but mac os x does not have it
static volatile int received_window_change_signal = 0;

struct termios
get_saved_tio(void)
{
        return _saved_tio;
}

void
leave_raw_mode(void)
{
        if (!_in_raw_mode)
                return;
        if (tcsetattr(fileno(stdin), TCSADRAIN, &_saved_tio) == -1)
                perror("tcsetattr");
        else
                _in_raw_mode = 0;
}

void
update_window_size(void)
{
	/* get window size */
	if (ioctl(fileno(stdin), TIOCGWINSZ, &ws) < 0)
		memset(&ws, 0, sizeof(ws));
}

void
enter_raw_mode(void)
{
        struct termios tio;

	update_window_size();

        if (tcgetattr(fileno(stdin), &tio) == -1) {
                perror("tcgetattr");
                return;
        }
        _saved_tio = tio;
        tio.c_iflag |= IGNPAR;
        tio.c_iflag &= ~(ISTRIP | INLCR | IGNCR | ICRNL | IXON | IXANY | IXOFF);
#ifdef IUCLC
        tio.c_iflag &= ~IUCLC;
#endif
        tio.c_lflag &= ~(ISIG | ICANON | ECHO | ECHOE | ECHOK | ECHONL);
#ifdef IEXTEN
        tio.c_lflag &= ~IEXTEN;
#endif
        tio.c_oflag &= ~OPOST;
        tio.c_cc[VMIN] = 1;
        tio.c_cc[VTIME] = 0;
        if (tcsetattr(fileno(stdin), TCSADRAIN, &tio) == -1)
                perror("tcsetattr");
        else
                _in_raw_mode = 1;

}


int get_ws_row() {
                return ws.ws_row;
}

int get_ws_col() {
		return ws.ws_col;
}

int get_ws_xpixel() {
                return ws.ws_xpixel;
}

int get_ws_ypixel() {
                return ws.ws_ypixel;
}

/*
 * Signal handler for the window change signal (SIGWINCH).  This just sets a
 * flag indicating that the window has changed.
 */

static void
window_change_handler(int sig)
{
        received_window_change_signal = 1;
        signal(SIGWINCH, window_change_handler);
}

void install_window_change_handler() {
        signal(SIGWINCH, window_change_handler);
}

int is_window_size_changed() {
	int toret = 0;
	if (received_window_change_signal) {
		toret = 1;
		received_window_change_signal = 0;
	}
	return toret;
}

#ifdef __cplusplus
}
#endif

