
#include "inc211143.h"

Theme211143::~Theme211143() {
    // Be sure things are destroyed in the right order (XXX check)
    m_vars = 10;
}

void Theme211143::loadConfig() {
    m_windowManager = 0;
}

void Theme211143::saveConfig() {
    m_vars = 111;
}
