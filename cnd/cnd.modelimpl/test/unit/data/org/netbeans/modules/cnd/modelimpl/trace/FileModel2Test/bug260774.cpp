namespace bug260774 {
    struct LogCallback260774 {
        virtual void sendLog(const char* log);
    };

    int main260774(int argc, char** argv) {
        class : public LogCallback260774 {
            virtual void sendLog(const char* log) {}
        } err;
        return 0;
    }
}
