template <template <class, class> class ThreadingModel,
class MX >
struct RefCountedMTAdj {
    template <class P>
    class RefCountedMT : public ThreadingModel< RefCountedMT<P>, MX > {
        typedef ThreadingModel< RefCountedMT<P>, MX > base_type;
        typedef typename base_type::IntType CountType;
        typedef volatile CountType *CountPtrType;
    public:
        //MWCW lacks template friends, hence the following kludge
        template <typename P1>
        RefCountedMT(const RefCountedMT<P1>& rhs)
        : pCount_(reinterpret_cast<const RefCountedMT<P>&> (rhs).pCount_) {
        }
        P Clone(const P& val) {
            ThreadingModel<RefCountedMT, MX>::AtomicIncrement(*pCount_);
            return val;
        }
    private:
        // Data
        CountPtrType pCount_;
    };
};