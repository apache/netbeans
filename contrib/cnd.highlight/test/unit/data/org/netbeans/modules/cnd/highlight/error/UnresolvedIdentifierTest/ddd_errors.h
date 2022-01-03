
struct Command
{
    int priority;		// Priority (highest get executed first)
};

template<class E>
struct QueueRec {
    E elem;
    QueueRec<E> *next;
};

template<class E>
class QueueIter {
private:
    QueueRec<E> *rec;
public:
    const E& operator()() const { return rec->elem; }
    E& operator()()             { return rec->elem; }

    bool ok() const { return rec != 0; }
    QueueIter<E> next() const { return QueueIter<E>(rec->next); }
};
