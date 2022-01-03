namespace {
    class Test
    {
        int test() const;
    };

    int Test::test() const try
    {
        return 0;
    }
    catch (...)
    {
    }
}