
#ifndef INC211143_H
#define	INC211143_H

class Theme211143
{
    public:
        Theme211143() : m_windowManager(0) {}
        virtual ~Theme211143();

        void loadConfig();
        void saveConfig();

    private:
        int m_vars;

        void* m_windowManager;
};


#endif	/* INC211143_H */

