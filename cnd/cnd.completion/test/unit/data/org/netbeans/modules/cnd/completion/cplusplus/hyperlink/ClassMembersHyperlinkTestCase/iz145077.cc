/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

namespace iz145077 {
/////////////////////////////////////////////////////////////////////////
//
// Forward declarations.
//
/////////////////////////////////////////////////////////////////////////

class MediaObj;

class MediaObj {
public:
    class ScopeHold;
    class Persist;
    friend class Persist;

    class Persist {
      public:
        friend class ScopeHold;
        friend class MediaObj;

        Persist()
          : m_obj(0)
        {}

        Persist(Persist& p);
        ~Persist();

        Persist& operator=(MediaObj* obj);
        Persist& operator=(ScopeHold&);
        Persist& operator=(Persist&);

      private:  // private methods
        MediaObj* preserve();
        void replace(MediaObj* obj);

      private:  // private data
        MediaObj*               m_obj;
    };

    class ScopeHold {
      public:
        ScopeHold()                 { m_obj = 0; }
        ScopeHold(Persist& p)       { m_obj = p.preserve(); }
        ScopeHold(MediaObj* o)      { if (o != 0) o->preserve(); m_obj=o; }
        ~ScopeHold()                { if (m_obj != 0) m_obj->release(); }
        bool null()                 { return m_obj == 0; }
        bool valid()                { return ! null(); }
        MediaObj* get()             { return m_obj; }
        MediaObj* forget()          { MediaObj* o=m_obj; m_obj=0; return o; }
        MediaObj* operator->()      { return m_obj; }

        int release()
        {
            int count = (m_obj != 0 ? m_obj->release() : 0);
            m_obj = 0;
            return count;
        }

        ScopeHold& operator=(Persist& p)
        {
            MediaObj* newObj = p.preserve();
            if (m_obj != 0)
                m_obj->release();
            m_obj = newObj;
            return *this;
        }

        ScopeHold& operator=(MediaObj* newObj)
        {
            // we assume newObj will not go away
            if (newObj != 0)
                newObj->preserve();
            if (m_obj != 0)
                m_obj->release();
            m_obj = newObj;
            return *this;
        }

        ScopeHold(const ScopeHold& rhs)
        {
            if (this != &rhs) {
                m_obj = rhs.m_obj;
                if (m_obj != 0)
                    m_obj->preserve();
            }
        }

        ScopeHold& operator=(const ScopeHold& rhs)
        {
            // we can do this because rhs has an extra reference
            if (this != &rhs) {
                if (m_obj != 0)
                    m_obj->release();
                m_obj = rhs.m_obj;
                if (m_obj != 0)         //MD: rhs.m_obj==0 (e.g. file locked)
                    m_obj->preserve();
            }
            return *this;
        }

        MediaObj& operator*()
        {
            return *m_obj;
        }

      private:
        MediaObj* m_obj;
    };

public:
    // reference counters
    virtual int preserve();
    virtual int release();
};

class CClipFile {
private:
    MediaObj::Persist m_obj;

public:
    bool getTimecode();
};

bool CClipFile::getTimecode()
{
    MediaObj::ScopeHold hold(m_obj);

    if (hold.valid()) {
        return true;
    }
    CClipFile* a;
    function(a->m_obj ? a->m_obj : a->m_obj);
    return false;
}
void function(void* ) {

}

class BasicFileIO {
  public:
    struct ReadReq {
        void*   cb;         // specifies callback mechanism
        const void*         reqTag;     // specifies request-specific tag
    };
};

class BufferTypes {
public:
    typedef BasicFileIO::ReadReq ReadReq;
};

class IntBufferTypes : public BufferTypes {
public:
    class PendingReq {
    private:
        const void*   m_readCb;   // specifies callback mechanism
        const void*         m_readTag;  // specifies request-specific tag


    public:     // methods
        void workingSet(const ReadReq& req)
        {
            m_readCb = &req.cb;     // ide says ok
            m_readTag = req.reqTag; // ide says ok
        }
        void brokenSet(const ReadReq&);
    };
};

void IntBufferTypes::PendingReq::brokenSet(const ReadReq& req)
{
    m_readCb = &req.cb;     // ide says cb can't be resolved
    m_readTag = req.reqTag; // ide says reqTag can't be resolved
}
}